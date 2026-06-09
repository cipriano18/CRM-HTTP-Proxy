package proxy.core;

import filter.BlocklistManager;
import logger.ProxyLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import proxy.tls.SniExtractor;
import proxy.tls.TlsClientHelloReader;

public class ClientHandler implements Runnable {

    /**
     * Ruta de la pagina HTML que se devuelve cuando una solicitud
     * es bloqueada por las reglas del proxy.
     */
    private static final String BLOCKED_HTML_PATH =
            "src/main/resources/html/blocked.html";

    /**
     * Tamano maximo permitido para los encabezados HTTP leidos
     * desde el cliente.
     */
    private static final int HEADER_MAX_BYTES = 32768;

    /**
     * Puerto por defecto utilizado para conexiones HTTPS cuando
     * el cliente no lo especifica en CONNECT.
     */
    private static final int HTTPS_DEFAULT_PORT = 443;

    /**
     * Socket que representa la conexion del cliente actual.
     */
    private final Socket clientSocket;

    /**
     * Crea un manejador para una conexion entrante.
     *
     * @param clientSocket socket asociado al cliente.
     */
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    /**
     * Procesa una solicitud del cliente.
     *
     * <p>
     * Distingue entre trafico HTTP tradicional y tuneles HTTPS
     * mediante CONNECT, aplica las reglas de bloqueo y registra
     * el resultado en el log.
     * </p>
     */
    @Override
    public void run() {
        try {
            System.out.println("Procesando cliente: " + clientSocket.getInetAddress());
            clientSocket.setSoTimeout(10000);

            InputStream clientIn = clientSocket.getInputStream();

            String headerText = readHttpHeaderBlock(clientIn);
            if (headerText == null || headerText.isBlank()) {
                closeQuietly(clientSocket);
                return;
            }

            List<String> headerLines = splitHeaderLines(headerText);
            if (headerLines.isEmpty()) {
                closeQuietly(clientSocket);
                return;
            }

            String requestLine = headerLines.get(0);
            System.out.println("REQUEST: " + requestLine);

            String method = getMethod(requestLine);
            String url = getUrl(requestLine);

            if (method.equalsIgnoreCase("CONNECT")) {
                handleConnectRequest(requestLine, clientIn);
                return;
            }

            String hostHeader = extractHostHeader(headerLines);

            if (hostHeader == null || hostHeader.isBlank()) {
                sendSimpleResponse(
                        "HTTP/1.1 400 Bad Request",
                        "<h1>Solicitud invalida</h1>"
                );
                return;
            }

            String host = extractHttpHost(hostHeader);
            int port = extractHttpPort(hostHeader);

            if (isDomainBlocked(host)) {
                System.out.println("DOMINIO BLOQUEADO: " + host);

                ProxyLogger.logRequest(
                        clientSocket.getInetAddress().getHostAddress(),
                        host,
                        method,
                        "BLOQUEADO",
                        0
                );

                sendBlockedPage();
                return;
            }

            if (method.equalsIgnoreCase("GET")
                    || method.equalsIgnoreCase("POST")
                    || method.equalsIgnoreCase("HEAD")) {

                String effectiveUrl = buildHttpUrlForFiltering(
                        host,
                        port,
                        url
                );

                if (isKeywordBlocked(effectiveUrl)) {
                    System.out.println("PALABRA BLOQUEADA EN URL: " + effectiveUrl);

                    ProxyLogger.logRequest(
                            clientSocket.getInetAddress().getHostAddress(),
                            effectiveUrl,
                            method,
                            "BLOQUEADO",
                            0
                    );

                    sendBlockedPage();
                    return;
                }

                String headers =
                        rebuildHeadersWithoutProxyConnection(headerLines);

                forwardHttpRequest(
                        requestLine,
                        host,
                        port,
                        headers
                );
                return;
            }

            ProxyLogger.logRequest(
                    clientSocket.getInetAddress().getHostAddress(),
                    host,
                    method,
                    "NO_SOPORTADO",
                    0
            );

            sendSimpleResponse(
                    "HTTP/1.1 501 Not Implemented",
                    "<h1>Metodo no soportado todavia</h1>"
            );

        } catch (IOException e) {
            System.out.println("Error procesando cliente: " + e.getClass().getName());
            System.out.println("Mensaje: " + e.getMessage());
            e.printStackTrace();
            closeQuietly(clientSocket);
        }
    }

    private void handleConnectRequest(
            String requestLine,
            InputStream clientIn) throws IOException {

        String connectTarget = getUrl(requestLine);
        String connectHost = extractConnectHost(connectTarget);
        int connectPort = extractConnectPort(connectTarget);

        if (connectHost.isBlank()) {
            sendSimpleResponse(
                    "HTTP/1.1 400 Bad Request",
                    "<h1>Solicitud CONNECT invalida</h1>"
            );
            return;
        }

        sendConnectEstablished();

        TlsClientHelloReader helloReader = new TlsClientHelloReader();
        SniExtractor sniExtractor = new SniExtractor();

        byte[] clientHello = helloReader.readClientHello(clientIn);
        String sni = sniExtractor.extractSni(clientHello);

        if (sni == null || sni.isBlank()) {
            ProxyLogger.logRequest(
                    clientSocket.getInetAddress().getHostAddress(),
                    connectHost,
                    "CONNECT",
                    "SIN_SNI",
                    0
            );
            closeQuietly(clientSocket);
            return;
        }

        String normalizedSni = cleanHost(sni);

        if (isDomainBlocked(normalizedSni)) {
            System.out.println("HTTPS BLOQUEADO POR SNI: " + normalizedSni);

            ProxyLogger.logRequest(
                    clientSocket.getInetAddress().getHostAddress(),
                    normalizedSni,
                    "CONNECT",
                    "BLOQUEADO_HTTPS_SNI",
                    0
            );

            closeQuietly(clientSocket);
            return;
        }

        try (Socket serverSocket = new Socket(connectHost, connectPort)) {
            serverSocket.setSoTimeout(10000);

            OutputStream serverOut = serverSocket.getOutputStream();
            serverOut.write(clientHello);
            serverOut.flush();

            long totalBytes = tunnelHttpsTraffic(clientSocket, serverSocket);

            ProxyLogger.logRequest(
                    clientSocket.getInetAddress().getHostAddress(),
                    normalizedSni,
                    "CONNECT",
                    "PERMITIDO_HTTPS",
                    totalBytes
            );
        } finally {
            closeQuietly(clientSocket);
        }
    }

    /**
     * Reenvia una solicitud HTTP al servidor de destino y copia la
     * respuesta de regreso al cliente.
     *
     * @param requestLine primera linea original de la solicitud.
     * @param host host destino.
     * @param port puerto destino.
     * @param headers encabezados filtrados que se reenviaran.
     * @throws IOException si ocurre un error de red.
     */
    private void forwardHttpRequest(
            String requestLine,
            String host,
            int port,
            String headers) throws IOException {

        System.out.println(
                "REENVIANDO A INTERNET: "
                        + host + ":" + port
        );

        long totalBytes = 0;

        try (Socket serverSocket = new Socket(host, port)) {
            serverSocket.setSoTimeout(10000);

            OutputStream serverOut = serverSocket.getOutputStream();

            String newRequestLine =
                    convertProxyRequestLineToOriginRequestLine(
                            requestLine,
                            host,
                            port
                    );

            serverOut.write(
                    (newRequestLine + "\r\n")
                            .getBytes(StandardCharsets.UTF_8)
            );

            serverOut.write(
                    headers.getBytes(StandardCharsets.UTF_8)
            );

            serverOut.write(
                    "Connection: close\r\n"
                            .getBytes(StandardCharsets.UTF_8)
            );

            serverOut.write(
                    "\r\n".getBytes(StandardCharsets.UTF_8)
            );

            serverOut.flush();

            OutputStream clientOut = clientSocket.getOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = serverSocket.getInputStream().read(buffer)) != -1) {
                totalBytes += bytesRead;
                clientOut.write(buffer, 0, bytesRead);
                clientOut.flush();
            }

            ProxyLogger.logRequest(
                    clientSocket.getInetAddress().getHostAddress(),
                    host,
                    getMethod(requestLine),
                    "PERMITIDO",
                    totalBytes
            );

        } finally {
            closeQuietly(clientSocket);
        }
    }

    /**
     * Mantiene un tunel bidireccional entre cliente y servidor
     * para trafico HTTPS ya autorizado.
     *
     * @param client socket del cliente.
     * @param server socket del servidor remoto.
     * @return cantidad total de bytes transferidos en ambos sentidos.
     * @throws IOException si falla alguna lectura o escritura.
     */
    private long tunnelHttpsTraffic(Socket client, Socket server)
            throws IOException {

        AtomicLong totalBytes = new AtomicLong(0);

        Thread clientToServer = new Thread(() -> copyStream(
                client,
                server,
                totalBytes
        ));

        clientToServer.start();

        copyStream(server, client, totalBytes);

        try {
            clientToServer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return totalBytes.get();
    }

    /**
     * Copia datos desde un socket origen hacia otro destino hasta que
     * uno de los extremos cierre la conexion.
     *
     * @param sourceSocket socket desde el cual se lee.
     * @param targetSocket socket hacia el cual se escribe.
     * @param totalBytes acumulador global de bytes transferidos.
     */
    private void copyStream(
            Socket sourceSocket,
            Socket targetSocket,
            AtomicLong totalBytes) {

        byte[] buffer = new byte[8192];

        try {
            InputStream sourceIn = sourceSocket.getInputStream();
            OutputStream targetOut = targetSocket.getOutputStream();
            int bytesRead;

            while ((bytesRead = sourceIn.read(buffer)) != -1) {
                totalBytes.addAndGet(bytesRead);
                targetOut.write(buffer, 0, bytesRead);
                targetOut.flush();
            }
        } catch (IOException e) {
            // Es normal que una mitad del tunel cierre primero.
        } finally {
            closeQuietly(sourceSocket);
            closeQuietly(targetSocket);
        }
    }

    /**
     * Lee desde el cliente el bloque inicial de encabezados HTTP
     * hasta encontrar el fin de cabecera.
     *
     * @param input flujo del cliente.
     * @return cabecera completa como texto o null si no se recibio nada.
     * @throws IOException si los encabezados son demasiado grandes
     * o la lectura falla.
     */
    private String readHttpHeaderBlock(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int previous = -1;
        int current;

        while ((current = input.read()) != -1) {
            buffer.write(current);

            if (buffer.size() > HEADER_MAX_BYTES) {
                throw new IOException("Encabezados HTTP demasiado grandes");
            }

            byte[] data = buffer.toByteArray();
            int length = data.length;

            if (length >= 4
                    && data[length - 4] == '\r'
                    && data[length - 3] == '\n'
                    && data[length - 2] == '\r'
                    && data[length - 1] == '\n') {
                break;
            }

            if (previous == '\n' && current == '\n') {
                break;
            }

            previous = current;
        }

        if (buffer.size() == 0) {
            return null;
        }

        return buffer.toString(StandardCharsets.UTF_8);
    }

    /**
     * Divide el bloque de encabezados en lineas individuales.
     *
     * @param headerText cabecera HTTP completa.
     * @return lista de lineas no vacias.
     */
    private List<String> splitHeaderLines(String headerText) {
        List<String> lines = new ArrayList<>();

        for (String line : headerText.replace("\r\n", "\n").split("\n")) {
            if (!line.isBlank()) {
                lines.add(line);
                System.out.println(line);
            }
        }

        return lines;
    }

    /**
     * Reconstruye los encabezados HTTP eliminando Proxy-Connection
     * para reenviar una version limpia al servidor remoto.
     *
     * @param headerLines lineas originales de la solicitud.
     * @return encabezados listos para reenvio.
     */
    private String rebuildHeadersWithoutProxyConnection(List<String> headerLines) {
        StringBuilder headersBuilder = new StringBuilder();

        for (int i = 1; i < headerLines.size(); i++) {
            String line = headerLines.get(i);

            if (!line.toLowerCase().startsWith("proxy-connection:")) {
                headersBuilder.append(line).append("\r\n");
            }
        }

        return headersBuilder.toString();
    }

    /**
     * Busca el encabezado Host dentro de la solicitud HTTP.
     *
     * @param headerLines lineas completas de la cabecera.
     * @return contenido del Host o null si no existe.
     */
    private String extractHostHeader(List<String> headerLines) {
        for (int i = 1; i < headerLines.size(); i++) {
            String line = headerLines.get(i);

            if (line.toLowerCase().startsWith("host:")) {
                return line.substring(5).trim();
            }
        }

        return null;
    }

    /**
     * Obtiene solo el nombre del host desde el encabezado Host.
     *
     * @param hostHeader valor del encabezado Host.
     * @return nombre del host normalizado.
     */
    private String extractHttpHost(String hostHeader) {
        String clean = hostHeader == null ? "" : hostHeader.trim();

        if (clean.contains(":")) {
            return clean.substring(0, clean.lastIndexOf(":")).toLowerCase();
        }

        return clean.toLowerCase();
    }

    /**
     * Obtiene el puerto desde el encabezado Host o usa 80 por defecto.
     *
     * @param hostHeader valor del encabezado Host.
     * @return puerto HTTP detectado.
     */
    private int extractHttpPort(String hostHeader) {
        String clean = hostHeader == null ? "" : hostHeader.trim();

        if (!clean.contains(":")) {
            return 80;
        }

        try {
            return Integer.parseInt(
                    clean.substring(clean.lastIndexOf(":") + 1)
            );
        } catch (NumberFormatException e) {
            return 80;
        }
    }

    /**
     * Responde al cliente que el tunel CONNECT fue aceptado.
     *
     * @throws IOException si no se puede escribir la respuesta.
     */
    private void sendConnectEstablished() throws IOException {
        OutputStream out = clientSocket.getOutputStream();
        out.write("HTTP/1.1 200 Connection Established\r\n\r\n"
                .getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    /**
     * Convierte la request-line propia de un proxy
     * en una request-line apta para el servidor origen.
     *
     * @param requestLine primera linea recibida del cliente.
     * @param host host destino.
     * @param port puerto destino.
     * @return request-line adaptada para el servidor remoto.
     */
    private String convertProxyRequestLineToOriginRequestLine(
            String requestLine,
            String host,
            int port) {

        String[] parts = requestLine.split(" ");

        if (parts.length < 3) {
            return requestLine;
        }

        String method = parts[0];
        String url = parts[1];
        String version = parts[2];

        String path = url;

        String prefixHttpWithPort =
                "http://" + host + ":" + port;

        String prefixHttp =
                "http://" + host;

        if (url.startsWith(prefixHttpWithPort)) {
            path = url.substring(prefixHttpWithPort.length());
        } else if (url.startsWith(prefixHttp)) {
            path = url.substring(prefixHttp.length());
        }

        if (path.isBlank()) {
            path = "/";
        }

        return method + " " + path + " " + version;
    }

    /**
     * Extrae el metodo HTTP desde la primera linea de la solicitud.
     *
     * @param requestLine primera linea HTTP.
     * @return metodo detectado.
     */
    private String getMethod(String requestLine) {
        String[] parts = requestLine.split(" ");
        return parts.length > 0 ? parts[0] : "";
    }

    /**
     * Extrae la URL o target desde la primera linea de la solicitud.
     *
     * @param requestLine primera linea HTTP.
     * @return URL o target solicitado.
     */
    private String getUrl(String requestLine) {
        String[] parts = requestLine.split(" ");
        return parts.length > 1 ? parts[1] : "";
    }

    /**
     * Construye una URL HTTP completa para evaluar reglas por palabra
     * incluso cuando la solicitud llega con ruta relativa.
     *
     * @param host host destino.
     * @param port puerto destino.
     * @param url URL o path recibido del cliente.
     * @return URL completa apta para filtrado.
     */
    private String buildHttpUrlForFiltering(String host, int port, String url) {
        String cleanUrl = url == null ? "" : url.trim();
        String cleanHost = host == null ? "" : host.trim().toLowerCase();

        if (cleanUrl.isBlank()) {
            cleanUrl = "/";
        }

        if (cleanUrl.startsWith("http://")
                || cleanUrl.startsWith("https://")) {
            return cleanUrl;
        }

        StringBuilder fullUrl = new StringBuilder("http://").append(cleanHost);

        if (port > 0 && port != 80) {
            fullUrl.append(":").append(port);
        }

        if (!cleanUrl.startsWith("/")) {
            fullUrl.append("/");
        }

        fullUrl.append(cleanUrl);
        return fullUrl.toString();
    }

    /**
     * Extrae el host solicitado en una peticion CONNECT.
     *
     * @param connectTarget valor host:puerto de CONNECT.
     * @return host limpio.
     */
    private String extractConnectHost(String connectTarget) {
        String cleanTarget = connectTarget == null ? "" : connectTarget.trim();
        int separatorIndex = cleanTarget.lastIndexOf(':');

        if (separatorIndex <= 0) {
            return cleanHost(cleanTarget);
        }

        return cleanHost(cleanTarget.substring(0, separatorIndex));
    }

    /**
     * Extrae el puerto solicitado en una peticion CONNECT.
     *
     * @param connectTarget valor host:puerto de CONNECT.
     * @return puerto detectado o 443 por defecto.
     */
    private int extractConnectPort(String connectTarget) {
        String cleanTarget = connectTarget == null ? "" : connectTarget.trim();
        int separatorIndex = cleanTarget.lastIndexOf(':');

        if (separatorIndex <= 0 || separatorIndex == cleanTarget.length() - 1) {
            return HTTPS_DEFAULT_PORT;
        }

        try {
            return Integer.parseInt(
                    cleanTarget.substring(separatorIndex + 1)
            );
        } catch (NumberFormatException e) {
            return HTTPS_DEFAULT_PORT;
        }
    }

    /**
     * Normaliza un host eliminando puertos comunes y uniformando
     * el texto a minusculas.
     *
     * @param host host original.
     * @return host limpio.
     */
    private String cleanHost(String host) {
        return host.replace(":80", "")
                .replace(":443", "")
                .toLowerCase()
                .trim();
    }

    /**
     * Verifica si un host coincide con alguna regla de dominio
     * ya sea exacta o por subdominio.
     *
     * @param host host a evaluar.
     * @return true si debe bloquearse.
     */
    private boolean isDomainBlocked(String host) {
        List<String> blockedDomains = BlocklistManager.getDomains();

        for (String domain : blockedDomains) {
            domain = domain.trim().toLowerCase();

            if (domain.isBlank()) {
                continue;
            }

            if (host.equals(domain)
                    || host.endsWith("." + domain)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Verifica si una URL contiene alguna palabra clave bloqueada.
     *
     * @param url URL completa a evaluar.
     * @return true si contiene una coincidencia.
     */
    private boolean isKeywordBlocked(String url) {
        List<String> blockedKeywords = BlocklistManager.getKeywords();
        String cleanUrl = url.toLowerCase();

        for (String keyword : blockedKeywords) {
            keyword = keyword.trim().toLowerCase();

            if (keyword.isBlank()) {
                continue;
            }

            if (cleanUrl.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Devuelve al cliente la pagina HTML de acceso bloqueado.
     *
     * @throws IOException si falla la lectura o el envio.
     */
    private void sendBlockedPage() throws IOException {
        String html = Files.readString(
                Paths.get(BLOCKED_HTML_PATH),
                StandardCharsets.UTF_8
        );

        byte[] body = html.getBytes(StandardCharsets.UTF_8);

        String response =
                "HTTP/1.1 403 Forbidden\r\n"
                        + "Content-Type: text/html; charset=UTF-8\r\n"
                        + "Content-Length: " + body.length + "\r\n"
                        + "Connection: close\r\n"
                        + "\r\n";

        OutputStream out = clientSocket.getOutputStream();
        out.write(response.getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();

        closeQuietly(clientSocket);
    }

    /**
     * Envia una respuesta HTTP simple generada por el proxy.
     *
     * @param status linea de estado HTTP.
     * @param html contenido HTML a devolver.
     * @throws IOException si no se puede escribir la respuesta.
     */
    private void sendSimpleResponse(
            String status,
            String html) throws IOException {

        byte[] body = html.getBytes(StandardCharsets.UTF_8);

        String response =
                status + "\r\n"
                        + "Content-Type: text/html; charset=UTF-8\r\n"
                        + "Content-Length: " + body.length + "\r\n"
                        + "Connection: close\r\n"
                        + "\r\n";

        OutputStream out = clientSocket.getOutputStream();
        out.write(response.getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();

        closeQuietly(clientSocket);
    }

    /**
     * Cierra un socket sin propagar excepciones.
     *
     * @param socket socket a cerrar.
     */
    private void closeQuietly(Socket socket) {
        if (socket == null || socket.isClosed()) {
            return;
        }

        try {
            socket.close();
        } catch (IOException e) {
        }
    }
}

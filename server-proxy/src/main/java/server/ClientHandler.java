package server;

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

/**
 * Atiende cada cliente conectado al proxy.
 * Soporta HTTP tradicional y tuneles HTTPS con filtrado por SNI.
 */
public class ClientHandler implements Runnable {

    private static final String BLOCKED_HTML_PATH =
            "src/main/resources/html/blocked.html";

    private static final int HEADER_MAX_BYTES = 32768;
    private static final int HTTPS_DEFAULT_PORT = 443;

    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

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

            String host = cleanHost(hostHeader);

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

                if (isKeywordBlocked(url)) {
                    System.out.println("PALABRA BLOQUEADA EN URL: " + url);

                    ProxyLogger.logRequest(
                            clientSocket.getInetAddress().getHostAddress(),
                            url,
                            method,
                            "BLOQUEADO",
                            0
                    );

                    sendBlockedPage();
                    return;
                }

                String headers = rebuildHeadersWithoutProxyConnection(headerLines);
                forwardHttpRequest(requestLine, host, headers);
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

    /**
     * Procesa una solicitud CONNECT, inspecciona el SNI y decide si
     * permite o bloquea el tunel HTTPS.
     */
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
     * Reenvia la solicitud HTTP al servidor real y devuelve la respuesta al navegador.
     */
    private void forwardHttpRequest(
            String requestLine,
            String host,
            String headers) throws IOException {

        System.out.println("REENVIANDO A INTERNET: " + host);

        long totalBytes = 0;

        try (Socket serverSocket = new Socket(host, 80)) {
            serverSocket.setSoTimeout(10000);

            OutputStream serverOut = serverSocket.getOutputStream();

            String newRequestLine =
                    convertProxyRequestLineToOriginRequestLine(
                            requestLine,
                            host
                    );

            serverOut.write((newRequestLine + "\r\n").getBytes(StandardCharsets.UTF_8));
            serverOut.write(headers.getBytes(StandardCharsets.UTF_8));
            serverOut.write("Connection: close\r\n".getBytes(StandardCharsets.UTF_8));
            serverOut.write("\r\n".getBytes(StandardCharsets.UTF_8));
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
     * Tuneliza trafico HTTPS en ambos sentidos sin inspeccion adicional.
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
     * Copia bytes de un socket origen a otro destino.
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
     * Lee solo los encabezados HTTP iniciales sin consumir datos TLS posteriores.
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
     * Convierte los encabezados HTTP a una lista de lineas.
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
     * Reconstruye encabezados para reenviar una solicitud HTTP.
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
     * Obtiene el host desde los encabezados HTTP.
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
     * Responde al navegador que el tunel CONNECT fue aceptado.
     */
    private void sendConnectEstablished() throws IOException {
        OutputStream out = clientSocket.getOutputStream();
        out.write("HTTP/1.1 200 Connection Established\r\n\r\n"
                .getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    /**
     * Convierte la primera linea que llega al proxy
     * en una linea valida para el servidor real.
     */
    private String convertProxyRequestLineToOriginRequestLine(
            String requestLine,
            String host) {

        String[] parts = requestLine.split(" ");

        if (parts.length < 3) {
            return requestLine;
        }

        String method = parts[0];
        String url = parts[1];
        String version = parts[2];

        String path = url;
        String prefixHttp = "http://" + host;

        if (url.startsWith(prefixHttp)) {
            path = url.substring(prefixHttp.length());
        }

        if (path.isBlank()) {
            path = "/";
        }

        return method + " " + path + " " + version;
    }

    /**
     * Obtiene el metodo HTTP.
     */
    private String getMethod(String requestLine) {
        String[] parts = requestLine.split(" ");
        return parts.length > 0 ? parts[0] : "";
    }

    /**
     * Obtiene la URL solicitada.
     */
    private String getUrl(String requestLine) {
        String[] parts = requestLine.split(" ");
        return parts.length > 1 ? parts[1] : "";
    }

    /**
     * Extrae el host a partir del destino de CONNECT.
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
     * Extrae el puerto a partir del destino de CONNECT.
     */
    private int extractConnectPort(String connectTarget) {
        String cleanTarget = connectTarget == null ? "" : connectTarget.trim();
        int separatorIndex = cleanTarget.lastIndexOf(':');

        if (separatorIndex <= 0 || separatorIndex == cleanTarget.length() - 1) {
            return HTTPS_DEFAULT_PORT;
        }

        try {
            return Integer.parseInt(cleanTarget.substring(separatorIndex + 1));
        } catch (NumberFormatException e) {
            return HTTPS_DEFAULT_PORT;
        }
    }

    /**
     * Limpia el host quitando puertos comunes.
     */
    private String cleanHost(String host) {
        return host.replace(":80", "")
                .replace(":443", "")
                .toLowerCase()
                .trim();
    }

    /**
     * Revisa bloqueo por dominio.
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
     * Revisa bloqueo por palabra clave en URL HTTP.
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
     * Envia la pagina HTML de bloqueo para HTTP.
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
     * Envia una respuesta HTML simple.
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
     * Cierra un socket ignorando errores.
     */
    private void closeQuietly(Socket socket) {
        if (socket == null || socket.isClosed()) {
            return;
        }

        try {
            socket.close();
        } catch (IOException e) {
            // Nada que hacer.
        }
    }
}

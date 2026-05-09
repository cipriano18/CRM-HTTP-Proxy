package server;

import filter.BlocklistManager;
import logger.ProxyLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Atiende cada cliente conectado al proxy.
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;

    private static final String BLOCKED_HTML_PATH =
            "src/main/resources/html/blocked.html";

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {

        try {
            System.out.println("Procesando cliente: " + clientSocket.getInetAddress());

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream(),
                            StandardCharsets.UTF_8
                    )
            );

            String requestLine = reader.readLine();

            if (requestLine == null || requestLine.isBlank()) {
                clientSocket.close();
                return;
            }

            System.out.println("REQUEST: " + requestLine);

            String method = getMethod(requestLine);
            String url = getUrl(requestLine);

            String host = null;
            String line;

            StringBuilder headersBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null && !line.isEmpty()) {

                System.out.println(line);

                if (line.toLowerCase().startsWith("host:")) {
                    host = line.substring(5).trim();
                }

                if (!line.toLowerCase().startsWith("proxy-connection:")) {
                    headersBuilder.append(line).append("\r\n");
                }
            }

            if (host == null) {
                sendSimpleResponse(
                        "HTTP/1.1 400 Bad Request",
                        "<h1>Solicitud inválida</h1>"
                );
                return;
            }

            host = cleanHost(host);

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
                    || method.equalsIgnoreCase("POST")) {

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
            }

            if (method.equalsIgnoreCase("GET")
                    || method.equalsIgnoreCase("POST")) {

                forwardHttpRequest(
                        requestLine,
                        host,
                        headersBuilder.toString()
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
                    "<h1>Método no soportado todavía</h1>"
            );

        } catch (IOException e) {
            System.out.println("Error procesando cliente: " + e.getClass().getName());
            System.out.println("Mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reenvía la solicitud HTTP al servidor real y devuelve la respuesta al navegador.
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
            clientSocket.close();
        }
    }

    /**
     * Convierte la primera línea que llega al proxy
     * en una línea válida para el servidor real.
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
     * Obtiene el método HTTP.
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
     * Envía la página HTML de bloqueo.
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

        clientSocket.close();
    }

    /**
     * Envía una respuesta HTML simple.
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

        clientSocket.close();
    }
}
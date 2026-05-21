/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author cipriano
 */
public class HttpRequest {

    private final String method;
    private final String url;
    private final String host;
    private final int port;
    private final String path;
    private final String headers;

    public HttpRequest(String method, String url, String host, int port,
            String path, String headers) {
        this.method = method;
        this.url = url;
        this.host = host;
        this.port = port;
        this.path = path;
        this.headers = headers;
    }

    // Parsea la primera línea + headers crudos y retorna un HttpRequest
    public static HttpRequest parse(String firstLine, String headers) {
        String[] parts = firstLine.split(" ");
        String method = parts[0];
        String url = parts.length > 1 ? parts[1] : "/";

        String host = "";
        int port = 80;
        String path = "/";

        try {
            java.net.URL parsed = new java.net.URL(url);
            host = parsed.getHost();
            path = parsed.getFile().isEmpty() ? "/" : parsed.getFile();
            port = parsed.getPort() != -1 ? parsed.getPort() : 80;
        } catch (Exception e) {
            // URL relativa o malformada — intentar extraer host de headers
            for (String line : headers.split("\r\n")) {
                if (line.toLowerCase().startsWith("host:")) {
                    host = line.substring(5).trim();
                    break;
                }
            }
        }

        return new HttpRequest(method, url, host, port, path, headers);
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public String getHeaders() {
        return headers;
    }

    @Override
    public String toString() {
        return method + " " + url;
    }
}

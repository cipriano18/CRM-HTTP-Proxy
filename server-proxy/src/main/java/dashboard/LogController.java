/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dashboard;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador HTTP encargado de leer el log del proxy
 * y convertirlo en metricas JSON para el dashboard.
 *
 *
 * @author cipriano
 */
public class LogController {

    /**
     * Ruta del archivo de log que se analiza para construir
     * las metricas agregadas.
     */
    private static final String LOG_PATH = "src/main/resources/data/proxy.log";

    /**
     * Atiende la solicitud HTTP de metricas del dashboard.
     *
     * @param exchange intercambio HTTP recibido.
     * @throws IOException si falla la lectura del log
     * o el envio de la respuesta.
     */
    public static void handleMetrics(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        Path path = Path.of(LOG_PATH);

        int totalRequests = 0;
        int blocked = 0;
        int allowed = 0;
        long totalBytes = 0;

        Map<String, Integer> domainCounter = new HashMap<>();
        Map<String, Integer> clientCounter = new HashMap<>();

        if (Files.exists(path)) {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    continue;
                }

                totalRequests++;

                String ip = extractValue(line, "IP=");
                String url = extractValue(line, "URL=");
                String status = extractValue(line, "STATUS=");
                String bytesText = extractValue(line, "BYTES=");

                if (status.toUpperCase().contains("BLOQUEADO")) {
                    blocked++;
                } else {
                    allowed++;
                }

                try {
                    totalBytes += Long.parseLong(bytesText);
                } catch (NumberFormatException e) {
                    // Si BYTES viene vacío o mal formado, lo ignora
                }

                if (!url.isBlank()) {
                    domainCounter.put(url, domainCounter.getOrDefault(url, 0) + 1);
                }

                if (!ip.isBlank()) {
                    clientCounter.put(ip, clientCounter.getOrDefault(ip, 0) + 1);
                }
            }
        }

        double totalMB = totalBytes / 1024.0 / 1024.0;

        String json = buildJson(
                totalRequests,
                blocked,
                allowed,
                totalMB,
                domainCounter,
                clientCounter
        );

        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    /**
     * Extrae el valor de una clave dentro de una linea del log.
     *
     * @param line linea completa del archivo.
     * @param key prefijo buscado, por ejemplo IP= o URL=.
     * @return valor asociado o cadena vacia si no existe.
     */
    private static String extractValue(String line, String key) {
        int start = line.indexOf(key);

        if (start == -1) {
            return "";
        }

        start += key.length();

        int end = line.indexOf("|", start);

        if (end == -1) {
            end = line.length();
        }

        return line.substring(start, end).trim();
    }

    /**
     * Construye el documento JSON de respuesta con los acumulados
     * calculados a partir del log.
     *
     * @param totalRequests total de solicitudes registradas.
     * @param blocked cantidad de solicitudes bloqueadas.
     * @param allowed cantidad de solicitudes permitidas.
     * @param totalMB trafico total en megabytes.
     * @param domains contador agrupado por URL o dominio.
     * @param clients contador agrupado por IP cliente.
     * @return respuesta JSON serializada manualmente.
     */
    private static String buildJson(
            int totalRequests,
            int blocked,
            int allowed,
            double totalMB,
            Map<String, Integer> domains,
            Map<String, Integer> clients
    ) {
        StringBuilder json = new StringBuilder();

        json.append("{");
        json.append("\"totalRequests\":").append(totalRequests).append(",");
        json.append("\"blocked\":").append(blocked).append(",");
        json.append("\"allowed\":").append(allowed).append(",");
        json.append("\"totalMB\":").append(String.format(java.util.Locale.US, "%.2f", totalMB)).append(",");

        json.append("\"domains\":[");
        appendCounterArray(json, domains, "domain");
        json.append("],");

        json.append("\"clients\":[");
        appendCounterArray(json, clients, "ip");
        json.append("]");

        json.append("}");

        return json.toString();
    }

    /**
     * Agrega al JSON una coleccion de pares clave-conteo.
     *
     * @param json acumulador principal de la respuesta.
     * @param data mapa con conteos.
     * @param fieldName nombre del campo que representara la clave.
     */
    private static void appendCounterArray(
            StringBuilder json,
            Map<String, Integer> data,
            String fieldName
    ) {
        boolean first = true;

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            if (!first) {
                json.append(",");
            }

            json.append("{");
            json.append("\"").append(fieldName).append("\":")
                    .append("\"").append(escapeJson(entry.getKey())).append("\",");
            json.append("\"count\":").append(entry.getValue());
            json.append("}");

            first = false;
        }
    }

    /**
     * Escapa caracteres especiales para que un texto
     * sea valido dentro del JSON generado.
     *
     * @param value texto original.
     * @return texto escapado.
     */
    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

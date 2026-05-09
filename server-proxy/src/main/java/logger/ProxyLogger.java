/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;


/**
 *
 * @author cipriano
 */
public class ProxyLogger {

    private static final String LOG_PATH =
            "src/main/resources/data/proxy.log";

    /**
     * Guarda una solicitud en proxy.log.
     *
     * @param clientIp IP del cliente
     * @param url dominio o URL solicitada
     * @param method método HTTP
     * @param status PERMITIDO o BLOQUEADO
     * @param bytes tamaño transferido
     */
    public static synchronized void logRequest(
            String clientIp,
            String url,
            String method,
            String status,
            long bytes) {

        String line =
                LocalDateTime.now()
                + " | IP=" + clientIp
                + " | METHOD=" + method
                + " | URL=" + url
                + " | STATUS=" + status
                + " | BYTES=" + bytes
                + System.lineSeparator();

        try {
            Files.write(
                    Path.of(LOG_PATH),
                    line.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.out.println("Error escribiendo proxy.log: " + e.getMessage());
        }
    }
}

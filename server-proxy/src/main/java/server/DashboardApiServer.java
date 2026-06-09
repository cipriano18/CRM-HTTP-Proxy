/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import com.sun.net.httpserver.HttpServer;
import controller.LogController;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Servidor HTTP auxiliar que expone metricas del proxy
 * para el dashboard local.
 *
 *
 * @author cipriano
 */
public class DashboardApiServer {

    /**
     * Puerto donde se publica la API del dashboard.
     */
    private static final int PORT = 8090;

    /**
     * Instancia interna del servidor HTTP embebido.
     */
    private HttpServer server;

    /**
     * Inicia la API local y registra el endpoint de metricas.
     */
    public void start() {
        try {

            server = HttpServer.create(
                    new InetSocketAddress(PORT),
                    0
            );

            server.createContext(
                    "/api/dashboard/metrics",
                    LogController::handleMetrics
            );

            server.setExecutor(null);

            server.start();

            System.out.println(
                    "Dashboard API iniciada en puerto "
                    + PORT
            );

        } catch (IOException e) {
            System.out.println(
                    "Error iniciando Dashboard API: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Detiene la API del dashboard si se encuentra activa.
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}

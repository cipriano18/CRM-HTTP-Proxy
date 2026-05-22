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
 *
 * @author cipriano
 */
public class DashboardApiServer {

    private static final int PORT = 8090;

    private HttpServer server;

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

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author cipriano
 */

/**
 * Servidor principal del proxy.
 * Escucha conexiones de navegadores o clientes HTTP.
 */
public class ProxyServer {

    /**
     * Puerto donde escuchará el proxy.
     */
    private static final int PORT = 8080;

    /**
     * Inicia el servidor proxy.
     * Por cada cliente crea un hilo independiente.
     */
    public void start() {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println(
                    "Proxy iniciado en puerto "
                    + PORT);

            while (true) {

                Socket clientSocket =
                        serverSocket.accept();

                System.out.println(
                        "Cliente conectado desde: "
                        + clientSocket
                                .getInetAddress()
                                .getHostAddress());

                /**
                 * Cada cliente se procesa
                 * en un hilo diferente.
                 */
                Thread clientThread =
                        new Thread(
                                new ClientHandler(
                                        clientSocket));

                clientThread.start();
            }

        } catch (IOException e) {

            System.out.println(
                    "Error iniciando el proxy: "
                    + e.getMessage());
        }
    }
}
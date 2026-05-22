package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Servidor principal del proxy. Escucha conexiones de navegadores o clientes
 * HTTP.
 */
public class ProxyServer {

    /**
     * Puerto donde escuchara el proxy.
     */
    private static final int PORT = 8080;

    private volatile boolean running;

    private ServerSocket serverSocket;

    /**
     * Inicia el servidor proxy. Por cada cliente crea un hilo independiente.
     */
    public void start() {
        if (running) {
            System.out.println("El proxy ya esta encendido.");
            return;
        }

        try {
            synchronized (this) {
                if (running) {
                    System.out.println("El proxy ya esta encendido.");
                    return;
                }

                serverSocket = new ServerSocket(PORT);
                running = true;
            }

            System.out.println(
                    "Proxy iniciado en puerto " + PORT
            );

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();

                    System.out.println(
                            "Cliente conectado desde: "
                            + clientSocket.getInetAddress()
                                    .getHostAddress()
                    );

                    Thread clientThread = new Thread(
                            new ClientHandler(clientSocket)
                    );
                    clientThread.start();

                } catch (SocketException e) {
                    if (running) {
                        System.out.println(
                                "Error aceptando cliente: "
                                + e.getMessage()
                        );
                    }
                }
            }

        } catch (IOException e) {
            System.out.println(
                    "Error iniciando el proxy: "
                    + e.getMessage()
            );
        } finally {
            running = false;
            closeServerSocket();
        }
    }

    /**
     * Apaga el servidor proxy.
     */
    public void stop() {
        if (!running) {
            return;
        }

        running = false;

        synchronized (this) {
            closeServerSocket();
        }
        System.out.println("Proxy detenido.");
    }

    /**
     * Indica si el servidor esta corriendo.
     *
     * @return true si esta corriendo.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Cierra el socket principal si existe.
     */
    private void closeServerSocket() {
        if (serverSocket == null || serverSocket.isClosed()) {
            return;
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println(
                    "Error cerrando el servidor: "
                    + e.getMessage()
            );
        }
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import server.ProxyServer;

/**
 * FXML Controller class
 *
 * @author cipriano
 */
public class home_proxy_controller implements Initializable {

    /**
     * Instancia del servidor proxy.
     */
    private final ProxyServer proxyServer =
            new ProxyServer();

    /**
     * Indica si el servidor ya fue iniciado.
     */
    private boolean serverRunning = false;
    @FXML
    private Button Iniciar_Proxy;

    /**
     * Inicializa el controlador.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    /**
     * Enciende el servidor proxy.
     */
    @FXML
    private void Iniciarproxy(ActionEvent event) {
        
        /**
         * Evita iniciar múltiples veces
         * el mismo servidor.
         */
        if (serverRunning) {

            System.out.println(
                    "El proxy ya está encendido.");

            return;
        }

        /**
         * Hilo encargado de ejecutar
         * el servidor proxy.
         */
        Thread serverThread =
                new Thread(() -> {

                    proxyServer.start();
                });

        serverThread.setDaemon(true);

        serverThread.start();

        serverRunning = true;

        System.out.println(
                "Servidor proxy iniciado.");
    }
}
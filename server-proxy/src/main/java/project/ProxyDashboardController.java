package project;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import repository.BlocklistRepository;
import server.ProxyServer;
import service.ProxyRuntimeService;

/**
 * Controlador de la vista inicial del proyecto.
 */
public class ProxyDashboardController {

    private static final String SAMPLE_HTTP_HOST = "facebook.com";
    private static final String SAMPLE_HTTP_URL =
            "http://facebook.com/videos/tiktok-trends";

    @FXML
    private Label projectStatusLabel;

    @FXML
    private Label packageStatusLabel;

    @FXML
    private void initialize() {
        // Se crea el servicio principal con sus dependencias básicas.
        ProxyRuntimeService runtimeService = new ProxyRuntimeService(
                new ProxyServer(),
                new BlocklistRepository());

        projectStatusLabel.setText(runtimeService.getStartupMessage());
        packageStatusLabel.setText(
                "HTTP de ejemplo bloqueado: "
                + runtimeService.isHttpBlocked(
                        SAMPLE_HTTP_HOST,
                        SAMPLE_HTTP_URL)
                + " | HTTPS de ejemplo bloqueado: "
                + runtimeService.isHttpsBlocked("youtube.com"));
    }
}

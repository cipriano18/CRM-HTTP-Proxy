package ui;

import filter.BlocklistManager;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import dashboard.DashboardApiServer;
import proxy.core.ProxyServer;

/**
 * Controlador de la pantalla de administracion del proxy.
 *
 * <p>
 * Permite administrar dominios y palabras clave bloqueadas, ademas de controlar
 * el arranque del servidor proxy.
 * </p>
 */
public class ServerScreenController {

    private static final String RULE_TYPE_DOMAIN = "DOMAIN";

    private static final String RULE_TYPE_KEYWORD = "KEYWORD";

    @FXML
    private TableView<BlockRuleEntry> adminTable;

    @FXML
    private TableColumn<BlockRuleEntry, String> idColumn;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField keyword_tf;

    @FXML
    private Button createButton;

    @FXML
    private Button createKeyword_btn;

    @FXML
    private Button revokeButton;

    @FXML
    private Button serverButton;

    @FXML
    private HBox adminContent;

    @FXML
    private Label sectionTitleLabel;

    private final ProxyServer proxyServer = new ProxyServer();

    private final DashboardApiServer dashboardApiServer
            = new DashboardApiServer();
    private Thread serverThread;

    private boolean serverRunning;

    private final ObservableList<BlockRuleEntry> rules
            = FXCollections.observableArrayList();

    /**
     * Inicializa la vista y deja bloqueada la administracion hasta que el proxy
     * sea encendido.
     */
    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cellData
                -> new ReadOnlyStringWrapper(
                        cellData.getValue().getDisplayValue()
                )
        );

        adminTable.getSelectionModel().setSelectionMode(
                SelectionMode.SINGLE
        );
        adminTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
        adminTable.setFixedCellSize(38);
        idColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        adminTable.setPlaceholder(
                new Label("No hay reglas registradas.")
        );
        adminTable.setItems(rules);

        syncRuleList();
        updateServerState(false);
    }

    /**
     * Agrega un dominio a la lista bloqueada.
     */
    @FXML
    private void handleCreateAdmin() {
        String domain = normalizeDomain(usernameField.getText());

        if (domain.isBlank()) {
            showAlert(
                    AlertType.WARNING,
                    "Dominio invalido",
                    "Debe ingresar un dominio valido."
            );
            return;
        }

        if (BlocklistManager.getDomains().contains(domain)) {
            showAlert(
                    AlertType.INFORMATION,
                    "Dominio existente",
                    "Ese dominio ya se encuentra en la lista."
            );
            return;
        }

        BlocklistManager.addDomain(domain);
        usernameField.clear();
        syncRuleList();

        showAlert(
                AlertType.INFORMATION,
                "Dominio agregado",
                "El dominio fue agregado correctamente."
        );
    }

    /**
     * Agrega una palabra clave a la lista bloqueada.
     */
    @FXML
    private void handleCreateKeyword() {
        String keyword = normalizeKeyword(keyword_tf.getText());

        if (keyword.isBlank()) {
            showAlert(
                    AlertType.WARNING,
                    "Palabra invalida",
                    "Debe ingresar una palabra clave valida."
            );
            return;
        }

        if (BlocklistManager.getKeywords().contains(keyword)) {
            showAlert(
                    AlertType.INFORMATION,
                    "Palabra existente",
                    "Esa palabra clave ya se encuentra en la lista."
            );
            return;
        }

        BlocklistManager.addKeyword(keyword);
        keyword_tf.clear();
        syncRuleList();

        showAlert(
                AlertType.INFORMATION,
                "Palabra agregada",
                "La palabra clave fue agregada correctamente."
        );
    }

    /**
     * Elimina la regla seleccionada de la lista bloqueada.
     */
    @FXML
    private void handleRevokeAdmin() {
        BlockRuleEntry selected = adminTable.getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            showAlert(
                    AlertType.WARNING,
                    "Seleccion requerida",
                    "Seleccione una regla para quitarla."
            );
            return;
        }

        boolean removed;

        if (RULE_TYPE_DOMAIN.equals(selected.getType())) {
            removed = BlocklistManager.removeDomain(selected.getValue());
        } else {
            removed = BlocklistManager.removeKeyword(selected.getValue());
        }

        if (!removed) {
            showAlert(
                    AlertType.WARNING,
                    "No se pudo quitar",
                    "La regla seleccionada ya no existe en la lista."
            );
            syncRuleList();
            return;
        }

        syncRuleList();

        showAlert(
                AlertType.INFORMATION,
                "Regla eliminada",
                "La regla fue eliminada correctamente."
        );
    }

    /**
     * Enciende o apaga el servidor proxy.
     */
    @FXML
    private void handleToggleServer() {
        if (serverRunning) {
            stopServer();
            return;
        }

        startServer();
    }

    /**
     * Inicia el servidor en segundo plano.
     */
    private void startServer() {
        if (serverRunning) {
            return;
        }

        serverThread = new Thread(() -> {
            proxyServer.start();
        });

        serverThread.setDaemon(true);
        serverThread.start();

        dashboardApiServer.start();
        serverRunning = true;
        updateServerState(true);

        showAlert(
                AlertType.INFORMATION,
                "Servidor iniciado",
                "El servidor proxy fue iniciado correctamente."
        );
    }

    /**
     * Detiene el servidor si esta en ejecucion.
     */
    private void stopServer() {
        if (!serverRunning) {
            return;
        }

        proxyServer.stop();
        dashboardApiServer.stop();
        if (serverThread != null && serverThread.isAlive()) {
            try {
                serverThread.join(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        serverRunning = false;
        updateServerState(false);

        showAlert(
                AlertType.INFORMATION,
                "Servidor apagado",
                "El servidor proxy fue detenido correctamente."
        );
    }

    /**
     * Actualiza el estado visual de la pantalla.
     *
     * @param running true si el servidor esta encendido.
     */
    private void updateServerState(boolean running) {
        adminContent.setDisable(!running);
        sectionTitleLabel.setDisable(!running);
        createButton.setDisable(!running);
        createKeyword_btn.setDisable(!running);
        revokeButton.setDisable(!running);
        usernameField.setDisable(!running);
        keyword_tf.setDisable(!running);
        adminTable.setDisable(!running);

        if (running) {
            adminContent.getStyleClass().remove("locked-section");
            sectionTitleLabel.getStyleClass().remove("locked-section");

            serverButton.setText("Apagar servidor");
            serverButton.getStyleClass().remove("btn-primary");

            if (!serverButton.getStyleClass().contains("btn-danger")) {
                serverButton.getStyleClass().add("btn-danger");
            }
            return;
        }

        if (!adminContent.getStyleClass().contains("locked-section")) {
            adminContent.getStyleClass().add("locked-section");
        }

        if (!sectionTitleLabel.getStyleClass().contains("locked-section")) {
            sectionTitleLabel.getStyleClass().add("locked-section");
        }

        serverButton.setText("Prender servidor");
        serverButton.getStyleClass().remove("btn-danger");

        if (!serverButton.getStyleClass().contains("btn-primary")) {
            serverButton.getStyleClass().add("btn-primary");
        }
    }

    /**
     * Recarga la tabla desde el archivo blocklist.
     */
    private void syncRuleList() {
        List<BlockRuleEntry> updatedRules = new ArrayList<>();

        for (String domain : BlocklistManager.getDomains()) {
            updatedRules.add(new BlockRuleEntry(
                    RULE_TYPE_DOMAIN,
                    domain
            ));
        }

        for (String keyword : BlocklistManager.getKeywords()) {
            updatedRules.add(new BlockRuleEntry(
                    RULE_TYPE_KEYWORD,
                    keyword
            ));
        }

        rules.setAll(updatedRules);
        adminTable.refresh();
    }

    /**
     * Normaliza el valor ingresado para dejar solo el dominio.
     *
     * @param value texto ingresado.
     * @return dominio limpio en minusculas.
     */
    private String normalizeDomain(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim().toLowerCase();

        if (normalized.startsWith("http://")) {
            normalized = normalized.substring(7);
        } else if (normalized.startsWith("https://")) {
            normalized = normalized.substring(8);
        }

        int slashIndex = normalized.indexOf('/');
        if (slashIndex >= 0) {
            normalized = normalized.substring(0, slashIndex);
        }

        int portIndex = normalized.indexOf(':');
        if (portIndex >= 0) {
            normalized = normalized.substring(0, portIndex);
        }

        while (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }

        while (normalized.endsWith(".")) {
            normalized = normalized.substring(
                    0,
                    normalized.length() - 1
            );
        }

        return normalized;
    }

    /**
     * Normaliza la palabra clave ingresada.
     *
     * @param value texto ingresado.
     * @return palabra limpia en minusculas.
     */
    private String normalizeKeyword(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().toLowerCase();
    }

    /**
     * Muestra una alerta simple en pantalla.
     *
     * @param type tipo de alerta.
     * @param title titulo.
     * @param message mensaje.
     */
    private void showAlert(
            AlertType type,
            String title,
            String message
    ) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Modelo simple para mostrar reglas en la tabla.
     */
    public static class BlockRuleEntry {

        private final String type;

        private final String value;

        public BlockRuleEntry(String type, String value) {
            this.type = type;
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        public String getDisplayValue() {
            return type + ": " + value;
        }
    }
}

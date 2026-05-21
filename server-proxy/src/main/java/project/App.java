package project;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada de la interfaz JavaFX del proxy.
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("serverscreen"), 860, 620);

        stage.setTitle("HTTP Proxy Server");
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                App.class.getResource("/fxml/" + fxml + ".fxml")
        );
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}

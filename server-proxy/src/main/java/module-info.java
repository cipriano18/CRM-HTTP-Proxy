module com.mycompany.serverproxy {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires jdk.httpserver;

    exports app;
    opens ui to javafx.fxml;
}

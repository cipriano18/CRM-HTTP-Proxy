module com.mycompany.serverproxy {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
   requires jdk.httpserver;
    opens project to javafx.fxml;
    exports project;
    opens controller to javafx.fxml;
}

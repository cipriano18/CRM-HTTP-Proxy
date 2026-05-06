module com.mycompany.serverproxy {
    requires javafx.controls;
    requires javafx.fxml;

    opens project to javafx.fxml;
    exports project;
    exports model;
    exports repository;
    exports server;
    exports handlers;
    exports service;
}

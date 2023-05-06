module com.github.javafx_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens com.example.client to javafx.fxml;
    exports com.example.client;
    exports com.example.client.entity;
    exports com.example.client.controller;
    opens com.example.client.controller to javafx.fxml;
}
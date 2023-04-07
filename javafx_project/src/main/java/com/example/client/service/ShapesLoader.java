package com.example.client.service;

import com.example.client.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;

import java.io.IOException;

public class ShapesLoader {

    public static Polyline loadPolyline(String resourceName) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Application.class.getResource(resourceName));
        return (Polyline) loader.load();
    }

    public static HBox loadHBox(String resourceName) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Application.class.getResource(resourceName));
        return (HBox) loader.load();
    }

    public static Line loadLine(String resourceName) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Application.class.getResource(resourceName));
        return (Line) loader.load();
    }
}

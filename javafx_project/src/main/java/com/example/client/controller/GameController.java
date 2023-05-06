package com.example.client.controller;

import com.example.client.entity.EventWrapper;
import com.example.client.entity.PlayersState;
import com.example.client.entity.GameState;
import com.example.client.service.ShapesLoader;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class GameController {
    @FXML
    private Circle targetBig;
    @FXML
    private Circle targetSmall;
    @FXML
    private TextField nameTextField;
    @FXML
    private Pane signupPane;
    @FXML
    private Pane winnerPane;
    @FXML
    private Label winnerLabel;
    @FXML
    private Pane gamePane;
    @FXML
    private VBox playersPane;
    @FXML
    private VBox scoreOwner;
    private static Gson gson = new Gson();

    private Line[] projectiles = new Line[4];
    private DataOutputStream out;
    private DataInputStream in;
    private double[] layouts = new double[4];
    private boolean linesInitialized = false;

    @FXML
    private void initialize() throws IOException {
        Socket socket = new Socket("localhost", 8080);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
    }

    @FXML
    protected void onStartButtonClick() throws IOException {
        if (!linesInitialized) {
            setLayouts();
            for (int i = 0; i < playersPane.getChildren().size(); i++) {
                Line line = ShapesLoader.loadLine("projectile.fxml");
                line.setLayoutY(layouts[i]);
                line.setLayoutX(96.0);
                line.setVisible(false);
                projectiles[i] = line;
                gamePane.getChildren().add(line);
            }
            linesInitialized = true;
        }
        out.writeUTF("start");
        out.flush();
    }

    @FXML
    protected void onStopButtonClick() throws IOException {
        out.writeUTF("stop");
        out.flush();
    }

    @FXML
    protected void onOkButtonClick() {
        winnerPane.setVisible(false);
    }

    @FXML
    protected void onShotButtonClick() throws IOException {
        out.writeUTF("shot");
        out.flush();
    }

    @FXML
    protected void onEnterButtonClick() throws IOException {
        String name = nameTextField.getText();
        out.writeUTF(name);
        out.flush();

        new Thread(() -> {
            while (true) {
                try {
                    EventWrapper eventWrapper = gson.fromJson(in.readUTF(), EventWrapper.class);
                    switch (eventWrapper.getEvent()) {
                        case ADD_PLAYERS -> addPlayers();
                        case ADD_PLAYER -> addPlayer();
                        case END_GAME -> endGame();
                        case UPDATE -> update();
                    }
                } catch (IOException ignored) {
                }
            }
        }).start();

        signupPane.setVisible(false);
    }

    private void update() throws IOException {
        GameState gameState = gson.fromJson(in.readUTF(), GameState.class);
        targetBig.setLayoutY(gameState.targetYCoords.get(0));
        targetSmall.setLayoutY(gameState.targetYCoords.get(1));

        for (int i = 0; i < playersPane.getChildren().size(); i++) {
            Line l = projectiles[i];

            HBox hBox = (HBox) scoreOwner.getChildren().get(i);
            Label shots = (Label) hBox.getChildren().get(1);
            Label score = (Label) hBox.getChildren().get(2);

            int finalI = i;
            Platform.runLater(
                    () -> {
                        l.setVisible(gameState.projectileXCoords.get(finalI) > 96.0);
                        l.setLayoutX(gameState.projectileXCoords.get(finalI));

                        shots.setText(gameState.shotsList.get(finalI).toString());
                        score.setText(gameState.scoreList.get(finalI).toString());
                    }
            );
        }
    }

    private void endGame() throws IOException {
        String winner = in.readUTF();
        Platform.runLater(
                () -> {
                    winnerLabel.setText(winner);
                    winnerPane.setVisible(true);
                }
        );
    }

    private void addPlayer() throws IOException {
        String data = in.readUTF();
        PlayersState list = gson.fromJson(data, PlayersState.class);
        HBox hBox = ShapesLoader.loadHBox("player-state.fxml");
        Label label = (Label) hBox.getChildren().get(0);
        label.setText(list.getPlayers().get(0));
        Platform.runLater(
                () -> scoreOwner.getChildren().add(hBox)
        );

        Polyline pl = ShapesLoader.loadPolyline("player-shape.fxml");
        Platform.runLater(
                () -> playersPane.getChildren().add(pl)
        );
    }

    private void addPlayers() throws IOException {
        String data = in.readUTF();
        PlayersState list = gson.fromJson(data, PlayersState.class);
        for (int i = 0; i < list.getPlayers().size() - 1; i++) {
            HBox hBox = ShapesLoader.loadHBox("player-state.fxml");
            Label label = (Label) hBox.getChildren().get(0);
            label.setText(list.getPlayers().get(i));
            Platform.runLater(
                    () -> scoreOwner.getChildren().add(hBox)
            );

            Polyline pl = ShapesLoader.loadPolyline("player-shape.fxml");
            Platform.runLater(
                    () -> playersPane.getChildren().add(pl)
            );
        }
        HBox hBox = ShapesLoader.loadHBox("player-state.fxml");
        Label label = (Label) hBox.getChildren().get(0);
        label.setText(list.getPlayers().get(list.getPlayers().size() - 1));
        Platform.runLater(
                () -> scoreOwner.getChildren().add(hBox)
        );

        Polyline pl = ShapesLoader.loadPolyline("client-player-shape.fxml");
        Platform.runLater(
                () -> playersPane.getChildren().add(pl)
        );
    }

    void setLayouts() {
        switch (playersPane.getChildren().size()) {
            case 1 -> {
                layouts[0] = 153;
            }
            case 2 -> {
                layouts[0] = 123;
                layouts[1] = 193;
            }
            case 3 -> {
                layouts[0] = 87;
                layouts[1] = 156;
                layouts[2] = 227;
            }
            case 4 -> {
                layouts[0] = 56;
                layouts[1] = 126;
                layouts[2] = 196;
                layouts[3] = 263;
            }
        }
    }
}

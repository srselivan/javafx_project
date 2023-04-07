package com.example.client.controller;

import com.example.client.entity.Action;
import com.example.client.entity.PlayersList;
import com.example.client.entity.Update;
import com.example.client.service.Border;
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
import javafx.scene.shape.Rectangle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameController {
    @FXML
    private Circle targetBig;
    @FXML
    private Circle targetSmall;
    @FXML
    private Rectangle gameField;
    @FXML
    private TextField nameInput;
    @FXML
    private Pane signUpPane;
    @FXML
    private Pane winnerPane;
    @FXML
    private Label winnerLabel;
    @FXML
    private Pane gameOwner;
    private List<Line> projectileOwner = new ArrayList<>(4);
    @FXML
    private VBox playersOwner;
    @FXML
    private VBox scoreOwner;
    private final String host = "localhost";
    private final int port = 8080;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private String name;
    private double[] layouts = new double[4];
    private boolean wasAdded = false;

    @FXML
    private void initialize() throws IOException {
        socket = new Socket(host, port);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
    }

    void setLayouts() {
        switch (playersOwner.getChildren().size()) {
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

    @FXML
    protected void onStartButtonClick() throws InterruptedException, IOException {
        if (!wasAdded) {
            setLayouts();
            for (int i = 0; i < playersOwner.getChildren().size(); i++) {
                Line l = ShapesLoader.loadLine("projectile.fxml");
                l.setLayoutY(layouts[i]);
                l.setLayoutX(96.0);
                l.setVisible(false);
                projectileOwner.add(l);
                gameOwner.getChildren().add(l);
            }
            wasAdded = true;
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
    protected void onOkButtonClick() throws IOException {
        winnerPane.setVisible(false);
    }

    @FXML
    protected void onShotButtonClick() throws IOException {
        out.writeUTF("shot");
        out.flush();
    }

    @FXML
    protected void onSaveButtonClick() throws IOException {
        Border border = new Border(gameField);
        System.out.println(border.top() + " " + border.bottom());

        name = nameInput.getText();
        out.writeUTF(name);
        out.flush();

        new Thread(() -> {
            boolean clientPlayerAdded = false;
            while (true) {
                try {
                    Action action = new Gson().fromJson(in.readUTF(), Action.class);
                    switch (action.action()) {
                        case ADD_PLAYERS -> {
                            String data = in.readUTF();
                            PlayersList list = new Gson().fromJson(data, PlayersList.class);
                            if (!list.players().isEmpty()) {
                                if (Objects.equals(list.players().get(0), name)) {
                                    break;
                                }
                            }
                            for (var player : list.players()) {
                                HBox hBox = ShapesLoader.loadHBox("player-state.fxml");
                                Label label = (Label) hBox.getChildren().get(0);
                                label.setText(player);
                                Platform.runLater(
                                        () -> scoreOwner.getChildren().add(hBox)
                                );

                                Polyline pl = ShapesLoader.loadPolyline("player-shape.fxml");
                                Platform.runLater(
                                        () -> playersOwner.getChildren().add(pl)
                                );
                            }
                            if (!clientPlayerAdded) {
                                HBox hBox = ShapesLoader.loadHBox("player-state.fxml");
                                Label label = (Label) hBox.getChildren().get(0);
                                label.setText(name);
                                Platform.runLater(
                                        () -> scoreOwner.getChildren().add(hBox)
                                );

                                Polyline pl = ShapesLoader.loadPolyline("client-player-shape.fxml");
                                Platform.runLater(
                                        () -> playersOwner.getChildren().add(pl)
                                );
                                clientPlayerAdded = true;
                            }
                        }
                        case END_GAME -> {
                            String winner = in.readUTF();
                            Platform.runLater(
                                    () -> {
                                        winnerLabel.setText(winner);
                                        winnerPane.setVisible(true);
                                    }
                            );
                        }
                        case UPDATE -> {
                            Update update = new Gson().fromJson(in.readUTF(), Update.class);
                            targetBig.setLayoutY(update.targetYCoords.get(0));
                            targetSmall.setLayoutY(update.targetYCoords.get(1));

                            for (int i = 0; i < playersOwner.getChildren().size(); i++) {
                                Line l = (Line) projectileOwner.get(i);

                                HBox hBox = (HBox) scoreOwner.getChildren().get(i);
                                Label shots = (Label) hBox.getChildren().get(1);
                                Label score = (Label) hBox.getChildren().get(2);

                                int finalI = i;
                                Platform.runLater(
                                        () -> {
                                            l.setVisible(update.projectileXCoords.get(finalI) > 96.0);
                                            l.setLayoutX(update.projectileXCoords.get(finalI));

                                            shots.setText(update.shotsList.get(finalI).toString());
                                            score.setText(update.scoreList.get(finalI).toString());
                                        }
                                );

                            }
                        }
                    }
                } catch (IOException ignored) {
                }
            }
        }).start();

        signUpPane.setVisible(false);
    }
}

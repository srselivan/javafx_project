package com.example.client.controller;

import com.example.client.entity.Action;
import com.example.client.entity.Player;
import com.example.client.entity.PlayersList;
import com.example.client.entity.Update;
import com.example.client.service.ShapesLoader;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameController {
    @FXML
    private Circle targetBig;
    @FXML
    private Circle targetSmall;
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
    @FXML
    private Pane leadersPane;
    @FXML
    private TableView<Player> leadersTable;
    @FXML
    private TableColumn<Player, String> nameColumn;
    @FXML
    private TableColumn<Player, Integer> winsColumn;
    private final String host = "localhost";
    private final int port = 8080;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private String name;
    private final double[] layouts = new double[4];
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
                            for (int i = 0; i < list.players().size() - 1; i++) {
                                HBox hBox = ShapesLoader.loadHBox("player-state.fxml");
                                Label label = (Label) hBox.getChildren().get(0);
                                label.setText(list.players().get(i));
                                Platform.runLater(
                                        () -> scoreOwner.getChildren().add(hBox)
                                );

                                Polyline pl = ShapesLoader.loadPolyline("player-shape.fxml");
                                Platform.runLater(
                                        () -> playersOwner.getChildren().add(pl)
                                );
                            }
                            HBox hBox = ShapesLoader.loadHBox("player-state.fxml");
                            Label label = (Label) hBox.getChildren().get(0);
                            label.setText(list.players().get(list.players().size() - 1));
                            Platform.runLater(
                                    () -> scoreOwner.getChildren().add(hBox)
                            );

                            Polyline pl = ShapesLoader.loadPolyline("client-player-shape.fxml");
                            Platform.runLater(
                                    () -> playersOwner.getChildren().add(pl)
                            );
                        }
                        case ADD_PLAYER -> {
                            String data = in.readUTF();
                            PlayersList list = new Gson().fromJson(data, PlayersList.class);
                            HBox hBox = ShapesLoader.loadHBox("player-state.fxml");
                            Label label = (Label) hBox.getChildren().get(0);
                            label.setText(list.players().get(0));
                            Platform.runLater(
                                    () -> scoreOwner.getChildren().add(hBox)
                            );

                            Polyline pl = ShapesLoader.loadPolyline("player-shape.fxml");
                            Platform.runLater(
                                    () -> playersOwner.getChildren().add(pl)
                            );
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
                                Line l = projectileOwner.get(i);

                                HBox hBox = (HBox) scoreOwner.getChildren().get(i);
                                Label shots = (Label) hBox.getChildren().get(1);
                                Label score = (Label) hBox.getChildren().get(2);
                                Label wins = (Label) hBox.getChildren().get(3);

                                int finalI = i;
                                Platform.runLater(
                                        () -> {
                                            l.setVisible(update.projectileXCoords.get(finalI) > 96.0);
                                            l.setLayoutX(update.projectileXCoords.get(finalI));

                                            shots.setText(update.shotsList.get(finalI).toString());
                                            score.setText(update.scoreList.get(finalI).toString());
                                            wins.setText(update.wins.get(finalI).toString());
                                        }
                                );

                            }
                        }
                        case WINNERS -> {
                            String data = in.readUTF();
                            PlayersList playersList = new Gson().fromJson(data, PlayersList.class);
                            ObservableList<Player> observableList = FXCollections.observableArrayList();

                            for (int i = 0; i < playersList.players().size(); i++) {
                                observableList.add(new Player(
                                        playersList.players().get(i).toString(),
                                        playersList.wins().get(i)
                                ));
                            }

                            Platform.runLater(
                                    () -> {
                                        nameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().name()));
                                        winsColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().winCount()));
                                        leadersTable.setItems(observableList);
                                        leadersTable.getSortOrder().add(winsColumn);
                                        leadersPane.setVisible(true);
                                    }
                            );
                        }
                    }
                } catch (IOException ignored) {
                }
            }
        }).start();

        signUpPane.setVisible(false);
    }

    @FXML
    protected void onCloseLeadersButtonClick() {
        leadersPane.setVisible(false);
        leadersTable.getItems().clear();
    }
    @FXML
    protected void onLeadersButtonClick() throws IOException {
        out.writeUTF("get_winners");
        out.flush();
    }
}

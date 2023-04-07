package server;

import com.google.gson.Gson;
import entity.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private static final int port = 8080;
    private final List<Socket> clientSockets = new ArrayList<>(4);
    private final List<DataInputStream> clientSocketsIn = new ArrayList<>(4);
    private final List<DataOutputStream> clientSocketsOut = new ArrayList<>(4);
    private final List<Projectile> projectiles = new ArrayList<>(4);
    private final List<Target> targets = new ArrayList<>(4);
    private final boolean[] shotsHandler = new boolean[4];
    private final int[] shotsCounter = new int[4];
    private final int[] scoreCounter = new int[4];
    private final PlayersList playersList = new PlayersList();
    private final ServerSocket srv;

    private AtomicInteger startConfirmCount = new AtomicInteger(0);

    public Server() throws IOException {
        srv = new ServerSocket(port);
        targets.add(new Target(5, 20, 446));
        targets.add(new Target(10, 10, 500));
    }

    public void start() throws IOException {
        while (clientSockets.size() < 2) {
            Socket socket = srv.accept();
            clientSockets.add(socket);
            Action action = new Action(Action.Actions.ADD_PLAYERS);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            clientSocketsIn.add(in);
            clientSocketsOut.add(out);
            projectiles.add(new Projectile(535));

            out.writeUTF(new Gson().toJson(action));
            out.flush();

            out.writeUTF(new Gson().toJson(playersList));
            out.flush();

            String name = in.readUTF();
            playersList.players().add(name);

            broadcast(action);
            int num = clientSockets.size() - 1;
            listenSocket(num);
        }

        while(startConfirmCount.get() < 2) {

        }

        new Thread(()-> {
            while (true) {
                try {
                    broadcast(new Action(Action.Actions.UPDATE));
                    Thread.sleep(30);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void listenSocket(int num) {
        DataInputStream in = clientSocketsIn.get(num);

        new Thread(() -> {
            while (true) {
                try {
                    String event = in.readUTF();
                    System.out.println(event);
                    switch (event) {
                        case "shot" -> {
                            shotsHandler[num] = true;
                            shotsCounter[num]++;
                        }
                        case "stop" -> {
                            //TODO: handle stop event
                        }
                        case "start" -> {
                            startConfirmCount.incrementAndGet();
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void broadcast(Action action) throws IOException {

        switch (action.action()) {
            case ADD_PLAYERS -> {
                for(var out : clientSocketsOut) {
                    int size = playersList.players().size();
                    PlayersList temp = new PlayersList();
                    temp.players().add(playersList.players().get(size - 1));

                    out.writeUTF(new Gson().toJson(action));
                    out.flush();

                    out.writeUTF(new Gson().toJson(temp));
                    out.flush();
                }
            }
            case STOP_GAME -> {
                //TODO: stop game
            }
            case UPDATE -> {
                Update update = new Update();
                for (int i = 0; i < 2; i++) {
                    if (shotsHandler[i]){
                        // 125 & 199
                        for(var target : targets) {
                            if (target.isInTarget(projectiles.get(i).x(), i == 1 ? 125 : 199)) {
                                projectiles.get(i).rollback();
                                shotsHandler[i] = false;
                                scoreCounter[i]++;
                                update.projectileXCoords.add(96.0);
                            }
                        }
                        if (!projectiles.get(i).isEnd()){
                            update.projectileXCoords.add(projectiles.get(i).move());
                        } else {
                            projectiles.get(i).rollback();
                            shotsHandler[i] = false;
                            update.projectileXCoords.add(96.0);
                        }
                    } else {
                        update.projectileXCoords.add(96.0);
                    }
                    update.shotsList().add(shotsCounter[i]);
                    update.scoreList().add(scoreCounter[i]);

                    System.out.println(scoreCounter[i] + " " + shotsCounter[i]);
                }

                for(var target : targets) {
                    update.targetYCoords.add(target.move());
                }

                for(var out : clientSocketsOut) {
                    out.writeUTF(new Gson().toJson(action));
                    out.flush();

                    out.writeUTF(new Gson().toJson(update));
                    out.flush();
                }
            }
        }
    }
}

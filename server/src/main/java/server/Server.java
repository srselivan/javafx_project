package server;

import com.google.gson.Gson;
import entity.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
public class Server {
    private static final int port = 8080;
    private final List<Socket> clientSockets = new ArrayList<>(4);
    private final List<DataInputStream> clientSocketsIn = new ArrayList<>(4);
    private final List<DataOutputStream> clientSocketsOut = new ArrayList<>(4);
    private final List<Projectile> projectiles = new ArrayList<>(4);
    private final List<Target> targets = new ArrayList<>(2);
    private final boolean[] shotsHandler = new boolean[4];
    private final int[] shotsCounter = new int[4];
    private final int[] scoreCounter = new int[4];
    private final double[] layouts = new double[4];
    private final PlayersList playersList = new PlayersList();
    private final ServerSocket srv;
    private String winner = "";
    private final AtomicBoolean isStopped = new AtomicBoolean(false);
    private final AtomicBoolean stopAccept = new AtomicBoolean(false);

    private final AtomicInteger startConfirmCount = new AtomicInteger(0);

    public Server() throws IOException {
        srv = new ServerSocket(port);
        targets.add(new Target(5, 20, 446));
        targets.add(new Target(10, 10, 500));
    }
    void setLayouts() {
        switch (clientSockets.size()) {
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

    public void start() throws InterruptedException {
        Thread acceptConn = new Thread(() -> {
            while (clientSockets.size() < 4) {
                try {
                    Socket socket = srv.accept();
                    if (stopAccept.get()) {
                        break;
                    }
                    clientSockets.add(socket);
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    clientSocketsIn.add(in);
                    clientSocketsOut.add(out);
                    projectiles.add(new Projectile(535));

                    Action action = new Action(Action.Actions.ADD_PLAYERS);
                    out.writeUTF(new Gson().toJson(action));
                    out.flush();

                    out.writeUTF(new Gson().toJson(playersList));
                    out.flush();

                    String name = in.readUTF();
                    playersList.players().add(name);

                    broadcast(action);
                    int num = clientSockets.size() - 1;
                    listenSocket(num);
                } catch (IOException ignored) {
                }
            }
        });

        acceptConn.start();

        while (startConfirmCount.get() != clientSockets.size() || startConfirmCount.get() == 0) {
        }
        acceptConn.interrupt();
        stopAccept.set(true);

        new Thread(()-> {
            setLayouts();
            while (true) {
                while (isStopped.get()) {
                }
                if (startConfirmCount.get() == clientSockets.size()) {
                    try {
                        if (!Objects.equals(getWinner(), "")) {
                            resetData();
                            broadcast(new Action(Action.Actions.UPDATE));
                            broadcast(new Action(Action.Actions.END_GAME));
                            isStopped.set(true);
                            startConfirmCount.set(0);
                            continue;
                        }
                        broadcast(new Action(Action.Actions.UPDATE));
                        Thread.sleep(30);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }

    private String getWinner() {
        for (int i = 0; i < 4; i++) {
            if (scoreCounter[i] >= 3) {
                winner = playersList.players().get(i);
            }
        }
        return winner;
    }

    private void resetData() {
        for (int i = 0; i < 4; i++) {
            scoreCounter[i] = 0;
            shotsCounter[i] = 0;
            shotsHandler[i] = false;
        }
        targets.set(0, new Target(5, 20, 446));
        targets.set(1, new Target(10, 10, 500));
        for(var projectile : projectiles) {
            projectile.rollback();
        }
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
                            if (!shotsHandler[num] && !isStopped.get()) {
                                shotsHandler[num] = true;
                                shotsCounter[num]++;
                            }
                        }
                        case "stop" -> {
                            if(!isStopped.get()) {
                                isStopped.set(true);
                                startConfirmCount.set(0);
                            }
                        }
                        case "start" -> {
                            startConfirmCount.incrementAndGet();
                            System.out.println(startConfirmCount.get() + " " + clientSockets.size());
                            if (startConfirmCount.get() == clientSockets.size()) {
                                isStopped.set(false);
                            }
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
            case END_GAME -> {
                for(var out : clientSocketsOut) {
                    out.writeUTF(new Gson().toJson(action));
                    out.flush();

                    out.writeUTF(new Gson().toJson(winner));
                    out.flush();
                }
                winner = "";
            }
            case UPDATE -> {
                Update update = new Update();
                for (int i = 0; i < clientSockets.size(); i++) {
                    if (shotsHandler[i]){
                        if (!projectiles.get(i).isEnd()){
                            double x = projectiles.get(i).move();
                            for(var target : targets) {
                                if (target.isInTarget(x, layouts[i])) {
                                    projectiles.get(i).rollback();
                                    shotsHandler[i] = false;
                                    scoreCounter[i]++;
                                }
                            }
                        } else {
                            projectiles.get(i).rollback();
                            shotsHandler[i] = false;
                        }
                    }
                    update.projectileXCoords.add(projectiles.get(i).x());
                    update.shotsList().add(shotsCounter[i]);
                    update.scoreList().add(scoreCounter[i]);
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

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
    private final PlayersState playersState = new PlayersState();
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

                    String name = in.readUTF();
                    playersState.getPlayers().add(validateName(name));

                    out.writeUTF(new Gson().toJson(new EventWrapper(EventWrapper.Event.ADD_PLAYERS)));
                    out.flush();

                    out.writeUTF(new Gson().toJson(playersState));
                    out.flush();

                    broadcast(new EventWrapper(EventWrapper.Event.ADD_PLAYER));
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
                            broadcast(new EventWrapper(EventWrapper.Event.UPDATE));
                            broadcast(new EventWrapper(EventWrapper.Event.END_GAME));
                            isStopped.set(true);
                            startConfirmCount.set(0);
                            continue;
                        }
                        broadcast(new EventWrapper(EventWrapper.Event.UPDATE));
                        Thread.sleep(30);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }

    private String validateName(String name) {
        String tmp = name;
        int counter = 0;
        while (true) {
            if (playersState.getPlayers().contains(tmp)) {
                tmp += Integer.toString(counter++);
            } else {
                return tmp;
            }
        }
    }

    private String getWinner() {
        for (int i = 0; i < 4; i++) {
            if (scoreCounter[i] >= 3) {
                winner = playersState.getPlayers().get(i);
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

    private void broadcast(EventWrapper eventWrapper) throws IOException {
        switch (eventWrapper.getEvent()) {
            case ADD_PLAYER -> {
                for (int i = 0; i < clientSocketsOut.size() - 1 ; i++) {
                    int size = playersState.getPlayers().size();
                    PlayersState temp = new PlayersState();
                    temp.getPlayers().add(playersState.getPlayers().get(size - 1));

                    clientSocketsOut.get(i).writeUTF(new Gson().toJson(eventWrapper));
                    clientSocketsOut.get(i).flush();

                    clientSocketsOut.get(i).writeUTF(new Gson().toJson(temp));
                    clientSocketsOut.get(i).flush();
                }
            }
            case END_GAME -> {
                for(var out : clientSocketsOut) {
                    out.writeUTF(new Gson().toJson(eventWrapper));
                    out.flush();

                    out.writeUTF(new Gson().toJson(winner));
                    out.flush();
                }
                winner = "";
            }
            case UPDATE -> {
                GameState gameState = new GameState();
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
                    gameState.projectileXCoords.add(projectiles.get(i).x());
                    gameState.shotsList().add(shotsCounter[i]);
                    gameState.scoreList().add(scoreCounter[i]);
                }

                for(var target : targets) {
                    gameState.targetYCoords.add(target.move());
                }

                for(var out : clientSocketsOut) {
                    out.writeUTF(new Gson().toJson(eventWrapper));
                    out.flush();

                    out.writeUTF(new Gson().toJson(gameState));
                    out.flush();
                }
            }
        }
    }
}

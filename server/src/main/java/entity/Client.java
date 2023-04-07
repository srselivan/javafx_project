package entity;

import java.io.*;
import java.net.Socket;

public class Client {
    private String name;
    private final Socket socket;
    private int shotsCount;
    private int score;
    private final int position;


    public Client(Socket socket, int position) {
        this.socket = socket;
        this.position = position;

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            name = in.readLine();
            in.close();
        } catch (IOException ignored) {
        }

    }
}

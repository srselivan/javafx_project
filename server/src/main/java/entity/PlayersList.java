package entity;

import java.util.ArrayList;
import java.util.List;

public class PlayersList {
    public List<String> players;

    public PlayersList() {
        players = new ArrayList<>(4);
    }

    public List<String> players() {
        return players;
    }
}

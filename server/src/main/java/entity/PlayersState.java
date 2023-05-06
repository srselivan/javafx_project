package entity;

import java.util.ArrayList;
import java.util.List;

public class PlayersState {
    public List<String> players;

    public PlayersState() {
        players = new ArrayList<>(4);
    }

    public List<String> getPlayers() {
        return players;
    }
}

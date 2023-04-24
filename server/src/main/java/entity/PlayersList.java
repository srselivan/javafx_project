package entity;

import model.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayersList {
    public List<String> players;
    public List<Integer> wins;

    public PlayersList() {
        players = new ArrayList<>(4);
        wins = new ArrayList<>(4);
    }

    public PlayersList(List<Player> players) {
        this.players = new ArrayList<>(players.size());
        this.wins = new ArrayList<>(players.size());

        for (var player : players) {
            System.out.println(player.name() + player.winCount());
            this.players.add(player.name());
            this.wins.add(player.winCount());
        }
    }

    public List<String> players() {
        return players;
    }

    public List<Integer> wins() {
        return wins;
    }
}

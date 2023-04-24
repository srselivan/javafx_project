package model;

import jakarta.persistence.*;

@Entity
@Table(name = "players")
public class Player {
    @Id
    @Column(name = "name")
    private String name;
    @Column(name = "wins")
    private int winCount;

    public Player() {
    }

    public Player(String name, int winCount) {
        this.name = name;
        this.winCount = winCount;
    }

    public String name() {
        return name;
    }

    public int winCount() {
        return winCount;
    }
}

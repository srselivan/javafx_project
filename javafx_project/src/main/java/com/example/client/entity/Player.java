package com.example.client.entity;

import javafx.beans.property.Property;

public class Player {
    private String name;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setWinCount(int winCount) {
        this.winCount = winCount;
    }
}

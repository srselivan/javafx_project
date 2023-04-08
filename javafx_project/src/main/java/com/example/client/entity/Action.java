package com.example.client.entity;

public class Action {
    public enum Actions {
        ADD_PLAYERS,
        ADD_PLAYER,
        END_GAME,
        UPDATE,
    }

    public Actions action;

    public Action(Actions action) {
        this.action = action;
    }

    public Actions action() {
        return action;
    }
}

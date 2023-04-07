package com.example.client.entity;

public class Action {
    public enum Actions {
        ADD_PLAYERS,
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

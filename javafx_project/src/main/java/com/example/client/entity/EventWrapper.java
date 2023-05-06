package com.example.client.entity;

public class EventWrapper {
    public enum Event {
        ADD_PLAYERS,
        ADD_PLAYER,
        END_GAME,
        UPDATE,
    }

    public Event event;

    public EventWrapper(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }
}

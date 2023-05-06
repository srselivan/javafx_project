package entity;

public class EventWrapper {
    public enum Event {
        ADD_PLAYERS,
        ADD_PLAYER,
        UPDATE,
        END_GAME,
    }

    public Event event;

    public EventWrapper(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }
}

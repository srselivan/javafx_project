package entity;

public class Action {
    public enum Actions {
        ADD_PLAYERS,
        ADD_PLAYER,
        UPDATE,
        END_GAME,
    }

    public Actions action;

    public Action(Actions action) {
        this.action = action;
    }

    public Actions action() {
        return action;
    }
}

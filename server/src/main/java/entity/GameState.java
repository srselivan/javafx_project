package entity;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    public List<Double> projectileXCoords;
    public List<Double> targetYCoords;
    public List<Integer> scoreList;
    public List<Integer> shotsList;

    public GameState() {
        this.projectileXCoords = new ArrayList<>(4);
        this.targetYCoords = new ArrayList<>(2);
        this.scoreList = new ArrayList<>(4);
        this.shotsList = new ArrayList<>(4);
    }

    public List<Integer> scoreList() {
        return scoreList;
    }

    public List<Integer> shotsList() {
        return shotsList;
    }
}

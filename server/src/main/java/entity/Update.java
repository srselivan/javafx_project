package entity;

import java.util.ArrayList;
import java.util.List;

public class Update {
    public List<Double> projectileXCoords;
    public List<Double> targetYCoords;
    public List<Integer> scoreList;
    public List<Integer> shotsList;

    public Update() {
        this.projectileXCoords = new ArrayList<>(4);
        this.targetYCoords = new ArrayList<>(2);
        this.scoreList = new ArrayList<>(4);
        this.shotsList = new ArrayList<>(4);
    }

    public List<Double> projectileXCoords() {
        return projectileXCoords;
    }

    public List<Double> targetYCoords() {
        return targetYCoords;
    }

    public List<Integer> scoreList() {
        return scoreList;
    }

    public List<Integer> shotsList() {
        return shotsList;
    }
}

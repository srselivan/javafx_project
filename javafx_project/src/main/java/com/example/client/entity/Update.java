package com.example.client.entity;

import java.util.List;

public class Update {
    public List<Double> projectileXCoords;
    public List<Double> targetYCoords;
    public List<Integer> scoreList;
    public List<Integer> shotsList;

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

package com.example.client.service;

import javafx.scene.control.Label;

import java.util.concurrent.atomic.AtomicInteger;

public class Counter{
    private final AtomicInteger shotsCount = new AtomicInteger();
    private final AtomicInteger scoreCount = new AtomicInteger();
    private final Label shotsLabel;
    private final Label scoreLabel;

    public Counter(Label shotsLabel, Label scoreLabel) {
        this.shotsLabel = shotsLabel;
        this.scoreLabel = scoreLabel;
    }

    public void incrementShotsCount() {
        shotsCount.incrementAndGet();
        shotsLabel.setText(shotsCount.toString());
    }

    public void incrementScoreCount() {
        scoreCount.incrementAndGet();
        scoreLabel.setText(scoreCount.toString());
    }
}


package com.example.client.service;

import javafx.scene.shape.Circle;

public class CircleTarget implements Target{
    private int direction = 1;
    private final double radius;
    private final Circle circle;
    private final Border border;
    private final int step;

    public CircleTarget(Circle circle, Border border, int step) {
        this.circle = circle;
        this.border = border;
        this.step = step;
        radius = circle.getRadius();
    }

    @Override
    public void move() {
        double layoutY = circle.getLayoutY();
        if (layoutY + radius * direction + step * direction > border.bottom() || layoutY + radius * direction + step * direction < border.top()) {
            direction *= -1;
        }
        circle.setLayoutY(layoutY + step * direction);
    }

    @Override
    public boolean isInTarget(double x, double y) {
        if (Math.abs(circle.getLayoutX() - x) <= radius) {
            return Math.abs(circle.getLayoutY() - y) <= radius;
        }

        return false;
    }
}

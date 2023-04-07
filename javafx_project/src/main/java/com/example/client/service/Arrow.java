package com.example.client.service;

import javafx.scene.shape.Line;

public class Arrow implements Projectile{
    private final Line line;
    private final double width;
    private final double startX;
    private final double startY;
    private final double step;
    private final Border border;

    public Arrow(Line line, Border border, double step) {
        this.line = line;
        this.border = border;
        this.step = step;
        width = line.getEndX() - line.getStartX();
        startX = line.getLayoutX();
        startY = line.getLayoutY();
        line.setVisible(false);
    }

    @Override
    public void move() throws Exception {
        line.setVisible(true);
        double layoutX = line.getLayoutX();

        if (layoutX + step + width > border.right()) {
            line.setVisible(false);
            throw new Exception();
        } else {
            line.setLayoutX(layoutX + step);
        }

        try {
            Thread.sleep(30);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void rollback() {
        line.setVisible(false);
        line.setLayoutX(startX);
        line.setLayoutY(startY);
    }

    public double getX(){
        return line.getLayoutX() + width;
    }

    public double getY() {
        return line.getLayoutY();
    }
}

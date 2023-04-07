package com.example.client.service;

import javafx.scene.shape.Rectangle;

public class Border {
    private final double top;
    private final double bottom;
    private final double right;
    private final double left;

    public Border(Rectangle rectangle) {
        top = rectangle.getLayoutY();
        bottom = top + rectangle.getHeight();
        left = rectangle.getLayoutX();
        right = left + rectangle.getWidth();
    }

    public double top() {
        return top;
    }

    public double bottom() {
        return bottom;
    }

    public double right() {
        return right;
    }

    public double left() {
        return left;
    }
}

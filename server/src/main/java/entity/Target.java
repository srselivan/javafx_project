package entity;

public class Target {
    private final double top = 19.0;
    private final double bottom = 309.0;
    private final double step;
    private final double rad;
    private int direction = 1;
    private double y = 150.0;
    private final double x;

    public Target(double step, double rad, double x) {
        this.step = step;
        this.rad = rad;
        this.x = x;
    }

    public double move() {
        double next = y + rad * direction + step * direction;
        if (next > bottom || next < top) {
            direction *= -1;
        }
        y = y + step * direction;
        return y;
    }

    public boolean isInTarget(double x, double y) {
        if (Math.abs(this.x - x) <= rad) {
            return Math.abs(this.y - y) <= rad;
        }
        return false;
    }
}

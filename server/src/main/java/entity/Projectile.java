package entity;

public class Projectile {
    private double x = 96;
    private final double step = 10;
    private final double startX = 96;
    private final double endX;
    private final double width = 30;


    public Projectile(double endX) {
        this.endX = endX;
    }

    public void rollback() {
        x = startX;
    }

    public double move() {
        x += step;
        return x;
    }

    public boolean isEnd() {
        return x + step >= endX;
    }

    public double x() {
        return x;
    }
}

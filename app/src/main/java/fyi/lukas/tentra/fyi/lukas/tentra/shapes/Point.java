package fyi.lukas.tentra.fyi.lukas.tentra.shapes;

public class Point {
    public float X, Y;

    public Point(float X, float Y) {
        this.X = X;
        this.Y = Y;
    }

    public void moveTowards(Point center, float factor) {
        X = (center.X - X) * factor + X;
        Y = (center.Y - Y) * factor + Y;
    }

    @Override
    public Point clone() {
        return new Point(X, Y);
    }
}


package fyi.lukas.tentra.fyi.lukas.tentra.shapes;

import android.graphics.Paint;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.Random;

public class Dot {
    public float X,Y;
    public float RADIUS;
    public boolean isPopped;
    private Vector2D direction;
    private Paint paint;
    private static double angle = 0;

    private static Paint getRandomPaint() {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        Random r = new Random();
        paint.setARGB(255, r.nextInt(256), r.nextInt(256), r.nextInt(256));
        return paint;
    }

    private static Vector2D getRandomDirection() {
        Random r = new Random();
        angle = (angle+0.3)%(2*Math.PI);
        //double angle = r.nextFloat()*2*Math.PI;
        return new Vector2D(Math.sin(angle), Math.cos(angle));
    }

    public Dot(float X, float Y, float RADIUS) {
        this(X, Y, RADIUS, getRandomPaint());
    }

    public Dot(float X, float Y, float RADIUS, Paint paint) {
        this.X = X;
        this.Y = Y;
        this.RADIUS = RADIUS;
        this.paint = paint;
        this.direction = getRandomDirection();
        isPopped = false;
    }

    public void step(float stepLength, float growth) {
        X = X+(float)direction.getX()*stepLength;
        Y = Y+(float)direction.getY()*stepLength;
        RADIUS*=growth;
    }

    public Paint getPaint() {
        return paint;
    }

    public void hide() {
        paint.setAlpha(0);
    }
}
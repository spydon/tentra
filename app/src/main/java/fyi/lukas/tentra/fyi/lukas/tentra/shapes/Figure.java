package fyi.lukas.tentra.fyi.lukas.tentra.shapes;

import android.graphics.*;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Figure extends Path {
    private Paint paint;
    private Vector2D direction;
    private static double angle = 0;

    public Figure() {
        this(getRandomPaint());
    }

    public Figure(Paint paint) {
        super();
        this.paint = paint;
        this.direction = getRandomDirection();
    }

    public Point getCenter() {
        RectF rectF = new RectF();
        computeBounds(rectF, true);
        return new Point(rectF.centerX(), rectF.centerY());
    }

    public Paint getPaint() {
        return paint;
    }

    public void step(float stepLength) {
        //for(Point point : this) {
        //    point.X = point.X + (float) direction.getX() * stepLength;
        //    point.Y = point.Y + (float) direction.getY() * stepLength;
        //}
        Matrix matrix = new Matrix();
        matrix.setTranslate((float)direction.getX()*stepLength, (float)direction.getY()*stepLength);
        transform(matrix);
    }

    private static Paint getRandomPaint() {
        Paint paint = new Paint();
        //paint.setStyle(Paint.Style.FILL);
        //paint.setStyle(Paint.Style.STROKE);
        //paint.setStrokeWidth(5);
        Random r = new Random();
        paint.setStyle(Paint.Style.STROKE);
        //paint.setColor(r.nextInt(Integer.MAX_VALUE)*-1);
        paint.setARGB(255, r.nextInt(256), r.nextInt(256), r.nextInt(256));
        //paint.setShadowLayer(12, 0, 0, Color.RED);
        return paint;
    }

    private static Vector2D getRandomDirection() {
        Random r = new Random();
        angle = (angle+0.3)%(2*Math.PI);
        //double angle = r.nextFloat()*2*Math.PI;
        return new Vector2D(Math.sin(angle), Math.cos(angle));
    }

    public void scale(float factor) {
        Matrix scaleMatrix = new Matrix();
        Point center = getCenter();
        scaleMatrix.setScale(factor, factor, center.X, center.Y);
        transform(scaleMatrix);
    }

    public void updateCenter(Point center) {
        Matrix matrix = new Matrix();
        RectF rectF = new RectF();
        computeBounds(rectF, true);
        matrix.setTranslate(-1*(rectF.centerX()-center.X), -1*(rectF.centerY()-center.Y));
        transform(matrix);
    }

    @Override
    public Figure clone() {
        Paint paint = new Paint();
        paint.setColor(this.paint.getColor());
        paint.setStyle(Paint.Style.STROKE);
        Figure figure = new Figure(paint);
        figure.set(this);
        //for(Point point : this) {
        //    figure.add(point.clone());
        //}
        return figure;
    }
}


package fyi.lukas.tentra.fyi.lukas.tentra.shapes;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Figure extends CopyOnWriteArrayList<Point> {
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
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;

        for(Point point : this) {
            // Use Float.max/min once API 24
            maxX = maxX < point.X ? point.X : maxX;
            maxY = maxY < point.Y ? point.Y : maxY;
            minX = minX > point.X ? point.X : minX;
            minY = minY > point.Y ? point.Y : minY;
        }

        return new Point(((maxX - minX) / 2) + minX, ((maxY - minY) / 2) + minY);
    }

    public Paint getPaint() {
        return paint;
    }

    public void step(float stepLength) {
        for(Point point : this) {
            point.X = point.X + (float) direction.getX() * stepLength;
            point.Y = point.Y + (float) direction.getY() * stepLength;
        }
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
        Point center = getCenter();
        int x = 0;
        for (Point point : this) {
            if(x%3==0) {
                point.moveTowards(center, factor);
            } else {
                this.remove(point);
            }
            x++;
        }
    }

    public void updateCenter(Point center) {
        Point oldCenter = getCenter();
        for(Point point : this) {
            point.X = center.X+oldCenter.X-point.X;
            point.Y = center.Y+oldCenter.Y-point.Y;
        }
    }

    @Override
    public Figure clone() {
        Figure figure = new Figure(paint);
        Path path = new Path();
        for(Point point : this) {
            figure.add(point.clone());
        }
        return figure;
    }
}


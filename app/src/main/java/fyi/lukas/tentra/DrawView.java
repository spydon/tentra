package fyi.lukas.tentra;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import fyi.lukas.tentra.fyi.lukas.tentra.shapes.Dot;
import fyi.lukas.tentra.fyi.lukas.tentra.shapes.Figure;
import fyi.lukas.tentra.fyi.lukas.tentra.shapes.Point;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class DrawView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private AnimationThread thread;
    private boolean notKilled;
    private GestureDetector mDetector;
    private Figure figure;

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        mDetector = new GestureDetector(this.getContext(), new gestureListener());
        figure = new Figure();
        notKilled = true;
        thread = new AnimationThread(new Point(getWidth() / 2, getHeight() / 2), getHolder());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        notKilled = false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        //thread.setCenter(new Point(motionEvent.getX(), motionEvent.getY()));
        boolean result = mDetector.onTouchEvent(event);
        if (!result) {
            figure.add(new Point(event.getX(), event.getY()));
            if (event.getAction() == MotionEvent.ACTION_UP) {
                thread.addFigure(deepCopyFigure());
                figure.clear();
                result = true;
            }
        }
        return result;
    }

    private Figure deepCopyFigure() {
        Figure copy = new Figure();
        for(Point p : figure) {
            copy.add(p.clone());
        }
        return copy;
    }

    class gestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    class AnimationThread extends Thread {
        private final SurfaceHolder surfaceHolder;
        private Paint paint;
        private float spawnRate = 1F;
        private int refreshRate = 20;
        private int stepLength = 10;
        private float growth = 1.015F;
        private int canvasWidth;
        private int canvasHeight;
        private Point center;
        private CopyOnWriteArrayList<Figure> figures;
        private CopyOnWriteArrayList<Figure> activeFigures;

        public AnimationThread(Point center, SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
            this.center = center;
            this.figures = new CopyOnWriteArrayList<>();
            this.activeFigures = new CopyOnWriteArrayList<>();
            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.CYAN);
        }

        private void updateDimensions(Canvas canvas) {
            canvasWidth = canvas.getWidth();
            canvasHeight = canvas.getHeight();
        }

        private void stepFigures() {
            for(Figure figure : activeFigures) {
                Point center = figure.getCenter();
                if (center.X > canvasWidth || center.X < 0 || center.Y > canvasHeight || center.Y < 0) {
                    //center.hide();
                    activeFigures.remove(figure);
                } else {
                    figure.step(stepLength);
                }
            }

            Random r = new Random();
            if(r.nextFloat() < spawnRate && figures.size() > 0) {
                Figure figure = figures.get(r.nextInt(figures.size())).clone();
                figure.updateCenter(new Point(getWidth()/2, getHeight()/2));
                activeFigures.add(figure);
            }

        }

        @Override
        public void run() {
            Canvas canvas;
            while(notKilled) {
                synchronized (surfaceHolder) {
                    canvas = surfaceHolder.lockCanvas(null);
                    stepFigures();

                    if(canvas != null) {
                        updateDimensions(canvas);
                        doDraw(canvas);
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }

                try {
                    sleep(refreshRate);
                } catch (InterruptedException ie) {
                    // Crash
                }
            }
        }

        public void addFigure(Figure figure) {
            figure.scale(0.8F);
            figure.updateCenter(new Point(getWidth()/2, getHeight()/2));
            this.figures.add(figure);
            this.activeFigures.add(figure);
        }

        private void drawFigure(Figure figure, Canvas canvas) {
            for (int i = 0; i < figure.size() - 1; i++) {
                Point current = figure.get(i);
                Point next = figure.get(i + 1);
                canvas.drawLine(current.X, current.Y, next.X, next.Y, figure.getPaint());
            }
        }

        private void doDraw(Canvas canvas) {
            canvas.drawColor(Color.BLACK);

            //for (Dot dot : dots) {
            //    canvas.drawCircle(dot.X, dot.Y, dot.RADIUS, dot.getPaint());
            //}

            //for (int i = 0; i < dots.size()-1; i++) {
            //    Dot current = dots.get(i);
            //    Dot next = dots.get(i+1);
            //    canvas.drawLine(current.X, current.Y, next.X, next.Y, current.getPaint());
            //}

            // Draw past figures
            figures.iterator();
            Iterator<Figure> iter = activeFigures.iterator();
            while(iter.hasNext()) {
                Figure figure = iter.next();
                drawFigure(figure, canvas);
            }

            // Draw current figure
            drawFigure(figure, canvas);

            canvas.save();
        }

        private void setCenter(Point point) {
            this.center = point;
        }
    }
}

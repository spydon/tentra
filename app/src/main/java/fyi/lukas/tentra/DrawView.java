package fyi.lukas.tentra;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.*;
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
        figure = new Figure(getRandomPaint(5));
        notKilled = true;
        thread = new AnimationThread(getHolder());
    }

    private static Paint getRandomPaint(int width) {
        Paint paint = new Paint();
        //paint.setStyle(Paint.Style.FILL);
        //paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(width);
        Random r = new Random();
        paint.setStyle(Paint.Style.STROKE);
        //paint.setColor(r.nextInt(Integer.MAX_VALUE)*-1);
        paint.setARGB(255, r.nextInt(256), r.nextInt(256), r.nextInt(256));
        //paint.setShadowLayer(12, 0, 0, Color.RED);
        return paint;
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
            if(figure.isEmpty()) {
                figure.moveTo(event.getX(), event.getY());
            }
            figure.lineTo(event.getX(), event.getY());
            if (event.getAction() == MotionEvent.ACTION_UP) {
                thread.addFigure(figure.clone());
                figure = new Figure(getRandomPaint(5));
                result = true;
            }
        }
        return result;
    }

    class gestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    class AnimationThread extends Thread {
        private final SurfaceHolder surfaceHolder;
        private float spawnRate = 0.07F;
        private int refreshRate = 20;
        private int stepLength = 10;
        private float growth = 1.015F;
        private int canvasWidth;
        private int canvasHeight;
        private CopyOnWriteArrayList<Figure> figures;
        private CopyOnWriteArrayList<Figure> activeFigures;

        public AnimationThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
            this.figures = new CopyOnWriteArrayList<>();
            this.activeFigures = new CopyOnWriteArrayList<>();
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
                Figure randFigure = figures.get(r.nextInt(figures.size()));
                for(int i = 0; i < 21; i++) {
                    Figure figure = randFigure.clone();
                    figure.updateCenter(new Point(getWidth() / 2, getHeight() / 2));
                    activeFigures.add(figure);
                }
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
        }

        private void drawFigure(Figure figure, Canvas canvas) {
            canvas.drawPath(figure, figure.getPaint());
        }

        private void doDraw(Canvas canvas) {
            canvas.drawColor(Color.BLACK);

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

    }
}

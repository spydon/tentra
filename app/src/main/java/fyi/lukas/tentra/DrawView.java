package fyi.lukas.tentra;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.*;
import fyi.lukas.tentra.fyi.lukas.tentra.shapes.Dot;

import java.util.Iterator;
import java.util.Vector;

public class DrawView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private AnimationThread thread;
    private boolean notKilled;

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        notKilled = true;
        thread = new AnimationThread(new Click(getWidth() / 2, getHeight() / 2), getHolder());
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
    public boolean onTouch(View view, MotionEvent motionEvent) {
        thread.setCenter(new Click(motionEvent.getX(), motionEvent.getY()));
        return false;
    }

    private class Click {
        private float X, Y;

        public Click(float X, float Y) {
            this.X = X;
            this.Y = Y;
        }
    }

    class AnimationThread extends Thread {
        private final SurfaceHolder surfaceHolder;
        private Paint paint;
        private Vector<Dot> dots;
        private int spawnRate = 3;
        private int refreshRate = 20;
        private int stepLength = 10;
        private float growth = 1.015F;
        private int canvasWidth;
        private int canvasHeight;
        private Click center;

        public AnimationThread(Click center, SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
            this.center = center;
            dots = new Vector<>();
            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.CYAN);
        }

        private void updateDimensions(Canvas canvas) {
            canvasWidth = canvas.getWidth();
            canvasHeight = canvas.getHeight();
        }

        private void stepDots() {
            Iterator<Dot> iter = dots.iterator();
            while(iter.hasNext()) {
                Dot dot = iter.next();
                if(dot.X > canvasWidth || dot.X < 0 || dot.Y > canvasHeight || dot.Y < 0) {
                    //dot.hide();
                    iter.remove();
                } else {
                    dot.step(stepLength, growth);
                }
            }

            for(int x = 0; x < spawnRate; x++) {
                dots.add(new Dot(center.X, center.Y, 3));
            }

        }

        @Override
        public void run() {
            Canvas canvas;
            while(notKilled) {
                synchronized (surfaceHolder) {
                    canvas = surfaceHolder.lockCanvas(null);
                    stepDots();

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

        private void doDraw(Canvas canvas) {
            canvas.drawColor(Color.BLACK);

            for (Dot dot : dots) {
                canvas.drawCircle(dot.X, dot.Y, dot.RADIUS, dot.getPaint());
            }

            for (int i = 0; i < dots.size()-1; i++) {
                Dot current = dots.get(i);
                Dot next = dots.get(i+1);
                canvas.drawLine(current.X, current.Y, next.X, next.Y, current.getPaint());
            }
            //drawPath(path, paint);
            //canvas.drawCircle(x,y,size,paint);
            canvas.save();
        }

        private void setCenter(Click click) {
            this.center = click;
        }
    }
}

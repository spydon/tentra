package fyi.lukas.tentra;

import android.content.Context;
import android.graphics.*;
import android.media.*;
import android.media.audiofx.Visualizer;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import fyi.lukas.tentra.fyi.lukas.tentra.shapes.Figure;
import fyi.lukas.tentra.fyi.lukas.tentra.shapes.Point;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

public class DrawView extends GLSurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private AnimationThread thread;
    private RecordingThread recordingThread;
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
        //recordingThread = new RecordingThread(thread);
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
        paint.setShadowLayer(12, 0, 0, Color.RED);
        return paint;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        thread.start();
        //micThread.start();
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
                figure.setFirst(event.getX(), event.getY());
                figure.moveTo(event.getX(), event.getY());
            }
            figure.lineTo(event.getX(), event.getY());
            if (event.getAction() == MotionEvent.ACTION_UP) {
                figure.setLast(event.getX(), event.getY());
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
        private int refreshRate = 10;
        private int stepLength = 10;
        private float growth = 1.015F;
        private int amplitude = 1000;
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

        public void setAmplitude(int amplitude) {
            System.out.println(amplitude);
            this.amplitude = amplitude;
        }

        private void stepFigures() {
            for(Figure figure : activeFigures) {
                if(figure.isOutside(canvasWidth, canvasHeight)) {
                    activeFigures.remove(figure);
                } else {
                    figure.step(stepLength);
                }
            }

            Random r = new Random();
            if(r.nextFloat() < spawnRate && figures.size() > 0 && amplitude > 100) {
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
            RectF r = new RectF();
            figure.computeBounds(r, false);
            Point center = new Point(getWidth()/2, getHeight()/2);
            if(r.height() < 10 && r.width() < 10) { // Add ball since figure is too small
                figure.reset();
                figure.setFilled(new Random().nextBoolean());
                figure.addCircle(center.X, center.Y, 10F, Path.Direction.CW);
                figure.close();
            } else {
                figure.maybeClose();
                figure.scale(0.8F);
                figure.updateCenter(center);
            }
            this.figures.add(figure);
            this.activeFigures.add(figure);
        }

        private void drawFigure(Figure figure, Canvas canvas) {
            canvas.drawPath(figure, figure.getPaint());
        }

        private void doDraw(Canvas canvas) {
            canvas.drawColor(Color.BLACK);

            // Draw past figures
            //figures.iterator();
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

    class MicThread extends Thread {
        private Visualizer visualizer;
        private Visualizer.OnDataCaptureListener vListener;
        private MediaRecorder recorder;
        private MediaPlayer player;
        private String file;
        private boolean isRecording = true;
        private int recordingRate = 2000;

        public MicThread() {
            //this.player = new MediaPlayer();
            //this.recorder = new MediaRecorder();
            player = MediaPlayer.create(getContext(), R.raw.test);
            player.setLooping(true);
            player.start();
            this.visualizer = new Visualizer(player.getAudioSessionId());
            this.vListener = new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                    System.out.println(bytes.length);
                    System.out.println(bytes[290]);
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                    System.out.println("onFftDataCapture");
                }
            };
            visualizer.setDataCaptureListener(vListener, Visualizer.getMaxCaptureRate() / 2, true, false);
            visualizer.setEnabled(true);
            //recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            //recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            //try {
            //    file = Environment.getExternalStorageDirectory().getAbsolutePath() + "/cache.3pg";
            //    recorder.setOutputFile(file);
            //    recorder.prepare();
            //} catch (IOException ioe) {
            //    Log.e("tentra", "MEMORYFILE ERROR");
            //}
        }

        @Override
        public void run() {

        }

        //private class StopRecording extends TimerTask {
        //    public void run() {
        //        stopRecording();
        //    }
        //}

        //@Override
        //public void run() {
        //    StopRecording timerTask = new StopRecording();
        //    recorder.start();
        //    new Timer().schedule(timerTask, recordingRate);
        //}

        //public void stopRecording() {
        //    recorder.stop();
        //    recorder.release();
        //    startPlaying();
        //}

        //private void startPlaying() {
        //    try {
        //        player.setDataSource(file);
        //        player.prepare();
        //        player.start();
        //    } catch (IOException e) {
        //        Log.e("tentra", "prepare() failed");
        //    }
        //}
    }
}

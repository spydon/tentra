package fyi.lukas.tentra;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;



public class RecordingThread extends Thread {
    public static final String DIRECTORY_TEMP = "AudioTemp";
    public static final int REPEAT_INTERVAL = 40;
    private DrawView.AnimationThread animationThread;

    private MediaRecorder recorder = null;
    private boolean isRecording = false;
    private Handler handler;

    public RecordingThread(DrawView.AnimationThread animationThread) {
        this.animationThread = animationThread;
        handler = new Handler();
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(DIRECTORY_TEMP + "/tmp_audio_file.mp3");
        updateVisualizer.run();
    }

    private void releaseRecorder() {
        if (recorder != null) {
            isRecording = false; // stop recording
            recorder.stop();
            recorder.reset();
            recorder.release();
            recorder = null;
            deleteFilesInDir();
        }
    }

    private void startRecorder() {
        try {
            recorder.prepare();
            recorder.start();
            isRecording = true; // we are currently recording
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteFilesInDir() {
        File path = new File(DIRECTORY_TEMP);
        if( path.exists() ) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for(File file : files) {
                if(!file.isDirectory()) {
                    file.delete();
                }
            }
        }
        return true;
    }

    Runnable updateVisualizer = new Runnable() {
        @Override
        public void run() {
            if (isRecording) {
                int x = recorder.getMaxAmplitude();
                releaseRecorder();
                animationThread.setAmplitude(x);
                startRecorder();
            } else {
                startRecorder();
            }
            handler.postDelayed(this, REPEAT_INTERVAL);
        }
    };
}
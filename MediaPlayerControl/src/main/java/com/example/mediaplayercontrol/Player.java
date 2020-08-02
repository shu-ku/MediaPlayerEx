package com.example.mediaplayercontrol;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

import static com.example.mediaplayercontrol.PlayerState.State.Idle;
import static com.example.mediaplayercontrol.PlayerState.State.Initialized;
import static com.example.mediaplayercontrol.PlayerState.State.Prepared;
import static com.example.mediaplayercontrol.PlayerState.State.Started;
import static com.example.mediaplayercontrol.PlayerState.State.PlaybackCompleted;
import static com.example.mediaplayercontrol.PlayerState.State.Paused;
import static com.example.mediaplayercontrol.PlayerState.State.Stopped;
import static com.example.mediaplayercontrol.PlayerState.State.End;
import static com.example.mediaplayercontrol.PlayerState.State.Error;
import static com.example.mediaplayercontrol.PlayerState.State;

public class Player {
    private final static String TAG = "Player";
    private Extractor extractor;
    private Format[] formats;
    private SampleQueue sampleQueue;
    private State state = Idle;
    private Clock clock;

    public Player() {
        transitState(Idle);
    }

    public void prepare(String path, SurfaceHolder surfaceHolder){
        sampleQueue = new SampleQueue();
        extractor = new Extractor();
        formats = new Format[2];
        formats[0] = new Format();
        formats[1] = new Format();

        if (state.checkSetDataSource()) {
            Log.i(TAG, "path=" + path);
            extractor.setDataSource(path);
            transitState();
        }

        if (state.checkPrepare()) {
            extractor.prepare(formats, sampleQueue);

            // audio prepare
            for (Format format : formats) {
                if (!format.isVideo) {
                    format.codec = new AudioCodec();
                    format.codec.prepare(format, surfaceHolder, sampleQueue);
                    clock = (Clock) format.codec;
                }
            }

            // video prepare
            for (Format format : formats) {
                if (format.isVideo) {
                    format.codec = new VideoCodec();
                    format.codec.prepare(format, surfaceHolder, sampleQueue);
                    format.codec.setClock(clock);
                }
            }
            transitState();
        }
    }

    public void start() {
        Log.i(TAG, "start()");
        if (state.checkStart()) {
            extractor.start();
            for (Format format : formats) {
                format.codec.start();
            }
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    while (!sampleQueue.isExtractorEOS() || sampleQueue.size() != 0) {
                        try {
                            Thread.sleep(1000); // 1sec sleep
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    for (Format format : formats) {
                        Log.i(TAG, "codec end");
                        format.codec.release();
                        break;
                    }
                }
            });
            thread.start();
            transitState();
        }
    }

    public void seekTo(int mSec) {
        Log.i(TAG, "seekTo(" + mSec + ")");
        long durationUs = getDurationUs();
        long currentPositionUs = clock.getCurrentPosition();
        long seekTimeUs = mSec * 1000;
        Log.i(TAG, "seekTo(" + mSec + ")" +
                " durationUs=" + String.format("%,d", durationUs) +
                " currentPositionUs=" + String.format("%,d", currentPositionUs));
        if (state.checkSeekTo()) {
//            extractor.seekTo(seekTimeUs);
//            transitState();
        }
    }

    public void pause() {
        Log.i(TAG, "pause()");
        if (state.checkPause()) {
            for (Format format : formats) {
                if (!format.isVideo) {
                    format.codec.pause();
                }
            }
            transitState();
        }
    }

    public long getDurationUs() {
        if (formats[1] != null) {
            return formats[1].durationUs;
        }
        return 0L;
    }

    public void transitState(State state) {
        Log.i(TAG, "transitState: " + this.state + " -> " + state);
        this.state = state;
    }

    public void transitState() {
        Log.i(TAG, "transitState: " + this.state + " -> " + PlayerState.getPlayerState());
        this.state = PlayerState.getPlayerState();
    }
}
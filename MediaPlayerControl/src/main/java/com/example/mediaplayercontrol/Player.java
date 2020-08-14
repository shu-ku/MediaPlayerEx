package com.example.mediaplayercontrol;

import android.util.Log;
import android.view.SurfaceHolder;

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

public class Player implements Codec.Callback {
    private final static String TAG = "Player";
    private Extractor extractor;
    private Format[] formats;
    private State state = Idle;
    private Clock clock;
    private Callback playbackCompleteCallback;

    public Player() {
        transitState(Idle);
    }

    public void prepare(String path, SurfaceHolder surfaceHolder) {
        SampleQueue sampleQueue = new SampleQueue();
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
                    format.codec.setPlaybackCompleteCallback(this);
                    break;
                }
            }
            // video prepare
            for (Format format : formats) {
                if (format.isVideo) {
                    format.codec = new VideoCodec();
                    format.codec.prepare(format, surfaceHolder, sampleQueue);
                    format.codec.setClock(clock);
                    break;
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
            transitState();
        }
    }

    public void seekTo(long seekPositionUs) {
        Log.i(TAG, "seekTo(" + seekPositionUs + ") -->");
        long durationUs = getDurationUs();
        long currentPositionUs = clock.getCurrentPositionUs();
        Log.i(TAG, "seekTo(" + seekPositionUs + ")" +
                " durationUs=" + String.format("%,d", durationUs) +
                " currentPositionUs=" + String.format("%,d", currentPositionUs));
        if (state.checkSeekTo()) {
            extractor.pause();
            if (extractor.seekTo(seekPositionUs)) {
                for (Format format : formats) {
                    format.codec.pause();
                    format.codec.seekTo(seekPositionUs);
                }
                transitState();
            }
        }
        Log.i(TAG, "seekTo(" + seekPositionUs + ") <--");
    }

    public void resume() {
        Log.i(TAG, "resume()");
        if (state.checkStart()) {
            if (state.checkSeekTo()) {
                long seekPositionUs = clock.getCurrentPositionUs();
                if (extractor.seekTo(seekPositionUs)) {
                    for (Format format : formats) {
//                        format.codec.flush();
                        format.codec.seekTo(seekPositionUs);
                    }
                    transitState();
                }
            }
        }
    }

    public void pause() {
        Log.i(TAG, "pause()");
        if (state.checkPause()) {
            extractor.pause();
            for (Format format : formats) {
                if (!format.isVideo) {
                    format.codec.pause();
                }
            }
            transitState();
        }
    }

    public void release() {
        if (state.checkRelease()) {
            Log.i(TAG, "release()");
            extractor.release();
            for (Format format : formats) {
                Log.i(TAG, "codec end");
                format.codec.release();
                break;
            }
            transitState();
        }
    }

    @Override
    public void playbackComplete() {
        Log.i(TAG, "playbackComplete");
        if (playbackCompleteCallback != null) {
            playbackCompleteCallback.playbackComplete();
        }
    }

    public long getDurationUs() {
        if (formats[1] != null) {
            return formats[1].durationUs;
        }
        return 0L;
    }

    public int getDurationMs() {
        return (int)(getDurationUs() / 1000);
    }

    public int getCurrentPositionMs() {
        return (int)(clock.getCurrentPositionUs() / 1000);
    }

    public long getCurrentPositionUs() {
        return clock.getCurrentPositionUs();
    }

    public void transitState(State state) {
        Log.i(TAG, "transitState: " + this.state + " -> " + state);
        this.state = state;
    }

    public void transitState() {
        Log.i(TAG, "transitState: " + this.state + " -> " + PlayerState.getPlayerState());
        this.state = PlayerState.getPlayerState();
    }

    public void setPlaybackCompleteCallback(Callback callback) {
        Log.i(TAG, "setPlaybackCompleteCallback");
        playbackCompleteCallback = callback;
    }

    public static interface Callback {
        void playbackComplete();
    }
}
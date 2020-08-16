package com.example.mediaplayercontrol;

import android.media.MediaCodec;
import android.util.Log;
import android.view.SurfaceHolder;

import java.nio.ByteBuffer;

import static android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM;

public class Codec extends Thread {
    private final String TAG = "Codec";
    protected Format format;
    protected MediaCodec codec;
    protected SampleQueue sampleQueue;
    protected long presentationTimeUs;
    protected int outputIndex;
    protected MediaCodec.BufferInfo info;
    protected SampleHolder sampleHolder;
    private int inputIndex;
    ByteBuffer inputBuffer;
    protected Callback playbackCompleteCallback;

    protected final static int NOT_SET_INDEX = -1;
    protected final static ByteBuffer NOT_SET_BUFFER = null;

    public Codec() {

    }

    public void prepare(Format format, SurfaceHolder surfaceHolder, SampleQueue sampleQueue) {
        this.sampleQueue = sampleQueue;
        this.format = format;
    }

    protected boolean processOutputBuffer() {
        return false;
    }

    public void run() {
        inputIndex = NOT_SET_INDEX;
        inputBuffer = NOT_SET_BUFFER;
        while (!sampleQueue.isExtractorEOS() || sampleQueue.size() > 0) {
            if (!QueueInputBuffer()) {
                try {
                    Thread.sleep(10); // 0.01sec sleep
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.i(TAG, "playbackComplete" +
                " isVideo=" + format.isVideo +
                " sampleQueue.isExtractorEOS()=" + sampleQueue.isExtractorEOS() +
                " sampleQueue.size()" + sampleQueue.size());
//        codec.queueInputBuffer(0, 0, 0, presentationTimeUs, BUFFER_FLAG_END_OF_STREAM);
    }

    public boolean QueueInputBuffer() {
        if (sampleQueue.size() <= 0) {
            Log.i(TAG, "isVideo=" + format.isVideo + " sampleQueue.size() <= 0");
            return false;
        }
        sampleHolder = sampleQueue.poll(format.trackIndex);
        if (sampleHolder == null) {
            Log.i(TAG, "isVideo=" + format.isVideo + " sampleHolder null");
            return false;
        }
        try {
            if (inputIndex == NOT_SET_INDEX) {
                inputIndex = codec.dequeueInputBuffer(10);
                if (inputIndex < 0) {
                    Log.i(TAG, "isVideo=" + format.isVideo + " inputIndex full");
                    return false;
                }
            }
            if (inputBuffer == NOT_SET_BUFFER) {
                inputBuffer = codec.getInputBuffer(inputIndex);
                if (inputBuffer == null) {
                    Log.i(TAG, "isVideo=" + format.isVideo + " inputBuffer full");
                    return false;
                }
            }
            presentationTimeUs = sampleHolder.presentationTimeUs;
            Log.i(TAG, "queueInputBuffer" +
                    " isVideo=" + format.isVideo +
                    " presentationTimeUs=" + String.format("%,d", presentationTimeUs) +
                    " inputBuffer=" + (inputBuffer != null));
            inputBuffer.put(sampleHolder.inputBuffer.array(), 0, sampleHolder.inputBuffer.limit());
            codec.queueInputBuffer(inputIndex, 0, sampleHolder.inputBuffer.limit(), presentationTimeUs, 0);
            if (processOutputBuffer()) {
                inputIndex = NOT_SET_INDEX;
                inputBuffer = NOT_SET_BUFFER;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // TODO Process for each Exception
            inputIndex = NOT_SET_INDEX;
            inputBuffer = NOT_SET_BUFFER;
        }
        return false;
    }

    public void release() {
        Log.i(TAG, "release");
        codec.stop();
        codec.release();
        codec = null;
    }

    public void setClock(Clock clock) {
    }

    public void pause() {
    }

    public void play() {
    }

    public void seekTo(long seekPositionUs) {
//        codec.flush();
    }

    public boolean isTunnelingEnabled() {
//        return RendererConfiguration.getInstance().isTunneling();
        return false;
    }

    public void setPlaybackCompleteCallback(Callback callback) {
        Log.i(TAG, "setPlaybackCompleteCallback");
        playbackCompleteCallback = callback;
    }

    public static interface Callback {
        void playbackComplete();
    }
}

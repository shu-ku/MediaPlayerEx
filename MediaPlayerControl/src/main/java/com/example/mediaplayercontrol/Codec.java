package com.example.mediaplayercontrol;

import android.media.MediaCodec;
import android.util.Log;
import android.view.SurfaceHolder;

import java.nio.ByteBuffer;

import static android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM;

public class Codec extends Thread{
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

    private final static int NOT_SET_INDEX = -1;
    private final static ByteBuffer NOT_SET_BUFFER = null;

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
        while (true) {
            if (!QueueInputBuffer()) {
                try {
                    Thread.sleep(10); // 0.01sec sleep
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // codec.queueInputBuffer(inputIndex, 0, 0, presentationTimeUs, BUFFER_FLAG_END_OF_STREAM);
    public boolean QueueInputBuffer() {
        if (sampleQueue.size() <= 0) {
            return false;
        }
        sampleHolder = sampleQueue.poll(format.trackIndex);
        if (sampleHolder == null) {
            return false;
        }
        try {
            if (inputIndex == NOT_SET_INDEX) {
                inputIndex = codec.dequeueInputBuffer(10);
                if (inputIndex < 0) {
                    return false;
                }
            }
            if (inputBuffer == NOT_SET_BUFFER) {
                inputBuffer = codec.getInputBuffer(inputIndex);
                if (inputBuffer == null) {
                    return false;
                }
            }
            presentationTimeUs = sampleHolder.presentationTimeUs;
            Log.i(TAG, "queueInputBuffer presentationTimeUs=" + String.format("%,d", presentationTimeUs) + " isVideo=" + format.isVideo);
            inputBuffer.put(sampleHolder.inputBuffer.array(), 0, sampleHolder.inputBuffer.limit());
            codec.queueInputBuffer(inputIndex, 0, sampleHolder.inputBuffer.limit(), presentationTimeUs, 0);
            inputIndex = NOT_SET_INDEX;
            inputBuffer = NOT_SET_BUFFER;
            return processOutputBuffer();
        } catch (Exception e) {
            e.printStackTrace();
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

    public void seekTo(long seekPositionUs) {
    }

    public boolean isTunnelingEnabled() {
//        return RendererConfiguration.getInstance().isTunneling();
        return false;
    }
}

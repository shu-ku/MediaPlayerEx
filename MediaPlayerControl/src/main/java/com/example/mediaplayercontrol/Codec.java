package com.example.mediaplayercontrol;

import android.media.MediaCodec;
import android.util.Log;
import android.view.SurfaceHolder;

import java.nio.ByteBuffer;

public class Codec extends Thread{
    private final String TAG = "Codec";
    protected Format format;
    protected MediaCodec codec;
    protected SampleQueue sampleQueue;
    protected long presentationTimeUs;
    protected int outputIndex;
    protected MediaCodec.BufferInfo info;
    private int playbackStatus;
    protected SampleHolder sampleHolder;

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
        boolean result = false;
        ByteBuffer inputBuffer = null;
        int count = 0;
        while (true) {
            if (sampleQueue.size() > 0 && sampleQueue.checkCodec(format.trackIndex)) {
                int inputIndex = codec.dequeueInputBuffer(10);
                if (inputIndex >= 0) {
                    inputBuffer = codec.getInputBuffer(inputIndex);
                    if (inputBuffer != null) {
                        sampleHolder = sampleQueue.poll();
                        presentationTimeUs = sampleHolder.presentationTimeUs;
                        Log.i(TAG, "queueInputBuffer presentationTimeUs=" + String.format("%,d", presentationTimeUs) + " isVideo=" + format.isVideo);
                        inputBuffer.put(sampleHolder.inputBuffer.array(), 0, sampleHolder.inputBuffer.limit());
                        codec.queueInputBuffer(inputIndex, 0, sampleHolder.inputBuffer.limit(), presentationTimeUs, 0);
                    }
                    result = processOutputBuffer();
                }
            } else {
                try {
                    Thread.sleep(10); // 0.01sec sleep
                    count++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void release() {
        Log.i(TAG, "release");
        codec.stop();
        codec.release();
        codec = null;
    }

    public void setClock(Clock clock) {
    };

    public void pause() {
    };

    public boolean isTunnelingEnabled() {
//        return RendererConfiguration.getInstance().isTunneling();
        return false;
    }
}

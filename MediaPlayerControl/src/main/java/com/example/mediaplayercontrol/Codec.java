package com.example.mediaplayercontrol;

import android.media.MediaCodec;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;

public class Codec extends Thread{
    protected ByteBuffer[] inputBuffers;
    protected ByteBuffer[] outputBuffers;
    protected ByteBuffer inputBuffer;
    protected Format format;
    protected MediaCodec codec;
    protected Queue<SampleHolder> sampleHolders;

    public Codec() {

    }
    public void initialize(Format format, SurfaceHolder surfaceHolder, Queue<SampleHolder> sampleHolders) {
        this.sampleHolders = sampleHolders;
    }

    public ByteBuffer getInputBuffer() {
        int inputBufferIndex = codec.dequeueInputBuffer(0);
        inputBuffer = inputBuffers[inputBufferIndex];
        return inputBuffer;
    }

    public void run() {
    }
}

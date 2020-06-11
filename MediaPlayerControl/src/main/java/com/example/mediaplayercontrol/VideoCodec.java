package com.example.mediaplayercontrol;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;

public class VideoCodec extends Codec{
    private final String TAG = "VideoCodec";
    private SurfaceHolder surfaceHolder;

    @Override
    public void initialize(Format format, SurfaceHolder sh, SampleQueue sampleQueue) {
        super.initialize(format, null, sampleQueue);
        this.format = format;
        surfaceHolder = sh;
        try {
            codec = MediaCodec.createDecoderByType(format.mimeType);
            codec.configure(format.format, surfaceHolder.getSurface(), null, 0);
            codec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean processOutputBuffer() {
        info = new MediaCodec.BufferInfo();
        outputIndex = codec.dequeueOutputBuffer(info, 0);
        Log.i(TAG, "dequeueOutputBuffer outputIndex=" + outputIndex);
        if (outputIndex >= 0) {
            ByteBuffer outputBuffer = codec.getOutputBuffer(outputIndex);

            codec.releaseOutputBuffer(outputIndex, true);
            Log.i(TAG, "releaseOutputBuffer presentationTimeUs=" + presentationTimeUs);
        } else if (outputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//            outputBuffers = codec.getOutputBuffers();
        } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            format.format = codec.getOutputFormat();
        }
        return true;
    }
}

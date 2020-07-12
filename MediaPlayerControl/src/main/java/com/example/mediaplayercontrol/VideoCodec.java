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
    private Clock clock;

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
            releaseOutputBuffer(outputIndex);
        } else if (outputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//            outputBuffers = codec.getOutputBuffers();
        } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            format.format = codec.getOutputFormat();
        }
        return true;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    };

    private void releaseOutputBuffer(int outputIndex) {
        long currentPosition = clock.getCurrentPosition();
        long differenceTime =  presentationTimeUs - currentPosition;
        Log.i(TAG, "releaseOutputBuffer" +
                " presentationTimeUs=" + presentationTimeUs +
                " currentPosition=" + currentPosition +
                " differenceTime=" + differenceTime);
        if (Math.abs(differenceTime) <= 500_000) {
            codec.releaseOutputBuffer(outputIndex, true);
        } else if (differenceTime < -500_000) {
            Log.i(TAG, "releaseOutputBuffer" +
                    " differenceTime=" + differenceTime + " drop");
            codec.releaseOutputBuffer(outputIndex, false);
        } else if (differenceTime > 500_000) {
            Log.i(TAG, "releaseOutputBuffer" +
                    " differenceTime=" + differenceTime + " wait");
        }
    }
}

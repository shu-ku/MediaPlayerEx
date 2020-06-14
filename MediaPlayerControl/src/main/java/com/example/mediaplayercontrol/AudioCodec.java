package com.example.mediaplayercontrol;

import android.media.MediaCodec;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioCodec extends Codec {
    private final String TAG = "AudioCodec";
    private Audio audio;
    private long baseNanoTime;

    @Override
    public void initialize(Format format, SurfaceHolder surfaceHolder, SampleQueue sampleQueue) {
        super.initialize(format, null, sampleQueue);
        this.format = format;
        baseNanoTime = System.nanoTime();
        audio = new Audio();
        audio.initialize(format);
        try {
            codec = MediaCodec.createDecoderByType(format.mimeType);
            codec.configure(format.format, null, null, 0);
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
        getCurrentPosition();
        if (outputIndex >= 0) {
            ByteBuffer outputBuffer = codec.getOutputBuffer(outputIndex);
            if (outputBuffer != null) {
                audio.write(outputBuffer);
                codec.releaseOutputBuffer(outputIndex, false);
                Log.i(TAG, "releaseOutputBuffer presentationTimeUs=" + presentationTimeUs);
            }
        } else if (outputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//            outputBuffers = codec.getOutputBuffers();
        } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            format.format = codec.getOutputFormat();
        }
        return true;
    }


    // Correct the time of sound
    // if (AudioPTS < VideoPTS - ??)
    public long getCurrentPosition() {
        long nanoTime = System.nanoTime() - baseNanoTime;
        Log.i(TAG, "nanoTime=" + String.format("%,d", nanoTime) + " presentationTimeUs=" + String.format("%,d", presentationTimeUs));
        return 0;
    }
}

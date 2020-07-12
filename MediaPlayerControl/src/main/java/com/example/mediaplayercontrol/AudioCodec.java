package com.example.mediaplayercontrol;

import android.media.MediaCodec;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioCodec extends Codec implements Clock{
    private final String TAG = "AudioCodec";
    private Audio audio = null;
    private long baseNanoTime;
    private boolean isFirst = true;

    @Override
    public void initialize(Format format, SurfaceHolder surfaceHolder, SampleQueue sampleQueue) {
        super.initialize(format, null, sampleQueue);
        this.format = format;
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

    public long getCurrentPosition() {
        if (audio != null) {
            return audio.getCurrentPosition();
        }
        return 0L;
    }

    public void pause() {
        if (audio != null) {
            audio.pause();
        }
    };
}

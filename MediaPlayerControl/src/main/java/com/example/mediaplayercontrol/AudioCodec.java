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

    @Override
    public void prepare(Format format, SurfaceHolder surfaceHolder, SampleQueue sampleQueue) {
        Log.i(TAG, "prepare");
        super.prepare(format, null, sampleQueue);
        audio = new Audio();
        audio.prepare(format);
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
        if (outputIndex >= 0 && audio != null) {
            ByteBuffer outputBuffer = codec.getOutputBuffer(outputIndex);
            if (outputBuffer != null) {
                audio.write(outputBuffer, presentationTimeUs);
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

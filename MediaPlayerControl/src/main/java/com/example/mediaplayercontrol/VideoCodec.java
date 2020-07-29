package com.example.mediaplayercontrol;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;

public class VideoCodec extends Codec{
    private final String TAG = "VideoCodec";
    private Clock clock;

    @Override
    public void prepare(Format format, SurfaceHolder sh, SampleQueue sampleQueue) {
        super.prepare(format, null, sampleQueue);
        try {
            codec = MediaCodec.createDecoderByType(format.mimeType);
            Log.i(TAG, "Video Tunnel mode:" + RendererConfiguration.getInstance().getTunnelingAudioSessionId());
            format.format.setInteger(MediaFormat.KEY_AUDIO_SESSION_ID,
                    RendererConfiguration.getInstance().getTunnelingAudioSessionId());
            codec.configure(format.format, sh.getSurface(), null, 0);
            codec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean processOutputBuffer() {
        if (isTunnelingEnabled()) {
            return false;
        }
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

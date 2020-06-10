package com.example.mediaplayercontrol;

import android.media.MediaCodec;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.Queue;

public class VideoCodec extends Codec{
    private SurfaceHolder surfaceHolder;

    @Override
    public void initialize(Format f, SurfaceHolder sh, Queue<SampleHolder> sampleHolders) {
        super.initialize(format, null, sampleHolders);
        format = f;
        surfaceHolder = sh;
        try {
            codec = MediaCodec.createDecoderByType(format.mimeType);
            codec.configure(format.format, surfaceHolder.getSurface(), null, 0);
//            inputBuffers = codec.getInputBuffers();
//            outputBuffers = codec.getOutputBuffers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

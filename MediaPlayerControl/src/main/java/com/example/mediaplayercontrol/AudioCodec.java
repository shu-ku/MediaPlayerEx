package com.example.mediaplayercontrol;

import android.media.MediaCodec;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.Queue;

public class AudioCodec extends Codec {
    @Override
    public void initialize(Format f, SurfaceHolder surfaceHolder, Queue<SampleHolder> sampleHolders) {
        super.initialize(format, null, sampleHolders);
        format = f;
        try {
            codec = MediaCodec.createDecoderByType(format.mimeType);
            codec.configure(format.format, null, null, 0);
//            inputBuffers = codec.getInputBuffers();
//            outputBuffers = codec.getOutputBuffers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.example.mediaplayercontrol;

import android.view.SurfaceHolder;

import java.nio.ByteBuffer;

public class SampleHolder {
    public ByteBuffer inputBuffer;
    public long presentationTimeUs;
    public int trackIndex;

    public SampleHolder(ByteBuffer inputBuffer, long presentationTimeUs, int trackIndex) {
        this.inputBuffer = inputBuffer;
        this.presentationTimeUs = presentationTimeUs;
        this.trackIndex = trackIndex;
    }

    @Override
    public String toString() {
        return "SampleHolder{" +
                "inputBuffer=" + inputBuffer +
                ", presentationTimeUs=" + presentationTimeUs +
                ", trackIndex=" + trackIndex +
                '}';
    }
}

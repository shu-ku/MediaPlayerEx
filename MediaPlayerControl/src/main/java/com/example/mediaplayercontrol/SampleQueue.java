package com.example.mediaplayercontrol;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

public class SampleQueue {
    private final Queue<SampleHolder> sampleHolders = new ArrayDeque<>();
    private boolean extractorEOS = false;

    public SampleQueue() {

    }

    public synchronized void add(SampleHolder sampleHolder) {
        sampleHolders.add(sampleHolder);
    }

    public synchronized SampleHolder poll() {
        return sampleHolders.poll();
    }

    public synchronized int size() {
        return sampleHolders.size();
    }

    public synchronized boolean checkCodec(int trackIndex) {
        return Objects.requireNonNull(sampleHolders.peek()).trackIndex == trackIndex;
    }

    public synchronized boolean isExtractorEOS() {
        return extractorEOS;
    }

    public synchronized void setExtractorEOS(boolean extractorEOS) {
        this.extractorEOS = extractorEOS;
    }
}

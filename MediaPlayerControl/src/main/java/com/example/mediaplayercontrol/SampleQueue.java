package com.example.mediaplayercontrol;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

public class SampleQueue {
    private final Queue<SampleHolder> sampleQueue = new ArrayDeque<>();
    private boolean extractorEOS = false;

    public SampleQueue() {

    }

    public synchronized void add(SampleHolder sampleHolder) {
        sampleQueue.add(sampleHolder);
    }

    public synchronized SampleHolder poll() {
        return sampleQueue.poll();
    }

    public synchronized int size() {
        return sampleQueue.size();
    }

    public synchronized boolean checkCodec(int trackIndex) {
        return Objects.requireNonNull(sampleQueue.peek()).trackIndex == trackIndex;
    }

    public synchronized boolean isExtractorEOS() {
        return extractorEOS;
    }

    public synchronized void setExtractorEOS(boolean extractorEOS) {
        this.extractorEOS = extractorEOS;
    }

    public synchronized void clear(){
        sampleQueue.clear();
    }
}

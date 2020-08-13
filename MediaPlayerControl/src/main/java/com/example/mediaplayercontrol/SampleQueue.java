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

    public synchronized SampleHolder poll(int trackIndex) {
        if (sampleQueue.size() > 0 && Objects.requireNonNull(sampleQueue.peek()).trackIndex == trackIndex) {
            return sampleQueue.poll();
        }
        return null;
    }

    public synchronized int size() {
        return sampleQueue.size();
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

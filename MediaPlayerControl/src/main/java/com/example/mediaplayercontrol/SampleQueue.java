package com.example.mediaplayercontrol;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

public class SampleQueue {
    private final Queue<SampleHolder> sampleHolders = new ArrayDeque<>();

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

    public synchronized boolean isVideo(int trackIndex) {
        return Objects.requireNonNull(sampleHolders.peek()).trackIndex == trackIndex;
    }
}

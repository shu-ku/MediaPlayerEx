package com.example.mediaplayercontrol;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Extractor extends Thread{
    private final static String TAG = "Extractor";

    private MediaExtractor extractor;
    SampleQueue sampleQueue;

    long minPrime;
    private ByteBuffer inputBuffer;
    private int state = 0;

    private final static int IDLE = 0;
    private final static int START = 1;
    private final static int PAUSE = 2;

    public Extractor() {
        extractor = new MediaExtractor();
        state = IDLE;
    }

    public void setDataSource(String path) {
        try {
            extractor.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void prepare(Format[] format, SampleQueue sampleQueue) {
        this.sampleQueue = sampleQueue;
        int tracks = extractor.getTrackCount();
        MediaFormat mediaFormat;
        String mimeType;
        long durationUs;
        for (int i = 0; i < tracks; ++i) {
            mediaFormat = extractor.getTrackFormat(i);
            mimeType = mediaFormat.getString(MediaFormat.KEY_MIME);
            Log.i(TAG, "mediaFormat=" + mediaFormat + "mimeType=" + mimeType + " format=" + format.length);
            if (mimeType.startsWith("video/")) {
                extractor.selectTrack(i);
                format[0].format = mediaFormat;
                format[0].mimeType = mimeType;
                format[0].isVideo = true;
                format[0].trackIndex = i;
                break;
            }
        }
        for (int i = 0; i < tracks; ++i) {
            mediaFormat = extractor.getTrackFormat(i);
            mimeType = mediaFormat.getString(MediaFormat.KEY_MIME);
            durationUs = mediaFormat.getLong(MediaFormat.KEY_DURATION);
            if (mimeType.startsWith("audio/")) {
                extractor.selectTrack(i);
                format[1].format = mediaFormat;
                format[1].mimeType = mimeType;
                format[1].isVideo = false;
                format[1].trackIndex = i;
                format[1].durationUs = durationUs;
                break;
            }
        }
        state = START;
    }

    public void run() {
        boolean inputEOS = false;
        while (!inputEOS) {
            if (state == START) {
                ByteBuffer inputBuffer = ByteBuffer.allocate(128 * 1024);
                long presentationTimeUs = 0;
                // IllegalArgumentException
                int bufferSize = extractor.readSampleData(inputBuffer, 0);
                if (bufferSize >= 0) {
                    presentationTimeUs = extractor.getSampleTime();
                    Log.i(TAG, "presentationTimeUs=" + String.format("%,d", presentationTimeUs) + " sampleQueue.size=" + sampleQueue.size());
                    sampleQueue.add(new SampleHolder(inputBuffer, presentationTimeUs, extractor.getSampleTrackIndex()));
                    extractor.advance();
                    if (sampleQueue.size() > 1000) {
                        try {
                            Thread.sleep(100); // 0.01sec sleep
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    inputEOS = true;
                }
            } else {
                try {
                    Thread.sleep(100); // 0.01sec sleep
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        release();
    }

    public void release() {
        extractor.release();
        extractor = null;
        sampleQueue.setExtractorEOS(true);
    }

    public void pause() {
        state = PAUSE;
    }

    public boolean seekTo(long seekPositionUs) {
        try {
            sampleQueue.clear();
            extractor.seekTo(seekPositionUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            state = START;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            state = START;
            return false;
        }
    }
}

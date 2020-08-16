package com.example.mediaplayercontrol;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Extractor extends Thread{
    private final static String TAG = "Extractor";

    private final static int IDLE = 0;
    private final static int START = 1;
    private final static int PAUSE = 2;
    private final static int RELEASE = 3;

    private MediaExtractor extractor;
    private SampleQueue sampleQueue;

    private int state = 0;

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
            if (state == START && sampleQueue.size() < 1000) {
                ByteBuffer inputBuffer = ByteBuffer.allocate(128 * 1024);
                long presentationTimeUs = 0;
                // IllegalArgumentException
                int bufferSize = extractor.readSampleData(inputBuffer, 0);
                if (bufferSize >= 0) {
                    presentationTimeUs = extractor.getSampleTime();
                    int trackIndex = extractor.getSampleTrackIndex();
                    Log.i(TAG, "presentationTimeUs=" + String.format("%,d", presentationTimeUs) +
                            " sampleQueue.size=" + sampleQueue.size() + " trackIndex=" + trackIndex);
                    sampleQueue.add(new SampleHolder(inputBuffer, presentationTimeUs, trackIndex));
                    extractor.advance();
                } else {
                    inputEOS = true;
                }
            } else if (state == RELEASE) {
                inputEOS = true;
            } else {
                sleep();
            }
        }
        sampleQueue.setExtractorEOS(true);
    }

    public void release() {
        Log.i(TAG, "release()");
        state = RELEASE;
        sleep();
        extractor.release();
        extractor = null;
        sampleQueue.clear();
        sampleQueue = null;
    }

    public void play() {
        state = START;
    }

    public void pause() {
        state = PAUSE;
        sleep();
        sampleQueue.clear();
    }

    public boolean seekTo(long seekPositionUs) {
        try {
            extractor.seekTo(seekPositionUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
//            sampleQueue.clear();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sleep() {
        try {
            Thread.sleep(10); // 0.01sec sleep
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

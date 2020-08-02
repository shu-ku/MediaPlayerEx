package com.example.mediaplayercontrol;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

public class Extractor extends Thread{
    private final static String TAG = "Extractor";

    private MediaExtractor extractor;
    SampleQueue sampleQueue;

    long minPrime;
    private ByteBuffer inputBuffer;

    public Extractor() {
        extractor = new MediaExtractor();
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
            if (mimeType.startsWith("audio/")) {
                extractor.selectTrack(i);
                format[1].format = mediaFormat;
                format[1].mimeType = mimeType;
                format[1].isVideo = false;
                format[1].trackIndex = i;
                break;
            }
        }
    }

    public void run() {
        boolean inputEOS = false;
        while (!inputEOS) {
            ByteBuffer inputBuffer = ByteBuffer.allocate(128 * 1024);
            long presentationTimeUs = 0;
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
        }
        extractor.release();
        extractor = null;
        sampleQueue.setExtractorEOS(true);
    }
}

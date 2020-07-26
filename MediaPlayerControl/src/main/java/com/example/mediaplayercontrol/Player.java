package com.example.mediaplayercontrol;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

public class Player {
    private final static String TAG = "Player";
    private String path = "";
    private SurfaceHolder surfaceHolder;
    private Extractor extractor;
    private Format formats[];
    private SampleQueue sampleQueue;

    public Player() {

    }

    public void prepare(String path, SurfaceHolder surfaceHolder){
        this.path = path;
        this.surfaceHolder = surfaceHolder;
        extractor = new Extractor();
        formats = new Format[2];
        formats[0] = new Format();
        formats[1] = new Format();
        sampleQueue = new SampleQueue();
        extractor.prepare(path, formats, sampleQueue);
        Clock clock = null;

        // audio prepare
        for (Format format: formats) {
            if (!format.isVideo) {
                format.codec = new AudioCodec();
                format.codec.prepare(format, surfaceHolder, sampleQueue);
                clock = (Clock) format.codec;
            }
        }

        // video prepare
        for (Format format: formats) {
            if (format.isVideo) {
                format.codec = new VideoCodec();
                format.codec.prepare(format, surfaceHolder, sampleQueue);
                format.codec.setClock(clock);
            }
        }
    }

    public void start() {
//        simpleVideoStart();
        Log.i(TAG, "start()");
        extractor.start();
        for (Format format: formats) {
            format.codec.start();
        }
        Thread thread = new Thread(new Runnable(){
            public void run() {
                while (!sampleQueue.isExtractorEOS() || sampleQueue.size() != 0) {
                    try {
                        Thread.sleep(1000); // 1sec sleep
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                for (Format format: formats) {
                    Log.i(TAG, "codec end");
                    format.codec.release();
                    break;
                }
            }
        });
        thread.start();
    }

    public void pause() {
        Log.i(TAG, "pause()");
        for (Format format: formats) {
            if (!format.isVideo) {
                format.codec.pause();
            }
        }
    }
}
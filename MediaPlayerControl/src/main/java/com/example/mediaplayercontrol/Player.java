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
    private Codec codec;
    private Format formats[];
    final Queue<SampleHolder> sampleHolders = new ArrayDeque<>();

    public Player() {

    }

    public void prepare(String path, SurfaceHolder surfaceHolder){
        this.path = path;
        this.surfaceHolder = surfaceHolder;
        extractor = new Extractor();
        formats = new Format[2];
        formats[0] = new Format();
        formats[1] = new Format();
        extractor.initialize(path, formats, sampleHolders);
        for (Format format: formats) {
            if (format.isVideo) {
                format.codec = new VideoCodec();
            } else {
                format.codec = new AudioCodec();
            }
            format.codec.initialize(format, surfaceHolder, sampleHolders);
        }
    }

    public void start() {
        Log.i(TAG, "start()");
        extractor.start();
//        simpleVideoStart();
        for (Format format: formats) {
            format.codec.start();
        }

        try {
            extractor.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        synchronized (sampleHolders) {
//
//        }

        for (SampleHolder sampleHolder: sampleHolders) {
            Log.i(TAG, sampleHolder.toString());
        }
    }

    public void simpleVideoStart(){
        Log.i(TAG, "simpleVideoStart");
        String mime = "";
        MediaFormat format = null;
        ByteBuffer[] inputBuffers;
        ByteBuffer[] outputBuffers;
        ByteBuffer inputBuffer;

        try {
            MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(path);
            int tracks = extractor.getTrackCount();
            for (int i = 0; i < tracks; ++i) {
                format = extractor.getTrackFormat(i);
                mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("video/")) {
                    extractor.selectTrack(i);
                    break;
                }
            }

            if (!mime.equals("") && format != null) {
                MediaCodec codec = MediaCodec.createDecoderByType(mime);
                codec.configure(format, surfaceHolder.getSurface(), null, 0);
                MediaFormat outputFormat = codec.getOutputFormat();
                codec.start();
                inputBuffers = codec.getInputBuffers();
                outputBuffers = codec.getOutputBuffers();
                boolean inputEOS = false;
                for (;;) {
                    int inputBufferIndex = codec.dequeueInputBuffer(0);
                    long presentationTimeUs = 0;
                    if (inputBufferIndex >= 0) {
                        inputBuffer = inputBuffers[inputBufferIndex];
                        Log.i(TAG,
                                "inputBufferIndex=" + inputBufferIndex +
                                        " inputBuffer=" + inputBuffer);
                        int bufferSize = extractor.readSampleData(inputBuffer, 0);
                        if (bufferSize < 0) {
                            inputEOS = true;
                            bufferSize = 0;
                        } else {
                            presentationTimeUs = extractor.getSampleTime();
                        }
                        Log.i(TAG, "presentationTimeUs=" + (presentationTimeUs / 1000 / 1000));
                        codec.queueInputBuffer(inputBufferIndex, 0, bufferSize, presentationTimeUs,
                                inputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
                        if (!inputEOS) {
                            extractor.advance();
                        }

                    }

                    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                    int outputBufferIndex = codec.dequeueOutputBuffer(info, 0);
                    if (outputBufferIndex >= 0) {
                        ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferIndex);
                        codec.releaseOutputBuffer(outputBufferIndex, true);
                    } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        outputBuffers = codec.getOutputBuffers();
                    } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        format = codec.getOutputFormat();
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }

    }
}
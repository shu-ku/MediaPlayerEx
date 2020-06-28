package com.example.mediaplayercontrol;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.nio.ByteBuffer;

public class Audio implements AudioTrack.OnPlaybackPositionUpdateListener{
    private final String TAG = "AudioTrack";
    private AudioTrack mAudioTrack;
    private AudioAttributes attributes;
    private AudioFormat audioFormat;
    private MediaFormat format;
    private int mSampleRate;
    private long pts;

    public Audio () {

    }

    public void write(ByteBuffer outputBuffer) {
        int writeSize = mAudioTrack.write(outputBuffer, outputBuffer.remaining(), AudioTrack.WRITE_BLOCKING);
//        Log.i(TAG,  "writeSize=" + writeSize);
    }

    public void initialize(Format format) {
        this.format = format.format;
        int channelOut = AudioFormat.CHANNEL_OUT_MONO;
        if (this.format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) == 2) {
            channelOut = AudioFormat.CHANNEL_OUT_STEREO;
        }
        mSampleRate = this.format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        configAudioAttributes();
        configAudioFormat(channelOut);
        Log.i(TAG,
        "CHANNEL_COUNT=" + channelOut +
                "　mSampleRate=" + mSampleRate);
        mAudioTrack = new AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(getBufferSizeInBytes(channelOut))
                .build();
        setNorification();
        mAudioTrack.play();
    }

    private void setNorification() {
        mAudioTrack.setPlaybackPositionUpdateListener(this);
        mAudioTrack.setPositionNotificationPeriod(1024);

//        int result = mAudioTrack.setPositionNotificationPeriod(1024);
//        Log.i(TAG, "setNorification=" + result);
//        mAudioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
//            @Override
//            public void onMarkerReached(AudioTrack track) {
//                Log.i(TAG, "onMarkerReached");
//            }
//
//            @Override
//            public void onPeriodicNotification(AudioTrack track) {
//                Log.i(TAG, "onPeriodicNotification");
//
//            }
//        });
    }

    private void configAudioAttributes() {
        attributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build();
    }

    private void configAudioFormat(int channelOut) {
        audioFormat = new AudioFormat.Builder()
                .setSampleRate(mSampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(channelOut)
                .build();
    }

    private int getBufferSizeInBytes(int channelOut) {
        int bufferSizeInBytes =  AudioTrack.getMinBufferSize(
                mSampleRate,
                channelOut,
                AudioFormat.ENCODING_PCM_16BIT);
        int maxBufferSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);

        Log.i(TAG,
                "bufferSizeInBytes=" + bufferSizeInBytes +
                        " maxBufferSize=" + maxBufferSize);
        return bufferSizeInBytes;
    }

    @Override
    public void onMarkerReached(AudioTrack track) {
        Log.i(TAG, "onMarkerReached");
    }

    @Override
    public void onPeriodicNotification(AudioTrack track) {
        Log.i(TAG, "onPeriodicNotification");
    }
}

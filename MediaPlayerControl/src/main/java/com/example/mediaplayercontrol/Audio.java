package com.example.mediaplayercontrol;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import java.nio.ByteBuffer;

public class Audio {
    private final String TAG = "AudioTrack";
    private AudioTrack mAudioTrack;
    private AudioAttributes attributes;
    private AudioFormat audioFormat;
    private MediaFormat format;
    private long pts;

    public Audio () {

    }

    public void write(ByteBuffer outputBuffer) {
        Log.i(TAG,  "outputBuffer.remaining()=" + outputBuffer.remaining());
        mAudioTrack.write(outputBuffer, outputBuffer.remaining(), AudioTrack.WRITE_BLOCKING);
    }

    public void initialize(Format format) {
        this.format = format.format;
        int channelOut = AudioFormat.CHANNEL_OUT_MONO;
        if (this.format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) == 2) {
            channelOut = AudioFormat.CHANNEL_OUT_STEREO;
        }
        configAudioAttributes();
        configAudioFormat(channelOut);
                Log.i(TAG,
                "CHANNEL_COUNT=" + this.format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) +
                        "SampleRate=" + this.format.getInteger(MediaFormat.KEY_SAMPLE_RATE));

        mAudioTrack = new AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(getBufferSizeInBytes(channelOut))
                .build();
        mAudioTrack.play();
    }
    private void configAudioAttributes() {
        attributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build();
    }

    private void configAudioFormat(int channelOut) {
        audioFormat = new AudioFormat.Builder()
                .setSampleRate(format.getInteger(MediaFormat.KEY_SAMPLE_RATE))
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(channelOut)
                .build();
    }

    private int getBufferSizeInBytes(int channelOut) {
        int bufferSizeInBytes =  AudioTrack.getMinBufferSize(
                format.getInteger(MediaFormat.KEY_SAMPLE_RATE),
                channelOut,
                AudioFormat.ENCODING_PCM_16BIT);
        int maxBufferSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);

        Log.i(TAG,
                "bufferSizeInBytes=" + bufferSizeInBytes +
                        " maxBufferSize=" + maxBufferSize);
        return bufferSizeInBytes;
    }
}

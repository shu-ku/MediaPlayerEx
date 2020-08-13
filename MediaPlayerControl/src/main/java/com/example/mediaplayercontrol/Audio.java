package com.example.mediaplayercontrol;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaFormat;
import android.util.Log;

import java.nio.ByteBuffer;

public class Audio{
    private final String TAG = "AudioTrack";
    private AudioTrack mAudioTrack = null;
    private AudioAttributes attributes;
    private AudioFormat audioFormat;
    private MediaFormat format;
    private int mSampleRate;
    private long pts;
    private boolean first;
    private ByteBuffer avSyncHeader;
    private int bytesUntilNextAvSync = 0;
    private long seekPositionUs = 0;

    public Audio () {

    }

    public long getCurrentPositionUs() {
        if (mAudioTrack != null) {
            long currentPositionUs = (long)((float)mAudioTrack.getPlaybackHeadPosition() / (float)(mSampleRate/1000) * 1000) + seekPositionUs;
            Log.i(TAG, "currentPositionUs=" + currentPositionUs);
            return currentPositionUs;
        }
        return 0L;
    }

    public void seekTo(long seekPositionUs) {
        if (mAudioTrack != null) {
            mAudioTrack.flush();
            this.seekPositionUs = seekPositionUs;
            mAudioTrack.play();
        }
    }

    public void write(ByteBuffer outputBuffer, long presentationTimeUs) {
        if (RendererConfiguration.getInstance().isTunneling()) {
//            Log.i(TAG, "writeTunnel presentationTimeUs=" + presentationTimeUs + " size=" + outputBuffer.remaining());
            int writeSize = writeTunnel(outputBuffer, presentationTimeUs);
        } else {
            Log.i(TAG, "writeTunnel presentationTimeUs=" + presentationTimeUs);
            int writeSize = mAudioTrack.write(outputBuffer, outputBuffer.remaining(), AudioTrack.WRITE_BLOCKING);
        }
    }

    public int writeNonBlocking(ByteBuffer outputBuffer, int size) {
        return mAudioTrack.write(outputBuffer, size, AudioTrack.WRITE_NON_BLOCKING);
    }

    public int writeTunnel(ByteBuffer outputBuffer, long presentationTimeUs) {
        return mAudioTrack.write(outputBuffer, outputBuffer.remaining(), AudioTrack.WRITE_NON_BLOCKING, presentationTimeUs);
    }

    public void pause() {
        Log.i(TAG, "pause()");
        if (mAudioTrack != null) {
            mAudioTrack.pause();
        }
    };

    public void prepare(Format format) {
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
                " mSampleRate=" + mSampleRate);
        mAudioTrack = new AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(getBufferSizeInBytes(channelOut))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();
        mAudioTrack.play();
        RendererConfiguration.getInstance().setTunnelingAudioSessionId(mAudioTrack.getAudioSessionId());
    }

    private void configAudioAttributes() {
        attributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                .setUsage(AudioAttributes.USAGE_MEDIA)
//                .setFlags(AudioAttributes.FLAG_HW_AV_SYNC)
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
}

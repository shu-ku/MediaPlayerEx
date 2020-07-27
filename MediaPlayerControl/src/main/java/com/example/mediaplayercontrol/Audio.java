package com.example.mediaplayercontrol;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

    public Audio () {

    }

    public long getCurrentPosition() {
        if (mAudioTrack != null) {
            long currentPosition = (long)((float)mAudioTrack.getPlaybackHeadPosition() / (float)(mSampleRate/1000) * 1000);
            Log.i(TAG, " playbackHeadPosition=" + currentPosition);
            return currentPosition;
        }
        return 0L;
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
        if (avSyncHeader == null) {
            avSyncHeader = ByteBuffer.allocate(16);
            avSyncHeader.order(ByteOrder.BIG_ENDIAN);
            avSyncHeader.putInt(0x55550001);
        }
        if (bytesUntilNextAvSync == 0) {
            avSyncHeader.putInt(4, outputBuffer.remaining());
            avSyncHeader.putLong(8, presentationTimeUs * 1000);
            avSyncHeader.position(0);
            bytesUntilNextAvSync = outputBuffer.remaining();
        }
        int avSyncHeaderBytesRemaining = avSyncHeader.remaining();
        if (avSyncHeaderBytesRemaining > 0) {
            int writeSize = mAudioTrack.write(avSyncHeader, avSyncHeader.remaining(), AudioTrack.WRITE_NON_BLOCKING);
            Log.i(TAG, "writeTunnel presentationTimeUs=" + presentationTimeUs + " size=" + avSyncHeader.remaining() + " writeSize=" + writeSize + " avSyncHeaderBytesRemaining=" + avSyncHeaderBytesRemaining);
            if (writeSize < 0) {
                bytesUntilNextAvSync = 0;
                return writeSize;
            }
            if (writeSize < avSyncHeaderBytesRemaining) {
                return 0;
            }
        }
        Log.i(TAG, "writeTunnel presentationTimeUs=" + presentationTimeUs + " size=" + avSyncHeader.remaining() + " writeNonBlocking=" + avSyncHeaderBytesRemaining);

        int writeSize = writeNonBlocking(outputBuffer, outputBuffer.remaining());
        if (writeSize < 0) {
            bytesUntilNextAvSync = 0;
            return writeSize;
        }
        bytesUntilNextAvSync -= writeSize;
        return writeSize;
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

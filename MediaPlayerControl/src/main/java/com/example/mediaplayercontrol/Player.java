package com.example.mediaplayercontrol;

import android.media.MediaFormat;
import android.util.Log;

public class Player {
    private static final String TAG = "Player";
    class MediaFormatManager {
        public MediaFormat format = null;
        public Codec codec = null;

        MediaFormatManager(MediaFormat format, Codec codec) {
            this.format = format;
            this.codec = codec;
        }
    }

    public void start() {
        Log.i(TAG, "");
    }
}


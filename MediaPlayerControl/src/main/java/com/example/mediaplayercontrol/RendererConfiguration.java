package com.example.mediaplayercontrol;

import android.media.AudioManager;

public class RendererConfiguration {

    private static final RendererConfiguration configuration = new RendererConfiguration();

    public static final int AUDIO_SESSION_ID_UNSET = 0;

    private int audioSessionId = AUDIO_SESSION_ID_UNSET;

    private RendererConfiguration() {}

    public static RendererConfiguration getInstance() {
        return configuration;
    }

    public void setTunnelingAudioSessionId(int audioSessionId) {
        this.audioSessionId = audioSessionId;
    }

    public int getTunnelingAudioSessionId() {
        return audioSessionId;
    }

    public boolean isTunneling() {
//        return audioSessionId != AUDIO_SESSION_ID_UNSET;
        return false;
    }
}

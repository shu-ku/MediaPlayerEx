package com.example.mediaplayercontrol;

import android.util.Log;

import androidx.annotation.NonNull;

public class PlayerState {
    private static final String TAG = "PlayerState";
    private static State state = State.Idle;

    // state　
    public static State getPlayerState() {
        return state;
    }
    /**
     * state machine
     * Change state if true is returned.
     * https://developer.android.com/reference/android/media/MediaPlayer
     */
    public enum State {
        Idle {
            @Override
            public boolean checkSetDataSource() {
                state = State.Initialized;
                return true;
            }
        },
        Initialized {
            @Override
            public boolean checkPrepare() {
                state = State.Prepared;
                return true;
            }
        },
        Prepared {
            @Override
            public boolean checkStart() {
                state = State.Started;
                return true;
            }
        },
        Started {
            @Override
            public boolean checkPause() {
                state = State.Paused;
                return false;
            }
            @Override
            public boolean checkSeekTo() {
                return true;
            }
            @Override
            public boolean checkStop() {
                state = State.Stopped;
                return true;
            }
        },
        PlaybackCompleted {
            @Override
            public boolean checkStart() {
                state = State.Started;
                return true;
            }
            @Override
            public boolean checkStop() {
                state = State.Stopped;
                return true;
            }
        },
        Paused {
            @Override
            public boolean checkStart() {
                state = State.Started;
                return true;
            }
            @Override
            public boolean checkSeekTo() {
                return true;
            }
            @Override
            public boolean checkStop() {
                state = State.Stopped;
                return true;
            }
        },
        Stopped {

        },
        End {

        },
        Error {

        };
        public boolean checkSetDataSource() {
            Log.i(TAG, "checkSetDataSource return false");
            return false;
        }
        public boolean checkPrepare() {
            Log.i(TAG, "checkPrepare return false");
            return false;
        }
        public boolean checkStart() {
            Log.i(TAG, "checkStart return false");
            return false;
        }
        public boolean checkPause() {
            Log.i(TAG, "checkPause return false");
            return false;
        }
        public boolean checkSeekTo() {
            Log.i(TAG, "checkSeekTo return false");
            return false;
        }
        public boolean checkStop() {
            Log.i(TAG, "checkStop return false");
            return false;
        }
        public boolean checkRelease() {
            Log.i(TAG, "checkRelease return true");
            return true;
        }
    }

}

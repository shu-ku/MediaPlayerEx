package com.example.mediaplayercontrol;

import android.util.Log;

public class Player {
    private final static String TAG = "Player";
    private String path = "";

    public Player() {

    }

    public void start() {
        Log.i(TAG, "start()");


    }

    public void setContentPath(String path){
        this.path = path;
    }
}
package com.example.mediaplayerex;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class PlaybackActivity extends AppCompatActivity implements SurfaceHolder.Callback{
    private final static String TAG = "PlaybackActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        SurfaceView surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(this);

        final Button start_btn = findViewById(R.id.playback_start);
        start_btn.setOnClickListener(buttonClick);
        final Button stop_btn = findViewById(R.id.playback_stop);
        stop_btn.setOnClickListener(buttonClick);
        final Button resume_btn = findViewById(R.id.playback_resume);
        resume_btn.setOnClickListener(buttonClick);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated" +
                " surface="+ holder.getSurface());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged" +
                " format="+ format +
                " width="+ width +
                " height="+ height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceChanged" +
                " surface="+ holder.getSurface());
    }

    private View.OnClickListener buttonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.playback_start:
                    Log.d(TAG,"start, Perform action on click");
                    break;
                case R.id.playback_stop:
                    Log.d(TAG,"stop, Perform action on click");
                    break;
                case R.id.playback_resume:
                    Log.d(TAG,"resume, Perform action on click");
                    break;
            }
        }
    };
}
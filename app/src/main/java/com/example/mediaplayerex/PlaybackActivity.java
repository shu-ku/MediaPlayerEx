package com.example.mediaplayerex;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.mediaplayercontrol.Player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PlaybackActivity extends AppCompatActivity implements SurfaceHolder.Callback{
    private final static String TAG = "PlaybackActivity";
    private final int REQUEST_PERMISSION = 1000;
    private Player mPlayer;
    private boolean isPlayer;
    private boolean readExternalStoragePermission;
    private SurfaceHolder surfaceHolder;

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

        checkPermission();
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
        surfaceHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceChanged" +
                " surface="+ holder.getSurface());
    }

    public void button_start() {
        if (!isPlayer && readExternalStoragePermission) {
            mPlayer = new Player();
            prepare();
            mPlayer.start();
            isPlayer = true;
        }
    }

    private View.OnClickListener buttonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.playback_start:
                    Log.d(TAG,"start, Perform action on click");
                    button_start();
                    break;
                case R.id.playback_stop:
                    Log.d(TAG,"stop, Perform action on click");
                    break;
                case R.id.playback_resume:
                    Log.d(TAG,"resume, Perform action on click");
                    checkPermission();
                    break;
            }
        }
    };

    private void prepare(){
//        String path = "sdcard/content/sintel-1024-surround.mp4";
        String path = "sdcard/content/pretender.mp4";
//        String path = "sdcard/content/sintel-1024-surround.mp4";
        mPlayer.prepare(path, surfaceHolder);
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            readExternalStoragePermission = true;
        } else {
            requestReadExternalStoragePermission();
        }
    }

    private void requestReadExternalStoragePermission() {
        ActivityCompat.requestPermissions(PlaybackActivity.this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readExternalStoragePermission = true;
            } else {
                Toast toast = Toast.makeText(this,
                        "許可されないとアプリが実行できません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}
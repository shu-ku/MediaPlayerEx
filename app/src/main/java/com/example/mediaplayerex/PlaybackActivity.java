package com.example.mediaplayerex;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mediaplayercontrol.Format;
import com.example.mediaplayercontrol.Player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;

public class PlaybackActivity extends AppCompatActivity implements SurfaceHolder.Callback, Player.Callback {
    private final static String TAG = "PlaybackActivity";
    private final int REQUEST_PERMISSION = 1000;
    private Player mPlayer;
    private boolean isPlayer;
    private boolean readExternalStoragePermission;
    private SurfaceHolder surfaceHolder;
    private String path;
    private int progressValue;
    private TextView textViewCurrentPosition;
    private TextView textViewDuration;
    private SeekBar seekBar;
    private long durationUs;
    private int durationMs;
    private int currentPositionMs;
    final Object lockObj = new Object();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);
        textViewCurrentPosition = (TextView)findViewById(R.id.playback_position);
        textViewDuration = (TextView)findViewById(R.id.playback_duration);
        setListView();
        setProgressBar();
        SurfaceView surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(this);

        findViewById(R.id.playback_start).setOnClickListener(buttonClick);
        findViewById(R.id.playback_stop).setOnClickListener(buttonClick);
        findViewById(R.id.playback_permission).setOnClickListener(buttonClick);
        findViewById(R.id.playback_prev).setOnClickListener(buttonClick);
        findViewById(R.id.playback_resume).setOnClickListener(buttonClick);
        findViewById(R.id.playback_next).setOnClickListener(buttonClick);

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
            isPlayer = true;
            mPlayer = new Player();
            mPlayer.setPlaybackCompleteCallback(this);
            prepare();
            mPlayer.start();
        }
    }

    public void button_stop() {
        if (isPlayer) {
            mPlayer.pause();
        }
    }

    public void button_next() {
        synchronized (lockObj) {
            Log.i(TAG, "seek button_next");
            if (isPlayer) {
                long seekPositionUs = mPlayer.getCurrentPositionUs() + 10_000_000L;
                if (seekPositionUs < durationUs) {
                    mPlayer.seekTo(seekPositionUs);
                }
            }
        }
    }

    public void button_prev() {
        synchronized (lockObj) {
            Log.i(TAG, "seek button_prev");
            if (isPlayer && readExternalStoragePermission) {
                long seekPositionUs = mPlayer.getCurrentPositionUs() - 10_000_000L;
                if (seekPositionUs > 0) {
                    mPlayer.seekTo(seekPositionUs);
                } else {
                    mPlayer.seekTo(0);
                }
            }
        }
    }

    private View.OnClickListener buttonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.playback_start:
                    Log.d(TAG,"start, Perform action on click");
                    button_start();
                    Log.d(TAG,"start, end");
                    break;
                case R.id.playback_stop:
                    Log.d(TAG,"stop, Perform action on click");
                    button_stop();
                    break;
                case R.id.playback_permission:
                    Log.d(TAG,"resume, Perform action on click");
                    checkPermission();
                    break;
                case R.id.playback_prev:
                    Log.d(TAG,"prev, Perform action on click");
                    button_prev();
                    break;
                case R.id.playback_resume:
                    Log.d(TAG,"resume, Perform action on click");
                    break;
                case R.id.playback_next:
                    Log.d(TAG,"next, Perform action on click");
                    button_next();
                    break;
            }
        }
    };

    @SuppressLint("SetTextI18n")
    private void prepare(){
        mPlayer.prepare(path, surfaceHolder);
        durationUs = mPlayer.getDurationUs();
        durationMs = (int)(durationUs / 1000);
        setProgressBarMax(durationMs);
        textViewDuration.setText("/" + progressFormat(durationMs));
        setCurrentPositionMs();
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

    public void setListView() {
        ArrayList<String> contents = new ArrayList<>();
        final File[] fileList  = getFileList();
        if (fileList != null) {
            for (File file : fileList) {
                contents.add(file.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contents);
            ListView listView = (ListView) findViewById(R.id.list_view);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    ListView listView = (ListView) adapterView;
                    String item = (String) listView.getItemAtPosition(position);
                    path = fileList[(int) l].getAbsolutePath();
                    Log.d(TAG, "ListView, Perform action on click itme=" + item + " path=" + path);
                }
            });
        }
    }

    public File[] getFileList() {
        return new File(Environment.getExternalStorageDirectory() + "/content").listFiles();
    }
    public void setProgressBar() {
        seekBar = (SeekBar)findViewById(R.id.playback_seek_bar);
        progressValue = 0;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Log.d(TAG, "onScrollChange, Perform action on click" +
                        " i=" + i +
                        " b=" + b);
                progressValue = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onScrollChange, Perform action on click Stop");
                setTextViewCurrentPosition(progressValue);
            }
        });
        seekBar.setMax(0);
        seekBar.setMin(0);
        seekBar.setProgress(progressValue);
        setTextViewCurrentPosition(progressValue);
    }

    public void setProgressBarMax(int mSec) {
        seekBar.setMax(mSec);
    }

    public void setTextViewCurrentPosition(int progressValue) {
        textViewCurrentPosition.setText(progressFormat(progressValue));
        Log.d(TAG, "setProgressValue, progressValue=" + progressValue);
    }

    @SuppressLint("DefaultLocale")
    public String progressFormat(int mSec) {
        int sec = mSec / 1000;
        int minute = sec / 60;
        int hour = minute / 60;
        if (hour > 0) {
            minute -= hour*60;
            sec -= hour*60*60;
        }
        if (minute > 0) {
            sec -= minute*60;
        }

        return String.format("%02d:%02d:%02d", hour, minute, sec);
    }

    private void setCurrentPositionMs() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (isPlayer && currentPositionMs < durationMs) {
                    currentPositionMs = mPlayer.getCurrentPositionMs();
                    setTextViewCurrentPosition(currentPositionMs);
                    seekBar.setProgress(currentPositionMs);
                    try {
                        Thread.sleep(10); // 10Ms sleep
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void playbackComplete() {
        Log.i(TAG, "playbackComplete");
        mPlayer.release();
        isPlayer = false;
        mPlayer = null;
    }
}
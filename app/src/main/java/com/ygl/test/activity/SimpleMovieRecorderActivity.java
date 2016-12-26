package com.ygl.test.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ygl.medialib.OnVideoFinishListener;
import com.ygl.medialib.SimpleMovieRecorderView;
import com.ygl.test.R;

import java.io.File;

public class SimpleMovieRecorderActivity extends AppCompatActivity {

    private static final int HANDLE_VIDEO_FINISH = 222;

    private SimpleMovieRecorderView simpleMovieRecorderView;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_VIDEO_FINISH:
                    String path = (String) msg.obj;
                    Log.i("plq", "path = "+path);// eg: path="/storage/emulated/0/SampleVideo/video/1482737980056.mp4"
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_movie_recorder);

        initView();
        initControl();
    }

    private void initControl() {
        simpleMovieRecorderView.setOnVideoFinishListener(new OnVideoFinishListener() {
            @Override
            public void finish(File file) {
                Message msg = handler.obtainMessage(HANDLE_VIDEO_FINISH, file.getAbsolutePath());
                handler.sendMessage(msg);
            }
        });
    }

    private void initView() {
        simpleMovieRecorderView = (SimpleMovieRecorderView) findViewById(R.id.simple_video);
        ((TextView)findViewById(R.id.title)).setText("录制视频");
        findViewById(R.id.title_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpleMovieRecorderView.delCurrentMovie();
                finish();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        simpleMovieRecorderView.setFinish(true);
        simpleMovieRecorderView.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkCameraPermission();
    }

    /**
     * 检测摄像头和录音权限
     */
    private void checkCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "视频录制和录音没有授权", Toast.LENGTH_LONG).show();
            this.finish();
        } else {
            simpleMovieRecorderView.reset();
        }
    }

    @Override
    public void onDestroy() {
        simpleMovieRecorderView.delCurrentMovie();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                simpleMovieRecorderView.delCurrentMovie();
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
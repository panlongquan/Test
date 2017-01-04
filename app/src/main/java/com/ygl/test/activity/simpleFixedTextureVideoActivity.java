package com.ygl.test.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ygl.medialib.SimpleFixedTextureVideoView;
import com.ygl.test.R;

public class SimpleFixedTextureVideoActivity extends AppCompatActivity {

    private SimpleFixedTextureVideoView simpleFixedTextureVideoView;
    private String videoUrl;

    public static Intent createInstance(Context context, String videoUrl){
        Intent i = new Intent(context, SimpleFixedTextureVideoActivity.class);
        i.putExtra("videoUrl", videoUrl);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_fixed_texture_video);
        videoUrl = getIntent().getStringExtra("videoUrl");

        int w = getResources().getDisplayMetrics().widthPixels;
        simpleFixedTextureVideoView = (SimpleFixedTextureVideoView) findViewById(R.id.simpleFixedTextureVideoView);

//        simpleFixedTextureVideoView.setVideoData(w, videoUrl, "http://pic95.nipic.com/file/20160406/20616631_131705638000_2.jpg");
        simpleFixedTextureVideoView.setVideoData(800, videoUrl, "");
    }
}

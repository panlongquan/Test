package com.ygl.test.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.ygl.medialib.SimpleFixedTextureVideoView;
import com.ygl.test.R;
import com.ygl.test.TestApp;
import com.ygl.test.greendao.dao.VideoEntityDao;
import com.ygl.test.greendao.entity.VideoEntity;

public class SimpleFixedTextureVideoActivity extends AppCompatActivity {

    private SimpleFixedTextureVideoView simpleFixedTextureVideoView;
    private String videoUrl;
    private Toolbar toolbar;
    private int videoWidth;
    private VideoEntityDao videoEntityDao;
    private EditText ed_video_desc;

    public static Intent createInstance(Context context, String videoUrl){
        Intent i = new Intent(context, SimpleFixedTextureVideoActivity.class);
        i.putExtra("videoUrl", videoUrl);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_fixed_texture_video);
        videoEntityDao = TestApp.getInstance().getSession().getVideoEntityDao();
        videoUrl = getIntent().getStringExtra("videoUrl");

        ed_video_desc = (EditText) findViewById(R.id.ed_video_desc);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        initToolbar();

        videoWidth = getResources().getDisplayMetrics().widthPixels - 100;
        simpleFixedTextureVideoView = (SimpleFixedTextureVideoView) findViewById(R.id.simpleFixedTextureVideoView);

//        simpleFixedTextureVideoView.setVideoData(w, videoUrl, "http://pic95.nipic.com/file/20160406/20616631_131705638000_2.jpg");
        simpleFixedTextureVideoView.setVideoData(videoWidth, videoUrl, "");
    }

    private void initToolbar() {
//        toolBar.setLogo(R.drawable.xlistview_arrow);
        toolbar.setTitle("标题");
//        toolBar.setSubtitle("子标题");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        toolbarTitle.setText("主页");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_save:
                Toast.makeText(this, "save", Toast.LENGTH_SHORT).show();
                videoEntityDao.insert(new VideoEntity(videoUrl, videoWidth, ed_video_desc.getText().toString().trim(), ""));
                return true;
            case R.id.to_list:
                startActivity(new Intent(this, VideoListActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.video_list_menu, menu);
        return true;
    }
}

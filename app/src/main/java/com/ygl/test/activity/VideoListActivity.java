package com.ygl.test.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.ygl.test.R;
import com.ygl.test.TestApp;
import com.ygl.test.adapter.VideoListAdapter;
import com.ygl.test.greendao.dao.VideoEntityDao;
import com.ygl.test.greendao.entity.VideoEntity;
import com.ygl.test.listener.RecyclerItemClickListener;

import java.util.List;

public class VideoListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<VideoEntity> videoList;
    private VideoListAdapter adapter;
    private VideoEntityDao videoEntityDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        videoEntityDao = TestApp.getInstance().getSession().getVideoEntityDao();
        initView();
        initData();
        initControl();
    }

    private void initControl() {
        adapter = new VideoListAdapter(this, videoList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this.getApplicationContext(), adapter.onItemClickListener));
        recyclerView.setAdapter(adapter);
    }

    private void initData() {
        videoList = videoEntityDao.queryBuilder().where(VideoEntityDao.Properties.VideoUrl.isNotNull()).list();
    }

    private void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
    }
}

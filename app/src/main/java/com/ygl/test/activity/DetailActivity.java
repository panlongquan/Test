package com.ygl.test.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ygl.test.R;
import com.ygl.test.greendao.entity.ImageEntity;

public class DetailActivity extends AppCompatActivity {
    public static final String PARAM_IMAGE = "param.Image";

    private ImageView iv_detail_top;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private TextView tv_detail_description;

    private ImageEntity entity;

    public static Intent newInstance(Context c, ImageEntity entity){
        Intent i = new Intent(c, DetailActivity.class);
        i.putExtra(PARAM_IMAGE, entity);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        entity = getIntent().getParcelableExtra(PARAM_IMAGE);

        initView();
        initToolBar();
        initData();
    }

    private void initView() {
        iv_detail_top = (ImageView) findViewById(R.id.iv_detail_top);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tv_detail_description = (TextView) findViewById(R.id.tv_detail_description);
    }

    private void initToolBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbar.setTitle(entity.getDescription().substring(0, 8));
    }

    private void initData() {
        tv_detail_description.setText(entity.getDescription());
        Glide.with(this).load(entity.getImage_url()).into(iv_detail_top);
    }

}

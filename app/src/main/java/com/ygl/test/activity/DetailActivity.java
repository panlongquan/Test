package com.ygl.test.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ygl.test.R;
import com.ygl.test.greendao.entity.ImageEntity;
import com.ygl.utilslib.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {
    public static final String PARAM_IMAGE = "param.Image";

    private ImageView iv_detail_top;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private TextView tv_detail_description;
    private RecyclerView listView;

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
        listView = (RecyclerView) findViewById(R.id.listView);
    }

    private void initToolBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbar.setTitle(!TextUtils.isEmpty(entity.getDescription()) && entity.getDescription().length() > 8 ? entity.getDescription().substring(0, 8) : entity.getDescription());
        collapsingToolbar.setExpandedTitleColor(Color.WHITE);// 扩张时title的颜色
        collapsingToolbar.setCollapsedTitleTextColor(Color.GREEN);// 收缩后title的颜色
        assert toolbar != null;
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private List<String> list;
    private void initData() {
        tv_detail_description.setText("提示: 触摸我无法实现收缩效果");
        Glide.with(this).load(entity.getImage_url()).into(iv_detail_top);

        list = new ArrayList<>();
        for (int i = 0; i < 20; i++){
            list.add(entity.getDescription()+i);
        }

        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false));
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ((ViewHolder)holder).textView.setText(list.get(position));
            }

            @Override
            public int getItemCount() {
                return list.size();
            }

            class ViewHolder extends RecyclerView.ViewHolder{

                public TextView textView;

                public ViewHolder(View itemView) {
                    super(itemView);
                    textView = (TextView) itemView.findViewById(android.R.id.text1);
                }
            }
        });
    }

}

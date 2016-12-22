package com.ygl.test.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ygl.test.R;
import com.ygl.test.activity.DetailActivity;
import com.ygl.test.greendao.entity.ImageEntity;

import java.util.List;

/**
 * author：ygl_panpan on 2016/12/22 15:57
 * email：pan.lq@i70tv.com
 */
public class MainRecyclerAdapter extends RecyclerView.Adapter<MainRecyclerAdapter.ViewHolder> {

    private List<ImageEntity> list;

    public MainRecyclerAdapter(){
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_recyc, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.tv_item_main_recyc.setText(list.get(position).getDescription());
        Glide.with(holder.itemView.getContext()).load(list.get(position).getImage_url()).into(holder.iv_item_main_recyc);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getContext().startActivity(DetailActivity.newInstance(v.getContext(), list.get(position)));
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView iv_item_main_recyc;
        public TextView tv_item_main_recyc;

        public ViewHolder(View v) {
            super(v);
            iv_item_main_recyc = (ImageView) v.findViewById(R.id.iv_item_main_recyc);
            tv_item_main_recyc = (TextView) v.findViewById(R.id.tv_item_main_recyc);
        }
    }

    public void setList(List<ImageEntity> list){
        this.list = list;
        notifyDataSetChanged();
    }
}

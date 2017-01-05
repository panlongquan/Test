package com.ygl.test.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ygl.medialib.SimpleFixedTextureVideoView;
import com.ygl.test.R;
import com.ygl.test.greendao.entity.VideoEntity;
import com.ygl.test.listener.RecyclerItemClickListener;

import java.util.List;

/**
 * author：ygl_panpan on 2017/1/5 17:19
 * email：pan.lq@i70tv.com
 */
public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> {

    private Activity context;
    private List<VideoEntity> videoList;

    public VideoListAdapter (Activity context, List<VideoEntity> videoList) {
        this.context = context;
        this.videoList = videoList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_recyc, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VideoEntity entity = videoList.get(position);
        holder.simpleFixedTextureVideoView.setVideoData(entity.getVideoWidth(), entity.getVideoUrl(), entity.getThumbnailUrl());
    }

    @Override
    public int getItemCount() {
        return videoList == null ? 0 : videoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public SimpleFixedTextureVideoView simpleFixedTextureVideoView;

        public ViewHolder(View v) {
            super(v);
            simpleFixedTextureVideoView = (SimpleFixedTextureVideoView) v.findViewById(R.id.item_simpleFixedTextureVideoView);
        }
    }

    public void setList(List<VideoEntity> list){
        this.videoList = list;
        notifyDataSetChanged();
    }

    public RecyclerItemClickListener.OnItemClickListener onItemClickListener = new RecyclerItemClickListener.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
//            Intent intent = DetailActivity.newInstance(view.getContext(), list.get(position));
//            ActivityCompat.startActivity(context, intent, options.toBundle());
        }
    };

}

package com.ygl.test.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ygl.test.R;
import com.ygl.test.activity.DetailActivity;
import com.ygl.test.greendao.entity.ImageEntity;
import com.ygl.test.listener.RecyclerItemClickListener;
import com.ygl.utilslib.Utils;

import java.util.List;

/**
 * author：ygl_panpan on 2016/12/22 15:57
 * email：pan.lq@i70tv.com
 */
public class MainRecyclerAdapter extends RecyclerView.Adapter<MainRecyclerAdapter.ViewHolder> {

    private List<ImageEntity> list;
    private Activity context;

    private boolean animateItems = false;
    private int lastAnimatedPosition = -1;

    public MainRecyclerAdapter(Activity context){
        TypedValue mTypedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        animateItems = true;
        lastAnimatedPosition = -1;
        notifyDataSetChanged();
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_recyc, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        runEnterAnimation(holder.itemView, position);
        holder.tv_item_main_recyc.setText(list.get(position).getDescription());
        Glide.with(holder.itemView.getContext()).load(list.get(position).getImage_url()).into(holder.iv_item_main_recyc);
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

    public RecyclerItemClickListener.OnItemClickListener onItemClickListener = new RecyclerItemClickListener.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            Intent intent = DetailActivity.newInstance(view.getContext(), list.get(position));

            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(context,
                            view.findViewById(R.id.iv_item_main_recyc),context.getString(R.string.transition_book_img));

            //让新的Activity从一个小的范围扩大到全屏
//            ActivityOptionsCompat options =
//                    ActivityOptionsCompat.makeScaleUpAnimation(view, //The View that the new activity is animating from
//                            (int)view.getWidth()/2, (int)view.getHeight()/2, //拉伸开始的坐标
//                            0, 0);//拉伸开始的区域大小，这里用（0，0）表示从无到全屏

            ActivityCompat.startActivity(context, intent, options.toBundle());
        }
    };

    private void runEnterAnimation(View view, int position) {
        if (!animateItems || position >= 3) {
            return;
        }

        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            view.setTranslationY(Utils.getScreenHeight(context));
            view.animate()
                    .translationY(0)
                    .setStartDelay(100 * position)
                    .setInterpolator(new DecelerateInterpolator(3.f))
                    .setDuration(700)
                    .start();
        }
    }
}

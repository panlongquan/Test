package com.ygl.medialib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Formatter;
import java.util.Locale;

import static android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM;

/**
 * 针对普通播放自定义的MediaController
 *
 */
public class CustomMediaController extends FrameLayout {
    private static final int sDefaultTimeout = 3500;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private MediaPlayerControl mPlayer;
    private Context mContext;
    private View mAnchor;
    private View mRoot;
    private SeekBar mProgress;
    private TextView mEndTime, mCurrentTime;
    /**
     * 重播和分享按钮
     */
    private TextView replay, share;
    /**
     * 缩略图
     */
    private ImageView thumbnail;
    /**
     * 下方time和seekbar的父布局
     */
    private LinearLayout ll_time_seek;
    /**
     *
     */
    private LinearLayout ll_pause;
    /**
     *
     */
    private LinearLayout ll_loading;
    /**
     * 重播和分享父布局
     */
    private LinearLayout ll_completion;
    /**
     * 是否初始化, 用于初始化时控制可全屏点击播放的状态
     */
    public boolean isInit;
    /**
     * 如果为true, 点击触摸都无效
     */
    private boolean isLoading;

    private int width;
    private int height;
    private Bitmap thumbnailBit;
    private String thumbnailUrl;

    private long mDuration;
    private boolean mShowing;
    private boolean mDragging;
    private boolean mInstantSeeking = true;
    private boolean mFromXml = false;
    private ImageButton mPauseButton;
    private AudioManager mAM;
    private OnShownListener mShownListener;
    private OnHiddenListener mHiddenListener;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            long pos;
            switch (msg.what) {
                case FADE_OUT:
                    hide();
                    break;
                case SHOW_PROGRESS:
                    pos = getCurrentProgress();
                    if (!mDragging && mShowing && mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    };
    private static final String TAG = "SimpleFixedTextureVideo";
    private View.OnClickListener mPauseListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (v.getId() == R.id.ll_pause){
                Log.i(TAG, "onClick: ");
                hide();
                prepared_hide();
                if (isInit){
                    loading_show();
                    isInit = false;
                    mPlayer.start();
                    if (mPlayer instanceof FixedTextureVideoView && ((FixedTextureVideoView) mPlayer).isNativePlayer()){
                        //如果是本地视频,开始播放后无需展示进度条, 并隐藏缩略图
                        hide();
                        thumbnail.setVisibility(View.GONE);
                    }
                }
            } else if (v.getId() == R.id.pause) {
                doPauseResume();
            } else if (v.getId() == R.id.replay){//重播
                hide();
                mPlayer.seekTo(0);
                mPlayer.start();
            } else if (v.getId() == R.id.share){//分享 TODO...
                Log.i("plq", "share");
            }
        }
    };

    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            mDragging = true;
            show(3600000);
            mHandler.removeMessages(SHOW_PROGRESS);
            if (mInstantSeeking)
                mAM.setStreamMute(AudioManager.STREAM_MUSIC, true);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser)
                return;

            long newposition = (mDuration * progress) / 1000;
            String time = stringForTime((int) newposition);
            if (mInstantSeeking)
                mPlayer.seekTo((int) newposition);
            if (mCurrentTime != null)
                mCurrentTime.setText(time);
        }

        public void onStopTrackingTouch(SeekBar bar) {
            if (!mInstantSeeking)
                mPlayer.seekTo((int) ((mDuration * bar.getProgress()) / 1000));
            getCurrentProgress();
            updatePausePlay();
            show(sDefaultTimeout);
            mHandler.removeMessages(SHOW_PROGRESS);
            mAM.setStreamMute(AudioManager.STREAM_MUSIC, false);
            mDragging = false;
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
//            mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 1000);
        }
    };

    /**
     * 在xml创建但是没有指定style的时候被调用
     * @param context
     * @param attrs
     */
    public CustomMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = this;
        mFromXml = true;
        initController(context);
    }

    /**
     * 代码创建被调用
     * @param context
     */
    public CustomMediaController(Context context) {
        super(context);
        initController(context);
    }

    public CustomMediaController(Context context, int width, int height, Bitmap thumbnailBit, String thumbnailUrl) {
        super(context);
        this.width = width;
        this.height = height;
        this.thumbnailBit = thumbnailBit;
        this.thumbnailUrl = thumbnailUrl;
        initController(context);
    }

    /**
     * 恒定返回true
     * @param context
     * @return
     */
    private boolean initController(Context context) {
        mContext = context;
        mAM = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        return true;
    }

    @Override
    public void onFinishInflate() {
        // 如果不是XML创建,不会走该方法
        if (mRoot != null)
            initControllerView(mRoot);
    }

    /**
     * 初始化MediaController的各个按钮
     * @param v
     */
    private void initControllerView(View v) {
        isInit = true;
        mPauseButton = (ImageButton) v.findViewById(R.id.pause);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }

        mProgress = (SeekBar) v.findViewById(R.id.mediacontroller_progress);
        if (mProgress != null) {
            mProgress.setOnSeekBarChangeListener(mSeekListener);
            mProgress.setThumbOffset(1);
            mProgress.setMax(1000);
        }

        mEndTime = (TextView) v.findViewById(R.id.time);
        mCurrentTime = (TextView) v.findViewById(R.id.time_current);

        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        ll_loading = (LinearLayout) v.findViewById(R.id.ll_loading);
        ll_pause = (LinearLayout) v.findViewById(R.id.ll_pause);
        ll_pause.setOnClickListener(mPauseListener);
        ll_time_seek = (LinearLayout) v.findViewById(R.id.ll_time_seek);
        ll_completion = (LinearLayout) v.findViewById(R.id.ll_completion);
        replay = (TextView) v.findViewById(R.id.replay);
        share = (TextView) v.findViewById(R.id.share);
        replay.setOnClickListener(mPauseListener);
        share.setOnClickListener(mPauseListener);

        thumbnail = (ImageView) v.findViewById(R.id.thumbnail);
        thumbnail.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams ivp = (RelativeLayout.LayoutParams) thumbnail.getLayoutParams();
        ivp.width = width;
        ivp.height = height;
        thumbnail.setLayoutParams(ivp);

        if (TextUtils.isEmpty(thumbnailUrl)){
            thumbnail.setImageBitmap(thumbnailBit);
        } else {
//            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mContext).build();
//            ImageLoader loader=ImageLoader.getInstance();
//            loader.init(config);
//            loader.displayImage(thumbnailUrl,thumbnail);
            Glide.with(mContext).load(thumbnailUrl).into(thumbnail);
        }

        prepared_show();
    }

    /**
     * Set the view that acts as the anchor for the control view. This can for
     * example be a VideoView, or your Activity's main view.
     *
     * @param view
     *            The view to which to anchor the controller when it is visible.
     */
    public void setAnchorView(View view) {
        mAnchor = view;
        if (!mFromXml) {
            removeAllViews();
            mRoot = makeControllerView();

            RelativeLayout.LayoutParams pp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            pp.addRule(ALIGN_PARENT_BOTTOM);
            mRoot.setLayoutParams(pp);
            ((ViewGroup)view).addView(mRoot);
        }
        initControllerView(mRoot);
    }

    /**
     * 加载mediaController布局
     * @return
     */
    protected View makeControllerView() {
        return ((LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.layout_mediacontroller_full, this);
    }

    public void setMediaPlayer(MediaPlayerControl player) {
        mPlayer = player;
        if(mShowing){
            updatePausePlay();
        }
    }

    public void show() {
        show(sDefaultTimeout);
    }

    /**
     * 使mPauseButton按钮可用(点击), setEnabled(true)
     */
    private void disableUnsupportedButtons() {
        try {
            if (mPauseButton != null && !mPlayer.canPause())
                mPauseButton.setEnabled(true);
        } catch (IncompatibleClassChangeError ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 添加并显示mediaController, 发送延迟消息隐藏mediaController
     * @param timeout
     */
    public void show(int timeout) {
        mHandler.removeMessages(FADE_OUT);
        if (!mShowing && mAnchor != null && mAnchor.getWindowToken() != null) {
            getCurrentProgress();
            if (mPauseButton != null)
                mPauseButton.requestFocus();
            disableUnsupportedButtons();

            if (mFromXml) {
                setVisibility(View.VISIBLE);
            } else {
                RelativeLayout.LayoutParams pp = (RelativeLayout.LayoutParams) mRoot.getLayoutParams();
                pp.addRule(ALIGN_PARENT_BOTTOM);
                if(mRoot.getParent() != null){
                    ((ViewGroup)mRoot.getParent()).removeView(mRoot);
                }
                ((ViewGroup)mAnchor).addView(mRoot, pp);

                mRoot.setVisibility(View.VISIBLE);
            }
            playOrpause_show();
            mShowing = true;
            if (mShownListener != null)
                mShownListener.onShown();
        }

        updatePausePlay();
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT), timeout);
        }
    }

    private void buttomClick(){
        mHandler.removeMessages(FADE_OUT);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT), sDefaultTimeout);
    }

    /**
     * 隐藏mediaController
     */
    public void hide() {
        if (mAnchor == null)
            return;

        isLoading = false;
        isCompletion = false;
        thumbnail.setVisibility(View.GONE);
        if (mShowing) {
            try {
                mHandler.removeMessages(SHOW_PROGRESS);
                if (mFromXml)
                    setVisibility(View.GONE);
                else
                    mRoot.setVisibility(View.GONE);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
            mShowing = false;
            if (mHiddenListener != null)
                mHiddenListener.onHidden();
        }
    }

    /**
     * 获取当前播放的进度(getCurrentPosition), 毫秒值
     * @return
     */
    private long getCurrentProgress() {
        if (mPlayer == null || mDragging)
            return 0;

        long position = mPlayer.getCurrentPosition();
        long duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        mDuration = duration;

        if (mEndTime != null)
            mEndTime.setText(stringForTime((int) mDuration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime((int) position));

        return position;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isLoading){//加载状态, 不可触摸点击
            return true;
        }

        if (!isCompletion){
            hide();
        }

        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (event.getRepeatCount() == 0
                && (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_SPACE)) {
            doPauseResume();
            show(sDefaultTimeout);
            if (mPauseButton != null)
                mPauseButton.requestFocus();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK
                || keyCode == KeyEvent.KEYCODE_MENU) {
            hide();
            return true;
        } else {
            show(sDefaultTimeout);
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 更新暂停/播放按钮的图片, 暂停状态更新为播放图片, 反之更新为暂停图片
     */
    private void updatePausePlay() {
        if (mRoot == null || mPauseButton == null)
            return;

        if (mPlayer.isPlaying()) {
            mPauseButton.setImageResource(R.drawable.ic_media_pause);
        }else {
            mPauseButton.setImageResource(R.drawable.ic_media_play);
        }
    }

    /**
     * 切换暂停/播放状态
     */
    private void doPauseResume() {
        thumbnail.setVisibility(View.GONE);
        show(sDefaultTimeout);
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        }else {
            mPlayer.start();
        }
        updatePausePlay();
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null)
            mPauseButton.setEnabled(enabled);
        if (mProgress != null)
            mProgress.setEnabled(enabled);
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }

    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    /**
     * 格式化时间为 HH:mm:ss
     * @param timeMs
     * @return
     */
    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /********************************public**********************************/
    public boolean isShowing() {
        return mShowing;
    }

    /**
     * 准备状态的显示
     */
    public void prepared_show(){
        if (mRoot != null){
            isInit = true;
            mRoot.setVisibility(View.VISIBLE);
            mShowing = true;
            ll_time_seek.setVisibility(View.GONE);
            ll_loading.setVisibility(View.GONE);
            ll_completion.setVisibility(View.GONE);
            ll_pause.setVisibility(View.VISIBLE);
            mPlayer.seekTo(0);
        }
    }

    public void prepared_hide(){
        if (mRoot != null){
            mRoot.setVisibility(View.GONE);
            mShowing = false;
        }
    }

    /**
     * 加载状态的显示
     */
    public void loading_show(){
        if (mRoot != null){
            isLoading = true;
            mRoot.setVisibility(View.VISIBLE);
            mShowing = true;
            ll_time_seek.setVisibility(View.GONE);
            ll_loading.setVisibility(View.VISIBLE);
            ll_completion.setVisibility(View.GONE);
            ll_pause.setVisibility(View.GONE);
        }
    }

    private boolean isCompletion;

    /**
     * 完成状态的显示
     */
    public void completion_show(){
        isCompletion = true;
        if (mRoot != null){
            mRoot.setVisibility(View.VISIBLE);
            mShowing = true;
            ll_time_seek.setVisibility(View.GONE);
            ll_loading.setVisibility(View.GONE);
            ll_completion.setVisibility(View.VISIBLE);
            ll_pause.setVisibility(View.GONE);
        }
    }

    /**
     * 播放或暂停状态的显示
     */
    private void playOrpause_show(){
        ll_time_seek.setVisibility(View.VISIBLE);
        mShowing = true;
        ll_loading.setVisibility(View.GONE);
        ll_completion.setVisibility(View.GONE);
        ll_pause.setVisibility(View.VISIBLE);
    }

    /**
     * Control the action when the seekbar dragged by user
     *
     * @param seekWhenDragging
     *            True the media will seek periodically
     */
    public void setInstantSeeking(boolean seekWhenDragging) {
        mInstantSeeking = seekWhenDragging;
    }

    public void setOnShownListener(OnShownListener l) {
        mShownListener = l;
    }

    public void setOnHiddenListener(OnHiddenListener l) {
        mHiddenListener = l;
    }

    public interface OnShownListener {
        public void onShown();
    }

    public interface OnHiddenListener {
        public void onHidden();
    }

    public interface MediaPlayerControl {
        void start();

        void pause();

        int getDuration();

        int getCurrentPosition();

        void seekTo(int pos);

        boolean isPlaying();

        int getBufferPercentage();

        boolean canPause();

        boolean canSeekBackward();

        boolean canSeekForward();
    }

}
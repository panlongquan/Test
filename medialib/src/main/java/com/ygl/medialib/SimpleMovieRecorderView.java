package com.ygl.medialib;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ygl.utilslib.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * author：ygl_panpan on 2016/12/26 11:20
 * email：pan.lq@i70tv.com
 */
public class SimpleMovieRecorderView extends RelativeLayout {
    /** 对焦what */
    private static final int HANDLE_HIDE_RECORD_FOCUS = 2;
    /**
     * 录制进度what
     */
    private static final int RECORD_PROGRESS = 100;
    /**
     * 录制结束what
     */
    private static final int RECORD_FINISH = 101;

    private Context context;
    /**
     * 真实高宽比
     */
    private static float realScale = 4.0f / 3.0f;
    /**
     * 期望高宽比
     */
    private static float expectScale = 3.0f / 4.0f;
    /**
     * 录制控件
     */
    private MovieRecorderView movieRecorderView;
    /**
     * 按住拍按钮
     */
    private Button buttonShoot;
    /**
     * 录制按钮和时间的整体根布局
     */
    private RelativeLayout rlBottomRoot;
    /**
     * 录制进度条
     */
    private ProgressBar progressVideo;
    /**
     * 录制时间
     */
    private TextView textViewCountDown;
    /**
     * 上移取消
     */
    private TextView textViewUpToCancel;
    /**
     * 释放取消
     */
    private TextView textViewReleaseToCancel;
    /**
     * 对焦图标-带动画效果
     */
    private ImageView mFocusImage;
    /**
     * 是否结束录制
     */
    private boolean isFinish = true;
    /**
     * 是否触摸在松开取消的状态
     */
    private boolean isTouchOnUpToCancel = false;
    /**
     * 当前进度
     */
    private int currentTime = 0;
    /**
     * 屏幕宽度
     */
    private static int screenWidth = 0;
    /**
     * 对焦图片宽度
     */
    private int mFocusWidth;
    /**
     * 顶部遮罩区域高度
     */
    private int topMaskAreaHeight;
    /**
     * 对焦动画
     */
    private Animation mFocusAnimation;
    /**
     * video下方占位的view
     */
    private TextView placeholder;
    /**
     * 上方遮挡板
     */
    private TextView top;
    /**
     * 下方遮挡板
     */
    private TextView bottom;
    /**
     * 拍摄时视频可视高度
     */
    private int videoHeight;
    /**
     * 按下拍按钮手指按下的位置
     */
    private float startY;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RECORD_PROGRESS:
                    progressVideo.setProgress(currentTime);
                    if (currentTime < 10) {
                        textViewCountDown.setText("00:0" + currentTime);
                    } else {
                        textViewCountDown.setText("00:" + currentTime);
                    }
                    break;
                case RECORD_FINISH:
                    if (isTouchOnUpToCancel) {//录制结束，还在上移删除状态没有松手，就复位录制
                        resetData();
                    } else {//录制结束，在正常位置，录制完成跳转页面
                        isFinish = true;
                        buttonShoot.setEnabled(false);
                        finishActivity();
                    }
                    break;
                case HANDLE_HIDE_RECORD_FOCUS:
                    mFocusImage.setVisibility(View.GONE);
                    break;
            }
        }
    };

    public SimpleMovieRecorderView(Context context) {
        super(context);
        initLayout(context);
    }

    public SimpleMovieRecorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout(context);
    }

    public SimpleMovieRecorderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout(context);
    }

    private void initLayout(Context context) {
        this.context = context;
        View root = LayoutInflater.from(context).inflate(R.layout.layout_simple_movie_recorder, null);
        addView(root);

        movieRecorderView = (MovieRecorderView) root.findViewById(R.id.movieRecorderView);
        buttonShoot = (Button) root.findViewById(R.id.button_shoot);
        rlBottomRoot = (RelativeLayout) root.findViewById(R.id.rl_bottom_root);
        progressVideo = (ProgressBar) root.findViewById(R.id.progressBar_loading);
        textViewCountDown = (TextView) root.findViewById(R.id.textView_count_down);
        textViewUpToCancel = (TextView) root.findViewById(R.id.textView_up_to_cancel);
        textViewReleaseToCancel = (TextView) root.findViewById(R.id.textView_release_to_cancel);
        mFocusImage = (ImageView) root.findViewById(R.id.record_focusing);
        top = (TextView) root.findViewById(R.id.top_tv);
        bottom = (TextView) root.findViewById(R.id.bottom_tv);
        placeholder = (TextView) root.findViewById(R.id.placeholder_tv);

        initData();
        initControl();
    }

    private void initData() {
        screenWidth = Utils.getScreenWidth(context);
        mFocusWidth = (int) Utils.dipToPX(context, 64);
        progressVideo.setMax(10);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) movieRecorderView.getLayoutParams();
        layoutParams.width = screenWidth;
        layoutParams.height = (int) (screenWidth * realScale);//根据屏幕宽度设置预览控件的尺寸，为了解决预览拉伸问题
        movieRecorderView.setLayoutParams(layoutParams);

        videoHeight = (int) (screenWidth * expectScale);
        // 视频真实高度
        int actualH = (int) (screenWidth * realScale);
        //占位view的高度
        int placeholderH = Utils.getScreenHeight(context) - actualH;
        // 上下被遮挡的总高度
        int diffV = actualH - videoHeight;

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) top.getLayoutParams();
        topMaskAreaHeight = diffV/2;
        params.height = diffV/2;
        top.setLayoutParams(params);

        RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) bottom.getLayoutParams();
        params1.height = diffV/2;
        bottom.setLayoutParams(params1);

        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) placeholder.getLayoutParams();
        params2.height = placeholderH;
        placeholder.setLayoutParams(params2);

        RelativeLayout.LayoutParams rlBottomRootLayoutParams = (RelativeLayout.LayoutParams) rlBottomRoot.getLayoutParams();
        rlBottomRootLayoutParams.height = placeholderH + diffV/2;
        rlBottomRoot.setLayoutParams(rlBottomRootLayoutParams);
    }

    private void initControl() {
        SurfaceView sf = movieRecorderView.getSurfaceView();
        if (sf != null){
            sf.setOnTouchListener(mOnSurfaveViewTouchListener);
        }

        //按下拍按钮的触摸事件处理
        buttonShoot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    textViewUpToCancel.setVisibility(View.VISIBLE);//提示上移取消

                    isFinish = false;//开始录制
                    startY = event.getY();//记录按下的坐标
                    movieRecorderView.record(new MovieRecorderView.OnRecordFinishListener() {
                        @Override
                        public void onRecordFinish() {
                            handler.sendEmptyMessage(RECORD_FINISH);
                        }
                    });
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    textViewUpToCancel.setVisibility(View.GONE);
                    textViewReleaseToCancel.setVisibility(View.GONE);

                    if (startY - event.getY() > 100) {//上移超过一定距离取消录制，删除文件
                        if (!isFinish) {
                            resetData();
                        }
                    } else {
                        if (movieRecorderView.getTimeCount() >= 3) {//录制时间超过三秒，录制完成
                            handler.sendEmptyMessage(RECORD_FINISH);
                        } else {//时间不足取消录制，删除文件
                            Toast.makeText(context, "视频录制时间太短", Toast.LENGTH_SHORT).show();
                            resetData();
                        }
                    }
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    //根据触摸上移状态切换提示
                    if (startY - event.getY() > 100) {
                        isTouchOnUpToCancel = true;//触摸在松开就取消的位置
                        if (textViewUpToCancel.getVisibility() == View.VISIBLE) {
                            textViewUpToCancel.setVisibility(View.GONE);
                            textViewReleaseToCancel.setVisibility(View.VISIBLE);
                        }
                    } else {
                        isTouchOnUpToCancel = false;//触摸在正常录制的位置
                        if (textViewUpToCancel.getVisibility() == View.GONE) {
                            textViewUpToCancel.setVisibility(View.VISIBLE);
                            textViewReleaseToCancel.setVisibility(View.GONE);
                        }
                    }
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    resetData();
                }
                return true;
            }
        });

        movieRecorderView.setOnRecordProgressListener(new MovieRecorderView.OnRecordProgressListener() {
            @Override
            public void onProgressChanged(int maxTime, int currentTime) {
                SimpleMovieRecorderView.this.currentTime = currentTime;
                handler.sendEmptyMessage(RECORD_PROGRESS);
            }
        });
    }

    /** 点击屏幕对焦 */
    private View.OnTouchListener mOnSurfaveViewTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (movieRecorderView == null) {
                return false;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 检测是否手动对焦
                    if (checkCameraFocus(event))
                        return true;
                    break;
            }
            return true;
        }
    };

    /** 手动对焦 */
    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private boolean checkCameraFocus(MotionEvent event) {
        mFocusImage.setVisibility(View.GONE);
        float x = event.getX();
        float y = event.getY();
        float touchMajor = event.getTouchMajor();
        float touchMinor = event.getTouchMinor();

        if(y <= topMaskAreaHeight){
            return false;
        }

        Rect touchRect = new Rect((int) (x - touchMajor / 2),
                (int) (y - touchMinor / 2), (int) (x + touchMajor / 2),
                (int) (y + touchMinor / 2));
        if (touchRect.right > screenWidth)
            touchRect.right = screenWidth;
        if (touchRect.left < 0)
            touchRect.left = 0;
        if (touchRect.right < 0)
            touchRect.right = 0;

        int mH = topMaskAreaHeight + videoHeight;
        if (touchRect.bottom > mH)
            touchRect.bottom = mH;

        if (touchRect.left >= touchRect.right
                || touchRect.top >= touchRect.bottom)
            return false;

        ArrayList<Camera.Area> focusAreas = new ArrayList<>();
        focusAreas.add(new Camera.Area(touchRect, 1000));
        if (!movieRecorderView.manualFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                mFocusImage.setVisibility(View.GONE);
                camera.cancelAutoFocus();
            }
        }, focusAreas)) {
            mFocusImage.setVisibility(View.GONE);
        }

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mFocusImage
                .getLayoutParams();
        int left = touchRect.left - (mFocusWidth / 2);
        int top = touchRect.top - (mFocusWidth / 2);
        if (left < 0)
            left = 0;
        else if (left + mFocusWidth > screenWidth)
            left = screenWidth - mFocusWidth;
        if (top + mFocusWidth > mH) {
            top = mH - mFocusWidth;
        } else if (top - mFocusWidth/2 < topMaskAreaHeight) {
            top = topMaskAreaHeight;
        }

        lp.leftMargin = left;
        lp.topMargin = top;
        mFocusImage.setLayoutParams(lp);
        mFocusImage.setVisibility(View.VISIBLE);

        if (mFocusAnimation == null)
            mFocusAnimation = AnimationUtils.loadAnimation(context,
                    R.anim.record_focus);

        mFocusImage.startAnimation(mFocusAnimation);

        handler.sendEmptyMessageDelayed(HANDLE_HIDE_RECORD_FOCUS, 2500);// 最多3.5秒也要消失
        return true;
    }

    /**
     * 重置所有状态and删除当前录制的视频
     */
    private void resetData() {
        if (movieRecorderView.getRecordFile() != null)
            movieRecorderView.getRecordFile().delete();
        movieRecorderView.stop();
        isFinish = true;
        currentTime = 0;
        progressVideo.setProgress(0);
        textViewCountDown.setText("00:00");
        buttonShoot.setEnabled(true);
        textViewUpToCancel.setVisibility(View.GONE);
        textViewReleaseToCancel.setVisibility(View.GONE);
        try {
            movieRecorderView.initCamera();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重置所有状态and删除当前录制的视频
     */
    public void reset(){
        resetData();
    }

    /**
     * 录制完成
     */
    private void finishActivity() {
        if (isFinish) {
            movieRecorderView.stop();
            if(listener != null) {
                listener.finish(movieRecorderView.getRecordFile());
            }
        }
    }

    /**
     * 录制完成监听(录制完成指的是手指从'按住拍'按钮上弹起并录制时间大于等于3s或者等于最大录制时间)
     */
    private OnVideoFinishListener listener;

    public void setOnVideoFinishListener(OnVideoFinishListener listener){
        this.listener = listener;
    }

    public void setFinish(boolean finish) {
        isFinish = finish;
    }

    /**
     * 停止拍摄
     */
    public void stop(){
        if (movieRecorderView != null)
            movieRecorderView.stop();
    }

    /**
     * 删除当前录制的视频文件
     */
    public void delCurrentMovie() {
        if (movieRecorderView != null) {
            File f = movieRecorderView.getRecordFile();
            if (f != null && f.exists()) {
                f.delete();
            }
        }
    }

    /**
     * 获取高宽比例
     * @return
     */
    public static float getScale() {
        return expectScale;
    }

    /**
     * 设置高宽比例, 传递参数时你可以这样传递: setScale(3.0f / 4.0f)
     * @param scale
     */
    public static void setScale(float scale) {
        SimpleMovieRecorderView.expectScale = scale;
    }

}
/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ygl.medialib;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.IOException;
import java.util.Map;

public class FixedTextureVideoView extends TextureView implements CustomMediaController.MediaPlayerControl {// implements MediaController.MediaPlayerControl / CustomMediaController.MediaPlayerControl
    private Uri mUri;
    private Map<String, String> mHeaders;
    /**
     * 错误状态
     */
    private static final int STATE_ERROR              = -1;
    /**
     * 闲置状态
     */
    private static final int STATE_IDLE               = 0;
    private static final int STATE_PREPARING          = 1;
    private static final int STATE_PREPARED           = 2;
    private static final int STATE_PLAYING            = 3;
    private static final int STATE_PAUSED             = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    private static final int HIDE_PROGRESSBAR_BUFFER_BORDER = 30;

    private int mCurrentState = STATE_IDLE;
    private int mTargetState  = STATE_IDLE;

    private Surface mSurface = null;
    private MediaPlayer mMediaPlayer = null;
    private int         mAudioSession;
    /**
     * 视频的真实宽度
     */
    private int         mVideoWidth;
    /**
     * 视频的真实高度
     */
    private int         mVideoHeight;

    private CustomMediaController mMediaController;
//    private MediaController mMediaController;

    private OnCompletionListener mOnCompletionListener;
    private OnBufferBorderListener mOnBufferBorderListener;

    private int         mCurrentBufferPercentage = 100;
    private int         mSeekWhenPrepared;  // recording the seek position while preparing
    private boolean     mCanPause;
    private boolean     mCanSeekBack;
    private boolean     mCanSeekForward;

    private int fixedWidth;
    private int fixedHeight;
    private Matrix matrix;
    private Context mContext;

    public FixedTextureVideoView(Context context) {
        super(context);
        initVideoView(context);
    }

    public FixedTextureVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initVideoView(context);
    }

    public FixedTextureVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initVideoView(context);
    }

    private void initVideoView(Context context) {
        mContext = context;
        mVideoWidth = 0;
        mVideoHeight = 0;
        setSurfaceTextureListener(mSurfaceTextureListener);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = STATE_IDLE;
        mTargetState  = STATE_IDLE;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (fixedWidth == 0 || fixedHeight == 0) {
            defaultMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            setMeasuredDimension(fixedWidth, fixedHeight);
        }
    }

    protected void defaultMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {//精确尺寸
                width = widthSpecSize;
                height = heightSpecSize;

                if ( mVideoWidth * height  < width * mVideoHeight ) {
                    // xml中定义的宽高比与实际上的图片(或视频)不符, XML中的height太小了,
                    // 所以按照实际上的图片(或视频)重新计算width
                    width = height * mVideoWidth / mVideoHeight;
                } else if ( mVideoWidth * height  > width * mVideoHeight ) {
                    // xml中定义的宽高比与实际上的图片(或视频)不符, XML中的width太小了,
                    // 所以按照实际上的图片(或视频)重新计算height
                    height = width * mVideoHeight / mVideoWidth;
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {//xml中width是写的精确值
                width = widthSpecSize;
                height = width * mVideoHeight / mVideoWidth;
                //layout_height指定为WRAP_CONTENT时, 并且计算的height比WRAP_CONTENT要大, 就使用WRAP_CONTENT得到的height, 否则就用计算的height
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    height = heightSpecSize;
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                height = heightSpecSize;
                width = height * mVideoWidth / mVideoHeight;
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    width = widthSpecSize;
                }
            } else {
                width = mVideoWidth;
                height = mVideoHeight;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    height = heightSpecSize;
                    width = height * mVideoWidth / mVideoHeight;
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    width = widthSpecSize;
                    height = width * mVideoHeight / mVideoWidth;
                }
            }
        }
        setMeasuredDimension(width, height);
    }

    /**
     * 设置video path, 并MediaPlayer调用prepareAsync方法进入准备状态, 一旦调用start就会开始播放
     *
     * @param path the path of the video.
     * @param isHasAudio 是否播放声音, 默认false
     */
    public void setVideoPath(String path, boolean isHasAudio) {
        this.isHasAudio = isHasAudio;
        setVideoURI(Uri.parse(path));
    }

    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    /**
     * Sets video URI using specific headers.
     *
     * @param uri     the URI of the video.
     * @param headers the headers for the URI request.
     *     默认情况下是允许跨域重定向的, 通过键"android-allow-cross-domain-redirect"和值“0”或“1”来禁止或允许跨域重定向
     */
    private void setVideoURI(Uri uri, Map<String, String> headers) {
        //本地: uri = /storage/emulated/0/SampleVideo/video/1481076111603.mp4
        //网络: uri = http://127.0.0.1:43422/http%3A%2F%2Fvideo.jiecao.fm%2F8%2F16%2F%25E4%25BF%25AF%25E5%258D%25A7%25E6%2592%2591.mp4
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
    }

    /**
     * 停止播放
     */
    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
//            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState  = STATE_IDLE;
            AudioManager am = (AudioManager) getContext().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
        }
    }

    private void openVideo() {
        if (mUri == null || mSurface == null) {
            return;
        }
        release(false);

        AudioManager am = (AudioManager) getContext().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        try {
            mMediaPlayer = new MediaPlayer();

            if (mAudioSession != 0) {
                mMediaPlayer.setAudioSessionId(mAudioSession);
            } else {
                mAudioSession = mMediaPlayer.getAudioSessionId();
            }
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);

            mCurrentBufferPercentage = 0;
            mMediaPlayer.setDataSource(getContext().getApplicationContext(), mUri, mHeaders);
            mMediaPlayer.setSurface(mSurface);
            setAudioStreamType();
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
//            mMediaPlayer.prepare();

            mCurrentState = STATE_PREPARING;
            attachMediaController();
        } catch (IOException ex) {
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
    }

    /**
     * 需求:视频等比例放大,直至一边铺满View的某一边,另一边超出View的另一边,再移动到View的正中央,
     * 这样长边两边会被裁剪掉同样大小的区域,视频看起来不会变形,也即是:先把视频区(实际的大小显示区)
     * 与View(定义的大小)区的两个中心点重合, 然后等比例放大或缩小视频区,直至一条边与View的一条边相等,
     * 另一条边超过View的另一条边,这时再裁剪掉超出的边, 使视频区与View区大小一样. 这样在不同尺寸的手机上,
     * 视频看起来不会变形,只是水平或竖直方向的两端被裁剪了一些.
     *
     * @param videoWidth
     * @param videoHeight
     */
    private void transformVideo(int videoWidth, int videoHeight) {
        if (getResizedHeight() == 0 || getResizedWidth() == 0) {
            return;
        }
        float sx = (float) getResizedWidth() / (float) videoWidth;
        float sy = (float) getResizedHeight() / (float) videoHeight;

        float maxScale = Math.max(sx, sy);
        if (this.matrix == null) {
            matrix = new Matrix();
        } else {
            matrix.reset();
        }

        //第2步:把视频区移动到View区,使两者中心点重合.
        matrix.preTranslate((getResizedWidth() - videoWidth) / 2, (getResizedHeight() - videoHeight) / 2);

        //第1步:因为默认视频是fitXY的形式显示的,所以首先要缩放还原回来.
        matrix.preScale(videoWidth / (float) getResizedWidth(), videoHeight / (float) getResizedHeight());

        //第3步,等比例放大或缩小,直到视频区的一边超过View一边, 另一边与View的另一边相等. 因为超过的部分超出了View的范围,所以是不会显示的,相当于裁剪了.
        matrix.postScale(maxScale, maxScale, getResizedWidth() / 2, getResizedHeight() / 2);//后两个参数坐标是以整个View的坐标系以参考的

        setTransform(matrix);
        postInvalidate();
    }

    private int getResizedWidth() {
        if (fixedWidth == 0) {
            return getWidth();
        } else {
            return fixedWidth;
        }
    }

    private int getResizedHeight() {
        if (fixedHeight== 0) {
            return getHeight();
        } else {
            return fixedHeight;
        }
    }

    /**
     * release the media player in any state
     */
    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState  = STATE_IDLE;
            }
            AudioManager am = (AudioManager) getContext().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
        }
    }

    /**
     * 设置MediaControl的显示与否, 如果正在显示就隐藏, 反之显示
     */
    private void toggleMediaControlsVisiblity() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    /**
     * 判断是否是正在播放/暂停/播放完成
     * @return
     */
    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }

    /**********************以下是所有设置监听方法**************************/
    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    public void setOnBufferBorderListener(OnBufferBorderListener l){
        mOnBufferBorderListener = l;
    }

    /**********************以下是所有监听**************************/
    private OnPreparedListener mPreparedListener = new OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mCurrentState = STATE_PREPARED;

            mCanPause = mCanSeekBack = mCanSeekForward = true;

            if (mMediaController != null) {
                mMediaController.setEnabled(true);
            }
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            int seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                getSurfaceTexture().setDefaultBufferSize(mVideoWidth, mVideoHeight);
                if (mTargetState == STATE_PLAYING) {
                    start();
                } else if ( !isPlaying() &&
                        (seekToPosition != 0 || getCurrentPosition() > 0) ) {
                    if (mMediaController != null) {
                        mMediaController.show(0);
                    }
                }
            } else {
                // We don't know the video size yet, but should start anyway.
                // The video size might be reported to us later.
                if (mTargetState == STATE_PLAYING) {
                    start();
                }
            }
        }
    };

    private OnCompletionListener mCompletionListener = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            mTargetState = STATE_PLAYBACK_COMPLETED;
            if (mMediaController != null) {
                mMediaController.hide();
                mMediaController.completion_show();
            }
            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(mMediaPlayer);
            }
        }
    };

    private OnInfoListener mInfoListener = new OnInfoListener() {
        @Override
        public  boolean onInfo(MediaPlayer mp, int what, int extra) {
            return true;
        }
    };

    private OnBufferingUpdateListener mBufferingUpdateListener = new OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            mCurrentBufferPercentage = percent;
            if (mp.isPlaying() && percent > HIDE_PROGRESSBAR_BUFFER_BORDER){
                mMediaController.hide();
                if (mOnBufferBorderListener != null) {
                    mOnBufferBorderListener.bufferBorder(true);
                }
            }
        }
    };

    private OnErrorListener mErrorListener = new OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            if (mMediaController != null) {
                mMediaController.hide();
            }

            /* Otherwise, pop up an error dialog so the user knows that
             * something bad has happened. Only try and pop up the dialog
             * if we're attached to a window. When we're going away and no
             * longer have a window, don't bother showing the user an error.
             */
            if (getWindowToken() != null) {
                Resources r = getContext().getResources();
                int messageId;

                if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                    messageId = android.R.string.VideoView_error_text_invalid_progressive_playback;
                } else {
                    messageId = android.R.string.VideoView_error_text_unknown;
                }

                new AlertDialog.Builder(getContext())
                        .setMessage(messageId)
                        .setPositiveButton(android.R.string.VideoView_error_button,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        /* If we get here, there is no onError listener, so
                                         * at least inform them that the video is over.
                                         */
                                        if (mOnCompletionListener != null) {
                                            mOnCompletionListener.onCompletion(mMediaPlayer);
                                        }
                                    }
                                })
                        .setCancelable(false)
                        .show();
            }
            return true;
        }
    };

    private SurfaceTextureListener mSurfaceTextureListener = new SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width, final int height) {
            boolean isValidState =  (mTargetState == STATE_PLAYING);
            boolean hasValidSize = (width > 0 && height > 0);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
        }

        @Override
        public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
            mSurface = new Surface(surface);
            openVideo();
        }

        @Override
        public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
            // after we return from this we can't use the surface any more
            if (mSurface != null) {
                mSurface.release();
                mSurface = null;
            }
            if (mMediaController != null) {
                mMediaController.hide();
            }
            release(true);
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(final SurfaceTexture surface) {
        }

    };

    private OnVideoSizeChangedListener mSizeChangedListener = new OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                getSurfaceTexture().setDefaultBufferSize(mVideoWidth, mVideoHeight);
                requestLayout();
                transformVideo(mVideoWidth, mVideoHeight);
            }
        }
    };

    public interface OnBufferBorderListener{
        void bufferBorder(boolean isHideProgress);
    }

    /**********************以下是MediaController#MediaPlayerControl接口方法, 相关操作方法**************************/
    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getDuration();
        }
        return -1;
    }

    @Override
    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //如果是是正在播放/暂停/播放完成的状态, 触摸视频反向显示MediaController
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isInPlaybackState() && mMediaController != null) {
                    toggleMediaControlsVisiblity();
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        //如果是是正在播放/暂停/播放完成的状态, 触摸视频反向显示MediaController
        if (isInPlaybackState() && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return super.onTrackballEvent(ev);
    }

    private void attachMediaController() {
        if (mMediaPlayer != null){
            if (com.ygl.utilslib.TextUtils.isEmpty(mThumbnailUrl)){
                mMediaController = new CustomMediaController(mContext, fixedWidth, fixedHeight, thumbnailBit, null);
            } else {
                mMediaController = new CustomMediaController(mContext, fixedWidth, fixedHeight, null, mThumbnailUrl);
            }
            mMediaController.hide();
            mMediaController.setMediaPlayer(this);

            View anchorView = this.getParent() instanceof View ?
                    (View)this.getParent() : this;

            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(isInPlaybackState());
        }
    }

    /***********************以下是对外提供的方法***************************/
    /**
     * 是否播放的是本地视频
     * @return
     */
    public boolean isNativePlayer(){
        return !TextUtils.isEmpty(mUri.toString()) && !mUri.toString().contains("http");
    }
    /**
     * 设置视频固定宽高, 调用此方法需要再次调用setVideoPath(String path, boolean isHasAudio)方法设置视频路径
     * @param width
     * @param height
     */
    public void setFixedSize(int width, int height) {
        fixedHeight = height;
        fixedWidth = width;
        requestLayout();//调用该方法会重走onMeasure方法
    }

    private String mThumbnailUrl;

    /**
     * 设置视频固定宽高和视频路径, 一步到位
     * @param width
     * @param height
     * @param videoPath
     * @param isHasAudio
     */
    public void setFixedSize(int width, int height, String videoPath, String thumbnailUrl, boolean isHasAudio) {
        fixedHeight = height;
        fixedWidth = width;
        mThumbnailUrl = thumbnailUrl;
        setVideoPath(videoPath, isHasAudio);
    }

    private Bitmap thumbnailBit;
    public void setFixedSize(int width, int height, String videoPath, Bitmap thumbnailBit, boolean isHasAudio) {
        fixedHeight = height;
        fixedWidth = width;
        this.thumbnailBit = thumbnailBit;
        setVideoPath(videoPath, isHasAudio);
    }
    /***********************以上是对外提供的方法***************************/

    /*********************以下是自己加上的*****************************/
    /**
     * 是否播放声音
     */
    private boolean isHasAudio = true;

    /**
     * 静音
     */
    private void closeVolume(){
        mMediaPlayer.setVolume(0, 0);
    }

    /**
     * 打开声音(系统音量),
     */
    private void openVolume(){
//        AudioManager audioManager=(AudioManager)getContext().getApplicationContext().getSystemService(Service.AUDIO_SERVICE);
//        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
//        mMediaPlayer.setVolume(audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM), audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
//        mMediaPlayer.start();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    /**
     * 设置是否有声音
     */
    public void setAudioStreamType() {
        if (isHasAudio) {
            openVolume();
        } else {
            closeVolume();
        }
    }

    /****************************以下方法可以不用理会**********************************/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&//返回键
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&//音量加
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&//音量减
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&//静音键
                keyCode != KeyEvent.KEYCODE_MENU &&//菜单键
                keyCode != KeyEvent.KEYCODE_CALL &&//拨号键
                keyCode != KeyEvent.KEYCODE_ENDCALL;//挂断键
        if (isInPlaybackState() && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||//按键Headset Hook
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {//多媒体键 播放/暂停
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP//多媒体键 停止
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {//多媒体键 暂停
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                }
                return true;
            } else {
                toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    /*********************以下是暂不知作用的方法*****************************/
    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(FixedTextureVideoView.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(FixedTextureVideoView.class.getName());
    }

    /***********************以下是暂没使用的方法***************************/
    /**
     * 获取AudioSessionId
     * @return
     */
    public int getAudioSessionId() {
        if (mAudioSession == 0) {
            MediaPlayer foo = new MediaPlayer();
            mAudioSession = foo.getAudioSessionId();
            foo.release();
        }
        return mAudioSession;
    }

    public void suspend() {
        release(false);
    }

    public void resume() {
        openVideo();
    }

    public void setVideoURI(Uri uri, boolean isHasAudio) {
        this.isHasAudio = isHasAudio;
        setVideoURI(uri, null);
    }

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        return getDefaultSize(desiredSize, measureSpec);
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

}

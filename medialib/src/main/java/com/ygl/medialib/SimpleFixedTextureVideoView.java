package com.ygl.medialib;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.danikula.videocache.HttpProxyCacheServer;
import com.ygl.utilslib.TextUtils;

import java.util.HashMap;

/**
 * author：ygl_panpan on 2017/1/4 15:13
 * email：pan.lq@i70tv.com
 */
public class SimpleFixedTextureVideoView extends RelativeLayout {
    private static final int SUCCESS_BITMAP = 111;

    private Context context;
    private FixedTextureVideoView videoView;

    private float displayWidth;
    private float displayHeight;
    private String path;
    private String thumbnailUrl;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SUCCESS_BITMAP) {
                Bitmap bit = (Bitmap) msg.obj;
                videoView.setFixedSize((int)displayWidth, (int)displayHeight, path, bit, true);
            }
        }
    };
    private View root;

    public SimpleFixedTextureVideoView(Context context) {
        super(context);
        initLayout(context);
    }

    public SimpleFixedTextureVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout(context);
    }

    public SimpleFixedTextureVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout(context);
    }

    private void initLayout(Context context) {
        this.context = context;
        root = LayoutInflater.from(context).inflate(R.layout.layout_simple_fixed_texture_video, null);
        addView(root);
        proxy = getProxy();

        videoView = (FixedTextureVideoView) root.findViewById(R.id.videoView);
    }

    private void getThumbnail() {
        new Thread(){

            @Override
            public void run() {
                Bitmap bitmap = null;
                if (!path.contains("http")) {
                    MediaMetadataRetriever metadataRetriever = null;
                    try {
                        metadataRetriever = new MediaMetadataRetriever();
                        metadataRetriever.setDataSource(path);
                        bitmap = metadataRetriever.getFrameAtTime();
//                        bitmap = metadataRetriever.getFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            metadataRetriever.release();
                        } catch (RuntimeException ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    bitmap = createVideoThumbnail(path, (int) displayWidth, 0);
                }

                if (bitmap != null){
                    Message msg = handler.obtainMessage(SUCCESS_BITMAP, bitmap);
                    handler.sendMessage(msg);
                }
            }
        }.start();
    }

    private Bitmap createVideoThumbnail(String url, int width, int height) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        int kind = MediaStore.Video.Thumbnails.MINI_KIND;
        try {
            if (Build.VERSION.SDK_INT >= 14) {
                retriever.setDataSource(url, new HashMap<String, String>());
            } else {
                retriever.setDataSource(url);
            }
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }
        if (kind == MediaStore.Images.Thumbnails.MICRO_KIND && bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }

    public void setVideoData(float width, String videoUrl, String thumbnailUrl){
        if (width > 0){
            displayWidth = width;
        }
        path = videoUrl;
        this.thumbnailUrl = thumbnailUrl;

        displayHeight = displayWidth * SimpleMovieRecorderView.getScale();

        RelativeLayout.LayoutParams rp = (RelativeLayout.LayoutParams) root.getLayoutParams();
        rp.width = (int) displayWidth;
        rp.height = (int) displayHeight;
        root.setLayoutParams(rp);

        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) videoView.getLayoutParams();
        p.width = (int) displayWidth;
        p.height = (int) displayHeight;
        videoView.setLayoutParams(p);
        if (!TextUtils.isEmpty(path)) {
            if (path.contains("http")) {
                path = proxy.getProxyUrl(path);
            }

            if (TextUtils.isEmpty(thumbnailUrl)){
                getThumbnail();
            } else {
                videoView.setFixedSize((int)displayWidth, (int)displayHeight, path, thumbnailUrl, true);
            }
        }
    }

    private HttpProxyCacheServer proxy;

    public HttpProxyCacheServer getProxy() {
        return proxy == null ? (proxy = newProxy()) : proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer(context);
    }

}

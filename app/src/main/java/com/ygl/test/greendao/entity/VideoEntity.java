package com.ygl.test.greendao.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * author：ygl_panpan on 2017/1/5 17:22
 * email：pan.lq@i70tv.com
 */
@Entity
public class VideoEntity implements Parcelable{
    @Id(autoincrement = true)
    private Long id;
    private String videoUrl;
    private int videoWidth;
    private int videoHeigth;
    private String videoDes;
    private String thumbnailUrl;

    protected VideoEntity(Parcel in) {
        videoUrl = in.readString();
        videoWidth = in.readInt();
        videoHeigth = in.readInt();
        videoDes = in.readString();
        thumbnailUrl = in.readString();
    }

    @Generated(hash = 1793703002)
    public VideoEntity(Long id, String videoUrl, int videoWidth, int videoHeigth,
            String videoDes, String thumbnailUrl) {
        this.id = id;
        this.videoUrl = videoUrl;
        this.videoWidth = videoWidth;
        this.videoHeigth = videoHeigth;
        this.videoDes = videoDes;
        this.thumbnailUrl = thumbnailUrl;
    }

    public VideoEntity(String videoUrl, int videoWidth,
                       String videoDes, String thumbnailUrl) {
        this.videoUrl = videoUrl;
        this.videoWidth = videoWidth;
        this.videoDes = videoDes;
        this.thumbnailUrl = thumbnailUrl;
    }

    @Generated(hash = 1984976152)
    public VideoEntity() {
    }

    public static final Creator<VideoEntity> CREATOR = new Creator<VideoEntity>() {
        @Override
        public VideoEntity createFromParcel(Parcel in) {
            return new VideoEntity(in);
        }

        @Override
        public VideoEntity[] newArray(int size) {
            return new VideoEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(videoUrl);
        dest.writeInt(videoWidth);
        dest.writeInt(videoHeigth);
        dest.writeString(videoDes);
        dest.writeString(thumbnailUrl);
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVideoUrl() {
        return this.videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public int getVideoWidth() {
        return this.videoWidth;
    }

    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public int getVideoHeigth() {
        return this.videoHeigth;
    }

    public void setVideoHeigth(int videoHeigth) {
        this.videoHeigth = videoHeigth;
    }

    public String getVideoDes() {
        return this.videoDes;
    }

    public void setVideoDes(String videoDes) {
        this.videoDes = videoDes;
    }

    public String getThumbnailUrl() {
        return this.thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}

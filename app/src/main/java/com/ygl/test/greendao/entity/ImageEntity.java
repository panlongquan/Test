package com.ygl.test.greendao.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * author：ygl_panpan on 2016/12/22 16:10
 * email：pan.lq@i70tv.com
 */
@Entity
public class ImageEntity implements Parcelable{

    @Id(autoincrement = true)
    private Long id;
    private String image_url;
    private String description;

    /*******************************************************************/
    protected ImageEntity(Parcel in) {
        image_url = in.readString();
        description = in.readString();
    }

    @Generated(hash = 99856511)
    public ImageEntity(Long id, String image_url, String description) {
        this.id = id;
        this.image_url = image_url;
        this.description = description;
    }

    @Generated(hash = 2080458212)
    public ImageEntity() {
    }

    public static final Creator<ImageEntity> CREATOR = new Creator<ImageEntity>() {
        @Override
        public ImageEntity createFromParcel(Parcel in) {
            return new ImageEntity(in);
        }

        @Override
        public ImageEntity[] newArray(int size) {
            return new ImageEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(image_url);
        dest.writeString(description);
    }
    /*******************************************************************/

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImage_url() {
        return this.image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

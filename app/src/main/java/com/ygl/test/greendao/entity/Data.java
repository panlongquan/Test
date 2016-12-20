package com.ygl.test.greendao.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * author：ygl_panpan on 2016/12/20 11:23
 * email：pan.lq@i70tv.com
 */
@Entity
public class Data {

    @Id(autoincrement = true)
    private Long id;
    private String code;
    private String msg;
    @Generated(hash = 1147319632)
    public Data(Long id, String code, String msg) {
        this.id = id;
        this.code = code;
        this.msg = msg;
    }
    @Generated(hash = 2135787902)
    public Data() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getCode() {
        return this.code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getMsg() {
        return this.msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }


}

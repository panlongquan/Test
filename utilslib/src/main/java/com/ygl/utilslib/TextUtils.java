package com.ygl.utilslib;

/**
 * author：ygl_panpan on 2016/12/20 11:34
 * email：pan.lq@i70tv.com
 */
public class TextUtils {

    public static boolean isEmpty(String s){
        return android.text.TextUtils.isEmpty(s) || "null".equalsIgnoreCase(s);
    }

}

package com.ygl.utilslib;

/**
 * author：ygl_panpan on 2016/12/20 11:34
 * email：pan.lq@i70tv.com
 */
public class TextUtils {

    public static final String EMPTY = "";

    public static boolean isEmpty(String s){
        return android.text.TextUtils.isEmpty(s) || "null".equalsIgnoreCase(s);
    }

    /**
     * Helper function for making null strings safe for comparisons, etc.
     *
     * @return (s == null) ? "" : s;
     */
    public static String makeSafe(String s) {
        return (s == null) ? "" : s;
    }

    /**
     *
     * @param str
     * @return
     */
    public static String trim(String str) {
        return str == null ? EMPTY : str.trim();
    }

}

package com.ygl.utilslib;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * author：ygl_panpan on 2016/12/20 11:32
 * email：pan.lq@i70tv.com
 */
public class Utils {

    /**
     * @param context
     * @return
     * @description 获取IMEI，唯一标示
     * @author gq
     */
    public static String getDeviceID(Context context) {
        String mIdentity = null;

        if (TextUtils.isEmpty(mIdentity)) {
            final TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            // 获取 设备 ID
            try {
                mIdentity = tm.getDeviceId();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 如果 依然获取不到 的话 就用 设备 唯一ID
            if (TextUtils.isEmpty(mIdentity)) {
                // 读取 设备 唯一 id
                final String androidId = android.provider.Settings.Secure
                        .getString(context.getContentResolver(),
                                android.provider.Settings.Secure.ANDROID_ID);
                try {
                    // android2.2或者是某些山寨手机使用这个也是有问题的，它会返回一个固定的值 9774d56d682e549c
                    if (!"9774d56d682e549c".equals(androidId)) {
                        mIdentity = UUID.nameUUIDFromBytes(
                                androidId.getBytes("UTF-8")).toString();
                    } else {
                        // 防止有一些手机不存在手机唯一id 生成随机 id
                        mIdentity = UUID.randomUUID().toString();
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return mIdentity;
    }

}

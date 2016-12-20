package com.ygl.test;

import android.app.Application;

import com.ygl.test.greendao.dao.DaoMaster;
import com.ygl.test.greendao.dao.DaoSession;
import com.ygl.test.greendao.helper.MySQLiteOpenHelper;
import com.ygl.utilslib.Utils;

/**
 * author：ygl_panpan on 2016/12/20 11:20
 * email：pan.lq@i70tv.com
 */
public class TestApp extends Application {

    private static TestApp instance;

    private DaoSession session;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initGreenDao();
    }

    private void initGreenDao() {
        DaoMaster.OpenHelper helper = new MySQLiteOpenHelper(this, "db_test", null);
        session = new DaoMaster(helper.getEncryptedWritableDb(Utils.getDeviceID(this))).newSession();
    }

    public static TestApp getInstance() {
        return instance;
    }

    public DaoSession getSession() {
        return session;
    }
}

package com.ygl.test.greendao.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.yuweiguocn.library.greendao.MigrationHelper;
import com.ygl.test.greendao.dao.DaoMaster;
import com.ygl.test.greendao.dao.DataDao;

/**
 * author：ygl_panpan on 2016/12/20 11:26
 * email：pan.lq@i70tv.com
 */
public class MySQLiteOpenHelper extends DaoMaster.OpenHelper {

    public MySQLiteOpenHelper(Context context, String name) {
        super(context, name);
    }

    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MigrationHelper.migrate(db, DataDao.class);
    }
}

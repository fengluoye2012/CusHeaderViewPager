package com.fly.androidvideocache.sourcestorage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fly.androidvideocache.source.SourceInfo;

/**
 * 本地持久化
 */
public class DatabaseSourceInfoStorage extends SQLiteOpenHelper implements SourceInfoStorage {

    DatabaseSourceInfoStorage(Context context) {
        super(context, "AndroidVideoCache.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public SourceInfo get(String url) {
        return null;
    }

    @Override
    public void put(String url, SourceInfo sourceInfo) {

    }

    @Override
    public void release() {
        close();
    }
}

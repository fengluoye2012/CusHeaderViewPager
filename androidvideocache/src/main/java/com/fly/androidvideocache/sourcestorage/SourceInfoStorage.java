package com.fly.androidvideocache.sourcestorage;

import com.fly.androidvideocache.source.SourceInfo;

/**
 * 接口类，对外暴露数据库操作方法
 */
public interface SourceInfoStorage {
    /**
     * 通过url 查询
     *
     * @param url
     * @return
     */
    SourceInfo get(String url);

    /**
     * 插入
     *
     * @param url
     * @param sourceInfo
     */
    void put(String url, SourceInfo sourceInfo);

    /**
     * 释放数据库
     */
    void release();
}

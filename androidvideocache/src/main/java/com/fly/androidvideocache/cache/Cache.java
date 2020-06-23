package com.fly.androidvideocache.cache;

import com.fly.androidvideocache.utils.ProxyCacheException;

public interface Cache {
    /**
     * 已经缓存的文件长度
     *
     * @return
     */
    long available() throws ProxyCacheException;

    int read(byte[] buffer, long offset, int length) throws ProxyCacheException;

    void append(byte[] data, int length) throws ProxyCacheException;

    void close() throws ProxyCacheException;

    void complete() throws ProxyCacheException;

    boolean isComplete();
}

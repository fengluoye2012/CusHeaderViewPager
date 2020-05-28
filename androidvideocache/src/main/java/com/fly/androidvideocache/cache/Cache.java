package com.fly.androidvideocache.cache;

public interface Cache {
    /**
     * 已经缓存的文件长度
     *
     * @return
     */
    long available();

    int read(byte[] buffer, long offset, int length);

    void append(byte[] data, int length);

    void close();

    boolean complete();

    boolean isComplete();
}

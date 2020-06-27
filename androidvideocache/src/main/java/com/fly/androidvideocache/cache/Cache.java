package com.fly.androidvideocache.cache;

import com.fly.androidvideocache.utils.ProxyCacheException;

public interface Cache {
    /**
     * 已经缓存的文件长度
     *
     * @return
     */
    long available() throws ProxyCacheException;

    /**
     * 从本地文件偏移offset,读取length的字节 写入buffer
     *
     * @param buffer
     * @param offset
     * @param length
     * @return 返回写入buffer中的字节长度
     * @throws ProxyCacheException
     */
    int read(byte[] buffer, long offset, int length) throws ProxyCacheException;

    /**
     * 往本地文件写入长度为length的字节数组
     *
     * @param data
     * @param length
     * @throws ProxyCacheException
     */
    void append(byte[] data, int length) throws ProxyCacheException;

    void close() throws ProxyCacheException;

    void complete() throws ProxyCacheException;

    boolean isComplete();
}

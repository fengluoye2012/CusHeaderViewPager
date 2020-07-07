package com.fly.androidvideocache.file;

import androidx.core.util.Preconditions;

import com.fly.androidvideocache.cache.Cache;
import com.fly.androidvideocache.utils.ProxyCacheException;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

/**
 * 将网络下载的流保存在内存中，无法持久化
 */
public class ByteArrayCache implements Cache {

    private volatile byte[] data;
    private volatile boolean completed;

    public ByteArrayCache() {
        this(new byte[0]);
    }

    public ByteArrayCache(byte[] data) {
        this.data = Preconditions.checkNotNull(data);
    }

    @Override
    public int read(byte[] buffer, long offset, int length) throws ProxyCacheException {
        if (offset >= data.length) {
            return -1;
        }
        return new ByteArrayInputStream(data).read(buffer, (int) offset, length);
    }

    @Override
    public long available() throws ProxyCacheException {
        return data.length;
    }

    @Override
    public void append(byte[] newData, int length) throws ProxyCacheException {
        Preconditions.checkNotNull(data);
        Preconditions.checkArgument(length >= 0 && length <= newData.length);

        byte[] appendedData = Arrays.copyOf(data, data.length + length);
        System.arraycopy(newData, 0, appendedData, data.length, length);
        data = appendedData;
    }

    @Override
    public void close() throws ProxyCacheException {
    }

    @Override
    public void complete() {
        completed = true;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }
}
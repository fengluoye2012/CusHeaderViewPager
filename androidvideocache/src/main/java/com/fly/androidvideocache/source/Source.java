package com.fly.androidvideocache.source;

import com.fly.androidvideocache.utils.ProxyCacheException;

public interface Source {

    void open(long offset) throws ProxyCacheException;

    int read(byte[] buffer) throws ProxyCacheException;

    long length() throws ProxyCacheException;

    void close();
}

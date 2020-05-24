package com.fly.androidvideocache.source;

public interface Source {
    long length();

    void open(long offset);

    void read(byte[] buffer);

    void close();
}

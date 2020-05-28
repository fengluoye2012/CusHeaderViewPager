package com.fly.androidvideocache.cache;

import java.io.File;

public class FileCache implements Cache {
    private File file;

    public FileCache(File file) {
        //this(file,new )
    }

    @Override
    public long available() {
        return 0;
    }

    @Override
    public int read(byte[] buffer, long offset, int length) {
        return 0;
    }

    @Override
    public void append(byte[] data, int length) {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean complete() {
        return false;
    }

    @Override
    public boolean isComplete() {
        return false;
    }
}

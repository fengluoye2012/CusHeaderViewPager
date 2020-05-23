package com.fly.androidvideocache.cache;

public interface Cache {
    public boolean complete();

    public int getAvailableLength();

    public void cache();
}

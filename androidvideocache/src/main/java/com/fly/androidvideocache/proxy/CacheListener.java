package com.fly.androidvideocache.proxy;

import java.io.File;

public interface CacheListener {
    void onCacheAvailable(File file, String url, int percentsAvailable);
}

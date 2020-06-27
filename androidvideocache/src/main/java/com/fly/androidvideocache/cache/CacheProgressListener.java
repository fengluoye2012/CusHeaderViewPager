package com.fly.androidvideocache.cache;

import java.io.File;

/**
 * 缓存进度监听
 */
public interface CacheProgressListener {

    /**
     * @param cacheFile         缓存的本地文件
     * @param url               当前缓存的Url
     * @param percentsAvailable 可获取的缓存进度
     */
    void onCacheAvailable(File cacheFile, String url, int percentsAvailable);
}

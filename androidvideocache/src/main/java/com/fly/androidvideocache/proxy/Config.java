package com.fly.androidvideocache.proxy;

import com.fly.androidvideocache.file.DiskUsage;
import com.fly.androidvideocache.file.FileNameGenerator;
import com.fly.androidvideocache.headerInjector.HeaderInjector;
import com.fly.androidvideocache.sourcestorage.SourceInfoStorage;

import java.io.File;

public class Config {
    private File cacheRoot;
    private FileNameGenerator fileNameGenerator;
    private DiskUsage diskUsage;
    private SourceInfoStorage sourceInfoStorage;
    private HeaderInjector headerInjector;

    public Config(File cacheRoot, FileNameGenerator fileNameGenerator, DiskUsage diskUsage, SourceInfoStorage sourceInfoStorage, HeaderInjector headerInjector) {
        this.cacheRoot = cacheRoot;
        this.fileNameGenerator = fileNameGenerator;
        this.diskUsage = diskUsage;
        this.sourceInfoStorage = sourceInfoStorage;
        this.headerInjector = headerInjector;
    }

    //根据url生成文件名
    File generateCacheFile(String url) {
        String name = fileNameGenerator.generate(url);
        return new File(cacheRoot, name);
    }
}

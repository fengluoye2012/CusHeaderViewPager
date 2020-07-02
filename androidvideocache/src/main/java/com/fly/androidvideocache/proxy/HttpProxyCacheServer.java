package com.fly.androidvideocache.proxy;

import android.content.Context;

import com.fly.androidvideocache.file.DiskUsage;
import com.fly.androidvideocache.file.FileNameGenerator;
import com.fly.androidvideocache.file.Md5FileNameGenerator;
import com.fly.androidvideocache.file.TotalSizeLruDiskUsage;
import com.fly.androidvideocache.headerInjector.EmptyHeaderInjector;
import com.fly.androidvideocache.headerInjector.HeaderInjector;
import com.fly.androidvideocache.sourcestorage.SourceInfoStorage;
import com.fly.androidvideocache.sourcestorage.SourceInfoStorageFactory;
import com.fly.androidvideocache.utils.StorageUtils;

import java.io.File;

//客户端代理服务器
public class HttpProxyCacheServer {


    public static final class Builder {
        private static final long DEFAULE_MAX_SIZE = 512 * 1024 * 1024;

        private File cacheRoot;
        private FileNameGenerator fileNameGenerator;
        private DiskUsage diskUsage;
        private SourceInfoStorage sourceInfoStorage;
        private HeaderInjector headerInjector;

        public Builder(Context context) {
            this.sourceInfoStorage = SourceInfoStorageFactory.newSourceInfoStorage(context);
            this.cacheRoot = StorageUtils.getIndividualCacheDirectory(context);
            this.diskUsage = new TotalSizeLruDiskUsage(DEFAULE_MAX_SIZE);
            this.fileNameGenerator = new Md5FileNameGenerator();
            this.headerInjector = new EmptyHeaderInjector();
        }

        public Builder setCacheRoot(File cacheRoot) {
            this.cacheRoot = cacheRoot;
            return this;
        }

        public Builder setFileNameGenerator(FileNameGenerator fileNameGenerator) {
            this.fileNameGenerator = fileNameGenerator;
            return this;
        }

        public Builder setDiskUsage(DiskUsage diskUsage) {
            this.diskUsage = diskUsage;
            return this;
        }

        public Builder setSourceInfoStorage(SourceInfoStorage sourceInfoStorage) {
            this.sourceInfoStorage = sourceInfoStorage;
            return this;
        }

        public Builder setHeaderInjector(HeaderInjector headerInjector) {
            this.headerInjector = headerInjector;
            return this;
        }
    }
}

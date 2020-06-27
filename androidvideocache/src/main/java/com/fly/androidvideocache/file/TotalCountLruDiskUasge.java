package com.fly.androidvideocache.file;

import java.io.File;

public class TotalCountLruDiskUasge extends LruDiskUsage {

    private int maxSize;

    public TotalCountLruDiskUasge(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalStateException("Max size must be positive number!");
        }
        this.maxSize = maxSize;
    }

    @Override
    protected boolean accept(File file, long totalSize, int totalCount) {
        return totalCount <= maxSize;
    }
}

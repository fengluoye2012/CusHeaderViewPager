package com.fly.androidvideocache.file;

import java.io.File;

public class TotalSizeLruDiskUsage extends LruDiskUsage {
    private long maxSize;

    public TotalSizeLruDiskUsage(long maxSize) {
        if (maxSize <= 0) {
            throw new IllegalStateException("Max size must be positive number!");
        }
        this.maxSize = maxSize;
    }

    /**
     * @param file       当前文件
     * @param totalSize  总文件字节大小
     * @param totalCount 总文件数量
     * @return
     */
    @Override
    protected boolean accept(File file, long totalSize, int totalCount) {
        return totalSize < maxSize;
    }
}

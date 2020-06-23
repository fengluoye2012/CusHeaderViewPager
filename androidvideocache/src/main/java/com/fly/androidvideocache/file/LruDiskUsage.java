package com.fly.androidvideocache.file;

import com.fly.androidvideocache.utils.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 将文件目录下的所有文件按最后修改时间排序，根据时间倒序删除
 */
public abstract class LruDiskUsage implements DiskUsage {

    private final ExecutorService workerThread = Executors.newSingleThreadExecutor();

    @Override
    public void touch(File file) throws IOException {
        workerThread.submit(new TouchCallable(file));
    }

    //1、修改当前文件的最后修改时间
    //2、根据最后访问时间进行排序，获取父目录下的所有文件
    //3、根据最后访问时间倒序删除
    private void touchInBackground(File file) throws IOException {
        Files.setLastModifiedNow(file);
        List<File> files = Files.getLruListFiles(file);
        trim(files);
    }

    protected abstract boolean accept(File file, long totalSize, int totalCount);

    private void trim(List<File> files) {
        long totalSize = countTotalSize(files);
        int totalCount = files.size();
        for (File file : files) {
            boolean accepted = accept(file, totalSize, totalCount);
            if (!accepted) {
                long fileSize = file.length();
                boolean deleted = file.delete();
                if (deleted) {
                    totalCount--;
                    totalSize -= fileSize;
                    LogUtil.i("Cache file " + file + " is deleted because it exceeds cache limit");
                } else {
                    LogUtil.e("Error deleting file " + file + " for trimming cache");
                }
            }
        }
    }

    //计算所有文件的总大小
    private long countTotalSize(List<File> files) {
        long totalSize = 0;
        for (File file : files) {
            totalSize += file.length();
        }
        return totalSize;
    }

    private class TouchCallable implements Callable<Void> {
        private final File file;

        public TouchCallable(File file) {
            this.file = file;
        }

        @Override
        public Void call() throws Exception {
            touchInBackground(file);
            return null;
        }
    }
}

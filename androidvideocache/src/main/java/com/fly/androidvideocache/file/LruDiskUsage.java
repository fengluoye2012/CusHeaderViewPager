package com.fly.androidvideocache.file;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LruDiskUsage implements DiskUsage {

    private final ExecutorService workerThread = Executors.newSingleThreadExecutor();

    @Override
    public void touch(File file) {
        workerThread.submit(new TouchCallable(file));
    }

    private void touchInBackground(File file){

    }

    private class TouchCallable implements Callable<Void> {
        private final File file;

        public TouchCallable(File file) {
            this.file = file;
        }

        @Override
        public Void call() throws Exception {

            return null;
        }
    }
}

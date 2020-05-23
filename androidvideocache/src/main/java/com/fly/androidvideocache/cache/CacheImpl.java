package com.fly.androidvideocache.cache;

import android.os.Environment;

import com.fly.androidvideocache.down.DownLoad;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;

class CacheImpl implements Cache {

    private static CacheImpl instance;

    private CacheImpl() {
    }

    public static CacheImpl getInstance() {
        if (instance == null) {
            synchronized (CacheImpl.class) {
                if (instance == null) {
                    instance = new CacheImpl();
                }
            }
        }
        return instance;
    }


    @Override
    public boolean complete() {
        return false;
    }

    @Override
    public int getAvailableLength() {
        return 0;
    }


    @Override
    public void cache() {
        try {
            File file = new File(Environment.getDownloadCacheDirectory(), "aa.apk");
            if (!file.exists()) {
                file.createNewFile();
            }

            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            int start = 0;
            int maxLength = 500 * 1024;
            int contentLength = 30 * 1024 * 1024;

            randomAccessFile.seek(start);
            DownLoad downLoad = new DownLoad();
            HttpURLConnection connection = downLoad.openConnect("", start, maxLength);
            InputStream inputStream = connection.getInputStream();

            byte[] buffers = new byte[1024];
            int len;
            while ((len = inputStream.read(buffers)) != -1) {
                randomAccessFile.write(buffers, 0, len);
            }

            if (randomAccessFile.length() == contentLength) {
                tryComplete();
            }

            randomAccessFile.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void tryComplete() {
        
    }
}

package com.fly.androidvideocache.source;

import com.fly.androidvideocache.headerInjector.HeaderInjector;
import com.fly.androidvideocache.sourcestorage.SourceInfoStorage;
import com.fly.androidvideocache.sourcestorage.SourceInfoStorageFactory;
import com.fly.androidvideocache.utils.ProxyCacheUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static androidx.core.util.Preconditions.checkNotNull;

public class HttpUrlSource implements Source {
    private SourceInfo sourceInfo;
    private SourceInfoStorage sourceInfoStorage;
    private HeaderInjector headerInjector;


    public HttpUrlSource(String url) {
        this(url, SourceInfoStorageFactory.newEmptySourceInfoStorage());
    }

    public HttpUrlSource(String url, SourceInfoStorage sourceInfoStorage) {
        checkNotNull(sourceInfoStorage);
        SourceInfo sourceInfo = sourceInfoStorage.get(url);
        this.sourceInfo = sourceInfo != null ? sourceInfo : new SourceInfo(url, Integer.MIN_VALUE, ProxyCacheUtil.getSupportiveMime());
    }

    @Override
    public long length() {
        return 0;
    }

    @Override
    public void open(long offset) {
        openConnect()
    }

    public HttpURLConnection openConnect(String urlStr, int start, int maxLength) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int end = start + maxLength;
        connection.setRequestProperty("Range", "bytes=" + start + "-" + end);

        int responseCode = connection.getResponseCode();
//        //处理重定向
//        while (responseCode > 300 && responseCode < 400) {
//
//        }
        return connection;
    }

    @Override
    public void read(byte[] buffer) {

    }

    @Override
    public void close() {

    }
}

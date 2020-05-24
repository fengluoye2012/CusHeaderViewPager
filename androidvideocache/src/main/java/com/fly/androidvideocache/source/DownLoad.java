package com.fly.androidvideocache.source;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownLoad {

    /**
     * 获取 URLConnection
     * Range 的使用，断点下载
     *
     * @param urlStr
     * @param start
     * @param maxLength
     * @return
     * @throws IOException
     */
    public HttpURLConnection openConnect(String urlStr, int start, int maxLength) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int end = start + maxLength;
        connection.setRequestProperty("Range", "bytes=" + start + "-" + end);

        int responseCode = connection.getResponseCode();
        //处理重定向
//        while (responseCode > 300 && responseCode < 400) {
//
//        }
        return connection;
    }
}

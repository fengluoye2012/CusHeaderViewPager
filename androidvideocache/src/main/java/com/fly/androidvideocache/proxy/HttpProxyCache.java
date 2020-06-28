package com.fly.androidvideocache.proxy;

import com.fly.androidvideocache.file.FileCache;
import com.fly.androidvideocache.source.HttpUrlSource;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class HttpProxyCache extends ProxyCache {

    private final HttpUrlSource source;
    private final FileCache cache;

    public HttpProxyCache(HttpUrlSource source, FileCache cache) {
        super(source, cache);
        this.source = source;
        this.cache = cache;
    }

    public void processRequest(GetRequest request, Socket socket) throws IOException {
        OutputStream out = new BufferedOutputStream(socket.getOutputStream());
        String responseHeaders = newResponseHeaders(request);
        out.write(responseHeaders.getBytes("UTF-8"));

        long offset = request.rangeOffset;
        if (isUseCache(request)) {
            responseWithCache(out, offset);
        } else {
            responseWithoutCache(out, offset);
        }
    }

    private String newResponseHeaders(GetRequest request) {
        return null;
    }

    private boolean isUseCache(GetRequest request) {
        return false;
    }

    private void responseWithCache(OutputStream out, long offset) {

    }

    private void responseWithoutCache(OutputStream out, long offset) {

    }
}

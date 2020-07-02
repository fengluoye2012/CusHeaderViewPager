package com.fly.androidvideocache.proxy;

import android.text.TextUtils;

import com.fly.androidvideocache.file.FileCache;
import com.fly.androidvideocache.source.HttpUrlSource;
import com.fly.androidvideocache.utils.ConstantUtil;
import com.fly.androidvideocache.utils.ProxyCacheException;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Locale;

public class HttpProxyCache extends ProxyCache {

    private final HttpUrlSource source;
    private final FileCache cache;
    //不缓存到本地边界
    private static final float NO_CACHE_BARRIER = .2f;

    public HttpProxyCache(HttpUrlSource source, FileCache cache) {
        super(source, cache);
        this.source = source;
        this.cache = cache;
    }

    public void processRequest(GetRequest request, Socket socket) throws IOException, ProxyCacheException {
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

    private String newResponseHeaders(GetRequest request) throws ProxyCacheException {
        String mime = source.getType();
        boolean mimeKnown = !TextUtils.isEmpty(mime);
        long length = cache.isComplete() ? cache.available() : source.length();
        boolean lengthKnown = length >= 0;
        long contentLength = request.partial ? length - request.rangeOffset : length;
        boolean addRange = lengthKnown && request.partial;
        return new StringBuilder()
                .append(request.partial ? "HTTP/1.1 206 PARTIAL CONTENT\n" : "HTTP/1.1 200 OK\n")
                .append("Accept-Ranges: bytes\n")
                .append(lengthKnown ? format("Content-Length: %d\n", contentLength) : "")
                .append(addRange ? format("Content-Range: bytes %d-%d/%d\n", request.rangeOffset, length - 1, length) : "")
                .append(mimeKnown ? format("Content-Type: %s\n", mime) : "")
                .append("\n") // headers end
                .toString();
    }

    private boolean isUseCache(GetRequest request) throws ProxyCacheException {
        long sourceLength = source.length();
        boolean sourceLengthKnown = sourceLength > 0;
        long cacheAvailable = cache.available();
        return !sourceLengthKnown || !request.partial || request.rangeOffset <= cacheAvailable + sourceLength * NO_CACHE_BARRIER;
    }

    //使用本地文件中的数据相应response;
    private void responseWithCache(OutputStream out, long offset) throws ProxyCacheException, IOException {
        byte[] buffer = new byte[ConstantUtil.DEFAULT_BUFFER_SIZE];
        int readBytes;
        while ((readBytes = read(buffer, offset, buffer.length)) != -1) {
            out.write(buffer, 0, readBytes);
            offset += readBytes;
        }
        out.flush();
    }

    //直接使用网络数据相应Response,每次只读取500K,需要循环读取
    private void responseWithoutCache(OutputStream out, long offset) throws ProxyCacheException, IOException {
        HttpUrlSource newSourceNoCache = new HttpUrlSource(this.source);
        try {
            while (offset < source.length()) {
                newSourceNoCache.open(offset);
                byte[] buffer = new byte[ConstantUtil.DEFAULT_BUFFER_SIZE];
                int readBytes;
                while ((readBytes = read(buffer, offset, buffer.length)) != -1) {
                    out.write(buffer, 0, readBytes);
                    offset += readBytes;
                }
                out.flush();
            }
        } finally {
            newSourceNoCache.close();
        }
    }

    private String format(String pattern, Object... args) {
        return String.format(Locale.US, pattern, args);
    }
}

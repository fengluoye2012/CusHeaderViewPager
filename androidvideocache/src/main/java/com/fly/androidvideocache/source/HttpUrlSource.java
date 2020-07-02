package com.fly.androidvideocache.source;

import android.text.TextUtils;
import android.util.Log;

import com.fly.androidvideocache.headerInjector.EmptyHeaderInjector;
import com.fly.androidvideocache.headerInjector.HeaderInjector;
import com.fly.androidvideocache.proxy.ProxyCacheUtils;
import com.fly.androidvideocache.sourcestorage.SourceInfoStorage;
import com.fly.androidvideocache.sourcestorage.SourceInfoStorageFactory;
import com.fly.androidvideocache.utils.ConstantUtil;
import com.fly.androidvideocache.utils.LogUtil;
import com.fly.androidvideocache.utils.ProxyCacheException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import static androidx.core.util.Preconditions.checkNotNull;
import static com.fly.androidvideocache.utils.ConstantUtil.DEFAULT_BUFFER_SIZE;
import static com.fly.androidvideocache.utils.ConstantUtil.HEAD_REQUEST_OFFSET;

public class HttpUrlSource implements Source {

    private SourceInfo sourceInfo;
    private SourceInfoStorage sourceInfoStorage;
    private final int MAX_REDIRECTS = 3;//最大重定向次数
    private BufferedInputStream inputStream;
    private HttpURLConnection connection;
    private HeaderInjector headerInjector;

    public HttpUrlSource(String url) {
        this(url, SourceInfoStorageFactory.newEmptySourceInfoStorage());
    }

    public HttpUrlSource(String url, SourceInfoStorage sourceInfoStorage) {
        this(url, sourceInfoStorage, new EmptyHeaderInjector());
    }

    public HttpUrlSource(String url, SourceInfoStorage sourceInfoStorage, HeaderInjector headerInjector) {
        this.sourceInfoStorage = checkNotNull(sourceInfoStorage);
        this.headerInjector = checkNotNull(headerInjector);
        SourceInfo sourceInfo = sourceInfoStorage.get(url);
        this.sourceInfo = sourceInfo != null ? sourceInfo :
                new SourceInfo(url, Integer.MIN_VALUE, ProxyCacheUtils.getSupposablyMime(url));
    }

    public HttpUrlSource(HttpUrlSource source) {
        this.sourceInfo = source.sourceInfo;
        this.sourceInfoStorage = source.sourceInfoStorage;
        this.headerInjector = source.headerInjector;
    }

    private HttpURLConnection openConnection(long offset, int timeout) throws IOException, ProxyCacheException {
        boolean redirected;
        int redirectCount = 0;
        String url = this.sourceInfo.getUrl();

        do {
            connection = (HttpURLConnection) new URL(url).openConnection();
            injectCusHeaders(connection, url);
            long end = Math.min(offset + ConstantUtil.MAX_LENGTH_ONCE, sourceInfo.getLength());
            connection.setRequestProperty("Range", "bytes=" + offset + "-" + end);
            connection.setRequestMethod(offset == HEAD_REQUEST_OFFSET ? "GET" : "HEAD");

            //系统默认超时时间为8*1000ms；
            if (timeout > 0) {
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);
            }

            int code = connection.getResponseCode();
            redirected = code > 300 && code < 400;
            if (redirected) {
                url = connection.getHeaderField("Location");
                redirectCount++;
                connection.disconnect();
            }
            if (redirectCount > MAX_REDIRECTS) {
                throw new ProxyCacheException("Too many redirects: " + redirectCount);
            }
        } while (redirected);
        return connection;
    }


    @Override
    public void open(long offset) throws ProxyCacheException {
        try {
            HttpURLConnection connection = openConnection(offset, 30 * 1000);
            String type = connection.getContentType();
            inputStream = new BufferedInputStream(connection.getInputStream(), DEFAULT_BUFFER_SIZE);
            this.sourceInfo = new SourceInfo(sourceInfo.getUrl(), sourceInfo.getLength(), type);
            this.sourceInfoStorage.put(sourceInfo.getUrl(), sourceInfo);
        } catch (IOException e) {
            throw new ProxyCacheException("Error opening connection for " + sourceInfo.getUrl() + " with offset " + offset, e);
        }
    }

    /**
     * 通过Head请求获取
     */
    private void fetchInfo() throws ProxyCacheException {
        HttpURLConnection connection = null;
        try {
            connection = openConnection(HEAD_REQUEST_OFFSET, 10 * 1000);
            int contentLength = connection.getContentLength();
            String type = connection.getContentType();
            sourceInfo = new SourceInfo(sourceInfo.getUrl(), contentLength, type);
            sourceInfoStorage.put(sourceInfo.getUrl(), sourceInfo);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 返回读取的长度或者-1；
     */
    @Override
    public int read(byte[] buffer) throws ProxyCacheException {
        if (inputStream == null) {
            throw new ProxyCacheException("Error reading data from " + sourceInfo.getUrl() + ": connection is absent!");
        }
        try {
            return inputStream.read(buffer, 0, buffer.length);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ProxyCacheException("Error reading data from" + sourceInfo.getUrl(), e);
        }
    }

    @Override
    public long length() throws ProxyCacheException {
        if (sourceInfo.getLength() == Integer.MIN_VALUE) {
            fetchInfo();
        }
        return sourceInfo.getLength();
    }


    @Override
    public void close() throws ProxyCacheException {
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (NullPointerException | IllegalArgumentException e) {
                String message = "Wait... but why? WTF!? " +
                        "Really shouldn't happen any more after fixing https://github.com/danikula/AndroidVideoCache/issues/43. " +
                        "If you read it on your device log, please, notify me danikula@gmail.com or create issue here " +
                        "https://github.com/danikula/AndroidVideoCache/issues.";
                throw new RuntimeException(message, e);
            } catch (ArrayIndexOutOfBoundsException e) {
                LogUtil.e("Error closing connection correctly. Should happen only on Android L. " +
                        "If anybody know how to fix it, please visit https://github.com/danikula/AndroidVideoCache/issues/88. " +
                        "Until good solution is not know, just ignore this issue :(", Log.getStackTraceString(e));
            }
        }
    }

    private void injectCusHeaders(HttpURLConnection connection, String url) {
        Map<String, String> extraHeaders = headerInjector.addHeader(url);
        Set<Map.Entry<String, String>> entrieSet = extraHeaders.entrySet();
        for (Map.Entry<String, String> header : entrieSet) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }
    }

    public synchronized String getType() throws ProxyCacheException {
        if (TextUtils.isEmpty(sourceInfo.getContentType())) {
            fetchInfo();
        }
        return sourceInfo.getContentType();
    }

    public String getUrl() {
        return sourceInfo.getUrl();
    }
}

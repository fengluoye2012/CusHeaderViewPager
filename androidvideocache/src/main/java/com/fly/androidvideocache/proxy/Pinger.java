package com.fly.androidvideocache.proxy;

import android.util.Log;

import com.fly.androidvideocache.source.HttpUrlSource;
import com.fly.androidvideocache.utils.ConstantUtil;
import com.fly.androidvideocache.utils.LogUtil;
import com.fly.androidvideocache.utils.ProxyCacheException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static androidx.core.util.Preconditions.checkArgument;
import static androidx.core.util.Preconditions.checkNotNull;

public class Pinger {
    private final String host;
    private final int port;

    private final ExecutorService pingExcutor = Executors.newSingleThreadExecutor();


    public Pinger(String host, int port) {
        this.host = checkNotNull(host);
        this.port = port;
    }

    boolean ping(int maxAttempts, int startTimeout) {
        checkArgument(maxAttempts >= 1);
        checkArgument(startTimeout > 0);

        int timeout = startTimeout;
        int attempts = 0;
        while (attempts < maxAttempts) {
            Future<Boolean> pingFuture = pingExcutor.submit(new PingCallable());
            try {
                Boolean pinged = pingFuture.get(timeout, TimeUnit.MILLISECONDS);
                if (pinged) {
                    return true;
                }
            } catch (ExecutionException | InterruptedException e) {
                LogUtil.e("Error pinging server due to unexpected error" + Log.getStackTraceString(e));
            } catch (TimeoutException e) {
                LogUtil.w("Error pinging server (attempt: " + attempts + ", timeout: " + timeout + "). ");
            }
            attempts++;
            timeout *= 2;
        }
        String error = String.format(Locale.US, "Error pinging server (attempts: %d, max timeout: %d). " +
                        "If you see this message, please, report at https://github.com/danikula/AndroidVideoCache/issues/134. " +
                        "Default proxies are: %s"
                , attempts, timeout / 2, getDefaultProxies());
        LogUtil.e(error);
        return false;
    }

    private List<Proxy> getDefaultProxies() {
        try {
            ProxySelector defaultProxySelector = ProxySelector.getDefault();
            return defaultProxySelector.select(new URI(getPingUrl()));
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private String getPingUrl() {
        return String.format(Locale.US, "http://%s:%d/%s", host, port, ConstantUtil.PING_REQUEST);
    }

    private class PingCallable implements Callable<Boolean> {
        @Override
        public Boolean call() throws Exception {
            return pingServer();
        }
    }

    public boolean isPingRequest(String request) {
        return ConstantUtil.PING_REQUEST.equals(request);
    }

    public void responseToPing(Socket socket) throws IOException {
        OutputStream out = socket.getOutputStream();
        out.write("HTTP/1.1 200 OK\n\n".getBytes());
        out.write(ConstantUtil.PING_RESPONSE.getBytes());
    }

    private Boolean pingServer() throws ProxyCacheException {

        String pingUrl = getPingUrl();
        HttpUrlSource source = new HttpUrlSource(pingUrl);
        try {
            byte[] expectedResponse = ConstantUtil.PING_RESPONSE.getBytes();
            source.open(0);
            byte[] response = new byte[expectedResponse.length];
            source.read(response);
            boolean pingOK = Arrays.equals(expectedResponse, response);
            LogUtil.i("Ping response " + new String(response) + ",pingOK?" + pingOK);
            return pingOK;
        } catch (ProxyCacheException e) {
            LogUtil.e("Error reading ping response " + Log.getStackTraceString(e));
            return false;
        } finally {
            source.close();
        }
    }
}

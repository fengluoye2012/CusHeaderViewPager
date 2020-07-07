package com.fly.androidvideocache.proxy;

import android.content.Context;
import android.util.Log;

import com.fly.androidvideocache.file.DiskUsage;
import com.fly.androidvideocache.file.FileNameGenerator;
import com.fly.androidvideocache.file.Md5FileNameGenerator;
import com.fly.androidvideocache.file.TotalSizeLruDiskUsage;
import com.fly.androidvideocache.headerInjector.EmptyHeaderInjector;
import com.fly.androidvideocache.headerInjector.HeaderInjector;
import com.fly.androidvideocache.sourcestorage.SourceInfoStorage;
import com.fly.androidvideocache.sourcestorage.SourceInfoStorageFactory;
import com.fly.androidvideocache.utils.ConstantUtil;
import com.fly.androidvideocache.utils.LogUtil;
import com.fly.androidvideocache.utils.ProxyCacheException;
import com.fly.androidvideocache.utils.ProxyCacheUtil;
import com.fly.androidvideocache.utils.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//客户端代理服务器
public class HttpProxyCacheServer {

    private final Object clientsLock = new Object();
    private final ExecutorService socketProcessor = Executors.newFixedThreadPool(8);
    private final Map<String, HttpProxyCacheServerClients> clientsMap = new ConcurrentHashMap<>();
    private final ServerSocket serverSocket;
    private final int port;
    private final Thread waitConnectionThread;
    private final Config config;
    private final Pinger pinger;

    public HttpProxyCacheServer(Context context) {
        this(new Builder(context).buildConfig());
    }

    public HttpProxyCacheServer(Config config) {
        this.config = config;
        try {
            InetAddress inetAddress = InetAddress.getByName(ConstantUtil.PROXY_HOST);
            this.serverSocket = new ServerSocket(0, 8, inetAddress);
            this.port = serverSocket.getLocalPort();
            IgnoreHostProxySelector.install(ConstantUtil.PROXY_HOST, port);
            CountDownLatch startSignal = new CountDownLatch(1);
            this.waitConnectionThread = new Thread(new WaitRequestRunnable(startSignal));
            this.waitConnectionThread.start();
            startSignal.await();
            this.pinger = new Pinger(ConstantUtil.PROXY_HOST, port);
            LogUtil.i("Proxy cache service started. It is alive? " + isAlive());
        } catch (IOException | InterruptedException e) {
            socketProcessor.shutdown();
            throw new IllegalStateException("Error starting local proxy server " + Log.getStackTraceString(e));
        }
    }

    private boolean isAlive() {
        return pinger.ping(3, 70);
    }

    private class WaitRequestRunnable implements Runnable {

        private final CountDownLatch startSignal;

        public WaitRequestRunnable(CountDownLatch startSignal) {
            this.startSignal = startSignal;
        }

        @Override
        public void run() {
            startSignal.countDown();
            waitForRequest();
        }
    }

    private void waitForRequest() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                LogUtil.d("Accept new socket" + socket);
                socketProcessor.submit(new SocketProcessorRunnable(socket));
            }
        } catch (IOException e) {
            onError(new ProxyCacheException("Error during waiting connection", e));
        }
    }

    private void onError(ProxyCacheException exception) {

    }

    private class SocketProcessorRunnable implements Runnable {
        private final Socket socket;

        public SocketProcessorRunnable(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            processSocket(socket);
        }
    }

    private void processSocket(Socket socket) {
        try {
            GetRequest request = GetRequest.read(socket.getInputStream());
            LogUtil.d("Request to cache proxy:" + request);
            String url = ProxyCacheUtil.decode(request.url);
            if (pinger.isPingRequest(url)) {
                pinger.responseToPing(socket);
            } else {
                HttpProxyCacheServerClients clients = getClients(url);
                clients.processRequest(request, socket);
            }
        } catch (SocketException e) {
            LogUtil.d("Closing socket… Socket is closed by client.");
        } catch (ProxyCacheException | IOException e) {
            onError(new ProxyCacheException("Error processing request", e));
        } finally {
            releaseSocket(socket);
            LogUtil.d("Opened connections: " + getClientsCount());
        }
    }

    private HttpProxyCacheServerClients getClients(String url) {
        synchronized (clientsLock) {
            HttpProxyCacheServerClients clients = clientsMap.get(url);
            if (clients == null) {
                clients = new HttpProxyCacheServerClients(url, config);
                clientsMap.put(url, clients);
            }
            return clients;
        }
    }

    private void releaseSocket(Socket socket) {
        closeSocketInput(socket);
        closeSocketOutput(socket);
        closeSocket(socket);
    }

    private int getClientsCount() {
        synchronized (clientsLock) {
            int count = 0;
            for (HttpProxyCacheServerClients clients : clientsMap.values()) {
                count += clients.getClientsCount();
            }
            return count;
        }
    }

    private void closeSocketInput(Socket socket) {
        try {
            if (!socket.isInputShutdown()) {
                socket.shutdownInput();
            }
        } catch (SocketException e) {
            LogUtil.d("Releasing input stream… Socket is closed by client.");
        } catch (IOException e) {
            onError(new ProxyCacheException("Error closing socket input stream", e));
        }
    }

    private void closeSocketOutput(Socket socket) {
        try {
            if (!socket.isOutputShutdown()) {
                socket.shutdownOutput();
            }
        } catch (IOException e) {
            LogUtil.w("Failed to close socket on proxy side: {}. It seems client have already closed connection.", e.getMessage());
        }
    }

    private void closeSocket(Socket socket) {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            onError(new ProxyCacheException("Error closing socket", e));
        }
    }

    public static final class Builder {

        private File cacheRoot;
        private FileNameGenerator fileNameGenerator;
        private DiskUsage diskUsage;
        private SourceInfoStorage sourceInfoStorage;
        private HeaderInjector headerInjector;

        public Builder(Context context) {
            this.sourceInfoStorage = SourceInfoStorageFactory.newSourceInfoStorage(context);
            this.cacheRoot = StorageUtils.getIndividualCacheDirectory(context);
            this.diskUsage = new TotalSizeLruDiskUsage(ConstantUtil.DEFAULT_MAX_SIZE);
            this.fileNameGenerator = new Md5FileNameGenerator();
            this.headerInjector = new EmptyHeaderInjector();
        }

        public Builder setCacheRoot(File cacheRoot) {
            this.cacheRoot = cacheRoot;
            return this;
        }

        public Builder setFileNameGenerator(FileNameGenerator fileNameGenerator) {
            this.fileNameGenerator = fileNameGenerator;
            return this;
        }

        public Builder setDiskUsage(DiskUsage diskUsage) {
            this.diskUsage = diskUsage;
            return this;
        }

        public Builder setSourceInfoStorage(SourceInfoStorage sourceInfoStorage) {
            this.sourceInfoStorage = sourceInfoStorage;
            return this;
        }

        public Builder setHeaderInjector(HeaderInjector headerInjector) {
            this.headerInjector = headerInjector;
            return this;
        }

        public HttpProxyCacheServer build() {
            Config config = buildConfig();
            return new HttpProxyCacheServer(config);
        }

        private Config buildConfig() {
            return new Config(cacheRoot, fileNameGenerator, diskUsage, sourceInfoStorage, headerInjector);
        }
    }
}

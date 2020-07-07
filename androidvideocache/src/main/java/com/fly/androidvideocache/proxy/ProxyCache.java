package com.fly.androidvideocache.proxy;

import android.util.Log;

import com.fly.androidvideocache.cache.Cache;
import com.fly.androidvideocache.source.Source;
import com.fly.androidvideocache.utils.ConstantUtil;
import com.fly.androidvideocache.utils.LogUtil;
import com.fly.androidvideocache.utils.ProxyCacheException;
import com.fly.androidvideocache.utils.ProxyCacheUtil;

import java.util.concurrent.atomic.AtomicInteger;

import static androidx.core.util.Preconditions.checkNotNull;

/**
 * Cache 静态代理类
 */
public class ProxyCache {

    private static final int MAX_READ_SOURCE_ATTEMPTS = 1;

    private final Source source;
    private final Cache cache;
    private final Object wc = new Object();
    private final Object stopLock = new Object();
    private final AtomicInteger readSourceErrorsCount;//读取资源错误次数
    private volatile Thread sourceReadThread;//异步读取数据线程
    private volatile boolean stopped;//读取过程是否暂停
    private volatile int percentsAvailable = -1;//文件缓存进度

    public ProxyCache(Source source, Cache cache) {
        this.source = checkNotNull(source);
        this.cache = checkNotNull(cache);
        this.readSourceErrorsCount = new AtomicInteger();
    }

    //读取数据，然后往buffer中写入
    public int read(byte[] buffer, long offset, int length) throws ProxyCacheException {
        ProxyCacheUtil.assertBuffer(buffer, offset, length);
        //本地缓存不能满足需要读取的长度
        if (!cache.isCompleted() && cache.available() < (offset + length) && !stopped) {
            readSourceAsync();
            waitForSourceData();
            checkReadSourceErrorCount();
        }

        int read = cache.read(buffer, offset, length);
        if (cache.isCompleted() && percentsAvailable != 100) {
            percentsAvailable = 100;
            onCachePercentsAvailableChanged(100);
        }
        return read;
    }

    //开启异步线程读取数据
    private void readSourceAsync() {
        //正在读取中
        boolean readingInProgress = sourceReadThread != null && sourceReadThread.getState() != Thread.State.TERMINATED;
        if (!stopped && !cache.isCompleted() && !readingInProgress) {
            sourceReadThread = new Thread(new SourceReaderRunnable());
            sourceReadThread.start();
        }
    }

    private void waitForSourceData() throws ProxyCacheException {
        synchronized (wc) {
            try {
                wc.wait(1000);
            } catch (InterruptedException e) {
                throw new ProxyCacheException("Waiting source data is interrupted!", e);
            }
        }
    }

    private void checkReadSourceErrorCount() throws ProxyCacheException {
        int errorsCount = readSourceErrorsCount.get();
        if (errorsCount >= MAX_READ_SOURCE_ATTEMPTS) {
            readSourceErrorsCount.set(0);
            throw new ProxyCacheException("Error reading source " + errorsCount + " times");
        }
    }

    //终止异步读取数据
    public void shutDown() {
        synchronized (stopLock) {
            LogUtil.d("Shutdown proxy for " + source);

            try {
                stopped = true;
                if (sourceReadThread != null) {
                    sourceReadThread.interrupt();
                }
                cache.close();
            } catch (ProxyCacheException e) {
                onError(e);
            }
        }
    }

    protected void onCachePercentsAvailableChanged(int percentsAvailable) {

    }

    private class SourceReaderRunnable implements Runnable {
        @Override
        public void run() {
            readSource();
        }
    }

    private void readSource() {
        long sourceAvailable = -1;
        long offset = 0;
        try {
            offset = cache.available();
            //打开链接
            source.open(offset);
            sourceAvailable = source.length();
            byte[] buffer = new byte[ConstantUtil.DEFAULT_BUFFER_SIZE];
            int readBytes;
            //不断从网络下载文件写入到本地中，更新缓存进度
            while ((readBytes = source.read(buffer)) != -1) {
                synchronized (stopLock) {
                    if (isStopped()) {
                        return;
                    }
                    cache.append(buffer, readBytes);
                }
                offset += readBytes;
                notifyNewCacheDataAvailable(offset, sourceAvailable);
            }
            tryComplete();
            onSourceRead();
        } catch (Throwable e) {
            readSourceErrorsCount.incrementAndGet();
            onError(e);
        } finally {
            closeSource();
            notifyNewCacheDataAvailable(offset, sourceAvailable);
        }
    }


    private boolean isStopped() {
        return Thread.currentThread().isInterrupted() || stopped;
    }

    //更新缓存进度
    private void notifyNewCacheDataAvailable(long cacheAvailable, long sourceAvailable) {
        onCacheAvailable(cacheAvailable, sourceAvailable);
        synchronized (wc) {
            wc.notifyAll();
        }
    }

    private void onCacheAvailable(long cacheAvailable, long sourceLength) {
        boolean zeroLengthSource = sourceLength == 0;
        int percents = zeroLengthSource ? 100 : (int) (cacheAvailable * 1.0F / sourceLength);
        boolean percentsChanged = percents != percentsAvailable;
        boolean sourceLengthKnown = sourceLength >= 0;
        if (sourceLengthKnown && percentsChanged) {
            onCachePercentsAvailableChanged(percents);
        }
        percentsAvailable = percents;
    }


    private void tryComplete() throws ProxyCacheException {
        synchronized (stopLock) {
            if (!isStopped() && cache.available() == source.length()) {
                cache.complete();
            }
        }
    }

    //如果每次只下载500k 一次的下载内容读取完成,即只显示具体的进度即可
    private void onSourceRead() {
        //percentsAvailable = 100;
        onCachePercentsAvailableChanged(percentsAvailable);
    }

    private void closeSource() {
        try {
            source.close();
        } catch (ProxyCacheException e) {
            onError(new ProxyCacheException("Error closing source " + source, e));
        }
    }

    private void onError(Throwable e) {
        boolean interruption = e instanceof InterruptedProxyCacheException;
        if (interruption) {
            LogUtil.d("ProxyCache is interrupted");
        } else {
            LogUtil.e("ProxyCache error" + Log.getStackTraceString(e));
        }
    }
}

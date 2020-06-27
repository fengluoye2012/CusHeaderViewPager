package com.fly.androidvideocache.proxy;

import com.fly.androidvideocache.cache.Cache;
import com.fly.androidvideocache.source.Source;
import com.fly.androidvideocache.utils.ConstantUtil;
import com.fly.androidvideocache.utils.ProxyCacheException;
import com.fly.androidvideocache.utils.ProxyCacheUtil;

import java.util.concurrent.atomic.AtomicInteger;

import static androidx.core.util.Preconditions.checkNotNull;

/**
 * Cache 静态代理类
 */
public class ProxyCache {
    private Source source;
    private Cache cache;
    private Object wc = new Object();
    private Object stopLock = new Object();
    private AtomicInteger readSourceErrorsCount;//读取资源错误次数
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
        if (!cache.isComplete() && cache.available() < (offset + length) && !stopped) {
            readSourceAsync();
            waitForSourceData();
            checkReadSourceErrorCount();
        }

        int read = cache.read(buffer, offset, length);
        if (cache.isComplete() && percentsAvailable != 100) {
            percentsAvailable = 100;
            onCachePercentsAvailableChanged(100);
        }
        return read;
    }

    //
    private void readSourceAsync() {
        //正在读取中
        boolean readingInProgress = sourceReadThread != null && sourceReadThread.getState() != Thread.State.TERMINATED;
        if (!stopped && !cache.isComplete() && !readingInProgress) {
            sourceReadThread = new Thread(new SourceReaderRunnable());
            sourceReadThread.start();
        }
    }

    private void waitForSourceData() {

    }

    private void checkReadSourceErrorCount() {

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
        }finally {
            closeSource();
            notifyNewCacheDataAvailable(offset,sourceAvailable);
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

    //如果每次只下载500k 一次的下载内容读取完成 可以这样写吗 todo
    private void onSourceRead() {
        percentsAvailable = 100;
        onCachePercentsAvailableChanged(percentsAvailable);
    }

    private void closeSource() {

    }

    private void onError(Throwable e) {

    }
}

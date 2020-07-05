package com.fly.androidvideocache.utils;

public class ConstantUtil {

    /**
     * 一次网络请求最多下载500K
     */
    public static final int MAX_LENGTH_ONCE = 500 * 1024;

    /**
     * 每次读取的长度
     */
    public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;


    public static final int MAX_ARRAY_PREVIEW = 16;

    /**
     * Head请求的offset
     */
    public static final int HEAD_REQUEST_OFFSET = -2;

    /**
     * 代理域名
     */
    public static final String PROXY_HOST = "127.0.0.1";

    public static final String PING_REQUEST = "ping";
    public static final String PING_RESPONSE = "ping ok";

    //默认文件缓存大小
    public static final long DEFAULT_MAX_SIZE = 512 * 1024 * 1024;

}

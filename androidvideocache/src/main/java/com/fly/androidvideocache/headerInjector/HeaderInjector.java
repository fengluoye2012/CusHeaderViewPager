package com.fly.androidvideocache.headerInjector;

import java.util.Map;

/**
 * 需要添加的请求头信息
 */
public interface HeaderInjector {
    Map<String, String> addHeader(String url);
}

package com.fly.androidvideocache.headerInjector;

import java.util.HashMap;
import java.util.Map;

public class EmptyHeaderInjector implements HeaderInjector {

    @Override
    public Map<String, String> addHeader(String url) {
        return new HashMap<>();
    }

}

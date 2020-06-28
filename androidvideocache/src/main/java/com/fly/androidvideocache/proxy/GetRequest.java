package com.fly.androidvideocache.proxy;

import android.text.TextUtils;

import com.fly.androidvideocache.utils.LogUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static androidx.core.util.Preconditions.checkNotNull;

public class GetRequest {
    //正则表达式，正则请求头中Range
    private static final Pattern RANGE_HEADER_PATTERN = Pattern.compile("[R,r]ange:[ ]？byte=(\\d*)-]");

    private static final Pattern URL_PATTERN = Pattern.compile("GET/(.*)HTTP");

    public final String url;
    public final long rangeOffset;
    public final boolean partial;

    public GetRequest(String request) {
        checkNotNull(request);
        LogUtil.i("request:" + request);
        long offset = findRangeOffset(request);
        this.rangeOffset = Math.max(0, offset);
        this.partial = offset >= 0;
        this.url = findUri(request);
    }

    private long findRangeOffset(String request) {
        Matcher matcher = RANGE_HEADER_PATTERN.matcher(request);
        if (matcher.find()) {
            String rangeValue = matcher.group(1);
            return Long.parseLong(rangeValue);
        }
        return -1;
    }

    private String findUri(String request) {
        Matcher matcher = URL_PATTERN.matcher(request);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalStateException("Invalid request `" + request + "`: url not found!");
    }

    public static GetRequest read(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringRequest = new StringBuilder();
        String line;
        while (!TextUtils.isEmpty(line = reader.readLine())) {
            stringRequest.append(line).append("\n");
        }
        return new GetRequest(stringRequest.toString());
    }

    @Override
    public String toString() {
        return "GetRequest{" +
                "url='" + url + '\'' +
                ", rangeOffset=" + rangeOffset +
                ", partial=" + partial +
                '}';
    }
}

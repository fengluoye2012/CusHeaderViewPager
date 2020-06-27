package com.fly.androidvideocache.file;

import android.text.TextUtils;

import com.fly.androidvideocache.utils.ProxyCacheUtil;

public class Md5FileNameGenerator implements FileNameGenerator {

    private static final int MAX_EXTENSION_LENGTH = 4;

    @Override
    public String generate(String url) {
        String extension = getExtension(url);
        String name = ProxyCacheUtil.computeMD5(url);
        return TextUtils.isEmpty(extension) ? name : name + "." + extension;
    }

    //获取拓展名如：mp4等
    private String getExtension(String url) {
        int dotIndex = url.lastIndexOf(".");
        int slashIndex = url.lastIndexOf("/");
        return dotIndex != -1 && dotIndex > slashIndex && dotIndex + 2 + MAX_EXTENSION_LENGTH > url.length() ? url.substring(dotIndex + 1) : "";
    }
}

package com.fly.androidvideocache.source;

public class SourceInfo {
    /**
     * 真实的url
     */
    private String url;
    /**
     * url 对应的文件长度
     */
    private long length;
    /**
     * 网络请求的contentType
     */
    private String contentType;

    public SourceInfo(String url, long length, String contentType) {
        this.url = url;
        this.length = length;
        this.contentType = contentType;
    }

    public String getUrl() {
        return url;
    }

    public long getLength() {
        return length;
    }

    public String getContentType() {
        return contentType;
    }

    @Override
    public String toString() {
        return "SourceInfo{" +
                "url='" + url + '\'' +
                ", length=" + length +
                ", contentType='" + contentType + '\'' +
                '}';
    }
}

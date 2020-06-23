package com.fly.androidvideocache.file;

import java.io.File;
import java.io.IOException;

//不对磁盘空间进行限制
public class UnlimitedDiskUsage implements DiskUsage {

    @Override
    public void touch(File file) throws IOException {

    }
}

package com.fly.androidvideocache.file;

import java.io.File;
import java.io.IOException;

public interface DiskUsage {
    void touch(File file) throws IOException;
}

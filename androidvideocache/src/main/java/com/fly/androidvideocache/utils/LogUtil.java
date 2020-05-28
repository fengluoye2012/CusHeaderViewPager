package com.fly.androidvideocache.utils;

import android.util.Log;

public class LogUtil {

    private static String TAG = "videoCache";

    private static boolean debug = true;

    public static void setDebug(boolean debug) {
        LogUtil.debug = debug;
    }

    public static void i(String msg) {
        Log.i(TAG, msg);
    }

    public static void i(String tag, String msg) {
        if (debug) {
            return;
        }
        Log.i(tag, msg);
    }

    public static void d(String msg) {
        Log.d(TAG, msg);
    }

    public static void d(String tag, String msg) {
        if (debug) {
            return;
        }
        Log.d(tag, msg);
    }

    public static void w(String msg) {
        Log.w(TAG, msg);
    }

    public static void w(String tag, String msg) {
        if (debug) {
            return;
        }
        Log.w(tag, msg);
    }

    public static void e(String msg) {
        if (debug) {
            return;
        }
        Log.e(TAG, msg);
    }

    public static void e(String tag, String msg) {
        if (debug) {
            return;
        }
        Log.e(tag, msg);
    }


}

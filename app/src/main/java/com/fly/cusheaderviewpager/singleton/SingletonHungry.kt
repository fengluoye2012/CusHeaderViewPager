package com.fly.cusheaderviewpager.singleton

import com.blankj.utilcode.util.LogUtils

object SingletonHungry {
    
    //无参无返回值
    public fun test() {
        LogUtils.i("test")
    }

    public fun test2(): Int {
        val a = 2
        return a
    }
}
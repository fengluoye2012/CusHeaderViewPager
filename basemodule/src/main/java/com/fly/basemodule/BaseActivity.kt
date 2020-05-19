package com.fly.basemodule

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * 抽象类
 */
abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var act: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        act = this

        setContentView(getLayoutId())

        initListeners()
        initData()
    }

    abstract fun getLayoutId(): Int

    open fun initListeners() {
    }

    open fun initData() {}
}
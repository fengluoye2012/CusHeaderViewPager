package com.fly.headerviewpager

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class CusViewPager : ViewPager {

    /**
     * 是否允许ViewPager拦截事件
     */
    private var allowIntercept: Boolean = false

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}


    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (!allowIntercept) {
            false
        } else super.onInterceptTouchEvent(ev)
    }


    fun setAllowIntercept(allowIntercept: Boolean) {
        this.allowIntercept = allowIntercept
    }
}

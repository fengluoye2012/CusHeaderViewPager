package com.fly.basemodule.swipeback

import android.app.Activity
import java.lang.ref.WeakReference

/**
 * 主构造函数
 */
class SwipeBackListenerActivityAdapter(private var mActivity: WeakReference<Activity>) : SwipeBackLayout.SwipeListenerEx {

    override fun onEdgeTouch(edgeFlag: Int) {
        val activity: Activity? = mActivity.get()
        activity?.let {  }

    }

    override fun onScrollOverThreshold() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onContentViewSwipedBack() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onScrollStateChange(state: Int, scrollPercent: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
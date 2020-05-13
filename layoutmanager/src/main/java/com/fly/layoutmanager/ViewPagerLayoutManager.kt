package com.fly.layoutmanager

import android.content.Context
import android.util.AttributeSet

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView 实现ViewPager的效果
 */
class ViewPagerLayoutManager : LinearLayoutManager {

    private var recyclerView: RecyclerView? = null
    private var pagerSnapHelper: PagerSnapHelper? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    fun init() {
        pagerSnapHelper = PagerSnapHelper()
    }

    fun setRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    override fun onAttachedToWindow(view: RecyclerView?) {
        super.onAttachedToWindow(view)

        pagerSnapHelper!!.attachToRecyclerView(recyclerView)
    }

    override fun onDetachedFromWindow(view: RecyclerView, recycler: RecyclerView.Recycler?) {
        super.onDetachedFromWindow(view, recycler)

    }
}

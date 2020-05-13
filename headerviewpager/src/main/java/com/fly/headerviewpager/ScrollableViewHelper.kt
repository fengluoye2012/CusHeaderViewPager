package com.fly.headerviewpager

import android.os.Build
import android.view.View
import android.webkit.WebView
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ScrollView

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class ScrollableViewHelper {

    private var scrollableInterface: ScrollableInterface? = null

    fun setScrollableView(scrollableInterface: ScrollableInterface) {
        this.scrollableInterface = scrollableInterface
    }

    val isTop: Boolean
        get() {
            return when (val scrollableView = scrollableInterface!!.scrollableView) {
                is RecyclerView -> isRecyclerViewTop(scrollableView)
                is AdapterView<*> -> isAdapterViewTop(scrollableView)
                is ScrollView -> isScrollViewViewTop(scrollableView)
                is WebView -> isWebViewTop(scrollableView)
                else -> false
            }
        }

    /**
     * 判断RecyclerView 是否处于顶部
     *
     *
     * todo 理解getTop、getBottom 等的含义
     *
     * @param recyclerView
     * @return
     */
    private fun isRecyclerViewTop(recyclerView: RecyclerView): Boolean {
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is LinearLayoutManager) {
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val childAt = recyclerView.getChildAt(0)
            //recyclerView index = 0 的item 不可见或者 第一条可见的item index 是0并且其top == 0,则RecyclerView 顶部
            if (childAt == null || firstVisibleItemPosition == 0 && childAt.top == 0) {//getTop()相对于parent顶部坐标
                return true
            }
        }
        return false
    }

    /**
     * 判断AdapterView(包括ListView、GridView) 是否处于顶部
     *
     * @param adapterView
     * @return
     */
    private fun isAdapterViewTop(adapterView: AdapterView<*>): Boolean {
        val firstVisiblePosition = adapterView.firstVisiblePosition
        val childAt = adapterView.getChildAt(0)
        //AdapterView index = 0 的item 不可见或者 第一条可见的item index 是0并且其top == 0,则RecyclerView 顶部
        return childAt == null || firstVisiblePosition == 0 && childAt.top == 0
    }

    private fun isScrollViewViewTop(scrollView: ScrollView): Boolean {
        val childAt = scrollView.getChildAt(0)

        return childAt == null || childAt.top == 0
    }

    private fun isWebViewTop(webView: WebView): Boolean {
        val childAt = webView.getChildAt(0)
        return childAt == null || childAt.top == 0
    }

    fun fling(yVelocity: Int, distance: Int, duration: Int) {
        val scrollableView = scrollableInterface!!.scrollableView

        if (scrollableView is RecyclerView) {
            scrollableView.fling(0, yVelocity)
        } else if (scrollableView is AbsListView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                scrollableView.fling(yVelocity)
            } else {
                scrollableView.smoothScrollBy(distance, duration)
            }
        } else if (scrollableView is ScrollView) {
            scrollableView.fling(yVelocity)
        } else if (scrollableView is WebView) {
            scrollableView.flingScroll(0, yVelocity)
        }
    }
}

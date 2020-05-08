package com.fly.headerviewpager;

import android.os.Build;
import android.view.View;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ScrollView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class ScrollableViewHelper {

    /**
     * 可滑动的View，可以是RecyclerView,ListView等
     */
    private View scrollableView;

    public ScrollableViewHelper() {

    }

    public View getScrollableView() {
        return scrollableView;
    }

    public void setScrollableView(View scrollableView) {
        this.scrollableView = scrollableView;
    }

    public boolean isTop() {
        if (scrollableView == null) {
            throw new NullPointerException("scrollableView can not null");
        }

        if (scrollableView instanceof RecyclerView) {
            return isRecyclerViewTop((RecyclerView) scrollableView);
        } else if (scrollableView instanceof AdapterView) {
            return isAdapterViewTop((AdapterView) scrollableView);
        } else if (scrollableView instanceof ScrollView) {
            return isScrollViewViewTop((ScrollView) scrollableView);
        } else if (scrollableView instanceof WebView) {
            return isWebViewTop((WebView) scrollableView);
        }
        return false;
    }

    /**
     * 判断RecyclerView 是否处于顶部
     * <p>
     * todo 理解getTop、getBottom 等的含义
     *
     * @param recyclerView
     * @return
     */
    private boolean isRecyclerViewTop(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            int firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
            View childAt = recyclerView.getChildAt(0);
            //recyclerView index = 0 的item 不可见或者 第一条可见的item index 是0并且其top == 0,则RecyclerView 顶部
            if (childAt == null || (firstVisibleItemPosition == 0 && childAt.getTop() == 0)) {//getTop()相对于parent顶部坐标
                return true;
            }
        }
        return false;
    }

    /**
     * 判断AdapterView(包括ListView、GridView) 是否处于顶部
     *
     * @param adapterView
     * @return
     */
    private boolean isAdapterViewTop(AdapterView adapterView) {
        int firstVisiblePosition = adapterView.getFirstVisiblePosition();
        View childAt = adapterView.getChildAt(0);
        //AdapterView index = 0 的item 不可见或者 第一条可见的item index 是0并且其top == 0,则RecyclerView 顶部
        if (childAt == null || (firstVisiblePosition == 0 && childAt.getTop() == 0)) {
            return true;
        }
        return false;
    }

    private boolean isScrollViewViewTop(ScrollView scrollView) {
        View childAt = scrollView.getChildAt(0);
        int scrollY = scrollView.getScrollY();

        if (childAt == null || childAt.getTop() == 0) {
            return true;
        }
        return false;
    }

    private boolean isWebViewTop(WebView webView) {
        View childAt = webView.getChildAt(0);
        int scrollY = webView.getScrollY();
        if (childAt == null || childAt.getTop() == 0) {
            return true;
        }
        return false;
    }

    public void fling(int yVelocity) {
        if (scrollableView == null) {
            throw new NullPointerException("scrollableView can not null");
        }

        if (scrollableView instanceof RecyclerView) {
            ((RecyclerView) scrollableView).fling(0, yVelocity);
        } else if (scrollableView instanceof AbsListView) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ((AbsListView) scrollableView).fling(yVelocity);
            } else {
                //((AbsListView) scrollableView).smoothScrollBy(yVelocity);
            }
        } else if (scrollableView instanceof ScrollView) {
            ((ScrollView) scrollableView).fling(yVelocity);
        } else if (scrollableView instanceof WebView) {
            ((WebView) scrollableView).flingScroll(0, yVelocity);
        }
    }
}

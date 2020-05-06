package com.fly.headerviewpager;

import android.view.View;


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
            return true;
        }
        return false;
    }
}

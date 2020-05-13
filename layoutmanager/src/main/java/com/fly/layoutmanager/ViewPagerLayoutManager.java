package com.fly.layoutmanager;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * RecyclerView 实现ViewPager的效果
 */
public class ViewPagerLayoutManager extends LinearLayoutManager {

    private RecyclerView recyclerView;
    private PagerSnapHelper pagerSnapHelper;

    public ViewPagerLayoutManager(Context context) {
        super(context);
        init();
    }

    public ViewPagerLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        init();
    }

    public ViewPagerLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void init() {
        pagerSnapHelper = new PagerSnapHelper();
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);

        pagerSnapHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);

    }
}

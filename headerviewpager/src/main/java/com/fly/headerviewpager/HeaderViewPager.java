package com.fly.headerviewpager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * 通过自定义View的方式，实现dispatchTouchEvent方法，在HeaderView 处于顶部的时候滑动
 * <p>
 * 实现View的遍历（深度和广度） todo
 */
public class HeaderViewPager extends LinearLayout {

    /**
     * 可滑动的View，可以是RecyclerView,ListView,ScrollView以及WebView;
     */
    private View scrollableView;
    /**
     * 用来实现View的滑动
     */
    private Scroller scroller;

    /**
     * 顶部可滑动的View
     */
    private View headerView;

    /**
     * 最小的滑动距离
     */
    private int minY;
    /**
     * 可滑动的最大距离
     */
    private int maxY;
    private float downX;
    private float downY;
    /**
     * 最小滑动距离
     */
    private int scaledTouchSlop;
    private boolean verticalScroll;

    public HeaderViewPager(Context context) {
        this(context, null);
    }

    public HeaderViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public HeaderViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setOrientation(VERTICAL);
        scroller = new Scroller(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        if (childCount < 1) {
            throw new IllegalStateException("父容器中必须有子View");
        }
        //将父容器中的第一个子View作为头布局
        headerView = getChildAt(0);

        //todo 精确获取headerView的高度
    }

    /**
     * 是否不允许父容器拦截事件
     *
     * @param disallowIntercept
     */
    public void disallowInterceptTouchEvent(boolean disallowIntercept) {
        disallowInterceptTouchEvent(disallowIntercept);
    }

    /**
     * 手指拖拽：
     * 当HeaderView 可见时，headerView滑动，容器整体滑动(当前容器滑动，将事件传递给scrollableView，scrollableView也滑动，
     * 但是scrollableView 滑动距离是前后两次move的手指相对于原点的距离，随后又再次矫正距离，结果正负抵消，
     * scrollableView并不发生滑动。即手指相对scrollableView来说没有发生位移）具体可实现scrollableView的滑动监听验证
     * <p>
     * 当HeaderView 不可见时，headerView不滑动，容器整体位置不变，scrollableView滑动
     * <p>
     * 惯性滑动：计算出惯性速度，传递给scrollableView
     * <p>
     *
     * @param ev
     * @return 返回true表示事件被当前容器或者其子View消费；
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = ev.getY();
                downX = ev.getX();
                break;

            case MotionEvent.ACTION_MOVE:
                float moveX = ev.getX();
                float moveY = ev.getY();
                float disX = moveX - downX;
                float disY = moveY - downY;

                verticalScroll = false;

                //竖直方向上的滑动
                if (Math.abs(disY) > scaledTouchSlop && Math.abs(disY) > Math.abs(disX)) {
                    verticalScroll = true;

                    //手指往上滑动（scrollableView 的内容往上滑动）
                    if (disY < 0) {
                        //如果headerView 可见或者 scrollableView top可见 滑动headerView；
                        if (isTop())
                    } else {

                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                break;
        }

        scrollableView.dispatchTouchEvent(ev);

        return true;
    }


    public View getScrollableView() {
        return scrollableView;
    }

    public void setScrollableView(View scrollableView) {
        this.scrollableView = scrollableView;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }


    public View getHeaderView() {
        return headerView;
    }

    public void setHeaderView(View headerView) {
        this.headerView = headerView;
    }

    /**
     * headerView 是否在顶部
     *
     * @return
     */
    public boolean isTop() {
        return false;
    }

    /**
     * 顶部是否已经固定
     *
     * @return
     */
    public boolean isStick() {
        return false;
    }
}

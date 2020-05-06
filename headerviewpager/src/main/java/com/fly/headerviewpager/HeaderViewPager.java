package com.fly.headerviewpager;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.Scroller;

import androidx.viewpager.widget.ViewPager;

/**
 * 通过自定义View的方式，实现dispatchTouchEvent方法，在HeaderView 处于顶部的时候滑动
 * <p>
 * 实现View的遍历（深度和广度） todo
 */
public class HeaderViewPager extends LinearLayout {

    /**
     * 用来实现View的滑动
     */
    protected Scroller scroller;

    /**
     * 顶部可滑动的View
     */
    protected View headerView;

    /**
     * 最小的滑动距离
     */
    protected int minY;
    /**
     * 可滑动的最大距离
     */
    protected int maxY;

    /**
     * 当前已经滑动的距离
     */
    protected int curY;

    protected float downX;
    protected float downY;
    /**
     * 最小滑动距离
     */
    protected int scaledTouchSlop;
    /**
     * 是否竖直方向滑动
     */
    protected boolean verticalScroll;

    /**
     * 最小、最大的Fling速度
     */
    protected int minimumFlingVelocity;
    protected int maximumFlingVelocity;

    /**
     * headerView的高度
     */
    protected int headerViewHeight;
    /**
     * HeaderView 的偏移量
     */
    protected float topOffset;
    /**
     * 是否允许容器拦截事件
     */
    protected boolean disallowIntercept;

    /**
     * ViewPager
     */
    protected ViewPager viewPager;


    /**
     * 可滑动View的Helper 类
     */
    protected ScrollableViewHelper scrollableViewHelper;

    public HeaderViewPager(Context context) {
        this(context, null);
    }

    public HeaderViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HeaderViewPager);
        topOffset = typedArray.getDimension(R.styleable.HeaderViewPager_hvp_topOffset, 0);
        typedArray.recycle();

    }

    public HeaderViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        minimumFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
        maximumFlingVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        scroller = new Scroller(context);
        scrollableViewHelper = new ScrollableViewHelper();
    }

    //熟练掌握 measure 相关方法 todo
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childCount = getChildCount();
        if (childCount < 1) {
            throw new IllegalStateException("父容器中必须有子View");
        }
        //将父容器中的第一个子View作为头布局
        headerView = getChildAt(0);

        measureChildWithMargins(headerView, widthMeasureSpec, 0, MeasureSpec.UNSPECIFIED, 0);
        headerViewHeight = headerView.getMeasuredHeight();
        //四舍五入
        maxY = Math.round(headerViewHeight - topOffset);

        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec) + maxY, MeasureSpec.EXACTLY));
    }

    /**
     * xml 渲染完成,设置headerView 是可点击的
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (headerView != null && !headerView.isClickable()) {
            headerView.setClickable(true);
        }
    }

    /**
     * 是否不允许父容器和当前容器拦截事件
     *
     * @param disallowIntercept
     */
    public void disallowHeaderViewPagerInterceptTouchEvent(boolean disallowIntercept) {
        this.disallowIntercept = disallowIntercept;
        requestDisallowInterceptTouchEvent(disallowIntercept);
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
                if (disallowIntercept) {
                    break;
                }

                float moveX = ev.getX();
                float moveY = ev.getY();
                float disX = moveX - downX;
                float disY = moveY - downY;

                verticalScroll = false;

                //竖直方向上的滑动
                if (Math.abs(disY) > scaledTouchSlop && Math.abs(disY) > Math.abs(disX)) {
                    verticalScroll = true;
                }

                if (Math.abs(disX) > scaledTouchSlop && Math.abs(disX) > Math.abs(disY)) {
                    verticalScroll = false;
                }

                //手指往上滑动（scrollableView 的内容往上滑动）
                if (disY < 0) {
                    //如果headerView 可见或者 scrollableView top可见 滑动headerView；
                    if (isTop() || scrollableViewHelper.isTop()) {

                    }
                } else {

                }
                break;

            case MotionEvent.ACTION_UP:
                break;

            case MotionEvent.ACTION_CANCEL:
                break;
        }

        super.dispatchTouchEvent(ev);

        return true;
    }

    /**
     * 重写scrollBy 防止滑动越界
     *
     * @param x
     * @param y
     */
    @Override
    public void scrollBy(int x, int y) {
        int scrollY = getScrollY();
        int toY = scrollY + y;
        if (toY >= maxY) {
            toY = maxY;
        } else if (toY <= minY) {
            toY = minY;
        }
        //如果滑动y之后，越界，则矫正y;
        super.scrollBy(x, toY - scrollY);
    }

    /**
     * 重写scrollTo 防止滑动越界
     *
     * @param x
     * @param y
     */
    @Override
    public void scrollTo(int x, int y) {
        if (y >= maxY) {
            y = maxY;
        } else if (y <= minY) {
            y = minY;
        }
        curY = y;

        super.scrollTo(x, y);
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

    public ViewPager getViewPager() {
        return viewPager;
    }

    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
    }

    public ScrollableViewHelper getScrollableViewHelper() {
        return scrollableViewHelper;
    }

    public void setScrollableViewHelper(ScrollableViewHelper scrollableViewHelper) {
        this.scrollableViewHelper = scrollableViewHelper;
    }

    /**
     * headerView 是否在顶部
     *
     * @return
     */
    public boolean isTop() {
        return curY == minY;
    }

    /**
     * 顶部是否已经固定
     *
     * @return
     */
    public boolean isStick() {
        return curY == maxY;
    }
}

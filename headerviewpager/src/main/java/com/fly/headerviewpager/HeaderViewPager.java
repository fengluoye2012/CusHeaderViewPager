package com.fly.headerviewpager;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
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

    //手指往上滑动
    protected static final int DIRECTION_UP = 1;
    //手指往下滑动
    protected static final int DIRECTION_DOWN = 2;

    /**
     * 滑动辅助类,Scroller本身不会去移动View，它只是一个移动计算辅助类，用于跟踪控件滑动的轨迹，只相当于一个滚动轨迹记录工具，
     * 最终还是通过View的scrollTo、scrollBy方法完成View的移动的。
     */
    protected Scroller scroller;

    /**
     * 顶部可滑动的View
     */
    protected View headerView;

    /**
     * headerView的高度
     */
    protected int headerViewHeight;
    /**
     * HeaderView 的偏移量
     */
    protected float topOffset;

    /**
     * 最小的滑动距离
     */
    protected int minY = 0;
    /**
     * 可滑动的最大距离,就是headerView的高度
     */
    protected int maxY;

    /**
     * 当前已经滑动的距离
     */
    protected int mScrollY;

    //action为down时的坐标
    protected float downX;
    protected float downY;

    //上一次action的坐标
    protected float lastY;
    protected float lastX;

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
     * 是否允许容器拦截事件
     */
    protected boolean disallowIntercept;

    /**
     * ViewPager
     */
    protected CusViewPager viewPager;

    /**
     * 是否点击到头部
     */
    protected boolean isClickHead;

    /**
     * 可滑动View的Helper 类
     */
    protected ScrollableViewHelper scrollableViewHelper;
    /**
     * 用来获取速度
     */
    private VelocityTracker velocityTracker;
    /**
     * 滑动的方向
     */
    private int mDirection;

    private String TAG = HeaderViewPager.class.getSimpleName();
    private int lastScrollY;

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

    /**
     * 熟练掌握 measure 相关方法
     * <p>
     * MeasureSpec 是由32位int值组成，前两位表示Mode，后30位表示某种测量模式下的测量大小Size
     * <p>
     * mode分为三种EXACTLY, AT_MOST, UNSPECIFIED
     * <p>
     * EXACTLY 父容器和当前View的尺寸 是具体的值或者Match_Parent   size就是View的尺寸
     * AT_MOST 父容器是具体的值或者Match_Parent，当前View的尺寸是wrap_content   size是View可显示的最大尺寸，View的具体尺寸需要测量；
     * UNSPECIFIED 能有多大就是多大一般是系统使用
     *
     * @param widthMeasureSpec  由父容器和自己的layoutParams决定的
     * @param heightMeasureSpec
     */
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

        //makeMeasureSpec 根据size和mode创建新的heightMeasureSpec；
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
     * Scroller 类的主要方法含义搞清楚
     * <p>
     * 在滑动的过程中，发现手势问题，导致scrollableView isTop() 无法上滑，是ViewPager 事件拦截的问题。
     *
     * @param ev
     * @return 返回true表示事件被当前容器或者其子View消费；
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        obtainVelocityTracker(ev);
        float curX = ev.getX();//相对View的x坐标
        float curY = ev.getY();//相对View的y坐标

        float diffX = Math.abs(curX - downX);//和downX的距离
        float diffY = Math.abs(curY - downY);//和downY的距离

        float moveY = lastY - curY;//两次滑动之间的Y 距离

        lastX = curX;
        lastY = curY;

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                verticalScroll = false;
                downX = curX;
                downY = curY;

                mDirection = 0;

                isClickHeader(curY, getScrollY());
                //手指再次按下，终止上一次的滑动
                scroller.abortAnimation();
                break;

            case MotionEvent.ACTION_MOVE:
                if (disallowIntercept) {
                    break;
                }


                //竖直方向上的滑动
                if (diffY > scaledTouchSlop && diffY >= diffX) {
                    verticalScroll = true;
                    viewPager.setAllowIntercept(false);
                }

                if (diffX > scaledTouchSlop && diffX > diffY) {
                    verticalScroll = false;
                    viewPager.setAllowIntercept(true);
                }

                //如果是竖直方向滑动
                if (verticalScroll) {
                    //手指往上滑动（scrollableView 的内容往上滑动）
                    //headerView 没有固定的情况下，scrollableView top可见 滑动headerView；
                    if (moveY > 0 && (!isStick() || scrollableViewHelper.isTop() || isClickHead)) {
                        Log.i(TAG, "scrollBy::" + moveY);
                        scrollBy(0, Math.round(moveY));
                    }

                    //手指往下滑动
                    if (moveY < 0 && (scrollableViewHelper.isTop() || isClickHead)) {
                        scrollBy(0, Math.round(moveY));
                    }
                }
                break;

            case MotionEvent.ACTION_UP:

                //为什么松手的时候，要考虑执行scroller.fling()；调用scroller.fling()方法会执行computeScroll()方法吗
                //根据Scroller类解释可知，调用startScroll()、fling()方法之后，会不断的调用View的computeScroll()，根据computeScrollOffset()判断滑动动画是否结束
                //竖直方向滑动，已经滑动最大距离或者scrollableView处于顶部，避免松手之后
                //if (verticalScroll && (scrollableViewHelper.isTop() || isStick())) {
                //竖直滑动，手指往上滑动，并且没有固定 或者 往下滑动 scrollableView 顶部可见
                if (verticalScroll) {
                    //获取1000ms允许滑动的最大距离是 maximumFlingVelocity
                    velocityTracker.computeCurrentVelocity(1000, maximumFlingVelocity);
                    //获取当前的速度
                    int yVelocity = (int) velocityTracker.getYVelocity();
                    Log.i(TAG, "yVelocity::" + yVelocity);

                    mDirection = yVelocity > 0 ? DIRECTION_DOWN : DIRECTION_UP;  //下滑速度大于0，上滑速度小于0
                    Log.i(TAG, "mDirection::" + mDirection);

                    //添加这样的判断，减少不必要的invalidate(),同时往下滑动时  先停止才能将headerView 拉下来
                    if ((mDirection == DIRECTION_UP && (scrollableViewHelper.isTop() || !isStick()))
                            || (mDirection == DIRECTION_DOWN && scrollableViewHelper.isTop() && !isTop())) {
                        Log.i(TAG, "调用scroller.fling()");

                        scroller.fling(0, getScrollY(), 0, -yVelocity,
                                Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);

                        lastScrollY = getScrollY();
                        invalidate();

                        //防止快速滑动时，点击事件发生，UP的时候，则将事件改为ACTION_CANCEL，就不会发生点击事件了；
                        if (diffX > scaledTouchSlop || diffY > scaledTouchSlop) {
                            int action = ev.getAction();
                            ev.setAction(MotionEvent.ACTION_CANCEL);
                            boolean dd = super.dispatchTouchEvent(ev);
                            ev.setAction(action);
                            return dd;
                        }
                    }
                }
                resetVelocityTracker();
                break;

            case MotionEvent.ACTION_CANCEL:
                resetVelocityTracker();
                break;
            default:
                break;
        }

        //手动将事件传递给子View,让子View处理
        super.dispatchTouchEvent(ev);

        //返回true 表示事件被当前View或者其子View消费
        return true;
    }


    /**
     * 父容器调用request()方法，获取getScrollX(),getScrollY()时调用
     * 如何优化，减少不必要的 invalidate()，在UP事件中，增加判断逻辑，减少不必要的 scroller.fling()方法调用
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
        //返回true 表示动画没有执行完成
        if (scroller.computeScrollOffset()) {
            Log.i(TAG, "computeScroll");
            int mCurrY = scroller.getCurrY();
            Log.i(TAG, "mCurrY::" + mCurrY + ",," + headerViewHeight);
            //手指往上滑动
            if (mDirection == DIRECTION_UP) {
                //如果header已经固定，则scrollableView 按照惯性继续滑动
                if (isStick()) {
                    //主要是将快速滚动时的速度对接起来，让布局看起来滚动连贯
                    int distance = scroller.getFinalY() - mCurrY;//除去布局滚动的距离，剩下的距离
                    int duration = scroller.getDuration() - scroller.timePassed();//除去滚动消费的时间，剩余的事件；
                    int yVelocity = calculateVelocity();
                    Log.i(TAG, "调用fling()");
                    //让ScrollableView fling起来
                    scrollableViewHelper.fling(yVelocity, distance, duration);
                    //同时停止外层滑动
                    scroller.abortAnimation();
                } else {
                    //由于Scroller是滑动辅助类，用于跟踪控件滑动轨迹，滑动依然依靠scrollTo(),同时调用invalidate()方法，会调用View的computeScroll();
                    scrollTo(0, mCurrY);
                    invalidate();
                }
                //手指往下滑动
            } else if (mDirection == DIRECTION_DOWN) {
                //可滑动的View处于顶部
                if (scrollableViewHelper.isTop()) {
                    int diffScrollY = mCurrY - lastScrollY;
                    scrollTo(0, getScrollY() + diffScrollY);
                    if (mScrollY <= minY) {
                        scroller.abortAnimation();
                        return;
                    }
                }
                //向下滑动是，初始状态可能不在顶部，所以要一直重绘，让computeScroll()一直调用，确保代码进入if判断
                invalidate();
            }
            lastScrollY = mCurrY;
        }
    }

    /**
     * 获取滑动的速度
     *
     * @return
     */
    private int calculateVelocity() {
        if (scroller == null) {
            return 0;
        }
        return (int) scroller.getCurrVelocity();
    }

    /**
     * 是否点击在Header
     * getY() 表示对于父容器top 的位置
     *
     * @return
     */
    private void isClickHeader(float downY, int mScrollY) {
        isClickHead = ((int) downY + mScrollY <= headerViewHeight);
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
        mScrollY = y;
        super.scrollTo(x, y);
    }


    /**
     * headerView 是否在顶部
     *
     * @return
     */
    public boolean isTop() {
        return mScrollY == minY;
    }

    /**
     * 顶部是否已经固定
     *
     * @return
     */
    public boolean isStick() {
        return mScrollY == maxY;
    }

    /**
     * 能否下拉刷新
     *
     * @return
     */
    public boolean canPtr() {
        return isTop() && scrollableViewHelper.isTop();
    }
    
    private void resetVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private void obtainVelocityTracker(MotionEvent ev) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);
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

    public void setViewPager(CusViewPager viewPager) {
        this.viewPager = viewPager;
    }

    public void setScrollableInterface(ScrollableInterface scrollableInterface) {
        scrollableViewHelper.setScrollableView(scrollableInterface);
    }

}

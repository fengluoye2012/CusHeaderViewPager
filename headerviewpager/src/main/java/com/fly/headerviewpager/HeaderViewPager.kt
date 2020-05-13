package com.fly.headerviewpager

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.LinearLayout
import android.widget.Scroller

/**
 * 通过自定义View的方式，实现dispatchTouchEvent方法，在HeaderView 处于顶部的时候滑动
 *
 *
 * 实现View的遍历（深度和广度） todo
 */
open class HeaderViewPager(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : LinearLayout(context, attrs, defStyleAttr) {

    //companion object 修饰为伴生对象,伴生对象在类中只能存在一个，类似于java中的静态方法 Java 中使用类访问静态成员，静态方法。
    companion object {
        //const 必须修饰val
        //const 只允许在top-level级别和object中声明
        //手指往上滑动
        protected const val DIRECTION_UP = 1
        //手指往下滑动
        protected const val DIRECTION_DOWN = 2
    }


    /**
     * 滑动辅助类,Scroller本身不会去移动View，它只是一个移动计算辅助类，用于跟踪控件滑动的轨迹，只相当于一个滚动轨迹记录工具，
     * 最终还是通过View的scrollTo、scrollBy方法完成View的移动的。
     */
    private var scroller: Scroller? = null

    /**
     * 顶部可滑动的View
     */
    var headerView: View? = null

    /**
     * headerView的高度
     */
    private var headerViewHeight: Int = 0
    /**
     * HeaderView 的偏移量
     */
    private var topOffset: Float = 0.toFloat()

    /**
     * 最小的滑动距离
     */
    private var minY = 0
    /**
     * 可滑动的最大距离,就是headerView的高度
     */
    private var maxY: Int = 0

    /**
     * 当前已经滑动的距离
     */
    private var mScrollY: Int = 0

    //action为down时的坐标
    private var downX: Float = 0.toFloat()
    private var downY: Float = 0.toFloat()

    //上一次action的坐标
    private var lastY: Float = 0.toFloat()
    private var lastX: Float = 0.toFloat()

    /**
     * 最小滑动距离
     */
    private var scaledTouchSlop: Int = 0
    /**
     * 是否竖直方向滑动
     */
    private var verticalScroll: Boolean = false

    /**
     * 最小、最大的Fling速度
     */
    private var minimumFlingVelocity: Int = 0
    private var maximumFlingVelocity: Int = 0

    /**
     * 是否允许容器拦截事件
     */
    private var disallowIntercept: Boolean = false

    /**
     * ViewPager
     */
    private lateinit var viewPager: CusViewPager

    /**
     * 是否点击到头部
     */
    private var isClickHead: Boolean = false

    /**
     * 可滑动View的Helper 类
     */
    private var scrollableViewHelper: ScrollableViewHelper
    /**
     * 用来获取速度
     */
    private var velocityTracker: VelocityTracker? = null
    /**
     * 滑动的方向
     */
    private var mDirection: Int = 0

    private val mTAG = HeaderViewPager::class.java.simpleName
    private var lastScrollY: Int = 0


    /**
     * headerView 是否在顶部
     *
     * @return
     */
    val isTop: Boolean
        get() = mScrollY == minY

    /**
     * 顶部是否已经固定
     *
     * @return
     */
    val isStick: Boolean
        get() = mScrollY == maxY

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : this(context, attrs, 0) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.HeaderViewPager)
        topOffset = typedArray.getDimension(R.styleable.HeaderViewPager_hvp_topOffset, 0f)
        typedArray.recycle()
    }

    init {
        orientation = LinearLayout.VERTICAL
        scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        minimumFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
        maximumFlingVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity
        scroller = Scroller(context)
        scrollableViewHelper = ScrollableViewHelper()
    }

    /**
     * 熟练掌握 measure 相关方法
     *
     *
     * MeasureSpec 是由32位int值组成，前两位表示Mode，后30位表示某种测量模式下的测量大小Size
     *
     *
     * mode分为三种EXACTLY, AT_MOST, UNSPECIFIED
     *
     *
     * EXACTLY 父容器和当前View的尺寸 是具体的值或者Match_Parent   size就是View的尺寸
     * AT_MOST 父容器是具体的值或者Match_Parent，当前View的尺寸是wrap_content   size是View可显示的最大尺寸，View的具体尺寸需要测量；
     * UNSPECIFIED 能有多大就是多大一般是系统使用
     *
     * @param widthMeasureSpec  由父容器和自己的layoutParams决定的
     * @param heightMeasureSpec
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val childCount = childCount
        if (childCount < 1) {
            throw IllegalStateException("父容器中必须有子View")
        }
        //将父容器中的第一个子View作为头布局
        headerView = getChildAt(0)

        measureChildWithMargins(headerView, widthMeasureSpec, 0, View.MeasureSpec.UNSPECIFIED, 0)
        headerViewHeight = headerView!!.measuredHeight
        //四舍五入
        maxY = Math.round(headerViewHeight - topOffset)

        //makeMeasureSpec 根据size和mode创建新的heightMeasureSpec；
        super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(heightMeasureSpec) + maxY, View.MeasureSpec.EXACTLY))
    }

    /**
     * xml 渲染完成,设置headerView 是可点击的
     */
    override fun onFinishInflate() {
        super.onFinishInflate()
        if (headerView != null && !headerView!!.isClickable) {
            headerView!!.isClickable = true
        }
    }

    /**
     * 是否不允许父容器和当前容器拦截事件
     *
     * @param disallowIntercept
     */
    fun disallowHeaderViewPagerInterceptTouchEvent(disallowIntercept: Boolean) {
        this.disallowIntercept = disallowIntercept
        requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    /**
     * 手指拖拽：
     * 当HeaderView 可见时，headerView滑动，容器整体滑动(当前容器滑动，将事件传递给scrollableView，scrollableView也滑动，
     * 但是scrollableView 滑动距离是前后两次move的手指相对于原点的距离，随后又再次矫正距离，结果正负抵消，
     * scrollableView并不发生滑动。即手指相对scrollableView来说没有发生位移）具体可实现scrollableView的滑动监听验证
     *
     *
     * 当HeaderView 不可见时，headerView不滑动，容器整体位置不变，scrollableView滑动
     *
     *
     * 惯性滑动：计算出惯性速度，传递给scrollableView
     *
     *
     * Scroller 类的主要方法含义搞清楚
     *
     *
     * 在滑动的过程中，发现手势问题，导致scrollableView isTop() 无法上滑，是ViewPager 事件拦截的问题。
     *
     * @param ev
     * @return 返回true表示事件被当前容器或者其子View消费；
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

        obtainVelocityTracker(ev)
        val curX = ev.x//相对View的x坐标
        val curY = ev.y//相对View的y坐标

        val diffX = Math.abs(curX - downX)//和downX的距离
        val diffY = Math.abs(curY - downY)//和downY的距离

        val moveY = lastY - curY//两次滑动之间的Y 距离

        lastX = curX
        lastY = curY

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                verticalScroll = false
                downX = curX
                downY = curY

                mDirection = 0

                isClickHeader(curY, scrollY)
                //手指再次按下，终止上一次的滑动
                scroller!!.abortAnimation()
            }

            MotionEvent.ACTION_MOVE -> {
                if (disallowIntercept) {

                }
                //竖直方向上的滑动
                if (diffY > scaledTouchSlop && diffY >= diffX) {
                    verticalScroll = true
                    viewPager.setAllowIntercept(false)
                }

                if (diffX > scaledTouchSlop && diffX > diffY) {
                    verticalScroll = false
                    viewPager.setAllowIntercept(true)
                }

                //如果是竖直方向滑动
                if (verticalScroll) {
                    //手指往上滑动（scrollableView 的内容往上滑动）
                    //headerView 没有固定的情况下，scrollableView top可见 滑动headerView；
                    if (moveY > 0 && (!isStick || scrollableViewHelper.isTop || isClickHead)) {
                        Log.i(mTAG, "scrollBy::$moveY")
                        scrollBy(0, Math.round(moveY))
                    }

                    //手指往下滑动
                    if (moveY < 0 && (scrollableViewHelper.isTop || isClickHead)) {
                        scrollBy(0, Math.round(moveY))
                    }
                }
            }

            MotionEvent.ACTION_UP -> {

                //为什么松手的时候，要考虑执行scroller.fling()；调用scroller.fling()方法会执行computeScroll()方法吗
                //根据Scroller类解释可知，调用startScroll()、fling()方法之后，会不断的调用View的computeScroll()，根据computeScrollOffset()判断滑动动画是否结束
                //竖直方向滑动，已经滑动最大距离或者scrollableView处于顶部，避免松手之后
                //if (verticalScroll && (scrollableViewHelper.isTop() || isStick())) {
                //竖直滑动，手指往上滑动，并且没有固定 或者 往下滑动 scrollableView 顶部可见
                if (verticalScroll) {
                    //获取1000ms允许滑动的最大距离是 maximumFlingVelocity
                    velocityTracker!!.computeCurrentVelocity(1000, maximumFlingVelocity.toFloat())
                    //获取当前的速度
                    val yVelocity = velocityTracker!!.yVelocity.toInt()
                    Log.i(mTAG, "yVelocity::$yVelocity")

                    mDirection = if (yVelocity > 0) DIRECTION_DOWN else DIRECTION_UP  //下滑速度大于0，上滑速度小于0
                    Log.i(mTAG, "mDirection::$mDirection")

                    //添加这样的判断，减少不必要的invalidate(),同时往下滑动时  先停止才能将headerView 拉下来
                    if (mDirection == DIRECTION_UP && (scrollableViewHelper.isTop || !isStick) || mDirection == DIRECTION_DOWN && scrollableViewHelper.isTop && !isTop) {
                        Log.i(mTAG, "调用scroller.fling()")

                        scroller!!.fling(0, scrollY, 0, -yVelocity,
                                Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE)

                        lastScrollY = scrollY
                        invalidate()

                        //防止快速滑动时，点击事件发生，UP的时候，则将事件改为ACTION_CANCEL，就不会发生点击事件了；
                        if (diffX > scaledTouchSlop || diffY > scaledTouchSlop) {
                            val action = ev.action
                            ev.action = MotionEvent.ACTION_CANCEL
                            val dd = super.dispatchTouchEvent(ev)
                            ev.action = action
                            return dd
                        }
                    }
                }
                resetVelocityTracker()
            }

            MotionEvent.ACTION_CANCEL -> resetVelocityTracker()
            else -> {
            }
        }

        //手动将事件传递给子View,让子View处理
        super.dispatchTouchEvent(ev)

        //返回true 表示事件被当前View或者其子View消费
        return true
    }


    /**
     * 父容器调用request()方法，获取getScrollX(),getScrollY()时调用
     * 如何优化，减少不必要的 invalidate()，在UP事件中，增加判断逻辑，减少不必要的 scroller.fling()方法调用
     */
    override fun computeScroll() {
        super.computeScroll()
        //返回true 表示动画没有执行完成
        if (scroller!!.computeScrollOffset()) {
            Log.i(mTAG, "computeScroll")
            val mCurrY = scroller!!.currY
            Log.i(mTAG, "mCurrY::$mCurrY,,$headerViewHeight")
            //手指往上滑动
            if (mDirection == DIRECTION_UP) {
                //如果header已经固定，则scrollableView 按照惯性继续滑动
                if (isStick) {
                    //主要是将快速滚动时的速度对接起来，让布局看起来滚动连贯
                    val distance = scroller!!.finalY - mCurrY//除去布局滚动的距离，剩下的距离
                    val duration = scroller!!.duration - scroller!!.timePassed()//除去滚动消费的时间，剩余的事件；
                    val yVelocity = calculateVelocity()
                    Log.i(mTAG, "调用fling()")
                    //让ScrollableView fling起来
                    scrollableViewHelper.fling(yVelocity, distance, duration)
                    //同时停止外层滑动
                    scroller!!.abortAnimation()
                } else {
                    //由于Scroller是滑动辅助类，用于跟踪控件滑动轨迹，滑动依然依靠scrollTo(),同时调用invalidate()方法，会调用View的computeScroll();
                    scrollTo(0, mCurrY)
                    invalidate()
                }
                //手指往下滑动
            } else if (mDirection == DIRECTION_DOWN) {
                //可滑动的View处于顶部
                if (scrollableViewHelper.isTop) {
                    val diffScrollY = mCurrY - lastScrollY
                    scrollTo(0, scrollY + diffScrollY)
                    if (mScrollY <= minY) {
                        scroller!!.abortAnimation()
                        return
                    }
                }
                //向下滑动是，初始状态可能不在顶部，所以要一直重绘，让computeScroll()一直调用，确保代码进入if判断
                invalidate()
            }
            lastScrollY = mCurrY
        }
    }

    /**
     * 获取滑动的速度
     *
     * @return
     */
    private fun calculateVelocity(): Int {
        return if (scroller == null) {
            0
        } else scroller!!.currVelocity.toInt()
    }

    /**
     * 是否点击在Header
     * getY() 表示对于父容器top 的位置
     *
     * @return
     */
    private fun isClickHeader(downY: Float, mScrollY: Int) {
        isClickHead = downY.toInt() + mScrollY <= headerViewHeight
    }

    /**
     * 重写scrollBy 防止滑动越界
     *
     * @param x
     * @param y
     */
    override fun scrollBy(x: Int, y: Int) {
        val scrollY = scrollY
        var toY = scrollY + y
        if (toY >= maxY) {
            toY = maxY
        } else if (toY <= minY) {
            toY = minY
        }

        //如果滑动y之后，越界，则矫正y;
        super.scrollBy(x, toY - scrollY)
    }

    /**
     * 重写scrollTo 防止滑动越界
     *
     * @param x
     * @param y
     */
    override fun scrollTo(x: Int, y: Int) {
        var y = y
        if (y >= maxY) {
            y = maxY
        } else if (y <= minY) {
            y = minY
        }
        mScrollY = y
        super.scrollTo(x, y)
    }

    /**
     * 能否下拉刷新
     *
     * @return
     */
    fun canPtr(): Boolean {
        return isTop && scrollableViewHelper.isTop
    }

    private fun resetVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker!!.recycle()
            velocityTracker = null
        }
    }

    private fun obtainVelocityTracker(ev: MotionEvent) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker!!.addMovement(ev)
    }

    fun setViewPager(viewPager: CusViewPager) {
        this.viewPager = viewPager
    }

    fun setScrollableInterface(scrollableInterface: ScrollableInterface) {
        scrollableViewHelper.setScrollableView(scrollableInterface)
    }
}

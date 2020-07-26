package com.fly.basemodule.swipeback

import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.fly.basemodule.R
import java.lang.IllegalArgumentException

/**
 * 自定义View的构造函数 Kotlin的构造函数：https://blog.csdn.net/xlh1191860939/article/details/79412319
 */
class SwipeBackLayout : FrameLayout {

    //常量的定义
    companion object {
        /**
         * Minimum velocity that will be detected as a fling
         */
        public const val MIN_FLING_VELOCITY = 400//

        public const val DEFAULT_SCRIM_COLOR = -0x67000000;

        public const val FULL_ALPHA = 255

        /**
         * Edge flag indicating that the left edge should be affected.
         */
        public const val EDGE_LEFT = ViewDragHelper.EDGE_LEFT

        /**
         * Edge flag indicating that the right edge should be affected.
         */
        public const val EDGE_RIGHT = ViewDragHelper.EDGE_RIGHT

        /**
         * Edge flag indicating that the bottom edge should be affected.
         */
        public const val EDGE_BOTTOM = ViewDragHelper.EDGE_BOTTOM

        /**
         * Edge flag set indicating all edges should be affected.
         */
        public const val EDGE_ALL = EDGE_LEFT or EDGE_RIGHT or EDGE_BOTTOM

        /**
         * A view is not currently being dragged or animating as a result of a
         * fling/snap.
         */
        public const val STATE_IDLE = ViewDragHelper.STATE_IDLE

        /**
         * A view is currently being dragged. The position is currently changing as
         * a result of user input or simulated user input.
         */
        public const val STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING

        /**
         * A view is currently settling into place as a result of a fling or
         * predefined non-interactive motion.
         */
        public const val STATE_SETTLING = ViewDragHelper.STATE_SETTLING

        /**
         * Default threshold of scroll
         */
        private const val DEFAULT_SCROLL_THRESHOLD = 0.3f

        private const val OVER_SCROLL_DISTANCE: Int = 10

        private val EDGE_FLAGS = intArrayOf(EDGE_LEFT, EDGE_RIGHT, EDGE_BOTTOM, EDGE_ALL);
    }

    //默认值为左滑
    private var mEdgeFlag = EDGE_LEFT

    /**
     * Threshold of scroll, we will close the activity, when scrollPercent over
     * this value;
     */
    private var mScrollThreshold = DEFAULT_SCROLL_THRESHOLD

    private var mEnable = true
    private var mContentView: View? = null
    private var mDragHelper: ViewDragHelper
    private var mScrollPercent = 0F
    private var mContentLeft = 0
    private var mContentTop = 0

    private var mListeners: MutableList<SwipeListener>? = null
    private var mShadowLeft: Drawable? = null
    private var mShadowRight: Drawable? = null
    private var mShadowBottom: Drawable? = null

    private var mScrimOpacity: Float = 0F
    private var mScrimColor = DEFAULT_SCRIM_COLOR

    private var mInlayout = false
    private var mTemRect = Rect()
    /**
     * Edge being dragged
     */
    private var mTrackingEdge = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mDragHelper = ViewDragHelper.create(this, ViewDragCallback())
        //retrieveAttributes(attrs: AttributeSet)方法只接受非空参数attrs
        attrs?.let { retrieveAttributes(attrs, defStyleAttr) }
    }

    //用来接收xml文件中的自定义属性
    private fun retrieveAttributes(attrs: AttributeSet, defStyleAttr: Int) {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SwipeBackLayout)
        val edgeSize: Int = typedArray.getDimensionPixelSize(R.styleable.SwipeBackLayout_edge_size, -1)
        if (edgeSize > 0) {
            setEdgeSize(edgeSize)
        }

        val mode = EDGE_FLAGS[typedArray.getInt(R.styleable.SwipeBackLayout_edge_flag, 0)]
        setEdgeTrackingEnabled(mode)

        val shadowLeft = typedArray.getResourceId(R.styleable.SwipeBackLayout_shadow_left, R.drawable.shadow_left);
        val shadowRight = typedArray.getResourceId(R.styleable.SwipeBackLayout_shadow_right, R.drawable.shadow_right);
        val shadowBottom = typedArray.getResourceId(R.styleable.SwipeBackLayout_shadow_bottom, R.drawable.shadow_bottom);
        setShadow(shadowLeft, EDGE_LEFT)
        setShadow(shadowRight, EDGE_RIGHT)
        setShadow(shadowBottom, EDGE_BOTTOM)
        typedArray.recycle()

        val density = resources.displayMetrics.density
        val minVel = MIN_FLING_VELOCITY * density
        mDragHelper.minVelocity = minVel
        mDragHelper.setMaxVelocity(minVel * 2F)
    }


    /**
     * Sets the sensitivity of the NavigationLayout.
     *
     * @param context     The application context.
     * @param sensitivity value between 0 and 1, the final value for touchSlop =
     *                    ViewConfiguration.getScaledTouchSlop * (1 / s);
     */
    public fun setSensitivity(context: Context, sensitivity: Float) {
        mDragHelper.setSensitivity(context, sensitivity)
    }

    /**
     * Set up contentView which will be moved by user gesture
     *
     * @param view
     */
    public fun setContentView(view: View) {
        mContentView = view
    }

    public fun setEnableGesture(enable: Boolean) {
        mEnable = enable
    }

    /**
     * Enable edge tracking for the selected edges of the parent view. The
     * callback's
     * {@link ViewDragHelper.Callback#onEdgeTouched(int, int)}
     * and
     * {@link ViewDragHelper.Callback#onEdgeDragStarted(int, int)}
     * methods will only be invoked for edges for which edge tracking has been
     * enabled.
     *
     * @param edgeFlags Combination of edge flags describing the edges to watch
     * @see #EDGE_LEFT
     * @see #EDGE_RIGHT
     * @see #EDGE_BOTTOM
     */
    public fun setEdgeTrackingEnabled(edgeFlags: Int) {
        mEdgeFlag = edgeFlags
        mDragHelper.setEdgeTrackingEnabled(mEdgeFlag)
    }

    public fun setScrimColor(color: Int) {
        mScrimColor = color
        invalidate()
    }

    private fun setEdgeSize(edgeSize: Int) {
        mDragHelper.edgeSize = edgeSize
    }

    /**
     * Add a callback to be invoked when a swipe event is sent to this view.
     *
     * @param listener the swipe listener to attach to this view
     */
    public fun addSwipListener(listener: SwipeListener) {
        if (mListeners == null) {
            mListeners = ArrayList()
        }
        mListeners?.add(listener)
    }

    public fun removeSwipeListener(listener: SwipeListener) {
        mListeners?.remove(listener)
    }

    /**
     * Set scroll threshold, we will close the activity, when scrollPercent over
     * this value
     *
     * @param threshold
     */
    public fun setScrollThresHold(threshold: Float) {
        if (threshold >= 1.0f || threshold <= 0) {
            throw IllegalArgumentException("Threshold value should be between 0 and 1.0")
        }
        mScrollThreshold = threshold
    }


    public fun setShadow(resId: Int, edgeFlag: Int) {
        setShadow(resources.getDrawable(resId), edgeFlag)
    }

    public fun setShadow(shadow: Drawable, edgeFlag: Int) {
        when {
            (edgeFlag and EDGE_LEFT) != 0 -> mShadowLeft = shadow
            (edgeFlag and EDGE_RIGHT) != 0 -> mShadowRight = shadow
            (edgeFlag and EDGE_BOTTOM) != 0 -> mShadowBottom = shadow
        }
        invalidate()
    }


    /**
     * Scroll out contentView and finish the activity
     *
     * 空处理：https://www.jianshu.com/p/983ff6490c00
     * kotlin 没有switch，用when代替
     */
    public fun scrollToFinishActivity() {
        val childWidth: Int = mContentView?.width ?: 0
        val childHeight: Int = mContentView?.height ?: 0

        var left = 0
        var top = 0

        when {
            (mEdgeFlag and EDGE_LEFT) != 0 -> {
                left = childWidth + (mShadowLeft?.intrinsicWidth ?: 0) + OVER_SCROLL_DISTANCE
                mTrackingEdge = EDGE_LEFT
            }

            (mEdgeFlag and EDGE_RIGHT) != 0 -> {
                left = -childWidth - (mShadowLeft?.intrinsicWidth ?: 0) - OVER_SCROLL_DISTANCE
                mTrackingEdge = EDGE_RIGHT
            }

            (mEdgeFlag and EDGE_BOTTOM) != 0 -> {
                top = -childHeight - (mShadowLeft?.intrinsicHeight ?: 0) - OVER_SCROLL_DISTANCE
                mTrackingEdge = EDGE_BOTTOM
            }
        }

        mDragHelper.smoothSlideViewTo(mContentView, left, top)
        invalidate()
    }


    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (!mEnable) {
            return false
        }
        return try {
            mDragHelper.shouldInterceptTouchEvent(ev)
        } catch (e: ArrayIndexOutOfBoundsException) {
            false
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!mEnable) {
            return false
        }
        mDragHelper.processTouchEvent(event)
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        mInlayout = true
        mContentView?.layout(mContentLeft, mContentTop,
                mContentLeft + (mContentView?.measuredWidth ?: 0),
                mContentTop + (mContentView?.measuredHeight ?: 0))
        mInlayout = false
    }

    override fun requestLayout() {
        if (!mInlayout) {
            super.requestLayout()
        }
    }


    override fun drawChild(canvas: Canvas?, child: View?, drawingTime: Long): Boolean {
        val drawContent: Boolean = child == mContentView
        var ret = super.drawChild(canvas, child, drawingTime)
        if (mScrimOpacity > 0 && drawContent && mDragHelper.viewDragState != ViewDragHelper.STATE_IDLE) {
            drawShadow(canvas, child)
            drawScrim(canvas, child)
        }
        return ret
    }

    private fun drawScrim(canvas: Canvas?, child: View?) {
        var baseAlpha = (mScrimColor and -0x1000000).ushr(24)
        var alpha: Int = (baseAlpha * mScrimOpacity).toInt()
        val color: Int = alpha shl 24 or (mScrimColor and 0xffffff)

        when {
            (mTrackingEdge and EDGE_LEFT) != 0 -> {
                canvas?.clipRect(0, 0, child?.left ?: 0, height)
            }

            (mTrackingEdge and EDGE_RIGHT) != 0 -> {
                canvas?.clipRect(child?.right ?: 0, 0, right, height)
            }

            (mTrackingEdge and EDGE_BOTTOM) != 0 -> {
                canvas?.clipRect(child?.left ?: 0, child?.bottom ?: 0, right, height)
            }
        }
        canvas?.drawColor(color)
    }

    private fun drawShadow(canvas: Canvas?, child: View?) {
        var childRect: Rect = mTemRect
        child?.getHitRect(childRect)

        if (mEdgeFlag and EDGE_LEFT != 0) {
            mShadowLeft?.setBounds(childRect.left - (mShadowLeft?.intrinsicWidth ?: 0),
                    childRect.top, childRect.right, childRect.bottom)
            mShadowLeft?.alpha = (mScrimOpacity * FULL_ALPHA).toInt()
            canvas?.let { mShadowLeft?.draw(it) }
        }

        if (mEdgeFlag and EDGE_RIGHT != 0) {
            mShadowRight?.setBounds(childRect.right, childRect.top,
                    childRect.right + (mShadowRight?.intrinsicWidth ?: 0), childRect.bottom)

            mShadowRight?.alpha = (mScrimOpacity * FULL_ALPHA).toInt()
            canvas?.let { mShadowRight?.draw(it) }
        }

        if (mEdgeFlag and EDGE_BOTTOM != 0) {
            mShadowBottom?.setBounds(childRect.left, childRect.bottom, childRect.right,
                    childRect.bottom + (mShadowBottom?.intrinsicHeight ?: 0))
            mShadowBottom?.alpha = (mScrimOpacity * FULL_ALPHA).toInt()
            canvas?.let { mShadowBottom?.draw(it) }
        }
    }

    public fun attachToActivity(act: Activity) {
        val int = null
        val a: TypedArray = act.theme.obtainStyledAttributes(intArrayOf(android.R.attr.windowBackground))
        val background: Int = a.getResourceId(0, 0)
        a.recycle()

        //强转
        val decor: ViewGroup = act.window.decorView as ViewGroup
        val decorChild: ViewGroup = decor.getChildAt(0) as ViewGroup
        decorChild.setBackgroundResource(background)
        decor.removeView(decorChild)
        addView(decorChild)
        //自己的方法，并不是Activity的setContentView()
        setContentView(decorChild)
        //addSwipListener(null)
        decor.addView(this)

    }


    /**
     * Kotlin 接口
     */
    public interface SwipeListener {
        /**
         * Invoke when state or scrollPercent changed
         *
         * @param state         flag to describe scroll state
         * @param scrollPercent scroll percent of this view
         * @see #STATE_IDLE
         * @see #STATE_DRAGGING
         * @see #STATE_SETTLING
         */
        public fun onScrollStateChange(state: Int, scrollPercent: Float)

        public fun onEdgeTouch(edgeFlag: Int)

        public fun onScrollOverThreshold();
    }

    public interface SwipeListenerEx : SwipeListener {
        public fun onContentViewSwipedBack()
    }

    //内部类
    private inner class ViewDragCallback : ViewDragHelper.Callback() {

        private var mIsScrollOverValid: Boolean = false

        /**
         * 只有返回true时才生效，
         */
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return true
        }

        /**
         * 状态变化 STATE_IDLE 闲置状态  STATE_DRAGGING 正在拖动  STATE_SETTLING 放置到某个位置
         */
        override fun onViewDragStateChanged(state: Int) {
            super.onViewDragStateChanged(state)

        }

        //当你拖动的View位置发生改变的时候回调
        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)

        }


        //捕获View的时候调用的方法
        override fun onViewCaptured(capturedChild: View, activePointerId: Int) {
            super.onViewCaptured(capturedChild, activePointerId)

        }

        //水平方向移动范围
        override fun getViewHorizontalDragRange(child: View): Int {
            return super.getViewHorizontalDragRange(child)

        }

        //竖直方向移动范围
        override fun getViewVerticalDragRange(child: View): Int {
            return super.getViewVerticalDragRange(child)

        }

        //水平拖拽的时候回调的方法
        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return super.clampViewPositionHorizontal(child, left, dx)

        }

        //竖直拖拽的时候回调的方法
        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return super.clampViewPositionVertical(child, top, dy)

        }


        /**
         *当View停止拖拽的时候调用的方法，一般在这个方法中重置一些参数，比如回弹什么的。。。
         */
        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            super.onViewReleased(releasedChild, xvel, yvel)

        }
    }
}

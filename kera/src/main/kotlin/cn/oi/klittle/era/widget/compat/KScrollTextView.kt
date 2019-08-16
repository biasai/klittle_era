package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.Scroller
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.utils.KStringUtils
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.singleLine


/**
 * fixme 可以滚动的文本框，原始的文本框，不具备滚动的能力（原始的setHorizontallyScrolling(true)也不行）。所以需要自定义。
 * fixme 没有滑动条哦。
 * fixme 默认就是可以垂直滚动。
 */
open class KScrollTextView : KTextView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//开启硬件加速,不然圆角没有效果
        initView()
    }

    //fixme 在computeScroll()里面计算最大值。
    var maxScrollY = -1//自定义最大滑动值(为0不具备滑动能力)，如果为-1。会根据文本高度自动去计算（自动计算，文本高度必须大于控件高度才会垂直滑动，且最大值不会超过文本高度）。
    var minScrollY = 0//自定义最小滑动值。
    var maxScrollX = -1//最大滑动值X
    var minScrollX = 0//最小滑动值X
    var mTouchSlop: Int = 0//滑动数值大于这个数值，才有效；
    var openScrollVertical = true//fixme 是否开启垂直滑动(默认开启垂直滑动,垂直滑动会自动计算文本的高)
    private var openScrollHorizontal = false//是否开启水平滑动;默认就是关闭的。建议调用 setHorizontallyScrolling(true)方法。


    //fixme 现在设置水平滚动是有效果的。不过水平滚动，只能有一行。
    override fun setHorizontallyScrolling(whether: Boolean) {
        super.setHorizontallyScrolling(whether)
        if (whether) {
            openScrollHorizontal = true//必须开启水平滚动才有效，水平滚动不是系统的，而是自己实现的。
            openScrollVertical = false//垂直就必须关掉。不然效果极差。
            maxLines = 1//只能设置成1，设置成其他的也无效。
            setLines(1)//这里设置为1，是为了以防万一。设置了水平滚动之后。默认就只能是一行。
            text?.toString()?.let {
                setText(KStringUtils.removeEnter(it))//fixme 去除换行符；如果文本里面有换行符的话，必须去除；不然依旧无法在一行里面显示。
            }
        } else {
            openScrollHorizontal = false
        }
    }

    private val INVALID_POINTER = -1
    private var mScroller: Scroller? = null
    private var mMinimumVelocity: Int = 0
    private var mMaximumVelocity: Int = 0
    private var mLastMotionY: Float = 0.toFloat()
    private var mLastMotionX: Float = 0.toFloat()
    private var mIsBeingDragged: Boolean = false
    private var mVelocityTracker: VelocityTracker? = null
    private var mActivePointerId = INVALID_POINTER

    private fun initView() {
        val cx = context
        //设置滚动减速器，在fling中会用到
        mScroller = Scroller(cx, DecelerateInterpolator(0.5f))
        val configuration = ViewConfiguration.get(cx)
        //mTouchSlop = configuration.getScaledTouchSlop()//默认设置为0即可。
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity()
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity()
    }

    override fun computeScroll() {
        super.computeScroll()
        val scroller = mScroller!!
        if (scroller.computeScrollOffset()) { //正在滚动，让view滚动到当前位置
            var scrollY = scroller.getCurrY()
            var scrollX = scroller.currX
            var toEdge = false
            if (!isScrollHorizontal && openScrollVertical) {
                var maxY: Int? = null
                //fixme 滑动最大Y值
                if (maxScrollY != -1) {
                    maxY = maxScrollY
                } else {
                    maxY = getTextScrollHeight()
                }
                toEdge = scrollY < minScrollY || scrollY > maxY
                //y值范围
                if (scrollY < minScrollY) {
                    scrollY = minScrollY
                } else if (scrollY > maxY) {
                    scrollY = maxY
                }
                //KLoggerUtils.e("最大值:\t" + maxY + "\tscrollY:\t" + scrollY + "\t文本高度:\t" + getTextLineHeight())
            }
            if (openScrollHorizontal) {
                var maxX: Int? = null
                if (maxScrollX != -1) {
                    maxX = maxScrollX
                } else {
                    maxX = getTextScrollWidth()
                }
                //x值范围
                if (scrollX < minScrollX) {
                    scrollX = minScrollX
                } else if (scrollX > maxX) {
                    scrollX = maxX
                }
            }
            //下面等同于：
            //mScrollY = scrollY
            //awakenScrollBars()//显示滚动条，必须在xml中配置。
            //postInvalidate()
            scrollTo(scrollX, scrollY)
            if (toEdge) {
                //移到两端，由于位置没有发生变化，导致滚动条不显示
                awakenScrollBars()
            }
        }
    }

    fun fling(velocityXY: Int) {
        var maxY: Int? = null
        var maxX: Int? = null
        //fixme 滑动最大Y值
        if (maxScrollY != -1) {
            maxY = maxScrollY
        } else {
            maxY = getTextScrollHeight()
        }
        if (maxScrollX != -1) {
            maxX = maxScrollX
        } else {
            maxX = getTextScrollWidth()
        }
        //fixme 滑动最小值
        if (isScrollHorizontal && openScrollHorizontal) {
            //水平滑动
            mScroller?.fling(scrollX, scrollY, velocityXY, 0, minScrollX, maxX, minScrollY,
                    Math.max(0, maxY))
        } else if (!isScrollHorizontal && openScrollVertical) {
            //垂直滑动
            mScroller?.fling(scrollX, scrollY, 0, velocityXY, minScrollX, maxX, minScrollY,
                    Math.max(0, maxY))
        }
        //刷新，让父控件调用computeScroll()
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null && (openScrollVertical || openScrollHorizontal)) {
            //事件处理方式：先自己处理后交给父类处理。
            //PS:方式不同，可能导致效果不同。请根据需求自行修改。
            var handled = false
            var contentHeight = lineCount * lineHeight
            //fixme 滑动最大Y值
            if (maxScrollY != -1) {
                contentHeight = maxScrollY
            }
            if (contentHeight > height || maxScrollY != -1 || openScrollHorizontal) {
                handled = processScroll(event)
            }
            return handled or super.onTouchEvent(event)
        } else {
            return super.onTouchEvent(event)
        }
    }

    private fun processScroll(ev: MotionEvent): Boolean {
        var handled = false
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker?.addMovement(ev) //帮助类，用来在fling时计算移动初速度
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mScroller?.let {
                    if (!it.isFinished) {
                        it.forceFinished(true)
                    }
                }
                mLastMotionX = ev.rawX
                mLastMotionY = ev.rawY
                mActivePointerId = ev.getPointerId(ev.actionIndex)
                mIsBeingDragged = true
                handled = true
            }
            MotionEvent.ACTION_MOVE -> {
                mActivePointerId = ev.getPointerId(ev.actionIndex)
                if (mIsBeingDragged && INVALID_POINTER != mActivePointerId) {
                    var deltaX = (mLastMotionX - ev.rawX).toInt()
                    var deltaY = (mLastMotionY - ev.rawY).toInt()
                    //KLoggerUtils.e("deltaX:\t" + deltaX + "\tdeltaY:\t" + deltaY + "\tx:\t" + x + "\ty:\t" + y + "\trawX:\t" + ev.rawX)
                    if (mTouchSlop <= 0 || Math.abs(deltaY) > mTouchSlop || Math.abs(deltaX) > mTouchSlop) { //移动距离(正负代表方向)必须大于ViewConfiguration设置的默认值
                        mLastMotionX = ev.rawX
                        mLastMotionY = ev.rawY
                        //默认滚动时间为250ms，建议立即滚动，否则滚动效果不明显
                        //或者直接使用scrollBy(0, deltaY);
                        if (isScrollHorizontal && openScrollHorizontal) {
                            //水平滑动
                            mScroller?.startScroll(scrollX, scrollY, deltaX, 0, 0)
                        } else if (!isScrollHorizontal && openScrollVertical) {
                            //垂直滑动
                            mScroller?.startScroll(scrollX, scrollY, 0, deltaY, 0)
                        }
                        invalidate()
                        handled = true
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (mIsBeingDragged && INVALID_POINTER != mActivePointerId) {
                    mVelocityTracker?.computeCurrentVelocity(700, mMaximumVelocity.toFloat())
                    if (!isScrollHorizontal && openScrollVertical) {
                        val initialVelocity = mVelocityTracker?.getYVelocity(mActivePointerId)!!.toInt()
                        if (Math.abs(initialVelocity) > mMinimumVelocity) {
                            fling(-initialVelocity)
                        }
                    } else if (isScrollHorizontal && openScrollHorizontal) {
                        val initialVelocitx = mVelocityTracker?.getXVelocity(mActivePointerId)!!.toInt()
                        if (Math.abs(initialVelocitx) > mMinimumVelocity) {
                            fling(-initialVelocitx)
                        }
                    }
                    mActivePointerId = INVALID_POINTER
                    mIsBeingDragged = false
                    if (mVelocityTracker != null) {
                        mVelocityTracker?.recycle()
                        mVelocityTracker = null
                    }
                    handled = true
                }
            }
        }
        return handled
    }

}
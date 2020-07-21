package cn.oi.klittle.era.widget.gamepad

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.exception.KCatchException
import cn.oi.klittle.era.utils.KGlideUtils.getBitmapFromAssets
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.utils.KLoggerUtils.e
import cn.oi.klittle.era.utils.KVibratorUtils
import cn.oi.klittle.era.widget.gamepad.entiy.KOrientation
import cn.oi.klittle.era.widget.gamepad.listener.JoystickListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

//                fixme Activity里的统一摇杆事件是重写：dispatchGenericMotionEvent(ev: MotionEvent?) 即可。
//                fixme 使用案例
//                kJoystickView {
//                    isVibraEnable = false//是否开启按键震动(默认是关闭的)。
//                    orientation {
//                        center {
//                            //手指离开的时候，一定会回调该方法。
//                            KLoggerUtils.e("正中间（原点）" + "\tx:\t" + x + "\ty:\t" + y)
//                        }
//                        left {
//                            KLoggerUtils.e("左（后）" + "\tx:\t" + x + "\ty:\t" + y)
//                        }
//                        top {
//                            KLoggerUtils.e("上" + "\tx:\t" + x + "\ty:\t" + y)
//                        }
//                        right {
//                            KLoggerUtils.e("右（前）" + "\tx:\t" + x + "\ty:\t" + y)
//                        }
//                        bottom {
//                            KLoggerUtils.e("下" + "\tx:\t" + x + "\ty:\t" + y)
//                        }
//                        one_right_top {
//                            KLoggerUtils.e("第一象限（右上角）" + "\tx:\t" + x + "\ty:\t" + y)
//                        }
//                        two_left_top {
//                            KLoggerUtils.e("第二象限（左上角）" + "\tx:\t" + x + "\ty:\t" + y)
//                        }
//                        three_left_bottom {
//                            KLoggerUtils.e("第三象限（左下角）" + "\tx:\t" + x + "\ty:\t" + y)
//                        }
//                        four_right_bottom {
//                            KLoggerUtils.e("第四象限（右下角）" + "\tx:\t" + x + "\ty:\t" + y)
//                        }
//                    }
//                }.lparams {
//                    width = kpx.x(300)
//                    height = width
//                    alignParentLeft()
//                    alignParentBottom()
//                }

/**
 * 滚珠方向键
 * 注意：底盘的宽度和高度占自身控件宽度和高度的2/3(居中),滚珠占1/3,且滚珠的滚动无法超过控件本身的宽度和高度(初始居中)
 * 控件的宽度和高度相等，且以较小的那个为标准。
 *
 * @author 彭治铭
 */
open class KJoystickView : View {

    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    //fixme 底盘
    var mJoystickBG: Bitmap? = null

    //fixme 滚珠
    var mJoystickRock: Bitmap? = null

    //fixme 底盘半径(layout（）方法里会自动计算)
    var mOuterRadius = 0.0

    //fixme 滚珠半径
    var mInnerRadius = 0.0

    private val mPaint: Paint = Paint()

    //控件的宽带
    private var mWidth = 0

    //控件的高度
    private var mHeight = 0

    //中心坐标X
    private var mCenterX = 0.0

    //中心坐标Y
    private var mCenterY = 0.0

    //滚珠圆心X
    private var mRockCenterX = 0.0

    //滚珠圆心Y
    private var mRockCenterY = 0.0

    private var mIsMotion = false
    var orientation: KOrientation? = null

    //fixme 滚珠事件分发，方向回调；
    fun orientation(block: KOrientation.() -> Unit): KJoystickView {
        if (orientation == null) {
            orientation = KOrientation()
        }
        orientation?.let {
            block(it)
        }
        return this
    }

    var orientation_offset = 0.31f//fixme 区域范围偏移量；0.31挺合适的。
    /**
     * fixme 方向回调;只有方向发生改变时（或者手指按下和离开时），才会回调。即：相同的方向不会重复回调。
     */
    private fun orientation(X: Float, Y: Float, ACTION: Int) {
        if (orientation == null) {
            return
        }
        var isRepeat = false
        if (ACTION == MotionEvent.ACTION_DOWN || ACTION == MotionEvent.ACTION_UP) {
            isRepeat = true//手指按下和离开时允许重复回调，防止手指离开时没有回调。
        }
        orientation?.let {
            //fixme x,y 都在（-1，0，1）范围内。
            it.x = X
            it.y = Y
            it.action = ACTION
        }
        when (ACTION) {
            MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (X <= orientation_offset && X >= -orientation_offset
                        && Y <= orientation_offset && Y >= -orientation_offset) {
                    //原点
                    orientation?.let {
                        if (it.isSameOrientation(it.orientation_center, true)) {
                            if (!isRepeat) {
                                return
                            }
                        }
                    }
                    orientation?.center?.let {
                        it()
                    }
                    return
                }
                if (X > 0 && Y <= orientation_offset && Y >= -orientation_offset) {
                    //正向右（前面）；一般都是操作向前，所以向前的判断放在第一位判断。
                    orientation?.let {
                        if (it.isSameOrientation(it.orientation_right, true)) {
                            if (!isRepeat) {
                                return
                            }
                        }
                    }
                    orientation?.right?.let {
                        it()
                    }
                    return
                }
                if (X < 0
                        && Y <= orientation_offset && Y >= -orientation_offset) {
                    //正向左（后面）
                    orientation?.let {
                        if (it.isSameOrientation(it.orientation_left, true)) {
                            if (!isRepeat) {
                                return
                            }
                        }
                    }
                    orientation?.left?.let {
                        it()
                    }
                    return
                }
                if (X <= orientation_offset && X >= -orientation_offset
                        && Y < 0) {
                    //正向上
                    orientation?.let {
                        if (it.isSameOrientation(it.orientation_top, true)) {
                            if (!isRepeat) {
                                return
                            }
                        }
                    }
                    orientation?.top?.let {
                        it()
                    }
                    return
                }
                if (X <= orientation_offset && X >= -orientation_offset
                        && Y > 0) {
                    //正向下
                    orientation?.let {
                        if (it.isSameOrientation(it.orientation_bottom, true)) {
                            if (!isRepeat) {
                                return
                            }
                        }
                    }
                    orientation?.bottom?.let {
                        it()
                    }
                    return
                }
                if (X > 0 && Y < 0) {
                    //第一限象
                    orientation?.let {
                        if (it.isSameOrientation(it.orientation_one_right_top, true)) {
                            if (!isRepeat) {
                                return
                            }
                        }
                    }
                    orientation?.one_right_top?.let {
                        it()
                    }
                    return
                }
                if (X < 0 && Y < 0) {
                    //第二限象
                    orientation?.let {
                        if (it.isSameOrientation(it.orientation_two_left_top, true)) {
                            if (!isRepeat) {
                                return
                            }
                        }
                    }
                    orientation?.two_left_top?.let {
                        it()
                    }
                    return
                }
                if (X < 0 && Y > 0) {
                    //第三限象
                    orientation?.let {
                        if (it.isSameOrientation(it.orientation_three_left_bottom, true)) {
                            if (!isRepeat) {
                                return
                            }
                        }
                    }
                    orientation?.three_left_bottom?.let {
                        it()
                    }
                    return
                }
                if (X > 0 && Y > 0) {
                    //第四限象
                    orientation?.let {
                        if (it.isSameOrientation(it.orientation_four_right_bottom, true)) {
                            if (!isRepeat) {
                                return
                            }
                        }
                    }
                    orientation?.four_right_bottom?.let {
                        it()
                    }
                    return
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                //fixme 手指离开的时候，触发原点回调。
                orientation?.let {
                    if (it.isSameOrientation(it.orientation_center, true)) {
                        if (!isRepeat) {
                            return
                        }
                    }
                }
                orientation?.center?.let {
                    it()
                }
            }
        }
    }

    private var isInitImg = false//图片是否正在初始化。
    override fun layout(left: Int, top: Int, right: Int, bottom: Int) {
        try {
            if (null == mJoystickBG && (right - left) > 0 && (bottom - top) > 0 && !isInitImg) {
                isInitImg = true
                mWidth = right - left
                mHeight = bottom - top
                mWidth = mWidth - 2
                mHeight = mHeight - 2
                //mOuterRadius = mWidth / 3.toDouble()
                //mInnerRadius = mWidth / 6.toDouble()
                mOuterRadius = mWidth / 2.5.toDouble()
                mInnerRadius = mWidth / 5.toDouble()
                mCenterX = mWidth / 2.toDouble()
                mCenterY = mHeight / 2.toDouble()
                mRockCenterX = mCenterX
                mRockCenterY = mCenterY
                GlobalScope.async {
                    //fixme 滚珠图片
                    val overrideWidth = (mInnerRadius * 2).toInt()
                    mJoystickRock = getBitmapFromAssets("kera/gamepad/kera_glass_ball.png", overrideWidth, overrideWidth, 1f, true)
                    val overrideWidth_out = (mOuterRadius * 2).toInt()
                    //fixme 滚珠底盘图片
                    mJoystickBG = getBitmapFromAssets("kera/gamepad/kera_glass_plate.png", overrideWidth_out, overrideWidth_out, 1f, true)
                    isInitImg = false
                    postInvalidate()
                }
            }
            super.layout(left, top, right, bottom)
        } catch (e: Exception) {
            e("滚珠布局layout异常：\t" + KCatchException.getExceptionMsg(e), true)
        }
    }

    override fun draw(canvas: Canvas) {
        try {
            super.draw(canvas)
            // 底盘(固定不变，画在画布中心)
            mJoystickBG?.let {
                if (!it.isRecycled) {
                    canvas.drawBitmap(it,
                            Math.round(mCenterX - it!!.width / 2 + paddingLeft).toFloat(),
                            Math.round(mCenterY - it!!.height / 2 + paddingTop).toFloat(), mPaint)
                }
            }
            // 滚珠
            mJoystickRock?.let {
                if (!it.isRecycled) {
                    canvas.drawBitmap(it,
                            Math.round(mRockCenterX - mInnerRadius + paddingLeft).toFloat(),
                            Math.round(mRockCenterY - mInnerRadius + paddingTop).toFloat(), mPaint)
                }
            }
        } catch (e: Exception) {
            e("滚珠draw()异常:\t" + KCatchException.getExceptionMsg(e), true)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        try {
            val x = event.x - paddingLeft
            val y = event.y - paddingTop
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_POINTER_DOWN -> return false
                MotionEvent.ACTION_DOWN -> {
                    if (Math.sqrt(Math.pow(mCenterX - x, 2.0)
                                    + Math.pow(mCenterY - y, 2.0)) >= width - mInnerRadius) {//mOuterRadius 改成->width-mInnerRadius
                        return false
                    }
                    mIsMotion = true
                    if (Math.sqrt(Math.pow(mCenterX - x, 2.0)
                                    + Math.pow(mCenterY - y, 2.0)) < width / 2 - mInnerRadius) {//mWidth / 4 改成-> width/2-mInnerRadius
                        mRockCenterX = x.toDouble()
                        mRockCenterY = y.toDouble()
                    } else {
                        val rad = getRad(mCenterX, mCenterY, x, y)
                        getXY(mCenterX, mCenterY, width / 2.toDouble() - mInnerRadius, rad)// mWidth / 4.toDouble() 改成-> width/2.toDouble()-mInnerRadius
                    }
                    //事件传递
                    //MotionTask().start()
                    motionTask(event.action)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (Math.sqrt(Math.pow(mCenterX - x, 2.0)
                                    + Math.pow(mCenterY - y, 2.0)) < width / 2 - mInnerRadius) {//mWidth / 4 改成-> width/2-mInnerRadius
                        mRockCenterX = x.toDouble()
                        mRockCenterY = y.toDouble()
                    } else {
                        val rad = getRad(mCenterX, mCenterY, x, y)
                        getXY(mCenterX, mCenterY, width / 2.toDouble() - mInnerRadius, rad)// mWidth / 4.toDouble() 改成-> width/2.toDouble()-mInnerRadius
                    }
                    motionTask(event.action)
                }
                MotionEvent.ACTION_POINTER_UP -> return false
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    mRockCenterX = mCenterX
                    mRockCenterY = mCenterY
                    mIsMotion = false
                    motionTask(event.action)
                }
            }
            invalidate()
        } catch (e: Exception) {
            e("滚珠事件分发异常：\t" + KCatchException.getExceptionMsg(e), true)
        }
        return true
    }

    private fun getRad(px1: Double, py1: Double, px2: Float, py2: Float): Double {
        val x = px2 - px1
        val y = py2 - py1
        val xx = Math.sqrt(Math.pow(x, 2.0) + Math.pow(y, 2.0))
        val cosA = x / xx
        val rad = Math.acos(cosA)
        return if (py2 < py1) -rad else rad
    }

    private fun getXY(centerX: Double, centerY: Double, radius: Double, rad: Double) {
        mRockCenterX = radius * Math.cos(rad) + mCenterX
        mRockCenterY = radius * Math.sin(rad) + mCenterY
    }

    var isVibraEnable = false//fixme 是否开启按键震动；默认不开启。(操作太频繁，建议还是关闭)

    private fun motionTask(ACTION: Int) {
        if (orientation == null) {
            return
        }
        try {
            if (isVibraEnable) {
                //震动音效
                KVibratorUtils.Vibrate(context as Activity, 200)
            }
            if (mIsMotion) {
                var x = (mRockCenterX - mCenterX).toFloat() / (width / 2).toFloat()//(mWidth / 4) 改成 -> (width / 2)
                var y = (mRockCenterY - mCenterY).toFloat() / (width / 2).toFloat()//(mWidth / 4) 改成 -> (width / 2)
                if (x > 0.999) {
                    x = 1f
                } else if (x < -0.999) {
                    x = -1f
                } else if (x < 0.033 && x > -0.033) {
                    x = 0f
                }
                if (y > 0.999) {
                    y = 1f
                } else if (y < -0.999) {
                    y = -1f
                } else if (y < 0.033 && y > -0.033) {
                    y = 0f
                }
                orientation(x, y, ACTION)
            } else {
                orientation(0f, 0f, ACTION)
            }
        } catch (e: Exception) {
            e("滚珠事件分发异常：\t" + KCatchException.getExceptionMsg(e), true)
        }
    }

    //fixme 销毁
    fun onDestroy() {
        try {
            mJoystickBG?.let {
                if (!it.isRecycled) {
                    it.recycle()
                }
            }
            mJoystickBG = null
            mJoystickRock?.let {
                if (!it.isRecycled) {
                    it.recycle()
                }
            }
            mJoystickRock = null
            orientation?.destroy()
            orientation = null
            setOnFocusChangeListener(null)
            setOnClickListener(null)
            setOnLongClickListener(null)
            setOnTouchListener(null)
            clearAnimation()
            clearFocus()
        } catch (e: Exception) {
            KLoggerUtils.e("KJoystickView销毁异常：\t" + KCatchException.getExceptionMsg(e), true)
        }
    }
}
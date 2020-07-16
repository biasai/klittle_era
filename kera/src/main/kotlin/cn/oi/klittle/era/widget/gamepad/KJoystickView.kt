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
import cn.oi.klittle.era.widget.gamepad.listener.JoystickListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

//                fixme Activity里的统一摇杆事件是重写：dispatchGenericMotionEvent(ev: MotionEvent?) 即可。
//                fixme 使用案例
//                kJoystickView {
//                    setJoystickListener {
//                        var event=it
//                        when(event.action){
//                            MotionEvent.ACTION_POINTER_DOWN,MotionEvent.ACTION_DOWN,MotionEvent.ACTION_MOVE->{
//                                if (event.getX() < 0 && event.getY() == 0f) {
//                                    //正向左
//                                }
//                                if (event.getX() > 0 && event.getY() == 0f) {
//                                    //正向右
//                                }
//                                if (event.getX() == 0f && event.getY() < 0) {
//                                   //正向上
//                                }
//                                if (event.getX() == 0f && event.getY() > 0) {
//                                    //正向下
//                                }
//                                if (event.getX() > 0 && event.getY() < 0) {
//                                    //第一限象
//                                }
//                                if (event.getX() < 0 && event.getY() < 0) {
//                                    //第二限象
//                                }
//                                if (event.getX() < 0 && event.getY() > 0) {
//                                    //第三限象
//                                }
//                                if (event.getX() > 0 && event.getY() > 0) {
//                                    //第四限象
//                                }
//                            }
//                        }
//                    }
//                }.lparams {
//                    width = kpx.x(400)
//                    height = width
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
    private var mJoystickListener: JoystickListener? = null
    private var mCallback: ((event: MotionEvent) -> Unit)? = null

    //设置滚珠监听事件
    //setJoystickListener(cn.oi.klittle.era.widget.gamepad.listener.JoystickListener(){})
    fun setJoystickListener(joystickListener: JoystickListener?) {
        mJoystickListener = joystickListener
    }

    fun setJoystickListener(callback: ((event: MotionEvent) -> Unit)?) {
        if (callback != null) {
            this.mCallback = callback
            setJoystickListener(cn.oi.klittle.era.widget.gamepad.listener.JoystickListener() { event ->
                mCallback?.let {
                    try {
                        it(event)
                    } catch (e: Exception) {
                        KLoggerUtils.e("setJoystickListener()滚轮回调异常：\t" + KCatchException.getExceptionMsg(e), true)
                    }
                }
            })
        } else {
            mJoystickListener = null
            mCallback = null
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
                mOuterRadius = mWidth / 3.toDouble()
                mInnerRadius = mWidth / 6.toDouble()
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
                                    + Math.pow(mCenterY - y, 2.0)) > mOuterRadius) {
                        return false
                    }
                    mIsMotion = true
                    mRockCenterX = x.toDouble()
                    mRockCenterY = y.toDouble()
                    //事件传递
                    MotionTask().start()
                }
                MotionEvent.ACTION_MOVE -> if (Math.sqrt(Math.pow(mCenterX - x, 2.0)
                                + Math.pow(mCenterY - y, 2.0)) < mWidth / 4) {
                    mRockCenterX = x.toDouble()
                    mRockCenterY = y.toDouble()
                } else {
                    val rad = getRad(mCenterX, mCenterY, x, y)
                    getXY(mCenterX, mCenterY, mWidth / 4.toDouble(), rad)
                }
                MotionEvent.ACTION_POINTER_UP -> return false
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    mRockCenterX = mCenterX
                    mRockCenterY = mCenterY
                    mIsMotion = false
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

    private inner class MotionTask : Thread() {
        override fun run() {
            try {
                //震动音效
                KVibratorUtils.Vibrate(context as Activity, 200)
                while (mIsMotion) {
                    if (null != mJoystickListener) {
                        var x = (mRockCenterX - mCenterX).toFloat() / (mWidth / 4).toFloat()
                        var y = (mRockCenterY - mCenterY).toFloat() / (mWidth / 4).toFloat()
//                        x = x > 0.999 ? 1 : x;
//                        x = x < -0.999 ? -1 : x;
//                        y = y > 0.999 ? 1 : y;
//                        y = y < -0.999 ? -1 : y;
//                        x = x < 0.033 && x > -0.033 ? 0 : x;
//                        y = y < 0.033 && y > -0.033 ? 0 : y;
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
                        //x = if (x > 0.999) 1 else x
                        //x = if (x < -0.999) -1 else x
                        //y = if (y > 0.999) 1 else y
                        //y = if (y < -0.999) -1 else y
                        //x = if (x < 0.033 && x > -0.033) 0 else x
                        //y = if (y < 0.033 && y > -0.033) 0 else y
                        //Log.e("test", "mRockCenterX:\t" + mRockCenterX + "\tmCenterX:\t" + mCenterX + "\tmWidth:\t" + mWidth + "\tx:\t" + x + "\ty:\t" + y);
                        val event = MotionEvent.obtain(
                                SystemClock.uptimeMillis(),
                                SystemClock.uptimeMillis(),
                                MotionEvent.ACTION_MOVE, x, y, 0f, 0f, 0, 0f, 0f, 1, 0) //倒数第二个参数是 设备ID.设备id最好大于等于1,不用小于等于0.不灵
                        mJoystickListener!!.onJoystikMotionEvent(event)
                        event.recycle()
                        try {
                            sleep(300)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    }
                }
                if (mJoystickListener != null) {
                    //循环之外，恢复原状
                    var event2 = MotionEvent.obtain(
                            SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_MOVE, 0f, 0f, 0f, 0f, 0, 0f, 0f, 1, 0)
                    mJoystickListener?.onJoystikMotionEvent(event2)
                    event2.recycle()
                }
            } catch (e: Exception) {
                e("滚珠异常：\t" + KCatchException.getExceptionMsg(e), true)
            }
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
            mJoystickListener = null
            mCallback = null
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
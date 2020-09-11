package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import cn.oi.klittle.era.entity.widget.compat.KTouchScaleEntity
import cn.oi.klittle.era.window.KWindow
import org.jetbrains.anko.alignParentLeft
import org.jetbrains.anko.alignParentTop


//                   fixme 调用案例
//                ktouchScaleMotionEventView {
//                    backgroundColor(Color.YELLOW)
//                    //fixme 触摸缩放
//                    touchScale {
//                        isTouchScaleEnable = true//是否开启缩放功能
//                        scaleMaxWidth = 1000//最大缩放宽度(高度会保持和宽度的比例)
//                        scaleMinWidth = 100//最新缩放宽度
//
//                        //fixme 不是对整个控件进行旋转，而是对整个画布进行旋转。是画布，不是控件
//                        // fixme(缩放和旋转同时开启，旋转会很抖动，效果十分不好。）无法解决抖动的问题，所以目前只能对画布进行旋转。
//                        //fixme 所以旋转和缩放最好只开一个。
//                        isRotationEnable = true//是否开启旋转；
//                        rotation = 30f//旋转角度，isRotationEnable为true才有效。
//                    }
//                }.lparams {
//                    width=kpx.x(200)
//                    height=width
//                }

/**
 * fixme 触摸缩放控件; 双指缩放; 单指拖动(指的是KDragMotionEventView拖动控件);KDragMotionEventView拖动控件继承了该触摸控件。
 */
open class KTouchScaleMotionEventView : KTextView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//开启硬件加速,不然圆角没有效果
        clearBackground()
        //去除按钮原有阴影
        clearButonShadow()
    }


    private var touchScale: KTouchScaleEntity? = null
    private fun getTouchScale(): KTouchScaleEntity {
        if (touchScale == null) {
            touchScale = KTouchScaleEntity()
        }
        return touchScale!!
    }

    fun touchScale(block: KTouchScaleEntity.() -> Unit): KTouchScaleMotionEventView {
        block(getTouchScale())
        touchScale?.let {
            mCanvasRotation = it.rotation//角度统一
        }
        return this
    }

    private var b: Boolean = false
    private var startDistanceX = 0f
    private var startDistanceY = 0F
    private var startDistance = 0f
    private var distanceX = 0f
    private var distanceY = 0F
    private var distance = 0f
    private var downWidth = 0
    private var downHeight = 0
    private var downLeftMargin = 0
    private var downTopMargin = 0
    private var downCenterX = 0
    private var downCenterY = 0
    private var downP = 1F
    private var scaleWidth = 0
    private var scaleHeight = 0
    private var scaleLeftMargin = 0
    private var scaleTopMargin = 0
    var isTouchScale = false
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        b = super.dispatchTouchEvent(event)
        event?.let {
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    isTouchScale = false//拖动和缩放不能同时进行；fixme 不然会位置会异常；很难处理。
                }
            }
        }
        touchScale?.let {
            //旋转
            if (touchScale!!.isRotationEnable) {
                event?.let {
                    when (event.action and MotionEvent.ACTION_MASK) {
                        MotionEvent.ACTION_POINTER_DOWN -> {
                            //第二个或第N个手指按下
                            if (event.pointerCount == 2) {
                                mLastVector = getPointF(event)
                            }
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (event.pointerCount >= 2) {
                                isTouchScale = true
                                if (mLastVector == null) {
                                    mLastVector = getPointF(event)
                                }
                                vector = getPointF(event)
                                if (mCanvasRotation != touchScale!!.rotation) {
                                    touchScale!!.rotation = mCanvasRotation//角度统一；亲测没问题。
                                }
                                touchScale!!.rotation += calculateDeltaDegree(mLastVector!!, vector!!)//fixme 加上变化的角度
                                if (mCanvasRotation != touchScale!!.rotation) {
                                    mCanvasRotation = touchScale!!.rotation
                                    //开启旋转
                                    this.pivotX = (getLayoutWidth() / 2).toFloat()
                                    this.pivotY = (getLayoutHeight() / 2).toFloat()
                                    //rotation= touchScale!!.rotation
                                    mLastVector = vector
                                    invalidate()//fixme 旋转只对画布进行旋转；不是整个控件;如果是整个控件的话，会很抖动。效果极度不好。
                                }
                            }
                        }
                        MotionEvent.ACTION_UP -> {
                            isTouchScale = false
                            if (!isTouchScale) {
                                mLeftMargin = left
                                mTopMargin = top
                            }
                        }
                    }
                }
            }

            //缩放(不要和旋转同时进行，很抖动)
            if (it.isTouchScaleEnable) {
                event?.let {
                    when (event.action and MotionEvent.ACTION_MASK) {
                        MotionEvent.ACTION_DOWN -> {
                            //手指按下（第一个手指）
                            downWidth = getLayoutWidth()
                            downHeight = getLayoutHeight()
                            downP = downHeight.toFloat() / downWidth.toFloat()
                            downLeftMargin = left
                            downTopMargin = top
                            //在margin()方法里有重新赋值。
                            downCenterX = left + downWidth / 2
                            downCenterY = top + downHeight / 2
                        }
                        MotionEvent.ACTION_POINTER_DOWN -> {
                            //第二个或第N个手指按下
                            if (event.pointerCount == 2) {
                                downWidth = getLayoutWidth()
                                downHeight = getLayoutHeight()
                                startDistanceX = Math.abs(event.getX(0) - event.getX(1))
                                startDistanceY = Math.abs(event.getY(0) - event.getY(1))
                                startDistance = Math.sqrt(startDistanceX.toDouble() * startDistanceX + startDistanceY * startDistanceY).toFloat();
                            }
                        }
                        MotionEvent.ACTION_MOVE -> {
                            //手指移动
                            if (event.pointerCount >= 2) {
                                isTouchScale = true
                                //两根手指以上才会有缩放效果
                                distanceX = Math.abs(event.getX(0) - event.getX(1))
                                distanceY = Math.abs(event.getY(0) - event.getY(1))
                                distance = Math.sqrt(distanceX.toDouble() * distanceX + distanceY * distanceY).toFloat()
                                distance = distance - startDistance
                                scaleWidth = downWidth + distance.toInt()
                                touchScale?.scaleMaxWidth?.let {
                                    if (it > 0 && scaleWidth > it) {
                                        scaleWidth = it//最大缩放值
                                    }
                                }
                                touchScale?.scaleMinWidth?.let {
                                    if (it > 0 && scaleWidth < it) {
                                        scaleWidth = it//最小缩放值
                                    }
                                }
                                scaleHeight = (scaleWidth * downP).toInt()//宽高等比例缩放。
                                //KLoggerUtils.e("distance:\t" + distance + "\tdownCenterX:\t" + downCenterX + "\tdownCenterY:\t" + downCenterY)
                                scaleLeftMargin = downCenterX - scaleWidth / 2
                                scaleTopMargin = downCenterY - scaleHeight / 2
                                setLayoutParams(scaleWidth, scaleHeight, scaleLeftMargin, scaleTopMargin)
                            }
                        }
                        MotionEvent.ACTION_UP -> {
                            //手指移动离开（最后一个手指）
                            if (isTouchScale) {
                                mLeftMargin = scaleLeftMargin
                                mTopMargin = scaleTopMargin
                            }
                            isTouchScale = false
                        }
                        MotionEvent.ACTION_POINTER_UP -> {
                            //第n个手指离开，反正不是最后一个。
                            //如果有三根手指，第三根手指离开的时候，此时event.pointerCount等于3
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            //其他异常
                        }
                    }
                }
            }

        }
        return b
    }

    var mCanvasRotation = 0f//fixme 画布旋转角度。拖动setDragId()会保存这个值。
    override fun draw(canvas: Canvas?) {
        canvas?.let {
            touchScale?.let {
                if (mCanvasRotation == 0f && it.rotation != 0f) {
                    mCanvasRotation = touchScale!!.rotation//防止初始的时候无效。
                }
            }
            if (mCanvasRotation != 0f && touchScale != null && touchScale!!.isRotationEnable) {
                it.rotate(mCanvasRotation, getLayoutWidth() / 2f, getLayoutHeight() / 2F)
            }
        }
        super.draw(canvas)
    }

    //获取实时真实的宽度
    open fun getLayoutWidth(): Int {
        return right - left
    }

    //获取实时真实的高度
    open fun getLayoutHeight(): Int {
        return bottom - top
    }

    //记录左边和顶部的外部丁;fixme 在K3DragMotionEventWidget拖动控件里有用到.
    var mLeftMargin = 0//等价于left
    var mTopMargin = 0//等价于top

    open fun setLayoutParams(width: Float, height: Float, leftMargin: Float = left.toFloat(), topMargin: Float = top.toFloat()) {
        setLayoutParams(width.toInt(), height.toInt(), leftMargin.toInt(), topMargin.toInt())
    }

    open fun setLayoutParams(width: Int, height: Int, leftMargin: Int = left, topMargin: Int = top) {
        if (viewGroup == null) {
            viewGroup = this
        }
        viewGroup?.apply {
            if (width != getLayoutWidth() || height != getLayoutHeight()) {
                var w = width
                var h = height
                this@KTouchScaleMotionEventView.w = w//fixme 兼容阴影控件。
                this@KTouchScaleMotionEventView.h = h
                margin(leftMargin = leftMargin, topMargin = topMargin)
                viewGroup?.requestLayout()
            }
        }
    }

    //fixme 设置外补丁；只对父容器是相对布局(RelativeLayout)有效。正负补丁都有效果。
    fun margin(leftMargin: Int? = null, topMargin: Int? = null, rightMargin: Int? = null, bottomMargin: Int? = null) {
        if (viewGroup == null) {
            viewGroup = this
        }
        viewGroup?.layoutParams?.let {
            if (it is RelativeLayout.LayoutParams) {
                //相对布局；不要判断 it.leftMargin != leftMargin
                if (leftMargin != null) {
                    //fixme 只有重新赋值(哪怕数值是一样的，也要重新赋值，防止布局不刷新)；
                    //fixme 只有重新赋值了，requestLayout()才有刷新有效。（哪怕赋值和之前是一样的，也要重新赋值。不然不刷新）
                    it.leftMargin = leftMargin
                } else {
                    it.leftMargin = left
                }
                if (topMargin != null) {
                    it.topMargin = topMargin
                } else {
                    it.topMargin = top//fixme 恢复原有属性，防止异常。
                }
                if (rightMargin != null) {
                    it.rightMargin = rightMargin
                } else {
                    it.rightMargin = -getLayoutWidth()//fixme 设置外补定为负数，这样补定才能支持负数。防止移动到最右边的时候被挤压
                }
                if (bottomMargin != null) {
                    it.bottomMargin = bottomMargin//fixme 下补定设置成负数，防止移动到最底部的时候，被挤压。
                } else {
                    it.bottomMargin = -getLayoutHeight()
                }
                if (Build.VERSION.SDK_INT >= 17) {//API 17是4.2版本
                    //fixme 移除原有对齐属性
                    it.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    it.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                    it.removeRule(RelativeLayout.CENTER_IN_PARENT)
                    it.removeRule(RelativeLayout.CENTER_HORIZONTAL)
                    it.removeRule(RelativeLayout.CENTER_VERTICAL)
                    it.removeRule(RelativeLayout.ALIGN_LEFT)
                    it.removeRule(RelativeLayout.ALIGN_TOP)
                }
                it.alignParentLeft()//以左边为对齐标准
                it.alignParentTop()//以顶部为对齐标准;fixme 防止leftMargin和topMargin无效。所以设置对齐标准。
                viewGroup?.requestLayout()//fixme requestLayout()内部会自己判断，如果外补定，宽高都没有变化。布局是不会重新刷新的。
            } else if (it is WindowManager.LayoutParams) {
                if (leftMargin != null) {
                    it.x = leftMargin
                }
                if (topMargin != null) {
                    it.y = topMargin
                }
                KWindow.updateViewLayout(viewGroup)//布局更新，WindowManager布局刷新更新，不太流畅。
            } else {
            }
        }
    }

    /**
     * 获取PointF 向量
     */
    private fun getPointF(event: MotionEvent): PointF {
        if (event.pointerCount >= 2) {
            return PointF(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1))
        }
        return PointF(0F, 0F)
    }

    /**
     * 计算两个手指头之间的中心点的位置
     * x = (x1+x2)/2;
     * y = (y1+y2)/2;
     *
     * @param event 触摸事件
     * @return 返回中心点的坐标
     */
    fun midPoint(event: MotionEvent): PointF {
        if (event.pointerCount >= 2) {
            val x = (event.getX(0) + event.getX(1)) / 2
            val y = (event.getY(0) + event.getY(1)) / 2
            return PointF(x, y)
        }
        return PointF(0F, 0F)
    }

    /**
     * 计算两个手指间的距离
     *
     * @param event 触摸事件
     * @return 放回两个手指之间的距离
     */
    fun distance(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()//两点间距离公式
    }

    private var mLastVector: PointF? = null
    private var vector: PointF? = null

    /**
     * 计算两个向量之间的夹角
     *
     * @param lastVector 上一次两只手指形成的向量
     * @param vector     本次两只手指形成的向量
     * @return 返回手指旋转过的角度
     */
    private fun calculateDeltaDegree(lastVector: PointF, vector: PointF): Float {
        var lastDegree = Math.atan2(lastVector.y.toDouble(), lastVector.x.toDouble()).toFloat()
        var degree = Math.atan2(vector.y.toDouble(), vector.x.toDouble()).toFloat()
        var deltaDegree = degree - lastDegree
        var rotation = Math.toDegrees(deltaDegree.toDouble()).toFloat()
//        if (rotation > 10 || rotation < -10 || Math.abs(rotation) < 0.5) {
//            rotation = 0f
//        }
        return rotation
    }

    override fun onDestroy() {
        super.onDestroy()
        viewGroup = null
    }
}


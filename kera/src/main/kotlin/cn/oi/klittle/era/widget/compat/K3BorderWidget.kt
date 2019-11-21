package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.entity.widget.compat.KBorderEntity

//            调用案例
//            KView(this).apply {
//                backgroundColor(Color.GREEN)
//                border {
//                    strokeWidth = kpx.x(10f) //所有边框的宽度
//                    leftStrokeWidth = strokeWidth / 2//左边边框的宽度
//                    bottomStrokeWidth = kpx.x(2f)//底部边框的宽度
//                    leftStrokeColor = Color.BLUE//左边边框的颜色
//                    rightStrokeVerticalColors(Color.RED, Color.CYAN)//右边边框垂直渐变色
//                    bottomDashWidth = kpx.x(15f)//底部边框虚线
//                    bottomDashGap = kpx.x(10f)
//                }
//                border_press {
//                    isDrawLeft = false//是否绘制左边边框
//                    isDrawTop = false//时候绘制顶部边框
//                    strokeHorizontalColors(Color.RED, Color.CYAN, Color.GRAY)
//                    dashGap = kpx.x(30f)
//                    dashWidth = kpx.x(50f)
//                }
//            }.lparams {
//                topMargin = kpx.x(50)
//                width = kpx.x(300)
//                height = kpx.x(300)
//            }

/**
 * fixme 绘制上下左右的边框；不支持圆角。
 */
open class K3BorderWidget : K3AStateView {
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

    //初始化画笔
    private fun initBorderPaint(paint: Paint, border: KBorderEntity, oritaton: Int = 0) {
        border?.let {
            var dashWidth = it.dashWidth
            var dashGap = it.dashGap
            var strokeWidth = it.strokeWidth
            var strokeColor = it.strokeColor
            var strokeHorizontalColors = it.strokeHorizontalColors
            var strokeVerticalColors = it.strokeVerticalColors
            //左
            if (oritaton == 1) {
                it.leftDashWidth?.let {
                    dashWidth = it
                }
                it.leftDashGap?.let {
                    dashGap = it
                }
                it.leftStrokeWidth?.let {
                    strokeWidth = it
                }
                it.leftStrokeColor?.let {
                    strokeColor = it
                }
                it.leftStrokeHorizontalColors?.let {
                    strokeHorizontalColors = it
                }
                it.leftStrokeVerticalColors?.let {
                    strokeVerticalColors = it
                }
            }
            //上
            if (oritaton == 2) {
                it.topDashWidth?.let {
                    dashWidth = it
                }
                it.topDashGap?.let {
                    dashGap = it
                }
                it.topStrokeWidth?.let {
                    strokeWidth = it
                }
                it.topStrokeColor?.let {
                    strokeColor = it
                }
                it.topStrokeHorizontalColors?.let {
                    strokeHorizontalColors = it
                }
                it.topStrokeVerticalColors?.let {
                    strokeVerticalColors = it
                }
            }
            //右
            if (oritaton == 3) {
                it.rightDashWidth?.let {
                    dashWidth = it
                }
                it.rightDashGap?.let {
                    dashGap = it
                }
                it.rightStrokeWidth?.let {
                    strokeWidth = it
                }
                it.rightStrokeColor?.let {
                    strokeColor = it
                }
                it.rightStrokeHorizontalColors?.let {
                    strokeHorizontalColors = it
                }
                it.rightStrokeVerticalColors?.let {
                    strokeVerticalColors = it
                }
            }
            //下
            if (oritaton == 4) {
                it.bottomDashWidth?.let {
                    dashWidth = it
                }
                it.bottomDashGap?.let {
                    dashGap = it
                }
                it.bottomStrokeWidth?.let {
                    strokeWidth = it
                }
                it.bottomStrokeColor?.let {
                    strokeColor = it
                }
                it.bottomStrokeHorizontalColors?.let {
                    strokeHorizontalColors = it
                }
                it.bottomStrokeVerticalColors?.let {
                    strokeVerticalColors = it
                }
            }
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = strokeWidth
            paint.color = strokeColor
            //虚线
            if (dashWidth > 0 && dashGap > 0) {
                var dashPathEffect = DashPathEffect(floatArrayOf(dashWidth, dashGap), 0f)
                paint.setPathEffect(dashPathEffect)
            }
            //边框颜色渐变，渐变颜色优先等级大于正常颜色。
            var linearGradient: LinearGradient? = null
            if (strokeHorizontalColors != null && strokeHorizontalColors!!.size > 1) {
                //fixme 水平渐变
                linearGradient = LinearGradient(0f + scrollX, 0f, w.toFloat() + scrollX, 0f, strokeHorizontalColors, null, Shader.TileMode.CLAMP)
            }
            if (strokeVerticalColors != null && strokeVerticalColors!!.size > 1) {
                //fixme 垂直渐变
                linearGradient = LinearGradient(0f, 0f + scrollY, 0f, 0f + scrollY + h.toFloat(), strokeVerticalColors, null, Shader.TileMode.CLAMP)
            }
            linearGradient?.let {
                paint.setShader(linearGradient)
            }
        }
    }

    //绘制边框；fixme 亲测边框的位置和圆角切割radius位置是一致的。如果位置不一致，那一定是两个控件的高度或位置不一致。
    override fun draw2Last(canvas: Canvas, paint: Paint) {
        super.draw2Last(canvas, paint)
        currentBorder?.let {
            if (it.strokeWidth > 0) {
                //绘制左边的边框
                if (it.isDrawLeft) {
                    initBorderPaint(resetPaint(paint), it, 1)
                    var startX = scrollX + paint.strokeWidth / 2
                    var startY = scrollY.toFloat()
                    var endX = startX
                    var endY = startY + h.toFloat()
                    canvas.drawLine(startX, startY, endX, endY, paint)
                }
                //绘制上边的边框
                if (it.isDrawTop) {
                    initBorderPaint(resetPaint(paint), it, 2)
                    var startX = scrollX.toFloat()
                    var startY = scrollY.toFloat() + paint.strokeWidth / 2
                    var endX = startX + w.toFloat()
                    var endY = startY
                    canvas.drawLine(startX, startY, endX, endY, paint)
                }
                //绘制右边的边框
                if (it.isDrawRight) {
                    initBorderPaint(resetPaint(paint), it, 3)
                    var startX = scrollX.toFloat() + w.toFloat() - paint.strokeWidth / 2
                    var startY = scrollY.toFloat()
                    var endX = startX
                    var endY = startY + h.toFloat()
                    canvas.drawLine(startX, startY, endX, endY, paint)
                }
                //绘制底部的边框
                if (it.isDrawBottom) {
                    initBorderPaint(resetPaint(paint), it, 4)
                    var startX = scrollX.toFloat()
                    var startY = scrollY.toFloat() + h - paint.strokeWidth / 2
                    var endX = startX + w.toFloat()
                    var endY = startY
                    canvas.drawLine(startX, startY, endX, endY, paint)
                }
            }
        }
    }

    fun getBorderEntity(): KBorderEntity {
        if (border == null) {
            border = KBorderEntity()
        }
        return border!!
    }

    var currentBorder: KBorderEntity? = null//当前边框

    var border: KBorderEntity? = null//正常
    fun border(block: KBorderEntity.() -> Unit): K3BorderWidget {
        clearButonShadow()//自定义圆角，就去除按钮默认的圆角阴影。不然效果不好。
        block(getBorderEntity())
        invalidate()
        return this
    }

    var border_enable: KBorderEntity? = null//不可用
    fun border_enable(block: KBorderEntity.() -> Unit): K3BorderWidget {
        if (border_enable == null) {
            border_enable = getBorderEntity().copy()
        }
        block(border_enable!!)
        invalidate()
        return this
    }

    var border_press: KBorderEntity? = null//按下
    fun border_press(block: KBorderEntity.() -> Unit): K3BorderWidget {
        if (border_press == null) {
            border_press = getBorderEntity().copy()
        }
        block(border_press!!)
        invalidate()
        return this
    }

    var border_focuse: KBorderEntity? = null//聚焦
    fun border_focuse(block: KBorderEntity.() -> Unit): K3BorderWidget {
        if (border_focuse == null) {
            border_focuse = getBorderEntity().copy()
        }
        block(border_focuse!!)
        invalidate()
        return this
    }

    var border_hove: KBorderEntity? = null//悬浮
    fun border_hove(block: KBorderEntity.() -> Unit): K3BorderWidget {
        if (border_hove == null) {
            border_hove = getBorderEntity().copy()
        }
        block(border_hove!!)
        invalidate()
        return this
    }

    var border_selected: KBorderEntity? = null//选中
    fun border_selected(block: KBorderEntity.() -> Unit): K3BorderWidget {
        if (border_selected == null) {
            border_selected = getBorderEntity().copy()
        }
        block(border_selected!!)
        invalidate()
        return this
    }

    private fun normal() {
        currentBorder = border
    }

    //状态：聚焦
    override fun changedFocused() {
        super.changedFocused()
        normal()
        border_focuse?.let {
            currentBorder = it
        }
    }

    //状态：悬浮
    override fun changedHovered() {
        super.changedHovered()
        normal()
        border_hove?.let {
            currentBorder = it
        }
    }

    //状态：选中
    override fun changedSelected() {
        super.changedSelected()
        normal()
        border_selected?.let {
            currentBorder = it
        }
    }

    //状态：不可用
    override fun changedEnabled() {
        super.changedEnabled()
        normal()
        border_enable?.let {
            currentBorder = it
        }
    }

    //状态：按下
    override fun changedPressed() {
        super.changedPressed()
        normal()
        border_press?.let {
            currentBorder = it
        }
    }

    //状态：正常
    override fun changedNormal() {
        super.changedNormal()
        normal()
    }

    override fun onDestroy() {
        super.onDestroy()
        border = null
        border_press = null
        border_hove = null
        border_focuse = null
        border_selected = null
        border_enable = null
    }

}
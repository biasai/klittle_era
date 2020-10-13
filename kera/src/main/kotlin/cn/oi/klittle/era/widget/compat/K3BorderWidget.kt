package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.entity.widget.KAirEntry
import cn.oi.klittle.era.entity.widget.compat.KBorderEntity
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.topPadding

//fixme rotation=45f 宽和高相等的情况下，整个控件整体旋转45度，就成菱形了。

//            调用案例
//            KView(this).apply {
//                backgroundColor(Color.GREEN)
//                border {
//                    strokeWidth = kpx.x(10f) //所有边框的宽度；
//                    leftStrokeWidth = strokeWidth / 2//左边边框的宽度 fixme 各个边框大小颜色都可以独自设置。
//                    bottomStrokeWidth = kpx.x(2f)//底部边框的宽度
//                    leftStrokeColor = Color.BLUE//左边边框的颜色
//                    rightStrokeVerticalColors(Color.RED, Color.CYAN)//右边边框垂直渐变色
//                    bottomDashWidth = kpx.x(15f)//底部边框虚线
//                    bottomDashGap = kpx.x(10f)
//                    all_radius=0f//所有圆角的角度（0~90）//fixme 角度统一使用radius；最好不要超过45度(即：左上角和左下角两个角度之和不要大于90度)，不然边框样式可能会错乱。
//                    right_bottom=45f//边框右下角角度;fixme border支持圆角
//                    //bg_color=Color.GREEN//fixme 支持背景色；具备切割效果；会像radius一样；自动去除边框以外的区域。只显示边框以内的。
//                    bgHorizontalColors(Color.RED,Color.CYAN)
//                    //leftMargin=kpx.x(50)//左外补丁，控件边框的间距；fixme 9.0正常，在低版本会切割显示会有问题（7.0），建议使用 radius {}；radius已经做了切割兼容处理。
//                    //topMargin=kpx.x(50)
//                    //rightMargin=kpx.x(50)
//                    //bottomMargin=kpx.x(50)
//                    //setAutoPaddingForBorder(kpx.x(16))//fixme 根据border的外补丁，自动设置文本内补丁。
//                }
//                border_press {
//                    isDrawLeft = false//是否绘制左边边框
//                    isDrawTop = false//时候绘制顶部边框
//                    strokeHorizontalColors(Color.RED, Color.CYAN, Color.GRAY)//边框水平渐变颜色
//                    dashGap = kpx.x(30f)
//                    dashWidth = kpx.x(50f)
//                }
//            }.lparams {
//                topMargin = kpx.x(50)
//                width = kpx.x(300)
//                height = kpx.x(300)
//            }

/**
 * fixme 绘制上下左右的边框；现在各个角已经支持圆角。具备切割能力。只显示边框内的内容。
 * fixme 边框添加支持了外补丁，控件边框的距离。
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

    /**
     * fixme 根据border的外补丁，自动设置文本的内补丁。
     * @param mPadding 内补丁；一般都设置为：kpx.x(16)；即8的倍数。8，16，24...比较好。
     * @param borderEnty 边框实体类
     */
    open fun setAutoPaddingForBorder(mPadding: Int, borderEnty: KBorderEntity? = this.border) {
        borderEnty?.apply {
            rightPadding = mPadding + rightMargin
            leftPadding = mPadding + leftMargin
            topPadding = mPadding + topMargin
            bottomPadding = mPadding + bottomMargin
        }
    }

    //初始化画笔
    private fun initBorderPaint(paint: Paint, border: KBorderEntity, oritaton: Int = 0) {
        //paint.strokeCap = Paint.Cap.BUTT
        //paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND//fixme 加个圆帽效果好一点，亲测。
        paint.strokeJoin = Paint.Join.ROUND
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
            } else {
                paint.setPathEffect(null)
            }
            //边框颜色渐变，渐变颜色优先等级大于正常颜色。
            var left = scrollX + paint.strokeWidth / 2 + it.leftMargin
            var top = scrollY.toFloat() + paint.strokeWidth / 2 + it.topMargin
            //KLoggerUtils.e("scrollY:\t"+scrollY+"\tcenterY:\t"+centerY+"\th / 2:\t"+(h / 2.0f)+"\th:\t"+h)
            var right = scrollX.toFloat() + w.toFloat() - it.rightMargin
            var bottom = scrollY.toFloat() + h - it.bottomMargin
            if (it.strokeVerticalColors != null) {
                var shader: LinearGradient? = null
                if (!it.isBgGradient) {
                    //垂直不渐变
                    if (it.strokeVerticalColors!!.size == 1) {
                        //fixme 颜色渐变数组必须大于等于2
                        var strokeVerticalColors = IntArray(2)
                        strokeVerticalColors[0] = it.strokeVerticalColors!![0]
                        strokeVerticalColors[1] = it.strokeVerticalColors!![0]
                        shader = getNotLinearGradient(top, bottom, strokeVerticalColors!!, true, scrollY)
                    } else {
                        shader = getNotLinearGradient(top, bottom, it.strokeVerticalColors!!, true, scrollY)
                    }
                }
                //垂直渐变，优先级高于水平(渐变颜色值数组必须大于等于2，不然异常)
                if (shader == null) {
                    if (it.strokeVerticalColors!!.size == 1) {
                        var strokeVerticalColors = IntArray(2)
                        strokeVerticalColors[0] = it.strokeVerticalColors!![0]
                        strokeVerticalColors[1] = it.strokeVerticalColors!![0]
                        shader = LinearGradient(0f, top, 0f, bottom, strokeVerticalColors, null, Shader.TileMode.MIRROR)
                    } else {
                        shader = LinearGradient(0f, top, 0f, bottom, it.strokeVerticalColors, null, Shader.TileMode.MIRROR)
                    }
                }
                paint.setShader(shader)
            } else if (it.strokeHorizontalColors != null) {
                var shader: LinearGradient? = null
                if (!it.isBgGradient) {
                    //水平不渐变
                    if (it.strokeHorizontalColors!!.size == 1) {
                        var strokeHorizontalColors = IntArray(2)
                        strokeHorizontalColors[0] = it.bgHorizontalColors!![0]
                        strokeHorizontalColors[1] = it.bgHorizontalColors!![0]
                        shader = getNotLinearGradient(left, right, strokeHorizontalColors!!, false, scrollY)
                    } else {
                        shader = getNotLinearGradient(left, right, it.strokeHorizontalColors!!, false, scrollY)
                    }
                }
                //水平渐变
                if (shader == null) {
                    if (it.strokeHorizontalColors!!.size == 1) {
                        var strokeHorizontalColors = IntArray(2)
                        strokeHorizontalColors[0] = it.bgHorizontalColors!![0]
                        strokeHorizontalColors[1] = it.bgHorizontalColors!![0]
                        shader = LinearGradient(left, 0f, right, 0f, strokeHorizontalColors, null, Shader.TileMode.MIRROR)
                    } else {
                        shader = LinearGradient(left, 0f, right, 0f, it.strokeHorizontalColors, null, Shader.TileMode.MIRROR)
                    }
                }
                paint.setShader(shader)
            } else {
                paint.setShader(null)
            }
        }
    }

    private var borderPath: Path? = null
    private var linePath: Path? = null

    override fun draw2Bg(canvas: Canvas, paint: Paint) {
        super.draw2Bg(canvas, paint)
        currentBorder?.let {
            if (true) {
                //画背景
                var isDrawColor = false//是否画背景色
                if (it.bg_color != Color.TRANSPARENT) {
                    paint.color = it.bg_color
                    isDrawColor = true
                }
//                var left = 0f + scrollX + centerX - w / 2.0f
//                var top = 0f + scrollY + centerY - h / 2.0f//fixme 2.0f必须设置成浮点型，不然可能会有0.5的偏差。
//                //KLoggerUtils.e("scrollY:\t"+scrollY+"\tcenterY:\t"+centerY+"\th / 2:\t"+(h / 2.0f)+"\th:\t"+h)
//                var right = w + left
//                var bottom = h + top
                var left = scrollX + paint.strokeWidth / 2 + it.leftMargin
                var top = scrollY.toFloat() + paint.strokeWidth / 2 + it.topMargin
                //KLoggerUtils.e("scrollY:\t"+scrollY+"\tcenterY:\t"+centerY+"\th / 2:\t"+(h / 2.0f)+"\th:\t"+h)
                var right = scrollX.toFloat() + w.toFloat() - it.rightMargin
                var bottom = scrollY.toFloat() + h - it.bottomMargin
                //KLoggerUtils.e("left:\t"+left+"\ttop:\t"+top+"\tright:\t"+right+"\tbottom:\t"+bottom)
                if (it.bgVerticalColors != null) {
                    var shader: LinearGradient? = null
                    if (!it.isBgGradient) {
                        //垂直不渐变
                        if (it.bgVerticalColors!!.size == 1) {
                            //fixme 颜色渐变数组必须大于等于2
                            var bgVerticalColors = IntArray(2)
                            bgVerticalColors[0] = it.bgVerticalColors!![0]
                            bgVerticalColors[1] = it.bgVerticalColors!![0]
                            shader = getNotLinearGradient(top, bottom, bgVerticalColors!!, true, scrollY)
                        } else {
                            shader = getNotLinearGradient(top, bottom, it.bgVerticalColors!!, true, scrollY)
                        }
                    }
                    //垂直渐变，优先级高于水平(渐变颜色值数组必须大于等于2，不然异常)
                    if (shader == null) {
                        if (it.bgVerticalColors!!.size == 1) {
                            var bgVerticalColors = IntArray(2)
                            bgVerticalColors[0] = it.bgVerticalColors!![0]
                            bgVerticalColors[1] = it.bgVerticalColors!![0]
                            shader = LinearGradient(0f, top, 0f, bottom, bgVerticalColors, null, Shader.TileMode.MIRROR)
                        } else {
                            shader = LinearGradient(0f, top, 0f, bottom, it.bgVerticalColors, null, Shader.TileMode.MIRROR)
                        }
                    }
                    paint.setShader(shader)
                    isDrawColor = true
                } else if (it.bgHorizontalColors != null) {
                    var shader: LinearGradient? = null
                    if (!it.isBgGradient) {
                        //水平不渐变
                        if (it.bgHorizontalColors!!.size == 1) {
                            var bgHorizontalColors = IntArray(2)
                            bgHorizontalColors[0] = it.bgHorizontalColors!![0]
                            bgHorizontalColors[1] = it.bgHorizontalColors!![0]
                            shader = getNotLinearGradient(left, right, bgHorizontalColors!!, false, scrollY)
                        } else {
                            shader = getNotLinearGradient(left, right, it.bgHorizontalColors!!, false, scrollY)
                        }
                    }
                    //水平渐变
                    if (shader == null) {
                        if (it.bgHorizontalColors!!.size == 1) {
                            var bgHorizontalColors = IntArray(2)
                            bgHorizontalColors[0] = it.bgHorizontalColors!![0]
                            bgHorizontalColors[1] = it.bgHorizontalColors!![0]
                            shader = LinearGradient(left, 0f, right, 0f, bgHorizontalColors, null, Shader.TileMode.MIRROR)
                        } else {
                            shader = LinearGradient(left, 0f, right, 0f, it.bgHorizontalColors, null, Shader.TileMode.MIRROR)
                        }
                    }
                    paint.setShader(shader)
                    isDrawColor = true
                }
                if (isDrawColor) {
                    currentBorder?.let {
                        var dW = w
                        if (dW > h) {
                            dW = h//以短边为基准
                        }
                        var mOffset = kpx.x(1.5f)//fixme 防止边框与背景边缘对不齐问题。1.5刚刚好。
                        if (it.strokeWidth <= kpx.x(1f)) {
                            mOffset = kpx.x(1f)
                            if (it.strokeWidth <= 0) {
                                mOffset = 0f
                            }
                        } else if (it.strokeWidth >= kpx.x(4f)) {
                            mOffset = it.strokeWidth / 2
                        }
                        if (it.isPorterDuffXfermode) {
                            //mOffset = 0f//切割不需要。
                            mOffset = kpx.x(1f)//还是偏移一下比较好。
                        }
                        var mLeftPadding = it.leftMargin + mOffset
                        var mRightPadding = it.rightMargin + mOffset
                        var mTopPadding = it.topMargin + mOffset
                        var mBottomPadding = it.bottomMargin + mOffset
                        var all_corner = it.all_radius
                        var left_top_corner = it.left_top//左上角
                        left_top_corner?.let {
                            if (it < 0 && all_corner >= 0) {
                                left_top_corner = all_corner
                            }
                            if (left_top_corner > 90f) {
                                left_top_corner = 90f
                            }
                        }
                        var left_bottom_corner = it.left_bottom//左下角
                        left_bottom_corner?.let {
                            if (it < 0 && all_corner >= 0) {
                                left_bottom_corner = all_corner
                            }
                            if (left_bottom_corner > 90f) {
                                left_bottom_corner = 90f
                            }
                        }
                        var right_top_corner = it.right_top//右上角
                        right_top_corner?.let {
                            if (it < 0 && all_corner >= 0) {
                                right_top_corner = all_corner
                            }
                            if (right_top_corner > 90f) {
                                right_top_corner = 90f
                            }
                        }
                        var right_bottom_corner = it.right_bottom//右下角
                        right_bottom_corner?.let {
                            if (it < 0 && all_corner >= 0) {
                                right_bottom_corner = all_corner
                            }
                            if (right_bottom_corner > 90f) {
                                right_bottom_corner = 90f
                            }
                        }
                        if (borderPath == null) {
                            borderPath = Path()
                        }
                        borderPath?.reset()
                        //绘制左边的边框
                        if (true) {
                            var startX = scrollX + paint.strokeWidth / 2 + mLeftPadding
                            var startY = scrollY.toFloat() + paint.strokeWidth / 2 + mTopPadding
                            var endX = startX
                            var endY = scrollY.toFloat() + h.toFloat() - paint.strokeWidth / 2 - mBottomPadding
                            var startX_border = scrollX.toFloat() + mLeftPadding
                            var startY_border = scrollY.toFloat() + mTopPadding
                            var endX_border = startX_border
                            var endY_border = scrollY.toFloat() + h.toFloat() - mBottomPadding
                            if (left_top_corner <= 0 && left_bottom_corner <= 0) {
                                //fixme
                                borderPath?.moveTo(endX_border, endY_border)
                                borderPath?.lineTo(startX_border, startY_border)
                            } else {
                                //左上角圆角
                                var starX0_border = startX_border + dW / 2 * (left_top_corner / 90f)
                                var starY0_border = startY_border
                                var controllX = startX
                                var controllY = startY
                                var endX0_border = startX_border
                                var endY0_border = startY_border + dW / 2 * (left_top_corner / 90f)
                                if (left_bottom_corner <= 0) {
                                    //fixme
                                    borderPath?.moveTo(endX_border, endY_border)
                                    borderPath?.lineTo(endX0_border, endY0_border)
                                } else {
                                    //左下角圆角
                                    var endY1_border = endY_border - dW / 2 * (left_bottom_corner / 90f)
                                    var controllX = endX
                                    var controllY = endY
                                    var endX2_border = startX_border + dW / 2 * (left_bottom_corner / 90f)
                                    var endY2_border = endY_border
                                    //fixme
                                    borderPath?.moveTo(endX2_border, endY2_border)
                                    borderPath?.quadTo(controllX, controllY, endX_border, endY1_border)
                                }
                                //fixme
                                borderPath?.lineTo(endX0_border, endY0_border)
                                borderPath?.quadTo(controllX, controllY, starX0_border, starY0_border)
                            }
                        }
                        //绘制上边的边框
                        if (true) {
                            var startX_border = scrollX.toFloat() + mLeftPadding
                            var startY_border = scrollY.toFloat() + mTopPadding
                            var endX_border = scrollX.toFloat() + w.toFloat() - mRightPadding
                            var endY_border = startY_border
                            if (left_top_corner <= 0 && right_top_corner <= 0) {
                                //fixme
                                borderPath?.lineTo(startX_border, startY_border)
                                borderPath?.lineTo(endX_border, endY_border)
                            } else {
                                var starX0_border = startX_border + dW / 2 * (left_top_corner / 90f)
                                var starY0_border = startY_border
                                var endX0_border = endX_border - dW / 2 * (right_top_corner / 90f)
                                //fixme
                                borderPath?.lineTo(starX0_border, starY0_border)
                                borderPath?.lineTo(endX0_border, endY_border)
                            }
                        }
                        //绘制右边的边框
                        if (true) {
                            var startX = scrollX.toFloat() + w.toFloat() - paint.strokeWidth / 2 - mRightPadding
                            var startY = scrollY.toFloat() + paint.strokeWidth / 2 + mTopPadding
                            var endX = startX
                            var endY = scrollY.toFloat() + h.toFloat() - paint.strokeWidth / 2 - mBottomPadding
                            var startX_border = scrollX.toFloat() + w.toFloat() - mRightPadding
                            var startY_border = scrollY.toFloat() + mTopPadding
                            var endX_border = startX_border
                            var endY_border = scrollY.toFloat() + h.toFloat() - mBottomPadding
                            if (right_top_corner <= 0 && right_bottom_corner <= 0) {
                                //fixme
                                borderPath?.lineTo(startX_border, startY_border)
                                borderPath?.lineTo(endX_border, endY_border)
                            } else {
                                //右上角圆角
                                var starX0_border = startX_border - dW / 2 * (right_top_corner / 90f)
                                var starY0_border = startY_border
                                var controllX = startX
                                var controllY = startY
                                var endX0_border = startX_border
                                var endY0_border = startY_border + dW / 2 * (right_top_corner / 90f)
                                //fixme
                                borderPath?.lineTo(starX0_border, starY0_border)
                                borderPath?.quadTo(controllX, controllY, endX0_border, endY0_border)
                                if (right_bottom_corner <= 0) {
                                    //fixme
                                    borderPath?.lineTo(endX0_border, endY0_border)
                                    borderPath?.lineTo(endX_border, endY_border)
                                } else {
                                    //右下角圆角
                                    var endY1_border = endY_border - dW / 2 * (right_bottom_corner / 90f)
                                    //fixme
                                    borderPath?.lineTo(endX0_border, endY0_border)
                                    borderPath?.lineTo(endX_border, endY1_border)
                                    var controllX = endX
                                    var controllY = endY
                                    var endX2_border = endX_border - dW / 2 * (right_bottom_corner / 90f)
                                    var endY2_border = endY_border
                                    //fixme
                                    borderPath?.lineTo(endX_border, endY1_border)
                                    borderPath?.quadTo(controllX, controllY, endX2_border, endY2_border)
                                }
                            }
                        }
                        //绘制底部的边框
                        if (true) {
                            var startX_border = scrollX.toFloat() + mLeftPadding
                            var startY_border = scrollY.toFloat() + h - mBottomPadding
                            var endX_border = startX_border + w.toFloat() - mRightPadding
                            var endY_border = startY_border
                            if (left_bottom_corner <= 0 && right_bottom_corner <= 0) {
                                //fixme
                                borderPath?.lineTo(startX_border, startY_border)
                                borderPath?.lineTo(endX_border, endY_border)
                            } else {
                                var starX0_border = startX_border + dW / 2 * (left_bottom_corner / 90f)
                                var starY0_border = startY_border
                                var endX2_border = endX_border - dW / 2 * (right_bottom_corner / 90f)
                                //fixme
                                borderPath?.lineTo(starX0_border, starY0_border)
                                borderPath?.lineTo(endX2_border, endY_border)
                            }
                        }
                    }
                    //fixme 画背景
                    if (borderPath != null) {
                        paint.strokeWidth = 0f
                        paint.style = Paint.Style.FILL
                        canvas.drawPath(borderPath, paint)
                    } else {
                        var left = 0f + scrollX + centerX - w / 2.0f
                        var top = 0f + scrollY + centerY - h / 2.0f//fixme 2.0f必须设置成浮点型，不然可能会有0.5的偏差。
                        //KLoggerUtils.e("scrollY:\t"+scrollY+"\tcenterY:\t"+centerY+"\th / 2:\t"+(h / 2.0f)+"\th:\t"+h)
                        var right = w + left
                        var bottom = h + top
                        var rectF = RectF(left, top, right, bottom)
                        canvas.drawRect(rectF, paint)
                    }
                }
            }
        }
    }

    //绘制边框；fixme 亲测边框的位置和圆角切割radius位置是一致的。如果位置不一致，那一定是两个控件的高度或位置不一致。
    override fun draw2Last(canvas: Canvas, paint: Paint) {
        super.draw2Last(canvas, paint)
        currentBorder?.let {
            var dW = w
            if (dW > h) {
                dW = h//以短边为基准
            }
            var mLeftPadding = it.leftMargin
            var mRightPadding = it.rightMargin
            var mTopPadding = it.topMargin
            var mBottomPadding = it.bottomMargin
            var all_corner = it.all_radius
            var left_top_corner = it.left_top//左上角
            left_top_corner?.let {
                if (it < 0 && all_corner >= 0) {
                    left_top_corner = all_corner
                }
                if (left_top_corner > 90f) {
                    left_top_corner = 90f
                }
            }
            var left_bottom_corner = it.left_bottom//左下角
            left_bottom_corner?.let {
                if (it < 0 && all_corner >= 0) {
                    left_bottom_corner = all_corner
                }
                if (left_bottom_corner > 90f) {
                    left_bottom_corner = 90f
                }
            }
            var right_top_corner = it.right_top//右上角
            right_top_corner?.let {
                if (it < 0 && all_corner >= 0) {
                    right_top_corner = all_corner
                }
                if (right_top_corner > 90f) {
                    right_top_corner = 90f
                }
            }
            var right_bottom_corner = it.right_bottom//右下角
            right_bottom_corner?.let {
                if (it < 0 && all_corner >= 0) {
                    right_bottom_corner = all_corner
                }
                if (right_bottom_corner > 90f) {
                    right_bottom_corner = 90f
                }
            }
            if (borderPath == null) {
                borderPath = Path()
            }
            if (linePath == null) {
                linePath = Path()
            }
            borderPath?.reset()
            linePath?.reset()
            //绘制左边的边框
            if (true || it.isDrawLeft) {
                initBorderPaint(resetPaint(paint), it, 1)
                var startX = scrollX + paint.strokeWidth / 2 + mLeftPadding
                var startY = scrollY.toFloat() + paint.strokeWidth / 2 + mTopPadding
                var endX = startX
                var endY = scrollY.toFloat() + h.toFloat() - paint.strokeWidth / 2 - mBottomPadding
                var startX_border = scrollX.toFloat() + mLeftPadding
                var startY_border = scrollY.toFloat() + mTopPadding
                var endX_border = startX_border
                var endY_border = scrollY.toFloat() + h.toFloat() - mBottomPadding
                if (left_top_corner <= 0 && left_bottom_corner <= 0) {
                    linePath?.reset()
                    linePath?.moveTo(endX, endY)
                    linePath?.lineTo(startX, startY)
                    if (it.isDrawLeft && paint.strokeWidth > 0) {
                        canvas.drawPath(linePath, paint)//fixme pathEffect虚线只对Path有效果；对drawLine（）没有效果。
                        //canvas.drawLine(startX, startY, endX, endY, paint)
                    }
                    //fixme
                    borderPath?.moveTo(endX_border, endY_border)
                    borderPath?.lineTo(startX_border, startY_border)
                } else {
                    //左上角圆角
                    var path = Path()
                    var starX0 = startX + dW / 2 * (left_top_corner / 90f)
                    var starY0 = startY
                    var starX0_border = startX_border + dW / 2 * (left_top_corner / 90f)
                    var starY0_border = startY_border
                    var controllX = startX
                    var controllY = startY
                    var endX0 = startX
                    var endY0 = startY + dW / 2 * (left_top_corner / 90f)
                    var endX0_border = startX_border
                    var endY0_border = startY_border + dW / 2 * (left_top_corner / 90f)
                    path.moveTo(starX0, starY0)
                    path.quadTo(controllX, controllY, endX0, endY0)
                    if (it.isDrawLeft && paint.strokeWidth > 0) {
                        canvas.drawPath(path, paint)
                    }
                    if (left_bottom_corner <= 0) {
                        linePath?.reset()
                        linePath?.moveTo(endX0, endY0)
                        linePath?.lineTo(endX, endY)
                        if (it.isDrawLeft && paint.strokeWidth > 0) {
                            canvas.drawPath(linePath, paint)
                            //canvas.drawLine(endX0, endY0, endX, endY, paint)
                        }
                        //fixme
                        borderPath?.moveTo(endX_border, endY_border)
                        borderPath?.lineTo(endX0_border, endY0_border)
                    } else {
                        //左下角圆角
                        var endY1 = endY - dW / 2 * (left_bottom_corner / 90f)
                        var endY1_border = endY_border - dW / 2 * (left_bottom_corner / 90f)
                        linePath?.reset()
                        linePath?.moveTo(endX0, endY0)
                        linePath?.lineTo(endX, endY1)
                        if (it.isDrawLeft && paint.strokeWidth > 0) {
                            canvas.drawPath(linePath, paint)
                            //canvas.drawLine(endX0, endY0, endX, endY1, paint)
                        }
                        var path2 = Path()
                        path2.moveTo(endX, endY1)
                        var controllX = endX
                        var controllY = endY
                        var endX2 = startX + dW / 2 * (left_bottom_corner / 90f)
                        var endY2 = endY
                        var endX2_border = startX_border + dW / 2 * (left_bottom_corner / 90f)
                        var endY2_border = endY_border
                        path2.quadTo(controllX, controllY, endX2, endY2)
                        if (it.isDrawLeft && paint.strokeWidth > 0) {
                            canvas.drawPath(path2, paint)
                        }
                        //fixme
                        borderPath?.moveTo(endX2_border, endY2_border)
                        borderPath?.quadTo(controllX, controllY, endX_border, endY1_border)
                    }
                    //fixme
                    borderPath?.lineTo(endX0_border, endY0_border)
                    borderPath?.quadTo(controllX, controllY, starX0_border, starY0_border)
                }
            }
            //绘制上边的边框
            if (true || it.isDrawTop) {
                initBorderPaint(resetPaint(paint), it, 2)
                var startX = scrollX.toFloat() + mLeftPadding// + paint.strokeWidth / 2
                var startY = scrollY.toFloat() + paint.strokeWidth / 2 + mTopPadding
                var endX = scrollX.toFloat() + w.toFloat() - mRightPadding// - paint.strokeWidth / 2
                var endY = startY
                var startX_border = scrollX.toFloat() + mLeftPadding
                var startY_border = scrollY.toFloat() + mTopPadding
                var endX_border = scrollX.toFloat() + w.toFloat() - mRightPadding
                var endY_border = startY_border
                if (left_top_corner <= 0 && right_top_corner <= 0) {
                    linePath?.reset()
                    linePath?.moveTo(startX, startY)
                    linePath?.lineTo(endX, endY)
                    if (it.isDrawTop && paint.strokeWidth > 0) {
                        canvas.drawPath(linePath, paint)
                        //canvas.drawLine(startX, startY, endX, endY, paint)
                    }
                    //fixme
                    borderPath?.lineTo(startX_border, startY_border)
                    borderPath?.lineTo(endX_border, endY_border)
                } else {
                    var starX0 = startX + dW / 2 * (left_top_corner / 90f)
                    var starY0 = startY
                    var endX0 = endX - dW / 2 * (right_top_corner / 90f)
                    var starX0_border = startX_border + dW / 2 * (left_top_corner / 90f)
                    var starY0_border = startY_border
                    var endX0_border = endX_border - dW / 2 * (right_top_corner / 90f)
                    linePath?.reset()
                    linePath?.moveTo(starX0, starY0)
                    linePath?.lineTo(endX0, endY)
                    if (it.isDrawTop && paint.strokeWidth > 0) {
                        canvas.drawPath(linePath, paint)
                        //canvas.drawLine(starX0, starY0, endX0, endY, paint)
                    }
                    //fixme
                    borderPath?.lineTo(starX0_border, starY0_border)
                    borderPath?.lineTo(endX0_border, endY_border)
                }
            }
            //绘制右边的边框
            if (true || it.isDrawRight) {
                initBorderPaint(resetPaint(paint), it, 3)
                var startX = scrollX.toFloat() + w.toFloat() - paint.strokeWidth / 2 - mRightPadding
                var startY = scrollY.toFloat() + paint.strokeWidth / 2 + mTopPadding
                var endX = startX
                var endY = scrollY.toFloat() + h.toFloat() - paint.strokeWidth / 2 - mBottomPadding
                var startX_border = scrollX.toFloat() + w.toFloat() - mRightPadding
                var startY_border = scrollY.toFloat() + mTopPadding
                var endX_border = startX_border
                var endY_border = scrollY.toFloat() + h.toFloat() - mBottomPadding
                if (right_top_corner <= 0 && right_bottom_corner <= 0) {
                    linePath?.reset()
                    linePath?.moveTo(startX, startY)
                    linePath?.lineTo(endX, endY)
                    if (it.isDrawRight && paint.strokeWidth > 0) {
                        canvas.drawPath(linePath, paint)
                        //canvas.drawLine(startX, startY, endX, endY, paint)
                    }
                    //fixme
                    borderPath?.lineTo(startX_border, startY_border)
                    borderPath?.lineTo(endX_border, endY_border)
                } else {
                    //右上角圆角
                    var path = Path()
                    var starX0 = startX - dW / 2 * (right_top_corner / 90f)
                    var starY0 = startY
                    var starX0_border = startX_border - dW / 2 * (right_top_corner / 90f)
                    var starY0_border = startY_border
                    var controllX = startX
                    var controllY = startY
                    var endX0 = startX
                    var endY0 = startY + dW / 2 * (right_top_corner / 90f)
                    var endX0_border = startX_border
                    var endY0_border = startY_border + dW / 2 * (right_top_corner / 90f)
                    path.moveTo(starX0, starY0)
                    path.quadTo(controllX, controllY, endX0, endY0)
                    if (it.isDrawRight && paint.strokeWidth > 0) {
                        canvas.drawPath(path, paint)
                    }
                    //fixme
                    borderPath?.lineTo(starX0_border, starY0_border)
                    borderPath?.quadTo(controllX, controllY, endX0_border, endY0_border)
                    if (right_bottom_corner <= 0) {
                        linePath?.reset()
                        linePath?.moveTo(endX0, endY0)
                        linePath?.lineTo(endX, endY)
                        if (it.isDrawRight && paint.strokeWidth > 0) {
                            canvas.drawPath(linePath, paint)
                            //canvas.drawLine(endX0, endY0, endX, endY, paint)
                        }
                        //fixme
                        borderPath?.lineTo(endX0_border, endY0_border)
                        borderPath?.lineTo(endX_border, endY_border)
                    } else {
                        //右下角圆角
                        var endY1 = endY - dW / 2 * (right_bottom_corner / 90f)
                        var endY1_border = endY_border - dW / 2 * (right_bottom_corner / 90f)
                        linePath?.reset()
                        linePath?.moveTo(endX0, endY0)
                        linePath?.lineTo(endX, endY1)
                        if (it.isDrawRight && paint.strokeWidth > 0) {
                            canvas.drawPath(linePath, paint)
                            //canvas.drawLine(endX0, endY0, endX, endY1, paint)
                        }
                        //fixme
                        borderPath?.lineTo(endX0_border, endY0_border)
                        borderPath?.lineTo(endX_border, endY1_border)
                        var path2 = Path()
                        path2.moveTo(endX, endY1)
                        var controllX = endX
                        var controllY = endY
                        var endX2 = endX - dW / 2 * (right_bottom_corner / 90f)
                        var endY2 = endY
                        var endX2_border = endX_border - dW / 2 * (right_bottom_corner / 90f)
                        var endY2_border = endY_border
                        path2.quadTo(controllX, controllY, endX2, endY2)
                        if (it.isDrawRight && paint.strokeWidth > 0) {
                            canvas.drawPath(path2, paint)
                        }
                        //fixme
                        borderPath?.lineTo(endX_border, endY1_border)
                        borderPath?.quadTo(controllX, controllY, endX2_border, endY2_border)
                    }
                }
            }
            //绘制底部的边框
            if (true || it.isDrawBottom) {
                initBorderPaint(resetPaint(paint), it, 4)
                var startX = scrollX.toFloat() + paint.strokeWidth / 2 + mLeftPadding
                var startY = scrollY.toFloat() + h - paint.strokeWidth / 2 - mBottomPadding
                var endX = scrollX.toFloat() + w.toFloat() - paint.strokeWidth / 2 - mRightPadding
                var endY = startY
                var startX_border = scrollX.toFloat() + mLeftPadding
                var startY_border = scrollY.toFloat() + h - mBottomPadding
                var endX_border = scrollX.toFloat() + w.toFloat() - mRightPadding
                var endY_border = startY_border
                if (left_bottom_corner <= 0 && right_bottom_corner <= 0) {
                    linePath?.reset()
                    linePath?.moveTo(startX, startY)
                    linePath?.lineTo(endX, endY)
                    if (it.isDrawBottom && paint.strokeWidth > 0) {
                        canvas.drawPath(linePath, paint)
                        //canvas.drawLine(startX, startY, endX, endY, paint)
                    }
                    //fixme
                    borderPath?.lineTo(startX_border, startY_border)
                    borderPath?.lineTo(endX_border, endY_border)
                } else {
                    var starX0 = startX + dW / 2 * (left_bottom_corner / 90f)
                    var starY0 = startY
                    var endX2 = endX - dW / 2 * (right_bottom_corner / 90f)
                    var starX0_border = startX_border + dW / 2 * (left_bottom_corner / 90f)
                    var starY0_border = startY_border
                    var endX2_border = endX_border - dW / 2 * (right_bottom_corner / 90f)
                    linePath?.reset()
                    linePath?.moveTo(starX0, starY0)
                    linePath?.lineTo(endX2, endY)
                    if (it.isDrawBottom && paint.strokeWidth > 0) {
                        canvas.drawPath(linePath, paint)
                        //canvas.drawLine(starX0, starY0, endX2, endY, paint)
                    }
                    //fixme
                    borderPath?.lineTo(starX0_border, starY0_border)
                    borderPath?.lineTo(endX2_border, endY_border)
                }
            }
            //fixme 只显示边框以内的区域；边框以外的不显示。
            if (it.isPorterDuffXfermode) {
                //fixme 切割。
                paint.strokeWidth = 0f
                paint.style = Paint.Style.FILL//fixme 解决边框切割问题。
                //paint.strokeWidth = it.strokeWidth
                //paint.style = Paint.Style.FILL_AND_STROKE
                paint.setPathEffect(null)
                paint.setShader(null)
                paint.color = Color.WHITE
                //paint.color = Color.BLACK
                borderPath?.close()
                paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
                borderPath?.setFillType(Path.FillType.INVERSE_WINDING)//反转
                canvas.drawPath(borderPath, paint)
                borderPath?.setFillType(Path.FillType.WINDING)//恢复正常
                paint.setXfermode(null)
            }
        }
    }

    private fun getBorderEntity(): KBorderEntity {
        if (border == null) {
            border = KBorderEntity()
        }
        return border!!
    }

    private var currentBorder: KBorderEntity? = null//当前边框

    var border: KBorderEntity? = null//正常
    fun border(block: KBorderEntity.() -> Unit): K3BorderWidget {
        clearButonShadow()//自定义圆角，就去除按钮默认的圆角阴影。不然效果不好。
        block(getBorderEntity())
        invalidate()
        return this
    }

    var border_notEnable: KBorderEntity? = null//不可用
    fun border_notEnable(block: KBorderEntity.() -> Unit): K3BorderWidget {
        if (border_notEnable == null) {
            border_notEnable = getBorderEntity().copy()
        }
        block(border_notEnable!!)
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
    override fun changedNotEnabled() {
        super.changedNotEnabled()
        normal()
        border_notEnable?.let {
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
        border_notEnable = null
        borderPath?.reset()
        borderPath = null
        linePath?.reset()
        linePath = null
        currentBorder = null
    }

}
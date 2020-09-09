package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.entity.widget.compat.KBorderEntity

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
//                    all_radius=0f//所有圆角的角度（0~90）//fixme 角度统一使用radius
//                    right_bottom=45f//边框右下角角度;fixme border支持圆角
//                    //bg_color=Color.GREEN//fixme 支持背景色；具备切割效果；会像radius一样；自动去除边框以外的区域。只显示边框以内的。
//                    bgHorizontalColors(Color.RED,Color.CYAN)
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
 * fixme 绘制上下左右的边框；现在各个角已经支持圆角。
 */
open class K3BorderWidget : K3BDiamondWidget {
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
            var linearGradient: LinearGradient? = null
            if (strokeHorizontalColors != null && strokeHorizontalColors!!.size > 1) {
                //fixme 水平渐变
                linearGradient = LinearGradient(0f + scrollX, 0f, w.toFloat() + scrollX, 0f, strokeHorizontalColors, null, Shader.TileMode.CLAMP)
            }
            if (strokeVerticalColors != null && strokeVerticalColors!!.size > 1) {
                //fixme 垂直渐变
                linearGradient = LinearGradient(0f, 0f + scrollY, 0f, 0f + scrollY + h.toFloat(), strokeVerticalColors, null, Shader.TileMode.CLAMP)
            }
            paint.setShader(null)
            linearGradient?.let {
                paint.setShader(linearGradient)
            }
        }
    }

    private var borderPath: Path? = null
    private var linePath: Path? = null

    override fun draw2Bg(canvas: Canvas, paint: Paint) {
        super.draw2Bg(canvas, paint)
        currentBorder?.let {
            if (true || it.strokeWidth > 0) {
                //画背景
                var isDrawColor = false//是否画背景色
                if (it.bg_color != Color.TRANSPARENT) {
                    paint.color = it.bg_color
                    isDrawColor = true
                }
                var left = 0f + scrollX + centerX - w / 2.0f
                var top = 0f + scrollY + centerY - h / 2.0f//fixme 2.0f必须设置成浮点型，不然可能会有0.5的偏差。
                //KLoggerUtils.e("scrollY:\t"+scrollY+"\tcenterY:\t"+centerY+"\th / 2:\t"+(h / 2.0f)+"\th:\t"+h)
                var right = w + left
                var bottom = h + top
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
                    //fixme 画背景
                    var rectF = RectF(left, top, right, bottom)
                    canvas.drawRect(rectF, paint)
                }
            }
        }
    }

    //绘制边框；fixme 亲测边框的位置和圆角切割radius位置是一致的。如果位置不一致，那一定是两个控件的高度或位置不一致。
    override fun draw2Last(canvas: Canvas, paint: Paint) {
        super.draw2Last(canvas, paint)
        currentBorder?.let {
            if (true || it.strokeWidth > 0) {
                var dW = w
                if (dW > h) {
                    dW = h//以短边为基准
                }
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
                    var startX = scrollX + paint.strokeWidth / 2
                    var startY = scrollY.toFloat() + paint.strokeWidth / 2
                    var endX = startX
                    var endY = scrollY.toFloat() + h.toFloat() - paint.strokeWidth / 2
                    var startX_border = scrollX.toFloat()
                    var startY_border = scrollY.toFloat()
                    var endX_border = startX_border
                    var endY_border = scrollY.toFloat() + h.toFloat()
                    if (left_top_corner <= 0 && left_bottom_corner <= 0) {
                        linePath?.reset()
                        linePath?.moveTo(endX, endY)
                        linePath?.lineTo(startX, startY)
                        if (it.isDrawLeft) {
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
                        if (it.isDrawLeft) {
                            canvas.drawPath(path, paint)
                        }
                        if (left_bottom_corner <= 0) {
                            linePath?.reset()
                            linePath?.moveTo(endX0, endY0)
                            linePath?.lineTo(endX, endY)
                            if (it.isDrawLeft) {
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
                            if (it.isDrawLeft) {
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
                            if (it.isDrawLeft) {
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
                    var startX = scrollX.toFloat()// + paint.strokeWidth / 2
                    var startY = scrollY.toFloat() + paint.strokeWidth / 2
                    var endX = scrollX.toFloat() + w.toFloat()// - paint.strokeWidth / 2
                    var endY = startY
                    var startX_border = scrollX.toFloat()
                    var startY_border = scrollY.toFloat()
                    var endX_border = scrollX.toFloat() + w.toFloat()
                    var endY_border = startY_border
                    if (left_top_corner <= 0 && right_top_corner <= 0) {
                        linePath?.reset()
                        linePath?.moveTo(startX, startY)
                        linePath?.lineTo(endX, endY)
                        if (it.isDrawTop) {
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
                        if (it.isDrawTop) {
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
                    var startX = scrollX.toFloat() + w.toFloat() - paint.strokeWidth / 2
                    var startY = scrollY.toFloat() + paint.strokeWidth / 2
                    var endX = startX
                    var endY = scrollY.toFloat() + h.toFloat() - paint.strokeWidth / 2
                    var startX_border = scrollX.toFloat() + w.toFloat()
                    var startY_border = scrollY.toFloat()
                    var endX_border = startX_border
                    var endY_border = scrollY.toFloat() + h.toFloat()
                    if (right_top_corner <= 0 && right_bottom_corner <= 0) {
                        linePath?.reset()
                        linePath?.moveTo(startX, startY)
                        linePath?.lineTo(endX, endY)
                        if (it.isDrawRight) {
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
                        if (it.isDrawRight) {
                            canvas.drawPath(path, paint)
                        }
                        //fixme
                        borderPath?.lineTo(starX0_border, starY0_border)
                        borderPath?.quadTo(controllX, controllY, endX0_border, endY0_border)
                        if (right_bottom_corner <= 0) {
                            linePath?.reset()
                            linePath?.moveTo(endX0, endY0)
                            linePath?.lineTo(endX, endY)
                            if (it.isDrawRight) {
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
                            if (it.isDrawRight) {
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
                            if (it.isDrawRight) {
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
                    var startX = scrollX.toFloat() + paint.strokeWidth / 2
                    var startY = scrollY.toFloat() + h - paint.strokeWidth / 2
                    var endX = scrollX.toFloat() + w.toFloat() - paint.strokeWidth / 2
                    var endY = startY
                    var startX_border = scrollX.toFloat()
                    var startY_border = scrollY.toFloat() + h
                    var endX_border = startX_border + w.toFloat()
                    var endY_border = startY_border
                    if (left_bottom_corner <= 0 && right_bottom_corner <= 0) {
                        linePath?.reset()
                        linePath?.moveTo(startX, startY)
                        linePath?.lineTo(endX, endY)
                        if (it.isDrawBottom) {
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
                        if (it.isDrawBottom) {
                            canvas.drawPath(linePath, paint)
                            //canvas.drawLine(starX0, starY0, endX2, endY, paint)
                        }
                        //fixme
                        borderPath?.lineTo(starX0_border, starY0_border)
                        borderPath?.lineTo(endX2_border, endY_border)
                    }
                }
                //fixme 只显示边框以内的区域；边框以外的不显示。
                paint.strokeWidth = 0F
                paint.style = Paint.Style.FILL//fixme 解决边框切割问题。
                //paint.style = Paint.Style.FILL_AND_STROKE
                paint.setPathEffect(null)
                paint.setShader(null)
                paint.color = Color.WHITE
                borderPath?.close()
                paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
                borderPath?.setFillType(Path.FillType.INVERSE_WINDING)//反转
                canvas.drawPath(borderPath, paint)
                borderPath?.setFillType(Path.FillType.WINDING)//恢复正常
                paint.setXfermode(null)
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
        borderPath?.reset()
        borderPath = null
        linePath?.reset()
        linePath = null
    }

}
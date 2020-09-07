package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.entity.widget.compat.KDiamondEntity

//fixme rotation=45f 宽和高相等的情况下，整个控件整体旋转45度，就成正方形了。

//            调用案例
//            KView(this).apply {
//                backgroundColor(Color.GREEN)
//                    diamond {
//                        strokeWidth = kpx.x(3f) //所有边框的宽度
//                        leftTopStrokeColor = Color.BLUE//左上边边框的颜色
//                        leftTopStrokeVerticalColors(Color.RED, Color.BLUE, Color.GREEN)
//                        rightBottomStrokeVerticalColors(Color.BLUE, Color.GREEN)//右下边边框垂直渐变色
//                        leftBottomDashWidth = kpx.x(8f)//左下边框虚线
//                        leftBottomDashGap = kpx.x(8f)
//                        //all_radius = 45f//所有圆角的角度（0~90）
//                        top_radius=90f
//                        strokeColor = Color.RED
//                        bgHorizontalColors(Color.TRANSPARENT, Color.CYAN)
//                        //isBgGradient=false
//                    }
//                    diamond_press {
//                        isDrawLeft_top = false//是否绘制左边边框
//                        isDrawLeft_Bottom=false
//                        isDrawRight_top=false
//                        isDrawRight_bottom=false
//                        strokeHorizontalColors(Color.RED, Color.CYAN, Color.GRAY)
//                    }
//            }.lparams {
//                topMargin = kpx.x(50)
//                width = kpx.x(300)
//                height = kpx.x(300)
//            }

/**
 * fixme 菱形
 */
open class K3BDiamondWidget : K3AStateView {
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
    private fun initBorderPaint(paint: Paint, border: KDiamondEntity, oritaton: Int = 0) {
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
            //左上角
            if (oritaton == 1) {
                it.leftTopDashWidth?.let {
                    dashWidth = it
                }
                it.leftTopDashGap?.let {
                    dashGap = it
                }
                it.leftTopStrokeWidth?.let {
                    strokeWidth = it
                }
                it.leftTopStrokeColor?.let {
                    strokeColor = it
                }
                it.leftTopStrokeHorizontalColors?.let {
                    strokeHorizontalColors = it
                }
                it.leftTopStrokeVerticalColors?.let {
                    strokeVerticalColors = it
                }
            }
            //右上角
            if (oritaton == 2) {
                it.rightTopDashWidth?.let {
                    dashWidth = it
                }
                it.rightTopDashGap?.let {
                    dashGap = it
                }
                it.rightTopStrokeWidth?.let {
                    strokeWidth = it
                }
                it.rightTopStrokeColor?.let {
                    strokeColor = it
                }
                it.rightTopStrokeHorizontalColors?.let {
                    strokeHorizontalColors = it
                }
                it.rightTopStrokeVerticalColors?.let {
                    strokeVerticalColors = it
                }
            }
            //右下角
            if (oritaton == 3) {
                it.rightBottomDashWidth?.let {
                    dashWidth = it
                }
                it.rightBottomDashGap?.let {
                    dashGap = it
                }
                it.rightBottomStrokeWidth?.let {
                    strokeWidth = it
                }
                it.rightBottomStrokeColor?.let {
                    strokeColor = it
                }
                it.rightBottomStrokeHorizontalColors?.let {
                    strokeHorizontalColors = it
                }
                it.rightBottomStrokeVerticalColors?.let {
                    strokeVerticalColors = it
                }
            }
            //左下角
            if (oritaton == 4) {
                it.leftBottomDashWidth?.let {
                    dashWidth = it
                }
                it.leftBottomDashGap?.let {
                    dashGap = it
                }
                it.leftBottomStrokeWidth?.let {
                    strokeWidth = it
                }
                it.leftBottomStrokeColor?.let {
                    strokeColor = it
                }
                it.leftBottomStrokeHorizontalColors?.let {
                    strokeHorizontalColors = it
                }
                it.leftBottomStrokeVerticalColors?.let {
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
    override fun draw2Front(canvas: Canvas, paint: Paint) {
        super.draw2Front(canvas, paint)
        currentDiamond?.let {
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
        currentDiamond?.let {
            if (true || it.strokeWidth > 0) {
                var dW = w
                if (dW > h) {
                    dW = h//以短边为基准
                }
                var all_corner = it.all_radius
                var top_radius = it.top_radius//顶部圆角
                top_radius?.let {
                    if (it < 0 && all_corner >= 0) {
                        top_radius = all_corner
                    }
                    if (top_radius > 90f) {
                        top_radius = 90f
                    }
                }
                var left_radius = it.left_radius//左边圆角
                left_radius?.let {
                    if (it < 0 && all_corner >= 0) {
                        left_radius = all_corner
                    }
                    if (left_radius > 90f) {
                        left_radius = 90f
                    }
                }
                var right_radius = it.right_radius//右边圆角
                right_radius?.let {
                    if (it < 0 && all_corner >= 0) {
                        right_radius = all_corner
                    }
                    if (right_radius > 90f) {
                        right_radius = 90f
                    }
                }
                var bottom_radius = it.bottom_radius//低部圆角
                bottom_radius?.let {
                    if (it < 0 && all_corner >= 0) {
                        bottom_radius = all_corner
                    }
                    if (bottom_radius > 90f) {
                        bottom_radius = 90f
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
                //绘制左上角边框
                if (true || it.isDrawLeft_top) {
                    initBorderPaint(resetPaint(paint), it, 1)
                    var startX = scrollX + paint.strokeWidth / 2
                    var startY = scrollY.toFloat() + h / 2
                    var endX = startX + w / 2
                    var endY = scrollY.toFloat() //+ paint.strokeWidth / 2
                    var startX_border = scrollX.toFloat()
                    var startY_border = scrollY.toFloat() + h / 2
                    var endX_border = startX_border + w / 2
                    var endY_border = scrollY.toFloat()
                    if (top_radius <= 0 && left_radius <= 0) {
                        linePath?.reset()
                        linePath?.moveTo(startX, startY)
                        linePath?.lineTo(endX, endY)
                        if (it.isDrawLeft_top) {
                            canvas.drawPath(linePath, paint)//fixme pathEffect虚线只对Path有效果；对drawLine（）没有效果。
                            //canvas.drawLine(startX, startY, endX, endY, paint)
                        }
                        //fixme
                        borderPath?.moveTo(startX_border, startY_border)
                        borderPath?.lineTo(endX_border, endY_border)
                    } else {
                        //fixme 顶部圆角
                        var startX2 = startX
                        var startY2 = startY
                        var startX2_border = startX_border
                        var startY2_border = startY_border
//                        if (left_radius > 0) {
//                            var subW = dW / 2 * (left_radius / 90f)
//                            startX2 = startX + subW
//                            startY2 = startY - subW
//                        }
                        var endX2 = endX
                        var endY2 = endY
                        var endX2_border = endX_border
                        var endY2_border = endY_border
                        if (top_radius > 0) {
                            var subW = dW / 2 * (top_radius / 90f)
                            endX2 = endX - subW
                            endY2 = endY + subW
                            endX2_border = endX_border - subW
                            endY2_border = endY_border + subW
                        }
                        linePath?.reset()
                        linePath?.moveTo(startX2, startY2)
                        linePath?.lineTo(endX2, endY2)
                        //fixme
                        borderPath?.moveTo(startX2_border, startY2_border)
                        borderPath?.lineTo(endX2_border, endY2_border)
//                        if (top_radius > 0) {
//                            var controllX = startX2
//                            var controllY = startY2
//                            var subW = dW / 2 * (top_radius / 90f)
//                            var endX3 = endX + subW
//                            var endY3 = endY + subW
//                            linePath?.quadTo(controllX, controllY, endX3, endY3)
//                            //fixme
//                            borderPath?.quadTo(controllX, controllY, endX3, endY3)
//                        }
                        if (it.isDrawLeft_top) {
                            canvas.drawPath(linePath, paint)
                        }
                    }
                }
                //绘制右上边的边框
                if (true || it.isDrawRight_top) {
                    initBorderPaint(resetPaint(paint), it, 2)
                    var startX = scrollX.toFloat() + w / 2
                    var startY = scrollY.toFloat() + paint.strokeWidth / 2
                    var endX = scrollX.toFloat() + w.toFloat() - paint.strokeWidth / 2
                    var endY = scrollY.toFloat() + h / 2
                    var startX_border = scrollX.toFloat() + w / 2
                    var startY_border = scrollY.toFloat()
                    var endX_border = scrollX.toFloat() + w.toFloat()
                    var endY_border = scrollY.toFloat() + h / 2
                    if (true||top_radius <= 0 && right_radius <= 0) {
                        linePath?.reset()
                        linePath?.moveTo(startX, startY)
                        linePath?.lineTo(endX, endY)
                        if (it.isDrawRight_top) {
                            canvas.drawPath(linePath, paint)
                            //canvas.drawLine(startX, startY, endX, endY, paint)
                        }
                        //fixme
                        borderPath?.lineTo(startX_border, startY_border)
                        borderPath?.lineTo(endX_border, endY_border)
                    } else {
                        //fixme 右边圆角
                        var startX2 = startX
                        var startY2 = startY
                        if (top_radius > 0) {
                            var subW = dW / 2 * (top_radius / 90f)
                            startX2 = startX + subW
                            startY2 = startY + subW
                        }
                        var endX2 = endX
                        var endY2 = endY
                        if (right_radius > 0) {
                            var subW = dW / 2 * (right_radius / 90f)
                            endX2 = endX - subW
                            endY2 = endY - subW
                        }
                        linePath?.reset()
                        linePath?.moveTo(startX2, startY2)
                        linePath?.lineTo(endX2, endY2)
                        //fixme
                        borderPath?.moveTo(startX2, startY2)
                        borderPath?.lineTo(endX2, endY2)
                        if (right_radius > 0) {
                            var controllX = startX2
                            var controllY = startY2
                            var subW = dW / 2 * (top_radius / 90f)
                            var endX3 = endX - subW
                            var endY3 = endY + subW
                            linePath?.quadTo(controllX, controllY, endX3, endY3)
                            //fixme
                            borderPath?.quadTo(controllX, controllY, endX3, endY3)
                        }
                        if (it.isDrawLeft_top) {
                            canvas.drawPath(linePath, paint)
                        }
                    }
                }
                //绘制右下角边框
                if (true || it.isDrawRight_bottom) {
                    initBorderPaint(resetPaint(paint), it, 3)
                    var startX = scrollX.toFloat() + w.toFloat() - paint.strokeWidth / 2
                    var startY = scrollY.toFloat() + h / 2
                    var endX = scrollX.toFloat() + w / 2
                    var endY = scrollY.toFloat() + h.toFloat() - paint.strokeWidth / 2
                    var startX_border = scrollX.toFloat() + w.toFloat()
                    var startY_border = scrollY.toFloat() + h / 2
                    var endX_border = scrollX.toFloat() + w / 2
                    var endY_border = scrollY.toFloat() + h.toFloat()
                    if (right_radius <= 0 && bottom_radius <= 0) {
                        linePath?.reset()
                        linePath?.moveTo(startX, startY)
                        linePath?.lineTo(endX, endY)
                        if (it.isDrawRight_bottom) {
                            canvas.drawPath(linePath, paint)
                            //canvas.drawLine(startX, startY, endX, endY, paint)
                        }
                        //fixme
                        borderPath?.lineTo(startX_border, startY_border)
                        borderPath?.lineTo(endX_border, endY_border)
                    } else {
                        //右上角圆角
//                        var path = Path()
//                        var starX0 = startX - dW / 2 * (right_radius / 90f)
//                        var starY0 = startY
//                        var starX0_border = startX_border - dW / 2 * (right_radius / 90f)
//                        var starY0_border = startY_border
//                        var controllX = startX
//                        var controllY = startY
//                        var endX0 = startX
//                        var endY0 = startY + dW / 2 * (right_radius / 90f)
//                        var endX0_border = startX_border
//                        var endY0_border = startY_border + dW / 2 * (right_radius / 90f)
//                        path.moveTo(starX0, starY0)
//                        path.quadTo(controllX, controllY, endX0, endY0)
//                        if (it.isDrawRight_bottom) {
//                            canvas.drawPath(path, paint)
//                        }
//                        //fixme
//                        borderPath?.lineTo(starX0_border, starY0_border)
//                        borderPath?.quadTo(controllX, controllY, endX0_border, endY0_border)
//                        if (bottom_radius <= 0) {
//                            linePath?.reset()
//                            linePath?.moveTo(endX0, endY0)
//                            linePath?.lineTo(endX, endY)
//                            if (it.isDrawRight_bottom) {
//                                canvas.drawPath(linePath, paint)
//                                //canvas.drawLine(endX0, endY0, endX, endY, paint)
//                            }
//                            //fixme
//                            borderPath?.lineTo(endX0_border, endY0_border)
//                            borderPath?.lineTo(endX_border, endY_border)
//                        } else {
//                            //右下角圆角
//                            var endY1 = endY - dW / 2 * (bottom_radius / 90f)
//                            var endY1_border = endY_border - dW / 2 * (bottom_radius / 90f)
//                            linePath?.reset()
//                            linePath?.moveTo(endX0, endY0)
//                            linePath?.lineTo(endX, endY1)
//                            if (it.isDrawRight_bottom) {
//                                canvas.drawPath(linePath, paint)
//                                //canvas.drawLine(endX0, endY0, endX, endY1, paint)
//                            }
//                            //fixme
//                            borderPath?.lineTo(endX0_border, endY0_border)
//                            borderPath?.lineTo(endX_border, endY1_border)
//                            var path2 = Path()
//                            path2.moveTo(endX, endY1)
//                            var controllX = endX
//                            var controllY = endY
//                            var endX2 = endX - dW / 2 * (bottom_radius / 90f)
//                            var endY2 = endY
//                            var endX2_border = endX_border - dW / 2 * (bottom_radius / 90f)
//                            var endY2_border = endY_border
//                            path2.quadTo(controllX, controllY, endX2, endY2)
//                            if (it.isDrawRight_bottom) {
//                                canvas.drawPath(path2, paint)
//                            }
//                            //fixme
//                            borderPath?.lineTo(endX_border, endY1_border)
//                            borderPath?.quadTo(controllX, controllY, endX2_border, endY2_border)
//                        }
                    }
                }
                //绘制左下角边框
                if (true || it.isDrawLeft_Bottom) {
                    initBorderPaint(resetPaint(paint), it, 4)
                    var startX = scrollX.toFloat() + w / 2
                    var startY = scrollY.toFloat() + h - paint.strokeWidth / 2
                    var endX = scrollX.toFloat() + paint.strokeWidth / 2
                    var endY = scrollY.toFloat() + h / 2
                    var startX_border = scrollX.toFloat() + w / 2
                    var startY_border = scrollY.toFloat() + h
                    var endX_border = scrollX.toFloat()
                    var endY_border = scrollY.toFloat() + h / 2
                    if (left_radius <= 0 && bottom_radius <= 0) {
                        linePath?.reset()
                        linePath?.moveTo(startX, startY)
                        linePath?.lineTo(endX, endY)
                        if (it.isDrawLeft_Bottom) {
                            canvas.drawPath(linePath, paint)
                            //canvas.drawLine(startX, startY, endX, endY, paint)
                        }
                        //fixme
                        borderPath?.lineTo(startX_border, startY_border)
                        borderPath?.lineTo(endX_border, endY_border)
                    } else {
//                        var starX0 = startX + dW / 2 * (left_radius / 90f)
//                        var starY0 = startY
//                        var endX2 = endX - dW / 2 * (bottom_radius / 90f)
//                        var starX0_border = startX_border + dW / 2 * (left_radius / 90f)
//                        var starY0_border = startY_border
//                        var endX2_border = endX_border - dW / 2 * (bottom_radius / 90f)
//                        linePath?.reset()
//                        linePath?.moveTo(starX0, starY0)
//                        linePath?.lineTo(endX2, endY)
//                        if (it.isDrawLeft_Bottom) {
//                            canvas.drawPath(linePath, paint)
//                            //canvas.drawLine(starX0, starY0, endX2, endY, paint)
//                        }
//                        //fixme
//                        borderPath?.lineTo(starX0_border, starY0_border)
//                        borderPath?.lineTo(endX2_border, endY_border)
                    }
                }
                //fixme 只显示边框以内的区域；边框以外的不显示。
                paint.strokeWidth = 0F
                paint.style = Paint.Style.FILL//fixme 解决边框切割问题。
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

    private fun getDiamondEntity(): KDiamondEntity {
        if (diamond == null) {
            diamond = KDiamondEntity()
        }
        return diamond!!
    }

    private var currentDiamond: KDiamondEntity? = null//当前边框

    var diamond: KDiamondEntity? = null//正常
    fun diamond(block: KDiamondEntity.() -> Unit): K3BDiamondWidget {
        clearButonShadow()//自定义圆角，就去除按钮默认的圆角阴影。不然效果不好。
        block(getDiamondEntity())
        invalidate()
        return this
    }

    var diamond_enable: KDiamondEntity? = null//不可用
    fun diamond_enable(block: KDiamondEntity.() -> Unit): K3BDiamondWidget {
        if (diamond_enable == null) {
            diamond_enable = getDiamondEntity().copy()
        }
        block(diamond_enable!!)
        invalidate()
        return this
    }

    var diamond_press: KDiamondEntity? = null//按下
    fun diamond_press(block: KDiamondEntity.() -> Unit): K3BDiamondWidget {
        if (diamond_press == null) {
            diamond_press = getDiamondEntity().copy()
        }
        block(diamond_press!!)
        invalidate()
        return this
    }

    var diamond_focuse: KDiamondEntity? = null//聚焦
    fun diamond_focuse(block: KDiamondEntity.() -> Unit): K3BDiamondWidget {
        if (diamond_focuse == null) {
            diamond_focuse = getDiamondEntity().copy()
        }
        block(diamond_focuse!!)
        invalidate()
        return this
    }

    var diamond_hove: KDiamondEntity? = null//悬浮
    fun diamond_hove(block: KDiamondEntity.() -> Unit): K3BDiamondWidget {
        if (diamond_hove == null) {
            diamond_hove = getDiamondEntity().copy()
        }
        block(diamond_hove!!)
        invalidate()
        return this
    }

    var diamond_selected: KDiamondEntity? = null//选中
    fun diamond_selected(block: KDiamondEntity.() -> Unit): K3BDiamondWidget {
        if (diamond_selected == null) {
            diamond_selected = getDiamondEntity().copy()
        }
        block(diamond_selected!!)
        invalidate()
        return this
    }

    private fun normal() {
        currentDiamond = diamond
    }

    //状态：聚焦
    override fun changedFocused() {
        super.changedFocused()
        normal()
        diamond_focuse?.let {
            currentDiamond = it
        }
    }

    //状态：悬浮
    override fun changedHovered() {
        super.changedHovered()
        normal()
        diamond_hove?.let {
            currentDiamond = it
        }
    }

    //状态：选中
    override fun changedSelected() {
        super.changedSelected()
        normal()
        diamond_selected?.let {
            currentDiamond = it
        }
    }

    //状态：不可用
    override fun changedEnabled() {
        super.changedEnabled()
        normal()
        diamond_enable?.let {
            currentDiamond = it
        }
    }

    //状态：按下
    override fun changedPressed() {
        super.changedPressed()
        normal()
        diamond_press?.let {
            currentDiamond = it
        }
    }

    //状态：正常
    override fun changedNormal() {
        super.changedNormal()
        normal()
    }

    override fun onDestroy() {
        super.onDestroy()
        diamond = null
        diamond_press = null
        diamond_hove = null
        diamond_focuse = null
        diamond_selected = null
        diamond_enable = null
        borderPath?.reset()
        borderPath = null
        linePath?.reset()
        linePath = null
    }

}
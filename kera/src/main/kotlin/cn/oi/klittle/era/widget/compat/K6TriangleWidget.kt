package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.base.KBaseView
import cn.oi.klittle.era.entity.widget.compat.KTriangleEntity

//                                fixme 使用案例
//                                KView(this).apply {
//                                    triangle {
//                                        width = kpx.x(128)
//                                        height = height
//                                        subWidth = kpx.x(85)//fixme 减去的宽度，用于实现矩形效果。
//                                        bgHorizontalColors(Color.parseColor("#DC4E41"), Color.parseColor("#C97676"))
//                                        text = "DEBUG"
//                                        isBold = true
//                                        textColor = Color.WHITE
//                                        textSize = kpx.x(26f)
//                                        //strokeColor = textColor
//                                        //strokeHorizontalColors(Color.parseColor("#DC4E41"),textColor,textColor,textColor, Color.parseColor("#FF8080"))
//                                        //strokeWidth = kpx.x(1f)
//                                        //dashWidth = kpx.x(15f)
//                                        //dashGap = kpx.x(10f)
//                                    }
//                                }.lparams {
//                                    width = kpx.x(200)
//                                    height = width
//                                }

//                                //fixme 防Flutter右上角DEBUG图标
//                                KView(this).apply {
//                                    mInit {
//                                        setLayerType(View.LAYER_TYPE_SOFTWARE, null)//阴影必须关闭硬件加速
//                                    }
//                                    var shadow_radius = kpx.x(15f)
//                                    var shadow_dx = kpx.x(0f)
//                                    var shadow_dy = kpx.x(0f)
//                                    var shadow_color = Color.BLACK//阴影
//                                    draw { canvas, paint ->
//                                        var flagX = 0f
//                                        var flagWidth = kpx.x(30)//宽度
//                                        var path = Path()
//                                        path.moveTo(flagX, 0f)
//                                        path.lineTo(flagX + flagWidth, 0f)
//                                        path.lineTo(w.toFloat(), h - flagX - flagWidth)
//                                        path.lineTo(w.toFloat(), h - flagX)
//                                        path.close()
//                                        paint.style = Paint.Style.FILL_AND_STROKE
//                                        paint.strokeWidth = 0f
//                                        paint.color = Color.parseColor("#7C3850")//颜色
//                                        paint.setShadowLayer(shadow_radius, shadow_dx, shadow_dy, shadow_color)
//                                        canvas.drawPath(path, paint)
//                                        //字体
//                                        paint.color = Color.WHITE
//                                        paint.textAlign = Paint.Align.CENTER
//                                        paint.textSize = kpx.x(22f)
//                                        paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
//                                        canvas.save()
//                                        var textX = w / 2f
//                                        var textY = h / 2f
//                                        canvas.rotate(45f, textX, textY)//旋转
//                                        canvas.drawText("DEBUG", textX, textY - paint.textSize / 7, paint)
//                                        canvas.restore()
//                                    }
//                                }.lparams {
//                                    width = kpx.x(105)
//                                    height = width
//                                    alignParentRight()
//                                }

/**
 * 画以左上角为基准，直角三角形。
 * fixme 新增圆形清除功能。clearCircle()
 */
open class K6TriangleWidget : K5LparamWidget {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    //按下
    var triangle_press: KTriangleEntity? = null

    fun triangle_press(block: KTriangleEntity.() -> Unit): K6TriangleWidget {
        if (triangle_press == null) {
            triangle_press = getmTriangle().copy()//整个属性全部复制过来。
        }
        block(triangle_press!!)
        invalidate()
        return this
    }

    //鼠标悬浮
    var triangle_hover: KTriangleEntity? = null

    fun triangle_hover(block: KTriangleEntity.() -> Unit): K6TriangleWidget {
        if (triangle_hover == null) {
            triangle_hover = getmTriangle().copy()//整个属性全部复制过来。
        }
        block(triangle_hover!!)
        invalidate()
        return this
    }

    //聚焦
    var triangle_focuse: KTriangleEntity? = null

    fun triangle_focuse(block: KTriangleEntity.() -> Unit): K6TriangleWidget {
        if (triangle_focuse == null) {
            triangle_focuse = getmTriangle().copy()//整个属性全部复制过来。
        }
        block(triangle_focuse!!)
        invalidate()
        return this
    }

    //选中
    var triangle_selected: KTriangleEntity? = null

    fun triangle_selected(block: KTriangleEntity.() -> Unit): K6TriangleWidget {
        if (triangle_selected == null) {
            triangle_selected = getmTriangle().copy()//整个属性全部复制过来。
        }
        block(triangle_selected!!)
        invalidate()
        return this
    }

    //fixme 正常状态（先写正常样式，再写其他状态的样式，因为其他状态的样式初始值是复制正常状态的样式的。）
    var triangle: KTriangleEntity? = null

    fun getmTriangle(): KTriangleEntity {
        if (triangle == null) {
            triangle = KTriangleEntity()
        }
        return triangle!!
    }

    fun triangle(block: KTriangleEntity.() -> Unit): K6TriangleWidget {
        block(getmTriangle())
        getmTriangle().initMeasure()
        invalidate()
        return this
    }


    //fixme 三角形，在AutoBg图片的上面。
    override fun draw2Front(canvas: Canvas, paint: Paint) {
        super.draw2Front(canvas, paint)
        drawTriangle(canvas, paint, this)
    }

    var triangleModel: KTriangleEntity? = null
    fun drawTriangle(canvas: Canvas, paint: Paint, view: View) {
        view?.apply {
            if (triangle != null) {
                triangleModel = null
                if (isPressed && triangle_press != null) {
                    //按下
                    triangleModel = triangle_press
                } else if (isHovered && triangle_hover != null) {
                    //鼠标悬浮
                    triangleModel = triangle_hover
                } else if (isFocused && triangle_focuse != null) {
                    //聚焦
                    triangleModel = triangle_focuse
                } else if (isSelected && triangle_selected != null) {
                    //选中
                    triangleModel = triangle_selected
                }
                //正常
                if (triangleModel == null) {
                    triangleModel = triangle
                }
                triangleModel?.let {
                    if (it.isDraw && it.width > 0 && it.height > 0) {
                        paint.setShader(null)
                        drawTriangle(canvas, paint, it, view)
                        paint.setShader(null)//防止其他地方受影响，所以渲染清空。
                    }
                }
            }
        }
    }

    //画三角形。
    open fun drawTriangle(canvas: Canvas, paint: Paint, triangle: KTriangleEntity, view: View) {
        view?.apply {
            var scrollX = view.scrollX
            var scrollY = view.scrollY
            //画三角形内部
            paint.style = Paint.Style.FILL
            paint.color = triangle.bg_color
            if (triangle.bgVerticalColors != null) {
                var shader: LinearGradient? = null
                var colors = triangle.bgVerticalColors
                if (!triangle.isBgGradient) {
                    //垂直不渐变
                    colors = getNotLinearGradientColors(triangle.getTriangleHeight(), colors!!)
                }
                //垂直渐变，优先级高于水平(渐变颜色值数组必须大于等于2，不然异常)(从左往右，以斜边上的高为标准，进行渐变)
                if (shader == null) {
                    shader = LinearGradient(triangle.x.toFloat() + scrollX, triangle.y.toFloat() + scrollY, triangle.x.toFloat() + triangle.getTriangleHeightX() + scrollX, triangle.y.toFloat() + triangle.getTriangleHeightY() + scrollY, colors, null, Shader.TileMode.MIRROR)
                }
                paint.setShader(shader)
            } else if (triangle.bgHorizontalColors != null) {
                var shader: LinearGradient? = null
                var colors = triangle.bgHorizontalColors
                if (!triangle.isBgGradient) {
                    //水平不渐变
                    colors = getNotLinearGradientColors(triangle.getHypotenuse(), colors!!)
                }
                //水平渐变(从左往右，以斜边为标准，进行渐变)
                if (shader == null) {
                    shader = LinearGradient(triangle.x.toFloat() + scrollX, (triangle.y + triangle.height).toFloat() + scrollY, triangle.x.toFloat() + triangle.width + scrollX, triangle.y.toFloat() + scrollY, colors, null, Shader.TileMode.MIRROR)
                }
                paint.setShader(shader)
            }
            var path = Path()
            path.moveTo(triangle.x.toFloat() + triangle.subWidth + scrollX, triangle.y.toFloat() + scrollY)//左上角，顶点
            path.lineTo(triangle.x.toFloat() + triangle.width + scrollX, triangle.y.toFloat() + scrollY)//长度
            path.lineTo(triangle.x.toFloat() + scrollX, triangle.y.toFloat() + triangle.height + scrollY)//高度
            path.lineTo(triangle.x.toFloat() + scrollX, triangle.y.toFloat() + triangle.subHeight + scrollY)
            path.close()
            canvas.drawPath(path, paint)
            paint.setShader(null)
            //画边框
            if (triangle.strokeWidth > 0) {
                //画三角形边框
                paint.style = Paint.Style.STROKE
                paint.color = triangle.strokeColor
                paint.strokeWidth = triangle.strokeWidth
                paint.strokeCap = Paint.Cap.BUTT
                paint.strokeJoin = Paint.Join.MITER
                if (triangle.strokeVerticalColors != null) {
                    var shader: LinearGradient? = null
                    var colors = triangle.strokeVerticalColors
                    if (!triangle.isStrokeGradient) {
                        //垂直不渐变
                        colors = getNotLinearGradientColors(triangle.getTriangleHeight(), colors!!)
                    }
                    //垂直渐变，优先级高于水平(渐变颜色值数组必须大于等于2，不然异常)(从左往右，以斜边上的高为标准，进行渐变)
                    if (shader == null) {
                        shader = LinearGradient(triangle.x.toFloat() + scrollX, triangle.y.toFloat() + scrollY, triangle.x.toFloat() + triangle.getTriangleHeightX() + scrollX, triangle.y.toFloat() + triangle.getTriangleHeightY() + scrollY, colors, null, Shader.TileMode.MIRROR)
                    }
                    paint.setShader(shader)
                } else if (triangle.strokeHorizontalColors != null) {
                    var shader: LinearGradient? = null
                    var colors = triangle.strokeHorizontalColors
                    if (!triangle.isStrokeGradient) {
                        //水平不渐变
                        colors = getNotLinearGradientColors(triangle.getHypotenuse(), colors!!)
                    }
                    //水平渐变(从左往右，以斜边为标准，进行渐变)
                    if (shader == null) {
                        shader = LinearGradient(triangle.x.toFloat() + scrollX, (triangle.y + triangle.height).toFloat() + scrollY, triangle.x.toFloat() + triangle.width + scrollX, triangle.y.toFloat() + scrollY, colors, null, Shader.TileMode.MIRROR)
                    }
                    paint.setShader(shader)
                }
                path.reset()
                path.moveTo(triangle.x.toFloat() + triangle.strokeWidth / 2 + triangle.subWidth + scrollX, triangle.y.toFloat() + triangle.strokeWidth / 2 + scrollY)//左上角，顶点
                path.lineTo(triangle.x.toFloat() + triangle.width - triangle.strokeWidth + scrollX, triangle.y.toFloat() + triangle.strokeWidth / 2 + scrollY)//长度
                path.lineTo(triangle.x.toFloat() + triangle.strokeWidth / 2 + scrollX, triangle.y.toFloat() + triangle.height - triangle.strokeWidth + scrollY)//高度
                path.lineTo(triangle.x.toFloat() + triangle.strokeWidth / 2 + scrollX, triangle.y.toFloat() - triangle.strokeWidth + triangle.subHeight + scrollY)
                path.close()
                //虚线
                if (triangle.dashWidth > 0 && triangle.dashGap > 0) {
                    var dashPathEffect = DashPathEffect(floatArrayOf(triangle.dashWidth, triangle.dashGap), trianglePhase)
                    paint.setPathEffect(dashPathEffect)
                }
                canvas.drawPath(path, paint)
                paint.setShader(null)
                paint.setPathEffect(null)
                //控制虚线流动性
                if (triangle.isdashFlow && (triangle.dashWidth > 0 && triangle.dashGap > 0)) {
                    if (triangle.dashSpeed > 0) {
                        if (trianglePhase >= Float.MAX_VALUE - triangle.dashSpeed) {
                            trianglePhase = 0f
                        }
                    } else {
                        if (trianglePhase >= Float.MIN_VALUE - triangle.dashSpeed) {
                            trianglePhase = 0f
                        }
                    }
                    trianglePhase += triangle.dashSpeed
                    invalidate()
                }
            }
            //画文本
            if (triangle.text != null && triangle.text!!.trim().length > 0) {

                //保存画布状态，后面还要恢复。
                if (triangle.textRotation != 0f) {
                    canvas.save()
                }
                paint.color = triangle.textColor
                paint.style = Paint.Style.FILL
                paint.textSize = triangle.textSize
                //fixme 文本位置，以斜边上高的交点为基准（起点）。textLeftPadding控制左边的距离，textTopPadding控制顶部的距离。
                paint.textAlign = Paint.Align.CENTER
                var l = triangle.x + triangle.width.toFloat() / 2.toFloat() + scrollX
                var t = triangle.y + triangle.height.toFloat() / 2.toFloat() + scrollY
                if (triangle.textRotation != 0f) {
                    canvas.rotate(triangle.textRotation, l, t)
                }
                l = l + triangle.textLeftPadding
                t = t + triangle.textTopPadding

                if (triangle.isBold) {
                    paint.isFakeBoldText = true
                } else {
                    paint.isFakeBoldText = false
                }
                //自定义字体
                if (triangle.typeface != null) {
                    paint.setTypeface(triangle.typeface)
                }
                //实现斜体效果，负数表示右斜，正数左斜
                paint.setTextSkewX(triangle.skewX)
                //paint.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC))//这个会覆盖自定义字体，所以不使用
                canvas.drawText(triangle.text, l, t, paint)

                //恢复画布状态。
                paint.setTextSkewX(0f)
                paint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
                if (triangle.textRotation != 0f) {
                    canvas.restore()
                }
            }
        }
    }

    var trianglePhase: Float = 0F

    private var clearPaint: Paint? = null
    fun getClearPaint(): Paint {
        if (clearPaint == null) {
            clearPaint = KBaseView.getPaint()
            clearPaint?.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
        }
        return clearPaint!!
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        /**
         * 清楚圆，该圆区域的内容会被清空。
         */
        canvas?.apply {
            kCirCles?.let {
                it.forEach {
                    if (it.mRadius > 0) {
                        drawCircle(it.mCx, it.mCy, it.mRadius, getClearPaint())
                    }
                }
            }
        }
    }

    class KCirCle {
        var mCx: Float = 0f
        var mCy: Float = 0f
        var mRadius: Float = 0f
    }

    var kCirCles: MutableList<KCirCle>? = null
    /**
     * 清除圆，该圆的内容会被清空
     * @param cx 圆心x坐标
     * @param cy 圆心y坐标
     * @param radius 半径
     */
    fun clearCircle(cx: Float, cy: Float, radius: Float) {
        if (kCirCles == null) {
            kCirCles = mutableListOf()
        }
        var circle = KCirCle()
        circle.mCx = cx
        circle.mCy = cy
        circle.mRadius = radius
        kCirCles?.add(circle)//可以清除多个圆
    }

    override fun onDestroy() {
        super.onDestroy()
        triangle = null
        triangle_focuse = null
        triangle_hover = null
        triangle_press = null
        triangle_selected = null
        triangleModel = null
        kCirCles?.clear()
        kCirCles = null
        clearPaint = null
    }

}
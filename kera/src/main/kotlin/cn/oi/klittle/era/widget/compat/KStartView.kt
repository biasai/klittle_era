package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.entity.widget.compat.KStartEntity
import cn.oi.klittle.era.utils.KLoggerUtils

//                fixme 使用案例：
//                kstartView {
//                    backgroundColor(Color.RED)
//                    start {
//                        bg_color=Color.YELLOW
//                        bgHorizontalColors(Color.BLACK,Color.WHITE)
//                        //bgVerticalColors(Color.BLACK,Color.WHITE)
//                        isBgGradient=false
//                    }
//                    start_press {
//                        strokeHorizontalColors(Color.WHITE,Color.BLACK)
//                        strokeColor=Color.BLUE
//                        strokeWidth=kpx.x(3f)
//                        bg_color=Color.LTGRAY
//                        dashWidth=kpx.x(15f)
//                        dashGap=dashWidth
//                        all_radius=kpx.x(45f)
//                        //bgVerticalColors(Color.YELLOW,Color.CYAN)
//                        //isBgGradient=false
//                        isPorterDuffXfermode=true//fixme 切割时，建议不要使用圆角。不然效果不好。
//                    }
//                }.lparams {
//                    width=kpx.x(300)
//                    height=kpx.x(200)
//                }

/**
 * fixme 五角星控件（居中绘制，短边决定了五角星的大小）;具备切割能力(isPorterDuffXfermode为true时有效)。有圆角效果时，切割效果不好。有切割时，最好不要有圆角。（切割对CornerPathEffect不支持）
 * 参考：https://blog.csdn.net/qq_15364915/article/details/75433651
 */
open class KStartView : KView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    //按下
    var start_press: KStartEntity? = null

    fun start_press(block: KStartEntity.() -> Unit): KStartView {
        if (start_press == null) {
            start_press = getmStart().copy()//整个属性全部复制过来。
        }
        block(start_press!!)
        invalidate()
        return this
    }

    //鼠标悬浮
    var start_hover: KStartEntity? = null

    fun start_hover(block: KStartEntity.() -> Unit): KStartView {
        if (start_hover == null) {
            start_hover = getmStart().copy()//整个属性全部复制过来。
        }
        block(start_hover!!)
        invalidate()
        return this
    }

    //聚焦
    var start_focuse: KStartEntity? = null

    fun start_focuse(block: KStartEntity.() -> Unit): KStartView {
        if (start_focuse == null) {
            start_focuse = getmStart().copy()//整个属性全部复制过来。
        }
        block(start_focuse!!)
        invalidate()
        return this
    }

    //选中
    var start_selected: KStartEntity? = null

    fun start_selected(block: KStartEntity.() -> Unit): KStartView {
        if (start_selected == null) {
            start_selected = getmStart().copy()//整个属性全部复制过来。
        }
        block(start_selected!!)
        invalidate()
        return this
    }

    //fixme 正常状态（先写正常样式，再写其他状态的样式，因为其他状态的样式初始值是复制正常状态的样式的。）
    var start: KStartEntity? = null

    fun getmStart(): KStartEntity {
        if (start == null) {
            start = KStartEntity()
        }
        return start!!
    }

    fun start(block: KStartEntity.() -> Unit): KStartView {
        block(getmStart())
        invalidate()
        return this
    }


    override fun draw2Front(canvas: Canvas, paint: Paint) {
        super.draw2Front(canvas, paint)
        drawStart(canvas, paint, this)
    }

    var startModel: KStartEntity? = null
    private fun drawStart(canvas: Canvas, paint: Paint, view: View) {
        view?.apply {
            if (start != null) {
                startModel = null
                if (isPressed && start_press != null) {
                    //按下
                    startModel = start_press
                } else if (isHovered && start_hover != null) {
                    //鼠标悬浮
                    startModel = start_hover
                } else if (isFocused && start_focuse != null) {
                    //聚焦
                    startModel = start_focuse
                } else if (isSelected && start_selected != null) {
                    //选中
                    startModel = start_selected
                }
                //正常
                if (startModel == null) {
                    startModel = start
                }
                startModel?.let {
                    if (it.isDraw) {
                        paint.setShader(null)
                        drawStart(canvas, paint, it, view)
                        paint.setShader(null)//防止其他地方受影响，所以渲染清空。
                    }
                }
            }
        }
    }

    //画五角星。
    private fun drawStart(canvas: Canvas, paint: Paint, triangle: KStartEntity, view: View) {
        view?.apply {
            var rw = width
            if (rw > height) {
                rw = height
            }
            var floats: FloatArray? = fivePoints(scrollX + width / 2f, scrollY.toFloat(), rw)
            var d = scrollY.toFloat()
            floats?.let {
                if (it.size > 4) {
                    d = it[3]
                    d = height - d - scrollY
                    d = d / 2 + scrollY
                }
            }
            floats = fivePoints(scrollX + width / 2f, d, rw)
            var scrollX = view.scrollX
            var scrollY = view.scrollY
            //fixme 旋转
            if (triangle.rotation != 0f) {
                canvas.save()
                var rl = scrollX + width / 2f
                var rt = scrollY + height / 2f
                canvas.rotate(triangle.rotation, rl, rt)
            }
            //画三角形内部
            paint.style = Paint.Style.FILL
            paint.color = triangle.bg_color
            if (triangle.bgVerticalColors != null) {
                var shader: LinearGradient? = null
                var colors = triangle.bgVerticalColors
                if (!triangle.isBgGradient) {
                    //垂直不渐变
                    colors = getNotLinearGradientColors(height, colors!!)
                }
                //垂直渐变，优先级高于水平(渐变颜色值数组必须大于等于2，不然异常)(从左往右，以斜边上的高为标准，进行渐变)
                if (shader == null) {
                    shader = LinearGradient(scrollX.toFloat(),  scrollY.toFloat(), scrollX.toFloat(), height.toFloat() + scrollY, colors, null, Shader.TileMode.MIRROR)
                }
                paint.setShader(shader)
            } else if (triangle.bgHorizontalColors != null) {
                var shader: LinearGradient? = null
                var colors = triangle.bgHorizontalColors
                if (!triangle.isBgGradient) {
                    //水平不渐变
                    colors = getNotLinearGradientColors(width, colors!!)
                }
                //水平渐变(从左往右，以斜边为标准，进行渐变)
                if (shader == null) {
                    shader = LinearGradient( scrollX.toFloat(), scrollY.toFloat(), width.toFloat() + scrollX,  scrollY.toFloat(), colors, null, Shader.TileMode.MIRROR)
                }
                paint.setShader(shader)
            }
            var path = Path()
            //KLoggerUtils.e("五角星坐标：\t"+floats?.size)
            if (floats != null) {
                var i = 2
                path.moveTo(floats[0], floats[1])
                while (i < floats.size - 1) {
                    path.lineTo(floats.get(i), floats.get(1.let { i += it; i }))
                    i++
                }
                path.close()
            }
            if (triangle.all_radius != 0f) {
                paint?.setPathEffect(CornerPathEffect(triangle.all_radius))
            } else {
                paint?.setPathEffect(null)
            }
            canvas.drawPath(path, paint)
            //画边框
            if (triangle.strokeWidth > 0) {
                //paint.setShader(null)
                //paint?.setPathEffect(null)
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
                        colors = getNotLinearGradientColors(height, colors!!)
                    }
                    //垂直渐变，优先级高于水平(渐变颜色值数组必须大于等于2，不然异常)(从左往右，以斜边上的高为标准，进行渐变)
                    if (shader == null) {
                        shader = LinearGradient(scrollX.toFloat(),  scrollY.toFloat(), scrollX.toFloat(), height.toFloat() + scrollY, colors, null, Shader.TileMode.MIRROR)
                    }
                    paint.setShader(shader)
                } else if (triangle.strokeHorizontalColors != null) {
                    var shader: LinearGradient? = null
                    var colors = triangle.strokeHorizontalColors
                    if (!triangle.isStrokeGradient) {
                        //水平不渐变
                        colors = getNotLinearGradientColors(width, colors!!)
                    }
                    //水平渐变(从左往右，以斜边为标准，进行渐变)
                    if (shader == null) {
                        shader = LinearGradient( scrollX.toFloat(), scrollY.toFloat(), width.toFloat() + scrollX, scrollY.toFloat(), colors, null, Shader.TileMode.MIRROR)
                    }
                    paint.setShader(shader)
                }
                path.reset()
                if (floats != null) {
                    var i = 2
                    path.moveTo(floats[0], floats[1])
                    while (i < floats.size - 1) {
                        path.lineTo(floats.get(i), floats.get(1.let { i += it; i }))
                        i++
                    }
                    path.close()
                }
                var cornerPathEffect: CornerPathEffect? = null
                if (triangle.all_radius != 0f) {
                    cornerPathEffect = CornerPathEffect(triangle.all_radius)
                }
                //虚线
                if (triangle.dashWidth > 0 && triangle.dashGap > 0) {
                    var dashPathEffect = DashPathEffect(floatArrayOf(triangle.dashWidth, triangle.dashGap), startPhase)
                    if (cornerPathEffect == null) {
                        paint?.setPathEffect(dashPathEffect)
                    } else {
                        paint?.setPathEffect(ComposePathEffect(dashPathEffect, cornerPathEffect))//圆角+虚线
                    }
                } else if (cornerPathEffect != null) {
                    paint?.setPathEffect(cornerPathEffect)
                }
                canvas.drawPath(path, paint)
                //paint.setShader(null)
                //paint.setPathEffect(null)
                //控制虚线流动性
                if (triangle.isdashFlow && (triangle.dashWidth > 0 && triangle.dashGap > 0)) {
                    if (triangle.dashSpeed > 0) {
                        if (startPhase >= Float.MAX_VALUE - triangle.dashSpeed) {
                            startPhase = 0f
                        }
                    } else {
                        if (startPhase >= Float.MIN_VALUE - triangle.dashSpeed) {
                            startPhase = 0f
                        }
                    }
                    startPhase += triangle.dashSpeed
                    invalidate()
                }
            }
            if (triangle.isPorterDuffXfermode){
                //fixme 切割
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.color = Color.WHITE
                paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
                path?.setFillType(Path.FillType.INVERSE_WINDING)//反转
                canvas.drawPath(path, paint)
                path?.setFillType(Path.FillType.WINDING)//恢复正常
                paint.setXfermode(null)
            }
            paint.setShader(null)
            paint.setPathEffect(null)
            if (triangle.rotation != 0f) {
                canvas.restore()//三角形旋转恢复。
            }
        }
    }

    private var startPhase: Float = 0F

    /**
     * @param xA 起始点位置A的x轴绝对位置;fixme 五角星顶点的坐标位置。
     * @param yA 起始点位置A的y轴绝对位置
     * @param rFive 五角星边的边长
     */
    open fun fivePoints(xA: Float, yA: Float, rFive: Int): FloatArray? {
        var xB = 0f
        var xC = 0f
        var xD = 0f
        var xE = 0f
        var yB = 0f
        var yC = 0f//fixme C和D是最低点。
        var yD = 0f
        var yE = 0f
        xD = (xA - rFive * Math.sin(Math.toRadians(18.0))).toFloat()
        xC = (xA + rFive * Math.sin(Math.toRadians(18.0))).toFloat()
        yC = (yA + Math.cos(Math.toRadians(18.0)) * rFive).toFloat()
        yD = yC
        yE = (yA + Math.sqrt(Math.pow((xC - xD).toDouble(), 2.0) - Math.pow((rFive / 2).toDouble(), 2.0))).toFloat()
        yB = yE
        xB = xA + rFive / 2
        xE = xA - rFive / 2
        return floatArrayOf(xA, yA, xD, yD, xB, yB, xE, yE, xC, yC, xA, yA)
    }


    override fun onDestroy() {
        super.onDestroy()
        start = null
        start_focuse = null
        start_hover = null
        start_press = null
        start_selected = null
        startModel = null
    }

}
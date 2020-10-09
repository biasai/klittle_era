package cn.oi.klittle.era.widget.chart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.entity.widget.chart.KAxisEntity
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.widget.compat.KScrollTextView

/**
 * 基本图表；集成x,y轴
 */
open class KBaseChartView : KScrollTextView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
        init()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    //初始化操作
    private fun init() {
        openScrollVertical = false//关闭垂直滚动（水平滚动默认就是关闭的。）
        mTouchSlop = 0//触摸滑动有效距离值
        clearButonShadow()//清除按钮默认的阴影效果
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    override fun draw2Last(canvas: Canvas, paint: Paint) {
        drawXAxis(canvas, resetPaint(paint))
        drawYAxis(canvas, resetPaint(paint))
        super.draw2Last(canvas, resetPaint(paint))//这里面有圆角属性，所以置后。
    }

    var xAxis_phase: Float = 0F
    /**
     * fixme 画X轴
     */
    open fun drawXAxis(canvas: Canvas, paint: Paint) {
        currentXAxis?.apply {
            if (isDraw) {
                paint.style = Paint.Style.FILL
                paint.color = color//颜色值
                var linearGradient: LinearGradient? = null
                if (verticalColors != null) {
                    if (!isGradient) {
                        //垂直不渐变
                        linearGradient = getNotLinearGradient(y.toFloat() + scrollY, y.toFloat() + height + scrollY, verticalColors!!, true)
                    }
                    //fixme 垂直渐变
                    if (linearGradient == null) {
                        linearGradient = LinearGradient(0f, y.toFloat() + scrollY, 0f, y.toFloat() + height + scrollY, verticalColors, null, Shader.TileMode.CLAMP)
                    }
                } else if (horizontalColors != null) {
                    var start = (x + scrollX).toFloat()
                    var end = width.toFloat() + x + scrollX
                    var distance = end - start
                    var distance2 = getWidth() - x
                    if (distance > distance2) {
                        end = (getWidth() + scrollX).toFloat()
                    }
                    if (!isGradient) {
                        //水平不渐变
                        linearGradient = getNotLinearGradient(start, end, horizontalColors!!, false)
                    }
                    //fixme 水平渐变
                    if (linearGradient == null) {
                        linearGradient = LinearGradient(start, 0f, end, 0f, horizontalColors, null, Shader.TileMode.CLAMP)
                    }
                }
                if (linearGradient != null) {
                    //颜色渐变
                    paint.setShader(linearGradient)
                }
                var path = Path()
                /**
                 * fixme 测试发现，线条的长度不能太长，貌似超过一万二之后就无法显示了；即 width = kpx.x(12000)
                 */
                //虚线
                if (dashWidth > 0 && dashGap > 0) {
                    var dashPathEffect = DashPathEffect(floatArrayOf(dashWidth, dashGap), xAxis_phase)
                    paint.setPathEffect(dashPathEffect)
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = height.toFloat()
                    if (left_top > 0 || left_bottom > 0 || right_top > 0 || right_bottom > 0) {
                        paint.strokeCap = Paint.Cap.ROUND
                    } else {
                        paint.strokeCap = Paint.Cap.BUTT
                    }
                    //虚线对矩形不管用,只对矩形的边框有效，所以只能画线
                    path.moveTo(x.toFloat(), (y + scrollY).toFloat())
                    path.lineTo((x + width).toFloat(), (y + scrollY).toFloat())
                } else {
                    // 矩形弧度
                    val radian = floatArrayOf(left_top!!, left_top!!, right_top, right_top, right_bottom, right_bottom, left_bottom, left_bottom)
                    //fixme  画矩形(x轴，不受y影响，所以加上scrollY)
                    var rectF = RectF(x.toFloat(), (y + scrollY).toFloat(), (x + width).toFloat(), height.toFloat() + y + scrollY)
                    path.addRoundRect(rectF, radian, Path.Direction.CW)
                }
                canvas.drawPath(path, paint)

                //控制虚线流动性
                if (isdashFlow && (dashWidth > 0 && dashGap > 0)) {
                    if (dashSpeed > 0) {
                        if (xAxis_phase >= Float.MAX_VALUE - dashSpeed) {
                            xAxis_phase = 0f
                        }
                    } else {
                        if (xAxis_phase >= Float.MIN_VALUE - dashSpeed) {
                            xAxis_phase = 0f
                        }
                    }
                    xAxis_phase += dashSpeed
                    invalidate()
                }

            }
        }
    }

    /**
     * fixme 画Y轴
     */
    open fun drawYAxis(canvas: Canvas, paint: Paint) {
        currentYAxis?.let {
            if (it.isDraw) {

            }
        }
    }

    //状态：聚焦
    override fun changedFocused() {
        super.changedFocused()
        currentXAxis = xAxis
        currentYAxis = yAxis
        xAxis_focuse?.let {
            currentXAxis = it
        }
        yAxis_focuse?.let {
            currentYAxis = it
        }
    }

    //状态：悬浮
    override fun changedHovered() {
        super.changedHovered()
        currentXAxis = xAxis
        currentYAxis = yAxis
        xAxis_hover?.let {
            currentXAxis = it
        }
        yAxis_hover?.let {
            currentYAxis = it
        }
    }

    //状态：选中
    override fun changedSelected() {
        super.changedSelected()
        currentXAxis = xAxis
        currentYAxis = yAxis
        xAxis_selected?.let {
            currentXAxis = it
        }
        yAxis_selected?.let {
            currentYAxis = it
        }
    }

    //状态不可用
    override fun changedNotEnabled() {
        super.changedNotEnabled()
        currentXAxis = xAxis
        currentYAxis = yAxis
        xAxis_notEnable?.let {
            currentXAxis = it
        }
        yAxis_notEnable?.let {
            currentYAxis = it
        }
    }

    //状态：按下
    override fun changedPressed() {
        super.changedPressed()
        currentXAxis = xAxis
        currentYAxis = yAxis
        xAxis_press?.let {
            currentXAxis = it
        }
        yAxis_notEnable?.let {
            currentYAxis = it
        }
    }

    //状态：正常
    override fun changedNormal() {
        super.changedNormal()
        currentXAxis = xAxis
        currentYAxis = yAxis
    }

    /**
     * fixme x轴
     */

    //不可用
    private var xAxis_notEnable: KAxisEntity? = null

    fun xAxis_notEnable(block: KAxisEntity.() -> Unit): KBaseChartView {
        if (xAxis_notEnable == null) {
            xAxis_notEnable = getAxis().copy()//整个属性全部复制过来。
        }
        block(xAxis_notEnable!!)
        invalidate()
        return this
    }

    //按下
    private var xAxis_press: KAxisEntity? = null

    fun xAxis_press(block: KAxisEntity.() -> Unit): KBaseChartView {
        if (xAxis_press == null) {
            xAxis_press = getAxis().copy()//整个属性全部复制过来。
        }
        block(xAxis_press!!)
        invalidate()
        return this
    }

    //鼠标悬浮
    private var xAxis_hover: KAxisEntity? = null

    fun radius_hover(block: KAxisEntity.() -> Unit): KBaseChartView {
        if (xAxis_hover == null) {
            xAxis_hover = getAxis().copy()//整个属性全部复制过来。
        }
        block(xAxis_hover!!)
        invalidate()
        return this
    }

    //聚焦
    private var xAxis_focuse: KAxisEntity? = null

    fun xAxis_focuse(block: KAxisEntity.() -> Unit): KBaseChartView {
        if (xAxis_focuse == null) {
            xAxis_focuse = getAxis().copy()//整个属性全部复制过来。
        }
        block(xAxis_focuse!!)
        invalidate()
        return this
    }

    //选中
    private var xAxis_selected: KAxisEntity? = null

    fun xAxis_selected(block: KAxisEntity.() -> Unit): KBaseChartView {
        if (xAxis_selected == null) {
            xAxis_selected = getAxis().copy()//整个属性全部复制过来。
        }
        block(xAxis_selected!!)
        invalidate()
        return this
    }

    //fixme 正常状态（先写正常样式，再写其他状态的样式，因为其他状态的样式初始值是复制正常状态的样式的。）
    private var xAxis: KAxisEntity? = null

    fun xAxis(block: KAxisEntity.() -> Unit): KBaseChartView {
        clearButonShadow()//自定义圆角，就去除按钮默认的圆角阴影。不然效果不好。
        block(getAxis())
        invalidate()
        return this
    }

    private fun getAxis(): KAxisEntity {
        if (xAxis == null) {
            xAxis = KAxisEntity()
        }
        return xAxis!!
    }

    private var currentXAxis: KAxisEntity? = null

    /**
     * fixme y轴
     */
    //不可用
    private var yAxis_notEnable: KAxisEntity? = null

    fun yAxis_notEnable(block: KAxisEntity.() -> Unit): KBaseChartView {
        if (yAxis_notEnable == null) {
            yAxis_notEnable = getAxis().copy()//整个属性全部复制过来。
        }
        block(yAxis_notEnable!!)
        invalidate()
        return this
    }

    //按下
    private var yAxis_press: KAxisEntity? = null

    fun yAxis_press(block: KAxisEntity.() -> Unit): KBaseChartView {
        if (yAxis_press == null) {
            yAxis_press = getAxis().copy()//整个属性全部复制过来。
        }
        block(yAxis_press!!)
        invalidate()
        return this
    }

    //鼠标悬浮
    private var yAxis_hover: KAxisEntity? = null

    fun yAxis_hover(block: KAxisEntity.() -> Unit): KBaseChartView {
        if (yAxis_hover == null) {
            yAxis_hover = getAxis().copy()//整个属性全部复制过来。
        }
        block(yAxis_hover!!)
        invalidate()
        return this
    }

    //聚焦
    private var yAxis_focuse: KAxisEntity? = null

    fun yAxis_focuse(block: KAxisEntity.() -> Unit): KBaseChartView {
        if (yAxis_focuse == null) {
            yAxis_focuse = getAxis().copy()//整个属性全部复制过来。
        }
        block(yAxis_focuse!!)
        invalidate()
        return this
    }

    //选中
    private var yAxis_selected: KAxisEntity? = null

    fun yAxis_selected(block: KAxisEntity.() -> Unit): KBaseChartView {
        if (yAxis_selected == null) {
            yAxis_selected = getAxis().copy()//整个属性全部复制过来。
        }
        block(yAxis_selected!!)
        invalidate()
        return this
    }

    //fixme 正常状态（先写正常样式，再写其他状态的样式，因为其他状态的样式初始值是复制正常状态的样式的。）
    private var yAxis: KAxisEntity? = null

    fun yAxis(block: KAxisEntity.() -> Unit): KBaseChartView {
        clearButonShadow()//自定义圆角，就去除按钮默认的圆角阴影。不然效果不好。
        block(getAxis())
        invalidate()
        return this
    }

    private var currentYAxis: KAxisEntity? = null

    override fun onDestroy() {
        super.onDestroy()
        xAxis = null
        xAxis_focuse = null
        xAxis_hover = null
        xAxis_press = null
        xAxis_selected = null
        xAxis_notEnable=null
        xAxis = null
        currentXAxis = null
        yAxis = null
        yAxis_focuse = null
        yAxis_hover = null
        yAxis_press = null
        yAxis_selected = null
        yAxis_notEnable=null
        yAxis = null
        currentYAxis = null
    }
}
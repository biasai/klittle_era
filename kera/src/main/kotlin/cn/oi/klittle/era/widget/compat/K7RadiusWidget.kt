package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.base.KBaseView
import cn.oi.klittle.era.entity.widget.compat.KBorderEntity
import cn.oi.klittle.era.entity.widget.compat.KRadiusEntity
import cn.oi.klittle.era.utils.KRadiusUtils
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.topPadding

//fixme 使用案例：

//                        radius {
//                            all_radius(60)
//                            bg_color = Color.parseColor("#00CAFC")
//                            bgHorizontalColors(Color.RED,Color.BLUE)
//                            bgVerticalColors(Color.BLUE,Color.CYAN)
//                            //strokeWidth = 0f
//                            strokeWidth = kpx.x(3f)
//                            strokeColor = Color.YELLOW
//                            strokeHorizontalColors(Color.GREEN, Color.BLUE)
//                            //leftMargin = kpx.x(100)//左外补丁，控件边框的间距；fixme radius已经做了兼容处理，能够正常切割。
//                            //topMargin = kpx.x(100)
//                            //rightMargin = kpx.x(50)
//                            //bottomMargin = kpx.x(50)
//                            setAutoPaddingForRadius(kpx.x(16),this)//fixme 根据radius的外补丁，自动设置文本内补丁。
//                            //isPorterDuffXfermode=true//fixme 是否切割，默认是
//                        }

/**
 * 七：集成圆角属性（这个圆角会遮挡住下面，是真正的圆角）,背景渐变。圆角兼容api 16 即4.1 。15及以下的版本没测试过。应该也可以。
 * fixme 兼容性没问题。能够兼容4.0的系统。比selectorDrawable的兼容性都好。亲测！
 * fixme 所以drawPath()比canvas.drawRoundRect()兼容性要好。多用路径。
 * fixme drawXfermodeCircle()切除一个圆；drawXfermodeRect（）切除一个矩形。
 *
 * fixme 切割最好最稳定的还是使用radius {}最完美。解决了低版本的问题。
 */
open class K7RadiusWidget : K5LparamWidget {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    init {
        //开启硬件加速,不然圆角没有效果
        //fixme path.setFillType(Path.FillType.INVERSE_WINDING)//反转 现在使用了这个。开不开硬件加速都无所谓了。都支持圆角了。不开硬件加速也行。
        //fixme INVERSE_WINDING对9.0及以上系统支持很好。对7.0及以下不是很完美，还是有缺陷的。在KRadiusUtils里已经做了兼容处理。能够完美切割。
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    /**
     * fixme 根据radius的外补丁，自动设置文本的内补丁。
     * @param mPadding 内补丁；一般都设置为：kpx.x(16)；即8的倍数。8，16，24...比较好。
     * @param borderEnty 边框实体类
     */
    open fun setAutoPaddingForRadius(mPadding: Int, radiusEnty: KRadiusEntity? = this.radius) {
        radiusEnty?.apply {
            rightPadding = mPadding + rightMargin
            leftPadding = mPadding + leftMargin
            topPadding = mPadding + topMargin
            bottomPadding = mPadding + bottomMargin
        }
    }

    //fixme 不可用状态
    var radius_notEnable: KRadiusEntity? = null

    fun radius_notEnable(block: KRadiusEntity.() -> Unit): K7RadiusWidget {
        if (radius_notEnable == null) {
            radius_notEnable = gtmRadius().copy()//整个属性全部复制过来。
        }
        block(radius_notEnable!!)
        invalidate()
        //requestLayout()
        return this
    }

    //按下
    var radius_press: KRadiusEntity? = null

    fun radius_press(block: KRadiusEntity.() -> Unit): K7RadiusWidget {
        if (radius_press == null) {
            radius_press = gtmRadius().copy()//整个属性全部复制过来。
        }
        block(radius_press!!)
        invalidate()
        //requestLayout()
        return this
    }

    //鼠标悬浮
    var radius_hover: KRadiusEntity? = null

    fun radius_hover(block: KRadiusEntity.() -> Unit): K7RadiusWidget {
        if (radius_hover == null) {
            radius_hover = gtmRadius().copy()//整个属性全部复制过来。
        }
        block(radius_hover!!)
        invalidate()
        //requestLayout()
        return this
    }

    //聚焦
    var radius_focuse: KRadiusEntity? = null

    fun radius_focuse(block: KRadiusEntity.() -> Unit): K7RadiusWidget {
        if (radius_focuse == null) {
            radius_focuse = gtmRadius().copy()//整个属性全部复制过来。
        }
        block(radius_focuse!!)
        invalidate()
        //requestLayout()
        return this
    }

    //选中
    var radius_selected: KRadiusEntity? = null

    fun radius_selected(block: KRadiusEntity.() -> Unit): K7RadiusWidget {
        if (radius_selected == null) {
            radius_selected = gtmRadius().copy()//整个属性全部复制过来。
        }
        block(radius_selected!!)
        invalidate()
        //requestLayout()
        return this
    }

    //fixme 正常状态（先写正常样式，再写其他状态的样式，因为其他状态的样式初始值是复制正常状态的样式的。）
    var radius: KRadiusEntity? = null

    fun gtmRadius(): KRadiusEntity {
        if (radius == null) {
            radius = KRadiusEntity()
        }
        return radius!!
    }

    fun radius(block: KRadiusEntity.() -> Unit): K7RadiusWidget {
        clearButonShadow()//自定义圆角，就去除按钮默认的圆角阴影。不然效果不好。
        block(gtmRadius())
//        fixme 有all_radius(all_radius: Float)设置所有圆角的方法。就不需要以下方法了。防止冲突。
//        gtmRadius().apply {
//            if (left_top <= 0) {
//                left_top = all_radius
//            }
//            if (left_bottom <= 0) {
//                left_bottom = all_radius
//            }
//            if (right_top <= 0) {
//                right_top = all_radius
//            }
//            if (right_bottom <= 0) {
//                right_bottom = all_radius
//            }
//        }
        invalidate()
        //requestLayout()
        return this
    }

    var currentRadius: KRadiusEntity? = null

    //fixme 画背景
    fun drawBg(canvas: Canvas, paint: Paint, view: View) {
        view?.apply {
            var w = view.width
            var h = view.height
            var scrollX = view.scrollX
            var scrollY = view.scrollY
            if (radius != null) {
                currentRadius = null
                if (isPressed && radius_press != null) {
                    //按下
                    currentRadius = radius_press
                } else if (isHovered && radius_hover != null) {
                    //鼠标悬浮
                    currentRadius = radius_hover
                } else if (isFocused && radius_focuse != null) {
                    //聚焦
                    currentRadius = radius_focuse
                } else if (isSelected && radius_selected != null) {
                    //选中
                    currentRadius = radius_selected
                }
                //不可用，优先级最高
                if (!isEnabled && radius_notEnable != null) {
                    currentRadius = radius_notEnable
                }
                //正常
                if (currentRadius == null) {
                    currentRadius = radius
                }
                currentRadius?.let {
                    if (it.width > 0) {
                        w = it.width
                    }
                    if (it.height > 0) {
                        h = it.height
                    }
                    //画背景
                    var isDrawColor = false//是否画背景色
                    if (it.bg_color != Color.TRANSPARENT) {
                        paint.color = it.bg_color
                        isDrawColor = true
                    }
//                    var left = 0f + scrollX + centerX - w / 2.0f
//                    var top = 0f + scrollY + centerY - h / 2.0f//fixme 2.0f必须设置成浮点型，不然可能会有0.5的偏差。
//                    //KLoggerUtils.e("scrollY:\t"+scrollY+"\tcenterY:\t"+centerY+"\th / 2:\t"+(h / 2.0f)+"\th:\t"+h)
//                    var right = w + left
//                    var bottom = h + top

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
                    if (Build.VERSION.SDK_INT <= 17) {
                        var h2 = h.toFloat()
                        if (w < h) {
                            h2 = w.toFloat()//取小的那一边
                        }
                        h2 = h2 / 2
                        if (it.left_top > h2) {
                            it.left_top = h2
                        }
                        if (it.right_top > h2) {
                            it.right_top = h2
                        }
                        if (it.right_bottom > h2) {
                            it.right_bottom = h2
                        }
                        if (it.left_bottom > h2) {
                            it.left_bottom = h2
                        }
                    }
                    if (isDrawColor) {
                        // fixme 矩形弧度,防止Toat背景色没有圆角效果。所以直接画圆角背景
                        val radian = floatArrayOf(it.left_top!!, it.left_top!!, it.right_top, it.right_top, it.right_bottom, it.right_bottom, it.left_bottom, it.left_bottom)
                        //fixme  画圆角矩形背景
                        var rectF = RectF(left, top, right, bottom)
                        var path = Path()
                        path.addRoundRect(rectF, radian, Path.Direction.CW)
                        canvas.drawPath(path, paint)
                    }
                }
            }
        }
    }

    override fun draw2Bg(canvas: Canvas, paint: Paint) {
        super.draw2Bg(canvas, paint)
        //画背景
        drawBg(canvas, paint, this)
    }

    override fun draw2Last(canvas: Canvas, paint: Paint) {
        super.draw2Last(canvas, paint)
        //画圆角
        drawRadius(canvas, this)
    }

    //fixme 画圆角
    fun drawRadius(canvas: Canvas, view: View) {
        currentRadius?.apply {
            drawRadius(canvas, this, view)
        }
    }

    private var kradius: KRadiusUtils? = KRadiusUtils()
    private var radius_phase: Float = 0F

    //画边框，圆角
    fun drawRadius(canvas: Canvas, model: KRadiusEntity, view: View) {
        view?.apply {
            model.let {
                //画圆角
                kradius?.apply {
                    x = 0f
                    y = 0f
                    w = view.width
                    h = view.height
                    leftMargin = it.leftMargin
                    topMargin = it.topMargin
                    rightMargin = it.rightMargin
                    bottomMargin = it.bottomMargin
                    view?.layoutParams?.let {
                        if (w < it.width) {
                            w = it.width
                        }
                        if (h < it.height) {
                            h = it.height
                        }
                    }
                    if (it.width > 0) {
                        w = it.width
                        x = (centerX - w / 2)//fixme 居中对齐
                    }
                    if (it.height > 0) {
                        h = it.height
                        y = (centerY - h / 2)//居中对齐
                    }
                    isDST_IN = it.isPorterDuffXfermode//fixme 取下面的交集
                    all_radius = 0f
                    left_top = it.left_top
                    left_bottom = it.left_bottom
                    right_top = it.right_top
                    right_bottom = it.right_bottom
                    strokeWidth = it.strokeWidth
                    strokeColor = it.strokeColor
                    //支持虚线边框
                    dashWidth = it.dashWidth
                    dashGap = it.dashGap
                    strokeGradientColors = it.strokeHorizontalColors
                    strokeGradientOritation = ORIENTATION_HORIZONTAL
                    if (it.strokeVerticalColors != null) {
                        strokeGradientColors = it.strokeVerticalColors
                        strokeGradientOritation = ORIENTATION_VERTICAL
                    }
                    isStrokeGradient = it.isStrokeGradient
                    drawRadius(canvas, radius_phase, view.scrollX, view.scrollY)
                    //控制虚线流动性
                    if (it.isdashFlow && (dashWidth > 0 && dashGap > 0)) {
                        if (it.dashSpeed > 0) {
                            if (radius_phase >= Float.MAX_VALUE - it.dashSpeed) {
                                radius_phase = 0f
                            }
                        } else {
                            if (radius_phase >= Float.MIN_VALUE - it.dashSpeed) {
                                radius_phase = 0f
                            }
                        }
                        radius_phase += it.dashSpeed
                        invalidate()
                    }
                }
            }
        }

    }

    private var xfermodePaint: Paint? = null
    private fun initXferModePaint() {
        if (xfermodePaint == null) {
            xfermodePaint = KBaseView.getPaint()
            xfermodePaint?.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
        }
    }

    /**
     * fixme 以下是独立切除方法
     */
//                        切除方法使用案例
//                        draw { canvas, paint ->
//                        //切除一个圆
//                        drawXfermodeCircle(canvas,200,200,100)
//                        //切除一个矩形
//                        //drawXfermodeRect(canvas,300,300,500,500)
//                        //切除圆角矩形
//                        //drawXfermodeRoundRect(canvas, RectF(300f,300f,500f,500f),30f,left_bottom = 0f)
//                        drawXfermodeRoundRect(canvas, 300,300,500,500,30,left_bottom = 0)
//                    }

    /**
     * fixme 切除一个圆
     */
    fun drawXfermodeCircle(canvas: Canvas, x: Float, y: Float, radius: Float) {
        initXferModePaint()
        canvas.drawCircle(x, y, radius, xfermodePaint)
    }

    fun drawXfermodeCircle(canvas: Canvas, x: Int, y: Int, radius: Int) {
        drawXfermodeCircle(canvas, x.toFloat(), y.toFloat(), radius.toFloat())
    }

    /**
     * fixme 切除一个矩形
     */
    fun drawXfermodeRect(canvas: Canvas, left: Int, top: Int, right: Int, bottom: Int) {
        initXferModePaint()
        canvas.drawRect(Rect(left, top, right, bottom), xfermodePaint)
    }

    fun drawXfermodeRect(canvas: Canvas, rect: Rect) {
        initXferModePaint()
        canvas.drawRect(rect, xfermodePaint)
    }

    /**
     * 切除圆角矩形
     * @param rectF 矩形
     * @param left_top 左上角
     * @param right_top 右上角
     * @param right_bottom 右下角
     * @param left_bottom 左下角
     */
    fun drawXfermodeRoundRect(canvas: Canvas, rectF: RectF, left_top: Float, right_top: Float = left_top, right_bottom: Float = left_top, left_bottom: Float = left_top) {
        initXferModePaint()
        // 矩形弧度
        val radian = floatArrayOf(left_top!!, left_top!!, right_top, right_top, right_bottom, right_bottom, left_bottom, left_bottom)
        var path = Path()
        path.addRoundRect(rectF, radian, Path.Direction.CW)
        canvas.drawPath(path, xfermodePaint)
    }

    fun drawXfermodeRoundRect(canvas: Canvas, rectF: RectF, left_top: Int, right_top: Int = left_top, right_bottom: Int = left_top, left_bottom: Int = left_top) {
        drawXfermodeRoundRect(canvas, rectF, left_top.toFloat(), right_top.toFloat(), right_bottom.toFloat(), left_bottom.toFloat())
    }

    fun drawXfermodeRoundRect(canvas: Canvas, left: Int, top: Int, right: Int, bottom: Int, left_top: Int, right_top: Int = left_top, right_bottom: Int = left_top, left_bottom: Int = left_top) {
        drawXfermodeRoundRect(canvas, RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat()), left_top.toFloat(), right_top.toFloat(), right_bottom.toFloat(), left_bottom.toFloat())
    }

    fun drawXfermodeRoundRect(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, left_top: Int, right_top: Int = left_top, right_bottom: Int = left_top, left_bottom: Int = left_top) {
        drawXfermodeRoundRect(canvas, RectF(left, top, right, bottom), left_top.toFloat(), right_top.toFloat(), right_bottom.toFloat(), left_bottom.toFloat())
    }

    fun drawXfermodeRoundRect(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, left_top: Float, right_top: Float = left_top, right_bottom: Float = left_top, left_bottom: Float = left_top) {
        drawXfermodeRoundRect(canvas, RectF(left, top, right, bottom), left_top, right_top, right_bottom, left_bottom)
    }

    override fun onDestroy() {
        super.onDestroy()
        radius = null
        radius_focuse = null
        radius_hover = null
        radius_press = null
        radius_selected = null
        kradius = null
        currentRadius = null
        xfermodePaint = null
        radius_notEnable = null
    }

}
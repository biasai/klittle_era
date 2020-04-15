package cn.oi.klittle.era.widget.web

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.base.KBaseView
import cn.oi.klittle.era.entity.widget.compat.KRadiusEntity
import cn.oi.klittle.era.utils.KRadiusUtils
import cn.oi.klittle.era.widget.compat.K0Widget
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundDrawable

/**
 * 自定义WebView圆角属性，在真机上测试是有效果的。在模拟器上加载网页好像无效（这个不影响，以真机为标准）。
 */
open class K1RadiusWebView : K0JsBridgeWebView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//开启硬件加速,不然圆角没有效果(现在的圆角已经不受硬件加速影响了，都有效)
    }

    //fixme 清空原始背景，无法清除button默认的点击阴影
    open fun clearBackground() {
        if (Build.VERSION.SDK_INT >= 16) {
            backgroundColor = Color.TRANSPARENT
            background = null
        } else {
            backgroundColor = Color.TRANSPARENT
            backgroundDrawable = null
        }
    }

    open fun background(color: String) {
        setBackgroundColor(Color.parseColor(color))
    }

    open fun background(resId: Int) {
        setBackgroundResource(resId)
    }

    open fun background(bitmap: Bitmap) {
        if (Build.VERSION.SDK_INT >= 16) {
            background = BitmapDrawable(bitmap)
        } else {
            backgroundDrawable = BitmapDrawable(bitmap)
        }
    }

    open var w: Int = 0
        //fixme 真实的宽度,现在设置w或lparams里的width都可以。两个同步了。
        get() {
            if (field == width && width > 0) {
                return field
            }
            var w = width
            if (layoutParams != null && layoutParams.width > w) {
                w = layoutParams.width
            }
            return w
        }
        set(value) {
            field = value
            layoutParams?.let {
                it.width = value
                requestLayout()
            }
        }
    open var h: Int = 0
        //fixme 真实的高度。设置h或height都可以。
        get() {
            if (field == height && height > 0) {
                return field
            }
            var h = height
            if (layoutParams != null && layoutParams.height > h) {
                h = layoutParams.height
            }
            field = h
            return h
        }
        set(value) {
            field = value
            layoutParams?.let {
                it.height = value
                requestLayout()
            }
        }

    //设置控件的高度和高度
    // matchParent:	-1 wrapContent:	-2
    open fun layoutParams(width: Int = w, height: Int = h): ViewGroup.LayoutParams? {
        if (width != w || height != h) {
            w = width
            h = height
            layoutParams?.apply {
                //设置宽和高
                this.width = w
                this.height = h
                requestLayout()
            }
        }
        return layoutParams
    }

    //按下
    var radius_press: KRadiusEntity? = null

    fun radius_press(block: KRadiusEntity.() -> Unit): K1RadiusWebView {
        if (radius_press == null) {
            radius_press = gtmRadius().copy()//整个属性全部复制过来。
        }
        block(radius_press!!)
        requestLayout()
        return this
    }

    //鼠标悬浮
    var radius_hover: KRadiusEntity? = null

    fun radius_hover(block: KRadiusEntity.() -> Unit): K1RadiusWebView {
        if (radius_hover == null) {
            radius_hover = gtmRadius().copy()//整个属性全部复制过来。
        }
        block(radius_hover!!)
        requestLayout()
        return this
    }

    //聚焦
    var radius_focuse: KRadiusEntity? = null

    fun radius_focuse(block: KRadiusEntity.() -> Unit): K1RadiusWebView {
        if (radius_focuse == null) {
            radius_focuse = gtmRadius().copy()//整个属性全部复制过来。
        }
        block(radius_focuse!!)
        requestLayout()
        return this
    }

    //选中
    var radius_selected: KRadiusEntity? = null

    fun radius_selected(block: KRadiusEntity.() -> Unit): K1RadiusWebView {
        if (radius_selected == null) {
            radius_selected = gtmRadius().copy()//整个属性全部复制过来。
        }
        block(radius_selected!!)
        requestLayout()
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

    fun radius(block: KRadiusEntity.() -> Unit): K1RadiusWebView {
        block(gtmRadius())
        requestLayout()
        return this
    }

    var model: KRadiusEntity? = null

    var mPaint = KBaseView.getPaint()
    //画布重置
    fun resetPaint(): Paint {
        return KBaseView.resetPaint(mPaint)
    }

    override fun draw(canvas: Canvas?) {
        canvas?.apply {
            if (w > 0 && h > 0) {
                draw2First(this, resetPaint())
                super.draw(canvas)//fixme 这里面会绘制H5页面。屏蔽了页面就没有内容了。
                draw2Last(this, resetPaint())
            }
        }
    }

    //画背景
    fun draw2First(canvas: Canvas, paint: Paint) {
        if (radius != null) {
            model = null
            if (isPressed && radius_press != null) {
                //按下
                model = radius_press
            } else if (isHovered && radius_hover != null) {
                //鼠标悬浮
                model = radius_hover
            } else if (isFocused && radius_focuse != null) {
                //聚焦
                model = radius_focuse
            } else if (isSelected && radius_selected != null) {
                //选中
                model = radius_selected
            }
            //正常
            if (model == null) {
                model = radius
            }
            model?.let {
                //画背景
                var isDrawColor = false//是否画背景色
                if (it.bg_color != Color.TRANSPARENT) {
                    paint.color = it.bg_color
                    isDrawColor = true
                }
                var left = 0f
                var top = 0f + scrollY
                var right = w + left
                var bottom = h + top
                if (it.bgVerticalColors != null) {
                    var shader: LinearGradient? = null
                    if (!it.isBgGradient) {
                        //垂直不渐变
                        shader = K0Widget.getNotLinearGradient(top, bottom, it.bgVerticalColors!!, true, scrollY)
                    }
                    //垂直渐变，优先级高于水平(渐变颜色值数组必须大于等于2，不然异常)
                    if (shader == null) {
                        shader = LinearGradient(0f, top, 0f, bottom, it.bgVerticalColors, null, Shader.TileMode.MIRROR)
                    }
                    paint.setShader(shader)
                    isDrawColor = true
                } else if (it.bgHorizontalColors != null) {
                    var shader: LinearGradient? = null
                    if (!it.isBgGradient) {
                        //水平不渐变
                        shader = K0Widget.getNotLinearGradient(left, right, it.bgHorizontalColors!!, false, scrollY)
                    }
                    //水平渐变
                    if (shader == null) {
                        shader = LinearGradient(left, 0f, right, 0f, it.bgHorizontalColors, null, Shader.TileMode.MIRROR)
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

    fun draw2Last(canvas: Canvas, paint: Paint) {
        model?.apply {
            drawRadius(canvas, this, paint)
        }
    }

    var kradius = KRadiusUtils()
    var phase: Float = 0F
    //画边框，圆角
    fun drawRadius(canvas: Canvas, model: KRadiusEntity, paint: Paint) {
        model.let {
            //画圆角
            kradius.apply {
                x = 0f
                y = 0f
                w = this@K1RadiusWebView.width
                h = this@K1RadiusWebView.height
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
                drawRadius(canvas, phase, scrollX, scrollY)
                //控制虚线流动性
                if (it.isdashFlow && (dashWidth > 0 && dashGap > 0)) {
                    if (it.dashSpeed > 0) {
                        if (phase >= Float.MAX_VALUE - it.dashSpeed) {
                            phase = 0f
                        }
                    } else {
                        if (phase >= Float.MIN_VALUE - it.dashSpeed) {
                            phase = 0f
                        }
                    }
                    phase += it.dashSpeed
                    invalidate()
                }
            }
        }
    }

}
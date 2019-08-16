package cn.oi.klittle.era.widget

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.entity.widget.toggle.KThumbEntity
import cn.oi.klittle.era.entity.widget.toggle.KToogleEntity

import cn.oi.klittle.era.widget.compat.KView

//          KToggleView比KShadowView效果要好；阴影建议使用KToggleView

//            使用案例(toggle和thumb都是居中于整个控件)
//            KToggleView(this).apply {
//                toggle {
//                    //正常状态（关闭状态）
//                    width = kpx.x(100)
//                    height = kpx.x(50)
//                    bgHorizontalColors(Color.parseColor("#C0C0C0"), Color.parseColor("#C0C0C0"))
//                    all_radius(kpx.x(50f))
//                }
//                toggle_checked {
//                    //选中状态（开状态）
//                    bgHorizontalColors(Color.parseColor("#0078D7"), Color.parseColor("#0078D7"))
//                }
//                thumb {
//                    //移动块（正常状态，关闭状态）
//                    width = kpx.x(50)
//                    height = kpx.x(50)
//                    shadow_radius = kpx.x(8f)
//                    shadow_color = Color.BLACK
//                    bg_color = Color.WHITE
//                    all_radius(kpx.x(50f))
//                    offset = 0f//偏移量（支持正负,左右会自动对称），0和toggle对齐
//                }
//                thumb_checked {
//                    //开状态（一般都不需要这个状态）
//                    //shadow_color = Color.RED
//                }
//                setOnChangedListener {
//                    //选中状态监听（最好在setChecked()之前设置监听。）
//                    //KLoggerUtils.e("开关状态：\t"+it)
//                }
//                setChecked(false)//设置是否选中状态
//            }.lparams {
//                width = kpx.x(200)
//                height = kpx.x(100)
//                topMargin = kpx.x(60)
//            }

/**
 * 开关按钮
 * Created by 彭治铭 on 2019/1/3
 */

class KToggleView : KView {
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

    //初始化
    private fun init() {
        clearButonShadow()//清除按钮默认的阴影效果
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)//关闭硬件加速，不然阴影无效。
        //默认添加点击事件
        onClick {
            setChecked(!isChecked)
        }
    }

    override fun draw2Last(canvas: Canvas, paint: Paint) {
        drawToggle(canvas, resetPaint(paint))
        drawThumb(canvas, resetPaint(paint))
        super.draw2Last(canvas, resetPaint(paint))//fixme 这里面有圆角属性，因为关闭了硬件加速，已经无效了。
    }

    var offOn = 0f//0是关，1是开。左关右开。必须为public，不然属性动画无效果
    private fun offOn(isChecked: Boolean, duration: Long = 350L) {
        var start = offOn
        var end = 0f
        if (isChecked) {
            end = 1f
        }
        if (start != end) {
            ofFloat("offOn", 0, duration, start, end)
        }
    }

    /**
     * fixme 左边是关，右边是开
     * @param isChecked true 选中，false没有选中
     * @param duration 开关动画变化时长
     */
    fun setChecked(isChecked: Boolean, duration: Long = 350L) {
        this.isChecked = isChecked
        normal()//防止状态无效，以防万一
        offOn(isChecked, duration)
        onChangedListener?.let {
            it(isChecked)
        }
    }

    var isChecked: Boolean = false// false 关；ture 开

    private var onChangedListener: ((isChecked: Boolean) -> Unit)? = null
    /**
     * 状态变化监听
     */
    fun setOnChangedListener(onChangedListener: ((isChecked: Boolean) -> Unit)? = null) {
        this.onChangedListener = onChangedListener
    }

    private var toggle_phase: Float = 0F
    private var toggleLeft = 0f
    private var toggleRight = 0f
    private var toggleWidth = 0f
    var colorMatrix: ColorMatrix? = null
    fun drawToggle(canvas: Canvas, paint: Paint) {
        currentToggle?.apply {
            paint.isDither = true
            paint.isAntiAlias = true
            paint.strokeWidth = 0f
            paint.style = Paint.Style.FILL
            var left = kpx.centerX(width, this@KToggleView.width).toFloat()
            var top = kpx.centerY(height, this@KToggleView.height).toFloat()
            var right = width + left
            var bottom = height + top
            toggleLeft = left
            toggleRight = right
            toggleWidth = toggleRight - toggleLeft
            paint.color = bg_color
            var shader2: LinearGradient? = null
            //fixme 第一个矩形，有阴影
            if (shadowVerticalColors != null) {
                if (!isShadowGradient) {
                    //垂直不渐变
                    shader2 = getNotLinearGradient(top, bottom, shadowVerticalColors!!, true)
                }
                //垂直渐变，优先级高于水平
                if (shader2 == null) {
                    shader2 = LinearGradient(0f, top, 0f, bottom, shadowVerticalColors, null, Shader.TileMode.MIRROR)
                }
            } else if (shadowHorizontalColors != null) {
                if (!isShadowGradient) {
                    //水平不渐变
                    shader2 = getNotLinearGradient(left, right, shadowHorizontalColors!!, false)
                }
                //水平渐变
                if (shader2 == null) {
                    shader2 = LinearGradient(left, 0f, right, 0f, shadowHorizontalColors, null, Shader.TileMode.MIRROR)
                }
            }
            if (shader2 != null) {
                paint.setShader(shader2)
            }
            //fixme 设置阴影，阴影色受背景色影响。白色背景是最明显的;要想有阴影，shadow_color就不能为透明。这个要注意。（方向渐变色的颜色高于shadow_color）
            paint.setShadowLayer(shadow_radius, shadow_dx, shadow_dy, shadow_color)
            if (Build.VERSION.SDK_INT <= 17) {
                var h2 = h.toFloat()
                if (w < h) {
                    h2 = w.toFloat()//取小的那一边
                }
                h2 = h2 / 2
                if (left_top > h2) {
                    left_top = h2
                }
                if (right_top > h2) {
                    right_top = h2
                }
                if (right_bottom > h2) {
                    right_bottom = h2
                }
                if (left_bottom > h2) {
                    left_bottom = h2
                }
            }
            // 矩形弧度
            val radian = floatArrayOf(left_top!!, left_top!!, right_top, right_top, right_bottom, right_bottom, left_bottom, left_bottom)
            // 画矩形
            var rectF = RectF(left, top, right, bottom)
            var path = Path()
            path.addRoundRect(rectF, radian, Path.Direction.CW)
            canvas.drawPath(path, paint)
            //fixme 第二个矩形，没有阴影。
            paint.reset()
            paint.isDither = true
            paint.isAntiAlias = true
            paint.strokeWidth = 0f
            paint.style = Paint.Style.FILL
            paint.color = bg_color
            var shader: LinearGradient? = null
            if (bgVerticalColors != null) {
                if (!isBgGradient) {
                    //垂直不渐变
                    shader = getNotLinearGradient(top, bottom, bgVerticalColors!!, true)
                }
                //垂直渐变，优先级高于水平
                if (shader == null) {
                    shader = LinearGradient(0f, top, 0f, bottom, bgVerticalColors, null, Shader.TileMode.MIRROR)
                }
            } else if (bgHorizontalColors != null) {
                if (!isBgGradient) {
                    //水平不渐变
                    shader = getNotLinearGradient(left, right, bgHorizontalColors!!, false)
                }
                //水平渐变
                if (shader == null) {
                    shader = LinearGradient(left, 0f, right, 0f, bgHorizontalColors, null, Shader.TileMode.MIRROR)
                }
            }
            if (shader != null) {
                paint.setShader(shader)
            }
            canvas.drawPath(path, paint)
            //fixme 第二个矩形，画边框
            if (strokeWidth > 0) {
                paint.strokeWidth = strokeWidth
                if (strokeColor != Color.TRANSPARENT) {
                    //fixme 边框大于0时，边框颜色不能为透明。不然无法显示出来。
                    //0是透明，颜色值是有负数的。
                    paint.color = strokeColor
                }
                paint.style = Paint.Style.STROKE
                paint.setShader(null)
                //边框颜色渐变，渐变颜色优先等级大于正常颜色。
                var linearGradient: LinearGradient? = null
                //渐变颜色数组必须大于等于2
                if (strokeVerticalColors != null) {
                    if (!isStrokeGradient) {
                        //垂直不渐变
                        linearGradient = getNotLinearGradient(top, bottom, strokeVerticalColors!!, true)
                    }
                    //fixme 垂直渐变
                    if (linearGradient == null) {
                        linearGradient = LinearGradient(0f, 0f, 0f, h.toFloat(), strokeVerticalColors, null, Shader.TileMode.CLAMP)
                    }
                } else if (strokeHorizontalColors != null) {
                    if (!isStrokeGradient) {
                        //水平不渐变
                        linearGradient = getNotLinearGradient(left, right, strokeHorizontalColors!!, false)
                    }
                    //fixme 水平渐变
                    if (linearGradient == null) {
                        linearGradient = LinearGradient(0f, h / 2f, w.toFloat(), h / 2f, strokeHorizontalColors, null, Shader.TileMode.CLAMP)
                    }
                }
                linearGradient?.let {
                    paint.setShader(linearGradient)
                }
                //虚线
                if (dashWidth > 0 && dashGap > 0) {
                    var dashPathEffect = DashPathEffect(floatArrayOf(dashWidth, dashGap), toggle_phase)
                    paint.setPathEffect(dashPathEffect)
                }
                canvas.drawPath(path, paint)
                //控制虚线流动性
                if (isdashFlow && (dashWidth > 0 && dashGap > 0)) {
                    if (dashSpeed > 0) {
                        if (toggle_phase >= Float.MAX_VALUE - dashSpeed) {
                            toggle_phase = 0f
                        }
                    } else {
                        if (toggle_phase >= Float.MIN_VALUE - dashSpeed) {
                            toggle_phase = 0f
                        }
                    }
                    toggle_phase += dashSpeed
                    invalidate()
                }
            }
        }
    }

    private var thumb_phase: Float = 0F
    fun drawThumb(canvas: Canvas, paint: Paint) {
        currentThumb?.apply {
            paint.isDither = true
            paint.isAntiAlias = true
            paint.strokeWidth = 0f
            paint.style = Paint.Style.FILL
            //注意是减去偏移量。
            var left = toggleLeft + offset + (toggleWidth - width - offset * 2) * offOn
            var top = kpx.centerY(height, this@KToggleView.height).toFloat()
            var right = width + left
            var bottom = height + top
            toggleLeft = left
            toggleRight = right
            paint.color = bg_color
            var shader2: LinearGradient? = null
            //fixme 第一个矩形，有阴影
            if (shadowVerticalColors != null) {
                if (!isShadowGradient) {
                    //垂直不渐变
                    shader2 = getNotLinearGradient(top, bottom, shadowVerticalColors!!, true)
                }
                //垂直渐变，优先级高于水平
                if (shader2 == null) {
                    shader2 = LinearGradient(0f, top, 0f, bottom, shadowVerticalColors, null, Shader.TileMode.MIRROR)
                }
            } else if (shadowHorizontalColors != null) {
                if (!isShadowGradient) {
                    //水平不渐变
                    shader2 = getNotLinearGradient(left, right, shadowHorizontalColors!!, false)
                }
                //水平渐变
                if (shader2 == null) {
                    shader2 = LinearGradient(left, 0f, right, 0f, shadowHorizontalColors, null, Shader.TileMode.MIRROR)
                }
            }
            if (shader2 != null) {
                paint.setShader(shader2)
            }
            //fixme 设置阴影，阴影色受背景色影响。白色背景是最明显的;要想有阴影，shadow_color就不能为透明。这个要注意。（方向渐变色的颜色高于shadow_color）
            paint.setShadowLayer(shadow_radius, shadow_dx, shadow_dy, shadow_color)
            if (Build.VERSION.SDK_INT <= 17) {
                var h2 = h.toFloat()
                if (w < h) {
                    h2 = w.toFloat()//取小的那一边
                }
                h2 = h2 / 2
                if (left_top > h2) {
                    left_top = h2
                }
                if (right_top > h2) {
                    right_top = h2
                }
                if (right_bottom > h2) {
                    right_bottom = h2
                }
                if (left_bottom > h2) {
                    left_bottom = h2
                }
            }
            // 矩形弧度
            val radian = floatArrayOf(left_top!!, left_top!!, right_top, right_top, right_bottom, right_bottom, left_bottom, left_bottom)
            // 画矩形
            var rectF = RectF(left, top, right, bottom)
            var path = Path()
            path.addRoundRect(rectF, radian, Path.Direction.CW)
            canvas.drawPath(path, paint)
            //fixme 第二个矩形，没有阴影。
            paint.reset()
            paint.isDither = true
            paint.isAntiAlias = true
            paint.strokeWidth = 0f
            paint.style = Paint.Style.FILL
            paint.color = bg_color
            var shader: LinearGradient? = null
            if (bgVerticalColors != null) {
                if (!isBgGradient) {
                    //垂直不渐变
                    shader = getNotLinearGradient(top, bottom, bgVerticalColors!!, true)
                }
                //垂直渐变，优先级高于水平
                if (shader == null) {
                    shader = LinearGradient(0f, top, 0f, bottom, bgVerticalColors, null, Shader.TileMode.MIRROR)
                }
            } else if (bgHorizontalColors != null) {
                if (!isBgGradient) {
                    //水平不渐变
                    shader = getNotLinearGradient(left, right, bgHorizontalColors!!, false)
                }
                //水平渐变
                if (shader == null) {
                    shader = LinearGradient(left, 0f, right, 0f, bgHorizontalColors, null, Shader.TileMode.MIRROR)
                }
            }
            if (shader != null) {
                paint.setShader(shader)
            }
            canvas.drawPath(path, paint)
            //fixme 第二个矩形，画边框
            if (strokeWidth > 0) {
                paint.strokeWidth = strokeWidth
                if (strokeColor != Color.TRANSPARENT) {
                    //fixme 边框大于0时，边框颜色不能为透明。不然无法显示出来。
                    //0是透明，颜色值是有负数的。
                    paint.color = strokeColor
                }
                paint.style = Paint.Style.STROKE
                paint.setShader(null)
                //边框颜色渐变，渐变颜色优先等级大于正常颜色。
                var linearGradient: LinearGradient? = null
                //渐变颜色数组必须大于等于2
                if (strokeVerticalColors != null) {
                    if (!isStrokeGradient) {
                        //垂直不渐变
                        linearGradient = getNotLinearGradient(top, bottom, strokeVerticalColors!!, true)
                    }
                    //fixme 垂直渐变
                    if (linearGradient == null) {
                        linearGradient = LinearGradient(0f, 0f, 0f, h.toFloat(), strokeVerticalColors, null, Shader.TileMode.CLAMP)
                    }
                } else if (strokeHorizontalColors != null) {
                    if (!isStrokeGradient) {
                        //水平不渐变
                        linearGradient = getNotLinearGradient(left, right, strokeHorizontalColors!!, false)
                    }
                    //fixme 水平渐变
                    if (linearGradient == null) {
                        linearGradient = LinearGradient(0f, h / 2f, w.toFloat(), h / 2f, strokeHorizontalColors, null, Shader.TileMode.CLAMP)
                    }
                }
                linearGradient?.let {
                    paint.setShader(linearGradient)
                }
                //虚线
                if (dashWidth > 0 && dashGap > 0) {
                    var dashPathEffect = DashPathEffect(floatArrayOf(dashWidth, dashGap), thumb_phase)
                    paint.setPathEffect(dashPathEffect)
                }
                canvas.drawPath(path, paint)
                //控制虚线流动性
                if (isdashFlow && (dashWidth > 0 && dashGap > 0)) {
                    if (dashSpeed > 0) {
                        if (thumb_phase >= Float.MAX_VALUE - dashSpeed) {
                            thumb_phase = 0f
                        }
                    } else {
                        if (thumb_phase >= Float.MIN_VALUE - dashSpeed) {
                            thumb_phase = 0f
                        }
                    }
                    thumb_phase += dashSpeed
                    invalidate()
                }
            }
        }
    }

    //状态：聚焦
    override fun changedFocused() {
        super.changedFocused()
        normal()
        toggle_focuse?.let {
            currentToggle = it
        }
        thumb_focuse?.let {
            currentThumb = it
        }
    }

    //状态：悬浮
    override fun changedHovered() {
        normal()
        super.changedHovered()
        toggle_hover?.let {
            currentToggle = it
        }
        thumb_hover?.let {
            currentThumb = it
        }
    }

    //状态：选中
    override fun changedSelected() {
        super.changedSelected()
        normal()
        toggle_selected?.let {
            currentToggle = it
        }
        thumb_selected?.let {
            currentThumb = it
        }
    }

    //状态：按下
    override fun changedPressed() {
        super.changedPressed()
        normal()
        toggle_press?.let {
            currentToggle = it
        }
        thumb_press?.let {
            currentThumb = it
        }
    }

    //状态：正常
    override fun changedNormal() {
        super.changedNormal()
        normal()
    }

    private fun normal() {
        currentToggle = toggle
        currentThumb = thumb
        if (isChecked) {
            //选中，开状态
            toggle_checked?.let {
                currentToggle = it
            }
            thumb_checked?.let {
                currentThumb = it
            }
        }

    }

    /**
     * fixme toggle
     */

    //按下
    private var toggle_press: KToogleEntity? = null

    fun toggle_press(block: KToogleEntity.() -> Unit): KToggleView {
        if (toggle_press == null) {
            toggle_press = getToogleEntity().copy()//整个属性全部复制过来。
        }
        block(toggle_press!!)
        invalidate()
        return this
    }

    //鼠标悬浮
    private var toggle_hover: KToogleEntity? = null

    fun toggle_hover(block: KToogleEntity.() -> Unit): KToggleView {
        if (toggle_hover == null) {
            toggle_hover = getToogleEntity().copy()//整个属性全部复制过来。
        }
        block(toggle_hover!!)
        invalidate()
        return this
    }

    //聚焦
    private var toggle_focuse: KToogleEntity? = null

    fun toggle_focuse(block: KToogleEntity.() -> Unit): KToggleView {
        if (toggle_focuse == null) {
            toggle_focuse = getToogleEntity().copy()//整个属性全部复制过来。
        }
        block(toggle_focuse!!)
        invalidate()
        return this
    }

    //选中
    private var toggle_selected: KToogleEntity? = null

    fun toggle_selected(block: KToogleEntity.() -> Unit): KToggleView {
        if (toggle_selected == null) {
            toggle_selected = getToogleEntity().copy()//整个属性全部复制过来。
        }
        block(toggle_selected!!)
        invalidate()
        return this
    }

    //fixme 选中=》开状态
    private var toggle_checked: KToogleEntity? = null

    fun toggle_checked(block: KToogleEntity.() -> Unit): KToggleView {
        if (toggle_checked == null) {
            toggle_checked = getToogleEntity().copy()//整个属性全部复制过来。
        }
        block(toggle_checked!!)
        invalidate()
        return this
    }

    //fixme 正常状态（关状态）
    private var toggle: KToogleEntity? = null

    fun toggle(block: KToogleEntity.() -> Unit): KToggleView {
        clearButonShadow()//自定义圆角，就去除按钮默认的圆角阴影。不然效果不好。
        block(getToogleEntity())
        invalidate()
        return this
    }

    fun getToogleEntity(): KToogleEntity {
        if (toggle == null) {
            toggle = KToogleEntity()
        }
        return toggle!!
    }

    private var currentToggle: KToogleEntity? = null

    /**
     * fixme thumb
     */

    //按下
    private var thumb_press: KThumbEntity? = null

    fun thumb_press(block: KThumbEntity.() -> Unit): KToggleView {
        if (thumb_press == null) {
            thumb_press = getThumbEntity().copy()//整个属性全部复制过来。
        }
        block(thumb_press!!)
        invalidate()
        return this
    }

    //鼠标悬浮
    private var thumb_hover: KThumbEntity? = null

    fun thumb_hover(block: KThumbEntity.() -> Unit): KToggleView {
        if (thumb_hover == null) {
            thumb_hover = getThumbEntity().copy()//整个属性全部复制过来。
        }
        block(thumb_hover!!)
        invalidate()
        return this
    }

    //聚焦
    private var thumb_focuse: KThumbEntity? = null

    fun thumb_focuse(block: KThumbEntity.() -> Unit): KToggleView {
        if (thumb_focuse == null) {
            thumb_focuse = getThumbEntity().copy()//整个属性全部复制过来。
        }
        block(thumb_focuse!!)
        invalidate()
        return this
    }

    //选中
    private var thumb_selected: KThumbEntity? = null

    fun thumb_selected(block: KThumbEntity.() -> Unit): KToggleView {
        if (thumb_selected == null) {
            thumb_selected = getThumbEntity().copy()//整个属性全部复制过来。
        }
        block(thumb_selected!!)
        invalidate()
        return this
    }

    //选中（开状态）
    private var thumb_checked: KThumbEntity? = null

    fun thumb_checked(block: KThumbEntity.() -> Unit): KToggleView {
        if (thumb_checked == null) {
            thumb_checked = getThumbEntity().copy()//整个属性全部复制过来。
        }
        block(thumb_checked!!)
        invalidate()
        return this
    }

    //fixme 正常状态（关闭状态）
    private var thumb: KThumbEntity? = null

    fun thumb(block: KThumbEntity.() -> Unit): KToggleView {
        clearButonShadow()//自定义圆角，就去除按钮默认的圆角阴影。不然效果不好。
        block(getThumbEntity())
        invalidate()
        return this
    }

    fun getThumbEntity(): KThumbEntity {
        if (thumb == null) {
            thumb = KThumbEntity()
        }
        return thumb!!
    }

    private var currentThumb: KThumbEntity? = null

    override fun onDestroy() {
        super.onDestroy()
        toggle = null
        toggle_focuse = null
        toggle_hover = null
        toggle_press = null
        toggle_selected = null
        currentToggle = null
        thumb = null
        thumb_focuse = null
        thumb_hover = null
        thumb_press = null
        thumb_selected = null
        currentThumb = null
        onChangedListener = null
        colorMatrix = null
    }
}

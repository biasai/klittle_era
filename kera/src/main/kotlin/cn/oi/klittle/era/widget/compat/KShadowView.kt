package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.*
import cn.oi.klittle.era.base.KBaseView
import cn.oi.klittle.era.entity.widget.compat.KShadowEntity
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KRadiusUtils
import org.jetbrains.anko.textColor


//            KToggleView比KShadowView效果要好；阴影建议使用KToggleView；fixme 现在已经改良，效果不错。可以使用。

//阴影背景

//            KShadowView(this).apply {
//                shadow_radius = kpx.x(18f)//阴影半径
//                shadow {
//                    shadow_dx = 0f
//                    //shadow_dy = shadow_radius/2//阴影偏移量
//                    shadow_color = Color.BLACK//阴影色
//                    all_radius(kpx.x(812f))//圆角
//                    bg_color=Color.WHITE//背景色
//                    //bgHorizontalColors(Color.parseColor("#28292E"), Color.parseColor("#2B2C31"), Color.parseColor("#2A2B30"))
//                }
//                param {
//                    width = kpx.x(600)
//                    height = width
//                }
//                param_press {
//                    width = kpx.x(500)
//                    height = width
//                }
//            }.lparams {
//                topMargin = kpx.x(50)
//                width = kpx.x(600)
//                height = width
//            }
//            fixme 圆角阴影图片；已经支持圆角切割了。一个控件就能实现阴影圆角图片
//            KShadowView(this).apply {
//                shadow_radius = kpx.x(18f)//阴影半径
//                shadow {
//                    shadow_dx = 0f
//                    shadow_dy =shadow_radius / 2//阴影偏移量
//                    shadow_color = Color.BLACK//阴影色
//                    all_radius(w.toFloat())//圆角
//                    //left_top=0f
//                    //right_bottom=0f
//                    //fixme 最好加上边框，效果好看。（不加边框，总感觉边缘有一条细小的白边。很小不影响。）
//                    strokeWidth=kpx.x(2f)
//                    strokeColor=Color.BLACK
//                    isDST_IN=true//fixme true会有圆角切割效果；false不会
//                }
//                //等控件加载完了再设置图片（防止dw,dh为空）
//                mGone {
//                    autoBg {
//                        width = w//与阴影同等宽和高
//                        height = h
//                        isAutoCenter = false//fixme 不居中；
//                        autoLeftPadding = dw.toFloat()//与阴影对齐
//                        autoTopPadding = dh.toFloat()
//                        autoBg(R.mipmap.p2)
//                    }
//                }
//            }.lparams {
//                topMargin = kpx.x(50)
//                width = kpx.x(600)
//                height = width
//            }

/**
 * fixme 阴影圆角组件（这个圆角真实有效，能够遮挡下面，不受硬件加速和关闭影响），升级版
 * Created by 彭治铭 on 2018/7/1.
 */
open class KShadowView : K5LparamWidget {

    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }

    var windowKey: String? = null//悬浮窗标志

    init {
        //fixme 必须关闭硬件加速，阴影不支持(实在不支持)；（9.0及以上阴影才支持硬件加速；低版本的不支持）
        //fixme 圆角现在对硬件加速和关闭硬件加速都支持。
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        clearButonShadow()//去掉button默认的点击阴影
        gravity = Gravity.CENTER//文本居中
    }


    var p = 1.25f//系数;防止阴影显示不全。fixme 测试发现 1 就足够了,为了以防万一,还是设置为1.25(绝对足够)

    //fixme 阴影的边界；
    var leftOffset = 0
    var topOffset = 0
    var rightOffset = 0
    var bottomOffset = 0

    //fixme 在onMeasure()中和canvas画布中会调用
    private fun measureDWDH() {
        if (shadow_radius > 0) {
            //左右偏移量
            if (shadow_dx > 0) {
                leftOffset = (shadow_radius * p).toInt()
                rightOffset = leftOffset + shadow_dx.toInt()
            } else {
                leftOffset = (shadow_radius * p - shadow_dx).toInt()
                rightOffset = (shadow_radius * p).toInt()
            }
            //上下偏移量
            if (shadow_dy > 0) {
                topOffset = (shadow_radius * p).toInt()
                bottomOffset = topOffset + shadow_dy.toInt()
            } else {
                topOffset = (shadow_radius * p - shadow_dy).toInt()
                bottomOffset = (shadow_radius * p).toInt()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //setMeasuredDimension()//fixme 不要使用这个方法,固定宽和高,会发生很多问题.不好;所以取消了rw,和rh;就使用正常的width和height
    }

    //fixme 阴影半径和偏移量，各个状态必须统一起来。
    var shadow_radius = kpx.x(8f)
        //阴影半径，决定了阴影的长度
        set(value) {
            field = value
            requestLayout()
        }
    var shadow_dx = kpx.x(0f)
        //x偏移量（阴影左右方向），0 阴影居中，小于0，阴影偏左，大于0,阴影偏右
        set(value) {
            field = value
            requestLayout()
        }
    var shadow_dy = kpx.x(0f)
        //y偏移量(阴影上下方法)，0 阴影居中，小于0，阴影偏上，大于0,阴影偏下
        set(value) {
            field = value
            requestLayout()
        }

    private var shadowModel: KShadowEntity? = null
    override fun draw2First(canvas: Canvas, paint: Paint) {
        super.draw2First(canvas, paint)
        //fixme 防止字体被遮挡,所以先画
        if (shadow != null) {
            canvas?.let {
                shadowModel = null
                if (isPressed && shadow_press != null) {
                    //按下
                    shadowModel = shadow_press
                } else if (isHovered && shadow_hover != null) {
                    //鼠标悬浮
                    shadowModel = shadow_hover
                } else if (isFocused && shadow_focuse != null) {
                    //聚焦
                    shadowModel = shadow_focuse
                } else if (isSelected && shadow_selected != null) {
                    //选中
                    shadowModel = shadow_selected
                } else {
                    shadowModel = shadow//正常
                }
                //不可用，优先级最高
                if (!isEnabled && shadow_notEnable != null) {
                    shadowModel = shadow_notEnable
                }
                //正常
                if (shadowModel == null) {
                    shadowModel = shadow
                }
                shadowModel?.let {
                    it?.textColor?.let {
                        //getCurrentTextColor()获取当前文本的颜色值
                        if (it != getCurrentTextColor()) {
                            textColor = it//重新设置颜色值
                        }
                    }
                    it.textSize?.let {
                        if (it > 0) {
                            var px = kpx.dpToPixel(it)
                            if (px != textSize) {
                                textSize = it//fixme 重新设置文本的大小。文本大小，set的时候单位的dp，get获取的时候，单位的px
                            }
                        }
                    }
                }
                shadowModel?.apply {
                    var paint = KBaseView.getPaint()
                    paint.isDither = true
                    paint.isAntiAlias = true
                    paint.strokeWidth = 0f
                    paint.style = Paint.Style.FILL//fixme 必须是填充类型；Paint.Style.STROKE边框类型是没有阴影效果的。
                    measureDWDH()
                    //fixme dw即left;dh即top
                    var left = leftOffset.toFloat() + scrollX
                    var top = topOffset.toFloat() + scrollY
                    var right = (w - rightOffset).toFloat() + scrollX
                    var bottom = (h - bottomOffset).toFloat() + scrollY
                    //KLoggerUtils.e("阴影:\tleft:\t"+left+"\ttop:\t"+top+"\tright:\t"+right+"\tbottom:\t"+bottom)
                    paint.color = bg_color//fixme 背景色;不能为透明；不然什么都不会显示。
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
                            var dashPathEffect = DashPathEffect(floatArrayOf(dashWidth, dashGap), phase)
                            paint.setPathEffect(dashPathEffect)
                        }
                        canvas.drawPath(path, paint)
                        //控制虚线流动性
                        if (isdashFlow && (dashWidth > 0 && dashGap > 0)) {
                            if (dashSpeed > 0) {
                                if (phase >= Float.MAX_VALUE - dashSpeed) {
                                    phase = 0f
                                }
                            } else {
                                if (phase >= Float.MIN_VALUE - dashSpeed) {
                                    phase = 0f
                                }
                            }
                            phase += dashSpeed
                            invalidate()
                        }
                    }
                    if (isDST_IN) {
                        //fixme 新建图层;这样圆角切割的时候就不会对阴影产生影响。
                        layerId = canvas.saveLayer(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint, Canvas.ALL_SAVE_FLAG)
                    }
                }
            }
        }
    }

    var layerId = 0;
    private var phase: Float = 0F

    override fun draw2Last(canvas: Canvas, paint: Paint) {
        super.draw2Last(canvas, paint)
        //画圆角
        drawRadius(canvas, this)
    }

    private var kradius: KRadiusUtils? = KRadiusUtils()

    //fixme 画圆角(对阴影控件是有效的)
    fun drawRadius(canvas: Canvas, view: View) {
        view?.apply {
            shadowModel?.let {
                if (!it.isDST_IN) {
                    return//fixme 不取下面的交集。
                }
                //画圆角
                kradius?.apply {
                    x = leftOffset.toFloat()
                    y = topOffset.toFloat()
                    w = this@KShadowView.w - (leftOffset + rightOffset)
                    h = this@KShadowView.h - (topOffset + bottomOffset)
                    //KLoggerUtils.e("圆角:\tleft:\t"+x+"\ttop:\t"+y+"\tright:\t"+w+"\tbottom:\t"+h)
                    isDST_IN = true//fixme 取下面的交集
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
                    drawRadius(canvas, phase, view.scrollX, view.scrollY)
                }
            }
        }
    }

    //fixme 不可用状态
    private var shadow_notEnable: KShadowEntity? = null

    fun shadow_notEnable(block: KShadowEntity.() -> Unit): KShadowView {
        if (shadow_notEnable == null) {
            shadow_notEnable = shadow?.copy()//整个属性全部复制过来。
        }
        block(shadow_notEnable!!)
        invalidate()
        requestLayout()
        return this
    }

    //按下
    private var shadow_press: KShadowEntity? = null

    fun shadow_press(block: KShadowEntity.() -> Unit): KShadowView {
        if (shadow_press == null) {
            shadow_press = shadow?.copy()//整个属性全部复制过来。
        }
        block(shadow_press!!)
        invalidate()
        requestLayout()
        return this
    }

    //鼠标悬浮
    private var shadow_hover: KShadowEntity? = null

    fun shadow_hover(block: KShadowEntity.() -> Unit): KShadowView {
        if (shadow_hover == null) {
            shadow_hover = shadow?.copy()//整个属性全部复制过来。
        }
        block(shadow_hover!!)
        invalidate()
        requestLayout()
        return this
    }

    //聚焦
    private var shadow_focuse: KShadowEntity? = null

    fun shadow_focuse(block: KShadowEntity.() -> Unit): KShadowView {
        if (shadow_focuse == null) {
            shadow_focuse = shadow?.copy()//整个属性全部复制过来。
        }
        block(shadow_focuse!!)
        invalidate()
        requestLayout()
        return this
    }

    //选中
    private var shadow_selected: KShadowEntity? = null

    fun shadow_selected(block: KShadowEntity.() -> Unit): KShadowView {
        if (shadow_selected == null) {
            shadow_selected = shadow?.copy()//整个属性全部复制过来。
        }
        block(shadow_selected!!)
        invalidate()
        requestLayout()
        return this
    }

    //正常状态
    private var shadow: KShadowEntity? = KShadowEntity()

    fun shadow(block: KShadowEntity.() -> Unit): KShadowView {
        block(shadow!!)
        invalidate()
        requestLayout()
        return this
    }

    override fun onDestroy() {
        super.onDestroy()
        shadow_focuse = null
        shadow_hover = null
        shadow_press = null
        shadow_selected = null
        shadow = null
        shadowModel = null
        windowKey = null
        shadow_notEnable=null
    }

}
package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.entity.widget.compat.KPathEntity

//                fixme 调用案例，以下设置了两个异形图
//                relativeLayout {
//
//                    kPathView {
//                        //backgroundColor(Color.LTGRAY)
//                        path {
//                            //基准宽和高（会根据控件大小，自动适配）
//                            baseWidth = kpx.x(600f)
//                            baseHeight = kpx.x(600f)
//                            //path坐标点集合，最后会首尾巴相连。内部调用了path.close()
//                            addPoint(kpx.x(30), kpx.x(15))
//                            addPoint(kpx.x(500), kpx.x(15))
//                            addPoint(kpx.x(300), kpx.x(500))
//                            addPoint(kpx.x(30), kpx.x(500))
//                            strokeHorizontalColors(Color.RED, Color.YELLOW, Color.BLUE)
//                            radius = kpx.x(50f)
//                            dashGap = kpx.x(15f)
//                            dashWidth = kpx.x(10f)
//                            strokeWidth=0f
//                            isRegionEnable=true//是否做区域判断
//                            shadow_color=Color.CYAN//阴影颜色;透明色Color.TRANSPARENT不显示阴影。
//                        }
//                        if (Build.VERSION.SDK_INT>=21) {
//                            z = 100f//默认数值是0；数值越到，越显示在上面。
//                        }
//                        onClick {
//                            KToast.showInfo("区域一")
//                        }
//                        param {
//                            width = kpx.x(600)
//                            height = kpx.x(500)
//                            topMargin = kpx.x(80)
//                        }
//                        param_press {
//                            width = kpx.x(620)
//                            height = kpx.x(530)
//                        }
//                        autoBg {
//                            isAutoCenter=false
//                            width = kpx.x(640)
//                            height = kpx.x(795)
//                            autoBg(R.mipmap.timg)
//                        }
//                    }.lparams {
//                        width = kpx.x(600)
//                        height = kpx.x(500)
//                        topMargin = kpx.x(80)
//                    }
//
//                    kPathView {
//                        //backgroundColor(Color.LTGRAY)
//                        path {
//                            //基准宽和高（会根据控件大小，自动适配）
//                            baseWidth = kpx.x(600f)
//                            baseHeight = kpx.x(600f)
//                            //path坐标点集合，最后会首尾巴相连。内部调用了path.close()
//                            addPoint(kpx.x(300), kpx.x(15))
//                            addPoint(kpx.x(550), kpx.x(15))
//                            addPoint(kpx.x(550), kpx.x(500))
//                            addPoint(kpx.x(15), kpx.x(500))
//                            strokeHorizontalColors(Color.RED, Color.YELLOW, Color.BLUE)
//                            radius = kpx.x(50f)
//                            dashGap = kpx.x(15f)
//                            dashWidth = kpx.x(10f)
//                            strokeWidth=0f
//                            isRegionEnable=true//是否做区域判断
//                            shadow_color=Color.BLACK
//                        }
//                        if (Build.VERSION.SDK_INT>=21) {
//                            z = 100f//默认数值是0；数值越到，越显示在上面。
//                        }
//                        onClick {
//                            KToast.showInfo("区域二")
//                        }
//                        param {
//                            width = kpx.x(500)
//                            height = kpx.x(500)
//                            leftMargin = kpx.x(350)
//                            topMargin = kpx.x(80)
//                        }
//                        autoBg {
//                            isAutoCenter=false
//                            width = kpx.x(640)
//                            height = kpx.x(795)
//                            autoBg(R.mipmap.timg)
//                        }
//                    }.lparams {
//                        width = kpx.x(500)
//                        height = kpx.x(500)
//                        leftMargin = kpx.x(350)
//                        rightMargin = leftMargin
//                        topMargin = kpx.x(80)
//                    }
//
//                }.lparams {
//                    width= matchParent
//                    height=kpx.x(700)
//                }

/**
 * fixme 路径控件（异形控件）
 */
open class KPathView : KView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    //不可用
    var path_enable: KPathEntity? = null

    fun path_enable(block: KPathEntity.() -> Unit): KPathView {
        if (path_enable == null) {
            path_enable = getmPath().copy()//整个属性全部复制过来。
        }
        block(path_enable!!)
        invalidate()
        return this
    }

    //按下
    var path_press: KPathEntity? = null

    fun path_press(block: KPathEntity.() -> Unit): KPathView {
        if (path_press == null) {
            path_press = getmPath().copy()//整个属性全部复制过来。
        }
        block(path_press!!)
        invalidate()
        return this
    }

    //鼠标悬浮
    var path_hover: KPathEntity? = null

    fun path_hover(block: KPathEntity.() -> Unit): KPathView {
        if (path_hover == null) {
            path_hover = getmPath().copy()//整个属性全部复制过来。
        }
        block(path_hover!!)
        invalidate()
        return this
    }

    //聚焦
    var path_focuse: KPathEntity? = null

    fun path_focuse(block: KPathEntity.() -> Unit): KPathView {
        if (path_focuse == null) {
            path_focuse = getmPath().copy()//整个属性全部复制过来。
        }
        block(path_focuse!!)
        invalidate()
        return this
    }

    //选中
    var path_selected: KPathEntity? = null

    fun path_selected(block: KPathEntity.() -> Unit): KPathView {
        if (path_selected == null) {
            path_selected = getmPath().copy()//整个属性全部复制过来。
        }
        block(path_selected!!)
        invalidate()
        return this
    }

    //正常状态
    var path: KPathEntity? = null

    private fun getmPath(): KPathEntity {
        if (path == null) {
            path = KPathEntity()
        }
        return path!!
    }

    fun path(block: KPathEntity.() -> Unit): KPathView {
        block(getmPath())
        invalidate()
        return this
    }

    private var modelPath: KPathEntity? = null
    private var pathPath: Path? = Path()
    override fun draw2First(canvas: Canvas, paint: Paint) {
        super.draw2First(canvas, paint)
        if (path != null) {
            if (!isEnabled && path_enable != null) {
                //不可用
                modelPath = path_enable
            } else if (isPressed && path_press != null) {
                //按下
                modelPath = path_press
            } else if (isHovered && path_hover != null) {
                //鼠标悬浮
                modelPath = path_hover
            } else if (isFocused && path_focuse != null) {
                //聚焦
                modelPath = path_focuse
            } else if (isSelected && path_selected != null) {
                //选中
                modelPath = path_selected
            }
            //正常
            if (modelPath == null) {
                modelPath = path
            }
            modelPath?.apply {
                points?.let {
                    if (it.size > 0) {
                        if (pathPath == null) {
                            pathPath = Path()
                        }
                        pathPath?.reset()
                        for (i in 0..it.lastIndex) {
                            var x = it[i].x
                            var y = it[i].y
                            if (baseWidth > 0 && baseHeight > 0) {
                                //fixme 基准宽高大于0，按比例适配缩放
                                x = width.toFloat() / baseWidth.toFloat() * x
                                y = height.toFloat() / baseHeight.toFloat() * y
                            }
                            if (i == 0) {
                                pathPath?.moveTo(x, y)
                            } else {
                                pathPath?.lineTo(x, y)
                            }
                        }
                        pathPath?.close()//fixme 最后一定要闭合(如果有圆角radius的话，会有起点也会有圆角效果)
                        //fixme 绘制阴影
                        if (shadow_color != Color.TRANSPARENT && shadow_radius > 0) {
                            paint.style = Paint.Style.FILL//fixme 必须是填充类型；Paint.Style.STROKE边框类型是没有阴影效果的。
                            if (radius > 0) {
                                //圆角
                                var cornerPathEffect = CornerPathEffect(radius)
                                paint.setPathEffect(cornerPathEffect)
                            }
                            //fixme 设置阴影，阴影色受背景色影响。白色背景是最明显的;要想有阴影，shadow_color就不能为透明。这个要注意。（方向渐变色的颜色高于shadow_color）
                            paint.setShadowLayer(shadow_radius, shadow_dx, shadow_dy, shadow_color)
                            canvas.drawPath(pathPath, paint)
                            if (isDST_IN) {
                                //fixme 新建图层;这样圆角切割的时候就不会对阴影产生影响。
                                canvas.saveLayer(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint, Canvas.ALL_SAVE_FLAG)
                            }
                        }
                    }
                }
            }
        }
    }

    var linePathPhase: Float = 0F//横线偏移量
    private var canvasPath2: Canvas? = null
    private var pathRegion: Region? = null
    private var isRegionEnable2: Boolean = true//是否开启区域点击判断
    override fun draw2Last(canvas: Canvas, paint: Paint) {
        super.draw2Last(canvas, paint)
        if (path != null) {
            modelPath?.apply {
                points?.let {
                    if (it.size > 0) {
                        if (pathPath == null) {
                            return
                        }
                        //path.lineTo(it[0].x,it[0].y)//起始点没有圆角效果
                        //画笔样式
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = strokeWidth
                        paint.color = strokeColor
                        //边框颜色渐变，渐变颜色优先等级大于正常颜色。
                        var linearGradient: LinearGradient? = null
                        //渐变颜色数组必须大于等于2
                        if (strokeVerticalColors != null) {
                            if (!isStrokeGradient) {
                                //垂直不渐变
                                linearGradient = getNotLinearGradient(0f, height.toFloat(), strokeVerticalColors!!, true)
                            }
                            //fixme 垂直渐变
                            if (linearGradient == null) {
                                linearGradient = LinearGradient(0f, 0f, 0f, bottom.toFloat(), strokeVerticalColors, null, Shader.TileMode.CLAMP)
                            }
                        } else if (strokeHorizontalColors != null) {
                            if (!isStrokeGradient) {
                                //水平不渐变
                                linearGradient = getNotLinearGradient(0f, width.toFloat(), strokeHorizontalColors!!, false)
                            }
                            //fixme 水平渐变
                            if (linearGradient == null) {
                                linearGradient = LinearGradient(0f, 0f, width.toFloat(), 0f, strokeHorizontalColors, null, Shader.TileMode.CLAMP)
                            }
                        }
                        linearGradient?.let {
                            paint.setShader(linearGradient)
                        }
                        var cornerPathEffect: CornerPathEffect? = null
                        if (radius > 0) {
                            //圆角
                            cornerPathEffect = CornerPathEffect(radius)
                        }
                        /**
                         * fixme 8.0;和9.0之后；圆角的矩形范围只包含圆角矩形内的范围；不再是整个矩形的范围。
                         * fixme 以下修复了圆角无效的问题。
                         * fixme 现在开不开硬件加速；都无所谓了。都支持圆角了。
                         */
                        if (isDST_IN) {
                            if (radius > 0) {
                                //fixme path.setFillType(Path.FillType.INVERSE_WINDING)不支持圆角属性，所有在此只能通过path转位图的方式实现了。
                                paint.setPathEffect(cornerPathEffect)
                                paint.style = Paint.Style.FILL_AND_STROKE
                                var bitmapPath2 = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)//fixme 每次都新建，且即用即消。这样不会出现显示上的缓存异常。亲测可行。
                                if (canvasPath2 == null) {
                                    canvasPath2 = Canvas(bitmapPath2)
                                } else {
                                    canvasPath2?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)//画布清除
                                    canvasPath2?.setBitmap(bitmapPath2)
                                }
                                canvasPath2?.drawPath(pathPath, paint)
                                paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))//取下面交集
                                canvas.drawBitmap(bitmapPath2, 0f, 0f, paint)
                                bitmapPath2?.recycle()//fixme 这个就在这里，即用即消。
                                bitmapPath2 = null
                            } else {
                                paint.setPathEffect(null)
                                paint.style = Paint.Style.FILL_AND_STROKE
                                paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))//清除
                                pathPath?.setFillType(Path.FillType.INVERSE_WINDING)//反转(不支持圆角属性，即不支持paint.setPathEffect(cornerPathEffect)，不然么有效果)
                                canvas.drawPath(pathPath, paint)
                                pathPath?.setFillType(Path.FillType.WINDING)//恢复正常
                                paint.setXfermode(null)
                            }
                        }
                        //fixme 区域判断
                        if (pathRegion == null) {
                            pathRegion = Region()
                        }
                        pathRegion?.set(0, 0, width, height)
                        pathRegion?.setPath(pathPath, pathRegion)
                        isRegionEnable2 = isRegionEnable//判断是否开启区域点击判断
                        if (strokeWidth > 0) {
                            paint.setXfermode(null)
                            paint.style = Paint.Style.STROKE
                            //虚线
                            if (dashWidth > 0 && dashGap > 0) {
                                var dashPathEffect = DashPathEffect(floatArrayOf(dashWidth, dashGap), linePathPhase)
                                if (cornerPathEffect != null) {
                                    paint.setPathEffect(ComposePathEffect(dashPathEffect, cornerPathEffect))//圆角cornerPathEffect放在dashPathEffect后面才有效。
                                } else {
                                    paint.setPathEffect(dashPathEffect)
                                }
                            } else if (cornerPathEffect != null) {
                                paint.setPathEffect(cornerPathEffect)
                            }
                            canvas.drawPath(pathPath, paint)
                        }
                        //虚线有效果，和硬件加速无关。
                        //控制虚线流动性【虚线的流动，不影响光标，也不影响文本输入。】
                        if (strokeWidth > 0 && isdashFlow && (dashWidth > 0 && dashGap > 0)) {
                            if (dashSpeed > 0) {
                                if (linePathPhase >= Float.MAX_VALUE - dashSpeed) {
                                    linePathPhase = 0f
                                }
                            } else {
                                if (linePathPhase >= Float.MIN_VALUE - dashSpeed) {
                                    linePathPhase = 0f
                                }
                            }
                            linePathPhase += dashSpeed
                            invalidate()
                        }
                    }
                }
            }
        }
    }

    private var isRegFirst: Boolean = false//fixme 判断是否第一次按下在点击区域。
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        //fixme 区域事件点击判断
        if (!isRegFirst) {
            pathRegion?.apply {
                if (isRegionEnable2) {
                    event?.let {
                        if (!contains(it.x.toInt(), it.y.toInt())) {
                            return false//fixme 这样就不会拦截其他控件的点击事件了。亲测有效。
                        }
                    }
                }
            }
        }
        event?.let {
            when (it.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    isRegFirst = true//fixme 第一次按下，在点击区域。（防止手指在以往的区域离开，状态异常。）
                }
                MotionEvent.ACTION_MOVE -> {
                }
                MotionEvent.ACTION_UP -> {
                    isRegFirst = false
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        modelPath = null
        canvasPath2 = null
        path = null
        path_enable = null
        path_press = null
        path_focuse = null
        path_selected = null
        path_hover = null
    }

}
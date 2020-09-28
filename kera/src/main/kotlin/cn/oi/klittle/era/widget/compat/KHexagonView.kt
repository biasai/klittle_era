package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.entity.widget.compat.KHexagonEntity

//                fixme 使用案例：

//                    khexagonView{
//                        backgroundColor(Color.RED)
//                        hexagon {
//                            bg_color = Color.YELLOW
//                            bgHorizontalColors(Color.BLACK, Color.WHITE)
//                            //bgVerticalColors(Color.BLACK,Color.WHITE)
//                            isBgGradient = false
//                            strokeHorizontalColors(Color.WHITE, Color.BLACK)
//                            strokeColor = Color.BLUE
//                            strokeWidth = kpx.x(3f)
//                        }
//                        hexagon_press {
////                            bg_color = Color.LTGRAY
////                            dashWidth = kpx.x(15f)
////                            dashGap = dashWidth
//                            //all_radius = kpx.x(45f)
//                            //bgVerticalColors(Color.YELLOW,Color.CYAN)
//                            //isBgGradient=false
//                            rotation=90f//旋转角度
//                            isPorterDuffXfermode=true//fixme 切割时，建议不要使用圆角。不然效果不好。
//                        }
//                        text="S"
//                        gravity=Gravity.CENTER
//                        textColor=Color.RED
//                        textSize=kpx.textSizeX(80f)
//                    }.lparams {
//                        width = kpx.x(300)
//                        height = kpx.x(200)
//                        topMargin=kpx.x(30)
//                    }

/**
 * fixme 六边形控件（居中绘制）;具备切割能力(isPorterDuffXfermode为true时有效)。有圆角效果时，切割效果不好。有切割时，最好不要有圆角。（切割对CornerPathEffect不支持）
 */
open class KHexagonView : KView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    //按下
    var hexagon_press: KHexagonEntity? = null

    fun hexagon_press(block: KHexagonEntity.() -> Unit): KHexagonView {
        if (hexagon_press == null) {
            hexagon_press = getmHexagon().copy()//整个属性全部复制过来。
        }
        block(hexagon_press!!)
        invalidate()
        return this
    }

    //鼠标悬浮
    var hexagon_hover: KHexagonEntity? = null

    fun hexagon_hover(block: KHexagonEntity.() -> Unit): KHexagonView {
        if (hexagon_hover == null) {
            hexagon_hover = getmHexagon().copy()//整个属性全部复制过来。
        }
        block(hexagon_hover!!)
        invalidate()
        return this
    }

    //聚焦
    var hexagon_focuse: KHexagonEntity? = null

    fun hexagon_focuse(block: KHexagonEntity.() -> Unit): KHexagonView {
        if (hexagon_focuse == null) {
            hexagon_focuse = getmHexagon().copy()//整个属性全部复制过来。
        }
        block(hexagon_focuse!!)
        invalidate()
        return this
    }

    //选中
    var hexagon_selected: KHexagonEntity? = null

    fun hexagon_selected(block: KHexagonEntity.() -> Unit): KHexagonView {
        if (hexagon_selected == null) {
            hexagon_selected = getmHexagon().copy()//整个属性全部复制过来。
        }
        block(hexagon_selected!!)
        invalidate()
        return this
    }

    //fixme 正常状态（先写正常样式，再写其他状态的样式，因为其他状态的样式初始值是复制正常状态的样式的。）
    var hexagon: KHexagonEntity? = null

    fun getmHexagon(): KHexagonEntity {
        if (hexagon == null) {
            hexagon = KHexagonEntity()
        }
        return hexagon!!
    }

    fun hexagon(block: KHexagonEntity.() -> Unit): KHexagonView {
        block(getmHexagon())
        invalidate()
        return this
    }


    override fun draw2Front(canvas: Canvas, paint: Paint) {
        super.draw2Front(canvas, paint)
        drawHexagon(canvas, paint, this)
    }

    private var hexagonModel: KHexagonEntity? = null
    private fun drawHexagon(canvas: Canvas, paint: Paint, view: View) {
        view?.apply {
            if (hexagon != null) {
                hexagonModel = null
                if (isPressed && hexagon_press != null) {
                    //按下
                    hexagonModel = hexagon_press
                } else if (isHovered && hexagon_hover != null) {
                    //鼠标悬浮
                    hexagonModel = hexagon_hover
                } else if (isFocused && hexagon_focuse != null) {
                    //聚焦
                    hexagonModel = hexagon_focuse
                } else if (isSelected && hexagon_selected != null) {
                    //选中
                    hexagonModel = hexagon_selected
                }
                //正常
                if (hexagonModel == null) {
                    hexagonModel = hexagon
                }
                hexagonModel?.let {
                    if (it.isDraw) {
                        paint.setShader(null)
                        drawHexagon(canvas, paint, it, view)
                        paint.setShader(null)//防止其他地方受影响，所以渲染清空。
                    }
                }
            }
        }
    }

    //画五角星。
    private fun drawHexagon(canvas: Canvas, paint: Paint, triangle: KHexagonEntity, view: View) {
        view?.apply {
            var rw = width
            if (rw > height) {
                rw = height
            }
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
            //KLoggerUtils.e("五角星坐标：\t"+floats?.size)
            var mWidth = width
            var mHeight = height

            // 计算中心点

            // 计算中心点
            var centreX = mWidth / 2f
            var centreY = mHeight / 2f

            var mLenght = mWidth / 2//fixme 六变形的边长
            if (mWidth>mHeight){
                mLenght = mHeight / 2
            }

            val radian30 = 30 * Math.PI / 180
            val a = (mLenght * Math.sin(radian30)).toFloat()//fixme 最左上角点X坐标
            val b = (mLenght * Math.cos(radian30)).toFloat()//fixme 两点相邻的垂直距离。
            val c: Float = (mHeight - 2 * b) / 2//fixme 最左上角点y坐标


            val path = Path()
//            path.moveTo(width.toFloat(), (height / 2).toFloat())//最右边点（以下点，顺时针去画的）
//            path.lineTo(width - a, height - c)//最右下角点
//            path.lineTo(width - a - mLenght, height - c)//最左下角点
//            path.lineTo(0f, (height / 2).toFloat())//最左边点
//            path.lineTo(a, c)//fixme 最左上角点
//            path.lineTo(width - a, c)//最右上角点。
            path.moveTo(centreX+a+mLenght/2, centreY.toFloat())//最右边点（以下点，顺时针去画的）
            path.lineTo(centreX+mLenght/2, centreY.toFloat() - b)//最右下角点
            path.lineTo(centreX - mLenght/2, centreY.toFloat() - b)//最左下角点
            path.lineTo(centreX - mLenght/2-a, centreY.toFloat())//最左边点
            path.lineTo(centreX - mLenght/2, centreY.toFloat()+b)//最左上角点
            path.lineTo(centreX+mLenght/2, centreY.toFloat()+b)//最右上角点
            path.close()
            if (triangle.all_radius != 0f) {
                paint?.setPathEffect(CornerPathEffect(triangle.all_radius))
            } else {
                //paint?.setPathEffect(null)
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
                    if (!triangle.isBgGradient) {
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
                    if (!triangle.isBgGradient) {
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
//                path.moveTo(width.toFloat(), (height / 2).toFloat())
//                path.lineTo(width - a, height - c)
//                path.lineTo(width - a - mLenght, height - c)
//                path.lineTo(0f, (height / 2).toFloat())
//                path.lineTo(a, c)
//                path.lineTo(width - a, c)
                path.moveTo(centreX+a+mLenght/2, centreY.toFloat())//最右边点（以下点，顺时针去画的）
                path.lineTo(centreX+mLenght/2, centreY.toFloat() - b)//最右下角点
                path.lineTo(centreX - mLenght/2, centreY.toFloat() - b)//最左下角点
                path.lineTo(centreX - mLenght/2-a, centreY.toFloat())//最左边点
                path.lineTo(centreX - mLenght/2, centreY.toFloat()+b)//最左上角点
                path.lineTo(centreX+mLenght/2, centreY.toFloat()+b)//最右上角点
                path.close()
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

    override fun onDestroy() {
        super.onDestroy()
        hexagon = null
        hexagon_focuse = null
        hexagon_hover = null
        hexagon_press = null
        hexagon_selected = null
        hexagonModel = null
    }

}
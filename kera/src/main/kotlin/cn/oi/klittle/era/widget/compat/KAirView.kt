package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.entity.widget.KAirEntry
import cn.oi.klittle.era.entity.widget.compat.KHexagonEntity
import cn.oi.klittle.era.utils.KCanvasUtils

//                fixme 使用案例（对话框样式）：

//                    kAirView {
//                        //backgroundColor(Color.YELLOW)
//                        air {
//                            direction = KAirEntry.DIRECTION_LEFT//气泡方向在左边；默认是居中。
//                            bg_color = Color.LTGRAY
//                            bgHorizontalColors(Color.WHITE,Color.parseColor("#EEEEEE"),Color.WHITE,Color.parseColor("#EEEEEE"))
//                            isBgGradient=true//背景颜色渐变
//                            strokeColor = Color.CYAN
//                            //strokeHorizontalColors(Color.BLUE,Color.RED)
//                            //isStrokeGradient=false//边框颜色不渐变
//                            strokeWidth = kpx.x(2f)
//                            //fixme 注意；一定要先设置宽度和高度；然后再调用。
//                            airWidth = kpx.x(30)//气泡三角的宽度
//                            airHeight = airWidth
//                            //xOffset = 0f//fixme 气泡三角，x轴偏移量，正数向右偏移，负数向左偏移。亲测有效。
//                            //yOffset = -kpx.x(30f)//气泡三角，y轴偏移量；正数向下偏移，负数向上偏移。
//                            all_radius =45f//圆角（所有的圆角；包括气泡）
//                            //isAirRadius = true//气泡三角是否具备圆角,true圆角，(all_radius大于0才有效)
//                            //isAirBorderRadius=true//fixme 气泡三角的两个边的连接处是否具有圆角效果。,true圆角，false尖角。
//                            dashWidth=kpx.x(15f)
//                            dashGap=kpx.x(10f)
//                        }
//                        txt {
//                            text = "Hello World！我的名字叫诺亚方舟。\n你了"
//                        }
//                        txt_press {
//                            text = ""
//                        }
//                        textColor = Color.WHITE
//                        textSize = kpx.textSizeX(30)
//                        gravity = Gravity.CENTER_VERTICAL
//                        leftPadding = kpx.x(50)
//                        rightPadding = kpx.x(25)
//                        topPadding = kpx.x(50)
//                        bottomPadding = kpx.x(40)
//                    }.lparams {
//                        topMargin = kpx.x(30)
//                        leftMargin = topMargin
//                        width = wrapContent
//                        height = wrapContent
//                    }

/**
 * fixme 聊天气泡控件;不具备切割能力。
 */
open class KAirView : KTextView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    //按下
    var air_press: KAirEntry? = null

    fun air_press(block: KAirEntry.() -> Unit): KAirView {
        if (air_press == null) {
            air_press = getMair().copy()//整个属性全部复制过来。
        }
        block(air_press!!)
        invalidate()
        return this
    }

    //鼠标悬浮
    var air_hover: KAirEntry? = null

    fun air_hover(block: KAirEntry.() -> Unit): KAirView {
        if (air_hover == null) {
            air_hover = getMair().copy()//整个属性全部复制过来。
        }
        block(air_hover!!)
        invalidate()
        return this
    }

    //聚焦
    var air_focuse: KAirEntry? = null

    fun air_focuse(block: KAirEntry.() -> Unit): KAirView {
        if (air_focuse == null) {
            air_focuse = getMair().copy()//整个属性全部复制过来。
        }
        block(air_focuse!!)
        invalidate()
        return this
    }

    //选中
    var air_selected: KAirEntry? = null

    fun air_selected(block: KAirEntry.() -> Unit): KAirView {
        if (air_selected == null) {
            air_selected = getMair().copy()//整个属性全部复制过来。
        }
        block(air_selected!!)
        invalidate()
        return this
    }

    //fixme 正常状态（先写正常样式，再写其他状态的样式，因为其他状态的样式初始值是复制正常状态的样式的。）
    var air: KAirEntry? = null

    fun getMair(): KAirEntry {
        if (air == null) {
            air = KAirEntry()
        }
        return air!!
    }

    fun air(block: KAirEntry.() -> Unit): KAirView {
        block(getMair())
        invalidate()
        return this
    }


    override fun draw2Front(canvas: Canvas, paint: Paint) {
        super.draw2Front(canvas, paint)
        drawAir(canvas, paint, this)
    }

    private var airModel: KAirEntry? = null
    private fun drawAir(canvas: Canvas, paint: Paint, view: View) {
        view?.apply {
            if (air != null) {
                airModel = null
                if (isPressed && air_press != null) {
                    //按下
                    airModel = air_press
                } else if (isHovered && air_hover != null) {
                    //鼠标悬浮
                    airModel = air_hover
                } else if (isFocused && air_focuse != null) {
                    //聚焦
                    airModel = air_focuse
                } else if (isSelected && air_selected != null) {
                    //选中
                    airModel = air_selected
                }
                //正常
                if (airModel == null) {
                    airModel = air
                }
                airModel?.let {
                    if (it.isDraw) {
                        paint.setShader(null)
                        drawAir(canvas, paint, it, view)
                        paint.setShader(null)//防止其他地方受影响，所以渲染清空。
                    }
                }
            }
        }
    }

    //画气泡。
    private fun drawAir(canvas: Canvas, paint: Paint, airEnty: KAirEntry, view: View) {
        view?.apply {
            KCanvasUtils.drawAirBubbles(canvas, airEnty, this)
        }
    }

    private var airPhase: Float = 0F

    override fun onDestroy() {
        super.onDestroy()
        air = null
        air_focuse = null
        air_hover = null
        air_press = null
        air_selected = null
        airModel = null
    }

}
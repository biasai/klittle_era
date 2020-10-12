package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.entity.widget.KAirEntry
import cn.oi.klittle.era.entity.widget.compat.KHexagonEntity
import cn.oi.klittle.era.utils.KCanvasUtils
import org.jetbrains.anko.bottomPadding
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.topPadding

//                fixme 使用案例（对话框样式）：

//                    kAirView {
//                        //backgroundColor(Color.YELLOW)
//                        air {
//                            direction = KAirEntry.DIRECTION_LEFT//气泡方向在左边；默认是居中。
//                            bg_color = Color.parseColor("#00CAFC")
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
//                            airOffset=kpx.x(0f)//fixme airOffset是气泡顶点的偏移量，可以实现顶点倾斜效果。xOffset和yOffset是气泡整体的偏移量。
//                            all_radius =45f//圆角（所有的圆角；包括气泡）
//                            //isAirRadius = true//气泡三角是否具备圆角,true圆角，(all_radius大于0才有效)
//                            //isAirBorderRadius=true//fixme 气泡三角的两个边的连接处是否具有圆角效果。,true圆角，false尖角。
//                            dashWidth=kpx.x(15f)
//                            dashGap=kpx.x(10f)
//                            setAutoPaddingForAir(kpx.x(16),this)//fixme 设置文本内补丁。一般都设置为16
//                        }
//                        air_press {
//                            bg_color = Color.parseColor("#00A2CA")
//                            dashWidth = 0f
//                            dashGap = 0f
//                            strokeHorizontalColors(Color.BLUE,Color.RED)
//                        }
//                        txt {
//                            text = "Hello World！我的名字叫诺亚方舟。\n你了"
//                        }
//                        txt_press {
//                            text = ""
//                        }
//                        textColor = Color.WHITE
//                        textSize = kpx.textSizeX(30)
//                        gravity = Gravity.CENTER_VERTICAL//fixme 文本居中方式
//                    }.lparams {
//                        topMargin = kpx.x(30)
//                        leftMargin = topMargin
//                        width = wrapContent//fixme 文本框的宽度和高度随文本变化，自适应。0387037448
//                        height = wrapContent
//                    }

//                    fixme 以下是仿QQ对话框样式。(9文件素材来自于QQ)
//                    kview {
//                        //fixme 仿QQ左对话框(使用9文件)
//                        //fixme 图片气泡三角的宽度大约为13像素；高17显示;该图片放在drawable-hdpi里。
//                        backgroundResource = R.drawable.kera_drawable_click_on_the_selector_sdk_air_left
//                        text = "我的名字叫诺亚方舟。哦\n你了"
//                        leftPadding = kpx.x(24 + 13)
//                        rightPadding = kpx.x(24)
//                        topPadding = kpx.x(16)
//                        bottomPadding = topPadding
//                        gravity = Gravity.CENTER_VERTICAL
//                    }.lparams {
//                        width = wrapContent
//                        height = wrapContent
//                    }
//                    kview {
//                        //fixme 仿QQ右对话框（使用九文件）
//                        //fixme 图片气泡三角的宽度大约为13像素；高17显示；该图片放在drawable-hdpi里。
//                        backgroundResource = R.drawable.kera_drawable_click_on_the_selector_sdk_air_right
//                        text = "我的名字叫诺亚方舟。哦"
//                        leftPadding = kpx.x(24)
//                        rightPadding = kpx.x(24 + 13)
//                        topPadding = kpx.x(16)
//                        bottomPadding = topPadding
//                        gravity = Gravity.CENTER_VERTICAL
//                    }.lparams {
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

    /**
     * fixme 根据气泡的方向，自动设置文本的内补丁。
     * @param mPadding 内补丁；一般都设置为：kpx.x(16)；即8的倍数。8，16，24...比较好。
     * @param airEnty 气泡实体类
     */
    open fun setAutoPaddingForAir(mPadding: Int, airEnty: KAirEntry? = air) {
        airEnty?.apply {
            if (direction == KAirEntry.DIRECTION_LEFT) {
                //左
                rightPadding = mPadding
                leftPadding = airWidth + mPadding
                topPadding = mPadding
                bottomPadding = mPadding
            } else if (direction == KAirEntry.DIRECTION_RIGHT) {
                //右
                leftPadding = mPadding
                rightPadding = airWidth + mPadding
                topPadding = mPadding
                bottomPadding = mPadding
            } else if (direction == KAirEntry.DIRECTION_TOP) {
                //上
                leftPadding = mPadding
                rightPadding = mPadding
                topPadding = mPadding + airHeight
                bottomPadding = mPadding
            } else if (direction == KAirEntry.DIRECTION_BOTTOM) {
                //下
                leftPadding = mPadding
                rightPadding = mPadding
                topPadding = mPadding
                bottomPadding = mPadding + airHeight
            }
        }
    }

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

    //不可用
    var air_notEnable: KAirEntry? = null

    fun air_notEnable(block: KAirEntry.() -> Unit): KAirView {
        if (air_notEnable == null) {
            air_notEnable = getMair().copy()//整个属性全部复制过来。
        }
        block(air_notEnable!!)
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

    //fixme 气泡样式，在系统文字的下面。
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
                if (isEnabled == false && air_notEnable != null) {
                    //不可用
                    airModel = air_notEnable
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
            KCanvasUtils.drawAirBubbles(canvas, paint, airEnty, this)
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
        air_notEnable = null
        airModel = null
    }

}
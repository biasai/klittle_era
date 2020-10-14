package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.entity.widget.compat.KBubblesEntry
//                    fixme 使用案例，仿对话框样式。
//                    kview {
//                        text = "我的名字叫诺亚方舟。哦"
//                        gravity = Gravity.CENTER_VERTICAL
//                        bubbles {
//                            direction = KAirEntry.DIRECTION_LEFT//fixme 气泡方向；如果是左方向，默认就是左垂直居中绘制。即：默认居中绘制。
//                            bubblesWidth = kpx.x(20)
//                            bubblesHeight = kpx.x(30)
//                            bg_color = Color.parseColor("#00CAFC")
//                            xOffset = kpx.x(11f)//气泡的偏移量（整体偏移）；正数向右偏移，负数向左偏移
//                            yOffset = 0f
//                            all_radius = 45f//气泡顶点的圆角角度（只对顶点有效）
//                            bubblesOffset = -kpx.x(20f)//气泡顶点的偏移量
//                        }
//                        bubbles_press {
//                            bg_color = Color.parseColor("#00A2CA")
//                            all_radius = 0f
//                            bubblesOffset = 0f
//                            strokeColor = Color.RED
//                            strokeWidth = kpx.x(3f)//fixme 边框，气泡最右边的边框是不会绘制。方便和radius组合对接。
//                            xOffset = kpx.x(14f)
//                        }
//                        radius {
//                            all_radius(90)
//                            bg_color = Color.parseColor("#00CAFC")
//                            strokeWidth = 0f
//                            leftMargin = kpx.x(30)//左外补丁，控件边框的间距；fixme radius已经做了兼容处理，能够正常切割。
//                            setAutoPaddingForRadius(kpx.x(16), this)//fixme 根据radius的外补丁，自动设置文本内补丁。
//                        }
//                        radius_press {
//                            bg_color = Color.parseColor("#00A2CA")
//                            strokeColor = Color.RED
//                            strokeWidth = kpx.x(3f)
//                        }
//                    }.lparams {
//                        width = kpx.x(300)
//                        height = kpx.x(150)
//                    }

/**
 * fixme 气泡三角形
 */
open class K6BubblesWidget : K5LparamWidget {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}


    //按下
    var bubbles_press: KBubblesEntry? = null

    fun bubbles_press(block: KBubblesEntry.() -> Unit): K6BubblesWidget {
        if (bubbles_press == null) {
            bubbles_press = getMBubbles().copy()//整个属性全部复制过来。
        }
        block(bubbles_press!!)
        invalidate()
        return this
    }

    //鼠标悬浮
    var bubbles_hover: KBubblesEntry? = null

    fun bubbles_hover(block: KBubblesEntry.() -> Unit): K6BubblesWidget {
        if (bubbles_hover == null) {
            bubbles_hover = getMBubbles().copy()//整个属性全部复制过来。
        }
        block(bubbles_hover!!)
        invalidate()
        return this
    }

    //聚焦
    var bubbles_focuse: KBubblesEntry? = null

    fun bubbles_focuse(block: KBubblesEntry.() -> Unit): K6BubblesWidget {
        if (bubbles_focuse == null) {
            bubbles_focuse = getMBubbles().copy()//整个属性全部复制过来。
        }
        block(bubbles_focuse!!)
        invalidate()
        return this
    }

    //选中
    var bubbles_selected: KBubblesEntry? = null

    fun bubbles_selected(block: KBubblesEntry.() -> Unit): K6BubblesWidget {
        if (bubbles_selected == null) {
            bubbles_selected = getMBubbles().copy()//整个属性全部复制过来。
        }
        block(bubbles_selected!!)
        invalidate()
        return this
    }

    //不可用
    var bubbles_notEnable: KBubblesEntry? = null

    fun bubbles_notEnable(block: KBubblesEntry.() -> Unit): K6BubblesWidget {
        if (bubbles_notEnable == null) {
            bubbles_notEnable = getMBubbles().copy()//整个属性全部复制过来。
        }
        block(bubbles_notEnable!!)
        invalidate()
        return this
    }

    //fixme 正常状态（先写正常样式，再写其他状态的样式，因为其他状态的样式初始值是复制正常状态的样式的。）
    var bubbles: KBubblesEntry? = null

    private fun getMBubbles(): KBubblesEntry {
        if (bubbles == null) {
            bubbles = KBubblesEntry()
        }
        return bubbles!!
    }

    fun bubbles(block: KBubblesEntry.() -> Unit): K6BubblesWidget {
        block(getMBubbles())
        invalidate()
        return this
    }

    //fixme 气泡样式，最后绘制。最后显示。即在切割圆角radius{}的上面。
    override fun draw2Last2(canvas: Canvas, paint: Paint) {
        super.draw2Last2(canvas, paint)
        drawBubbles(canvas, paint, this)
    }

    private var bubblesModel: KBubblesEntry? = null
    private fun drawBubbles(canvas: Canvas, paint: Paint, view: View) {
        view?.apply {
            if (bubbles != null) {
                bubblesModel = null
                if (isPressed && bubbles_press != null) {
                    //按下
                    bubblesModel = bubbles_press
                } else if (isHovered && bubbles_hover != null) {
                    //鼠标悬浮
                    bubblesModel = bubbles_hover
                } else if (isFocused && bubbles_focuse != null) {
                    //聚焦
                    bubblesModel = bubbles_focuse
                } else if (isSelected && bubbles_selected != null) {
                    //选中
                    bubblesModel = bubbles_selected
                }
                if (isEnabled == false && bubbles_notEnable != null) {
                    //不可用
                    bubblesModel = bubbles_notEnable
                }
                //正常
                if (bubblesModel == null) {
                    bubblesModel = bubbles
                }
                bubblesModel?.let {
                    if (it.isDraw) {
                        paint.setShader(null)
                        drawBubbles(canvas, paint, it)
                        paint.setShader(null)//防止其他地方受影响，所以渲染清空。
                    }
                }
            }
        }
    }

    private var mBubblesPath: Path? = null
    private var mBubblesPathStroke: Path? = null

    //fixme 画气泡三角;(气泡三角默认都是居中绘制的。)
    private fun drawBubbles(canvas: Canvas, paint: Paint, bubblesEnty: KBubblesEntry) {
        bubblesEnty?.apply {
            if (!isDraw) {
                return
            }
            if (mBubblesPath == null) {
                mBubblesPath = Path()
            }
            mBubblesPath?.reset()
            if (mBubblesPathStroke == null) {
                mBubblesPathStroke = Path()
            }
            mBubblesPathStroke?.reset()
            if (direction == KBubblesEntry.DIRECTION_LEFT) {
                //左居中气泡
                //左边顶点坐标
                var x1 = 0f + xOffset
                var y1 = height / 2 + yOffset + bubblesOffset
                //右边上方点坐标
                var x2 = 0f + xOffset + bubblesWidth
                var y2 = height / 2 + yOffset - bubblesHeight / 2
                //右边下方点坐标
                var x3 = 0f + xOffset + bubblesWidth
                var y3 = height / 2 + yOffset + bubblesHeight / 2
                mBubblesPath?.moveTo(x1, y1)
                mBubblesPath?.lineTo(x2, y2)
                if (all_radius != 0f) {
                    mBubblesPath?.lineTo(x2, y2)
                }
                mBubblesPath?.lineTo(x3, y3)
                if (all_radius != 0f) {
                    mBubblesPath?.lineTo(x3, y3)
                }
                mBubblesPath?.close()
                //边框
                if (strokeWidth > 0) {
                    var offset = strokeWidth
                    x2 = x2 - offset
                    x3 = x3 - offset
                    mBubblesPathStroke?.moveTo(x1, y1)
                    mBubblesPathStroke?.lineTo(x2, y2)
                    if (all_radius != 0f) {
                        mBubblesPathStroke?.lineTo(x2, y2)
                    }
                    mBubblesPathStroke?.moveTo(x3, y3)//fixme 画边框，最右边的边框就不画了。
                    if (all_radius != 0f) {
                        mBubblesPathStroke?.lineTo(x3, y3)
                    }
                    mBubblesPathStroke?.lineTo(x1, y1)
                }
            }
            //圆角
            var corner: CornerPathEffect? = null
            if (all_radius != 0f) {
                corner = CornerPathEffect(all_radius)
                paint.setPathEffect(corner)//圆角属性
            }
            //fixme 画三角
            paint.color = bg_color
            paint.style = Paint.Style.FILL
            paint.strokeWidth = 0f
            canvas?.drawPath(mBubblesPath, paint)
            //fixme 画边框
            if (strokeWidth > 0) {
                //虚线
                var dashPathEffect: DashPathEffect? = null
                if (dashWidth > 0 && dashGap > 0) {
                    dashPathEffect = DashPathEffect(floatArrayOf(dashWidth, dashGap), 0f)
                    paint.setPathEffect(dashPathEffect)
                }
                //组合动画（保留圆角和虚线的效果）
                if (corner != null && dashPathEffect != null) {
                    var composePathEffect = ComposePathEffect(dashPathEffect, corner)
                    paint.setPathEffect(composePathEffect)
                }
                paint.color = strokeColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth
                canvas?.drawPath(mBubblesPathStroke, paint)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bubbles = null
        bubbles_focuse = null
        bubbles_hover = null
        bubbles_press = null
        bubbles_selected = null
        bubbles_notEnable = null
        bubblesModel = null
        mBubblesPath?.reset()
        mBubblesPath = null
    }

}
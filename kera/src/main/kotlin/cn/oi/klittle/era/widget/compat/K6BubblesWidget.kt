package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.entity.widget.compat.KBubblesEntry

//                    fixme 使用案例，仿对话框样式。贝塞尔曲线就不添加了。仿QQ曲线连接效果太难画了。直接用图片比较方便。(QQ用的也是图片)
//                    fixme 左对话框 常态颜色（浅灰色）Color.parseColor("#F5F6FA")，按下颜色：Color.parseColor("#C4C5C8")
//                    fixme 右对话框 常态颜色（浅蓝色）Color.parseColor("#00CAFC")，按下颜色：Color.parseColor("#00A2CA")
//                    kview {
//                        text = "我的名字叫诺亚方舟。哦"
//                        gravity = Gravity.CENTER_VERTICAL
//                        var mDirection = KBubblesEntry.DIRECTION_RIGHT//气泡方向
//                        var mMargin = kpx.x(30)//radius外补丁
//                        var norColor = Color.parseColor("#00CAFC")//常态颜色
//                        var pressColor = Color.parseColor("#00A2CA")//按下颜色
//                        bubbles {
//                            direction = mDirection//fixme 气泡方向；如果是左方向，默认就是左垂直居中绘制。即：默认居中绘制。
//                            bubblesWidth = kpx.x(20)
//                            bubblesHeight = kpx.x(30)
//                            bg_color = norColor
//                            if (direction == KBubblesEntry.DIRECTION_LEFT || direction == KBubblesEntry.DIRECTION_RIGHT) {
//                                xOffset = kpx.x(11f)//气泡的偏移量（整体偏移）；正数向右偏移，负数向左偏移(fixme 即：正数向内偏移，负数向外偏移。)
//                            } else {
//                                yOffset = kpx.x(4f)
//                            }
//                            all_radius = 45f//气泡顶点的圆角角度（只对顶点有效）
//                            bubblesOffset = -kpx.x(20f)//气泡顶点的偏移量
//                        }
//                        bubbles_press {
//                            bg_color = pressColor
//                            all_radius = 0f
//                            bubblesOffset = 0f
//                            strokeColor = Color.RED
//                            strokeWidth = kpx.x(3f)
//                            isDrawRightStroke = false//fixme 是否绘制右边的边框（气泡顶点对应的底边）;默认不绘制。方便和radius组合对接。
//                            if (direction == KBubblesEntry.DIRECTION_LEFT || direction == KBubblesEntry.DIRECTION_RIGHT) {
//                                xOffset = kpx.x(14f)
//                            } else {
//                                yOffset = kpx.x(4f)
//                            }
//                            dashWidth=kpx.x(15f)
//                            dashGap=kpx.x(10f)
//                        }
//                        radius {
//                            all_radius(45)
//                            bg_color = norColor
//                            strokeWidth = 0f
//                            if (mDirection == KBubblesEntry.DIRECTION_LEFT) {
//                                leftMargin = mMargin
//                            } else if (mDirection == KBubblesEntry.DIRECTION_RIGHT) {
//                                rightMargin = mMargin
//                            } else if (mDirection == KBubblesEntry.DIRECTION_TOP) {
//                                topMargin = mMargin
//                            } else if (mDirection == KBubblesEntry.DIRECTION_BOTTOM) {
//                                bottomMargin = mMargin
//                            }
//                            setAutoPaddingForRadius(kpx.x(16), this)//fixme 根据radius的外补丁，自动设置文本内补丁。
//                        }
//                        radius_press {
//                            bg_color = pressColor
//                            strokeColor = Color.RED
//                            strokeWidth = kpx.x(3f)
//                            dashWidth=kpx.x(15f)
//                            dashGap=kpx.x(10f)
//                        }
//                    }.lparams {
//                        width = kpx.x(400)
//                        height = kpx.x(100)
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
            //顶点坐标
            var x1 = 0f
            var y1 = 0f
            //右边上方点坐标
            var x2 = 0f
            var y2 = 0f
            //右边下方点坐标
            var x3 = 0f
            var y3 = 0f
            if (direction == KBubblesEntry.DIRECTION_LEFT) {
                //fixme 左居中气泡
                //左边顶点坐标
                x1 = 0f + xOffset
                y1 = height / 2 + yOffset + bubblesOffset
                //右边上方点坐标
                x2 = 0f + xOffset + bubblesWidth
                y2 = height / 2 + yOffset - bubblesHeight / 2
                //右边下方点坐标
                x3 = 0f + xOffset + bubblesWidth
                y3 = height / 2 + yOffset + bubblesHeight / 2
            } else if (direction == KBubblesEntry.DIRECTION_RIGHT) {
                //fixme 右居中气泡
                //顶点
                x1 = width - xOffset
                y1 = height / 2 + yOffset + bubblesOffset
                //点2
                x2 = width - xOffset - bubblesWidth
                y2 = height / 2 + yOffset - bubblesHeight / 2
                //点3
                x3 = width - xOffset - bubblesWidth
                y3 = height / 2 + yOffset + bubblesHeight / 2
            } else if (direction == KBubblesEntry.DIRECTION_TOP) {
                //fixme 上居中气泡
                //顶点
                x1 = width / 2 + xOffset + bubblesOffset
                y1 = 0 + yOffset
                //点2
                x2 = width / 2 + xOffset - bubblesWidth / 2
                y2 = y1 + bubblesHeight
                //点3
                x3 = width / 2 + xOffset + bubblesWidth / 2
                y3 = y2
            } else if (direction == KBubblesEntry.DIRECTION_BOTTOM) {
                //fixme 下居中气泡
                //顶点
                x1 = width / 2 + xOffset + bubblesOffset
                y1 = height - yOffset
                //点2
                x2 = width / 2 + xOffset - bubblesWidth / 2
                y2 = y1 - bubblesHeight
                //点3
                x3 = width / 2 + xOffset + bubblesWidth / 2
                y3 = y2
            }
            //点坐标连线
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
            //边框点坐标连线
            if (strokeWidth > 0) {
                var strokeOffset = strokeWidth
                if (!isDrawRightStroke) {
                    //右边边框不绘制时，右边留点间隙，方便与radius组合对接。
                    if (direction == KBubblesEntry.DIRECTION_LEFT) {
                        x2 = x2 - strokeOffset
                        x3 = x3 - strokeOffset
                    } else if (direction == KBubblesEntry.DIRECTION_RIGHT) {
                        x2 = x2 + strokeOffset
                        x3 = x3 + strokeOffset
                    } else if (direction == KBubblesEntry.DIRECTION_TOP) {
                        y2 = y2 - strokeOffset
                        y3 = y3 - strokeOffset
                    } else if (direction == KBubblesEntry.DIRECTION_BOTTOM) {
                        y2 = y2 + strokeOffset
                        y3 = y3 + strokeOffset
                    }
                }
                mBubblesPathStroke?.moveTo(x1, y1)
                mBubblesPathStroke?.lineTo(x2, y2)
                if (all_radius != 0f) {
                    mBubblesPathStroke?.lineTo(x2, y2)
                }
                if (isDrawRightStroke) {
                    //绘制右边边框
                    mBubblesPathStroke?.lineTo(x3, y3)
                } else {
                    mBubblesPathStroke?.moveTo(x3, y3)//fixme 画边框，最右边的边框就不画了。
                }
                if (all_radius != 0f) {
                    mBubblesPathStroke?.lineTo(x3, y3)
                }
                mBubblesPathStroke?.lineTo(x1, y1)
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
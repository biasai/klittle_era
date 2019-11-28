package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.entity.widget.compat.KPathEntity

/**
 * fixme 路径控件（异形控件）
 */
open class KPathView : KView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//开启硬件加速,不然圆角没有效果
    }

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

    var linePathPhase: Float = 0F//横线偏移量
    override fun draw2(canvas: Canvas, paint: Paint) {
        super.draw2(canvas, paint)
        if (path != null) {
            var model: KPathEntity? = null
            if (!isEnabled && path_enable != null) {
                //不可用
                model = path_enable
            } else if (isPressed && path_press != null) {
                //按下
                model = path_press
            } else if (isHovered && path_hover != null) {
                //鼠标悬浮
                model = path_hover
            } else if (isFocused && path_focuse != null) {
                //聚焦
                model = path_focuse
            } else if (isSelected && path_selected != null) {
                //选中
                model = path_selected
            }
            //正常
            if (model == null) {
                model = path
            }
            model?.apply {
                points?.let {
                    if (it.size > 0) {
                        var path = Path()
                        for (i in 0..it.lastIndex) {
                            var x = it[i].x
                            var y = it[i].y
                            if (baseWidth > 0 && baseHeight > 0) {
                                //fixme 基准宽高大于0，按比例适配缩放
                                x = width.toFloat() / baseWidth.toFloat() * x
                                y = height.toFloat() / baseHeight.toFloat() * y
                            }
                            if (i == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }
                        path.close()//fixme 最后一定要闭合(如果有圆角radius的话，会有起点也会有圆角效果)
                        //画笔样式
                        if (strokeWidth > 0) {
                            //线条中心，默认底部居中
                            if (centerX < 0 && width > 0) {
                                centerX = width / 2.toFloat()
                            }
                            if (centerY < 0 && height > 0) {
                                centerY = height - strokeWidth
                            }
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
                            //虚线
                            if (dashWidth > 0 && dashGap > 0) {
                                var dashPathEffect = DashPathEffect(floatArrayOf(dashWidth, dashGap), linePathPhase)
                                if (cornerPathEffect != null) {
                                    paint.setPathEffect(ComposePathEffect(cornerPathEffect, dashPathEffect))
                                } else {
                                    paint.setPathEffect(dashPathEffect)
                                }
                            } else if (cornerPathEffect != null) {
                                paint.setPathEffect(cornerPathEffect)
                            }
                        }
//                        if (isDST_IN) {
//                            paint.style = Paint.Style.FILL_AND_STROKE
//                            paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))//fixme 取下面的交集
//                        } else {
//                            paint.setXfermode(null)
//                        }

                        /**
                         * fixme 8.0;和9.0之后；圆角的矩形范围只包含圆角矩形内的范围；不再是整个矩形的范围。
                         * fixme 以下修复了圆角无效的问题。
                         * fixme 现在开不开硬件加速；都无所谓了。都支持圆角了。
                         */
//                        if (isDST_IN) {
//                            paint.style = Paint.Style.FILL_AND_STROKE
//                            paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
////                            path.setFillType(Path.FillType.INVERSE_WINDING)//反转
//                            canvas.drawPath(path, paint)
////                            path.setFillType(Path.FillType.WINDING)//恢复正常
////                            paint.setXfermode(null)
//                        }

                        canvas.drawPath(path, paint)
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

    override fun onDestroy() {
        super.onDestroy()
        path = null
        path_enable = null
        path_press = null
        path_focuse = null
        path_selected = null
        path_hover = null
    }

}
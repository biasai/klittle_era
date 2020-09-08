package cn.oi.klittle.era.entity.widget.compat

import android.graphics.Color
import cn.oi.klittle.era.comm.kpx

/**
 * fixme 菱形；各个边框的大小必须一致的，不能独自设置；各个边框的颜色可以独自设置。
 * @param strokeWidth 边框宽度(所有);fixme 菱形的所有边框都必须一致；要想独自设置各个边框的大小，就到border{}属性里去设置。
 * @param strokeColor 边框颜色(所有)
 * @param dashWidth 虚线长度(所有)
 * @param dashGap 虚线之间的间隙(所有)
 * @param strokeHorizontalColors 边框水平渐变颜色数组值【均匀渐变】，(所有)
 * @param strokeVerticalColors 边框垂直渐变颜色数组值【均匀】,会覆盖水平渐变。优先级比水平高(所有)
 * @param isDrawLeft_top 是否绘制左上角边边框,默认都是true
 * @param isDrawRight_top 是否绘制右上角的边框
 * @param isDrawRight_bottom 是否绘制右下角的边框
 * @param isDrawLeft_Bottom 是否绘制左下角边框
 *
 * @param leftTopStrokeHorizontalColors 左上角边框
 *
 * @param leftTopDashGap 左上角边框 虚线之间的间隙
 * @param rightTopDashGap 右上边框 虚线之间的间隙
 * @param rightBottomDashGap 右下角边框 虚线之间的间隙
 * @param leftBottomDashGap 左下角边框 虚线之间的间隙
 *
 * @param all_radius 所有圆角的角度(0~90度)
 * @param top_radius 顶部圆角角度,fixme 优先级比all_radius高。各个圆角都可以独自设置
 * @param right_radius 右边圆角角度
 * @param bottom_radius 低部圆角
 * @param left_radius 左边圆角
 *
 * @param bg_color 矩形画布背景颜色，不能为透明，不然什么也看不见（包括阴影），也就是说画布必须有一个背景色
 * @param bgHorizontalColors 背景颜色水平渐变
 * @param bgVerticalColors 背景颜色垂直渐变（优先级比水平高）
 * @param isBgGradient 背景色是否渐变,默认是
 */
data class KDiamondEntity(var strokeWidth: Float = kpx.x(2f), var strokeColor: Int = Color.LTGRAY,
                          var dashWidth: Float = 0F, var dashGap: Float = 0F,
                          var strokeHorizontalColors: IntArray? = null, var strokeVerticalColors: IntArray? = null,
                          var isDrawLeft_top: Boolean = true, var isDrawRight_top: Boolean = true,
                          var isDrawRight_bottom: Boolean = true, var isDrawLeft_Bottom: Boolean = true,
                          var leftTopStrokeColor: Int? = null,
                          var leftTopDashWidth: Float? = null, var leftTopDashGap: Float? = null,
                          var leftTopStrokeHorizontalColors: IntArray? = null, var leftTopStrokeVerticalColors: IntArray? = null,
                          var rightTopStrokeColor: Int? = null,
                          var rightTopDashWidth: Float? = null, var rightTopDashGap: Float? = null,
                          var rightTopStrokeHorizontalColors: IntArray? = null, var rightTopStrokeVerticalColors: IntArray? = null,
                          var rightBottomStrokeColor: Int? = null,
                          var rightBottomDashWidth: Float? = null, var rightBottomDashGap: Float? = null,
                          var rightBottomStrokeHorizontalColors: IntArray? = null, var rightBottomStrokeVerticalColors: IntArray? = null,
                          var leftBottomStrokeColor: Int? = null,
                          var leftBottomDashWidth: Float? = null, var leftBottomDashGap: Float? = null,
                          var leftBottomStrokeHorizontalColors: IntArray? = null, var leftBottomStrokeVerticalColors: IntArray? = null,
                          var all_radius: Float = -1F,
                          var top_radius: Float = -1F,
                          var right_radius: Float = -1F,
                          var bottom_radius: Float = -1F,
                          var left_radius: Float = -1F,
                          var bg_color: Int = Color.TRANSPARENT,
                          var bgHorizontalColors: IntArray? = null, var bgVerticalColors: IntArray? = null,
                          var isBgGradient: Boolean = true) {

    //fixme 所有

    open fun strokeHorizontalColors(vararg color: Int) {
        strokeVerticalColors = null
        strokeHorizontalColors = color
    }

    open fun strokeHorizontalColors(vararg color: String) {
        strokeVerticalColors = null
        strokeHorizontalColors = IntArray(color.size)
        strokeHorizontalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }


    open fun strokeVerticalColors(vararg color: Int) {
        strokeHorizontalColors = null
        strokeVerticalColors = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun strokeVerticalColors(vararg color: String) {
        strokeHorizontalColors = null
        strokeVerticalColors = IntArray(color.size)
        strokeVerticalColors?.apply {
            if (color.size > 0) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            }
        }
    }

    //fixme 左上角

    open fun leftTopStrokeHorizontalColors(vararg color: Int) {
        leftTopStrokeVerticalColors = null
        leftTopStrokeHorizontalColors = color
    }

    open fun leftTopStrokeHorizontalColors(vararg color: String) {
        leftTopStrokeVerticalColors = null
        leftTopStrokeHorizontalColors = IntArray(color.size)
        leftTopStrokeHorizontalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }


    open fun leftTopStrokeVerticalColors(vararg color: Int) {
        leftTopStrokeHorizontalColors = null
        leftTopStrokeVerticalColors = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun leftTopStrokeVerticalColors(vararg color: String) {
        leftTopStrokeHorizontalColors = null
        leftTopStrokeVerticalColors = IntArray(color.size)
        leftTopStrokeVerticalColors?.apply {
            if (color.size > 0) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            }
        }
    }

    //fixme 右上角

    open fun rightTopStrokeHorizontalColors(vararg color: Int) {
        rightTopStrokeVerticalColors = null
        rightTopStrokeHorizontalColors = color
    }

    open fun rightTopStrokeHorizontalColors(vararg color: String) {
        rightTopStrokeVerticalColors = null
        rightTopStrokeHorizontalColors = IntArray(color.size)
        rightTopStrokeHorizontalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }


    open fun rightTopStrokeVerticalColors(vararg color: Int) {
        rightTopStrokeHorizontalColors = null
        rightTopStrokeVerticalColors = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun rightTopStrokeVerticalColors(vararg color: String) {
        rightTopStrokeHorizontalColors = null
        rightTopStrokeVerticalColors = IntArray(color.size)
        rightTopStrokeVerticalColors?.apply {
            if (color.size > 0) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            }
        }
    }

    //fixme 右下角

    open fun rightBottomStrokeHorizontalColors(vararg color: Int) {
        rightBottomStrokeVerticalColors = null
        rightBottomStrokeHorizontalColors = color
    }

    open fun rightBottomStrokeHorizontalColors(vararg color: String) {
        rightBottomStrokeVerticalColors = null
        rightBottomStrokeHorizontalColors = IntArray(color.size)
        rightBottomStrokeHorizontalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }


    open fun rightBottomStrokeVerticalColors(vararg color: Int) {
        rightBottomStrokeHorizontalColors = null
        rightBottomStrokeVerticalColors = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun rightBottomStrokeVerticalColors(vararg color: String) {
        rightBottomStrokeHorizontalColors = null
        rightBottomStrokeVerticalColors = IntArray(color.size)
        rightBottomStrokeVerticalColors?.apply {
            if (color.size > 0) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            }
        }
    }

    //fixme 左下角

    open fun leftBottomStrokeHorizontalColors(vararg color: Int) {
        leftBottomStrokeVerticalColors = null
        leftBottomStrokeHorizontalColors = color
    }

    open fun leftBottomStrokeHorizontalColors(vararg color: String) {
        leftBottomStrokeVerticalColors = null
        leftBottomStrokeHorizontalColors = IntArray(color.size)
        leftBottomStrokeHorizontalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }


    open fun leftBottomStrokeVerticalColors(vararg color: Int) {
        leftBottomStrokeHorizontalColors = null
        leftBottomStrokeVerticalColors = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun leftBottomStrokeVerticalColors(vararg color: String) {
        leftBottomStrokeHorizontalColors = null
        leftBottomStrokeVerticalColors = IntArray(color.size)
        leftBottomStrokeVerticalColors?.apply {
            if (color.size > 0) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            }
        }
    }

    open fun bgHorizontalColors(vararg color: Int) {
        bgVerticalColors = null//fixme 垂直渐变和水平渐变不能同时存在，只能存在一个
        bgHorizontalColors = color
    }

    open fun bgHorizontalColors(vararg color: String) {
        bgVerticalColors = null
        bgHorizontalColors = IntArray(color.size)
        bgHorizontalColors?.apply {
            if (color.size > 0) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            }
        }

    }

    open fun bgVerticalColors(vararg color: Int) {
        bgHorizontalColors = null
        bgVerticalColors = color
    }

    open fun bgVerticalColors(vararg color: String) {
        bgHorizontalColors = null
        bgVerticalColors = IntArray(color.size)
        bgVerticalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }
    }

}
package cn.oi.klittle.era.entity.widget.compat

import android.graphics.Color
import cn.oi.klittle.era.comm.kpx

/**
 * 四个边框（在控件的边缘绘制）；主要是为了实现表格效果。
 * @param strokeWidth 边框宽度(所有)
 * @param strokeColor 边框颜色(所有)
 * @param dashWidth 虚线长度(所有)
 * @param dashGap 虚线之间的间隙(所有)
 * @param strokeHorizontalColors 边框水平渐变颜色数组值【均匀渐变】，(所有)
 * @param strokeVerticalColors 边框垂直渐变颜色数组值【均匀】,会覆盖水平渐变。优先级比水平高(所有)
 * @param isDrawLeft 是否绘制左边边框,默认都是true
 * @param isDrawTop 是否绘制上面的边框
 * @param isDrawRight 是否绘制右边的边框
 * @param isDrawBottom 是否绘制底部边框
 *
 * @param leftDashGap 左边框 虚线之间的间隙
 * @param topDashGap 上边框 虚线之间的间隙
 * @param rightDashGap 右边框 虚线之间的间隙
 * @param bottomDashGap 底部边框 虚线之间的间隙
 *
 * @param all_radius 所有圆角的角度(0~90度)
 * @param left_top 左上角圆角,fixme 优先级比all_radius高。
 * @param left_bottom 左下角圆角
 * @param right_top 右上角圆角
 * @param right_bottom 右下角圆角
 *
 * @param bg_color 矩形画布背景颜色，不能为透明，不然什么也看不见（包括阴影），也就是说画布必须有一个背景色
 * @param bgHorizontalColors 背景颜色水平渐变
 * @param bgVerticalColors 背景颜色垂直渐变（优先级比水平高）
 * @param isBgGradient 背景色是否渐变,默认是
 */
data class KBorderEntity(var strokeWidth: Float = kpx.x(2f), var strokeColor: Int = Color.LTGRAY,
                         var dashWidth: Float = 0F, var dashGap: Float = 0F,
                         var strokeHorizontalColors: IntArray? = null, var strokeVerticalColors: IntArray? = null,
                         var isDrawLeft: Boolean = true, var isDrawTop: Boolean = true,
                         var isDrawRight: Boolean = true, var isDrawBottom: Boolean = true,
                         var leftStrokeWidth: Float? = null, var leftStrokeColor: Int? = null,
                         var leftDashWidth: Float? = null, var leftDashGap: Float? = null,
                         var leftStrokeHorizontalColors: IntArray? = null, var leftStrokeVerticalColors: IntArray? = null,
                         var topStrokeWidth: Float? = null, var topStrokeColor: Int? = null,
                         var topDashWidth: Float? = null, var topDashGap: Float? = null,
                         var topStrokeHorizontalColors: IntArray? = null, var topStrokeVerticalColors: IntArray? = null,
                         var rightStrokeWidth: Float? = null, var rightStrokeColor: Int? = null,
                         var rightDashWidth: Float? = null, var rightDashGap: Float? = null,
                         var rightStrokeHorizontalColors: IntArray? = null, var rightStrokeVerticalColors: IntArray? = null,
                         var bottomStrokeWidth: Float? = null, var bottomStrokeColor: Int? = null,
                         var bottomDashWidth: Float? = null, var bottomDashGap: Float? = null,
                         var bottomStrokeHorizontalColors: IntArray? = null, var bottomStrokeVerticalColors: IntArray? = null,
                         var all_radius: Float = -1F,
                         var left_top: Float = -1F,
                         var left_bottom: Float = -1F,
                         var right_top: Float = -1F,
                         var right_bottom: Float = -1F,
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

    //fixme 左

    open fun leftStrokeHorizontalColors(vararg color: Int) {
        leftStrokeVerticalColors = null
        leftStrokeHorizontalColors = color
    }

    open fun leftStrokeHorizontalColors(vararg color: String) {
        leftStrokeVerticalColors = null
        leftStrokeHorizontalColors = IntArray(color.size)
        leftStrokeHorizontalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }


    open fun leftStrokeVerticalColors(vararg color: Int) {
        leftStrokeHorizontalColors = null
        leftStrokeVerticalColors = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun leftStrokeVerticalColors(vararg color: String) {
        leftStrokeHorizontalColors = null
        leftStrokeVerticalColors = IntArray(color.size)
        leftStrokeVerticalColors?.apply {
            if (color.size > 0) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            }
        }
    }

    //fixme 上

    open fun topStrokeHorizontalColors(vararg color: Int) {
        topStrokeVerticalColors = null
        topStrokeHorizontalColors = color
    }

    open fun topStrokeHorizontalColors(vararg color: String) {
        topStrokeVerticalColors = null
        topStrokeHorizontalColors = IntArray(color.size)
        topStrokeHorizontalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }


    open fun topStrokeVerticalColors(vararg color: Int) {
        topStrokeHorizontalColors = null
        topStrokeVerticalColors = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun topStrokeVerticalColors(vararg color: String) {
        topStrokeHorizontalColors = null
        topStrokeVerticalColors = IntArray(color.size)
        topStrokeVerticalColors?.apply {
            if (color.size > 0) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            }
        }
    }

    //fixme 右

    open fun rightStrokeHorizontalColors(vararg color: Int) {
        rightStrokeVerticalColors = null
        rightStrokeHorizontalColors = color
    }

    open fun rightStrokeHorizontalColors(vararg color: String) {
        rightStrokeVerticalColors = null
        rightStrokeHorizontalColors = IntArray(color.size)
        rightStrokeHorizontalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }


    open fun rightStrokeVerticalColors(vararg color: Int) {
        rightStrokeHorizontalColors = null
        rightStrokeVerticalColors = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun rightStrokeVerticalColors(vararg color: String) {
        rightStrokeHorizontalColors = null
        rightStrokeVerticalColors = IntArray(color.size)
        rightStrokeVerticalColors?.apply {
            if (color.size > 0) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            }
        }
    }

    //fixme 下

    open fun bottomStrokeHorizontalColors(vararg color: Int) {
        bottomStrokeVerticalColors = null
        bottomStrokeHorizontalColors = color
    }

    open fun bottomStrokeHorizontalColors(vararg color: String) {
        bottomStrokeVerticalColors = null
        bottomStrokeHorizontalColors = IntArray(color.size)
        bottomStrokeHorizontalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }


    open fun bottomStrokeVerticalColors(vararg color: Int) {
        bottomStrokeHorizontalColors = null
        bottomStrokeVerticalColors = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun bottomStrokeVerticalColors(vararg color: String) {
        bottomStrokeHorizontalColors = null
        bottomStrokeVerticalColors = IntArray(color.size)
        bottomStrokeVerticalColors?.apply {
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
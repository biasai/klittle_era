package cn.oi.klittle.era.entity.widget.compat

import android.graphics.Color
import android.graphics.Typeface
import cn.oi.klittle.era.comm.kpx

/**
 * 五角星
 * @param bg_color 背景颜色
 * @param bgHorizontalColors 背景颜色水平渐变
 * @param bgVerticalColors 背景颜色垂直渐变（优先级比水平高）
 * @param isBgGradient 背景色是否渐变,默认是
 * @param strokeWidth 边框宽度
 * @param strokeColor 边框颜色
 * @param strokeHorizontalColors 边框水平渐变颜色数组值【均匀渐变】，[测试发现，渐变色对阴影也有效果]
 * @param strokeVerticalColors 边框垂直渐变颜色数组值【均匀】,会覆盖水平渐变。
 * @param isStrokeGradient 边框色是否渐变,默认是
 * @param all_radius 圆角（0~90度）
 * @param rotation 旋转角度。（以控件中心进行旋转）
 * @param dashWidth 虚线长度
 * @param dashGap 虚线之间的间隙
 * @param isdashFlow 虚线是否流动。
 * @param dashSpeed 虚线的流动速度,绝对值越大速度越快。正数先左流动(正数逆时针方向)。负数向右流动。即绝对值控制速度。正负控制方向。
 * @param isPorterDuffXfermode 是否切割。
 * @param isDraw 是否绘制
 */
data class KStartEntity(var bg_color: Int = Color.BLUE, var bgHorizontalColors: IntArray? = null, var bgVerticalColors: IntArray? = null, var isBgGradient: Boolean = true,
                        var strokeWidth: Float = 0F, var strokeColor: Int = Color.WHITE, var strokeHorizontalColors: IntArray? = null, var strokeVerticalColors: IntArray? = null, var isStrokeGradient: Boolean = true,
                        var all_radius: Float = 0F, var rotation:Float=0f,
                        var dashWidth: Float = 0F, var dashGap: Float = 0F,
                        var isdashFlow: Boolean = false, var dashSpeed: Float = kpx.x(1f),
                        var isPorterDuffXfermode:Boolean=false,
                        var isDraw: Boolean = true) {


    open fun bgHorizontalColors(vararg color: Int) {
        bgHorizontalColors = color
    }

    open fun bgHorizontalColors(vararg color: String) {
        bgHorizontalColors = IntArray(color.size)
        bgHorizontalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }

    open fun bgVerticalColors(vararg color: Int) {
        bgVerticalColors = color
    }

    open fun bgVerticalColors(vararg color: String) {
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

    open fun strokeHorizontalColors(vararg color: Int) {
        strokeHorizontalColors = color
    }

    open fun strokeHorizontalColors(vararg color: String) {
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
        strokeVerticalColors = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun strokeVerticalColors(vararg color: String) {
        strokeVerticalColors = IntArray(color.size)
        strokeVerticalColors?.apply {
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
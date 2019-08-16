package cn.oi.klittle.era.entity.widget.compat

import android.graphics.Color
import cn.oi.klittle.era.comm.kpx

/**
 * 文本输入框，底部的横线。
 * @param strokeWidth 线的宽度
 * @param strokeColor 线的颜色
 * @param dashWidth 虚线的宽度
 * @param dashGap 虚线的间隙
 * @param strokeHorizontalColors 水平渐变色
 * @param strokeVerticalColors 垂直渐变色
 * @param isStrokeGradient 线条是颜色是否渐变，默认是
 * @param strokeLength 线条的长度。
 * @param centerX 线条的中心坐标X
 * @param centerY 线条的中心坐标Y
 * @param isdashFlow 虚线是否流动。
 * @param dashSpeed 虚线的流动速度,绝对值越大速度越快。正数先左流动(正数逆时针方向)。负数向右流动。即绝对值控制速度。正负控制方向。
 */
data class KEditLineEntity(var strokeWidth: Float = kpx.x(2f), var strokeColor: Int = Color.parseColor("#ededed"), var dashWidth: Float = 0F, var dashGap: Float = 0F,
                           var strokeHorizontalColors: IntArray? = null, var strokeVerticalColors: IntArray? = null,
                           var isStrokeGradient: Boolean = true,
                           var strokeLength: Float = 0f, var centerX: Float = -1F, var centerY: Float = -1F,
                           var isdashFlow: Boolean = false, var dashSpeed: Float = kpx.x(1f)) {

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
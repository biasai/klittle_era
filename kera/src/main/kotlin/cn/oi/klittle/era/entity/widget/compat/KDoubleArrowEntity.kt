package cn.oi.klittle.era.entity.widget.compat

import android.graphics.Color
import cn.oi.klittle.era.comm.kpx

/**
 * @param strokeWidth 边框宽度(所有)
 * @param strokeColor 边框颜色(所有)
 * @param dashWidth 虚线长度(所有)
 * @param dashGap 虚线之间的间隙(所有)
 * @param strokeHorizontalColors 边框水平渐变颜色数组值【均匀渐变】，(所有)
 * @param strokeVerticalColors 边框垂直渐变颜色数组值【均匀】,会覆盖水平渐变。优先级比水平高(所有)
 */
data class KDoubleArrowEntity(var strokeWidth: Float = kpx.x(2f), var strokeColor: Int = Color.LTGRAY,
                              var dashWidth: Float = 0F, var dashGap: Float = 0F,
                              var strokeHorizontalColors: IntArray? = null, var strokeVerticalColors: IntArray? = null) {
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
}
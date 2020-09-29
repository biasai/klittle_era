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
 * @param isGradient 颜色色是否渐变,默认是
 * @param isROUND 线条是否为圆角线帽
 * @param isHorizontal 是否为水平线；true水平线，false垂直线。
 * @param arrowLength 箭头的长度。
 * @param isArrowSolid fixme 箭头是否为实心。实心就画三角形箭头。
 * @param isDrawMain fixme 是否绘制中间的主干线（连接两个箭头之间的线条）
 * @param isDrawLeftArrow 是否绘制左边（水平）的箭头，垂直就是上面。
 * @param isDrawRightArrow 是否绘制右边（水平）箭头，垂直就是下边。
 * @param isArrowDash fixme 箭头是否也为虚线。默认为false,不为虚线。
 * @param isLeftTurnDownOrLeft 左边（水平）或上面（垂直）的箭头，是否向下面(水平)或左边（垂直）转弯；fixme isLeftTurnDownOrLeft （向左转）优先级比向右转 isLeftTurnUpOrRight高。
 * @param isLeftTurnUpOrRight 左边（水平）或上面（垂直）的箭头，是否向上面(水平)或右边（垂直）转弯
 * @param isRightTurnDownOrLeft 右边（水平）或下面（垂直）的箭头，是否向下面(水平)或左边（垂直）转弯
 * @param isRightTurnUpOrRight 右边（水平）或下面（垂直）的箭头，是否向下面(水平)或左边（垂直）转弯
 */
data class KDoubleArrowEntity(var strokeWidth: Float = kpx.x(2f), var strokeColor: Int = Color.LTGRAY,
                              var dashWidth: Float = 0F, var dashGap: Float = 0F,
                              var strokeHorizontalColors: IntArray? = null, var strokeVerticalColors: IntArray? = null,
                              var isGradient: Boolean = true, var isROUND: Boolean = true, var isHorizontal: Boolean = true,
                              var arrowLength: Float = kpx.x(20f),
                              var isArrowSolid:Boolean=false,
                              var isDrawMain: Boolean = true,
                              var isDrawLeftArrow: Boolean = true, var isDrawRightArrow: Boolean = true,
                              var isArrowDash: Boolean = false,
                              var isLeftTurnDownOrLeft: Boolean = false, var isLeftTurnUpOrRight: Boolean = false,
                              var isRightTurnDownOrLeft: Boolean = false, var isRightTurnUpOrRight: Boolean = false) {
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
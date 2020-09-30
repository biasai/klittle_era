package cn.oi.klittle.era.entity.widget

import android.graphics.Color
import cn.oi.klittle.era.comm.kpx
/**
 * 气泡实体类
 * @param direction 气泡的方向
 * @param xOffset 气泡的偏移量；气泡三角，x轴偏移量，正数向右偏移，负数向左偏移
 * @param yOffset 气泡三角，y轴偏移量；正数向下偏移，负数向上偏移。
 * @param airWidth 气泡的宽度
 * @param airHeight 气泡的高度
 * @param all_radius 圆角（所有的圆角；包括气泡）
 * @param isAirRadius 气泡三角是否也具有圆角效果;true圆角，false尖角。(all_radius大于0才有效);fixme 默认是true,具备圆角效果。
 * @param isAirBorderRadius 气泡三角的两个边的连接处是否具有圆角效果。
 * @param strokeWidth 边框的宽度
 * @param strokeColor 边框的颜色
 * @param strokeHorizontalColors 边框水平渐变颜色数组值【均匀渐变】，[测试发现，渐变色对阴影也有效果]
 * @param strokeVerticalColors 边框垂直渐变颜色数组值【均匀】,会覆盖水平渐变。
 * @param isStrokeGradient 边框色是否渐变,默认是
 * @param bg_color 背景色
 * @param bgHorizontalColors 背景颜色水平渐变
 * @param bgVerticalColors 背景颜色垂直渐变（优先级比水平高）
 * @param isBgGradient 背景色是否渐变,默认是
 * @param dashWidth 虚线长度（fixme 这里圆角和虚线一起使用；效果好像不是很好。）
 * @param dashGap 虚线之间的间隙
 * @param isDraw 是否绘制
 * Created by 彭治铭 on 2019/4/18.
 */
data class KAirEntry(var direction: Int = KAirEntry.DIRECTION_BOTTOM, var xOffset: Float = 0F, var yOffset: Float = 0F,
                     var airWidth: Int = kpx.x(25), var airHeight: Int = airWidth,
                     var all_radius: Float = 0F, var isAirRadius: Boolean = true,var isAirBorderRadius: Boolean = true,
                     var strokeWidth: Float = 0F, var strokeColor: Int = Color.BLACK,
                     var strokeHorizontalColors: IntArray? = null, var strokeVerticalColors: IntArray? = null, var isStrokeGradient: Boolean = true,
                     var bg_color: Int = Color.WHITE,
                     var bgHorizontalColors: IntArray? = null, var bgVerticalColors: IntArray? = null, var isBgGradient: Boolean = true,
                     var dashWidth: Float = 0F, var dashGap: Float = 0F,
                     var isDraw: Boolean = true) {

    companion object {
        //fixme 气泡箭头的方向：0左边（居中）；1上面（居中）；2右边（居中）；3下面（居中）
        val DIRECTION_LEFT = 0
        val DIRECTION_TOP = 1
        val DIRECTION_RIGHT = 2
        val DIRECTION_BOTTOM = 3
    }

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
package cn.oi.klittle.era.entity.widget.chart

import android.graphics.Color
import cn.oi.klittle.era.comm.kpx

/**
 * x轴，y轴实体类
 * @param x 起点x坐标
 * @param y 起点y坐标
 * @param width 宽带（长度）
 * @param height 高度
 * @param left_top 左上角圆角角度
 * @param left_bottom 左下角圆角角度
 * @param right_top 右上角
 * @param right_bottom 右下角
 * @param color 颜色
 * @param dashWidth 虚线长度
 * @param dashGap 虚线之间的间隙
 * @param horizontalColors 边框水平渐变颜色数组值【均匀渐变】，[测试发现，渐变色对阴影也有效果]
 * @param verticalColors 边框垂直渐变颜色数组值【均匀】,会覆盖水平渐变。
 * @param isGradient 颜色色是否渐变,默认是
 * @param isdashFlow 虚线是否流动。
 * @param dashSpeed 虚线的流动速度,绝对值越大速度越快。正数先左流动(正数逆时针方向)。负数向右流动。即绝对值控制速度。正负控制方向。
 * @param isDraw 是否绘制
 */
data class KAxisEntity(var x: Int = 0, var y: Int = 0, var width: Int = 0, var height: Int = 0,
                       var left_top: Float = 0f, var left_bottom: Float = 0f, var right_top: Float = 0f, var right_bottom: Float = 0f,
                       var dashWidth: Float = 0F, var dashGap: Float = 0F,
                       var color: Int = Color.WHITE,
                       var horizontalColors: IntArray? = null, var verticalColors: IntArray? = null,
                       var isGradient: Boolean = true,
                       var isdashFlow: Boolean = false, var dashSpeed: Float = kpx.x(2f),
                       var isDraw: Boolean = true) {

    //fixme 直接设置所有圆角，all_radius属性去除（鸡肋不需要）。
    fun all_radius(all_radius: Float) {
        //this.all_radius = all_radius
        left_top = all_radius
        left_bottom = all_radius
        right_top = all_radius
        right_bottom = all_radius
    }

    open fun horizontalColors(vararg color: Int) {
        horizontalColors = null
        horizontalColors = color
    }

    open fun horizontalColors(vararg color: String) {
        horizontalColors = null
        horizontalColors = IntArray(color.size)
        horizontalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }


    open fun verticalColors(vararg color: Int) {
        verticalColors = null
        verticalColors = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun verticalColors(vararg color: String) {
        verticalColors = null
        verticalColors = IntArray(color.size)
        verticalColors?.apply {
            if (color.size > 0) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            }
        }
    }

}
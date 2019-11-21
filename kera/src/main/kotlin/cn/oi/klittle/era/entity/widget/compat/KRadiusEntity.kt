package cn.oi.klittle.era.entity.widget.compat

import android.graphics.Color
import cn.oi.klittle.era.comm.kpx

/**
 * 圆角边框实体类
 * @param x 起点坐标
 * @param y 起点坐标
 * @param width 宽带（长度）;居中对齐
 * @param height 进度条(src)或低层(dst)高度;0表示控件本身的高度;居中对齐
 * @param left_top 左上角圆角角度
 * @param left_bottom 左下角圆角角度
 * @param right_top 右上角
 * @param right_bottom 右下角
 * @param strokeWidth 边框宽度
 * @param strokeColor 边框颜色
 * @param dashWidth 虚线长度
 * @param dashGap 虚线之间的间隙
 * @param bg_color 矩形画布背景颜色，不能为透明，不然什么也看不见（包括阴影），也就是说画布必须有一个背景色
 * @param strokeHorizontalColors 边框水平渐变颜色数组值【均匀渐变】，[测试发现，渐变色对阴影也有效果]
 * @param strokeVerticalColors 边框垂直渐变颜色数组值【均匀】,会覆盖水平渐变。
 * @param bgHorizontalColors 背景颜色水平渐变
 * @param bgVerticalColors 背景颜色垂直渐变（优先级比水平高）
 * @param isBgGradient 背景色是否渐变,默认是
 * @param isStrokeGradient 边框色是否渐变,默认是
 * @param isdashFlow 虚线是否流动。
 * @param dashSpeed 虚线的流动速度,绝对值越大速度越快。正数向左流动(正数逆时针方向)。负数向右流动。即绝对值控制速度。正负控制方向。
 */
data class KRadiusEntity(var x: Float = 0F, var y: Float = 0F, var width: Int = 0, var height: Int = 0,
                         var left_top: Float = 0f, var left_bottom: Float = 0f, var right_top: Float = 0f, var right_bottom: Float = 0f,
                         var strokeWidth: Float = 0F, var strokeColor: Int = Color.TRANSPARENT,
                         var dashWidth: Float = 0F, var dashGap: Float = 0F,
                         var bg_color: Int = Color.TRANSPARENT,
                         var strokeHorizontalColors: IntArray? = null, var strokeVerticalColors: IntArray? = null,
                         var bgHorizontalColors: IntArray? = null, var bgVerticalColors: IntArray? = null,
                         var isBgGradient: Boolean = true, var isStrokeGradient: Boolean = true,
                         var isdashFlow: Boolean = false, var dashSpeed: Float = kpx.x(2f)) {

    fun all_radius(all_radius: Int) {
        all_radius(all_radius.toFloat())
    }

    //fixme 直接设置所有圆角，all_radius属性去除（鸡肋不需要）。
    fun all_radius(all_radius: Float) {
        //this.all_radius = all_radius
        left_top = all_radius
        left_bottom = all_radius
        right_top = all_radius
        right_bottom = all_radius
    }

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
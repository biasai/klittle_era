package cn.oi.klittle.era.entity.widget.compat

import android.graphics.Color
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.widget.compat.KPoint

/**
 * 路径，异形控件
 * @param baseWidth 基准宽
 * @param baseHeight 基准高，如果基准宽高大于0，坐标点集合会根据基准宽高和控件的实际宽高自动适配的。
 * @param points 数据坐标点
 * @param radius 圆角（统一设置，目前Path无法对单个线分别设置）
 * @param strokeWidth 线的宽度
 * @param strokeColor 线的颜色
 * @param dashWidth 虚线的宽度
 * @param dashGap 虚线的间隙
 * @param strokeHorizontalColors 水平渐变色
 * @param strokeVerticalColors 垂直渐变色
 * @param isStrokeGradient 线条是颜色是否渐变，默认是
 * @param isdashFlow 虚线是否流动。
 * @param dashSpeed 虚线的流动速度,绝对值越大速度越快。正数先左流动(正数逆时针方向)。负数向右流动。即绝对值控制速度。正负控制方向。
 * @param shadow_radius 阴影半径
 * @param shadow_dx x偏移量（阴影左右方向），0 阴影居中，小于0，阴影偏左，大于0,阴影偏右
 * @param shadow_dy y偏移量(阴影上下方法)，0 阴影居中，小于0，阴影偏上，大于0,阴影偏下
 * @param shadow_color 阴影颜色
 * @param isDST_IN fixme 是否取下面的交集；默认是。
 * @param isRegionEnable fixme 是否开启区域点击判断，即只有在path区域内点击事件才有效。这样就不会拦截其他控件的点击事件了。亲测有效。
 */
data class KPathEntity(
        var baseWidth: Float = 0F,
        var baseHeight: Float = 0F,
        var points: MutableList<KPoint>? = null,
        var radius: Float = 0F,
        var strokeWidth: Float = kpx.x(2f), var strokeColor: Int = Color.parseColor("#ededed"), var dashWidth: Float = 0F, var dashGap: Float = 0F,
        var strokeHorizontalColors: IntArray? = null, var strokeVerticalColors: IntArray? = null,
        var isStrokeGradient: Boolean = true,
        var isdashFlow: Boolean = false, var dashSpeed: Float = kpx.x(1f),
        var shadow_radius: Float = kpx.x(8f),
        var shadow_dx: Float = kpx.x(0f),
        var shadow_dy: Float = kpx.x(0f),
        var shadow_color: Int = Color.BLACK,
        var isDST_IN: Boolean = true,
        var isRegionEnable: Boolean = true) {

    open fun points(vararg point: KPoint) {
        if (point != null) {
            points = point.toMutableList()//fixme 覆盖原有的数据点
        }
    }

    open fun addPoints(vararg point: KPoint) {
        if (point != null) {
            if (points != null) {
                points?.addAll(point.toMutableList())//fixme 数据点集合会叠加
            } else {
                points = point.toMutableList()
            }
        }
    }

    open fun addPoint(point: KPoint) {
        if (point != null) {
            if (points == null) {
                points = mutableListOf()
            }
            points?.add(point)
        }
    }

    open fun addPoint(x: Float, y: Float) {
        if (points == null) {
            points = mutableListOf()
        }
        points?.add(KPoint(x, y))
    }

    open fun addPoint(x: Int, y: Int) {
        if (points == null) {
            points = mutableListOf()
        }
        points?.add(KPoint(x.toFloat(), y.toFloat()))
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
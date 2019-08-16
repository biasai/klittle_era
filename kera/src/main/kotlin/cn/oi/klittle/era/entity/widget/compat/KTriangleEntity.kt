package cn.oi.klittle.era.entity.widget.compat

import android.graphics.Color
import android.graphics.Typeface
import cn.oi.klittle.era.comm.kpx

/**
 * 直角三角形(左上角是直角)
 * @param x 起点x坐标。默认左上角顶点。
 * @param y 起点y坐标，默认左上角顶点
 * @param width 三角形，x轴方向长度。宽度和高度只要设置一个即可。设置一个。宽和高会自动相等。
 * @param height 三角形，y轴方向高度。
 * @param subWidth 减去的宽度，用于实现矩形效果。
 * @param subHeight 减去的高度，用于实现矩形效果。fixme 宽度和高度，如果只设置一个，另一个会自动相等。所以设置一个即可。
 * @param bg_color 背景颜色
 * @param bgHorizontalColors 背景颜色水平渐变 fixme (从左往右，以斜边为标准，进行渐变)
 * @param bgVerticalColors 背景颜色垂直渐变（优先级比水平高）fixme (从左往右，以斜边上的高为标准，进行渐变)
 * @param isBgGradient 背景色是否渐变,默认是
 * @param strokeWidth 边框宽度
 * @param strokeColor 边框颜色
 * @param strokeHorizontalColors 边框水平渐变颜色数组值【均匀渐变】，[测试发现，渐变色对阴影也有效果]
 * @param strokeVerticalColors 边框垂直渐变颜色数组值【均匀】,会覆盖水平渐变。
 * @param isStrokeGradient 边框色是否渐变,默认是
 * @param dashWidth 虚线长度
 * @param dashGap 虚线之间的间隙
 * @param isdashFlow 虚线是否流动。
 * @param dashSpeed 虚线的流动速度,绝对值越大速度越快。正数先左流动(正数逆时针方向)。负数向右流动。即绝对值控制速度。正负控制方向。
 * @param text 文本内容
 * @param textSize 文本大小，这里是画笔paint直接设置，所以，这里单位是像素px
 * @param textLeftPadding 文字左边的距离。fixme 为了了好计算，文本位置，以斜边上高的交点为基准（起点）。
 * @param textTopPadding 文字上边的距离
 * @param textRotation 文本旋转角度，以斜边上高的交点为基准进行旋转。正数，顺时针旋转。默认逆时针旋转45度。
 * @param isBold 字体是否为粗体
 * @param skewX 实现斜体效果，负数表示右斜，正数左斜。一般 skewX=-0.3f 即可。
 * @param typeface 自定义字体
 * @param isDraw 是否绘制（显示）三角形
 */
data class KTriangleEntity(var x: Int = 0, var y: Int = 0, var width: Int = 0, var height: Int = 0, var subWidth: Int = 0, var subHeight: Int = 0,
                           var bg_color: Int = Color.BLUE, var bgHorizontalColors: IntArray? = null, var bgVerticalColors: IntArray? = null, var isBgGradient: Boolean = true,
                           var strokeWidth: Float = 0F, var strokeColor: Int = Color.WHITE, var strokeHorizontalColors: IntArray? = null, var strokeVerticalColors: IntArray? = null, var isStrokeGradient: Boolean = true,
                           var dashWidth: Float = 0F, var dashGap: Float = 0F,
                           var isdashFlow: Boolean = false, var dashSpeed: Float = kpx.x(1f),
                           var text: String? = null, var textSize: Float = kpx.x(30f), var textColor: Int = Color.WHITE, var textLeftPadding: Float = 0f, var textTopPadding: Float = 0f,
                           var textRotation: Float = -45F, var isBold: Boolean = false, var skewX: Float = 0F, var typeface: Typeface? = null,
                           var isDraw: Boolean = true) {

    //fixme 初始化计算。 为了了好计算，文本位置，以斜边上高的交点为基准（起点）。
    fun initMeasure() {
        if (width <= 0 && height > 0) {
            width = height
        }
        if (height <= 0 && width > 0) {
            height = width
        }
        if (subWidth <= 0 && subHeight > 0 && subHeight < height) {
            subWidth = subHeight
        }
        if (subHeight <= 0 && subWidth > 0 && subWidth < width) {
            subHeight = subWidth
        }
        if (textRotation == -45f && textTopPadding == 0f) {
            textTopPadding = -getTriangleHeight() / 3.5.toFloat()//fixme 默认实现三角形居中(近似，如果不精确还是需要手动去设置)
            if (subWidth > 0 && subHeight > 0) {
                var a = subWidth
                var b = subHeight
                var c2 = a * a + b * b//勾股定理。
                var c = Math.sqrt(c2.toDouble())
                var mTriangleSubHeight = (a * b / c).toInt()
                var sub = mTriangleHeight - mTriangleSubHeight
                textTopPadding = -(sub / 3.5).toFloat()//fixme 默认实现矩形居中(近似)
            }
        }
    }

    var mHypotenuse: Int = 0
    //fixme 获取直角三角形斜边的长度。
    fun getHypotenuse(): Int {
        if (mHypotenuse > 0) {
            return mHypotenuse
        }
        var a = width
        var b = height
        var c2 = a * a + b * b//勾股定理。
        var c = Math.sqrt(c2.toDouble())
        mHypotenuse = c.toInt()
        return mHypotenuse
    }

    /**
     * 知识点：直角三角形直角边为a,b,斜边为c,那么斜边上的高等于ab÷c
     */
    var mTriangleHeight = 0

    //fixme 获取直角三角形，斜边上的高。
    fun getTriangleHeight(): Int {
        if (mTriangleHeight > 0) {
            return mTriangleHeight
        }
        var a = width
        if (height <= 0) {
            height = width
        }
        var b = height
        var c2 = a * a + b * b//勾股定理。
        var c = Math.sqrt(c2.toDouble())
        mTriangleHeight = (a * b / c).toInt()
        return mTriangleHeight
    }

    var mTriangleHeightY = 0
    //fixme 获取三角形斜边上高的Y坐标
    fun getTriangleHeightY(): Int {
        if (mTriangleHeightY > 0) {
            return mTriangleHeightY
        }
        var c = width
        var b = getTriangleHeight()
        var a = Math.sqrt((c * c - b * b).toDouble())
        mTriangleHeightY = (a * b / c).toInt()
        return mTriangleHeightY
    }

    var mTriangleHeightX = 0
    //fixme 获取三角形斜边上高的X坐标
    fun getTriangleHeightX(): Int {
        if (mTriangleHeightX > 0) {
            return mTriangleHeightX
        }
        var c = height
        var b = getTriangleHeight()
        var a = Math.sqrt((c * c - b * b).toDouble())
        mTriangleHeightX = (a * b / c).toInt()
        return mTriangleHeightX
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
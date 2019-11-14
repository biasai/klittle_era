package cn.oi.klittle.era.entity.widget.compat

import android.graphics.Color
import cn.oi.klittle.era.comm.kpx

/**
 * 阴影实体类
 * @param left_top 左上角圆角角度
 * @param left_bottom 左下角圆角角度
 * @param right_top 右上角
 * @param right_bottom 右下角
 * @param bg_color 矩形画布背景颜色，不能为透明，不然什么也看不见（包括阴影），也就是说画布必须有一个背景色
 * @param shadow_color fixme 阴影颜色，会根据这个颜色值进行阴影渐变
 * @param shadowHorizontalColors 阴影水平渐变颜色数组值【均匀渐变】，[测试发现，渐变色对阴影也有效果]
 * @param shadowVerticalColors 阴影垂直渐变颜色数组值【均匀】,会覆盖水平渐变。
 * @param bgHorizontalColors 背景颜色水平渐变
 * @param bgVerticalColors 背景颜色垂直渐变（优先级比水平高）
 * @param strokeWidth 边框宽度
 * @param strokeColor 边框颜色
 * @param dashWidth 虚线长度
 * @param dashGap 虚线之间的间隙
 * @param strokeHorizontalColors 边框水平渐变，优先级比strokeColor高
 * @param strokeVerticalColors 边框垂直渐变色，优先级最高
 * @param isBgGradient 背景色是否渐变,默认是
 * @param isStrokeGradient 边框色是否渐变,默认是
 * @param isShadowGradient 阴影色是否渐变，默认是
 * @param isdashFlow 虚线是否流动。
 * @param dashSpeed 虚线的流动速度,绝对值越大速度越快。正数先左流动(正数逆时针方向)。负数向右流动。即绝对值控制速度。正负控制方向。
 * @param isDST_IN fixme 是否取下面的交集；默认是。
 * @param textColor 字体颜色；fixme 字体颜色和大小，是新增的。为了弥补按钮没有集成txt{}属性。 阴影控件KShadowView就不要太添加txt{}属性，以免文本颜色大小冲突。
 * @param textSize 文本大小；fixme set的时候单位是dp，get获取的时候，单位是px。这里的单位是dp,即保存的是 kpx.textSizeX(30f)
 */
data class KShadowEntity(var left_top: Float = 0f, var left_bottom: Float = 0f, var right_top: Float = 0f, var right_bottom: Float = 0f,
                         var bg_color: Int = Color.WHITE, var shadow_color: Int = Color.BLACK,
                         var shadowHorizontalColors: IntArray? = null, var shadowVerticalColors: IntArray? = null,
                         var bgHorizontalColors: IntArray? = null, var bgVerticalColors: IntArray? = null,
                         var strokeWidth: Float = 0F, var strokeColor: Int = Color.TRANSPARENT,
                         var dashWidth: Float = 0F, var dashGap: Float = 0F,
                         var strokeHorizontalColors: IntArray? = null, var strokeVerticalColors: IntArray? = null,
                         var isBgGradient: Boolean = true, var isStrokeGradient: Boolean = true, var isShadowGradient: Boolean = true,
                         var isdashFlow: Boolean = false, var dashSpeed: Float = kpx.x(1f),
                         var isDST_IN: Boolean = true,
                         var textColor: Int? = null,var textSize: Float? = null) {

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

    open fun shadowHorizontalColors(vararg color: Int) {
        shadowHorizontalColors = color
    }

    open fun shadowHorizontalColors(vararg color: String) {
        shadowHorizontalColors = IntArray(color.size)
        shadowHorizontalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }


    open fun shadowVerticalColors(vararg color: Int) {
        shadowVerticalColors = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun shadowVerticalColors(vararg color: String) {
        shadowVerticalColors = IntArray(color.size)
        shadowVerticalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }
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
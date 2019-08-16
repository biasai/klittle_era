package cn.oi.klittle.era.entity.widget.compat

import android.graphics.Bitmap
import android.graphics.Color

/**
 * 进度条，dst层和src层，实体类
 * @param width 宽带（长度）;居中对齐
 * @param height 进度条(src)或低层(dst)高度;0表示控件本身的高度;居中对齐
 * @param left_top 左上角圆角角度（圆角只对颜色有效，位图无效。）
 * @param left_bottom 左下角圆角角度
 * @param right_top 右上角
 * @param right_bottom 右下角
 * @param bg_color 矩形画布背景颜色，不能为透明，不然什么也看不见（包括阴影），也就是说画布必须有一个背景色
 * @param bgHorizontalColors 背景颜色水平渐变
 * @param bgVerticalColors 背景颜色垂直渐变（优先级比水平高）
 * @param isBgGradient 背景色是否渐变,默认是
 * @param isSrcGradient (只对src层有效)true 渐变色是以src进度条的长度为标准开始渐变;false 渐变色是以整个控件的长度为标准开始渐变。（false 默认以整个控件的长度为标准。）
 * @param bg_bp 位图，直接填充整个控件;(dst全部显示，src只显示进度条的那一部分。)
 */
data class KProgressbar_dst_src_Entity(var width: Int = 0, var height: Int = 0,
                                       var left_top: Float = 0f, var left_bottom: Float = 0f, var right_top: Float = 0f, var right_bottom: Float = 0f,
                                       var bg_color: Int = Color.TRANSPARENT,
                                       var bgHorizontalColors: IntArray? = null, var bgVerticalColors: IntArray? = null,
                                       var isBgGradient: Boolean = true,
                                       var isSrcGradient: Boolean = false,
                                       var bg_bp: Bitmap? = null) {

    //fixme 直接设置所有圆角，all_radius属性去除（鸡肋不需要）。
    fun all_radius(all_radius: Float) {
        //this.all_radius = all_radius
        left_top = all_radius
        left_bottom = all_radius
        right_top = all_radius
        right_bottom = all_radius
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

}
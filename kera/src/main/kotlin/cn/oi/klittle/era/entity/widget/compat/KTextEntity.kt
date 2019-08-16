package cn.oi.klittle.era.entity.widget.compat

import android.graphics.Color

/**
 * 文本状态
 * @param text 文本内容
 * @param textColor 文本颜色值
 * @param textHorizontalColors 文本水平颜色渐变值(以整个控件的宽度为标准进行渐变)
 * @param textVerticalColors  文本垂直颜色渐变（以整个控件的高度为标准进行渐变）
 * @param isTextGradient 文本颜色是否渐变
 * @param textSize fixme 文本大小，set的时候单位是dp，get获取的时候，单位是px。这里的单位是dp,即保存的是 kpx.textSizeX(30f)
 * @param isFakeBoldText 字体是否为粗体
 * @param searchText 搜索文本
 * @param searchTextColor 搜索文本的颜色。
 * @param isMul true搜索所有匹配字符，false只搜索第一个匹配字符。
 */
data class KTextEntity(var text: String? = null, var textColor: Int? = Color.TRANSPARENT,
                       var textHorizontalColors: IntArray? = null, var textVerticalColors: IntArray? = null, var isTextGradient: Boolean = true,
                       var textSize: Float = 0f, var isFakeBoldText: Boolean = false,
                       var searchText: String? = null, var searchTextColor: Int = Color.TRANSPARENT, var isMul: Boolean = true) {

    open fun textHorizontalColors(vararg color: Int) {
        textHorizontalColors = color
    }

    open fun textHorizontalColors(vararg color: String) {
        textHorizontalColors = IntArray(color.size)
        textHorizontalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }


    open fun textVerticalColors(vararg color: Int) {
        textVerticalColors = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun textVerticalColors(vararg color: String) {
        textVerticalColors = IntArray(color.size)
        textVerticalColors?.apply {
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
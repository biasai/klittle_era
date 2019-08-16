package cn.oi.klittle.era.entity.widget.MPAndroidChart

import android.graphics.Color
import com.github.mikephil.charting.data.PieEntry

/**
 * 饼状图数据实体类
 * @param value 值
 * @param label 标签值
 */
open class KPieEntry(value: Float, label: String) : PieEntry(value, label) {
    var color: Int = Color.TRANSPARENT//新增的属性，饼状图的颜色;如："#99FFFF00",前两位颜色透明度是有效果的。

    constructor(value: Float, label: String, color: String) : this(value, label) {
        this.color = Color.parseColor(color)
    }

    constructor(value: Float, label: String, color: Int) : this(value, label) {
        this.color = color
    }

    constructor(value: Int, label: String, color: String) : this(value.toFloat(), label) {
        this.color = Color.parseColor(color)
    }

    constructor(value: Int, label: String, color: Int) : this(value.toFloat(), label) {
        this.color = color
    }

}
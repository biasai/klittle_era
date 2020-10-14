package cn.oi.klittle.era.entity.widget.compat

import android.graphics.Color
import cn.oi.klittle.era.comm.kpx

/**
 * 气泡三角实体类
 * @param direction 气泡的方向
 * @param xOffset 气泡的偏移量（整体偏移）；气泡三角，x轴偏移量，正数向右偏移，负数向左偏移
 * @param yOffset 气泡三角，y轴偏移量；正数向下偏移，负数向上偏移。
 * @param bubblesOffset fixme airOffset是气泡顶点的偏移量，可以实现顶点倾斜效果。xOffset和yOffset是气泡整体的偏移量。
 * @param bubblesWidth 气泡的宽度
 * @param bubblesHeight 气泡的高度
 * @param all_radius 圆角(仅仅是气泡顶点的角度)
 * @param strokeWidth 边框的宽度
 * @param strokeColor 边框的颜色
 * @param bg_color 背景色
 * @param dashWidth 虚线长度（fixme 这里圆角和虚线一起使用；效果好像不是很好。）
 * @param dashGap 虚线之间的间隙
 * @param isDraw 是否绘制
 * Created by 彭治铭 on 2019/4/18.
 */
data class KBubblesEntry(var direction: Int = KBubblesEntry.DIRECTION_LEFT, var xOffset: Float = 0F, var yOffset: Float = 0F, var bubblesOffset: Float = 0f,
                         var bubblesWidth: Int = kpx.x(25), var bubblesHeight: Int = bubblesWidth,
                         var all_radius: Float = 0F,
                         var strokeWidth: Float = 0F, var strokeColor: Int = Color.BLACK,
                         var bg_color: Int = Color.WHITE,
                         var dashWidth: Float = 0F, var dashGap: Float = 0F,
                         var isDraw: Boolean = true) {

    companion object {
        //fixme 气泡箭头的方向：0左边（居中）；1上面（居中）；2右边（居中）；3下面（居中）
        val DIRECTION_LEFT = 0
        val DIRECTION_TOP = 1
        val DIRECTION_RIGHT = 2
        val DIRECTION_BOTTOM = 3
    }

}
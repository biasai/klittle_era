package cn.oi.klittle.era.entity.widget

import android.graphics.Color
import cn.oi.klittle.era.comm.kpx

//                        调用案例
//                        var airEnty = KAirEntry().apply {
//                            direction = KAirEntry.DIRECTION_BOTTOM
//                            //fixme 注意；一定要先设置宽度和高度；然后再调用。
//                            rectWidth = kpx.x(90)
//                            rectHeight = kpx.x(45)
//                            airWidth = kpx.x(25)
//                            airHeight = airWidth
//                            //posX和posY是MPAndroidChart图表的值
//                            x = (posX - rectWidth / 2).toInt()
//                            y = (posY - rectHeight - airHeight - yOffset).toInt() - 10
//                            all_radius = kpx.x(10f)//圆角（所有的圆角；包括气泡）
//                            isAirRadius=false//气泡是否具备圆角
//                            bg_color = Color.LTGRAY
//                            strokeColor = Color.CYAN
//                            strokeWidth = kpx.x(2f)
//                            //圆角和虚线一起使用；效果好像不是很好
//                            //dashWidth=kpx.x(15f)
//                            //dashGap=kpx.x(10f)
//                        }
//                        //画气泡
//                        KCanvasUtils.drawAirBubbles(canvas, airEnty)

/**
 * 气泡实体类
 * @param x 左上角坐标(以左上角为起点标准)
 * @param y
 * @param rectWidth 矩形的宽度
 * @param rectHeight 矩形的高度
 * @param direction 气泡的方向
 * @param xOffset 气泡的偏移量
 * @param yOffset
 * @param airWidth 气泡的宽度
 * @param airHeight 气泡的高度
 * @param all_radius 圆角（所有的圆角；包括气泡）
 * @param isAirRadius 气泡是否也具有圆角效果（默认没有圆角）
 * @param strokeWidth 边框的宽度
 * @param strokeColor 边框的颜色
 * @param bg_color 背景色
 * @param dashWidth 虚线长度（fixme 这里圆角和虚线一起使用；效果好像不是很好。）
 * @param dashGap 虚线之间的间隙
 * Created by 彭治铭 on 2019/4/18.
 */
class KAirEntry(var x: Int = 0, var y: Int = 0, var rectWidth: Int = kpx.x(90), var rectHeight: Int = kpx.x(45),
                var direction: Int = KAirEntry.DIRECTION_BOTTOM, var xOffset: Float = 0F, var yOffset: Float = 0F,
                var airWidth: Int = kpx.x(25), var airHeight: Int = airWidth,
                var all_radius: Float = 0F, var isAirRadius: Boolean = false,
                var strokeWidth: Float = 0F, var strokeColor: Int = Color.BLACK,
                var bg_color: Int = Color.WHITE,
                var dashWidth: Float = 0F, var dashGap: Float = 0F) {

    companion object {
        //气泡的方向：0左边（居中）；1上面（居中）；2右边（居中）；3下面（居中）
        val DIRECTION_LEFT = 0
        val DIRECTION_TOP = 1
        val DIRECTION_RIGHT = 2
        val DIRECTION_BOTTOM = 3
    }

    //FIXME 气泡矩形的中心坐标（这个是在画布Canvas中实时计算出来的。是为了方便文字的添加。）
    var centerX: Float = 0F
    var centerY: Float = 0F
}
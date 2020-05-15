package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

//                        fixme 圆角+气泡使用案例；使用到了 KAirEntry气泡实体类。
//                        kview {
//                            radius {
//                                bg_color=Color.BLUE
//                                all_radius(kpx.x(50))
//                                strokeColor=Color.RED
//                                strokeWidth=kpx.x(2f)
//                            }
//                            //调用案例
//                            var airEnty = KAirEntry().apply {
//                                direction = KAirEntry.DIRECTION_BOTTOM
//                                //fixme 注意；一定要先设置宽度和高度；然后再调用。
//                                rectWidth = kpx.x(90)
//                                rectHeight = kpx.x(45)
//                                airWidth = kpx.x(25)
//                                airHeight = airWidth
//                                var posX= kpx.x(150)
//                                var posY= kpx.x(100)
//                                //posX和posY是MPAndroidChart图表的值
//                                x = (posX - rectWidth / 2).toInt()
//                                y = (posY - rectHeight - airHeight - yOffset).toInt() - 10
//                                all_radius = kpx.x(10f)//圆角（所有的圆角；包括气泡）
//                                isAirRadius = false//气泡是否具备圆角
//                                bg_color = Color.LTGRAY
//                                strokeColor = Color.CYAN
//                                strokeWidth = kpx.x(2f)
//                                //圆角和虚线一起使用；效果好像不是很好
//                                //dashWidth=kpx.x(15f)
//                                //dashGap=kpx.x(10f)
//                            }
//                            draw { canvas, paint ->
//                                //画气泡
//                                KCanvasUtils.drawAirBubbles(canvas, airEnty)
//                            }
//                        }.lparams {
//                            topMargin=kpx.x(30)
//                            leftMargin=topMargin
//                            width=kpx.x(300)
//                            height=kpx.x(200)
//                        }

/**
 * 一般View。继承圆角组件。
 */
open class KView : K7RadiusWidget {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//开启硬件加速,不然圆角没有效果
        clearBackground()
        //去除按钮原有阴影
        clearButonShadow()
    }

}
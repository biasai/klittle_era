package cn.oi.klittle.era.entity.widget

import android.graphics.Color
import cn.oi.klittle.era.comm.kpx

//                     调用案例
//                    //正三角形实体类
//                    var kistriangle=KIsTriangle().apply {
//                        strokeColor=Color.WHITE
//                        strokeWidth=kpx.x(2f)
//                        width=kpx.x(50f)//三角形三个边的长度
//                        height= (KIsTriangle.getHeight(width)+strokeWidth).toInt()//三角形的高
//                        centerX=width/2//三角形中心坐标
//                        centerY=height/2f
//                        //rotation=-90f//旋转角度
//                        all_radius=kpx.x(10f)
//                    }
//                    //画三角形
//                    KCanvasUtils.drawTriangle(canvas,kistriangle)

/**
 * 正三角形（三个边都想等）
 * Created by 彭治铭 on 2019/4/20.
 *
 * @param width 三个边的宽度
 * @param bg_color 背景色
 * @param strokeWidth 边框的宽度
 * @param strokeColor 边框的颜色
 * @param centerX 正三角形的中心点x坐标
 * @param centerY 正三角形的中心点y坐标
 * @param all_radius 圆角（三个角的圆角）;
 * @param rotation 旋转角度；正数顺时针旋转；负数逆时针旋转。fixme 正三角形的默认样式是方向正朝下。
 */
class KIsTriangle(var width: Float = kpx.x(50f), var bg_color: Int = Color.LTGRAY,
                  var strokeWidth: Float = kpx.x(2f), var strokeColor: Int = Color.WHITE,
                  var centerX: Float = width / 2, var centerY: Float = getHeight(width) / 2 + strokeWidth,
                  var all_radius: Float = kpx.x(10f),
                  var rotation: Float = 0F) {

    companion object {
        //计算正三角形的高
        fun getHeight(width: Float): Float {
            var c = width//斜边
            var a = width / 2
            c = c * c
            a = a * a
            var b = c - a
            b = Math.sqrt(b.toDouble()).toFloat()
            return b
        }
    }
}
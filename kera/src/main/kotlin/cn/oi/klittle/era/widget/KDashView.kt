package cn.oi.klittle.era.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.base.KBaseView
import cn.oi.klittle.era.comm.kpx

/**
 * 实现虚线。支持水平和垂直[都是居中]
 * 如果宽大于高，虚线是水平的。如果高大于宽，虚线是垂直的。
 */
open class KDashView : KBaseView {

    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        //fixme 在7.0及以下版本；需要关闭硬件加速；路径画虚线才有效果。
        //fixme 8.0及以上；开启硬件加速虚线也有效果。
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)//虚线必须关闭硬件加速
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }


    //关闭硬件加速。不然在部分手机，如小米。线条与线条之间的连接处有锯齿。
    constructor(context: Context?) : super(context, false) {}

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)//虚线必须关闭硬件加速
    }

    var strokeWidth: Float = kpx.x(0.5f)//边框的宽度
    var strokeColor: Int = Color.WHITE//边框颜色
    var dashWidth: Float = 15F
    var dashGap: Float = 10F
    var isdashFlow: Boolean = false
    var dashSpeed: Float = kpx.x(1f)
    //fixme 0f~20f,20f最好等于实线+虚线的长度。这样不会卡顿。
    private var phase = 0f//虚线偏移量,属性动画，0f,20f向左流动。20f,0f向右流动。总之以0f开始，或以0f结束。0开始是左流动。0结束是右流动。

    var gradientStartColor = Color.WHITE//渐变开始颜色
    var gradientEndColor = Color.WHITE//渐变结束颜色
    //fixme 渐变颜色数组值【均匀渐变】，gradientColors优先
    var gradientColors: IntArray? = null

    fun gradientColors(vararg color: Int) {
        gradientColors = color
    }

    fun gradientColors(vararg color: String) {
        gradientColors = IntArray(color.size)
        gradientColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }
    }


    override fun draw2(canvas: Canvas, paint: Paint) {
        super.draw2(canvas, paint)
        var dashPathEffect = DashPathEffect(floatArrayOf(dashWidth, dashGap), phase)
        paint.setPathEffect(dashPathEffect)
        paint.color = strokeColor
        paint.strokeWidth = strokeWidth
        paint.style = Paint.Style.STROKE
        if (w >= h) {
            //水平虚线
            var linearGradient: LinearGradient? = null
            if (gradientColors != null) {
                linearGradient = LinearGradient(0f, h / 2f, w.toFloat(), h / 2f, gradientColors, null, Shader.TileMode.CLAMP)
            } else {
                linearGradient = LinearGradient(0f, h / 2f, w.toFloat(), h / 2f, gradientStartColor, gradientEndColor, Shader.TileMode.CLAMP)
            }
            paint.setShader(linearGradient)
            canvas.drawLine(0f, h / 2f, w.toFloat(), h / 2f, paint)
        } else {
            //垂直虚线
            var linearGradient: LinearGradient? = null
            if (gradientColors != null) {
                linearGradient = LinearGradient(w / 2f, 0f, w / 2f, h.toFloat(), gradientColors, null, Shader.TileMode.CLAMP)
            } else {
                linearGradient = LinearGradient(w / 2f, 0f, w / 2f, h.toFloat(), gradientStartColor, gradientEndColor, Shader.TileMode.CLAMP)
            }
            paint.setShader(linearGradient)
            canvas.drawLine(w / 2f, 0f, w / 2f, h.toFloat(), paint)
        }

        //控制虚线流动性
        if (isdashFlow && (dashWidth > 0 && dashGap > 0)) {
            if (dashSpeed > 0) {
                if (phase >= Float.MAX_VALUE - dashSpeed) {
                    phase = 0f
                }
            } else {
                if (phase >= Float.MIN_VALUE - dashSpeed) {
                    phase = 0f
                }
            }
            phase += dashSpeed
            invalidate()
        }

    }

}
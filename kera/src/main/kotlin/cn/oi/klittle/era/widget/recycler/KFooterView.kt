package cn.oi.klittle.era.widget.recycler

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.TextView
import cn.oi.klittle.era.comm.kpx
import android.graphics.SweepGradient
import android.view.ViewGroup
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.base.KBaseView


/**
 * 底部加载更多视图
 * //fixme 圆圈默认左居中。圆的坐标变径等属性已经提供出来。可自行修改。
 * //fixme 文本默认右居中，文本就是普通的TextView文本，可以自由控制
 */
open class KFooterView : TextView {

    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        viewGroup.addView(this)//直接添加进去,省去addView(view)
        initUi()
    }

    constructor(viewGroup: ViewGroup, HARDWARE: Boolean) : super(viewGroup.context) {
        if (HARDWARE) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        viewGroup.addView(this)//直接添加进去,省去addView(view)
        initUi()
    }

    //默认开启硬件加速
    constructor(context: Context?, HARDWARE: Boolean = true) : super(context) {
        if (HARDWARE) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        initUi()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        initUi()
    }


    private fun initUi(){
        gravity = Gravity.CENTER or Gravity.RIGHT//fixme 文本默认右居中
        setTextSize(kpx.textSizeX(26))//字体大小
        setTextColor(Color.parseColor("#5D5D5D"))//字体颜色
        setText(KBaseUi.getString(R.string.kloadMore))//正在加载...
    }

    //自定义画布，根据需求。自主实现
    open var draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //自定义，重新绘图
    open fun draw(draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null): KFooterView {
        this.draw = draw
        postInvalidate()//刷新
        return this
    }

    var degrees = 0f//旋转角度
    //fixme 圆圈默认左居中
    //圆心坐标和半径,边框及边框颜色
    var circle_x = 0f
    var circle_y = 0f
    var circle_radus = 0f
    var circle_strokeWidth = kpx.x(3f)
    var circle_startColor = Color.parseColor("#FFDDDDDD")//圆圈开始渐变颜色
    var circle_endColor = Color.parseColor("#00DDDDDD")//圆圈结束渐变颜色
    var paint = KBaseView.getPaint()
    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        if (height > 0) {
            canvas?.let {
                paint=KBaseView.resetPaint(paint)
                paint.isAntiAlias = true
                paint.isDither = true
                //画左边的圈圈
                paint.style = Paint.Style.STROKE
                paint.setStrokeWidth(circle_strokeWidth)
                //圆心
                if (circle_x <= circle_strokeWidth) {
                    circle_x = width / 2f - circle_radus
                }
                if (circle_y <= 0) {
                    circle_y = height / 2f
                }
                if (circle_radus <= 0) {
                    circle_radus = (height - circle_strokeWidth) / 2f
                }
                val sweepGradient = SweepGradient(circle_x, circle_y, intArrayOf(circle_startColor, circle_endColor), null)
                paint.shader = sweepGradient
                canvas.save()
                canvas.rotate(degrees, circle_x, circle_y)
                canvas.drawCircle(circle_x, circle_y, circle_radus, paint)
                canvas.restore()
                paint.shader = null
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.strokeWidth = 0f
                degrees += 5//圆圈转动的速度
                if (degrees >= Int.MAX_VALUE) {
                    degrees = 0f
                }
                invalidate()//不停的刷新
                //自定义绘图
                draw?.let {
                    paint=KBaseView.resetPaint(paint)
                    paint.isAntiAlias = true
                    paint.isDither = true
                    paint.style = Paint.Style.FILL_AND_STROKE
                    paint.strokeWidth = 0f
                    it(canvas, paint)
                }
            }
        }
    }

    //fixme 画自己【onDraw在draw()的super.draw(canvas)流程里面，即在它的前面执行】
    //fixme 可以认为 draw()是前景[上面后画]，onDraw是背景[下面先画]。
    var onDraw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //fixme 画自己[onDraw与系统名冲突，所以加一个横线]
    open fun onDraw_(onDraw: ((canvas: Canvas, paint: Paint) -> Unit)? = null): KFooterView {
        this.onDraw = onDraw
        postInvalidate()//刷新
        return this
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            onDraw?.let {
                var paint = Paint()
                paint.isAntiAlias = true
                paint.isDither = true
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.strokeWidth = 0f
                it(canvas, paint)
            }
        }
    }
}
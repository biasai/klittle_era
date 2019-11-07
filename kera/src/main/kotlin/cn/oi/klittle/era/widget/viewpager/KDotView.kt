package cn.oi.klittle.era.widget.viewpager

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.base.KBaseView
import cn.oi.klittle.era.utils.KProportionUtils

/**
 * viewpager下移动的圆点;圆点之间的距离会根据控件的大小，自由平均分配。
 */
class KDotView : KBaseView {

    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(viewGroup: ViewGroup, HARDWARE: Boolean) : super(viewGroup.context) {
        if (HARDWARE) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    //默认开启硬件加速
    constructor(context: Context?, HARDWARE: Boolean = true) : super(context, HARDWARE) {}

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    //默认圆点颜色
    var defaultColor = Color.parseColor("#9FA1A0")

    fun defaultColor(defaultColor: Int = this.defaultColor): KDotView {
        this.defaultColor = defaultColor
        return this
    }

    //选择圆点颜色
    var selectColor = Color.parseColor("#7160C6")

    fun selectColor(selectColor: Int = this.selectColor): KDotView {
        this.selectColor = selectColor
        return this
    }

    var radius = KProportionUtils.getInstance().adapterInt(15 / 2)//圆点半径
    fun radius(radius: Int = this.radius): KDotView {
        this.radius = radius
        return this
    }

    var offset = KProportionUtils.getInstance().adapterInt(30)//圆点之间的间隙
    fun offset(offset: Int = this.offset): KDotView {
        this.offset = offset
        return this
    }

    private var viewPager: ViewPager? = null
    private var viewPagerOffset: Float = 0f
    fun viewPager(viewPager: ViewPager?): KDotView {
        this.viewPager = viewPager
        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                //position 指向滑动页面的下标
                //positionOffset 偏移量，0到1之间
                if (position >= selectPosition) {
                    //向前
                    viewPagerOffset = (offset + radius * 2) * positionOffset
                } else if (position < selectPosition) {
                    //向后
                    viewPagerOffset = (offset + radius * 2) * (1 - positionOffset)
                    viewPagerOffset *= -1
                }
                invalidate()
            }

            override fun onPageSelected(position: Int) {
                viewPagerOffset = 0f
                selectPosition(position)
            }
        })
        return this
    }

    private var selectPosition = 0//选中下标
    fun selectPosition(selectPosition: Int = this.selectPosition): KDotView {
        this.selectPosition = selectPosition
        invalidate()
        return this
    }

    //当前选中的x,y坐标
    var cx = 0f
    var cy = 0f
    override fun draw2(canvas: Canvas, paint: Paint) {
        super.draw2(canvas, paint)
        viewPager?.let {
            var count = it.adapter?.count//圆点个数
            count?.let {
                canvas.run {
                    var w = radius * count + offset * (count - 1)//圆点+间隙总长度
                    var x = (width - w) / 2 + radius//第一个圆点的x坐标
                    var y = height / 2


                    for (i in 1..count) {
                        //画普通的点
                        paint.color = defaultColor
                        drawDefault?.let {
                            it(canvas, paint, x.toFloat(), y.toFloat())
                        }
                        if (drawDefault == null) {
                            drawCircle(x.toFloat(), y.toFloat(), radius.toFloat(), paint)
                        }

                        if (i == selectPosition + 1) {
                            //选中点的坐标
                            cx = x.toFloat() + viewPagerOffset
                            cy = y.toFloat()
                        }

                        x += radius + offset + radius
                    }
                    //画选中点
                    paint.color = selectColor
                    drawSelect?.let {
                        it(canvas, paint, cx, cy)
                    }
                    if (drawSelect == null) {
                        drawCircle(cx, cy, radius.toFloat(), paint)
                    }
                }
            }

        }
    }

    protected var drawDefault: ((canvas: Canvas, paint: Paint, x: Float, y: Float) -> Unit)? = null
    //fixme 自定义绘制普通圆点,返回的是圆心坐标点。
    fun drawDefault(drawDefault: (canvas: Canvas, paint: Paint, x: Float, y: Float) -> Unit) {
        this.drawDefault = drawDefault
    }

    protected var drawSelect: ((canvas: Canvas, paint: Paint, x: Float, y: Float) -> Unit)? = null
    //fixme 自定义绘制选中圆点,返回的是圆心坐标点。
    fun drawSelect(drawSelect: (canvas: Canvas, paint: Paint, x: Float, y: Float) -> Unit) {
        this.drawSelect = drawSelect
    }
}
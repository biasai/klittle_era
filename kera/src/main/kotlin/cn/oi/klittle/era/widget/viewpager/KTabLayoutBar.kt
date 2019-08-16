package cn.oi.klittle.era.widget.viewpager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

import cn.oi.klittle.era.base.KBaseView
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KAssetsUtils
import cn.oi.klittle.era.utils.KProportionUtils


/**
 * 菜单滑动条
 *
 *
 * android:src="@drawable/crown_p_line_focus" //滑动条为图片
 * android:src="#3388FF" //滑动条为颜色值
 * android:layout_width="match_parent"//是整个控件的宽度，不是滑动条的宽度
 *
 *
 * 或者代码 setTab(滑动位图)，setColor(滑动颜色)
 *
 *
 * mTabLayout?.setViewPager(viewpager)//setViewPager()//与顺序无关，什么时候添加都行。
 *
 * @author 彭治铭
 */
//           fixme 自定义移动条调用案例
//            var tabLayoutBar=ktabLayoutBar {
//                draw { canvas, paint, x, y ->
//                    paint.color = Color.parseColor("#2f5dd9")
//                    paint.style = Paint.Style.STROKE
//                    paint.strokeWidth = kpx.x(6f)
//                    paint.strokeCap = Paint.Cap.ROUND
//                    var x1 = x + kpx.x(170f)
//                    canvas.drawLine(x1, y, x1 + kpx.x(37f), y, paint)
//                }
//            }.lparams {
//                width = matchParent
//                height = kpx.x(10)
//                topMargin=kpx.x(-5)
//            }
//            viewPager {
//                id = kpx.id//fixme viewpager必须添加一个id,不然会报错
//                var list = arrayListOf<Fragment>()
//                adapter = KFragmentPagerAdapter(supportFragmentManager, list)
//                tabLayoutBar.setViewPager(this)//fixme 移动条和viewpager绑定。
//            }.lparams {
//                width = matchParent
//                height = matchParent
//            }

class KTabLayoutBar : android.support.v7.widget.AppCompatImageView, ViewPager.OnPageChangeListener {

    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)//默认关闭硬件加速，不然线帽Cap没有效果。
    }

    constructor(viewGroup: ViewGroup, HARDWARE: Boolean) : super(viewGroup.context) {
        if (HARDWARE) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    private var tab: Bitmap? = null//位图
    private var color = Color.parseColor("#3388FF")//滑动条颜色，默认为蓝色
    var paint: Paint = KBaseView.getPaint()
    var paint2: Paint? = null
    var count = 0//页面个数
    var w = 0//单个tab的宽度
    var x = 0
    var y = 0
    var offset = 0//x的偏移量，用于图片居中

    //fixme 设置ViewPager
    internal var viewPager: ViewPager? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        paint.isAntiAlias = true
        paint?.isDither = true
        paint.color = color
        val src = drawable
        if (src == null) {
            return
        } else if (src is ColorDrawable) {
            color = src.color
            paint.color = color
        } else {
            tab = (src as BitmapDrawable).bitmap
            tab = KProportionUtils.getInstance().adapterBitmap(tab)//适配位图。
        }
        setImageBitmap(null)//src颜色和位图都会清空
    }

    //设置滑动位图
    fun setTab(tab: Bitmap) {
        this.tab = tab
        measure()
    }

    fun setTab(resID: Int) {
        this.tab = KAssetsUtils.getInstance().getBitmapFromAssets(null, resID, true)
        this.tab?.let {
            this.tab = kpx.xBitmap(it)//位图适配
        }
        measure()
    }

    fun getTab(): Bitmap? {
        return tab
    }

    fun getColor(): Int {
        return color
    }

    //设置滑动颜色
    fun setColor(color: Int) {
        this.color = color
        invalidate()
    }

    fun setColor(color: String) {
        this.color = Color.parseColor(color)
        invalidate()
    }

    //fixme 自定义画布，根据需求。自主实现,返回当前移动的坐标点【优先级最高】
    protected open var draw: ((canvas: Canvas, paint: Paint, x: Float, y: Float) -> Unit)? = null

    //自定义，重新绘图,
    open fun draw(draw: ((canvas: Canvas, paint: Paint, x: Float, y: Float) -> Unit)? = null) {
        this.draw = draw
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.let {
            if (this.w <= 0 || this.count <= 0) {
                measure()
            }
            if (this.w > 0) {
                if (draw !== null) {//自定义绘图最优先。
                    if (paint2 == null) {
                        paint2 = KBaseView.getPaint()
                    }
                    draw!!(canvas, paint2!!, x.toFloat(), height / 2f)
                } else {
                    if (this.tab != null && !tab!!.isRecycled) {
                        //位图比颜色优先。
                        canvas.drawBitmap(tab!!, x.toFloat(), y.toFloat(), paint)
                    } else {
                        paint.color = color
                        val rectF = RectF(x.toFloat(), 0f, (x + w).toFloat(), height.toFloat())
                        canvas.drawRect(rectF, paint)
                    }
                }
            }
        }

    }

    fun measure() {
        if (viewPager != null && viewPager!!.adapter != null) {
            count = viewPager!!.adapter!!.count
        }
        if (count > 0) {
            this.w = width / count
            //Log.e("test","getWidth：\t"+getWidth()+"\tw：\t"+w);
            if (tab != null && !tab!!.isRecycled) {
                offset = (this.w - tab!!.width) / 2
                y = (height - tab!!.height) / 2
                x = 0 * w + offset
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        measure()//测量
    }

    fun setViewPager(viewPager: ViewPager?) {
        if (viewPager != null) {
            this.viewPager = viewPager
            viewPager.addOnPageChangeListener(this)//addOnPageChangeListener滑动事件监听，可以添加多个监听。不会冲突。
            measure()
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
//        if (positionOffset >= 0 && positionOffset <= 1 && count > 0) {
//            //positionOffsetPixels是ViewPager的滑动宽度
//            //((float)getWidth()/(float) viewPager.getWidth()) 控件和ViewPager的宽度对比，获取真正的滑动距离。
//            x = (positionOffsetPixels * (width.toFloat() / viewPager!!.width.toFloat()) / count + (position * w).toFloat() + offset.toFloat()).toInt()
//            //Log.e("test","滑动x：\t"+x+"\tposition:\t"+position+"\toffset:\t"+offset+"\twidth:\t"+getWidth()+"\tw:\t"+w+"\tcount:\t"+count+"\tpositionOffsetPixels:\t"+positionOffsetPixels);
//            invalidate()
//        }
        x = (positionOffsetPixels * (width.toFloat() / viewPager!!.width.toFloat()) / count + (position * w).toFloat() + offset.toFloat()).toInt()
        invalidate()
    }

    override fun onPageSelected(position: Int) {
        //x = position * w + offset;//fixme 这里就不要在做计算了。以免计算的不一样。出现卡顿。
        //invalidate();
        //Log.e("test","选中x：\t"+x);
    }

    override fun onPageScrollStateChanged(state: Int) {

    }
}

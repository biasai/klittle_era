package cn.oi.klittle.era.widget.viewpager

import android.content.Context
import android.graphics.*
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
//            var layoutBar=kLayoutBar {
//                //自定义样式（优先使用）
//                draw { canvas, paint, x, y ->
//                    paint.color = Color.parseColor("#2f5dd9")
//                    paint.style = Paint.Style.STROKE
//                    paint.strokeWidth = kpx.x(6f)
//                    paint.strokeCap = Paint.Cap.ROUND
//                    var x1 = x + kpx.x(170f)
//                    canvas.drawLine(x1, y, x1 + kpx.x(37f), y, paint)
//                }
//               setColor(Color.BLUE)//设置颜色值
//               all_radius(kpx.x(15f))//设置圆角
//               xOffset_left= kpx.screenWidth()/2/3/2/2//控制长度半屏幕宽，3个条目，用于居中的间距（自己去细调）
//               xOffset_right=xOffset_left
//            }.lparams {
//                width = matchParent//fixme 会根据控件的宽度和viewpager页面的个数，自动平分item滑动条的宽度。
//                height = kpx.x(10)
//                topMargin=kpx.x(-5)
//            }
//            viewPager {
//                id = kpx.id//fixme viewpager必须添加一个id,不然会报错
//                var list = arrayListOf<Fragment>()
//                adapter = KFragmentPagerAdapter(supportFragmentManager, list)
//                layoutBar.setViewPager(this)//fixme 移动条和viewpager绑定。（这一步，会进行item宽度的计算）
//            }.lparams {
//                width = matchParent
//                height = matchParent
//            }

//fixme 最新使用说明
//                linearLayout {
//                    layoutbar=klayoutBar {
//                        setColor(Color.BLUE)//设置颜色值
//                        all_radius(kpx.x(15f))//设置圆角
//                        //right_top=kpx.x(30f)
//                        var w=kpx.screenWidth()/2
//                        xOffset_left= w/3/2//fixme 两个条目居中设置
//                        xOffset_right=xOffset_left
//                    }.lparams {
//                        width = kpx.screenWidth()/2
//                        height = kpx.x(15)
//                        topMargin = kpx.x(24)
//                    }
//                }.lparams {
//                    width= matchParent
//                }
//                viewPager = kviewPager {
//                    backgroundColor = Color.WHITE
//                    datas?.add("")
//                    datas?.add("")
//                    adapter = KPagerAdapter<String>(datas)
//                    layoutbar?.setViewPager(this)//viewPager个数改变时，这个也要手动调用一次
//                }.lparams {
//                    width = matchParent
//                    height = matchParent
//                }
//fixme viewPager动态改变个数时。
//                        viewPager?.apply {
//                            adapter = KPagerAdapter<String>(datas)//viewPager改变个数，一般都是重制适配器
//                            layoutbar?.setViewPager(this)//个数改变后，滑动条也要重新设置一次。
//                            layoutbar?.apply {
//                                xOffset_left=width/3/2/2 //fixme 3个条目居中
//                                xOffset_right=xOffset_left
//                            }
//                        }

class KLayoutBar : android.support.v7.widget.AppCompatImageView, ViewPager.OnPageChangeListener {

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

    private var bitmap: Bitmap? = null//位图
    private var color = Color.parseColor("#3388FF")//滑动条颜色，默认为蓝色
    var paint: Paint = KBaseView.getPaint()
    var paint2: Paint? = null
    var count = 0//页面个数
    var w = 0//单个tab的宽度
    var x = 0
    var y = 0
    var xOffset = 0//x的偏移量，用于图片居中（限制一般都不用这个变量，一般都使用下面两个变量来控制。比较准确）

    //xOffset_left= kpx.screenWidth()/2/3/2 （两个item，控制长度半屏幕宽）
    //xOffset_left= kpx.screenWidth()/2/3/2/2 （三个item）
    //xOffset_right=xOffset_left
    var xOffset_left = 0//x(控制滑动条与左边的距离)
    var xOffset_right = 0//x(控制滑动条与右边的距离)

    //fixme 设置ViewPager
    internal var viewPager: ViewPager? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        try {
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
                bitmap = (src as BitmapDrawable).bitmap
                bitmap = KProportionUtils.getInstance().adapterBitmap(bitmap)//适配位图。
            }
            setImageBitmap(null)//src颜色和位图都会清空
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //设置滑动位图
    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        measure()
    }

    fun setBitmap(resID: Int) {
        this.bitmap = KAssetsUtils.getInstance().getBitmapFromAssets(null, resID, true)
        this.bitmap?.let {
            this.bitmap = kpx.xBitmap(it)//位图适配
        }
        measure()
    }

    fun getBitmap(): Bitmap? {
        return bitmap
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

    var left_top: Float = 0f//左上角
    var left_bottom: Float = 0f//左下角
    var right_top = 0f//右上角
    var right_bottom = 0f//右下角

    /**
     * fixme 圆角，统一设置。（对颜色值color有效）
     */
    fun all_radius(all_radius: Float) {
        this.left_top = all_radius
        this.left_bottom = all_radius
        this.right_top = all_radius
        this.right_bottom = all_radius
    }

    fun all_radius(all_radius: Int) {
        all_radius(all_radius.toFloat())
    }

    //fixme 自定义画布，根据需求。自主实现,返回当前移动的坐标点【优先级最高】
    protected open var draw: ((canvas: Canvas, paint: Paint, x: Float, y: Float) -> Unit)? = null

    //自定义，重新绘图,
    open fun draw(draw: ((canvas: Canvas, paint: Paint, x: Float, y: Float) -> Unit)? = null) {
        this.draw = draw
    }

    private var strokeWidth = 0f
    override fun draw(canvas: Canvas?) {
        try {
            super.draw(canvas)
            canvas?.drawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)//fixme 画布抗锯齿效果，亲测有效，比画笔paint设置抗锯齿效果还要好。杠杠的。
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
                        if (this.bitmap != null && !bitmap!!.isRecycled) {
                            //位图比颜色优先。
                            canvas.drawBitmap(bitmap!!, x.toFloat(), y.toFloat(), paint)
                        } else {
                            paint.color = color
                            paint?.strokeWidth?.let {
                                if (it > 0) {
                                    strokeWidth = paint.strokeWidth / 2
                                }
                            }
                            if (strokeWidth < 0) {
                                strokeWidth = 0f
                            }
                            val rectF = RectF(x.toFloat() + strokeWidth + xOffset_left, 0f + strokeWidth, (x + w).toFloat() - strokeWidth - xOffset_right, height.toFloat() - strokeWidth)
                            if (left_top > 0 || left_bottom > 0 || right_top > 0 || right_bottom > 0) {
                                // 矩形弧度
                                val radian = floatArrayOf(left_top!!, left_top!!, right_top, right_top, right_bottom, right_bottom, left_bottom, left_bottom)
                                //fixme 路径填充矩形
                                var path = Path()
                                path.addRoundRect(rectF, radian, Path.Direction.CW)
                                canvas.drawPath(path, paint)
                            } else {
                                canvas.drawRect(rectF, paint)
                            }
                        }
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun measure() {
        try {
            if (viewPager != null && viewPager!!.adapter != null) {
                count = viewPager!!.adapter!!.count
            }
            if (count > 0) {
                this.w = width / count
                //Log.e("test","getWidth：\t"+getWidth()+"\tw：\t"+w);
                if (bitmap != null && !bitmap!!.isRecycled) {
                    xOffset = (this.w - bitmap!!.width) / 2
                    y = (height - bitmap!!.height) / 2
                    x = 0 * w + xOffset
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
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
        try {
            x = (positionOffsetPixels.toFloat() * (width.toFloat() / viewPager!!.width.toFloat()) / count.toFloat() + (position * w).toFloat() + xOffset.toFloat()).toInt()
            invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPageSelected(position: Int) {
        //x = position * w + offset;//fixme 这里就不要在做计算了。以免计算的不一样。出现卡顿。
        //invalidate();
        //Log.e("test","选中x：\t"+x);
    }

    override fun onPageScrollStateChanged(state: Int) {

    }
}

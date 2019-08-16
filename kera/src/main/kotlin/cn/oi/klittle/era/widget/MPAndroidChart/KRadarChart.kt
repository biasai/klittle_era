package cn.oi.klittle.era.widget.MPAndroidChart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import cn.oi.klittle.era.base.KBaseView
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.entity.widget.MPAndroidChart.KPieEntry
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import java.lang.Exception
import java.text.DecimalFormat

//            fixme 调用案例
//            KBaseUi.apply {
//                kradarChart {
//                    var entries = ArrayList<RadarEntry>()
//                    val mul = 80f
//                    val min = 20f
//                    for (i in 0 until 7) {
//                        val val1 = (Math.random() * mul).toFloat() + min//随机生成y轴数据.
//                        entries.add(RadarEntry(val1))
//                    }
//                    addData(entries,"雷达蜘蛛网图"){}
//                    //图列
//                    legend {
//                        setVerticalAlignment(Legend.LegendVerticalAlignment.TOP)//顶部
//                        setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER)//居中
//                        setOrientation(Legend.LegendOrientation.HORIZONTAL)
//                    }
//                    var mActivities=arrayOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")
//                    //x周，边的顶点标签
//                    xAxis(*mActivities){}
//                    yAxis?.setDrawLabels(false)//是否绘制标签值,true绘制,false不绘制
//                }.lparams {
//                    width = matchParent
//                    height = matchParent
//                }
//            }



/**
 * 饼状图（扇型图）
 */
open class KRadarChart : RadarChart {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)
        initStyle()
    }

    constructor(context: Context?) : super(context) {
        initStyle()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initStyle()
    }

    //样式初始化;父类已经有一个init()方法了，所以就取名initStyle()
    fun initStyle() {
        setBackgroundColor(Color.rgb(60, 65, 82))//背景色
        getDescription().setEnabled(false)//不绘制描述
        setWebLineWidth(1f)//经线的宽度（）
        setWebColor(Color.LTGRAY)//经线的颜色
        setWebLineWidthInner(1f)//纬线的宽度
        //setWebColorInner(Color.LTGRAY)//纬线的颜色
        //setWebAlpha(100)//透明度(0~255),对经纬线都有效
        /**
         * fixme 雷达图和饼状图一样,不能缩放,没有setScaleEnabled()方法。
         */
        //isRotationEnabled=false//fixme 是否允许旋转.默认允许为true
        //rotationAngle=180f//旋转角度
        //fixme 动画
        //animateXY(1400, 1400, Easing.EaseInOutQuad)
        //animateX(1400, Easing.EaseInOutQuad)
        animateY(1000, Easing.EaseInOutQuad)//感觉y轴动画效果最好,扩张效果
    }

    /**
     * 设置图例；默认在右上角（垂直排列）
     */
    fun legend(block: Legend.() -> Unit): KRadarChart {
        legend.isEnabled = true
        //legend.isEnabled=false//false隐藏标签;true是显示(默认)
        //legend.formSize = kpx.x(25f)//控制标签的大小
        legend.formToTextSpace = kpx.x(5f)//控制图形和文字之间的距离。
        //legend.textColor = Color.GRAY//标签的字体颜色
        //legend.textSize = kpx.textSizeX(12)//标签字体大小；单位是dp
        //legend.typeface //控制标签字体
        //位置偏移量（标签默认位置在左下角；通过偏移量可以控制位置）
        //legend.setDrawInside(true)//true偏移量不会对图表产生影响；false会产生印象（如：yOffset会把整个图表也一起上移）；默认是false；
        //legend.xOffset = -5f //正数；向右边偏移；负数向左边偏移；（X轴方向从左往右）
        //legend.yOffset = 5f //正数；向上面偏移；负数向下偏移；(Y轴方向从下往上)
        // 请注意，这会降低性能和仅适用于”legend 位于图表下面”的情况。
        //legend.isWordWrapEnabled = true
        //矩形
        // legend.formSize 控制矩形的长度；（长度和高度是相等的。亲测是相等；如果你看上去不相等；那肯定是没有显示完全。）
        legend.form = Legend.LegendForm.SQUARE//方形(长度和高度相等)；（不设置的情况下；默认就是方形）
        //圆形
        //legend.formSize 控制圆形的半径
        //legend.form = Legend.LegendForm.CIRCLE//圆形
        //legend.form = Legend.LegendForm.DEFAULT//也是圆形。
        //空的；不显示（等于是透明的）
        //legend.form = Legend.LegendForm.EMPTY//空的；不显示。
        //legend.form=Legend.LegendForm.NONE//也是空形；什么都不显示
        //线条类型
        //legend.formSize控制线的长度；
        //legend.formLineDashEffect= DashPathEffect(floatArrayOf(15f,10f),0f) 虚线(只对线条类型有效)
        //legend.formLineWidth线的高度(可以理解为边框的宽度);(只对线条类型有效)
        //legend.form = Legend.LegendForm.LINE//线条类型；
        legend?.apply {
            setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT)//右对齐
            setVerticalAlignment(Legend.LegendVerticalAlignment.TOP)//上对齐
            setOrientation(Legend.LegendOrientation.VERTICAL)//垂直方向
            setDrawInside(false)//true偏移量不会对图表产生影响；false会产生影响（如：yOffset会把整个图表也一起上移）；默认是false；
            setXEntrySpace(7f)//图列水平之间的间隙；对Legend.LegendOrientation.HORIZONTAL水平方向才有效。
            setYEntrySpace(0f)//图列标签垂直之间的间隙。对Legend.LegendOrientation.VERTICAL垂直方向有效。
            setYOffset(0f)//偏移量；对整个图列整体进行偏移。
            textColor
            textSize
        }
        block(legend)
        return this
    }

    /**
     * 描述；默认设置在右下角
     */
    fun description(block: Description.() -> Unit): KRadarChart {
        description.isEnabled = true//true显示；false隐藏
        description.text = ""//文本，如：线性统计图
        description.textSize//字体大小
        description.textColor//字体颜色
        description.typeface//字体
        description.textAlign = Paint.Align.RIGHT//对齐位置
        //description.setPosition(0f, 50f)//设置的是textAlign的位置;默认在右下角
        //description.setPosition(kpx.screenWidth().toFloat()-kpx.x(36),kpx.x(720).toFloat()-description.textSize)//这个位置效果不错，推荐
        block(description)
        return this
    }

    /**
     * fixme 雷达多边形，边的顶点坐标就是X轴坐标
     * fixme y轴，越靠近边越大。即从内到外y值越大。
     */

    /**
     * x轴:决定边的个数(如果不设置，会根据数据自动计算出来的。)
     */
    open fun xAxis(vararg label: String, block: XAxis.() -> Unit): KRadarChart {
        xAxis?.apply {
            setTextSize(9f)//字体大小
            setTextColor(Color.WHITE)//字体颜色
            setYOffset(0f)//文本y轴偏移量
            setXOffset(0f)//文本x轴偏移量
            //自定义轴值
            setValueFormatter(object : ValueFormatter() {
                //private val mActivities = arrayOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")
                private val mActivities = label
                override fun getFormattedValue(value: Float): String {
                    return mActivities[value.toInt() % mActivities.size]
                }
            })
            block(xAxis)
        }
        return this
    }

    /**
     * y轴:决定环的个数(越靠近边越大。即从内到外y值越大)
     */
    open fun yAxis(vararg label: String, block: YAxis.() -> Unit): KRadarChart {
        yAxis?.apply {
            setTextSize(9f)//字体大小
            setTextColor(Color.WHITE)//字体颜色
            setYOffset(0f)//文本y轴偏移量
            setXOffset(0f)//文本x轴偏移量
            //自定义轴值
            setValueFormatter(object : ValueFormatter() {
                //private val mActivities = arrayOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")
                private val mActivities = label
                override fun getFormattedValue(value: Float): String {
                    return mActivities[value.toInt() % mActivities.size]
                }
            })
            block(yAxis)
        }
        return this
    }

    //fixme clear() 直接调用，清除所有数据
    //fixme setNoDataText("数据为空时显示")

    /**
     * 添加数据（可以添加多条）
     * @param entries 数据点集合
     * @param label 标签
     * @param block 回调，返回LineDataSet
     */
    open fun addData(entries: ArrayList<RadarEntry>, label: String,block: (RadarDataSet.() -> Unit)? = null): KRadarChart {
        var radarDataSet = RadarDataSet(entries, label)
        radarDataSet.apply {
            //setColor(Color.rgb(103, 110, 129))//边框的颜色,灰色
            setColor(Color.rgb(121, 162, 175))//浅绿色
            setLineWidth(2f)//边框的宽度
            setDrawFilled(true)//填充
            //setFillColor(Color.rgb(103, 110, 129))//填充的颜色,灰色
            setFillColor(Color.rgb(121, 162, 175))//浅绿色
            setFillAlpha(180)//填充透明的
            setDrawHighlightCircleEnabled(true)//是否绘制圆圈（外圈和内圈组成的环）
            //半径，以下两个半径组成一个圆环
            //highlightCircleInnerRadius = 5f//内圈的半径
            //highlightCircleOuterRadius=10f//外圈的半径
            //highlightCircleFillColor = Color.RED//填充色，内圈和外圈之间的填充色
            //以下属性只对外圈有效。
            //highlightCircleStrokeWidth = 3f//外圈边框的宽度
            //highlightCircleStrokeColor = Color.CYAN//外圈边框的颜色
            //highlightCircleStrokeAlpha=0 //外圈边框透明度(0~255)
            //十字线
            setDrawHighlightIndicators(false)//true 点击时会有十字线
            //highLightColor=Color.BLUE//十字线的颜色
            //highlightLineWidth=12f//十字线边框的宽度
        }

        var radarData = RadarData()
        radarData.addDataSet(radarDataSet)//添加数据，可以添加多条数据（即多个DataSet）
        radarData?.apply {
            //setValueTypeface(tfLight)
            setValueTextSize(8f)
            setDrawValues(false)//true显示数值,false不显示.
            setValueTextColor(Color.WHITE)
        }
        block?.let {
            it(radarDataSet)
        }
        data = radarData//设置数据
        return this
    }

    /**
     * 数据点集合
     */
    open fun entries(block: ArrayList<RadarEntry>.() -> Unit): ArrayList<RadarEntry> {
        var entries = ArrayList<RadarEntry>()
        block(entries)
        return entries
    }

    //自定义点击选中效果（饼状图也有效）；
    fun setDrawMarkers(callback: (canvas: Canvas, paint: Paint, posX: Float, posY: Float, entry: Entry) -> Unit) {
        setDrawMarkers(true)//开启自定义效果；默认是false
        marker = object : IMarker {
            override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
                return MPPointF.getInstance(posX, posY)
            }

            override fun getOffset(): MPPointF {
                return MPPointF.getInstance(0f, 0f)
            }


            var entry: Entry? = null//记录当前选中数据实体类
            override fun refreshContent(e: Entry?, highlight: Highlight?) {
                //KLoggerUtils.e("refreshContent():\t"+e?.x+"\t"+e?.data)
                this.entry = e
            }

            var paint = KBaseView.getPaint()
            //fixme 先调用 refreshContent()
            //fixme 然后再调用 draw()
            override fun draw(canvas: Canvas?, posX: Float, posY: Float) {
                //fixme posX，posY 返回的始终是选中数据点的坐标值，entry是当前选中的实体类。
                if (canvas != null && entry != null) {
                    callback?.let {
                        it(canvas, KBaseView.resetPaint(paint), posX, posY, entry!!)
                    }
                }
            }
        }
    }

    /**
     * fixme 点击事件监听
     */
    private var isEnableSelected = false//判断点击是否初始化

    private fun enableSelected() {
        if (!isEnableSelected) {
            isEnableSelected = true
            //fixme 选中事件(第一次点击的时候，会触发选中；再次点击相同的地方（区域）触发非选中)
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onNothingSelected() {
                    //什么都没选中
                    //KLoggerUtils.e("onNothingSelected()")
                    onNothingSelected?.let {
                        it()
                    }
                }

                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    //选中
                    //KLoggerUtils.e("e:\t"+e.toString()+"\th:\t"+h)
                    onValueSelected?.let {
                        if (e != null && h != null) {
                            it(e, h)
                        }
                    }
                }
            })
        }
    }

    private var onNothingSelected: (() -> Unit)? = null
    //点击，什么都没有选中时调用
    fun onNothingSelected(onNothingSelected: (() -> Unit)? = null) {
        this.onNothingSelected = onNothingSelected
        enableSelected()
    }

    private var onValueSelected: ((e: Entry, h: Highlight) -> Unit)? = null
    //点击，选中时调用
    fun onValueSelected(onValueSelected: ((e: Entry?, h: Highlight?) -> Unit)? = null) {
        this.onValueSelected = onValueSelected
        enableSelected()
    }

    /**
     * fixme 以下是手势监听
     */

    private var isEnableGesture = false//判断手势是否初始化

    //启用手势
    private fun enableGesture() {
        if (onChartGestureListener == null || !isEnableGesture) {
            isEnableGesture = true
            //fixme 手势监听
            onChartGestureListener = object : OnChartGestureListener {
                override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
                    //KLoggerUtils.e("onChartGestureEnd()\t结束")//每次结束时都会调用
                    onChartGestureEnd?.let {
                        if (me != null && lastPerformedGesture != null) {
                            it(me, lastPerformedGesture)
                        }
                    }
                }

                //fixme 如果图表缩放了。可以滑动；则不会调用这个方法；这个快速滑动是在图表没有移动的时候才会调用。
                //fixme 即 setDragEnabled(false) 时才会调用。（只要为false;不管是快滑，还是慢滑；只要触摸了基本都会调用）
                override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {
                    //velocityX手指滑动的偏移量（负数：向左边滑动（也就是向左边移动）；  正数：向右边滑动）；即 左负右正。
                    //KLoggerUtils.e("onChartFling()\t快速滑动\tvelocityX:\t" + velocityX)
                    onChartFling?.let {
                        if (me1 != null && me2 != null) {
                            it(me1, me2, velocityX, velocityY)
                        }
                    }
                }

                override fun onChartSingleTapped(me: MotionEvent?) {
                    //KLoggerUtils.e("onChartSingleTapped()\t点击")
                    onChartSingleTapped?.let {
                        if (me != null) {
                            it(me)
                        }
                    }
                }

                override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {
                    //KLoggerUtils.e("onChartGestureStart()\t开始")//每次开始时都会调用
                    onChartGestureStart?.let {
                        if (me != null && lastPerformedGesture != null) {
                            it(me, lastPerformedGesture)
                        }
                    }
                }

                override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
                    //KLoggerUtils.e("onChartScale()\t缩放")
                    onChartScale?.let {
                        if (me != null) {
                            it(me, scaleX, scaleY)
                        }
                    }
                }

                override fun onChartLongPressed(me: MotionEvent?) {
                    //KLoggerUtils.e("onChartLongPressed()\t长按")
                    onChartLongPressed?.let {
                        if (me != null) {
                            it(me)
                        }
                    }
                }

                override fun onChartDoubleTapped(me: MotionEvent?) {
                    //KLoggerUtils.e("onChartDoubleTapped()\t双击")
                    onChartDoubleTapped?.let {
                        if (me != null) {
                            it(me)
                        }
                    }
                }

                //fixme setDragEnabled(true) 为true时调用。
                override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
                    //dx手指滑动的偏移量（负数：向左边移动；  正数：向右边移动）
                    //KLoggerUtils.e("onChartTranslate()\t移动(滑动)\tdx:\t" + dX)
                    onChartTranslate?.let {
                        if (me != null) {
                            it(me, dX, dY)
                        }
                    }
                }
            }
        }

    }

    //一:每次结束时都会调用
    private var onChartGestureEnd: ((me: MotionEvent, lastPerformedGesture: ChartTouchListener.ChartGesture) -> Unit)? = null

    fun onChartGestureEnd(onChartGestureEnd: ((me: MotionEvent, lastPerformedGesture: ChartTouchListener.ChartGesture) -> Unit)? = null) {
        this.onChartGestureEnd = onChartGestureEnd
        enableGesture()
    }

    //二:快速滑动
    private var onChartFling: ((me1: MotionEvent, me2: MotionEvent, velocityX: Float, velocityY: Float) -> Unit)? = null

    fun onChartFling(onChartFling: ((me1: MotionEvent, me2: MotionEvent, velocityX: Float, velocityY: Float) -> Unit)? = null) {
        this.onChartFling = onChartFling
        enableGesture()
    }

    //三:单击
    private var onChartSingleTapped: ((me: MotionEvent) -> Unit)? = null

    fun onChartSingleTapped(onChartSingleTapped: ((me: MotionEvent) -> Unit)? = null) {
        this.onChartSingleTapped = onChartSingleTapped
        enableGesture()
    }

    //四:每次开始时都会调用
    private var onChartGestureStart: ((me: MotionEvent, lastPerformedGesture: ChartTouchListener.ChartGesture) -> Unit)? = null

    fun onChartGestureStart(onChartGestureStart: ((me: MotionEvent, lastPerformedGesture: ChartTouchListener.ChartGesture) -> Unit)? = null) {
        this.onChartGestureStart = onChartGestureStart
        enableGesture()
    }

    //五:缩放回调
    private var onChartScale: ((me: MotionEvent, scaleX: Float, scaleY: Float) -> Unit)? = null

    fun onChartScale(onChartScale: ((me: MotionEvent, scaleX: Float, scaleY: Float) -> Unit)? = null) {
        this.onChartScale = onChartScale
        enableGesture()
    }

    //六:长按
    private var onChartLongPressed: ((me: MotionEvent) -> Unit)? = null

    fun onChartLongPressed(onChartSingleTapped: ((me: MotionEvent) -> Unit)? = null) {
        this.onChartLongPressed = onChartLongPressed
        enableGesture()
    }

    //七:双击
    private var onChartDoubleTapped: ((me: MotionEvent) -> Unit)? = null

    fun onChartDoubleTapped(onChartDoubleTapped: ((me: MotionEvent) -> Unit)? = null) {
        this.onChartDoubleTapped = onChartDoubleTapped
        enableGesture()
    }

    //八:移动
    private var onChartTranslate: ((me: MotionEvent, dX: Float, dY: Float) -> Unit)? = null

    fun onChartTranslate(onChartTranslate: ((me: MotionEvent, dX: Float, dY: Float) -> Unit)? = null) {
        this.onChartTranslate = onChartTranslate
        enableGesture()
    }

    //fixme 销毁
    open fun onDestroy() {
        try {
            clearAnimation()
            clear()
            setOnChartValueSelectedListener(null)
            isEnableSelected = false
            onChartGestureListener = null
            isEnableGesture = false
            onChartGestureEnd = null
            onChartFling = null
            onChartSingleTapped = null
            onChartGestureStart = null
            onChartScale = null
            onChartLongPressed = null
            onChartDoubleTapped = null
            onChartTranslate = null
            marker = null
            setOnClickListener(null)
            setOnLongClickListener(null)
            setOnTouchListener(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
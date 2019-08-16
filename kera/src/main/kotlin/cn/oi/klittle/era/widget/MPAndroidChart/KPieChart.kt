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
import com.github.mikephil.charting.charts.PieChart
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

//                fixme 调用案例
//            KBaseUi.apply {
//                kpieChart {
//                    //setMaxAngle(180f)// HALF CHART(半圆)
//                    setRotationAngle(180f)//旋转角度。校正位置
//                    //添加数据
//                    addData(entries {
//                        add(KPieEntry(2, "宁波", Color.RED))
//                        add(KPieEntry(2, "上海", Color.CYAN))
//                        add(KPieEntry(2, "龙山", Color.BLUE))
//                        add(KPieEntry(2, "长沙", Color.LTGRAY))
//                    }, "饼状统计图") {
//                        setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE)//在外部显示(会有一条横线(线条))
//                        //PieDataSet.ValuePosition.OUTSIDE_SLICE时有效；设置线条的属性
//                        setValueLinePart1OffsetPercentage(80f)//线条的起始位置
//                        setValueLinePart1Length(0.2f)//第一段线条
//                        setValueLinePart2Length(0.4f)//第二代线条
//                        setSelectionShift(5f)//设置饼块选中时偏离饼图中心的距离
//                        sliceSpace = 0f//饼块之间的间隙
//                    }
//                    //图列
//                    legend {
//
//                    }
//                    //描述
//                    description {
//
//                    }
//                    //自定义点击选中效果（饼状图也有效）
//                    setDrawMarkers { canvas, paint, posX, posY, entry ->
//
//                    }
//                }.lparams {
//                    width = matchParent
//                    height = matchParent
//                }
//            }


/**
 * 饼状图（扇型图）
 */
open class KPieChart : PieChart {
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
        //fixme 饼状图的半径由宽和高的最小边决定。
//            extraLeftOffset = kpx.pixelToDp(50f)//左边偏移量，单位是dp
//            extraRightOffset = kpx.pixelToDp(50f)//右边偏移量
//            extraTopOffset=kpx.pixelToDp(50f)
//            extraBottomOffset=kpx.pixelToDp(50f)
        setExtraOffsets(20f, 10f, 20f, 10f)//设置图表整体的偏移量
        //fixme 饼状态没有缩放功能。即没有setScaleEnabled()方法。
        isHighlightPerTapEnabled = true//点击时选中的部分会变大。只是变大。不会有颜色变化。
        isRotationEnabled = true//fixme 是否开启旋转功能(手指触摸时可以旋转)
        getDescription().setEnabled(false)//不显示描述
        rotationAngle = 0f//旋转角度；正数顺时针旋转。默认值是 270.0
        //fixme 标签值
        setDrawEntryLabels(true)//是否显示标签值
        setEntryLabelColor(Color.WHITE)//设置标签的字体颜色
        //setEntryLabelTypeface(tfRegular)
        //setEntryLabelTextSize(16f)//设置标签的字体大小。单位dp
        setEntryLabelTextSize(kpx.pixelToDp(36f))
        //fixme setEntryLabelTextSize();setValueTextSize()(在addData()里调用了);标签值的大小和轴值的大小不要相差太大；字体大小最好差不多(间隙效果最好)
        //fixme 不然标签值可能会覆盖一部分轴值。标签值和轴值的大小差不多的情况不会出现遮挡问题。
        //fixme 标签值和轴值之间的间隙目前是没办法改的。

        //fixme 滑动摩擦系数
        isDragDecelerationEnabled = true//是否开启滑动加速。true开启；false不会有滑动摩擦；会立即停止。
        setDragDecelerationFrictionCoef(0.95f)//设置手指触摸之后，减速摩擦系数。[0;区间，高值 表示速度将缓慢下降，例如，如果将其设置为0，则为 会立即停止。1很长时间才会停下来]
        //fixme 中间圆圈
        isDrawHoleEnabled = true//是否画中间的圆圈
        setHoleColor(Color.WHITE)//圆圈的颜色

        setTransparentCircleColor(Color.WHITE)//透明圆圈的颜色
        setTransparentCircleAlpha(110)//透明圆圈的透明度

        setHoleRadius(58f)//圆圈的半径，单位是百分比。是整个饼状图的百分比。
        setTransparentCircleRadius(61f)//透明圆圈的半径，单位也是百分比；以整个饼状图的半径为参照物。

        //fixme 画中间文字（文本默认都是居中显示的。）
        setDrawCenterText(true)//是否绘制中间文件
        //setCenterTextTypeface(tfLight)//中间文字字体
        setCenterText(generateCenterSpannableText())//fixme 绘制中间文字
        setCenterTextSize(kpx.textSizeX(24))//中间文字字体大小；单位DP
        setCenterTextColor(Color.BLUE)
        setCenterTextOffset(0f, 0f)//字体偏移量;参数一是x轴偏移量。参数二是y轴偏移量。
        //动画；以下三个动画；对饼状图效果都是一样。都是逆时针旋转的动画。
        //animateX(1400)
        //animateY(1400)
        animateXY(1400, 1400)//参数一是x轴动画时间；参数二是y轴动画时间。XY轴动画同时进行
    }

    //设置中间文本
    private fun generateCenterSpannableText(): SpannableString? {
        try {
            val s = SpannableString("MPAndroidChart\ndeveloped by Philipp Jahoda")
            //MPAndroidChart 变大1.7倍
            s.setSpan(RelativeSizeSpan(1.7f), 0, 14, 0)
            //developed by
            s.setSpan(StyleSpan(Typeface.NORMAL), 14, s.length - 15, 0)//正常
            s.setSpan(ForegroundColorSpan(Color.GRAY), 14, s.length - 15, 0)//灰色
            s.setSpan(RelativeSizeSpan(.8f), 14, s.length - 15, 0)//0.8倍大下
            //Philipp Jahoda
            s.setSpan(StyleSpan(Typeface.ITALIC), s.length - 14, s.length, 0)//斜体
            s.setSpan(ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length - 14, s.length, 0)
            return s
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 设置图例；默认在右上角（垂直排列）
     */
    fun legend(block: Legend.() -> Unit): KPieChart {
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
    fun description(block: Description.() -> Unit): KPieChart {
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

    //fixme clear() 直接调用，清除所有数据
    //fixme setNoDataText("数据为空时显示")

    /**
     * 添加数据（可以添加多条）
     * @param entries 数据点集合
     * @param label 标签
     * @param block 回调，返回LineDataSet
     */
    open fun addData(entries: ArrayList<KPieEntry>, label: String, isPercentFormatter: Boolean = true, block: (PieDataSet.() -> Unit)? = null): KPieChart {
        var datas = arrayListOf<PieEntry>()
        var colors = arrayListOf<Int>()
        for (i in 0..entries.lastIndex){
            datas.add(entries[i])
            colors.add(entries[i].color)//fixme 颜色值
        }
        var pieDataSet = PieDataSet(datas, label)//一个LineDataSet就对应一条数据线
        pieDataSet.apply {
            //setDrawEntryLabels(false)//是否显示标签值；true显示；false不显示；initPieData()方法里已经设置过了。
            //setColor(Color.RED, Color.GREEN)//这样设置颜色数组无效
//            val colors = java.util.ArrayList<Int>()//fixme  这样设置颜色数组才有效。(数据大于颜色数组时；会依次循环。)
//            colors.apply {
//                //fixme 注意是add()不是addColor()哦。
//                add(Color.parseColor("#99FFFF00"))//前两位颜色透明度是有效果的。
//                add(Color.parseColor("#99FF8000"))
//                add(Color.parseColor("#9980FF80"))
//                add(Color.parseColor("#9900FFFF"))
//            }
            setColors(colors)
            setDrawValues(true)//是否显示数值
            setValueTextColor(Color.BLACK)
            //fixme setEntryLabelTextSize()(在initStyle()里调用了);setValueTextSize();标签值的大小和轴值的大小不要相差太大；字体大小最好差不多(间隙效果最好)
            setValueTextSize(kpx.pixelToDp(32f))//字体大小；单位dp
//                setValueTextSize(15f)
            valueTypeface//字体
            sliceSpace = kpx.x(3f)//fixme 间隙（饼块之间的间隙）
            setSelectionShift(5f)//fixme 设置饼块选中时偏离饼图中心的距离(点击时的偏离距离)

            setXValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE)//在内部显示
            //setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE)//在外部显示(会有一条横线(线条))

            //PieDataSet.ValuePosition.OUTSIDE_SLICE时有效；设置线条的属性
            setValueLinePart1OffsetPercentage(80f)//线条的起始位置
            setValueLinePart1Length(0.2f)//第一段线条
            setValueLinePart2Length(0.4f)//第二代线条
            valueLineColor
            valueLineWidth

        }
        var pieData = data
        if (pieData == null) {
            pieData = PieData()
        }
        pieData.addDataSet(pieDataSet)
        setUsePercentValues(true)//fixme 数据格式变为百分比格式如：3.19f变成 31.90 ；但是末尾没有百分比符号%
        if (isPercentFormatter) {
            setPercentFormatter()//设置百分比格式
        }
        highlightValues(null)//fixme 不选中任何。
        block?.let {
            it(pieDataSet)
        }
        //fixme 数值属性设置
        data = pieData
        return this
    }

    /**
     * 数据点集合
     */
    open fun entries(block: ArrayList<KPieEntry>.() -> Unit): ArrayList<KPieEntry> {
        var entries = ArrayList<KPieEntry>()
        block(entries)
        return entries
    }

    fun setPercentFormatter() {
        data?.let {
            //fixme 重写PercentFormatter
            var percentFormatter = object : PercentFormatter() {
                override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
                    var str = super.getPieLabel(value, pieEntry)
                    if (str.contains("%")) {
                        return str
                    } else {
                        return str + "%" //加上百分比符号%
                    }
                }

            }
            percentFormatter.mFormat = DecimalFormat("00.00")//fixme 自定义数据格式
            //fixme 注意 setUsePercentValues(true)+PercentFormatter()一起使用才能正确的实现百分比。
            it.setValueFormatter(percentFormatter)
        }
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
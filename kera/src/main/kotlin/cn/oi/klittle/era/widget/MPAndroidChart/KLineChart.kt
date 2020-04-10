package cn.oi.klittle.era.widget.MPAndroidChart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import cn.oi.klittle.era.base.KBaseView
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.entity.widget.compat.KRadiusEntity
import cn.oi.klittle.era.utils.KCanvasUtils
import cn.oi.klittle.era.utils.KSelectorUtils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
//import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.lang.Exception

//                fixme 调用案例
//                klineChart {
//                    //设置图例
//                    legend {}
//                    //描述
//                    description {
//                        text = "线型统计图"
//                        setPosition(kpx.screenWidth().toFloat() - kpx.x(36), kpx.x(720).toFloat() - description.textSize)
//                    }
//                    //第一条线数据
//                    addData(entries {
//                        add(Entry(0f, 2f))
//                        add(Entry(1f, 4f))
//                        add(Entry(2f, 0f))
//                        add(Entry(3f, 2f))
//
//                        add(Entry(4f, 4f))
//                        add(Entry(5f, 0f))
//                        add(Entry(6f, 2f))
//
//                        add(Entry(7f, 4f))
//                        add(Entry(8f, 0f))
//                        add(Entry(9f, 2f))
//                    }, "A") {}
//                    //第二条线数据
//                    addData(entries {
//                        add(Entry(0f, 2f))
//                        add(Entry(1f, 0f))
//                        add(Entry(2f, 4f))
//                        add(Entry(3f, 2f))
//
//                        add(Entry(4f, 0f))
//                        add(Entry(5f, 4f))
//                    }, "B") {
//                        setColor(Color.RED)
//                        setMode(LineDataSet.Mode.HORIZONTAL_BEZIER)
//                        setDrawCircles(false)
//                        setAxisDependency(YAxis.AxisDependency.LEFT)
//                        fillDrawable = KSelectorUtils.getGradientDrawable("#8080FF", "#008080FF")
//                    }
//                    //警戒线
//                    addLimitLine(3f, "警告") {}
//                    //左边的y轴
//                    axisLeft {}
//                    //x轴
//                    xAxis {
//                        setDrawLabels(true)
//                        setLabelCount(7)
//                    }
//                    valueFormatter("周一", "周二", "周三", "周四", "周五", "周六", "周末")
//                    //缩放和滑动到指定位置
//                    zoom(4, 2)
//                    //自定义点击选中效果
//                    var bitmap = KAssetsUtils.getInstance().getBitmapFromResource(R.mipmap.kera_water)
//                    bitmap=kpx.compressBitmap(bitmap,kpx.x(60))
//                    setDrawMarkers { canvas, paint, posX, posY, entry ->
//                            var x=posX-bitmap.width/2
//                            var y=posY-bitmap.height
//                            canvas.drawBitmap(bitmap,x,y,paint)
//                    }
//                    onNothingSelected {
//                        KLoggerUtils.e("没有选中")
//                    }
//                    onValueSelected { e, h ->
//                        KLoggerUtils.e("选中：\t"+e+"\t"+h)
//                    }
//                    onChartDoubleTapped {
//                        KLoggerUtils.e("双击")
//                    }
//                }.lparams {
//                    width = matchParent
//                    height = kpx.x(720)
//                }


/**
 * 线型图表
 */
open class KLineChart : LineChart {
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
        //fixme 边框
        setDrawBorders(false)//是否画边框；ture绘制；false不绘制（默认）
        //setBorderColor(Color.RED)//边框颜色
        //setBorderWidth(3f)//边框宽度
        setScaleEnabled(false)//是否允许缩放，true允许，false不允许缩放
        //点击高亮效果；具体的效果要在lineDataSet.highLightColor = Color.RED里设置
        isHighlightPerTapEnabled = true//条形点击时；会有高亮颜色变化
        isHighlightPerDragEnabled = true//拖动时；也会有高亮效果的变化。
        //fixme 触摸滑动（放大之后才能触摸滑动）
        setDragEnabled(true)//true能够手动触摸滑动；false不能
        setPinchZoom(false)//true X,Y轴同时缩放，false则X,Y轴单独缩放,默认false
        //fixme 动画；
        //animateX(1000)//画x轴的动画时间；单位毫秒；效果感觉不怎么好；只有在x轴数据比较多时效果才明显才会；不然不怎么好。
        //animateXY(500, 2000)//x轴，y轴都会有动画效果
        //animateY(1500)//会有上升的效果
        //动画效果都不怎么好，所以默认屏蔽掉。
    }

    fun zoom(visiNum: Int, toX: Int) {
        zoom(visiNum, toX.toFloat())
    }

    /**
     * 缩放滑动（最好先添加数据(会自动计算出最大值和最小值坐标)，然后再调用）
     * @param visiNum要显示的标签个数
     * @param toX要滑动到的位置坐标
     */
    fun zoom(visiNum: Int, toX: Float = 0F) {
        //fixme x轴本身是不会滑动的。会滑动是因为被放大了。只有放大了才会滑动。（所以要想实现滑动功能；把x轴放大即可。）
        //xAxis.mAxisMaximum=11f
        //fixme 计算出条形图的最大长度值。（一定要先设置数据；只有设置数据之后axisMaximum才会自动计算出来。）
        var maxLength = xAxis.axisMaximum - xAxis.axisMinimum //最大坐标值（如果数量是7个，那边坐标就应该是6；因为下标是从0开始的
        //KLoggerUtils.e("axisMaximum:\t"+xAxis.axisMaximum+"\taxisMinimum:\t"+xAxis.axisMinimum)
        //var visibleNum = 4f//fixme 要显示的标签个数(等价于要显示的x轴长度)；
        var visibleNum = visiNum - 1//fixme 因为线性图是从下标0开始的。所以要减去一。
        xAxis.labelCount = visibleNum.toInt()//fixme 设置一下要显示的个数；这样才能正确的显示出整数标签；很重要。
        //xAxis.setLabelCount(4,true)//这个是强制；即一旦强制label标签就固定了；不会移动。
        var scaleX = maxLength.toFloat() / visibleNum.toFloat()//fixme 缩放倍率=最大长度值/要显示的个数
        //按照比率缩放显示,1f表示不放大缩小；最小不能小于1；小于1无效。
        //zoom(scaleX, 1f, 0f, 0f)//scaleX x轴缩放；scaleY y轴缩放；x,y感觉没什么作用；设置为0f即可(不能实现指定位置滑动)
        //var toX = 2F//要滑动到的x轴值。（fixme 如果是条形图；则会滑动到对应的下标。）
        //fixme 所以最好加上xAxis.axisMinimum；一定要转float类型，不然不精准。
        var xValue = toX + visibleNum.toFloat() / 2.toFloat() + xAxis.axisMinimum//加上要显示x轴值的一半；（xValue是居中值；所以要想滑动到最左边；必须再加上x轴值的一半。这样居中就能移动到最左边了。）
        zoom(scaleX, 1f, xValue, 0f, YAxis.AxisDependency.LEFT)//fixme 可以实现到指定位置滑动； 参数三和四 xValue,yValue 可以让该值滑动到图表中间(是居中不是最左边)。让该值居中。（前提是能滑动。）
    }

    /**
     * 设置图例；默认矩形左下角
     */
    fun legend(block: Legend.() -> Unit): KLineChart {
        legend.isEnabled = true
        //legend.isEnabled=false//false隐藏标签;true是显示(默认)
        //legend.formSize = kpx.x(25f)//控制标签的大小
        legend.formToTextSpace = kpx.x(5f)//控制图形和文字之间的距离。
        //legend.textColor = Color.GRAY//标签的字体颜色
        //legend.textSize = kpx.textSizeX(12)//标签字体大小；单位是dp
        //legend.typeface //控制标签字体
        //位置偏移量（标签默认位置在左下角；通过偏移量可以控制位置）
        //legend.setDrawInside(true)//true偏移量不会对图表产生影响；false会产生印象（如：yOffset会把整个图表也一起上移）；默认是false；
        legend.xOffset = -5f //正数；向右边偏移；负数向左边偏移；（X轴方向从左往右）
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
        //图例显示的位置：如下2行代码设置图例显示在左下角
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        //图例的排列方式：水平排列和竖直排列2种
        legend.orientation = Legend.LegendOrientation.HORIZONTAL//默认就是水平
        block(legend)
        return this
    }

    /**
     * 描述；默认设置在右下角
     */
    fun description(block: Description.() -> Unit): KLineChart {
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
    open fun addData(entries: ArrayList<Entry>, label: String, block: (LineDataSet.() -> Unit)?=null): KLineChart {
        var lineDataSet = LineDataSet(entries, label)//一个LineDataSet就对应一条数据线
        lineDataSet.setDrawValues(true)//true显示数据(默认显示)；false不显示数据
        //fixme 标签（legend 或者叫图例）
        //lineDataSet.label = "A"//标签名称
        //fixme 线条边框的宽度（控制线条的大小）
        lineDataSet.lineWidth = kpx.x(0.5f)
        //fixme 线条的颜色(标签的颜色和线条的颜色是一致的。)
        lineDataSet.setColor(Color.GREEN)
        //这个也能设置线条的颜色和标签的颜色；但是多个颜色值是标签色的个数；不是渐变色。
        //lineDataSet.setColors(Color.YELLOW,Color.RED,Color.BLACK)
        //lineDataSet.setGradientColor(Color.RED, Color.CYAN)//渐变色没有效果。
        //fixme 虚线
        lineDataSet.enableDashedLine(15f, 10f, 5f)//开启虚线；参数为 虚线的长度；间隙；偏移量
        //lineDataSet.disableDashedLine()//关闭虚线
        //fixme 线条颜色填充
        lineDataSet.setDrawFilled(true)//开启颜色填充。（默认是关闭的）
        //lineDataSet.fillColor = Color.parseColor("#00FF40")//填充颜色值（透明度设置无效；内部自己会设置透明度。）
        //lineDataSet.fillAlpha = 22//这里设置填充色的透明度；需要单独设置。在颜色值里设置透明度无效。
        //这个可以实现填充颜色渐变。
        //lineDataSet.fillDrawable = KSelectorUtils.getGradientDrawable(Color.parseColor("#00FFFF"), Color.parseColor("#0000FFFF"))//透明色不要使用Color.TRANSPARENT(低版本7.0及以下；会有黑色阴影问题。)
        lineDataSet.fillDrawable = KSelectorUtils.getGradientDrawable("#00FFFF", "#0000FFFF")
        //fixme 字体颜色大小和字体
        //设置字体的颜色；和圆圈一样；依次循环
        //lineDataSet.setValueTextColors(arrayListOf(Color.RED,Color.GREEN,Color.BLUE))
        //字体颜色
        lineDataSet.valueTextColor = Color.BLACK
        //字体大小；单位是dp
        //lineDataSet.valueTextSize = kpx.textSizeX(18f)
        //lineDataSet.valueTypeface=null//字体样式
        //fixme 数据点圆圈
        //lineDataSet.circleColorCount 设置圆圈颜色值的个数；默认是1
        //lineDataSet.circleColors = arrayListOf(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.BLACK, Color.WHITE)//数据点的圆圈第一个是红色的，第二个是蓝色的；第三个又是红色的；以此循环。
        //lineDataSet.setCircleColor(Color.CYAN)//设置数据点圆圈的颜色
        //lineDataSet.setDrawCircles(false)//true画圆圈；false不画；默认就true
        lineDataSet.setDrawCircleHole(true)//圆圈画洞；true是空心；false是实心的；默认就是true空心。
        //lineDataSet.circleHoleColor = Color.GREEN//内部圆的颜色
        lineDataSet.circleHoleRadius = kpx.x(2.5f)//内部圆的半径
        lineDataSet.circleRadius = kpx.x(3.5f)//圆的半径
        //lineDataSet.resetCircleColors()// 报错(无用)
        //fixme 模式
        //lineDataSet.mode=LineDataSet.Mode.LINEAR//正常连接（默认）
        lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER)//数据点之间的连接；线条具有两个弯曲的效果(类似正弦函数一样。两个弯是对齐的。)
        //lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER)//一个弯曲效果。
        //lineDataSet.mode=LineDataSet.Mode.STEPPED//走势图；（数据点之间以直角的方式进行连接）
        //fixme 轴的依赖【针对y轴的；y轴有左右两个；默认是左边的y轴】
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT)//依赖左轴。
        //fixme 高亮显示
        lineDataSet.highLightColor = Color.RED
        lineDataSet.highlightLineWidth = 0.5f
        lineDataSet.setDrawVerticalHighlightIndicator(true);    //绘制垂直高亮(只有线行图有；条形图没有)
        lineDataSet.setDrawHorizontalHighlightIndicator(true); //绘制水平高亮(只有线行图有；条形图没有)
        lineDataSet.enableDashedHighlightLine(15f, 10f, 0f)//高亮虚线样式
        block?.let {
            it(lineDataSet)
        }
        var lineData = lineData//可以有多条LineDataSet（即多条线）
        if (lineData == null) {
            lineData = LineData()
        }
        lineData.addDataSet(lineDataSet)
        data = lineData//设置数据
        return this
    }

    /**
     * 数据点集合
     */
    open fun entries(block: ArrayList<Entry>.() -> Unit): ArrayList<Entry> {
        var entries = ArrayList<Entry>()
        block(entries)
        return entries
    }

    /**
     *  添加警戒线（可以添加多条）
     *  @param limit 警戒线Y轴的坐标位置
     *  @param label 标签名称
     *  @param yAxis Y轴，默认左边的Y轴，警戒线依附在Y轴上。
     *  @param block 警戒线回调
     *
     */
    open fun addLimitLine(limit: Float, label: String, yAxis: YAxis = axisLeft, block: LimitLine.() -> Unit): KLineChart {
        yAxis?.apply {
            var limitLine = LimitLine(limit, label)
            //var limitLine = LimitLine(3f, "limit")//参数一 警戒线的坐标位置；参数二 警戒线名称
            //limitLine.isEnabled = true//true显示（默认）；false不显示
            limitLine.lineColor = Color.RED//线条颜色
            //limitLine.lineWidth //线条边框宽度
            limitLine.lineWidth = kpx.x(0.3f)//警戒线的宽度
            limitLine.textColor = Color.RED //字体颜色
            //limitLine.textSize //字体大小；单位dp
            //limitLine.typeface //自定义字体
            limitLine.enableDashedLine(15f, 10f, 0f)//警戒线虚线（有效）
            //警戒线标签位置
            //limitLine.labelPosition = LimitLine.LimitLabelPosition.LEFT_BOTTOM//左边下面
            limitLine.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP//左边上面
            //limitLine.labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM//右边下面
            //limitLine.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP//右边上面
            //警戒线位置偏移量
            //limitLine.xOffset
            limitLine.yOffset = 5f
            limitLines.add(limitLine)//添加警戒线(可以添加多条)
            block(limitLine)
        }
        return this
    }

    /**
     * 左边的y轴
     */
    open fun axisLeft(block: YAxis.() -> Unit): KLineChart {
        axisLeft?.apply {
            //fixme 反转（只有y轴有；x轴没有）
            //isInverted = true//true y轴反转(数值从上往下);false正常（数值从下往上）[默认就是false]
            //fixme 位置；
            //setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)//数值(标签)在y轴里面显示
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)//数值(标签)在y轴外面（默认）
            //axisMaximum=3f //最大值（有效）；如果没有设置；会根据数据自动设置轴值
            //axisMinimum=1f//最小值（有效）
            //setLabelCount(4,true) //设置标签的个数；不设置；会根据数据自动适配
            //valueFormatter
            //设置标签和轴的之间的间隙
            xOffset = 10f
            yOffset = 0f
            //setCenterAxisLabels(true)
        }
        block(axisLeft)
        return this
    }

    /**
     * 右边的y轴
     */
    open fun axisRight(block: YAxis.() -> Unit): KLineChart {
        axisRight?.apply {
            //fixme 反转（只有y轴有；x轴没有）
            //isInverted = true//true y轴反转(数值从上往下);false正常（数值从下往上）[默认就是false]
            //fixme 位置；
            //setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)//数值(标签)在y轴里面显示
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)//数值(标签)在y轴外面（默认）
            //axisMaximum=3f //最大值（有效）；如果没有设置；会根据数据自动设置轴值
            //axisMinimum=1f//最小值（有效）
            //setLabelCount(4,true) //设置标签的个数；不设置；会根据数据自动适配
            //valueFormatter
            //设置标签和轴的之间的间隙
            xOffset = 10f
            yOffset = 0f
            //setCenterAxisLabels(true)
        }
        block(axisRight)
        return this
    }

    /**
     * x轴
     */
    open fun xAxis(block: XAxis.() -> Unit): KLineChart {
        xAxis?.apply {
            isEnabled = true//true显示(默认)；false不显示
            //fixme 位置
            //XAxis.XAxisPosition.BOTH_SIDED 顶部，底部都显示；数值都在x轴的外面
            //XAxis.XAxisPosition.BOTTOM 底部显示；数值在x轴的外面
            //XAxis.XAxisPosition.BOTTOM_INSIDE 底部显示；数值在x轴的内部显示
            //XAxis.XAxisPosition.TOP 顶部显示；数值在x轴的外面（默认就是这个）
            //XAxis.XAxisPosition.TOP_INSIDE 顶部显示；数值在x轴的里面显示
            position = XAxis.XAxisPosition.BOTTOM//在底部显示
            //fixme 字体
            typeface//字体
            textColor//字体颜色
            textSize//字体大小
            //fixme x轴线
            setDrawAxisLine(true)//true绘制x轴线；false不绘制
            //axisLineColor = Color.CYAN //轴线的颜色
            //axisLineWidth = 0.7f//轴线的线条边框宽度
            //x轴虚线（暂时没有效果；估计还没开发）
            //enableAxisLineDashedLine(15f, 10f, 0f)//x轴虚线（暂时无效）
            //setAxisLineDashedLine(DashPathEffect(floatArrayOf(15f, 10f), 0f))//x轴虚线(暂时无效)
            //fixme 网格线
            setDrawGridLines(false)//true绘制网格线；false不绘制网格线
            //gridColor = Color.CYAN//网格线的颜色
            //gridLineWidth = 0.5f//网格线的线条边框宽度
            //lineLength 虚线的长度；spaceLength 虚线之间的间隙；phase偏移量
            //enableGridDashedLine(15f, 10f, 0f)//网格线虚线（有效）
            //setGridDashedLine(DashPathEffect(floatArrayOf(15f, 10f), 0f))//网格线虚线(有效)
            //fixme 轴标签
            setDrawLabels(true)//true绘制轴的标签(x轴上的数据)[默认绘制]；false 不绘制
            //labelRotationAngle = 30f //标签旋转角度（整数顺时针旋转）
            //setCenterAxisLabels(true)//标签会在两个坐标点的中间显示（居中显示）。
            //显示label标签的个数；参数二true是强制；false不强制(会自主根据实际情况显示)；
            //fixme 一般都没有指定标签个数；自动适配挺好。（标签个数写死之后;会丧失滑动功能；位置就不动了。如果x轴可以缩放滑动；最好不要写死。）
            //setLabelCount(4,true)//只是控制标签的个数；不能控制x轴的单位个数。
            //设置标签和轴的之间的间隙
            xOffset = 0f
            yOffset = 10f
            //fixme 自定义轴值；（如果没有设置；会根据数据自动设置轴值；一般都不用设置。自动适配挺好的。）
            //axisMinimum = 0f//最小值(x轴最左边的值)
            //axisMaximum = 4f//最大值(x轴最右边的值)
            //fixme 警戒线
            //var limitLine = LimitLine(3f, "limit")
            //limitLines.add(limitLine)
        }
        block(xAxis)
        return this
    }

//                    var array = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周末")
//                    valueFormatter(*array) { value, position, label, labels ->
//                        //fixme value坐标轴上的原始值，
//                        //fixme postion 下标(value的整数化)，
//                        //fixme label 当前坐标轴上对应的标签值
//                        //fixme labes 标签值数组
//                        var labe2 = label
//                        if (labe2.trim().equals("")) {
//                            //labe2=labels[position]//fixme 基本上，只需要处理label为空""的情况。
//                        }
//                        labe2//高阶函数表达式，会自动返回最后一行的数据
//                    }
    /**
     * 自定义X轴标签
     *var array = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周末")
     * valueFormatter(*array)//直接传数组
     * valueFormatter("周一", "周二", "周三", "周四", "周五", "周六", "周末")//传动态参数
     * @param callback 重新定义标签值的返回（基本上不需要，以下方法基本都处理好了）。
     */
    open fun valueFormatter(vararg labes: String, callback: ((value: Float, position: Int, label: String, labes: MutableList<String>) -> String?)? = null): KLineChart {
        xAxis?.apply {
            //自定义轴标签（默认是数值标签）
            valueFormatter = object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                    //return super.getAxisLabel(value, axis)
                    //fixme 图表缩放之后；value数值是不会变化；即value的数值返回的是原有的数值。
                    //KLoggerUtils.e("value:\t"+value)
                    var position = value.toInt()
                    var label: String? = ""
                    if (position >= 0 && labes.size > 0) {
                        if (position.toFloat() == value) {//如果相等说明是整数。这里只显示整数部分的标签[因为下标是整数]
                            if (position != 0) {//0不能作除数
                                position = position % labes.size//fixme 防止下标越界；以及下标依次循环;模运算只会小；不会大也不会等于。
                            }
                            if (position <= labes.lastIndex) {
                                label = labes[position]//fixme 自定义标签（只显示下标为整数部分的标签值）
                            }
                        } else {
                            label = ""//非整数部分不显示。
                        }
                        //回调可以重新定义返回的值。（以上方法的刷新，只显示整数部分的标签。）
                        callback?.let {
                            if (label == null) {
                                label = ""
                            }
                            try {
                                //fixme value坐标轴上的原始值，
                                //fixme postion 下标(value的整数化)，
                                //fixme label 当前坐标轴上对应的标签值
                                //fixme labes 标签值数组
                                it(value, position, label!!, labes.toMutableList()).let {
                                    label = it//fixme 回调，基本上只需要处理label为空""的情况，不为空时，可以直接返回label
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                        if (label != null) {
                            return label!!.trim()//fixme 标签值不为空，就返回，为空null就不返回！
                        }
                    }
                    return super.getAxisLabel(value, axis)//原有数值标签
                }
            }
        }
        return this
    }

    //自定义点击选中效果；
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
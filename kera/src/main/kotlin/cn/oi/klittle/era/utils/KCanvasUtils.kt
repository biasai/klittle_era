package cn.oi.klittle.era.utils

import android.graphics.*
import cn.oi.klittle.era.base.KBaseView
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.entity.widget.KAirEntry
import cn.oi.klittle.era.entity.widget.KIsTriangle
import cn.oi.klittle.era.entity.widget.compat.KRadiusEntity
import cn.oi.klittle.era.entity.widget.compat.KTriangleEntity

/**
 * 画布工具类
 * Created by 彭治铭 on 2019/4/11.
 */
object KCanvasUtils {
    private var paint = KBaseView.getPaint()
    private var kradiusUtils = KRadiusUtils()
    private fun drawRadius(canvas: Canvas, model: KRadiusEntity, kradius: KRadiusUtils) {
        model.let {
            //画圆角
            kradius?.apply {
                x = it.x
                y = it.y
                w = it.width
                h = it.height
                all_radius = 0f
                left_top = it.left_top
                left_bottom = it.left_bottom
                right_top = it.right_top
                right_bottom = it.right_bottom
                strokeWidth = it.strokeWidth
                strokeColor = it.strokeColor
                //支持虚线边框
                dashWidth = it.dashWidth
                dashGap = it.dashGap
                strokeGradientColors = it.strokeHorizontalColors
                strokeGradientOritation = ORIENTATION_HORIZONTAL
                if (it.strokeVerticalColors != null) {
                    strokeGradientColors = it.strokeVerticalColors
                    strokeGradientOritation = ORIENTATION_VERTICAL
                }
                isStrokeGradient = it.isStrokeGradient
                isDST_IN = false//fixme 不取下面的交集
                //fixme 画圆角矩形；取消了虚线偏移量(不具备流动效果。)
                drawRadius(canvas, 0f, 0, 0)
            }
        }
    }

    //获取画笔
    fun getPaint(): Paint {
        return KBaseView.resetPaint(paint)
    }

    /**
     * 画垂直文本
     * x,y 是文本的起点位置
     * offset 垂直文本之间的间隙
     */
    fun drawVerticalText(text: String, canvas: Canvas?, paint: Paint, x: Float, y: Float, offset: Float) {
        canvas?.let {
            KBaseView.drawVerticalText(text, it, paint, x, y, offset)
        }
    }

    //fixme 画圆角矩形
    fun drawRadius(canvas: Canvas?, radius: KRadiusEntity) {
        canvas?.apply {
            drawRadius(canvas, radius, kradiusUtils)
        }
    }

    //fixme 参照 https://blog.csdn.net/weixin_33851177/article/details/87383294
    //fixme 画水滴
    /**
     * @param startX 开始坐标(顶点坐标；上面（居中）)
     * @param startY
     * @param waterWidth 水滴的宽度
     * @param waterHeight 水滴的高度
     * @param waterColor 水滴的颜色
     */
    fun drawWater(canvas: Canvas?, startX: Float, startY: Float, waterWidth: Int, waterHeight: Int, waterColor: Int = Color.parseColor("#8080C0")) {
        canvas?.apply {
            var waterPaint = getPaint()
            var waterPath = Path()
            var startPoint = PointF()//起点
            var endPoint = PointF()//结束点
            var control1 = PointF()//控制点1
            var control2 = PointF()//控制点2
            var control3 = PointF()//控制点3
            var control4 = PointF()//控制点4
            var xoff = waterWidth / 3f
            startPoint.x = startX
            startPoint.y = startY
            endPoint.x = startPoint.x
            endPoint.y = startPoint.y + waterHeight
            control1.x = startPoint.x - xoff
            control1.y = startPoint.y
            control2.x = startPoint.x + xoff
            control2.y = control1.y
            control3.x = startPoint.x - xoff / 4
            control3.y = endPoint.y - waterHeight / 3f
            control4.x = startPoint.x + xoff / 4
            control4.y = control3.y
            waterPaint.color = waterColor
            waterPath.moveTo(startPoint.x, startPoint.y)
            waterPath.cubicTo(control1.x, control1.y, control3.x, control3.y, endPoint.x, endPoint.y)
            waterPath.cubicTo(control4.x, control4.y, control2.x, control2.y, startPoint.x, startPoint.y)
            canvas.drawPath(waterPath, waterPaint)
        }
    }

    /**
     * fixme 画气泡；KAirEntry里面有调用案例。（对话框样式）
     */
    fun drawAirBubbles(canvas: Canvas?, kAirEntry: KAirEntry) {
        canvas?.apply {
            var paint = getPaint()
            paint.color = kAirEntry.bg_color
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.strokeWidth = kAirEntry.strokeWidth
            //画内容
            drawAirBubbles2(canvas, kAirEntry, paint)
            if (kAirEntry.strokeWidth > 0 && kAirEntry.strokeColor != Color.TRANSPARENT) {
                paint = getPaint()
                paint.color = kAirEntry.strokeColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = kAirEntry.strokeWidth
                //画边框
                drawAirBubbles2(canvas, kAirEntry, paint)
            }
        }
    }

    private fun drawAirBubbles2(canvas: Canvas?, kAirEntry: KAirEntry, paint: Paint) {
        canvas?.apply {
            //            var paint = paint()
//            paint.color = Color.BLACK
//            paint.style = Paint.Style.FILL_AND_STROKE
//            paint.strokeWidth = kAirEntry.strokeWidth
            var path = Path()
            var rectWidth = kAirEntry.rectWidth//矩形宽度
            var rectHeight = kAirEntry.rectHeight//矩形高度
            var leftTopPoint = Point()//左上角
            leftTopPoint.x = kAirEntry.x
            leftTopPoint.y = kAirEntry.y
            var rightTopPoint = Point()//右上角
            rightTopPoint.x = leftTopPoint.x + rectWidth
            rightTopPoint.y = leftTopPoint.y
            var leftBottomPoint = Point()//左下角
            leftBottomPoint.x = leftTopPoint.x
            leftBottomPoint.y = leftTopPoint.y + rectHeight
            var rightBottomPoint = Point()//右下角
            rightBottomPoint.x = rightTopPoint.x
            rightBottomPoint.y = leftBottomPoint.y

            //计算气泡矩形区域的中心坐标
            kAirEntry.centerX = (leftTopPoint.x + kAirEntry.rectWidth / 2).toFloat()
            kAirEntry.centerY = (leftTopPoint.y + kAirEntry.rectHeight / 2).toFloat()

            var direction = kAirEntry.direction//气泡的方向：0左边（居中）；1上面（居中）；2右边（居中）；3下面（居中）
            //气泡偏移量（以居中为标准进行便宜。）
            var xOffset = kAirEntry.xOffset//正数向右偏移；负数向左偏移。
            var yOffset = kAirEntry.yOffset//正数向下偏移；负数向上偏移。
            var airWidth = kAirEntry.airWidth//气泡宽度
            var airHeight = kAirEntry.airHeight//气泡高度
            //路径连接方向；从左往右；顺时针。
            path.moveTo(leftTopPoint.x.toFloat(), leftTopPoint.y.toFloat())
            if (direction == KAirEntry.DIRECTION_TOP) {
                //上面（居中）
                var tox1 = leftTopPoint.x + rectWidth / 2 - airWidth / 2 + xOffset
                var toy1 = leftTopPoint.y + yOffset
                path.lineTo(tox1, toy1)
                var tox2 = tox1 + airWidth / 2
                var toy2 = toy1 - airHeight
                path.lineTo(tox2, toy2)
                if (!kAirEntry.isAirRadius) {
                    //气泡不具备圆角
                    path.lineTo(tox2, toy2)//fixme 同一个点连接两次；就不会具备圆角效果。
                }
                var tox3 = tox2 + airWidth / 2
                var toy3 = toy1
                path.lineTo(tox3, toy3)
            }
            path.lineTo(rightTopPoint.x.toFloat(), rightTopPoint.y.toFloat())
            if (direction == KAirEntry.DIRECTION_RIGHT) {
                //右边（居中）
                var tox1 = rightTopPoint.x + xOffset
                var toy1 = rightTopPoint.y + rectHeight / 2 - airWidth / 2 + yOffset
                path.lineTo(tox1, toy1)
                var tox2 = tox1 + airHeight
                var toy2 = toy1 + airWidth / 2
                path.lineTo(tox2, toy2)
                if (!kAirEntry.isAirRadius) {
                    //气泡不具备圆角
                    path.lineTo(tox2, toy2)
                }
                var tox3 = tox1
                var toy3 = toy2 + airWidth / 2
                path.lineTo(tox3, toy3)
            }
            path.lineTo(rightBottomPoint.x.toFloat(), rightBottomPoint.y.toFloat())
            if (direction == KAirEntry.DIRECTION_BOTTOM) {
                //下面居中
                var tox1 = rightBottomPoint.x - rectWidth / 2 + airWidth / 2 + xOffset
                var toy1 = rightBottomPoint.y + yOffset
                path.lineTo(tox1, toy1)
                var tox2 = tox1 - airWidth / 2
                var toy2 = toy1 + airHeight
                path.lineTo(tox2, toy2)
                if (!kAirEntry.isAirRadius) {
                    //气泡不具备圆角
                    path.lineTo(tox2, toy2)
                }
                var tox3 = tox2 - airWidth / 2
                var toy3 = toy1
                path.lineTo(tox3, toy3)
            }
            path.lineTo(leftBottomPoint.x.toFloat(), leftBottomPoint.y.toFloat())
            if (direction == KAirEntry.DIRECTION_LEFT) {
                //左边居中
                var tox1 = leftTopPoint.x + xOffset
                var toy1 = leftTopPoint.y + rectHeight / 2 + airWidth / 2 + yOffset
                path.lineTo(tox1, toy1)
                var tox2 = tox1 - airHeight
                var toy2 = toy1 - airWidth / 2
                path.lineTo(tox2, toy2)
                if (!kAirEntry.isAirRadius) {
                    //气泡不具备圆角
                    path.lineTo(tox2, toy2)
                }
                var tox3 = tox1
                var toy3 = toy2 - airWidth / 2
                path.lineTo(tox3, toy3)
            }
            //path.lineTo(leftTopPoint.x.toFloat(), leftTopPoint.y.toFloat())//直接连接起点；可能圆角效果无效。
            path.close()//封闭；路径圆角有效果
            //圆角
            var corner: CornerPathEffect? = null
            if (kAirEntry.all_radius > 0) {
                corner = CornerPathEffect(kAirEntry.all_radius)
                paint.setPathEffect(corner)//圆角属性
            }
            //虚线
            var dashPathEffect: DashPathEffect? = null
            if (kAirEntry.dashWidth > 0 && kAirEntry.dashGap > 0) {
                dashPathEffect = DashPathEffect(floatArrayOf(kAirEntry.dashWidth, kAirEntry.dashGap), 0f)
                paint.setPathEffect(dashPathEffect)
            }
            //组合动画（保留圆角和虚线的效果）
            if (corner != null && dashPathEffect != null) {
                var composePathEffect = ComposePathEffect(corner, dashPathEffect)
                paint.setPathEffect(composePathEffect)
            }
            //绘制路径
            drawPath(path, paint)
        }
    }

    /**
     * fixme 画正三角形
     */
    fun drawTriangle(canvas: Canvas?, kIsTriangle: KIsTriangle) {
        canvas?.apply {
            var paint = getPaint()
            paint.setStyle(Paint.Style.FILL_AND_STROKE)//可以画实心三角形
            paint.strokeWidth = kIsTriangle.strokeWidth
            paint.color = kIsTriangle.bg_color
            drawTriangle2(canvas, kIsTriangle, paint)
            if (kIsTriangle.strokeWidth > 0 && kIsTriangle.strokeColor != Color.TRANSPARENT) {
                var paint = getPaint()
                paint.setStyle(Paint.Style.STROKE)//画三角形边框
                paint.strokeWidth = kIsTriangle.strokeWidth
                paint.color = kIsTriangle.strokeColor
                drawTriangle2(canvas, kIsTriangle, paint)
            }
        }
    }

    private fun drawTriangle2(canvas: Canvas?, kIsTriangle: KIsTriangle, paint: Paint) {
        canvas?.apply {
            var path = Path()
            var width = kIsTriangle.width//三条边的宽度
            var height = KIsTriangle.getHeight(width)//正三角形的高
            var centerX = kIsTriangle.centerX//正三角形中心坐标
            var centerY = kIsTriangle.centerY
            var x1 = centerX - width / 2 + kIsTriangle.strokeWidth
            var y1 = centerY - height / 2 + kIsTriangle.strokeWidth
            var x2 = x1 + width
            var y2 = y1
            var x3 = centerX
            var y3 = centerY + height / 2 - kIsTriangle.strokeWidth

            if (kIsTriangle.rotation != 0f) {
                canvas.save()
                canvas.rotate(kIsTriangle.rotation, centerX, centerY)//旋转
            }
            path.moveTo(x1, y1)
            path.lineTo(x2, y2)//这样一定要注意，很容易错。不要把lineTo写成moveTo
            path.lineTo(x3, y3)
            path.close()
            if (kIsTriangle.all_radius > 0) {
                //圆角效果
                var corner = CornerPathEffect(kIsTriangle.all_radius)
                paint.setPathEffect(corner)
            }
            canvas.drawPath(path, paint)
            if (kIsTriangle.rotation != 0f) {
                canvas.restore()
            }
        }
    }
}
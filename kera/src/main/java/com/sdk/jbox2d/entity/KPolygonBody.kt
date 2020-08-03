package com.sdk.jbox2d.entity

import android.graphics.*
import cn.oi.klittle.era.comm.kpx
import org.jbox2d.dynamics.Body

/**
 * 多边形（矩形）刚体
 * fixme width，height宽高和body刚体;初始化之后，就不能在修改。修改了也没有效果。
 */
open class KPolygonBody(private var width: Float = kpx.x(200f), private var height: Float = kpx.x(50f)) : KBaseBody() {
    //var body: Body? = null//fixme 刚体，body.position.x, body.position.y是刚体的中心坐标。

    //fixme 以下属性是绘图属性，可以任意修改。
    var color: Int = Color.RED//颜色
    var strokeWidth: Float = kpx.x(2f)//边框宽度
    var dashWidth: Float = 0F//虚线宽度
    var dashGap: Float = 0F//虚线间隔
    var phase: Float = 0F//虚线偏移量
    var dashSpeed: Float = 0F//虚线流动速度
    var style: Paint.Style = Paint.Style.FILL_AND_STROKE//类型

    //fixme 绘制矩形
    fun draw(canvas: Canvas?, paint: Paint?) {
        if (canvas == null || paint == null) {
            return
        }
        drawBitmap(canvas,paint)?.let {
            if (it){
                return //fixme 优先绘制位图
            }
        }
        body?.let {
            if (color != Color.TRANSPARENT && width > 0 && height > 0) {
                paint?.color = color
                paint?.style = style
                paint?.strokeWidth = strokeWidth
                //虚线
                if (dashWidth > 0 && dashGap > 0 && style != Paint.Style.FILL) {
                    var dashPathEffect: DashPathEffect? = DashPathEffect(floatArrayOf(dashWidth, dashGap), phase)
                    paint?.setPathEffect(dashPathEffect)
                    if (dashSpeed != 0f) {
                        if ((phase + dashSpeed) >= Float.MAX_VALUE) {
                            phase = 0f
                        }
                        phase += dashSpeed
                    }
                    dashPathEffect = null
                }
                var left = it.position.x.toInt() - width
                var top = it.position.y.toInt() - height
                var right = it.position.x.toInt() + width
                var bottom = it.position.y.toInt() + height
                if (style != Paint.Style.FILL) {
                    left = left + strokeWidth / 2
                    right = right - strokeWidth / 2
                    top = top + strokeWidth / 2
                    bottom = bottom - strokeWidth / 2
                }
                if (it.angle!=0f){
                    canvas.save()
                    canvas.rotate(Math.toDegrees(it.angle.toDouble()).toFloat(),it.position.x,it.position.y)//fixme 旋转角度。
                }
                canvas.drawRect(Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt()), paint)
                if (it.angle!=0f){
                    canvas.restore()
                }
                paint?.setPathEffect(null)
            }
        }
    }
}
package com.sdk.jbox2d.entity

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import cn.oi.klittle.era.comm.kpx
import org.jbox2d.dynamics.Body

/**
 * 圆形刚体
 * fixme radius半径和body刚体;初始化之后，就不能在修改。修改了也没有效果。
 */
open class KCircleBody(private var radius: Float = kpx.x(50f)) {
    var body: Body? = null//fixme 刚体，body.position.x, body.position.y是刚体的中心坐标。

    //fixme 以下属性是绘图属性，可以任意修改。
    var color: Int = Color.RED//颜色
    var strokeWidth: Float = kpx.x(2f)//边框宽度
    var dashWidth: Float = 0F//虚线宽度
    var dashGap: Float = 0F//虚线间隔
    var phase: Float = 0F//虚线偏移量
    var dashSpeed: Float = 0F//虚线流动速度
    var style: Paint.Style = Paint.Style.FILL_AND_STROKE//类型

    //fixme 绘制圆形
    fun drawCircle(canvas: Canvas?, paint: Paint?) {
        if (canvas == null || paint == null) {
            return
        }
        body?.let {
            if (color != Color.TRANSPARENT && radius > 0) {
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
                if (style == Paint.Style.FILL) {
                    canvas?.drawCircle(it.position.x, it.position.y, radius, paint)
                } else {
                    canvas?.drawCircle(it.position.x, it.position.y, radius - strokeWidth, paint)
                }
                paint?.setPathEffect(null)
            }
        }
    }
}
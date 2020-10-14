package cn.oi.klittle.era.utils

import android.graphics.*
import android.os.Build
import cn.oi.klittle.era.base.KBaseView
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.widget.compat.K0Widget
import cn.oi.klittle.era.widget.compat.K1Widget

/**
 * 圆角功能工具类
 */
open class KRadiusUtils {

    //绘制矩形的起点。
    var x: Float = 0F
    var y: Float = 0F

    //外补丁
    var leftMargin: Int = 0;
    var rightMargin: Int = 0;
    var topMargin: Int = 0;
    var bottomMargin: Int = 0

    //绘制矩形的宽和高
    var w: Int = 0
    var h: Int = 0

    var all_radius: Float = 0F//默认，所有圆角的角度
    var left_top: Float = 0f//左上角
    var left_bottom: Float = 0f//左下角
    var right_top = 0f//右上角
    var right_bottom = 0f//右下角

    var strokeWidth = 0f//边框宽度
    var strokeColor = Color.TRANSPARENT//边框颜色
    var dashWidth: Float = 0F//虚线长度
    var dashGap: Float = 0F//虚线之间的间隙

    var isDST_IN = true//fixme 是否取下面的交集；默认是。

    //fixme 边框颜色渐变
    var strokeGradientStartColor = Color.TRANSPARENT//渐变开始颜色
    var strokeGradientEndColor = Color.TRANSPARENT//渐变结束颜色

    //fixme 渐变颜色数组值【均匀渐变】，gradientColors优先
    var strokeGradientColors: IntArray? = null
    var ORIENTATION_VERTICAL = 0//垂直
    var ORIENTATION_HORIZONTAL = 1//水平
    var strokeGradientOritation = ORIENTATION_HORIZONTAL//渐变颜色方向，默认水平
    var isStrokeGradient: Boolean = true

    /**
     * FIXME 画边框，圆角;亲测以下方法百分百可行，没问题！
     * phase 虚线偏移量，实现虚线流动性。
     * scrollX 水平滑动值
     * scrollY 上下滑动值，兼容滑动
     */
    fun drawRadius(canvas: Canvas?, phase: Float = 0f, scrollX: Int = 0, scrollY: Int = 0) {
        //KLoggerUtils.e("test","宽:\t"+w+"\t高:\t"+h+"\t边框宽度：\t"+strokeWidth+"\t边框颜色:\t"+strokeColor)
        canvas?.let {
            if (all_radius > 0) {
                if (left_top <= 0) {
                    left_top = all_radius
                }
                if (left_bottom <= 0) {
                    left_bottom = all_radius
                }
                if (right_top <= 0) {
                    right_top = all_radius
                }
                if (right_bottom <= 0) {
                    right_bottom = all_radius
                }
            }
            //利用内补丁画圆角。只对负补丁有效(防止和正补丁冲突，所以取负)
            var paint = KBaseView.getPaint()
            paint.strokeCap = Paint.Cap.BUTT
            paint.strokeJoin = Paint.Join.MITER
            paint.isDither = true
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL_AND_STROKE//FIXME 统一使用 FILL_AND_STROKE样式。感觉效果要比Paint.Style.FILL好那么一丁点。
            paint.strokeWidth = 0f
            if (isDST_IN) {
                paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))//fixme 取RectF()下面的交集
            } else {
                paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST))//fixme 显示下面的。
            }
            // 矩形弧度
            val radian = floatArrayOf(left_top!!, left_top!!, right_top, right_top, right_bottom, right_bottom, left_bottom, left_bottom)
            //fixme  画矩形
            var rectF = RectF(0f + x + scrollX + leftMargin, 0f + y + scrollY + topMargin, w.toFloat() + x + scrollX - rightMargin, h.toFloat() + y + scrollY - bottomMargin)
            //KLoggerUtils.e("left:\t"+rectF.left+"\ttop:\t"+rectF.top+"\tright:\t"+rectF.right+"\tbottom:\t"+rectF.bottom)
            var path = Path()
            path.moveTo(0f + x + scrollX + leftMargin, 0f + y + scrollY + topMargin)
            path.addRoundRect(rectF, radian, Path.Direction.CW)
            path.close()
            if (all_radius > 0 || left_top > 0 || left_bottom > 0 || right_top > 0 || right_bottom > 0) {
                canvas.drawPath(path, paint)
            }
            /**
             * fixme 8.0;和9.0之后；圆角的矩形范围只包含圆角矩形内的范围；不再是整个矩形的范围。
             * fixme 以下修复了圆角无效的问题。
             * fixme 现在开不开硬件加速；都无所谓了。都支持圆角了。
             */
            if (isDST_IN) {
                if (all_radius > 0 || left_top > 0 || left_bottom > 0 || right_top > 0 || right_bottom > 0) {
                    paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
                    path.setFillType(Path.FillType.INVERSE_WINDING)//反转
                    canvas.drawPath(path, paint)
                    path.setFillType(Path.FillType.WINDING)//恢复正常
                    //fixme 兼容低部版切割不全的问题。9.0及以上系统正常。7.0及以下系统会有问题。以下就是解决7.0的问题的。
                    if (leftMargin > 0||x>0) {
                        var rectFLeft = RectF(0f, 0f, 0f + x + scrollX + leftMargin, h.toFloat())
                        canvas.drawRect(rectFLeft, paint)
                    }
                    if (topMargin > 0||y>0) {
                        var rectFTop = RectF(0f, 0f, w.toFloat(), 0f + y + scrollY + topMargin)
                        canvas.drawRect(rectFTop, paint)
                    }
                    if (rightMargin > 0) {
                        var rectFRight = RectF(w.toFloat() + x + scrollX - rightMargin, 0f, w.toFloat(), h.toFloat())
                        canvas.drawRect(rectFRight, paint)
                    }
                    if (bottomMargin > 0) {
                        var rectFBottom = RectF(0f, h.toFloat() + y + scrollY - bottomMargin, w.toFloat(), h.toFloat())
                        canvas.drawRect(rectFBottom, paint)
                    }
                }
            }

            //FIXME 有边框时
            if (strokeWidth > 0) {
                paint.strokeWidth = strokeWidth
                if (strokeColor != Color.TRANSPARENT) {
                    //fixme 边框大于0时，边框颜色不能为透明。不然无法显示出来。
                    //0是透明，颜色值是有负数的。
                    paint.color = strokeColor
                }
                //fixme 画矩形边框
                rectF = RectF(0f + strokeWidth / 2F + x + scrollX + leftMargin, 0f + strokeWidth / 2F + y + scrollY + topMargin, w.toFloat() - strokeWidth / 2F + x + scrollX - rightMargin, h.toFloat() - strokeWidth / 2F + y + scrollY - bottomMargin)
                path.reset()//fixme 重置
                path.moveTo(0f + strokeWidth / 2F + x + scrollX + leftMargin, 0f + strokeWidth / 2F + y + scrollY + topMargin)
                //fixme 路径填充矩形
                path.addRoundRect(rectF, radian, Path.Direction.CW)
                path.close()
                paint.style = Paint.Style.FILL_AND_STROKE
                if (isDST_IN) {
                    paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))//fixme 取RectF()下面的交集
                } else {
                    paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST))//fixme 显示下面的。
                }
                canvas.drawPath(path, paint)

                //fixme 画边框
                paint.style = Paint.Style.STROKE
                paint.setXfermode(null)//正常
                //边框颜色渐变，渐变颜色优先等级大于正常颜色。
                var linearGradient: LinearGradient? = null
                if (strokeGradientColors != null) {
                    if (strokeGradientOritation == ORIENTATION_HORIZONTAL) {
                        if (!isStrokeGradient) {
                            //水平不渐变
                            linearGradient = K0Widget.getNotLinearGradient(0f + x + scrollX + leftMargin, w.toFloat() + x + scrollX - rightMargin, strokeGradientColors!!, false)
                        }
                        //fixme 水平渐变
                        if (linearGradient == null) {
                            linearGradient = LinearGradient(0f + x + scrollX + leftMargin, h / 2f + scrollY, w.toFloat() + x + scrollX - rightMargin, h / 2f + scrollY, strokeGradientColors, null, Shader.TileMode.CLAMP)
                        }
                    } else {
                        if (!isStrokeGradient) {
                            //垂直不渐变
                            linearGradient = K0Widget.getNotLinearGradient(0f + y + scrollY + topMargin, h.toFloat() + y + scrollY - bottomMargin, strokeGradientColors!!, true)
                        }
                        //fixme 垂直渐变
                        if (linearGradient == null) {
                            linearGradient = LinearGradient(0f, 0f + y + scrollY + topMargin, 0f, h.toFloat() + y + scrollY - bottomMargin, strokeGradientColors, null, Shader.TileMode.CLAMP)
                        }
                    }
                } else {
                    if (!(strokeGradientStartColor == Color.TRANSPARENT && strokeGradientEndColor == Color.TRANSPARENT)) {
                        if (strokeGradientOritation == ORIENTATION_HORIZONTAL) {
                            //fixme 水平渐变
                            linearGradient = LinearGradient(0f + x + scrollX + leftMargin, h / 2f, w.toFloat() + x + scrollX - rightMargin, h / 2f, strokeGradientStartColor, strokeGradientEndColor, Shader.TileMode.CLAMP)
                        } else {
                            //fixme 垂直渐变
                            linearGradient = LinearGradient(0f, 0f + scrollY + topMargin, 0f, h.toFloat() + scrollY - bottomMargin, strokeGradientStartColor, strokeGradientEndColor, Shader.TileMode.CLAMP)
                        }
                    }
                }
                linearGradient?.let {
                    paint.setShader(linearGradient)
                }
                //虚线
                if (dashWidth > 0 && dashGap > 0) {
                    var dashPathEffect = DashPathEffect(floatArrayOf(dashWidth, dashGap), phase)
                    paint.setPathEffect(dashPathEffect)
                }
                canvas.drawPath(path, paint)
                paint.setPathEffect(null)

                //fixme 修复。去除边框之外的内容。
                if (isDST_IN) {
                    if (all_radius > 0 || left_top > 0 || left_bottom > 0 || right_top > 0 || right_bottom > 0) {
                        paint.style = Paint.Style.FILL_AND_STROKE
                        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
                        path.setFillType(Path.FillType.INVERSE_WINDING)
                        canvas.drawPath(path, paint)
                        path.setFillType(Path.FillType.WINDING)//恢复正常
                    }
                }

            }
        }
    }

}
package com.sdk.jbox2d.entity

import android.graphics.*
import cn.oi.klittle.era.exception.KCatchException
import cn.oi.klittle.era.utils.KGlideUtils
import cn.oi.klittle.era.utils.KLoggerUtils
import com.sdk.jbox2d.utils.KBodyUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.World
import java.lang.Exception

open class KBaseBody() {
    var body: Body? = null//fixme 刚体，body.position.x, body.position.y是刚体的中心坐标。
    var world: World? = null
    var bitmap: Bitmap? = null
    var isRecycledBitmap = true//是否销毁问题。默认销毁；在 destroy（）方法里。

    fun getBitmapFromAssets(path: String, overrideWidth: Int, overrideHeight: Int) {
        GlobalScope.async {
            bitmap = KGlideUtils.getBitmapFromAssets(path, overrideWidth, overrideHeight, 1f, true)
        }
    }

    fun getBitmapFromResouce(resID: Int, overrideWidth: Int, overrideHeight: Int) {
        GlobalScope.async {
            bitmap = KGlideUtils.getBitmapFromResouce(resID, overrideWidth, overrideHeight, 1f, true)
        }
    }

    fun getBitmapFromPath(path: String, overrideWidth: Int, overrideHeight: Int) {
        GlobalScope.async {
            bitmap = KGlideUtils.getBitmapFromPath(path, overrideWidth, overrideHeight, 1f, true)
        }
    }

    fun getBitmapFromUrl(url: String, overrideWidth: Int, overrideHeight: Int) {
        GlobalScope.async {
            bitmap = KGlideUtils.getBitmapFromUrl(url, overrideWidth, overrideHeight, null, null, true)
        }
    }

    //fixme 绘制位图；绘制成功，返回true；在子类draw（）方法中，优化绘制。
    protected fun drawBitmap(canvas: Canvas?, paint: Paint?): Boolean {
        if (canvas == null || paint == null) {
            return false
        }
        body?.let {
            var angle=it.angle//旋转弧度。Math.toDegrees（）弧度转角度。
            var body = it
            bitmap?.let {
                if (!it.isRecycled) {
                    //KLoggerUtils.e("旋转角度：\t"+angle+"\t"+Math.toDegrees(angle.toDouble()).toFloat(),true)
                    if (angle!=0f){
                        canvas.save()
                        canvas.rotate(Math.toDegrees(angle.toDouble()).toFloat(),body.position.x,body.position.y)//fixme 旋转角度。
                    }
                    //KLoggerUtils.e("it.width:\t"+it.width+"\tit.height:\t"+it.height,true)
                    canvas.drawBitmap(it, Rect(0, 0, it.width, it.height), RectF(body.position.x - it.width / 2, body.position.y - it.height / 2, body.position.x + it.width / 2, body.position.y + it.height / 2), paint)
                    if (angle!=0f){
                        canvas.restore()
                    }
                    return true//fixme 位图绘制成功。
                }
            }
        }
        return false
    }

    //fixme 设置刚体的位置
    fun setBodyPosition(x: Float, y: Float) {
        KBodyUtils.setBodyPosition(body, x, y)
    }

    /**
     * fixme 刚体移动
     * @param offsetX x的偏移量
     * @param offsetY y的偏移量
     */
    fun moveBodyPosition(offsetX: Float, offsetY: Float) {
        KBodyUtils.moveBodyPosition(body, offsetX, offsetY)
    }


    /**
     * fixme 给刚体设置速度。
     * @param vec2x x轴方向的速度（正负代表方向）
     * @param vec2y y轴的速度
     */
    fun setLinearVelocity(vec2x: Float, vec2y: Float) {
        KBodyUtils.setLinearVelocity(body, vec2x, vec2y)
    }

    //fixme 销毁刚体
    fun destroy() {
        try {
            bitmap?.let {
                if (!it.isRecycled && isRecycledBitmap) {
                    it.recycle()
                }
            }
            bitmap = null
            if (body != null) {
                world?.destroyBody(body)
            }
            world = null
            body = null
        } catch (e: Exception) {
            KLoggerUtils.e("刚体销毁异常：\t" + KCatchException.getExceptionMsg(e), true)
        }
    }
}
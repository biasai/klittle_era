package com.sdk.jbox2d.entity

import android.graphics.*
import com.sdk.jbox2d.utils.KBodyUtils
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.World

open class KBaseBody() {
    var body: Body? = null//fixme 刚体，body.position.x, body.position.y是刚体的中心坐标。
    var world: World? = null
    var bitmap: Bitmap? = null

    //fixme 绘制位图
    fun drawBitmap(canvas: Canvas?, paint: Paint?) {
        if (canvas == null || paint == null) {
            return
        }
        body?.let {
            var body = it
            bitmap?.let {
                if (!it.isRecycled) {
                    canvas.drawBitmap(it, Rect(0, 0, it.width, it.height), RectF(body.position.x-it.width/2, body.position.y-it.height/2, body.position.x+it.width/2, body.position.y+it.height/2), paint)
                }
            }
        }
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
        if (body != null) {
            world?.destroyBody(body)
        }
        world = null
        body = null
    }
}
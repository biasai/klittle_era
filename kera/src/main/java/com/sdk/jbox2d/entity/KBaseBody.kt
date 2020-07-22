package com.sdk.jbox2d.entity

import com.sdk.jbox2d.utils.KBodyUtils
import org.jbox2d.dynamics.Body

open class KBaseBody {
    var body: Body? = null//fixme 刚体，body.position.x, body.position.y是刚体的中心坐标。

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

}
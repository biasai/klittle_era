package com.sdk.jbox2d

import com.sdk.jbox2d.entity.KCircleBody
import com.sdk.jbox2d.entity.KPolygonBody
import com.sdk.jbox2d.utils.KBodyUtils
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.World

/**
 * 对刚体Body进行管理
 */
open class KBody {
    var world: World? = null//世界
    var bodyList: ArrayList<Body?>? = arrayListOf()//刚体集合

    /**
     * fixme 创建圆形刚体(物体)
     * @param x 圆心坐标位置
     * @param y
     * @param radius 半径
     * @param density 密度,如果为0；在世界world里面就是静止不动的。
     * @param friction 摩擦系数
     * @param restitution 归还能量率（即：碰撞之后，剩余能量率；0剩余能量为0;1剩余能量为百分百，即没有损耗）
     */
    open fun createCircleBody(x: Float, y: Float, radius: Float, density: Float = 7f, friction: Float = 0.8f, restitution: Float = 0.3f): KCircleBody? {
        var body = KBodyUtils.createCircleBody(world, x, y, radius, density = density, friction = friction, restitution = restitution)
        body?.body?.let {
            bodyList?.add(it)
        }
        return body
    }

    /**
     * fixme 创建多边形(矩形)刚体
     * @param x 中心坐标位置
     * @param y
     * @param width 宽度(以x中心坐标，左右宽度都为width)
     * @param height 高度(以y中心坐标，上下高度都为height)
     * @param density 密度,如果为0；在世界world里面就是静止不动的。
     * @param friction 摩擦系数
     * @param restitution 归还能量率（即：碰撞之后，剩余能量率；0剩余能量为0;1剩余能量为百分百，即没有损耗）
     */
    fun createPolygonBox(x: Float, y: Float, width: Float, height: Float, density: Float = 7f, friction: Float = 0.8f, restitution: Float = 0.3f): KPolygonBody? {
        var body = KBodyUtils.createPolygonBox(world, x, y, width, height, density = density, friction = friction, restitution = restitution)
        body?.body?.let {
            bodyList?.add(it)
        }
        return body
    }

    //fixme 设置刚体的位置
    fun setBodyPosition(body: Body?, x: Float, y: Float) {
        KBodyUtils.setBodyPosition(body, x, y)
    }

    /**
     * fixme 刚体移动
     * @param body 刚体
     * @param offsetX x的偏移量
     * @param offsetY y的偏移量
     */
    fun moveBodyPosition(body: Body?, offsetX: Float, offsetY: Float) {
        KBodyUtils.moveBodyPosition(body, offsetX, offsetY)
    }

    //fixme 销毁刚体
    fun destroyBody() {
        if (world != null) {
            bodyList?.forEach {
                if (it != null) {
                    world?.destroyBody(it)//清除所有刚体
                }
            }
        }
        bodyList?.clear()
    }

    //fixme 销毁
    fun destroy() {
        destroyBody()
        bodyList = null
        world = null
    }
}
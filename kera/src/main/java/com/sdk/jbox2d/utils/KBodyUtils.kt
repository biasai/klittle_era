package com.sdk.jbox2d.utils

import cn.oi.klittle.era.exception.KCatchException
import cn.oi.klittle.era.utils.KLoggerUtils
import com.sdk.jbox2d.entity.KCircleBody
import com.sdk.jbox2d.entity.KPolygonBody
//import org.jbox2d.collision.CircleDef
//import org.jbox2d.collision.PolygonDef
//import org.jbox2d.common.Vec2
//import org.jbox2d.dynamics.Body
//import org.jbox2d.dynamics.BodyDef
//import org.jbox2d.dynamics.World

/**
 * fixme 刚体工具类;测试发现刚体的最大创建数量是 2048；超过这个数量，body?.createShape（）会报错。
 */
object KBodyUtils {
//    /**
//     * fixme 创建圆形刚体(物体)
//     * @param world 世界
//     * @param x 圆心坐标位置
//     * @param y
//     * @param radius 半径
//     * @param density 密度,如果为0；在世界world里面就是静止不动的。fixme 密度
//     * @param friction 摩擦系数 fixme 摩擦力
//     * @param restitution 归还能量率（即：碰撞之后，剩余能量率；0剩余能量为0;1剩余能量为百分百，即没有损耗）fixme 弹力
//     */
//    open fun createCircleBody(world: World?, x: Float, y: Float, radius: Float, density: Float = 7f, friction: Float = 0.8f, restitution: Float = 0.3f): KCircleBody? {
//        if (world == null) {
//            return null
//        }
//        try {
//            val circleDef = CircleDef()
//            circleDef.radius = radius//半径
//            circleDef.density = density//fixme 密度,如果为0；在世界world里面就是静止不动的。
//            circleDef.friction = friction//摩擦系数
//            circleDef.restitution = restitution//fixme 归还能量率（即：碰撞之后，剩余能量率；0剩余能量为0;1剩余能量为百分百，即没有损耗）
//            val bodyDef = BodyDef()
//            bodyDef.position.set(x, y)//fixme 设置位置,是刚体的中心坐标位置.
//            var body = world?.createBody(bodyDef)//在世界创建刚体
//            body?.createShape(circleDef)//指定刚体形状
//            body?.setMassFromShapes() //从附加的形状计算质量属性，最后执行，必不可少
//            var circleBody = KCircleBody(radius)
//            circleBody.body = body
//            circleBody.world = world
//            return circleBody
//        } catch (e: Exception) {
//            KLoggerUtils.e("圆形刚体KCircleBody创建异常：\t" + KCatchException.getExceptionMsg(e), true)
//        }
//        return null
//    }
//
//    /**
//     * fixme 创建多边形(矩形)刚体
//     * @param world 世界
//     * @param x 中心坐标位置
//     * @param y
//     * @param width 宽度(以x中心坐标，fixme 左右宽度都为width;即实际长度就是两倍width的宽度。)
//     * @param height 高度(以y中心坐标，上下高度都为height)
//     * @param density 密度,如果为0；在世界world里面就是静止不动的。
//     * @param friction 摩擦系数
//     * @param restitution 归还能量率（即：碰撞之后，剩余能量率；0剩余能量为0;1剩余能量为百分百，即没有损耗）
//     */
//    fun createPolygonBox(world: World?, x: Float, y: Float, width: Float, height: Float, density: Float = 7f, friction: Float = 0.8f, restitution: Float = 0.3f): KPolygonBody? {
//        if (world == null) {
//            return null
//        }
//        try {
//            val shape = PolygonDef()
//            shape.density = density//fixme 密度,如果为0；在世界world里面就是静止不动的。
//            shape.friction = friction//摩擦系数
//            shape.restitution = restitution//fixme 归还能量率（即：碰撞之后，剩余能量率；0剩余能量为0;1剩余能量为百分百，即没有损耗）
//            //fixme widht:宽度(以x中心坐标，左右宽度都为width)
//            //fixme height:高度(以y中心坐标，上下高度都为height)
//            shape.setAsBox(width, height)
//            val bodyDef = BodyDef()
//            bodyDef.position.set(x, y)//fixme 设置位置,是刚体的中心坐标位置.
//            var body = world?.createBody(bodyDef)
//            body?.createShape(shape)
//            body?.setMassFromShapes()
//            var polygonBody = KPolygonBody(width, height)
//            polygonBody.body = body
//            polygonBody.world = world
//            return polygonBody
//        }catch (e:Exception){
//            KLoggerUtils.e("多边形刚体KPolygonBody创建异常：\t" + KCatchException.getExceptionMsg(e), true)
//        }
//        return null
//    }
//
//    //fixme 设置刚体的位置
//    fun setBodyPosition(body: Body?, x: Float, y: Float) {
//        if (body == null) {
//            return
//        }
//        var vec = body?.getPosition();
//        vec?.let {
//            it.x = x;
//            it.y = y
//        }
//        //body?.getPosition()
//        body?.setXForm(vec, 0f);
//    }
//
//    /**
//     * fixme 刚体移动
//     * @param body 刚体
//     * @param offsetX x的偏移量
//     * @param offsetY y的偏移量
//     */
//    fun moveBodyPosition(body: Body?, offsetX: Float, offsetY: Float) {
//        if (body == null) {
//            return
//        }
//        var vec = body?.getPosition();
//        vec?.let {
//            it.x += offsetX;
//            it.y += offsetY
//        }
//        //body?.getPosition()
//        body?.setXForm(vec, 0f);
//    }
//
//    /**
//     * fixme 给刚体设置速度。
//     * @param body 刚体
//     * @param vec2x x轴方向的速度（正负代表方向）
//     * @param vec2y y轴的速度
//     */
//    fun setLinearVelocity(body: Body?, vec2x: Float, vec2y: Float) {
//        body?.let {
//            var vec = Vec2(vec2x, vec2y)
//            it.wakeUp()//唤醒刚体。不然速度不会生效。
//            it.linearVelocity = vec
//        }
//    }

}
package com.sdk.jbox2d

//import org.jbox2d.collision.AABB
//import org.jbox2d.common.Vec2
//import org.jbox2d.dynamics.World

/**
 * 世界管理类
 */
open class KWorld() {
//    var world: World? = null//fixme 世界
//    var aabb: AABB? = null//fixme 可以理解为世界的边界盒子
//    var gravity: Vec2? = null//fixme 重力向量
//    var doSleep: Boolean = true//fixme 是否节省性能,当没有碰撞时休眠
//    var body: KBody? = null//fixme 刚体
//
//
//    //fixme 碰撞世界：(在世界world创建之前设置才有效)
//    fun aabb(block: (AABB.() -> Unit)? = null): KWorld {
//        if (aabb == null) {
//            aabb = AABB()
//            aabb?.let {
//                //fixme 边界值，注意这里使用的是现实世界的单位
//                //fixme 当刚体position中心坐标到达边界时，刚体就会禁止不动。（左上右下，四个面，只要到达边界就会禁止，物理引擎将失效）会被冻结并停止模拟
//                it.lowerBound.set(-100.0f, -100.0f);//fixme 最小边界（上边界，以左上角为起点[0,0]）
//                it.upperBound.set(1000.0f, 1000.0f);//fixme 最大边界（下边界，即右下角）
//            }
//        }
//        if (block != null) {
//            aabb?.let {
//                block(it)
//            }
//        }
//        return this
//    }
//
//    //fixme 重力向量：(在世界world创建之前设置才有效)
//    fun gravity(block: (Vec2.() -> Unit)? = null): KWorld {
//        if (gravity == null) {
//            gravity = Vec2(0.0f, 100.0f)
//        }
//        if (block != null) {
//            gravity?.let {
//                block(it)
//            }
//        }
//        return this
//    }
//
//    //fixme 重新创建世界
//    fun createWorld(): KWorld {
//        if (aabb == null) {
//            aabb(null)
//        }
//        if (gravity == null) {
//            gravity(null)
//        }
//        body?.destroyBody()//fixme 释放之前的刚体。
//        //fixme 一旦实例化世界，所有的属性aabb,gravity等都不能再改变。
//        //fixme 属性改变了也无效。除非重新实例化。
//        world = World(aabb, gravity, doSleep);
//        body?.world = world//fixme 实时更新world
//        return this
//    }
//
//    //fixme 刚体：(世界world创建之后，再创建刚体)
//    fun body(block: (KBody.() -> Unit)? = null): KWorld {
//        if (body == null && world != null) {
//            body = KBody()
//        }
//        body?.world = world//fixme 实时更新世界
//        if (block != null) {
//            body?.let {
//                block(it)
//            }
//        }
//        return this
//    }
//
//    //fixme 销毁
//    fun destroy() {
//        aabb = null
//        gravity = null
//        body?.destroy()
//        body = null
//        world = null
//    }
}
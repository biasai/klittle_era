package com.sdk.jbox2d

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.exception.KCatchException
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.widget.compat.KView
import org.jbox2d.collision.AABB
import org.jbox2d.collision.CircleDef
import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.Body
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.World


/**
 * fixme JBox2d物理引擎（官网下载地址：https://sourceforge.net/projects/jbox2d/）;http://www.jbox2d.org/
 * 参考案例：
 * https://blog.csdn.net/cre2017/article/details/81268995
 * https://blog.csdn.net/htttw/article/details/7600277
 */
open class KJBox2dView : KView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//开启硬件加速,不然圆角没有效果
        clearBackground()
        //去除按钮原有阴影
        clearButonShadow()
        //fixme 初始化创建碰撞世界.
        world { }
    }

    override fun draw(canvas: Canvas?) {
        try {
            super.draw(canvas)
            world?.let {
                it?.step(timeStep, iterations) //fixme 开始物理模拟,必须实时调用,不然没有效果
                invalidate()//fixme 不停的绘制
            }
        } catch (e: Exception) {
            KLoggerUtils.e("Jbox2d异常:\t" + KCatchException.getExceptionMsg(e), true)
        }
    }

    var timeStep = 1f / 60f //模拟的的频率
    var iterations = 30//迭代越大，模拟约精确，但性能越低
    var world: World? = null//fixme 世界
    fun world(block: World.() -> Unit): KJBox2dView {
        if (world == null) {
            if (aabb == null) {
                aabb { }
            }
            //重力向量
            val gravity = Vec2(0.0f, 200.0f)
            //是否节省性能,当没有碰撞时休眠
            val doSleep = true
            world = World(aabb, gravity, doSleep);
        }
        world?.let {
            block(it)
        }
        return this
    }

    var aabb: AABB? = null//fixme 可以理解为世界的边界盒子
    fun aabb(block: AABB.() -> Unit): KJBox2dView {
        if (aabb == null) {
            aabb = AABB()
            aabb?.let {
                //fixme 边界值，注意这里使用的是现实世界的单位
                it.lowerBound.set(-100.0f, -100.0f);//fixme 最小边界（下边界）
                it.upperBound.set(1000.0f, 1000.0f);//fixme 最大边界（上边界）
            }
        }
        aabb?.let {
            block(it)
        }
        return this
    }

    /**
     * fixme 创建圆形刚体(物体)
     * @param x 圆心坐标位置
     * @param y
     * @param radius 半径
     */
    open fun createCircleBody(x: Float, y: Float, radius: Float): Body? {
        if (world == null) {
            world { }
        }
        var circleDef = CircleDef()
        circleDef.density = 7f//fixme 密度,如果为0；在世界world里面就是静止不动的。
        circleDef.friction = 0.8f//摩擦系数
        circleDef.radius = radius//半径
        circleDef.restitution = 0.3f//能量损失率
        var bodyDef = BodyDef()
        bodyDef.position.set(x, y)//fixme 设置位置,是刚体的中心坐标位置.
        var body = world?.createBody(bodyDef)//在世界创建刚体
        body?.createShape(circleDef)//指定刚体形状
        body?.setMassFromShapes() //从附加的形状计算质量属性，最后执行，必不可少
        return body
    }

}
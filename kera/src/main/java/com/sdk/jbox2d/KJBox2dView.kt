package com.sdk.jbox2d

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.exception.KCatchException
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.widget.compat.KView

//                fixme 简单调用案例：
//                kJBox2dView {
//                    timeStep = 1f / 60f //模拟的的频率（1f / 60f 代表刷新频率为60帧每秒）
//                    iterations = 10//迭代越大，模拟越精确，但性能越低（正常15左右即可）
//                    var circleBody: KCircleBody? = null//fixme 圆形刚体
//                    var circleBody2: KCircleBody? = null//fixme 圆形刚体
//                    var polygonBox: KPolygonBody? = null//fixme 多边形（矩形）刚体
//
//                    world {
//                        //fixme 碰撞盒子边界：createWorld()之前调用
//                        aabb {
//                            //fixme 当刚体position中心坐标到达边界时，刚体就会禁止不动。（左上右下，四个面，只要到达边界就会禁止，物理引擎将失效）会被冻结并停止模拟
//                            lowerBound.set(0f, 0f);//fixme 最小边界（上边界，以左上角为起点[0,0]）
//                            upperBound.set(1000.0f, 10000.0f);//fixme 最大边界（下边界，即右下角）
//                        }
//                        //fixme 重力向量：createWorld()之前调用
//                        gravity {
//                            set(0.0f, 100.0f)
//                        }
//                        createWorld()//fixme 创建世界(比不可少，必须手动创建)
//                        //fixme 创建刚体：createWorld()之后调用
//                        body {
//                            //fixme 创建圆形刚体
//                            circleBody = createCircleBody(kpx.x(100f), 0f, kpx.x(50f), density = 100000f, friction = 0.8f, restitution = 1f)?.apply {
//                                color = Color.RED
//                                style = Paint.Style.STROKE
//                                strokeWidth = kpx.x(2f)
//                                dashWidth = kpx.x(15f)
//                                dashGap = kpx.x(10f)
//                                dashSpeed = kpx.x(2f)
//                            }
//                            circleBody2 = createCircleBody(kpx.x(300F), 0F, kpx.x(50f), density = 100f, friction = 110.8f, restitution = 0.9f)//fixme 创建圆形刚体
//                            polygonBox = createPolygonBox(kpx.x(100f), kpx.x(800f), kpx.x(500f), kpx.x(100f), density = 0f)?.apply {
//                                color = Color.BLUE
//                            }
//                        }
//                    }
//                    draw { canvas, paint ->
//                        //fixme 绘制圆形刚体
//                        circleBody?.drawCircle(canvas, paint)
//                        circleBody2?.drawCircle(canvas, paint)
//                        //fixme 绘制矩形刚体
//                        polygonBox?.drawRect(canvas, paint)
//                    }
//                    //fixme 销毁
//                    destroy {
//                        circleBody = null
//                        circleBody2=null
//                        polygonBox=null
//                    }
//                }.lparams {
//                    width = matchParent
//                    height = matchParent
//                }

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
    }

    override fun draw(canvas: Canvas?) {
        if (!isOnDestroy) {
            try {
                super.draw(canvas)
                world?.let {
                    it?.world?.let {
                        it?.step(timeStep, iterations) //fixme 开始物理模拟,必须实时调用,不然没有效果
                        invalidate()//fixme 不停的绘制
                    }
                }
            } catch (e: Exception) {
                KLoggerUtils.e("Jbox2d异常:\t" + KCatchException.getExceptionMsg(e), true)
            }
        }
    }

    var timeStep = 1f / 60f //模拟的的频率（1f / 60f 代表刷新频率为60帧每秒）
    var iterations = 15//迭代越大，模拟越精确，但性能越低（正常15左右即可）
    var world: KWorld? = null//fixme 世界
    fun world(block: KWorld.() -> Unit): KJBox2dView {
        if (world == null) {
            world = KWorld();
        }
        world?.let {
            block(it)
        }
        postInvalidate()//界面刷新
        return this
    }

    //fixme 销毁
    override fun onDestroy() {
        super.onDestroy()
        world?.destroy()
        world = null
    }

}
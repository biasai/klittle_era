package cn.oi.klittle.era.widget

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.base.KBaseView
import cn.oi.klittle.era.utils.KScrimUtil

/**
 * fixme 颜色渐变视图;添加 gradientColor()方法，使用KScrimUtil实现更柔和的渐变色。
 * fixme 在低版本上（5.1及以下）"#00F2F2F2" 到 "#F2F2F2" 变化会比较圆润。但是 Color.TRANSPARENT("#00000000") 到 "#F2F2F2"就不圆润了。要注意。
 */
open class KGradientView : KBaseView {

    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(viewGroup: ViewGroup, HARDWARE: Boolean) : super(viewGroup.context) {
        if (HARDWARE) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//开启硬件加速
    }

    /**
     * fixme 使用KScrimUtil实现更柔和的颜色透明渐变
     * @param color 初始颜色值（最后会变成透明色）
     * @param numStops 渐变层数（越大，渐变色越柔和）
     * @param gravity 初始渐变方向
     */
    open fun gradientColor(color: Int, numStops: Int = 8, gravity: Int = Gravity.TOP) {
        //要求版本号大于16
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN && color != null) {
            horizontalColors = null
            verticalColors = null
            setBackground(
                    KScrimUtil.makeCubicGradientScrimDrawable(
                            color, //顏色
                            numStops, //漸層數
                            gravity)); //起始方向
        }
    }

    open fun gradientColor(color: String, numStops: Int = 8, gravity: Int = Gravity.TOP) {
        gradientColor(Color.parseColor(color), numStops, gravity)
    }

    //fixme 水平渐变颜色数组值【均匀渐变】
    var horizontalColors: IntArray? = null

    open fun horizontalColors(vararg color: Int) {
        verticalColors = null
        horizontalColors = color
    }

    open fun horizontalColors(vararg color: String) {
        verticalColors = null
        horizontalColors = IntArray(color.size)
        horizontalColors?.apply {
            if (color.size > 0) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            }
        }
    }

    //fixme 垂直渐变颜色数组值【均匀】
    var verticalColors: IntArray? = null

    open fun verticalColors(vararg color: Int) {
        horizontalColors = null
        verticalColors = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun verticalColors(vararg color: String) {
        horizontalColors = null
        verticalColors = IntArray(color.size)
        verticalColors?.apply {
            if (color.size > 0) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            }
        }
    }

    var top_color = Color.TRANSPARENT//fixme 上半部分颜色
    open fun top_color(top_color: Int) {
        this.top_color = top_color
    }

    open fun top_color(top_color: String) {
        this.top_color = Color.parseColor(top_color)
    }

    var bottom_color = Color.TRANSPARENT//fixme 下半部分颜色
    open fun bottom_color(bottom_color: Int) {
        this.bottom_color = bottom_color
    }

    open fun bottom_color(bottom_color: String) {
        this.bottom_color = Color.parseColor(bottom_color)
    }

    var left_color = Color.TRANSPARENT//fixme 左半部分颜色
    open fun left_color(left_color: Int) {
        this.left_color = left_color
    }

    open fun left_color(left_color: String) {
        this.left_color = Color.parseColor(left_color)
    }

    var right_color = Color.TRANSPARENT//fixme 右半部分颜色
    open fun right_color(right_color: Int) {
        this.right_color = right_color
    }

    open fun right_color(right_color: String) {
        this.right_color = Color.parseColor(right_color)
    }

    var left_top_color = Color.TRANSPARENT//fixme 左上角部分颜色
    open fun left_top_color(left_top_color: Int) {
        this.left_top_color = left_top_color
    }

    open fun left_top_color(left_top_color: String) {
        this.left_top_color = Color.parseColor(left_top_color)
    }

    var right_top_color = Color.TRANSPARENT//fixme 右上角部分颜色
    open fun right_top_color(right_top_color: Int) {
        this.right_top_color = right_top_color
    }

    open fun right_top_color(right_top_color: String) {
        this.right_top_color = Color.parseColor(right_top_color)
    }

    var left_bottom_color = Color.TRANSPARENT//fixme 左下角部分颜色
    open fun left_bottom_color(left_bottom_color: Int) {
        this.left_bottom_color = left_bottom_color
    }

    open fun left_bottom_color(left_bottom_color: String) {
        this.left_bottom_color = Color.parseColor(left_bottom_color)
    }

    var right_bottom_color = Color.TRANSPARENT//fixme 右下角部分颜色
    open fun right_bottom_color(right_bottom_color: Int) {
        this.right_bottom_color = right_bottom_color
    }

    open fun right_bottom_color(right_bottom_color: String) {
        this.right_bottom_color = Color.parseColor(right_bottom_color)
    }

    override fun draw2(canvas: Canvas, paint: Paint) {
        super.draw2(canvas, paint)
        canvas.apply {
            paint.isAntiAlias = true
            paint.isDither = true
            paint.style = Paint.Style.FILL_AND_STROKE

            //上半部分颜色
            if (top_color != Color.TRANSPARENT) {
                paint.color = top_color
                drawRect(RectF(0f, 0f, width.toFloat(), height / 2f), paint)
            }

            //下半部分颜色
            if (bottom_color != Color.TRANSPARENT) {
                paint.color = bottom_color
                drawRect(RectF(0f, height / 2f, width.toFloat(), height.toFloat()), paint)
            }


            //左半部分颜色
            if (left_color != Color.TRANSPARENT) {
                paint.color = left_color
                drawRect(RectF(0f, 0f, width.toFloat() / 2, height.toFloat()), paint)
            }

            //右半部分颜色
            if (right_color != Color.TRANSPARENT) {
                paint.color = right_color
                drawRect(RectF(width / 2f, 0f, width.toFloat(), height.toFloat()), paint)
            }

            //左上角部分颜色
            if (left_top_color != Color.TRANSPARENT) {
                paint.color = left_top_color
                drawRect(RectF(0f, 0f, width.toFloat() / 2, height.toFloat() / 2), paint)
            }

            //右上角部分颜色
            if (right_top_color != Color.TRANSPARENT) {
                paint.color = right_top_color
                drawRect(RectF(width / 2f, 0f, width.toFloat(), height.toFloat() / 2), paint)
            }

            //左下角部分颜色
            if (left_bottom_color != Color.TRANSPARENT) {
                paint.color = left_bottom_color
                drawRect(RectF(0f, height / 2f, width.toFloat() / 2, height.toFloat()), paint)
            }

            //右下角部分颜色
            if (right_bottom_color != Color.TRANSPARENT) {
                paint.color = right_bottom_color
                drawRect(RectF(width / 2f, height / 2f, width.toFloat(), height.toFloat()), paint)
            }

            //水平渐变
            horizontalColors?.let {
                if (it.size > 1) {
                    //fixme 渐变颜色数组必须大于等于2，不然异常
                    var shader = LinearGradient(0f, 0f, width.toFloat(), 0f, it, null, Shader.TileMode.MIRROR)
                    paint.setShader(shader)
                } else if (it.size == 1) {
                    paint.color = it[0]
                }
                drawPaint(paint)
            }

            //fixme 水平渐变 和 垂直渐变 效果会叠加。垂直覆盖在水平的上面。

            //垂直渐变
            verticalColors?.let {
                if (it.size > 1) {
                    var shader = LinearGradient(0f, 0f, 0f, height.toFloat(), it, null, Shader.TileMode.MIRROR)
                    paint.setShader(shader)
                } else if (it.size == 1) {
                    paint.color = it[0]
                }
                drawPaint(paint)
            }
        }
    }

}
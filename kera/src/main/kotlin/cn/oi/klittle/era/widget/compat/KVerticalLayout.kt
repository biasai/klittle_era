package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import cn.oi.klittle.era.exception.KCatchException
import cn.oi.klittle.era.utils.KLoggerUtils
import org.jetbrains.anko.backgroundDrawable
import java.lang.Exception

//            fixme 使用案例：
//            //kverticalLayout?.inner?.heightToggle() fixme 调用heightToggle()高度变化方法。
//            var kverticalLayout=KVerticalLayout(this).apply {
//                backgroundColor = Color.GREEN
//                gravity=Gravity.CENTER
//                //inner fixme 继承K7RadiusWidget，集成了部分方法。
//                inner {
//                    radius {
//                        all_radius(kpx.x(200f))//fixme 圆角
//                    }
//                }
//            }.lparams {
//                width= matchParent
//                height=kpx.x(500)
//            }

/**
 * Created by 彭治铭 on 2018/11/24.
 * 自所以用线性布局，是因为 .lparams{}默认就是线性布局，方便。
 */
open class KVerticalLayout : LinearLayout {

    /**
     * fixme 设置是否可用(可以设置所有子控件哦)
     * @param isEnabled true 可用(能够接收点击触摸等事件);false 不可用(点击触摸不会有反应)
     * @param isChild true 所有子控件设置成同样的状态
     */
    open fun setEnabled(isEnabled: Boolean, isChild: Boolean, viewGroup: ViewGroup = this) {
        try {
            viewGroup?.let {
                it.isEnabled = isEnabled
                if (isChild) {
                    if (it.childCount > 0) {
                        for (i in 0..it.childCount - 1) {
                            it.getChildAt(i)?.let {
                                it.isEnabled = isEnabled
                                if (it is ViewGroup) {
                                    if (it.childCount > 0) {
                                        setEnabled(isEnabled, isChild, it)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//开启硬件加速,不然圆角没有效果
        orientation = VERTICAL//默认垂直方向
        isClickable = true//具备点击触摸能力。不然无法监听手指离开和滑动
        //fixme 以下两个属性，防止布局里面的文本框焦点错乱异常。
        isFocusable = true
        //isFocusableInTouchMode = true//fixme 注意，这个最好还是不要设置；设置之后，第一次点击可能无效(第二次点击才有效)
        //事件重写，主要用于视图刷新（防止他不刷新。）。
        onFocusChange { hasFocus -> }
        onHover { v, event -> false }
    }

    private var onFocusChangeList = mutableListOf<((hasFocus: Boolean) -> Unit)?>()
    //fixme 重写聚焦事件,会覆盖之前的聚焦事件
    open fun onFocusChange(callbak: (hasFocus: Boolean) -> Unit) {
        onFocusChangeList?.clear()
        onFocusChangeList?.add(callbak)
        setOnFocusChangeListener { v, hasFocus ->
            if (v == this) {
                for (i in onFocusChangeList) {
                    i?.let {
                        it(hasFocus)
                    }
                }
                invalidate()//聚焦改变时刷新视图
            }
        }
    }

    //fixme 添加聚焦事件，不会覆盖之前的聚焦事件。
    open fun addFocusChange(callbak: (hasFocus: Boolean) -> Unit) {
        onFocusChangeList?.add(callbak)
        setOnFocusChangeListener { v, hasFocus ->
            if (v == this) {
                for (i in onFocusChangeList) {
                    i?.let {
                        it(hasFocus)
                    }
                }
                invalidate()//聚焦改变时刷新视图
            }
        }
    }

    //fixme 重写鼠标悬浮事件
    open fun onHover(callbak: (v: View, event: MotionEvent) -> Boolean) {
        setOnHoverListener { v, event ->
            var b = callbak(v, event)
            invalidate()
            b
        }
    }


    //放到这里不会和子view冲突，不要放到dispatchTouchEvent()
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var b = super.onTouchEvent(event)
        //防止点击事件冲突。所以。一定要放到super()后面。
        event?.let {
            when (it.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    inner?.dispatchDown(it)
                    isPressed = true
                    invalidate()//视图刷新。
                }
                MotionEvent.ACTION_MOVE -> {
                    inner?.dispatchMove(it)
                    isPressed = true
                    //fixme 触摸时，不刷新。防止卡顿
                }
                MotionEvent.ACTION_UP -> {
                    isPressed = false
                    inner?.dispatchUp(it)
                    invalidate()
                }
                MotionEvent.ACTION_CANCEL -> {
                    //其他异常
                    isPressed = false
                    invalidate()
                }
            }
        }
        return b
    }

    //内部布局，实现圆角等属性。
    var inner: K7RadiusWidget? = K7RadiusWidget(context)

    //内部方法
    fun inner(block: K7RadiusWidget.() -> Unit) {
        inner?.viewGroup = this
        inner?.autoView = this
        inner?.paramView = this
        inner?.let {
            block(it)
        }
        //视图刷新
        invalidate()
        requestLayout()
    }

    /**
     * 有 2 的都是方法重写，交给子类去写。
     * 没有 2 的都是回调函数。
     */

    //fixme 自定义画布()，后画，会显示在前面,交给调用者去实现
    open var drawBehind: ((canvas: Canvas, paint: Paint) -> Unit)? = null
    //fixme 自定义画布()，先画，会显示在后面,交给调用者去实现
    open var drawFront: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //fixme 自定义画布()，最先画，会显示在最后面,交给子类去实现(一般用于实现背景)
    protected open fun draw2First(canvas: Canvas, paint: Paint) {}

    //fixme 自定义画布()，最后画，会显示在最前面,交给子类去实现(一般用于实现圆角矩形)
    protected open fun draw2Last(canvas: Canvas, paint: Paint) {}


    //fixme 自定义画布，根据需求。自主实现
    private var draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //fixme 自定义，重新绘图(就在super.draw(canvas)的后面执行。)
    open fun draw(draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null) {
        this.draw = draw
    }

    //fixme 什么都不做，交给子类去实现绘图
    //fixme 之所以会有这个方法。是为了保证自定义的 draw和onDraw的执行顺序。始终是在最后。
    protected open fun draw2(canvas: Canvas, paint: Paint) {}

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        try {
            super.onMeasure(widthSpec, heightSpec)
        } catch (e: java.lang.Exception) {
            KLoggerUtils.e("自定义KVerticalLayout onMeasure异常：\t" + KCatchException.getExceptionMsg(e),isLogEnable = true)
        }
    }

    override fun draw(canvas: Canvas?) {
        try {
            super.draw(canvas)
        } catch (e: java.lang.Exception) {
            KLoggerUtils.e("自定义KVerticalLayout draw异常：\t" + KCatchException.getExceptionMsg(e),isLogEnable = true)
        }

    }

    //fixme 必须在dispatchDraw()里面画才有效，在draw()里面无效。
    //fixme dispatchDraw()在viewgroup背景的上面哦。
    override fun dispatchDraw(canvas: Canvas?) {
        try {
            if (inner != null) {
                canvas?.let {
                    inner?.viewGroup = this
                    //状态
                    inner?.drawState(this)
                    draw2First(canvas, inner!!.resetPaint())
                    //画自定义背景图片
                    inner?.autoView = this
                    inner?.drawAuto(canvas, inner!!.resetPaint(), this)
                    //画圆角背景
                    inner?.drawBg(canvas, inner!!.resetPaint(), this)
                    drawFront?.let {
                        it(canvas, inner!!.resetPaint())
                    }
                    super.dispatchDraw(canvas)//画子View
                    draw2(canvas, inner!!.resetPaint())
                    draw?.let {
                        it(canvas, inner!!.resetPaint())
                    }
                    drawBehind?.let {
                        it(canvas, inner!!.resetPaint())
                    }
                    //画左上角的三角形
                    inner?.drawTriangle(canvas, inner!!.resetPaint(), this)
                    //画水波纹
                    inner?.drawWaterRipple(canvas)
                    //画圆角
                    inner?.drawRadius(canvas, this)
                    draw2Last(canvas, inner!!.resetPaint())
                    //组件大小
                    inner?.paramView = this
                    inner?.drawParam(canvas, this)
                }
            }
        } catch (e: Exception) {
            KLoggerUtils.e("自定义KVerticalLayout dispatchDraw异常：\t" + KCatchException.getExceptionMsg(e),isLogEnable = true)
        }

    }

    open fun onDestroy() {
        try {
            try {
                backgroundDrawable = null
                if (Build.VERSION.SDK_INT >= 16) {
                    background = null
                }
                setOnFocusChangeListener(null)
                setOnClickListener(null)
                setOnLongClickListener(null)//fixme 长按事件销毁。
                setOnTouchListener(null)
                setOnFocusChangeListener(null)
                //交给KTextView去清除;文本清除之前，先清除文本变化回调。
                //removeTextChangedListener(textWatcher)
                //setText(null)

                clearAnimation()
                clearFocus()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            draw = null
            drawBehind = null
            drawFront = null
            inner?.onDestroy()
            inner = null
            onFocusChangeList?.clear()
            onDetachedFromWindow()
            destroyDrawingCache()
        } catch (e: Exception) {
            e.printStackTrace()
            KLoggerUtils.e("销毁异常:\t" + e.message,isLogEnable = true)
        }
    }

}
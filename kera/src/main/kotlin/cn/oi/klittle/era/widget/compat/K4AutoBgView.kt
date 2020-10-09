package cn.oi.klittle.era.widget.compat

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import cn.oi.klittle.era.base.KBaseApplication
import cn.oi.klittle.era.entity.widget.compat.KAutoBgEntity
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.entity.widget.KBall
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.utils.KVibratorUtils

/**
 * 四：自定义背景图片相关。 fixme 还有抖动动画 jitter()；位图抖动动画autoBg_jitter()
 */
//fixme 调用案例
//                autoBg {
//                    width=kpx.x(500)//fixme 宽和高最好先配置；然后再加载图片
//                    height=kpx.x(300)
//                    isFill=true
//                    isAutoRight=true//右对齐；isAutoCenter为false时才有效。
//                    var url="https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1541996861818&di=5a9b356b9d0e491a95fe3458d5c784aa&imgtype=0&src=http%3A%2F%2Fwx2.sinaimg.cn%2Flarge%2F70c688f7gy1fwaevylwhsj20l60bxqq7.jpg"
//                    autoBgFromUrl(url){
//                        //fixme 最好等常态图片加载完成了，再配置其他状态，防止位图为空。因为其他状态的图片是复制常态的。如果图片不同。就无所谓了。
//                        autoBg_press {
//                            alpha=99
//                        }
//                    }
//                }

//fixme 调用案例二；添加了图片点击事件。
//                autoBg {
//                    width = kpx.x(63)
//                    height = width
//                    autoBg(R.mipmap.ohx)
//                    isAutoCenter = false
//                    isAutoRight = true//右对齐；isAutoCenter为false时才有效。
//                    autoRightPadding = kpx.x(12f)
//                    autoTopPadding = kpx.x(12f)
//                }
//                autoBg_press {
//                    //fixme 位图点击时候；优先级在普通点击事件之前。执行了图片点击事件；就不会在执行普通点击事件。
//                    //fixme 注意；点击事件要(最好)写在按下状态里。因为点击触发的按下状态。不然不会执行。（有按下状态时要写在按下状态里。没有就写在正常状态里。）
//                    onClickCallback {
//                        KToast.showInfo("图片按下点击事情2")
//                    }
//                }
//                //fixme 普通点击事件；必不可少(不然不会执行图片点击事件)；因为图片点击事件是在普通点击事件之前做的拦截。
//                //fixme 现在可以不用手动填加点击事件了,onClickCallback点击事件会自动判断,如果没有点击事件,会主动加上
//                onClick {
//                    KToast.show("普通点击事情")
//                }

//fixme 调用案例三；保证图片清晰不会糊掉。（图片之所以会糊掉就是太小了。或本身像素就不高。）
//fixme 测试发现，如果图片本身很糊的话，把图片压缩放大，再进行缩放处理，也能解决图片糊掉的问题。
//fixme 现在画布Canvas设置了画布抗锯齿效果，现在图片基本不会糊掉了。
//                        autoBg {
//                            width = px.x(90)
//                            height = width
//                            isRecycle=true
//                            autoBg(R.drawable.ic_unchecked_bwg)//fixme  会根据width和height对图片进行压缩处理。
//                            //fixme 再设置width和height;画布显示的时候会根据宽和高对图片进行缩放处理。
//                            width = px.x(45)
//                            height = width
//
//                            //fixme 即压缩的时候把图片宽和高设置的大一点(压缩处理)；压缩之后再把宽和高设置的小一定(缩放处理)。这样保证图片清晰；不会糊掉。亲测有效。
//                        }
//                        autoBg_selected {
//                            width = px.x(90)
//                            height = width
//                            isCompress=true //fixme 对图片进行压缩处理，默认就是true
//                            isRecycle=true //fixme 压缩图片之后；是否对原图进行释放。默认true释放。
//                            autoBg(R.drawable.ic_checked_bwg)//fixme 根据widht和height会对图片压缩处理（获取图片的时候）
//                            width = px.x(45)//fixme 缩放处理（图片获取完成之后，就不会在进行压缩处理，显示的是只会进行缩放处理）
//                            height = width//fixme 显示的时候，Canvas画布根据宽和高对位图进行缩放处理（不会压缩）
//                        }

//                        fixme ball() //两个小球动画。autoBg图片为空的时候，会显示小球动画效果。一般加载网络图片的时候。才用。

//                        fixme 如果一个图标被多个控件引用，且尺寸不一样；为防止异常，这时 isRecycle=false 不要释放位图原图

//fixme autoBg自定义图片共有三张。分别为 autoBg，autoBg2（在autoBg的上面显示）和autoBg3（在autoBg2的上面显示）。三张自定义位图都有不同的状态的。

open class K4AutoBgView : K3BorderWidget {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {
        onClickInit()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        onClickInit()
    }

    private var viewBitmap: Bitmap? = null//控件本身视图位图

    /**
     * 获取控件本身的位图
     * @param isRefresh 是否强制刷新；true每次都获取最新的；false 会返回旧的(fixme 默认返回旧的)。
     */
    fun getViewBitmap(isRefresh: Boolean = false): Bitmap? {
        if (isRefresh || viewBitmap == null || viewBitmap!!.isRecycled) {
            recycleViewBitmap()
            var view: View? = this
            if (viewGroup != null) {
                view = viewGroup//兼容组件
            }
            if (view != null) {
                viewBitmap = KBaseApplication.snapShotView(view)//fixme 内部调用的是 原生 view.getDrawingCache()方法
            }
            view = null
        }
        return viewBitmap
    }

    //释放位图
    fun recycleViewBitmap() {
        viewBitmap?.let {
            if (!it.isRecycled) {
                it?.recycle()//释放原图
            }
        }
        viewBitmap = null
    }

    //自定义图片点击事件初始化。
    private fun onClickInit() {
        mOnClickCallback {
            var isDeal = false//是否处理，默认没有处理
            //fixme 第一张图片点击事件
            autoBgModel?.let {
                if (it.onClickCallback != null && it.isDraw) {
                    if (it.isContains(pointDownX, pointDownY)) {
                        it.onClickCallback?.let {
                            it()//图片点击事件
                            isDeal = true//已经处理
                        }
                    }
                }
            }
            //fixme 第二张图片点击事件
            autoBgModel2?.let {
                if (it.onClickCallback != null && it.isDraw) {
                    if (it.isContains(pointDownX, pointDownY)) {
                        it.onClickCallback?.let {
                            it()//图片点击事件
                            isDeal = true//已经处理
                        }
                    }
                }
            }
            //fixme 第三张图片点击事件
            autoBgModel3?.let {
                if (it.onClickCallback != null && it.isDraw) {
                    if (it.isContains(pointDownX, pointDownY)) {
                        it.onClickCallback?.let {
                            it()//图片点击事件
                            isDeal = true//已经处理
                        }
                    }
                }
            }
            isDeal
        }
    }

    //fixme 不管了。AutoBg位图在自定义背景颜色值和阴影的上面。在文字的下面。
    //fixme AutoBg在颜色值的上面。
    override fun draw2Front(canvas: Canvas, paint: Paint) {
        super.draw2Front(canvas, paint)
        drawAuto(canvas, paint, this)
    }

    //fixme 加载时，默认图片.AutoBg还没加载时显示。目前只对AutoBg第一张图片有效。
    var autoBg_load: KAutoBgEntity? = null

    fun autoBg_load(block: KAutoBgEntity.() -> Unit): K4AutoBgView {
        if (autoBg_load == null) {
            autoBg_load = getmAutoBg().copy()//整个属性全部复制过来。
        }
        block(autoBg_load!!)
        autoView?.invalidate()
        autoView?.requestLayout()
        return this
    }

    //不可用
    private var autoBg_notEnable: KAutoBgEntity? = null

    //备份真实的,用于实现两个autoBg之间的动画渐变
    private var autoBg_notEnable_real: KAutoBgEntity? = null

    fun autoBg_notEnable(block: KAutoBgEntity.() -> Unit): K4AutoBgView {
        if (autoBg_notEnable == null) {
            autoBg_notEnable = getmAutoBg().copy()//整个属性全部复制过来。
        }
        block(autoBg_notEnable!!)
        autoBg_notEnable_real = null
        autoBg_notEnable?.apply {
            if (isDraw && duration > 0) {
                autoBg_notEnable_real = autoBg_notEnable?.copy()
            }
        }
        autoView?.invalidate()
        autoView?.requestLayout()
        return this
    }

    //按下
    var autoBg_press: KAutoBgEntity? = null

    //备份真实的,用于实现两个autoBg之间的动画渐变
    private var autoBg_press_real: KAutoBgEntity? = null

    fun autoBg_press(block: KAutoBgEntity.() -> Unit): K4AutoBgView {
        if (autoBg_press == null) {
            autoBg_press = getmAutoBg().copy()//整个属性全部复制过来。
        }
        block(autoBg_press!!)
        autoBg_press_real = null
        autoBg_press?.apply {
            if (isDraw && duration > 0) {
                autoBg_press_real = autoBg_press?.copy()
            }
        }
        autoView?.invalidate()
        autoView?.requestLayout()
        return this
    }

    //鼠标悬浮
    var autoBg_hover: KAutoBgEntity? = null
    private var autoBg_hover_real: KAutoBgEntity? = null
    fun autoBg_hover(block: KAutoBgEntity.() -> Unit): K4AutoBgView {
        if (autoBg_hover == null) {
            autoBg_hover = getmAutoBg().copy()//整个属性全部复制过来。
        }
        block(autoBg_hover!!)
        autoBg_hover_real = null
        autoBg_hover?.apply {
            if (isDraw && duration > 0) {
                autoBg_hover_real = autoBg_hover?.copy()
            }
        }
        autoView?.invalidate()
        autoView?.requestLayout()
        return this
    }

    //聚焦
    var autoBg_focuse: KAutoBgEntity? = null
    private var autoBg_focuse_real: KAutoBgEntity? = null

    fun autoBg_focuse(block: KAutoBgEntity.() -> Unit): K4AutoBgView {
        if (autoBg_focuse == null) {
            autoBg_focuse = getmAutoBg().copy()//整个属性全部复制过来。
        }
        block(autoBg_focuse!!)
        autoBg_focuse_real = null
        autoBg_focuse?.apply {
            if (isDraw && duration > 0) {
                autoBg_focuse_real = autoBg_focuse?.copy()
            }
        }
        autoView?.invalidate()
        autoView?.requestLayout()
        return this
    }

    //选中
    var autoBg_selected: KAutoBgEntity? = null
    private var autoBg_selected_real: KAutoBgEntity? = null

    fun autoBg_selected(block: KAutoBgEntity.() -> Unit): K4AutoBgView {
        if (autoBg_selected == null) {
            autoBg_selected = getmAutoBg().copy()//整个属性全部复制过来。
        }
        block(autoBg_selected!!)
        autoBg_selected_real = null
        autoBg_selected?.apply {
            if (isDraw && duration > 0) {
                autoBg_selected_real = autoBg_selected?.copy()
            }
        }
        autoView?.invalidate()
        autoView?.requestLayout()
        return this
    }

    var autoView: View? = this

    //fixme 正常状态（先写正常样式，再写其他状态的样式，因为其他状态的样式初始值是复制正常状态的样式的。）
    var autoBg: KAutoBgEntity? = null
    private var autoBg_real: KAutoBgEntity? = null
    fun getmAutoBg(): KAutoBgEntity {
        if (autoBg == null) {
            if (autoView == null) {
                autoView = this
            }
            autoBg = KAutoBgEntity(autoView)
        }
        return autoBg!!
    }

    fun autoBg(block: KAutoBgEntity.() -> Unit): K4AutoBgView {
        block(getmAutoBg())
        autoBg_real = null
        autoBg?.apply {
            if (isDraw && duration > 0) {
                autoBg_real = this?.copy()
            }
        }
        autoView?.invalidate()
        autoView?.requestLayout()
        return this
    }


    var ball_left: KBall? = null
    private fun getMball(): KBall {
        if (ball_left == null) {
            ball_left = KBall()
        }
        if (autoView == null) {
            autoView = this
        }
        return ball_left!!
    }

    var isDrawBall = true//fixme 是否绘制小球。 AutoBg图片为空的时候，才绘制小球。图片不为空的时候。就不绘制小球。

    /**
     * fixme 设置两个小球的颜色，半径和之间的间距;一般加载网络图片的时候。才用。
     * @param color_left 左边小球的颜色
     * @param color_right 右边小球的颜色
     * @param radius 小球的半径
     * @param offset 两个小球之间的距离（间隙）
     */
    fun ball(color_left: Int = Color.RED, color_right: Int = Color.CYAN, radius: Float = kpx.x(10f), offset: Float = kpx.x(15f)): K4AutoBgView {
        ball_left {
            color = color_left
            this.radius = radius
        }
        ball_right {
            color = color_right
            this.radius = radius
        }
        ball_center_offset = offset
        return this
    }

    //左边小球
    fun ball_left(block: KBall.() -> Unit): K4AutoBgView {
        block(getMball())
        autoView?.invalidate()
        autoView?.requestLayout()
        return this
    }

    var ball_right: KBall? = null

    //右边小球
    fun ball_right(block: KBall.() -> Unit): K4AutoBgView {
        ball_right = getMball().copy()
        block(ball_right!!)
        autoView?.invalidate()
        autoView?.requestLayout()
        return this
    }

    private var ball_offset = 2f
    private var ball_max_centerX = 0f
    private var ball_min_centerX = 0f
    private var ball_center_offset = kpx.x(15f)//两个小球之间的间隙

    var autoBgModel: KAutoBgEntity? = null//第一张当前状态位图
    var autoBgModel2: KAutoBgEntity? = null//第二张当前状态位图
    var autoBgModel3: KAutoBgEntity? = null//第三张当前状态位图
    fun drawAuto(canvas: Canvas, paint: Paint, view: View) {
        view.apply {
            //fixme 小球动画
            if (ball_left != null && ball_right != null && isDrawBall) {
                ball_left?.apply {
                    if (centerX <= 0 && this@K4AutoBgView.width > 0) {
                        centerX = this@K4AutoBgView.centerX() - ball_center_offset
                    }
                    if (centerY <= 0 && this@K4AutoBgView.height > 0) {
                        centerY = this@K4AutoBgView.centerY()
                    }
                    if (radius <= 0) {
                        radius = kpx.x(10f)
                    }
                }
                ball_right?.apply {
                    if (centerX <= 0 && this@K4AutoBgView.width > 0) {
                        centerX = this@K4AutoBgView.centerX() + ball_center_offset
                    }
                    if (centerY <= 0 && this@K4AutoBgView.height > 0) {
                        centerY = this@K4AutoBgView.centerY()
                    }
                    if (radius <= 0) {
                        radius = kpx.x(10f)
                    }
                }
                if (ball_max_centerX <= 0) {
                    if (ball_left!!.centerX > ball_right!!.centerX) {
                        ball_max_centerX = ball_left!!.centerX
                        ball_min_centerX = ball_right!!.centerX
                        ball_left!!.isAdd = false
                        ball_right!!.isAdd = true
                    } else {
                        ball_max_centerX = ball_right!!.centerX
                        ball_min_centerX = ball_left!!.centerX
                        ball_left!!.isAdd = true
                        ball_right!!.isAdd = false
                    }
                }
                //先画大的球,防止重叠
                paint.style = Paint.Style.FILL_AND_STROKE
                if (ball_left!!.isAdd) {
                    ball_offset = 2f
                    paint.color = ball_right!!.color
                    canvas.drawCircle(ball_right!!.centerX, ball_right!!.centerY, ball_right!!.radius, paint);
                    paint.color = ball_left!!.color
                    canvas.drawCircle(ball_left!!.centerX, ball_left!!.centerY, ball_left!!.radius, paint);
                } else {
                    ball_offset = 1f
                    paint.color = ball_left!!.color
                    canvas.drawCircle(ball_left!!.centerX, ball_left!!.centerY, ball_left!!.radius, paint);
                    paint.color = ball_right!!.color
                    canvas.drawCircle(ball_right!!.centerX, ball_right!!.centerY, ball_right!!.radius, paint);
                }
                ball_left?.let {
                    if (it.isAdd) {
                        it.centerX += ball_offset
                    } else {
                        it.centerX -= ball_offset
                    }
                    if (it.centerX <= ball_min_centerX) {
                        it.centerX = ball_min_centerX
                        it.isAdd = true
                    }
                    if (it.centerX >= ball_max_centerX) {
                        it.centerX = ball_max_centerX
                        it.isAdd = false
                    }
                }
                ball_right?.let {
                    if (it.isAdd) {
                        it.centerX += ball_offset
                    } else {
                        it.centerX -= ball_offset
                    }
                    if (it.centerX <= ball_min_centerX) {
                        it.centerX = ball_min_centerX
                        it.isAdd = true
                    }
                    if (it.centerX >= ball_max_centerX) {
                        it.centerX = ball_max_centerX
                        it.isAdd = false
                    }
                }
                invalidate()//fixme 小球接着刷新
            }
            //fixme 第一张图片
            if (autoBg != null) {
                autoBgModel = null
                if (!isEnabled && autoBg_notEnable != null) {
                    //不可用
                    autoBgModel = autoBg_notEnable
                } else if (view.isPressed && autoBg_press != null) {
                    //按下
                    autoBgModel = autoBg_press
                } else if (view.isHovered && autoBg_hover != null) {
                    //鼠标悬浮
                    autoBgModel = autoBg_hover
                } else if (view.isFocused && autoBg_focuse != null) {
                    //聚焦
                    autoBgModel = autoBg_focuse
                } else if (view.isSelected && autoBg_selected != null) {
                    //选中
                    autoBgModel = autoBg_selected
                }
                //正常
                if (autoBgModel == null) {
                    autoBgModel = autoBg
                }
                if ((view.width > 0 || view.height > 0)) {
                    if (autoBgModel != null) {
                        autoBgModel?.let {
                            //加载时图片
                            if (it.autoBg == null || it.autoBg!!.isRecycled) {
                                //fixme 显示加载时图片
                                autoBg_load?.let {
                                    drawAutoBg(canvas, paint, it, view)
                                }
                                if (autoBg_load == null) {
                                    isDrawBall = true//fixme 绘制两个小球。
                                }
                            } else {
                                isDrawBall = false//fixme 图片不为空，不绘制两个小球。
                            }
                        }
                        drawAutoBg(canvas, paint, autoBgModel, view)//fixme autoBgModel图片为空时，会自动去获取的。
                    } else {
                        //加载时图片
                        autoBg_load?.let {
                            drawAutoBg(canvas, paint, it, view)
                        }
                    }
                }
            }
            //fixme 第二张图片
            if (autoBg2 != null) {
                autoBgModel2 = null
                if (!isEnabled && autoBg2_notEnable != null) {
                    //不可用
                    autoBgModel2 = autoBg2_notEnable
                } else if (isPressed && autoBg2_press != null) {
                    //按下
                    autoBgModel2 = autoBg2_press
                } else if (isHovered && autoBg2_hover != null) {
                    //鼠标悬浮
                    autoBgModel2 = autoBg2_hover
                } else if (isFocused && autoBg2_focuse != null) {
                    //聚焦
                    autoBgModel2 = autoBg2_focuse
                } else if (isSelected && autoBg2_selected != null) {
                    //选中
                    autoBgModel2 = autoBg2_selected
                }
                //正常
                if (autoBgModel2 == null) {
                    autoBgModel2 = autoBg2
                }
                if ((view.width > 0 || view.height > 0) && autoBgModel2 != null) {
                    drawAutoBg(canvas, paint, autoBgModel2, view)
                }
            }
            //fixme 第三张图片（就搞三种图片，够了）
            if (autoBg3 != null) {
                autoBgModel3 = null
                if (!isEnabled && autoBg3_notEnable != null) {
                    //不可用
                    autoBgModel3 = autoBg3_notEnable
                } else if (isPressed && autoBg3_press != null) {
                    //按下
                    autoBgModel3 = autoBg3_press
                } else if (isHovered && autoBg3_hover != null) {
                    //鼠标悬浮
                    autoBgModel3 = autoBg3_hover
                } else if (isFocused && autoBg3_focuse != null) {
                    //聚焦
                    autoBgModel3 = autoBg3_focuse
                } else if (isSelected && autoBg3_selected != null) {
                    //选中
                    autoBgModel3 = autoBg3_selected
                }
                //正常
                if (autoBgModel3 == null) {
                    autoBgModel3 = autoBg3
                }
                if ((view.width > 0 || view.height > 0) && autoBgModel3 != null) {
                    drawAutoBg(canvas, paint, autoBgModel3, view)
                }
            }
        }
    }

    //画背景颜色值
    open fun drawAutoBg_BgColor(canvas: Canvas, paint: Paint, autoBgModel: KAutoBgEntity, view: View) {
        view?.apply {
            var w = view.width
            var h = view.height
            var scrollX = view.scrollX
            var scrollY = view.scrollY
            autoBgModel.let {
                //画背景
                var isDrawColor = false//是否画背景色
                if (it.bg_color != Color.TRANSPARENT) {
                    paint.color = it.bg_color
                    isDrawColor = true
                }
                var left = 0f + scrollX
                var top = 0f + scrollY//兼容滚动
                var right = w + left
                var bottom = h + top
                if (it.bgVerticalColors != null) {
                    var shader: LinearGradient? = null
                    if (!it.isBgGradient) {
                        //垂直不渐变
                        shader = getNotLinearGradient(top, bottom, it.bgVerticalColors!!, true)
                    }
                    //垂直渐变，优先级高于水平(渐变颜色值数组必须大于等于2，不然异常)
                    if (shader == null) {
                        shader = LinearGradient(0f, top, 0f, bottom, it.bgVerticalColors, null, Shader.TileMode.MIRROR)
                    }
                    paint.setShader(shader)
                    isDrawColor = true
                } else if (it.bgHorizontalColors != null) {
                    var shader: LinearGradient? = null
                    if (!it.isBgGradient) {
                        //水平不渐变
                        shader = getNotLinearGradient(left, right, it.bgHorizontalColors!!, false)
                    }
                    //水平渐变
                    if (shader == null) {
                        shader = LinearGradient(left, 0f, right, 0f, it.bgHorizontalColors, null, Shader.TileMode.MIRROR)
                    }
                    paint.setShader(shader)
                    isDrawColor = true
                }
                if (isDrawColor) {
                    //fixme  画矩形
                    var rectF = RectF(left, top, right, bottom)
                    var path = Path()
                    path.addRect(rectF, Path.Direction.CCW)
                    //canvas.drawRect(rectF, paint)
                    canvas.drawPath(path, paint)
                }
                paint.setShader(null)
            }
        }
    }

    //画前景颜色值
    open fun drawAutoBg_FgColor(canvas: Canvas, paint: Paint, autoBgModel: KAutoBgEntity, view: View) {
        view.apply {
            var w = view.width
            var h = view.height
            var scrollX = view.scrollX
            var scrollY = view.scrollY
            autoBgModel.let {
                //画背景
                var isDrawColor = false//是否画背景色
                if (it.fg_color != Color.TRANSPARENT) {
                    paint.color = it.fg_color
                    isDrawColor = true
                }
                var left = 0f + scrollX
                var top = 0f + scrollY//兼容滚动
                var right = w + left
                var bottom = h + top
                if (it.fgVerticalColors != null) {
                    var shader: LinearGradient? = null
                    if (!it.isFgGradient) {
                        //垂直不渐变
                        shader = getNotLinearGradient(top, bottom, it.fgVerticalColors!!, true)
                    }
                    //垂直渐变，优先级高于水平(渐变颜色值数组必须大于等于2，不然异常)
                    if (shader == null) {
                        shader = LinearGradient(0f, top, 0f, bottom, it.fgVerticalColors, null, Shader.TileMode.MIRROR)
                    }
                    paint.setShader(shader)
                    isDrawColor = true
                } else if (it.fgHorizontalColors != null) {
                    var shader: LinearGradient? = null
                    if (!it.isFgGradient) {
                        //水平不渐变
                        shader = getNotLinearGradient(left, right, it.fgHorizontalColors!!, false)
                    }
                    //水平渐变
                    if (shader == null) {
                        shader = LinearGradient(left, 0f, right, 0f, it.fgHorizontalColors, null, Shader.TileMode.MIRROR)
                    }
                    paint.setShader(shader)
                    isDrawColor = true
                }
                if (isDrawColor) {
                    //fixme  画矩形
                    var rectF = RectF(left, top, right, bottom)
                    var path = Path()
                    path.addRect(rectF, Path.Direction.CCW)
                    //canvas.drawRect(rectF, paint)
                    canvas.drawPath(path, paint)
                }
                //KLoggerUtils.e("test","left:\t"+left+"\tright:\t"+right+"\ttop:\t"+top+"\tbottom:\t"+bottom+"\tisDrawColor:\t"+isDrawColor+"\tfg_color:\t"+it.fg_color)
                paint.setShader(null)
            }
        }

    }

    //画自定义背景
    open fun drawAutoBg(canvas: Canvas, paint: Paint, autoBgModel: KAutoBgEntity?, view: View) {
        if (autoBgModel == null) {
            return
        }
        view?.apply {
            var w = view.width
            var h = view.height
            var scrollX = view.scrollX
            var scrollY = view.scrollY
            if (autoBgModel.isDraw) {
                if (autoBgModel.autoBg == null || autoBgModel.autoBg!!.isRecycled) {
                    autoBgModel.autoBg()//fixme 主动再次获取图片
                }
            }
            paint.color = Color.WHITE//防止受影响，所以颜色重置一下
            if (autoBgModel.isDraw) {
                drawAutoBg_BgColor(canvas, paint, autoBgModel, view)//画背景色
            }
            if (autoBgModel.isDraw && autoBgModel.autoBg != null && !autoBgModel.autoBg!!.isRecycled && width > 0 && height > 0) {
                if (autoBgModel.rotation != 0f) {
                    canvas.save()//旋转角度不等于0，保存画布状态。
                }
                if (autoBgModel.alpha < 255 && autoBgModel.alpha >= 0 && autoBg != null) {
                    paint.alpha = autoBgModel.alpha//透明度
                }
                autoBgModel?.apply {
                    if (saturation != 1f && autoBg != null) {//1是正常。就没有必要设置了。
                        //创建颜色变换矩阵
                        val mColorMatrix = ColorMatrix()
                        //设置灰度影响范围
                        mColorMatrix.setSaturation(saturation)
                        //创建颜色过滤矩阵
                        val mColorFilter = ColorMatrixColorFilter(mColorMatrix)
                        //设置画笔的颜色过滤矩阵
                        paint.setColorFilter(mColorFilter)
                    }
                    if (autoBgColor != Color.TRANSPARENT && autoBg != null) {
                        paint.setColorFilter(LightingColorFilter(Color.TRANSPARENT, autoBgColor))//去除原有位图的颜色，直接变成制定颜色的位图
                    }
                    var bitmap = autoBg!!
                    if (!bitmap.isRecycled) {
                        isDrawBall = false//fixme 有位图，就不画转动的小圆点了。
                        if (isFill) {
                            if (rotation != 0f) {
                                var l = centerX
                                var t = centerY + scrollY
                                canvas.rotate(rotation, l, t)
                            }
                            autoLeft = 0f
                            autoTop = 0f + scrollY
                            autoRight = w.toFloat()
                            autoBottom = h.toFloat() + scrollY
                            if (!bitmap.isRecycled) {
                                var src = getAutoRect(bitmap, autoBgModel)
                                //fixme 填充整个组件,垂直居中，水平居中也就不存在了。直接铺满整个控件
                                //canvas.drawBitmap(bitmap, src, RectF(0f + scrollX, 0f + scrollY, w.toFloat() + scrollX, h.toFloat() + scrollY), paint)
                                //KLoggerUtils.e("test","宽度:\t"+width+"\t高度:\t"+height+"\t位图:\t"+autoBgModel.autoBg?.width+"\tscrollY:\t"+scrollY)
                                if (autoBgModel.rotationX == 0f && autoBgModel.rotationY == 0f) {
                                    //正常
                                    canvas.drawBitmap(bitmap, src, RectF(0f + scrollX, 0f + scrollY, w.toFloat() + scrollX, h.toFloat() + scrollY), paint)
                                } else {
                                    //沿x轴或y轴旋转
                                    rotateX(autoBgModel.rotationX, centerX = (centerX + scrollX), centerY = (centerY + scrollY), isRestore = true) {
                                        rotateY(autoBgModel.rotationY, centerX = (centerX + scrollX), centerY = (centerY + scrollY), isRestore = true) {
                                            canvas.drawBitmap(bitmap, src, RectF(0f + scrollX, 0f + scrollY, w.toFloat() + scrollX, h.toFloat() + scrollY), paint)
                                        }
                                    }
                                }
                            }
                        } else {
                            if (isAutoCenterVertical && !isAutoCenterHorizontal) {
                                var l = autoLeftPadding + scrollX
                                var t = kpx.centerBitmapY(autoBgModel.height, h.toFloat()) + autoTopPadding + scrollY
                                //右靠齐
                                if (isAutoRight) {
                                    l = w - autoBgModel.width - autoRightPadding + scrollX
                                }
                                //底对其
                                if (isAutoBottom) {
                                    t = kpx.centerBitmapY(autoBgModel.height, h.toFloat()) - autoBottomPadding + scrollY
                                }
                                if (rotation != 0f) {
                                    canvas.rotate(rotation, l + autoBgModel.width / 2, t + autoBgModel.height / 2)
                                }
                                autoLeft = l
                                autoTop = t
                                autoRight = l + autoBgModel.width
                                autoBottom = t + autoBgModel.height
                                //fixme 垂直居中，优先级最高
                                //canvas.drawBitmap(bitmap, l, t, paint)
                                if (!bitmap.isRecycled) {
                                    var src = getAutoRect(bitmap, autoBgModel)
                                    if (autoBgModel.rotationX == 0f && autoBgModel.rotationY == 0f) {
                                        //正常
                                        canvas.drawBitmap(bitmap, src, RectF(l, t, l + autoBgModel.width, t + autoBgModel.height), paint)
                                    } else {
                                        //沿x轴或y轴旋转
                                        rotateX(autoBgModel.rotationX, centerX = (l + autoBgModel.width / 2), centerY = (t + autoBgModel.height / 2), isRestore = true) {
                                            rotateY(autoBgModel.rotationY, centerX = (l + autoBgModel.width / 2), centerY = (t + autoBgModel.height / 2), isRestore = true) {
                                                canvas.drawBitmap(bitmap, src, RectF(l, t, l + autoBgModel.width, t + autoBgModel.height), paint)
                                            }
                                        }
                                    }
                                }
                            } else if (isAutoCenterHorizontal && !isAutoCenterVertical) {
                                var l = kpx.centerBitmapX(autoBgModel.width, w.toFloat()) + autoLeftPadding + scrollX
                                var t = autoTopPadding + scrollY
                                //右靠齐
                                if (isAutoRight) {
                                    l = kpx.centerBitmapX(autoBgModel.width, w.toFloat()) - autoRightPadding + scrollX
                                }
                                //底对其
                                if (isAutoBottom) {
                                    t = h - autoBgModel.height - autoBottomPadding + scrollY
                                }
                                if (rotation != 0f) {
                                    canvas.rotate(rotation, l + autoBgModel.width / 2, t + autoBgModel.height / 2)
                                }
                                autoLeft = l
                                autoTop = t
                                autoRight = l + autoBgModel.width
                                autoBottom = t + autoBgModel.height
                                //fixme 水平居中优先级次之
                                //canvas.drawBitmap(bitmap, l, t, paint)
                                if (!bitmap.isRecycled) {
                                    var src = getAutoRect(bitmap, autoBgModel)
                                    //canvas.drawBitmap(bitmap, src, RectF(l, t, l + autoBgModel.width, t + autoBgModel.height), paint)
                                    if (autoBgModel.rotationX == 0f && autoBgModel.rotationY == 0f) {
                                        //正常
                                        canvas.drawBitmap(bitmap, src, RectF(l, t, l + autoBgModel.width, t + autoBgModel.height), paint)
                                    } else {
                                        //沿x轴或y轴旋转
                                        rotateX(autoBgModel.rotationX, centerX = (l + autoBgModel.width / 2), centerY = (t + autoBgModel.height / 2), isRestore = true) {
                                            rotateY(autoBgModel.rotationY, centerX = (l + autoBgModel.width / 2), centerY = (t + autoBgModel.height / 2), isRestore = true) {
                                                canvas.drawBitmap(bitmap, src, RectF(l, t, l + autoBgModel.width, t + autoBgModel.height), paint)
                                            }
                                        }
                                    }
                                }
                            } else if (isAutoCenter || (isAutoCenterVertical && isAutoCenterHorizontal)) {
                                var l = kpx.centerBitmapX(autoBgModel.width, w.toFloat()) + autoLeftPadding + scrollX
                                var t = kpx.centerBitmapY(autoBgModel.height, h.toFloat()) + autoTopPadding + scrollY
                                //右靠齐
                                if (isAutoRight) {
                                    l = kpx.centerBitmapX(autoBgModel.width, w.toFloat()) - autoRightPadding + scrollX
                                }
                                //底对其
                                if (isAutoBottom) {
                                    t = kpx.centerBitmapY(autoBgModel.height, h.toFloat()) - autoBottomPadding + scrollY
                                }
                                if (rotation != 0f) {
                                    canvas.rotate(rotation, l + autoBgModel.width / 2, t + autoBgModel.height / 2)
                                }
                                autoLeft = l
                                autoTop = t
                                autoRight = l + autoBgModel.width
                                autoBottom = t + autoBgModel.height
                                //fixme 水平居中+垂直居中
                                //canvas.drawBitmap(bitmap, l, t, paint)
                                if (!bitmap.isRecycled) {
                                    //src保证宽和高的比例。不会变形。
                                    var src = getAutoRect(bitmap, autoBgModel)
                                    //canvas.drawBitmap(bitmap, src, RectF(l, t, l + autoBgModel.width, t + autoBgModel.height), paint)
                                    if (autoBgModel.rotationX == 0f && autoBgModel.rotationY == 0f) {
                                        //正常
                                        canvas.drawBitmap(bitmap, src, RectF(l, t, l + autoBgModel.width, t + autoBgModel.height), paint)
                                    } else {
                                        //沿x轴或y轴旋转
                                        rotateX(autoBgModel.rotationX, centerX = (l + autoBgModel.width / 2), centerY = (t + autoBgModel.height / 2), isRestore = true) {
                                            rotateY(autoBgModel.rotationY, centerX = (l + autoBgModel.width / 2), centerY = (t + autoBgModel.height / 2), isRestore = true) {
                                                canvas.drawBitmap(bitmap, src, RectF(l, t, l + autoBgModel.width, t + autoBgModel.height), paint)
                                            }
                                        }
                                    }
                                }
                            } else {
                                var l = autoLeftPadding + scrollX
                                var t = autoTopPadding + scrollY
                                //右靠齐
                                if (isAutoRight) {
                                    l = w - autoBgModel.width - autoRightPadding + scrollX
                                }
                                //底对齐
                                if (isAutoBottom) {
                                    t = h - autoBgModel.height - autoBottomPadding + scrollY
                                }
                                if (rotation != 0f) {
                                    canvas.rotate(rotation, l + autoBgModel.width / 2, t + autoBgModel.height / 2)
                                }
                                autoLeft = l
                                autoTop = t
                                autoRight = l + autoBgModel.width
                                autoBottom = t + autoBgModel.height
                                //fixme 以左上角为标准
                                //canvas.drawBitmap(bitmap, l, t, paint)
                                if (!bitmap.isRecycled) {
                                    var src = getAutoRect(bitmap, autoBgModel)
                                    //canvas.drawBitmap(bitmap, src, RectF(l, t, l + autoBgModel.width, t + autoBgModel.height), paint)
                                    if (autoBgModel.rotationX == 0f && autoBgModel.rotationY == 0f) {
                                        //正常
                                        canvas.drawBitmap(bitmap, src, RectF(l, t, l + autoBgModel.width, t + autoBgModel.height), paint)
                                    } else {
                                        //沿x轴或y轴旋转
                                        rotateX(autoBgModel.rotationX, centerX = (l + autoBgModel.width / 2), centerY = (t + autoBgModel.height / 2), isRestore = true) {
                                            rotateY(autoBgModel.rotationY, centerX = (l + autoBgModel.width / 2), centerY = (t + autoBgModel.height / 2), isRestore = true) {
                                                canvas.drawBitmap(bitmap, src, RectF(l, t, l + autoBgModel.width, t + autoBgModel.height), paint)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    paint.setColorFilter(null)//必须清除，不然对后面有影响。
                }
                if (autoBgModel.alpha < 255 && autoBgModel.alpha >= 0) {
                    paint.alpha = 255//恢复透明度
                }
                if (autoBgModel.rotation != 0f) {
                    canvas.restore()//恢复旋转之前的状态
                }
            }
            drawAutoBg_FgColor(canvas, paint, autoBgModel, view)//画前景色
        }
    }

    //fixme 保证位图的宽和高显示的时候比例不会变形。
    private fun getAutoRect(bitmap: Bitmap, autoBgModel: KAutoBgEntity): Rect {
        var bw = autoBgModel.width.toFloat()
        var bh = autoBgModel.height.toFloat()
        //将bw变的和位图一样大。保证宽和高的比例
        if (bw > bitmap.width) {
            var p = bw.toFloat() / bitmap.width.toFloat()
            bh = bh / p
            bw = bitmap.width.toFloat()
        } else {
            var p = bitmap.width.toFloat() / bw
            bh = bh * p
            bw = bitmap.width.toFloat()
        }
        if (bh > bitmap.height) {
            bh = bitmap.height.toFloat()
        }
        var bleft = ((bitmap.width - bw) / 2).toInt()
        var bright = (bleft + bw).toInt()
        var btop = ((bitmap.height - bh) / 2).toInt()
        var bbottom = (btop + bh).toInt()
        var src = Rect(bleft, btop, bright, bbottom)
        return src
    }

    //fixme 第二张图片，在第一张的上面

    //不可用
    var autoBg2_notEnable: KAutoBgEntity? = null
    private var autoBg2_notEnable_real: KAutoBgEntity? = null

    fun autoBg2_notEnable(block: KAutoBgEntity.() -> Unit): K4AutoBgView {
        if (autoBg2_notEnable == null) {
            autoBg2_notEnable = getmAutoBg2().copy()//整个属性全部复制过来。
        }
        block(autoBg2_notEnable!!)
        autoBg2_notEnable_real = null
        autoBg2_notEnable?.apply {
            if (isDraw && duration > 0) {
                autoBg2_notEnable_real = autoBg2_notEnable?.copy()
            }
        }
        autoView?.invalidate()
        autoView?.requestLayout()
        return this
    }

    //按下
    var autoBg2_press: KAutoBgEntity? = null
    private var autoBg2_press_real: KAutoBgEntity? = null

    fun autoBg2_press(block: KAutoBgEntity.() -> Unit): K4AutoBgView {
        if (autoBg2_press == null) {
            autoBg2_press = getmAutoBg2().copy()//整个属性全部复制过来。
        }
        block(autoBg2_press!!)
        autoBg2_press_real = null
        autoBg2_press?.apply {
            if (isDraw && duration > 0) {
                autoBg2_press_real = autoBg2_press?.copy()
            }
        }
        autoView?.invalidate()
        autoView?.requestLayout()
        return this
    }

    //鼠标悬浮
    var autoBg2_hover: KAutoBgEntity? = null
    private var autoBg2_hover_real: KAutoBgEntity? = null

    fun autoBg2_hover(block: KAutoBgEntity.() -> Unit): K4AutoBgView {
        if (autoBg2_hover == null) {
            autoBg2_hover = getmAutoBg2().copy()//整个属性全部复制过来。
        }
        block(autoBg2_hover!!)
        autoBg2_hover_real = null
        autoBg2_hover?.apply {
            if (isDraw && duration > 0) {
                autoBg2_hover_real = autoBg2_hover?.copy()
            }
        }
        requestLayout()
        return this
    }

    //聚焦
    var autoBg2_focuse: KAutoBgEntity? = null
    private var autoBg2_focuse_real: KAutoBgEntity? = null

    fun autoBg2_focuse(block: KAutoBgEntity.() -> Unit): K4AutoBgView {
        if (autoBg2_focuse == null) {
            autoBg2_focuse = getmAutoBg2().copy()//整个属性全部复制过来。
        }
        block(autoBg2_focuse!!)
        autoBg2_focuse_real = null
        autoBg2_focuse?.apply {
            if (isDraw && duration > 0) {
                autoBg2_focuse_real = autoBg2_focuse?.copy()
            }
        }
        autoView?.invalidate()
        autoView?.requestLayout()
        return this
    }

    //选中
    var autoBg2_selected: KAutoBgEntity? = null
    private var autoBg2_selected_real: KAutoBgEntity? = null

    fun autoBg2_selected(block: KAutoBgEntity.() -> Unit): K4AutoBgView {
        if (autoBg2_selected == null) {
            autoBg2_selected = getmAutoBg2().copy()//整个属性全部复制过来。
        }
        block(autoBg2_selected!!)
        autoBg2_selected_real = null
        autoBg2_selected?.apply {
            if (isDraw && duration > 0) {
                autoBg2_selected_real = autoBg2_selected?.copy()
            }
        }
        autoView?.invalidate()
        autoView?.requestLayout()
        return this
    }

    //fixme 正常状态（先写正常样式，再写其他状态的样式，因为其他状态的样式初始值是复制正常状态的样式的。）
    var autoBg2: KAutoBgEntity? = null
    private var autoBg2_real: KAutoBgEntity? = null
    fun getmAutoBg2(): KAutoBgEntity {
        if (autoBg2 == null) {
            if (autoView == null) {
                autoView = this
            }
            autoBg2 = KAutoBgEntity(autoView)
        }
        return autoBg2!!
    }

    fun autoBg2(block: KAutoBgEntity.() -> Unit): K4AutoBgView {
        block(getmAutoBg2())
        autoBg2_real = null
        autoBg2?.apply {
            if (isDraw && duration > 0) {
                autoBg2_real = autoBg2?.copy()
            }
        }
        autoView?.invalidate()
        autoView?.requestLayout()
        return this
    }

    //fixme 第三张图片，在第二张图片的上面（暂时就搞三张图片，够了）

    //不可用
    var autoBg3_notEnable: KAutoBgEntity? = null
    private var autoBg3_notEnable_real: KAutoBgEntity? = null
    fun autoBg3_notEnable(block: KAutoBgEntity.() -> Unit): K4AutoBgView {
        if (autoBg3_notEnable == null) {
            autoBg3_notEnable = getmAutoBg3().copy()//整个属性全部复制过来。
        }
        block(autoBg3_notEnable!!)
        autoBg3_notEnable_real = null
        autoBg3_notEnable?.apply {
            if (isDraw && duration > 0) {
                autoBg3_notEnable_real = autoBg3_notEnable?.copy()
            }
        }
        autoView?.invalidate()
        autoView?.requestLayout()
        return this
    }

    //按下
    var autoBg3_press: KAutoBgEntity? = null
    private var autoBg3_press_real: KAutoBgEntity? = null
    fun autoBg3_press(block: KAutoBgEntity.() -> Unit): K4AutoBgView {
        if (autoBg3_press == null) {
            autoBg3_press = getmAutoBg3().copy()//整个属性全部复制过来。
        }
        block(autoBg3_press!!)
        autoBg3_press_real = null
        autoBg3_press?.apply {
            if (isDraw && duration > 0) {
                autoBg3_notEnable_real = autoBg3_press?.copy()
            }
        }
        autoView?.invalidate()
        autoView?.requestLayout()
        return this
    }

    //鼠标悬浮
    var autoBg3_hover: KAutoBgEntity? = null
    private var autoBg3_hover_real: KAutoBgEntity? = null
    fun autoBg3_hover(block: KAutoBgEntity.() -> Unit): K4AutoBgView {
        if (autoBg3_hover == null) {
            autoBg3_hover = getmAutoBg3().copy()//整个属性全部复制过来。
        }
        block(autoBg3_hover!!)
        autoBg3_hover_real = null
        autoBg3_hover?.apply {
            if (isDraw && duration > 0) {
                autoBg3_hover_real = autoBg3_hover?.copy()
            }
        }
        autoView?.invalidate()
        autoView?.requestLayout()
        return this
    }

    //聚焦
    var autoBg3_focuse: KAutoBgEntity? = null
    private var autoBg3_focuse_real: KAutoBgEntity? = null
    fun autoBg3_focuse(block: KAutoBgEntity.() -> Unit): K4AutoBgView {
        if (autoBg3_focuse == null) {
            autoBg3_focuse = getmAutoBg3().copy()//整个属性全部复制过来。
        }
        block(autoBg3_focuse!!)
        autoBg3_focuse_real = null
        autoBg3_focuse?.apply {
            if (isDraw && duration > 0) {
                autoBg3_focuse_real = autoBg3_focuse?.copy()
            }
        }
        autoView?.invalidate()
        autoView?.requestLayout()
        return this
    }

    //选中
    var autoBg3_selected: KAutoBgEntity? = null
    private var autoBg3_selected_real: KAutoBgEntity? = null
    fun autoBg3_selected(block: KAutoBgEntity.() -> Unit): K4AutoBgView {
        if (autoBg3_selected == null) {
            autoBg3_selected = getmAutoBg3().copy()//整个属性全部复制过来。
        }
        block(autoBg3_selected!!)
        autoBg3_selected_real = null
        autoBg3_selected?.apply {
            if (isDraw && duration > 0) {
                autoBg3_selected_real = autoBg3_selected?.copy()
            }
        }
        autoView?.invalidate()
        autoView?.requestLayout()
        return this
    }

    //fixme 正常状态（先写正常样式，再写其他状态的样式，因为其他状态的样式初始值是复制正常状态的样式的。）
    var autoBg3: KAutoBgEntity? = null
    private var autoBg3_real: KAutoBgEntity? = null
    fun getmAutoBg3(): KAutoBgEntity {
        if (autoBg3 == null) {
            if (autoView == null) {
                autoView = this
            }
            autoBg3 = KAutoBgEntity(autoView)
        }
        return autoBg3!!
    }

    fun autoBg3(block: KAutoBgEntity.() -> Unit): K4AutoBgView {
        block(getmAutoBg3())
        autoBg3_real = null
        autoBg3?.apply {
            if (isDraw && duration > 0) {
                autoBg3_real = autoBg3?.copy()
            }
        }
        autoView?.invalidate()
        autoView?.requestLayout()
        return this
    }

    //为了方便还是改成了fasle(大多数都是false)。需要手动设置成true（控件和图片一样大小。）
    var isAutoBgWH = false//fixme 控件的宽度和高度是否为自定义位图的宽度和高度。(以最大的那张图片为标准)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var w = 0
        var h = 0
        if (isAutoBgWH) {
            autoBg_notEnable?.autoBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoBg_press?.autoBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoBg_hover?.autoBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoBg_focuse?.autoBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoBg_selected?.autoBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoBg?.autoBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            //fixme 第二张图片
            autoBg2_notEnable?.autoBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoBg2_press?.autoBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoBg2_hover?.autoBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoBg2_focuse?.autoBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoBg2_selected?.autoBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoBg2?.autoBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            //fixme 第三张图片
            autoBg3_notEnable?.autoBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoBg3_press?.autoBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoBg3_hover?.autoBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoBg3_focuse?.autoBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoBg3_selected?.autoBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoBg3?.autoBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }

        }
        if (w > 0 && h > 0) {
            //取自定义位图宽度和高度最大的那个。
            this.w = w
            this.h = h
            layoutParams.width = w
            layoutParams.height = h
            setMeasuredDimension(w, h)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }


    //判断抖动动画是否正在进行，true正在进行动画，false没有。
    var isView_jitter: Boolean = false
    var view_jitter_rotation: Float = 0F

    /**
     * fixme 这个实现整个控件的抖动。rotation旋转只对view有效，viewGroup无效。
     */
    fun jitter(degree: Float = 45f, repeatCount: Int = 3, duration: Long = 800, isVibrator: Boolean = true, vibratorDuration: Long = 200) {
        if (duration > 0 && repeatCount >= 0 && viewGroup != null) {
            if (!isView_jitter!!) {
                var cRotation = viewGroup!!.rotation
                ofFloat("view_jitter_rotation", repeatCount, duration / (repeatCount + 1), cRotation, cRotation + degree, cRotation, cRotation - degree, cRotation, isInvalidate = false) {
                    viewGroup?.pivotX = (viewGroup!!.scrollX + viewGroup!!.width).toFloat() / 2
                    viewGroup?.pivotY = (viewGroup!!.scrollY + viewGroup!!.height).toFloat() / 2
                    viewGroup?.rotation = it
                    viewGroup?.invalidate()//刷新
                    if (it == cRotation) {
                        isView_jitter = false
                    } else {
                        isView_jitter = true
                    }
                }
                if (isVibrator && context is Activity) {
                    KVibratorUtils.Vibrate(context as Activity, vibratorDuration)
                }
            }
        }
    }

    //判断抖动动画是否正在进行，true正在进行动画，false没有。
    private var isAutoBg_jitter: Boolean? = null

    /**
     * 实现抖动效果
     * @param autoBg, this.autoBgModel是当前状态的AutoBg(第一张)
     * @param degree 抖动角度（与当前角度的差）
     * @param repeatCount 抖动次数，0代表一次
     * @param duration 抖动时间。计算过了。是抖动的总时间。
     * @param isVibrator 是否震动。
     * @param vibratorDuration 震动的时间
     */
    fun autoBg_jitter(autoBg: KAutoBgEntity? = this.autoBgModel, degree: Float = 45f, repeatCount: Int = 3, duration: Long = 800, isVibrator: Boolean = true, vibratorDuration: Long = 200) {
        if (duration > 0 && repeatCount >= 0 && autoBg != null && autoBg.isDraw) {
            if (isAutoBg_jitter == null) {
                isAutoBg_jitter = false
            }
            if (!isAutoBg_jitter!!) {
                var cRotation = autoBg.rotation
                ofFloat("autoBg_jitter", repeatCount, duration / (repeatCount + 1), cRotation, cRotation + degree, cRotation, cRotation - degree, cRotation, isInvalidate = false) {
                    autoBg.rotation = it
                    autoView?.invalidate()//刷新
                    if (it == cRotation) {
                        isAutoBg_jitter = false
                    } else {
                        isAutoBg_jitter = true
                    }
                }
                if (isVibrator && context is Activity) {
                    KVibratorUtils.Vibrate(context as Activity, vibratorDuration)
                }
            }
        }
    }

    /**
     * 实现动画旋转效果
     * @param toRotation 旋转指定角度
     * @param duration 旋转时间
     */
    fun autoBg_rotation(autoBg: KAutoBgEntity?, toRotation: Float, duration: Long = 300) {
        if (autoBg != null && autoBg.isDraw) {
            if (duration <= 0 && (toRotation != autoBg.rotation)) {
                autoBg.rotation = toRotation
                invalidate()
            } else if (toRotation != autoBg.rotation) {
                ofFloat("autoBg_rotation", 0, duration, autoBg.rotation, toRotation, isInvalidate = false) {
                    autoBg.rotation = it
                    autoView?.invalidate()//刷新
                }
            }
        }
    }

    /**
     * 实现动画渐隐效果
     * @param toAlpha 指定透明度
     * @param duration 渐隐时间
     */
    fun autoBg_alpha(autoBg: KAutoBgEntity?, toAlpha: Int, duration: Long = 350) {
        if (autoBg != null && autoBg.isDraw && toAlpha >= 0 && toAlpha <= 255) {
            if (duration <= 0 && (toAlpha != autoBg.alpha)) {
                autoBg.alpha = toAlpha
                invalidate()
            } else if (toAlpha != autoBg.alpha) {
                ofInt("autoBg_alpha", 0, duration, autoBg.alpha, toAlpha, isInvalidate = false) {
                    autoBg.alpha = it
                    autoView?.invalidate()//刷新
                }
            }
        }
    }

    /**
     * fixme 两个AutoBg之间的渐变，目前就只给第一张AutoBg做了渐变过程。第二张，第三张。没有做。也不需要。做一张就可以了。
     * fixme 颜色颜色值的渐变效果，不怎么好。所以，把颜色渐变去掉了。
     */
    var autoBgwidth: Int = 0
    var autoBgheight: Int = 0
    var autoBgrotation: Float = 0F
    var autoBgalpha = 0
    var autoBgsaturation = 0f

    /**
     * 两个autoBg直接的属性渐变，rotation,alpha,width,height,saturation等
     * autoBg向 -》autoBg2进行变化。
     * autoBg_change 是当前实际变化的autoBg
     */
    private fun autoBg_anime1(autoBg: KAutoBgEntity?, autoBg2: KAutoBgEntity?, autoBg_change: KAutoBgEntity?, callback: (() -> Unit)) {
        var autoBg = autoBg
        if (autoBg == null) {
            autoBg = this.autoBg
        }
        if (autoBg == null || autoBg2 == null) {
            return
        }
        if (autoBg == autoBg2) {
            return//同一个状态，不变化
        }
        if (!autoBg2!!.isDraw || autoBg2!!.duration <= 0) {
            return
        }
        var propertyValuesHolder1 = PropertyValuesHolder.ofInt("autoBgwidth", autoBg.width, autoBg2.width)
        var propertyValuesHolder2 = PropertyValuesHolder.ofInt("autoBgheight", autoBg.height, autoBg2.height)
        var propertyValuesHolder3 = PropertyValuesHolder.ofFloat("autoBgrotation", autoBg.rotation, autoBg2.rotation)
        var propertyValuesHolder4 = PropertyValuesHolder.ofInt("autoBgalpha", autoBg.alpha, autoBg2.alpha)
        var propertyValuesHolder5 = PropertyValuesHolder.ofFloat("autoBgsaturation", autoBg.saturation, autoBg2.saturation)

        if (autoBgwidth > 0 && autoBgheight > 0) {
            autoBg_change?.width = autoBgwidth
            autoBg_change?.height = autoBgheight
            autoBg_change?.rotation = autoBgrotation
            autoBg_change?.alpha = autoBgalpha
            autoBg_change?.saturation = autoBgsaturation
        } else {
            autoBg_change?.width = autoBg.width
            autoBg_change?.height = autoBg.height
            autoBg_change?.rotation = autoBg.rotation
            autoBg_change?.alpha = autoBg.alpha
            autoBg_change?.saturation = autoBg.saturation

            autoBgwidth = autoBg.width
            autoBgheight = autoBg.height
            autoBgrotation = autoBg.rotation
            autoBgalpha = autoBg.alpha
            autoBgsaturation = autoBg.saturation
        }

        val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(this, propertyValuesHolder1, propertyValuesHolder2, propertyValuesHolder3, propertyValuesHolder4, propertyValuesHolder5)
        objectAnimator.repeatCount = 0
        objectAnimator.duration = autoBg2.duration
        objectAnimator.interpolator = LinearInterpolator()//线性变化，平均变化
        objectAnimator.addUpdateListener {
            callback()
            autoView?.invalidate()//刷新
        }
        objectAnimator.start()
    }

    private fun changeAutoBg(autoBg: KAutoBgEntity?) {
        autoBg?.apply {
            width = autoBgwidth
            height = autoBgheight
            rotation = autoBgrotation
            alpha = autoBgalpha
            saturation = autoBgsaturation
        }
    }

    //当前状态
    var autoBg_current: KAutoBgEntity? = null

    //当前实际状态
    var autoBg_change: KAutoBgEntity? = null


    private fun onChangeAutoBg() {
        if (autoBg_current == null) {
            autoBg_current = autoBg_real
        }
        if (autoBg_change == null) {
            autoBg_change = autoBg
        }
        if (state_previous == state_notEnable) {
            //按下->按下
            autoBg_anime1(autoBg_notEnable, autoBg_current, autoBg_change) {
                changeAutoBg(autoBg_change)
            }
        } else if (state_previous == state_press) {
            //按下->按下
            autoBg_anime1(autoBg_press, autoBg_current, autoBg_change) {
                changeAutoBg(autoBg_change)
            }
        } else if (state_previous == state_hover) {
            //悬浮-》按下
            autoBg_anime1(autoBg_hover, autoBg_current, autoBg_change) {
                changeAutoBg(autoBg_change)
            }
        } else if (state_previous == state_focuse) {
            //聚焦-》按下
            autoBg_anime1(autoBg_focuse, autoBg_current, autoBg_change) {
                changeAutoBg(autoBg_change)
            }
        } else if (state_previous == state_selected) {
            //选中-》按下
            autoBg_anime1(autoBg_selected, autoBg_current, autoBg_change) {
                changeAutoBg(autoBg_change)
            }
        } else if (state_previous == state_normal) {
            //常态-》按下
            autoBg_anime1(autoBg, autoBg_current, autoBg_change) {
                changeAutoBg(autoBg_change)
            }
        }
    }

    override fun changedNotEnabled() {
        super.changedNotEnabled()
        //当前状态
        autoBg_current = autoBg_notEnable_real
        //当前实际状态
        autoBg_change = autoBg_notEnable
        onChangeAutoBg()
    }

    override fun changedPressed() {
        super.changedPressed()
        //当前状态
        autoBg_current = autoBg_press_real
        //当前实际状态
        autoBg_change = autoBg_press
        onChangeAutoBg()

    }

    override fun changedHovered() {
        super.changedHovered()
        //当前状态
        autoBg_current = autoBg_hover_real
        //当前实际状态
        autoBg_change = autoBg_hover
        onChangeAutoBg()
    }

    override fun changedFocused() {
        super.changedFocused()
        //当前状态
        autoBg_current = autoBg_focuse_real
        //当前实际状态
        autoBg_change = autoBg_focuse
        onChangeAutoBg()
    }

    override fun changedSelected() {
        super.changedSelected()
        //当前状态
        autoBg_current = autoBg_selected_real
        //当前实际状态
        autoBg_change = autoBg_selected
        onChangeAutoBg()
    }

    override fun changedNormal() {
        super.changedNormal()
        //当前状态
        autoBg_current = autoBg_real
        //当前实际状态
        autoBg_change = autoBg
        onChangeAutoBg()
    }

    //fixme 释放AutoBg对象
    private fun recycleAutoBg(autoBg: KAutoBgEntity?) {
        autoBg?.let {
            it.view = null
            it.autoBg?.apply {
                if (!isRecycled) {
                    recycle()
                }
            }
            it.autoBg = null
            it.isDraw = false
            it.resId = null
            it.assetsPath = null
            it.filePath = null
            it.url = null
        }
    }

    //fixme 释放位图;KAutoBgEntity本身会制空。
    fun recycleAutoBg() {
        autoBg_notEnable?.onClickCallback = null
        autoBg_notEnable?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg_notEnable?.autoBg = null
        autoBg_notEnable?.view = null
        recycleAutoBg(autoBg_notEnable)
        autoBg_notEnable = null

        autoBg_notEnable_real?.onClickCallback = null
        autoBg_notEnable_real?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg_notEnable_real?.autoBg = null
        autoBg_notEnable_real?.view = null
        recycleAutoBg(autoBg_notEnable_real)
        autoBg_notEnable_real = null

        autoBg_press?.onClickCallback = null
        autoBg_press?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg_press?.autoBg = null
        autoBg_press?.view = null
        recycleAutoBg(autoBg_press)
        autoBg_press = null

        autoBg_press_real?.onClickCallback = null
        autoBg_press_real?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg_press_real?.autoBg = null
        autoBg_press_real?.view = null
        recycleAutoBg(autoBg_press_real)
        autoBg_press_real = null

        autoBg_hover?.onClickCallback = null
        autoBg_hover?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg_hover?.autoBg = null
        autoBg_hover?.view = null
        recycleAutoBg(autoBg_hover)
        autoBg_hover = null

        autoBg_hover_real?.onClickCallback = null
        autoBg_hover_real?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg_hover_real?.autoBg = null
        autoBg_hover_real?.view = null
        recycleAutoBg(autoBg_hover_real)
        autoBg_hover_real = null

        autoBg_focuse?.onClickCallback = null
        autoBg_focuse?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg_focuse?.autoBg = null
        autoBg_focuse?.view = null
        recycleAutoBg(autoBg_focuse)
        autoBg_focuse = null

        autoBg_focuse_real?.onClickCallback = null
        autoBg_focuse_real?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg_focuse_real?.autoBg = null
        autoBg_focuse_real?.view = null
        recycleAutoBg(autoBg_focuse_real)
        autoBg_focuse_real = null

        autoBg_selected?.onClickCallback = null
        autoBg_selected?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg_selected?.autoBg = null
        autoBg_selected?.view = null
        recycleAutoBg(autoBg_selected)
        autoBg_selected = null

        autoBg_selected_real?.onClickCallback = null
        autoBg_selected_real?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg_selected_real?.autoBg = null
        autoBg_selected_real?.view = null
        recycleAutoBg(autoBg_selected_real)
        autoBg_selected_real = null

        autoBg?.onClickCallback = null
        autoBg?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
            //KLoggerUtils.e("释放：\t"+isRecycled)//fixme 有时可能不会打印出来，但是的确是执行了。
        }
        autoBg?.autoBg = null
        autoBg?.view = null
        recycleAutoBg(autoBg)
        autoBg = null

        autoBg_real?.onClickCallback = null
        autoBg_real?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg_real?.view = null
        autoBg_real?.autoBg = null
        recycleAutoBg(autoBg_real)
        autoBg_real = null

        //fixme 第二张
        autoBg2_notEnable?.onClickCallback = null
        autoBg2_notEnable?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg2_notEnable?.autoBg = null
        autoBg2_notEnable?.view = null
        recycleAutoBg(autoBg2_notEnable)
        autoBg2_notEnable = null

        autoBg2_notEnable_real?.onClickCallback = null
        autoBg2_notEnable_real?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg2_notEnable_real?.autoBg = null
        autoBg2_notEnable_real?.view = null
        recycleAutoBg(autoBg2_notEnable_real)
        autoBg2_notEnable_real = null

        autoBg2_press?.onClickCallback = null
        autoBg2_press?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg2_press?.autoBg = null
        autoBg2_press?.view = null
        recycleAutoBg(autoBg2_press)
        autoBg2_press = null

        autoBg2_press_real?.onClickCallback = null
        autoBg2_press_real?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg2_press_real?.autoBg = null
        autoBg2_press_real?.view = null
        recycleAutoBg(autoBg2_press_real)
        autoBg2_press_real = null

        autoBg2_hover?.onClickCallback = null
        autoBg2_hover?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg2_hover?.autoBg = null
        autoBg2_hover?.view = null
        recycleAutoBg(autoBg2_hover)
        autoBg2_hover = null

        autoBg2_hover_real?.onClickCallback = null
        autoBg2_hover_real?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg2_hover_real?.autoBg = null
        autoBg2_hover_real?.view = null
        recycleAutoBg(autoBg2_hover_real)
        autoBg2_hover_real = null

        autoBg2_focuse?.onClickCallback = null
        autoBg2_focuse?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg2_focuse?.autoBg = null
        autoBg2_focuse?.view = null
        recycleAutoBg(autoBg2_focuse)
        autoBg2_focuse = null

        autoBg2_focuse_real?.onClickCallback = null
        autoBg2_focuse_real?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg2_focuse_real?.autoBg = null
        autoBg2_focuse_real?.view = null
        recycleAutoBg(autoBg2_focuse_real)
        autoBg2_focuse_real = null

        autoBg2_selected?.onClickCallback = null
        autoBg2_selected?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg2_selected?.autoBg = null
        autoBg2_selected?.view = null
        recycleAutoBg(autoBg2_selected)
        autoBg2_selected = null

        autoBg2_selected_real?.onClickCallback = null
        autoBg2_selected_real?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg2_selected_real?.autoBg = null
        autoBg2_selected_real?.view = null
        recycleAutoBg(autoBg2_selected_real)
        autoBg2_selected_real = null

        autoBg2?.onClickCallback = null
        autoBg2?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg2?.view = null
        autoBg2?.autoBg = null
        recycleAutoBg(autoBg2)
        autoBg2 = null

        autoBg2_real?.onClickCallback = null
        autoBg2_real?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg2_real?.view = null
        autoBg2_real?.autoBg = null
        recycleAutoBg(autoBg2_real)
        autoBg2_real = null

        //fixme 第三张
        autoBg3_notEnable?.onClickCallback = null
        autoBg3_notEnable?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg3_notEnable?.autoBg = null
        autoBg3_notEnable?.view = null
        recycleAutoBg(autoBg3_notEnable)
        autoBg3_notEnable = null

        autoBg3_notEnable_real?.onClickCallback = null
        autoBg3_notEnable_real?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg3_notEnable_real?.autoBg = null
        autoBg3_notEnable_real?.view = null
        recycleAutoBg(autoBg3_notEnable_real)
        autoBg3_notEnable_real = null

        autoBg3_press?.onClickCallback = null
        autoBg3_press?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg3_press?.autoBg = null
        autoBg3_press?.view = null
        recycleAutoBg(autoBg3_press)
        autoBg3_press = null

        autoBg3_press_real?.onClickCallback = null
        autoBg3_press_real?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg3_press_real?.autoBg = null
        autoBg3_press_real?.view = null
        recycleAutoBg(autoBg3_press_real)
        autoBg3_press_real = null

        autoBg3_hover?.onClickCallback = null
        autoBg3_hover?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg3_hover?.autoBg = null
        autoBg3_hover?.view = null
        recycleAutoBg(autoBg3_hover)
        autoBg3_hover = null

        autoBg3_hover_real?.onClickCallback = null
        autoBg3_hover_real?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg3_hover_real?.autoBg = null
        autoBg3_hover_real?.view = null
        recycleAutoBg(autoBg3_hover_real)
        autoBg3_hover_real = null

        autoBg3_focuse?.onClickCallback = null
        autoBg3_focuse?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg3_focuse?.autoBg = null
        autoBg3_focuse?.view = null
        recycleAutoBg(autoBg3_focuse)
        autoBg3_focuse = null

        autoBg3_focuse_real?.onClickCallback = null
        autoBg3_focuse_real?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg3_focuse_real?.autoBg = null
        autoBg3_focuse_real?.view = null
        recycleAutoBg(autoBg3_focuse_real)
        autoBg3_focuse_real = null

        autoBg3_selected?.onClickCallback = null
        autoBg3_selected?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg3_selected?.autoBg = null
        autoBg3_selected?.view = null
        recycleAutoBg(autoBg3_selected)
        autoBg3_selected = null

        autoBg3_selected_real?.onClickCallback = null
        autoBg3_selected_real?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg3_selected_real?.autoBg = null
        autoBg3_selected_real?.view = null
        recycleAutoBg(autoBg3_selected_real)
        autoBg3_selected_real = null

        autoBg3?.onClickCallback = null
        autoBg3?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg3?.view = null
        autoBg3?.autoBg = null
        recycleAutoBg(autoBg3)
        autoBg3 = null

        autoBg3_real?.onClickCallback = null
        autoBg3_real?.autoBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoBg3_real?.view = null
        autoBg3_real?.autoBg = null
        recycleAutoBg(autoBg3_real)
        autoBg3_real = null

        //autoBg常态不能为空。这个常态的view就不要为空。其他状态还要复制这个了。
        invalidate()
        System.gc()//提醒内存回收
    }

    var isRecycleAutoBg: Boolean = true//fixme 是否释放AutoBg;默认释放。之所以加这个控件是因为常用的位图，如返回键就不需要清除。

    override fun onDestroy() {
        super.onDestroy()
        //fixme 图片释放之后，放心不会报错，可以安心使用。
        //fixme 如果是系统的背景图片释放了可能会报错。但是AutoBg不会(亲测，autoBg释放了不会报错)。
        if (isRecycleAutoBg) {
            recycleAutoBg()
            recycleViewBitmap()
        }
        autoView = null
        viewGroup = null
        ball_left = null
        ball_right = null
        isDrawBall = false
    }

    //fixme 新加方法；仅仅只是清除AutoBg位图,url等属性。不会对KAutoBgEntity对象本身制空
    fun recycleAutoBgBitmap() {
        recycleAutoBgBitmap(autoBg_notEnable)
        recycleAutoBgBitmap(autoBg_notEnable_real)
        recycleAutoBgBitmap(autoBg_press)
        recycleAutoBgBitmap(autoBg_press_real)
        recycleAutoBgBitmap(autoBg_hover)
        recycleAutoBgBitmap(autoBg_hover_real)
        recycleAutoBgBitmap(autoBg_focuse)
        recycleAutoBgBitmap(autoBg_focuse_real)
        recycleAutoBgBitmap(autoBg_selected)
        recycleAutoBgBitmap(autoBg_selected_real)
        recycleAutoBgBitmap(autoBg)
        recycleAutoBgBitmap(autoBg_real)
        recycleAutoBgBitmap(autoBg2_notEnable)
        recycleAutoBgBitmap(autoBg2_notEnable_real)
        recycleAutoBgBitmap(autoBg2_press)
        recycleAutoBgBitmap(autoBg2_press_real)
        recycleAutoBgBitmap(autoBg2_hover)
        recycleAutoBgBitmap(autoBg2_hover_real)
        recycleAutoBgBitmap(autoBg2_focuse)
        recycleAutoBgBitmap(autoBg2_focuse_real)
        recycleAutoBgBitmap(autoBg2_selected)
        recycleAutoBgBitmap(autoBg2_selected_real)
        recycleAutoBgBitmap(autoBg2)
        recycleAutoBgBitmap(autoBg2_real)
        recycleAutoBgBitmap(autoBg3_notEnable)
        recycleAutoBgBitmap(autoBg3_notEnable_real)
        recycleAutoBgBitmap(autoBg3_press)
        recycleAutoBgBitmap(autoBg3_press_real)
        recycleAutoBgBitmap(autoBg3_hover)
        recycleAutoBgBitmap(autoBg3_hover_real)
        recycleAutoBgBitmap(autoBg3_focuse)
        recycleAutoBgBitmap(autoBg3_focuse_real)
        recycleAutoBgBitmap(autoBg3_selected)
        recycleAutoBgBitmap(autoBg3_selected_real)
        recycleAutoBgBitmap(autoBg3)
        recycleAutoBgBitmap(autoBg3_real)
    }

    //fixme 对位图和url进行清空。不会对KAutoBgEntity对象本身制空
    private fun recycleAutoBgBitmap(autoBg: KAutoBgEntity?) {
        autoBg?.let {
            it.job_res?.cancel()
            it.job_assets?.cancel()
            it.job_file?.cancel()
            it.job_url?.cancel()
            it.autoBg?.apply {
                if (!isRecycled) {
                    recycle()
                }
            }
            it.autoBg = null
            it.resId = null
            it.assetsPath = null
            it.filePath = null
            it.url = null
            it.job_res = null
            it.job_assets = null
            it.job_file = null
            it.job_file = null
            it.isAutoBging = false
        }
    }

}
package cn.oi.klittle.era.widget

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.base.KBaseView
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.https.bit.KBitmaps
import cn.oi.klittle.era.utils.KAssetsUtils
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.runOnUiThread

//                    调用案例
//                    kscrollView {
//                        isHasEditTextView = true//fixme 解决弹性滑动时；输入框软键盘弹出的问题
//                        openUpAnime=true//开启上拉弹性效果(默认就是开启的)
//                        openDownAnime=true//开启下拉弹性效果
//                        //maxMoveHeightDrop_Down//最大下拉高度(0,默认就是整个控件的高度。)
//                        //maxMoveHeightDrop_Up 最大上拉高度
//                        onScrollChanged { x, y, oldx, oldy ->
//                            //滑动时监听
//                        }
//                        dropUp{
//                            //上拉监听
//                        }
//                        dropDown {
//                            //下拉监听
//                        }
//                        //autoMatrixBg() 拉伸图片
//                        //isAutoMatris 图片是否进行拉伸。默认拉伸
//                        //maxAutoMatrixBgScaleSeed 图片拉伸率。数字越大。变化越大。2是等距离变大
//                        verticalLayout {
//                            //fixme 最外层只能有一个元素;切记！！
//                        }
//                    }.lparams {
//                        width= matchParent
//                        height= matchParent
//                    }

/**
 * 背景颜色渐变的弹性ScrollView
 */
open class KGradientScrollView : KBounceScrollView {

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
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    open fun background(resId: Int) {
        setBackgroundResource(resId)
    }

    open fun background(bitmap: Bitmap) {
        if (Build.VERSION.SDK_INT >= 16) {
            background = BitmapDrawable(bitmap)
        } else {
            backgroundDrawable = BitmapDrawable(bitmap)
        }
    }

    //fixme 水平渐变颜色数组值【均匀渐变】
    var horizontalColors: IntArray? = null

    open fun horizontalColors(vararg color: Int) {
        horizontalColors = color
    }

    open fun horizontalColors(vararg color: String) {
        horizontalColors = IntArray(color.size)
        horizontalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }

    //fixme 垂直渐变颜色数组值【均匀】
    var verticalColors: IntArray? = null

    open fun verticalColors(vararg color: Int) {
        verticalColors = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    open fun verticalColors(vararg color: String) {
        verticalColors = IntArray(color.size)
        verticalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
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
    fun bottom_color(bottom_color: Int) {
        this.bottom_color = bottom_color
    }

    open fun bottom_color(bottom_color: String) {
        this.bottom_color = Color.parseColor(bottom_color)
    }

    var left_color = Color.TRANSPARENT//fixme 左半部分颜色
    open fun left_color(left_color: Int) {
        this.left_color = left_color
    }

    fun left_color(left_color: String) {
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

    override fun draw(canvas: Canvas?) {
        canvas?.apply {
            var paint = KBaseView.getPaint()
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
                var shader = LinearGradient(0f, 0f, width.toFloat(), 0f, it, null, Shader.TileMode.MIRROR)
                paint.setShader(shader)
                drawPaint(paint)
            }

            //fixme 水平渐变 和 垂直渐变 效果会叠加。垂直覆盖在水平的上面。

            //垂直渐变
            verticalColors?.let {
                var shader = LinearGradient(0f, 0f, 0f, height.toFloat(), it, null, Shader.TileMode.MIRROR)
                paint.setShader(shader)
                drawPaint(paint)
            }

            //fixme 画拉伸图片。backgroundColor=Color.RED背景色会覆盖拉伸图片。
            //fixme scrollview移动的时候。画布也在一起移动。所以拉伸图片也会随scrollview一起滚动。
            drawAutoMatrixBg(canvas, paint)
        }
        super.draw(canvas)//在下面。不然内容会被覆盖【这里是ScrollView内部的子控件】

        canvas?.apply {
            //顶部渐变
            top_gradient_color?.let {
                if (top_gradient_height > 0) {
                    var paint = Paint()
                    paint.isAntiAlias = true
                    paint.isDither = true
                    paint.style = Paint.Style.FILL_AND_STROKE
                    var shader = LinearGradient(0f, 0f, 0f, top_gradient_height, it, null, Shader.TileMode.CLAMP)
                    paint.setShader(shader)
                    drawRect(RectF(0f, 0f, width.toFloat(), top_gradient_height), paint)
                }
            }

            //底部渐变
            bottom_gradient_color?.let {
                if (bottom_gradient_height > 0) {
                    var paint = Paint()
                    paint.isAntiAlias = true
                    paint.isDither = true
                    paint.style = Paint.Style.FILL_AND_STROKE
                    var shader = LinearGradient(0f, height.toFloat() - bottom_gradient_height, 0f, height.toFloat(), it, null, Shader.TileMode.CLAMP)
                    paint.setShader(shader)
                    drawRect(RectF(0f, height.toFloat() - bottom_gradient_height, width.toFloat(), height.toFloat()), paint)
                }
            }
        }

        canvas?.let {
            draw?.let {
                var paint = Paint()
                paint.isAntiAlias = true
                paint.isDither = true
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.strokeWidth = 0f
                it(canvas, paint)
            }
        }

    }

    //fixme 顶部渐变颜色,如：top_gradient_color("#ffffff","#00ffffff") 白色渐变,颜色是均匀变化的
    var top_gradient_color: IntArray? = null
    //fixme 顶部渐变高度
    var top_gradient_height: Float = 0f

    open fun top_gradient_color(vararg color: Int) {
        top_gradient_color = color
    }

    open fun top_gradient_color(vararg color: String) {
        top_gradient_color = IntArray(color.size)
        top_gradient_color?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }
    }

    //fixme 底部渐变颜色，如：bottom_gradient_color("#00ffffff","#ffffff") 白色渐变,颜色是均匀变化的
    var bottom_gradient_color: IntArray? = null
    //fixme 底部渐变高度
    var bottom_gradient_height: Float = 0f

    open fun bottom_gradient_color(vararg color: Int) {
        bottom_gradient_color = color
    }

    open fun bottom_gradient_color(vararg color: String) {
        bottom_gradient_color = IntArray(color.size)
        bottom_gradient_color?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }
    }


    //自定义画布，根据需求。自主实现
    open var draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //自定义，重新绘图
    open fun draw(draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null): KGradientScrollView {
        this.draw = draw
        postInvalidate()//刷新
        return this
    }

    //画自己【onDraw在draw()的流程里面，即在它的前面执行】
    var onDraw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //画自己
    open fun onDraw_(onDraw: ((canvas: Canvas, paint: Paint) -> Unit)? = null): KGradientScrollView {
        this.onDraw = onDraw
        postInvalidate()//刷新
        return this
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            onDraw?.let {
                var paint = Paint()
                paint.isAntiAlias = true
                paint.isDither = true
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.strokeWidth = 0f
                it(canvas, paint)
            }
        }
    }

    //fixme ========================================================================================以下是图片拉伸效果

    var w: Int = 0
        //获取控件的真实宽度
        get() {
            var w = width
            if (layoutParams != null && layoutParams.width > w) {
                w = layoutParams.width
            }
            return w
        }

    var h: Int = 0
        //获取控件的真实高度
        get() {
            var h = height
            if (layoutParams != null && layoutParams.height > h) {
                h = layoutParams.height
            }
            return h
        }

    var autoLeftPadding = 0f//左补丁(负数也有效哦)
    var autoTopPadding = 0f//上补丁
    var isAutoCenter = false
        //位图是否居中,（水平+垂直居中）
        set(value) {
            field = value
            if (field) {
                isAutoCenterHorizontal = false
                isAutoCenterVertical = false
            }
        }
    var isAutoCenterHorizontal = true
        //fixme 水平居中,默认
        set(value) {
            field = value
            if (field) {
                isAutoCenter = false
                isAutoCenterVertical = false
            }
        }
    var isAutoCenterVertical = false
        //垂直居中
        set(value) {
            field = value
            if (field) {
                isAutoCenter = false
                isAutoCenterHorizontal = false
            }
        }

    //这个背景图片，会铺满整个控件。不会对位图进行适配。只会对图片矩阵（拉伸）处理。就和背景图片一样
    private var autoMatrixBg: Bitmap? = null

    //设置控件的高度和高度
    // matchParent:	-1 wrapContent:	-2
    open fun layoutParams(width: Int, height: Int) {
        layoutParams?.apply {
            //设置宽和高
            this.width = width
            this.height = height
            requestLayout()
        }
    }

    //记录autoMatrixBg拉伸之后的宽度和高度
    var autoMatrixBgWidth: Int = 0
    var autoMatrixBgHeight: Int = 0
    //设置矩阵的宽和高，不是图片。对图片继续拉伸处理
    //fixme 设置矩阵拉伸后的宽度和高度,参数Int是实际拉伸后的宽度和高度
    open fun autoMatrixBgScale(width: Int = this.w, height: Int = this.h) {
        autoMatrixBg?.let {
            if (!it.isRecycled) {
                autoMatrixBgWidth = width
                autoMatrixBgHeight = height
                invalidate()
            }
        }
    }

    //fixme 设置矩阵拉伸后的比率。1是原图的比率。,参数Float是实际拉伸后的比率。实际宽度和高度。会自行计算
    open fun autoMatrixBgScale(sx: Float = 1f, sy: Float = 1f) {
        autoMatrixBg?.apply {
            if (!isRecycled) {
                autoMatrixBgWidth = (width * sx).toInt()
                autoMatrixBgHeight = (height * sy).toInt()
                invalidate()
            }
        }
    }

    /**
     * 拉伸图片。会随ScrollView的滚动而滚动。因为scrollView滚动的时候。整个画布也在移动。
     */
    open fun drawAutoMatrixBg(canvas: Canvas, paint: Paint) {
        autoMatrixBg?.apply {
            if (!isRecycled) {
                paint.isAntiAlias = true
                paint.isDither = true
                if (autoMatrixBgWidth <= 0) {
                    autoMatrixBgWidth = width
                }
                if (autoMatrixBgHeight <= 0) {
                    autoMatrixBgHeight = height
                }
                var offsetLeft = (autoMatrixBgWidth - width) / 2
                var offsetTop = (autoMatrixBgHeight - height) / 2
                if (isAutoCenter) {
                    //canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                    var left = kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding - offsetLeft
                    var top = kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding - offsetTop
                    var right = left + autoMatrixBgWidth
                    var bottom = top + autoMatrixBgHeight
                    canvas.drawBitmap(this, null, RectF(left, top, right, bottom), paint)
                } else if (isAutoCenterHorizontal) {
                    //canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, autoTopPadding, paint)
                    var left = kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding - offsetLeft
                    var top = autoTopPadding - offsetTop
                    var right = left + autoMatrixBgWidth
                    var bottom = top + autoMatrixBgHeight
                    canvas.drawBitmap(this, null, RectF(left, top, right, bottom), paint)
                } else if (isAutoCenterVertical) {
                    //canvas.drawBitmap(this, autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                    var left = autoLeftPadding - offsetLeft
                    var top = kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding - offsetTop
                    var right = left + autoMatrixBgWidth
                    var bottom = top + autoMatrixBgHeight
                    canvas.drawBitmap(this, null, RectF(left, top, right, bottom), paint)
                } else {
                    //canvas.drawBitmap(this, autoLeftPadding, autoTopPadding, paint)
                    var left = autoLeftPadding - offsetLeft
                    var top = autoTopPadding - offsetTop
                    var right = left + autoMatrixBgWidth
                    var bottom = top + autoMatrixBgHeight
                    canvas.drawBitmap(this, null, RectF(left, top, right, bottom), paint)
                }
            }
        }
    }

    fun autoMatrixBg(bitmap: Bitmap?) {
        this.autoMatrixBg = bitmap
        if (context != null && context is Activity) {
            context.runOnUiThread {
                invalidate()
            }
        }
    }

    fun autoMatrixBg(resId: Int, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        this.autoMatrixBg = KAssetsUtils.getInstance().getBitmapFromAssets(null, resId, isRGB_565)
        if (width >= 0 && height >= 0) {
            autoMatrixBg?.let {
                if (!it.isRecycled) {
                    autoMatrixBg = kpx.xBitmap(it, width, height, true)
                }
            }
        }
        if (context != null && context is Activity) {
            context.runOnUiThread {
                invalidate()
            }
        }
    }

    fun autoMatrixBgFromAssets(assetsPath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        this.autoMatrixBg = KAssetsUtils.getInstance().getBitmapFromAssets(assetsPath, 0, isRGB_565)
        if (width >= 0 && height >= 0) {
            autoMatrixBg?.let {
                if (!it.isRecycled) {
                    autoMatrixBg = kpx.xBitmap(it, width, height, true)
                }
            }
        }
        if (context != null && context is Activity) {
            context.runOnUiThread {
                invalidate()
            }
        }
    }

    /**
     * @param url 网络地址
     * @param width 位图的宽度,默认0是服务器原图的尺寸（之后不会对位图进行适配，只会进行拉伸处理）。
     * @param height 位图的高度
     * @param isLoad 是否显示进度条
     * @param isRepeat 网络是否允许重复加载
     */
    fun autoMatrixBgFromUrl(url: String?, width: Int = 0, height: Int = 0, isLoad: Boolean = false, isRepeat: Boolean = false, finish: ((bitmap: Bitmap) -> Unit)? = null) {
        //Log.e("test", "宽度和高度:\t" + width + "\t" + height)
        if (isLoad && context != null && context is Activity) {
            KBitmaps(url).isOptionsRGB_565(false).isShowLoad(true).activity(context as Activity).isRepeatRequest(isRepeat).width(width).height(height).get() {
                autoMatrixBg = it
                if (context != null && context is Activity) {
                    context.runOnUiThread {
                        invalidate()
                        finish?.let {
                            autoMatrixBg?.apply {
                                if (!isRecycled) {
                                    it(this)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            KBitmaps(url).isOptionsRGB_565(false).isShowLoad(false).isRepeatRequest(isRepeat).width(width).height(height).get() {
                //Log.e("test", "成功:\t" + it.width)
                autoMatrixBg = it
                if (context != null && context is Activity) {
                    context.runOnUiThread {
                        invalidate()
                        finish?.let {
                            autoMatrixBg?.apply {
                                if (!isRecycled) {
                                    it(this)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    var isAutoMatris: Boolean = true//图片是否进行拉伸。默认拉伸
    //fixme scrollview移动的时候。画布也在一起移动。所以拉伸图片也会随scrollview一起滚动。
    // fixme 所以，再次就做下拉放大的效果。上滑的效果。基本不需要（图片都滑上去了，都看不到了）。所以就不做了。
    var maxAutoMatrixBgScaleSeed = 2f//fixme 图片拉伸率。数字越大。变化越大。2是等距离变大(滑动多少就变大多少)。不过感觉还为1的时候，效果最好。

    //fixme 下拉时，图片会放大。下拉越大。图片拉伸越大。
    //fixme 点击事件，直接在scrollview布局顶部添加一个透明控件。给改控件添加事件即可。
    override fun onDropDownAutoMatrixBg(distance: Int) {
        super.onDropDownAutoMatrixBg(distance)
        autoMatrixBg?.let {
            if (isAutoMatris && !it.isRecycled) {
                //var sx=(distance.toFloat()/maxMoveHeightDrop_Down.toFloat())*maxAutoMatrixBgScale+1
                //Log.e("test","拉伸比例:\t"+sx)
                var height2 = it.height + distance * maxAutoMatrixBgScaleSeed
                var width2 = it.width * (height2.toFloat() / it.height.toFloat())
                //Log.e("test","距离:\t"+distance+"\t高度:\t"+it.height+"\tmaxAutoMatrixBgScaleSeed:\t"+maxAutoMatrixBgScaleSeed)
                autoMatrixBgScale(width2.toInt(), height2.toInt())
            }
        }
    }

}
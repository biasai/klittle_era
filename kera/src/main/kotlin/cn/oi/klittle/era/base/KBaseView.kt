package cn.oi.klittle.era.base

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KTimerUtils
import cn.oi.klittle.era.utils.KSelectorUtils
import cn.oi.klittle.era.R
import cn.oi.klittle.era.utils.KRadiusUtils
import cn.oi.klittle.era.https.bit.KBitmaps
import cn.oi.klittle.era.utils.KAssetsUtils
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.runOnUiThread


/**
 * 无论是自定义view还是普通的layout布局。都不能在async和launch协程里面初始化，要么报错，要么不显示。
 * fixme 从现在开始。主要更新KView,不在更新KRadiusButton，KRadiusEditText，KRadiusRelativeLayout，KRadiusTextView。同时更新五个太累了。功能只要有一个有就够了。
 */
open class KBaseView : View {

    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//默认就开启硬件加速，不然圆角无效果
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

    //默认开启硬件加速
    constructor(context: Context?, HARDWARE: Boolean = true) : super(context) {
        if (HARDWARE) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//默认硬件加速
        context?.let {
            val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.RoundCornersRect)
            typedArray?.let {
                var all_radius = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_all, 0f)
                left_top = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_left_top, all_radius)
                left_bottom = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_left_bottom, all_radius)
                right_top = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_right_top, all_radius)
                right_bottom = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_right_bottom, all_radius)
            }
        }
    }

    open fun background(color: String) {
        setBackgroundColor(Color.parseColor(color))
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


    // 两次点击按钮之间的点击间隔不能少于1000毫秒（即1秒）
    var MIN_CLICK_DELAY_TIME = 1000
    var lastClickTime: Long = System.currentTimeMillis()//记录最后一次点击时间

    //判断是否快速点击，true是快速点击，false不是
    open fun isFastClick(): Boolean {
        var flag = false
        var curClickTime = System.currentTimeMillis()
        if ((curClickTime - lastClickTime) <= MIN_CLICK_DELAY_TIME) {
            flag = true//快速点击
        }
        lastClickTime = curClickTime
        return flag
    }

    private var onClickes = mutableListOf<() -> Unit>()
    private var hasClick = false//判断是否已经添加了点击事情。
    //fixme 自定义点击事件，可以添加多个点击事情。互不影响,isMul是否允许添加多个点击事件。默认不允许
    open fun onClick(isMul: Boolean = false, onClick: () -> Unit) {
        if (!hasClick) {
            isClickable = true//设置具备点击能力
            //点击事件
            setOnClickListener {
                //fixme 防止快速点击
                if (!isFastClick()) {
                    for (i in onClickes) {
                        i?.let {
                            it()//点击事件
                        }
                    }
                }
            }
            hasClick = true
        }
        if (!isMul) {
            //不允许添加多个点击事件
            onClickes.clear()//清除之前的点击事件
        }
        onClickes.add(onClick)
    }

    //触摸点击效果。默认具备波浪效果
    open fun onPress(isRipple: Boolean = true) {
        Companion.onPress(this, isRipple)
    }

    var bindView: View? = null//状态绑定的View
        set(value) {
            field = value
            if (value != null) {
                if (value is KBaseView) {
                    if (value.bindView == null) {
                        value.bindView = this//相互绑定
                    }
                }
            }
        }

    fun bindView(bindView: View?) {
        this.bindView = bindView
    }

    //状态同步
    fun bindSycn() {
        bindView?.let {
            it.isSelected = isSelected
            it.isPressed = isPressed
        }
    }

    //fixme selectorDrawable(R.mipmap.p_dont_agree,null, R.mipmap.p_agree)
    //fixme 注意，如果要用选中状态，触摸状态最好设置为null空。不会有卡顿冲突。
    //重写选中状态。isSelected=true。选中状态。一定要手动调用。
    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        bindView?.let {
            if (it.isSelected != isSelected) {
                it?.isSelected = isSelected//选中状态
            }
        }
        onSelectChangedList.forEach {
            it?.let {
                it(selected)//选中监听
            }
        }
    }

    //fixme 监听选中状态。防止多个监听事件冲突，所以添加事件数组。
    private var onSelectChanged: ((selected: Boolean) -> Unit)? = null
    private var onSelectChangedList = mutableListOf<((selected: Boolean) -> Unit)?>()
    fun addSelected(onSelectChanged: ((selected: Boolean) -> Unit)) {
        onSelectChanged.let {
            onSelectChangedList?.add(it)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        var b = super.dispatchTouchEvent(event)
        //防止点击事件冲突。所以。一定要放到super()后面。
        event?.let {
            when (it.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_MOVE -> {
                    bindView?.isPressed = true//按下状态
                    isPressed = true
                    invalidate()//视图刷新。
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    bindView?.isPressed = false
                    isPressed = false
                    invalidate()
                }
                MotionEvent.ACTION_CANCEL -> {
                    //其他异常
                    bindView?.isPressed = false
                    invalidate()
                }
            }
        }
        return b
    }

    //fixme 自定义画布，根据需求。自主实现
    protected open var draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //自定义，重新绘图
    open fun draw(draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null): KBaseView {
        this.draw = draw
        postInvalidate()//刷新
        return this
    }

    //fixme 什么都不做，交给子类去实现绘图
    //fixme 之所以会有这个方法。是为了保证自定义的 draw和onDraw的执行顺序。始终是在最后。
    protected open fun draw2(canvas: Canvas, paint: Paint) {}

    protected open fun onDraw2(canvas: Canvas, paint: Paint) {}

    var all_radius: Float = 0F//默认，所有圆角的角度
    var left_top: Float = 0f//左上角
    var left_bottom: Float = 0f//左下角
    var right_top = 0f//右上角
    var right_bottom = 0f//右下角

    var kStrokeWidth = 0f//边框宽度
    var kStrokeColor = Color.TRANSPARENT//边框颜色

    //fixme 边框颜色渐变
    var kStrokeGradientStartColor = Color.TRANSPARENT//渐变开始颜色
    var kStrokeGradientEndColor = Color.TRANSPARENT//渐变结束颜色
    //fixme 渐变颜色数组值【均匀渐变】，gradientColors优先
    var kStrokeGradientColors: IntArray? = null
    var K_ORIENTATION_VERTICAL = 0//垂直
    var K_ORIENTATION_HORIZONTAL = 1//水平
    var kStrokeGradientOritation = K_ORIENTATION_HORIZONTAL//渐变颜色方向，默认水平

    fun kStrokeGradientColors(vararg color: Int) {
        kStrokeGradientColors = color
    }

    fun kStrokeGradientColors(vararg color: String) {
        kStrokeGradientColors = IntArray(color.size)
        kStrokeGradientColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }
    }

    //fixme 清空原始背景
    open fun clearBackground() {
        if (Build.VERSION.SDK_INT >= 16) {
            backgroundColor = Color.TRANSPARENT
            background = null
        } else {
            backgroundColor = Color.TRANSPARENT
            backgroundDrawable = null
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
                if (isAutoWH) {
                    requestLayout()
                } else {
                    invalidate()
                }
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
                if (isAutoWH) {
                    requestLayout()
                } else {
                    invalidate()
                }
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
                if (isAutoWH) {
                    requestLayout()
                } else {
                    invalidate()
                }
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
                        if (isAutoWH) {
                            requestLayout()
                        } else {
                            invalidate()
                        }
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
                        if (isAutoWH) {
                            requestLayout()
                        } else {
                            invalidate()
                        }
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


    //这个背景图片，会铺满整个控件
    private var autoUrlBg: Bitmap? = null//fixme 自定义网络背景图片,对图片是否为空，是否释放，做了判断。防止奔溃。比原生的背景图片更安全。

    fun autoUrlBg(bitmap: Bitmap?) {
        this.autoUrlBg = bitmap
        if (context != null && context is Activity) {
            context.runOnUiThread {
                if (isAutoWH) {
                    requestLayout()
                } else {
                    invalidate()
                }
            }
        }
    }

    fun autoUrlBg(resId: Int, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        this.autoUrlBg = KAssetsUtils.getInstance().getBitmapFromAssets(null, resId, isRGB_565)
        if (width >= 0 && height >= 0) {
            autoUrlBg?.let {
                if (!it.isRecycled) {
                    autoUrlBg = kpx.xBitmap(it, width, height, true)
                }
            }
        }
        if (context != null && context is Activity) {
            context.runOnUiThread {
                if (isAutoWH) {
                    requestLayout()
                } else {
                    invalidate()
                }
            }
        }
    }

    fun autoUrlBgFromAssets(assetsPath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        this.autoUrlBg = KAssetsUtils.getInstance().getBitmapFromAssets(assetsPath, 0, isRGB_565)
        if (width >= 0 && height >= 0) {
            autoUrlBg?.let {
                if (!it.isRecycled) {
                    autoUrlBg = kpx.xBitmap(it, width, height, true)
                }
            }
        }
        if (context != null && context is Activity) {
            context.runOnUiThread {
                invalidate()
            }
        }
    }

    //fixme 防止无法获取宽和高，所以延迟100毫秒，这样就能获取控件的宽度和高度了。
    //fixme 肉眼对200毫秒内变化是感觉不出来的。
    fun autoUrlBgDelay(url: String?, delay: Long = 100) {
        if (w <= 0 || h <= 0) {
            //无法获取宽度和高度，就延迟再获取
            async {
                kotlinx.coroutines.experimental.delay(delay)
                autoUrlBg(url)
            }
        } else {
            autoUrlBg(url)
        }
    }

    /**
     * url 网络图片地址
     * isLoad 是否显示进度条，默认不显示
     * isRepeat 是否允许重复加载（网络重复请求）
     * fixme width,height位图的宽和高(最好手动设置一下，或者延迟一下，不能无法获取宽和高)
     */
    fun autoUrlBg(url: String?, isLoad: Boolean = false, isRepeat: Boolean = false, width: Int = this.w, height: Int = this.h, finish: ((bitmap: Bitmap) -> Unit)? = null) {
        //Log.e("test", "宽度和高度:\t" + width + "\t" + height)
        if (isLoad && context != null && context is Activity) {
            KBitmaps(url).isOptionsRGB_565(false).isShowLoad(true).activity(context as Activity).isRepeatRequest(isRepeat).width(width).height(height).get() {
                autoUrlBg = it
                if (context != null && context is Activity) {
                    context.runOnUiThread {
                        if (isAutoWH) {
                            requestLayout()
                        } else {
                            invalidate()
                        }
                        finish?.let {
                            autoUrlBg?.apply {
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
                autoUrlBg = it
                if (context != null && context is Activity) {
                    context.runOnUiThread {
                        if (isAutoWH) {
                            requestLayout()
                        } else {
                            invalidate()
                        }
                        finish?.let {
                            autoUrlBg?.apply {
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

    /**
     * 重新自定义背景图片(为了适配正确，位图最好都放在nodpi文件夹里。)
     */
    private var autoDefaultBg: Bitmap? = null//fixme 默认图片

    fun autoDefaultBg(bitmap: Bitmap?) {
        this.autoDefaultBg = bitmap
        if (context != null && context is Activity) {
            context.runOnUiThread {
                if (isAutoWH) {
                    requestLayout()
                } else {
                    invalidate()
                }
            }
        }
    }

    fun autoDefaultBg(resId: Int, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBg = KAssetsUtils.getInstance().getBitmapFromAssets(null, resId, isRGB_565)
        autoDefaultBg?.let {
            autoDefaultBg = kpx.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        } else {
            invalidate()
        }
    }

    fun autoDefaultBg(assetsPath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBg = KAssetsUtils.getInstance().getBitmapFromAssets(assetsPath, 0, isRGB_565)
        autoDefaultBg?.let {
            autoDefaultBg = kpx.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        } else {
            invalidate()
        }
    }

    private var autoPressBg: Bitmap? = null//fixme 按下图片
    fun autoPressBg(bitmap: Bitmap?) {
        this.autoPressBg = bitmap
        if (context != null && context is Activity) {
            context.runOnUiThread {
                if (isAutoWH) {
                    requestLayout()
                } else {
                    invalidate()
                }
            }
        }
    }

    fun autoPressBg(resId: Int, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoPressBg = KAssetsUtils.getInstance().getBitmapFromAssets(null, resId, isRGB_565)
        autoPressBg?.let {
            autoPressBg = kpx.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        } else {
            invalidate()
        }
        isClickable = true//具备点击能力
    }

    fun autoPressBg(assetsPath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoPressBg = KAssetsUtils.getInstance().getBitmapFromAssets(assetsPath, 0, isRGB_565)
        autoPressBg?.let {
            autoPressBg = kpx.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        } else {
            invalidate()
        }
        isClickable = true//具备点击能力
    }

    private var autoSelectBg: Bitmap? = null//fixme 选中图片（优先级最高）
    fun autoSelectBg(bitmap: Bitmap?) {
        this.autoSelectBg = bitmap
        if (context != null && context is Activity) {
            context.runOnUiThread {
                if (isAutoWH) {
                    requestLayout()
                } else {
                    invalidate()
                }
            }
        }
    }


    fun autoSelectBg(resId: Int, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoSelectBg = KAssetsUtils.getInstance().getBitmapFromAssets(null, resId, isRGB_565)
        autoSelectBg?.let {
            autoSelectBg = kpx.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        } else {
            invalidate()
        }
        isClickable = true//具备点击能力
    }

    fun autoSelectBg(assetsPath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoSelectBg = KAssetsUtils.getInstance().getBitmapFromAssets(assetsPath, 0, isRGB_565)
        autoSelectBg?.let {
            autoSelectBg = kpx.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        } else {
            invalidate()
        }
        isClickable = true//具备点击能力
    }


    //fixme 防止触摸状态和选中状态冲突，会出现一闪的情况。把触摸状态制空。
    //fixme autoBg(R.mipmap.p_second_gou_gay,null, R.mipmap.p_second_gou_blue)
    fun autoBg(default: Int, press: Int? = default, select: Int? = press, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBg(default, width, height, isRGB_565)
        if (press == default) {
            autoPressBg = autoDefaultBg
        } else {
            press?.apply {
                autoPressBg(this, width, height, isRGB_565)
                isClickable = true//具备点击能力
            }
        }
        if (press == select) {
            autoSelectBg = autoPressBg
        } else {
            select?.apply {
                autoSelectBg(this, width, height, isRGB_565)
                isClickable = true//具备点击能力
            }
        }
    }

    fun autoBg(default: String, press: String? = default, select: String? = press, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBg(default, width, height, isRGB_565)
        if (press == default || press.equals(default)) {
            autoPressBg = autoDefaultBg
        } else {
            press?.apply {
                autoPressBg(this, width, height, isRGB_565)
                isClickable = true//具备点击能力
            }
        }
        if (press == select || press.equals(select)) {
            autoSelectBg = autoPressBg
        } else {
            select?.apply {
                autoSelectBg(this, width, height, isRGB_565)
                isClickable = true//具备点击能力
            }
        }
    }

    //fixme 来自sd卡,普通
    fun autoDefaultBgFromFile(filePath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBg = KAssetsUtils.getInstance().getBitmapFromFile(filePath, isRGB_565)
        autoDefaultBg?.let {
            autoDefaultBg = kpx.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        } else {
            invalidate()
        }
    }

    //fixme 来自sd卡,触摸
    fun autoPressBgFromFile(filePath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoPressBg = KAssetsUtils.getInstance().getBitmapFromFile(filePath, isRGB_565)
        autoPressBg?.let {
            autoPressBg = kpx.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        } else {
            invalidate()
        }
        isClickable = true//具备点击能力
    }

    //fixme 来自sd卡,选中
    fun autoSelectBgFromFile(filePath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoSelectBg = KAssetsUtils.getInstance().getBitmapFromFile(filePath, isRGB_565)
        autoSelectBg?.let {
            autoSelectBg = kpx.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        } else {
            invalidate()
        }
        isClickable = true//具备点击能力
    }

    //fixme 来自sd卡,普通，触摸，选中
    fun autoBgFromFile(default: String, press: String? = default, select: String? = press, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBgFromFile(default, width, height, isRGB_565)//fixme 普通
        if (press == default || press.equals(default)) {
            autoPressBg = autoDefaultBg
        } else {
            press?.apply {
                autoPressBgFromFile(this, width, height, isRGB_565)//fixme 触摸
                isClickable = true//具备点击能力
            }
        }
        if (press == select || press.equals(select)) {
            autoSelectBg = autoPressBg
        } else {
            select?.apply {
                autoSelectBgFromFile(this, width, height, isRGB_565)//fixme 选中
                isClickable = true//具备点击能力
            }
        }
    }

    var isAutoWH = true//fixme 控件的宽度和高度是否为自定义位图的宽度和高度。默认是
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var w = 0
        var h = 0
        if (isAutoWH) {
            autoDefaultBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoPressBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoSelectBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoUrlBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoMatrixBg?.apply {
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

    var autoLeftPadding = 0f//左补丁(负数也有效哦)
    var autoTopPadding = 0f//上补丁
    var isAutoCenter = true//位图是否居中,默认居中（水平+垂直居中）
        set(value) {
            field = value
            if (field) {
                isAutoCenterHorizontal = false
                isAutoCenterVertical = false
            }
        }
    var isAutoCenterHorizontal = false//水平居中
        set(value) {
            field = value
            if (field) {
                isAutoCenter = false
                isAutoCenterVertical = false
            }
        }
    var isAutoCenterVertical = false//垂直居中
        set(value) {
            field = value
            if (field) {
                isAutoCenter = false
                isAutoCenterHorizontal = false
            }
        }

    open var isRecycleAutoUrlBg: Boolean = true//图片适配时，是否释放原位图。
    //画自定义背景
    open fun drawAutoBg(canvas: Canvas) {
        if (w <= 0 || h <= 0) {
            return
        }
        var paint = KBaseView.getPaint()
        //Log.e("test", "网络位图:\t" + autoUrlBg?.width + "\t" + autoUrlBg?.isRecycled)
        //网络背景位图（铺满整个背景控件）
        autoUrlBg?.apply {
            if (!isRecycled) {
                if (width != w || height != h) {
                    autoUrlBg = kpx.xBitmap(this, w, h, isRecycleAutoUrlBg)//位图和控件拉伸到一样大小
                    autoUrlBg?.apply {
                        if (!isRecycled) {
                            canvas.drawBitmap(this, 0f, 0f, paint)
                        }
                    }
                } else {
                    canvas.drawBitmap(this, 0f, 0f, paint)
                }
            }
        }
        //拉伸图片
        drawAutoMatrixBg(canvas, paint)
        //Log.e("test", "isSelected:\t" + isSelected + "\tisPress：\t" + isPressed)
        if (isSelected && autoSelectBg != null) {
            //选中状态图片,优先级最高
            autoSelectBg?.apply {
                if (!isRecycled) {
                    if (isAutoCenter) {
                        canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                    } else if (isAutoCenterHorizontal) {
                        canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, autoTopPadding, paint)
                    } else if (isAutoCenterVertical) {
                        canvas.drawBitmap(this, autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                    } else {
                        canvas.drawBitmap(this, autoLeftPadding, autoTopPadding, paint)
                    }
                }
            }
        } else {
            if (isPressed && autoPressBg != null) {
                //按下状态
                autoPressBg?.apply {
                    if (!isRecycled) {
                        if (isAutoCenter) {
                            canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                        } else if (isAutoCenterHorizontal) {
                            canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, autoTopPadding, paint)
                        } else if (isAutoCenterVertical) {
                            canvas.drawBitmap(this, autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                        } else {
                            canvas.drawBitmap(this, autoLeftPadding, autoTopPadding, paint)
                        }
                    }
                }
            } else {
                //普通状态
                autoDefaultBg?.apply {
                    if (!isRecycled) {
                        if (isAutoCenter) {
                            canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                        } else if (isAutoCenterHorizontal) {
                            canvas.drawBitmap(this, kpx.centerBitmapX(this, w.toFloat()) + autoLeftPadding, autoTopPadding, paint)
                        } else if (isAutoCenterVertical) {
                            canvas.drawBitmap(this, autoLeftPadding, kpx.centerBitmapY(this, h.toFloat()) + autoTopPadding, paint)
                        } else {
                            canvas.drawBitmap(this, autoLeftPadding, autoTopPadding, paint)
                        }
                    }
                }
            }
        }
    }

    //释放位图
    fun recycleAutoBg() {
        autoDefaultBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoDefaultBg = null
        autoPressBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoPressBg = null
        autoSelectBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoSelectBg = null
        autoUrlBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoUrlBg = null
        autoMatrixBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoMatrixBg = null
        invalidate()
        System.gc()//提醒内存回收
    }


    var baseAfterDrawRadius = true//fixme 圆角边框是否最后画。默认最后画。不管是先画，还是后面。总之都在背景上面。背景最底层。

    override fun draw(canvas: Canvas?) {
        if (Build.VERSION.SDK_INT <= 19 && (left_top > 0 || left_bottom > 0 || right_top > 0 || right_bottom > 0 || all_radius > 0)) {//19是4.4系统。这个系统已经很少了。基本上也快淘汰了。
            //防止4.4及以下的系统。背景出现透明黑框。
            //只能解决。父容器有背景颜色的时候。如果没有背景色。那就没有办法了。
            var color = getParentColor(this)
            canvas?.drawColor(color)//必不可少，不能为透明色。
            canvas?.saveLayerAlpha(RectF(0f, 0f, w.toFloat(), h.toFloat()), 255, Canvas.ALL_SAVE_FLAG)//必不可少，解决透明黑框。
        }
        super.draw(canvas)
        //画自定义背景
        canvas?.let {
            drawAutoBg(it)
        }
        //圆角，边框最先画。
        if (!baseAfterDrawRadius) {
            drawRadius(canvas)
        }
        canvas?.let {
            draw2(it, getPaint())
            draw?.let {
                it(canvas, getPaint())
            }
            //画水平进度
            drawHorizontalProgress?.let {
                it(canvas, getPaint(), w * horizontalProgress / 100f)
            }
            //画垂直进度
            drawVerticalProgress?.let {
                it(canvas, getPaint(), h - h * verticalProgress / 100f)//方向从下往上。
            }
        }
        //圆角，边框最后画。
        if (baseAfterDrawRadius) {
            drawRadius(canvas)
        }
    }


    var kradius = KRadiusUtils()
    //画边框，圆角
    fun drawRadius(canvas: Canvas?) {
        this.let {
            kradius.apply {
                x = 0f
                y = 0f
                w = it.w
                h = it.h
                all_radius = it.all_radius
                left_top = it.left_top
                left_bottom = it.left_bottom
                right_top = it.right_top
                right_bottom = it.right_bottom
                strokeWidth = it.kStrokeWidth
                strokeColor = it.kStrokeColor
                strokeGradientStartColor = it.kStrokeGradientStartColor
                strokeGradientEndColor = it.kStrokeGradientEndColor
                strokeGradientColors = it.kStrokeGradientColors
                strokeGradientOritation = it.kStrokeGradientOritation
                drawRadius(canvas)
            }
        }
    }

    //fixme 画自己【onDraw在draw()的super.draw(canvas)流程里面，即在它的前面执行】
    //fixme 可以认为 draw()是前景[上面后画]，onDraw是背景[下面先画]。
    protected var onDraw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //fixme 画自己[onDraw与系统名冲突，所以加一个横线]
    fun onDraw_(onDraw: ((canvas: Canvas, paint: Paint) -> Unit)? = null): KBaseView {
        this.onDraw = onDraw
        postInvalidate()//刷新
        return this
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            onDraw2(it, getPaint())
            onDraw?.let {
                it(canvas, getPaint())
            }
        }
    }

    var w: Int = 0//获取控件的真实宽度
        get() {
            var w = width
            if (layoutParams != null && layoutParams.width > w) {
                w = layoutParams.width
            }
            return w
        }

    var h: Int = 0//获取控件的真实高度
        get() {
            var h = height
            if (layoutParams != null && layoutParams.height > h) {
                h = layoutParams.height
            }
            return h
        }

    //获取文本居中Y坐标
    fun getCenterTextY(paint: Paint): Float {
        var baseline = (h - (paint.descent() - paint.ascent())) / 2 - paint.ascent()
        return baseline
    }

    /**
     * 获取文本实际居中Y坐标。
     */
    fun getTextY(paint: Paint, y: Float): Float {
        var centerY = getCenterTextY(paint)
        var sub = h / 2 - centerY
        var y2 = y - sub
        return y2
    }

    /**
     * 获取文本的高度
     */
    fun getTextHeight(paint: Paint): Float {
        return paint.descent() - paint.ascent()
    }

    var centerX = 0f
        get() = centerX()

    fun centerX(): Float {
        return w / 2f
    }

    var centerY = 0f
        get() = centerY()

    fun centerY(): Float {
        return h / 2f
    }

    //根据宽度，获取该宽度居中值
    fun centerX(width: Float): Float {
        return (w - width) / 2
    }

    //根据高度，获取该高度居中值
    fun centerY(height: Float): Float {
        return (h - height) / 2
    }

    /**
     * 获取新画笔
     */
    fun getPaint(): Paint {
        return KBaseView.getPaint()
    }

    /**
     * NormalID 默认背景图片id
     * PressID 按下背景图片id
     * SelectID 选中(默认和按下相同)时背景图片id,即选中时状态。需要isSelected=true才有效。
     */
    fun selectorDrawable(NormalID: Int, PressID: Int?, SelectID: Int? = PressID) {
        KSelectorUtils.selectorDrawable(this, NormalID, PressID, SelectID)
    }

    //图片
    fun selectorDrawable(NormalBtmap: Bitmap?, PressBitmap: Bitmap?, SelectBitmap: Bitmap? = PressBitmap) {
        KSelectorUtils.selectorBitmap(this, NormalBtmap, PressBitmap, SelectBitmap)
    }

    //fixme 颜色,调用之前一定要先设置圆角的属性。不然圆角不正确
    fun selectorColor(NormalColor: Int, PressColor: Int?, SelectColor: Int? = PressColor) {
        KSelectorUtils.selectorColor(this, NormalColor, PressColor, SelectColor)

    }

    //fixme 颜色,调用之前一定要先设置圆角的属性。不然圆角不正确
    fun selectorColor(NormalColor: String, PressColor: String?, SelectColor: String? = PressColor) {
        KSelectorUtils.selectorColor(this, NormalColor, PressColor, SelectColor)
    }

    //字体颜色
    fun selectorTextColor(NormalColor: Int, PressColor: Int?, SelectColor: Int? = PressColor) {
        KSelectorUtils.selectorTextColor(this, NormalColor, PressColor, SelectColor)
    }

    fun selectorTextColor(NormalColor: String, PressColor: String?, SelectColor: String? = PressColor) {
        KSelectorUtils.selectorTextColor(this, NormalColor, PressColor, SelectColor)
    }

    //fixme 防止和以下方法冲突，all_radius不要设置默认值
    //fixme 调用之前一定要先设置圆角的属性。不然圆角不正确
    fun selectorRippleDrawable(NormalColor: String?, PressColor: String?, all_radius: Float) {
        KSelectorUtils.selectorRippleDrawable(this, Color.parseColor(NormalColor), Color.parseColor(PressColor), Color.parseColor(PressColor), left_top = all_radius, right_top = all_radius, right_bottom = all_radius, left_bottom = all_radius)
    }

    /**
     * 波纹点击效果
     * all_radius 圆角
     */
    fun selectorRippleDrawable(NormalColor: Int, PressColor: Int, all_radius: Float) {
        KSelectorUtils.selectorRippleDrawable(this, NormalColor, PressColor, PressColor, left_top = all_radius, right_top = all_radius, right_bottom = all_radius, left_bottom = all_radius)
    }

    fun selectorRippleDrawable(NormalColor: String, PressColor: String, SelectColor: String? = PressColor, strokeWidth: Int = 0, strokeColor: Int = Color.TRANSPARENT, all_radius: Float = this.all_radius, left_top: Float = this.left_top, right_top: Float = this.right_top, right_bottom: Float = this.right_bottom, left_bottom: Float = this.left_bottom) {
        KSelectorUtils.selectorRippleDrawable(this, Color.parseColor(NormalColor), Color.parseColor(PressColor), Color.parseColor(SelectColor), strokeWidth = strokeWidth, strokeColor = strokeColor, all_radius = all_radius, left_top = left_top, right_top = right_top, right_bottom = right_bottom, left_bottom = left_bottom)
    }

    /**
     * 波纹点击效果
     * NormalColor 正常背景颜色值
     * PressColor  按下正常背景颜色值 ,也可以理解为波纹点击颜色
     * SelectColor 选中(默认和按下相同)背景颜色值
     */
    fun selectorRippleDrawable(NormalColor: Int, PressColor: Int, SelectColor: Int? = PressColor, strokeWidth: Int = 0, strokeColor: Int = Color.TRANSPARENT, all_radius: Float = this.all_radius, left_top: Float = this.left_top, right_top: Float = this.right_top, right_bottom: Float = this.right_bottom, left_bottom: Float = this.left_bottom) {
        KSelectorUtils.selectorRippleDrawable(this, NormalColor, PressColor, SelectColor, strokeWidth = strokeWidth, strokeColor = strokeColor, all_radius = all_radius, left_top = left_top, right_top = right_top, right_bottom = right_bottom, left_bottom = left_bottom)
    }


    //属性动画集合
    var objectAnimates = arrayListOf<ObjectAnimator?>()

    //停止所有属性动画
    fun stopAllObjAnim() {
        for (i in 0 until objectAnimates.size) {
            objectAnimates[i]?.let {
                it.end()
            }
        }
        objectAnimates.clear()//清除所有动画
    }

    //属性动画
    fun ofFloat(propertyName: String, repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null): ObjectAnimator {
        var objectAnimator = ofFloat(this, this, propertyName, repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
        objectAnimates.add(objectAnimator)
        return objectAnimator
    }

    fun ofInt(propertyName: String, repeatCount: Int, duration: Long, vararg value: Int, AnimatorUpdateListener: ((values: Int) -> Unit)? = null): ObjectAnimator {
        var objectAnimator = ofInt(this, this, propertyName, repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
        objectAnimates.add(objectAnimator)
        return objectAnimator
    }

    var realHeight = -1//保存控件的实际高度
        get() {
            if (field < 0 && h > 0) {
                field = h
            }
            return field
        }
    var isShowHeight = true//true展开状态，false关闭状态
        get() {
            if (realHeight > 0) {
                if (h > realHeight / 2) {
                    return true//展开状态
                } else {
                    return false//关闭状态
                }
            } else {
                if (h > 0) {
                    return true//展开状态
                } else {
                    return false//关闭状态
                }
            }
        }

    //要显示的高度（控制高度的变化）
    fun showHeight(mHeight: Int, duration: Long = 300) {
        if (realHeight < 0 && h > 0) {
            realHeight = h//保存实际原有高度
        }
        if (realHeight > 0) {
            //属性动画，随便搞个属性即可。不存在也没关系。仅仅需要这个属性值的变化过程
            ofInt("mmmShowHeight", 0, duration, h, mHeight) {
                layoutParams.apply {
                    //设置宽和高
                    height = it
                }
                requestLayout()
            }
        }
    }

    //高度变化，0->h 或者 h->0 自主判断
    fun showToggleHeight(duration: Long = 300) {
        if (realHeight < 0 && h > 0) {
            realHeight = h//保存实际原有高度
        }
        if (realHeight > 0) {
            if (isShowHeight) {
                //显示状态 改为 关闭状态，高度设置为0
                showHeight(0, duration)
            } else {
                //关闭状态 改为 显示状态，高度设置为原有高度
                showHeight(realHeight, duration)
            }
        }
    }


    var realWidth = -1//保存控件的实际宽度
        get() {
            if (field < 0 && w > 0) {
                field = w
            }
            return field
        }
    var isShowWidth = true//true展开状态，false关闭状态
        get() {
            if (realWidth > 0) {
                if (w > realWidth / 2) {
                    return true//展开状态
                } else {
                    return false//关闭状态
                }
            } else {
                if (w > 0) {
                    return true//展开状态
                } else {
                    return false//关闭状态
                }
            }
        }

    //要显示的宽度（控制宽度的变化）
    fun showWidth(mWidth: Int, duration: Long = 300) {
        if (realWidth < 0 && w > 0) {
            realWidth = w//保存实际原有宽度
        }
        if (realWidth > 0) {
            //属性动画，随便搞个属性即可。不存在也没关系。仅仅需要这个属性值的变化过程
            ofInt("mmmShowWidth", 0, duration, w, mWidth) {
                layoutParams.apply {
                    //设置宽和高
                    width = it
                }
                requestLayout()
            }
        }
    }

    //宽度变化，0->h 或者 h->0 自主判断
    fun showToggleWidth(duration: Long = 300) {
        if (realWidth < 0 && w > 0) {
            realWidth = w//保存实际原有宽度
        }
        if (realWidth > 0) {
            if (isShowWidth) {
                //显示状态 改为 关闭状态，宽度设置为0
                showWidth(0, duration)
            } else {
                //关闭状态 改为 显示状态，宽度设置为原有宽度
                showWidth(realWidth, duration)
            }
        }
    }


    //透明动画,透明度 0f(完全透明)到1f(完全不透明)
    fun alpha(repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null): ObjectAnimator {
        return ofFloat("alpha", repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
    }

    /**
     * 封装位置移动动画
     * toX,toY相对于父容器的移动的目标坐标点。
     * durationMillis 动画时间，单位毫秒。
     * end 回调，动画结束后(结束了才回调)，返回当前的位置坐标。[位置会实际发生改变]
     * fixme 注意，如果有多个控件同时开启动画，移动的时候可能会卡顿和抖动现象。多个控件最好不要同时进行动画，太耗性能了。
     */
    //调用案例：translateAnimation(300f,800f,500){x,y-> }
    fun translateAnimation(toX: Float, toY: Float, durationMillis: Long = 300, end: ((x: Float, y: Float) -> Unit)? = null): TranslateAnimation {
        return translateAnimation(this, toX, toY, durationMillis, end)
    }

    var objectAnimatorScaleX: ObjectAnimator? = null
    var objectAnimatorScaleY: ObjectAnimator? = null
    //缩放动画(因为有两个属性。就不添加监听了)
    //pivotX,pivotY 变换基准点，默认居中
    fun scale(repeatCount: Int, duration: Long, vararg value: Float, pivotX: Float = w / 2f, pivotY: Float = h / 2f) {
        endScale()
        this.pivotX = pivotX
        this.pivotY = pivotY
        //支持多个属性，同时变化，放心会同时变化的。
        objectAnimatorScaleX = ofFloat("scaleX", repeatCount, duration, *value)
        objectAnimatorScaleY = ofFloat("scaleY", repeatCount, duration, *value)
    }

    //暂停缩放（属性会保持当前的状态）
    fun pauseScale() {
        objectAnimatorScaleX?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.pause()
            } else {
                it.end()
            }
        }
        objectAnimatorScaleY?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.pause()
            } else {
                it.end()
            }
        }
    }

    //继续缩放
    fun resumeScale() {
        objectAnimatorScaleX?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.resume()
            } else {
                it.start()//动画会重新开始
            }
        }
        objectAnimatorScaleY?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.resume()
            } else {
                it.start()//动画会重新开始
            }
        }
    }

    //fixme 停止缩放,属性会恢复到原始状态。动画也会结束。
    fun endScale() {
        objectAnimatorScaleX?.let {
            it.end()//fixme 一旦调用了end()属性动画也就结束了，并且属性也会恢复到原始状态。
            objectAnimatorScaleX = null
        }
        objectAnimatorScaleY?.let {
            it.end()
            objectAnimatorScaleY = null
        }
    }

    var objectAnimatorRotation: ObjectAnimator? = null
    //旋转动画
    //pivotX,pivotY 变换基准点，默认居中
    fun rotation(repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null, pivotX: Float = w / 2f, pivotY: Float = h / 2f): ObjectAnimator {
        endRotation()
        this.pivotX = pivotX
        this.pivotY = pivotY
        objectAnimatorRotation = ofFloat("rotation", repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
        return objectAnimatorRotation!!
    }

    //暂停旋转（属性会保持当前的状态）
    fun pauseRotation() {
        objectAnimatorRotation?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.pause()
            } else {
                it.end()
            }
        }
        objectAnimatorRotation?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.pause()
            } else {
                it.end()
            }
        }
    }

    //继续旋转
    fun resumeRotation() {
        objectAnimatorRotation?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.resume()
            } else {
                it.start()//动画会重新开始
            }
        }
        objectAnimatorRotation?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.resume()
            } else {
                it.start()//动画会重新开始
            }
        }
    }

    //fixme 停止旋转,属性会恢复到原始状态。动画也会结束。
    fun endRotation() {
        objectAnimatorRotation?.let {
            it.end()//fixme 一旦调用了end()属性动画也就结束了，并且属性也会恢复到原始状态。
            objectAnimatorRotation = null
        }
        objectAnimatorRotation?.let {
            it.end()
            objectAnimatorRotation = null
        }
    }

    var kTimer: KTimerUtils.KTimer? = null
    //定时刷新
    fun refresh(count: Long = 60, unit: Long = 1000, firstUnit: Long = 0, callback: (num: Long) -> Unit): KTimerUtils.KTimer? {
        if (context != null && context is Activity) {
            endRefresh()
            kTimer = KTimerUtils.refreshUI(context as Activity, count, unit, firstUnit, callback)
        }
        return kTimer
    }

    //暂停
    fun pauseRefresh() {
        kTimer?.let {
            it.pause()
        }
    }

    //判断是否暂停
    fun isPauseRefresh(): Boolean {
        var pause = false
        kTimer?.let {
            pause = it.isPause()
        }
        return pause
    }

    //继续
    fun resumeRefresh() {
        kTimer?.let {
            it.resume()
        }
    }

    //定时器停止
    fun endRefresh() {
        kTimer?.let {
            //一个View就添加一个定时器，防止泄露。
            it.pause()
            it.end()//如果定时器不为空，那一定要先停止之前的定时器。
            kTimer = null
        }
    }

    //水平进度(范围 0F~ 100F),从左往右
    var horizontalProgress = 0f

    fun horizontalProgress(repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null): ObjectAnimator {
        return ofFloat("horizontalProgress", repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
    }

    //返回当前水平移动坐标X,
    var drawHorizontalProgress: ((canvas: Canvas, paint: Paint, x: Float) -> Unit)? = null

    fun drawHorizontalProgress(drawHorizontalProgress: ((canvas: Canvas, paint: Paint, x: Float) -> Unit)) {
        this.drawHorizontalProgress = drawHorizontalProgress
    }

    //fixme 垂直进度(范围 0F~ 100F)注意：方向从下往上。0是最底下，100是最顶部。
    var verticalProgress = 0f

    fun verticalProgress(repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null): ObjectAnimator {
        return ofFloat("verticalProgress", repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
    }

    //返回当前垂直移动坐标Y
    var drawVerticalProgress: ((canvas: Canvas, paint: Paint, y: Float) -> Unit)? = null

    fun drawVerticalProgress(drawVerticalProgress: ((canvas: Canvas, paint: Paint, y: Float) -> Unit)) {
        this.drawVerticalProgress = drawVerticalProgress
    }

    companion object {

        //默认触摸点击波浪效果。isRipple是否具备波浪效果
        open fun onPress(view: View?, isRipple: Boolean) {
            view?.apply {
                //这两个颜色，比较和谐。
                if (isRipple) {
                    //波浪效果
                    KSelectorUtils.selectorRippleDrawable(this, Color.WHITE, Color.parseColor("#E4E4E4"))
                } else {
                    //平常效果
                    KSelectorUtils.selectorColor(this, Color.parseColor("#ffffff"), Color.parseColor("#E4E4E4"))
                }
            }
        }

        fun getPaint(): Paint {
            var paint = Paint()
            return resetPaint(paint)
        }

        fun resetPaint(paint: Paint): Paint {
            paint.reset()
            paint.isAntiAlias = true
            paint.isDither = true
            paint.setDither(true)//防抖动，柔和效果。
            paint.color = Color.WHITE
            paint.textAlign = Paint.Align.CENTER//文本居中
            paint.textSize = kpx.x(12f)
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.strokeWidth = 0f
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeJoin = Paint.Join.ROUND
            typeface?.let {
                if (isGlobalTypeface) {
                    paint.setTypeface(it)//全局应用自定义字体
                }
            }
            return paint
        }

        fun getPaint(typeface: Typeface?): Paint {
            var paint = getPaint()
            paint.typeface = typeface
            return paint
        }

        //获取自定义字体画笔
        fun getPaintTypefaceFromAsset(path: String): Paint {
            var paint = getPaint()
            paint.typeface = getTypefaceFromAsset(path)
            return paint
        }

        fun getPaintTypefaceFromFile(path: String): Paint {
            var paint = getPaint()
            paint.typeface = getTypefaceFromFile(path)
            return paint
        }

        var isGlobalTypeface = false//是否应用全局字体，默认false
        fun isGlobalTypeface(isGlobalTypeface: Boolean = true) {
            this.isGlobalTypeface = isGlobalTypeface
        }

        var typeface: Typeface? = null//自定义全局字体
        fun typeface(typeface: Typeface?) {
            this.typeface = typeface
        }

        /**
         * path字体路径，来自assets目录 如："fonts/ALIHYAIHEI.TTF"
         */
        fun getTypefaceFromAsset(path: String): Typeface {
            return Typeface.createFromAsset(KBaseApplication.getInstance().getResources().getAssets(), path)//字体必须拷贝在assets文件里
        }

        /**
         * path字体完整路径，来自存储卡。
         */
        fun getTypefaceFromFile(path: String): Typeface {
            return Typeface.createFromFile(path)//字体必须拷贝在assets文件里
        }

        /**
         * 画垂直文本
         * x,y 是文本的起点位置
         * offset 垂直文本之间的间隙
         */
        fun drawVerticalText(text: String, canvas: Canvas, paint: Paint, x: Float, y: Float, offset: Float) {
            var list = text.toList()
            for (i in 0 until text.length) {
                var h = paint.textSize
                if (i == 0) {
                    canvas.drawText(list[i].toString(), x, y + h, paint)
                } else {
                    canvas.drawText(list[i].toString(), x, y + (i + 1) * h + (i * offset), paint)
                }
            }
        }

        fun ofFloat(view: View, propertyName: String, repeatCount: Int, duration: Long, vararg value: Float, isInvalidate: Boolean = true, AnimatorUpdateListener: ((values: Float) -> Unit)? = null): ObjectAnimator {
            return ofFloat(view, view, propertyName, repeatCount, duration, *value, isInvalidate = isInvalidate, AnimatorUpdateListener = AnimatorUpdateListener)
        }

        /**
         * fixme 属性动画，属性值不能为私有，不能为private
         * @param view 获取属性用的
         * @param viewGroup 实际刷新用的。为了兼容组件
         * propertyName 属性名称
         * repeatCount  动画次数,从0开始。0表示一次，1表示两次。Integer.MAX_VALUE是最大值。
         * duration  动画时间，单位毫秒。1000表示一秒。
         * value 可变参数。属性的变化值
         * isInvalidate 是否需要刷新，默认是true（自动刷新）
         * AnimatorUpdateListener 动画监听，返回当前变化的属性值。
         */
        fun ofFloat(view: View, viewGroup: View = view, propertyName: String, repeatCount: Int, duration: Long, vararg value: Float, isInvalidate: Boolean = true, AnimatorUpdateListener: ((values: Float) -> Unit)? = null): ObjectAnimator {
            var objectAnimator = ObjectAnimator.ofFloat(view, propertyName.trim(), *value)
            if (repeatCount >= Int.MAX_VALUE) {
                objectAnimator.repeatCount = Int.MAX_VALUE - 1//防止Int.MAX_VALUE无效。
            } else {
                objectAnimator.repeatCount = repeatCount
            }
            objectAnimator.duration = duration
            objectAnimator.interpolator = LinearInterpolator()//线性变化，平均变化
            objectAnimator.addUpdateListener {
                var value = it.getAnimatedValue(propertyName.trim())
                value?.let {
                    if (isInvalidate) {
                        viewGroup.invalidate()//fixme 不停的自我刷新，省去了set里面进去刷新。
                    }
                    AnimatorUpdateListener?.let {
                        it(value as Float)
                    }
                }
            }
            objectAnimator.start()//fixme 放心吧。多个属性动画可以同时进行。不要使用AnimatorSet，8.0系统不支持。
            return objectAnimator
        }

        fun ofInt(view: View, propertyName: String, repeatCount: Int, duration: Long, vararg value: Int, isInvalidate: Boolean = true, AnimatorUpdateListener: ((values: Int) -> Unit)? = null): ObjectAnimator {
            return ofInt(view, view, propertyName, repeatCount, duration, *value, isInvalidate = isInvalidate, AnimatorUpdateListener = AnimatorUpdateListener)
        }

        fun ofInt(view: View, viewGroup: View = view, propertyName: String, repeatCount: Int, duration: Long, vararg value: Int, isInvalidate: Boolean = true, AnimatorUpdateListener: ((values: Int) -> Unit)? = null): ObjectAnimator {
            var objectAnimator = ObjectAnimator.ofInt(view, propertyName.trim(), *value)
            if (repeatCount >= Int.MAX_VALUE) {
                objectAnimator.repeatCount = Int.MAX_VALUE - 1//防止Int.MAX_VALUE无效。
            } else {
                objectAnimator.repeatCount = repeatCount
            }
            objectAnimator.duration = duration
            objectAnimator.interpolator = LinearInterpolator()//线性变化，平均变化
            objectAnimator.addUpdateListener {
                var value = it.getAnimatedValue(propertyName.trim())
                value?.let {
                    if (isInvalidate) {
                        viewGroup.invalidate()//fixme 不停的自我刷新，省去了set里面进去刷新。
                    }
                    AnimatorUpdateListener?.let {
                        it(value as Int)
                    }
                }
            }
            objectAnimator.start()
            return objectAnimator
        }

        /**
         * 封装位置移动动画
         * toX,toY相对于父容器的移动的目标坐标点。
         * durationMillis 动画时间，单位毫秒。
         * end 回调，动画结束后，返回当前的位置坐标。[位置会实际发生改变]
         * fixme 注意，如果有多个控件同时开启动画，移动的时候可能会卡顿和抖动现象。多个控件最好不要同时进行动画，太耗性能了。
         */
        fun translateAnimation(view: View, toX: Float, toY: Float, durationMillis: Long = 300, end: ((x: Float, y: Float) -> Unit)? = null): TranslateAnimation {
            var toXDelta = toX - view.x//动画结束的点离当前View X坐标上的差值
            var toYDelta = toY - view.y//动画开始的点离当前View Y坐标上的差值
            var translateAnimation = TranslateAnimation(0f, toXDelta, 0f, toYDelta)
            //动画时长,单位毫秒
            translateAnimation.setDuration(durationMillis)
            translateAnimation.interpolator = LinearInterpolator()//平滑，速度平均移动
            //view位置停留在动画结束的位置
            translateAnimation.setFillAfter(false)
            translateAnimation.repeatCount = 0//动画次数。0代表一次。
            translateAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {}
                override fun onAnimationEnd(p0: Animation?) {
                    //fixme 动画结束【手动更改控件实际位置】
                    //fixme 注意，位置属性不能出现centerInParent(),centerHorizontally()等设置。只能用外补丁来控制位置。
                    //fixme 除了外补丁，不要出现其他多余的位置属性。不然位置设置无法生效。
                    view.layoutParams.apply {
                        if (this is ViewGroup.MarginLayoutParams) {
                            view.clearAnimation()//动画清除，防止动画结束时抖动
                            setMargins(toX.toInt(), toY.toInt(), rightMargin, bottomMargin)
                            view.requestLayout()
                            end?.let {
                                it(toX, toY)
                            }
                        }
                    }
                }

                override fun onAnimationStart(p0: Animation?) {}
            })
            //开始动画
            view.startAnimation(translateAnimation)
            return translateAnimation
        }

        //获取最近父容器的颜色值
        fun getParentColor(view: View): Int {
            var color = Color.WHITE//默认白色
            var viewParent: ViewParent? = view.parent
            viewParent?.let {
                if (it is View) {
                    var v = it as View
                    val background: Drawable? = v.background
                    //background包括color和Drawable,这里分开取值
                    if (background != null && (background is ColorDrawable)) {
                        val colordDrawable = background as ColorDrawable
                        color = colordDrawable.getColor()
                        if (color == Color.TRANSPARENT) {
                            color = getParentColor(v)
                        }
                    } else {
                        color = getParentColor(v)
                    }
                }
            }
            return color
        }

    }

}
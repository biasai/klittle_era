package cn.oi.klittle.era.widget.compat

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseApplication
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.base.KBaseView
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KAssetsUtils
import cn.oi.klittle.era.utils.KLoggerUtils
import kotlinx.android.synthetic.*
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.*
import java.lang.Exception
import android.graphics.Paint.FILTER_BITMAP_FLAG
import android.graphics.Paint.ANTI_ALIAS_FLAG
import cn.oi.klittle.era.entity.camera.KCamera
import cn.oi.klittle.era.exception.KCatchException

//                    fixme 设置音频播放
//                    issMediaPlayerEnable = true//开启全局音频播放
//                    K0Widget.Companion.apply {
//                        fixme 全局音频不会释放，需要手动去释放，调用sReleaseMediaPlayer()方法
//                        setsSoundsRaw(R.raw.kpictureselect_music)//设置全局(set方法带s)播放音频（点击的时候会播放）
//                    }

//                    setSoundsRaw(R.raw.kpictureselect_music)//设置当前(方法不带s)控件的播放音频（在onDestroy()里会对音频进行释放）
/**
 * 一：基本组件，集成基本功能。
 */
open class K1Widget : K0Widget {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    //ishardware是否进行硬件加速
    constructor(viewGroup: ViewGroup, ishardware: Boolean) : super(viewGroup.context) {
        if (ishardware) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    init {
        KBaseView.typeface?.let {
            if (KBaseView.isGlobalTypeface) {
                typeface = it//fixme 设置全局自定义字体
            }
        }
        textSize = kpx.textSizeX(30f)
        textColor = Color.parseColor("#181818")
        hintTextColor = Color.parseColor("#9b9b9b")
        //gravity = Gravity.CENTER_VERTICAL or Gravity.LEFT//文本左靠齐，垂直居中
        gravity = Gravity.LEFT//文本还是应该设置成左对齐即可（对其左上角）。不要居中
        isClickable = true//局部点击能力
        isFocusable = true//具备聚焦能力
        isFocusableInTouchMode = false//如果为true，点击事件要点两次才生效。
        padding = 0
        getPaint().setAntiAlias(true)//抗锯齿
        clearBackground()//清除背景
        //textAllCaps
        setTransformationMethod(null)//解决英文默认大写问题。
        setSoundEffectsEnabled(false)//禁止按钮自己默认的点击音效。
        //事件重写，主要用于视图刷新（防止他不刷新。）。
        onFocusChange { v, hasFocus -> }
        onHover { v, event -> false }
    }

    //fixme 这个可以去掉button默认的点击阴影，必须要手动调用一次才能清除默认阴影。不然一样有。
    //fixme 而且必须在控件没有加载完成之前调用。不然也无效。即一开始的时候就调用。
    open fun clearButonShadow() {
        //button在5.0系统上，默认带有阴影效果。
        if (Build.VERSION.SDK_INT >= 21) {
            viewGroup?.stateListAnimator = null
        }
    }

    //fixme 清空原始背景，无法清除button默认的点击阴影
    open fun clearBackground() {
        viewGroup.apply {
            if (Build.VERSION.SDK_INT >= 16) {
                backgroundColor = Color.TRANSPARENT
                background = null
            } else {
                backgroundColor = Color.TRANSPARENT
                backgroundDrawable = null
            }
        }
    }

    open fun backgroundColor(color: String) {
        viewGroup.apply {
            setBackgroundColor(Color.parseColor(color))
        }
    }

    open fun backgroundColor(color: Int) {
        viewGroup.apply {
            setBackgroundColor(color)
        }
    }

    //fixme 设置背景位图
    open fun background(resId: Int, isRGB_565: Boolean = false) {
        viewGroup.apply {
            background(getBitmapFromResource(resId, isRGB_565))
            //setBackgroundResource(resId)
        }
    }

    /**
     * fixme 设置背景位图
     * @param filePath 文件路径
     */
    open fun background(filePath: String, isRGB_565: Boolean = false) {
        viewGroup.apply {
            background(getBitmapFromFile(filePath, isRGB_565))
            //setBackgroundResource(resId)
        }
    }

    open fun background(bitmap: Bitmap) {
        viewGroup.apply {
            if (Build.VERSION.SDK_INT >= 16) {
                background = BitmapDrawable(bitmap)
            } else {
                backgroundDrawable = BitmapDrawable(bitmap)
            }
        }
    }

    open var w: Int = 0
        //fixme 真实的宽度,现在设置w或lparams里的width都可以。两个同步了。
        get() {
            if (viewGroup != null) {
                if (field == viewGroup!!.width && viewGroup!!.width > 0) {
                    return field
                }
                var w = viewGroup!!.width
                if (viewGroup!!.layoutParams != null && viewGroup!!.layoutParams.width > w) {
                    w = viewGroup!!.layoutParams.width
                }
                field = w
            }
            return field
        }
        set(value) {
            field = value
            viewGroup?.layoutParams?.let {
                it.width = value
            }
        }
    open var h: Int = 0
        //fixme 真实的高度。设置h或height都可以。
        get() {
            if (viewGroup != null) {
                if (field == viewGroup!!.height && viewGroup!!.height > 0) {
                    return field
                }
                var h = viewGroup!!.height
                if (viewGroup!!.layoutParams != null && viewGroup!!.layoutParams.height > h) {
                    h = viewGroup!!.layoutParams.height
                }
                field = h
            }
            return field
        }
        set(value) {
            field = value
            viewGroup?.layoutParams?.let {
                it.height = value
            }
        }

    // 两次点击按钮之间的点击间隔不能少于300毫秒
    var MIN_CLICK_DELAY_TIME = 300
    private var lastClickTime: Long = System.currentTimeMillis()//记录最后一次点击时间


    //是否开启静态全局音频，默认关闭
    var issMediaPlayerEnable = false
    //关闭按钮自定义声音
    //setSoundEffectsEnabled(false)
    //按钮声音播放
    var mediaPlayer: MediaPlayer? = null

    //释放掉音频
    fun releaseMediaPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun setSounds(mediaPlayer: MediaPlayer?) {
        if (mediaPlayer != this.mediaPlayer) {
            releaseMediaPlayer()//防止自己把自己销毁了。
        }
        this.mediaPlayer = mediaPlayer
    }

    /**
     * 设置Raw目录下的音频
     */
    fun setSoundsRaw(rawId: Int) {
        try {
            releaseMediaPlayer()//先释放
            mediaPlayer = MediaPlayer.create(context, rawId)
            mediaPlayer?.prepare()//必不可少(目前好像已经不需要了)。必须用try异常进行捕捉。该方法抛出了异常。所以要捕捉。
            mediaPlayer?.setLooping(false)//不循环播放
        } catch (e: Exception) {
        }
    }

    /**
     * 设置SD卡下的音频。
     * @param path 文件的完整路径（包括文件的后缀名,如:"sound/sb.WAV"）；
     */
    fun setSoundsSD(path: String) {
        try {
            releaseMediaPlayer()//先释放
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDataSource(path)
            mediaPlayer?.prepare()
            mediaPlayer?.setLooping(false)//不循环播放
        } catch (e: Exception) {
        }
    }

    /**
     * 设置Assets目录下的音频。
     * @param path 文件的完整路径（包括文件的后缀名,如:"sound/sb.WAV"）；
     */
    fun setSoundsAssets(path: String) {
        try {
            releaseMediaPlayer()//先释放
            mediaPlayer = MediaPlayer()
            var fileDescriptor = context.assets.openFd(path)
            mediaPlayer?.setDataSource(fileDescriptor.fileDescriptor,
                    fileDescriptor.startOffset, fileDescriptor.length)
            mediaPlayer?.prepare()
            mediaPlayer?.setLooping(false)//不循环播放
        } catch (e: Exception) {
        }
    }

    /**
     * 设置SD卡下的音频。
     * @param url 网络上的音频地址
     */
    fun setSoundsUrl(url: String) {
        try {
            releaseMediaPlayer()//先释放
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDataSource(url)//可以播放在线音频
            mediaPlayer?.prepare()
            mediaPlayer?.setLooping(false)//不循环播放
        } catch (e: Exception) {
        }
    }

    /**
     * 播放音频
     */
    fun playMediaPlayer() {
        this.mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
            }
        }
    }

    //是否允许快速点击事件，true可以快速点击，false不允许快速点击。
    var isFastClickEnable = false

    //判断是否快速点击，true是快速点击，false不是
    protected fun isFastClick(): Boolean {
        var flag = false
        var curClickTime = System.currentTimeMillis()
        if ((curClickTime - lastClickTime) <= MIN_CLICK_DELAY_TIME) {
            flag = true//快速点击
        }
        lastClickTime = curClickTime
        return flag
    }


    //fixme 自定义图片点击事件；在一般的点击事件之前。如果图片事件处理了。就不会再执行普通的点击事件了。
    protected var mOnClickCallback: (() -> Boolean)? = null

    protected fun mOnClickCallback(mOnClickCallback: (() -> Boolean)? = null) {
        this.mOnClickCallback = mOnClickCallback
    }

    //fixme 重写点击事件
    override fun setOnClickListener(l: OnClickListener?) {
        if (l == null) {
            hasClick = false//fixme 防止适配器界面刷新之后，点击事件无效问题。
        }
        super.setOnClickListener(l)
    }

    private var onClickes: MutableList<() -> Unit>? = mutableListOf<() -> Unit>()
    var hasClick = false//判断是否已经添加了点击事情。
    //fixme 自定义点击事件，可以添加多个点击事情。互不影响,isMul是否允许添加多个点击事件。默认不允许
    //isMul 是否允许添加多个点击事件，ture允许。false默认不允许。
    open fun onClick(isMul: Boolean = false, onClick: () -> Unit) {
        if (onClickes == null) {
            onClickes = mutableListOf<() -> Unit>()
        }
        if (viewGroup == null) {
            viewGroup = this
        }
        //viewGroup兼容组件
        viewGroup?.apply {
            if (!hasClick) {
                isClickable = true//设置具备点击能力
                //点击事件
                setOnClickListener {
                    try {
                        if (onClickes == null) {
                            return@setOnClickListener
                        }
                        //fixme 防止快速点击
                        if (isFastClickEnable) {
                            if (mediaPlayer != null) {
                                async {
                                    playMediaPlayer()
//                                if (!mediaPlayer!!.isPlaying) {
//                                    mediaPlayer?.start()//fixme 播放自己音频(优先播放,优先级比静态全局的高！)
//                                }
                                }
                            } else if (issMediaPlayerEnable && sMediaPlayer != null) {
                                async {
                                    K0Widget.sPlayMediaPlayer()
//                                if (!sMediaPlayer!!.isPlaying) {
//                                    sMediaPlayer?.start()//播放全局静态音频
//                                }
                                }
                            }
                            //放在循环外面；放在多次执行。
                            var b = mOnClickCallback?.let {
                                it()//fixme 自定义图片点击事件；在一般的点击事件之前。如果图片事件处理了。就不会再执行普通的点击事件了。
                            }
                            //true允许快速点击事件
                            for (i in onClickes!!) {
                                i?.let {
                                    if (b == null || !b) {
                                        it()//fixme 点击事件
                                    }
                                }
                            }
                        } else if (!isFastClick()) {//fixme 不允许快速点击
                            if (mediaPlayer != null) {
                                async {
                                    playMediaPlayer()
//                                if (!mediaPlayer!!.isPlaying) {
//                                    mediaPlayer?.start()//播放自己音频(优先播放)
//                                }
                                }
                            } else if (issMediaPlayerEnable && sMediaPlayer != null) {
                                async {
                                    K0Widget.sPlayMediaPlayer()
//                                if (!sMediaPlayer!!.isPlaying) {
//                                    sMediaPlayer?.start()//播放全局静态音频
//                                }
                                }
                            }
                            var b = mOnClickCallback?.let {
                                it()//fixme 自定义图片点击事件；在一般的点击事件之前。如果图片事件处理了。就不会再执行普通的点击事件了。
                            }
                            if (onClickes != null) {
                                //不允许快速点击
                                for (i in onClickes!!) {
                                    i?.let {
                                        if (b == null || !b) {
                                            it()//fixme 点击事件
                                        }
                                    }
                                    if (onClickes == null) {
                                        break
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        KLoggerUtils.e("K1Widget.kt点击事件异常：\t" + KCatchException.getExceptionMsg(e))
                    }
                }
                hasClick = true
            }
            if (!isMul) {
                //不允许添加多个点击事件
                onClickes?.clear()//清除之前的点击事件
            }
            onClickes?.add(onClick)
        }
    }

    //fixme 注意，如果要用选中状态，触摸状态最好设置为null空。不会有卡顿冲突。
    //重写选中状态。isSelected=true。选中状态。一定要手动调用。
    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        bindView?.let {
            if (it.isSelected != isSelected) {
                it?.isSelected = isSelected//选中状态
            }
        }
        onSelectChangedList?.forEach {
            it?.let {
                it(selected)//选中监听
            }
        }
    }

    //fixme 监听选中状态。防止多个监听事件冲突，所以添加事件数组。
    private var onSelectChanged: ((selected: Boolean) -> Unit)? = null
    private var onSelectChangedList = mutableListOf<((selected: Boolean) -> Unit)?>()
    fun addSelectChanged(onSelectChanged: ((selected: Boolean) -> Unit)) {
        onSelectChanged.let {
            onSelectChangedList?.add(it)
        }
    }

    private var onFocusChangeList: MutableList<((v: View, hasFocus: Boolean) -> Unit)?>? = mutableListOf<((v: View, hasFocus: Boolean) -> Unit)?>()
    //fixme 重写聚焦事件,会覆盖之前的聚焦事件
    open fun onFocusChange(callbak: ((v: View, hasFocus: Boolean) -> Unit)? = null) {
        if (onFocusChangeList == null) {
            onFocusChangeList = mutableListOf<((v: View, hasFocus: Boolean) -> Unit)?>()
        }
        onFocusChangeList?.clear()//fixme 清除之前的聚焦事件
        if (callbak != null) {
            onFocusChangeList?.add(callbak)
            onFocusChangeList()
        }
    }

    private fun onFocusChangeList() {
        setOnFocusChangeListener { v, hasFocus ->
            try {
                if (v == this && onFocusChangeList != null) {
                    if (onFocusChangeList != null) {
                        for (i in onFocusChangeList!!) {
                            i?.let {
                                it(v, hasFocus)
                            }
                            if (onFocusChangeList == null) {
                                return@setOnFocusChangeListener
                            }
                        }
                        invalidate()//聚焦改变时刷新视图
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //fixme 添加聚焦事件，不会清除（覆盖）之前的聚焦事件。
    open fun addFocusChange(callbak: ((v: View, hasFocus: Boolean) -> Unit)? = null) {
        if (onFocusChangeList == null) {
            onFocusChangeList = mutableListOf<((v: View, hasFocus: Boolean) -> Unit)?>()
        }
        onFocusChangeList?.add(callbak)
        if (callbak != null) {
            onFocusChangeList?.add(callbak)
            onFocusChangeList()
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

    var bindView: View? = null
        //fixme 状态绑定的View
        set(value) {
            field = value
            if (value != null) {
                if (value is K1Widget) {
                    if (value.bindView == null) {
                        value.bindView = this//相互绑定
                    }
                } else if (value is KBaseView) {
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
            //聚焦是不可能同步的。聚聚只能有一个聚焦。
        }
    }

    var isScrollHorizontal = false//判断是否水平滑动（第一次）
    var isFirst = true//是否为第一次滑动。
    var previousX = 0f
    var previousY = 0f
    var deltaX = 0f//水平滑动偏移量
    var deltaY = 0f//垂直滑动偏移量

    var pointDownTime: Long = 0//手指按下时间
    var pointUpTime: Long = 0//手指离开时时间
    var pointSubTime: Long = 0//手指按下到手指离开之间的时间差。
    //记录手指按下的点。
    var pointDownX: Float = 0f
    var pointDownY: Float = 0f
    var isMoveMotion: Boolean = false//是否进行了触摸
    var isEnableTouch = true//是否允许触摸，默认允许。

    //按下
    fun dispatchDown(event: MotionEvent?) {
        event?.let {
            pointDownTime = System.currentTimeMillis()
            isMoveMotion = false
            pointDownX = it.getX()
            pointDownY = it.getY()

            isFirst = true
            previousX = event.getX()
            previousY = event.getY()

            waterRipple()//按下水波纹效果
        }
    }

    //触摸移动
    fun dispatchMove(event: MotionEvent?) {
        event?.let {
            isMoveMotion = true
            deltaX = event.getX() - previousX
            deltaY = event.getY() - previousY
            previousX = event.getX()
            previousY = event.getY()
            if (isFirst) {
                if (Math.abs(deltaX) < Math.abs(deltaY)) {
                    isScrollHorizontal = false//垂直滑动方向
                    isFirst = false
                } else if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    isScrollHorizontal = true//水平滑动方向
                    isFirst = false
                }
            }
        }
    }

    //手指离开
    fun dispatchUp(event: MotionEvent?) {
        pointUpTime = System.currentTimeMillis()
        pointSubTime = pointUpTime - pointDownTime
    }

    private var b = false
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnableTouch) {
            //禁止触摸和点击，禁止一切事件了。
            return true
        }
        event?.let {
            when (it.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    dispatchDown(it)
                }
                MotionEvent.ACTION_MOVE -> {
                    dispatchMove(it)
                }
                MotionEvent.ACTION_UP -> {
                    dispatchUp(it)
                }
            }
        }
        b = super.dispatchTouchEvent(event)
        //防止点击事件冲突。所以。一定要放到super()后面。
        event?.let {
            when (it.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    isPressed = true
                    bindView?.isPressed = true//按下状态
                    //waterRipple()//按下水波纹效果
                    invalidate()//视图刷新。
                }
                MotionEvent.ACTION_MOVE -> {
                    isPressed = true
                    bindView?.isPressed = true//按下状态
                    //fixme 触摸时，不刷新。防止卡顿
                }
                MotionEvent.ACTION_UP -> {
                    bindView?.isPressed = false
                    isPressed = false
                    //requestFocusFromTouch()//点击时，获取焦点[fixme 这样写，edittext输入框；长按时系统的复制黏贴弹框很快就行消失。无法正常复制黏贴]
                    invalidate()
                }
                MotionEvent.ACTION_CANCEL -> {
                    //其他异常
                    isPressed = false
                    bindView?.isPressed = false
                    invalidate()
                }
            }
        }
        return b
    }

    /**
     * fixme 调用者实现：
     */

    //fixme 自定义画布()，后画，会显示在前面,交给调用者去实现
    open var drawBehind: ((canvas: Canvas, paint: Paint) -> Unit)? = null
    //fixme 自定义画布()，先画，会显示在后面,交给调用者去实现
    open var drawFront: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    open fun drawFront(drawFront: ((canvas: Canvas, paint: Paint) -> Unit)? = null) {
        this.drawFront = drawFront
    }

    open fun drawBehind(drawBehind: ((canvas: Canvas, paint: Paint) -> Unit)? = null) {
        this.drawBehind = drawBehind
    }

    //fixme 自定义画布()，后画，会显示在前面,交给调用者去实现
    open var drawFirst: ((canvas: Canvas, paint: Paint) -> Unit)? = null
    //fixme 自定义画布()，先画，会显示在后面,交给调用者去实现
    open var drawLast: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    open fun drawFirst(drawFirst: ((canvas: Canvas, paint: Paint) -> Unit)? = null) {
        this.drawFirst = drawFirst
    }

    open fun drawLast(drawLast: ((canvas: Canvas, paint: Paint) -> Unit)? = null) {
        this.drawLast = drawLast
    }

    //fixme 自定义画布，根据需求。自主实现
    open var draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //fixme 自定义，重新绘图(就在super.draw(canvas)的后面执行。)
    open fun draw(draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null) {
        this.draw = draw
    }

    /**
     * fixme 以下有2的都交给子类重写实现。
     */

    //fixme 自定义画布()，后画，会显示在前面,交给子类去实现
    protected open fun draw2Behind(canvas: Canvas, paint: Paint) {}

    //fixme 自定义画布()，最先画，会显示在最后面,交给子类去实现(一般用于实现背景)
    protected open fun draw2First(canvas: Canvas, paint: Paint) {}

    //fixme 自定义画布()，最后画，会显示在最前面,交给子类去实现(一般用于实现圆角矩形)
    protected open fun draw2Last(canvas: Canvas, paint: Paint) {}

    //fixme 自定义画布()，先画，会显示在后面,交给子类去实现
    protected open fun draw2Front(canvas: Canvas, paint: Paint) {}

    //fixme 什么都不做，交给子类去实现绘图
    //fixme 之所以会有这个方法。是为了保证自定义的 draw和onDraw的执行顺序。始终是在最后。
    protected open fun draw2(canvas: Canvas, paint: Paint) {}

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


    var mPaint: Paint? = KBaseView.getPaint()
    //画布重置
    fun resetPaint(): Paint {
        if (mPaint == null) {
            mPaint = KBaseView.getPaint()
        }
        return KBaseView.resetPaint(mPaint!!)
    }

    var mCanvas: Canvas? = null
    var mCamera: KCamera? = null//画图3D旋转相机。

    fun getCamera(): KCamera? {
        if (mCamera == null) {
            mCamera = KCamera(this)
        }
        return mCamera
    }

    //fixme 系统View的旋转方法是这样的：rotationX=30f；所以不会和view的旋转起冲突。并且系统的3D旋转方法，在华为荣耀的部分机型好像还没有效果。
    //fixme pivotX,pivotY旋转中心，好像对只对rotation有效（旋转Z轴），对x轴，y轴旋转无效。
    fun rotateX(degree: Float, centerX: Float = this.centerX, centerY: Float = this.centerY, isRestore: Boolean = true, callBack: (() -> Unit)? = null) {
        if (degree == 0f) {
            //fixme 回调;旋转角度为0，直接回调。不做任何操作。
            callBack?.let {
                it()
            }
            return
        }
        getCamera()?.rotateX(degree, centerX, centerY, isRestore, callBack)
    }

    fun rotateY(degree: Float, centerX: Float = this.centerX, centerY: Float = this.centerY, isRestore: Boolean = true, callBack: (() -> Unit)? = null) {
        if (degree == 0f) {
            //fixme 回调。
            callBack?.let {
                it()
            }
            return
        }
        getCamera()?.rotateY(degree, centerX, centerY, isRestore, callBack)
    }

    fun rotateZ(degree: Float, centerX: Float = this.centerX, centerY: Float = this.centerY, isRestore: Boolean = true, callBack: (() -> Unit)? = null) {
        if (degree == 0f) {
            //fixme 回调。
            callBack?.let {
                it()
            }
            return
        }
        getCamera()?.rotateZ(degree, centerX, centerY, isRestore, callBack)
    }

    fun rotate(degree: Float, centerX: Float = this.centerX, centerY: Float = this.centerY, isRestore: Boolean = true, callBack: (() -> Unit)? = null) {
        if (degree == 0f) {
            //fixme 回调。
            callBack?.let {
                it()
            }
            return
        }
        getCamera()?.rotate(degree, centerX, centerY, isRestore, callBack)
    }

    override fun dispatchDraw(canvas: Canvas?) {
        try {
            super.dispatchDraw(canvas)
        } catch (e: java.lang.Exception) {
            KLoggerUtils.e("自定义View dispatchDraw异常：\t" + KCatchException.getExceptionMsg(e))
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        try {
            super.onMeasure(widthSpec, heightSpec)
        } catch (e: java.lang.Exception) {
            KLoggerUtils.e("自定义View onMeasure异常：\t" + KCatchException.getExceptionMsg(e))
        }
    }

    override fun draw(canvas: Canvas?) {
        try {
            if (width <= 0 || height <= 0 || canvas == null) {
                return
            }
            canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)//画布清除
            //fixme 这里系统传下来的canvas可能每次都是实例化的哦。
            mCamera?.save()//fixme 保存相机初始的状态
            canvas?.apply {
                canvas?.drawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)//fixme 画布抗锯齿效果，亲测有效，比画笔paint设置抗锯齿效果还要好。杠杠的。
                mCanvas = canvas
                //调用者实现
                drawFirst?.let {
                    it(this, resetPaint())
                }
                mCanvas = canvas
                //子类实现，最先画。显示在最下面
                draw2First(this, resetPaint())
                mCanvas = canvas//fixme 为了保证mCanvas是最新的，所以每次都赋值。
                //子类实现
                draw2Front(this, resetPaint())
                mCanvas = canvas
                //调用者实现
                drawFront?.let {
                    it(this, resetPaint())
                }
            }
            try {
                mCanvas = canvas
                super.draw(canvas)//这里面有文本。不要遮挡。
            } catch (e: Exception) {
                //防止异常。
                e.printStackTrace()
                KLoggerUtils.e("原生 super.draw 异常：\t" + e.message)
            }
            canvas?.apply {
                mCanvas = canvas
                draw2(this, resetPaint())
                mCanvas = canvas
                draw?.let {
                    it(canvas, resetPaint())
                }
                mCanvas = canvas
                //子类去实现。
                draw2Behind(this, resetPaint())
                mCanvas = canvas
                //画水平进度
                drawHorizontalProgress?.let {
                    it(canvas, resetPaint(), w * horizontalProgress / 100f)
                }
                mCanvas = canvas
                //画垂直进度
                drawVerticalProgress?.let {
                    it(canvas, resetPaint(), h - h * verticalProgress / 100f)//方向从下往上。
                }
                mCanvas = canvas
                //调用者去实现
                drawBehind?.let {
                    it(this, resetPaint())
                }
                mCanvas = canvas
                //水波纹效果
                drawWaterRipple(canvas)
                mCanvas = canvas
                //子类实现，最后画。显示在最上面
                draw2Last(this, resetPaint())
                mCanvas = canvas
                //调用者实现
                drawLast?.let {
                    it(this, resetPaint())
                }
                mCanvas = canvas
            }
            mCamera?.restore()//fixme 恢复相机的状态；防止异常
        } catch (e: Exception) {
            e.printStackTrace()
            KLoggerUtils.e("自定义View draw 异常：\t" + KCatchException.getExceptionMsg(e))
        }
    }

    fun drawWaterRipple(canvas: Canvas) {
        if (viewGroup == null) {
            return
        }
        canvas.apply {
            if (isWaterRipple && waterColor != Color.TRANSPARENT && waterProgress > 0) {
                var b = true
                if (waterProgress >= 1 && !viewGroup!!.isPressed) {
                    b = false
                }
                if (b) {
                    var waterRadius = w.toFloat()
                    if (w < h) {
                        waterRadius = h.toFloat()
                    }
                    waterRadius = waterRadius * waterProgress * 1.35f//防止长度不够，所以多乘以1.35倍数。
                    if (waterPaint == null) {
                        waterPaint = KBaseView.getPaint()
                    }
                    waterPaint?.let {
                        it.color = waterColor
                        it.style = Paint.Style.FILL
                        drawCircle(pointDownX, pointDownY, waterRadius, waterPaint)
                    }
                }
            }
        }

    }


    //获取文本居中Y坐标
    fun getCenterTextY(paint: Paint): Float {
        var baseline = (h - (paint.descent() - paint.ascent())) / 2 - paint.ascent()
        return baseline
    }

    /**
     * 获取文本实际对应的Y坐标。
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
     * 属性动画
     * @param propertyName 属性名
     * @param repeatCount 循环次数，0代表一次，1代表2次，依次类推
     * @param duration 动画时间
     * @param value 属性变化值
     * @param isInvalidate 是否自动刷新。默认是
     * @param AnimatorUpdateListener 回调，返回当前变化值。
     */
    fun ofFloat(propertyName: String, repeatCount: Int, duration: Long, vararg value: Float, isInvalidate: Boolean = true, AnimatorUpdateListener: ((values: Float) -> Unit)? = null): ObjectAnimator {
        var objectAnimator = KBaseView.ofFloat(this, viewGroup!!, propertyName, repeatCount, duration, *value, isInvalidate = isInvalidate, AnimatorUpdateListener = AnimatorUpdateListener)
        return objectAnimator
    }

    fun ofInt(propertyName: String, repeatCount: Int, duration: Long, vararg value: Int, isInvalidate: Boolean = true, AnimatorUpdateListener: ((values: Int) -> Unit)? = null): ObjectAnimator {
        var objectAnimator = KBaseView.ofInt(this, viewGroup!!, propertyName, repeatCount, duration, *value, isInvalidate = isInvalidate, AnimatorUpdateListener = AnimatorUpdateListener)
        return objectAnimator
    }


    //是否开启自定义水波纹效果
    var isWaterRipple: Boolean = false
    var waterProgress: Float = 0f//水波纹进度,不能私有（不能private）。不然属性动画没有效果。
    var waterColor: Int = Color.parseColor("#88E4E4E4")//水波纹颜色
    var waterDuration = 550L//水波纹时间
    private var waterPaint: Paint? = null
    private var objectAnimatorWaterRipple: ObjectAnimator? = null
    //自定义水波纹效果(效果杠杠的，系统的效果感觉不好看，所以自定义)
    fun waterRipple() {
        if (isWaterRipple && waterColor != Color.TRANSPARENT)
            if (objectAnimatorWaterRipple == null) {
                objectAnimatorWaterRipple = ofFloat("waterProgress", 0, waterDuration, 0f, 1f)
            } else {
                objectAnimatorWaterRipple?.end()
                waterProgress = 0f
                objectAnimatorWaterRipple?.start()
            }
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

    //记录布局加载完成的时间。防止短时间内重复调用。
    private var onGlobalLayoutFinishTime = 0L

    /**
     * fixme 控件加载完成的时候调用（已经具备固定的宽和高）
     * @param callback 回调返回x，y坐标(相对父容器)及宽和高
     */
    fun addOnGlobalLayoutListener(callback: ((x: Float, y: Float, width: Int, height: Int) -> Unit)? = null) {
        if (callback != null) {
            viewTreeObserver?.addOnGlobalLayoutListener {
                var w = width
                var h = height
                if (layoutParams != null) {
                    if (width < layoutParams.width) {
                        w = layoutParams.width
                    }
                    if (height < layoutParams.height) {
                        h = layoutParams.height
                    }
                }
                //宽和高不能为空，要返回具体的值。
                if (w > 0 && h > 0) {
                    //防止多次重复调用，一定的时间内只允许调用一次。
                    if (System.currentTimeMillis() - onGlobalLayoutFinishTime > 1000) {
                        onGlobalLayoutFinishTime = System.currentTimeMillis()
                        callback(x, y, w, h)
                    }
                }
            }
        }
    }

    /**
     * fixme 获取与屏幕低部的距离（以整个屏幕为标准。绝对距离）
     * fixme top是顶部的位置，bottom是低部的位置。
     */
    fun bottom2(): Int {
        context?.let {
            if (it is Activity) {
                if (!it.isFinishing) {
                    return it.window.decorView.height - (getLocationOnScreenY() + height)
                }
            }
        }
        return 0
    }

    private var intArray = intArrayOf(0, 1)
    //fixme 获取在整个屏幕的Y绝对坐标。
    fun getLocationOnScreenY(): Int {
        getLocationOnScreen(intArray)
        return intArray[1]
    }

    //fixme 获取在整个屏幕的X绝对坐标。
    fun getLocationOnScreenX(): Int {
        getLocationOnScreen(intArray)
        return intArray[0]
    }

    var isOnDestroy: Boolean = false//fixme 判断是否销毁了。
    //fixme 销毁
    open fun onDestroy() {
        try {
            isOnDestroy = true
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

                if (onGlobalLayoutListener != null && Build.VERSION.SDK_INT >= 16) {
                    viewTreeObserver?.removeOnGlobalLayoutListener(onGlobalLayoutListener)
                }
                onGlobalLayoutListener = null

                clearAnimation()
                clearFocus()
                clearFindViewByIdCache()

            } catch (e: Exception) {
                e.printStackTrace()
            }
            hasClick = false
            mCanvas = null
            mCamera = null
            waterPaint = null
            objectAnimatorWaterRipple = null
            viewGroup = null
            mPaint = null
            draw = null
            drawBehind = null
            drawFront = null
            drawFirst = null
            drawLast = null
            drawHorizontalProgress = null
            drawVerticalProgress = null
            mOnClickCallback = null
            onClickes?.clear()
            onClickes = null
            onFocusChangeList?.clear()
            onFocusChangeList = null
            releaseMediaPlayer()//fixme 释放掉自己的音频（静态的音频是全局的，不会自动销毁，要自己手动去释放。）
            onDetachedFromWindow()
            destroyDrawingCache()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
package cn.oi.klittle.era.widget.compat

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import cn.oi.klittle.era.base.KBaseView
import cn.oi.klittle.era.utils.KBitmapUtils
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.utils.KSelectorUtils
import cn.oi.klittle.era.utils.KTimerUtils
import org.jetbrains.anko.runOnUiThread

//                                fxime 多图片渐变动画调用案例
//                                GlobalScope.async {
//                                var overrideWidth = kpx.x(640)
//                                var overrideHeight = kpx.x(795)
//                                //fixme 位图
//                                var bm1 = KGlideUtils.getBitmapFromResouce(R.mipmap.timg, overrideWidth, overrideHeight)
//                                var bm2 = KGlideUtils.getBitmapFromResouce(R.mipmap.timg2, overrideWidth, overrideHeight)
//                                startTransition(bm1,bm2,durationMillis=3000)
//                            }

/**
 * 二：基本组件，动画(属性动画，只是耗性能，不耗内存。亲测只要是局部变量即用即消。),resh刷新，选择器。
 */
open class K2AnimeWidget : K1Widget {
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


    //透明动画,透明度 0f(完全透明)到1f(完全不透明)
    fun alpha(repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null): ObjectAnimator {
        return ofFloat("alpha", repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
    }

    /**
     * 封装位置移动动画
     * toX,toY相对于父容器的移动的目标坐标点。
     * durationMillis 动画时间，单位毫秒。
     * end 回调，动画结束后，返回当前的位置坐标。[位置会实际发生改变]
     * fixme 注意，如果有多个控件同时开启动画，移动的时候可能会卡顿和抖动现象。多个控件最好不要同时进行动画，太耗性能了。
     */
    fun translateAnimation(toX: Float, toY: Float, durationMillis: Long = 300, end: ((x: Float, y: Float) -> Unit)? = null): TranslateAnimation {
        return KBaseView.translateAnimation(viewGroup!!, toX, toY, durationMillis, end)
    }

    var objectAnimatorScaleX: ObjectAnimator? = null
    var objectAnimatorScaleY: ObjectAnimator? = null

    //缩放动画(因为有两个属性。就不添加监听了)
    //pivotX,pivotY 变换基准点，默认居中
    fun scale(repeatCount: Int, duration: Long, vararg value: Float, pivotX: Float = w / 2f, pivotY: Float = h / 2f) {
        scaleEnd()
        viewGroup?.apply {
            this.pivotX = pivotX
            this.pivotY = pivotY
            //支持多个属性，同时变化，放心会同时变化的。
            objectAnimatorScaleX = ofFloat("scaleX", repeatCount, duration, *value)
            objectAnimatorScaleY = ofFloat("scaleY", repeatCount, duration, *value)
        }
    }

    //暂停缩放（属性会保持当前的状态）
    fun scalePause() {
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
    fun scaleResume() {
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
    fun scaleEnd() {
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
    //调用案例;rotation(0,500,0f,360f)
    /**
     * @param repeatCount 动画次数，0是一次，1是两次，以此类推
     * @param duration 动画时间，一个循环的动画时间。即一个次数的时间。不是所有次数。是一次的时间
     * pivotX,pivotY 变换基准点，默认居中
     */
    fun rotation(repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null, pivotX: Float = w / 2f, pivotY: Float = h / 2f): ObjectAnimator {
        rotationEnd()
        viewGroup?.apply {
            this.pivotX = pivotX
            this.pivotY = pivotY
            objectAnimatorRotation = ofFloat("rotation", repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
        }
        return objectAnimatorRotation!!
    }

    //暂停旋转（属性会保持当前的状态）
    fun rotationPause() {
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
    fun rotationResume() {
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
    fun rotationEnd() {
        objectAnimatorRotation?.let {
            it.end()//fixme 一旦调用了end()属性动画也就结束了，并且属性也会恢复到原始状态。
            objectAnimatorRotation = null
        }
        objectAnimatorRotation?.let {
            it.end()
            objectAnimatorRotation = null
        }
    }

    protected var kmmWidth: Int = 0
    protected var kmmHeight: Int = 0
    protected var kmmLeftMargin: Int = 0
    protected var kmmTopMargin: Int = 0
    protected var kmmRightMargin: Int = 0
    protected var kmmBottomMargin: Int = 0
    protected var kmmScale: Float = 1f
    protected var kmmRotation: Float = 0f
    protected var kmmAlpha: Float = 1F

    var kTimer: KTimerUtils.KTimer? = null

    //fixme 定时刷新
    fun refresh(count: Long = 60, unit: Long = 1000, firstUnit: Long = 0, callback: (num: Long) -> Unit): KTimerUtils.KTimer? {
        if (context != null && context is Activity) {
            refreshEnd()
            kTimer = KTimerUtils.refreshUI(context as Activity, count, unit, firstUnit, callback)
        }
        return kTimer
    }

    //暂停
    fun refreshPause() {
        kTimer?.let {
            it.pause()
        }
    }

    //判断是否暂停
    fun isRefreshPause(): Boolean {
        var pause = false
        kTimer?.let {
            pause = it.isPause()
        }
        return pause
    }

    //继续
    fun refreshResume() {
        kTimer?.let {
            it.resume()
        }
    }

    //定时器停止
    fun refreshEnd() {
        kTimer?.let {
            //一个View就添加一个定时器，防止泄露。
            it.pause()
            it.end()//如果定时器不为空，那一定要先停止之前的定时器。
            kTimer = null
        }
    }

    //fixme 选择器

    open fun onPress(isWaterRipple: Boolean = false) {
        //这两个颜色，比较和谐。
        if (isWaterRipple) {
            //波浪效果(有效果，但是短按，就看不出来。需要长按一下效果才明显),效果不怎么好。
            //KSelectorUtils.selectorRippleDrawable(this, Color.WHITE, Color.parseColor("#E4E4E4"))
            //使用自定义的波浪效果。系统的有时没效果，而且还不好。
            this.isWaterRipple = true
        } else {
            //平常效果(默认)
            KSelectorUtils.selectorColor(viewGroup, Color.parseColor("#ffffff"), Color.parseColor("#E4E4E4"))
        }
    }

    fun selectorRippleDrawable(NormalColor: String, RipperColor: String, SelectColor: String? = null, FocuseColor: String? = null, HoverColor: String? = RipperColor, strokeWidth: Int = 0, strokeColor: Int = Color.TRANSPARENT, dashWidth: Float = 0F, dashGap: Float = 0F, all_radius: Float = 0f, left_top: Float = all_radius, right_top: Float = all_radius, right_bottom: Float = all_radius, left_bottom: Float = all_radius) {
        KSelectorUtils.selectorRippleDrawable(viewGroup!!, KSelectorUtils.parseColor(NormalColor)!!, KSelectorUtils.parseColor(RipperColor)!!, KSelectorUtils.parseColor(SelectColor), KSelectorUtils.parseColor(FocuseColor), KSelectorUtils.parseColor(HoverColor), strokeWidth, strokeColor, dashWidth, dashGap, all_radius, left_top, right_top, right_bottom, left_bottom)
    }

    /**
     * fixme 波纹点击本身就是一种按下的效果。所以在此波纹颜色取代点击颜色。
     * fixme 聚焦和悬浮时，并不会有波纹效果（波纹效果一定是点击时才会发生）仅仅只是被盖上了一层遮罩层而已。
     * @param NormalColor 正常背景颜色
     * @param SelectColor 选中时颜色
     * @param FocuseColor 聚焦时颜色
     * @param HoverColor 鼠标悬浮时颜色
     * @param RipperColor 点击时波纹颜色,不可为空
     * @param strokeWidth 边框宽度
     * @param strokeColor 边框颜色
     * @param dashWidth 虚线宽度
     * @param dashGap 虚线间隔
     * @param all_radius 所有圆角度数
     * @param left_top 左上角圆角
     * @param right_top 右上角圆角
     * @param right_bottom 右下角圆角
     * @param left_bottom 左下角圆角
     */
    fun selectorRippleDrawable(NormalColor: Int, RipperColor: Int, SelectColor: Int? = null, FocuseColor: Int? = null, HoverColor: Int? = RipperColor, strokeWidth: Int = 0, strokeColor: Int = Color.TRANSPARENT, dashWidth: Float = 0F, dashGap: Float = 0F, all_radius: Float = 0f, left_top: Float = all_radius, right_top: Float = all_radius, right_bottom: Float = all_radius, left_bottom: Float = all_radius) {
        KSelectorUtils.selectorRippleDrawable(viewGroup!!, NormalColor, RipperColor, SelectColor, FocuseColor, HoverColor, strokeWidth, strokeColor, dashWidth, dashGap, all_radius, left_top, right_top, right_bottom, left_bottom)
    }


    fun selectorDrawable(NormalColor: String, PressColor: String, SelectColor: String? = PressColor, FocuseColor: String? = null, HoverColor: String? = PressColor, strokeWidth: Int = 0, strokeColor: Int = Color.TRANSPARENT, dashWidth: Float = 0F, dashGap: Float = 0F, all_radius: Float = 0f, left_top: Float = all_radius, right_top: Float = all_radius, right_bottom: Float = all_radius, left_bottom: Float = all_radius) {
        KSelectorUtils.selectorDrawable(viewGroup!!, KSelectorUtils.parseColor(NormalColor)!!, KSelectorUtils.parseColor(PressColor)!!, KSelectorUtils.parseColor(SelectColor), KSelectorUtils.parseColor(FocuseColor), KSelectorUtils.parseColor(HoverColor), strokeWidth, strokeColor, dashWidth, dashGap, all_radius, left_top, right_top, right_bottom, left_bottom)
    }

    /**
     * fixme 圆角边框Drawable
     * fixme 聚焦和悬浮时，并不会有波纹效果（波纹效果一定是点击时才会发生）仅仅只是被盖上了一层遮罩层而已。
     * @param NormalColor 正常背景颜色
     * @param SelectColor 选中时颜色
     * @param FocuseColor 聚焦时颜色
     * @param HoverColor 鼠标悬浮时颜色
     * @param RipperColor 点击时波纹颜色,不可为空
     * @param strokeWidth 边框宽度
     * @param strokeColor 边框颜色
     * @param dashWidth 虚线宽度
     * @param dashGap 虚线间隔
     * @param all_radius 所有圆角度数
     * @param left_top 左上角圆角
     * @param right_top 右上角圆角
     * @param right_bottom 右下角圆角
     * @param left_bottom 左下角圆角
     */
    fun selectorDrawable(NormalColor: Int, PressColor: Int, SelectColor: Int? = PressColor, FocuseColor: Int? = null, HoverColor: Int? = PressColor, strokeWidth: Int = 0, strokeColor: Int = Color.TRANSPARENT, dashWidth: Float = 0F, dashGap: Float = 0F, all_radius: Float = 0f, left_top: Float = all_radius, right_top: Float = all_radius, right_bottom: Float = all_radius, left_bottom: Float = all_radius) {
        KSelectorUtils.selectorDrawable(viewGroup!!, NormalColor, PressColor, SelectColor, FocuseColor, HoverColor, strokeWidth, strokeColor, dashWidth, dashGap, all_radius, left_top, right_top, right_bottom, left_bottom)

    }

    /**
     * NormalBtmap 默认背景位图
     * PressBitmap 按下时背景位图
     * SelectBitmap 选中(默认和按下相同)时背景位图
     * FocuseBitmap 聚焦时图片
     * HoverBitmap 鼠标悬浮时图片
     */
    //fixme 添加了聚焦状态+悬浮状态
    fun selectorBitmap(NormalBtmap: Bitmap?, PressBitmap: Bitmap?, SelectBitmap: Bitmap? = PressBitmap, FocuseBitmap: Bitmap? = null, HoverBitmap: Bitmap? = PressBitmap) {
        KSelectorUtils.selectorBitmap(viewGroup, NormalBtmap, PressBitmap, SelectBitmap, FocuseBitmap, HoverBitmap)
    }

    //fixme 添加了聚焦状态+悬浮状态
    fun selectorBitmap(NormalID: Int, PressID: Int?, SelectID: Int? = PressID, FocuseID: Int? = null, HoverID: Int? = PressID) {
        KSelectorUtils.selectorBitmap(viewGroup, KSelectorUtils.parseBitmap(NormalID), KSelectorUtils.parseBitmap(PressID), KSelectorUtils.parseBitmap(SelectID), KSelectorUtils.parseBitmap(FocuseID), KSelectorUtils.parseBitmap(HoverID))
    }


    /**
     * NormalColor fixme 正常背景颜色值,不能为空。防止kotlin分辨不出方法类型。
     * PressColor  按下正常背景颜色值
     * SelectColor 选中(默认和按下相同)背景颜色值
     * FocuseColor 聚焦状态,为了防止冲突，默认设置为空。
     * HoverColor 鼠标悬浮状态，目前模拟器测试不出来。为了以防万一，还是加上
     */
    //fixme 添加了聚焦状态+悬浮状态
    fun selectorColor(NormalColor: Int, PressColor: Int?, SelectColor: Int? = PressColor, FocuseColor: Int? = null, HoverColor: Int? = PressColor) {
        KSelectorUtils.selectorColor(viewGroup, NormalColor, PressColor, SelectColor, PressColor, FocuseColor)
    }

    //fixme 添加了聚焦状态+悬浮状态
    fun selectorColor(NormalColor: String, PressColor: String?, SelectColor: String? = PressColor, FocuseColor: String? = null, HoverColor: String? = PressColor) {
        KSelectorUtils.selectorColor(viewGroup, KSelectorUtils.parseColor(NormalColor)!!, KSelectorUtils.parseColor(PressColor), KSelectorUtils.parseColor(SelectColor), KSelectorUtils.parseColor(FocuseColor), KSelectorUtils.parseColor(HoverColor))
    }

    /**
     * NormalColor 正常字体颜色值
     * PressColor  按下正常字体颜色值
     * SelectColor 选中(默认和按下相同)字体颜色值
     * FocuseColor 聚焦状态,为了防止冲突，默认设置为空。
     * HoverColor 鼠标悬浮状态，目前模拟器测试不出来。为了以防万一，还是加上
     */
    //fixme 添加了聚焦状态+悬浮状态
    fun selectorTextColor(NormalColor: Int, PressColor: Int?, SelectColor: Int? = PressColor, FocuseColor: Int? = null, HoverColor: Int? = PressColor) {
        KSelectorUtils.selectorTextColor(viewGroup, NormalColor, PressColor, SelectColor, FocuseColor, HoverColor)
    }


    //fixme 添加了聚焦状态+悬浮状态
    fun selectorTextColor(NormalColor: String, PressColor: String?, SelectColor: String? = PressColor, FocuseColor: String? = null, HoverColor: String? = null) {
        KSelectorUtils.selectorTextColor(viewGroup, KSelectorUtils.parseColor(NormalColor)!!, KSelectorUtils.parseColor(PressColor), KSelectorUtils.parseColor(SelectColor), KSelectorUtils.parseColor(FocuseColor), KSelectorUtils.parseColor(HoverColor))
    }

//                                fxime 多图片渐变动画调用案例
//                                GlobalScope.async {
//                                var overrideWidth = kpx.x(640)
//                                var overrideHeight = kpx.x(795)
//                                //fixme 位图
//                                var bm1 = KGlideUtils.getBitmapFromResouce(R.mipmap.timg, overrideWidth, overrideHeight)
//                                var bm2 = KGlideUtils.getBitmapFromResouce(R.mipmap.timg2, overrideWidth, overrideHeight)
//                                startTransition(bm1,bm2,durationMillis=3000)
//                            }
    var imageTransitionDrawable: TransitionDrawable? = null
    var imageTransitionBitmap: ArrayList<Bitmap?>? = null//保存渐变位图；方便销毁

    /**
     * fixme 多图片渐变动画
     * @param args 多个位图
     * @param durationMillis 渐变时间;从第一个位图到第二个位图的过渡时间。单位毫秒。
     */
    fun startTransition(vararg args: Bitmap?, durationMillis: Int = 3000) {
        try {
            if (imageTransitionBitmap == null) {
                imageTransitionBitmap = ArrayList()
            }
            destroyTransitionBitmap()
            var drawables: Array<BitmapDrawable?> = arrayOfNulls(args.size)
            var index = 0
            args?.forEach {
                drawables[index++] = KBitmapUtils.bitmapToDrawable(it)
                imageTransitionBitmap?.add(it)//记录渐变位图
            }
            if (drawables.size > 0) {
                //fixme 位图渐变对象
                imageTransitionDrawable = TransitionDrawable(drawables)
                context?.runOnUiThread {
                    //fixme 必须在主线程中设置才有效。
                    if (imageTransitionDrawable != null) {
                        //设置背景图片为渐变图片
                        setBackgroundDrawable(imageTransitionDrawable)
                        //经过3000ms的图片渐变过程
                        imageTransitionDrawable?.startTransition(durationMillis)
                    }
                }
            }
        } catch (e: Exception) {
            KLoggerUtils.e("K2AnimeWidget->startTransition()异常：\t" + e.message)
        }
    }

    //fixme 销毁渐变位图
    fun destroyTransitionBitmap() {
        imageTransitionBitmap?.forEach {
            it?.let {
                if (!it.isRecycled) {
                    it.recycle()//fixme 位图销毁
                }
            }
        }
        imageTransitionBitmap?.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        objectAnimatorScaleX = null
        objectAnimatorScaleY = null
        objectAnimatorRotation = null
        kTimer?.end()
        kTimer = null
        destroyTransitionBitmap()
        imageTransitionDrawable?.clearColorFilter()
        imageTransitionDrawable = null
        imageTransitionBitmap?.clear()
        imageTransitionBitmap = null
    }

}
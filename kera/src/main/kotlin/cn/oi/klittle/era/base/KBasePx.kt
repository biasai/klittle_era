package cn.oi.klittle.era.base

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Paint
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.*
import cn.oi.klittle.era.utils.KLanguageUtil
import cn.oi.klittle.era.utils.KStringUtils
import org.jetbrains.anko.contentView
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

/**
 * Created by 彭治铭 on 2018/7/21.
 */
open class KBasePx {
    var statusHeight = 0//状态栏高度
    var navigationBarHeight = 0//底部虚拟键高度。(如果为0。则表示没有底部虚拟按键)
    var baseWidth = 750f//基准宽
    var baseHeight = 1334f//基准高
    var horizontalProportion: Float = 0.toFloat()//真实水平比例大小
    var verticalProportion: Float = 0.toFloat()//真实垂直比例大小
    var density: Float = 0.toFloat()//当前设备dpi密度值比例，即 dpi/160 的比值
    var ignorex: Boolean = false//是否忽悠比例缩放
    var ignorey: Boolean = false//是否忽悠比例缩放
    /**
     * fixme 注意，宽（水平方向）和高（垂直方向）是会随屏幕切屏（横屏竖屏）而改变的。在此我们已竖屏为标准。
     */
    private var screenWidth = 0f//屏幕宽(以竖屏为标准，宽度比高度小)
    //fixme 屏幕高
    private var screenHeight = 0f

    //fixme 屏幕最大，屏幕真正的最大高度。（不是Activity的contentView视图的真实高度。）contentViewHeight()可以获取真实的高度。
    //fixme 对全屏状态和有状态栏状态。都适用。都一样。都能获取最真实的最大高度。(亲测)
    fun maxScreenHeight(): Int {
        navigationBarHeight = getNavigationBarHeight(KBaseApplication.getInstance())
        if (navigationBarHeight <= 0) {
            //当导航栏高度为0时,screenHeight就是整个屏幕的高(包含了状态栏的高度).
            return screenHeight.toInt()
        } else {
            //但导航栏高度大于0时,screenHeight即不包含导航栏,也不包含状态栏.
            return (screenHeight + navigationBarHeight).toInt() + statusHeight
        }
    }

    /**
     * fixme 获取Activity视图contentView的实际高度。最真实的高度（不用再管什么状态栏导航栏的高度，系统会自动判断是否有导航栏，返回真实的高度）
     * fixme 建议使用这个方法；比maxScreenHeight()要可靠。
     * fixme 该方法即可以获取视图高度，也可以判断Activity是否加载完成；加载完成了才会有高度。
     * @param activity
     * @param delay 延迟加载实际；单位毫秒；一般100毫秒足已；200毫秒以内肉眼是分辨不出来的。
     * @param callBack 回调，返回高度。contentView的高度必须等Activity加载完成了才会有值。所以才回调。
     */
    fun contentViewHeight(activity: Activity? = KBaseUi.getActivity(), delay: Long = 150, callBack: ((height: Int) -> Unit)?) {
        if (activity == null || callBack == null) {
            return
        }
        try {
            activity?.apply {
                if (!isFinishing) {//判断Activity是否关闭
                    //fixme 获取ContentView的高度；测试Activity至少要加载86毫秒之后才会有高度。
                    contentView?.height?.let {
                        if (it > 0) {
                            var height = it
                            callBack?.let {
                                it(height)
                            }
                            return
                        }
                    }
                    if (delay > 10) {//判断延迟时间，防止无限循环加载
                        GlobalScope.async {
                            delay(delay)//延迟
                            var delay = delay - 10//时间递减；从150毫秒递减相加，为1190毫秒。
                            runOnUiThread {
                                contentViewHeight(activity, delay, callBack)
                            }
                        }
                    } else {
                        callBack?.let {
                            it(maxScreenHeight())//fixme 直接返回最大高度
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //设置最外层视图的高度
    fun setWindowHeight(window: Window?, height: Int?) {
        if (height == null) {
            return
        }
        try {
            getWindowContentView(window)?.let {
                it.layoutParams?.let {
                    it.height = height
                }
                it.requestLayout()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //设置最外层视图的宽度
    fun setWindowWidth(window: Window?, width: Int?) {
        if (width == null) {
            return
        }
        try {
            getWindowContentView(window)?.let {
                it.layoutParams?.let {
                    it.width = width
                }
                it.requestLayout()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //设置最外层视图的宽度和高度
    fun setWindow(window: Window?, width: Int?, height: Int?) {
        try {
            getWindowContentView(window)?.let {
                it.layoutParams?.let {
                    if (width != null) {
                        it.width = width
                    }
                    if (height != null) {
                        it.height = height
                    }
                }
                it.requestLayout()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //获取window视图；一定要在setContentView()添加布局之后，再调用，才有效。
    fun getWindowContentView(window: Window?): View? {
        try {
            return window?.decorView?.findViewById<ViewGroup>(android.R.id.content)?.getChildAt(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    //fixme 监听window视图加载；视图刷选时也会回调。
    fun addOnGlobalLayoutListener(window: Window?, callBack: () -> Unit) {
        getWindowContentView(window)?.getViewTreeObserver()?.addOnGlobalLayoutListener(ViewTreeObserver.OnGlobalLayoutListener { callBack?.let { it() } })
    }

    /**
     * 获取当前Activity屏幕方向，true竖屏，false横屏
     */
    fun oritation(activity: Activity): Boolean {
        if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            return false
        }
        //ActivityInfo.SCREEN_ORIENTATION_PORTRAIT 竖屏
        return true
    }

    /**
     * fixme 获取屏幕宽，isVertical true以竖屏为标准。默认是。false以横屏为标准
     */
    fun screenWidth(isVertical: Boolean = true): Int {
        if (isVertical) {
            if (screenWidth < screenHeight) {
                return screenWidth.toInt()//竖屏宽度比高度小
            } else {
                return screenHeight.toInt()
            }
        } else {
            if (screenWidth > screenHeight) {
                return screenWidth.toInt()//横屏宽度比高度大
            } else {
                return screenHeight.toInt()
            }
        }
    }

    /**
     * fixme 获取屏幕高，isVertical true以竖屏为标准。默认是。false以横屏为标准
     */
    fun screenHeight(isVertical: Boolean = true, context: Context? = KBaseUi.getActivity()): Int {
        if (isVertical) {
            if (screenHeight > screenWidth) {
                return screenHeight.toInt()//竖屏，高度比宽度大
            } else {
                return screenWidth.toInt()
            }
        } else {
            if (screenHeight < screenWidth) {
                return screenHeight.toInt()//横屏，高度比宽度小
            } else {
                return screenWidth.toInt()
            }
        }
    }


    init {
        init()
    }

    //初始化，基准宽或高，发生变化时(以竖屏为标准)，可以手动调用，重新初始化
    //fixme 注意：以竖屏为标准，宽度比高度小(高度大于宽度)
    fun init(baseWidth: Float = 750f, baseHeight: Float = 1334f) {
        this.baseWidth = baseWidth
        this.baseHeight = baseHeight
        //真实值
        var displayMetrics: DisplayMetrics? = context()?.resources?.displayMetrics
        screenWidth = displayMetrics!!.widthPixels.toFloat()
        screenHeight = displayMetrics.heightPixels.toFloat()
        if (displayMetrics != null) {
            density = displayMetrics.density
        }
        if (screenWidth > screenHeight) {
            var w = screenWidth
            screenWidth = screenHeight
            screenHeight = w
        }
        horizontalProportion = screenWidth / baseWidth
        verticalProportion = screenHeight / baseHeight
        //获取状态栏的高度
        statusHeight()
        //獲取底部導航欄高度[親測可行。]
        navigationBarHeight = getNavigationBarHeight(KBaseApplication.getInstance())
        ignorex()
        ignorey()
    }

    private fun ignorex() {
        //防止比例为1的时候做多余的适配
        if (horizontalProportion >= 0.999 && horizontalProportion <= 1.001) { //750/720=1.04166 苹果/安卓
            ignorex = true
        } else {
            ignorex = false
        }
    }

    private fun ignorey() {
        //防止比例为1的时候做多余的适配
        if (verticalProportion >= 0.999 && verticalProportion <= 1.001) { //1334/1280=1.04218 苹果/安卓
            ignorey = true
        } else {
            ignorey = false
        }
    }

    private var urlMap = HashMap<String, Boolean?>()//true 正在对该问题进行压缩操作，false没有操作。

    /**
     * 判断是否重复 fixme 添加了同步锁 @Synchronized
     * @return true 并发操作了，false没有并发操作
     */
    private @Synchronized
    fun isRpeateKey(url: String?, src: Bitmap?): Boolean {
        var isRepeat1: Boolean? = false
        var isRepeat2: Boolean? = false
        src?.let {
            isRepeat1 = urlMap?.get(it.toString())
        }
        url?.let {
            isRepeat2 = urlMap?.get(it)//fixme url地址作为标识
        }
        if ((isRepeat1 == null || isRepeat1 == false) && (isRepeat2 == null || isRepeat2 == false)) {
            return false
        } else {
            return true
        }
    }

    //存储键
    fun putKey(url: String?, src: Bitmap?) {
        url?.let {
            urlMap?.put(it, true)//正在操作该位图。
        }
        src?.let {
            urlMap?.put(it.toString(), true)
        }
    }

    //清除键
    fun removeKey(url: String?, src: Bitmap?) {
        url?.let {
            urlMap?.put(it, false)//该位图压缩完成
            urlMap?.remove(url)
        }
        src?.let {
            urlMap?.put(it.toString(), false)
            urlMap?.remove(it.toString())
        }
    }

    //fixme 清除所有键;在BaseActivity里的onCreate()方法调用一次；防止万一异常。
    fun removeAllKey() {
        urlMap?.clear()
    }

    fun keyBitmap(key: Int, src: Bitmap, width: Int, height: Int, isCompress: Boolean? = true, isRecycle: Boolean? = true, callBack: (bitmap: Bitmap) -> Unit) {
        keyBitmap(key.toString(), src, width, height, isCompress, isRecycle, callBack)
    }

    /**
     * 位图压缩；（fixme 防止对同一个位图的并发操作。）暴力测试过，能够解决位图并发操作。建议使用。
     * @param url 该网络位图的标识，最好就是它本身的url(统一一下)。
     * @param isCompress 是否压缩
     * @param isRecycle 压缩之后，是否释放原图。
     */
    fun keyBitmap(url: String?, src: Bitmap, width: Int, height: Int, isCompress: Boolean? = true, isRecycle: Boolean? = true, callBack: (bitmap: Bitmap) -> Unit) {
        try {
            var src = src
            if (src == null || src.isRecycled || (isCompress == null || !isCompress) || (width == 0 && height == 0)) {
                //fixme 不压缩
                try {
                    callBack?.let {
                        it(src)
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                //fixme 清除键
                removeKey(url, src)
                return
            }
            GlobalScope.async {
                if (!isRpeateKey(url, src)) {
                    //KLoggerUtils.e("没有重复：\t" + src + "\turl:\t" + url + "\t" + src.isRecycled)
                    //fixme 开始操作位图，缓存键
                    putKey(url, src)
                    try {
                        var bitmap = compressBitmap(src, width, height, isRecycle)//fixme 调用compressBitmap()方法（宽和高都为0时不会压缩），比xBitmap()更安全。
                        callBack?.let {
                            it(bitmap)
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    GlobalScope.async {
                        delay(10)//避免并发，还是延迟一下。（亲测，延迟一下很好）
                        //fixme 位图操作完成，清除键
                        removeKey(url, src)
                    }
                } else {
                    //fixme 对同一个位图操作重复;延迟等待。
                    try {
                        var delay = 10L//延迟请求(最低值)；fixme 一个位图的压缩时间，一般只需要2毫秒左右就够了。(建议还是不要低于10毫秒)
                        var radom0 = KStringUtils.getRandom(2).toLong()//范围（0~99）
                        var radom1 = KStringUtils.getRandom(2).toLong()
                        var radom2 = KStringUtils.getRandom(2).toLong()
                        var time = delay + radom0 + radom1 + radom2
                        //KLoggerUtils.e("重复压缩延迟时间：\t" + time + "\turl:\t" + url + "\tsrc:\t" + src + "\t" + src.isRecycled)
                        delay(time)//延迟
                        keyBitmap(url, src, width, height, isCompress, isRecycle, callBack)//fixme 递归
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            removeKey(url, src)//fixme 异常移除键
        }
    }

    /**
     * 根据传入的宽和高，压缩位图;fixme 压缩一个位图时间只要几毫秒就够了，一般2毫秒。
     * @param w 压缩后的宽度;(宽和高传一边即可，另一边会自动按比例计算出来)
     * @param h 压缩后的高度，一般只需要传宽度即可。高度会按比例自动计算出来。
     * @param isRecycle fixme 是否释放原图,默认释放原图
     */
    //fixme 为了防止异常，一定要尽可能的先判断src.isRecycled
    //fixme 多个控件，对同一个位图操作时，容易并发，会报错，建议使用keyBitmap()
    fun compressBitmap(src: Bitmap, w: Int = 0, h: Int = 0, isRecycle: Boolean? = true): Bitmap {
        if (src.isRecycled || (src.width == w && src.height == h) || (w == 0 && h == 0)) {
            //fixme 就返回原始图片，不要新建位图；太鸡肋了。也很危险。直接返回原图最安全。传入的宽和高不能都为0，都为0不会压缩（防止异常无限压缩）。
            return src
        }
        var width = w
        var height = h
        //如果宽度和高度小于0。就以位置自身的宽和高进行适配
        if (!src.isRecycled && width <= 0 && h > 0) {
            var p = h.toFloat() / src.height.toFloat()
            width = (src.width * p).toInt()
        }
        if (!src.isRecycled && height <= 0 && w > 0) {
            var p = w.toFloat() / src.width.toFloat()
            height = (src.height * p).toInt()
        }
        if (!src.isRecycled && width > 0 && height > 0 && src.width != width) {
            var bm = GeomeBitmap(src, width = width.toFloat(), height = height.toFloat(), isRecycle = isRecycle)
            return bm
        }
        return src
    }

    /**
     * 以x为标准适配位图;fixme 压缩一个位图时间只要几毫秒就够了，一般2毫秒。
     * @param w 压缩后的宽度
     * @param h 压缩后的高度，一般只需要传宽度即可。高度会按比例自动计算出来。
     * @param isRecycle fixme 是否释放原图,默认释放原图
     */
    //fixme 为了防止异常，一定要尽可能的先判断src.isRecycled
    //fixme 多个控件，对同一个位图操作时，容易并发，会报错，建议使用keyBitmap()
    fun xBitmap(src: Bitmap, w: Int = 0, h: Int = 0, isRecycle: Boolean? = true): Bitmap {
        if (src.isRecycled || (src.width == w && src.height == h)) {
            //fixme 就返回原始图片，不要新建位图；太鸡肋了。也很危险。直接返回原图最安全。
            return src
        }
        var width = w
        var height = h
        //如果宽度和高度小于0。就以位置自身的宽和高进行适配
        if (!src.isRecycled && width <= 0) {
            //以水平X轴，为标准进行适配
            width = x(src.width)
        }
        if (!src.isRecycled && height <= 0) {
            height = x(src.height)
        }
        if (!src.isRecycled && width > 0 && height > 0 && src.width != width) {
            var bm = GeomeBitmap(src, width = width.toFloat(), height = height.toFloat(), isRecycle = isRecycle)
            return bm
        }
        return src
    }

    //以y为标准适配位图
    //isRecycle 是否释放原图
    fun yBitmap(src: Bitmap, w: Int = 0, h: Int = 0, isRecycle: Boolean = true): Bitmap {
        if (src.isRecycled || (src.width == w && src.height == h)) {
            return src
        }
        var width = w
        var height = h
        //如果宽度和高度小于0。就以位置自身的宽和高进行适配
        if (!src.isRecycled && width <= 0) {
            //以垂直Y轴，为标准进行适配
            width = y(src.width)
        }
        if (!src.isRecycled && height <= 0) {
            height = y(src.height)
        }
        if (!src.isRecycled && width > 0 && height > 0 && src.width != width) {
            var bm = GeomeBitmap(src, width = width.toFloat(), height = height.toFloat(), isRecycle = isRecycle)
            return bm
        }
        return src
    }

    //等比压缩，压缩之后，宽和高相等。
    //参数values是压缩后的宽度及高度。
    //计算比率时，千万要注意，一定要使用float类型。千万不要使用int类型。不然计算不出。
    //这个方法，图片不会变形[取中间的那一部分]
    //isRecycle 是否释放原图
    fun GeomeBitmap(src: Bitmap, value: Float, isRecycle: Boolean = true): Bitmap {
        if (src.isRecycled || (src.width == value.toInt() && src.height == value.toInt()) || value <= 0f) {
            return src//防止重复压缩
        }
        var dst: Bitmap? = null
        if (!src.isRecycled && src.width == src.height) {
            dst = Bitmap.createScaledBitmap(src, value.toInt(), value.toInt(), true)
        } else if (!src.isRecycled) {
            //以较小边长为计算标准
            //宽小于高
            if (!src.isRecycled && src.width < src.height) {
                val p = src.width.toFloat() / value
                val heith = src.height.toFloat() / p
                if (!src.isRecycled) {
                    dst = Bitmap.createScaledBitmap(src, value.toInt(), heith.toInt(), true)
                    val y = (dst.height - dst.width) / 2
                    dst = Bitmap.createBitmap(dst, 0, y, dst.width, dst.width)
                }
            } else if (!src.isRecycled) {
                //宽大于高，或等于高。
                //高小于宽
                val p = src.height.toFloat() / value
                val width = src.width.toFloat() / p
                if (!src.isRecycled) {
                    dst = Bitmap.createScaledBitmap(src, width.toInt(), value.toInt(), true)
                    val x = (dst.width - dst.height) / 2
                    dst = Bitmap.createBitmap(dst, x, 0, dst.height, dst.height)
                }
            }
        }
        if (isRecycle) {
            if (src != null && !src.isRecycled && (src.width != value.toInt() || src.height != value.toInt()) && src != dst && dst != null) {
                src?.let {
                    if (!it.equals(dst)) {
                        src?.recycle()
                    }
                }
            }
        }
        if (dst != null) {
            return dst//fixme 这才是压缩后的图片
        } else {
            return src
        }
        //return dst
    }

    /**
     * 不要在适配器里。对图片进行压缩。适配器反反复复的执行。多次执行。会内存溢出的。切记。
     *
     *
     * 根据宽和宽的比率压缩Bitmap
     * 这个方法，图片不会变形（按比例缩放）[取中间的那一部分，即居中截取]
     *
     * @param src   原图
     * @param width 压缩后的宽
     * @param height 压缩后的高（高度最大不会超过比例高度。一般只需要传宽度即可。高度会按比例自动计算出来。）
     * @param isRecycle 是否释放原图
     * @return
     */
    fun GeomeBitmap(src: Bitmap, width: Float, height: Float, isRecycle: Boolean? = true): Bitmap {
        if (src.isRecycled || (src.width == width.toInt() && src.height == height.toInt()) || width <= 0f || height <= 0f) {
            return src//防止重复压缩
        }
        if (!src.isRecycled && width == height) {
            //宽和高相等,等比压缩
            return GeomeBitmap(src, width)
        } else if (!src.isRecycled) {
            //KLoggerUtils.e("0" + "\t" + src.isRecycled)
            var sp = height / width//高的比率
            var dst: Bitmap? = null
            //KLoggerUtils.e("1" + "\t" + src.isRecycled)
            if (src.isRecycled) {
                return src
            }
            val dp = src.height.toFloat() / src.width.toFloat()
            //KLoggerUtils.e("2" + "\t" + src.isRecycled)
            val pp = Math.abs(sp - dp)
            if (pp < 0.01 && !src.isRecycled) {
                //KLoggerUtils.e("3" + "\t" + src.isRecycled)
                //fixme Bitmap.createScaledBitmap 如果缩放位图和原有位图大小差异在1%之内，使用的还是同一个位图对象。
                //fixme 大小差异超过1%左右，使用的就是新的位图，和原位图就没有关系了。
                //fixme 要求的宽和高与位图的宽和高，比例一致。
                //fixme Bitmap.createScaledBitmap宽和高一致时，返回的是同一个位图。
                dst = Bitmap.createScaledBitmap(src, width.toInt(), (width * sp).toInt(), true)
            } else if (!src.isRecycled) {
                //KLoggerUtils.e("4" + "\t" + src.isRecycled)
                //fixme 要求的宽和高与位图的宽和高，比例不一致。按比例要求，居中截取。
                val p = src.width.toFloat() / width
                //KLoggerUtils.e("5" + "\t" + src.isRecycled)
                val heith = src.height.toFloat() / p
                //KLoggerUtils.e("6" + "\t" + src.isRecycled)
                if (!src.isRecycled) {
                    dst = Bitmap.createScaledBitmap(src, width.toInt(), heith.toInt(), true)
                    //KLoggerUtils.e("7" + "\t" + src.isRecycled)
                    val height = (width * sp).toInt()
                    //要求高，小于压缩后的高。对压缩后的高进行截取
                    if (dst != null && !dst.isRecycled && (height < dst!!.height)) {
                        //KLoggerUtils.e("8" + "\t" + src.isRecycled)
                        val y = (dst.height - height) / 2
                        dst = Bitmap.createBitmap(dst, 0, y, dst.width, height)
                    }
                }
            }
            if (isRecycle != null && isRecycle) {
                //KLoggerUtils.e("9" + "\t" + src.isRecycled)
                if (src != null && !src.isRecycled && (src.width != width.toInt() || src.height != height.toInt()) && src != dst && dst != null) {
                    //fixme 为了防止异常，一定要尽可能的先判断src.isRecycled
                    //src?.recycle()//fixme 可能会报  Abort message: 'Error, cannot access an invalid/free'd bitmap here!' 错误
                    //fixme 位图释放，也可能会报：Channel is unrecoverably broken and will be disposed!错误
                    src?.let {
                        if (!it.equals(dst)) {
                            src?.recycle()
                        }
                    }
                }
                //KLoggerUtils.e("10" + "\t" + src.isRecycled)
            }
            if (dst != null) {
                return dst//fixme 这才是压缩后的图片
            } else {
                return src
            }
            //return dst
        }
        return src
    }


    /**
     * fixme 适配x值(默认全屏)，以竖屏为标准。
     */
    fun x(x: Int = baseWidth.toInt()): Int {
        var x2 = x(x.toFloat()).toInt()
        if (x != 0 && x2 == 0) {
            x2 = 1
        }
        return x2
    }

    //Int类型已经添加了默认参数，Float就不能添加默认参数了。不然无法识别
    fun x(x: Float): Float {
        if (ignorex) {
            return x
        }
        var x2 = x * horizontalProportion
        if (x != 0f && x2 == 0f) {
            x2 = 1f
        }
        return x2
    }

    /**
     * fixme 适配y值，始终以竖屏为标准。
     */
    fun y(y: Int = baseHeight.toInt()): Int {
        var y2 = y(y.toFloat()).toInt()
        if (y != 0 && y2 == 0) {
            y2 = 1
        }
        return y2
    }

    fun y(y: Float): Float {
        if (ignorey) {
            return y;
        }
        var y2 = y * verticalProportion
        if (y != 0f && y2 == 0f) {
            y2 = 1f
        }
        return y2
    }


    //是否只对中国语言进行字体缩放。(默认只对中国语言字体进行缩放)
    var isEnableTextSizeScaleForOnlyChinese = true//fixme true 只对中国语言进行缩放；false 对所有语言都进行缩放处理。

    fun isEnableTextSizeScaleForOnlyChinese(isEnableTextSizeScaleForOnlyChinese: Boolean) {
        this.isEnableTextSizeScaleForOnlyChinese = isEnableTextSizeScaleForOnlyChinese
    }

    //判断当前语言是否需要缩放(亲测有效)
    private fun isSclaeForLanguage(): Boolean {
        if (isEnableTextSizeScaleForOnlyChinese) {
            if (KLanguageUtil.isChinese(KBaseActivityManager.getInstance().stackTopActivity)) {
                return true //只对中国语言进行缩放
            } else {
                return false//其他语言不缩放
            }
        }
        return true//所有语言都缩放
    }

    /**
     * fixme 控制文本大小缩放倍率。(等于1时，不会进行缩放处理。)
     */
    var textSizeXScale = 1f
    var textSizeYScale = 1f

    private var textSizeScale_max_default = 38f//最大缩放文本，默认值
    private var textSizeScale_min_default = 8f//最小缩放文本，默认值

    //fixme 大于textSizeXScale_min并且小于textSizeXScale_max才会进行缩放处理(该区间才会缩放)；(等于不进行缩放。)
    var textSizeXScale_max = textSizeX(textSizeScale_max_default, false)//缩放之后的最大字体；超过这个大小，不进行缩放
    var textSizeXScale_min = textSizeX(textSizeScale_min_default, false)//缩放之后的最小字体；小于这个大小，也不进行缩放处理。

    var textSizeYScale_max = textSizeY(textSizeScale_max_default, false)
    var textSizeYScale_min = textSizeY(textSizeScale_min_default, false)

    fun textSizeXScale(textSizeXScale: Float = 1f) {
        this.textSizeXScale = textSizeXScale
    }

    fun textSizeXScale(textSizeXScale: Int = 1) {
        this.textSizeXScale = textSizeXScale.toFloat()
    }

    fun textSizeYScale(textSizeYScale: Float = 1f) {
        this.textSizeYScale = textSizeYScale
    }

    fun textSizeYScale(textSizeYScale: Int = 1) {
        this.textSizeYScale = textSizeYScale.toFloat()
    }

    /**
     * fixme 设置缩放最大字体；单位像素
     */
    fun textSizeXScale_max(textSizeXScale_max: Int = textSizeScale_max_default.toInt()) {
        this.textSizeXScale_max = textSizeX(textSizeXScale_max, false)//fixme 像素会自动转dp
    }

    fun textSizeXScale_max(textSizeXScale_max: Float = textSizeScale_max_default) {
        this.textSizeXScale_max = textSizeX(textSizeXScale_max, false)
    }

    /**
     * fixme 设置缩放最小字体；单位像素
     */
    fun textSizeXScale_min(textSizeXScale_min: Int = textSizeScale_min_default.toInt()) {
        this.textSizeXScale_min = textSizeX(textSizeXScale_min, false)//fixme 像素自动转dp
    }

    fun textSizeXScale_min(textSizeXScale_min: Float = textSizeScale_min_default) {
        this.textSizeXScale_min = textSizeX(textSizeXScale_min, false)
    }


    fun textSizeYScale_max(textSizeYScale_max: Int = textSizeScale_max_default.toInt()) {
        this.textSizeYScale_max = textSizeY(textSizeYScale_max, false)
    }

    fun textSizeYScale_max(textSizeYScale_max: Float = textSizeScale_max_default) {
        this.textSizeYScale_max = textSizeY(textSizeYScale_max, false)
    }

    fun textSizeYScale_min(textSizeYScale_min: Int = textSizeScale_min_default.toInt()) {
        this.textSizeYScale_min = textSizeY(textSizeYScale_min, false)
    }

    fun textSizeYScale_min(textSizeYScale_min: Float = textSizeScale_min_default) {
        this.textSizeYScale_min = textSizeY(textSizeYScale_min, false)
    }


    fun textSizeX(x: Float): Float {
        return textSizeX(x, true)//fixme 默认进行缩放
    }

    /**
     * fixme 设置文字大小。以X为标准
     * @param x 文本大小，单位是像素
     * @param isScale fixme 是否进行textSizeXScale进行缩放。
     */
    fun textSizeX(x: Float, isScale: Boolean): Float {
        pixelToDp(x(x)).let {
            if (isScale && textSizeXScale != 1F && it > textSizeXScale_min && it < textSizeXScale_max && isSclaeForLanguage()) {
                (it * textSizeXScale).let {
                    if (it < textSizeXScale_min) {
                        return textSizeXScale_min//fixme 最小文本
                    } else if (it > textSizeXScale_max) {
                        return textSizeXScale_max//fixme 最大文本
                    } else {
                        return it//fixme 缩放文本
                    }
                }
            } else {
                return it//fixme 正常文本
            }
        }
        //return pixelToDp(x(x)) * textSizeXScale//textView.setTextSize单位是dp,且是float类型。设置文字大小。
    }

    fun textSizeX(x: Int): Float {
        return textSizeX(x, true)
        //return pixelToDp(x(x.toFloat())) * textSizeXScale
    }

    fun textSizeX(x: Int, isScale: Boolean): Float {
        return textSizeX(x.toFloat(), isScale)
        //return pixelToDp(x(x.toFloat())) * textSizeXScale
    }

    fun textSizeY(y: Float): Float {
        return textSizeY(y, true)//fixme 默认进行缩放
    }

    /**
     * fixme 设置文字大小。以Y为标准
     * @param y 文本大小，单位是像素
     * @param isScale fixme 是否进行textSizeXScale进行缩放。
     */
    fun textSizeY(y: Float, isScale: Boolean): Float {
        pixelToDp(y(y))?.let {
            if (isScale && textSizeYScale != 1F && it > textSizeYScale_min && it < textSizeYScale_max && isSclaeForLanguage()) {
                (it * textSizeYScale).let {
                    if (it < textSizeYScale_min) {
                        return textSizeYScale_min//fixme 最小文本
                    } else if (it > textSizeYScale_max) {
                        return textSizeYScale_max//fixme 最大文本
                    } else {
                        return it//fixme 缩放文本
                    }
                }
            } else {
                return it//fixme 正常文本
            }
        }
        //return pixelToDp(y(y)) * textSizeYScale//textView.setTextSize单位是dp,且是float类型。设置文字大小。
    }

    fun textSizeY(y: Int): Float {
        return textSizeY(y, true)
        //return pixelToDp(y(y.toFloat())) * textSizeYScale
    }

    fun textSizeY(y: Int, isScale: Boolean): Float {
        return textSizeY(y.toFloat(), isScale)
        //return pixelToDp(y(y.toFloat())) * textSizeYScale
    }

    //与屏幕边缘左边的距离
    fun left(view: View?): Int {
        if (view != null) {
            //获取现对于整个屏幕的位置。
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            return location[0]
        }
        return 0
    }

    //与屏幕边缘右边的距离
    fun right(view: View?): Int {
        if (view != null) {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            return (screenWidth - location[0] - view.width).toInt()
        }
        return 0
    }

    //与屏幕边缘上边的距离
    fun top(view: View?): Int {
        if (view != null) {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            return location[1]
        }
        return 0
    }

    //与屏幕边缘下边的距离
    fun bottom(view: View?): Int {
        if (view != null) {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            return (screenHeight - location[1] - view.height).toInt()
        }
        return 0
    }

    //测量两个View之间的X坐标间距
    fun distanceX(view1: View?, view2: View?): Float {
        if (view1 != null && view2 != null) {
            return view2.x - view1.x
        }
        return 0f
    }

    //测量两个View之间的Y坐标间距
    fun distanceY(view1: View?, view2: View?): Float {
        if (view1 != null && view2 != null) {
            return view2.y - view1.y
        }
        return 0f
    }

    //获取文本居中Y坐标,height：以这个高度进行对其。即对其高度
    fun centerTextY(paint: Paint, height: Float): Float {
        var baseline = (height - (paint.descent() - paint.ascent())) / 2 - paint.ascent()
        return baseline
    }

    //获取文本居中X坐标，以文本居左为计算标准，即：paint.textAlign=Paint.Align.LEFT
    fun centerTextX(text: String, paint: Paint, width: Float): Float {
        val w = paint.measureText(text, 0, text.length)//测量文本的宽度
        var x = (width - w) / 2
        return x
    }

    //获取位图居中Y坐标
    fun centerBitmapY(bitmap: Bitmap, height: Float): Float {
        var y = (height - bitmap.height) / 2
        return y
    }

    fun centerBitmapY(bpHeight: Int, height: Float): Float {
        var y = (height - bpHeight) / 2
        return y
    }

    //获取位图居中X坐标，width对其的宽度[即总宽度]
    fun centerBitmapX(bpWidht: Int, width: Float): Float {
        var x = (width - bpWidht) / 2
        return x
    }

    fun centerBitmapX(bitmap: Bitmap, width: Float): Float {
        var x = (width - bitmap.width) / 2
        return x
    }

    //获取居中X坐标，width对其的宽度[即总宽度]
    fun centerX(littleWidth: Int, bigWidth: Int): Int {
        var x = (bigWidth - littleWidth) / 2
        return x
    }

    //获取居中Y坐标
    fun centerY(littleHeight: Int, bigHeight: Int): Int {
        var y = (bigHeight - littleHeight) / 2
        return y
    }

    /**
     * fixme 单位转换
     */

    //Dp转像素
    fun dpToPixel(dp: Float): Float {
        return dp * density//其中 density就是 dpi/160的比值。
    }

    fun dpToPixel(dp: Int): Int {
        return dp * density.toInt()//其中 density就是 dpi/160的比值。
    }

    //像素转Dp
    fun pixelToDp(px: Float): Float {
        return px / density
    }

    fun pixelToDp(px: Int): Int {
        return px / density.toInt()
    }

    /**
     * sp(估计是 榜(英镑)) 转像素
     */
    fun spToPixel(value: Float): Float {
        context()?.resources?.displayMetrics?.let {
            try {
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, it)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return value
    }

    /**
     * 像素转榜（英镑）
     */
    fun pixelToSp(value: Float): Float {
        var px = spToPixel(1.0f)
        return value / px
    }

    /**
     * Pt 转像素
     */
    fun ptToPixel(value: Float): Float {
        context()?.resources?.displayMetrics?.let {
            try {
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, value, it)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return value
    }

    /**
     * 像素转Pt
     */
    fun pixelToPt(value: Float): Float {
        var px = ptToPixel(1.0f)
        return value / px// fixme Pt：	210.0	像素：	1176.6155	再Pt：	210.00002  (其他单位转换都正常，就是这个Pt有那么万分之一的误差。可忽略)
    }

    /**
     * 英寸转像素;fixme 中国习惯使用厘米，而国外则使用英寸。厘米和英寸单位差不多。
     */
    fun inToPixel(value: Float): Float {
        //KLoggerUtils.e("metrics.xdpi:\t"+context()?.resources?.displayMetrics?.xdpi)
        context()?.resources?.displayMetrics?.let {
            try {
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, value, it)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return value
    }

    /**
     * 像素转英寸
     */
    fun pixelToIn(value: Float): Float {
        var px = inToPixel(1.0f)//1英寸等于多少像素;
        return value / px
    }

    /**
     * 毫米转像素;fixme 与设备有关；比如210mm毫米；在不同的设备上，换算的像素也是不同的。
     * fixme 因为像素不能直接换算成英寸、厘米，要在dpi(每英寸多少点)下才能换算!而各个设备的dpi又不一样。(mm毫米固定单位，而像素是虚拟单位的。)
     */
    fun mmToPixel(value: Float): Float {
        //KLoggerUtils.e("metrics.xdpi:\t"+context()?.resources?.displayMetrics?.xdpi)
        context()?.resources?.displayMetrics?.let {
            try {
                //fixme TypedValue.applyDimension是一个将各种单位的值转换为像素的方法
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, value, it)//fixme 系统方法
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return value
    }

    /**
     * 像素转毫米
     */
    fun pixelToMm(value: Float): Float {
        var px = mmToPixel(1.0f)//1毫米等于多少像素; 1mm==n px;
        return value / px//像素等于多少毫米；fixme 自己想的方法。亲测可行。
    }

    /**
     * 厘米转像素
     */
    fun cmToPixel(value: Float): Float {
        var mm = value * 10//厘米转毫米
        return mmToPixel(mm)
    }


    /**
     * 像素转厘米
     */
    fun pixelToCm(value: Float): Float {
        var px = cmToPixel(1.0f)//1厘米等于多少像素
        return value / px
    }


    var id: Int = 0
        //id不能小于0，-1表示没有id
        get() = id()

    fun id(key: Int): Int {
        return id(key.toString())
    }

    //id生成器(xml系统布局id都是从20亿开始的。所以绝对不会和系统id重复。)
    //即能生成id,也能获取id
    fun id(key: String? = null): Int {
        var key = key
        KBaseActivityManager.getInstance()?.stackTopActivity?.let {
            //KLoggerUtils.e("id class:\t"+it.javaClass.toString())
            //it.javaClass.toString() 类名是不变了。如：class com.example.myapplication.MainActivity
            key = it.javaClass.toString() + key//fixme id和当前Activity绑定；防止多个Activity里的控件id重复。不同Activity之间，最好不要有相同的id。不然很容易发生意想不到的错误。切记。
        }
        //根据键值获取id
        //id不能小于0，-1表示没有id
        //constraintLayout id找不到时，就以父容器为主。(前提：id不能小于0)
        key?.let {
            map[it]?.let {
                return it//如果该键值的id已经存在，直接返回
            }
        }
        //如果id不存在，就重新创建id
        ids++
        //Log.e("test", "id:\t" + ids)
        var id = ids
        key?.let {
            map.put(it, id)
        }
        return id
    }

    /**
     * 获取icon图标的尺寸。
     */
    fun iconSize(): Int {
        var am = KBaseApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        var iconSize = am.getLauncherLargeIconSize()//获取ICON图标的尺寸
        return iconSize
    }

    /**
     * 获得状态栏的高度，单位像素
     *
     * @return
     */
    public fun statusHeight(context: Application? = null): Int {
        if (statusHeight <= 0) {
            try {
                val clazz = Class.forName("com.android.internal.R\$dimen")
                val `object` = clazz.newInstance()
                val height = Integer.parseInt(clazz.getField("status_bar_height")
                        .get(`object`).toString())
                statusHeight = context(context)?.resources?.getDimensionPixelSize(height) ?: 0
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return statusHeight
    }

    /**
     * 获取底部导航栏高度。
     */
    fun getNavigationBarHeight(activity: Application): Int {
        if (isNavigationBarAvailable()) {
            val resources = activity.getResources()
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            val height = resources.getDimensionPixelSize(resourceId)
            return height
        }
        return 0//沒有虛擬鍵，設置高度為0（这个是手动设置的，虚拟键一直都有，只是没有显示出来而已。）
    }

    /**
     * 判断是否有底部虚拟栏
     */
    fun isNavigationBarAvailable(): Boolean {
        var hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
        var hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME)
        return (!(hasBackKey && hasHomeKey))
    }

    /**
     * 判断当前Activity是否有状态栏
     * true 有状态栏，false没有状态栏
     */
    public fun isStatusBarVisible(activity: Activity): Boolean {
        if (activity.getWindow().getAttributes().flags and WindowManager.LayoutParams.FLAG_FULLSCREEN === 0) {
            return true//有状态栏
        } else {
            return false//没有有状态栏
        }
    }

    //通过反射获取ActivityThread【隐藏类】
    private fun getActivityThread(): Any? {
        try {
            val clz = Class.forName("android.app.ActivityThread")
            val method = clz.getDeclaredMethod("currentActivityThread")
            method.isAccessible = true
            return method.invoke(null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private var application: Application? = null

    //上下文
    //px.context(this.application) 或 px.context()
    fun context(context: Application? = null): Application? {
        if (application != null) {
            return application
        }
        context?.let {
            application = context
        }
        if (application == null) {
            //如果配置文件没有声明，也没有手动初始化。则通过反射自动初始化。【反射是最后的手段，效率不高】
            //通过反射，手动获取上下文。
            val activityThread = getActivityThread()
            if (null != activityThread) {
                try {
                    val getApplication = activityThread.javaClass.getDeclaredMethod("getApplication")
                    getApplication.isAccessible = true
                    application = getApplication?.invoke(activityThread) as Application ?: null
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
        return application
    }

    companion object {
        /**
         * fixme id记录集合；静态。子类共享。
         */
        var ids = 1000//记录id生成的个数，依次叠加，保证不重复。
        var map = mutableMapOf<String, Int>()//保存id键值
    }

}
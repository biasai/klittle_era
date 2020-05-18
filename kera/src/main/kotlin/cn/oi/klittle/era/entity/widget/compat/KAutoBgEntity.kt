package cn.oi.klittle.era.entity.widget.compat

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.https.bit.KBitmaps
import cn.oi.klittle.era.utils.*
import cn.oi.klittle.era.widget.compat.K1Widget
import java.lang.Exception
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.Deferred

/**
 * 自定义背景图片
 * @param view 组件
 * @param key 位图缓存的键值
 * @param isInSampleSize 加载位图的一个属性。省内存。目前只对本地图片使用，网络图片没有使用。
 * @param autoBg 背景图片
 * @param autoLeftPadding 左内补丁，控制位图的位置(负数也有效哦)；fixme 居中时也有效
 * @param autoTopPadding 上内补丁;fixme 居中时也有效。
 * @param isAutoCenter 位图是否居中,默认居中（水平+垂直居中），fixme 优先级最低,但是默认就是这个。
 * @param isAutoCenterHorizontal 水平居中，优先级次之
 * @param isAutoCenterVertical 垂直居中，优先级最高
 * @param autoRightPadding 与右边的距离，isAutoRight为true时才有效。
 * @param autoBottomPadding 与底部的距离，isAutoBottom为true时才有效。
 * @param isAutoRight 是否右对齐
 * @param isAutoBottom 是否底部对齐
 * @param isFill 位图是否填充整个组件
 * @param saturation 灰色度。0是灰色图像。1是正常的。即小于1都是偏灰色的。大于1都是偏亮的。
 * @param autoBgColor 位图颜色，可以将位图变成指定颜色值的位图。
 * @param bg_color 背景色，在位图的下面
 * @param bgHorizontalColors 背景水平渐变色
 * @param bgVerticalColors 背景垂直渐变色
 * @param fg_color 前景色，在位图的上面
 * @param fgHorizontalColors 前景水平渐变色
 * @param fgVerticalColors 前景垂直渐变色
 * @param isBgGradient 背景颜色是否渐变，默认是
 * @param isFgGradient 前景背景色是否渐变，默认是
 * @param rotation 位图旋转(正常沿Z轴旋转)角度（以位图中心为基准。）正数，顺时针旋转。
 * @param rotationX 位图沿X轴旋转，以位图中心为基准。正数(图片上半部分向内)
 * @param rotationY 位图沿Y轴旋转，以位图中心为基准。正数(图片右半部分向内)
 * @param alpha 位图透明度（0~255），0完全透明，255完全不透明。是位图的透明度。不是背景也不是前景。这里仅仅是位图的透明度。
 * @param width 位图宽度，如果小于等于0，就是位图原始的宽度。画位图的时候，宽度不一致。会对位图进行拉伸处理哦。
 * @param height 位图高度，如果小于等于0，就是位图原始的高度
 * @param isAutoWH 是否自动获取控件宽和高。默认是。
 * @param isDraw 是否绘制，true显示出来。默认显示出来。
 * @param duration 两个AutoBg之间的动画渐变时间（时间大于0才有动画效果。），主要用于实现两天图片大小之间的变化。fixme 只对第一张图片做了效果，第二张，第三张没有。也不需要。有一张就可以了。
 * @param resId 图片资源id
 * @param isRGB_565
 * @param assetsPath Assets目录下的图片路径
 * @param filePath 本地图片路径
 * @param url 网络图片地址
 * @param isRepeat fixme 是否允许重复加载（网络重复请求）;再次最好允许，防止加载相同图片的时候，加载不出。
 * @param isRecycle 压缩图片之后；是否对原图进行释放。默认true释放。
 * @param isCompress 是否根据宽和高对图片进行压缩处理，默认是true
 * @param maxAutoCount 最大自动请求次数；防止异常死循环卡死。
 * @param saveTime 网络位图本地缓存的时间，单位秒，默认七天（一周）（超过时间的位图会自动清理掉）。不要设置成无限，防止位图越来越多。
 * @param isCache 是否缓存网络位图；默认是
 * @param isGlide 是否使用第三方Glide库加载图片。效果杠杠，谷歌都推荐使用，所以默认就是true
 * @param isCenterCrop isGlide加载图片，是否居中裁剪，true图片大于指定宽高时会居中裁剪；fasle不会裁剪（图片能够完整显示，整体会按比例缩放）
 * @param headers Glide网络请求的头部参数
 * @param params 网络参数
 */
data class KAutoBgEntity(var view: View?,
                         var key: String? = null,
                         var isInSampleSize: Boolean = true,
                         var autoBg: Bitmap? = null,
                         var autoLeftPadding: Float = 0f, var autoTopPadding: Float = 0f,
                         var isAutoCenter: Boolean = true, var isAutoCenterHorizontal: Boolean = false, var isAutoCenterVertical: Boolean = false,
                         var autoRightPadding: Float = 0f, var autoBottomPadding: Float = 0f,
                         var isAutoRight: Boolean = false, var isAutoBottom: Boolean = false,
                         var isFill: Boolean = false, var saturation: Float = 1f, var autoBgColor: Int = Color.TRANSPARENT,
                         var bg_color: Int = Color.TRANSPARENT, var bgHorizontalColors: IntArray? = null, var bgVerticalColors: IntArray? = null,
                         var fg_color: Int = Color.TRANSPARENT, var fgHorizontalColors: IntArray? = null, var fgVerticalColors: IntArray? = null,
                         var isBgGradient: Boolean = true, var isFgGradient: Boolean = true,
                         var rotation: Float = 0F,
                         var rotationX: Float = 0F,
                         var rotationY: Float = 0F,
                         var alpha: Int = 255,
                         var width: Int = 0, var height: Int = 0,
                         var isAutoWH: Boolean = true,
                         var isDraw: Boolean = true, var duration: Long = 0,
                         var resId: Int? = null, var isRGB_565: Boolean = false,
                         var assetsPath: String? = null, var filePath: String? = null,
                         var url: String? = null, var isRepeat: Boolean = true,
                         var isRecycle: Boolean = true,
                         var isCompress: Boolean = true, var maxAutoCount: Int = 20, var saveTime: Int = KCacheUtils.TIME_WEEK,
                         var isCache: Boolean = true,
                         var isGlide: Boolean = true,
                         var isCenterCrop: Boolean = true,
                         var headers: MutableMap<String, String>? = null, var params: MutableMap<String, String>? = null) {

    private var autoCount = 0//记录自动请求次数。

    //位图的区域；会在画布中自动计算出来。
    var autoLeft: Float = 0f
    var autoTop: Float = 0f
    var autoRight: Float = 0f
    var autoBottom: Float = 0f

    //fixme 不要放在主构造函数里；防止事件被复制。事件不需要复制。
    //fixme 图片点击回掉事件;优先级比一般点击事件要高。如果执行图片点击事件；就不会再执行一般点击时候。点击事件只会触发一个。
    var onClickCallback: (() -> Unit)? = null

    fun onClickCallback(onClickCallback: (() -> Unit)? = null) {
        view?.let {
            if (it is K1Widget) {
                if (!it.hasClick) {
                    it.onClick { }//fixme 如果没有点击事件,则自动加上,图片点击事件依赖普通点击事件;
                }
            }
        }
        this.onClickCallback = onClickCallback
    }

    //fixme 判断点击区域是否在位图上
    fun isContains(event: MotionEvent?): Boolean {
        return isContains(event?.x, event?.y)
    }

    fun isContains(x: Float?, y: Float?): Boolean {
        if (x != null && y != null) {
            if (x >= autoLeft && x <= autoRight && y >= autoTop && y <= autoBottom) {
                return true//点击区域在该位图。
            }
        }
        return false
    }

    //fixme 自动获取控件的宽和高。
    private fun AutoWH() {
        if (isAutoWH) {
            view?.let {
                //fixme 宽和高小于等于0时，自动获取控件的宽和高。
                if (width <= 0 && it.width > 0) {
                    width = it.width
                }
                if (height <= 0 && it.height > 0) {
                    height = it.height
                }
                it?.layoutParams?.let {
                    if (width <= 0 && it.width > 0) {
                        width = it.width
                    }
                    if (height <= 0 && it.height > 0) {
                        height = it.height
                    }
                }

            }
        }
    }

    private var autoBgTime = 0L
    //fixme 根据参数自己主动获取图片（这个方法一般都是在画布里面自动执行的。）
    fun autoBg() {
        if (isDraw && autoCount <= maxAutoCount && (autoBg == null || autoBg!!.isRecycled)) {
            if (System.currentTimeMillis() - autoBgTime < 100) {
                return//防止时间过短重复执行。
            }
            autoBgTime = System.currentTimeMillis()
            if (resId != null && resId != 0) {
                //autoCount++//res不计数，防止图片为空。网络才计数。
                if (isGlide) {
                    autoBg(resId, isRGB_565)
                } else {
                    GlobalScope.async {
                        autoBg(resId, isRGB_565)
                        view?.postInvalidate()//fixme 必不可少，防止刷新失败；亲测有效。
                        //KLoggerUtils.e("刷新了")
                    }
                }
            } else if (assetsPath != null) {
                //autoCount++
                if (isGlide) {
                    autoBgFromAssets(assetsPath, isRGB_565)
                } else {
                    GlobalScope.async {
                        autoBgFromAssets(assetsPath, isRGB_565)
                        view?.postInvalidate()
                    }
                }
            } else if (filePath != null) {
                //autoCount++
                if (isGlide) {
                    autoBgFromFile(filePath, isRGB_565)
                } else {
                    GlobalScope.async {
                        autoBgFromFile(filePath, isRGB_565)
                        view?.postInvalidate()
                    }
                }
            } else if (url != null) {
                autoCount++//fixme 网络才计数（在autoBgFromUrl()方法，图片加载成功后，会自动恢复autoCount=0）
                if (isGlide) {
                    autoBgFromUrl(url, false, isRepeat, isRGB_565)
                } else {
                    GlobalScope.async {
                        //fixme 加载网络图片
                        autoBgFromUrl(url, false, isRepeat, isRGB_565) {
                            view?.postInvalidate()
                        }
                    }
                }
            }
        }
    }

    /**
     * fixme 注意，调用之前，一定要先设置宽度和高度。最好先设置width和height;
     */
    var isAutoBging = false//fixme true 正在执行，false 执行完毕;私有属性，防止重复执行。
    var job_res: Deferred<Any?>? = null//job?.cancel()可以取消当前协程的任务哦。
    fun autoBg(resId: Int?, isRGB_565: Boolean = this.isRGB_565,callback:((bitmap:Bitmap)->Unit)?=null): Deferred<Any?>? {
        if (this.resId != resId || job_res == null || !job_res!!.isActive || job_res!!.isCancelled) {
            if (autoBg != null && !autoBg!!.isRecycled && this.resId == resId) {
                isAutoBging = false
                return job_res
            }
            if (resId != null && !isAutoBging && isDraw) {
                isAutoBging = true
                this.resId = resId
                this.isRGB_565 = isRGB_565
                //fixme 读取本地Res目录下的图片，最好不要使用Glide(不支持同步)；异步问题很多。
                //fixme 这里就不要开协程了，加载会变慢。
                try {
                    AutoWH()//fixme 自动获取宽和高。
                    if (isGlide) {
                        try {
                            job_res?.cancel()//fixme 取消上一次
                        }catch (e:Exception){e.printStackTrace()}
                        key = KGlideUtils.getKeyForRes(resId, width, height)
                        //fixme 异步新开协程
                        job_res = KGlideUtils.getBitmapFromResouce(resId, width, height, isCenterCrop = isCenterCrop) { key, bitmap ->
                            this@KAutoBgEntity.key = key
                            autoBg = bitmap
                            isAutoBging = false
                            try {
                                job_res?.cancel()//fixme 取消上一次
                            }catch (e:Exception){e.printStackTrace()}
                            job_res = null
                            callback?.let {
                                if (autoBg!=null&&!autoBg!!.isRecycled){
                                    it(autoBg!!)
                                }
                            }
                            requestLayout()//fixme 会跳转主线程主动刷新。防止回调的后面。最后执行。
                        }
                    } else {
                        //fixme 同步
                        if (isInSampleSize) {
                            autoBg = KAssetsUtils.getInstance().getBitmapFromResource(resId!!, isRGB_565, width, height)
                            key = KAssetsUtils.getInstance().getKeyForRes(resId, isRGB_565, width, height)
                        } else {
                            autoBg = KAssetsUtils.getInstance().getBitmapFromResource(resId!!, isRGB_565)
                            key = KAssetsUtils.getInstance().getKeyForRes(resId, isRGB_565)
                        }
                        //fixme 自动适配，将位图的宽度和高度，压缩成指定宽度（width）和高度(height),如果为0，就原图的尺寸。
                        autoBg?.let {
                            if (!it.isRecycled && (it.width != width || it.height != height) && isCompress) {
                                var p = it.width.toFloat() / width.toFloat()
                                if (p > 1.3 || p < 0.7) {
                                    kpx.keyBitmap(resId!!, it, width, height, isCompress, isRecycle) {
                                        autoBg = it
                                        if (autoBg != null && !autoBg!!.isRecycled) {
                                            if (isInSampleSize) {
                                                key = KAssetsUtils.getInstance().getKeyForRes(resId, isRGB_565, width, height)
                                            } else {
                                                key = KAssetsUtils.getInstance().getKeyForRes(resId, isRGB_565)
                                            }
                                            KAssetsUtils.getInstance().setCacleBiatmap(key, autoBg)//fixme 重新缓存位图(因为压缩后的位图和原有的位图已经不是同一个对象了。)
                                        }
                                    }
                                }
                            }
                            callback?.let {
                                if (autoBg!=null&&!autoBg!!.isRecycled){
                                    it(autoBg!!)
                                }
                            }
                            requestLayout()//fixme 会跳转主线程主动刷新。
                        }
                        isAutoBging = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                autoBg = null
                isAutoBging = false
            }
        }
        isAutoBging = false
        return job_res
    }

    var job_assets: Deferred<Any?>? = null//job?.cancel()可以取消当前协程的任务哦。
    fun autoBgFromAssets(assetsPath: String?, isRGB_565: Boolean = this.isRGB_565,callback:((bitmap:Bitmap)->Unit)?=null): Deferred<Any?>? {
        if (this.assetsPath != assetsPath || job_assets == null || !job_assets!!.isActive || job_assets!!.isCancelled) {
            if (assetsPath != null && !isAutoBging && isDraw) {
                if (autoBg != null && !autoBg!!.isRecycled && this.assetsPath == assetsPath) {
                    isAutoBging = false
                    return job_assets
                }
                isAutoBging = true
                this.assetsPath = assetsPath
                this.isRGB_565 = isRGB_565
                AutoWH()//fixme 自动获取宽和高
                try {
                    if (isGlide) {
                        job_assets?.cancel()//fixme 取消上一次的
                        key = KGlideUtils.getKeyForPath(assetsPath, width, height)
                        //新开协程
                        job_assets = KGlideUtils.getBitmapFromAssets(assetsPath, width, height, isCenterCrop = isCenterCrop) { key, bitmap ->
                            this@KAutoBgEntity.key = key
                            autoBg = bitmap
                            isAutoBging = false
                            job_assets?.cancel()
                            job_assets = null
                            callback?.let {
                                if (autoBg!=null&&!autoBg!!.isRecycled){
                                    it(autoBg!!)
                                }
                            }
                            requestLayout()
                        }
                    } else {
                        if (isInSampleSize) {
                            autoBg = KAssetsUtils.getInstance().getBitmapFromAssets(assetsPath, isRGB_565, width, height)
                            key = KAssetsUtils.getInstance().getKeyForAssets(assetsPath, isRGB_565, width, height)
                        } else {
                            autoBg = KAssetsUtils.getInstance().getBitmapFromAssets(assetsPath, isRGB_565)
                            key = KAssetsUtils.getInstance().getKeyForAssets(assetsPath, isRGB_565)
                        }
                        autoBg?.let {
                            if (!it.isRecycled && (it.width != width || it.height != height) && isCompress) {
                                //autoBg = kpx.xBitmap(it, width, height, isRecycle = isRecycle)//自动适配
                                var p = it.width.toFloat() / width.toFloat()
                                if (p > 1.3 || p < 0.7) {
                                    kpx.keyBitmap(assetsPath!!, it, width, height, isCompress, isRecycle) {
                                        autoBg = it
                                        if (autoBg != null && !autoBg!!.isRecycled) {
                                            if (isInSampleSize) {
                                                key = KAssetsUtils.getInstance().getKeyForAssets(assetsPath, isRGB_565, width, height)
                                            } else {
                                                key = KAssetsUtils.getInstance().getKeyForAssets(assetsPath, isRGB_565)
                                            }
                                            KAssetsUtils.getInstance().setCacleBiatmap(key, autoBg)//fixme 重新缓存位图(因为压缩后的位图和原有的位图已经不是同一个对象了。)
                                        }
                                    }
                                }
                            }
                            callback?.let {
                                if (autoBg!=null&&!autoBg!!.isRecycled){
                                    it(autoBg!!)
                                }
                            }
                            requestLayout()
                        }
                        isAutoBging = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                autoBg = null
            }
        }
        isAutoBging = false
        return job_assets
    }

    var job_file: Deferred<Any?>? = null//job?.cancel()可以取消当前协程的任务哦。
    //fixme 来自sd存储卡
    fun autoBgFromFile(filePath: String?, isRGB_565: Boolean = this.isRGB_565,callback:((bitmap:Bitmap)->Unit)?=null): Deferred<Any?>? {
        if (this.filePath != filePath || job_file == null || !job_file!!.isActive || job_file!!.isCancelled) {
            if (autoBg != null && !autoBg!!.isRecycled && this.filePath == filePath) {
                isAutoBging = false
                return job_file
            }
            if (filePath != null && !isAutoBging && isDraw) {
                isAutoBging = true
                this.filePath = filePath
                this.isRGB_565 = isRGB_565
                AutoWH()//fixme 自动获取宽和高
                try {
                    if (isGlide) {
                        job_file?.cancel()//fixme 取消上一次的
                        key = KGlideUtils.getKeyForPath(filePath, width, height)
                        //新开协程
                        job_file = KGlideUtils.getBitmapFromPath(filePath, width, height, isCenterCrop = isCenterCrop) { key, bitmap ->
                            autoBg = bitmap
                            this@KAutoBgEntity.key = key
                            isAutoBging = false
                            job_file?.cancel()
                            job_file = null
                            callback?.let {
                                if (autoBg!=null&&!autoBg!!.isRecycled){
                                    it(autoBg!!)
                                }
                            }
                            requestLayout()
                        }
                    } else {
                        //当前线程
                        if (isInSampleSize) {
                            autoBg = KAssetsUtils.getInstance().getBitmapFromFile(filePath, isRGB_565, width, height)
                            key = KAssetsUtils.getInstance().getKeyForPath(filePath, isRGB_565, width, height)
                        } else {
                            autoBg = KAssetsUtils.getInstance().getBitmapFromFile(filePath, isRGB_565)
                            key = KAssetsUtils.getInstance().getKeyForPath(filePath, isRGB_565)
                        }
                        autoBg?.let {
                            if (!it.isRecycled && (it.width != width || it.height != height) && isCompress) {
                                //autoBg = kpx.xBitmap(it, width, height, isRecycle = isRecycle)//自动适配
                                var p = it.width.toFloat() / width.toFloat()
                                //Log.e("test","width:\t"+width+"\tbitmap:\t"+it.width+"\tp:\t"+p)
                                if (p > 1.3 || p < 0.7) {
                                    //差距不是很大，就不需要重新压缩了。
                                    kpx.keyBitmap(filePath!!, it, width, height, isCompress, isRecycle) {
                                        autoBg = it
                                        if (autoBg != null && !autoBg!!.isRecycled) {
                                            if (isInSampleSize) {
                                                key = KAssetsUtils.getInstance().getKeyForPath(filePath, isRGB_565, width, height)
                                            } else {
                                                key = KAssetsUtils.getInstance().getKeyForPath(filePath, isRGB_565)
                                            }
                                            KAssetsUtils.getInstance().setCacleBiatmap(key, autoBg)//重新缓存位图
                                        }
                                    }
                                }
                            }
                            callback?.let {
                                if (autoBg!=null&&!autoBg!!.isRecycled){
                                    it(autoBg!!)
                                }
                            }
                            requestLayout()
                        }
                        isAutoBging = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                isAutoBging = false
            } else {
                autoBg = null
            }
        }
        isAutoBging = false
        return job_file
    }


    //添加头部参数
    fun addHeader(key: String, value: String): KAutoBgEntity {
        if (headers == null) {
            headers = mutableMapOf()
        }
        headers?.put(key, value)
        return this
    }

    //添加头部参数(融合两个Map)
    fun addHeader(header: MutableMap<String, String>? = null): KAutoBgEntity {
        if (headers == null) {
            headers = mutableMapOf()
        }
        header?.let {
            for ((key, value) in header.entries) {
                headers?.put(key, value)
            }
        }
        return this
    }

    fun addParam(key: String, value: String): KAutoBgEntity {
        if (params == null) {
            params = mutableMapOf()
        }
        params?.put(key, value)
        return this
    }

    fun addParam(param: MutableMap<String, String>? = null): KAutoBgEntity {
        if (params == null) {
            params = mutableMapOf()
        }
        param?.let {
            for ((key, value) in param.entries) {
                params?.put(key, value)
            }
        }
        return this
    }

    var job_url: Deferred<Any?>? = null//job?.cancel()可以取消当前协程的任务哦。
    /**
     * url 网络图片地址
     * isLoad 是否显示进度条，默认不显示
     * isRepeat fixme 是否允许重复加载（网络重复请求）;再次最好允许，防止加载相同图片的时候，加载不出。
     * fixme width,height位图的宽和高(最好手动设置一下，或者延迟一下，不能无法获取宽和高，或者先设置lparam(常态)也可以)
     * @param finish 位图回调，fixme 最好等回调完成之后，再配置autoBg_press{}其他状态。防止位图为空。
     */
    fun autoBgFromUrl(url: String?, isLoad: Boolean = false, isRepeat: Boolean = true, isRGB_565: Boolean = false, finish: ((bitmap: Bitmap) -> Unit)? = null): Deferred<Any?>? {
        if (url != null && !isAutoBging && isDraw) {
            if (autoBg != null && !autoBg!!.isRecycled && this.url == url) {
                isAutoBging = false
                return job_url
            }
            //KLoggerUtils.e("网络图片加载中。。。")
            isAutoBging = true
            if (!isGlide) {
                this.url = url
            }
            this.isRGB_565 = isRGB_565
            this.isRepeat = isRepeat//fixme 是否允许网络重复加载。
            try {
                AutoWH()//fixme 自动获取宽和高。
                if (isGlide) {
                    if (this.url != url || job_url == null || !job_url!!.isActive || job_url!!.isCancelled) {
                        this.url = url
                        job_url?.cancel()//fixme 取消上一次的
                        key = KGlideUtils.getKeyForUrl(url, width, height)
                        //新开协程
                        job_url = KGlideUtils.getBitmapFromUrl(url, width, height, headers, params, isCenterCrop = isCenterCrop) { key, bitmap ->
                            autoBg = bitmap
                            autoBg?.let {
                                if (!it.isRecycled) {
                                    autoCount = 0//fixme 图片获取成功，计数恢复0。
                                    urlCount = 0
                                    requestLayout()
                                }
                            }
                            this@KAutoBgEntity.key = key
                            isAutoBging = false
                            job_url?.cancel()
                            job_url = null
                        }
                    }
                    return job_url//job?.cancel()//可以取消当前任务
                } else {
                    var activity: Activity? = null
                    view?.apply {
                        if (context != null && context is Activity) {
                            activity = context as Activity
                        }
                    }
                    if (isLoad && activity != null && !activity!!.isFinishing && isDraw) {
                        var bw = width
                        var bh = height
                        if (!isCompress) {
                            //不压缩，返回服务器原图
                            bw = 0
                            bh = 0
                        }
                        //fixme 最好还是传入控件的宽和高；防止异常。有利于安全。宽和高不同返回不同的位图。（引用不同的位图会安全很多。），都引用服务器原图，引用同一个对象很容易报错(位图占用错误)。
                        //fixme 传入宽和高，返回对应尺寸的位图；下面的压缩方法，如果宽和高一致，不会重复压缩的。
                        KBitmaps(url).isOptionsRGB_565(isRGB_565).isCacle(isCache).saveTime(saveTime).isShowLoad(true).isRecycle(isRecycle).isCompress(isCompress).activity(activity).isRepeatRequest(isRepeat).width(bw).height(bh).get(finish = {
                            isAutoBging = false//fixme 网络请求结束
                        }) {
                            autoBg = it
                            //fixme 返回的位图本身就是经过压缩的，防止异常；不需要重复压缩。
                            //fixme 参数四 isRecycle true 图片压缩后，会释放原图。
                            //autoBg = kpx.xBitmap(it, width, height, true)//fixme 自动适配,以1334x750为基准进行适配;基本上会生成一个新的位图。
                            //fixme 网络图片就不要重新缓存了。保存的始终是服务器原始图片。
                            //fixme 设置宽度和高度
                            if (autoBg != null && !autoBg!!.isRecycled) {
                                if (width <= 0) {
                                    width = autoBg!!.width
                                }
                                if (height <= 0) {
                                    height = autoBg!!.height
                                }
                            }
                            if (activity != null && !activity!!.isFinishing && autoBg != null && !autoBg!!.isRecycled) {
                                activity?.runOnUiThread {
                                    autoCount = 0//fixme 图片获取成功，计数恢复0。
                                    urlCount = 0
                                    view?.invalidate()//防止requestLayout()无效。
                                    view?.requestLayout()//主线程重新布局
                                    finish?.let {
                                        autoBg?.apply {
                                            if (!isRecycled) {
                                                it(this)
                                            }
                                        }
                                    }
                                    activity = null
                                }
                            }
                            if ((autoBg == null || autoBg!!.isRecycled) && isDraw && this.url != null && urlCount < maxUrlCount) {
                                view?.let {
                                    if (it.visibility == View.VISIBLE) {
                                        urlCount++//fixme 以防万一；防止异常死循环。
                                        view?.postInvalidateDelayed(postInvalidateDelayed)//fixme 自动延迟刷新(单位毫秒),防止网络图片加载失败。
                                    }
                                }
                            }
                            isAutoBging = false//fixme 网络请求接收
                        }
                    } else {
                        var bw = width
                        var bh = height
                        if (!isCompress) {
                            //不压缩，返回服务器原图
                            bw = 0
                            bh = 0
                        }
                        KBitmaps(url).isOptionsRGB_565(isRGB_565).isCacle(isCache).saveTime(saveTime).isShowLoad(false).isRecycle(isRecycle).isCompress(isCompress).isRepeatRequest(isRepeat).width(bw).height(bh).get(finish = {
                            isAutoBging = false//fixme 网络请求结束
                        }) {
                            autoBg = it
                            //autoBg = kpx.xBitmap(it, width, height, true)//自动适配,以1334x750为基准进行适配
                            //fixme 设置宽度和高度
                            if (autoBg != null && !autoBg!!.isRecycled) {
                                if (width <= 0) {
                                    width = autoBg!!.width
                                }
                                if (height <= 0) {
                                    height = autoBg!!.height
                                }
                            }
                            if (activity != null && !activity!!.isFinishing && autoBg != null && !autoBg!!.isRecycled) {
                                //跳转到主线程
                                activity?.runOnUiThread {
                                    autoCount = 0//fixme 图片获取成功，计数恢复0。
                                    urlCount = 0
                                    view?.invalidate()//防止requestLayout()无效。
                                    view?.requestLayout()//主线程重新布局
                                    finish?.let {
                                        autoBg?.apply {
                                            if (!isRecycled) {
                                                it(this)
                                            }
                                        }
                                    }
                                    activity = null
                                }
                            } else if (autoBg != null && !autoBg!!.isRecycled) {
                                autoCount = 0//fixme 图片获取成功，计数恢复0。
                                urlCount = 0
                                requestLayout()
                                //次线程
                                finish?.let {
                                    autoBg?.apply {
                                        if (!isRecycled) {
                                            it(this)
                                        }
                                    }
                                }
                            }
                            if ((autoBg == null || autoBg!!.isRecycled) && isDraw && this.url != null && urlCount < maxUrlCount) {
                                view?.let {
                                    if (it.visibility == View.VISIBLE) {
                                        urlCount++//fixme 以防万一；防止异常死循环。
                                        view?.let {
                                            if (it is K1Widget) {
                                                if (!it.isOnDestroy) {
                                                    view?.postInvalidateDelayed(postInvalidateDelayed)//fixme 自动延迟刷新(单位毫秒),防止网络图片加载失败。
                                                } else {
                                                }
                                            } else {
                                                view?.postInvalidateDelayed(postInvalidateDelayed)//fixme 自动延迟刷新(单位毫秒),防止网络图片加载失败。
                                            }
                                        }
                                    }
                                }
                            }
                            isAutoBging = false//fixme 网络请求接收
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                isAutoBging = false//fixme 网络请求结束
            }
        }
        return null
    }

    private var urlCount: Int = 0//fixme 以防万一；防止异常死循环刷新。记录当前网络请求次数
    private var maxUrlCount = 10
    private var postInvalidateDelayed = 200L

    //fixme 重新布局
    open fun requestLayout() {
        //fixme 设置宽度和高度
        if (autoBg != null && !autoBg!!.isRecycled) {
            if (width <= 0) {
                width = autoBg!!.width
            }
            if (height <= 0) {
                height = autoBg!!.height
            }
        }
        view?.apply {
            if (context != null && context is Activity) {
                var activity: Activity? = context as Activity
                if (activity != null && !activity!!.isFinishing && autoBg != null && !autoBg!!.isRecycled) {
                    activity?.runOnUiThread {
                        view?.invalidate()//防止requestLayout()无效。
                        view?.requestLayout()//主线程重新布局
                        activity = null
                    }
                }
            }
        }
        view?.postInvalidate()//fixme 防止不刷新，亲测有效。
    }

    //背景色
    open fun bgHorizontalColors(vararg color: Int) {
        bgHorizontalColors = color
    }

    open fun bgHorizontalColors(vararg color: String) {
        bgHorizontalColors = IntArray(color.size)
        bgHorizontalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }

    open fun bgVerticalColors(vararg color: Int) {
        bgVerticalColors = color
    }

    open fun bgVerticalColors(vararg color: String) {
        bgVerticalColors = IntArray(color.size)
        bgVerticalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }
    }

    //前景色
    open fun fgHorizontalColors(vararg color: Int) {
        fgHorizontalColors = color
    }

    open fun fgHorizontalColors(vararg color: String) {
        fgHorizontalColors = IntArray(color.size)
        fgHorizontalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }

    open fun fgVerticalColors(vararg color: Int) {
        fgVerticalColors = color
    }

    open fun fgVerticalColors(vararg color: String) {
        fgVerticalColors = IntArray(color.size)
        fgVerticalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }
    }


}
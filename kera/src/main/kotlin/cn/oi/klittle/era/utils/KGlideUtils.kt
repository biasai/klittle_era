package cn.oi.klittle.era.utils

import android.graphics.Bitmap
import android.util.Log
import android.widget.ImageView
import cn.oi.klittle.era.base.KBaseApplication
import cn.oi.klittle.era.https.KHttp
import cn.oi.klittle.era.https.ko.KHttps
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.Deferred

/**
 * 使用第三方Glide加载图片；调用以下方法时必须在子线程中调用，不能在UI主线程中调用。
 * fixme Glide已经解决了本地图片旋转方向不正确的问题，图片也不会变形，默认都是居中显示。
 * fixme 以及加载速度和效果都杠杠的。谷歌推荐使用。
 */
object KGlideUtils {

    fun getInSampleSize(sizeMultiplier: Float): Int {
        if (sizeMultiplier >= 1f || sizeMultiplier <= 0) {
            return 1
        }
        return 100 / (sizeMultiplier * 100).toInt()
    }

    //适用于Path和Assets
    fun getKeyForPath(path: String, overrideWidth: Int, overrideHeight: Int, sizeMultiplier: Float = 1F): String {
        if (overrideWidth > 0 && overrideHeight > 0) {
            return KAssetsUtils.getInstance().getKeyForPath(path, overrideWidth, overrideHeight)
        }
        return KAssetsUtils.getInstance().getKeyForPath(path, getInSampleSize(sizeMultiplier))
    }

    fun getKeyForUrl(url: String, overrideWidth: Int, overrideHeight: Int): String {
        return KAssetsUtils.getInstance().getKeyForUrl(url, overrideWidth, overrideHeight)
    }

    //适用于Resource
    fun getKeyForRes(resID: Int, overrideWidth: Int, overrideHeight: Int, sizeMultiplier: Float = 1F): String {
        if (overrideWidth > 0 && overrideHeight > 0) {
            return KAssetsUtils.getInstance().getKeyForRes(resID, overrideWidth, overrideHeight)
        }
        return KAssetsUtils.getInstance().getKeyForRes(resID, getInSampleSize(sizeMultiplier))
    }

    /**
     * 缓存位图到内存
     */
    fun setCacheBiatmapOfMemery(key: String, bitmap: Bitmap) {
        KAssetsUtils.getInstance().setCacleBiatmap(key, bitmap)
    }

    //获取内存缓存位图
    fun getCacheBitmapOfMemery(key: String): Bitmap? {
        KAssetsUtils.getInstance().getCacleBitmap(key)?.let {
            if (!it.isRecycled) {
                return it
            }
        }
        return null
    }

    //释放内存位图
    fun recycleCacheBitmapOfMemery(key: String) {
        KAssetsUtils.getInstance().recycleBitmap(key)
    }

    /**
     * 缓存字节流到本地
     */
    fun setCacheByteArrayOfStrategy(key: String, byteArray: ByteArray) {
        KCacheUtils.getCache().put(key, byteArray, KCacheUtils.TIME_WEEK * 2)//缓存两周
    }


    fun getCacheByteArrayOfStrategy(key: String): ByteArray? {
        return KCacheUtils.getCache().getAsBinary(key)
    }

    private var keyMap = mutableMapOf<String, Int>()//判断是否重新执行。
    //fixme 判断是否重新执行。防止卡死。亲测有效！（基本解决了防重复和卡死的现象）;基本能够百分百解决并发问题，效果杠杠的。
    private var keyMap2 = mutableMapOf<String, Deferred<Any?>?>()
    private var minCount = 5
    private var maxCount = 100
    private var delayTime = 50L//单位毫秒(Glide读取本地一张1M多的大图，大约200毫秒。)
    private fun remove(key: String?) {
        try {
            key?.let {
                if (it.length > 0) {
                    var key = it
                    keyMap?.let {
                        if (it.containsKey(key)) {
                            try {
                                it.remove(key)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                KLoggerUtils.e("kGlideUtils remove移除键值异常1：\t" + e.message)
                            }
                        }
                    }
                    keyMap2?.let {
                        if (it.containsKey(key)) {
                            try {
                                it.remove(key)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                KLoggerUtils.e("kGlideUtils remove移除键值异常2：\t" + e.message)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            KLoggerUtils.e("kGlideUtils remove移除键值异常：\t" + e.message)
        }
    }

    //job?.cancel()可以取消当前协程，释放资源哦。
    fun getBitmapFromPath(path: String?, overrideWidth: Int, overrideHeight: Int, sizeMultiplier: Float = 1F, isCenterCrop: Boolean = false, callBack: (key: String, bitmap: Bitmap) -> Unit): Deferred<Any?>? {
        try {
            if (KStringUtils.isEmpty(path)) {
                return null
            }
            var key = getKeyForPath(path!!, overrideWidth, overrideHeight, sizeMultiplier)
            var isRuning = keyMap.containsKey(key)//fixme 判断，在协程外判断实时准确。
            if (!isRuning) {
                keyMap.put(key, 1)//fixme 正在操作
            }
            var job: Deferred<Any?>? = null
            var bitmap: Bitmap? = getCacheBitmapOfMemery(key)//读取缓存位图
            if (bitmap != null && !bitmap.isRecycled) {
                remove(key)//fixme 移除
                callBack?.let {
                    it(key, bitmap)
                }
            } else {
                job = GlobalScope.async {
                    try {
                        if (!isRuning) {//fixme 防止重复加载
                            var bitmap = KGlideUtils.getBitmapFromPath(path, overrideWidth, overrideHeight, sizeMultiplier, isCenterCrop)
                            remove(key)//fixme 移除
                            if (bitmap != null && !bitmap.isRecycled) {
                                callBack?.let {
                                    it(key, bitmap)
                                }
                            } else {
                            }
                        } else {
                            keyMap.get(key)?.let {
                                var value = it + 1
                                keyMap.put(key, value)
                                //KLoggerUtils.e("等待:\t"+value)
                                if (value >= minCount) {
                                    if (value >= maxCount) {
                                        remove(key)//fixme 移除(防止万一死循环)
                                        return@async
                                    } else {
                                        keyMap2.get(key)?.let {
                                            if (!it.isActive || it.isCancelled) {
                                                //没有活动或者已经取消。
                                                remove(key)//fixme 移除
                                            }
                                        }
                                    }
                                }
                            }
                            delay(delayTime)
                            getBitmapFromPath(path, overrideWidth, overrideHeight, sizeMultiplier, isCenterCrop, callBack)//fixme 延迟闭合重新调用。
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
                if (!keyMap2.containsKey(key)) {
                    keyMap2.put(key, job)//保存当前协程，判断该协程释放完成
                }
            }
            return job//返回键值
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * @param path 本地图片路径(获取网络图片url也可以。能够加载网络图片，亲测可行)
     * @param overrideWidth 图片宽度
     * @param overrideHeight 图片高度
     * @param sizeMultiplier 图片压缩比例（0~1）
     * @param isCenterCrop 是否居中裁剪，true图片大于指定宽高时会居中裁剪；fasle不会裁剪（图片能够完整显示，整体会按比例缩放）
     */
    fun getBitmapFromPath(path: String?, overrideWidth: Int, overrideHeight: Int, sizeMultiplier: Float = 1F, isCenterCrop: Boolean = false): Bitmap? {
        try {
            if (KStringUtils.isEmpty(path)) {
                return null
            }
            var bitmap: Bitmap? = getCacheBitmapOfMemery(getKeyForPath(path!!, overrideWidth, overrideHeight, sizeMultiplier))
            if (bitmap != null && !bitmap.isRecycled) {
                return bitmap
            }
            val options = RequestOptions()
            if (overrideWidth <= 0 && overrideHeight <= 0) {
                options.sizeMultiplier(sizeMultiplier)//0~1之间;1是原图大小。
            } else {
                options.override(overrideWidth, overrideHeight)//限制图片的大小
            }
            options.skipMemoryCache(true)// 不使用内存缓存
            options.diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
            //options.diskCacheStrategy(DiskCacheStrategy.ALL);
            if (isCenterCrop) {
                options.centerCrop()//居中剪切
            }
            //options.placeholder(R.drawable.image_placeholder)
            //fixme 必须放在子线程中执行;这里就不新开线程了，交给调用者去开。
            bitmap = Glide.with(KBaseApplication.getInstance())
                    .asBitmap()
                    .load(path)
                    .apply(options)
                    .submit().get()
            //fixme Glide内部已经解决了图片方向不正确的问题了。
            //解决图片方向显示不正确的问题。(图片模糊和这行没有关系，亲测。)
            //bitmap = KPictureUtils.INSTANCE.rotateBitmap(bitmap, path);
            //保存当前Bitmap
            if (bitmap != null && !bitmap.isRecycled) {
                setCacheBiatmapOfMemery(getKeyForPath(path, overrideWidth, overrideHeight, sizeMultiplier), bitmap)//fixme 缓存
                return bitmap
            }
        } catch (e: Exception) {
            Log.e("test", "Glide File流异常" + e.message)
        }
        return null
    }

    //job?.cancel()可以取消当前协程，释放资源哦。
    fun getBitmapFromAssets(path: String?, overrideWidth: Int, overrideHeight: Int, sizeMultiplier: Float = 1F, isCenterCrop: Boolean = false, callBack: (key: String, bitmap: Bitmap) -> Unit): Deferred<Any?>? {
        try {
            if (KStringUtils.isEmpty(path)) {
                return null
            }
            var key = getKeyForPath(path!!, overrideWidth, overrideHeight, sizeMultiplier)
            var isRuning = keyMap.containsKey(key)//fixme 判断，在协程外判断实时准确。
            if (!isRuning) {
                keyMap.put(key, 1)//fixme 正在操作
            }
            var job: Deferred<Any?>? = null
            var bitmap: Bitmap? = getCacheBitmapOfMemery(key)//读取缓存位图
            if (bitmap != null && !bitmap.isRecycled) {
                remove(key)//fixme 移除
                //KLoggerUtils.e("缓存")
                callBack?.let {
                    it(key, bitmap)
                }
            } else {
                job = GlobalScope.async {
                    try {
                        if (!isRuning) {//fixme 防止重复加载
                            //KLoggerUtils.e("新获取")
                            var bitmap = KGlideUtils.getBitmapFromAssets(path, overrideWidth, overrideHeight, sizeMultiplier, isCenterCrop)
                            remove(key)//fixme 移除
                            if (bitmap != null && !bitmap.isRecycled) {
                                callBack?.let {
                                    it(key, bitmap)
                                }
                            } else {
                            }
                        } else {
                            keyMap.get(key)?.let {
                                var value = it + 1
                                //KLoggerUtils.e("等待：\t" + value)
                                keyMap.put(key, value)
                                if (value >= minCount) {
                                    if (value >= maxCount) {
                                        remove(key)//fixme 移除(防止万一死循环)
                                        return@async
                                    } else {
                                        keyMap2.get(key)?.let {
                                            if (!it.isActive || it.isCancelled) {
                                                //没有活动或者已经取消。
                                                remove(key)//fixme 移除
                                            }
                                        }
                                    }
                                }
                            }
                            delay(delayTime)
                            getBitmapFromAssets(path, overrideWidth, overrideHeight, sizeMultiplier, isCenterCrop, callBack)//fixme 延迟闭合重新调用。
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (!keyMap2.containsKey(key)) {
                    keyMap2.put(key, job)//保存当前协程，判断该协程释放完成
                }
            }
            return job//返回键值
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }


    /**
     * @param path Assets目录下的图片路径;如："html/p.jpg"
     * @param overrideWidth 图片宽度
     * @param overrideHeight 图片高度
     * @param sizeMultiplier 图片压缩比例（0~1）
     * @param isCenterCrop 是否居中裁剪，true图片大于指定宽高时会居中裁剪；fasle不会裁剪（图片能够完整显示，整体会按比例缩放）
     */
    fun getBitmapFromAssets(path: String?, overrideWidth: Int, overrideHeight: Int, sizeMultiplier: Float = 1F, isCenterCrop: Boolean = false): Bitmap? {
        try {
            if (KStringUtils.isEmpty(path)) {
                return null
            }
            var bitmap: Bitmap? = getCacheBitmapOfMemery(getKeyForPath(path!!, overrideWidth, overrideHeight, sizeMultiplier))
            if (bitmap != null && !bitmap.isRecycled) {
                return bitmap
            }
            val options = RequestOptions()
            if (overrideWidth <= 0 && overrideHeight <= 0) {
                options.sizeMultiplier(sizeMultiplier)//0~1之间;1是原图大小。
            } else {
                options.override(overrideWidth, overrideHeight)//限制图片的大小
            }
            options.skipMemoryCache(true)// 不使用内存缓存
            options.diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
            //options.diskCacheStrategy(DiskCacheStrategy.ALL);
            if (isCenterCrop) {
                options.centerCrop()//居中剪切
            }
            //options.placeholder(R.drawable.image_placeholder)
            //fixme 必须放在子线程中执行;这里就不新开线程了，交给调用者去开。
            bitmap = Glide.with(KBaseApplication.getInstance())
                    .asBitmap()
                    .load("file:///android_asset/" + path)
                    .apply(options)
                    .submit().get()
            //fixme Glide内部已经解决了图片方向不正确的问题了。
            //解决图片方向显示不正确的问题。(图片模糊和这行没有关系，亲测。)
            //bitmap = KPictureUtils.INSTANCE.rotateBitmap(bitmap, path);
            //保存当前Bitmap
            if (bitmap != null && !bitmap.isRecycled) {
                setCacheBiatmapOfMemery(getKeyForPath(path, overrideWidth, overrideHeight, sizeMultiplier), bitmap)//fixme 缓存
                return bitmap
            }
        } catch (e: Exception) {
            Log.e("test", "Glide Assets流异常" + e.message)
        }
        return null
    }

    //job?.cancel()可以取消当前协程，释放资源哦。
    //fixme 子线程，主线程都可以调用。加了了回调方法。
    fun getBitmapFromResouce(resID: Int?, overrideWidth: Int, overrideHeight: Int, sizeMultiplier: Float = 1F, isCenterCrop: Boolean = false, callBack: (key: String, bitmap: Bitmap) -> Unit): Deferred<Any?>? {
        try {
            if (resID == null) {
                return null
            }
            var key = getKeyForRes(resID, overrideWidth, overrideHeight, sizeMultiplier)
            var isRuning = keyMap.containsKey(key)//fixme 判断，在协程外判断实时准确。
            if (!isRuning) {
                keyMap.put(key, 1)//fixme 正在操作
            }
            var job: Deferred<Any?>? = null
            var bitmap: Bitmap? = getCacheBitmapOfMemery(key)//读取缓存位图
            if (bitmap != null && !bitmap.isRecycled) {
                remove(key)//fixme 移除
//            KLoggerUtils.e("缓存")
                callBack?.let {
                    it(key, bitmap)
                }
            } else {
                job = GlobalScope.async {
                    try {
                        if (!isRuning) {//fixme 防止重复加载
//                    KLoggerUtils.e("新获取：\t" + resID)
                            var bitmap = KGlideUtils.getBitmapFromResouce(resID, overrideWidth, overrideHeight, sizeMultiplier, isCenterCrop)
                            if (bitmap != null && !bitmap.isRecycled) {
                                callBack?.let {
                                    it(key, bitmap)
                                }
                            } else {
                            }
                            //remove(key)//fixme 移除
                        } else {
                            keyMap.get(key)?.let {
                                var value = it + 1
//                        KLoggerUtils.e("等待：\t" + value)
                                keyMap.put(key, value)
                                if (value >= minCount) {
                                    if (value >= maxCount) {
                                        remove(key)//fixme 移除(防止万一死循环)
                                        return@async
                                    } else {
                                        keyMap2.get(key)?.let {
                                            if (!it.isActive || it.isCancelled) {
                                                //没有活动或者已经取消。
                                                remove(key)//fixme 移除
                                            }
                                        }
                                    }
                                }
                            }
                            delay(delayTime)
                            getBitmapFromResouce(resID, overrideWidth, overrideHeight, sizeMultiplier, isCenterCrop, callBack)//fixme 延迟闭合重新调用。
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (!keyMap2.containsKey(key)) {
                    keyMap2.put(key, job)//保存当前协程，判断该协程释放完成
                }
            }
            return job//返回键值
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * fixme 必须在子线程中调用，主线程会报错哦。
     * @param resID Res目录下图片id
     * @param overrideWidth 图片宽度
     * @param overrideHeight 图片高度
     * @param sizeMultiplier 图片压缩比例（0~1）
     * @param isCenterCrop 是否居中裁剪，true图片大于指定宽高时会居中裁剪；fasle不会裁剪（图片能够完整显示，整体会按比例缩放）
     */
    fun getBitmapFromResouce(resID: Int?, overrideWidth: Int, overrideHeight: Int, sizeMultiplier: Float = 1F, isCenterCrop: Boolean = false): Bitmap? {
        try {
            if (resID == null) {
                return null
            }
            var bitmap: Bitmap? = getCacheBitmapOfMemery(getKeyForRes(resID, overrideWidth, overrideHeight, sizeMultiplier))
            if (bitmap != null && !bitmap.isRecycled) {
                return bitmap
            }
            val options = RequestOptions()
            if (overrideWidth <= 0 && overrideHeight <= 0) {
                options.sizeMultiplier(sizeMultiplier)//0~1之间;1是原图大小。
            } else {
                options.override(overrideWidth, overrideHeight)//限制图片的大小
            }
            options.skipMemoryCache(true)// 不使用内存缓存
            options.diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
            //options.diskCacheStrategy(DiskCacheStrategy.ALL);
            if (isCenterCrop) {
                options.centerCrop()//居中剪切
            }
            //options.placeholder(R.drawable.image_placeholder)
            //fixme 必须放在子线程中执行;这里就不新开线程了，交给调用者去开。
            bitmap = Glide.with(KBaseApplication.getInstance())
                    .asBitmap()
                    .load(resID)
                    .apply(options)
                    .submit().get()
            //fixme Glide内部已经解决了图片方向不正确的问题了。
            //解决图片方向显示不正确的问题。(图片模糊和这行没有关系，亲测。)
            //bitmap = KPictureUtils.INSTANCE.rotateBitmap(bitmap, path);
            //保存当前Bitmap
            if (bitmap != null && !bitmap.isRecycled) {
                setCacheBiatmapOfMemery(getKeyForRes(resID, overrideWidth, overrideHeight, sizeMultiplier), bitmap)//fixme 缓存
                return bitmap
            }
        } catch (e: Exception) {
            Log.e("test", "Glide Res流异常" + e.message)//fixme You must call this method on a background thread;必须在子线程中调用，主线程会报错哦。
        }
        return null
    }

    //job?.cancel()可以取消当前协程，释放资源哦。
    fun getBitmapFromUrl(url: String?, overrideWidth: Int, overrideHeight: Int, headers: Map<String, String>? = null, params: Map<String, String>? = null, isCenterCrop: Boolean = false, callBack: (key: String, bitmap: Bitmap) -> Unit): Deferred<Any?>? {
        try {
            if (KStringUtils.isEmpty(url)) {
                return null
            }
            var key = getKeyForUrl(url!!, overrideWidth, overrideHeight)
            var isRuning = keyMap.containsKey(key)//fixme 判断，在协程外判断实时准确。
            var isRuning2 = keyMap.containsKey(url)
            if (!isRuning) {
                keyMap.put(key, 1)//fixme 正在操作
            }
            if (!isRuning2) {
                keyMap.put(url, 1)//fixme 正在操作2
            }
            var job: Deferred<Any?>? = null
            var bitmap: Bitmap? = getCacheBitmapOfMemery(key)//读取缓存位图
            if (bitmap != null && !bitmap.isRecycled) {
                remove(key)//fixme 移除
                remove(url)//fixme 移除2
                //KLoggerUtils.e("缓存:\t" + key)
                callBack?.let {
                    it(key, bitmap)
                }
            } else {
                job = GlobalScope.async {
                    try {
                        if (!isRuning && !isRuning2) {//fixme 防止重复加载
//                    KLoggerUtils.e("新获取网络位图")
                            var bitmap = KGlideUtils.getBitmapFromUrl(url, overrideWidth, overrideHeight, headers, params, isCenterCrop)
                            remove(key)//fixme 移除
                            remove(url)//fixme 移除2
                            if (bitmap != null && !bitmap.isRecycled) {
                                callBack?.let {
                                    it(key, bitmap)
                                }
                            } else {
                            }
                        } else {
                            keyMap.get(key)?.let {
                                var value = it + 1
                                keyMap.put(key, value)
//                        KLoggerUtils.e("等待:\t" + value)
                                if (value >= minCount) {
                                    if (value >= maxCount) {
                                        remove(key)//fixme 移除(防止万一死循环)
                                        remove(url)//fixme 移除2
                                        return@async
                                    } else {
                                        keyMap2.get(key)?.let {
                                            if (!it.isActive || it.isCancelled) {
                                                //没有活动或者已经取消。
                                                remove(key)//fixme 移除
                                                remove(url)//fixme 移除2
                                            }
                                        }
                                    }
                                }
                            }
                            delay(delayTime)
                            getBitmapFromUrl(url, overrideWidth, overrideHeight, headers, params, isCenterCrop, callBack)//fixme 延迟闭合重新调用。
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
                if (!keyMap2.containsKey(key)) {
                    keyMap2.put(key, job)//保存当前协程，判断该协程释放完成
                }
            }
            return job//返回键值
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

    var url_signature = "url_signature_"//fixme 签名字段，如果服务器图片改变了，但是url没有变，本地想要更新图片。那可以改变这个签名值。
    /**
     * @param path 本地图片路径(获取网络图片url也可以。能够加载网络图片，亲测可行)
     * @param overrideWidth 图片宽度
     * @param overrideHeight 图片高度
     * @param sizeMultiplier 图片压缩比例（0~1）
     * @param isCenterCrop 是否居中裁剪，true图片大于指定宽高时会居中裁剪；fasle不会裁剪（图片能够完整显示，整体会按比例缩放）
     */
    fun getBitmapFromUrl(url: String?, overrideWidth: Int, overrideHeight: Int, headers: Map<String, String>? = null, params: Map<String, String>? = null, isCenterCrop: Boolean): Bitmap? {
        try {
            if (KStringUtils.isEmpty(url)) {
                return null
            }
            var bitmap: Bitmap? = getCacheBitmapOfMemery(getKeyForUrl(url!!, overrideWidth, overrideHeight))
            if (bitmap != null && !bitmap.isRecycled) {
                return bitmap
            }
            var byteArrayKey = url_signature + getKeyForUrl(url, 0, 0)//fixme 签名key
            var byteArray = getCacheByteArrayOfStrategy(byteArrayKey)//读取本地缓存字节流
            //KLoggerUtils.e("本地字节：\t" + byteArray?.size)
            if (byteArray == null) {
                byteArray = KHttp.getNetByteArray(url, headers, params)//fixme 读取网络字节流(亲测有效，该字节流可以转位图)
            }
            if (byteArray == null) {
                return null
            }
            setCacheByteArrayOfStrategy(byteArrayKey, byteArray)//fixme 自己缓存到本地磁盘(保存的是原图数据)(保证并发准确性，不能新开线程。)
            val options = RequestOptions()
            if (overrideWidth <= 0 && overrideHeight <= 0) {
                options.sizeMultiplier(1F)//0~1之间;1是原图大小。
            } else {
                options.override(overrideWidth, overrideHeight)//限制图片的大小
            }
            options.skipMemoryCache(true)// 不使用内存缓存
            options.diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
            //options.diskCacheStrategy(DiskCacheStrategy.ALL);
//            DiskCacheStrategy.NONE：表示不缓存任何内容。
//            DiskCacheStrategy.SOURCE：表示只缓存原始图片。
//            DiskCacheStrategy.RESULT：表示只缓存转换过后的图片（默认选项）。
//            DiskCacheStrategy.ALL ：表示既缓存原始图片，也缓存转换过后的图片。
            if (isCenterCrop) {
                options.centerCrop()//居中剪切
            }
            //options.placeholder(R.drawable.image_placeholder)
            //fixme 必须放在子线程中执行;这里就不新开线程了，交给调用者去开。
            bitmap = Glide.with(KBaseApplication.getInstance())
                    .asBitmap()
//                    .load(url)
                    .load(byteArray)//可以直接传字节数组哦
                    .apply(options)
                    .submit().get()
            //fixme Glide内部已经解决了图片方向不正确的问题了。
            //解决图片方向显示不正确的问题。(图片模糊和这行没有关系，亲测。)
            //bitmap = KPictureUtils.INSTANCE.rotateBitmap(bitmap, path);
            //保存当前Bitmap
            byteArray = null
            if (bitmap != null && !bitmap.isRecycled) {
                setCacheBiatmapOfMemery(getKeyForUrl(url, overrideWidth, overrideHeight), bitmap)//fixme 缓存
                return bitmap
            }
        } catch (e: Exception) {
            Log.e("test", "Glide Url流异常" + e.message)
        }
        return null
    }

    /**
     * 设置GIF动态图片
     * @param path 本地图片路径
     * @param overrideWidth 宽度，一般为480
     * @param overrideHeight 高度，一般为800
     * @param imgageView
     */
    fun setGif(path: String?, overrideWidth: Int, overrideHeight: Int, imgageView: ImageView?) {
        try {
            if (path == null || imgageView == null) {
                return
            }
            var gifOptions = RequestOptions()
                    .override(overrideWidth, overrideHeight)
                    .priority(Priority.HIGH)//优先级高
                    // fixme 使用内存缓存(不知道gif图片怎么释放，所以gig内存缓存交给Glide处理(内部好像是弱引用)。)
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)//不缓存磁盘
            Glide.with(KBaseApplication.getInstance())
                    .asGif()
                    .apply(gifOptions)
                    .load(path)
                    .into(imgageView)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

}
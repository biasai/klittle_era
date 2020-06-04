package cn.oi.klittle.era.utils

import java.io.File
import java.io.Serializable


//fixme KCacheUtils.getCache().getAsString("key") //获取字符串数据
//fixme KCacheUtils.getCache().getAsObject("key") //获取任意类型数据

//fixme KCacheUtils.getCache().clear() 数据清除。亲测有效。
//fixme clearCache()清除普通缓存目录数据。
//fixme KCacheUtils.clearImg()清除缓存图片。

/**
 * Created by 彭治铭 on 2018/10/25.
 * fixme 应用卸载之后，数据会自动清除。只有卸载才会清除。重新安装不会清除
 * fixme 新安装的apk版本号必须大于当前，才是重新安装。如果版本号相同，就是覆盖安装(会先卸载再安装)。数据同样会清除。
 * fixme 高版本覆盖低版本，是重新安装。不会发生卸载。只要不卸载。数据就不会丢失。
 *
 *
 * fixme putAny()可以存储数组哦。包括数组(ArrayList或MutableList);数组里的实体类必须继承Serializable才行。亲测可行。
 *
 */
object KCacheUtils {

    /**
     * fixme 亲测，以下普通缓存目录，图片缓存目录，私有缓存目录。数据互不影响。
     * fixme 即:clearCache()清除普通目录数据，对getCacheImg（）图片目录 和 getCacheSecret（）私有目录不会有任何影响。亲测
     */

    //fixme 公用缓存目录(主要存储JSON缓存数据，普通的缓存数据)；KHttp有用到 -> (主要缓存网络Json数据)。
    fun getCache(): KCache {
        return KCache.getInstance()
    }

    //fixme 图片缓存目录，主要缓存图片(kGlideUtils和KHttp里GetNetBitmap()有用到) ->(主要缓存网络图片数据)
    fun getCacheImg(): KCache {
        return KCache.getInstanceImg()
    }

    //fixme 私有缓存目录。主要用于缓存重要的用户数据。将用户数据和普通的缓存数据分开。->(主要缓存重要的用户数据)
    fun getCacheSecret(): KCache {
        return KCache.getInstanceSecret()
    }

    //fixme 自定义目录缓存（传入的是文件目录。不是具体的文件。是目录。）
    fun getCacheAuto(cacheDir: File): KCache {
        return KCache.get(cacheDir)
    }

    //fixme 普通缓存数据清除； 亲测，getCacheImg（）图片目录和getCacheSecret（）私有目录，不会受任何影响。
    fun clearCache() {
        getCache().clear()
    }

    //fixme 图片缓存目录清除
    fun clearImg() {
        getCacheImg().clear()
    }

    //fixme 私有缓存目录清除
    fun clearSecret() {
        getCacheSecret().clear()
    }

    //fixme 清除所有缓存数据
    fun clearAll() {
        clearCache()
        clearImg()
        clearSecret()
    }

    //存储时间，亲测有效。单位秒。即1就代表一秒
    val TIME_MINUTES = 60//1分钟
    val TIME_HOUR = TIME_MINUTES * 60//1小时
    val TIME_DAY = TIME_HOUR * 24//一天
    val TIME_WEEK = TIME_DAY * 7//七天（一周）
    val TIME_MONTH = TIME_DAY * 30//大约一个月
    val TIME_YEAR = TIME_MONTH * 12//大约一年

//    defaultConfig {
//        //6.0是23。即在此必须设置成23及以上，getExternalFilesDir()就不需要权限。getExternal获取的SD卡的路径。
//        //22及以下。getExternalFilesDir()仍然需要权限。
//        targetSdkVersion 23
//    }

    //缓存目录
    fun getCacheDir(): File {
        //KCacheUtils.get(KApplication.getInstance().getFilesDir().getAbsoluteFile());//这个是之前用的。
        //return KBaseApplication.getInstance().cacheDir
        return KPathManagerUtils.getCacheDir()
    }

    //缓存目录的路径,不需要SD卡权限
    fun getCachePath(): String {
        //return getCacheDir().absolutePath//fixme 在应用cache目录下，如：/data/user/0/com.应用包名/cache
        return KPathManagerUtils.getCachePath()
    }

    //fixme 私有缓存目录
    fun getCacheSecretDir(): File {
        //return KBaseApplication.getInstance().getFilesDir()
        return KPathManagerUtils.getCacheSecretDir()
    }

    //fixme 私有缓存目录的路径,不需要SD卡权限
    fun getCacheSecretPath(): String {
        //return getCacheDirSecret().absolutePath//fixme 在应用files目录下。如：/data/user/0/com.应用包名/files
        return KPathManagerUtils.getCacheSecretPath()
    }

    //fixme 图片缓存目录
    fun getCacheImgDir(): File {
        return KPathManagerUtils.getCacheImgDir()
    }

    //fixme 图片缓存目录的路径,不需要SD卡权限
    fun getCacheImgPath(): String {
        return KPathManagerUtils.getCacheImgPath()
    }

    //获取缓存目录大小
    //计算大小,单位根据文件大小情况返回。返回结果带有单位。
    fun getCacheSize(): String {
        var size = KFileUtils.getDirSize(getCacheDir())
        return KStringUtils.getDataSize(size) ?: "0KB"
    }

    //获取缓存目录大小,单位B
    fun getCacheSizeDouble(): Double {
        return KFileUtils.getDirSize(getCacheDir())
    }

    //fixme 获取私有缓存目录大小
    //计算大小,单位根据文件大小情况返回。返回结果带有单位。
    fun getCacheSecretSize(): String {
        var size = KFileUtils.getDirSize(getCacheSecretDir())
        return KStringUtils.getDataSize(size) ?: "0KB"
    }

    //fixme 获取私有缓存目录大小,单位B
    fun getCacheSecretSizeDouble(): Double {
        return KFileUtils.getDirSize(getCacheSecretDir())
    }

    //fixme 获取图片缓存目录大小
    //计算大小,单位根据文件大小情况返回。返回结果带有单位。
    fun getCacheImgSize(): String {
        var size = KFileUtils.getDirSize(getCacheImgDir())
        return KStringUtils.getDataSize(size) ?: "0KB"
    }

    //fixme 获取图片缓存目录大小,单位B
    fun getCacheImgSizeDouble(): Double {
        return KFileUtils.getDirSize(getCacheImgDir())
    }

    //fixme ========================================================================================以下简化私有目录的使用；主要是兼容以前的调用。

    fun putSecret(key: String?, value: Serializable?) {
        putSecret(key, value, null)
    }

    /**
     * 保存 数据 到 缓存中
     *
     * @param key      保存的key
     * @param value    保存的数据
     * @param saveTime 保存的时间，单位：秒
     */
    fun putSecret(key: String?, value: Serializable?, saveTime: Int?) {
        if (key != null) {
            if (value != null && key.trim().length > 0) {
                if (saveTime != null && saveTime > 0) {
                    getCacheSecret().put(key, value, saveTime)//有存储时间限制
                } else {
                    getCacheSecret().put(key, value)//fixme 没有时间限制。即永久保存。
                }
            } else {
                getCacheSecret().remove(key)
            }
        }
    }

    //获取可序列化Serializable对象
    fun getSecret(key: String?): Any? {
        if (key != null) {
            if (key.trim().length > 0) {
                return getCacheSecret().getAsObject(key)
            }
        }
        return null
    }

}
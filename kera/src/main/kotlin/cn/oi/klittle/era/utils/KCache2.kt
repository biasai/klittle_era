package cn.oi.klittle.era.utils

import cn.oi.klittle.era.exception.KCatchException
import cn.oi.klittle.era.gson.KGsonUtils.parseAnyToJSON
import cn.oi.klittle.era.gson.KGsonUtils.parseJSONToAny
import cn.oi.klittle.era.utils.KCacheUtils.getCacheDir
import cn.oi.klittle.era.utils.KCacheUtils.getCacheImgDir
import cn.oi.klittle.era.utils.KCacheUtils.getCacheSecretDir
import java.io.File

class KCache2(cacheDir: File?, max_size: Long, max_count: Int) : KCache(cacheDir, max_size, max_count) {

    companion object {
        var mInstanceMap: HashMap<String, KCache2> = HashMap()
        var cache: KCache2? = null

        //初始化
        fun getInstance(): KCache2 {
            if (cache == null) {
                //cache = KCacheUtils.get(KApplication.getInstance().getFilesDir().getAbsoluteFile());
                cache = get(getCacheDir())
            }
            return cache!!
        }

        var cacheSecret: KCache2? = null

        //fixme 私有目录
        fun getInstanceSecret(): KCache2 {
            if (cacheSecret == null) {
                //cache = KCacheUtils.get(KApplication.getInstance().getFilesDir().getAbsoluteFile());
                cacheSecret = get(getCacheSecretDir())
            }
            return cacheSecret!!
        }

        var cacheImg: KCache2? = null

        //fixme 图片目录
        fun getInstanceImg(): KCache2 {
            if (cacheImg == null) {
                //cache = KCacheUtils.get(KApplication.getInstance().getFilesDir().getAbsoluteFile());
                cacheImg = get(getCacheImgDir())
            }
            return cacheImg!!
        }


        /**
         * @param cacheDir 缓存目录
         * @return
         */
        public fun get(cacheDir: File): KCache2 {
            return get(cacheDir, MAX_SIZE.toLong(), MAX_COUNT)
        }

        private operator fun get(cacheDir: File, max_zise: Long, max_count: Int): KCache2 {
            var manager: KCache2? = null
            if (mInstanceMap == null) {
                //防止異常為空。
                mInstanceMap = HashMap()
            }
            //fixme 利用Map,防止重复实例化。
            val key = cacheDir.absoluteFile.toString() + myPid() + max_zise + max_count
            if (mInstanceMap.containsKey(key)) {
                manager = mInstanceMap[key]
            }
            if (manager == null) {
                manager = KCache2(cacheDir, max_zise, max_count)
                mInstanceMap[cacheDir.absolutePath + myPid()] = manager

            }
            return manager!!
        }

        //fixme 对Any类型的key做独有标记，用于判断是进行了 putAny（）存储。
        fun getKeyForAny(key: String?): String {
            return "kera_little_any_" + key//fixme 为了以后的兼容性，不能在变了。千万不要变哦。
        }

    }


    //fixme 移除数据，兼容 Any（）数据移除。
    override fun remove(key: String?): Boolean {
        var b = super.remove(key)//移除正常键值数据
        var b2 = super.remove(getKeyForAny(key))//移除Any键值数据
        if (b || b2) {
            return true
        } else {
            return false
        }
    }

    // =======================================
    //fixme 将数据，已JSON的方式存储；序列化，不靠谱。还是JSON好用。
    fun putAny(key: String?, obj: Any?) {
        if (key == null || obj == null) {
            return
        }
        var str = parseAnyToJSON(obj).toString()
        put(getKeyForAny(key), str)
    }

    fun putAny(key: String?, obj: Any?, saveTime: Int) {
        if (key == null || obj == null) {
            return
        }
        var str = parseAnyToJSON(obj).toString()
        put(getKeyForAny(key), str, saveTime)
    }

    inline fun <reified T> getAny(key: String?): T? {
        return getAsAny<T>(key)
    }

//     fixme 不要搞个默认String类型，还是让用户手动指明具体类型比较好。防止出错。让用户手动指明。
//     fun getAny(key: String?): String? {
//        return getAsAny<String>(key)
//    }

    //fixme 获取任意数据类型。
    inline fun <reified T> getAsAny(key: String?): T? {
        if (key == null) {
            return null
        }
        try {
            //fixme 通过JSON数据获取，使用getAnyKey(key)。
            getAsString(getKeyForAny(key))?.trim()?.let {
                //KLoggerUtils.e("JSON数据", isLogEnable = true)
                if (it.length > 0) {
                    return parseJSONToAny<T>(it)
                }
            }
        } catch (e: Exception) {
            KLoggerUtils.e("getAsAny()JSON异常：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
        }
        try {
            //fixme 通过反序列化获取。（兼容之前的序列化存储。）；直接使用 key。
            getAsObject(key)?.let {
                if (it is T) {
                    //KLoggerUtils.e("反序列化数据", isLogEnable = true)
                    remove(key)//fixme 先移除之前序列化存储的数据。
                    putAny(key, it)//fixme 再重新将序列化数据，转JSON存储。
                    return it
                }
            }
        } catch (e: Exception) {
            KLoggerUtils.e("getAsAny()反序列化异常2：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
        }

        return null
    }
}
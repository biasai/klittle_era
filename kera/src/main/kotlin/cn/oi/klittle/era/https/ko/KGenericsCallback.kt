package cn.oi.klittle.era.https.ko

import cn.oi.klittle.era.https.KHttp
import cn.oi.klittle.era.utils.KCacheUtils
import cn.oi.klittle.era.utils.KLoggerUtils
import kotlinx.coroutines.experimental.async

/**
 * Created by 彭治铭 on 2018/6/6.
 */
//最后的可变参数，对应json解析的字段
abstract class KGenericsCallback(var https: KHttps? = null) {

    //开始
    open fun onStart() {
        https?.start?.let {
            it()
            //https?.start!!()
        }
        https?.requestCallback?.start?.let {
            it()
        }
        //fixme 显示进度条
        if (https?.isShowLoad ?: false) {
            https?.showProgressbar()
        }
    }

    //成功
    open fun onSuccess(response: String) {
        var result = https?.onPostResponse(response) ?: ""//对服务器返回数据，在解析之前，优先做处理。如解密等
        https?.success?.let {
            it(result)
        }
        https?.requestCallback?.success?.let {
            it(result)
        }
        https?.let {
            //fixme 缓存
            if (it.isCacle) {
                var isCale = true//是否缓存
                it.onIsCacle?.let {
                    isCale = it(response)//判断缓存条件
                }
                if (isCale) {
                    var saveTime = it.saveTime
                    var hp = it
                    KHttp.getCacheUnique(it)?.let {
                        if (hp.isJava) {
                            //fixme 缓存保存在java端
                            if (hp.saveTime != null) {
                                KCacheUtils.getCacheAuto(hp.getJaveCacheFile()).put(it, result, saveTime!!)
                            } else {
                                KCacheUtils.getCacheAuto(hp.getJaveCacheFile()).put(it, result)
                            }
                        } else {
                            //fixme 缓存数据,默认在 公用缓存目录
                            KCacheUtils.put(it, result, saveTime = saveTime)
                        }
                    }
                }
            }
        }
        result?.let {
            onResponse(it)//fixme 成功会回调;
        }
        //最后执行
        onFinish()
    }

    //失败【基本可以断定是网络异常】
    open fun onFailure(errStr: String?) {
        https?.failure?.let {
            it(errStr)
        }
        https?.requestCallback?.failure?.let {
            it(errStr)
        }
        //全局网络错误处理；
        KHttps.error?.let {
            //防止相同的时间段内，重复调用。
            if (System.currentTimeMillis() - KHttps.errorTime > KHttps.errorTimeInterval || KHttps.isFirstError) {
                KHttps.isFirstError = false
                if (https != null) {
                    it(errStr, https!!.isCacle, https!!.cacleInfo)
                }
                KHttps.errorTime = System.currentTimeMillis()
            }
        }

        var response: String? = null
        https?.let {
            var hp = it
            if (it.isCacle) {
                KHttp.getCacheUnique(it)?.let {
                    if (hp.isJava) {
                        //fixme 读取java端的缓存
                        response = KCacheUtils.getCacheAuto(hp.getJaveCacheFile()).getAsString(it)
                    } else {
                        //fixme 读取缓存数据
                        response = KCacheUtils.getString(it)
                    }
                }
            }
        }
        response?.let {
            onResponse(it)//fixme 失败也会回调,只要有数据,和成功回调的是同一个方法
        }
        //最后执行
        onFinish()
    }

    //返回的数据为服务器原始数据，或缓存数据。如果断网，且缓存数据为空，则返回空。
    open fun onResponse(response: String?) {

    }

    //结束，无论是成功还是失败都会调用。且最后执行
    open fun onFinish() {
        https?.cancel()
        https = null
    }
}

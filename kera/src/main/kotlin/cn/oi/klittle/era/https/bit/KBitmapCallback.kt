package cn.oi.klittle.era.https.bit

import android.graphics.Bitmap
import cn.oi.klittle.era.https.KHttp
import cn.oi.klittle.era.https.ko.KHttps

/**
 * 位图回调
 */
abstract class KBitmapCallback(var bitmaps: KBitmaps? = null) {

    open var isUiThread:Boolean=false//是否在主线程回调
    open fun isUiThread(isUiThread: Boolean = true): KBitmapCallback {
        this.isUiThread = isUiThread
        return this
    }

    //开始
    open fun onStart():KBitmapCallback {
        bitmaps?.start?.let {
            it()
            //https?.start!!()
        }
        //fixme 显示进度条
        if (bitmaps?.load ?: false) {
            bitmaps?.showProgress()
        }
        return this
    }

    //成功
    open fun onSuccess(bitmap: Bitmap) {
        //最后执行
        onFinish()
    }

    //失败【基本可以断定是网络异常】
    open fun onFailure(errStr: String?):KBitmapCallback {
        bitmaps?.failure?.let {
            it(errStr)
        }
        //最后执行
        onFinish()
        return this
    }

    //结束，无论是成功还是失败都会调用。且最后执行
    open fun onFinish():KBitmapCallback {

        //fixme 关闭进度条
        if (bitmaps?.load ?: false) {
            bitmaps?.dismissProgress()
        }

        //结束回调（在进度条关闭之后，再回调。防止进度条和activity同时关闭。）
        bitmaps?.finish?.let {
            it()
        }

        //fixme 去除网络请求标志(网络请求结束)
        bitmaps?.let {
            KHttp.map.remove(it?.getUrlUnique())
        }

        bitmaps?.activity = null
        bitmaps?.headers?.clear()
        bitmaps?.params?.clear()
        bitmaps?.url = null
        bitmaps = null
        return this
    }


}
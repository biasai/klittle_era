package cn.oi.klittle.era.dialog

import android.content.Context
import android.view.Gravity
import android.view.View
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseDialog
import cn.oi.klittle.era.comm.KToast
import cn.oi.klittle.era.https.KHttp
import cn.oi.klittle.era.https.ko.KHttps
import cn.oi.klittle.era.view.KProgressCircleView
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import org.jetbrains.anko.UI
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.verticalLayout
import java.util.concurrent.TimeUnit

//fixme timeOutCallback新增超时事件回调。

/**
 * 网络进度条
 * Created by 彭治铭 on 2018/6/24.
 */
open class KProgressDialog(ctx: Context, isStatus: Boolean = true, isTransparent: Boolean = false, var https: KHttps? = null) : KBaseDialog(ctx, isStatus = isStatus, isTransparent = isTransparent) {

    override fun onCreateView(context: Context): View? {
        return context.UI {
            verticalLayout {
                gravity = Gravity.CENTER
                var progressView = KProgressCircleView(this.context)
                addView(progressView)
            }
        }.view
    }

    init {
        isDismiss(false)//触摸不消失
        isLocked(true)//屏蔽返回键
    }

    //被观察者
    private var observable: Observable<Boolean>? = Observable.create<Boolean> {
        async {
            delay(timeOut, TimeUnit.MILLISECONDS)
            it.onComplete()
        }
    }
            .subscribeOn(Schedulers.io())//执行线程在io线程(子线程)
            .observeOn(AndroidSchedulers.mainThread())//回调线程在主线程


    var disposable: Disposable? = null
    //观察者
    private var observe: Observer<Boolean>? = object : Observer<Boolean> {
        override fun onSubscribe(d: Disposable?) {
            disposable = d
        }

        override fun onComplete() {
            showTime?.let {
                if (System.currentTimeMillis() - it >= timeOut) {
                    dismiss()//fixme 弹框超时关闭
                    ctx?.runOnUiThread {
                        try {
                            if (timeOutCallback == null) {
                                //KToast.showError(timeOutInfo)//连接超时
                                showTimeOutInfo()//显示超时错误提示信息
                            } else {
                                timeOutCallback?.let {
                                    it(timeOutInfo)//超时事件回调。
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        override fun onNext(value: Boolean?) {
        }

        override fun onError(e: Throwable?) {
        }
    }

    private var showTime: Long? = 0//记录显示的时间
    var timeOut: Long = 35000//fixme 弹框超时时间默认设置为35秒;单位是毫秒。
    var timeOutInfo: String? = getString(R.string.ktimeout)//连接超时信息设置。
    fun timeOut(timeOut: Long): KProgressDialog {
        this.timeOut = timeOut
        return this
    }

    fun timeOutInfo(timeOutInfo: String?): KProgressDialog {
        this.timeOutInfo = timeOutInfo
        return this
    }

    //显示超时错提示信息
    open fun showTimeOutInfo():KProgressDialog {
        timeOutInfo?.trim()?.let {
            if (it.length > 0) {
                KToast.showError(timeOutInfo)//连接超时
            }
        }
        return this
    }

    //超时事件回调(会在主线程中回调);返回timeOutInfo超时设置信息
    var timeOutCallback: ((timeOutInfo: String?) -> Unit)? = null

    fun timeOutCallback(timeOutCallback: ((timeOutInfo: String?) -> Unit)? = null) {
        this.timeOutCallback = timeOutCallback
    }


    private fun dispose() {
        try {
            //使用RxJava完成弹框超时设置。亲测可行。
            if (observe != null && timeOut > 0) {//timeOut小于等于0不做超时判断
                showTime = System.currentTimeMillis()//记录显示的时间
                disposable?.dispose()//旧的回调取消
                observable?.subscribe(observe)//开启新的回调计时
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun show() {
        super.show()
        dispose()//fixme 防止计时不准确，在show()和onShow()里都调用一次。
    }

    override fun onShow() {
        super.onShow()
        dispose()
    }

    private fun dispose2() {
        try {
            https?.let {
                //fixme 超时处理
                //fixme 移除网络重复请求标志；防止第二次请求时没有反应；
                KHttp.map.remove(KHttp.getUrlUnique(it))
                //https?.onDestrory() fixme 不要调用这个销毁方法；这个在KGenericsCallback里面调用；其他地方不要调用
            }
            https = null
            disposable?.dispose()//旧的回调取消
            disposable = null
            showTime = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDismiss() {
        super.onDismiss()
        dispose2()
    }

    override fun onDestroy() {
        try {
            super.onDestroy()
            https = null
            disposable?.dispose()
            observable = null
            observe = null
            disposable = null
            showTime = null
            timeOutCallback = null
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

}
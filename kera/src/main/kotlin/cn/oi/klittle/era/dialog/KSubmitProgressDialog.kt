package cn.oi.klittle.era.dialog

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseDialog
import cn.oi.klittle.era.comm.KToast
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.https.KHttp
import cn.oi.klittle.era.view.KProgressCircleView
import cn.oi.klittle.era.view.KProgressCircleView2
import org.jetbrains.anko.*
import cn.oi.klittle.era.widget.KToggleView
import cn.oi.klittle.era.widget.compat.KTextView
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

//调用案例
//KSubmitProgressDialog(this).apply {
//    mession("1/2\t提交中...")
//    show()
//}

/**
 * 提交网络进度条（系统进度条+带文本）；fixme timeOut 超时设置，默认是100秒；单位是毫秒。
 * Created by 彭治铭 on 2018/6/24.
 */
open class KSubmitProgressDialog(ctx: Context, isStatus: Boolean = true, isTransparent: Boolean = false) : KBaseDialog(ctx, isStatus = isStatus, isTransparent = isTransparent) {

    var bg: KToggleView? = null
    var progress: KProgressCircleView2? = null
    var mession: KTextView? = null
    override fun onCreateView(context: Context): View? {
        return context.UI {
            verticalLayout {
                gravity = Gravity.CENTER
                relativeLayout {
                    //背景
                    bg = KToggleView(this).apply {
                        toggle {
                            width = kpx.x(600)
                            height = kpx.x(280)
                            shadow_color = Color.BLACK
                            shadow_radius = kpx.x(15f)
                            //all_radius(kpx.x(30f))//不要圆角，圆角效果感觉不好看。
                            //bgHorizontalColors(Color.parseColor("#28292E"), Color.parseColor("#2B2C31"), Color.parseColor("#2A2B30"))
                            bg_color=Color.parseColor("#414141")
                        }
                    }.lparams {
                        width = matchParent
                        height = matchParent
                        centerInParent()
                    }
                    //左边的进度条
                    progress = KProgressCircleView2(this).apply {
                    }.lparams {
                        centerVertically()
                        alignParentLeft()
                        leftMargin = kpx.x(90)
                    }
                    //右边的文本框
                    mession = KTextView(this).apply {
                        textColor = Color.WHITE
                        textSize = kpx.textSizeX(34f)
                        gravity = Gravity.CENTER
                        //text = "1/1\t提交中..."
                        text = "1/1\t" + getString(R.string.ksubmit) + "..."
                    }.lparams {
                        alignParentLeft()
                        centerVertically()
                        leftMargin = kpx.x(160)
                        width = matchParent
                        height= wrapContent
                    }
                }.lparams {
                    width = kpx.x(630)
                    height = kpx.x(380)
                }
            }
        }.view
    }

    init {
        isDismiss(false)//触摸不消失
        isLocked(true)//屏蔽返回键
    }

    //文本设置
    fun mession(mession: String?) {
        this.mession?.setText(mession)
    }

    //被观察者
    private var observable: Observable<Boolean>? = Observable.create<Boolean> {
        GlobalScope.async {
            delay(timeOut)
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
                                showTimeOutInfo()//显示超时错误信息
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
    var timeOut: Long = 90000//fixme 弹框超时时间默认设置为90秒;单位是毫秒。
    fun timeOut(timeOut: Long): KSubmitProgressDialog {
        this.timeOut = timeOut
        return this
    }

    var timeOutInfo: String? = getString(R.string.ktimeout)//fixme 默认为空；getString(R.string.ktimeout)//连接超时信息设置。
    fun timeOutInfo(timeOutInfo: String?): KSubmitProgressDialog {
        this.timeOutInfo = timeOutInfo
        return this
    }

    //显示超时错误提示信息
    open fun showTimeOutInfo():KSubmitProgressDialog{
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

    private fun dispose2() {
        try {
            disposable?.dispose()//旧的回调取消
            disposable = null
            showTime = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun show() {
        super.show()
        dispose()//fixme 防止计时不准确，在show()和onShow()里都调用一次。
    }

    open override fun onShow() {
        super.onShow()
        dispose()//fixme 防止计时不准确，在show()和onShow()里都调用一次。
    }

    override fun onDismiss() {
        super.onDismiss()
        dispose2()
    }

    override fun onDestroy() {
        try {
            super.onDestroy()
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
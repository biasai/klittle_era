package cn.oi.klittle.era.https.ko

import android.app.Activity
import cn.oi.klittle.era.dialog.KProgressDialog

//调用案例
//KPolling().apply {
//    isShowLoad(true)//显示进度条
//    //设置网络请求次数
//    maxProgress(10)
//
//    url("")//设置网络请求接口
//
//    //当前网络请求
//    onNext { currentProgress, http ->
//        //currentProgress 返回当前请求次数（从1开始）
//        //http 网络请求；里面已经实现了onNext()方法。
//        http.apply {
//            url("")//可以重置网络请求接口
//            onFailure {
//                isPolling = false//网络异常；停止轮询。
//            }
//            //开始执行网络请求
//            post<String>() {
//
//            }
//        }
//    }
//    //所有网络请求完成
//    onComplete {
//
//    }
//    //开始执行（必须动调用）；放在最后调用（防止上面的回调方法无效）
//    start {
//
//    }
//}

/**
 * 网络轮询【亲测可行。】
 * Created by 彭治铭 on 2019/4/1.
 */
open class KPolling {

    companion object {
        //fixme 防止轮询重复提交[必須等所有轮询结束后，才能进行下一轮的轮询。]
        //fixme 坚决不能允许重复轮询；[所以不添加允许重复的字段。为了安全和保险不允许重复。]
        fun getUrlUnique(polling: KPolling): String {
            polling.url?.let {
                if (it.length > 0) {
                    return it
                }
            }
            return "blank"
        }

        var map = mutableMapOf<String, String>()
        fun setUrlUnique(key: String) {
            map.put(key, "Polling is ongoing")
        }

        fun removeUrlUnique(key: String) {
            map.remove(key)
        }

        //判断是否重复请求
        fun isRepeatRequest(key: String): Boolean {
            if (map.containsKey(key)) {
                return true //请求重复
            } else {
                return false//没有重复
            }
        }

    }

    constructor(url: String? = null, maxProgress: Int? = 1) {
        this.url = url
        this.maxProgress = maxProgress
    }

    var maxProgress: Int? = 1
    //网络请求总次数。
    open fun maxProgress(maxProgress: Int?): KPolling {
        this.maxProgress = maxProgress
        return this
    }

    //当前网络进度（从1开始）
    var currentProgress: Int = 1

    open fun currentProgress(currentProgress: Int): KPolling {
        this.currentProgress = currentProgress
        return this
    }

    var successNumber = 0//成功次数（自己可以根据轮询的调用结果；来记录成功的次数。扩展字段。自主调用）
    var failureNumber = 0//失败次数
    var errorNumber = 0//错误次数（服务器连接异常等）

    var isPolling = true//是否继续轮询；false停止轮询;如果网络异常了。可以手动设置false；

    open fun isPolling(isPolling: Boolean): KPolling {
        this.isPolling = isPolling
        return this
    }

    /**
     * 每次网络完成；都会回调一次；返回的是当前网络进度。
     * 回调参数 progress从1开始。
     */
    private var next: ((currentProgress: Int, http: KHttps) -> Unit)? = null

    //fixme 调用者主要实现这个方法。
    open fun onNext(next: ((currentProgress: Int, http: KHttps) -> Unit)? = null) {
        this.next = next
    }

    private var complete: (() -> Unit)? = null
    //fixme 完成；所有网络请求完成时回调。
    open fun onComplete(complete: (() -> Unit)? = null) {
        this.complete = complete
    }

    /**
     * fixme 开始执行；必须手动调用。
     * @return true执行成功；false执行失败
     */
    open fun start(start: (() -> Unit)? = null): Boolean {
        if (maxProgress == null) {
            return false
        }
        maxProgress?.let {
            if (it <= 0) {
                return false
            }
        }
        if (isRepeatRequest(getUrlUnique(this))) {
            return false//fixme 不允许重复轮询
        }
        setUrlUnique(getUrlUnique(this))//fixme 设置正在轮询标志
        start?.let {
            it()//最开始的回调
        }
        //显示进度条；内部会根据isShowLoad判断是否显示进度条。
        showProgressbar()
        //轮询回调
        subscribe()
        return true//执行成功
    }

    var url: String? = null
    open fun url(url: String? = null): KPolling {
        this.url = url
        return this
    }

    //获取网络请求框架[子类重写改这个就行了。]
    open protected fun getHttps(): KHttps? {
        return KHttps().url(url)
    }

    //执行
    private fun subscribe() {
        next?.let {
            var https = getHttps()
            //默认不显示网络进度条；轮询里面进度条一开一关效果很不好；
            // 轮询应该有自己进度条。（放在start()和onComplete()里面）
            https?.isShowParams(false)
            https?.onNext {
                //fixme 下次回调执行[针对下一次的]
                if (isPolling && currentProgress < maxProgress!!) {
                    currentProgress++//fixme 网络请求完成一次；累加一次。
                    subscribe()//继续轮询
                } else {
                    complete()//完成
                }
            }
            //fixme 当前执行[针对当前]
            if (isPolling && currentProgress <= maxProgress!! && https != null) {
                it(currentProgress, https)
            } else {
                complete()//完成
                https = null
            }
        }
    }

    //完成
    private fun complete() {
        removeUrlUnique(getUrlUnique(this))//fixme 轮询结束
        complete?.let {
            it()//所有请求完成；或isPolling==false时回调完成。
        }
        dismissProgressbar()//关闭进度条
        next = null
        complete = null
    }

    open var isShowLoad: Boolean = false//fixme 是否显示进度条，默认不显示
    open fun isShowLoad(isShowLoad: Boolean = true): KPolling {
        this.isShowLoad = isShowLoad
        return this
    }

    var activity: Activity? = null

    fun activity(activity: Activity? = KHttps.getActivity()): KPolling {
        this.activity = activity
        return this
    }

    //进度条变量名，子类虽然可以重写，但是类型改不了。所以。进度条就不允许继承了。子类自己去定义自己的进度条。
    open var progressbar: KProgressDialog? = null//进度条(Activity不能为空，Dialog需要Activity的支持)

    //fixme 显示进度条[子类要更改进度条，可以重写这个]
    //重写的时候，注意屏蔽父类的方法，屏蔽 super.showProgress()
    open fun showProgressbar() {
        if (isShowLoad) {
            //进度条必须在主线程中实例化
            if (activity == null) {
                activity = KHttps.getActivity()
            }
            activity?.let {
                if (!it.isFinishing) {
                    it.runOnUiThread {
                        try {
                            if ((progressbar == null || progressbar?.dialog == null) && activity != null) {
                                progressbar = KProgressDialog(activity!!)
                                progressbar?.timeOut(100000)//询询超时时间设置为100秒
                            }
                            progressbar?.show()
                        } catch (e: Exception) {
                            //这里异常了不会影响回调进度。服务器（域名错误）异常时，会回调onFailure()
                            //KLoggerUtils.e("网络弹框实例化异常：\t" + e.message+"\t"+progressbar+"\t"+activity)
                        }
                    }
                }
            }
        }
    }

    //fixme 关闭进度条[子类可以重写,重写的时候，记得对自己的进度条进行内存释放。]
    //重写的时候，注意屏蔽父类的方法，屏蔽 super.showProgress()
    open fun dismissProgressbar() {
        if (isShowLoad) {
            if (activity != null && !activity!!.isFinishing) {
                //fixme 进度条关闭最好在ui主线程中进行。防止错误。
                activity?.runOnUiThread {
                    progressbar?.let {
                        progressbar?.dismiss()
                        progressbar?.onDestroy()
                        progressbar = null
                    }
                }
            } else {
                progressbar?.let {
                    progressbar?.dismiss()
                    progressbar?.onDestroy()
                    progressbar = null
                }
            }
        }
    }

}
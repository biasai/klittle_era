package cn.oi.klittle.era.widget.web

import android.content.Context
import android.graphics.Bitmap
import android.net.http.SslError
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.utils.KLoggerUtils
import com.github.lzyzsd.jsbridge.BridgeWebView
import com.github.lzyzsd.jsbridge.BridgeWebViewClient
import org.jetbrains.anko.runOnUiThread
import java.lang.Exception

//fixme 调用案例：bridge 是BridgeWebView控件
//bridge?.apply {
//    //fixme java调用js
//    //调用默认方法
//    send("传递给js的string参数") {
//        //js调用成功回调
//    }
//    //调用指定方法
//    callHandler("js方法名", "参数") {
//        //js调用成功回调
//    }
//
//    //fixme js调用java方法
//    //默认方法
//    setDefaultHandler { data, function ->
//        data//js传递过来的参数
//        function.onCallBack("js调用java成功")//回调给js
//    }
//    //调用指定方法
//    registerHandler("注册方法名；与js端保持一致") { data, function ->
//        data//js传递过来的参数
//        function.onCallBack("js调用java成功")//回调给js
//    }
//}

/**
 * fixme android和js回调,只对当前loadUrl()页面才有效;其他页面无效(如:页面里发生了跳转.)
 * fixme visibility= View.GONE 控件隐藏了，android与js同样能够互调（亲测）
 * fixme 注意Android调用Js必须在主线程中才能调用。
 *
 * fixme progress 当前页面的加载进度；bridge?.progress自带进度值。
 * fixme bridge?.progress获取的时候，必须在主线程中才能获取，不然异常报错。切记！！是主线程。
 */

//github地址:https://github.com/lzyzsd/JsBridge

//https://github.com/wendux/DSBridge-Android 这个听说好像是JsBridge升级版；但是start星数好像没有JsBridge多。先存一下。
/**
 * 引用（（1.0.4就是当前最新的）） implementation 'com.github.lzyzsd:jsbridge:1.0.4'
 * 这个BridgeWebView能完美适配手机版网页；比我自己要好很多。
 */
open class KJsBridgeWebView : BridgeWebView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
        initWeb()//fixme 初始化
    }

    constructor(context: Context) : super(context) {
        initWeb()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initWeb()
    }

    //fixme 注意：loadUrl()必须在主线程中进行。

    private var curl: String? = null
    private var ctime: Long? = 0L
    override fun loadUrl(url: String?) {
        try {
            if (ctime == null) {
                ctime = 0L
            }
            if (url != null && url.equals(curl) && System.currentTimeMillis() - ctime!! <= 1000) {
                return//防止相同的时间内重复加载
            }
            curl = url
            ctime = System.currentTimeMillis()
            resumeTimers()//fixme 重新开始，防止JS回调无效。
            context?.runOnUiThread {
                super.loadUrl(url)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun reload() {
        try {
            context?.runOnUiThread {
                super.reload()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * loadUrl(url)这个是加载网络页面
     * 加载本地页面（来自Assets目录），如;loadUrlFromAssets("html/Hello.html")
     * @param assetsPath Assets目录下的html文件路径
     */
    open fun loadUrlFromAssets(assetsPath: String) {
        loadUrl("file:///android_asset/" + assetsPath)
    }

    //loadCallBack添加进度回调
    open fun loadUrlFromAssets(assetsPath: String, loadCallBack: ((progress: Int) -> Unit)? = null) {
        loadUrlFromAssets(assetsPath)
        loadCallBack(loadCallBack)
    }

    /**
     * 加载本地页面（来自SD存储卡）
     */
    open fun loadUrlFromFile(filePath: String) {
        loadUrl("file:///sdcard/" + filePath)
    }

    //loadCallBack添加进度回调
    fun loadUrlFromFile(filePath: String, loadCallBack: ((progress: Int) -> Unit)? = null) {
        loadUrlFromFile(filePath)
        loadCallBack(loadCallBack)
    }

    //fixme loadCallBack添加进度回调
    open fun loadUrl(url: String, loadCallBack: ((progress: Int) -> Unit)?) {
        loadUrl(url)
        loadCallBack(loadCallBack)
    }

    /**
     * fixme 重新加载
     * @param loadCallBack 添网页进度回调（0~100）
     */
    open fun reload(loadCallBack: ((progress: Int) -> Unit)) {
        reload()//fixme 重新加载。原生自带方法
        loadCallBack(loadCallBack)//添加页面进度监听，亲测有效。
    }

    open fun getUseWideViewPort(): Boolean {
        return settings.useWideViewPort
    }

    /**
     * fixme true 自适应手机屏幕，完整的显示页面;
     *
     * fixme false BridgeWebView第三方默认就是false;只要改成false就能恢复成BridgeWebVie默认样式；
     * fixme （默认效果还是很不错的。对普通的网页手机适配。如:文本大小在pc端和手机端大小几乎是一致的。）
     */
    open fun setUseWideViewPort(useWideViewPort: Boolean? = getUseWideViewPort()) {
        useWideViewPort?.let {
            getSettings().setUseWideViewPort(it)//fixme 会立即生效，页面会立即刷新（亲测有效。）
        }
    }


    private fun initWeb() {
        //设置字符集编码
        getSettings().setDefaultTextEncodingName("UTF-8")
        //开启JavaScript支持
        getSettings().setJavaScriptEnabled(true)
        getSettings().setLoadWithOverviewMode(true)
        getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL)
        getSettings().setDomStorageEnabled(true)
        //fixme 自适应手机屏幕，完整的显示页面
        setUseWideViewPort(true)
        //setInitialScale(1)//fixme 屏蔽掉，不要调用；防止BridgeWebView原生的显示异常。
        getSettings().setLoadsImagesAutomatically(true)
        //fixme 屏蔽掉loadData();防止页面第一次加载；js回调异常（5.1及以下回调异常）。
        //loadData("<meta name=\"viewport\" content=\"width=device-width\">\n" + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no\">", "text/html", "UTF-8")

        //不支持缩放
        getSettings().setSupportZoom(false)
        //取消滚动条
        setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY)
        //清除缓存
        clearCache(true)
        //不从缓存中读取，仅从网络中读取
        getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE)
        setWebViewClient()
        setWebChromeClient()
    }

    var isLoadSuccess: Boolean = true//判断页面是否加载成功。//fixme 只有当isLoadSuccess==true，并且progress==100时，页面才算加载成功。
    //fixme progress 当前页面的加载进度；bridge?.progress自带进度值。
    //fixme bridge?.progress获取的时候，必须在主线程中才能获取，不然异常报错。切记！！是主线程。

    open fun setWebViewClient() {
//        fixme 父类中已经设置了WebViewClient(),如:setWebViewClient(generateBridgeWebViewClient());
//        fixme 自定义WebViewClient必须要继承BridgeWebViewClient;不然android,js互相调用就无效了;
//        继续在当前browser中响应;
        setWebViewClient(object : BridgeWebViewClient(this) {
//            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
//                super.shouldOverrideUrlLoading(view, url)
//                // TODO Auto-generated method stub
//                view.loadUrl(url)
//                return true
//            }

            var isOnError = false//判断页面是否加载错误。

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                //KLoggerUtils.e("开始加载")
                isOnError = false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                //KLoggerUtils.e("加载完成")
                //不管是成功，还是失败，都会回调该方法。
                if (isOnError) {
                    isLoadSuccess = false
                } else {
                    isLoadSuccess = true
                }
                isOnError = false
            }

            //onReceivedError()还有一个方法，哪个方法感觉一点屁用都没有，就用这个。
            //不能保证百分比，会执行，如发送了500等错误，就不会调用。
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                //如果加载失败了，onReceivedError会在onPageFinished()之前调用。
                isOnError = true
                //KLoggerUtils.e("加载失败:\t"+description)
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                //super.onReceivedSslError(view, handler, error)
                //handler.cancel();// super中默认的处理方式，WebView变成空白页
                if (handler != null && isIgnoreSsslError) {
                    handler.proceed();//fixme 忽略证书的错误继续加载页面内容，不会变成空白页面（比如https://inv-veri.chinatax.gov.cn/）
                }
            }

        })
    }

    //fixme https://blog.csdn.net/Wang_WY/article/details/86253980 解决https无法认证的问题。但是这样不允许上架谷歌商店。
    var isIgnoreSsslError = true//fixme 是否忽略ssl错误。http 可以直接加载，但 https 是经过ssl 加密的

    //fixme 设置进度回调，和允许弹窗。
    //fixme 即使WebView隐藏（visibility= View.GONE）了；弹窗也不会隐藏。亲测。
    //fixme js里的alert()弹框，会直接在安卓的界面显示出来（居中）不会被遮挡，和安卓的Dialog是一样的。与webView已经没有关系了。弹窗大小也不受webView的大小影响。
    open fun setWebChromeClient() {
        setWebChromeClient(object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                view?.let {
                    //当进度走到100的时候,页面加载完成
                    //if (newProgress == 100) { }
                    loadCallBack?.let {
                        it(newProgress)
                        if (newProgress == 100) {
                            loadCallBack = null//置空,防止重复回调.
                        }
                    }
                }
            }
        })//fixme 允许弹窗。设置之后,才能显示页面弹窗,不然显示不出来
    }

    var loadCallBack: ((progress: Int) -> Unit)? = null

    /**
     * fixme 页面加载回调;只对手动调用loadUrl(url)的页面才有回调;页面内部自己跳转新页面是没有回调的.
     * fixme 所以每次loadUrl(url)之后,都要重新loadCallBack()一次.
     * @param progress 加载进度(0~100)
     */
    open fun loadCallBack(loadCallBack: ((progress: Int) -> Unit)? = null) {
        this.loadCallBack = loadCallBack
    }

    //fixme 注意Android调用Js必须在主线程中才能调用。

    /**
     * fixme 获取所有所有云打印机设备
     */
    open fun getPrintAoList(handlerName: String = "AndroidGetPrintAoList", data: String? = "android", callback: ((data: String?) -> Unit)? = null) {
        try {
            context?.runOnUiThread {
                callHandler(handlerName, data) {
                    var data = it//js返回的数据
                    callback?.let {
                        it(data)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * fixme android端掉用js里的打印方法；（js端使用Lodop云打印实现）;再页面加载完成之后再调用。
     * @param handlerName 与js端统一的方法名；
     * @param data 参数
     * @param callback 回调;返回是否成功，和js原数据。
     */
    open fun print(handlerName: String = "AndroidPrint", data: String? = "android", callback: ((isSuccess: Boolean, data: String?) -> Unit)? = null) {
        try {
            context?.runOnUiThread {
                /**
                 * fixme android 调用js指定的方法名;可以作为一个案例。
                 * @param handlerName 与js端统一的方法名。
                 * @param data 参数，可以为空；
                 * @param callback js方法调用成功后，js给android端的回调。
                 */
                callHandler(handlerName, data) {
                    var data = it//js返回的数据
                    callback?.let {
                        var isSuccess = false//判断是否打印成功
                        data?.let {
                            if (it.trim().equals("100")) {
                                isSuccess = true//打印成功；规定100成功，其他都是失败。
                                data = KBaseUi.getString(R.string.kprint_success)//打印成功
                            } else if (it.trim().equals("0") || it.trim().equals("")) {
                                data = KBaseUi.getString(R.string.kprint_fail)//打印失败
                            }
                        }
                        it(isSuccess, data)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * fixme 销毁;最后记得置空。
     */
    open fun onDestroy() {
        try {
            curl = null
            ctime = null
            loadCallBack = null
            clearCache(true);
            //mWebView.loadUrl("about:blank"); // clearView() should be changed to loadUrl("about:blank"), since clearView() is deprecated now
            freeMemory();
            pauseTimers();
            //加载null内容
            loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
            //清除历史记录
            clearHistory()
            //移除WebView
            if (getParent() != null && getParent() is ViewGroup) {
                (getParent() as ViewGroup).removeView(this)
            }
            //销毁VebView
            destroy()
            //WebView置为null
            //mWebView = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
package cn.oi.klittle.era.widget.web

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import org.jetbrains.anko.backgroundColor


/**
 * 自定义WebView,H5基本设置
 */
open class KMyWebView : KRadiusWebView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

//    init {
//        setLayerType(View.LAYER_TYPE_HARDWARE, null)//如果关闭了，视频会无法播放。所以建议开启。
//        //默认背景色是白色，在此设置成透明(也可以设置成其他颜色)。一般都有效，极少数设备可能无效。
//        backgroundColor = Color.TRANSPARENT
//        setWebChromeClient(WebChromeClient()) //（一定要加）
//        //设置字符集编码
//        getSettings().setDefaultTextEncodingName("UTF-8")
//        //开启JavaScript支持
//        getSettings().setJavaScriptEnabled(true)
//        //自适应手机屏幕，完整的显示页面
//        getSettings().setUseWideViewPort(true)
//        getSettings().setLoadWithOverviewMode(true)
//        getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL)
//        getSettings().setDomStorageEnabled(true)
//        //setInitialScale(1)//fixme 屏蔽掉，不要调用；防止BridgeWebView原生的显示异常。
//        getSettings().setLoadsImagesAutomatically(true)
//        //fixme 屏蔽掉loadData();防止页面第一次加载；js回调异常（5.1及以下回调异常）。
//        //loadData("<meta name=\"viewport\" content=\"width=device-width\">\n" + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no\">", "text/html", "UTF-8")
//
//        //不支持缩放
//        getSettings().setSupportZoom(false)
//        //取消滚动条
//        setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY)
//        //清除缓存
//        clearCache(true)
//        //不从缓存中读取，仅从网络中读取
//        getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE)
//        //继续在当前browser中响应
//        setWebViewClient(object : WebViewClient() {
//            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
//                // TODO Auto-generated method stub
//                view.loadUrl(url)
//                return true
//            }
//        })
//    }

}
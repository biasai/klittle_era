package cn.oi.klittle.era.widget.web

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 * 自定义WebView
 */
open class KWebView : KMyWebView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

//    init {
//        setLayerType(View.LAYER_TYPE_HARDWARE, null)//开启硬件加速,不然圆角没有效果(现在的圆角已经不受硬件加速影响了，都有效)
//    }

}
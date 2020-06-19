package cn.oi.klittle.era.base

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import cn.oi.klittle.era.R
import cn.oi.klittle.era.utils.KAssetsUtils
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.view.KAutoLinefeedCenterLayout
import cn.oi.klittle.era.view.KAutoLinefeedLayout
import cn.oi.klittle.era.view.KProgressCircleView
import cn.oi.klittle.era.view.KProgressCircleView2
import cn.oi.klittle.era.widget.*
import cn.oi.klittle.era.widget.MPAndroidChart.*
import cn.oi.klittle.era.widget.book.view.KBookPageView
import cn.oi.klittle.era.widget.chart.KBaseChartView
import cn.oi.klittle.era.widget.compat.*
import cn.oi.klittle.era.widget.drawerLayout.KDrawerLayout
import cn.oi.klittle.era.widget.layout.KSwipeMenuLayout
import cn.oi.klittle.era.widget.photo.KImageView
import cn.oi.klittle.era.widget.photo.KPhotoView
import cn.oi.klittle.era.widget.recycler.KFooterView
import cn.oi.klittle.era.widget.recycler.KImageItemRecyclerView
import cn.oi.klittle.era.widget.recycler.KRecyclerView
import cn.oi.klittle.era.widget.seekbar.KSeekBarProgressBar
import cn.oi.klittle.era.widget.seekbar.KVerticalSeekBarProgressBar
import cn.oi.klittle.era.widget.video.KVideoView
import cn.oi.klittle.era.widget.viewpager.*
import cn.oi.klittle.era.widget.web.K0JsBridgeWebView
import cn.oi.klittle.era.widget.web.K3WebView
import cn.oi.klittle.era.widget.web.KBridgeWebView
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import java.lang.Exception

//            简单实用;这个是直接到Activity中使用;自己可以在子类中进行封装。
//            var ui=object :KBaseUi(){
//            override fun createView(ctx: Context?): View? {
//                return ctx?.UI { verticalLayout {  } }?.view
//            }
//
//            override fun destroyView() {
//            }
//        }
//        setContentView(ui?.createView())

//        fixme 调用案例，字类可以继承KBaseUi（）实现里面的createView（）方法。或者如下直接实例化。
//        var ui=object : KBaseUi() {
//            override fun createView(ctx: Context?): View? {
//                var view=verticalLayout {
//                    backgroundColor = Color.WHITE
//                    var toolbar = KToolbar(this, ctx as Activity)?.apply {
//                        contentView?.apply {
//                            backgroundColor = Color.parseColor("#0078D7")
//                        }
//                        //左边返回文本（默认样式自带一个白色的返回图标）
//                        leftTextView?.apply {
//                        }
//                        //中间文本
//                        titleTextView?.apply {
//                            text = "视频"
//                        }
//                    }
//                }
//                return view
//            }
//        }
//        setContentView(ui?.createView(ctx))

//            //fixme 直接在Activity里面调用也可以哦。可以显示UI；亲测可行。直接在Actvity中调用，就不需要再调用setContentView（）了。
//            KBaseUi.apply {
//                knumberProgressBar {
//                    setProgress(10)//设置进度
//                    setTextColor(Color.parseColor("#418fde"))//字体颜色
//                    setDstColor(Color.parseColor("#dedede"))//底部进度颜色
//                    setProgressColor(Color.parseColor("#418fde"))//进度条颜色，一般和字体颜色一致
//                }.lparams {
//                    width = kpx.x(700)
//                    height = kpx.x(36)
//                }
//            }

//            KBaseUi.apply {
//                kverticalLayout {
//                    //fixme 这里面，自定义布局无法正常使用lparams{}属性。
//                }.apply {
//                    //fixme 使用apply{}；自定义布局就可以正常的使用lparams{}了
//                    button {
//                        text = "你好"
//                    }.lparams {
//                        width = kpx.x(300)
//                        height = wrapContent
//                    }
//                }
//            }

/**
 * 集成自定义组件。
 */

abstract class KBaseUi {

    //初始化布局，交给子类去实现。ctx作为参数传进来。防止获取不到。
    //fixme 注意，这里的上下文不能是application的上下文，必须是Activity的。
    //return ctx?.UI { verticalLayout {  } }?.view
    //fixme setContentView(ui.createView(this))//在Activity中调用。
    abstract fun createView(ctx: Context? = getActivity()): View?

    private var isDestroy: Boolean? = false//判断是否已经销毁；防止重复异常

    //fixme 子类可以重写(记得在Activity里的finish()方法中手动调用一次)
    open fun destroy(activity: Activity?) {
        isDestroy?.let {
            destroyView(activity)
        }
        isDestroy = null
    }

    //fixme 销毁(防止泄露)；在Activity关闭的时候，记得手动调用一次（最好在finish()方法中调用，onDestroy()不一定会执行）
    //fixme 这里要注意，最好手动传入Activity,防止错误。（防止错误，就不使用默认参数了）
    private fun destroyView(activity: Activity?) {
        destroyView(getContentView(activity), activity)//fixme 手动到 finish() 中调用。
    }


    var destroyDelay = 1000L//fixme 单位毫秒，1000即一秒；测试一秒够了。

    //fixme 销毁该View及其所有的子View
    private fun destroyView(view: View?, activity: Activity?) {
        view?.postDelayed({
            activity?.runOnUiThread {
                destroyViewGroup(view)//要在主线程中进行
            }
        }, destroyDelay)//fixme 防止跳转的时候效果不好(如：看到桌面，突然消失等。)。所以延迟清除；单位毫秒
        //fixme 放心 postDelayed延迟会执行的。以前4.0之前可能不会执行；现在基本百分百都会执行。
        //fixme 之所以要延迟，是因为直接销毁，视觉效果不会。还是延迟一会儿好。
    }

    //统一背景色
    open fun backgroundColor(): Int {
        return Color.parseColor("#f8f8f8")
    }

    //标题栏颜色
    open fun toolBarColor(): Int {
        return Color.parseColor("#ffffff")
    }

    /**
     * fixme 父容器设置获取焦点；解决edit文本输入框软键盘自动弹窗的问题。
     * fixme 最好在最顶层的父容器中设置。亲测有效。
     */
    fun setRequestFocus(view: View?) {
        view?.let {
            //fixme 解决软键盘自动弹出，就使用这个方法；不要手动设置SOFT_INPUT_STATE_HIDDEN（效果很不好）
            it.isFocusable = true
            it.isFocusableInTouchMode = true
            it.requestFocus()
            it.requestFocusFromTouch()
        }
    }

    companion object {
        fun getContext(): Context {
            return KBaseApplication.getInstance()
        }

        fun getActivity(): Activity? {
            return KBaseActivityManager.getInstance().stackTopActivity
        }

        //    fixme 可以在RecyclerView里的 onViewRecycled()方法调用。
//    override fun onViewRecycled(holder: MyViewHolder) {
//        super.onViewRecycled(holder)
//        holder?.content?.let {
//            KBaseUi.recycleAutoBgBitmap(it)
//        }
//    }
        //fixme 仅仅只是清除AutoBg位图,url等属性。不会对KAutoBgEntity对象本身制空
        //fixme 清除该View（或所有子View）的AutoBg位图。
        fun recycleAutoBgBitmap(view: View?) {
            try {
                if (view != null) {
                    if (view is K4AutoBgView) {
                        view.recycleAutoBgBitmap()//fixme 仅仅清除位图
                        //KLoggerUtils.e("销毁AutoBg位图")
                    } else if (view is ViewGroup) {
                        var count = view.childCount
                        if (count > 0) {
                            for (i in 0..(count - 1)) {
                                recycleAutoBgBitmap(view.getChildAt(i))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        //fixme Activity调用案例：KBaseUi.destroyViewGroup(contentView);注意：要在主线程中调用，不然会异常。
        //fixme 释放控件（亲测有效，能够释放View下面所有的子View）
        fun destroyViewGroup(view: View?) {
            if (view == null) {
                return
            }
            view?.let {
                if (it is ViewGroup) {
                    //fixme 销毁之后，后面会把子View全部清空的。
                    if (it.childCount <= 0 || it.getTag() == 4444) {
                        //KLoggerUtils.e("反复销毁:\t" + it.getTag()+"\t"+it.childCount+"\t"+it.id)
                        return//fixme 防止重复调用。
                    } else if (it.childCount > 0) {
                        it.setTag(4444)//fixme 标志已经销毁；防止重复销毁。以防万一。
                        //KLoggerUtils.e("正在销毁===\t"+it.id)
                    }
                }
            }
            //KLoggerUtils.e("销毁")
            try {
                if (view != null) {
                    if (view is K1Widget) {
                        view.onDestroy()//fixme 销毁，防止内存泄漏,放心会执行最子类的那个方法。即会执行重写的方法。
                        //KLoggerUtils.e("======================================================释放")
                        //log打印太过频繁且内容相同，可以不会打印。
                        //fixme 打印有时可能不会打印出来。但是代码已经执行了，只是没有打印出来而已。（放心已经执行了）
                    } else if (view is KBridgeWebView) {
                        //webView销毁
                        view?.onDestroy()
                    } else if (view is KVideoView) {
                        //视频销毁
                        view?.onDestory()
                    } else if (view is KLineChart) {
                        //线性图表
                        view.onDestroy()
                    } else if (view is ViewGroup) {
                        if (view is KVerticalLayout) {
                            view.onDestroy()//fixme 销毁，防止内存泄漏
                            //KLoggerUtils.e("释放2")
                        } else if (view is RecyclerView) {
                            //fixme 释放适配器
                            //view?.adapter?.notifyDataSetChanged()
                            view?.adapter = null
                            view?.layoutManager = null
                        } else if (view is ViewPager) {
                            //view?.adapter?.notifyDataSetChanged()
                            view?.adapter = null//释放适配器。
                        }
                        var count = view.childCount
                        if (count > 0) {
                            for (i in 0..(count - 1)) {
                                destroyViewGroup(view.getChildAt(i))
                            }
                        }
                    }
                    clearView(view)//fixme 最后再清除一遍。
                }
            } catch (e: Exception) {
                e.printStackTrace()
                KLoggerUtils.e("KBaseUi destroyViewGroup（）异常：\t" + e.message, isLogEnable = true)
            }
        }

        //fixme 控件清除
        private fun clearView(view: View?) {
            view?.apply {
                //KLoggerUtils.e("class:\t" + view.javaClass)
                when (view.javaClass.toString()) {
                    "class android.support.v4.widget.CircleImageView" -> {
                        //这个控件背景清除之后，会异常。
                    }
                    //以下控件背景清除之后不会报错，测试过。没有测试过的控件不要随便添加。
                    "class org.jetbrains.anko._RelativeLayout",
                    "class org.jetbrains.anko._LinearLayout",
                    "class org.jetbrains.anko.constraint.layout._ConstraintLayout",
                    "class org.jetbrains.anko._FrameLayout",
                    "class org.jetbrains.anko._AbsoluteLayout",
                    "class android.view.View",
                    "class android.widget.TextView",
                    "class android.widget.Button",
                    "class android.widget.ImageView",
                    "class android.widget.ImageButton",
                    "class org.jetbrains.anko.recyclerview.v7._RecyclerView",
                    "class android.support.v4.widget.SwipeRefreshLayout"
                    -> {
                        clearBackground(view)//fixme 清除背景，防止背景清除之后报错，不要对所有控件都清除背景。
                    }
                    else -> {
                        //其他
                    }
                }
                setOnFocusChangeListener(null)
                setOnClickListener(null)
                setOnLongClickListener(null)
                setOnTouchListener(null)
                clearAnimation()
                clearFocus()
                if (this is ViewGroup) {
                    removeAllViews()//fixme 清除所有的子View
                    setTag(null)
                }
                visibility = View.GONE
            }
        }

        //fixme 清除背景
        // "class android.support.v4.widget.CircleImageView" 背景清除之后报错了。
        //防止报错异常；所以不要对所有的控件都清除背景;
        private fun clearBackground(view: View?) {
            view?.apply {
                backgroundDrawable = null
                if (Build.VERSION.SDK_INT >= 16) {
                    background = null
                }
                //KLoggerUtils.e("背景清除:\t" + view.javaClass)
            }
        }

        //获取Activity最外层的容器
        fun getContentView(activity: Activity? = getActivity()): View? {
            try {
                var window: Window? = activity?.window
                window?.let {
                    var decorView = it.decorView//布局里面的最顶层控件，本质上是FrameLayout(帧布局)，FrameLayout.LayoutParams
                    var contentView = decorView.findViewById<View>(android.R.id.content) as ViewGroup//我们的布局文件。就放在contentView里面。contentView本质上也是FrameLayout(帧布局)，FrameLayout.LayoutParams
                    if (contentView.childCount > 0) {
                        return contentView.getChildAt(0)//这就是我们xml布局文件最外层的那个父容器控件。
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        /**
         * getColor()这个方法系统已经有了，不能再重载
         * 获取颜色值（默认从Resources目录，从color文件中获取）
         */
        open fun getColor(id: Int): Int {
            return getContext().getResources().getColor(id)
        }

        /**
         * 默认就从Res目录下读取
         * 获取String文件里的字符,<string name="names">你好%s</string>//%s 是占位符,位置随意
         * @param formatArgs 是占位符
         */
        open fun getString(id: Int, formatArgs: String?): String {
            try {
                if (formatArgs != null) {
                    //fixme 不要去除空格；空格也占位，有时也需要也很重要的。
                    return getContext().resources.getString(id, formatArgs) as String
                }
                return getContext().getString(id) as String
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }

        open fun getString(id: Int): String {
            return Companion.getString(id, null)
        }

        /**
         * 获取String文件里的字符串數組
         */
        open fun getStringArray(id: Int): Array<String> {
            return getContext().resources.getStringArray(id)
        }

        open fun getBundle(activity: Activity? = getActivity()): Bundle? {
            activity?.intent?.let {
                it.extras?.let {
                    return it
                }
            }
            return null
        }

        //获取位图
        open fun getBitmapFromAssets(filePath: String, isRGB_565: Boolean = false): Bitmap {
            return KAssetsUtils.getInstance().getBitmapFromAssets(filePath, isRGB_565)
        }

        open fun getBitmapFromResource(resID: Int, isRGB_565: Boolean = false): Bitmap {
            return KAssetsUtils.getInstance().getBitmapFromResource(resID, isRGB_565)
        }

        open fun getBitmapFromFile(filePath: String, isRGB_565: Boolean = false): Bitmap {
            return KAssetsUtils.getInstance().getBitmapFromFile(filePath, isRGB_565)
        }

        //自定义控件
        //必须在AnkoComponent里面定义（只能在Activity中和Fragment中添加，才有效。）
        //ViewManager.随便取名
        //fixme 测试发现，放在普通类文件里也有效果。放在静态方法中也有效果。
        inline fun ViewManager.kbaseView(init: (@AnkoViewDslMarker KBaseView).() -> Unit): KBaseView {
            return ankoView({ ctx: Context -> KBaseView(ctx) }, theme = 0) { init() }
        }

        //fixme KRecyclerView
        inline fun ViewManager.krecyclerView(init: (@AnkoViewDslMarker KRecyclerView).() -> Unit): KRecyclerView {
            return ankoView({ ctx: Context -> KRecyclerView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kRecyclerView(init: (@AnkoViewDslMarker KRecyclerView).() -> Unit): KRecyclerView {
            return ankoView({ ctx: Context -> KRecyclerView(ctx) }, theme = 0) { init() }
        }


//                    fixme 有垂直进度条的KRecyclerView调用案例(可以多次调用)
//                kRecyclerViewBar(ctx,this)?.apply {
//                    setLinearLayoutManager()
//                    var datas: MutableList<String>? = mutableListOf()
//                    for (i in 0..300) {
//                        datas?.add("" + i)
//                    }
//                    adapter = KRecyclerAdapter(datas)
//                    lparams {
//                        width= matchParent
//                        height=kpx.screenHeight()/2
//                    }
//                }

        //fixme 有垂直滚动条的KRecyclerView
        fun kRecyclerViewBar(context: Context?, viewGroup: ViewGroup?): KRecyclerView? {
            try {
                if (context != null && viewGroup != null) {
                    var recyclerView = LayoutInflater.from(context).inflate(
                            R.layout.kera_widget_recycler, viewGroup, false) as KRecyclerView
                    viewGroup?.addView(recyclerView)
                    recyclerView?.setVerticalScrollBarEnabled()
                    return recyclerView
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun krecyclerViewBar(context: Context?, viewGroup: ViewGroup?): KRecyclerView? {
            return kRecyclerViewBar(context, viewGroup)
        }

        inline fun ViewManager.kimageItemRecyclerView(init: (@AnkoViewDslMarker KImageItemRecyclerView).() -> Unit): KImageItemRecyclerView {
            return ankoView({ ctx: Context -> KImageItemRecyclerView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kImageItemRecyclerView(init: (@AnkoViewDslMarker KImageItemRecyclerView).() -> Unit): KImageItemRecyclerView {
            return ankoView({ ctx: Context -> KImageItemRecyclerView(ctx) }, theme = 0) { init() }
        }

        //fixme viewPager
        inline fun ViewManager.kviewPager(init: (@AnkoViewDslMarker KViewPager).() -> Unit): KViewPager {
            return ankoView({ ctx: Context -> KViewPager(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kViewPager(init: (@AnkoViewDslMarker KViewPager).() -> Unit): KViewPager {
            return ankoView({ ctx: Context -> KViewPager(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.verticalViewPager(init: (@AnkoViewDslMarker VerticalViewPager).() -> Unit): VerticalViewPager {
            return ankoView({ ctx: Context -> VerticalViewPager(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kverticalViewPager(init: (@AnkoViewDslMarker KVerticalViewPager).() -> Unit): KVerticalViewPager {
            return ankoView({ ctx: Context -> KVerticalViewPager(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kVerticalViewPager(init: (@AnkoViewDslMarker KVerticalViewPager).() -> Unit): KVerticalViewPager {
            return ankoView({ ctx: Context -> KVerticalViewPager(ctx) }, theme = 0) { init() }
        }

        //fixme 弹性ScrollView
        inline fun ViewManager.kscrollView(init: (@AnkoViewDslMarker KGradientScrollView).() -> Unit): KGradientScrollView {
            return ankoView({ ctx: Context -> KGradientScrollView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kScrollView(init: (@AnkoViewDslMarker KGradientScrollView).() -> Unit): KGradientScrollView {
            return ankoView({ ctx: Context -> KGradientScrollView(ctx) }, theme = 0) { init() }
        }

        //                    fixme 有垂直滚动条的弹性ScrollView;调用案例
//                kScrollViewBar(ctx, this)?.apply {
//                    verticalLayout {  }
//                }

        //fixme 有垂直滚动条的弹性ScrollView
        fun kscrollViewBar(context: Context?, viewGroup: ViewGroup?): KGradientScrollView? {
            try {
                if (context != null && viewGroup != null) {
                    var scrollView = LayoutInflater.from(context).inflate(
                            R.layout.kera_widget_scrollview, viewGroup, false) as KGradientScrollView//fixme 和RecyclerView一样；在xml布局文件中加载才会有滚动条。
                    viewGroup?.addView(scrollView)
                    scrollView?.setVerticalScrollBarEnabled(true)
                    scrollView?.isFillViewport = true
                    return scrollView
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun kScrollViewBar(context: Context?, viewGroup: ViewGroup?): KGradientScrollView? {
            return kscrollViewBar(context, viewGroup)
        }

        inline fun ViewManager.kgradientView(init: (@AnkoViewDslMarker KGradientView).() -> Unit): KGradientView {
            return ankoView({ ctx: Context -> KGradientView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kGradientView(init: (@AnkoViewDslMarker KGradientView).() -> Unit): KGradientView {
            return ankoView({ ctx: Context -> KGradientView(ctx) }, theme = 0) { init() }
        }

        //虚线
        inline fun ViewManager.kdashView(init: (@AnkoViewDslMarker KDashView).() -> Unit): KDashView {
            return ankoView({ ctx: Context -> KDashView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kDashView(init: (@AnkoViewDslMarker KDashView).() -> Unit): KDashView {
            return ankoView({ ctx: Context -> KDashView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kframeView(init: (@AnkoViewDslMarker KFrameView).() -> Unit): KFrameView {
            return ankoView({ ctx: Context -> KFrameView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kFrameView(init: (@AnkoViewDslMarker KFrameView).() -> Unit): KFrameView {
            return ankoView({ ctx: Context -> KFrameView(ctx) }, theme = 0) { init() }
        }

        //数字进度条
        inline fun ViewManager.knumberProgressBar(init: (@AnkoViewDslMarker KNumberProgressBar).() -> Unit): KNumberProgressBar {
            return ankoView({ ctx: Context -> KNumberProgressBar(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kNumberProgressBar(init: (@AnkoViewDslMarker KNumberProgressBar).() -> Unit): KNumberProgressBar {
            return ankoView({ ctx: Context -> KNumberProgressBar(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kfooterView(init: (@AnkoViewDslMarker KFooterView).() -> Unit): KFooterView {
            return ankoView({ ctx: Context -> KFooterView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kFooterView(init: (@AnkoViewDslMarker KFooterView).() -> Unit): KFooterView {
            return ankoView({ ctx: Context -> KFooterView(ctx) }, theme = 0) { init() }
        }

        //viewpager下移动的圆点
        inline fun ViewManager.kdotView(init: (@AnkoViewDslMarker KDotView).() -> Unit): KDotView {
            return ankoView({ ctx: Context -> KDotView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kDotView(init: (@AnkoViewDslMarker KDotView).() -> Unit): KDotView {
            return ankoView({ ctx: Context -> KDotView(ctx) }, theme = 0) { init() }
        }

        //菜单滑动条(自定义)
        inline fun ViewManager.kLayoutBar(init: (@AnkoViewDslMarker KLayoutBar).() -> Unit): KLayoutBar {
            return ankoView({ ctx: Context -> KLayoutBar(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.klayoutBar(init: (@AnkoViewDslMarker KLayoutBar).() -> Unit): KLayoutBar {
            return ankoView({ ctx: Context -> KLayoutBar(ctx) }, theme = 0) { init() }
        }

        //菜单滑动条(系统原生：android.support.design.widget.TabLayout)
        inline fun ViewManager.kTabLayout(init: (@AnkoViewDslMarker KTabLayout).() -> Unit): KTabLayout {
            return ankoView({ ctx: Context -> KTabLayout(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.ktabLayout(init: (@AnkoViewDslMarker KTabLayout).() -> Unit): KTabLayout {
            return ankoView({ ctx: Context -> KTabLayout(ctx) }, theme = 0) { init() }
        }

        //fixme cmpat兼容组件
        //按钮
        inline fun ViewManager.kbutton(init: (@AnkoViewDslMarker KButton).() -> Unit): KButton {
            return ankoView({ ctx: Context -> KButton(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kButton(init: (@AnkoViewDslMarker KButton).() -> Unit): KButton {
            return ankoView({ ctx: Context -> KButton(ctx) }, theme = 0) { init() }
        }

        //文本
        inline fun ViewManager.ktextView(init: (@AnkoViewDslMarker KTextView).() -> Unit): KTextView {
            return ankoView({ ctx: Context -> KTextView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kTextView(init: (@AnkoViewDslMarker KTextView).() -> Unit): KTextView {
            return ankoView({ ctx: Context -> KTextView(ctx) }, theme = 0) { init() }
        }

        //阴影
        inline fun ViewManager.kshadowView(init: (@AnkoViewDslMarker KShadowView).() -> Unit): KShadowView {
            return ankoView({ ctx: Context -> KShadowView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kShadowView(init: (@AnkoViewDslMarker KShadowView).() -> Unit): KShadowView {
            return ankoView({ ctx: Context -> KShadowView(ctx) }, theme = 0) { init() }
        }

        //普通view
        inline fun ViewManager.kview(init: (@AnkoViewDslMarker KView).() -> Unit): KView {
            return ankoView({ ctx: Context -> KView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kView(init: (@AnkoViewDslMarker KView).() -> Unit): KView {
            return ankoView({ ctx: Context -> KView(ctx) }, theme = 0) { init() }
        }

        //文本输入框
        inline fun ViewManager.keditText(init: (@AnkoViewDslMarker KEditText).() -> Unit): KEditText {
            return ankoView({ ctx: Context -> KEditText(ctx) }, theme = 0) { init() }
        }


        inline fun ViewManager.kEditText(init: (@AnkoViewDslMarker KEditText).() -> Unit): KEditText {
            return ankoView({ ctx: Context -> KEditText(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kmenuHidingEditText(init: (@AnkoViewDslMarker KMenuHidingEditText).() -> Unit): KMenuHidingEditText {
            return ankoView({ ctx: Context -> KMenuHidingEditText(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kMenuHidingEditText(init: (@AnkoViewDslMarker KMenuHidingEditText).() -> Unit): KMenuHidingEditText {
            return ankoView({ ctx: Context -> KMenuHidingEditText(ctx) }, theme = 0) { init() }
        }

        //可以滚动的文本框
        inline fun ViewManager.kscrollTextView(init: (@AnkoViewDslMarker KScrollTextView).() -> Unit): KScrollTextView {
            return ankoView({ ctx: Context -> KScrollTextView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kScrollTextView(init: (@AnkoViewDslMarker KScrollTextView).() -> Unit): KScrollTextView {
            return ankoView({ ctx: Context -> KScrollTextView(ctx) }, theme = 0) { init() }
        }

        //fixme 添加组件（布局控件），去掉 @AnkoViewDslMarker，这样就能识别出lparams(不过，也只能识别出线性布局的lparam,其他布局的依然识别不出)
        inline fun ViewManager.kverticalLayout(init: KVerticalLayout.() -> Unit): KVerticalLayout {
            return ankoView({ ctx: Context -> KVerticalLayout(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kVerticalLayout(init: KVerticalLayout.() -> Unit): KVerticalLayout {
            return ankoView({ ctx: Context -> KVerticalLayout(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.klinearLayout(init: KVerticalLayout.() -> Unit): KVerticalLayout {
            var linearLayout = ankoView({ ctx: Context -> KVerticalLayout(ctx) }, theme = 0) { init() }
            linearLayout.orientation = LinearLayout.HORIZONTAL //水平线性布局
            return linearLayout
        }

        inline fun ViewManager.kLinearLayout(init: KVerticalLayout.() -> Unit): KVerticalLayout {
            var linearLayout = ankoView({ ctx: Context -> KVerticalLayout(ctx) }, theme = 0) { init() }
            linearLayout.orientation = LinearLayout.HORIZONTAL //水平线性布局
            return linearLayout
        }

        //webView
        inline fun ViewManager.kwebView(init: (@AnkoViewDslMarker K3WebView).() -> Unit): K3WebView {
            return ankoView({ ctx: Context -> K3WebView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kWebView(init: (@AnkoViewDslMarker K3WebView).() -> Unit): K3WebView {
            return ankoView({ ctx: Context -> K3WebView(ctx) }, theme = 0) { init() }
        }

        //安卓js交互
        inline fun ViewManager.kjsBridgeWebView(init: (@AnkoViewDslMarker K0JsBridgeWebView).() -> Unit): K0JsBridgeWebView {
            return ankoView({ ctx: Context -> K0JsBridgeWebView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kJsBridgeWebView(init: (@AnkoViewDslMarker K0JsBridgeWebView).() -> Unit): K0JsBridgeWebView {
            return ankoView({ ctx: Context -> K0JsBridgeWebView(ctx) }, theme = 0) { init() }
        }

        //KSeekBarProgressBar
        inline fun ViewManager.kseekbarProgressbar(init: (@AnkoViewDslMarker KSeekBarProgressBar).() -> Unit): KSeekBarProgressBar {
            return ankoView({ ctx: Context -> KSeekBarProgressBar(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kSeekbarProgressbar(init: (@AnkoViewDslMarker KSeekBarProgressBar).() -> Unit): KSeekBarProgressBar {
            return ankoView({ ctx: Context -> KSeekBarProgressBar(ctx) }, theme = 0) { init() }
        }

        //垂直seekbar
        inline fun ViewManager.kverticalSeekbarProgressbar(init: (@AnkoViewDslMarker KVerticalSeekBarProgressBar).() -> Unit): KVerticalSeekBarProgressBar {
            return ankoView({ ctx: Context -> KVerticalSeekBarProgressBar(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kVerticalSeekbarProgressbar(init: (@AnkoViewDslMarker KVerticalSeekBarProgressBar).() -> Unit): KVerticalSeekBarProgressBar {
            return ankoView({ ctx: Context -> KVerticalSeekBarProgressBar(ctx) }, theme = 0) { init() }
        }

        /**
         * 开关按钮
         */
        inline fun ViewManager.ktoggleView(init: (@AnkoViewDslMarker KToggleView).() -> Unit): KToggleView {
            return ankoView({ ctx: Context -> KToggleView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kToggleView(init: (@AnkoViewDslMarker KToggleView).() -> Unit): KToggleView {
            return ankoView({ ctx: Context -> KToggleView(ctx) }, theme = 0) { init() }
        }

        /**
         * 验证码
         */
        inline fun ViewManager.kvalidationView(init: (@AnkoViewDslMarker KValidationView).() -> Unit): KValidationView {
            return ankoView({ ctx: Context -> KValidationView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kValidationView(init: (@AnkoViewDslMarker KValidationView).() -> Unit): KValidationView {
            return ankoView({ ctx: Context -> KValidationView(ctx) }, theme = 0) { init() }
        }

        /**
         * 图片控件
         */
        inline fun ViewManager.kphotoView(init: (@AnkoViewDslMarker KPhotoView).() -> Unit): KPhotoView {
            return ankoView({ ctx: Context -> KPhotoView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kPhotoView(init: (@AnkoViewDslMarker KPhotoView).() -> Unit): KPhotoView {
            return ankoView({ ctx: Context -> KPhotoView(ctx) }, theme = 0) { init() }
        }

        /**
         * 普通图片控件
         */
        inline fun ViewManager.kimageView(init: (@AnkoViewDslMarker KImageView).() -> Unit): KImageView {
            return ankoView({ ctx: Context -> KImageView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kImageView(init: (@AnkoViewDslMarker KImageView).() -> Unit): KImageView {
            return ankoView({ ctx: Context -> KImageView(ctx) }, theme = 0) { init() }
        }

        /**
         * 重写原生视频控件
         */
        inline fun ViewManager.kvideoView(init: (@AnkoViewDslMarker KVideoView).() -> Unit): KVideoView {
            return ankoView({ ctx: Context -> KVideoView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kVideoView(init: (@AnkoViewDslMarker KVideoView).() -> Unit): KVideoView {
            return ankoView({ ctx: Context -> KVideoView(ctx) }, theme = 0) { init() }
        }

        /**
         * 仿真翻页效果
         */
        inline fun ViewManager.kbookPageView(init: (@AnkoViewDslMarker KBookPageView).() -> Unit): KBookPageView {
            return ankoView({ ctx: Context -> KBookPageView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kBookPageView(init: (@AnkoViewDslMarker KBookPageView).() -> Unit): KBookPageView {
            return ankoView({ ctx: Context -> KBookPageView(ctx) }, theme = 0) { init() }
        }

        /**
         * 图表
         */
        inline fun ViewManager.kbaseChartView(init: (@AnkoViewDslMarker KBaseChartView).() -> Unit): KBaseChartView {
            return ankoView({ ctx: Context -> KBaseChartView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kBaseChartView(init: (@AnkoViewDslMarker KBaseChartView).() -> Unit): KBaseChartView {
            return ankoView({ ctx: Context -> KBaseChartView(ctx) }, theme = 0) { init() }
        }

        /**
         * MPAndroidChart图表
         */

        //线型图表
        inline fun ViewManager.klineChart(init: (@AnkoViewDslMarker KLineChart).() -> Unit): KLineChart {
            return ankoView({ ctx: Context -> KLineChart(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kLineChart(init: (@AnkoViewDslMarker KLineChart).() -> Unit): KLineChart {
            return ankoView({ ctx: Context -> KLineChart(ctx) }, theme = 0) { init() }
        }

        //条型图表
        inline fun ViewManager.kbarChart(init: (@AnkoViewDslMarker KBarChart).() -> Unit): KBarChart {
            return ankoView({ ctx: Context -> KBarChart(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kBarChart(init: (@AnkoViewDslMarker KBarChart).() -> Unit): KBarChart {
            return ankoView({ ctx: Context -> KBarChart(ctx) }, theme = 0) { init() }
        }

        //水平条型图表
        inline fun ViewManager.khorizontalBarChart(init: (@AnkoViewDslMarker KHorizontalBarChart).() -> Unit): KHorizontalBarChart {
            return ankoView({ ctx: Context -> KHorizontalBarChart(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kHorizontalBarChart(init: (@AnkoViewDslMarker KHorizontalBarChart).() -> Unit): KHorizontalBarChart {
            return ankoView({ ctx: Context -> KHorizontalBarChart(ctx) }, theme = 0) { init() }
        }

        //饼状图（扇型图）
        inline fun ViewManager.kpieChart(init: (@AnkoViewDslMarker KPieChart).() -> Unit): KPieChart {
            return ankoView({ ctx: Context -> KPieChart(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kPieChart(init: (@AnkoViewDslMarker KPieChart).() -> Unit): KPieChart {
            return ankoView({ ctx: Context -> KPieChart(ctx) }, theme = 0) { init() }
        }

        //雷达图（蜘蛛网图）
        inline fun ViewManager.kradarChart(init: (@AnkoViewDslMarker KRadarChart).() -> Unit): KRadarChart {
            return ankoView({ ctx: Context -> KRadarChart(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kRadarChart(init: (@AnkoViewDslMarker KRadarChart).() -> Unit): KRadarChart {
            return ankoView({ ctx: Context -> KRadarChart(ctx) }, theme = 0) { init() }
        }

        //侧滑菜单布局
        inline fun ViewManager.KSwipeMenuLayout(init: (@AnkoViewDslMarker KSwipeMenuLayout).() -> Unit): KSwipeMenuLayout {
            return ankoView({ ctx: Context -> KSwipeMenuLayout(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kSwipeMenuLayout(init: (@AnkoViewDslMarker KSwipeMenuLayout).() -> Unit): KSwipeMenuLayout {
            return ankoView({ ctx: Context -> KSwipeMenuLayout(ctx) }, theme = 0) { init() }
        }

        //路径控件(异形控件)
        inline fun ViewManager.KPathView(init: (@AnkoViewDslMarker KPathView).() -> Unit): KPathView {
            return ankoView({ ctx: Context -> KPathView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kPathView(init: (@AnkoViewDslMarker KPathView).() -> Unit): KPathView {
            return ankoView({ ctx: Context -> KPathView(ctx) }, theme = 0) { init() }
        }

        //路径控件(异形控件)
        inline fun ViewManager.KDrawerLayout(init: (@AnkoViewDslMarker KDrawerLayout).() -> Unit): KDrawerLayout {
            return ankoView({ ctx: Context -> KDrawerLayout(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kDrawerLayout(init: (@AnkoViewDslMarker KDrawerLayout).() -> Unit): KDrawerLayout {
            return ankoView({ ctx: Context -> KDrawerLayout(ctx) }, theme = 0) { init() }
        }

        //fixme 自动水平换行布局(向左靠齐)
        inline fun ViewManager.KAutoLinefeedLayout(init: (@AnkoViewDslMarker KAutoLinefeedLayout).() -> Unit): KAutoLinefeedLayout {
            return ankoView({ ctx: Context -> KAutoLinefeedLayout(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kAutoLinefeedLayout(init: (@AnkoViewDslMarker KAutoLinefeedLayout).() -> Unit): KAutoLinefeedLayout {
            return ankoView({ ctx: Context -> KAutoLinefeedLayout(ctx) }, theme = 0) { init() }
        }

        //fixme 自动水平换行布局(居中换行)
        inline fun ViewManager.KAutoLinefeedCenterLayout(init: (@AnkoViewDslMarker KAutoLinefeedCenterLayout).() -> Unit): KAutoLinefeedCenterLayout {
            return ankoView({ ctx: Context -> KAutoLinefeedCenterLayout(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kAutoLinefeedCenterLayout(init: (@AnkoViewDslMarker KAutoLinefeedCenterLayout).() -> Unit): KAutoLinefeedCenterLayout {
            return ankoView({ ctx: Context -> KAutoLinefeedCenterLayout(ctx) }, theme = 0) { init() }
        }

        //fixme 进度条
        inline fun ViewManager.KProgressCircleView(init: (@AnkoViewDslMarker KProgressCircleView).() -> Unit): KProgressCircleView {
            return ankoView({ ctx: Context -> KProgressCircleView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kProgressCircleView(init: (@AnkoViewDslMarker KProgressCircleView).() -> Unit): KProgressCircleView {
            return ankoView({ ctx: Context -> KProgressCircleView(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.KProgressCircleView2(init: (@AnkoViewDslMarker KProgressCircleView2).() -> Unit): KProgressCircleView2 {
            return ankoView({ ctx: Context -> KProgressCircleView2(ctx) }, theme = 0) { init() }
        }

        inline fun ViewManager.kProgressCircleView2(init: (@AnkoViewDslMarker KProgressCircleView2).() -> Unit): KProgressCircleView2 {
            return ankoView({ ctx: Context -> KProgressCircleView2(ctx) }, theme = 0) { init() }
        }

    }

}
package cn.oi.klittle.era.base

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.v4.view.ViewPager
import android.view.*
import cn.oi.klittle.era.R
import cn.oi.klittle.era.entity.feature.KBlur
import cn.oi.klittle.era.utils.KAssetsUtils
import cn.oi.klittle.era.utils.KBitmapUtils
import cn.oi.klittle.era.utils.KLoggerUtils
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.runOnUiThread
import android.opengl.ETC1.getWidth
import android.opengl.ETC1.getHeight
import cn.oi.klittle.era.comm.kpx
import android.opengl.ETC1.getWidth
import android.opengl.ETC1.getHeight
import android.R.attr.gravity
import android.app.AlertDialog


//    子类初始化参考
//    init {
//        isDark(true).isDismiss(false).isLocked(true) {
//            Log.e("ui", "返回键按下监听")
//        }
//    }

//                    //fixme 高斯模糊；以下是都是默认值
//                    alertDialog.blur {
//                        isBlur=true//开启模糊效果
//                        level=25f //模糊等级【0 ~ 25之间】
//                        coverColor=Color.TRANSPARENT
//                        //coverColor=Color.parseColor("#888080FF")//要蒙上的颜色;fixme 注意要带上透明度哦。
//                    }

/**
 * @author 彭治铭
 */
//fixme isStatus 是否显示状态栏【true会显示状态栏，false不会显示状态栏】,默认有状态栏
//fixme isTransparent 背景是否透明,true透明，false背景会有遮罩层半透明的效果。默认背景透明
open class KBaseDialog() {
    //fixme Activity不要使用全局变量。局部即可。防止内存泄露
    //fixme 不要使用单列模式，一个Activity就对应一个Dialog。（Dialog需要Activity的支持）
    var dialog: Dialog? = null

    /**
     * 销毁
     */
    open fun onDestroy() {
        if (dialog != null && ctx != null) {
            try {
                dismiss()
                onShow = null
                onDismiss = null
                layoutId = null
                dialog = null
                ctx = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    open var ctx: Context? = null//上下文
    open var layoutId: Int? = 0//布局文件
    open var isStatus: Boolean = true//是否有状态栏
    open var isTransparent: Boolean = true//背景是否透明

    constructor(ctx: Context, layoutId: Int? = 0, isStatus: Boolean = true, isTransparent: Boolean = true) : this() {
        this.ctx = ctx
        this.layoutId = layoutId
        this.isStatus = isStatus
        this.isTransparent = isTransparent
        //fixme init中父类无法调用子类主构造函数里的参数；所以在此不使用init{}而是使用传统的构造函数
        initUi(ctx, layoutId, isStatus, isTransparent)
    }

    /**
     * fixme 如果传入的xml布局为空。则可重写以下方法来创建视图View,一般都是重写的该方法。
     */
    open fun onCreateView(context: Context): View? {
        //return UI { }.view//使用Anko布局
        return null
    }

    var isCurrentDark = KBaseApplication.getInstance().isDeaultDark//fixme 记录当前状态栏字体的颜色
    //fixme 手动设置状态栏字体颜色(true黑色(深色，默认)，false白色(浅色)。)[亲测有效]
    fun isDark(isDark: Boolean = isCurrentDark): KBaseDialog {
        isCurrentDark = isDark
        KBaseApplication.getInstance().setStatusBarDrak(dialog!!.window, isCurrentDark)
        return this
    }

    //fixme 触摸最外层控件，弹窗是否消失。
    fun isDismiss(isDismiss: Boolean = true): KBaseDialog {
        //触摸最外层控件，是否消失\
        dialog?.window.let {
            if (isDismiss) {
                getParentView(it)?.setOnTouchListener { view, motionEvent ->
                    if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                        dismiss()
                    }
                    true
                }
            } else {
                getParentView(it)?.setOnTouchListener(null)
            }
        }
        return this
    }


    //fixme true屏蔽返回键，false不屏蔽返回键。
    //fixme 是否屏蔽返回键+监听返回键（只监听返回键按下。）
    fun isLocked(isLocked: Boolean = true, callback: (() -> Unit)? = null): KBaseDialog {
        //屏蔽返回键,并且监听返回键（只监听返回键按下。）
        dialog?.setOnKeyListener { dialog, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                callback?.let {
                    it()//监听返回键(按下)
                }
                isLocked//true,已经处理(屏蔽)，false没有处理，系统会自行处理。
            } else {
                false//返回false，不屏蔽
                //返回键以外交给系统自行处理。不可以屏蔽，不然输入法键盘的按键可能无效。如删除键
            }
        }
        return this
    }

    //fixme 为Window设置动画【Dialog和PopuWindow动画都是Style文件】
    fun setWindowAnimations(styleId: Int) {
        dialog?.window?.setWindowAnimations(styleId);
    }

    open fun setNavigationBarColor(color: Int): KBaseDialog {
        //对版本号21 即5.0及以上才有效。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog?.getWindow()?.setNavigationBarColor(color);
        }
        return this
    }

    open fun setNavigationBarColor(color: String): KBaseDialog {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog?.getWindow()?.setNavigationBarColor(Color.parseColor(color));
        }
        return this
    }

    /**
     * 界面和底部导航栏融为一体,親測有效。
     */
    open fun setNavigationBarTransparent(): KBaseDialog {
        // 透明导航栏，屏幕的底部[部分手机完全透明，部分手机可能半透明。]
        dialog?.getWindow()?.addFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        return this
    }

    /**
     *fixme 不拦截按键事件(如：返回键)和触摸点击事件
     */
    open fun setNotFocusableAndNotTouchable() {
        var window = dialog?.window
        var layoutParams = window?.getAttributes();
        //layoutParams!!.flags兼容之前的设置，不然会覆盖之前的设置。
        //WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 不拦截按键（如返回键）
        //WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE 不拦截触摸点击事件，即Dialog里所有控件都不具备点击能力了。
        layoutParams?.flags = layoutParams!!.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE//fixme 核心代码是这个属性。
        window?.setAttributes(layoutParams);
        window?.setDimAmount(0f);
        dialog?.setCanceledOnTouchOutside(false);
        window = null
        layoutParams = null
    }

    /**
     * fixme 不拦截按键事件（如：返回键）；任然具备触摸点击事件。（会拦截触摸点击事件）
     */
    open fun setNotFocusable() {
        var window = dialog?.window
        var layoutParams = window?.getAttributes();
        //layoutParams!!.flags兼容之前的设置，不然会覆盖之前的设置。
        //WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 不拦截按键（如返回键）
        layoutParams?.flags = layoutParams!!.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE//fixme 核心代码是这个属性。
        window?.setAttributes(layoutParams);
        window?.setDimAmount(0f);
        dialog?.setCanceledOnTouchOutside(false);
        window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)   //fixme 去除不拦截触摸点击事件(亲测有效)；即具备触摸点击事件能力。
        window = null
        layoutParams = null
    }

    //初始化视图
    protected fun initUi(ctx: Context?, layoutId: Int? = 0, isStatus: Boolean = true, isTransparent: Boolean = true) {
        //==========================================================================================开始

        //if (Build.VERSION.SDK_INT < 19) {//4.4以下全部全屏。因为控制不了状态栏的颜色。所以就不要状态栏。直接全屏}
        //如果Activity是全屏,弹窗也会是全屏。主题文件也没办法。
        val styleTheme: Int//布局居中

        //与Activity是否有状态栏无关。Dialog可以自己控制自己是否显示状态栏。即使Acitivy全屏，Dilog也可以有自己的状态栏。亲测
        if (isStatus && Build.VERSION.SDK_INT >= 19) {//是否有状态栏
            if (isTransparent) {//背景是否透明
                styleTheme = R.style.KTheme_Dialog_hasStatus_transparent
            } else {
                styleTheme = R.style.KTheme_Dialog_hasStatus
            }
        } else {
            if (isTransparent) {
                styleTheme = R.style.KTheme_Dialog_full_transparent
            } else {
                styleTheme = R.style.KTheme_Dialog_full
            }
        }
        //fixme Dialog主题，必须在构造函数中传入才有效，其他地方设置主题无效。只有构造函数中才有效。
        dialog = object : Dialog(ctx!!, styleTheme) {
            override fun onAttachedToWindow() {
                super.onAttachedToWindow()
                //附加到窗口,每次显示的时候都会调用
                createBlur()//创建高斯模糊
                onShow()
                onShow?.let {
                    it()
                }//在listener的后面调用
            }

            override fun onDetachedFromWindow() {
                super.onDetachedFromWindow()
                onDismiss?.let {
                    it()
                }//在recycleView的前面调用
                //释放高斯模糊图片
                recyclerBlur()
                //从当前窗口移除。每次dismiss的时候都会调用
                onDismiss()
            }
        }
        var window = dialog!!.window
        if (Build.VERSION.SDK_INT < 19) {
            //低版本显示窗体，必须在window.setContentView之前调用一次。其后就可随便调show()了。
            //高版本可在window.setContentView()之后调用。
            if (dialog != null) {
                ctx?.let {
                    if (it is Activity && !it.isFinishing) {
                        dialog!!.show()
                    }
                }
            }
        }
        //true(按其他区域会消失),按返回键还起作用。false(按对话框以外的地方不起作用,即不会消失)【现在的布局文件，基本都是全屏的。这个属性设置已经没有意义了。触屏消失，需要自己手动去实现。】
        //都以左上角为标准对齐。没有外区，全部都是Dialog区域。已经确保百分百全屏。所以这个方法已经没有意义
        //dialog.setCanceledOnTouchOutside(true);// 调用这个方法时，按对话框以外的地方不起作用。按返回键还起作用
        window = dialog!!.window
        //彻底解决乐视黑屏问题
        window!!.setFormat(PixelFormat.RGBA_8888)
        window.setBackgroundDrawable(null)
        //window.setContentView之前设置
        //window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏,无效。全屏必须到style主题文件里设置才有效
        window!!.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)//状态栏透明,4.4及以上有效
        window.decorView.fitsSystemWindows = false
        //fixme =====================================================================================布局
        if (layoutId != null && layoutId > 0) {
            window.setContentView(layoutId)// xml布局
        } else {
            ctx?.apply {
                onCreateView(this)?.let {
                    window.setContentView(it)//一般为anko布局
                }
            }
        }
        //fixme 为Window设置动画【Dialog和PopuWindow动画都是Style文件】
        window.setWindowAnimations(R.style.kera_window_samll_large2)//这个放大的动画效果，还不错。
        //popupWindow.setAnimationStyle(R.style.CustomDialog)
        //如果是windowIsFloating为false。则以左上角为标准。居中无效。并且触摸外区也不会消失。因为没有外区。整个屏幕都是Dialog区域。
        window.setGravity(Gravity.CENTER)//居中。
        //设置状态栏背景透明【亲测有效】
        try {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)   //去除半透明状态栏
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  //一般配合fitsSystemWindows()使用, 或者在根部局加上属性android:fitsSystemWindows="true", 使根部局全屏显示
            val decordView = window.decorView as ViewPager.DecorView     //获取DecorView实例
            val field = ViewPager.DecorView::class.java.getDeclaredField("mSemiTransparentStatusBarColor")  //获取特定的成员变量
            field.isAccessible = true   //设置对此属性的可访问性
            field.setInt(decordView, Color.TRANSPARENT)  //修改属性值
        } catch (e: Exception) {
            //Log.e("test", "状态栏异常:\t" + e.getMessage());
        }
        if (Build.VERSION.SDK_INT >= 21) {
            window.statusBarColor = Color.TRANSPARENT
        }
        //默认设置底部导航栏为白色。
        setNavigationBarColor(Color.WHITE)
        //Activity关闭回调
        try {
            if (ctx != null && ctx is KBaseActivity) {
                ctx?.addFinishCallBack {
                    onDestroy()//fixme Activity关闭之前要弹窗进行销毁（不然会弹框会异常，关闭了就不会了。）
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        //==========================================================================================结束
        initEvent()//初始化事件
    }

    //获取xml文件最外层控件。
    fun getParentView(window: Window?): ViewGroup? {
        window?.let {
            val decorView = it.decorView//布局里面的最顶层控件，本质上是FrameLayout(帧布局)，FrameLayout.LayoutParams
            val contentView = decorView?.findViewById<View>(android.R.id.content) as ViewGroup//我们的布局文件。就放在contentView里面。contentView本质上也是FrameLayout(帧布局)，FrameLayout.LayoutParams
            val parent = contentView?.getChildAt(0)//这就是我们xml布局文件最外层的那个父容器控件。
            if (parent != null) {
                return parent as ViewGroup
            }
        }
        return null
    }

    //返回Dimen Int类型
    fun getDimensionPixelSize(id: Int): Int {
        var dimen = dialog?.window?.decorView?.resources?.getDimensionPixelSize(id)
        if (dimen != null) {
            return dimen
        } else {
            return 0
        }
    }

    //返回Dimen Float类型
    fun getDimension(id: Int): Float {
        var dimen = dialog?.window?.decorView?.resources?.getDimension(id)
        if (dimen != null) {
            return dimen
        } else {
            return 0f
        }
    }

    //fixme 高斯模糊（毛玻璃背景效果）;默认是空，需要自己手动初始化。
    var blur: KBlur? = null//fixme 默认不开启，节省内存。

    fun blur(block: KBlur.() -> Unit): KBaseDialog {
        if (blur == null) {
            blur = KBlur()
        }
        block(blur!!)
        return this
    }

    private var currentActivityBitmap: Bitmap? = null//Activity高斯模糊图片
    //创建高斯模糊（在弹窗显示的时候会调用）
    private fun createBlur() {
        blur?.let {
            try {
                dialog?.window?.let {
                    //弹窗最外层布局
                    getParentView(it)?.apply {
                        if (blur!!.isBlur) {
                            //开启毛玻璃效果
                            currentActivityBitmap = KBaseApplication.getInstance().getCurrentActivityBitmap(ctx)
                            if (currentActivityBitmap != null && !currentActivityBitmap!!.isRecycled) {
                                currentActivityBitmap = KBitmapUtils.blurBitmap(ctx, currentActivityBitmap, blur!!.level, true)
                                if (blur!!.coverColor != null && blur!!.coverColor != Color.TRANSPARENT) {
                                    currentActivityBitmap = KBitmapUtils.coverColorForBitmap(currentActivityBitmap, blur!!.coverColor!!)
                                }
                                currentActivityBitmap?.let {
                                    if (!it.isRecycled) {
                                        ctx?.runOnUiThread {
                                            if (Build.VERSION.SDK_INT >= 16) {
                                                background = BitmapDrawable(currentActivityBitmap)
                                            } else {
                                                backgroundDrawable = BitmapDrawable(currentActivityBitmap)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            ctx?.runOnUiThread {
                                backgroundDrawable = null
                            }
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                KLoggerUtils.e("弹窗毛玻璃异常:\t" + e.message)
            }
        }
    }

    //释放高斯模糊（在弹窗消失的时候会调用。）
    private fun recyclerBlur() {
        currentActivityBitmap?.recycle()
        currentActivityBitmap = null
    }

    /**
     * fixme 判断弹框是否显示
     */
    fun isShow(): Boolean {
        if (dialog != null) {
            return dialog!!.isShowing
        }
        return false
    }

    //事件，视图UI初始化完成之后，会调用（初始化只调用一次）。，都会调用。子类重写
    //在initUi()末尾里调用了。
    open protected fun initEvent() {}

    //事件，弹窗每次显示时，都会调用。子类重写
    open protected fun onShow() {}

    private var onShow: (() -> Unit)? = null
    //显示监听。调用着监听
    fun onShow(onShow: (() -> Unit)?): KBaseDialog {
        this.onShow = onShow
        return this
    }

    //事件，弹窗每次消失时，都会调用。子类重写
    open protected fun onDismiss() {
    }

    var onDismiss: (() -> Unit)? = null
    //消失监听。调用者监听
    fun onDismiss(onDismiss: (() -> Unit)?): KBaseDialog {
        this.onDismiss = onDismiss
        return this
    }

    //防止内存泄漏
    //在activity结束时记得手动调用一次
    fun recycles() {
        dialog?.dismiss()
        dialog?.cancel()
        dialog = null
        System.gc()
    }


    //获取控件
    fun <T : View?> findViewById(id: Int): T? {
        if (dialog != null && ctx != null) {
            try {
                return dialog?.findViewById<T?>(id)
            } catch (e: Exception) {
                e.printStackTrace()
                KLoggerUtils.e("dialog控件获取异常：\t" + e.message)
            }
        }
        return null
    }

    //关闭弹窗
    open fun dismiss() {
        if (dialog != null && ctx != null) {
            dialog?.let {
                if (it.isShowing) {
                    //关闭
                    try {
                        ctx?.runOnUiThread {
                            try {
                                if (ctx != null && this is Activity) {
                                    if (!isFinishing) {
                                        dialog?.let {
                                            if (it.isShowing) {
                                                it.dismiss()//关闭
                                            }
                                        }
                                        System.gc()//垃圾内存回收
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                KLoggerUtils.e("dialog关闭异常：\t" + e.message)
                            }
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    //显示窗体
    open fun show() {
        if (dialog != null && ctx != null) {
            try {
                ctx?.runOnUiThread {
                    try {
                        if (ctx != null && this is Activity) {
                            if (!isFinishing) {
                                if (dialog != null && !dialog!!.isShowing) {
                                    //显示窗体，必须在window.setContentView之前调用一次。其后就可随便调show()了。
                                    dialog?.show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        KLoggerUtils.e("dialog显示异常：\t" + e.message)
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    // 两次点击按钮之间的点击间隔不能少于1000毫秒（即1秒）
    var MIN_CLICK_DELAY_TIME = 1000
    var lastClickTime: Long = System.currentTimeMillis()//记录最后一次点击时间

    //判断是否快速点击，true是快速点击，false不是
    open fun isFastClick(): Boolean {
        var flag = false
        var curClickTime = System.currentTimeMillis()
        if ((curClickTime - lastClickTime) <= MIN_CLICK_DELAY_TIME) {
            flag = true//快速点击
        }
        lastClickTime = curClickTime
        return flag
    }

    //fixme 自定义点击事件，可以添加多个点击事情。互不影响
    open fun onClick(view: View?, onClick: () -> Unit) {
        //点击事件
        view?.setOnClickListener {
            //fixme 防止快速点击
            if (!isFastClick()) {
                onClick()//点击事件
            }
        }
    }

    open fun getColor(id: Int): Int? {
        return ctx?.getResources()?.getColor(id)
    }

    /**
     * 获取颜色值
     */
    open fun getColorFromResources(id: Int): Int? {
        return ctx?.getResources()?.getColor(id)
    }

    //默认就从Res目录下读取
    open fun getString(id: Int, formatArgs: String? = null): String {
        return getStringFromResources(id, formatArgs)
    }

    /**
     * 获取String文件里的字符,<string name="names">你好%s</string>//%s 是占位符,位置随意
     * @param formatArgs 是占位符
     */
    open fun getStringFromResources(id: Int, formatArgs: String? = null): String {
        if (formatArgs != null) {
            return ctx?.resources?.getString(id, formatArgs) as String
        }
        return ctx?.resources?.getString(id) as String
    }

    /**
     * 获取String文件里的字符串數組
     */
    open fun getStringArrayFromResources(id: Int): Array<String>? {
        return ctx?.resources?.getStringArray(id)
    }

    /**
     * fixme 隐藏软键盘Dialog(onShow())里调用。Dialog就不会在一开始显示的时候自动弹出弹框。
     */
    open fun setSoftInputMode_adjustpan(window: Window? = dialog?.window) {
        //正常，不会挤压屏幕（默认），在这里手动设置了，弹框显示时，键盘输入框不会自动弹出,并且文本同时还具备光标(亲测)。
        //fixme 对Activity，Dialog都有效。(在Activity(onResume())和Dialog(onShow())显示的时候调用有效。)
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        //dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)//fixme 隐藏软键盘Dialog(onShow())里调用。Dialog就不会在一开始显示的时候自动弹出弹框。
    }

//        scrollView {
//        isFillViewport = true//子控件充满scrollView
//        setVerticalScrollBarEnabled(false);//实现滚动条隐藏.
//        verticalLayout {  }
//    }

    /**
     * fixme 最外层布局必须是scrollView（必须，不然部分设备无效）;设置了以下模式，软键盘不会挤压屏幕（会覆盖在布局上）。SOFT_INPUT_STATE_UNSPECIFIED
     * fixme inputHeightListener()仍然可以获取软键盘高度。不挤压屏幕，依旧可以获取软键盘的高度。dialog弹窗也有效。
     */
    open fun setSoftInputMode2_unspecified(window: Window? = dialog?.window) {
        //fixme 可能会自动弹出软键盘；解决方案，在文本输入框的父容器中，加入以下聚焦代码即可。
        //isFocusable=true
        //isFocusableInTouchMode=true
        //requestFocus()
        //requestFocusFromTouch()
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED)
    }

    /**
     * fixme 软键盘不会挤压屏幕（会覆盖在布局上）。SOFT_INPUT_ADJUST_NOTHING亲测有效
     * fixme 这个完全不挤压屏幕，也无法获取软键盘的高度。软键盘高度始终获取为0
     * fixme 对Dialog设置好像无效，依旧会挤压布局，不会覆盖。
     */
    open fun setSoftInputMode3_adjustnothing(window: Window? = dialog?.window) {
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)//fixme 不会自动弹出软键盘，最外层布局没有要求，什么布局都有效。
    }

    companion object {
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
    }

}

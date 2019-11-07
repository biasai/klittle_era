package cn.oi.klittle.era.base

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.FragmentActivity
import android.transition.Explode
import android.transition.Fade
import android.transition.Slide
import android.transition.Transition
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import cn.oi.klittle.era.R
import cn.oi.klittle.era.activity.photo.entity.KLocalMedia
import cn.oi.klittle.era.activity.photo.manager.KPictureSelector
import cn.oi.klittle.era.comm.KToast
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.dialog.KProgressDialog
import cn.oi.klittle.era.dialog.KTimiAlertDialog
import cn.oi.klittle.era.helper.KUiHelper
import cn.oi.klittle.era.utils.*
import cn.oi.klittle.era.widget.viewpager.KViewPager
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import org.jetbrains.anko.act
import java.util.concurrent.TimeUnit


/**
 * Created by 彭治铭 on 2018/6/24.
 * fixme 注意Frament和Activity的状态栏和导航栏样式是一致的，引用的是同一个Window
 * fixme Dialog是独立的Window。样式和Activity不一样。
 * fixme Activity，Fragment，Dialgo都分别添加了状态栏和导航栏的控制方法。
 * 不要基础AppCompatActivity（屁事太大，对主题Theme.AppCompat有要求。）
 */
open class KBaseActivity : FragmentActivity() {
    open fun getActivity(): KBaseActivity {
        return this
    }

    //直接调用KBaseUi里的静态方法
    fun ui(block: KBaseUi.Companion.() -> Unit): KBaseActivity {
        KBaseUi.apply {
            block(this)
        }
        return this
    }

    //触摸点击效果。isRipple是否具备波浪效果
    open fun onPress(view: View?, isRipple: Boolean = true) {
        KBaseView.onPress(view, isRipple)
    }

    //右边滑动的阴影效果。子类可以自定义效果。
    open fun shadowSlidingDrawable(): Int {
        return R.drawable.kera_drawable_left_shadow
    }

    //右边滑动的有效位置。子类可以自定义效果。
    open fun shadowSlidingWidth(): Int {
        return -1
    }

    //右边滑动的有效位置(垂直)。子类可以自定义效果。
    open fun shadowSlidingHeight(): Int {
        return kpx.y(200)
    }

    //右边滑动反弹的距离。
    open fun shadowSlidingReboundWidth(): Int {
        return -1
    }

    //是否开启左滑移除效果。交给子类重写。true开启，false不开启。
    open fun isEnableSliding(): Boolean {
        return false//fixme 默认不开启（节省内存）
    }

    //fixme 是否开启滑动，新增方法方便进行手动控制。(亲测有效)
    fun setEnableSliding(isEnableSliding: Boolean) {
        if (isEnableSliding()) {
            slideLayout?.isEnableSliding = isEnableSliding
        }
    }


    open fun isPortrait(): Boolean {
        return true//是否竖屏。默认就是竖屏。
    }

    //fixme 是否进行切屏(横屏和竖屏的转换,只在onCreate()里面做了判断)
    open fun isOrientation(): Boolean {
        return true//fixme 是否固定竖屏或横屏方向。true会固定方向。false就不会(竖屏，横屏无效)。
    }

    //true 竖屏，false横屏
    open fun orientation(isPortrait: Boolean) {
        try {
            if (!(Build.VERSION.SDK_INT == 26 && getApplicationInfo().targetSdkVersion >= 26)) {
                if (isPortrait) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)//竖屏
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)//横屏
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * fixme 获取Activity视图contentView的真实高度。
     */
    open fun contentViewHeight(delay: Long = 150, callBack: ((height: Int) -> Unit)?) {
        kpx.contentViewHeight(this, delay, callBack)
    }

    /**
     * fixme 获取屏幕最大高度
     */
    open fun maxScreenHeight(): Int {
        return kpx.maxScreenHeight()
    }

    private var isEnableSliding = false//判断左滑是否已开启，防止重复执行。
    private var slideLayout: KBaseSlideLayout? = null
    override fun onAttachedToWindow() {
        try {
            super.onAttachedToWindow()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        try {
            //fixme 之前放在onCreate()方法里，在部分机型，如华为畅享6s就会奔溃。
            //fixme 可能是因为actvity没有初始化完成的原因吧。所以为了保险。放在这里执行。
            if (!isFinishing() && isEnableSliding() && !isEnableSliding) {
                //开启左滑移除效果
                if (slideLayout == null) {
                    slideLayout = KBaseSlideLayout(this, shadowSlidingDrawable())
                }
                slideLayout?.setShadowSlidingWidth(shadowSlidingWidth())//有效滑动距离
                slideLayout?.setShadowSlidingHeight(shadowSlidingHeight())//有效滑动距离(垂直)
                slideLayout?.setShadowSlidingReboundWidth(shadowSlidingReboundWidth())//滑动反弹距离。
                slideLayout?.bindActivity(this)
                isEnableSliding = true//左滑已开启。
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    private var isOnCreateSuper = false//防止Activity还没执行oncreate就突然的挂掉（系统有这个Bug）
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            try {
                //fixme 防止按home键返回之后，Activity重新加载的问题。
                if (intent != null && intent.action != null && !this.isTaskRoot) {
                    if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intent.action == Intent.ACTION_MAIN) {
                        finish()
                        return
                    }
                }
                kpx.removeAllKey()//fixme 清除所有键值，防止图片加载不出来。
                super.onCreate(savedInstanceState)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            isOnCreateSuper = true//是否执行了onCreate的super方法。
            //fixme 在8.0(api 26)系统的时候，Actvity透明和锁屏（横屏或竖屏）只能存在一个。这个Bug，8.1已经修复了。
            //fixme 这个Bug在 targetSdkVersion >= 27时，且系统是8.0才会出现 Only fullscreen activities can request orientation
            //这个情况会崩溃，不能横竖屏。是系统Bug
            if (isOrientation()) {
                orientation(isPortrait())
            }
            if (intent != null) {
                //fixme 这个也是个Bug，Kotlin里面调用了startActivityForResult(),返回时是不允许为空的。不然直接异常奔溃。
                //所以在此先setResult(),放心会被后面新的setResult()覆盖的。
                var intent = intent
                setResult(-44, intent)
            }
        } catch (e: Exception) {
            KLoggerUtils.e("test", "系统框架脑抽筋:\t" + e.message)
        }

        // 将当前Activity添加到栈中
        KBaseActivityManager.getInstance().pushActivity(this)
        requestWindowFeature(Window.FEATURE_NO_TITLE)//无标题栏(setContentView()之前才有效)
        //设置状态栏透明
        KBaseApplication.getInstance().setStatusBarTransparent(window)
        //默认设置底部导航栏为白色（底部为白色，虚拟键会自动为深色（浅黑色））
        //setNavigationBarColor(Color.WHITE)//不要设置，最好采用系统默认的样式。因为虚拟键的颜色无法控制。
    }

    //取代的是onCreate()
    override fun onRestart() {
        try {
            super.onRestart()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //在onCreate()或onRestart()后面执行。
    override fun onStart() {
        try {
            //KLoggerUtils.e("onStart()")
            super.onStart()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        try {
            isOnPause = false
            super.onResume()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        try {
            isBack = false
            //设置状态栏字体颜色
            setStatusBarDrak(isDark())
            //fixme 开启左滑功能
            if (isEnableSliding()) {
                //获取上一个Activity的界面位图
                async {
                    delay(350, TimeUnit.MILLISECONDS)//延迟几秒获取，确保获取的界面尽可能是最新的。立即获取界面可能不是最新的。
                    if (!isFinishing()) {
                        KBaseApplication.getInstance().recyclePreviousBitmap()
                        //开启位图视觉差效果(现在主题不推荐使用透明主题，所以必须开启绘制位图。)
                        KBaseApplication.getInstance().getPreviousBitmap(this@KBaseActivity)
                    }
                }
            } else {
                //未开启左滑功能，销毁位图。
                async {
                    KBaseApplication.getInstance().recyclePreviousBitmap()
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    open var isOnPause = false//界面是否暂停

    override fun onPause() {
        try {
            isOnPause = true
            super.onPause()
            //onAnime()//fixme 转场动画；就在onCreate()和onFinish()里两个方法里调用即可。
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        try {
            super.onStop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //fixme 判断activity是否已经返回
    var isBack: Boolean = false

    //监听返回键
    override fun onBackPressed() {
        try {
            if (System.currentTimeMillis() - exitTime2 > exitIntervalTime) {
                exitTime2 = System.currentTimeMillis()
                //返回键第一次按下监听
                onBackPressed1?.let {
                    it()
                }
            } else {
                //第二次按下监听
                onBackPressed2?.let {
                    it()
                }
            }
            if (isExit()) {
                if (System.currentTimeMillis() - exitTime > exitIntervalTime) {
                    onShowExit()
                    exitTime = System.currentTimeMillis()
                } else {
                    finish()
                    KBaseActivityManager.getInstance().finishAllActivity()
                    KBaseApplication.getInstance().exit()//退出应用（杀进程）
                }
            } else if (onBackPressed1 == null && onBackPressed2 == null) {
                isBack = true
                try {
                    super.onBackPressed()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    var isCurrentDark = KBaseApplication.getInstance().isDeaultDark//fixme 记录当前状态栏字体的颜色

    //true 状态栏字体颜色为 黑色，false 状态栏字体颜色为白色。子类可以重写
    protected open fun isDark(): Boolean {
        return isCurrentDark
    }

    //设置状态栏字体颜色,true黑色（深色），false白色（浅色）
    open fun setStatusBarDrak(isDark: Boolean) {
        window?.let {
            isCurrentDark = isDark//记录当前状态栏字体的颜色
            KBaseApplication.getInstance().setStatusBarDrak(window, isCurrentDark)
        }
    }

    //fixme 状态栏的颜色，默认已经设置成透明了。和背景一个色。不需要管了。真的不需要管了。
    // fixme (如果要重新设置；必须放在super.onCreate(savedInstanceState)的后面才有效。因为前面默认设置成了透明)

    //设置状态栏颜色
    open fun setStatusBarColor(color: Int = Color.TRANSPARENT) {
        //对版本号21 即5.0及以上才有效。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().statusBarColor = color
        }
    }

    //设置状态栏颜色；不要设置默认参数；因为上面Int类型已经设置默认参数了。防止方法无法区分冲突。
    open fun setStatusBarColor(color: String) {
        //对版本号21 即5.0及以上才有效。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().statusBarColor = Color.parseColor(color)
        }
    }

    //fixme 测试发现，目前全屏状态下，不包括底部导航栏。（即全屏模式，状态栏会消失，导航栏不会消失。正如：屏幕高度包含状态栏高度，但不包含底部导航栏高度。）

    /**
     * fixme 設置底部导航栏颜色。（即虚拟按键的位置）,底部导航默认一般都是白色的。
     * fixme 如果为透明色。就会看到桌面的背景。Activity的界面不会和底部导航融栏为一体。
     * fixme 测试发现，只有当底部导航栏设置为纯白色时，虚拟键才会是黑色。其他颜色。模拟键默认都是白色。
     */
    open fun setNavigationBarColor(color: Int = Color.TRANSPARENT) {
        //对版本号21 即5.0及以上才有效。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(color);
        }
    }

    open fun setNavigationBarColor(color: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Color.parseColor(color));
        }
    }

    /**
     * fixme Activity界面和底部导航栏融为一体（全屏也有效）。底部导航也在Activiry界面内。
     * fixme 虽然融为一体，但是事件还是虚拟键优先级最高。
     */
    open fun setNavigationBarTransparent() {
        // 透明导航栏，屏幕的底部[部分手机完全透明，部分手机可能半透明。]
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    }

    /**
     * fixme 設置当前Activity的亮度（其他Activity界面不受影響，只對當前Activity有效。）
     * brightness是一个0.0-1.0之间的一个float类型数值
     */
    open fun setBrightness(brightness: Float) {
        var window = act.getWindow()
        var lp: WindowManager.LayoutParams = window.getAttributes()
        if (brightness > 1) {
            lp.screenBrightness = 1f//最亮
        } else if (brightness < 0) {
            lp.screenBrightness = 0f//最暗
        } else {
            lp.screenBrightness = brightness
        }
        window.setAttributes(lp)
    }

    /**
     * fixme 亮度恢復到默认亮度。
     */
    open fun setBrightnessDefault() {
        var window = act.getWindow()
        var lp: WindowManager.LayoutParams = window.getAttributes()
        lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE//这个值是  -1.0F，-1是默认亮度。
        window.setAttributes(lp)
    }

    /**
     * 獲取當前Activity的亮度。-1是默認亮度。
     */
    open fun getBrightness(): Float {
        var window = act.getWindow()
        var lp: WindowManager.LayoutParams = window.getAttributes()
        return lp.screenBrightness
    }

    //true 程序按两次退出。false正常按键操作。[子类可以重写]
    protected open fun isExit(): Boolean {
        return false//默认不监听返回键，不退出
    }

    private var exitTime: Long = 0
    private var exitTime2: Long = 0
    var exitIntervalTime: Long = 2000//结束间隔时间
    //open var exitInfo = "再按一次退出"//退出提示信息[子类可以重写]
    open fun getExitInfo(): String {
        return getString(R.string.kexitInfo)//"别点了，再点我就要走了"
    }

    //退出时，提示语句。子类可重写。
    open fun onShowExit() {
        KToast.show(getExitInfo())
    }

    private var onBackPressed1: (() -> Unit)? = null
    //返回返回键第一次按下监听
    fun onBackPressed1(onBackPressed1: (() -> Unit)?) {
        this.onBackPressed1 = onBackPressed1
    }

    private var onBackPressed2: (() -> Unit)? = null
    //返回返回键第二次按下监听
    fun onBackPressed2(onBackPressed2: (() -> Unit)?) {
        this.onBackPressed2 = onBackPressed2
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

    //系统权限申请回调
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        try {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            KPermissionUtils.onRequestPermissionsResult(getActivity(), requestCode, permissions, grantResults)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //fixme onActivityResult在onResume()的前面执行。
    //fixme 不要在这里关闭，这里关闭自己定义的动画效果可能无效。(在onResume()中关闭就有activity动画效果)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            super.onActivityResult(requestCode, resultCode, data)
            KPictureUtils.onActivityResult(this, requestCode, resultCode, data)
            if (requestCode == KIntentUtils.BLUTOOTH_REQUEST_ENABLE_BT) {
                if (resultCode == RESULT_OK) {
                    KIntentUtils.BluetoothOpenCallback?.let {
                        it(true)//蓝牙打开成功
                    }
                } else {
                    KIntentUtils.BluetoothOpenCallback?.let {
                        it(false)//蓝牙打开失败
                    }
                }
                KIntentUtils.BluetoothOpenCallback = null//制空，防止重复回调。
            } else if (requestCode == KIntentUtils.BLUTOOTH_REQUESTCODE_DISCOVER) {
                //KLoggerUtils.e("resultCode:\t" + resultCode)
                if (resultCode == RESULT_CANCELED) {
                    KIntentUtils.BluetoothDiscoverCallback?.let {
                        it(false)//蓝牙可见失败
                    }
                } else {
                    KIntentUtils.BluetoothDiscoverCallback?.let {
                        it(true)//蓝牙可见(允许的resultCode应该是120)
                    }
                }
                KIntentUtils.BluetoothDiscoverCallback = null//制空，防止重复回调。
            } else if (requestCode == KPermissionUtils.requestCode_CanDrawOverlays) {
                //fixme 悬浮窗权限
                if (Build.VERSION.SDK_INT >= 23) {
                    KPermissionUtils.onRequestPermissionsResult(Settings.canDrawOverlays(getApplicationContext()))
                }
            } else if (resultCode === Activity.RESULT_OK) {
                when (requestCode) {
                    PictureConfig.CHOOSE_REQUEST -> {
                        // 图片选择结果回调
                        mSelectList = PictureSelector.obtainMultipleResult(data)?.toMutableList()
                        var pathes = ArrayList<String>()
                        mSelectList?.forEach {
                            var path = it.path//原图路径
                            if (it.isCut) {
                                path = it.cutPath//剪切路径
                            }
                            //fixme 逻辑：先剪切；最后再压缩。压缩是在原图或者剪切图的基础上最后进行的压缩。
                            if (it.isCompressed) {
                                path = it.compressPath//压缩后路径；最后进行。
                            }
                            pathes.add(path)
                        }
                        if (pathes.size > 0 && mSelectList != null) {
                            mSelectListCallback?.let {
                                it(pathes)//回调图片路径集合
                            }
                            mSelectListCallback = null
                            mSelectListCallback2?.let {
                                it(mSelectList!!)//回调MutableList集合
                            }
                            mSelectListCallback2 = null
                        }
                    }
                }
            }
            this.mSelectListCallback = null
            this.mSelectListCallback2 = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private var mSelectList: MutableList<LocalMedia>? = null
    private var mSelectListCallback: ((path: ArrayList<String>) -> Unit)? = null
    private var mSelectListCallback2: ((mSelectList: MutableList<LocalMedia>) -> Unit)? = null
    /**
     * 图片选择器
     * @param maxSelectNum 图片选择最大张数；不要设置默认参数，防止和子类方法冲突
     * @param imageSpanCount 图片选择器列表每行的个数
     * @param callback 图片回调(返回图片路径)，不要设置默认参数，防止和子类方法冲突。
     */
    open fun pictrueSelectorForPath(maxSelectNum: Int = 1, imageSpanCount: Int = 4, medias: List<LocalMedia>? = mSelectList, isCompress: Boolean = true, enableCrop: Boolean = false, aspect_ratio_x: Int = 1, aspect_ratio_y: Int = 1, callback: ((path: ArrayList<String>) -> Unit)?) {
        this.mSelectListCallback = callback
        this.mSelectListCallback2 = null
        PictureSelector.create(act)
                .openGallery(PictureMimeType.ofImage()).imageSpanCount(imageSpanCount).maxSelectNum(maxSelectNum)
                .compress(isCompress)//是否压缩；true压缩;fixme 测试发现 宽和高直接压一半。
                .enableCrop(enableCrop)//是否裁剪
                .withAspectRatio(aspect_ratio_x, aspect_ratio_y)//裁剪比例（宽高比例）
                .selectionMedia(medias)//选中默认的
                .minimumCompressSize(100)// 小于100kb的图片不压缩
                .forResult(PictureConfig.CHOOSE_REQUEST)
    }

    /**
     * fixme PictureSelector图片选择器默认主题都是AppTheme；如果裁剪报主题错误，
     * fixme 在AppTheme(主moudle里的同名主题会覆盖类库Library)里添加如下两行即可：
     * fixme <item name="windowActionBar">false</item>
     * fixme <item name="windowNoTitle">true</item>
     */

    /**
     * 图片选择器
     * @param maxSelectNum 图片选择最大张数；
     * @param imageSpanCount 图片选择器列表每行的个数
     * @param callback 图片回调(返回LocalMedia对象集合)，
     */
    open fun pictrueSelectorForLocalMedia(maxSelectNum: Int = 1, imageSpanCount: Int = 4, medias: List<LocalMedia>? = mSelectList, isCompress: Boolean = true, enableCrop: Boolean = false, aspect_ratio_x: Int = 1, aspect_ratio_y: Int = 1, callback: ((medias: List<LocalMedia>) -> Unit)?) {
        this.mSelectListCallback = null
        this.mSelectListCallback2 = callback
        PictureSelector.create(act)
                .openGallery(PictureMimeType.ofImage()).imageSpanCount(imageSpanCount).maxSelectNum(maxSelectNum)
                .compress(isCompress)//是否压缩；true压缩;fixme 测试发现 宽和高直接压一半。
                .enableCrop(enableCrop)//是否裁剪
                .withAspectRatio(aspect_ratio_x, aspect_ratio_y)//裁剪比例（宽高比例）
                .selectionMedia(medias)//选中默认的
                .minimumCompressSize(100)// 小于100kb的图片不压缩
                .forResult(PictureConfig.CHOOSE_REQUEST)
    }

    /**
     * 图片预览
     * @param position 当前预览图片下标
     * @param medias 预览图片集合
     */
    open fun pictruePreview(position: Int = 0, medias: List<LocalMedia>? = mSelectList) {
        medias?.let {
            if (it.lastIndex >= position) {
                PictureSelector.create(act).themeStyle(R.style.picture_default_style).openExternalPreview(position, medias);
            }
        }
    }

    /**
     * fixme 自己的图片选择器
     */

    /**
     * 图片选择器
     * @param selectionMedia 选中数据
     * @param maxSelectNum 图片选择最大张数；
     * @param imageSpanCount 图片选择器列表每行的个数
     * @param selectCallback 图片选中回调(返回以及选中的KLocalMedia对象集合)
     */
    open fun pictrueSelectorForLocalMedia2(selectionMedia: MutableList<KLocalMedia>? = KPictureSelector.selectionMedia, type: Int = PictureConfig.TYPE_IMAGE, maxSelectNum: Int = 1, imageSpanCount: Int = 3, isCompress: Boolean = true, selectCallback: ((selectDatas: MutableList<KLocalMedia>) -> Unit)? = null) {
        KPictureSelector.type(type).imageSpanCount(imageSpanCount).maxSelectNum(maxSelectNum).isCompress(isCompress).selectionMedia(selectionMedia).minimumCompressSize(100).forResult(this) {
            var data = it
            selectCallback?.let { it(data) }
        }
    }

    /**
     * 图片选择器
     * @param maxSelectNum 图片选择最大张数；不要设置默认参数，防止和子类方法冲突
     * @param imageSpanCount 图片选择器列表每行的个数
     * @param selectDatas 图片回调(返回图片路径)，不要设置默认参数，防止和子类方法冲突。
     */
    open fun pictrueSelectorForPath2(selectionMedia: MutableList<KLocalMedia>? = KPictureSelector.selectionMedia, type: Int = PictureConfig.TYPE_IMAGE, maxSelectNum: Int = 1, imageSpanCount: Int = 3, isCompress: Boolean = true, selectCallback: ((path: ArrayList<String>) -> Unit)? = null) {
        KPictureSelector.type(type).imageSpanCount(imageSpanCount).maxSelectNum(maxSelectNum).isCompress(isCompress).selectionMedia(selectionMedia).minimumCompressSize(100).forResult(this) {
            var pathes = ArrayList<String>()
            it?.forEach {
                var path = it.path//原图路径
                if (it.isCut) {
                    path = it.cutPath//剪切路径
                }
                //fixme 逻辑：先剪切；最后再压缩。压缩是在原图或者剪切图的基础上最后进行的压缩。
                if (it.isCompressed) {
                    path = it.compressPath//压缩后路径；最后进行。
                }
                path?.let {
                    pathes.add(it)
                }
            }
            if (pathes.size > 0) {
                selectCallback?.let { it(pathes) }
            }
        }
    }

    /**
     * 图片预览
     * @param position 当前预览图片下标
     * @param medias 预览图片集合
     */
    open fun pictruePreview2(position: Int = 0, medias: MutableList<KLocalMedia>? = KPictureSelector.selectionMedia) {
        medias?.let {
            if (it.lastIndex >= position) {
                KPictureSelector.openExternalPreview(this, position, it, isCheckable = false)
            }
        }
    }

    /**
     * 在Activity应用<meta-data>元素。
     */
    open fun getMetaDataFromActivity(key: String): String {
        val info = this.packageManager
                .getActivityInfo(componentName,
                        PackageManager.GET_META_DATA)
        val msg = info.metaData.getString(key)
        return msg
    }

    /**
     * 在application应用<meta-data>元素。
     */
    open fun getMetaDataFromApplication(key: String): String {
        val appInfo = this.packageManager
                .getApplicationInfo(packageName,
                        PackageManager.GET_META_DATA)

        return appInfo.metaData.getString(key)
    }

    /**
     * getColor()这个方法系统已经有了，不能再重载
     * 获取颜色值（默认从Resources目录，从color文件中获取）
     */
    open fun getColorFromResources(id: Int): Int {
        return getResources().getColor(id)
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
            //fixme 不要去除空格；空格也占位，有时也需要也很重要的。
            return this.resources.getString(id, formatArgs) as String
        }
        return this.resources.getString(id) as String
    }

    /**
     * 获取String文件里的字符串數組
     */
    open fun getStringArrayFromResources(id: Int): Array<String> {
        return resources.getStringArray(id)
    }

    open fun getBundle(): Bundle? {
        intent?.let {
            it.extras?.let {
                return it
            }
        }
        return null
    }

    open fun getStringFromBundle(key: String, defaultValue: String? = null): String? {
        var value = getBundle()?.getString(key, null)
        if (value == null && defaultValue != null) {
            value = defaultValue
        }
        return value
    }

    open fun startActivity(clazz: Class<*>) {
        KUiHelper.goActivity(clazz)
    }

    open fun startActivity(clazz: Class<*>, bundle: Bundle) {
        KUiHelper.goActivity(clazz, bundle, act)
    }

    open fun goActivity(clazz: Class<*>) {
        KUiHelper.goActivity(clazz, act)
    }

    open fun goActivity(clazz: Class<*>, bundle: Bundle) {
        KUiHelper.goActivity(clazz, bundle, act)
    }

    open fun goActivity(intent: Intent) {
        KUiHelper.goActivity(intent, act)
    }

    open fun goActivityForResult(clazz: Class<*>) {
        KUiHelper.goActivityForResult(clazz, act)
    }

    open fun goActivityForResult(clazz: Class<*>, bundle: Bundle) {
        KUiHelper.goActivityForResult(clazz, bundle, act)
    }

    open fun goActivityForResult(intent: Intent) {
        KUiHelper.goActivityForResult(intent, act)
    }

    open fun goActivityForResult(clazz: Class<*>, requestCode: Int) {
        KUiHelper.goActivityForResult(clazz, act, requestCode)
    }

    open fun goActivityForResult(clazz: Class<*>, bundle: Bundle, requestCode: Int) {
        KUiHelper.goActivityForResult(clazz, bundle, act, requestCode)
    }

    open fun goActivityForResult(intent: Intent, requestCode: Int) {
        KUiHelper.goActivityForResult(intent, act, requestCode)
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

        open fun setSoftInputMode(window: Window? = KBaseUi.getActivity()?.window) {
            //正常，不会挤压屏幕（默认），在这里手动设置了，弹框显示时，键盘输入框不会自动弹出,并且文本同时还具备光标(亲测)。
            //fixme 对Activity，Dialog都有效。
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        }

    }

    private var kTimi: KTimiAlertDialog? = null
    /**
     * 显示弹窗信息
     */
    open fun showMsg(mession: String?): KTimiAlertDialog? {
        try {
            if (!isOnPause && !isFinishing && mession != null && mession?.trim().length > 0) {
                //判断界面是否暂停；防止页面跳转或已经消失了，调用。
                if (kTimi == null) {
                    kTimi = KTimiAlertDialog(this)
                }
                kTimi?.apply {
                    mession(mession?.trim())
                    show()
                }
                return kTimi
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    var kprogressbar: KProgressDialog? = null
    /**
     * 显示进度条
     */
    open fun showProgressbar() {
        if (kprogressbar == null) {
            kprogressbar = KProgressDialog(this)
        }
        kprogressbar?.show()
    }

    /**
     * 关闭进度条
     */
    open fun shutProgressbar() {
        kprogressbar?.dismiss()
    }

    //fixme Activity关闭的时候一定会调用，返回键也会调用该方法。
    override fun finish() {
        //KLoggerUtils.e("finish()")
        try {
            if (isOnCreateSuper) {//防止Activity还没开始就突然的挂掉。这是个系统的Bug
                isBack = true
                try {
                    super.finish()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                //fixme 将当前Activity移除栈(以防万一，多次移除，没关系。)
                KBaseActivityManager.getInstance().finishActivity(this)
                kTimi?.dismiss()
                kTimi?.onDestroy()
                kTimi = null
                mSelectListCallback = null
                mSelectListCallback2 = null
                kprogressbar?.dismiss()
                kprogressbar?.onDestroy()
                kprogressbar = null
                slideLayout = null
                //fixme 退出动画，在finish()之后调用有效，多次调用也有效，后面的会覆盖前面的。
                //fixme 参数一  上一个Activity的动画效果，参数二当前Activity的动画效果。
                //目前动画，左进，右出。
                //overridePendingTransition是传统动画，5.0的转场动画效果不怎么好。不建议使用
                overridePendingTransition(R.anim.kera_slide_in_left, R.anim.kera_slide_out_right)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //Activity关闭时，不一定会执行。
    override fun onDestroy() {
        try {
            //将当前Activity移除栈
            KBaseActivityManager.getInstance().finishActivity(this)
            kTimi?.dismiss()
            kTimi?.onDestroy()
            kTimi = null
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        try {
            super.onDestroy()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

}
package cn.oi.klittle.era.base

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import cn.oi.klittle.era.helper.KUiHelper
import cn.oi.klittle.era.utils.KAssetsUtils
import org.jetbrains.anko.act
import org.jetbrains.anko.support.v4.act
import java.lang.Exception

/**
 * 继承本Fragment，主构造函数传入一个布局id或者一个View即可(一般都是frameLayout控件)。然后就可以像Activity一样使用了。
 * Activity中加载说明：supportFragmentManager.beginTransaction().replace(px.id("frameLayoutID"),Myfragment()).commit()即可;已经集成到KBaseActivity里面去了。replace()方法。
 * Fragment中最好使用：childFragmentManager.beginTransaction()；fixme 也已经集成到KBaseFragment里面去了，replace()方法。 Fragment里面还包含Fragment。
 * Created by 彭治铭 on 2018/4/20.
 */
abstract open class KBaseFragment(var layout: Int = 0, var content: View? = null) : Fragment() {

    //触摸点击效果。isRipple是否具备波浪效果
    open fun onPress(view: View?, isRipple: Boolean = true) {
        KBaseView.onPress(view, isRipple)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (layout <= 0) {
            content?.let {
                return it
            }
            content = onCreateView()//子类可以直接重写onCreateView来创建View
            content?.let {
                return it
            }
            return super.onCreateView(inflater, container, savedInstanceState)
        } else {
            //获取xml布局
            if (content == null) {
                content = inflater.inflate(layout, container, false)
            }
            return content
        }
    }

    //fixme 如果传入的布局和view都为空。则可重写以下方法,一般都是重写的该方法。
    open fun onCreateView(): View? {
        //return UI { }.view//使用Anko布局
        return null
    }

    //通过ID获取控件
    fun <T> findViewById(id: Int): T? {
        try {
            var view = content?.findViewById<View>(id)
            return view as? T
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun onResume() {
        super.onResume()
        setStatusBarDrak(isDark())
    }

    var isCurrentDark = KBaseApplication.getInstance().isDeaultDark//fixme 记录当前状态栏字体的颜色

    //true 状态栏字体颜色为 黑色，false 状态栏字体颜色为白色。子类可以重写
    protected open fun isDark(): Boolean {
        return isCurrentDark
    }

    //设置状态栏字体颜色,true黑色（深色），false白色（浅色）
    open fun setStatusBarDrak(isDark: Boolean) {
        activity?.window?.let {
            isCurrentDark = isDark//记录当前状态栏字体的颜色
            KBaseApplication.getInstance().setStatusBarDrak(it, isCurrentDark)
        }
    }

    open fun setNavigationBarColor(color: Int) {
        //对版本号21 即5.0及以上才有效。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity?.getWindow()?.setNavigationBarColor(color);
        }
    }

    open fun setNavigationBarColor(color: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity?.getWindow()?.setNavigationBarColor(Color.parseColor(color));
        }
    }

    /**
     * 界面和底部导航栏融为一体,親測有效。
     */
    open fun setNavigationBarTransparent() {
        // 透明导航栏，屏幕的底部[部分手机完全透明，部分手机可能半透明。]
        activity?.getWindow()?.addFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
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

    /**
     * fixme 切换Fragment
     * @param id 控件id(一般都是frameLayout控件;用来装载Fragment)
     * @param fragment 要切换的Fragment
     */
    open fun replace(id: Int, fragment: Fragment) {
        childFragmentManager.beginTransaction().replace(id, fragment).commit()//即可
    }

    /**
     * 在Activity应用<meta-data>元素。
     */
    open fun getMetaDataFromActivity(key: String): String {
        val info = act.packageManager
                .getActivityInfo(act.componentName,
                        PackageManager.GET_META_DATA)
        val msg = info.metaData.getString(key)
        return msg
    }

    /**
     * 在application应用<meta-data>元素。
     */
    open fun getMetaDataFromApplication(key: String): String {
        val appInfo = act.packageManager
                .getApplicationInfo(act.packageName,
                        PackageManager.GET_META_DATA)

        return appInfo.metaData.getString(key)
    }

    open fun getColor(id: Int): Int {
        return getResources().getColor(id)
    }

    /**
     * 获取颜色值
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
        act.intent?.let {
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
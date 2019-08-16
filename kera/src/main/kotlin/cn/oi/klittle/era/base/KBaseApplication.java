package cn.oi.klittle.era.base;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import cn.oi.klittle.era.utils.KAppUtils;
import cn.oi.klittle.era.utils.KAssetsUtils;
import cn.oi.klittle.era.utils.KCacheUtils;
import cn.oi.klittle.era.utils.KLoggerUtils;
import cn.oi.klittle.era.utils.KProportionUtils;


/**
 * Created by  彭治铭 on 2017/9/10.
 */
//必须在AndroidManifest.xml中application指明
//<application android:name=".base.BaseApplication">
//配置文件声明之后，才会调用onCreate()等什么周期。
// fixme Application就用java写，之前用kotlin写。结果发生错误。找不到上下文。
public class KBaseApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //解决方法数过多问题；
        MultiDex.install(this);
    }

    private static KBaseApplication sInstance;

    public Bitmap previousBitmap = null;//上一个Activity的位图，用于实现左滑视觉差效果

    /**
     * 释放上一个Activity的位图
     */
    public void recyclePreviousBitmap() {
        if (KBaseApplication.getInstance().previousBitmap != null && !KBaseApplication.getInstance().previousBitmap.isRecycled()) {
            KBaseApplication.getInstance().previousBitmap.recycle();
            KBaseApplication.getInstance().previousBitmap = null;
        }
    }

    /**
     * 获取上一个Activity的位图
     *
     * @param activity
     * @return
     */
    public Bitmap getPreviousBitmap(Activity activity) {
        previousBitmap = KBaseApplication.getInstance().snapShotWindow(KBaseActivityManager.getInstance().getStackPreviousActivity(activity));
        return previousBitmap;
    }

    /**
     * 获取当前Activity的位图
     */
    public Bitmap getCurrentActivityBitmap(Activity activity) {
        if (activity == null) {
            activity = KBaseActivityManager.getInstance().getStackTopActivity();//获取当前Activity
        }
        return KBaseApplication.getInstance().snapShotWindow(activity);
    }

    public Bitmap getCurrentActivityBitmap(Context context) {
        if (context == null) {
            context = KBaseActivityManager.getInstance().getStackTopActivity();//获取当前Activity
        }
        return KBaseApplication.getInstance().snapShotWindow(context);
    }

    public Bitmap getCurrentActivityBitmap() {
        return KBaseApplication.getInstance().snapShotWindow(KBaseActivityManager.getInstance().getStackTopActivity());
    }

    //通过反射获取ActivityThread【隐藏类】
    private static Object getActivityThread() {
        try {
            final Class<?> clz = Class.forName("android.app.ActivityThread");
            final Method method = clz.getDeclaredMethod("currentActivityThread");
            method.setAccessible(true);
            final Object activityThread = method.invoke(null);
            return activityThread;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //初始化
    public static KBaseApplication getInstance() {
        if (sInstance == null) {
            //如果配置文件没有声明，也没有手动初始化。则通过反射自动初始化。【反射是最后的手段，效率不高】
            //通过反射，手动获取上下文。
            final Object activityThread = getActivityThread();
            if (null != activityThread) {
                try {
                    final Method getApplication = activityThread.getClass().getDeclaredMethod("getApplication");
                    getApplication.setAccessible(true);
                    Context applicationContext = (Context) getApplication.invoke(activityThread);
                    setsInstance(applicationContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return sInstance;
    }

    //如果没有在配置文件中配置，则需要手动调用以下方法，手动初始化BaseApplication
    //不会调用onCreate()等什么周期
    //BaseApplication.setsInstance(getApplication());
    public static void setsInstance(Context application) {
        if (sInstance == null) {
            sInstance = new KBaseApplication();
            //统一上下文
            sInstance.attachBaseContext(application);
            closeAndroidPDialog();
        }
    }

    @Override
    public void onCreate() {
        try {
            // TODO Auto-generated method stub
            super.onCreate();
            sInstance = this;
            closeAndroidPDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //判断是否第一次启动，true 首次启动，false不是
    public boolean isFirstStart() {
        return KAppUtils.isFirstStart(this);
    }

    /**
     * 获取app自身的应用名称
     *
     * @return
     */
    public String getAppName() {
        return KAppUtils.getAppName();
    }

    //当前应用的位图图标(亲测可行)
    public Bitmap getAppIconBp() {
        return KAppUtils.getAppIconBp(this);
    }

    //当前应用的图标(亲测可行)
    public Integer getAppIconRes() {
        return KAppUtils.getAppIconRes(this);
    }

    //当前应用的版本名称
    public String getVersionName() {
        return KAppUtils.getVersionName(this);
    }

    //当前应用的版本号
    public int getVersionCode() {
        return KAppUtils.getVersionCode(this);
    }

    /**
     * 获取targetSdkVersion版本。
     */
    public int getTargetSdkVersion() {
        return getApplicationInfo().targetSdkVersion;
    }

    //获取当前应用包名,getPackageName()是自带的方法。
    //fixme 主要，无论是Activity还是Context。getPackageName()返回的都是当前应用的包名。(是应用的包名，不是自己所在类的包名)
    //KBaseApplication.getInstance().getPackageName()

    //获取SDK的版本号，23是6.0  21是5.0   14是4.0
    public static int getSDK_INT() {
        return Build.VERSION.SDK_INT;
    }

    //获取SDK系统版本发布名称，如: 8.1.0
    public static String getSDK_NAME() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取设备品牌，如 xiaomi , HUAWEI
     *
     * @return
     */
    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    /**
     * 获取设备具体名称，如：Xiaomi Redmi Note 5
     *
     * @return
     */
    public static String getDeviceName() {
        return KAppUtils.getDeviceName();
    }

    //退出程序
    public void exit() {
        try {
            KBaseActivityManager.getInstance().finishAllActivity();
            android.os.Process.killProcess(android.os.Process.myPid());//杀进程
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("test", "退出异常:\t" + e.getMessage());
        }
    }

    //跳转到桌面
    public void goHome() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//防止报错
        startActivity(intent);
    }


    //键值【作用范围应用全局】
    private Map _objectContainer = new HashMap();

    //存储键值
    public void put(Object key, Object value) {
        _objectContainer.put(key, value);
    }

    //获取键值
    public Object get(Object key) {
        return _objectContainer.get(key);
    }

    //移出键值
    public void remove(Object key) {
        _objectContainer.remove(key);
    }

    //清楚所有键值
    public void clear() {
        _objectContainer.clear();
    }

    /**
     * 获取当前屏幕截图
     *
     * @param activity
     * @param hasStatus true 保护状态栏，false不包含状态栏
     * @return
     */
    public static Bitmap snapShotWindow(Activity activity, Boolean hasStatus) {
        View view = activity.getWindow().getDecorView();//最顶层控件就是DecorView
        view.setDrawingCacheEnabled(true);//==========================重点掌握
        view.buildDrawingCache();//=================================
        Bitmap bmp = view.getDrawingCache();//========================
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);//获取View的矩形
        int statusBarHeight = frame.top;

        int width = (int) KProportionUtils.realWidthPixels;
        int height = (int) KProportionUtils.realHeightPixels;
        Bitmap bp = null;
        if (hasStatus) {
            //包含状态栏
            bp = Bitmap.createBitmap(bmp, 0, 0, width, height);
        } else {
            //不包含状态栏
            bp = Bitmap.createBitmap(bmp, 0, statusBarHeight, width, height
                    - statusBarHeight);//对原 Bitmap进行截取，一定要新建Bitmap位图，尽量不要对原有的Bitmap进行操作。
        }
        view.destroyDrawingCache();//=================================要关闭。很好性能。
        view.setDrawingCacheEnabled(false);
        return bp;

    }

    public static Bitmap snapShotWindow(Context context) {
        if (context instanceof Activity) {
            return snapShotWindow((Activity) context);
        }
        return null;
    }

    /**
     * 获取当前屏幕位图（全屏，不包含状态栏）
     *
     * @param activity
     * @return
     */
    public static Bitmap snapShotWindow(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return null;
        }
        View decorView = activity.getWindow().getDecorView();
        decorView.destroyDrawingCache();
        decorView.setDrawingCacheEnabled(false);
        if (activity.getParent() != null &&
                activity.getParent().getWindow().getDecorView().getHeight() > decorView.getHeight()) {
            decorView = activity.getParent().getWindow().getDecorView();
            decorView.destroyDrawingCache();
            decorView.setDrawingCacheEnabled(false);
        }
        if (decorView.getRootView() != null) {
            decorView = decorView.getRootView();
            decorView.destroyDrawingCache();
            decorView.setDrawingCacheEnabled(false);
        }
        if (decorView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) decorView;
            destroyDrawingCache(viewGroup);//fixme 清除位图缓存,获取实时位图！（亲测有效。）
        }
        return snapShotView(decorView);
    }

    //fixme 清除位图缓存,获取实时位图！（亲测有效。）
    public static void destroyDrawingCache(ViewGroup viewGroup) {
        try {
            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = viewGroup.getChildAt(i);
                if (view != null) {
                    view.destroyDrawingCache();//fixme 循环对每一个view都清除缓存。这样才能实时获取最新的视图。(自定义View同样有效。)
                    view.setDrawingCacheEnabled(false);
                    if (view instanceof ViewGroup) {
                        ViewGroup viewGroup2 = (ViewGroup) view;
                        destroyDrawingCache(viewGroup2);
                    }
                }
            }
        } catch (Exception e) {
        }

    }

    //获取当前View截图
    public static Bitmap snapShotView(View view) {
        view.destroyDrawingCache();//fixme 清除一下缓存；这样才能实时获取最新的视图。
        //KLoggerUtils.e("TEST", "获取上一个位图:\t"+previousBitmap);
        view.setDrawingCacheEnabled(true);//==========================重点掌握
        Bitmap bitmap = view.getDrawingCache();
        int width = view.getWidth() > view.getLayoutParams().width ? view.getWidth() : view.getLayoutParams().width;
        int height = view.getHeight() > view.getLayoutParams().height ? view.getHeight() : view.getLayoutParams().height;
        Bitmap bp = Bitmap.createBitmap(bitmap, 0, 0, width, height);//对原 Bitmap进行截取，一定要新建Bitmap位图，尽量不要对原有的Bitmap进行操作。
        view.destroyDrawingCache();//=================================要关闭。很好性能。fixme 这一步就已经对位图bitmap进行释放了。
        //Log.e("test","是否销毁：\t"+bitmap.isRecycled());
        view.setDrawingCacheEnabled(false);
        //Bitmap bp = Bitmap.createBitmap(view.getWidth() , view.getHeight(), Bitmap.Config.ARGB_8888);
        //Canvas c = new Canvas(bp);
        //view.draw(c);
        return bp;
    }

    /**
     * 设置状态栏透明
     *
     * @param window getWindow()
     */
    public void setStatusBarTransparent(Window window) {
        if (Build.VERSION.SDK_INT < 19) {//4.4
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏,有效。因为4.4以下状态栏透明设置无效，奇丑无比，所以设置全屏。
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//状态栏背景透明(和应用背景一样4.4及以上才有效,测试真机，亲测有效)
        //设置状态栏背景透明【亲测有效】
        try {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);   //去除半透明状态栏
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);  //一般配合fitsSystemWindows()使用, 或者在根部局加上属性android:fitsSystemWindows="true", 使根部局全屏显示
            if (Build.VERSION.SDK_INT >= 21) {
                window.setStatusBarColor(Color.TRANSPARENT);
            }
            ViewPager.DecorView decordView = (ViewPager.DecorView) window.getDecorView();     //获取DecorView实例
            Field field = ViewPager.DecorView.class.getDeclaredField("mSemiTransparentStatusBarColor");  //获取特定的成员变量
            field.setAccessible(true);   //设置对此属性的可访问性
            field.setInt(decordView, Color.TRANSPARENT);  //修改属性值
        } catch (Exception e) {
            //Log.e("test", "状态栏透明设置异常:\t" + e.getMessage());
        }
    }

    //fixme 状态栏是否为黑色,true黑色，false白色(浅色)（系统默认是白色浅色）
    public boolean isDeaultDark = false;//fixme 在此默认设置和系统一样，默认就是白色。

    //这个是全局引用，决定了全局状态栏默认字体的颜色
    public void isDark(boolean isDark) {
        this.isDeaultDark = isDark;
    }

    //调用案例：BaseApplication.getInstance().setStatusBarDrak(activity?.window, isDark())

    /**
     * 设置状态栏字体颜色
     *
     * @param window
     * @param isDark
     */
    //一般需要在 super.onCreate(savedInstanceState); 方法之后，调用才有效。
    //放到BaseApplication里面。方便全局调用。Activity和Dailog都可以调用
    //状态栏字体颜色，true 黑色。false 白色【一般默认就是白色,所以白色一般不需要調用】。
    // 如果要設置黑色狀態欄字體，在子類中調用setStatusBarDrak(true);即可。setContentView();之前之后調用都可以,最好在之前調用。
    public void setStatusBarDrak(Window window, boolean isDark) {
        setAndroidStatusBark(window, isDark);
        setMiuiStatusBarDark(window, isDark);
        setFlyMeStatusBarDark(window, isDark);
    }

    //对于android6.0及以上（不是所有的都可以，部分可能無效。）,但是小米魅族不适配
    //测试发现，只对android api 24及以上才有效。即真实只对7.0及以上才有效。
    //字体颜色，true 黑色。false 白色。
    //亲测，对android 7.0及以上有效，api 24
    private void setAndroidStatusBark(Window window, boolean isDark) {
        if (Build.VERSION.SDK_INT >= 19) {//19是4.4
            if (isDark) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//深色，一般为黑色
            } else {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);//浅色，一般为白色
            }
        }
    }

    //修改小米状态栏字体颜色【只针对miui6以上有效】。true 黑色。false 白色。
    // 亲测，对小米有效
    private boolean setMiuiStatusBarDark(Window window, boolean isDark) {
        Class<? extends Window> clazz = window.getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(window, isDark ? darkModeFlag : 0, darkModeFlag);
            return true;
        } catch (Exception e) {
            //Log.e("test", "Miui状态栏字体颜色修改失败:\t" + e.getMessage());
        }
        return false;
    }

    //改变魅族的状态栏字体为黑色，要求FlyMe4以上,true 黑色。false 白色。
    //亲测，对魅族有效。
    private void setFlyMeStatusBarDark(Window window, boolean isDark) {
        WindowManager.LayoutParams lp = window.getAttributes();
        try {
            Class<?> instance = Class.forName("android.view.WindowManager$LayoutParams");
            int value = instance.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON").getInt(lp);
            Field field = instance.getDeclaredField("meizuFlags");
            field.setAccessible(true);
            int origin = field.getInt(lp);
            if (isDark) {
                field.set(lp, origin | value);
            } else {
                field.set(lp, (~value) & origin);
            }
        } catch (Exception e) {
            //Log.e("test", "魅族状态栏字体颜色修改失败:\t" + e.getMessage());
        }
    }

    /**
     * 去掉在Android P上的提醒弹窗 （Detected problems with API compatibility(visit g.co/dev/appcompat for more info)
     */
    private static void closeAndroidPDialog() {
        try {
            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

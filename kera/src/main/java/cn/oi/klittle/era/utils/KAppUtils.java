package cn.oi.klittle.era.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.oi.klittle.era.R;
import cn.oi.klittle.era.base.KBaseApplication;
import cn.oi.klittle.era.base.KBaseCallBack;
import cn.oi.klittle.era.base.KBaseUi;
import cn.oi.klittle.era.comm.KToast;


/**
 * 启动应用和卸载应用 使用： 直接实例化一个对象，调用里面的方法即可。
 *
 * @author 彭治铭
 */
public class KAppUtils {

    //判断是否第一次启动，true 首次启动，false不是
    public static boolean isFirstStart(Context context) {
        String key = "versionCode" + getVersionCode(context);//确保每个版本都唯一
        SharedPreferences preferences = context.getSharedPreferences(
                "application", 0);// 0是默认模式
        Boolean bool = preferences.getBoolean(key, true);
        if (bool) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(key, false);
            editor.commit();
        }
        return bool;
    }

    //当前应用的位图图标(亲测可行)
    public static Bitmap getAppIconBp(Context context) {
//        try {
//            PackageManager manager = context.getPackageManager();
//            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
//            Drawable drawable = info.applicationInfo.loadIcon(manager);
//            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
//            return bitmapDrawable.getBitmap();
//        } catch (Exception e) {
//            KLoggerUtils.e("test", "获取应用图标位图异常:\t" + e.getMessage());
//        }
//        return null;
        //以上方法可以获取应用图标，使用以下方式获取。便于位图重复利用。
        return KAssetsUtils.getInstance().getBitmapFromResource(getAppIconRes(context), false);
    }

    //当前应用的图标(亲测可行)
    public static Integer getAppIconRes(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            Drawable drawable = info.applicationInfo.loadIcon(manager);
            return info.applicationInfo.icon;
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("test", "获取应用图标资源异常:\t" + e.getMessage());
        }
        return null;
    }

    //当前应用的版本名称
    public static String getVersionName() {
        return getVersionName(KBaseApplication.getInstance());
    }

    //当前应用的版本名称
    public static String getVersionName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("test", "获取应用版本号异常:\t" + e.getMessage());
        }
        return null;
    }

    //当前应用的版本号
    public static int getVersionCode() {
        return getVersionCode(KBaseApplication.getInstance());
    }

    //当前应用的版本号
    public static int getVersionCode(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("test", "获取应用版本号异常:\t" + e.getMessage());
        }
        return 1;
    }

//        defaultConfig {
//        targetSdkVersion 23
//        }

    /**
     * 获取targetSdkVersion版本。
     */
    public static int getTargetSdkVersion(Context context) {
        return context.getApplicationInfo().targetSdkVersion;
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
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }


    /**
     * 根据时间关闭应用
     *
     * @param year  年
     * @param month 月
     */
    public static void shutApp(final int year, final int month) {
        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                removeMessages(0);
                String yyyy = KCalendarUtils.INSTANCE.getCurrentTime("yyyy");
                int y = Integer.valueOf(yyyy);
                if (y == year) {
                    String MM = KCalendarUtils.INSTANCE.getCurrentTime("MM");
                    int M = Integer.valueOf(MM);
                    //Log.e("test", "M:\t" + M);
                    if (M > month) {
                        //ToastUtils.showToastView("异常:\tcom.android.internal.policy.DecorView");
                        KBaseApplication.getInstance().exit();
                    }
                }
                if (y > year) {
                    //ToastUtils.showToastView("异常:\tcom.android.internal.policy.DecorView");
                    KBaseApplication.getInstance().exit();
                }

            }
        };
        handler.sendEmptyMessageDelayed(0, 10000);//10秒发送
    }

    /**
     * 启动应用
     *
     * @param packageName 启动应用包名
     * @return true 启动成功，false失败
     */
    public static boolean startApp(Context context, String packageName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            //intent为空的原因
            //1：没有安装该应用
            //2：安装了该应用，但是没有配置 <category android:name="android.intent.category.LAUNCHER"/> ；所以也就没有启动项。
            if (intent != null) {
                context.startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            Log.e("test", "App应用启动失败失败:\t" + e.getMessage());
        }
        return false;//如果intent为空，还可以通过包名，跳转到该应用的详情页，让用户手动打开。
    }

    /**
     * 卸载应用【估计需要系统权限】
     *
     * @param apply 包名
     */
    public static void uninstallApp(Context context, String apply) throws Exception {
        try {
            Uri packageURI = Uri.parse("package:" + apply);// xx是包名
            Intent intent = new Intent(Intent.ACTION_DELETE, packageURI);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("test", "App卸载失败:\t" + e.getMessage());
        }
    }

    /**
     * 安装应用(兼容7.0版本),如果要安装assets里的apk，必须先把apk复制到存储卡。然后再安装。（无法直接安装assets里的app,必须复制出来）
     * fixme 安装应用时，需要安装未知来源的权限（这个权限不需要手动申请，安装的时候会自动提示的。也无法动态申请。放心安装时系统会自动提示的。）
     *
     * @param apk app的完整路径
     */
    public static void installation(Context context, File apk) {
        try {
            if (apk == null && context != null) {
                return;
            }
            String path = apk.getAbsolutePath();
            if (isAppComplete(context, path)) {//判斷apk安裝包是否完整
                /* apk安装界面跳转 */
                //fixme 这个直接跳转到安装界面；需要安装未知应用权限（这个权限，安装的时候，系统会自动去申请，不需要手动申请，亲测！）
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri fileUri = Uri.fromFile(apk);
                if (Build.VERSION.SDK_INT >= 23) {//7.0及以上版本(版本号24),为了兼容6.0(版本号23)，防止6.0也可能会有这个问题。
                    //getPackageName()和${applicationId}显示的都是当前应用的包名。无论是在library还是moudle中，都是一样的。都显示的是当前应用moudle的。与类库无关。请放心使用。
                    fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".kera.provider",//与 android:authorities="${applicationId}.kera.provider"对应上
                            apk);
                }
                intent.setDataAndType(fileUri,
                        "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_STREAM, fileUri);//必不可少
                try {
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    KLoggerUtils.INSTANCE.e("安装异常：\t" + e.getMessage());
                }
            } else {
                //Toast.makeText(context, "安装包解析错误", Toast.LENGTH_LONG).show();
                String msg = KBaseUi.Companion.getString(R.string.kapkFailr);//安装包解析错误
                KToast.INSTANCE.showError(msg, null, null);
                //KLoggerUtils.INSTANCE.e("安装包大小(下载完成)：\t" + apk.length() + "\t路径：\t" + apk.getAbsolutePath());
                //安装包大小：	150	路径：	/storage/emulated/0/Android/data/com.example.myapplication/cache/down/201912171237498776.apk
                //fixme 删除安装包(错误的安装包一定要删除;错误的原因是下载不完整导致的，或者网络下载链接获取网络文件实际大小错误。)
                KFileUtils.getInstance().delFile(apk.getAbsolutePath(), null, null);
            }
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("App安装失败:\t" + e.getMessage());
        }
    }

    /**
     * 安装Assets里的apk文件(亲测可行)
     *
     * @param assetsPath assets 里的文件。如("文件夹/文件名.后缀")
     */
    public static void installationFromAssets(String assetsPath) {
        //fixme 第一步 将Apk复制到SD卡上（这一步速度很快就几秒）
        KAssetsUtils.getInstance().copyFileFromAssets(assetsPath, KFileLoadUtils.getInstance().cacheDir, new KBaseCallBack() {
            @Override
            public void onResult(Object o) {
                if (o != null && o instanceof File) {
                    //fixme 第二步，安装SD卡上的apk文件。
                    installation(KBaseApplication.getInstance(), (File) o);
                }
            }
        });
    }

    /**
     * 方法一 判断apk是否安装
     *
     * @param uri apk的包名
     * @return
     */
    public static boolean isAppInstalled(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    /**
     * 方法二 判断apk是否安装
     *
     * @param packageName apk的包名
     * @return
     */
    public static boolean isAppInstalleds(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        List<String> pName = new ArrayList<String>();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return pName.contains(packageName);
    }


    /**
     * fixme 获取apK包名
     *
     * @param apk 安装包的完整路径
     * @return
     */
    public static String getPackageName(Context context, String apk) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apk,
                PackageManager.GET_ACTIVITIES);
        ApplicationInfo appInfo = null;
        if (info != null) {
            appInfo = info.applicationInfo;
            return appInfo.packageName;
        }
        return null;
    }

    /**
     * 判断apk安装包是否完整
     *
     * @param filePath
     * @return
     */
    public static boolean isAppComplete(Context context, String filePath) {
        boolean result = false;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(filePath,
                    PackageManager.GET_ACTIVITIES);
            String packageName = null;
            if (info != null) {
                result = true;
            }
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    //获取当前应用包名,getPackageName()是自带的方法。
    //fixme 主要，无论是Activity还是Context。getPackageName()返回的都是当前应用的包名。(是应用的包名，不是自己所在类的包名)
    public String getPackageName() {
        return KBaseApplication.getInstance().getPackageName();
    }

    /**
     * 获取application中指定的meta-data (渠道号)，如(写在清单里，与Activity一样)<meta-data android:name="PUSH_APPID" android:value="1Y1Zt2hJmV5rHGdLRg0Ya" />
     *
     * @return 如果没有获取成功(没有对应值 ， 或者异常)，则返回值为空
     */
    public static String getAppMetaData(Context context, String key) {
        if (context == null || TextUtils.isEmpty(key)) {
            return null;
        }
        String resultData = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager
                        .getApplicationInfo(context.getPackageName(),
                                PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        resultData = applicationInfo.metaData.getString(key);
                    }
                }

            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return resultData;
    }

    /**
     * 查询手机内系统应用(不需要,小米手机能够正确获取。部分手机就不行。如华为荣耀6就不行。)
     *
     * @param context
     * @return
     */
    public static List<PackageInfo> getAllSysApps(Context context) {
        List<PackageInfo> apps = new ArrayList<PackageInfo>();
        PackageManager pManager = context.getPackageManager();
        //获取手机内所有应用
        List<PackageInfo> paklist = pManager.getInstalledPackages(0);
        for (int i = 0; i < paklist.size(); i++) {
            PackageInfo pak = (PackageInfo) paklist.get(i);
            //判断系统预装的应用程序
            if (!((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) <= 0)) {
                apps.add(pak);
                //Log.e("test", "包名:\t" + pak.packageName + "\t应用名:\t" + pManager.getApplicationLabel(pak.applicationInfo).toString());
            }
        }
        return apps;
    }

    /**
     * 獲取非系统应用
     *
     * @param context
     * @return
     */
    public static List<PackageInfo> getAllNotSysApps(Context context) {
        List<PackageInfo> apps = new ArrayList<PackageInfo>();
        PackageManager pManager = context.getPackageManager();
        //获取手机内所有应用
        List<PackageInfo> paklist = pManager.getInstalledPackages(0);
        for (int i = 0; i < paklist.size(); i++) {
            PackageInfo pak = (PackageInfo) paklist.get(i);
            //判断系统预装的应用程序
            if (!((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) <= 0)) {
                //系统应用
                //apps.add(pak);
                //Log.e("test", "包名:\t" + pak.packageName + "\t应用名:\t" + pManager.getApplicationLabel(pak.applicationInfo).toString());
            } else {
                //非系统应用
                apps.add(pak);
            }
        }
        return apps;
    }

    /**
     * 获取所有应用包名
     *
     * @param context
     * @return
     */
    public static List<PackageInfo> getAllApps(Context context) {
        List<PackageInfo> apps = new ArrayList<PackageInfo>();
        PackageManager pManager = context.getPackageManager();
        //获取手机内所有应用
        List<PackageInfo> paklist = pManager.getInstalledPackages(0);
        for (int i = 0; i < paklist.size(); i++) {
            PackageInfo pak = (PackageInfo) paklist.get(i);
            apps.add(pak);
            //pak.applicationInfo.name fixme 這個無法獲取應用名稱，获得的是空
            //Log.e("test", "包名:\t" + pak.packageName + "\t应用名:\t" + pManager.getApplicationLabel(pak.applicationInfo).toString());
        }
        return apps;
    }

    /**
     * 根据包名，获取启动的Intent
     *
     * @param context
     * @param pak
     * @return
     */
    public static Intent getLaunchIntentForPackage(Context context, PackageInfo pak) {
        if (context != null && pak != null) {
            return context.getPackageManager().getLaunchIntentForPackage(pak.packageName);
        }
        return null;
    }

    /**
     * 根据包名，获取启动的Intent
     *
     * @param context
     * @param packageName
     * @return
     */
    public static Intent getLaunchIntentForPackage(Context context, String packageName) {
        if (context != null && packageName != null) {
            return context.getPackageManager().getLaunchIntentForPackage(packageName);
        }
        return null;
    }

    /**
     * 获取应用名称
     *
     * @param context
     * @param pak
     * @return
     */
    public static String getAppName(Context context, PackageInfo pak) {
        return context.getPackageManager().getApplicationLabel(pak.applicationInfo).toString();
    }

    /**
     * 获取应用本身的名称;fixme 注意，因为读取的不是string.xml文件；所以该方法。不支持多语言。
     *
     * @return
     */
    public static String getAppName() {
        Context context = KBaseApplication.getInstance();
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = context.getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        String applicationName =
                (String) packageManager.getApplicationLabel(applicationInfo);
        return applicationName;
    }

    /**
     * 获取系统相册包名
     *
     * @param context
     * @return
     */
    public static String getGalleryPackName(Context context) {
        String packagename = null;
        PackageManager pManager = context.getPackageManager();
        List<PackageInfo> appList = getAllSysApps(context);
        for (int i = 0; i < appList.size(); i++) {
            PackageInfo pinfo = appList.get(i);
            String name = pManager.getApplicationLabel(pinfo.applicationInfo).toString().trim();
            if (("画廊").equals(name) || ("图库").equals(name) || ("图片库").equals(name) || ("图册").equals(name) || ("相册").equals(name) || ("相薄").equals(name) || ("相片").equals(name) || ("照片").equals(name) || ("图片").equals(name) || ("美图").equals(name)) {
                packagename = pinfo.packageName;
                //Log.e("test", "系统相册:\t" + pinfo.packageName + "\tname:\t" + name);
                break;
            }
            if (("畫廊").equals(name) || ("圖庫").equals(name) || ("圖片庫").equals(name) || ("圖冊").equals(name) || ("相簿").equals(name) || ("圖片").equals(name) || ("美圖").equals(name)) {
                packagename = pinfo.packageName;
                //Log.e("test", "系统相册:\t" + pinfo.packageName + "\tname:\t" + name);
                break;
            }
            if (("Gallery").equals(name) || ("Photo").equals(name) || ("Picture").equals(name)) {
                packagename = pinfo.packageName;
                //Log.e("test", "系统相册:\t" + pinfo.packageName + "\tname:\t" + name);
                break;
            }
            if (("gallery").equals(name.toLowerCase()) || ("photo").equals(name.toLowerCase()) || ("picture").equals(name.toLowerCase())) {
                packagename = pinfo.packageName;
                //Log.e("test", "系统相册:\t" + pinfo.packageName + "\tname:\t" + name);
                break;
            }
        }
        return packagename;
    }

    /**
     * 获取系统相机包名
     *
     * @param context
     * @return
     */
    public static String getCameraPackName(Context context) {
        String packagename = null;
        try {
            PackageManager pManager = context.getPackageManager();
            List<PackageInfo> appList = getAllSysApps(context);
            for (int i = 0; i < appList.size(); i++) {
                PackageInfo pinfo = appList.get(i);
                String name = pManager.getApplicationLabel(pinfo.applicationInfo).toString().trim();
                //Log.e("test","相机名：\t"+name);
                if (("相机").equals(name) || ("相機").equals(name) || ("照相机").equals(name) || ("照相機").equals(name) || ("照片机").equals(name) || ("Camera").equals(name) || ("camera").equals(name.toLowerCase())) {
                    packagename = pinfo.packageName;
                    //Log.e("test", "系统相册:\t" + pinfo.packageName + "\tname:\t" + name);
                    break;
                }
                if (("照相馆").equals(name) || ("相馆").equals(name) || ("照相館").equals(name) || ("相館").equals(name)) {
                    packagename = pinfo.packageName;
                    //Log.e("test", "系统相册:\t" + pinfo.packageName + "\tname:\t" + name);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packagename;
    }

}

package cn.oi.klittle.era.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.net.URI;

import cn.oi.klittle.era.BuildConfig;

/**
 * 权限设置界面
 */
public class KIntentPersionSettingUtils {

    public static void goPermisssion(Context context) {
        goPermisssion(context, null);
    }

    public static void goPermisssion(Context context, String requestCode) {
        //String sdk = android.os.Build.VERSION.SDK; // SDK号
        //String model = android.os.Build.MODEL; // 手机型号
        //String release = android.os.Build.VERSION.RELEASE; // android系统版本号

        String brand = Build.BRAND.trim().toLowerCase();//手机厂商
        if (TextUtils.equals(brand.toLowerCase(), "redmi") || TextUtils.equals(brand.toLowerCase(), "xiaomi")) {
            gotoMiuiPermission(context, requestCode);//小米
        } else if (TextUtils.equals(brand.toLowerCase(), "meizu")) {
            gotoMeizuPermission(context, requestCode);//魅族
        } else if (TextUtils.equals(brand.toLowerCase(), "huawei") || TextUtils.equals(brand.toLowerCase(), "honor")) {
            gotoHuaweiPermission(context, requestCode);//华为
        } else {
            if (requestCode != null && requestCode.trim().length() > 0 && context instanceof Activity) {
                ((Activity) context).startActivityForResult(getAppDetailSettingIntent(context), Integer.parseInt(requestCode));
            } else {
                context.startActivity(getAppDetailSettingIntent(context));
            }
        }

    }

    /**
     * 跳转到miui的权限管理页面
     */
    private static void gotoMiuiPermission(Context context, String requestCode) {
        try { // MIUI 8
            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            localIntent.putExtra("extra_pkgname", context.getPackageName());
            if (requestCode != null && requestCode.trim().length() > 0 && context instanceof Activity) {
                ((Activity) context).startActivityForResult(localIntent, Integer.parseInt(requestCode));
            } else {
                context.startActivity(localIntent);
            }
        } catch (Exception e) {
            try { // MIUI 5/6/7
                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                localIntent.putExtra("extra_pkgname", context.getPackageName());
                if (requestCode != null && requestCode.trim().length() > 0 && context instanceof Activity) {
                    ((Activity) context).startActivityForResult(localIntent, Integer.parseInt(requestCode));
                } else {
                    context.startActivity(localIntent);
                }
            } catch (Exception e1) { // 否则跳转到应用详情
                if (requestCode != null && requestCode.trim().length() > 0 && context instanceof Activity) {
                    ((Activity) context).startActivityForResult(getAppDetailSettingIntent(context), Integer.parseInt(requestCode));
                } else {
                    context.startActivity(getAppDetailSettingIntent(context));
                }
            }
        }
    }

    /**
     * 跳转到魅族的权限管理系统
     */
    private static void gotoMeizuPermission(Context context, String requestCode) {
        try {
            Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
            if (requestCode != null && requestCode.trim().length() > 0 && context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, Integer.parseInt(requestCode));
            } else {
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (requestCode != null && requestCode.trim().length() > 0 && context instanceof Activity) {
                ((Activity) context).startActivityForResult(getAppDetailSettingIntent(context), Integer.parseInt(requestCode));
            } else {
                context.startActivity(getAppDetailSettingIntent(context));
            }
        }
    }

    /**
     * 华为的权限管理页面
     */
    private static void gotoHuaweiPermission(Context context, String requestCode) {
        try {
            Intent intent = new Intent();
//            fixme startActivityForResult 不要使用FLAG_ACTIVITY_NEW_TASK，不然无法正常回调
//            if (context instanceof Activity) {
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//Activity才能添加FLAG_ACTIVITY_NEW_TASK
//            }
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");//华为权限管理
            intent.setComponent(comp);
            if (requestCode != null && requestCode.trim().length() > 0 && context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, Integer.parseInt(requestCode));
            } else {
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (requestCode != null && requestCode.trim().length() > 0 && context instanceof Activity) {
                ((Activity) context).startActivityForResult(getAppDetailSettingIntent(context), Integer.parseInt(requestCode));
            } else {
                context.startActivity(getAppDetailSettingIntent(context));
            }
        }

    }

    /**
     * 获取应用详情页面intent（如果找不到要跳转的界面，也可以先把用户引导到系统设置页面）
     *
     * @return
     */
    public static Intent getAppDetailSettingIntent(Context context) {
        //Intent localIntent = new Intent();
        Uri packageURI = Uri.parse("package:" + context.getPackageName());
        Intent localIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
//        fixme startActivityForResult 不要使用FLAG_ACTIVITY_NEW_TASK，不然无法正常回调
//        if (context instanceof Activity) {
//            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//Activity才能添加FLAG_ACTIVITY_NEW_TASK;
//        }
//        if (Build.VERSION.SDK_INT >= 9) {
//            //localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
//            //localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
//        }
        if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        return localIntent;
    }

    /**
     * 根据包名（可以是自己应用的包名，也可以是其他第三方应用的包名，即可以跳转到其他应用的详情页。）
     * 跳转到该应用的详情界面（可以打开该应用，非系统应用还可以卸载，系统应用不能卸载，但是能停用和启动。）。
     *
     * @param packageName
     * @return
     */
    public static Intent getAppDetailSettingIntent(String packageName) {
        if (packageName == null) {
            return null;
        }
        //Intent localIntent = new Intent();
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent localIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
//        fixme startActivityForResult 不要使用FLAG_ACTIVITY_NEW_TASK，不然无法正常回调
//        if (context instanceof Activity) {
//            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//Activity才能添加FLAG_ACTIVITY_NEW_TASK;
//        }
//        if (Build.VERSION.SDK_INT >= 9) {
//            //localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
//            //localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
//        }
        if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", packageName);
        }
        return localIntent;
    }

}

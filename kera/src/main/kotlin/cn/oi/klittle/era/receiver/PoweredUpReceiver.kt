package cn.oi.klittle.era.receiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import cn.oi.klittle.era.helper.KUiHelper
import cn.oi.klittle.era.utils.KCacheUtils
import cn.oi.klittle.era.utils.KLoggerUtils

//资料 https://www.cnblogs.com/jetereting/p/4572302.html

//最好配置；指定把app安装到内部存储
//<manifest
//package="cn.weixq.autorun"
//xmlns:android="http://schemas.android.com/apk/res/android"
//android:installLocation="internalOnly">


//在主Activity中调用。
//KCacheUtils.putSecret("app_pkg", packageName)
//KCacheUtils.putSecret("app_cls", "MainActivity")

/**
 * 开机广播监听
 */
class PoweredUpReceiver : BroadcastReceiver() {
    //安装app到手机上，然后启动一次程序（据说安卓4.0以后，必须先启动一次程序才能接收到开机完成的广播，目的是防止恶意程序）
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            try {
                if (it.action.equals("android.intent.action.BOOT_COMPLETED")) {
                    val i = Intent()
                    var pkg = KCacheUtils.getSecret("app_pkg")
                    var cls = KCacheUtils.getSecret("app_cls")
                    //var componentName = ComponentName("tv.gamehot.gamehotbox.general", "tv.gamehot.gamehotbox.general.MainActivity");
                    if (pkg != null && cls != null) {
                        //实现开机自启动
                        var componentName = ComponentName(pkg.toString(), pkg.toString() + "." + cls.toString())
                        i.setComponent(componentName)
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context?.startActivity(i)
                    }
                    //上面的 i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 非常重要，如果缺少的话，程序将在启动时报错
                }
            } catch (e: Exception) {
                KLoggerUtils.e("开机启动异常：\t" + e.message)
            }
        }
    }
}
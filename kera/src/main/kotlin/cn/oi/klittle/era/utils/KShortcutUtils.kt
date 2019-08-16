package cn.oi.klittle.era.utils

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.v4.content.pm.ShortcutInfoCompat
import android.support.v4.content.pm.ShortcutManagerCompat
import android.support.v4.graphics.drawable.IconCompat
import cn.oi.klittle.era.base.KBaseApplication


//            <!--目标Activity清单必须添加以下两个属性，桌面快捷启动图标，必不可少（不然无法启动）。主要是 action里的MAIN，category里面的DEFAULT无所谓。为了以防万一。两个都加上。-->
//                    <intent-filter>
//                    <action android:name="android.intent.action.MAIN" />
//                    <category android:name="android.intent.category.DEFAULT" />
//                    </intent-filter>

/**
 * 桌面快捷方式添加（目前就只实现这个功能。查询和删除多多少少都有问题，不靠谱，暂时不用。）
 */
object KShortcutUtils {

    internal class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //当添加快捷方式的确认弹框弹出来时，将被回调(和点不点确认按钮无关，只要确认框弹出就会调用)
            //广播如果没有注册也不会回调。必须在清单里注册了才会回调。（receiver 和 activity同级）
            //在此，没有注册。所以在此不会有回调。
        }
    }

    //添加快捷方式，是向桌面应用(launcher)发送相关action的广播，相关的action如下：
    val ACTION_ADD_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT"

    /**
     * 添加桌面快捷方法（fixme 重复添加问题，和重复添加之后没有更新的问题。需要用户手动删除图标之后。再重新添加才会更新。没办法，系统限制太多。）
     *
     * @param context    上下文
     * @param id         8.0才需要（8.0以下不需要）
     * @param name       名称
     * @param iconBitmap 位图图标（优先使用）
     * @param iconRes    Res目录下资源图标
     * @param clazz      目标Activity
     */
    fun addShortCut(context: Context = KBaseApplication.getInstance(), id: String?, name: String?, iconBitmap: Bitmap?, iconRes: Int?, clazz: Class<*>?, bundle: Bundle? = null) {
        if (name == null || clazz == null) {
            KLoggerUtils.e("TEST", "快捷方式名称或目标Activity为空")
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //fixme 8.0及以上系统,必须通过这个方式添加
            if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                val shortcutInfoIntent = Intent(context, clazz)
                if (bundle != null) {
                    shortcutInfoIntent.putExtras(bundle)
                }
                shortcutInfoIntent.action = Intent.ACTION_VIEW //action必须设置，不然报错
                var id2 = id
                if (id2 == null) {
                    id2 = "only id"
                }
                val builder = ShortcutInfoCompat.Builder(context, id2!!)//id 8.0通过id来区分不同的快捷方式。是唯一标识。
                if (iconBitmap != null && !iconBitmap.isRecycled) {
                    builder.setIcon(IconCompat.createWithBitmap(iconBitmap))//位图图标
                } else if (iconRes != null) {
                    builder.setIcon(IconCompat.createWithResource(context, iconRes))//res目录下的图标
                }
                builder.setShortLabel(name!!)//名称
                        .setIntent(shortcutInfoIntent)//目标Activity
                        .build()
                val info = builder.build()
                //当添加快捷方式的确认弹框弹出来时，将被回调(和点不点确认按钮无关，只要确认框弹出就会调用)
                //该广播接收器不能为空，不然报错。该广播如果没有注册也不会回调。必须注册了才会回调。
                val shortcutCallbackIntent = PendingIntent.getBroadcast(context, 0, Intent(context, MyReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
                ShortcutManagerCompat.requestPinShortcut(context, info, shortcutCallbackIntent.intentSender)
            }
        } else {
            //fixme 8.0以下系统使用传统方式添加。
            val addShortcutIntent = Intent(ACTION_ADD_SHORTCUT)
            // 不允许重复创建
            addShortcutIntent.putExtra("duplicate", false)// 经测试不是根据快捷方式的名字判断重复的
            // 应该是根据快链的Intent来判断是否重复的,即Intent.EXTRA_SHORTCUT_INTENT字段的value
            // 但是名称不同时，虽然有的手机系统会显示Toast提示重复，仍然会建立快链
            // 屏幕上没有空间时会提示
            // 注意：重复创建的行为MIUI和三星手机上不太一样，小米上似乎不能重复创建快捷方式

            // 名字
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name)
            //ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            //int iconSize = am.getLauncherLargeIconSize();//获取ICON图标的尺寸
            if (iconBitmap != null && !iconBitmap.isRecycled) {
                addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON,
                        iconBitmap)//位图图标
            } else if (iconRes != null) {
                addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                        Intent.ShortcutIconResource.fromContext(context,
                                iconRes))//res目录下icon
            }
            // 设置关联程序
            val launcherIntent = Intent(Intent.ACTION_MAIN)
            launcherIntent.setClass(context, clazz)
            launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            launcherIntent.action = Intent.ACTION_MAIN
            launcherIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            //fixme 可以传递指定参数。
            if (bundle != null) {
                launcherIntent.putExtras(bundle)
            }
            addShortcutIntent
                    .putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent)

            // 发送广播
            context.sendBroadcast(addShortcutIntent)
        }
    }
}

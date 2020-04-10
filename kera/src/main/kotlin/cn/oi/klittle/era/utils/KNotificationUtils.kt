package cn.oi.klittle.era.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import cn.oi.klittle.era.base.KBaseApplication
import android.content.Context.NOTIFICATION_SERVICE
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.*

/**
 * fixme 新版特性：现在通知栏如果有消息，应用图标就会有红色的小圆点提醒。
 */
object KNotificationUtils {
    private var manager: NotificationManager//消息管理类
    var CHANNEL_ID = "knotification_default_id"//8.0及以上，需要创建通道
    var CHANNEL_NAME = "kNotification_Default_Channel"
    private fun getContext(): Context {
        return KBaseApplication.getInstance()
    }

    init {
        manager = getContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()//创建通道
    }

    //fixme 8.0必须有通道才能发送消息
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //var channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            var channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);//重要通道，优先级高
            //是否绕过请勿打扰模式
            channel.canBypassDnd();
            //闪光灯
            channel.enableLights(true);
            //锁屏显示通知
            channel.setLockscreenVisibility(VISIBILITY_SECRET);
            //闪关灯的灯光颜色
            channel.setLightColor(Color.RED);
            //桌面launcher的消息角标
            channel.canShowBadge();
            //是否允许震动
            channel.enableVibration(true);
            //获取系统通知响铃声音的配置
            channel.getAudioAttributes();
            //获取通知取到组
            channel.getGroup();
            //设置可绕过  请勿打扰模式
            channel.setBypassDnd(true);
            //设置震动模式
            channel.setVibrationPattern(longArrayOf(100, 100, 200));
            //是否会有灯光
            channel.shouldShowLights();
            manager.createNotificationChannel(channel);
        }
    }

    fun getBuilder(): NotificationCompat.Builder {
        var builder: NotificationCompat.Builder? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = NotificationCompat.Builder(getContext(), CHANNEL_ID)//8.0及以上，需要创建通道
        } else {
            builder = NotificationCompat.Builder(getContext());//内部会自动处理兼容问题。
            //builder.setPriority(PRIORITY_DEFAULT);//设置优先级
        }
        builder.setPriority(PRIORITY_HIGH);//设置优先级,默认设置重要右下角（能够悬浮通知）
        return builder
    }

    /**
     * 发送消息
     * @param clazz 通知要跳转的Activity
     * @param leftLargeIcon 最左边的大图标(直接传位图)
     * @param rightSmallIcon 最右边的小图标(或者在状态栏里提示的小图标)，只能是res下的图标。一般为应用app的图标
     * @param notificationId 通知ID,唯一标示。区分不同的通知
     * @param ticker 状态栏一开始提醒的信息
     * @param title 标题
     * @param content 提示信息正文
     * @param info 提示信息，出现在正文之后
     * @param isClear 用户是否可以手动去除通知(true,可以手动划去通知，false,无法手动去除通知)
     */
    //@SuppressLint("NewApi")//api 要求16
    fun sendNotification(clazz: Class<*>, leftLargeIcon: Bitmap, rightSmallIcon: Int, notificationId: Int,
                         ticker: String, title: String, content: String, info: String, isShowWhen: Boolean = true, time: Long = System.currentTimeMillis(), isAutoCancel: Boolean = true, isClear: Boolean = true,isHIGH: Boolean = true) {
        val notificationIntent = Intent(getContext(), clazz)
        val pendingIntent = PendingIntent.getActivity(getContext(), 0, notificationIntent, 0)
        var builder = getBuilder()
        if (isHIGH){
            //builder.setPriority(PRIORITY_HIGH)
            builder.setPriority(PRIORITY_MAX)
        }else{
            builder.setPriority(PRIORITY_DEFAULT)
        }
        builder.setContentIntent(pendingIntent)
        builder.setLargeIcon(leftLargeIcon)//fixme 最左边的大图标.
        if (isAutoCancel) {
            builder.setAutoCancel(true)//fixme 自己维护通知的消失,点击的时候，会自己消失。如果为false就需要自己手动去移除了
        } else {
            builder.setAutoCancel(false)
        }
        if (isShowWhen) {
            builder.setShowWhen(true)//fixme 显示时间。右上角
            builder.setWhen(time)//fixme 设置时间，setShowWhen必须为true才有效。
        } else {
            builder.setShowWhen(false)
        }
        builder.setContentTitle(title)//fixme 标题
        builder.setContentText(content)//fixme 标题下的正文

        /**
         * fixme 以下三个属性，7.0左右的可能不会显示。
         */
        builder.setSmallIcon(rightSmallIcon)//fixme 最右边的小图标(或者在状态栏里提示的小图标)，这个必不可少（7.0就算不显示，也不能少。8.0也不能少）。不然异常。只能是res下的图标，一般为app应用图标。
        builder.setTicker(ticker)//fixme 一开始通知时，所显示的文字。状态栏的文字
        builder.setContentInfo(info)//fixme 提示信息，出现在正文之后

        var notification = builder.build()
        notification.defaults = Notification.DEFAULT_SOUND;// 设置为默认的声音
        if (isClear) {
            notification.flags = Notification.FLAG_AUTO_CANCEL//可以清除
        } else {
            notification.flags = Notification.FLAG_NO_CLEAR//fixme 用户无法清除,系统也清理不掉，需要自己手动去清理。用户体验不好，建议不要用。

        }
        manager.notify(notificationId, notification)
    }

    //fixme 自定义视图通知栏
    fun sendNotificationRemoteViews(clazz: Class<*>, remoteViews: RemoteViews, rightSmallIcon: Int, notificationId: Int,
                                    ticker: String, info: String, isShowWhen: Boolean = true, time: Long = System.currentTimeMillis(), isAutoCancel: Boolean = true, isClear: Boolean = true,isHIGH: Boolean = true) {
        val notificationIntent = Intent(getContext(), clazz)
        val pendingIntent = PendingIntent.getActivity(getContext(), 0, notificationIntent, 0)
        var builder = getBuilder()
        if (isHIGH){
            //builder.setPriority(PRIORITY_HIGH)
            builder.setPriority(PRIORITY_MAX)
        }else{
            builder.setPriority(PRIORITY_DEFAULT)
        }
        builder.setContentIntent(pendingIntent)

        //远程视图，既不是当前进程的视图，而是属于SystemServer进程
        //builder.setContent(remoteViews)//setCustomContentView只能显示系统通知栏高度。
        builder.setCustomBigContentView(remoteViews)//setCustomBigContentView可以显示remoteviews的完整高度，

        if (isAutoCancel) {
            builder.setAutoCancel(true)//fixme 自己维护通知的消失,点击的时候，会自己消失。如果为false就需要自己手动去移除了
        } else {
            builder.setAutoCancel(false)
        }
        if (isShowWhen) {
            builder.setShowWhen(true)//fixme 显示时间。右上角
            builder.setWhen(time)//fixme 设置时间，setShowWhen必须为true才有效。
        } else {
            builder.setShowWhen(false)
        }

        /**
         * fixme 以下三个属性，7.0左右的可能不会显示。
         */
        builder.setSmallIcon(rightSmallIcon)//fixme 最右边的小图标(或者在状态栏里提示的小图标)，这个必不可少（7.0就算不显示，也不能少。8.0也不能少）。不然异常。只能是res下的图标，一般为app应用图标。
        builder.setTicker(ticker)//fixme 一开始通知时，所显示的文字。状态栏的文字
        builder.setContentInfo(info)//fixme 提示信息，出现在正文之后

        var notification = builder.build()
        notification.defaults = Notification.DEFAULT_SOUND;// 设置为默认的声音
        if (isClear) {
            notification.flags = Notification.FLAG_AUTO_CANCEL//可以清除
        } else {
            notification.flags = Notification.FLAG_NO_CLEAR//fixme 用户无法清除,系统也清理不掉，需要自己手动去清理。用户体验不好，建议不要用。

        }
        manager.notify(notificationId, notification)
    }

    /**
     * 发送消息，自定义View
     * @param cls 通知要跳转的Activity
     * @param drawRightId 下拉列表最右边的图标(也是通知一开始所显示的提示图标,既状态栏图标)。必不可少，不然通知无反应！
     * @param viewId 自定义布局ID
     * @param textId 自定义布局里的TextView Id
     * @param text   自定义布局里的TextView 里的内容
     * @param notificationId 通知ID
     * @param isRemove 用户是否可以手动去除通知(true,可以手动划去通知，false,无法手动去除通知)
     */
    //@SuppressLint("NewApi")//api 要求16
    fun sendNotificationView(clazz: Class<*>, drawRightId: Int, viewId: Int, textId: Int, text: String, notificationId: Int, isRemove: Boolean = false) {
        var notification = Notification()
        val notificationIntent = Intent(getContext(), clazz)
        val pendingIntent = PendingIntent.getActivity(getContext(), 0, notificationIntent, 0)
        val builder = getBuilder()
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(true);//fixme 自己维护通知的消失,点击的时候，会自己消失。如果为false就需要自己手动去移除了
        builder.setShowWhen(true)//fixme 右上角显示消息发送的时间
        /**
         * setSmallIcon状态栏里的图标，下拉表里最右边的图标(也是通知一开始所显示的提示图标)。必不可少，不然通知无反应！
         */
        builder.setSmallIcon(drawRightId)
        /**
         * setContent自定义视图覆盖系统默认的，以上的设置都将无效
         */
        val remoteViews = RemoteViews(getContext().packageName, viewId)
        remoteViews.setTextViewText(textId, text)//重写Text文本
        builder.setContent(remoteViews)

        notification = builder.build()
        if (!isRemove) {
            notification.flags = Notification.FLAG_NO_CLEAR//用户无法清除
        }
        manager.notify(notificationId, notification)
    }

    /**
     * 根据通知ID,去除消息
     */
    fun cancelNotification(notificationId: Int) {
        manager.cancel(notificationId)
    }
}

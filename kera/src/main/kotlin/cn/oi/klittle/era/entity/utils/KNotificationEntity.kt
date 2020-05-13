package cn.oi.klittle.era.entity.utils

import android.content.Context
import android.graphics.Bitmap
import androidx.core.app.NotificationManagerCompat
import android.widget.RemoteViews
import cn.oi.klittle.era.base.KBaseApplication
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.utils.KNotificationUtils

//            使用案例
//                            KNotificationEntity().apply {
//                                id = 1//通知ID,唯一标示。区分不同的通知
//                                clazz = MainActivity::class.java
//                                leftLargeIcon = getBitmapFromResource(R.mipmap.timg)//左边图标；KBaseUi里有这个获取位图的方法。
//                                rightSmallIcon=R.mipmap.timg2//状态栏小图标
//                                title = "标题"
//                                content = "正文"
//                                sendNotifi()//发送
//                            }

//                            KGlideUtils.getBitmapFromResouce(R.mipmap.timg, kpx.x(200), kpx.x(200), isCenterCrop = true) { key, bitmap ->
//                                KNotificationEntity().apply {
//                                    id = 1
//                                    clazz = MainActivity::class.java
//                                    leftLargeIcon = bitmap//左边图标
//                                    rightSmallIcon = R.mipmap.timg2//状态栏小图标
//                                    title = "标题"
//                                    content = "正文"
//                                    //ticker,和 info可有可无。现在基本度没有效果。
//                                    sendNotifi()//发送
//                                }
//                            }

//fixme 自定义视图通知栏
//KNotificationEntity().apply {
//    id = 2
//    clazz = MainActivity::class.java
//    remoteViews(R.layout.notify_item)//自定义通知栏视图
//    remoteViews?.setTextViewText(R.id.hello, "我叫彭治铭")
//    sendNotifi()//发送
//}

/**
 * fixme 消息通知栏实体类;如果有消息没有查看。app图标会有圆点提示（5.0之后都有）。
 */
open class KNotificationEntity {

    fun getContext(): Context {
        return KBaseApplication.getInstance()
    }

    /**
     * 判断通知栏权限是否开启（true开启，false为开启）。只能判断。不能动态去申请。必须用户自己去手动开启。
     * 判断的是普通的通知栏权限；不是悬浮通知权限。
     */
    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(getContext()).areNotificationsEnabled()
    }

    var isHIGH: Boolean = true//fixme 是否为重要通知，默认是。重要通知能够悬浮顶部通知;在通知权限里面，需要开启悬浮通知权限(需要用户手动去开启)。

    /**
     * fixme 重点设置以下五个属性即可
     */
    var id: Int = 0//通知ID,唯一标示。区分不同的通知
    var clazz: Class<*>? = null//目标跳转Activity

    //calss类型这样设置即可
    fun clazz(any: Any) {
        clazz = any::class.java
    }

    var leftLargeIcon: Bitmap? = null//左边的大图标位图
    var title: String? = null//标题
    var content: String? = null//标题下的正文


    /**
     * RemoteViews没有提供findViewById()，因为无法直接访问View元素，必须通过RemoteView提供的一系列set方法。
     * FrameLayout、LinearLayout、RelativeLayout、GridLayout、AnalogClock、Button、Chronometer、
     * ImageButton、ImageView、ProgressBar、TextView、ViewFlipper、
     * ListView、GridView、StackView、AdapterViewFlipper、ViewStub。
     * 只支持以上View。无法自定义View。具有局限性。
     * 可以做桌面小部件 ：通过AppWidgetProvider来实现。(后续有需求，再研究)
     */
    //fixme 自定义视图，如果使用自定义视图。图标，标题，正文就都不用设置了。(优先使用自定义视图)
    //远程视图，既不是当前进程的视图，而是属于SystemServer进程
    var remoteViews: RemoteViews? = null

    /**
     * 设置和获取远程视图。
     * @param layoutId 布局id。只能通过xml获取,注意了，不支持所有的布局。只能是相对，线性等简单布局，不然报错。
     */
    fun remoteViews(layoutId: Int): RemoteViews? {
        if (remoteViews == null) {
            remoteViews = RemoteViews(getContext().packageName, layoutId)
        }
        return remoteViews
    }

    //以下属性，建议设置成默认即可
    var isShowWhen: Boolean = true//是否显示右上角的时间。
    var time: Long = System.currentTimeMillis()//默认为当前系统时间，isShowWhen为true才会显示
    var isAutoCancel: Boolean = true//自己维护通知的消失,点击的时候，自己会消失
    var isClear: Boolean = true//用户是否可以手动清除通知栏。true可以。false不可以（用户体验不好），需要自己代码去移除。


    /**
     * fixme 以下三个属性，7.0及以上的可能不会显示。默认以下设置就可以了，不用管。
     */
    //sdk21(5.0)开始，Google要求，所有应用程序的通知栏图标，应该只使用alpha图层来进行绘制，而不应该包括RGB图层。
    //通俗地说，就是我们的通知栏图标不要带颜色就可以了。不然不显示。（如果带颜色了，会在状态栏提示里显示。）
    var rightSmallIcon: Int = KBaseApplication.getInstance().appIconRes//右边的小图标(或者在状态栏里提示的小图标)，这个必不可少。一般为app应用图标。
    var ticker: String = ""//一开始通知时，所显示的文字。状态栏的文字
    var contentInfo: String = ""//提示信息，出现在正文之后

    /**
     * 发生通知。
     * @param callback 回调是否发送成功。发送失败一般都是没有开启通知栏权限。
     */
    open fun sendNotifi(callback: ((isSuccess: Boolean) -> Unit)? = null) {
        if (areNotificationsEnabled()) {
            if (clazz != null) {
                if (remoteViews != null) {
                    //优先使用自定义视图
                    KNotificationUtils.sendNotificationRemoteViews(clazz = clazz, remoteViews = remoteViews, rightSmallIcon = rightSmallIcon, notificationId = id, ticker = ticker, info = contentInfo, isShowWhen = isShowWhen, time = time, isAutoCancel = isAutoCancel, isClear = isClear, isHIGH = isHIGH)
                } else {
                    KNotificationUtils.sendNotification(clazz = clazz, leftLargeIcon = leftLargeIcon!!, rightSmallIcon = rightSmallIcon, notificationId = id, ticker = ticker, title = title, content = content, info = contentInfo, isShowWhen = isShowWhen, time = time, isAutoCancel = isAutoCancel, isClear = isClear, isHIGH = isHIGH)
                }
                callback?.let {
                    it(true)//发送成功
                }
            } else {
                KLoggerUtils.e("TEST", "notification消失发送失败，目标Activity为空")
            }
        } else {
            callback?.let {
                it(false)//发送失败
            }
        }

    }

    //根据id,移除通知
    open fun cancel() {
        KNotificationUtils.cancelNotification(id)
    }

}
package cn.oi.klittle.era.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.oi.klittle.era.utils.KLoggerUtils


/**
 * home键通过广播监听
 */
class HomeKeyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            try {
                var action = it.action
                //KLoggerUtils.e("action：\t"+action)
                if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                    //home键
                    //KLoggerUtils.e("HOME键")
                } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                    //开屏
                    //KLoggerUtils.e("开屏")
                } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                    //锁屏
                    //KLoggerUtils.e("锁屏")
                } else {
                    //fixme 基本上只能监听解锁。其他的都没有监听成功。
                    // 解锁
                    //Intent.ACTION_USER_PRESENT
                    //KLoggerUtils.e("解锁")
                }
            } catch (e: Exception) {
                KLoggerUtils.e("home按键监听异常：\t" + e.message)
            }
        }
    }
}
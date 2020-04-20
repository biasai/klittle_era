package cn.oi.klittle.era.utils

import android.util.Log

/**
 * Log打印输入，可以开启和关闭日志。
 */

object KLoggerUtils {

    //设为true 开启日志;false关闭日志
    var isLOG_ENABLE = true
    //test也能搜索的到。是模糊匹配。好像不区分大小写。
    var TAG: String = "KTEST"

    fun i(msg: String, tag: String = TAG) {
        if (isLOG_ENABLE) {
            Log.i(tag, msg)
        }
    }

    fun i(msg: String) {
        if (isLOG_ENABLE) {
            Log.i(TAG, msg)
        }
    }

    fun v(msg: String, tag: String = TAG) {
        if (isLOG_ENABLE) {
            Log.v(tag, msg)
        }
    }

    fun v(msg: String) {
        if (isLOG_ENABLE) {
            Log.v(TAG, msg)
        }
    }

    fun d(msg: String, tag: String = TAG) {
        if (isLOG_ENABLE) {
            Log.d(tag, msg)
        }
    }

    fun d(msg: String) {
        if (isLOG_ENABLE) {
            Log.d(TAG, msg)
        }
    }

    fun w(msg: String, tag: String = TAG) {
        if (isLOG_ENABLE) {
            Log.w(tag, msg)
        }
    }

    fun w(msg: String) {
        if (isLOG_ENABLE) {
            Log.w(TAG, msg)
        }
    }

    fun e(msg: String, tag: String = TAG) {
        if (isLOG_ENABLE) {
            Log.e(tag, msg)
        }
    }

    fun e(msg: String, tag: String = TAG, isLogEnable: Boolean) {
        if (isLOG_ENABLE || isLogEnable) {
            Log.e(tag, msg)
        }
    }

    fun e(msg: String) {
        if (isLOG_ENABLE) {
            Log.e(TAG, msg)
        }
    }

    /**
     * @param isLogEnable 是否打印Log日志；不受isLOG_ENABLE控制。
     */
    fun e(msg: String, isLogEnable: Boolean) {
        if (isLOG_ENABLE || isLogEnable) {
            Log.e(TAG, msg)
        }
    }

    fun e_long(msg: String?) {
        KLoggerUtils.e_long(msg, TAG, false)
    }

    fun e_long(msg: String?, isLogEnable: Boolean) {
        KLoggerUtils.e_long(msg, TAG, isLogEnable)
    }

    /**
     * 分段打印所有日志
     */
    fun e_long(msg: String?, tag: String = TAG, isLogEnable: Boolean) {
        if (!isLogEnable) {
            if (!isLOG_ENABLE) {
                return
            }
        }
        if (msg == null) {
            return
        }
        msg?.trim()?.let {
            if (it.length <= 0) {
                return
            }
        }
        //"\n的前面必须加个空格号换行才有效，不然第一行换行无效。"
        var ln = " \n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"//换行，这样可以分清打印的数据。
        var msg = msg
        val segmentSize = 3 * 1024
        val length = msg.length.toLong()
        // 长度小于等于限制直接打印
        if (length <= segmentSize) {
            e(ln + msg, tag)
        } else {
            // 循环分段打印日志
            while (msg!!.length > segmentSize) {
                val logContent = msg.substring(0, segmentSize)
                msg = msg.replace(logContent, "")
                e(ln + logContent, tag)
            }
            // 打印剩余日志
            e(ln + msg, tag)
        }
    }

}

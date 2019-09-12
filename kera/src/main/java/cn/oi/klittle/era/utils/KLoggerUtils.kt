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

    fun e(msg: String) {
        if (isLOG_ENABLE) {
            Log.e(TAG, msg)
        }
    }

    /**
     * 分段打印所有日志
     */
    fun e_long(msg: String?) {
        if (msg == null) {
            return
        }
        var msg = msg
        val segmentSize = 3 * 1024
        val length = msg.length.toLong()
        // 长度小于等于限制直接打印
        if (length <= segmentSize) {
            e(msg)
        } else {
            // 循环分段打印日志
            while (msg!!.length > segmentSize) {
                val logContent = msg.substring(0, segmentSize)
                msg = msg.replace(logContent, "")
                e(logContent)
            }
            // 打印剩余日志
            e(msg)
        }
    }

}

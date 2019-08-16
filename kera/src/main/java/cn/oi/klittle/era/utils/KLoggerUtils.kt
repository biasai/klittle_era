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

}

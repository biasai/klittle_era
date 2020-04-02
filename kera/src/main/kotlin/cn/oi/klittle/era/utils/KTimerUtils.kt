package cn.oi.klittle.era.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.Deferred
/**
 * 定时刷新工具类，参数说明,0F是浮点型，0L是长整形。
 * count 刷新次数，从1开始。计循环次数是从 1到count （包括1和num）, Long.MAX_VALUE这个值非常大，比Int.MAX_VALUE大。所以选择Long类型
 * unit 每次刷新的时间间距。单位毫秒。1000等于一秒
 * firstUnit 第一刷新间隔时间，默认0
 * callback 刷新回调，返回当前刷新次数。
 */
object KTimerUtils {
    class KTimer {
        //没有重新开始。要重新开始。直接再调用刷新方法即可。
        //不加入开始的原因，是不想对Activity或View进行持有。防止泄露。

        private var isPause = false//是否暂停
        private var isEnd = false//是否结束

        //暂停
        fun pause() {
            isPause = true
        }

        fun isPause(): Boolean {
            return isPause
        }

        //继续
        fun resume() {
            isPause = false
        }

        //结束
        fun end() {
            isEnd = true
        }

        fun isEnd(): Boolean {
            return isEnd
        }

    }

    //子线程定时刷新
    fun refresh(count: Long = 60, unit: Long = 1000, firstUnit: Long = 0, callback: (num: Long) -> Unit): KTimer {
        //Int.MAX_VALUE//2147483647
        //Long.MAX_VALUE//9223372036854775807L
        var kTimer = KTimer()
        if (count > 0) {
            GlobalScope.async {
                var i = 1L//fixme 次数从1开始，
                while (!kTimer.isEnd()) {
                    if (kTimer.isEnd()) {
                        break//结束
                    }
                    if (i == 1L) {
                        //首次，第一次刷新间隔时间
                        if (firstUnit > 0) {
                            delay(firstUnit)
                        }
                    } else {
                        delay(unit)
                    }
                    if (kTimer.isEnd()) {
                        break//结束
                    }
                    //判断是否暂停
                    if (!kTimer.isPause()) {
                        val num = i
                        callback(num)//子线程
                        i++
                    }
                    if (i > count) {//fixme 即循环次数包括（等于）count。大于count马上结束。大于num不会执行。
                        kTimer.end()
                        break
                    }
                }
            }
        }
        return kTimer
    }

    //主线程刷新
    fun refreshUI(activity: Activity?, count: Long = 60, unit: Long = 1000, firstUnit: Long = 0, callback: (num: Long) -> Unit): KTimer {
        var kTimer = KTimer()
        if (count > 0) {
            activity?.let {
                GlobalScope.async {
                    var i = 1L
                    while (!kTimer.isEnd()) {
                        if (kTimer.isEnd()) {
                            break//结束
                        }
                        if (i == 1L) {
                            //首次，第一次刷新间隔时间
                            if (firstUnit > 0) {
                                delay(firstUnit)
                            }
                        } else {
                            delay(unit)
                        }
                        if (kTimer.isEnd()) {
                            break//结束
                        }
                        //判断是否暂停
                        if (!kTimer.isPause()) {
                            var isBreak = false//是否跳出循环
                            activity?.let {
                                if (!it.isFinishing) {
                                    val num = i//防止计算错误（不同线程）。所以必须新建一个变量计时。
                                    it.runOnUiThread {
                                        callback(num)//主线程
                                    }
                                    i++
                                } else {
                                    isBreak = true
                                }
                            }
                            if (isBreak) {
                                break
                            }
                        }
                        if (i > count) {//即循环次数包括（等于）count。大于count马上结束。大于count不会执行。
                            kTimer.end()
                            break
                        }
                    }
                }
            }
        }
        return kTimer
    }

    //控件刷新
    fun refreshView(view: View?, count: Long = 60, unit: Long = 1000, firstUnit: Long = 0, callback: (num: Long) -> Unit): KTimer? {
        var kTimer = KTimer()
        if (count > 0) {
            var activity: Activity? = null
            view?.let {
                var ctx: Context? = it.context
                ctx?.let {
                    if (it is Activity) {
                        activity = ctx as Activity
                    }
                }
            }
            activity?.let {
                if (!it.isFinishing) {
                    GlobalScope.async {
                        var i = 1L
                        while (!kTimer.isEnd()) {
                            if (kTimer.isEnd()) {
                                break//结束
                            }
                            if (i == 1L) {
                                //首次，第一次刷新间隔时间
                                if (firstUnit > 0) {
                                    delay(firstUnit)
                                }
                            } else {
                                delay(unit)
                            }
                            if (kTimer.isEnd()) {
                                break//结束
                            }
                            //判断是否暂停
                            if (!kTimer.isPause()) {
                                var isBreak = false//是否跳出循环
                                view?.let {
                                    //判断控件是否显示，不显示就停止刷新计时
                                    if (!it.isShown) {
                                        isBreak = true
                                    }
                                    if (it.visibility == View.INVISIBLE) {
                                        isBreak = true
                                    }
                                }
                                if (isBreak) {
                                    break
                                }
                                activity?.let {
                                    if (!it.isFinishing) {
                                        val num = i//防止计算错误（不同线程）。所以必须新建一个变量计时。
                                        it.runOnUiThread {
                                            callback(num)//主线程
                                        }
                                        i++
                                    } else {
                                        isBreak = true
                                    }
                                }
                                if (isBreak) {
                                    break
                                }
                            }
                            if (i > count) {//即循环次数包括（等于）count。大于count马上结束。大于count不会执行。
                                kTimer.end()
                                break
                            }
                        }
                    }
                }
            }
        }
        return kTimer
    }
}
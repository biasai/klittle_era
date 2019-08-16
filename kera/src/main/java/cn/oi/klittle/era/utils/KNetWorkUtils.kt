package cn.oi.klittle.era.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import cn.oi.klittle.era.base.KBaseApplication

/**
 * 手机如果同时连上了数据流量和wifi,以wifi的为主。即使wifi没有网，仍然连接的是wifi。只要wifi连着，不管有网没网。都是以wifi为主。不会去连数据流量。
 * 除非wifi断开。或者wifi连接失败(没有获得IP)。才会去连数据流量。
 * 网络链接状态类【需要wifi等权限】
 */
object KNetWorkUtils {
    fun getContext():Context{
        return KBaseApplication.getInstance().baseContext
    }
    /**
     * 判断是否连上了网络。不能判断是否有网。比如你连上了一个wifi，但是该wifi是虚网，无法真正上网。
     * 只要数据流量打开或者随便连上一个wifi就会返回true，什么都没连，返回false
     *
     * @return true 可能连上了数据流量，也可能连接上wifi。 false什么都没连。
     */
    fun isNetworkAvailable(): Boolean {
        val context = getContext()
        // 获取手机所有链接管理对象(包括对wi-fi,net等链接的管理)
        val connectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager == null) {
            return false
        } else {
            // 获取NetWorkInfo对象
            val networkInfo = connectivityManager.allNetworkInfo

            if (networkInfo != null && networkInfo.size > 0) {
                for (i in networkInfo.indices) {
                    // 判断当前网络状态是否为链接状态
                    if (networkInfo[i].state == NetworkInfo.State.CONNECTED) {
                        return true
                    }
                }
            }

        }
        return false
    }

    //判断wifi是否已连接【只能判断是否连接上wifi,不能判断是否有网】,如果wifi已经连接上了。即使数据流量连接上，上网也是已wifi为主，不会消耗流量。
    fun isWifi(): Boolean {
        val connectivityManager = getContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetInfo = connectivityManager.activeNetworkInfo
        return if (activeNetInfo != null && activeNetInfo.type == ConnectivityManager.TYPE_WIFI) {
            true
        } else false
    }

    /**
     * 判断上网方式，是否为手机流量
     *
     * @param context
     * @return
     */
    fun isTel(): Boolean {
        return if (isNetworkAvailable()) {//是否连接上网络
            if (isWifi()) {//是否连接上wifi
                false
            } else {
                true//手机流量
            }
        } else false
    }

}

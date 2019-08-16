package cn.oi.klittle.era.https.ko

object KOhttp {
    //普通网络请求
    fun url(url: String): KHttps {
        return KHttps().url(url)
    }

    //轮询网络请求
    fun polling(url: String): KPolling {
        return KPolling(url = url)
    }

}
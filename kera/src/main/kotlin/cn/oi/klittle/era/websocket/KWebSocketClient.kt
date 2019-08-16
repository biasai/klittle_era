package cn.oi.klittle.era.websocket

import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.utils.KNetWorkUtils
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.concurrent.TimeUnit

//仓库         mavenCentral()
//使用依赖     api "org.java-websocket:Java-WebSocket:1.3.9"
//github地址   https://github.com/TooTallNate/Java-WebSocket

//        fixme 使用案例
//        var ws = KWebSocketClient("ws://dev-wss.thinkhome.com.cn:32012").apply {
//            connect()//连接
//            onOpen {
//                KLoggerUtils.e("打开:\t" + isConnect())
//                //发送握手数据（与服务器端进行认证）
//                send("LINK {\"accessToken\":\"th.fa517888a189c02a66199ee97765b56b-27b7f225-86f8-4fc9-b6a2-d95b75ca1a7b\",\"deviceToken\":\"278f7e009499bd2a\",\"clientID\":\"ThinkHome_Client_Android\",\"clientValue\":\"\",\"homeID\":\"H20180814141857524\"}")
//                //发送心跳包，表示在线状态
//                sendHeart("KEEPALIVE", 19000) { text, count ->
//                    KLoggerUtils.e("心跳数据：\t" + text + "\t发送次数：\t" + count)
//                }
//            }
//            onError {
//                KLoggerUtils.e("错误：\t" + it)
//            }
//            onClose { code, reason, remote ->
//                KLoggerUtils.e("关闭：\tcode:\t" + code + "\treason:\t" + reason + "\tremote:\t" + remote + "\t是否打开：\t" + isConnect())
//            }
//            onMessage {
//                KLoggerUtils.e("接收：\t" + it)
//            }
//        }

/**
 *
 * webSocket封装，来自 Java-WebSocket这个库。比较好。
 *
 * 名词介绍：
 * fixme 握手：即连接成功之后，向服务器发送一个认证的指令（由服务器和客户端自己约定）。
 * fixme 心跳包：即每隔一定的时间段(一般为20秒左右)客户端主动向服务器发送一个指令(如：KEEPALIVE，也有服务端和客户端自己决定)。表示在线。
 *
 * http是单向的；而websocket是双向的。
 *
 * @param serverUri 地址，如：ws://dev-wss.thinkhome.com.cn:32012 ;其中ws就和http一样。是固定的。格式为：ws://地址:端口号
 */
open class KWebSocketClient(serverUri: String) : WebSocketClient(URI(serverUri)) {


    //是否开启自动重连功能，默认开启(手动断开，会自动关闭重连功能)
    var isEnableAutoConnect: Boolean = true
    //自动连接的时间，单位毫秒。默认五秒
    var autoConnectTime: Long = 5000
    //是否正在重连，防止重复调用该方法。
    private var isAutoConeting = false

    /**
     * 自动重连（断开之后，会自动重写连接）；在onError()和onClose()中会自动调用该方法。
     */
    private fun autoConnect() {
        if (isEnableAutoConnect && !isAutoConeting) {
            async {
                while (isEnableAutoConnect && !isConnect()) {
                    isAutoConeting = true//正在循环重连
                    delay(autoConnectTime, TimeUnit.MILLISECONDS)
                    //首先判断是否有网
                    if (KNetWorkUtils.isNetworkAvailable()) {
                        reconnect()//重连连接
                    }
                }
                isAutoConeting = false//循环重连结束
            }
        }
    }

    //连接（第一次连接的时候调用。如果关闭了，就不能再调用，就必须调用重新连接。）
    override fun connect() {
        async {
            if (!isConnect()) {
                super.connect()
            }
        }
    }

    /**
     * 重新连接（关闭之后，可以调用重新连接。）；fixme 即关闭（close()）之后,必须调用重新连接才能连接上。（因为重新连接里面调用了重置的方法。）
     */
    override fun reconnect() {
        async {
            if (!isConnect()) {
                super.reconnect()
            }
        }
    }

    //判断是否连接
    open fun isConnect(): Boolean {
        if (isOpen) {
            return true
        }
        return false
    }

    //手动关闭
    override fun close() {
        async {
            if (isConnect()) {
                isEnableAutoConnect = false//手动断开，则关闭自动重连功能。
                super.close()
            }
        }
    }

    //发送字符串数据
    override fun send(text: String) {
        async {
            super.send(text)
        }
    }

    //发送字节数组
    override fun send(data: ByteArray) {
        async {
            super.send(data)
        }
    }

    //是否开启心跳包，默认开启。
    var isEnableHeart: Boolean = true
    //心跳回调，回调只有一个。防止多次回调。
    private var heartCallback: ((text: String, count: Long) -> Unit)? = null
    //心跳次数
    private var heartCount = 0L
    //是否正在进行心跳，防止重复调用
    private var isHearting = false
    //心跳数据
    private var heartData: String = ""
    //心跳间隔时间
    private var heartTime = 20000L

    /**
     * 发送心跳包（只允许一个心跳事件）
     * @param text 心跳包数据,如：KEEPALIVE（有自己和服务器决定）
     * @param firstTime 首次发送间隔时间
     * @param time 心跳间隔时间，单位毫秒。默认20秒。
     * @param callback 回调，每次发送心跳时都会回调，返回发送的心跳数据及发送次数。
     */
    open fun sendHeart(text: String, firstTime: Long = 0L, time: Long = heartTime, callback: ((text: String, count: Long) -> Unit)? = null) {
        heartCallback = callback//回調更新。
        heartData = text//心跳数据更新。
        heartTime = time//心跳间隔时间更新。
        if (isHearting) {
            return//正在心跳中。防止多次重复心跳。
        }
        async {
            try {
                if (firstTime > 0) {
                    delay(firstTime, TimeUnit.MILLISECONDS)
                }
                while (isConnect()) {//连接断开，自动跳出循环。
                    isHearting = true
                    if (isEnableHeart) {
                        send(heartData)
                        if (heartCount >= Int.MAX_VALUE) {
                            heartCount = 0//以防万一，防止数字太大奔溃。
                        }
                        heartCount++
                        heartCallback?.let {
                            it(text, heartCount)
                        }
                    } else {
                        break//跳出心跳循环。
                    }
                    delay(heartTime, TimeUnit.MILLISECONDS)//单位毫秒，1000等于一秒
                }
                isHearting = false//心跳结束，跳出循环。
            } catch (e: java.lang.Exception) {
                KLoggerUtils.e("心跳数据发送异常：\t" + e.message)
            }
        }
    }

    var onOpen: (() -> Unit)? = null
    //打开（与服务器端连接成功时） 回调
    fun onOpen(onOpen: (() -> Unit)? = null) {
        this.onOpen = onOpen
    }

    var onError: ((error: String?) -> Unit)? = null
    //错误(与服务器连接失败)
    fun onError(onError: ((error: String?) -> Unit)? = null) {
        this.onError = onError
    }

    var onClose: ((code: Int, reason: String?, remote: Boolean) -> Unit)? = null
    //关闭 回调
    fun onClose(onClose: ((code: Int, reason: String?, remote: Boolean) -> Unit)? = null) {
        //reason关闭的原因(一般都是空的)，remote是否是远程关闭（服务器端关闭），网络断开时也会是true(连接不上服务器，认为是服务器断掉的。)
        this.onClose = onClose
    }


    var onMessage: ((message: String?) -> Unit)? = null
    //接收信息回调（服务器端发送消息，会回调此方法）
    fun onMessage(onMessage: ((message: String?) -> Unit)? = null) {
        //接收信息
        this.onMessage = onMessage
    }

    /**
     * fixme 以下是接口实现
     */

    override fun onOpen(handshakedata: ServerHandshake?) {
        //打开（连接成功的时候调用）
        onOpen?.let {
            it()
        }
    }

    override fun onError(ex: Exception?) {
        //错误(一般都是连接失败的时候调用)
        onError?.let {
            it(ex?.message)
        }
        //自动重连
        autoConnect()
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        //关闭
        onClose?.let {
            //reason关闭的原因(一般都是空的)，remote是否是远程关闭（服务器端关闭），网络断开时也会是true(连接不上服务器，认为是服务器断掉的。)
            it(code, reason, remote)
        }
        //自动重连
        autoConnect()
    }

    override fun onMessage(message: String?) {
        //接收信息
        onMessage?.let {
            it(message)
        }
    }

}
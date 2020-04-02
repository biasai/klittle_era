package cn.oi.klittle.era.udp


import cn.oi.klittle.era.socket.KIpPort
import cn.oi.klittle.era.socket.KState
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress

import cn.oi.klittle.era.utils.KLoggerUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.Deferred

/**
 * fixme 同一网段（同一路由器下有效。不同wifi只要在同一个路由器下都有效）
 * fixme 广播UDP与单播UDP的区别就是IP地址不同，广播使用广播地址255.255.255.255，将消息发送到在同一广播网络上的每个主机
 *
 * fixme Udp广播（向同一网段（同一个路由器下，不同的局域网只要在同一路由器下都可以）下发送消息）；udp不属于长连接。所以没有判断是否连接的方法。(UDP是不需要建立连接的)
 * fixme udp广播发送消息，不能保证百分百发送成功，但一般都能发送成功。
 * fixme 广播ip默认:255.255.255.255 即可。 同一个局域网都能接收到。
 * 亲测，能用：var udp = KUdpBroadcast("10.10.100.254", 48899) 这个是SmartAP的固定ip和端口。
 * @param port 对方的端口号
 * @param ip 是对方的ip,即服务端的ip;即可以是 255.255.255.255;也可以是指定的ip地址。
 * @param soTimeout 超时时间(单位毫秒1000是一秒)，0默认是无限大。最好默认不要设置。因为设置之后，接收也会有超时时间限制。
 */
open class KUdpBroadcast(open var port: Int? = KIpPort.port(), open var ip: String? = "255.255.255.255", open var soTimeout: Int = 0) {
    private val TAG = "UdpBroadcast:\t"

    private var socket: DatagramSocket? = null
    private var inetAddress: InetAddress? = null//对方的ip,即发送对象的ip

    var ipPort: String? = null
        get() {
            return KIpPort.getIpPort(ip, port)
        }

    init {
        open()//fixme 初始化，打开
    }

    /**
     * fixme 打开socket(当ip地址或端口号改变时，要重新打开。)
     */
    fun open(ip: String? = this.ip, port: Int? = this.port, soTimeout: Int = this.soTimeout) {
        if (ip != null && ip.length > 0 && port != null)
            try {
                close()//打开之前，要先关闭；以防万一。
                /**
                 * fixme 这里只是指定ip和端口。与对方网络是否打开没有关系。UDP是不需要建立连接的
                 * fixme 一般都是成功。异常一般只在发送的时候产生（对方网络异常的情况下）
                 */
                inetAddress = InetAddress.getByName(ip)
                //socket = new DatagramSocket(port);
                socket = DatagramSocket(null)
                socket?.reuseAddress = true
                socket?.bind(InetSocketAddress(port!!))
                socket?.broadcast = true//fixme 默认就是true;为了以防万一，还是受到设置true;单播和广播的区别不是这个属性。区别是ip
                socket?.soTimeout = soTimeout//fixme 要在一开始的时候就设置。不要在发送或接收的时候设置。不然卡死。
            } catch (e: Exception) {
                KLoggerUtils.e(TAG + "打开异常" + e.message)
            }
    }

    /**
     * fixme Close udp socket 关闭
     */
    fun close() {
        stop()
        isCircleReceive = false//停止接收，不一定结束循环，但是关闭，肯定会接收循环。
        if (socket != null) {
            socket?.close()
        }
        inetAddress = null
        socket = null
    }


    /**
     * fixme broadcast message 发送广播
     * @param text 发送文本数据
     * @param callback 回调（判断是否发送成功）
     */
    fun send(text: String?, callback: ((state: KState) -> Unit)? = null) {
        if (socket == null || text == null || port == null || inetAddress == null || socket!!.isClosed) {
            return
        }
        GlobalScope.async {
            try {
                var text = text
                text = text.trim { it <= ' ' }
                var packetToSend = DatagramPacket(
                        text.toByteArray(), text.toByteArray().size, inetAddress, port!!)
                socket?.send(packetToSend)
                //回調
                callback?.let {
                    it(KState(true))//fixme 发送成功
                }
            } catch (e: Exception) {
                callback?.let {
                    it(KState(false, e.message))//fixme 发送失败
                }
                KLoggerUtils.e(TAG + "发送异常：\t" + e.message)
            }
        }
    }


    //是否停止接收（true停止，还没开始接受；false没有暂停，正在接收。）
    private var isStopedReceive = true
    private var receiveCallBack: ((msg: String) -> Unit)? = null
    //判断是否正在循环接收，防止多次操作。
    private var isCircleReceive = false

    /**
     * fixme 接收;回调返回接收到的数据（会循环不停的接收）
     * @param callback 回调，返回接收到的数据
     */
    fun onMessage(callback: ((msg: String) -> Unit)?) {
        if (socket == null || callback == null || socket!!.isClosed) {
            return
        }
        this.receiveCallBack = callback//fixme 回调函數实时更新
        isStopedReceive = false
        //是否正在接收
        if (isCircleReceive) {
            return//fixme 正在接收中。receive不能多次调用。不能对同一个流多次操作。
        }
        GlobalScope.async {
            try {
                while (!isStopedReceive && receiveCallBack != null && socket != null && !socket!!.isClosed) {
                    try {
                        isCircleReceive = true//fixme 正在循环接收
                        //创建一个byte数组用于接收，发送的数据不能超过这个大小
                        var byteArray = ByteArray(BUFFER_SIZE)
                        var packetToReceive = DatagramPacket(byteArray, byteArray.size)
                        //fixme 使用receive方法接收发送方所发送的数据,同时这也是一个阻塞的方法。只有接受到了数据之后，才会往下继续执行。
                        socket?.receive(packetToReceive)
                        receiveCallBack?.let {
                            var msg = String(packetToReceive.getData(), packetToReceive.getOffset(), packetToReceive.getLength())//字节数组
                            if (msg != null) {
                                it(msg)
                            }
                        }
                    } catch (e: Exception) {
                        //fixme socket关闭时，会发生一次接受一次；没关系。也就断开的时候发生一次而已。
                        //KLoggerUtils.e(TAG + "接收异常：\t" + e.message)
                    }
                }
                //跳出循环，接收停止！
                receiveCallBack = null
                isStopedReceive = true
                isCircleReceive = false//fixme 循环操作结束
            } catch (e: Exception) {
            }

        }
    }

    /**
     * fixme Stop to receive 停止接收（停止接收后，需要重新调用onMessage()进行重新接收）
     */
    fun stop() {
        isStopedReceive = true
        receiveCallBack = null
    }

    companion object {
        private val BUFFER_SIZE = 2048 //普遍的還有1024；一般都是1024的整数。
    }
}

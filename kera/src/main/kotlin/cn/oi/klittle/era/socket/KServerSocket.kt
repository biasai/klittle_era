package cn.oi.klittle.era.socket

import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.Deferred


/**
 * Socket服务端
 * 同一机器，同一应用。不能即做服务端，又做客户端。
 * 即，如果即做服务端，又做客户端。就会出现操作同一流的情况。而一个流是不能被同时操作的。尽量不要用同一个设置即做服务器又做客户端。最好用两个设备测试。
 */
open class KServerSocket(var ip: String? = KIpPort.getHostIp4(), var port: Int = KIpPort.port()) {
    var serverSocket: ServerSocket? = null
    var ipPort: String? = null
        get() {
            return KIpPort.getIpPort(ip, port)
        }

    //开启服务
    fun open(callback: ((state: KState) -> Unit)? = null) {
        //fixme 服务已经开启
        serverSocket?.let {
            if (!(serverSocket?.isClosed ?: true)) {
                callback?.let {
                    it(KState(true))//fixme 服务开启成功
                }
                return
            }
        }
        GlobalScope.async {
            try {
                //fixme 开始启动服务
                //没有指定本地其他Ip的时候,默认ip就是 0.0.0.0(即本机。运行在同一个机器上才有效)，如果ip显示的是 :	:: (这个就是0.0.0.0本机)
                //serverSocket = ServerSocket(port)//注意，传入端口号
                var backlog: Int = 200//backlog客户端连接请求的队列长度
                serverSocket = ServerSocket(port, backlog, InetAddress.getByName(ip))

                serverSocket?.let {
                    if (!(serverSocket?.isClosed ?: true)) {
                        callback?.let {
                            it(KState(true))//fixme 服务开启成功
                        }
                    }

                    while (!serverSocket!!.isClosed) {
                        //var msg = "无限循环等待下一个客服链接中。。。服务器地址:\t" + getIp() + "\t端口号:\t" + getPort()+"\t外网:\t"+HttpUtils.getInstance().netIp
                        var socket: Socket? = serverSocket?.accept()//等待用户链接，阻塞线程
                        socket?.let {
                            //msg = "客服端已链接，ip地址\t" + socket.inetAddress.hostAddress + "\t端口port：\t" + socket.port
                            async {
                                if (!socket.isClosed) {
                                    //fixme 新用户链接上
                                    var socketAddress = socket.remoteSocketAddress//一般是服务器端的Socket调用，意为获取远程Socket地址
                                    var ip = socket.inetAddress.hostAddress//Ip地址
                                    var port = socket.port
                                    var kSocket = KSocket(ip, port)
                                    kSocket.socket = socket
                                    onNewConnect?.let {
                                        it(kSocket)
                                    }
                                }
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                callback?.let {
                    it(KState(false, e.message))//fixme 服务开启失败
                }
            }
        }
    }

    //fixme 新用户连接时回调
    private var onNewConnect: ((kSocket: KSocket) -> Unit)? = null

    fun onNewConnect(onNewConnect: (kSocket: KSocket) -> Unit) {
        this.onNewConnect = onNewConnect
    }

    //ServerSocket的isBound()方法判断ServerSocket是否已经与一个端口绑定，只要ServerSocket已经与一个端口绑定，即使它已经被关闭，isBound()方法也会返回true。
    //关闭服务，使服务器释放占用的端口，并且断开与所有客户的连接
    fun close(): KServerSocket {
        serverSocket?.let {
            if (!it.isClosed) {
                serverSocket?.close()//只有关闭了，isClosed才会返回true,否则一直返回false
            }
        }
        return this
    }

    //ServerSocket的isClosed()方法判断ServerSocket是否关闭，只有执行了ServerSocket的close()方法，isClosed()方法才返回true；否则，即使ServerSocket还没有和特定端口绑定，isClosed()方法也会返回false。
    fun isClosed(): Boolean {
        return serverSocket?.isClosed ?: true
    }


}
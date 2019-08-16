package cn.oi.klittle.era.udp


import cn.oi.klittle.era.socket.KIpPort

/**
 * fixme 同一网段（同一路由器下有效。不同wifi只要在同一个路由器下都有效）
 * fixme 广播UDP与单播UDP的区别就是IP地址不同，广播使用的ip地址是：255.255.255.255，将消息发送到在同一广播网络上的每个主机
 *
 * fixme Udp 单播。 一对一。指定特定的ip地址
 * @param port 对方的端口号
 * @param ip 是对方的ip
 * @param soTimeout 超时时间(单位毫秒1000是一秒)，0默认是无限大。最好默认不要设置。因为设置之后，接收也会有超时时间限制。
 */
open class KUdpUnicast(override var ip: String? = KIpPort.getHostIp4(), override var port: Int? = KIpPort.port(), override var soTimeout: Int = 0) : KUdpBroadcast(port = port, ip = ip, soTimeout = soTimeout) {
    init {
        //因为父类无法访问子类的属性，所以子类必须要重新配置一下。
        //即使重写，父类依然无法访问子类的属性(ip,port等)
        open(ip, port, soTimeout)
    }
}

package cn.oi.klittle.era.socket

import android.util.Log
import cn.oi.klittle.era.utils.KLoggerUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.*
import java.util.regex.Pattern
import java.net.NetworkInterface.getNetworkInterfaces
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import cn.oi.klittle.era.base.KBaseApplication


/**
 * ip地址和端口号
 * 理论上IP的范围是0.0.0.0~255.255.255.255
 * 端口号为1~65535
 * 服务器端（本地服务器，必须确保客户端和服务器在同一局域网中才能通信）
 * 局域网，即同一个网段下，即IP地址的前三段必须相同。如：192.168.1.110，即192.168.1 前三段相同，就属于同一个网段。
 */
open class KIpPort {
    //内网IP4地址
    var hostIp4: String? = null
        get() {
            return getHostIp4()//防止更改，每次都实时获取。
        }
    //外网IP4地址,要放到后台线程里处理
    var netIp4: String? = null
        get() {
            return getNetIp4()
        }

    //当前要使用的ip4地址和端口号
    var Ip4: String? = hostIp4 //这里Ip4和hostIp4只是赋值。不是同一个对象。
    var Port = defaultPort
        set(value) {
            field = Companion.port(value)
        }

    companion object {
        var defaultPort = 31053//默认端口号
        //获取或设置端口号
        fun port(port: Int = defaultPort): Int {
            //端口号规定为16位，即允许一个IP主机有2的16次方65535个不同的端口
            //0~1023：分配给系统的端口号,如果设置端口为0，则系统会自动为其分配一个端口；
            //1024~49151：登记端口号，主要是让第三方应用使用
            //49152~65535：短暂端口号，是留给客户进程选择暂时使用，一个进程使用完就可以供其他进程使用。
            if (port < 1024 || port > 65535) {
                return defaultPort//默认端口号
            } else {
                return port
            }
        }

        //获取ip地址+端口号port 形式为 192.168.0.1:8889
        fun getIpPort(ip: String?, port: Int?): String? {
            return ip + ":" + port
        }

        //获取内网IP4地址【绝对可行】
        fun getHostIp4(): String? {
            var hostIp: String? = null
            try {
                val nis = NetworkInterface.getNetworkInterfaces()
                var ia: InetAddress? = null
                while (nis.hasMoreElements()) {
                    val ni = nis.nextElement() as NetworkInterface
                    val ias = ni.inetAddresses
                    while (ias.hasMoreElements()) {
                        ia = ias.nextElement()
                        if (ia is Inet6Address) {
                            continue// skip ipv6,跳过IP6地址
                        }
                        val ip = ia!!.hostAddress
                        if ("127.0.0.1" != ip) {
                            hostIp = ia.hostAddress
                            break
                        }
                    }
                }
            } catch (e: SocketException) {
                e.printStackTrace()
                KLoggerUtils.e("test", "IP获取异常:\t" + e.message)
            }
            return hostIp
        }

        //获取外网的IP(要访问Url，要放到后台线程里处理)，也是IP4地址
        fun getNetIp4(): String? {
            var infoUrl: URL? = null
            var inStream: InputStream? = null
            var ipLine: String? = null
            var httpConnection: HttpURLConnection? = null
            try {
                //infoUrl = new URL("http://ip168.com/");
                infoUrl = URL("http://pv.sohu.com/cityjson?ie=utf-8")
                val connection = infoUrl.openConnection()
                httpConnection = connection as HttpURLConnection
                val responseCode = httpConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    inStream = httpConnection.inputStream
                    val reader = BufferedReader(
                            InputStreamReader(inStream!!, "utf-8"))
                    val strber = StringBuilder()
                    var line: String? = reader.readLine()
                    while (line != null) {
                        strber.append(line!! + "\n")
                        line = reader.readLine()
                    }
                    val pattern = Pattern
                            .compile("((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))")
                    val matcher = pattern.matcher(strber.toString())
                    if (matcher.find()) {
                        ipLine = matcher.group()
                    }
                }
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    inStream!!.close()
                    httpConnection!!.disconnect()
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
            return ipLine
        }

        //获取内网IP6地址
        fun getHostIp6(): String? {
            try {
                val en = NetworkInterface
                        .getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val intf = en.nextElement()
                    val enumIpAddr = intf
                            .inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
                        if (!inetAddress.isLoopbackAddress) {
                            return inetAddress.hostAddress.toString()
                        }
                    }
                }
            } catch (ex: SocketException) {
            }

            return null
        }

        private var mac: String? = null
        //通过wifi（与wifi是否开启没有关系，亲测能够获取） 获取mac地址[物理地址，不会变。理论上是不变，因为是出厂时被厂家烧在网卡上的。但不能保证绝对。]
        fun getMacAddress(): String {
            if (mac == null) {
                var wifi = KBaseApplication.getInstance().baseContext.getSystemService(KBaseApplication.WIFI_SERVICE) as WifiManager
                var info = wifi.getConnectionInfo()
                mac = info.getMacAddress()
            }
            return mac!!
        }
    }

}
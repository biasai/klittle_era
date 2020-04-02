package cn.oi.klittle.era.bluetooth

import android.bluetooth.BluetoothSocket
import android.os.Build
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.socket.KState
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.utils.KStringUtils
import java.io.DataOutputStream
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
//                    fixme 实例案例
//                    //打开BluetoothServerSocket服务
//                    KBluetoothAdapter.openBluetoothServerSocket {  }
//                    //通过蓝牙名称进行设备连接
//                    KBluetoothAdapter.connectBluetooth("蓝牙名称"){
//                        //通过uuid连接到指定的BluetoothServerSocket服务
//                        it?.connectBluetoothServerSocket {
//                            it.onMessage {
//                                //接收信息
//                            }
//                            it.send("发送信息"){
//                                it.isSuccess//判断是否发送成功
//                                it.msg//发送失败原因，如果成功，返回发送的信息
//                            }
//                        }
//                    }

/**
 * BluetoothSocket 通过这个 Socket 就可以在这两个设备间传输数据了。
 * 获取 InputStream 和 OutputStream
 * 使用 read（byte[]）和 write（byte []）读取或者写入流式传输
 *
 * fixme 亲测，半径十米之内都可以发送和接收信息。
 * fixme 有些设备搜索不到，需要停留在蓝色设置界面才能搜索到蓝牙，比如PDA
 *
 */
class KBluetoothSocket(var bluetoothSocket: BluetoothSocket?) {
    var address: String? = null//mac地址
    var name: String? = null//蓝牙名称

    var onMessage: ((message: String) -> Unit)? = null
    //fixme 读取监听
    fun onMessage(callback: (message: String) -> Unit) {
        onMessage = callback
        readUTF()
    }

    //fixme 发送信息
    fun send(text: String, callback: ((state: KState) -> Unit)? = null) {
        send(text.toByteArray(), callback)
    }

    //向对方写入信息（可以多次调用）
    fun send(data: ByteArray, callback: ((state: KState) -> Unit)? = null) {
        if (isClose) {
            return
        }
        data?.let {
            if (it.size > 0) {
                GlobalScope.async {
                    try {
                        if (isConnect()) {
                            var out = DataOutputStream(bluetoothSocket?.outputStream)
                            //out.writeUTF(text)
                            out.write(data)
                            out.flush()//清除缓冲区
                            callback?.let {
                                it(KState(true, KStringUtils.bytesToString(data)))//fixme 发送成功记录一下发送的文本
                            }
                        } else {
                            callback?.let {
                                it(KState(false, KBaseUi.getString(R.string.kconnetfailure)))//连接失败
                            }
                        }
                    } catch (e: Exception) {
                        callback?.let {
                            it(KState(false, e.message))
                        }
                    }
                }
            }
        }
    }

    fun isConnect(): Boolean {
        if (isClose) {
            return false
        }
        bluetoothSocket?.let {
            if (it.isConnected) {
                return true
            }
        }
        return false
    }

    private var isReadUTF = false
    private var readErrorCount = 0
    private var len = 0
    private fun readUTF() {
        if (isReadUTF || isClose) {
            return
        }
        isReadUTF = true
        kotlin.run {
            GlobalScope.async {
                while (isConnect()) {
                    //无限循环读取
                    try {
                        bluetoothSocket?.getInputStream()?.let {

                            //fixme available()获取下一次读取流的长度;如果长度为0，read()读取的时候将被阻塞。
                            var buff = ByteArray(1024)//fixme 蓝牙Socket流的有效最大长度available()好像是990（大约一次只能读取340个左右的中文字符串）
                            len = it.read(buff)//fixme read(byte[])才能读取成功，其他方法不行(如：readUTF()会一直处于阻塞读取状态，不会返回)，亲测。
                            //KLoggerUtils.e("len：\t" + len)
                            //fixme 注意，len永远都不会为-1，因为read()是阻塞线程的。一定是有数据读取出来，才会返回值。所以不会返回-1
                            if (len != -1) {
                                KStringUtils.bytesToString(buff)?.let {
                                    readErrorCount = 0
                                    var text = it
                                    //KLoggerUtils.e("读取文本：\t" + text + "\t" + onMessage)
                                    onMessage?.let {
                                        it(text)//fixme 大约一次可以读取340个中文字符左右。 数据太多，蓝牙一次读不完，会分次读取。
                                    }
                                }
                            }

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        //Log.e("test", "读取异常:\t" + e.message)
                        readErrorCount++
                        if (readErrorCount > 10) {
                            break//结束循环
                        }
                    }
                }
                isReadUTF = false
            }
        }
    }


    private var isClose = false//是否关闭
    /**
     * fixme 关闭
     */
    fun close() {
        try {
            isClose = true
            bluetoothSocket?.inputStream?.close()
            bluetoothSocket?.outputStream?.close()
            bluetoothSocket?.close()
            bluetoothSocket = null
            name = null
            address = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
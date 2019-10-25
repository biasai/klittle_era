package cn.oi.klittle.era.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothSocket
import android.os.Build
import cn.oi.klittle.era.utils.KLoggerUtils
import kotlinx.coroutines.experimental.async
import java.lang.Exception
import java.util.*


/**
 * fixme 自定义蓝牙设备;简化，方便操作
 */
class KBluetoothDevice(var gatt: BluetoothGatt?) {
    var device: BluetoothDevice? = null//gatt?.device

    init {
        if (KBluetoothAdapter.isVersion18()) {
            device = gatt?.device
        }
    }

//                                                        if (Build.VERSION.SDK_INT >= 18) {
//                                                        //fixme gatt可以获取Service的UUID
//                                                        it?.gatt?.services?.forEach {
//                                                            KLoggerUtils.e("serviceUuid:\t" + it.uuid)
//                                                            //fixme Service可以获取characteristics通道的uuid
//                                                            it.characteristics?.forEach {
//                                                                KLoggerUtils.e("characteristicUuid:\t" + it.uuid)
//                                                            }
//                                                        }
//                                                    }

    //fixme 这两个uuid，都从硬件工程师哪里获取
    //var service=gatt?.getService(UUID.fromString("uuid格式"))
    //var characteristic=service?.getCharacteristic(UUID.fromString("uuid格式"))
    var characteristic: BluetoothGattCharacteristic? = null

    /**
     * 设置通知
     * @param bluetoothGattCharacteristic fixme 需要uuid（从硬件工程师哪里获取）
     * @param enable 设置为true以启用通知/指示;不要动，最好默认设置成true
     */
    fun setCharacteristicNotification(bluetoothGattCharacteristic: BluetoothGattCharacteristic?, enable: Boolean = true) {
        this.characteristic = bluetoothGattCharacteristic//fixme 保存当前的通道
        KBluetoothAdapter.setCharacteristicNotification(this.characteristic, gatt, enable)
    }

    /**
     * 设置通知
     * @param serviceUuid Service的UUID
     * @param characteristicUUID Characteristic的UUID
     */
    fun setCharacteristicNotification(serviceUuid: String, characteristicUUID: String, enable: Boolean = true) {
        var uuidService = UUID.fromString(serviceUuid)
        var uuidCharacteristic = UUID.fromString(characteristicUUID)
        setCharacteristicNotification(uuidService, uuidCharacteristic, enable)
    }

    fun setCharacteristicNotification(serviceUuid: UUID, characteristicUUID: UUID, enable: Boolean = true) {
        if (gatt != null && Build.VERSION.SDK_INT >= 18) {
            var service = gatt?.getService(serviceUuid)
            var characteristic = service?.getCharacteristic(characteristicUUID)
            setCharacteristicNotification(characteristic, enable)
        }
    }

    /**
     * 发送数据
     * @param msg 数据
     * @param callback 回调，true发送成功
     */
    fun send(msg: String?, callback: ((b: Boolean) -> Unit)? = null) {
        KBluetoothAdapter.send(characteristic, gatt, msg, callback)
    }

    fun send(msg: ByteArray?, callback: ((b: Boolean) -> Unit)? = null) {
        KBluetoothAdapter.send(characteristic, gatt, msg, callback)
    }

    var onMessage: ((message: String?) -> Unit)? = null
    //接收信息回调
    fun onMessage(onMessage: ((message: String?) -> Unit)? = null) {
        //接收信息
        this.onMessage = onMessage
    }

    /**
     * 判断是否连接
     */
    fun isConnect(): Boolean {
        gatt?.let {
            return KBluetoothAdapter.isConnect(it)
        }
        return false
    }

    /**
     * 断开连接
     */
    fun disConnect() {
        try {
            KBluetoothAdapter.disConnectBluetooth(gatt)
            if (KBluetoothAdapter.isVersion18()) {
                gatt?.disconnect()
                gatt?.close()
            }
            gatt = null
            characteristic = null
            onMessage = null
        } catch (e: Exception) {
            KLoggerUtils.e("KBluetoothDevice断开异常：\t" + e.message)
        }

    }

    /**
     * fixme 连接BluetoothSocket服务，并返回BluetoothSocket对象
     * BluetoothSocket 通过这个 Socket 就可以在这两个设备间传输数据了。
     * 获取 InputStream 和 OutputStream
     * 使用 read（byte[]）和 write（byte []）读取或者写入流式传输
     */
    fun connectBluetoothServerSocket(uuid: UUID = KBluetoothAdapter.uuid, callback: ((bluetoothSocket: BluetoothSocket) -> Unit)?) {
        if (device != null) {
            async {
                try {
                    var socket = device?.createRfcommSocketToServiceRecord(uuid)// 这里的 UUID 需要和服务器的一致
                    try {
                        if (socket != null) {
                            KBluetoothAdapter.cancleDiscovery()//关闭发现设备，连接设备的时候，最好每次都调用一下。
                            socket?.connect()//连接
                            KBluetoothAdapter.mSockets?.add(socket)//fixme 记录客户端
                            callback?.let {
                                it(socket)
                            }
                        } else {
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        socket?.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
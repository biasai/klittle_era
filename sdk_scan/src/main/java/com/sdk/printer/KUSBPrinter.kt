package com.sdk.printer

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import cn.oi.klittle.era.base.KBaseApplication

import java.io.UnsupportedEncodingException

//调用案例

//var usb: KUSBPrinter? = KUSBPrinter()

//        usb?.apply {
//            open()//fixme 打开初始化
//            onConnectSuccess {
//                runOnUiThread {
//                    KToast.showSuccess("连接成功：\t" + it.deviceName)
//                }
//            }
//            onConnectFailure {
//                runOnUiThread {
//                    KToast.showError("连接失败")
//                }
//            }
//            onUsbInsert {
//                runOnUiThread {
//                    KToast.showInfo("USB插入")
//                }
//            }
//            onUsbOut {
//                runOnUiThread {
//                    KToast.showInfo("USB拔出")
//                }
//            }
//        }

//                    //fixme 打印
//                    usb?.apply {
//                        if (isConnection()) {
//                            async {
//                                bold(true)//字体加粗
//                                setTextSize(3)//0:正常大小 1:两倍高 2:两倍宽 3:两倍大小
//                                setAlign(1)//设置对齐方式 0 左对齐；1居中；2右对齐；
//                                printTextNewLine("字体加粗；两倍大小；居中；换行打印文字==================")
//                                printLine(1)//打印空行
//                                bold(false)
//                                printTextNewLine("不加粗；换行打印文字")
//                                printTextNewLine("不加粗；换行打印文字")
//                                printBarCode("一维条码：\t6936983800013")//打印一维条形码
//                            }
//                        } else {
//                            runOnUiThread {
//                                KToast.showInfo("请先连接打印机")
//                            }
//                        }
//                    }

//usb?.close()//fixme 关闭
//usb = null

/**
 * USB连接打印机[对TSC的标签打印机好像无效；但是对中盈的针式打印机好像有效。(针式打印机一张纸很容易卡住)];打印效果和方法都不理想
 * Created by 彭治铭 on 2019/4/8.
 */
//记得在Actvity的onDestroy生命周期里调用USBPrinter.getInstance().close()方法
class KUSBPrinter {

    val ACTION_USB_PERMISSION = "com.usb.printer.USB_PERMISSION"
    var TIME_OUT = 50000//写入超时时间；单位毫秒

    private var mContext: Context? = null
    private var mPermissionIntent: PendingIntent? = null
    private var mUsbManager: UsbManager? = null
    private var mUsbDeviceConnection: UsbDeviceConnection? = null

    private var ep: UsbEndpoint? = null
    private var printerEp: UsbEndpoint? = null
    private var usbInterface: UsbInterface? = null

    private val mUsbDeviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            //Log.d("action", action);
            val mUsbDevice = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) && mUsbDevice != null) {
                        //Log.d("receiver", action);
                        connectUsbPrinter(mUsbDevice)
                    } else {
                        //ToastUtil.showShort(context, "USB设备请求被拒绝");
                        onConnectFailure?.let {
                            it()//fixme 连接失败
                        }
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                if (mUsbDevice != null) {
                    //ToastUtil.showShort(context, "有设备拔出");
                    mUsbDeviceConnection = null
                    onUsbOut?.let {
                        it()//fixme 有usb拔出
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {
                //ToastUtil.showShort(context, "有设备插入");
                if (mUsbDevice != null) {
                    if (mUsbDevice.getInterface(0).interfaceClass == 7) {
                        usbInterface = mUsbDevice.getInterface(0)
                    }
                    if (!mUsbManager!!.hasPermission(mUsbDevice)) {
                        mUsbManager!!.requestPermission(mUsbDevice, mPermissionIntent)
                    }
                    onUsbInsert?.let {
                        it()//fixme usb插入
                    }
                }
            }
        }
    }

    //连接成功【返回连接的设备】
    private var onConnectSuccess: ((mUsbDevice: UsbDevice) -> Unit)? = null

    fun onConnectSuccess(onConnectSuccess: ((mUsbDevice: UsbDevice) -> Unit)? = null) {
        this.onConnectSuccess = onConnectSuccess
    }

    //连接失败
    private var onConnectFailure: (() -> Unit)? = null

    fun onConnectFailure(onConnectFailure: (() -> Unit)? = null) {
        this.onConnectFailure = onConnectFailure
    }

    //USB插入
    private var onUsbInsert: (() -> Unit)? = null

    fun onUsbInsert(onUsbInsert: (() -> Unit)? = null) {
        this.onUsbInsert = onUsbInsert
    }

    //USB拔出
    private var onUsbOut: (() -> Unit)? = null

    fun onUsbOut(onUsbOut: (() -> Unit)? = null) {
        this.onUsbOut = onUsbOut
    }

    /**
     * 判断是否已连接
     *
     * @return
     */
    fun isConnection(): Boolean {
        if (mUsbDeviceConnection != null) {
            return true
        } else {
            return false
        }
    }

    /**
     * 打开(初始化)打印机，需要与close对应
     *
     * @param context 上下文
     */
    fun open() {
        mContext = KBaseApplication.getInstance()
        mUsbManager = mContext!!.getSystemService(Context.USB_SERVICE) as UsbManager
        mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, Intent(ACTION_USB_PERMISSION), 0)
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        mContext?.registerReceiver(mUsbDeviceReceiver, filter)
        // 列出所有的USB设备，并且都请求获取USB权限
        val deviceList = mUsbManager!!.deviceList
        for (device in deviceList.values) {
            if (device.getInterface(0).interfaceClass == 7) {
                usbInterface = device.getInterface(0)
                //Log.d("device", device.getProductName() + "     " + device.getManufacturerName());
                //Log.d("device", device.getVendorId() + "     " + device.getProductId() + "      " + device.getDeviceId());
                //Log.d("device", usbInterface.getInterfaceClass() + "");
                if (!mUsbManager!!.hasPermission(device)) {
                    mUsbManager!!.requestPermission(device, mPermissionIntent)
                } else {
                    connectUsbPrinter(device)
                }
                break
            }
        }

    }

    /**
     * 关闭；记得在Activity关闭时；调用一下。
     */
    fun close() {
        if (mUsbDeviceConnection != null) {
            mUsbDeviceConnection?.close()
            mUsbDeviceConnection = null
        }
        mContext?.unregisterReceiver(mUsbDeviceReceiver)
        mContext = null
        mUsbManager = null
        onConnectSuccess = null
        onConnectFailure = null
        onUsbInsert = null
        onUsbOut = null
    }

    //声明接口；必须；不然打印机没反应；
    fun claimInterface() {
        mUsbDeviceConnection?.claimInterface(usbInterface, true)//声明接口；必须；不然打印机没反应；
    }

    //释放接口。
    fun releaseInterface() {
        mUsbDeviceConnection?.releaseInterface(usbInterface)//释放接口。
    }

    //连接打印机
    private fun connectUsbPrinter(usbDevice: UsbDevice?) {
        //KToast.show("usbDevice:\t"+(usbDevice!=null)+"\t"+(usbInterface!=null)+"\t"+usbInterface?.endpointCount)
        if (usbDevice != null && usbInterface != null) {
            for (i in 0 until usbInterface!!.endpointCount) {
                ep = usbInterface?.getEndpoint(i)
                if (ep?.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    if (ep?.direction == UsbConstants.USB_DIR_OUT) {
                        mUsbDeviceConnection = mUsbManager?.openDevice(usbDevice)
                        printerEp = ep
                        if (mUsbDeviceConnection != null) {
                            //ToastUtil.showShort(mContext, "设备已连接");
                            //mUsbDeviceConnection?.claimInterface(usbInterface, true)//声明接口；必须；不然打印机没反应；
                            //mUsbDeviceConnection?.releaseInterface(usbInterface)//释放接口。
                            claimInterface()
                            onConnectSuccess?.let {
                                it(usbDevice)//fixme 连接成功
                            }
                            break
                        }
                    }
                }
            }
        }
    }

    /**
     * 写入数据；如果打印机没有纸；打印机会处于等待过程。（此时也会造成线程堵塞）
     * fixme 注意数据太多时；要自己记得\n换行（一行数据不要太多）；打印机没有那么智能会卡死的。
     * fixme 在打印机里面，\n的动作是必要的，如果没有\n的动作，打印机是不会打印出任何东西的
     * @param TIME_OUT 是超时设置；超过了这个时间；就算没有打完（或者没有打）；也会停止。
     */
    private fun write(bytes: ByteArray?, timeout: Int = TIME_OUT) {
        //单个write()不要放进协程里；防止多个write()只执行一个；最好多个write()一起放进协程里。
        if (mUsbDeviceConnection != null && bytes != null) {
            claimInterface()//最好声明一次。
            mUsbDeviceConnection?.bulkTransfer(printerEp, bytes, bytes!!.size, timeout)//这一步会线程阻塞。
            //releaseInterface()
        }
    }

    /**
     * 打印文字
     *
     * @param msg
     */
    fun printText(msg: String, timeout: Int = TIME_OUT) {
        var bytes = ByteArray(0)
        try {
            bytes = msg.toByteArray(charset("gbk"))//一般都是gbk格式；不然乱码。
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        write(bytes, timeout)
    }

    /**
     * 换行打印文字
     *
     * @param msg
     */
    fun printTextNewLine(msg: String, timeout: Int = TIME_OUT) {
        var bytes = ByteArray(0)
        try {
            bytes = msg.toByteArray(charset("gbk"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        write(("\n").toByteArray(), timeout)
        write(bytes, timeout)
    }

    /**
     * 打印空行
     *
     * @param size 空行的个数
     */
    fun printLine(size: Int, timeout: Int = TIME_OUT) {
        for (i in 0 until size) {
            printText("\n", timeout)
        }
    }

    /**
     * 设置字体大小
     *
     * @param size 0:正常大小 1:两倍高 2:两倍宽 3:两倍大小 4:三倍高 5:三倍宽 6:三倍大 7:四倍高 8:四倍宽 9:四倍大小 10:五倍高 11:五倍宽 12:五倍大小
     */
    fun setTextSize(size: Int, timeout: Int = TIME_OUT) {
        write(KESCUtil.setTextSize(size), timeout)
    }

    /**
     * 字体加粗
     *
     * @param isBold
     */
    fun bold(isBold: Boolean, timeout: Int = TIME_OUT) {
        if (isBold) {
            write(KESCUtil.boldOn(), timeout)
        } else {
            write(KESCUtil.boldOff(), timeout)
        }
    }

    /**
     * 打印一维条形码
     *
     * @param data
     */
    fun printBarCode(data: String, timeout: Int = TIME_OUT) {
        write(KESCUtil.getPrintBarCode(data, 5, 90, 5, 2), timeout)
    }

    /**
     * 设置对齐方式 0 左对齐；1居中；2右对齐；
     *
     * @param position
     */
    fun setAlign(position: Int, timeout: Int = TIME_OUT) {
        var bytes: ByteArray? = null
        when (position) {
            0 -> bytes = KESCUtil.alignLeft()
            1 -> bytes = KESCUtil.alignCenter()
            2 -> bytes = KESCUtil.alignRight()
        }
        write(bytes, timeout)
    }

    /**
     * 切纸(一般表示一页打印完成；感觉没什么用)
     */
    fun cutPager(timeout: Int = TIME_OUT) {
        write(KESCUtil.cutter(), timeout)
    }
}

package com.sdk.scan.utils

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.device.ScanDevice
import cn.oi.klittle.era.base.KBaseActivityManager
import cn.oi.klittle.era.base.KBaseApplication
import cn.oi.klittle.era.utils.KLoggerUtils
import com.sdk.scan.act.KScanActivity
import java.util.concurrent.TimeUnit
//import kotlinx.coroutines.experimental.async
//import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
/**
 * 扫描工具类;引用jar包scanSDK.jar;fixme 针对旧版PDA，品牌alps，型号Alps PL-40L有效。依赖 scanSDK.jar
 * Created by 彭治铭 on 2019/3/12
 */
object KScanUtils {

    private fun getContext(): Context {
        return KBaseApplication.getInstance()
    }

    private fun getActivity(): Activity? {
        return KBaseActivityManager.getInstance().stackTopActivity
    }

    private var mScanDevice: ScanDevice?
    private var isScanSupport: Boolean//设备是否支持扫描

    init {
        mScanDevice = null
        isScanSupport = true
        try {
            //fixme 判断一个类是否存在(亲测有效。)；注意对内部类无效：如;android.os.IScanService.Stub
            var stub = Class.forName("android.os.IScanService")
            //KLoggerUtils.e("stub:\t" + stub)
            if (stub != null) {
                stub = null
                mScanDevice = ScanDevice()
                if (mScanDevice != null) {
                    default()
                    //广播注册之所以延迟注册。是因为init还有实例化完成。必须实例化完成之后注册才有效。
                    GlobalScope.async {
                        delay(800L)
                        registerReceiver()//注册广播
                    }
                }
            } else {
                mScanDevice = null
                isScanSupport = false
            }
        } catch (e: Exception) {
            mScanDevice = null
            isScanSupport = false//设备不支持扫描
            //KLoggerUtils.e("扫描类初始化异常：\t" + e.message)
        }
        if (mScanDevice == null) {
            isScanSupport = false
        }
        //KLoggerUtils.e("是否支持扫描：\t" + isScanSupport)
    }

    /**
     * 默认状态
     */
    fun default() {
        openScan()//开启扫描
        //则设置为广播，0接收广播 1 直接输出到文本框
        setOutScanMode(0)
        // 如果扫描成功震动未开启，则开启
        setScanVibrate()
        // 开启提示音
        setScanBeep()
        //扫描成功后，结果后附加回车键
        setScanCodeEnterKey()
    }

    /**
     * 判断设备是否支持扫描
     */
    fun isScanSupport(): Boolean {
        return isScanSupport
    }

    /**
     * 判断扫描是否打开；true扫描已打开；false扫描已关闭
     */
    fun isScanOpened(): Boolean {
        mScanDevice?.let {
            return it.isScanOpened
        }
        return false
    }

    /**
     * 打开扫描
     */
    fun openScan() {
        if (!isScanOpened()) {
            mScanDevice?.openScan()
        }
    }

    /**
     * 关闭扫描
     */
    fun closeScan() {
        if (isScanOpened()) {
            mScanDevice?.closeScan()
        }
    }

    /**
     * 开始扫描
     */
    fun startScan() {
        mScanDevice?.startScan()
    }

    /**
     * 停止扫描(最好在onPause()中调用 )
     */
    fun stopScan() {
        //mScanDevice?.setScanLaserMode(8) // 8 结束扫描
        mScanDevice?.stopScan()
    }

    /**
     * 获取输出模式；0接收广播 ；1 直接输出到文本框(当前聚焦的editText文本框,自己定义的KEditText也行,直接输出到文本末尾，不会清空原有文本。textView不行。)
     */
    fun getOutScanMode(): Int {
        if (mScanDevice != null) {
            return mScanDevice!!.outScanMode
        }
        return 0
    }

    /**
     * 设置输入模式；0接收广播 ；1 直接输出到文本框(当前聚焦的editText文本框,自己定义的KEditText也行,直接输出到文本末尾，不会清空原有文本。textView不行)
     */
    fun setOutScanMode(mode: Int = 0) {
        mScanDevice?.setOutScanMode(mode)
    }

    /**
     *获取扫描结果后是否附加回车键
     */
    fun getScanCodeEnterKeyMode(): Boolean {
        if (mScanDevice != null) {
            return mScanDevice!!.scanCodeEnterKeyMode
        }
        return false
    }

    /**
     * 扫描结果后附加回车键（是在结果文本的前面添加回车键(即换行)）。【在文本模式下才有效，在广播模式下无效，广播模式下是没有回车的，就是纯文本】
     */
    fun setScanCodeEnterKey() {
        mScanDevice?.setScanCodeEnterKey()
    }

    /**
     * 扫描结果后不附加回车键。
     */
    fun setScanCodeNoEnterKey() {
        mScanDevice?.setScanCodeNoEnterKey()
    }

    /**
     * 功能：获取扫描成功时振动是否开启；
     * 返回值：True 振动功能开启；False 未开启。
     */
    fun getScanVibrateState(): Boolean {
        if (mScanDevice != null) {
            return mScanDevice!!.scanVibrateState
        }
        return false
    }

    /**
     * 功能：设置扫描成功时振动。
     */
    fun setScanVibrate() {
        mScanDevice?.setScanVibrate()
    }

    /**
     * 取消扫描成功震动
     */
    fun setScanUnVibrate() {
        mScanDevice?.setScanUnVibrate()
    }

    /**
     * 获取扫描成功时，是否开启提示音；true 开启；false未开启
     */
    fun getScanBeepState(): Boolean {
        if (mScanDevice != null) {
            return mScanDevice!!.scanBeepState
        }
        return false
    }

    /**
     * 设置扫描成功时，开启提示音
     */
    fun setScanBeep() {
        mScanDevice?.setScanBeep()
    }

    /**
     * 设置扫描成功时，关闭提示音
     */
    fun setScanUnBeep() {
        mScanDevice?.setScanUnBeep()
    }

    /**
     * 接收扫描结果广播[广播本身就是ui主线程]
     */
    private val mScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                if (KScanActivity.isFastScan()) {
                    return
                }
                val barocode = intent.getByteArrayExtra("barocode")
                val length = intent.getIntExtra("length", 0)
                val barcodeType = intent.getByteExtra("barcodeType", 0.toByte())
                val barcodeStr = String(barocode, 0, length)
                onScanResult(barcodeType, barcodeStr)
                stopScan()
            } catch (e: Exception) {
                KLoggerUtils.e("扫描广播接收异常：\t" + e.message)
            }
        }
    }


    /**
     * 处理扫描结果
     * 编码规则：
     *
     * @param barcodeType 类型
     * @param barcodeStr  内容
     */
    private fun onScanResult(barcodeType: Byte, barcodeStr: String) {
        //KLoggerUtils.e("类型：\t" + barcodeType + "\t内容：\t" + barcodeStr)
        onScanResult?.let {
            it(barcodeType, barcodeStr)
        }
    }

    /**
     * fixme 扫描结果回调(必须要先注册广播)
     */
    private var onScanResult: ((barcodeType: Byte, barcodeStr: String) -> Unit)? = null

    fun onScanResult(onScanResult: ((barcodeType: Byte, barcodeStr: String) -> Unit)? = null) {
        KScanUtils.onScanResult = onScanResult
    }

    private val SCAN_ACTION = "scan.rcv.message"
    private var isRegister = false//判断广播是否已经注册；防止重复注册奔溃
    /**
     * 广播注册，用于接收扫描回调。
     */
    fun registerReceiver() {
        if (!isRegister && mScanReceiver != null) {
            try {
                isRegister = true
                val filter = IntentFilter()
                filter.addAction(SCAN_ACTION)
                getContext()?.registerReceiver(mScanReceiver, filter)
            } catch (e: Exception) {
            }
        }
    }

    /**
     * 取消广播注册
     */
    fun unregisterReceiver() {
        if (isRegister&&mScanReceiver!=null) {
            try {
                getContext()?.unregisterReceiver(mScanReceiver)
                isRegister = false
            } catch (e: Exception) {
            }
        }
    }

}
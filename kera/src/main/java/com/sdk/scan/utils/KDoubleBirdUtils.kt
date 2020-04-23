package com.sdk.scan.utils

import android.app.ScansManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import cn.oi.klittle.era.base.KBaseApplication
import cn.oi.klittle.era.utils.KLoggerUtils
import com.sdk.scan.act.KScanActivity


/**
 * fixme 新版PDA-DoubleBird 扫描工具类;依赖 scansV1.2_20101119.jar
 */
object KDoubleBirdUtils {
    private fun getContext(): Context {
        return KBaseApplication.getInstance()
    }

    private val SCANS_SERVICE = "scans"
    private var mScansManager: ScansManager? = null

    init {
        getScansManager()
    }

    fun getScansManager(): ScansManager? {
        if (mScansManager == null && SCANS_SERVICE != null) {
            try {
                mScansManager = getContext().getSystemService(SCANS_SERVICE!!) as ScansManager?
                //0 广播模式----数据将会以广播的方式上报
                //1 直接输入模式---数据直接输入到当前焦点的文本框
                mScansManager?.inputMode = 0
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return mScansManager
    }

    fun stopScan() {
        getScansManager()?.stopScan()
    }

    /**
     * fixme 扫描结果回调(必须要先注册广播)
     */
    private var onScanResult: ((barcodeType: Byte, barcodeStr: String) -> Unit)? = null

    fun onScanResult(onScanResult: ((barcodeType: Byte, barcodeStr: String) -> Unit)? = null) {
        KDoubleBirdUtils.onScanResult = onScanResult
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
//                val barocode = intent.getByteArrayExtra("barocode")
//                val length = intent.getIntExtra("length", 0)
//                val barcodeType = intent.getByteExtra("barcodeType", 0.toByte())
//                val barcodeStr = String(barocode, 0, length)
                var barcodeStr = intent.getStringExtra("barcode")
                onScanResult(0, barcodeStr)
                stopScan()
                //KLoggerUtils.e("扫描广播接收：\t"+barcodeStr)
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

    private val SCAN_ACTION = "android.scanservice.action.UPLOAD_BARCODE_DATA"
    private var isRegister = false//判断广播是否已经注册；防止重复注册奔溃
    /**
     * 广播注册，用于接收扫描回调。
     */
    fun registerReceiver() {
        if (!isRegister && mScanReceiver != null) {
            try {
                getScansManager()
                isRegister = true
                val filter = IntentFilter()
                filter.addAction(SCAN_ACTION)
                //fixme getContext()使用Application的上下文；生命周期就是整个应用的生命周期。建议使用Application
                getContext()?.registerReceiver(mScanReceiver, filter)
                //KLoggerUtils.e("扫描广播注册")
            } catch (e: Exception) {
            }
        }
    }

    /**
     * 取消广播注册
     */
    fun unregisterReceiver() {
        if (isRegister && mScanReceiver != null) {
            try {
                getContext()?.unregisterReceiver(mScanReceiver)
                isRegister = false
            } catch (e: Exception) {
            }
        }
    }

}
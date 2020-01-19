package com.sdk.scan.act

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import cn.oi.klittle.era.base.KBaseActivityManager
import cn.oi.klittle.era.utils.KAppUtils
import cn.oi.klittle.era.utils.KStringUtils
import com.sdk.scan.utils.KScanReader
import com.sdk.scan.utils.KScanUtils
import java.lang.Exception

/**
 * 集成扫描功能和 NFC读卡功能
 * Created by 彭治铭 on 2019/3/13.
 */
open class KScanActivity : KRfidActivity() {

    //fixme 是否开启扫描
    open fun isEnableScan(): Boolean {
        return true//默认开启
    }

    //新增一个方法，控制扫描回调开关。
    open fun isEnableScan2(): Boolean {
        return true//默认开启
    }

    /**
     * fixme 扫描回调（isEnableScan()开启了才会回调）；交给子类重写
     */
    open fun onScanResult(barcodeType: Byte, barcodeStr: String) {}

    private var onScanResult: ((barcodeType: Byte, barcodeStr: String) -> Unit)? = null

    private fun onScanResult(onScanResult: ((barcodeType: Byte, barcodeStr: String) -> Unit)? = null) {
        this.onScanResult = onScanResult
    }

    private var isNewPdA_Alpas2 = false//是否为新版PDA
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            if (isEnableScan()) {
                //回调监听
                onScanResult { barcodeType, barcodeStr ->
                    if (isEnableScan2()) {
                        onScanResult(barcodeType, barcodeStr)
                    }
                }
                //监听扫描
                if (isNewPdA_Alpas()) {
                    isNewPdA_Alpas2 = true
                    //新版扫描注册
                    onResultReceiver()
                } else {
                    isNewPdA_Alpas2 = false
                    KScanUtils.registerReceiver()
                    //旧版本扫描注册
                    KScanUtils.onScanResult(onScanResult)//在finish和onpause里不对它进行回收了；防止没有反应。
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        if (scanReader != null) {
            scanReader?.stopScan()//停止扫描(关闭扫描灯)
        }
    }

    override fun onResume() {
        super.onResume()
//        if (scanReader != null) {
//            scanReader?.startScan()//启动扫描（会开启扫描灯的）
//        }
        try {
            if (isEnableScan()) {
                //监听扫描
                if (isNewPdA_Alpas2) {
                    //新版扫描注册
                } else {
                    //旧版本扫描注册;fixme 重新赋值扫描回调函数。防止跳转下一个Activity后，回调被覆盖。
                    KScanUtils.onScanResult(onScanResult)//在finish和onpause里不对它进行回收了；防止没有反应。
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 新版PDA扫描接收
     */
    private var scanReader: KScanReader? = null
    private var resultReceiver: BroadcastReceiver? = null
    private fun onResultReceiver() {
        try {
            if (isNewPdA_Alpas()) {
                if (scanReader == null) {
                    scanReader = KScanReader(this)
                    scanReader?.init()//初始扫描
                }
                if (resultReceiver == null) {
                    //接收返回数据
                    resultReceiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            try {
                                if (isFastScan()) {
                                    return//fixme 防止重复扫描
                                }
                                //fixme 只对当前Activity进行回调。
                                if (KBaseActivityManager.getInstance().stackTopActivity === getActivity()) {
                                    var barcode = intent.getByteArrayExtra(KScanReader.SCAN_RESULT)
                                    //Log.e("MainActivity", "barcode = " + String(barcode!!))
                                    if (barcode != null) {
                                        onScanResult?.let {
                                            it(0, KStringUtils.bytesToString(barcode).trim())
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    var filter = IntentFilter()
                    filter.addAction(KScanReader.ACTION_SCAN_RESULT)
                    registerReceiver(resultReceiver, filter)//广播注册接收
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun finish() {
        super.finish()
        try {
            if (isNewPdA_Alpas()) {
                scanReader?.closeScan()//关闭扫描
                if (resultReceiver != null) {
                    unregisterReceiver(resultReceiver)
                    resultReceiver = null
                }
                scanReader = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    companion object {
        // 两次扫描之间的点击间隔不能少于200毫秒
        var MIN_CLICK_DELAY_TIME_scan: Long = 200
        var lastClickTime_scan = 0L//记录最后一次刷卡时间

        //判断是否超快速，重复扫描
        fun isFastScan(): Boolean {
            var flag = false
            val curClickTime = System.currentTimeMillis()
            if (curClickTime - lastClickTime_scan <= MIN_CLICK_DELAY_TIME_scan) {
                flag = true//快速点击
            }
            lastClickTime_scan = curClickTime
            return flag
        }
    }

}
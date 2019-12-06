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
                    //新版扫描注册
                    onResultReceiver()
                } else {
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

}
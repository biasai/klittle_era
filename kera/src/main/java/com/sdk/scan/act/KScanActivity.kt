package com.sdk.scan.act

import android.os.Bundle
import com.sdk.scan.utils.KScanUtils
import java.lang.Exception

/**
 * 集成扫描功能和 NFC读卡功能
 * Created by 彭治铭 on 2019/3/13.
 */
open class KScanActivity : KNfcActivity() {

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
                KScanUtils.registerReceiver()
                onScanResult { barcodeType, barcodeStr ->
                    if (isEnableScan2()) {
                        onScanResult(barcodeType, barcodeStr)
                    }
                }
                //监听扫描
                KScanUtils.onScanResult(onScanResult)//在finish和onpause里不对它进行回收了；防止没有反应。
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
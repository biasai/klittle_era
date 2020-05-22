package cn.oi.klittle.era.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseActivityManager
import cn.oi.klittle.era.base.KBaseApplication
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.comm.KToast


/**
 * fixme 指纹开发工具类；参照：https://www.cnblogs.com/changyiqiang/p/11506692.html
 */
object KFingerprintUtils {

    fun getContext(): Context {
        return KBaseApplication.getInstance()
    }

    fun getActivity(): Activity? {
        return KBaseActivityManager.getInstance().stackTopActivity
    }

    open fun getString(id: Int): String {
        return KBaseUi.getString(id, null)
    }

    val support_success = 0//支持指纹
    val support_error_1 = 1
    val support_error_2 = 2
    val support_error_3 = 3
    val support_error_4 = 4

    /**
     * 判断是否支持指纹
     * @param isShowError 如果不支持指纹功能；是否显示错误原因。
     */
    fun isSupportFingerprint(isShowError: Boolean = true): Boolean {
        return supportFingerprint(isShowError) == support_success
    }

    /**
     * 判断是否支持指纹
     * @param isShowError 如果不支持指纹功能；是否显示错误原因。
     * @return 4表示支持。
     */
    fun supportFingerprint(isShowError: Boolean = true): Int {
        if (Build.VERSION.SDK_INT < 23) {
            KToast.showInfo(getString(R.string.ksysnotfinger))//您的系统版本过低，不支持指纹功能
            return support_error_1
        } else {
            //键盘锁管理者
            val keyguardManager: KeyguardManager = getContext()?.getSystemService(KeyguardManager::class.java)
            //指纹管理者
            val fingerprintManager: FingerprintManager = getContext()?.getSystemService(FingerprintManager::class.java)
            if (!fingerprintManager.isHardwareDetected) { //判断硬件支不支持指纹
                KToast.showInfo(getString(R.string.kdevicenotfinger))//您的手机不支持指纹功能
                return support_error_2
            } else if (!keyguardManager.isKeyguardSecure) { //还未设置锁屏
                KToast.showInfo(getString(R.string.kfinger_notsuoping))//您还未设置锁屏，请先设置锁屏并添加一个指纹
                return support_error_3
            } else if (!fingerprintManager.hasEnrolledFingerprints()) { //指纹未登记
                KToast.showInfo(getString(R.string.kfinger_needone))//您至少需要在系统设置中添加一个指纹
                return support_error_4
            }
        }
        return support_success
    }

}
package cn.oi.klittle.era.utils

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseActivityManager
import cn.oi.klittle.era.base.KBaseApplication
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.comm.KToast
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

//                            fixme 调用案例
//                            KFingerprintUtils.startListening(true) { isSuccess, info, isCancel ->
//                            //fixme isSuccess 是否认证成功；info 返回信息；isCancel 指纹回调是否已经结束。false 可以继续指纹认证。
//                            if (isSuccess) {
//                                //认证成功
//                            }
//                            if (isCancel) {
//                                //系统指纹回调已经结束
//                                KFingerprintUtils.stopListening()//fixme 停止监听，最好在Activity的onPause()里也调用一次。
//                            }
//                        }

/**
 * fixme 指纹开发工具类；参照：https://www.cnblogs.com/changyiqiang/p/11506692.html
 * fixme 指纹认证，认证的是手机系统指纹。即系统录入的指纹认证。调用的是系统的接口。
 * fixme app是不可以访问指纹信息的，所以每次验证的时候，都只能调用系统的验证方法。
 * Android从6.0系统开始就支持指纹认证功能了，指纹功能还需要有硬件支持才行
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
     * fixme 判断是否支持指纹
     * @param isShowError 如果不支持指纹功能；是否显示错误原因。
     */
    fun isSupportFingerprint(isShowError: Boolean = true): Boolean {
        return supportFingerprint(isShowError) == support_success
    }

    /**
     * fixme 判断是否支持指纹
     * @param isShowError 如果不支持指纹功能；是否显示错误原因。
     * @return 4表示支持。
     */
    fun supportFingerprint(isShowError: Boolean = true): Int {
        if (Build.VERSION.SDK_INT < 23) {
            KToast.showInfo(getString(R.string.ksysnotfinger))//您的系统版本过低，不支持指纹功能
            return support_error_1
        } else {
            //键盘锁管理者
            var keyguardManager: KeyguardManager = getContext()?.getSystemService(KeyguardManager::class.java)
            //指纹管理者
            var fingerprintManager: FingerprintManager = getContext()?.getSystemService(FingerprintManager::class.java)
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

    var keyStore: KeyStore? = null
    var provider: String = "AndroidKeyStore"
    var DEFAULT_KEY_NAME = "default_key";

    /**
     * fixme 初始化密钥，先调用initKey（）；再调用initCipher（）
     */
    private fun initKey() {
        if (Build.VERSION.SDK_INT >= 23) {//android 6.0及以上系统才支持。
            try {
                if (keyStore == null) {
                    keyStore = KeyStore.getInstance(provider)
                    keyStore?.load(null)
                    //秘钥生成器
                    val keyGenerator: KeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, provider)
                    val builder = KeyGenParameterSpec.Builder(DEFAULT_KEY_NAME,
                            KeyProperties.PURPOSE_ENCRYPT or
                                    KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                            .setUserAuthenticationRequired(true)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    keyGenerator.init(builder.build())
                    keyGenerator.generateKey()
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

    var cipher: Cipher? = null
    private fun initCipher() {
        try {
            if (cipher == null) {
                var key: SecretKey = keyStore?.getKey(DEFAULT_KEY_NAME, null) as SecretKey
                cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_CBC + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7)
                cipher?.init(Cipher.ENCRYPT_MODE, key)
            }
        } catch (e: java.lang.Exception) {
            throw java.lang.RuntimeException(e)
        }
    }

    var mCancellationSignal: CancellationSignal? = null
    var fingerprintManager: FingerprintManager? = null

    var isSelfCancelled = false//fixme 标识是否是用户主动取消的认证。true 取消了，就不会有回调了。

//                            fixme 调用案例
//                            KFingerprintUtils.startListening(true) { isSuccess, info, isCancel ->
//                            //fixme isSuccess 是否认证成功；info 返回信息；isCancel 指纹回调是否已经结束。false 可以继续指纹认证。
//                            if (isSuccess) {
//                                //认证成功
//                            }
//                            if (isCancel) {
//                                //系统指纹回调已经结束
//                                KFingerprintUtils.stopListening()//fixme 停止监听，最好在Activity的onPause()里也调用一次。
//                            }
//                        }

    /**
     * fixme 开始指纹认证监听
     * @param isShowInfo 是否显示提示信息
     * @param callback fixme 指纹认证回调；认证失败了，可以重复继续认证(一般可以尝试三次)。
     * fixme isSuccess 是否认证成功; info 成功或错误的信息； isCancel 回调是否取消。 true(指纹回调已取消，不会再回调)；false（指纹认证可以继续回调）
     */
    fun startListening(isShowInfo: Boolean = true, callback: ((isSuccess: Boolean, info: String?, isCancel: Boolean) -> Unit)? = null) {
        if (isSupportFingerprint(isShowInfo)) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (cipher == null) {
                    //fixme 初始化密钥
                    initKey()
                    initCipher()
                }
                if (cipher == null) {
                    return
                }
                isSelfCancelled = false//fixme 指纹认证，没有取消；取消了，就不会回调了。
                if (mCancellationSignal == null) {
                    mCancellationSignal = CancellationSignal()
                }
                if (fingerprintManager == null) {
                    fingerprintManager = getContext().getSystemService(FingerprintManager::class.java)
                }
                fingerprintManager?.authenticate(FingerprintManager.CryptoObject(cipher), mCancellationSignal, 0, object : FingerprintManager.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (fingerprintManager == null) {
                            return
                        }
                        //fixme 指纹验证失败，不可再验
                        //fixme 这个回调是系统的错误，无法再继续监听指纹回调；需要重新调用回调方法。startListening（）
                        var errString = errString?.toString()//系统返回的错误信息
                        if (errorCode == 7) {
                            errString = getString(R.string.kfinger_errorCode_7)//尝试次数过多，请稍后重试。
                        }
                        if (!isSelfCancelled) {
                            if (isShowInfo) {
                                KToast.showError(errString?.toString())
                            }
                            callback?.let { it(false, errString?.toString(), true) }
                        }
                        //KLoggerUtils.e("errorCode:\t" + errorCode + "\terrString:\t" + errString)
                    }

                    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
                        if (fingerprintManager == null) {
                            return
                        }
                        //fixme 指纹验证失败，可再验，
                        //fixme 这个错误回调，还可以继续监听指纹回调
                        var helpString = helpString?.toString()//系统返回的错误信息
                        if (helpCode == 2) {
                            helpString = getString(R.string.kfinger_helpCode_2)//无法处理指纹，请重试。
                        } else if (helpCode == 5) {
                            helpString = getString(R.string.kfinger_helpCode_5)//手指移动太快，请重试。
                        }
                        if (!isSelfCancelled) {
                            if (isShowInfo) {
                                KToast.showError(helpString?.toString())
                            }
                            callback?.let { it(false, helpString?.toString(), false) }
                        }
                        //KLoggerUtils.e("helpCode:\t" + helpCode + "\thelpString:\t" + helpString)
                    }

                    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
                        if (fingerprintManager == null) {
                            return
                        }
                        //fixme 认证成功，不会再回调;重新认证，需要重新调用认证方法。startListening（）
                        if (!isSelfCancelled) {
                            if (isShowInfo) {
                                KToast.showSuccess(getString(R.string.kfinger_success))//指纹认证成功
                            }
                            callback?.let { it(true, getString(R.string.kfinger_success), true) }
                        }
                    }

                    override fun onAuthenticationFailed() {
                        if (fingerprintManager == null) {
                            return
                        }
                        //fixme 指纹验证失败，可再验，
                        //fixme 这个错误回调，还可以继续监听指纹回调
                        if (!isSelfCancelled) {
                            if (isShowInfo) {
                                KToast.showInfo(getString(R.string.kfinger_fail))//指纹认证失败，请再试一次
                            }
                            callback?.let { it(false, getString(R.string.kfinger_fail), false) }
                        }
                    }
                }, null)
            }
        }
    }

    //fixme 停止指纹认证监听；最好在Activity的onPause()里调用一次。
    fun stopListening() {
        if (mCancellationSignal != null) {
            if (Build.VERSION.SDK_INT >= 16) {//14是4.0；16是android 4.1
                mCancellationSignal?.cancel()
            }
            mCancellationSignal = null
            isSelfCancelled = true
        }
        fingerprintManager = null
        keyStore = null
        cipher = null
    }
}
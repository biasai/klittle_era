package cn.oi.klittle.era.helper

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import cn.oi.klittle.era.R
import cn.oi.klittle.era.activity.camera.KCameraActivity
import cn.oi.klittle.era.activity.camera.KCameraRecorderActivity
import cn.oi.klittle.era.activity.ringtone.KRingtoneActivity
import cn.oi.klittle.era.activity.video.KScreenVideoActivity
import cn.oi.klittle.era.base.KBaseActivityManager
import cn.oi.klittle.era.base.KBaseApplication
import cn.oi.klittle.era.comm.KToast
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.utils.KPermissionUtils
import com.sdk.Qr_code.act.KQr_codeActivity
import com.sdk.Qr_code.act.KQr_codeResultActivity
import com.sdk.Qr_code.utils.KZxingUtils
import java.io.File

/**
 * Created by 彭治铭 on 2018/4/25.
 */
object KUiHelper {

    fun getContext(): Context {
        return KBaseApplication.getInstance()
    }

    fun getActivity(): Activity? {
        return KBaseActivityManager.getInstance().stackTopActivity
    }

    /**
     * 默认就从Res目录下读取
     * 获取String文件里的字符,<string name="names">你好%s</string>//%s 是占位符,位置随意
     * @param formatArgs 是占位符
     */
    open fun getString(id: Int, formatArgs: String? = null): String {
        if (formatArgs != null) {
            return getContext().resources.getString(id, formatArgs) as String
        }
        return getContext().getString(id) as String
    }

    /**
     * 获取String文件里的字符串數組
     */
    open fun getStringArray(id: Int): Array<String> {
        return getContext().resources.getStringArray(id)
    }

    //如：SettingActivity::class.java
    fun goActivity(clazz: Class<*>, nowActivity: Activity? = getActivity()) {
        nowActivity?.let {
            if (!it.isFinishing) {
                var intent = Intent(nowActivity, clazz)
                goActivity(intent, it)
            }
        }
    }

    fun goActivity(clazz: Class<*>, bundle: Bundle, nowActivity: Activity? = getActivity()) {
        nowActivity?.let {
            if (!it.isFinishing) {
                var intent = Intent(nowActivity, clazz)
                intent.putExtras(bundle)
                goActivity(intent, it)
            }
        }
    }

    private var goTime = 0L
    var goFastTime = 300L

    //防止极短时间内，重复跳转调用。
    fun goActivity(intent: Intent, nowActivity: Activity? = getActivity()) {
        try {
            if (System.currentTimeMillis() - goTime > goFastTime) {
                goTime = System.currentTimeMillis()
                nowActivity?.startActivity(intent)
                //fixme 进入动画，一般在startActivity()或startActivityForResult()之后调用有效。多次调用也有效，后面的会覆盖前面的。
                //fixme 退出动画，在super.finish()之后调用有效，多次调用也有效，后面的会覆盖前面的。
                //fixme 参数一，目标Activity的动画。参数二，当前Activity的动画效果。
                //目前动画，左进，右出。
                //overridePendingTransition是传统动画，5.0的转场动画效果不怎么好。不建议使用
                nowActivity?.overridePendingTransition(R.anim.kera_slide_in_right, R.anim.kera_slide_out_left)
            }
        } catch (e: Exception) {
            //e.printStackTrace()
            KLoggerUtils.e("goActivity()跳转异常：\t" + e.message, isLogEnable = true)
        }
    }

//    if (context instanceof Activity) {
//        //Activity才能添加FLAG_ACTIVITY_NEW_TASK fixme 注意，startActivityForResult 不要使用FLAG_ACTIVITY_NEW_TASK，不然无法正常回调
//        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//    }

    var requestCode = 0
    fun goActivityForResult(clazz: Class<*>, nowActivity: Activity? = getActivity()) {
        val intent = Intent(nowActivity, clazz)
        startActivityForResult(intent, nowActivity, requestCode)
    }

    fun goActivityForResult(clazz: Class<*>, bundle: Bundle, nowActivity: Activity? = getActivity()) {
        val intent = Intent(nowActivity, clazz)
        intent.putExtras(bundle)
        startActivityForResult(intent, nowActivity, requestCode)
    }

    fun goActivityForResult(intent: Intent, nowActivity: Activity? = getActivity()) {
        startActivityForResult(intent, nowActivity, requestCode)
    }

    fun goActivityForResult(clazz: Class<*>, nowActivity: Activity? = getActivity(), requestCode: Int) {
        val intent = Intent(nowActivity, clazz)
        startActivityForResult(intent, nowActivity, requestCode)
    }

    fun goActivityForResult(clazz: Class<*>, bundle: Bundle, nowActivity: Activity? = getActivity(), requestCode: Int) {
        val intent = Intent(nowActivity, clazz)
        intent.putExtras(bundle)
        startActivityForResult(intent, nowActivity, requestCode)
    }

    fun goActivityForResult(intent: Intent, nowActivity: Activity? = getActivity(), requestCode: Int) {
        startActivityForResult(intent, nowActivity, requestCode)
    }

    //防止极短时间内，重复跳转调用。
    private fun startActivityForResult(intent: Intent, nowActivity: Activity? = getActivity(), requestCode: Int) {
        try {
            if (System.currentTimeMillis() - goTime > goFastTime) {
                goTime = System.currentTimeMillis()
                nowActivity?.startActivityForResult(intent, requestCode)
                //fixme 进入动画，一般在startActivity()或startActivityForResult()之后调用有效。多次调用也有效，后面的会覆盖前面的。
                //fixme 退出动画，在super.finish()之后调用有效，多次调用也有效，后面的会覆盖前面的。
                //fixme 参数一，目标Activity的动画。参数二，当前Activity的动画效果。
                //目前动画，左进，右出。
                //overridePendingTransition是传统动画，5.0的转场动画效果不怎么好。不建议使用
                //参数一，目标Activity的动画。参数二，当前Activity的动画效果。
                nowActivity?.overridePendingTransition(R.anim.kera_slide_in_right, R.anim.kera_slide_out_left)
            }
        } catch (e: Exception) {
            //e.printStackTrace()
            KLoggerUtils.e("startActivityForResult()跳转异常：\t" + e.message, isLogEnable = true)
        }
    }

    var qrCallback: ((result: String) -> Unit)? = null

    /**
     * fixme 跳转到 二维码扫描界面
     * @param qrCallback 二维码扫描结果回调(返回选择)
     */
    fun goQr_codeActivity(nowActivity: Activity? = getActivity(), qrCallback: ((result: String) -> Unit)? = null) {
        try {
            //需要相机权限（必须）
            KPermissionUtils.requestPermissionsCamera {
                if (it) {
                    this.qrCallback = qrCallback
                    goActivityForResult(KQr_codeActivity::class.java, nowActivity, KZxingUtils.requestCode_Qr)
                } else {
                    KPermissionUtils.showFailure()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val qr_result = "kera_qr_result"

    /**
     * fixme 跳转到 二维码扫描结果显示界面
     * @param result 二维码扫描内容结果
     */
    fun goQr_codeResultActivity(result: String?, nowActivity: Activity? = getActivity()) {
        try {
            result?.trim()?.let {
                if (it.length > 0) {
                    var intent = Intent(nowActivity, KQr_codeResultActivity::class.java)
                    intent.putExtra(qr_result, result)
                    goActivity(intent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * fixme 跳转到 自定义相机拍摄
     * @param isBackCamera true后置摄像头；false前置摄像头
     */
    fun goCameraActivity(isBackCamera: Boolean = true, nowActivity: Activity? = getActivity()) {
        try {
            //需要相机权限（必须）
            KPermissionUtils.requestPermissionsCamera(nowActivity) {
                if (it) {
                    var intent = Intent(nowActivity, KCameraActivity::class.java)
                    intent.putExtra("isBackCamera", isBackCamera)
                    goActivity(intent)
                } else {
                    KPermissionUtils.showFailure()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * fixme 跳转到 自定义相机录像
     * @param isBackCamera true后置摄像头；false前置摄像头
     */
    fun goKCameraRecorderActivity(isBackCamera: Boolean = true, nowActivity: Activity? = getActivity()) {
        try {
            //fixme 1.录像视频的使用，还需要SD卡操作的权限哦。不然录像文件会报打不开的异常哦。
            KPermissionUtils.requestPermissionsStorage {
                if (it) {
                    //fixme 2.需要相机权限（必须）
                    KPermissionUtils.requestPermissionsCamera(nowActivity) {
                        if (it) {
                            //fixme 3.录像还需要录音权限;不然会异常报错。
                            KPermissionUtils.requestPermissionsRecording(nowActivity) {
                                if (it) {
                                    var intent = Intent(nowActivity, KCameraRecorderActivity::class.java)
                                    intent.putExtra("isBackCamera", isBackCamera)
                                    goActivity(intent)
                                } else {
                                    KPermissionUtils.showFailure()
                                }
                            }
                        } else {
                            KPermissionUtils.showFailure()
                        }
                    }
                } else {
                    KPermissionUtils.showFailure()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 跳转到 铃声选择界面
     */
    fun goRingtoneActivity(nowActivity: Activity? = getActivity()) {
        try {
            goActivity(KRingtoneActivity::class.java, nowActivity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val videoPath_key = "kvideoPath"
    var isPortrait_screenVideo: Boolean = false//全屏播放，是否竖屏。
    var process_msec_screenVideo: Int = 0//记录当前播放的进度

    /**
     * 跳转到 视频全屏播放界面
     * @param videoPath 视频播放路径（本地和网络都可以）
     * @param process_msec 当前播放进度。毫秒数。
     * @param isPortrait 是否竖屏；true  竖屏；false横屏
     */
    fun goScreenVideoActivity(nowActivity: Activity? = getActivity(), videoPath: String?, process_msec: Int = 0, isPortrait: Boolean = false) {
        try {
            videoPath?.trim()?.let {
                if (it.length > 0) {
                    File(it)?.let {
                        if (it.exists() && it.length() > 0) {
                            isPortrait_screenVideo = isPortrait
                            process_msec_screenVideo = process_msec
                            var bundle = Bundle()
                            bundle.putString(videoPath_key, videoPath)
                            goActivity(KScreenVideoActivity::class.java, bundle, nowActivity)
                        } else {
                            KToast.showError(getString(R.string.kpicture_video_error2))//视频不存在或已损坏
                        }
                    }
                }
            }
        } catch (e: Exception) {
            //e.printStackTrace()
            KLoggerUtils.e("goScreenVideoActivity()跳转异常：\t" + e.message, isLogEnable = true)
        }
    }

    /**
     * @param sharedElement fixme 共享元素（这里一般为VideoView）；过渡动画。
     */
    fun goScreenVideoActivity(nowActivity: Activity? = getActivity(), sharedElement: View?, videoPath: String?, process_msec: Int = 0, isPortrait: Boolean = false) {
        try {
            videoPath?.trim()?.let {
                if (it.length > 0) {
                    File(it)?.let {
                        if (it.exists() && it.length() > 0) {
                            isPortrait_screenVideo = isPortrait
                            process_msec_screenVideo = process_msec
                            var bundle = Bundle()
                            bundle.putString(videoPath_key, videoPath)
                            var intent = Intent(nowActivity, KScreenVideoActivity::class.java)
                            intent.putExtras(bundle)
                            goActivity(intent, sharedElement, nowActivity)
                        } else {
                            KToast.showError(getString(R.string.kpicture_video_error2))//视频不存在或已损坏
                        }
                    }
                }
            }
        } catch (e: Exception) {
            //e.printStackTrace()
            KLoggerUtils.e("goScreenVideoActivity()跳转异常2：\t" + e.message, isLogEnable = true)
        }
    }

//    if (Build.VERSION.SDK_INT>=21) {
//        transitionName = "sharedView"//fixme 共享元素名称；第一个Activity的View可以不写；第二个Activity的View必须写(最好还是写上)，不然没有效果。
//    }
    /**
     * fixme 5.0；api 21;共享元素；过渡动画。效果非常不错。
     * @param sharedElement fixme 第一个Activity的共享元素控件;必须设置 transitionName 元素名称。目前就只写一个共享元素的动画。多个元素不常用，就不写了（太麻烦了）。
     */
    fun goActivity(clazz: Class<*>, sharedElement: View?, nowActivity: Activity? = getActivity()) {
        var intent = Intent(nowActivity, clazz)
        goActivity(intent, sharedElement, nowActivity)
    }

    fun goActivity(intent: Intent, sharedElement: View?, nowActivity: Activity? = getActivity()) {
        if (Build.VERSION.SDK_INT >= 21 && sharedElement != null && sharedElement.transitionName != null) {
            try {
                if (System.currentTimeMillis() - goTime > goFastTime) {
                    goTime = System.currentTimeMillis()
                    //防止极短的时间内重复跳转
                    //fixme 共享元素动画跳转；在第一个Activity跳转的时候，和第二个Activity关闭(finish())的时候也会有效果。
                    //var intent = Intent(nowActivity, clazz)
                    nowActivity?.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(nowActivity, sharedElement, sharedElement.transitionName).toBundle())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                KLoggerUtils.e("共享元素跳转异常：\t" + e.message, isLogEnable = true)
            }
        } else {
            //普通正常跳转
            goActivity(intent, nowActivity)
        }
    }

    fun goActivityForResult(clazz: Class<*>, sharedElement: View?, nowActivity: Activity? = getActivity(), requestCode: Int = this.requestCode) {
        var intent = Intent(nowActivity, clazz)
        goActivityForResult(intent, sharedElement, nowActivity, requestCode)
    }

    fun goActivityForResult(intent: Intent, sharedElement: View?, nowActivity: Activity? = getActivity(), requestCode: Int = this.requestCode) {
        try {
            if (Build.VERSION.SDK_INT >= 21 && sharedElement != null && sharedElement.transitionName != null) {
                if (System.currentTimeMillis() - goTime > goFastTime) {
                    goTime = System.currentTimeMillis()
                    nowActivity?.startActivityForResult(intent, requestCode, ActivityOptions.makeSceneTransitionAnimation(nowActivity, sharedElement, sharedElement.transitionName).toBundle())
                }
            } else {
                startActivityForResult(intent, nowActivity, requestCode)
            }
        } catch (e: Exception) {
            //e.printStackTrace()
            KLoggerUtils.e("goActivityForResult()共享元素跳转异常：\t" + e.message, isLogEnable = true)
        }
    }

}
package cn.oi.klittle.era.helper

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import cn.oi.klittle.era.R
import cn.oi.klittle.era.activity.ringtone.KRingtoneActivity
import cn.oi.klittle.era.activity.video.KScreenVideoActivity
import cn.oi.klittle.era.base.KBaseActivity
import cn.oi.klittle.era.base.KBaseActivityManager
import cn.oi.klittle.era.base.KBaseApplication
import cn.oi.klittle.era.utils.KPermissionUtils
import com.sdk.Qr_code.act.KCaptureActivity
import com.sdk.Qr_code.utils.KZxingUtils

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
                //fixme 进入动画，一般在startActivity()之后调用有效。多次调用也有效，后面的会覆盖前面的。
                //fixme 退出动画，在finish()之后调用有效，多次调用也有效，后面的会覆盖前面的。
                //fixme 参数一，目标Activity的动画。参数二，当前Activity的动画效果。
                //目前动画，左进，右出。
                //overridePendingTransition是传统动画，5.0的转场动画效果不怎么好。不建议使用
                nowActivity?.overridePendingTransition(R.anim.kera_slide_in_right, R.anim.kera_slide_out_left)
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
                //参数一，目标Activity的动画。参数二，当前Activity的动画效果。
                nowActivity?.overridePendingTransition(R.anim.kera_slide_in_right, R.anim.kera_slide_out_left)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var qrCallback: ((result: String) -> Unit)? = null
    /**
     * 跳转到 二维码扫描界面
     * @param qrCallback 二维码扫描结果回调(返回选择)
     */
    fun goCaptureActivity(nowActivity: Activity? = getActivity(), qrCallback: ((result: String) -> Unit)? = null) {
        try {
            //需要相机权限（必须）
            KPermissionUtils.requestPermissionsCamera {
                if (it) {
                    this.qrCallback = qrCallback
                    goActivityForResult(KCaptureActivity::class.java, nowActivity, KZxingUtils.requestCode_Qr)
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

    val videoPath_key = "videoPath"
    /**
     * 跳转到 视频全屏播放界面
     * @param videoPath 视频播放路径（本地和网络都可以）
     */
    fun goScreenVideoActivity(nowActivity: Activity? = getActivity(), videoPath: String?) {
        try {
            var bundle = Bundle()
            bundle.putString(videoPath_key, videoPath)
            goActivity(KScreenVideoActivity::class.java, bundle, nowActivity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
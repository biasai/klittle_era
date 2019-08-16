package cn.oi.klittle.era.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import cn.oi.klittle.era.base.KBaseActivityManager
import cn.oi.klittle.era.base.KBaseApplication

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
    var goFastTime=300L
    //防止极短时间内，重复跳转调用。
    fun goActivity(intent: Intent, nowActivity: Activity? = getActivity()) {
        if (System.currentTimeMillis() - goTime > goFastTime) {
            goTime = System.currentTimeMillis()
            nowActivity?.startActivity(intent)
        }
    }

//    if (context instanceof Activity) {
//        //Activity才能添加FLAG_ACTIVITY_NEW_TASK fixme 注意，startActivityForResult 不要使用FLAG_ACTIVITY_NEW_TASK，不然无法正常回调
//        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//    }

    var requestCode = 0
    fun goActivityForResult(clazz: Class<*>, nowActivity: Activity? = getActivity()) {
        val intent = Intent(nowActivity, clazz)
        startActivityForResult(intent,nowActivity, requestCode)
    }

    fun goActivityForResult(clazz: Class<*>, bundle: Bundle, nowActivity: Activity? = getActivity()) {
        val intent = Intent(nowActivity, clazz)
        intent.putExtras(bundle)
        startActivityForResult(intent,nowActivity, requestCode)
    }

    fun goActivityForResult(intent: Intent, nowActivity: Activity? = getActivity()) {
        startActivityForResult(intent,nowActivity, requestCode)
    }

    fun goActivityForResult(clazz: Class<*>, nowActivity: Activity? = getActivity(), requestCode: Int) {
        val intent = Intent(nowActivity, clazz)
        startActivityForResult(intent,nowActivity, requestCode)
    }

    fun goActivityForResult(clazz: Class<*>, bundle: Bundle, nowActivity: Activity? = getActivity(), requestCode: Int) {
        val intent = Intent(nowActivity, clazz)
        intent.putExtras(bundle)
        startActivityForResult(intent,nowActivity, requestCode)
    }

    fun goActivityForResult(intent: Intent, nowActivity: Activity? = getActivity(), requestCode: Int) {
        startActivityForResult(intent,nowActivity, requestCode)
    }

    //防止极短时间内，重复跳转调用。
    private fun startActivityForResult(intent: Intent, nowActivity: Activity? = getActivity(), requestCode: Int){
        if (System.currentTimeMillis() - goTime > goFastTime) {
            goTime = System.currentTimeMillis()
            nowActivity?.startActivityForResult(intent, requestCode)
        }
    }

}
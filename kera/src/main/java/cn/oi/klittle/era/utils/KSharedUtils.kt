package cn.oi.klittle.era.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import android.util.Log
import cn.oi.klittle.era.R

import java.io.File

import cn.oi.klittle.era.base.KBaseApplication
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.comm.KToast


/**
 * 安卓原生分享
 * Created by 彭治铭 on 2018/3/13.
 */

object KSharedUtils {

    //分享弹窗标题，通过方法获取（有利于中英文实时切换）
    fun getDialogTitle(): String {
        return KBaseUi.getString(R.string.kdialogTitle)// "独乐乐不如众乐乐"//分享弹窗的标题
    }

    var packageQQ = "com.tencent.mobileqq"//QQ
    var packageQQ_Lite = "com.tencent.qqlite"//QQ极速版
    var packageWX = "com.tencent.mm"//微信
    var packageWB = "com.sina.weibo"//新浪微博

    /**
     * 判断是否安装腾讯、新浪等指定的分享应用
     *
     * @param packageName 应用的包名
     */
    fun checkPackage(packageName: String): Boolean {
        try {
            KBaseApplication.getInstance().packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            //Log.e("test", "包名存在:\t" + packageName);
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            //Log.e("test", "不存在存在:\t" + packageName);
            return false
        }

    }

    /**
     * 分享功能【多个选择会弹出系统选择框，如果只有一个则不会弹出，直接跳转该应用，如ComponentName】
     *
     * @param msgTitle 分享主题【空间里的主题，就普通分享一个好友，是没有显示的。】
     * @param msgText  消息内容
     * @param imgPath  图片路径，不分享图片则传null
     * @param Package  fixme 分享应用包名。 通过包名指定具体的目标应用(亲测可以直接分享到QQ和微信)。可以为空（分享所有）
     * @param dialogTitle    弹框标题(即分享弹出框的标题)
     * @param activity
     */
    fun sharedMsg(msgTitle: String, msgText: String, imgPath: String? = null, Package: String? = null, dialogTitle: String = getDialogTitle(), activity: Activity? = KBaseUi.getActivity()) {
        if (activity == null) {
            return
        }
        try {
            val intent = Intent(Intent.ACTION_SEND)
            if (Package != null && Package.trim { it <= ' ' } != "") {
                intent.setPackage(Package)//通过指定包名，去筛选要分享的应用，如果不写，那么有能够分享的应用都会显示出来
            }
            if (imgPath == null || imgPath == "") {
                intent.type = "text/plain" // 纯文本
            } else {
                val f = File(imgPath)
                if (f != null && f.exists() && f.isFile) {
                    intent.type = "image/png" //fixme 图片分享，优先处理

                    var fileUri: Uri? = null
                    if (Build.VERSION.SDK_INT >= 23) {//7.0及以上版本(版本号24),为了兼容6.0(版本号23)，防止6.0也可能会有这个问题。
                        //getPackageName()和${applicationId}显示的都是当前应用的包名。无论是在library还是moudle中，都是一样的。都显示的是当前应用moudle的。与类库无关。请放心使用。
                        fileUri = FileProvider.getUriForFile(activity, activity.packageName + ".kera.provider", //与android:authorities="${applicationId}.provider"对应上
                                f)
                    } else {
                        fileUri = Uri.fromFile(f)
                    }
                    //share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                    intent.putExtra(Intent.EXTRA_STREAM, fileUri)
                    //以下两个addFlags必不可少。【以防万一出错】
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                    //                    Uri u = Uri.fromFile(f);
                    //                    intent.putExtra(Intent.EXTRA_STREAM, u);
                    intent.putExtra(Intent.EXTRA_STREAM, fileUri)

                }
            }
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle)
            intent.putExtra(Intent.EXTRA_TEXT, msgText)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            activity.startActivity(Intent.createChooser(intent, dialogTitle))//添加了Task，必须使用Acitivity,不然报错。

        } catch (e: Exception) {
            Log.e("test", "分享异常:\t" + e.message)
        }

    }

    /**
     * QQ文本分享；亲测有效。
     * @param callback fixme 回调，判断是否安装了该应用;true 表示安装了该应用，能够分享；false 没有安装该应用，不能分享
     */
    fun sharedMsgQQ(msgTitle: String, msgText: String, imgPath: String? = null, dialogTitle: String = getDialogTitle(), activity: Activity? = KBaseUi.getActivity(), callback: ((b: Boolean) -> Unit)? = null) {
        if (activity == null) {
            return
        }
        if (checkPackage(packageQQ)) {
            //正常QQ
            callback?.let { it(true) }
            sharedMsg(msgTitle, msgText, imgPath, packageQQ, dialogTitle, activity)
        } else {
            //QQ极速版
            if (checkPackage(packageQQ_Lite)) {
                callback?.let { it(true) }
                sharedMsg(msgTitle, msgText, imgPath, packageQQ_Lite, dialogTitle, activity)
            } else {
                if (callback != null) {
                    callback(false)
                } else {
                    KToast.showInfo(KBaseUi.getString(R.string.kinstallqq))//请先安装QQ
                }
            }
        }
    }


    /**
     * 微信文本分享
     */
    fun sharedMsgWX(msgTitle: String, msgText: String, imgPath: String? = null, dialogTitle: String = getDialogTitle(), activity: Activity? = KBaseUi.getActivity(), callback: ((b: Boolean) -> Unit)? = null) {
        if (activity == null) {
            return
        }
        if (checkPackage(packageWX)) {
            callback?.let { it(true) }
            sharedMsg(msgTitle, msgText, imgPath, packageWX, dialogTitle, activity)
        } else {
            if (callback != null) {
                callback(false)
            } else {
                KToast.showInfo(KBaseUi.getString(R.string.kinstallww))//"请先安装微信"
            }
        }
    }

    /**
     * 新浪微博文本分享
     */
    fun sharedMsgWB(msgTitle: String, msgText: String, imgPath: String? = null, dialogTitle: String = getDialogTitle(), activity: Activity? = KBaseUi.getActivity(), callback: ((b: Boolean) -> Unit)? = null) {
        if (activity == null) {
            return
        }
        if (checkPackage(packageWB)) {
            callback?.let { it(true) }
            sharedMsg(msgTitle, msgText, imgPath, packageWB, dialogTitle, activity)
        } else {
            if (callback != null) {
                callback(false)
            } else {
                KToast.showInfo(KBaseUi.getString(R.string.kinstallwb))//请先安装微博
            }
        }
    }

    /**
     * 短信分享
     *
     * @param activity
     * @param smstext  短信分享内容
     * @return
     */
    fun sharedSendSms(smstext: String, activity: Activity? = KBaseUi.getActivity()) {
        if (activity == null) {
            return
        }
        val smsToUri = Uri.parse("smsto:")
        val mIntent = Intent(Intent.ACTION_SENDTO, smsToUri)
        mIntent.putExtra("sms_body", smstext)
        activity?.startActivity(mIntent)
    }

    /**
     * 邮件分享
     *
     * @param title    邮件的标题
     * @param text     邮件的内容
     * @param activity
     * @return
     */
    fun sharedSendMail(title: String, text: String, activity: Activity? = KBaseUi.getActivity()) {
        if (activity == null) {
            return
        }
        // 调用系统发邮件
        val emailIntent = Intent(Intent.ACTION_SEND)
        // 设置文本格式
        emailIntent.type = "text/plain"
        // 设置对方邮件地址
        emailIntent.putExtra(Intent.EXTRA_EMAIL, "")
        // 设置标题内容
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, title)
        // 设置邮件文本内容
        emailIntent.putExtra(Intent.EXTRA_TEXT, text)
        activity?.startActivity(Intent.createChooser(emailIntent, "Choose Email Client"))
    }

    // 調用系統方法分享文件
    fun shareFile(file: File?, dialogTitle: String = KBaseUi.getString(R.string.ksharefile), activity: Activity? = KBaseUi.getActivity()) {//"分享文件"
        if (activity == null) {
            return
        }
        var context = activity
        if (null != file && file.exists()) {
            val share = Intent(Intent.ACTION_SEND)

            var fileUri: Uri? = null
            if (Build.VERSION.SDK_INT >= 23) {//7.0及以上版本(版本号24),为了兼容6.0(版本号23)，防止6.0也可能会有这个问题。
                //getPackageName()和${applicationId}显示的都是当前应用的包名。无论是在library还是moudle中，都是一样的。都显示的是当前应用moudle的。与类库无关。请放心使用。
                fileUri = FileProvider.getUriForFile(context, context.packageName + ".kera.provider", //与android:authorities="${applicationId}.provider"对应上
                        file)
            } else {
                fileUri = Uri.fromFile(file)
            }
            //share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            share.putExtra(Intent.EXTRA_STREAM, fileUri)
            //以下两个addFlags必不可少。【以防万一出错】
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            share.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            share.type = getMimeType(file.absolutePath)//此处可发送多种文件
            share.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(Intent.createChooser(share, dialogTitle))
        } else {
            KToast.showError(KBaseUi.getString(R.string.ksharefilenot))//"分享文件不存在"
        }
    }

    // 根据文件后缀名获得对应的MIME类型。
    fun getMimeType(filePath: String?): String {
        val mmr = MediaMetadataRetriever()
        var mime = "*/*"
        if (filePath != null) {
            try {
                mmr.setDataSource(filePath)
                mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            } catch (e: IllegalStateException) {
                return mime
            } catch (e: IllegalArgumentException) {
                return mime
            } catch (e: RuntimeException) {
                return mime
            }

        }
        return mime
    }

}

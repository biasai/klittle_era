package cn.oi.klittle.era.dialog

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.TextView
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseApplication
import cn.oi.klittle.era.base.KBaseDialog
import cn.oi.klittle.era.comm.KToast
import cn.oi.klittle.era.utils.*
import cn.oi.klittle.era.widget.KNumberProgressBar
import org.jetbrains.anko.runOnUiThread
import java.io.File
import java.lang.Exception

/**
 * 版本更新
 * 使用说明：KVersionUpdateDialog(this).setUrl(url).setSrcFileName("app名称带后缀（如果为null或""空，会自动获取网络上的名称）.apk")
 */
open class KVersionUpdateDialog(ctx: Context, isStatus: Boolean = true, isTransparent: Boolean = true) : KBaseDialog(ctx, R.layout.kera_dialog_version_update, isStatus, isTransparent) {
    //进度条
    val numprogressbar: KNumberProgressBar? by lazy { findViewById<KNumberProgressBar>(R.id.numprogressbar) }

    //apk下载链接
    var url: String? = null

    //文件名，包括后缀。如果为null或""空，会自动获取网络上的名称。
    var srcFileName: String? = null

    open fun setUrl(url: String): KVersionUpdateDialog {
        this.url = url
        return this
    }

    open fun setSrcFileName(srcFileName: String): KVersionUpdateDialog {
        this.srcFileName = srcFileName
        return this
    }

    init {
        if (ctx is Activity) {
            KProportionUtils.getInstance().adapterWindow(ctx, dialog?.window)//适配
        }
        //取消
        findViewById<View>(R.id.crown_txt_cancel)?.setOnClickListener {
            dismiss()
        }
        //更新
        findViewById<View>(R.id.crown_txt_ok)?.setOnClickListener {
            loadDown()
        }
        findViewById<TextView>(R.id.crown_txt_version_name)?.setText(getString(R.string.kfaxianxinbanben) + ":" + KBaseApplication.getInstance().versionName)//"发现新版本："
        isDismiss(false)//触摸屏幕不会消失
    }

    //fixme 下载
    open fun loadDown() {
        try {
            ctx?.runOnUiThread {
                isLoading = true
                isLoading()
                url?.let {
                    //fixme 默认为apk下载。
                    KFileLoadUtils.getInstance(true).downLoad(ctx, url, srcFileName, object : KFileLoadUtils.RequestCallBack {
                        override fun onStart() {
                            //开始下载
                        }

                        override fun onFailure(isLoad: Boolean?, result: String?, code: Int, file: File?) {
                            isLoading = false
                            dismiss()
                            ctx?.runOnUiThread {
                                //下载失败
                                if (isLoad!! && ctx != null) {
                                    //已经下载(进行安装)
                                    file?.let {
                                        KAppUtils.installation(ctx, file)
                                    }
                                } else {
                                    if (result != null) {
                                        //KToast.showError(result)
                                        showError(result, code)
                                    } else {
                                        //KToast.showError(getString(R.string.kappdownfail))//下载失败
                                        showError(getString(R.string.kappdownfail), code)
                                    }
                                }
                            }
                        }

                        override fun onSuccess(file: File?) {
                            isLoading = false
                            dismiss()
                            ctx?.runOnUiThread {
                                //下载完成安装
                                if (ctx != null && file != null) {
                                    KAppUtils.installation(ctx, file)
                                    numprogressbar?.setProgress(0)//进度条恢复到0
                                }
                                //KLoggerUtils.e("下载文件：\t"+file?.absolutePath+"\t大小：\t"+KStringUtils.getDataSize(file?.length()))
                            }
                        }

                        override fun onLoad(current: Long, max: Long, bias: Int) {
                            if (current < max) {
                                isLoading = true
                                isLoading()
                            } else {
                                isLoading = false
                            }
                            ctx?.runOnUiThread {
                                //下载进度
                                numprogressbar?.setProgress(bias)
                            }
                        }
                    })
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            dismiss()
        }
    }

    //显示错误信息
    open fun showError(result: String?, code: Int) {
        //下载失败；错误代码：code
        if (result != null) {
            KToast.showError(result + ";" + getString(R.string.kerror_code) + ":\t" + code)
        }
    }

    var isForceLoad = false//是否强制下载

    //判断是否强制下载
    fun isForceLoad() {
        try {
            ctx?.runOnUiThread {
                if (ctx != null) {
                    if (isForceLoad) {
                        //强制下载
                        isLocked(true)//屏蔽返回键
                        findViewById<View>(cn.oi.klittle.era.R.id.crown_txt_cancel)?.visibility = View.INVISIBLE//取消按钮不显示。
                    } else {
                        isLocked(false)//不屏蔽返回键
                        findViewById<View>(cn.oi.klittle.era.R.id.crown_txt_cancel)?.visibility = View.VISIBLE//显示取消按钮。
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var isLoading = false//是否正在下载

    //判断是否正在下载
    fun isLoading() {
        try {
            ctx?.runOnUiThread {
                if (ctx != null) {
                    if (isLoading) {
                        //正在下载
                        findViewById<View>(R.id.crown_update)?.visibility = View.INVISIBLE//隐藏更新弹框
                        findViewById<View>(R.id.crown_progress)?.visibility = View.VISIBLE//显示进度条
                        if (isForceLoad) {
                            isForceLoad = false//fixme 正在下载的时候，就不强制更新了，可以右键返回关闭弹窗（防止卡死在弹窗界面）
                            isForceLoad()
                        }
                    } else {
                        findViewById<View>(R.id.crown_update)?.visibility = View.VISIBLE
                        findViewById<View>(R.id.crown_progress)?.visibility = View.INVISIBLE
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onShow() {
        super.onShow()
        isLoading()
        isForceLoad()
    }

    /**
     * 更新版本号
     */
    open fun setUpdateVersion(version: String): KVersionUpdateDialog {
        findViewById<TextView>(R.id.crown_txt_version_name)?.setText(getString(R.string.kfaxianxinbanben) + ":" + version)
        return this
    }

    open fun setUpdateVersion2(version: String?): KVersionUpdateDialog {
        findViewById<TextView>(R.id.crown_txt_version_name)?.setText(version)
        return this
    }

    //设置标题
    open fun setUpdateTitle(title: String?): KVersionUpdateDialog {
        findViewById<TextView>(R.id.crown_txt_title)?.setText(title)
        return this
    }

    /**
     * 更新内容
     */
    open fun setUpdateContent(content: String): KVersionUpdateDialog {
        findViewById<TextView>(R.id.crown_txt_version_content)?.setText(content)
        return this
    }

    //设置确定按钮名称
    open fun setOkButtonName(btnName: String) {
        findViewById<TextView>(R.id.crown_txt_ok)?.setText(btnName)
    }

    override fun onDismiss() {
        super.onDismiss()
    }

}
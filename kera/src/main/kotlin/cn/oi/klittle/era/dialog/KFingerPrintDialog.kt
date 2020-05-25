package cn.oi.klittle.era.dialog

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.View
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseDialog
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KFingerprintUtils
import cn.oi.klittle.era.widget.compat.KTextView
import cn.oi.klittle.era.widget.compat.KView
import org.jetbrains.anko.*

//                        fixme 调用案例
//                        var figerDialog = KFingerPrintDialog(context)
//                        //fixme 指纹认证回调
//                        figerDialog.startListening(true) { isSuccess, info, isCancel -> }
//                        figerDialog.show()

/**
 * 指纹认证弹框
 */
open class KFingerPrintDialog(ctx: Context, isStatus: Boolean = true, isTransparent: Boolean = false) : KBaseDialog(ctx, isStatus = isStatus, isTransparent = isTransparent) {

    override fun onCreateView(context: Context): View? {
        return context.UI {
            verticalLayout {
                gravity = Gravity.CENTER
                KBaseUi.apply {
                    verticalLayout {
                        id = kpx.id("crown_alert_parent")
                        isClickable = true
                        gravity = Gravity.CENTER_HORIZONTAL
                        setBackgroundResource(R.drawable.kera_drawable_alert)
                        if (Build.VERSION.SDK_INT >= 21) {
                            z = kpx.x(24f)//会有投影，阴影效果。
                        }

                        kview {
                            id = kpx.id("crown_logo")
                            autoBg {
                                width = kpx.x(128)
                                height = kpx.x(128)
                                autoBg(R.mipmap.kera_ic_fingerprint_blue128)
                            }
                        }.lparams {
                            width = kpx.x(128)
                            height = kpx.x(128)
                            topMargin = kpx.x(36)
                        }

                        ktextView {
                            id = kpx.id("crown_txt_mession")
                            textSize = kpx.textSizeX(36)
                            textColor = Color.BLACK
                            text = getString(R.string.kfinger_yanzhen)//请验证指纹解锁
                        }.lparams {
                            width = wrapContent
                            height = wrapContent
                            topMargin = kpx.x(36)
                        }

                        kview {
                            id = kpx.id("crown_line")
                            backgroundColor = Color.BLACK
                        }.lparams {
                            width = matchParent
                            height = kpx.x(1)
                            topMargin = kpx.x(36)
                        }

                        //取消
                        ktextView {
                            id = kpx.id("crown_txt_Negative")
                            gravity = Gravity.CENTER
                            //textColor = Color.parseColor("#239F93")
                            textColor = Color.parseColor("#1296DB")
                            textSize = kpx.textSizeX(36)
                            text = getString(R.string.kcancel)//取消
                        }.lparams {
                            width = kpx.x(300)
                            height = kpx.x(100)
                        }


                    }.lparams {
                        width = kpx.x(620)
                        height = wrapContent
                    }
                }

            }
        }.view
    }

    val container: View? by lazy { findViewById<View>(kpx.id("crown_alert_parent")) }//最外层容器

    //信息文本
    var txt_mession: String? = KBaseUi.getString(R.string.kfinger_yanzhen)
    val mession: KTextView? by lazy { findViewById<KTextView>(kpx.id("crown_txt_mession")) }
    open fun mession(mession: String? = null): KFingerPrintDialog {
        txt_mession = mession
        return this
    }


    //指纹图标
    val logo: KView? by lazy { findViewById<KView>(kpx.id("crown_logo")) }

    //横线
    val line: KView? by lazy { findViewById<KView>(kpx.id("crown_line")) }


    val negative: KTextView? by lazy { findViewById<KTextView>(kpx.id("crown_txt_Negative")) }

    //左边，取消按钮
    open fun negative(negative: String? = getString(R.string.kcancel), callback: (() -> Unit)? = null): KFingerPrintDialog {
        this.negative?.setText(negative)
        this.negative?.setOnClickListener {
            callback?.run {
                this()
            }
            dismiss()
        }
        return this
    }

    init {
        //取消
        negative?.setOnClickListener {
            dismiss()
        }
        isDismiss(false)//默认不消失
    }


    override fun onShow() {
        super.onShow()
        mession?.setText(txt_mession)//先设置文本，再计算高度。
    }


    /**
     * fixme 开始指纹认证监听
     * @param isShowInfo 是否显示提示信息
     * @param callback fixme 指纹认证回调；认证失败了，可以重复继续认证(一般可以尝试三次)。
     * fixme isSuccess 是否认证成功; info 成功或错误的信息； isCancel 回调是否取消。 true(指纹回调已取消，不会再回调)；false（指纹认证可以继续回调）
     */
    fun startListening(isShowInfo: Boolean = true, callback: ((isSuccess: Boolean, info: String?, isCancel: Boolean) -> Unit)? = null) {
        KFingerprintUtils.startListening(isShowInfo) { isSuccess, info, isCancel ->
            if (isSuccess || isCancel) {
                dismiss()//fixme 指纹认证成功，弹窗自动关闭。
            }
            callback?.let {
                it(isSuccess, info, isCancel)//fixme 指纹回调
            }
        }
    }

    override fun onDismiss() {
        super.onDismiss()
        KFingerprintUtils.stopListening()//fixme 停止监听
    }

    override fun onDestroy() {
        super.onDestroy()
        mession?.onDestroy()
    }


}
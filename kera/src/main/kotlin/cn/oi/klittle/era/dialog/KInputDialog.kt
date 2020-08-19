package cn.oi.klittle.era.dialog

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.TextView
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseDialog
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.widget.compat.KEditText
import org.jetbrains.anko.*
import java.lang.Exception

//                                fixme 文本输入框弹窗，调用案例。
//                                var inputDialog:KInputDialog?=null
//                                if (inputDialog==null) {
//                                    inputDialog = KInputDialog(ctx)
//                                }
//                                inputDialog?.bardodeCallback {
//                                    KLoggerUtils.e("输入框文本回调：\t"+it)
//                                }
//                                inputDialog?.show()

/**
 * 输入框弹框
 */
open class KInputDialog(
        ctx: Context,
        isStatus: Boolean = true,
        isTransparent: Boolean = false
) : KBaseDialog(ctx, isStatus = isStatus, isTransparent = isTransparent) {

    var bardodeEditText: KEditText? = null//条码输入框
    override fun onCreateView(context: Context): View? {
        return context.UI {
            verticalLayout {
                gravity = Gravity.CENTER
                relativeLayout {
                    id = kpx.id("crown_alert_parent")
                    isClickable = true
                    //background=resources.getDrawable(R.drawable.crown_drawable_alert)
                    setBackgroundResource(R.drawable.kera_drawable_alert)
                    //标题
                    textView {
                        id = kpx.id("crown_txt_title")
                        textColor = Color.parseColor("#242424")
                        textSize = kpx.textSizeX(38)
                        text = getString(R.string.kpeleaseInput)//清输入
                    }.lparams {
                        topMargin = kpx.x(48)
                        leftMargin = kpx.x(24)
                        rightMargin = leftMargin
                        width = wrapContent
                        height = wrapContent
                    }

                    verticalLayout {
                        gravity = Gravity.CENTER
                        bardodeEditText = KEditText(this).apply {
                            textColor = Color.parseColor("#242424")
                            textSize = kpx.textSizeX(36)
                            singleLine = true
                            maxLines = 1
                            hint = getString(R.string.kinputNotBig)//不区分大小写
                            //setText("J20190227004001A")
                            account(22)
                            line {
                                //strokeColor = Color.parseColor("#239F93")
                                strokeColor = Color.parseColor("#50000000")
                            }
                            setCursorColor(Color.parseColor("#239F93"))
                            openDefaultAnimeLine()
                            requestFocus()
                        }.lparams {
                            width = matchParent
                            height = kpx.x(60)
                            leftMargin = kpx.x(26)
                            rightMargin = leftMargin
                            topMargin = kpx.x(60)
                        }

                    }.lparams {
                        width = matchParent
                        height = kpx.x(200)
                        centerInParent()
                    }


                    //取消
                    textView {
                        id = kpx.id("crown_txt_Negative")
//                        textColor = Color.parseColor("#239F93")
//                        textSize = kpx.textSizeX(32)
                        textColor = Color.parseColor("#C8C5C9")
                        textSize = kpx.textSizeX(34)
                        padding = kpx.x(24)
                        text = getString(R.string.kcancel)//取消
                    }.lparams {
                        alignParentBottom()
                        leftOf(kpx.id("crown_txt_Positive"))
                        rightMargin = kpx.x(24)
                    }

                    //确定
                    textView {
                        id = kpx.id("crown_txt_Positive")
//                        textColor = Color.parseColor("#239F93")
//                        textSize = kpx.textSizeX(32)
                        textColor = Color.parseColor("#42A8E1")
                        textSize = kpx.textSizeX(34)
                        padding = kpx.x(24)
                        text = getString(R.string.kconfirm)//确定
                    }.lparams {
                        alignParentBottom()
                        alignParentRight()
                        rightMargin = kpx.x(24)
                    }

                }.lparams {
                    width = kpx.x(620)
                    height = kpx.x(320)
                }
            }
        }.view
    }

    val container: View? by lazy { findViewById<View>(kpx.id("crown_alert_parent")) }

    //标题栏文本
    var txt_title: String? = getString(R.string.kpeleaseInput)
    val title: TextView? by lazy { findViewById<TextView>(kpx.id("crown_txt_title")) }
    open fun title(title: String? = null): KInputDialog {
        txt_title = title
        return this
    }

    val negative: TextView? by lazy { findViewById<TextView>(kpx.id("crown_txt_Negative")) }

    //左边，取消按钮
    open fun negative(
            negative: String? = getString(R.string.kcancel),
            callback: (() -> Unit)? = null
    ): KInputDialog {
        this.negative?.setText(negative)
        this.negative?.setOnClickListener {
            callback?.run {
                this()
            }
            dismiss()
        }
        return this
    }

    val positive: TextView? by lazy { findViewById<TextView>(kpx.id("crown_txt_Positive")) }

    //右边，确定按钮
    open fun positive(
            postive: String? = getString(R.string.kconfirm),
            callback: (() -> Unit)? = null
    ): KInputDialog {
        this.positive?.setText(postive)
        this.positive?.setOnClickListener {
            callback?.run {
                this()
            }
            dismiss()
        }
        return this
    }

    init {
        try {
            //取消
            negative?.setOnClickListener {
                dismiss()
            }

            //文本变化监听
            bardodeEditText?.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s.toString().length > 0) {
                        //positive?.textColor = Color.parseColor("#239F93")
                        positive?.textColor = Color.parseColor("#42A8E1")
                        positive?.isEnabled = true
                    } else {
                        //positive?.textColor = Color.parseColor("#8CB39E")
                        positive?.textColor = Color.parseColor("#C8C5C9")
                        positive?.isEnabled = false
                    }
                }

                override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }
            })
            //fixme 监听软键盘右下角按钮
            bardodeEditText?.addDone {
                bardodeEditText?.let {
                    var adress = it.text.toString().trim()
                    if (adress.length > 0) {
                        bardodeCallback?.let {
                            it(adress)//回调
                        }
                    }
                }
                dismiss()
            }
            //确定
            positive?.setOnClickListener {
                bardodeEditText?.let {
                    var adress = it.text.toString().trim()
                    if (adress.length > 0) {
                        bardodeCallback?.let {
                            it(adress)//回调
                        }
                    }
                }
                dismiss()
            }
            isDismiss(false)//默认不消失
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //条码回调
    var bardodeCallback: ((text: String) -> Unit)? = null

    fun bardodeCallback(bardodeCallback: ((text: String) -> Unit)? = null) {
        this.bardodeCallback = bardodeCallback
    }

    override fun onShow() {
        super.onShow()
        title?.setText(txt_title)
        if (bardodeEditText?.text.toString().length > 0) {
            positive?.textColor = Color.parseColor("#42A8E1")
            positive?.isEnabled = true
            bardodeEditText?.apply {
                try {
                    setSelection(text.toString().length)//光标设置在末尾
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            positive?.textColor = Color.parseColor("#C8C5C9")
            positive?.isEnabled = false
        }
    }

    override fun onDismiss() {
        bardodeEditText?.context?.let {
            if (it is Activity) {
                if (!it.isFinishing) {
                    bardodeEditText?.hideSoftKeyboard()
                }
            }
        }
        super.onDismiss()
    }


}
package cn.oi.klittle.era.widget

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.EditText
import cn.oi.klittle.era.R
import cn.oi.klittle.era.comm.KToast
import cn.oi.klittle.era.utils.KRegexUtils
import cn.oi.klittle.era.utils.KTimerUtils
import cn.oi.klittle.era.widget.compat.KTextView

//            使用案例
//            KValidationView(this).apply {
//                init {
//                    text = "获取验证码"
//                    textSize = kpx.textSizeX(26)
//                    gravity = Gravity.CENTER
//                }
//                radius {
//                    all_radius(kpx.x(25f))
//                    strokeColor = Color.parseColor("#2f5dd9")
//                    strokeWidth = kpx.x(2f)
//                }
//                radius_press {
//                    bg_color=Color.parseColor("#2f5dd9")
//                }
//                txt {
//                    textColor=Color.parseColor("#2f5dd9")
//                }
//                txt_press {
//                    textColor=Color.WHITE
//
//                }
//                callback {
//                    //返回手机号码
//
//                    //调用验证码接口
//                    //...
//                    //验证码接口调用完成后调用计时
//                    success()
//                }
//            }.lparams {
//                width = kpx.x(150)
//                height = kpx.x(80)
//            }

/**
 * fixme 验证码
 */
open class KValidationView : KTextView {

    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
        minit()
    }

    constructor(context: Context) : super(context) {
        minit()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        minit()
    }

    //手机文本框
    var editText: EditText? = null

    fun editText(editText: EditText?, callback: ((phone: String) -> Unit)? = null) {
        this.editText = editText
        callback?.let {
            this.callback = it
        }
    }

    //fixme 点击时，回调。返回正确的手机号。一般在这个回调里调用验证码接口。
    var callback: ((phone: String) -> Unit)? = null

    fun callback(callback: (phone: String) -> Unit) {
        this.callback = callback
    }

    //初始化
    open fun minit() {
        onClick {
            editText?.let {
                var phone = it.text.toString().trim()
                if (phone.length <= 0) {
                    KToast.showInfo(getString(R.string.KNotEmptyOfPhone))//手机号不能为空
                } else if (!KRegexUtils.isMobileNO(phone)) {
                    KToast.showError(getString(R.string.KPleaseInputCorrectPhone))//请输入正确的手机号
                } else {
                    callback?.let {
                        it(phone)
                    }
                }
            } ?: KToast.showInfo(getString(R.string.KEmptyOfTextPhone))//手机文本框为空
        }
    }

    //fixme 验证码接口调用成功后，手动调用。开始计时
    open fun success() {
        isEnabled = false//一旦禁止，点击事件也会无效。
        KTimerUtils.refreshUI(context as Activity, 60, 1000) {
            if (it >= 60L) {
                text = getString(R.string.KGetVerificationCode)//获取验证码
                isEnabled = true
            } else {
                text = getString(R.string.KVerificationCode) + "(" + (60 - it) + ")"//验证码
            }
        }
    }

}
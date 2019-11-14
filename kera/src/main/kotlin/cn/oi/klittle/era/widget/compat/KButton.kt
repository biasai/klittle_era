package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.text.*
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseView
import cn.oi.klittle.era.utils.KRegexUtils
import cn.oi.klittle.era.utils.KStringUtils
import java.util.regex.Pattern

//                    fixme 使用案例
//            KButton(this).apply {
//                text = "提交"
//                textSize = kpx.textSizeX(36)
//                textColor = Color.WHITE
//                gravity = Gravity.CENTER
//                //fixme 最好在这里手动设置宽和高（实际宽和高会再加上阴影的宽高）
//                w = kpx.screenWidth() - kpx.x(24) * 2
//                h = kpx.x(100)
//                shadow_dx = 0f
//                shadow_dy = 0f
//                shadow_radius = kpx.x(15f)
//                shadow {
//                    shadow_color = Color.BLACK
//                    all_radius(kpx.x(100f))
//                    bgHorizontalColors(Color.parseColor("#216CEB"), Color.parseColor("#79A7F3"))
//                    //strokeWidth = kpx.x(2f)
//                    //strokeColor = Color.BLACK
//                }
//                shadow_press {
//                    shadow_color = Color.RED
//                }
//
//                //fixme 参数里面的错误提示有默认值（默认值就是下面这些）
//
//                //fixme 绑定手机号输入框
//                tel(editText, "手机号格式不正确", "手机号不能为空")
//                //tel2(editText)//可能会有两个手机号
//
//                //fixme 绑定密码输入框
//                var password = editText
//                password(password, "密码格式不正确", "密码不能为空")
//                passwordPatten//密码判断，优先使用正则表达式一
//                passwordPatten2
//                var password2 = editText
//                //绑定密码框（验证密码是否正确）只对password和password2作对比判断，password3是另外的，不做对比判断
//                password2(password2, "两次输入的密码不一致", "确认密码不能为空")
//                var password3 = editText
//                password3(password3, "新密码格式不正确", "新密码不能为空")
//
//                //fixme 绑定验证码
//                code(editText, "验证码不正确", "验证码不能为空")
//                /**
//                 * 参数一 真实的验证码
//                 * 参数二 验证码为空时的提示信息
//                 * 参数三 前台，是否需要判断验证码是否正确
//                 */
//                realCole("123456", "请先获取验证码", true)
//
//                //fixme 绑定身份证输入框
//                idCard(editText, "身份证号格式不正确", "身份证号不能为空")
//
//                //fixme 绑定邮箱
//                email(editText, "邮箱格式不正确", "邮箱不能为空")
//
//                //fixme 绑定普通文本（可以添加多个），主要判断不能为空。
//                addTextView(editText, "不能为空")
//
//                //fixme 绑定同意控件，如协议等；参数二是 协议没有同意时的提示信息
//                addAgreeView(editText, "请同意")
//
//                //fixme 返回错误信息，和错误文本框。该方法在点击事件的前面。
//                onError { error, textView ->
//                    KToast.showError(error)
//                }
//
//                //fixme 点击事件；所有判断都通过时才会触发。
//                onClick {
//                    KToast.showSuccess("成功")
//                }
//            }.lparams {
//                //不要设置宽和高
//                leftMargin = kpx.x(24)
//                rightMargin = leftMargin
//            }

/**
 * 按钮事件相关。fixme 之前继承的是KTextView,现在继承KShadowView阴影控件；按钮还是应该具备阴影效果才好看。
 */
open class KButton : KShadowView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    init {
        //fixme 必须关闭硬件加速，阴影不支持(实在不支持)；（9.0及以上阴影才支持硬件加速；低版本的不支持）
        //fixme 圆角现在对硬件加速和关闭硬件加速都支持。
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        clearButonShadow()//去掉button默认的点击阴影
        gravity = Gravity.CENTER//文本居中
    }


    var errorTextView: TextView? = null
    //fixme 错误信息回调函数，交给调用者去实现。返回校验错误信息
    //KCompatEditText 和 EditText 都能转化成 TextView
    var onError: ((error: String, textView: TextView?) -> Unit)? = null

    //fixme 返回错误信息，和错误文本框。该方法在点击事件的前面。
    //fixme 只有数据正确了才会触发点击事件。
    fun onError(onError: (error: String, textView: TextView?) -> Unit) {
        isEnable()
        this.onError = onError
    }

    //fixme 点击事件，onError()没有错误返回时，才会触发
    fun onClick(onClick: () -> Unit) {
        setOnClickListener {
            //fixme 防止快速点击
            if (!isFastClick()) {
                var isRegular = true//判断数据是否正确，默认正确。
                onError?.let {
                    var error: String? = onRegular()
                    error?.apply {
                        isRegular = false//数据错误。
                        it(this, errorTextView)
                    }
                }
                if (isRegular) {
                    onClick()//fixme 如果数据校验正确，才会触发点击事件
                }
            }
        }
    }


    //文本变化监听[主要监听是否为空]
    private fun addTextChanged(editText: TextView?) {
        editText?.apply {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    isEnable()
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })
        }
    }

    //判断文本是否为空
    private fun isEmpty(editText: TextView?): Boolean {
        editText?.apply {
            if (this.text.toString().trim().length <= 0) {
                isEnable = false//空,不可用
                return true
            }
        }
        return false//不为空
    }

    private var isEnable = true

    //fixme 判断按钮是否可用。可用时，就是选中状态，不可用时，就是非选中状态。
    //fixme 即所有文本都不为空时(并且所有协议都选中)，就是选中状态。
    private fun isEnable() {
        isEnable = true
        isEmpty(tel)
        isEmpty(tel2)
        isEmpty(password)
        isEmpty(password2)
        isEmpty(password3)
        isEmpty(code)
        isEmpty(email)
        isEmpty(idCard)
        isEmpty(bankNo)
        //普通文本框集合，主要就是判断是否为空。
        textViewtList.forEach() {
            isEmpty(it.textView)
        }
        //同意控件，主要判断是否选中。选中就表示同意
        agreeViewList.forEach {
            it.view?.let {
                if (!it.isSelected) {
                    isEnable = false//没有选中，就不能用。
                }
            }
        }
        //fixme 可用状态就是选中状态。自己规定的。(继续保持这个设计，这样能保证按钮可点，可触发错误信息回调onError{})
        isSelected = isEnable
    }

    //手机号
    private var tel: TextView? = null
    var telError: String = getString(R.string.ktelError)//"手机号格式不正确"
    var telEmptyError: String? = getString(R.string.ktelEmptyError)//"手机号不能为空"

    /**
     * @param tel 手机号文本框
     * @param telError 手机号错误信息，验证的错误时，会返回该信息
     * @param telEmptyError 手机号为空时，返回
     */
    fun tel(tel: TextView?, telError: String? = this.telError, telEmptyError: String? = this.telEmptyError) {
        this.tel = tel
        telError?.let {
            this.telError = it
        }
        this.telEmptyError = telEmptyError
        tel?.apply {
            addTextChanged(this)
        }
    }

    //手机号2(可能会有两个手机号,如：新手机号和旧手机号)
    private var tel2: TextView? = null
    var tel2Error: String = getString(R.string.ktelError)//"手机号格式不正确"
    var tel2EmptyError: String? = getString(R.string.ktelEmptyError)//"手机号不能为空"

    fun tel2(tel2: TextView?, tel2Error: String? = this.tel2Error, telEmptyError2: String? = this.tel2EmptyError) {
        this.tel2 = tel2
        tel2Error?.let {
            this.tel2Error = it
        }
        this.tel2EmptyError = telEmptyError2
        tel2?.apply {
            addTextChanged(this)
        }
    }

    //fixme 密码正则表达式,优先使用passwordPatten，如果为空，再使用passwordPatten2。两个都为空。则不判断。
    //fixme 我们可以手动的赋值 passwordPatten 或 passwordPatten2
    //正则表达式一 ，复杂（大小写字母、数字、特殊符号 四选三）,并且长度不能小于八位
    var passwordPatten: String? = "^(?![a-zA-Z]+$)(?![A-Z0-9]+$)(?![A-Z\\W_]+$)(?![a-z0-9]+$)(?![a-z\\W_]+$)(?![0-9\\W_]+$)[a-zA-Z0-9\\W_]{8,}$"
    //正则表达式二，简单
    //var passwordPatten2: String? = "^[0-9A-Za-z]{6,20}\$"//由6-20字母和数字组成
    var passwordPatten2: String? = "^.{6,20}\$"//由6-20位任意字符组成 .匹配除 "/n" 之外的任何单个字符

    //fixme 判断密码是否正确
    fun isPasswordCorrect(password: String): Boolean {
        var patten = passwordPatten//优先使用正则表达式一
        if (KStringUtils.isEmpty(patten)) {
            patten = passwordPatten2//其次使用正则表达式二
        }
        if (KStringUtils.isEmpty(patten)) {
            return true//如果两个正则都为空，则不进行判断，直接判断为正确
        }
        val p = Pattern.compile(patten)
        val m = p.matcher(password)
        return m.matches()
    }

    //密码一
    private var password: TextView? = null
    var passwordError: String = getString(R.string.kpasswordError)//"密码格式不正确"
    var passwordEmptyError: String? = getString(R.string.kpasswordEmptyError)//"密码不能为空"

    fun password(password: TextView?, passwordError: String? = this.passwordError, passwordEmptyError: String? = this.passwordEmptyError) {
        this.password = password
        passwordError?.let {
            this.passwordError = it
        }
        this.passwordEmptyError = passwordEmptyError
        password?.apply {
            addTextChanged(this)
        }
    }

    //fixme 只对password和password2作对比判断，password3是另外的，不做对比判断
    //再次确认重复密码，和第一个密码作比较
    private var password2: TextView? = null
    var password2Error: String = getString(R.string.kpassword2Error)//"两次输入的密码不一致"
    var password2EmptyError: String? = getString(R.string.kpassword2EmptyError)//"确认密码不能为空"

    fun password2(password2: TextView?, password2Error: String? = this.password2Error, password2EmptyError: String? = this.password2EmptyError) {
        this.password2 = password2
        password2Error?.let {
            this.password2Error = it
        }
        this.password2EmptyError = password2EmptyError
        password2?.apply {
            addTextChanged(this)
        }
    }

    //防止要输入第三个密码，如：旧密码
    private var password3: TextView? = null
    var password3Error: String = getString(R.string.kpassword3Error)//"新密码格式不正确"
    var password3EmptyError: String? = getString(R.string.kpassword3EmptyError)//"新密码不能为空"

    fun password3(password3: TextView?, password3Error: String? = this.password3Error, password3EmptyError: String? = this.password3EmptyError) {
        this.password3 = password3
        password3Error?.let {
            this.password3Error = it
        }
        this.password3EmptyError = password3EmptyError
        password3?.apply {
            addTextChanged(this)
        }
    }

    //fixme 验证码文本框
    private var code: TextView? = null
    var codeError: String = getString(R.string.kcodeError)//"验证码不正确"
    var codeEmptyError: String? = getString(R.string.kcodeEmptyError)//"验证码不能为空"
    fun code(code: TextView?, codeError: String? = this.codeError, codeEmptyError: String? = this.codeEmptyError) {
        this.code = code
        codeError?.let {
            this.codeError = it
        }
        this.codeEmptyError = codeEmptyError
        code?.apply {
            addTextChanged(this)
        }
    }

    //fixme 真实的验证码
    var realCode: String? = null
    var realCodeEmptyError: String = getString(R.string.krealCodeEmptyError)//"请先获取验证码"
    var isVerificationCode: Boolean = true//前台，是否需要判断验证码是否正确
    fun realCole(realCode: String?, realCodeEmptyError: String? = this.realCodeEmptyError, isVerificationCode: Boolean = this.isVerificationCode) {
        this.realCode = realCode
        this.isVerificationCode
        realCodeEmptyError?.let {
            this.realCodeEmptyError = it
        }
    }

    //身份证号
    private var idCard: TextView? = null
    var idCardError: String = getString(R.string.kidCardError)//"身份证号格式不正确"
    var idCardEmptyError: String? = getString(R.string.kidCardEmptyError)//"身份证号不能为空"
    fun idCard(idCard: TextView?, idCardError: String? = this.idCardError, idCardEmptyError: String? = this.idCardEmptyError) {
        this.idCard = idCard
        idCardError?.let {
            this.idCardError = it
        }
        this.idCardEmptyError = idCardEmptyError
        idCard?.apply {
            addTextChanged(this)
        }
    }

    //银行卡号
    private var bankNo: EditText? = null
    var bankNoError: String = getString(R.string.kbankNoError)//"银行卡号格式不正确"
    var bankNoEmptyError: String? = getString(R.string.kbankNoEmptyError)//"银行卡号不能为空"
    fun bankNo(bankNo: EditText?, bankNoError: String? = this.bankNoError, bankNoEmptyError: String? = this.bankNoEmptyError) {
        this.bankNo = bankNo
        bankNoError?.let {
            this.bankNoError = it
        }
        this.bankNoEmptyError = bankNoEmptyError
        bankNo?.apply {
            addTextChanged(this)
        }
    }

    //邮箱
    private var email: TextView? = null
    var emailError: String = getString(R.string.kemailError)//"邮箱格式不正确"
    var emailEmptyError: String? = getString(R.string.kemailEmptyError)//"邮箱不能为空"

    fun email(email: TextView?, emailError: String? = this.emailError, emailEmptyError: String? = this.emailEmptyError) {
        this.email = email
        emailError?.let {
            this.emailError = it
        }
        this.emailEmptyError = emailEmptyError
        email?.apply {
            addTextChanged(this)
        }
    }

    class TextModel {
        var textView: TextView? = null
        var emptyError: String? = null//空文本错误提示信息
    }

    class EditModel {
        var editText: EditText? = null
        var emptyError: String? = null
    }

    //协议，是否同意。
    class AgreeModel {
        var view: View? = null
        var error: String? = null//协议不同意，时提示信息
    }

    //fixme 其它普通的文本输入框集合,对TextVeiw文本也进行监听。主要监听不能为空。
    private var textViewtList = mutableListOf<TextModel>()

    //普通文本框
    fun addTextView(textView: TextView?, emptyError: String?) {
        textView?.apply {
            addTextChanged(this)
            var textModel = TextModel()
            textModel.textView = textView
            textModel.emptyError = emptyError
            textViewtList.add(textModel)
        }
    }

    //同意，即协议。必须同意了，才能触发点击事件。
    private var agreeViewList = mutableListOf<AgreeModel>()

    /**
     * 普通协议，isSelected选中即表示同意。
     * @param error 协议没有同意时的提示信息。
     */
    fun addAgreeView(view: View?, error: String?) {
        view?.apply {
            if (this is KBaseView) {
                this.addSelected {
                    isEnable()//选中状态发生改变时，判断是否可用。
                }
            } else if (this is K1Widget) {
                this.addSelectChanged {
                    isEnable()//选中状态发生改变时，判断是否可用。
                }
            }
            var agreeModel = AgreeModel()
            agreeModel.view = view
            agreeModel.error = error
            agreeViewList.add(agreeModel)
        }
    }

    //fixme 正则判断，返回错误的信息，如果正确则返回为空。
    open fun onRegular(): String? {
        errorTextView = null
        tel?.apply {
            if (isEmpty(this)) {
                errorTextView = this
                return telEmptyError//"手机号为空
            }
            if (!KRegexUtils.isMobileNO(this.text.toString().trim())) {
                errorTextView = this
                return telError//"手机号格式不正确"
            }
        }

        password3?.apply {
            if (isEmpty(this)) {
                errorTextView = this
                return password3EmptyError//"密码为空
            }
            if (!isPasswordCorrect(this.text.toString())) {
                errorTextView = this
                return password3Error//密码格式不正确（旧密码，一般旧密码都在前，所以先判断）
            }
        }

        password?.apply {
            if (isEmpty(this)) {
                errorTextView = this
                return passwordEmptyError//"密码为空
            }
            if (!isPasswordCorrect(this.text.toString())) {
                errorTextView = this
                return passwordError//密码格式不正确
            }
        }

        //fixme 只对password和password2作对比判断，password3是另外的，不做对比判断
        if (password != null && password2 != null) {
            if (isEmpty(password2)) {
                errorTextView = password2
                return password2EmptyError//"确认密码为空
            }
            var p1 = password?.text.toString()
            var p2 = password2?.text.toString()
            if ((!p1.equals(p2))) {
                errorTextView = password2
                return password2Error//"两次输入的密码不一致"
            }
        }
        tel2?.apply {
            if (isEmpty(this)) {
                errorTextView = this
                return tel2EmptyError//"手机号为空
            }
            if (!KRegexUtils.isMobileNO(this.text.toString().trim())) {
                errorTextView = this
                return tel2Error//"手机号格式不正确"（新手机号）
            }
        }
        email?.apply {
            if (isEmpty(this)) {
                errorTextView = this
                return emailEmptyError//"邮箱为空
            }
            if (!KRegexUtils.isEmail(this.text.toString().trim())) {
                errorTextView = this
                return emailError//"邮箱格式不正确"
            }
        }

        idCard?.apply {
            if (isEmpty(this)) {
                errorTextView = this
                return idCardEmptyError//"身份证号为空
            }
            if (!KRegexUtils.isIdCard(this.text.toString().trim())) {
                errorTextView = this
                return idCardError//"身份证号格式不正确"
            }
        }

        bankNo?.apply {
            if (isEmpty(this)) {
                errorTextView = this
                return bankNoEmptyError//"银行卡为空
            }
            if (!KRegexUtils.isBankCard(this.text.toString().trim())) {
                errorTextView = this
                return bankNoError//"银行卡号格式不正确"
            }
        }

        //普通文本框集合，主要就是判断是否为空。
        textViewtList.forEach() {
            if (isEmpty(it.textView)) {
                return it.emptyError//"文本框为空
            }
        }
        //同意控件，主要判断是否选中。选中就表示同意
        agreeViewList.forEach {
            it.view?.apply {
                if (!isSelected) {
                    return it.error//没有选中，返回提示
                }
            }
        }
        //fixme 验证码最后判断
        code?.apply {
            //fixme 一 首先判断验证码是否获取
            if (realCode == null || realCode?.trim().equals("")) {
                errorTextView = this
                return realCodeEmptyError//请先获取验证码
            }
            //fixme 二 判断验证码文本框是否为空
            if (isEmpty(this)) {
                errorTextView = this
                return codeEmptyError//"验证码文本框为空
            }
            //fixme 三 对验证码进行判断，是否正确。
            //是否需要判断，验证吗是否正确
            if (isVerificationCode) {
                if (!code!!.text.toString().trim().equals(realCode!!.trim())) {
                    errorTextView = this
                    return codeError//"验证码不正确"
                }
            }
        }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        errorTextView = null
        onError = null
        tel = null
        tel2 = null
        password = null
        password2 = null
        email = null
        emailEmptyError = null
        tel2EmptyError = null
        agreeViewList.clear()
        textViewtList.clear()
        bankNo = null
        bankNoEmptyError = null
        idCard = null
        idCardEmptyError = null
        code = null
        codeEmptyError = null
        realCode = null
        passwordPatten = null
        passwordPatten2 = null
        setOnClickListener(null)
    }

}
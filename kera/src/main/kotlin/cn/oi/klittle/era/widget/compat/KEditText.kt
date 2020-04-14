package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.*
import android.text.Editable
import android.text.InputType
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import cn.oi.klittle.era.entity.widget.compat.KEditLineEntity
import cn.oi.klittle.era.utils.KLoggerUtils

import cn.oi.klittle.era.utils.KRegexUtils
import cn.oi.klittle.era.utils.KStringUtils
import org.jetbrains.anko.singleLine
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.Deferred
/**
 * 文本输入框相关。
 * fixme inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL  不能直接设置inputType，不然画布canvas没有内容显示。
 * fixme setRawInputType(InputType.TYPE_CLASS_NUMBER)//仅仅只是弹出数字文本框。不会做数据校验。还是需要自己手动去做校验。
 */

//正常，不会挤压屏幕（默认），在这里手动设置了，弹框显示时，键盘输入框不会自动弹出,并且文本同时还具备光标(亲测)。
//fixme 对Activity，Dialog都有效。(在Activity(onResume())和Dialog(onShow())显示的时候调用有效。)
//window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)//fixme 已经集成在了KBaseActivity里了。

// 这个是使用案例：
////              底线
//                line {
//                    strokeHorizontalColors(Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE)
//                }
////              底线的底线
//                line_bg {
//                    strokeHorizontalColors = null
//                }
////              底线的默认动画
//                openDefaultAnimeLine()

////              错误的图片
//                autoBg {
//                    width=kpx.x(35)
//                    autoBg(R.mipmap.kera_error)
//                    isAutoCenterVertical = true
//                    isAutoRight = true
//                    isShow=false
//                }
////              正确的图片
//                autoBg2 {
//                    width=kpx.x(40)
//                    autoBg(R.mipmap.kera_correct)
//                    isAutoCenterVertical = true
//                    isAutoRight = true
//                    isShow=false
//                }
////              类型
//                tel() {
//                    if (isTel()) {
//                        autoBg?.isShow = false
//                        autoBg2?.isShow = true
//                        autoBg2?.alpha=0
//                        autoBg_alpha(autoBg2, 255)
//                    }else{
//                        autoBg2?.isShow = false
//                        autoBg2?.alpha=0
//                    }
//                }
////              焦点变化
//                addFocusChange {
//                    if (!it){
//                        if (isTel()) {
//                            autoBg?.isShow = false
//                            autoBg2?.isShow = true
//                            autoBg_alpha(autoBg2, 255)
//                        } else {
//                            autoBg?.isShow = true
//                            autoBg2?.isShow = false
//                            autoBg_jitter(autoBg)
//                        }
//                    }
//                }


//                fixme 适配器中正确使用案例
//                var data = get(position)
//                holder.numberbp?.maxDecimal(null)//fixme 必须清空，防止异常
//                holder.numberbp?.onFocusChange(null)
//                //聚焦事件
//                holder.numberbp?.onFocusChange { v, hasFocus ->
//                    //datas[position] == data 判断是否位同一个对象；防止焦点异常。
//                    if (!hasFocus && datas != null && datas.size > position && datas[position] == data) {
//
//                    }
//                }


//                fixme 常用方法
//                //文本变化监听
//                addTextWatcher {
//                }
//                //聚焦监听（会覆盖之前的聚焦事件）
//                onFocusChange { v, hasFocus ->
//                }
//                //聚焦监听（不会覆盖之前的聚焦事件）
//                addFocusChange { v, hasFocus ->
//
//                }
//                //点击
//                onClick {
//                }
//                //长按
//                onLongClick {
//
//                }
//                //触摸事件
//                onTouch { v, event ->
//                }

//                            //fixme 只允许输入数值类型，比如003会自动转成3；00也会自动转成0，(会自动去除前面的0,会自动转成Long类型再转String类型)
//                            //fixme isInt=true 文本输入框会变成数值文本输入框。set()方法里设置了 setRawInputType(InputType.TYPE_CLASS_NUMBER)
//                            isInt=true

//                            minDecimal(0)//最大值
//                            maxDecimal(100)//最小值

//    fixme containsRegex("0123.+",6)//只允许输入0123.+ 这几个字符，且长度最大为6

//    //fixme 调用案例/不包含567这三字符
//    notContainsRegex("567") {
//        KLoggerUtils.e("文本变化：\t"+it)
//    }
//fixme notContainsRegex("\n") {} 不包含换行符，即单行。不允许按回车键换键(亲测有效)。

//fixme 单行设置
//maxLines=1(无效)
//singleLine=true (这个有效)

//fixme 控制最大值及数值类型，常用方法。
//decimal(0, 1)//小数点后保留位数，小数总长度(包含小数)
//maxDecimal(2)//最大值
//isInt=true//输入法为数字类型

//inputHeightListener {}//fixme 软键盘（输入法）高度变化监听。即界面被弹窗挤上去和挤下来时，会回调。返回的是软键盘的高度。

open class KEditText : KMyEditText {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//开启硬件加速,不然圆角没有效果
    }

    override fun setText(text: CharSequence?, type: TextView.BufferType) {
        try {
            //fixme 下面的最大值，最小值检测；必不可少；防止异常的。
            var text = text
            //fixme 最小值(先判断最小值)
            minDecimal?.apply {
                if (text != null && text.toString().trim().length > 0) {
                    var cd = text.toString()
                    if (cd.toDouble() < this.toDouble()) {
                        text = this.toString()
                    }
                } else {
                    text = this.toString()//设置为最小值
                }
            }
            //fixme 最大值
            maxDecimal?.apply {
                if (text != null && text.toString().trim().length > 0) {
                    var cd = text.toString()
                    if (cd.toDouble() > this.toDouble()) {
                        text = this.toString()
                    }
                }
            }

            super.setText(text, TextView.BufferType.EDITABLE)
            text?.toString()?.let {
                if (it.length > 0) {
                    //setText()手动赋值的，光标统一设置到末尾；
                    //输入发输入的光标不受影响。因为输入法输入调用的不是setText()方法
                    setSelection(length())
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            KLoggerUtils.e("输入框setText异常2：\t" + e.message)
        }
    }


    //fixme ^ 匹配输入字符串的开始位置，除非在方括号表达式中使用，此时它表示不接受该字符集合。要匹配 ^ 字符本身，请使用 \^。

    //str.contains("字符串")//是否包含该字符串，true包含。false不包含。
    //toString().replace(str, "")//替换该字符串
    //str.contains(regex) 是否符合该正则表达式，只要有一个符合就是true
    //str.replace(regex, "")替换符合该正则的字符，即替换符合条件的字符。fixme 也只能是一些简单的正则。复杂了就不行了。

    companion object {
        var REGEX_Has_Empty = " "//空
        var REGEX_Has_China_Empty = "[\u4E00-\u9FA5\u0020\n\r]"//包含中文和空格和换行
        var REGEX_Has_China = "[\u4E00-\u9FA5]"// 包含中文
        //var REGEX_NotHas_China = "[^\u4E00-\u9FA5]"// 不包含中文
        //fixme "[^A-Za-z0-9*.,，。+-]" 直接往后面加字符就行了
        //var REGEX_NotHas_ENGLISH = "[^A-Za-z0-9]"//不包含（数字+英文大小写）
        var REGEX_NotHas_Decimal = "[^0-9.]"//不包含数字和点
        var REGEX_NotHas_Number = "[^0-9]"//不包含数字

        var REGEX_NotHas_IdCard = "[^0-9xX]"//不包含数字和xX
        var REGEX_NotHas_Email = "[^A-Za-z0-9.@|–_-]"//不包含数字字母点和@
    }

    //fixme ==========================================================================校验->开始


    //fixme "[^A-Za-z0-9*.,，。+-]" 直接往后面加字符就行了，不包含这些字符
    /**
     * fixme 只包含以下字符
     * @param strs 包含的字符
     * @param strLength 文本长度
     * @param callback fixme 文本发生改变时的回调 ,注意isRegex为true时才会回调。
     */
    fun containsRegex(strs: String, strLength: Int = 32, callback: ((edt: Editable) -> Unit)? = null) {
        addTextWatcher2("containsRegex") {
            if (isRegex) {
                var rege = "[^" + strs + "]"
                remove(it, rege.toRegex())//fixme 移除符合条件的，如："[^0123]" 正则表示不包含0123，即移除0123以外的所有字符。
                var str = it.toString()
                if (str.length > strLength) {//最大输入长度,现在最长就是32位的。网易的是6-18个字符
                    //超过总长度，数值不变。
                    replace(it, beforeText!!)
                }
                callback?.apply {
                    this(it)
                }
            }
        }
    }


    /**
     * fixme 不包含以下字符
     * @param strs 不包含的字符 "\n"不允许换行。（亲测有效）;fixme 默认不包含中文，空格和换行(回车键)。
     * @param callback 文本变化监听
     */
    fun notContainsRegex(strs: String = "\u4E00-\u9FA5\u0020\n\r", callback: ((edt: Editable) -> Unit)? = null) {
        addTextWatcher2("notContainsRegex") {
            if (isRegex) {
                var rege = "[" + strs + "]"
                remove(it, rege.toRegex())//fixme 移除符合条件的（有效）
                //remove(it, "["+strs+"]".toRegex())//fixme 无效，必须新建变量才有效。即 变量.toRegex()才有效。亲测。
                callback?.apply {
                    this(it)
                }
            }
        }
    }

    //替换字符，防止事件冲突。所以先移除监听，再添加监听。
    fun replace(edt: Editable, st: Int, en: Int, text: CharSequence) {
        if (st >= 0 && st <= edt.lastIndex && en >= st && en <= edt.length) {
            removeTextChangedListener(textWatcher)
            edt.replace(st, en, text)
            addTextChangedListener(textWatcher)
        }
    }

    //替换成新的字符
    fun replace(edt: Editable, newValue: String) {
        replace(edt, 0, edt.length, newValue)
    }

    //去除指定字符
    fun remove(edt: Editable, str: String) {
        if (edt.toString().contains(str)) {
            removeTextChangedListener(textWatcher)
            //et.setText方法可能会引起键盘变化,所以用editable.replace
            var inputStr = edt.toString().replace(str, "").trim()
            edt.replace(0, edt.length, inputStr)
            addTextChangedListener(textWatcher)
        }
    }

    //去除符合正则条件的字符。String字符串类型可以.toRegex()
    fun remove(edt: Editable, regex: Regex) {
        if (edt.contains(regex)) {
            removeTextChangedListener(textWatcher)//防止事件冲突。先移除。
            //et.setText方法可能会引起键盘变化,所以用editable.replace
            var inputStr = edt.toString().replace(regex, "").trim()
            edt.replace(0, edt.length, inputStr)
            addTextChangedListener(textWatcher)
        }
    }

    //去除空字符串
    fun removeEmpty(edt: Editable? = text) {
        edt?.let {
            remove(edt, REGEX_Has_Empty)
        }
    }

    //去除中文
    fun removeChina(edt: Editable? = text) {
        edt?.let {
            remove(edt, REGEX_Has_China.toRegex())
        }
    }

    //去除中文和空格
    fun removeChinaAndBlankSpace(edt: Editable?) {
        edt?.let {
            remove(edt, REGEX_Has_China_Empty.toRegex())
        }
    }


    //fixme ==========================================================================校验->结束

    //fixme ==============================================================================================类型->开始


    var isRegex: Boolean = true//fixme 判断是否需要对数据进行校验。
    //fixme 系统的setText(),不能为空,不能为null。只能setText(""),setText()方法是final又不能重写。
    /**
     * @param isRegex 是否需要对数据进行校验。不添加默认值，防止和系统方法冲突。
     */
    fun setText(text: CharSequence, isRegex: Boolean) {
        this.isRegex = isRegex
        setText(text)
        this.isRegex = true//设置完成之后，马上恢复，默认需要验证。
    }

    //获取小数具体值
    fun getDecimal(): String? {
        if (text.toString().trim().length > 0) {
            var decimal = text.toString().replace(REGEX_NotHas_Decimal.toRegex(), "").trim()
            if (decimal.contains(".")) {
                var index = decimal.indexOf(".")
                if (index == decimal.lastIndex) {//如果末尾是小数点，则去除。即去除末尾的小数点。
                    decimal = decimal.substring(0, index)//小数点前面的数
                }
                if (decimal.toDouble() == 0.0) {
                    decimal = "0"
                }
            }
            return decimal
        }
        return null//"0"//小数，空就是0
    }

//                小数点类型扩展，实现金钱类型。
//                decimal(3,8){
//                    var str=decimalString(it.toString(),3,false,true,true)
//                    replace(it,"￥"+str)
//                    //replace(it,str+"￥")
//                    //replace(it,str)
//                }

    var decimalNum: Int = 2//小数点后保留位数
    /**
     * fixme 小数类型，整数类型，和金钱类型; 不支持负数。
     * 小数类型，如果小数点个数为0。就是整数类型了。所以可以作为;小数类型，整数类型，和金钱类型。
     * @param num 小数点后保留位数
     * @param length 小数总长度(包含小数)。之所以设置这个长度，是因为比 setMaxLength()安全。
     * @param callback fixme 文本发生改变时的回调,注意isRegex为true时才会回调。
     */
    fun decimal(num: Int = decimalNum, length: Int = Long.MAX_VALUE.toString().length, callback: ((edt: Editable) -> Unit)? = null) {
        this.decimalNum = num
        if (this.decimalNum <= 0) {
            isInt = true//fixme 整数(小数个数为0，肯定是整数)
        } else {
            isInt = false
        }
        setRawInputType(InputType.TYPE_CLASS_NUMBER)
        addTextWatcher2("decimal") {
            try {
                if (isRegex) {
                    var isMax = false//是否超过总长度
                    if (num > 0) {
                        remove(it, REGEX_NotHas_Decimal.toRegex())//去除（数字和点）以外的字符。(中文，空格，换行都会去除)
                    } else {
                        remove(it, REGEX_NotHas_Number.toRegex())//去除（数字）以外的字符。(中文，空格，点，换行都会去除)
                    }
                    var str = it.toString()
                    if (str.length > length) {
                        if (maxDecimal != null) {
                            var strd = 0.0
                            if (str.length >= 2) {
                                strd = KStringUtils.removeFrontZero(str).toDouble()//去除前面的0
                            }
                            if (strd > maxDecimal!!.toDouble()) {
                                replace(it, maxDecimal!!)//赋值最大值
                            } else {
                                replace(it, strd.toString())
                            }
                        } else {
                            //超过总长度，数值不变。
                            replace(it, beforeText!!)
                        }
                        remove(it, REGEX_NotHas_Decimal.toRegex())//去除（数字和点）以外的字符。(中文，空格，换行都会去除)
                        str = it.toString()
                        isMax = true
                    }
                    if (str.length >= 2) {
                        str = KStringUtils.removeFrontZero(str)//去除前面的0
                        replace(it, str)
                    }
                    str = it.toString()
                    if (str.length >= 1) {
                        if (it.contains(".")) {
                            if (KStringUtils.indexOf2(str, '.') != -1) {
                                //去除第二个点。只允许一个小数点。
                                replace(it, selectionStart - 1, selectionStart, "")
                                str = it.toString()
                            }
                            if (str.substring(0, 1).equals(".")) {
                                //第一个字符是点
                                //首个字符不能为点。前面必须加上一个0
                                replace(it, 0, 1, "0.")
                            }
                            //控制小数点后面的位数
                            var s = it.substring(it.indexOf("."))
                            if (s.length >= num) {
                                var start = str.indexOf(".")
                                var end = str.length
                                s = s.substring(1)
                                if (s.length > num) {
                                    replace(it, start + num + 1, end, "")
                                }
                            }
                        } else {
                            if (str.length > 1 && str.substring(0, 1).equals("0")) {//首位数为0
                                if ((isInt != null && isInt!!) || decimalNum <= 0) {
                                    //转整型
                                    replace(it, 0, it.length, str.toDouble().toInt().toString())
                                } else {
                                    //首个字符是0自动补上小数点。
                                    replace(it, 0, 1, "0.")
                                }

                            }
                        }
                    }
                    var count = it.toString().length
                    //fixme 验证完成
                    callback?.apply {
                        this(it)
                    }
                    var count2 = it.toString().length
                    //解决decimalString()之后，焦点错乱问题
                    if (isMax && selectionStart < it.toString().lastIndex && count != count2) {
                        setSelection(selectionStart + 1)//恢复光标位置,尽可能的恢复，无法保证百分比恢复。
                    }
                    //fixme 最小值(位置换一下，先判断最小值)
                    minDecimal?.apply {
                        var cd = getDecimal()
                        if (cd != null) {
                            //KLoggerUtils.e("cd:\t"+cd)
                            if (cd.toDouble() < this.toDouble()) {
                                var currentSelectionStart = selectionStart
                                if (decimalNum <= 0) {
                                    setText(this.toDouble().toInt().toString())//整数
                                } else {
                                    if (cd.contains(".")) {
                                        if (isInt != null && isInt!!) {
                                            setText(this.toDouble().toInt().toString())//fixme 整数
                                        } else {
                                            setText(this.toDouble().toString())//浮点型
                                        }
                                    } else {
                                        setText(this)//原型
                                    }
                                }
                                if (it.lastIndex > currentSelectionStart) {
                                    setSelection(currentSelectionStart)
                                } else {
                                    setSelection(length())//光标
                                }
                            } else if (cd.contains(".") && isInt != null && isInt!! && cd.length > 1) {
                                var str = cd.substring(cd.indexOf(".") + 1)
                                setText(str)//fixme 整数
                                setSelection(length())//光标
                            }
                        } else {
                            setText(this)//最小值。
                        }

                    }
                    //fixme 最大值
                    maxDecimal?.apply {
                        var cd = getDecimal()
                        if (cd != null) {
                            if (cd.toDouble() > this.toDouble()) {
                                var currentSelectionStart = selectionStart
                                if (decimalNum <= 0) {
                                    setText(this.toDouble().toInt().toString())//整数
                                } else {
                                    if (cd.contains(".")) {
                                        if (isInt != null && isInt!!) {
                                            setText(this.toDouble().toInt().toString())//fixme 整数
                                        } else {
                                            setText(this.toDouble().toString())//浮点型
                                        }
                                    } else {
                                        setText(this)//原型
                                    }
                                }
                                if (it.lastIndex > currentSelectionStart) {
                                    setSelection(currentSelectionStart)
                                } else {
                                    setSelection(length())//光标
                                }
                            } else if (cd.contains(".")) {
                                if (isInt != null && isInt!!) {
                                    setText(this.toDouble().toInt().toString())//fixme 整数
                                }
                            }
                        }
                    }
                    if (length() == 1) {
                        setSelection(length())//fixme 长度只有1时，光标末尾。
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    //fixme 不支持负数。小数相加
    fun addDecimal(v2: Double, isDecimal: Boolean = true) {
        var hasDecimal = isDecimal//是否有小数点
        var v1 = getDecimal()
        if (v1 == null) {
            v1 = "0"
        }
        if (v1 == null) {
            return
        }
        if (v1.trim().length <= 0) {
            v1 = "0"
        }
        if (v1.contains(".")) {
            hasDecimal = true
        }
        try {
            var v3 = KStringUtils.addDouble(v1.toDouble(), v2).toString()
            if (decimalNum <= 0 && v3.contains(".")) {
                v3 = v3.toDouble().toInt().toString()//不能直接转Int，先转Double，再转Int
            }
            //不允许出现负数。
            if (v3.toDouble() < 0) {
                v3 = "0"
            }
            if (hasDecimal) {
                setText(v3)
            } else {
                setText(v3.toDouble().toInt().toString())//没有小数点
            }
        } catch (e: Exception) {
        }
    }

    //兼容整数
    fun addDecimal(v2: Long) {
        addDecimal(v2.toDouble(), false)
    }

    //fixme 不支持负数。小数相减
    fun subDecimal(v2: Double, isDecimal: Boolean = true) {
        var hasDecimal = isDecimal//是否有小数点
        var v1 = getDecimal()
        if (v1 == null) {
            v1 = "0"
        }
        if (v1 == null) {
            return
        }
        if (v1.trim().length <= 0) {
            v1 = "0"
        }
        if (v1.contains(".")) {
            hasDecimal = true
        }
        try {
            var v3 = KStringUtils.subDouble(v1.toDouble(), v2).toString()
            if (decimalNum <= 0 && v3.contains(".")) {
                v3 = v3.toDouble().toInt().toString()
            }
            //不允许出现负数。
            if (v3.toDouble() < 0) {
                v3 = "0"
            }
            if (hasDecimal) {
                setText(v3)
            } else {
                setText(v3.toDouble().toInt().toString())//没有小数点
            }
        } catch (e: Exception) {
        }
    }

    //兼容整数
    fun subDecimal(v2: Long) {
        subDecimal(v2.toDouble(), false)
    }


    /**
     * fixme 账号类型，不允许输入中文，空格和换行符。
     * @param length 账号的长度。
     * @param callback fixme 文本发生改变时的回调 ,注意isRegex为true时才会回调。
     */
    fun account(length: Int = 18, callback: ((edt: Editable) -> Unit)? = null) {
        addTextWatcher2("account") {
            if (isRegex) {
                remove(it, REGEX_Has_China_Empty.toRegex())//去除中文和空格和换行符
                var str = it.toString()
                if (str.length > length) {
                    //超过总长度，数值不变。
                    if (beforeText != null) {
                        replace(it, beforeText!!)
                    }
                }
                callback?.apply {
                    this(it)
                }
            }
        }
    }

    /**
     * fixme 密码类型
     * @param length 密码长度
     * @param passWordChar 密码符文。
     * @param callback fixme 文本发生改变时的回调 ,注意isRegex为true时才会回调。
     */
    fun password(length: Int = 18, passWordChar: Char? = this.passWordChar, callback: ((edt: Editable) -> Unit)? = null) {
        hiddenPassword(passWordChar)
        addTextWatcher2("password") {
            if (isRegex) {
                remove(it, REGEX_Has_China_Empty.toRegex())//去除中文和空格和换行符
                var str = it.toString()
                if (str.length > length) {
                    //超过总长度，数值不变。
                    replace(it, beforeText!!)
                }
                callback?.apply {
                    this(it)
                }
            }
        }
    }

    /**
     * fixme 手机号类型
     * @param callback fixme 文本发生改变时的回调 ,注意isRegex为true时才会回调。
     */
    fun tel(callback: ((edt: Editable) -> Unit)? = null) {
        decimal(0, 11, callback)
    }

    /**
     * fixme 数字验证码类型
     * @param length 验证码个数
     * @param callback fixme 文本发生改变时的回调 ,注意isRegex为true时才会回调。
     */
    fun code(length: Int = 6, callback: ((edt: Editable) -> Unit)? = null) {
        decimal(0, length, callback)
    }

    /**
     * fixme 身份证类型(一般由18位数字，或x组成。x代表10)
     * @param callback fixme 文本发生改变时的回调 ,注意isRegex为true时才会回调。
     */
    fun idCard(callback: ((edt: Editable) -> Unit)? = null) {
        addTextWatcher2("idCard") {
            if (isRegex) {
                remove(it, REGEX_NotHas_IdCard.toRegex())//去除中文和空格和换行符
                var str = it.toString()
                if (str.length > 18) {//15位的叫居民身份证编号；18位的叫公民身份号码
                    //超过总长度，数值不变。
                    replace(it, beforeText!!)
                }
                callback?.apply {
                    this(it)
                }
            }
        }
    }

    /**
     * fixme 银行卡类型
     * @param callback fixme 文本发生改变时的回调 ,注意isRegex为true时才会回调。
     */
    fun bankNo(callback: ((edt: Editable) -> Unit)? = null) {
        decimal(0, 19, callback)//最大输入长度，银行卡一般为16-19位,没有20位的。
    }

    /**
     * fixme 邮箱类型
     * @param callback fixme 文本发生改变时的回调 ,注意isRegex为true时才会回调。
     */
    fun email(callback: ((edt: Editable) -> Unit)? = null) {
        addTextWatcher2("email") {
            if (isRegex) {
                remove(it, REGEX_NotHas_Email.toRegex())
                var str = it.toString()
                if (str.length > 32) {//最大输入长度,现在最长就是32位的。网易的是6-18个字符
                    //超过总长度，数值不变。
                    replace(it, beforeText!!)
                }
                callback?.apply {
                    this(it)
                }
            }
        }
    }

    //fixme ==============================================================================================类型->结束

    //fixme 常用正则表达式，对自身进行判断

    //是否为手机号
    fun isTel(): Boolean {
        return KRegexUtils.isMobileNO(this.text.toString().trim())
    }

    //是否为邮箱
    fun isEmail(): Boolean {
        return KRegexUtils.isEmail(this.text.toString().trim())
    }

    //是否为身份证
    fun isIdCard(): Boolean {
        return KRegexUtils.isIdCard(this.text.toString().trim())
    }

    //是否为银行卡号
    fun isBankNo(): Boolean {
        return KRegexUtils.isBankCard(this.text.toString().trim())
    }


    //fixme OnEditorActionListener只能监听右下角按钮，其他按键监听不到
    //fixme ==============================================================================================右下角按钮->开始

    /**
     * 监听输入框右下角完成按钮
     * @param isHideSoftKeyboard 是否隐藏软键盘
     */
    fun addDone(isHideSoftKeyboard: Boolean = true, callbak: (() -> Unit)? = null) {
        imeOptions = EditorInfo.IME_ACTION_DONE//设置成完成类型
        singleLine = true//单行才有效
        callbak?.let {
            val onEditorActionListener = OnEditorActionListener { textView, actionId, keyEvent ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {//确定/完成
                    if (isHideSoftKeyboard) {
                        hideSoftKeyboard()//隐藏软键盘
                    }
                    it()
                    true
                } else false
            }
            setOnEditorActionListener(onEditorActionListener)
        }
    }

    //监听输入框右下角搜索按钮
    fun addSearch(isHideSoftKeyboard: Boolean = true, callbak: (() -> Unit)? = null) {
        imeOptions = EditorInfo.IME_ACTION_SEARCH//设置成搜索类型
        singleLine = true//单行才有效
        callbak?.let {
            val onEditorActionListener = OnEditorActionListener { textView, actionId, keyEvent ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {//搜索
                    if (isHideSoftKeyboard) {
                        hideSoftKeyboard()//隐藏软键盘
                    }
                    it()
                    true
                } else false
            }
            setOnEditorActionListener(onEditorActionListener)
        }
    }

    //监听输入框右下角发送按钮
    fun addSend(isHideSoftKeyboard: Boolean = true, callbak: (() -> Unit)? = null) {
        imeOptions = EditorInfo.IME_ACTION_SEND//设置成发送类型
        singleLine = true//单行才有效
        callbak?.let {
            val onEditorActionListener = OnEditorActionListener { textView, actionId, keyEvent ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {//发送
                    if (isHideSoftKeyboard) {
                        hideSoftKeyboard()//隐藏软键盘
                    }
                    it()
                    true
                } else false
            }
            setOnEditorActionListener(onEditorActionListener)
        }
    }

    //监听输入框右下角下一步按钮
    fun addNext(isHideSoftKeyboard: Boolean = true, callbak: (() -> Unit)? = null) {
        imeOptions = EditorInfo.IME_ACTION_NEXT//设置下一步类型
        singleLine = true//单行才有效
        callbak?.let {
            val onEditorActionListener = OnEditorActionListener { textView, actionId, keyEvent ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {//下一步
                    if (isHideSoftKeyboard) {
                        hideSoftKeyboard()//隐藏软键盘
                    }
                    it()
                    true
                } else false
            }
            setOnEditorActionListener(onEditorActionListener)
        }
    }


    //fixme ==============================================================================================右下角按钮->结束

    //fixme 低部横线；对gravity = Gravity.CENTER 和 gravity = Gravity.CENTER_HORIZONTAL水平居中属性无效。如果设置了这个属性，就没有效果了。
    //底线的底线。一直都存在。在最下面。实现线条重叠效果
    var line_bg: KEditLineEntity? = null

    fun line_bg(block: KEditLineEntity.() -> Unit): KEditText {
        if (line_bg == null) {
            line_bg = getmLine().copy()//fixme 整个属性全部复制过来。同样是复制常态的。
        }
        block(line_bg!!)
        invalidate()
        return this
    }

    //不可用
    var line_enable: KEditLineEntity? = null

    fun line_enable(block: KEditLineEntity.() -> Unit): KEditText {
        if (line_enable == null) {
            line_enable = getmLine().copy()//整个属性全部复制过来。
        }
        block(line_enable!!)
        invalidate()
        return this
    }

    //按下
    var line_press: KEditLineEntity? = null

    fun line_press(block: KEditLineEntity.() -> Unit): KEditText {
        if (line_press == null) {
            line_press = getmLine().copy()//整个属性全部复制过来。
        }
        block(line_press!!)
        invalidate()
        return this
    }

    //鼠标悬浮
    var line_hover: KEditLineEntity? = null

    fun line_hover(block: KEditLineEntity.() -> Unit): KEditText {
        if (line_hover == null) {
            line_hover = getmLine().copy()//整个属性全部复制过来。
        }
        block(line_hover!!)
        invalidate()
        return this
    }

    //聚焦
    var line_focuse: KEditLineEntity? = null

    fun line_focuse(block: KEditLineEntity.() -> Unit): KEditText {
        if (line_focuse == null) {
            line_focuse = getmLine().copy()//整个属性全部复制过来。
        }
        block(line_focuse!!)
        invalidate()
        return this
    }

    //选中
    var line_selected: KEditLineEntity? = null

    fun line_selected(block: KEditLineEntity.() -> Unit): KEditText {
        if (line_selected == null) {
            line_selected = getmLine().copy()//整个属性全部复制过来。
        }
        block(line_selected!!)
        invalidate()
        return this
    }

    //正常状态
    var line: KEditLineEntity? = null

    private fun getmLine(): KEditLineEntity {
        if (line == null) {
            line = KEditLineEntity()
        }
        return line!!
    }

    fun line(block: KEditLineEntity.() -> Unit): KEditText {
        block(getmLine())
        invalidate()
        return this
    }


    //fixme =======================================================================================绘图，画底横线(顺序在文字的上面显示)

    var lineProgress: Float = 1f//底线的显示进度。范围[0~1]
    //fixme 开始线条变化动画。如果是虚线，变化时间就要长一点（850毫秒左右），不能太短（不太好）。
    fun startAnimeLine(vararg value: Float, duration: Long = 350, AnimatorUpdateListener: ((values: Float) -> Unit)? = null) {
        ofFloat("lineProgress", 0, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
    }

    //fixme 开启默认线条动画，这个是默认写好的线条动画，直接调用即可。
    fun openDefaultAnimeLine(isDash: Boolean = false, duration: Long = 0L) {
        var duration2 = 350L
        if (isDash) {
            duration2 = 850L//如果是虚线，时间应该长一点效果才好。
        }
        if (duration > 0) {
            duration2 = duration
        }
        addFocusChange { v, hasFocus ->
            if (hasFocus) {
                startAnimeLine(0f, 1f, duration = duration2)
            } else {
                startAnimeLine(1f, 0f, duration = duration2)
            }
        }
    }

    var softInputHeight = 0//记录当前屏幕被挤上去的高度（软键盘的高度）；0软键盘没有弹窗，大于0，软键盘会弹出。
    private var inputHeightListener: ((inputHeight: Int) -> Unit)? = null
    /**
     * fixme 软键盘高度监听;小于等于0，软键盘没有显示，大于0，软键盘显示了(布局被挤上去了)。亲测有效！
     * @param window 可以为空，为空的时候，会自动通过context上下文去获取。
     */
    fun softInputHeightListener(window: Window? = null, inputHeightListener: ((inputHeight: Int) -> Unit)? = null) {
        if (window != null) {
            this.mWindow = window
        }
        this.inputHeightListener = inputHeightListener
    }

    private var mWindow: Window? = null
    private fun inputHeightListener() {
        inputHeightListener?.let {
            if (mWindow != null) {
                getSoftInputHeight(mWindow).let {
                    if (it != softInputHeight) {//软键盘高度有变化时，才回调。防止重复回调。
                        softInputHeight = it//fixme 记录当前屏幕被挤上去的高度（软键盘的高度）
                        inputHeightListener?.let {
                            it(softInputHeight)
                        }
                    }
                }
            } else {
                getSoftInputHeight(context).let {
                    if (it != softInputHeight) {//软键盘高度有变化时，才回调。防止重复回调。
                        softInputHeight = it//fixme 记录当前屏幕被挤上去的高度（软键盘的高度）
                        inputHeightListener?.let {
                            it(softInputHeight)
                        }
                    }
                }
            }
        }
    }

    var currentLineLenght: Float = 0F//当前进度显示的横线长度。
    var linePhase: Float = 0F//横线偏移量
    override fun draw2(canvas: Canvas, paint: Paint) {
        try {
            //KLoggerUtils.e("输入发，绘制")//fixme 输入的draw()绘制方法，大约每500毫秒会自动调用。不管是否聚焦，都在不断的自动执行中。
            super.draw2(canvas, paint)
            if (line != null) {
                var model: KEditLineEntity? = null
                if (!isEnabled && line_enable != null) {
                    //不可用
                    model = line_enable
                } else if (isPressed && line_press != null) {
                    //按下
                    model = line_press
                } else if (isHovered && line_hover != null) {
                    //鼠标悬浮
                    model = line_hover
                } else if (isFocused && line_focuse != null) {
                    //聚焦
                    model = line_focuse
                } else if (isSelected && line_selected != null) {
                    //选中
                    model = line_selected
                }
                //正常
                if (model == null) {
                    model = line
                }
                //画底线的底线（没有动画效果，一直存在），实现线条重叠效果
                line_bg?.apply {
                    drawLine(canvas, paint, this, 1f)
                }
                model?.apply {
                    drawLine(canvas, paint, this, lineProgress)
                }
            }
            inputHeightListener()//fixme 软键盘监听
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        var b = true
        try {
            b = super.dispatchTouchEvent(event)
            event?.action?.let {
                if (it == MotionEvent.ACTION_UP) {
                    //fixme 手指离开
                    GlobalScope.async {
                        inputHeightListener()//fixme 软键盘监听
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return b
    }

    //画底线
    fun drawLine(canvas: Canvas, paint: Paint, model: KEditLineEntity, lineProgress: Float = 1f) {
        model.apply {
            if (strokeWidth > 0 && lineProgress > 0) {
                if (strokeLength <= 0) {
                    strokeLength = width.toFloat()//线条长度，如果为0，则等价于整个组件的长度。
                }
                //线条中心，默认底部居中
                if (centerX < 0 && width > 0) {
                    centerX = width / 2.toFloat()
                }
                if (centerY < 0 && height > 0) {
                    centerY = height - strokeWidth
                }
                currentLineLenght = strokeLength * lineProgress
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth
                paint.color = strokeColor
                var top = centerY - strokeWidth / 2
                var bottom = top + strokeWidth
                var left = centerX - strokeLength / 2
                var right = left + strokeLength
                //边框颜色渐变，渐变颜色优先等级大于正常颜色。
                var linearGradient: LinearGradient? = null
                //渐变颜色数组必须大于等于2
                if (strokeVerticalColors != null) {
                    if (!isStrokeGradient) {
                        //垂直不渐变
                        linearGradient = getNotLinearGradient(top, bottom, strokeVerticalColors!!, true)
                    }
                    //fixme 垂直渐变
                    if (linearGradient == null) {
                        linearGradient = LinearGradient(0f, top, 0f, bottom, strokeVerticalColors, null, Shader.TileMode.CLAMP)
                    }
                } else if (strokeHorizontalColors != null) {
                    if (!isStrokeGradient) {
                        //水平不渐变
                        linearGradient = getNotLinearGradient(left, right, strokeHorizontalColors!!, false)
                    }
                    //fixme 水平渐变
                    if (linearGradient == null) {
                        linearGradient = LinearGradient(left, centerY, right, centerY, strokeHorizontalColors, null, Shader.TileMode.CLAMP)
                    }
                }
                linearGradient?.let {
                    paint.setShader(linearGradient)
                }
                //虚线
                if (dashWidth > 0 && dashGap > 0) {
                    var dashPathEffect = DashPathEffect(floatArrayOf(dashWidth, dashGap), linePhase)
                    paint.setPathEffect(dashPathEffect)
                }
                var startX = centerX - currentLineLenght / 2
                var stopX = startX + currentLineLenght
                var path = Path()
                path.moveTo(startX, centerY)
                path.lineTo(stopX, centerY)
                //canvas.drawLine(startX, centerY, stopX, centerY, paint)//虚线没有效果，需要关闭硬件加速
                canvas.drawPath(path, paint)//虚线有效果，和硬件加速无关。

                //控制虚线流动性【虚线的流动，不影响光标，也不影响文本输入。】
                if (isdashFlow && (dashWidth > 0 && dashGap > 0)) {
                    if (dashSpeed > 0) {
                        if (linePhase >= Float.MAX_VALUE - dashSpeed) {
                            linePhase = 0f
                        }
                    } else {
                        if (linePhase >= Float.MIN_VALUE - dashSpeed) {
                            linePhase = 0f
                        }
                    }
                    linePhase += dashSpeed
                    invalidate()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textWatcher = null
        line = null
        line_bg = null
        line_enable = null
        line_press = null
        line_focuse = null
        line_selected = null
        line_hover = null
        inputHeightListener = null
        mWindow = null
    }

}

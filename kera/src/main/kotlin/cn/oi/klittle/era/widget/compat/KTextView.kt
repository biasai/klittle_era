package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.*
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.entity.feature.KSearchEntity
import cn.oi.klittle.era.entity.widget.compat.KTextEntity
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.utils.KStringUtils
import org.jetbrains.anko.*


/**
 * 文本框相关。
 *
 * fixme 内部丁和外补丁知识点
 *
 *内补丁 leftPadding 和 paddingLeft本身都是一样的；即数值都是一样的。
 *
 * 唯一的区别就是leftPadding可以手动动态赋值的，而paddingLeft只能读不能手动赋值。
 * 但是leftPadding赋值之后，paddingLeft也会同步改变。而且是一样的。
 *
 * 外补丁只有leftMargin；没有leftMargin
 */
//fixme setLineSpacing(kpx.x(8f),1.5f) 设置行高之后；lineHeight会自动更新。以下方法能够正确获取文本的实际高度。行间距离是行与行之间垂直距离；不是文字水平间距。
//fixme setMore()更多显示不下时，会显示3个点；单行，多行都有效。且只对KTextView有效，文本输入框KEditText无效
//fixme isOverFlowedMore()判断文本是否超过，是否显示了更多...
open class KTextView : KAutoSplitTextView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//开启硬件加速,不然圆角没有效果
    }

    /**
     * 获取文本的宽度;
     * @param text fixme 默认文本，不要去除空格哦。空格也占位的。（即最好不要.trim()）
     */
    fun getTextWidth(text: String = getText().toString()): Int {
        return paint.measureText(text).toInt()
    }

    private var mLeftPadding = 0
    private var mRightPadding = 0
    //fixme setLineSpacing(kpx.x(8f),1.5f) 设置行高之后；lineHeight会自动更新。以下方法能够正确获取文本的实际高度。
    /**
     * 获取文本的高度（是文本的总高度，不是控件的高度。fixme 在布局加载完成之后，再调用，不然控件宽度获取不到。无法正确计算。）
     */
    fun getTextHeight(): Int {
        var count = lineCount//行数
        //KLoggerUtils.e("最大值：\t"+Int.MAX_VALUE+"\t文本的宽度：\t"+getTextWidth()+"\tcount:\t"+count+"\tw:\t"+w)
        //防止lineCount为0;因为调用setText()的时候；lineCount可能会为0
        //KLoggerUtils.e("count:\t"+count+"\t行高：\t"+lineHeight)
        //fixme 以下行数计算亲测可行。
        if ((count <= 0 || mLeftPadding != leftPadding || mRightPadding != rightPadding) && text.toString().length > 0) {
            count = Math.ceil((getTextWidth().toDouble() / (w.toDouble() - paddingLeft - paddingRight))).toInt()
            //KLoggerUtils.e("文本宽度：\t" + getTextWidth() + "\t控件宽度：\t" + width)
            //Math.ceil(向上取整)；靠的住
            //输出结果：1.0:	1.0	1.1:	2.0	0.1:	1.0	0.0:	0.0
            //KLoggerUtils.e("1.0:\t"+Math.ceil(1.0)+"\t1.1:\t"+Math.ceil(1.1)+"\t0.1:\t"+Math.ceil(0.1)+"\t0.0:\t"+Math.ceil(0.0))
            mLeftPadding = leftPadding
            mRightPadding = rightPadding
            // KLoggerUtils.e("count:\t" + count + "\tpaddingLeft:\t" + paddingLeft + "\tpaddingRight:\t" + paddingRight)
        }
        //KLoggerUtils.e("lineCount:\t" + lineCount + "\tcount:\t" + count + "\tlineHeight:\t" + lineHeight + "\tpaddingTop:\t" + paddingTop + "\tpaddingBottom:\t" + paddingBottom)
        //fixme textSize.toInt()/6 是增添的量；防止不够。这个数值很精准了。但是随着行数的曾多，误差会越来越大。
        //return count * (lineHeight + (textSize.toInt() / 6)) + paddingTop + paddingBottom//lineHeight 是单行的高度。
        //KLoggerUtils.e("字体大小：\t" + textSize)
        //fixme 以下判断方式已经很精准了。
        if (textSize < 30) {
            return count * lineHeight + (textSize * 2f).toInt() + paddingTop + paddingBottom//fixme 亲测 (textSize*2f).toInt()添加量效果最好。最准确。
        } else {
            return count * lineHeight + (textSize).toInt() + paddingTop + paddingBottom
        }

    }

    //获取文本能够滑动的高度；不能马上获取，需要等控件加载完成之后，才会获取成功。
    fun getTextScrollHeight(): Int {
        return getTextHeight() - height
    }

    private var textLength = 0
    private var textNum = 0
    //获取文本能够滑动的宽度(多行不太靠谱，但一行可以准确计算出来)
    fun getTextScrollWidth(): Int {
        if (textNum != text.toString().length || textLength == 0) {
            var line = lineCount
            //文本的行数
            if (Build.VERSION.SDK_INT >= 16 && line > maxLines) {
                line = maxLines
            }
            var width = paint.measureText(text.toString(), 0, text.toString().length)
            if (line > 1) {
                width = width / line//fixme 多行计算的每一行平局宽度。
            }
            width = width + leftPadding + rightPadding - w
            textNum = text.toString().length
            textLength = width.toInt()
        }

        return textLength//记录文本的长度；防止重复计算。
    }

    //是否可以长按复制黏贴
    fun isCocy(isCocy: Boolean = false) {
        if (isCocy) {
            //可以长按复制黏贴
            setLongClickable(true)
        } else {
            //取消长按事件。禁止复制黏贴
            setLongClickable(false)
        }
    }

    /**
     * 复制文本
     * fixme isSelectable 所有的View都具备select选中能力,即文本框可以复制粘贴。
     * copyText 为要复制的文本内容。如果为空。则复制文本控件的文本。
     */
    fun copyText(copyText: String? = null) {
        if (context != null) {
            context.apply {
                var cm: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                if (copyText != null && copyText.length > 0) {
                    cm.setText(copyText)//复制指定文本
                } else {
                    cm.setText(getText())//复制控件文本
                }
            }
        }
    }

    /**
     * fixme 搜索指定字符，显示指定颜色。每次文本重新赋值时，都必须手动再调用一次才有效。
     * 字体颜色对下划线，删除线（中线）都有效。即线的颜色和字体一致。
     * @param text 全部文本
     */
    fun search(vararg search: KSearchEntity, text: String = this.text.toString()) {
        var txt2 = text
        val spannableString = SpannableString(txt2)//原始文本
        setText(txt2)//恢复原样
        for (i in 0 until search.size) {
            var txt3 = search[i].text
            txt3?.let {
                var length = it.length
                if (length > 0 && txt2.length >= length) {
                    var start = txt2.indexOf(it)//开始下标（包含）,如果没有搜索到会返回-1
                    var end = start + length//结束下标（不包含）
                    //Log.e("test", "开始下标:\t" + start + "\t结束:\t" + end)
                    if (start >= 0) {
                        //参数为 开始下标，和结束下标。
                        spannableString.setSpan(ForegroundColorSpan(search[i].color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        if (search[i].isMul) {
                            //搜索多个
                            var index = start + length
                            while (txt2.length > index && txt2.indexOf(it, index) >= 0) {
                                start = txt2.indexOf(it, index)
                                end = start + length
                                spannableString.setSpan(ForegroundColorSpan(search[i].color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                index = start + length
                            }
                        }
                        setText(spannableString)//特定颜色显示。
                    }
                }
            }
        }
    }


    var symbText: String? = null//保存真实内容,
    fun symbText(text: Long, num: Int = 4, symbol: String = "*") {
        symbText(text.toString(), num, symbol)
    }

    /**
     * 中间内容带符号，如星号*
     * content 文本内容
     * symbolNum 符号个数
     * symbol 符号
     * symbolStar 符号开始的位置
     * frontNum 前半部分，添加的间隙个数。
     * behindNum 后半部分添加的间隙。
     */
    fun symbText(text: String?, symbolNum: Int = 4, symbol: String = "*", symbolStar: Int? = null, frontNum: Int? = null, behindNum: Int? = null) {
        text?.let {
            if (it.trim().length >= symbolNum && symbolNum > 0) {
                this.symbText = it//保存真实内容
                var length = text.length - symbolNum
                var i = Math.floor(length / 2.0).toInt()//floor是取小，所以头部是小于尾部的。
                if (symbolStar != null && symbolStar >= 0) {
                    i = symbolStar
                }
                var front = it.substring(0, i)
                var behind = it.substring(i + symbolNum)
                var sym = ""
                for (i in 1..symbolNum) {
                    sym = sym + symbol//星号
                }
                //前面部分，添加的间隙
                frontNum?.let {
                    if (it >= 1) {
                        for (i in 1..it) {
                            front += "\u0020"
                        }
                    }
                }
                //后半部分，添加的间隙
                behindNum?.let {
                    if (it >= 1) {
                        for (i in 1..it) {
                            behind = "\u0020" + behind
                        }
                    }
                }
                this.text = front + sym.trim() + behind
            }
        }
    }


    /**
     * fixme 设置最大输入个数。即最大文字个数。
     * setMaxLines(lines) 设置行数
     */
    fun setMaxLength(num: Int) {
        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(num)) //最大输入长度，网易的是6-18个字符
    }

    private var watcher: ((edt: Editable) -> Unit)? = null
    private var watcheres2: MutableList<((edt: Editable) -> Unit)?>? = mutableListOf()
    private var watcherMap: MutableMap<String, ((edt: Editable) -> Unit)>? = mutableMapOf()
    var beforeText: String? = null//文本变化之前的内容。
    var inputText: String? = null//fixme 当前输入的文本。只记录此时输入的。不会记录删除的。
    var inputStart: Int = 0//fixme 输入文本的下标
    private var preDecimalText: String? = null//记录上一次正确的Double类型的文本

    var isInt: Boolean? = null
        //fixme 是否为整形（整数。）;toDouble().toInt().toString()//有小数点不能直接转Long，先转Double，再转Long类型
        set(value) {
            if (value != null && value) {
                setRawInputType(InputType.TYPE_CLASS_NUMBER)//只允许输入数值类型
            }
            field = value
        }

    //fixme 最大值，最小值判断
    fun maxMinDecimal(mWatcher: Editable): Boolean {
        //fixme 最小值(先判断最小值)
        minDecimal?.apply {
            var cd = mWatcher.toString()
            if (cd != null && cd.trim().length > 0) {
                try {
                    if (cd.toDouble() < this.toDouble()) {
                        setText(this.toString())
                        //return@let
                        return true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    preDecimalText?.let {
                        try {
                            preDecimalText?.toDouble()
                            setText(preDecimalText)//恢复到上一次正确文本。
                            return@let
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } else {
                setText(this.toString())
                //return@let
                return true
            }
        }
        //fixme 最大值
        maxDecimal?.apply {
            var cd = mWatcher.toString()
            if (cd != null && cd.trim().length > 0) {
                try {
                    if (cd.toDouble() > this.toDouble()) {
                        setText(this.toString())
                        //return@let
                        return true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    preDecimalText?.let {
                        try {
                            preDecimalText?.toDouble()
                            setText(preDecimalText)//恢复到上一次正确文本。
                            //return@let
                            return true
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        return false
    }

    //代码调用setText()时都会监听到。
    var textWatcher: TextWatcher? = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            s?.let {
                try {
                    //fixme 下面的最大值，最小值检测；必不可少；防止异常的。
                    var mWatcher = it
                    //fixme 最大值，最小值判断
                    if (maxMinDecimal(mWatcher)) {
                        return@let
                    }
                    //if (it.toString().length > 0) { }//fixme 不要判断个数，防止空格的时候无法监听。
                    watcheres2?.forEach {
                        it?.apply {
                            this(mWatcher)//这一步，mWatcher会发生改变。
                        }
                    }
                    //KLoggerUtils.e("mWatcher:\t" + mWatcher + "\ttext:\t" + text + "\t" + text.toString().toDouble().toInt().toString() + "\t" + isInt)
                    mWatcher?.let {
                        if (mWatcher != null && mWatcher.trim().length > 0 && isInt != null && isInt!!) {
                            //fixme 整数;text.toString().toDouble().toInt().toString()，有小数必须先转成浮点型，不能直接转int；不然报错异常。
                            if ((!it.toString().equals(text.toString().toDouble().toLong().toString()))) {
                                mWatcher?.replace(0, mWatcher.length, text.toString().toDouble().toLong().toString())//fixme 防止异常不相等，以时间text为准。（修复，亲测有效）
                            }
                            if (!text.toString().equals(text.toString().toDouble().toLong().toString())) {
                                setText(text.toString().toDouble().toLong().toString())
                                return@let
                            }
                        } else {
                            if ((!it.toString().equals(text.toString()))) {
                                mWatcher?.replace(0, mWatcher.length, text)//fixme 防止异常不相等，以实时text为准。（修复，亲测有效）
                            }
                        }
                    }
                    watcher?.apply {
                        this(mWatcher)
                    }
                    if (minDecimal != null || maxDecimal != null && mWatcher != null && mWatcher.trim().length > 0) {
                        try {
                            mWatcher.toString()?.toDouble()
                            preDecimalText = mWatcher.toString()//记录当前正确的文本。
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    KLoggerUtils.e("文本框数值异常：\t" + e.message)
                }
            }
            inputText = null
            beforeText = null//使用完成之后，之前的文本清除掉。
        }

        var beforeCount = 0
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            s?.length?.let {
                beforeCount = it
            }
            beforeText = s.toString()
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            inputText = null
            //start是变化字符的下标。
            s?.length?.let {
                if (it - beforeCount == 1) {
                    inputText = s?.substring(start, start + 1)
                    inputStart = start
                }
            }
        }
    }

    //文本监听
    //fixme 重点说一下，为什么返回Editable，而不是string文本
    //fixme 因为 et.setText方法可能会引起键盘变化,所以用editable.replace来显示内容最好。不会引起键盘变化，也不会引起焦点变化。
    //removeTextChangedListener(textWatcher) 和 addTextChangedListener(textWatcher) //防止事件冲突。先移除。再添加
    //fixme 事件文本监听可以重复添加,不会覆盖之前的。内部判断一般都是调用的这个方法，如最大值，最小值等。
    /**
     * @param watcherKey 文本监听事件标志，防止重复添加
     * @param watcher 文本监听回调。
     */
    fun addTextWatcher2(watcherKey: String?, watcher: (edt: Editable) -> Unit) {
        try {
            if (textWatcher != null) {
                removeTextChangedListener(textWatcher)
                addTextChangedListener(textWatcher)
                watcheres2?.let {
                    if (it.contains(watcher)) {
                        it.remove(watcher)//去重，防止重复添加同一个监听事件。
                    }
                }
                watcherMap?.let {
                    if (watcherKey != null) {
                        if (it.containsKey(watcherKey)) {
                            watcheres2?.remove(it.get(watcherKey))//fixme 去除重复事件。
                            it.remove(watcherKey)
                        }
                    }
                }
                watcheres2?.add(watcher)
                watcherKey?.let {
                    watcherMap?.put(watcherKey, watcher)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //fixme 文本监听只有一个，会覆盖之前的。
    fun addTextWatcher(watcher: (edt: Editable) -> Unit) {
        try {
            if (textWatcher != null) {
                removeTextChangedListener(textWatcher)
                addTextChangedListener(textWatcher)
                this.watcher = watcher
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected var maxDecimal: String? = null//fixme 最大值;默认位空null
    fun maxDecimal(maxDecimal: Double?) {
        this.maxDecimal = maxDecimal?.toString()
    }

    //支持浮点型和整型。
    fun maxDecimal(maxDecimal: Long) {
        this.maxDecimal = maxDecimal?.toString()
    }

    protected var minDecimal: String? = null//fixme 最小值，不支持负数。
    fun minDecimal(minDecimal: Double?) {
        this.minDecimal = minDecimal?.toString()
        this.minDecimal?.let {
            if (it.toDouble() < 0) {
                this.minDecimal = "0"
            }
        }
    }

    fun minDecimal(minDecimal: Long) {
        this.minDecimal = minDecimal?.toString()
        this.minDecimal?.let {
            if (it.toDouble() < 0) {
                this.minDecimal = "0"
            }
        }
    }

    private var mMoreLine = 1
    /**
     * fixme 更多（显示不全时）显示三个点...  单行，多行都有效。且只对KTextView有效，文本输入框KEditText无效
     * lines 显示的最大行数。
     */
    fun setMore(lines: Int = 1) {
        mMoreLine = lines
        //能水平滚动较长的文本内容。不要用这个。圆角会没有效果的。就是这个搞的圆角没有效果。
        //setHorizontallyScrolling(true)
        //setSingleLine(true)//是否單行顯示。过时了。也会导致圆角没有效果。
        //fixme 上面两个属性导致圆角无效。不要使用。TextView,editText,button都会导致圆角无效。

        setMaxLines(lines);//fixme 显示最大行,这个也是关键。setMaxLines和setEllipsize同时设置，才会显示更多。
        //代码不换行，更多显示三个点...
        setEllipsize(TextUtils.TruncateAt.END)//fixme 这个才是关键，会显示更多
    }

    private fun getAvailableWidth(): Int {
        return width - paddingLeft - paddingRight
    }

    /**
     * fixme 判断文本是否超过，是否显示了更多... true超过；false没有超过。
     * fixme 亲测，单行，多行都能够正确判断。
     */
    open fun isOverFlowedMore(): Boolean {
        try {
            var paint: Paint = paint
            var width = paint.measureText(text.toString())//文本的总长度。
            //KLoggerUtils.e("lineCount：\t"+lineCount+"\tmMoreLine:\t"+mMoreLine+"\twidth:\t"+width+"\tgetAvailableWidth():\t"+getAvailableWidth()*mMoreLine)
            if (mMoreLine <= 1) {
                //单行判断是否超过
                return if (width > getAvailableWidth()) true else false
            } else {
                //多行判断是否超过;必须大于才行。相等会完全显示出来。只有超过了才会出现三个省略号...
                if (width > getAvailableWidth() * mMoreLine) {
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * @param d fixme double数据类型，数值太大，小数太长都会丢失精度。（精度丢失时会四舍五入。）
     * @param num 保留小数点后的位数
     * @param isKeepEnd0 是否保留小数点后末尾的0
     * @param isMicro 是否保留千位分隔符。（1如：789,012.12399即逗号）只有整数部分有千位分隔符，小数部分没有。
     */
    fun doubleString(d: Double, num: Int = 2, isKeepEnd0: Boolean = true, isMicroSymb: Boolean = false): String {
        var str = KStringUtils.doubleString(d, num, isKeepEnd0, isMicroSymb)!!
        setText(str)
        return str
    }

    //人民币符号 ￥

    /**
     * @param str fixme 字符串,不会发生精度丢失问题。所以不会发生四舍五入。
     * @param num 小数点保留个数
     * @param isKeepEnd0 小数点后末尾如果是0,是否保留0。true保留0，false不保留。默认保留。
     * @param isKeepEndPoint 是否保留末尾的小数点，如：12. ->true 12. ->false 12
     * @param isMicro 是否保留千位分隔符。（1如：789,012.12399即逗号）只有整数部分有千位分隔符，小数部分没有。
     * @param microSymb 千位分隔符
     */
    fun decimalString(str: String, num: Int = 2, isKeepEnd0: Boolean = true, isKeepEndPoint: Boolean = false, isMicroSymb: Boolean = false, microSymb: String = ","): String {
        var str = KStringUtils.decimalString(str, num, isKeepEnd0, isKeepEndPoint, isMicroSymb, microSymb)!!
        setText(str)
        return str
    }

    //fixme 是否设置斜体，true 是。false不是。在5.0以上才有效果。需要api 20及以上。
    fun isItalic(isItalic: Boolean = true) {
        if (isItalic) {
            paint.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC))
        } else {
            paint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
        }
    }

    //fixme 文本是否加粗，true加粗（中文，英文，数字都可以加粗）,false不加粗
    fun isBold(isBold: Boolean = true) {
        getPaint().setFakeBoldText(isBold)
    }

    //fixme 是否添加删除线,删除线（中线）的颜色和字体颜色一致
    fun isDeleteLine(isDelete: Boolean = true) {
        if (isDelete) {
            //添加删除线
            //getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG)//这个会覆盖加粗和下划线效果。
            paintFlags += Paint.STRIKE_THRU_TEXT_FLAG//这样添加，不会覆盖加粗和下划线
        } else {
            //去掉删除线
            paintFlags = getPaintFlags() and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    //fixme 是否添加下划线，true添加,false不添加。下划线的颜色也和字体一样。
    fun isUnderLine(isUnderLine: Boolean = true) {
        if (isUnderLine) {
            //getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG)////这个会覆盖加粗和中线效果。
            paintFlags += Paint.UNDERLINE_TEXT_FLAG//这样添加，不会覆盖加粗和中线
        } else {
            //去掉下划线
            paintFlags = getPaintFlags() and Paint.UNDERLINE_TEXT_FLAG.inv()
        }
    }

    //判断是否为空
    fun isEmpty(text: String?): Boolean {
        return KStringUtils.isEmpty(text)
    }

    var isMarquee = false//是否跑马灯效果。可以手动设置哦。
    /**
     * 设置文本跑马灯效果（聚焦时才有效果，且文本长度大于控件长度才有效果），1是一次，-1是无限循环。
     * 跑马灯循环完一次之后，会停顿一秒。再跑。
     * 一般鼠标点击，按下时。跑马灯就停止。如果不想触摸时让跑马灯停止，直接设置 isEnableTouch=false 关闭触摸即可。
     */
    fun setMarquee(count: Int = -1) {
        setHorizontallyScrolling(true)
        setSingleLine(true)
        setEllipsize(TextUtils.TruncateAt.MARQUEE)
        marqueeRepeatLimit = count
        if (count < 0 || count > 10) {
            isMarquee = true
        } else {
            isMarquee = false
        }
    }

    //重载是否聚焦事件
    override fun isFocused(): Boolean {
        if (isMarquee) {
            return true//跑马灯必须要聚焦才有效果。这样返回ture跑马灯才有效。
        }
        return super.isFocused()
    }

    //不可用;这个enable一般就表示不可用
    var txt_enable: KTextEntity? = null

    fun txt_enable(block: KTextEntity.() -> Unit): KTextView {
        if (txt_enable == null) {
            txt_enable = getmTxt().copy()//整个属性全部复制过来。
        }
        block(txt_enable!!)
        text?.trim()?.let {
            //文本获取不会为null最多为空字符串
            if (it.length <= 0) {
                txt_enable?.text?.let {
                    if (it.length > 0) {
                        setText2(it)//fixme 必须设置一下文本，防止部分机型没有文本显示。
                    }
                }
            }
        }
        requestLayout()
        invalidate()
        postInvalidate()
        return this
    }

    //按下
    var txt_press: KTextEntity? = null

    fun txt_press(block: KTextEntity.() -> Unit): KTextView {
        if (txt_press == null) {
            txt_press = getmTxt().copy()//整个属性全部复制过来。
        }
        block(txt_press!!)
        text?.trim()?.let {
            //文本获取不会为null最多为空字符串
            if (it.length <= 0) {
                txt_press?.text?.let {
                    if (it.length > 0) {
                        setText2(it)//fixme 必须设置一下文本，防止部分机型没有文本显示。
                    }
                }
            }
        }
        requestLayout()
        invalidate()
        postInvalidate()
        return this
    }

    //鼠标悬浮
    var txt_hover: KTextEntity? = null

    fun txt_hover(block: KTextEntity.() -> Unit): KTextView {
        if (txt_hover == null) {
            txt_hover = getmTxt().copy()//整个属性全部复制过来。
        }
        block(txt_hover!!)
        text?.trim()?.let {
            //文本获取不会为null最多为空字符串
            if (it.length <= 0) {
                txt_hover?.text?.let {
                    if (it.length > 0) {
                        setText2(it)//fixme 必须设置一下文本，防止部分机型没有文本显示。
                    }
                }
            }
        }
        requestLayout()
        invalidate()
        postInvalidate()
        return this
    }

    //聚焦
    var txt_focuse: KTextEntity? = null

    fun txt_focuse(block: KTextEntity.() -> Unit): KTextView {
        if (txt_focuse == null) {
            txt_focuse = getmTxt().copy()//整个属性全部复制过来。
        }
        block(txt_focuse!!)
        text?.trim()?.let {
            //文本获取不会为null最多为空字符串
            if (it.length <= 0) {
                txt_focuse?.text?.let {
                    if (it.length > 0) {
                        setText2(it)//fixme 必须设置一下文本，防止部分机型没有文本显示。
                    }
                }
            }
        }
        requestLayout()
        invalidate()
        postInvalidate()
        return this
    }

    //选中
    var txt_selected: KTextEntity? = null

    fun txt_selected(block: KTextEntity.() -> Unit): KTextView {
        if (txt_selected == null) {
            txt_selected = getmTxt().copy()//整个属性全部复制过来。
        }
        block(txt_selected!!)
        text?.trim()?.let {
            //文本获取不会为null最多为空字符串
            if (it.length <= 0) {
                txt_selected?.text?.let {
                    if (it.length > 0) {
                        setText2(it)//fixme 必须设置一下文本，防止部分机型没有文本显示。
                    }
                }
            }
        }
        requestLayout()
        invalidate()
        postInvalidate()
        return this
    }

    //fixme 正常状态（先写正常样式，再写其他状态的样式，因为其他状态的样式初始值是复制正常状态的样式的。）
    var txt: KTextEntity? = null

    private fun getmTxt(): KTextEntity {
        if (txt == null) {
            txt = KTextEntity(textSize = kpx.pixelToDp(textSize), textColor = currentTextColor, isFakeBoldText = getPaint().isFakeBoldText)
//            文本不需要复制，因为文本为空，是不会发生改变的。不需要再复制一个，省内存。
//            if (text != null && text.length > 0) {
//                //默认文本
//                txt?.apply {
//                    text = this@KCompatTextView.text.toString()
//                }
//            }
        }
        return txt!!
    }

    fun txt(block: KTextEntity.() -> Unit): KTextView {
        block(getmTxt())
        text?.trim()?.let {
            //文本获取不会为null最多为空字符串
            if (it.length <= 0) {
                getmTxt()?.text?.let {
                    if (it.length > 0) {
                        setText2(it)//fixme 必须设置一下文本，防止部分机型没有文本显示。
                    }
                }
            }
        }
        requestLayout()
        invalidate()
        postInvalidate()
        return this
    }

    var texts_model: KTextEntity? = null

    //fixme 必须设置一下文本，防止部分机型没有文本显示。
    private fun setText2(it: String?) {
        if (it != null && it.length > 0) {
            setText(it)
            invalidate()
        }
    }

    override fun draw(canvas: Canvas?) {
        if (txt != null) {
            texts_model = null
            if (isPressed && txt_press != null) {
                //按下
                texts_model = txt_press
            } else if (isHovered && txt_hover != null) {
                //鼠标悬浮
                texts_model = txt_hover
            } else if (isFocused && txt_focuse != null) {
                //聚焦
                texts_model = txt_focuse
            } else if (isSelected && txt_selected != null) {
                //选中
                texts_model = txt_selected
            }
            //不可用，优先级最高
            if (!isEnabled && txt_enable != null) {
                texts_model = txt_enable
            }
            //正常
            if (texts_model == null) {
                texts_model = txt
            }
            texts_model?.let {
                var mtxt = it
                it.text?.let {
                    if (!text.toString().trim().equals(it)) {
                        var isSearch = false
                        if (mtxt.searchText != null && mtxt.searchText!!.trim().length > 0 && mtxt.searchTextColor != Color.TRANSPARENT) {
                            isSearch = true
                        }
                        if (isSearch) {
                            search(KSearchEntity(mtxt.searchText, mtxt.searchTextColor, mtxt.isMul), text = it)
                        } else {
                            setText(it)//重新设置文本
                        }
                    }
                }
                it.textColor?.let {
                    //getCurrentTextColor()获取当前文本的颜色值
                    if (it != getCurrentTextColor()) {
                        textColor = it//重新设置颜色值
                    }
                }
                it.textSize?.let {
                    if (it > 0) {
                        var px = kpx.dpToPixel(it)
                        if (px != textSize) {
                            textSize = it//fixme 重新设置文本的大小。文本大小，set的时候单位的dp，get获取的时候，单位的px
                        }
                    }
                }
                if (it.isFakeBoldText != getPaint().isFakeBoldText) {
                    getPaint().setFakeBoldText(it.isFakeBoldText)//是否粗体
                }

                //边框颜色渐变，渐变颜色优先等级大于正常颜色。
                var linearGradient: LinearGradient? = null
                //渐变颜色数组必须大于等于2
                if (it.textVerticalColors != null) {
                    var top = scrollY.toFloat() + topPadding
                    var bottom = top + h - bottomPadding
                    if (!it.isTextGradient) {
                        //垂直不渐变
                        linearGradient = getNotLinearGradient(top, bottom, it.textVerticalColors!!, true)
                    }
                    //fixme 垂直渐变
                    if (linearGradient == null) {
                        linearGradient = LinearGradient(0f, top, 0f, bottom, it.textVerticalColors, null, Shader.TileMode.CLAMP)
                    }
                } else if (it.textHorizontalColors != null) {
                    var left = scrollX.toFloat() + leftPadding
                    var right = left + w - rightPadding
                    if (!it.isTextGradient) {
                        //水平不渐变
                        linearGradient = getNotLinearGradient(left, right, it.textHorizontalColors!!, false)
                    }
                    //fixme 水平渐变
                    if (linearGradient == null) {
                        linearGradient = LinearGradient(left, centerY, right, centerY, it.textHorizontalColors, null, Shader.TileMode.CLAMP)
                    }
                }
                paint.setShader(linearGradient)
            }
        }
        super.draw(canvas)//防止最后，防止文本颜色渐变无效。
    }

    override fun onDestroy() {
        try {
            super.onDestroy()
            txt = null
            txt_focuse = null
            txt_hover = null
            txt_press = null
            txt_enable = null
            txt_selected = null
            texts_model = null
            watcheres2?.clear()
            watcheres2 = null
            watcher = null
            watcherMap?.clear()
            watcherMap = null
            if (textWatcher != null) {
                try {
                    setOnFocusChangeListener(null)
                    removeTextChangedListener(textWatcher)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            textWatcher = null
            setText(null)//文本清空
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}
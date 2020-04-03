package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.support.v7.widget.AppCompatTextView
import android.text.TextUtils
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.ViewTreeObserver

//参考链接：https://blog.csdn.net/P876643136/article/details/88082850?depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromBaidu-1&utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromBaidu-1
/**
 * fixme 修复文本异常换行等问题(一行没有满就自动换行了)。setAutoSplitText（文本）开启手动计算时候换行。
 */
open class KAutoSplitTextView : KView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}


    private var onGlobalLayoutListener2: ViewTreeObserver.OnGlobalLayoutListener? = null
    private var indent: String?=null

    /**
     * fixme 设置自动换行文本。（自己手动计算是否换行，解决原生换行异常问题(一行文本没有满就自动换行了。)）
     * @param text 文本
     * @param indent 悬挂缩进效果的文本。即该文本会出头，其他文本都在该文本的右边。
     */
    fun setAutoSplitText(text: CharSequence?,indent: String?=null) {
        this.indent=indent
        setText(text)//fixme 必须先设置一下原始文本,必不可少。
        if (width > 0) {
            setAutoSplitText2()
        } else {
            //fixme 监听布局是否加载完成。
            if (onGlobalLayoutListener2 == null) {
                onGlobalLayoutListener2 = ViewTreeObserver.OnGlobalLayoutListener {
                    //宽和高不能为空，要返回具体的值。
                    if (width > 0 && height > 0 && onGlobalLayoutListener2 != null) {
                        //fixme 防止多次重复调用，只执行一次
                        if (Build.VERSION.SDK_INT >= 16 && onGlobalLayoutListener2 != null) {
                            viewTreeObserver?.removeOnGlobalLayoutListener(onGlobalLayoutListener2)
                        }
                        onGlobalLayoutListener2 = null
                        setAutoSplitText2()
                    }
                }
                if (onGlobalLayoutListener2 != null) {
                    viewTreeObserver?.addOnGlobalLayoutListener(onGlobalLayoutListener2)
                }
            }
        }
    }

    private fun setAutoSplitText2() {
        if (width > 0) {
            text?.trim()?.let {
                if (it.length > 0) {
                    var newText:String?=null
                    if (indent!=null){
                        newText = autoSplitText(this,indent)
                    }else{
                        newText = autoSplitText(this)
                    }
                    if (!TextUtils.isEmpty(newText)) {
                        setText(newText)
                    }
                }
            }
        }
    }

    //自动计算换行
    private fun autoSplitText(tv: AppCompatTextView): String {
        try {
            var rawText = tv.text.toString() //原始文本
            var tvPaint: Paint = tv.paint //paint，包含字体等信息
            var tvWidth = tv.width - tv.paddingLeft - tv.paddingRight.toFloat() //控件可用宽度
            //将原始文本按行拆分
            var rawTextLines = rawText.replace("\r".toRegex(), "").split("\n").toTypedArray()
            var sbNewText = StringBuilder()
            for (rawTextLine in rawTextLines) {
                if (tvPaint.measureText(rawTextLine) <= tvWidth) { //如果整行宽度在控件可用宽度之内，就不处理了
                    sbNewText.append(rawTextLine)
                } else { //如果整行宽度超过控件可用宽度，则按字符测量，在超过可用宽度的前一个字符处手动换行
                    var lineWidth = 0f
                    var cnt = 0
                    while (cnt != rawTextLine.length) {
                        val ch = rawTextLine[cnt]
                        lineWidth += tvPaint.measureText(ch.toString())
                        if (lineWidth <= tvWidth) {
                            sbNewText.append(ch)
                        } else {
                            sbNewText.append("\n")
                            lineWidth = 0f
                            --cnt
                        }
                        ++cnt
                    }
                }
                sbNewText.append("\n")
            }
            //把结尾多余的\n去掉
            if (!rawText.endsWith("\n")) {
                sbNewText.deleteCharAt(sbNewText.length - 1)
            }
            return sbNewText.toString()
        }catch (e:Exception){e.printStackTrace()}
        return ""
    }

    //自动计算换行+悬挂缩进效果
    private fun autoSplitText(tv: AppCompatTextView, indent: String?): String {
        try {
            val rawText = tv.text.toString() //原始文本
            val tvPaint: Paint = tv.paint //paint，包含字体等信息
            val tvWidth = tv.width - tv.paddingLeft - tv.paddingRight.toFloat() //控件可用宽度
            //将缩进处理成空格
            var indentSpace = ""
            var indentWidth = 0f
            if (!TextUtils.isEmpty(indent)) {
                val rawIndentWidth = tvPaint.measureText(indent)
                if (rawIndentWidth < tvWidth) {
                    while (tvPaint.measureText(indentSpace).also { indentWidth = it } < rawIndentWidth) {
                        indentSpace += " "
                    }
                }
            }
            //将原始文本按行拆分
            val rawTextLines = rawText.replace("\r".toRegex(), "").split("\n").toTypedArray()
            val sbNewText = StringBuilder()
            for (rawTextLine in rawTextLines) {
                if (tvPaint.measureText(rawTextLine) <= tvWidth) { //如果整行宽度在控件可用宽度之内，就不处理了
                    sbNewText.append(rawTextLine)
                } else { //如果整行宽度超过控件可用宽度，则按字符测量，在超过可用宽度的前一个字符处手动换行
                    var lineWidth = 0f
                    var cnt = 0
                    while (cnt != rawTextLine.length) {
                        val ch = rawTextLine[cnt]
                        //从手动换行的第二行开始，加上悬挂缩进
                        if (lineWidth < 0.1f && cnt != 0) {
                            sbNewText.append(indentSpace)
                            lineWidth += indentWidth
                        }
                        lineWidth += tvPaint.measureText(ch.toString())
                        if (lineWidth <= tvWidth) {
                            sbNewText.append(ch)
                        } else {
                            sbNewText.append("\n")
                            lineWidth = 0f
                            --cnt
                        }
                        ++cnt
                    }
                }
                sbNewText.append("\n")
            }
            //把结尾多余的\n去掉
            if (!rawText.endsWith("\n")) {
                sbNewText.deleteCharAt(sbNewText.length - 1)
            }
            return sbNewText.toString()
        }catch (e:Exception){e.printStackTrace()}
        return ""
    }

    override fun onDestroy() {
        super.onDestroy()
        this.indent=null
        if (Build.VERSION.SDK_INT >= 16 && onGlobalLayoutListener2 != null) {
            viewTreeObserver?.removeOnGlobalLayoutListener(onGlobalLayoutListener2)
        }
        onGlobalLayoutListener2 = null
    }
}
package cn.oi.klittle.era.widget.compat

import android.app.Activity
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Editable
import android.text.Selection
import android.text.TextUtils
import android.text.method.ArrowKeyMovementMethod
import android.text.method.HideReturnsTransformationMethod
import android.text.method.MovementMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView

import android.view.inputmethod.InputMethodManager
import cn.oi.klittle.era.helper.KAsteriskPasswordTransformationMethod
import cn.oi.klittle.era.utils.KLoggerUtils
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import org.jetbrains.anko.runOnUiThread
import java.util.concurrent.TimeUnit

/**
 * fixme 自定义文本输入框。拷贝了系统的EditText
 * fixme 文本长按默认自带复制黏贴功能。
 */

//fixme 文本框焦点错乱解决方案。文本框如果是布局内的第一个控件，就容易发生异常（系统Bug）。如果不是布局内的第一个控件，一般都不会发生焦点异常。

//fixme 方案一
//在父容器中，添加以下两个属性。一般只要添加 isFocusable=true即可。以防万一，两个属性都加上
//isFocusable=true
//isFocusableInTouchMode=true//fixme 设置了true之后，第一次点击时会聚焦，不会触发点击事件。第二次点击才会触发点击事件（即聚焦之后点击才会触发点击事件。）

//fixme 方案二
//在父容器中，第一个控件。添加如下：

//                view {
//                    isFocusable = true//必不可少
//                }.lparams {
//                    width = 0
//                    height = kpx.x(14)//测试发现，高度必须大于等于kpx.x(14)才有效。宽度无所谓
//                }

open class KMyEditText : KTextView {

    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {
        initUi()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initUi()
    }

    companion object {
        //fixme 手动调出软键盘,这个文本框不会主动弹出，需要手动调用。所以不需要担心输入框自动弹出的问题。
        fun showSoftInput(context: Context?, view: View) {
            try {
                if (context != null && context is Activity) {
                    var inputManager: InputMethodManager? = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager?.showSoftInput(view, 0)
                    inputManager = null
                    //KLoggerUtils.e("test", "弹出软键盘")
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        /**
         * fixme 强制隐藏软键盘(有效)
         * @param activity
         */
        fun hideSoftKeyboard(context: Context?, view: View? = null) {
            try {
                if (context != null && context is Activity) {
                    var inputManager: InputMethodManager? = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager?.hideSoftInputFromWindow((context as Activity).getCurrentFocus().getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN); //强制隐藏键盘
                    view?.let {
                        //fixme 这个能解决Dialog弹窗上面，软键盘不消失的问题。亲测。
                        inputManager?.hideSoftInputFromWindow(it.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN)
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        /**
         * fixme 软键盘交替（亲测可行！）（目前没有判断软键盘是否弹出的方法。）
         */
        fun toggleSoftInput(context: Context?) {
            try {
                if (context != null && context is Activity) {
                    var inputManager: InputMethodManager? = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager?.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0)//软键盘弹出消失交替（没有软键盘会弹出，有软键盘会消失）
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

    }

    /**
     * fixme 软键盘交替（亲测可行！）（目前没有判断软键盘是否弹出的方法。）
     */
    fun toggleSoftInput() {
        KMyEditText.toggleSoftInput(context)
    }

    //调出软键盘
    fun showSoftInput() {
        KMyEditText.showSoftInput(context, this)
    }

    //调出软键盘(防止初始化时软键盘调不出，所以延迟一下，再弹出软键盘，亲测有效。)
    fun showSoftInput2() {
        try {
            async {
                delay(30, TimeUnit.MILLISECONDS)//10毫秒足以。
                getContext()?.runOnUiThread {
                    KMyEditText.showSoftInput(this, this@KMyEditText)
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //隐藏软键盘
    fun hideSoftKeyboard() {
        KMyEditText.hideSoftKeyboard(context, this)
    }

    //初始化
    private fun initUi() {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//开启硬件加速,不然圆角没有效果
        clearFocus()//初始化不要聚焦
        isFocusable = true
        isFocusableInTouchMode = true//防止点击的时候，焦点发生变化。缺点就是点击事件必须在聚焦之后，点击才会触发。
        clearFocus()
        onFocusChange { v, hasFocus ->
            //KLoggerUtils.e("是否聚焦：\t"+hasFocus)
            if (hasFocus && v != null && v == this) {
                if (!isMoveMotion && pointSubTime < 500) {//触摸，长按。都不会调出软键盘。
                    //软键盘消失的时候，焦点就已经失去，已经发生改变。
                    //fixme 聚焦时，手动弹出文本框。每次短按时，都会触发这个事件。
                    showSoftInput(context, this)
                }
            }
        }
        clearFocus()//初始化不要聚焦
    }

    //设置光标宽度和颜色【8.0以上好像无效了】
    fun setCursorColor(color: Int) {
        try {
            var fCursorDrawableRes = TextView::class.java.getDeclaredField("mCursorDrawableRes")//获取这个字段
            fCursorDrawableRes.setAccessible(true)//代表这个字段、方法等等可以被访问
            var mCursorDrawableRes = fCursorDrawableRes.getInt(this)

            var fEditor = TextView::class.java.getDeclaredField("mEditor");
            fEditor.setAccessible(true);
            var editor = fEditor.get(this)

            var clazz = editor::class.java
            var fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
            fCursorDrawable.setAccessible(true);

            val drawables = arrayOfNulls<Drawable>(2)
            drawables[0] = getContext().getResources().getDrawable(mCursorDrawableRes);
            drawables[1] = getContext().getResources().getDrawable(mCursorDrawableRes);
            drawables[0]?.setColorFilter(color, PorterDuff.Mode.SRC_IN);//SRC_IN 上下层都显示。下层居上显示。
            drawables[1]?.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            fCursorDrawable.set(editor, drawables);
        } catch (e: Exception) {
            //Log.e("test", "光标颜色设置异常")
        }
    }

    //内容清除
    fun clear() {
        setText(null)
    }

    //显示密码
    fun showPassword() {
        setTransformationMethod(HideReturnsTransformationMethod.getInstance())//密码显示
        setSelection(length())//fixme 光标位置从1开始不是0.设置光标位置为最后一个。默认是第一个。
    }

    var passWordChar: Char? = null//自定义密码符号
    //隐藏密码
    fun hiddenPassword(passWordChar: Char? = this.passWordChar) {
        try {
            if (passWordChar == null) {
                setTransformationMethod(PasswordTransformationMethod.getInstance())//密码隐藏(系统默认密码符文。)
            } else {
                passWordChar?.let {
                    transformationMethod = KAsteriskPasswordTransformationMethod(it)//自定义密码符号
                }
            }
            setSelection(length())//设置光标
        } catch (e: Exception) {
        }

    }

    override fun getFreezesText(): Boolean {
        return true
    }

    override fun getDefaultEditable(): Boolean {
        return true
    }

    override fun getDefaultMovementMethod(): MovementMethod {
        return ArrowKeyMovementMethod.getInstance()
    }

    override fun getText(): Editable? {
        val text = super.getText() ?: return null
        // This can only happen during construction.
        if (text is Editable) {
            return super.getText() as Editable
        }
        super.setText(text, TextView.BufferType.EDITABLE)
        return super.getText() as Editable
    }

    private var inputTime = 0L//记录文本输入时间
    private var inputNum = 0//快速输入次数
    /**
     * fixme 解决setText(null)时的异常；
     */
    override fun setText(text: CharSequence?, type: TextView.BufferType) {
        try {
            if (getText() != text && (!getText().toString().equals(text))) {
                if (System.currentTimeMillis() - inputTime <= 3) {
                    if (inputNum >= 5) {//连续五次急速输入；就不正常了。（人的手速最快最快也要几百毫秒）
                        inputNum = 0
                        return//fixme 防止程序异常；极短时间疯狂输出。
                    }
                    inputNum++
                } else {
                    inputNum = 0
                }
                inputTime = System.currentTimeMillis()
                //fixme 防止重复赋值，防止异常。
                if (text != null) {
                    super.setText(text, TextView.BufferType.EDITABLE)
                } else {
                    super.setText("", TextView.BufferType.EDITABLE)
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            KLoggerUtils.e("输入框setText异常：\t" + e.message)
        }
    }

    /**
     * Convenience for [Selection.setSelection].
     */
    fun setSelection(start: Int, stop: Int) {
        Selection.setSelection(text, start, stop)
    }

    /**
     * Convenience for [Selection.setSelection].
     */
    fun setSelection(index: Int) {
        Selection.setSelection(text, index)
    }

    /**
     * Convenience for [Selection.selectAll].
     */
    fun selectAll() {
        Selection.selectAll(text)
    }

    /**
     * Convenience for [Selection.extendSelection].
     */
    fun extendSelection(index: Int) {
        Selection.extendSelection(text, index)
    }

    /**
     * Causes words in the text that are longer than the view's width to be ellipsized instead of
     * broken in the middle. [ TextUtils.TruncateAt#MARQUEE][TextUtils.TruncateAt.MARQUEE] is not supported.
     *
     * @param ellipsis Type of ellipsis to be applied.
     * @throws IllegalArgumentException When the value of `ellipsis` parameter is
     * [TextUtils.TruncateAt.MARQUEE].
     * @see TextView.setEllipsize
     */
    override fun setEllipsize(ellipsis: TextUtils.TruncateAt) {
        if (ellipsis == TextUtils.TruncateAt.MARQUEE) {
            throw IllegalArgumentException("EditText cannot use the ellipsize mode " + "TextUtils.TruncateAt.MARQUEE")
        }
        super.setEllipsize(ellipsis)
    }

    override fun getAccessibilityClassName(): CharSequence {
        return KMyEditText::class.java.name
    }

    /** @hide
     */
    protected fun supportsAutoSizeText(): Boolean {
        return false
    }

    /** @hide
     */
    fun onInitializeAccessibilityNodeInfoInternal(info: AccessibilityNodeInfo) {
        if (isEnabled) {
            if (Build.VERSION.SDK_INT >= 21) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_TEXT)
            }
        }
    }
}

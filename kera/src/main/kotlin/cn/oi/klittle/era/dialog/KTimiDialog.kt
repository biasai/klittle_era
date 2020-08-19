package cn.oi.klittle.era.dialog

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseDialog
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.exception.KCatchException
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.widget.compat.KTextView
import org.jetbrains.anko.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import java.lang.Exception

//fixme 没有标题，只有确定一个按钮。

//        val alert: KTimiDialog by lazy { KTimiDialog(this) }
//        alert.mession("是否确认退出？").positive("确定") {
//            KToast.showSuccess("点击确定")
//        }.isDismiss(false).show()

open class KTimiDialog(var act: Context, isStatus: Boolean = true, isTransparent: Boolean = false) :
        KBaseDialog(act, isStatus = isStatus, isTransparent = isTransparent) {

    override fun onCreateView(context: Context): View? {
        return context.UI {
            verticalLayout {
                gravity = Gravity.CENTER
                relativeLayout {
                    id = kpx.id("crown_alert_parent")
                    isClickable = true
                    setBackgroundResource(R.drawable.kera_drawable_alert)
                    if (Build.VERSION.SDK_INT >= 21) {
                        z = kpx.x(24f)//会有投影，阴影效果。
                    }
                    scrollView {
                        id = kpx.id("crown_scrollView")
                        isFillViewport = true
                        setVerticalScrollBarEnabled(false)
                        verticalLayout {
                            gravity = Gravity.CENTER
                            padding = 0
                            //内容
                            KTextView(this).apply {
                                id = kpx.id("crown_txt_mession")
                                textColor = Color.parseColor("#242424")
                                textSize = kpx.textSizeX(36)
                                gravity = Gravity.CENTER_VERTICAL
                                padding = 0
                                setLineSpacing(kpx.textSizeX(20f), 1f)
                            }.lparams {
                                width = wrapContent
                                height = matchParent
                                topMargin = kpx.x(12)
                                bottomMargin = 0
                                leftMargin = kpx.x(12)
                                rightMargin = leftMargin
                            }
                        }
                    }.lparams {
                        width = matchParent
                        height = matchParent
                        topMargin = 0
                        bottomMargin = 0
                        above(kpx.id("crown_line"))
                    }

                    //横线
                    view {
                        id = kpx.id("crown_line")
                        //backgroundColor = Color.parseColor("#F2F2F2")
                        backgroundColor = Color.parseColor("#50000000")
                    }.lparams {
                        above(kpx.id("crown_txt_Positive"))
                        width = matchParent
                        height = kpx.x(1)
                    }

                    //确定
                    textView {
                        id = kpx.id("crown_txt_Positive")
                        //textColor = Color.parseColor("#239F93")
                        //textColor = Color.parseColor("#7B7BF7")
                        textColor = Color.parseColor("#42A8E1")
                        textSize = kpx.textSizeX(36)
                        gravity = Gravity.CENTER
                        text = getString(R.string.kconfirm)//确定
                    }.lparams {
                        width = matchParent
                        height = kpx.x(100)
                        alignParentBottom()
                    }

                }.lparams {
                    width = kpx.x(620)
                    height = kpx.x(320)
                }
            }
        }.view
    }

    val container: View? by lazy { findViewById<View>(kpx.id("crown_alert_parent")) }//最外层容器

    val scrollView: ScrollView? by lazy { findViewById<ScrollView>(kpx.id("crown_scrollView")) }

    //信息文本
    var txt_mession: String? = ""
    val mession: KTextView? by lazy { findViewById<KTextView>(kpx.id("crown_txt_mession")) }
    open fun mession(mession: String? = null): KTimiDialog {
        txt_mession = mession
        return this
    }

    val positive: TextView? by lazy { findViewById<TextView>(kpx.id("crown_txt_Positive")) }

    //确定按钮
    open fun positive(postive: String? = getString(R.string.kconfirm), callback: (() -> Unit)? = null): KTimiDialog {
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
            //确定
            positive?.setOnClickListener {
                dismiss()
            }
            isDismiss(false)//默认不消失
            //isLocked(true)//fixme 屏蔽返回键(关闭只能点确定按钮)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onShow() {
        super.onShow()
        ctx?.runOnUiThread {
            try {
                //mession?.setText(txt_mession)
                mession?.setAutoSplitText(txt_mession)
            }catch (e:Exception){
                KLoggerUtils.e("KTimiDialog显示异常：\t"+KCatchException.getExceptionMsg(e),true)
            }
        }
    }
}
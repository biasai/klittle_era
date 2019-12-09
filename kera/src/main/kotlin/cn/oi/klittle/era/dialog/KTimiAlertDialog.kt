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
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.widget.KGradientScrollView
import cn.oi.klittle.era.widget.compat.KScrollTextView
import cn.oi.klittle.era.widget.compat.KShadowView
import cn.oi.klittle.era.widget.compat.KTextView
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.*

//        val alert: KTimiAlertDialog by lazy { KTimiAlertDialog(this) }
//        alert.title("温馨提示").mession("是否确认退出？").positive("确定") {
//            KToast.showSuccess("点击确定")
//        }.negative("NO") {
//            KToast.showError("NO!!!")
//        }.isDismiss(false).show()

open class KTimiAlertDialog(ctx: Context, isStatus: Boolean = true, isTransparent: Boolean = false) : KBaseDialog(ctx, isStatus = isStatus, isTransparent = isTransparent) {

    override fun onCreateView(context: Context): View? {
        return context.UI {
            verticalLayout {
                gravity = Gravity.CENTER
                relativeLayout {
                    id = kpx.id("crown_alert_parent")
                    isClickable = true
                    //background=resources.getDrawable(R.drawable.crown_drawable_alert)
                    setBackgroundResource(R.drawable.kera_drawable_alert)
                    if (Build.VERSION.SDK_INT >= 21) {
                        z = kpx.x(24f)//会有投影，阴影效果。
                    }
                    //标题
                    textView {
                        id = kpx.id("crown_txt_title")
                        textColor = Color.parseColor("#242424")
                        textSize = kpx.textSizeX(36)
                        text = getString(R.string.ktishi)//提示
                    }.lparams {
                        leftMargin = kpx.x(24)
                        rightMargin = leftMargin
                        width = wrapContent
                        height = wrapContent
                        topMargin = kpx.x(32)

                    }
                    //横线
                    view {
                        id = kpx.id("crown_line")
                        backgroundColor = Color.parseColor("#50000000")
                    }.lparams {
                        below(kpx.id("crown_txt_title"))
                        topMargin = kpx.x(24)
                        width = matchParent
                        height = kpx.x(1)
                    }
                    scrollView {
                        id = kpx.id("crown_scrollView")
                        isFillViewport = true
                        setVerticalScrollBarEnabled(false)
                        verticalLayout {
                            gravity = Gravity.CENTER_VERTICAL
                            padding = 0
                            //内容
                            KTextView(this).apply {
                                id = kpx.id("crown_txt_mession")
                                textColor = Color.parseColor("#242424")
                                textSize = kpx.textSizeX(34)
                                gravity = Gravity.CENTER_VERTICAL
                                padding = 0
                            }.lparams {
                                width = matchParent
                                height = matchParent
                                leftMargin = kpx.x(24)
                                rightMargin = kpx.x(10)//右边比左边少一点，效果较好。
                                topMargin = 0
                                bottomMargin = 0
                            }
                        }
                    }.lparams {
                        width = matchParent
                        height = matchParent
                        topMargin = 0
                        bottomMargin = 0
                        below(kpx.id("crown_line"))
                        above(kpx.id("crown_txt_Negative"))
                    }

                    //取消
                    textView {
                        id = kpx.id("crown_txt_Negative")
                        //textColor = Color.parseColor("#239F93")
                        textColor = Color.parseColor("#C8C5C9")
                        textSize = kpx.textSizeX(30)
                        leftPadding = kpx.x(24)
                        rightPadding = leftPadding
                        topPadding = kpx.x(8)
                        bottomPadding = kpx.x(32)
                        text = getString(R.string.kcancel)//取消
                    }.lparams {
                        alignParentBottom()
                        leftOf(kpx.id("crown_txt_Positive"))
                        width = wrapContent
                        height = wrapContent
                    }

                    //确定
                    textView {
                        id = kpx.id("crown_txt_Positive")
                        //textColor = Color.parseColor("#239F93")
                        textColor = Color.parseColor("#42A8E1")
                        textSize = kpx.textSizeX(30)
                        leftPadding = kpx.x(24)
                        rightPadding = leftPadding
                        topPadding = kpx.x(8)
                        bottomPadding = kpx.x(32)
                        text = getString(R.string.kconfirm)//确定
                    }.lparams {
                        alignParentBottom()
                        alignParentRight()
                        rightMargin = kpx.x(24)
                        leftOf(kpx.id("crown_txt_Positive"))
                        width = wrapContent
                        height = wrapContent
                    }

                }.lparams {
                    width = kpx.x(620)
                    height = kpx.y(300)
                }
            }
        }.view
    }

    val container: View? by lazy { findViewById<View>(kpx.id("crown_alert_parent")) }//最外层容器
    //标题栏文本
    var txt_title: String? = getString(R.string.ktishi)//提示
    val title: TextView? by lazy { findViewById<TextView>(kpx.id("crown_txt_title")) }
    open fun title(title: String? =  getString(R.string.ktishi)): KTimiAlertDialog {
        txt_title = title
        return this
    }

    val scrollView: ScrollView? by lazy { findViewById<ScrollView>(kpx.id("crown_scrollView")) }

    //信息文本
    var txt_mession: String? = ""
    val mession: KTextView? by lazy { findViewById<KTextView>(kpx.id("crown_txt_mession")) }
    open fun mession(mession: String? = null): KTimiAlertDialog {
        txt_mession = mession
        return this
    }

    val negative: TextView? by lazy { findViewById<TextView>(kpx.id("crown_txt_Negative")) }
    //左边，取消按钮
    open fun negative(negative: String? = getString(R.string.kcancel), callback: (() -> Unit)? = null): KTimiAlertDialog {
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
    open fun positive(postive: String? = getString(R.string.kconfirm), callback: (() -> Unit)? = null): KTimiAlertDialog {
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
        //取消
        negative?.setOnClickListener {
            dismiss()
        }
        //确定
        positive?.setOnClickListener {
            dismiss()
        }
        isDismiss(false)//默认不消失
    }


    private var standardHeight = 0//正常高度。
    private var mHeight=kpx.x(300)
    private var maxHeight= kpx.maxScreenHeight() - kpx.statusHeight*2 - kpx.navigationBarHeight - mHeight
    override fun onShow() {
        super.onShow()
        title?.setText(txt_title)
        mession?.setText(txt_mession)//先设置文本，再计算高度。
        var distanceHeight = 0//文本多出控件的高度
        async {
            //新开线程，防止文本框没有初始化完成，宽和高没有获取得到。
            ctx?.runOnUiThread {
                scrollView?.apply {
                    if (standardHeight!=null&&standardHeight <= 0) {
                        standardHeight = height//记录一开始文本正常的高度。
                    }
                }
                mession?.apply {
                    if (standardHeight!=null&&standardHeight > 0) {
                        distanceHeight = getTextHeight() - standardHeight//实时计算文本多出的高度。
                    }
                }
                if (distanceHeight > 0 && distanceHeight > maxHeight) {
                    distanceHeight =maxHeight
                }
                if (distanceHeight > 0) {
                    container?.apply {
                        if (distanceHeight > 0) {
                            layoutParams.apply {
                                height = mHeight + distanceHeight
                                requestLayout()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDismiss() {
        super.onDismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        mession?.onDestroy()
    }


}
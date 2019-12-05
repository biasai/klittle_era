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
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import org.jetbrains.anko.*
import java.util.concurrent.TimeUnit

//        val alert: KTopTimiDialog by lazy { KTopTimiDialog(this) }
//        alert.mession("提示").show()
/**
 * fixme 顶部提示框
 */
open class KTopTimiDialog(ctx: Context, isStatus: Boolean = true, isTransparent: Boolean = true) : KBaseDialog(ctx, isStatus = isStatus, isTransparent = isTransparent) {

    override fun initEvent() {
        super.initEvent()
        setWindowAnimations(R.style.kera_popuwindow_top)
    }

    override fun onCreateView(context: Context): View? {
        return context.UI {
            verticalLayout {
                gravity = Gravity.CENTER_HORIZONTAL
                isClickable = false
                //内容
                KTextView(this).apply {
                    id = kpx.id("crown_txt_mession")
                    //textColor = Color.parseColor("#242424")
                    textColor = Color.WHITE
                    textSize = kpx.textSizeX(36)
                    gravity = Gravity.CENTER_VERTICAL
                    topPadding = kpx.x(24) + kpx.statusHeight
                    bottomPadding = kpx.x(24)
                    radius {
                        bgVerticalColors(Color.RED, Color.parseColor("#DD5246"))
                    }
                    gravity = Gravity.CENTER
                }.lparams {
                    width = matchParent
                    height = wrapContent
                }
            }
        }.view
    }

    //信息文本
    var txt_mession: String? = ""
    val mession: KTextView by lazy { findViewById<KTextView>(kpx.id("crown_txt_mession")) }
    open fun mession(mession: String? = null): KTopTimiDialog {
        txt_mession = mession
        return this
    }


    init {
        isDismiss(true)//默认不消失
    }


    var job: Deferred<Any?>? = null
    override fun onShow() {
        super.onShow()
        mession?.setText(txt_mession)//先设置文本，再计算高度。
        job?.cancel()//取消协程
        job = async {
            delay(2000, TimeUnit.MILLISECONDS)
            dismiss()//定时关闭
            job = null
        }
    }

    override fun onDismiss() {
        super.onDismiss()
    }


}
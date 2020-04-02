package cn.oi.klittle.era.dialog

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ScrollView
import android.widget.TextView
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseDialog
import cn.oi.klittle.era.comm.KToast
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.toolbar.KToolbar
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.widget.KGradientScrollView
import cn.oi.klittle.era.widget.KGradientView
import cn.oi.klittle.era.widget.compat.KScrollTextView
import cn.oi.klittle.era.widget.compat.KShadowView
import cn.oi.klittle.era.widget.compat.KTextView
import org.jetbrains.anko.*
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.Deferred

//        val alert: KTopTimiDialog by lazy { KTopTimiDialog(this) }
//        alert.mession("提示").show()
/**
 * fixme 顶部提示框（从顶部出来，从顶部消失）
 */
open class KTopTimiDialog(ctx: Context, isStatus: Boolean = true, isTransparent: Boolean = true) : KBaseDialog(ctx, isStatus = isStatus, isTransparent = isTransparent) {

    override fun initEvent() {
        super.initEvent()
        setWindowAnimations(R.style.kera_popuwindow_top)
        setNotFocusable()//不拦截按键（如：返回键）
        isDismiss(true)//点击会消失。
    }

    override fun onCreateView(context: Context): View? {
        return context.UI {
            verticalLayout {
                gravity = Gravity.CENTER_HORIZONTAL
                isClickable = false
                isFocusable = false
                isFocusableInTouchMode = false
                //内容
                KTextView(this).apply {
                    id = kpx.id("crown_txt_mession")
                    //textColor = Color.parseColor("#242424")
                    textColor = Color.WHITE
                    textSize = kpx.textSizeX(36)
                    gravity = Gravity.CENTER_VERTICAL
                    //fixme 高度盖过KToolbar的高度比较好看，所以设置成30
                    topPadding = kpx.x(30) + kpx.statusHeight
                    bottomPadding = kpx.x(30)
                    radius {
                        //bgVerticalColors(Color.parseColor("#DC5A4F"), Color.parseColor("#FF8080"))//备份一下，这个淡红色效果还不错。
                        bgVerticalColors(Color.parseColor("#DC5A4F"), Color.parseColor("#FF8080"))
                    }
                    gravity = Gravity.CENTER
                }.lparams {
                    width = matchParent
                    height = wrapContent
                }
                KGradientView(this).apply {
                    id = kpx.id("shadow_view_bottom")
                    gradientColor(KToolbar.getShadowColor(), KToolbar.getShadowHeight(), Gravity.TOP)//使用KScrimUtil实现更柔和的渐变色。效果不错。
                }.lparams {
                    width = matchParent
                    height = KToolbar.getShadowHeight()//获取阴影线高度（使用KToolbar统一一下比较好）
                }
            }
        }.view.apply {
            isClickable = false
            isFocusable = false
            isFocusableInTouchMode = false
        }
    }

    //信息文本
    var txt_mession: String? = ""
    val mession: KTextView? by lazy { findViewById<KTextView>(kpx.id("crown_txt_mession")) }
    open fun mession(mession: String? = null): KTopTimiDialog {
        txt_mession = mession
        return this
    }

    //阴影线
    val shadowLine: KGradientView? by lazy { findViewById<KGradientView>(kpx.id("shadow_view_bottom")) }


    var job: Deferred<Any?>? = null
    override fun onShow() {
        super.onShow()
        mession?.setText(txt_mession)//先设置文本，再计算高度。
        job?.cancel()//取消协程
        job = GlobalScope.async {
            delay(2000)
            dismiss()//定时关闭
            job = null
        }
    }

    override fun onDismiss() {
        super.onDismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            job?.cancel()//取消协程
            job = null
            mession?.onDestroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}
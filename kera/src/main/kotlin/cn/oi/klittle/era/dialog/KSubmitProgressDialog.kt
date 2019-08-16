package cn.oi.klittle.era.dialog

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import cn.oi.klittle.era.base.KBaseDialog
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.view.KProgressCircleView
import org.jetbrains.anko.*
import cn.oi.klittle.era.widget.KToggleView
import cn.oi.klittle.era.widget.compat.KTextView

//调用案例
//KSubmitProgressDialog(this).apply {
//    mession("1/2\t提交中...")
//    show()
//}

/**
 * 提交网络进度条（系统进度条+带文本）
 * Created by 彭治铭 on 2018/6/24.
 */
open class KSubmitProgressDialog(ctx: Context, isStatus: Boolean = true, isTransparent: Boolean = false) : KBaseDialog(ctx, isStatus = isStatus, isTransparent = isTransparent) {

    var bg: KToggleView? = null
    var progress: KProgressCircleView? = null
    var mession: KTextView? = null
    override fun onCreateView(context: Context): View? {
        return context.UI {
            verticalLayout {
                gravity = Gravity.CENTER
                relativeLayout {
                    //背景
                    bg = KToggleView(this).apply {
                        toggle {
                            width = kpx.x(600)
                            height = kpx.x(300)
                            shadow_color = Color.BLACK
                            shadow_radius = kpx.x(10f)
                            all_radius(kpx.x(24f))
                            bgHorizontalColors(Color.parseColor("#28292E"), Color.parseColor("#2B2C31"), Color.parseColor("#2A2B30"))
                        }
                    }.lparams {
                        width = matchParent
                        height = matchParent
                        centerInParent()
                    }
                    //左边的进度条
                    progress = KProgressCircleView(this).apply {
                        load = null
                    }.lparams {
                        centerVertically()
                        alignParentLeft()
                    }
                    //右边的文本框
                    mession = KTextView(this).apply {
                        textColor = Color.WHITE
                        textSize = kpx.textSizeX(34f)
                        gravity = Gravity.CENTER
                        text = "1/1\t提交中..."
                    }.lparams {
                        alignParentLeft()
                        centerVertically()
                        leftMargin = kpx.x(190)
                        width = matchParent
                    }
                }.lparams {
                    width = kpx.x(630)
                    height = kpx.x(380)
                }
            }
        }.view
    }

    init {
        isDismiss(false)//触摸不消失
        isLocked(true)//屏蔽返回键
    }

    //文本设置
    fun mession(mession: String?) {
        this.mession?.setText(mession)
    }

    open override fun onShow() {
        super.onShow()
    }

    override fun onDismiss() {
        super.onDismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
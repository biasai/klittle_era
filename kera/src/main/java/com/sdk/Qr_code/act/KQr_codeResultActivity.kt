package com.sdk.Qr_code.act

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseActivity
import cn.oi.klittle.era.comm.KToast
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.helper.KUiHelper
import cn.oi.klittle.era.toolbar.KToolbar
import org.jetbrains.anko.*

/**
 * fixme 二维码结果显示页面;调用案例： KUiHelper.goQr_codeResultActivity(it)
 */
open class KQr_codeResultActivity : KBaseActivity() {

    override fun isDark(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui {
            verticalLayout {
                backgroundColor = Color.WHITE
                KToolbar(this, ctx as Activity)?.apply {
                    contentView?.apply {
                        backgroundColor = Color.parseColor("#EDEDED")
                    }
                    //左边返回文本（默认样式自带一个白色的返回图标）
                    leftTextView?.apply {
                        autoBg {
                            width = kpx.x(50)
                            height = width
                            autoBg(R.mipmap.kera_error2)
                            isAutoCenterVertical = true
                            isAutoCenterHorizontal = false
                            autoLeftPadding = toolbarOffset.toFloat()
                        }
                    }
                    //中间文本
                    titleTextView?.apply {
                    }
                    //右上角图片选择器图标
                    rightTextView?.apply {

                    }
                    bottomGradientView?.apply {
                        visibility = View.INVISIBLE//隐藏阴影线。
                    }
                }
                kscrollView {
                    verticalLayout {
                        ktextView {
                            textColor = Color.BLACK
                            textSize = kpx.textSizeX(33)
                            isBold(true)
                            intent?.extras?.getString(KUiHelper.qr_result)?.trim()?.let {
                                text = it
                                if (it.length > 0) {
                                    onClick {
                                        copyText()
                                        KToast.showInfo(getString(R.string.textCopy))//文本已复制
                                    }
                                }
                            }
                            leftPadding = kpx.x(8)
                            bottomPadding = leftPadding
                        }
                    }
                }.lparams {
                    width = matchParent
                    height = matchParent
                }
            }
        }
    }
}

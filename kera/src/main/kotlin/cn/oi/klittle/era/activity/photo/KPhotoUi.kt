package cn.oi.klittle.era.activity.photo

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import cn.oi.klittle.era.R
import cn.oi.klittle.era.activity.photo.adapter.KPhotoAdapter
import cn.oi.klittle.era.activity.photo.manager.KPictureSelector
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.toolbar.KToolbar
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.widget.compat.KTextView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

open class KPhotoUi : KBaseUi() {

    var spanCount = KPictureSelector.imageSpanCount//网格，列的个数；4列太小了，感觉还是3列效果最好。

    var toolbar: KToolbar? = null
    var preview: KTextView? = null//预览
    var complete: KTextView? = null//请选择，已完成
    var num: KTextView? = null//图片选择个数
    var photoAdapter: KPhotoAdapter? = null
    override fun createView(ctx: Context?): View? {
        return ctx?.UI {
            verticalLayout {
                backgroundColor = Color.WHITE
                toolbar = KToolbar(this, getActivity())?.apply {
                    //标题栏背景色
                    contentView?.apply {
                        backgroundColor = Color.parseColor("#0078D7")
                    }
                    //左边返回文本（默认样式自带一个白色的返回图标）
                    leftTextView?.apply {
                        autoBg {
                            //                            width = kpx.x(60)//和PDA的返回键大小保持一致。
//                            height = width
//                            autoBg(R.mipmap.kera_ic_back)
//                            autoBgColor = Color.WHITE
                        }
                    }
                    titleTextView?.apply {
                        textColor = Color.WHITE
                        textSize = kpx.textSizeX(32)
                        gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                        autoBg {
                            width = kpx.x(35)
                            height = width
                            autoBg(R.mipmap.kera_bottom)
                            autoBgColor = Color.WHITE
                            isAutoRight = true
                            isAutoCenterVertical = true
                        }
                        autoBg_selected {
                            autoBg(R.mipmap.kera_top)
                        }
                        rightPadding = kpx.x(50)
                        text = KBaseUi.Companion.getString(R.string.kxiangjijiaojuan)//相机胶卷
                    }
//                    rightTextView?.apply {
//                        text = KBaseUi.Companion.getString(R.string.kcancel)//取消
//                        textColor = Color.WHITE
//                        textSize = kpx.textSizeX(28)
//                        onClick {
//                            getActivity()?.finish()
//                        }
//                    }
                }
                relativeLayout {
                    krecyclerView {
                        setGridLayoutManager(spanCount)
                        photoAdapter = KPhotoAdapter()
                        adapter = photoAdapter
                    }.lparams {
                        width = matchParent
                        height = matchParent
                        above(kpx.id("photo_bottom"))
                    }
                    //底部
                    relativeLayout {
                        id = kpx.id("photo_bottom")
                        backgroundColor = Color.parseColor("#FAFAFA")
                        preview = ktextView {
                            text = KBaseUi.Companion.getString(R.string.kpreview)//预览
                            txt {
                                textSize = kpx.textSizeX(28)
                                textColor = Color.parseColor("#9B9B9B")
                            }
                            txt_selected {
                                textColor = Color.parseColor("#FA632D")
                            }
                        }.lparams {
                            centerVertically()
                            leftMargin = kpx.x(24)
                        }

                        complete = ktextView {
                            id = kpx.id("kcomplete")
                            txt {
                                text = KBaseUi.Companion.getString(R.string.kchoose_photo)//请选择
                                textSize = kpx.textSizeX(28)
                                textColor = Color.parseColor("#9B9B9B")
                            }
                            txt_selected {
                                textColor = Color.parseColor("#FA632D")
                                text = KBaseUi.Companion.getString(R.string.kcomplete)//已完成
                            }
                            gravity=Gravity.CENTER
                        }.lparams {
                            width = wrapContent
                            //height = wrapContent
                            height= matchParent
                            centerVertically()
                            alignParentRight()
                            rightMargin = kpx.x(24)
                        }

                        num = ktextView {
                            backgroundColor = Color.parseColor("#FA632D")
                            textColor = Color.WHITE
                            textSize = kpx.textSizeX(24)
                            gravity = Gravity.CENTER
                            radius {
                                all_radius(kpx.x(200))
                            }
                            visibility=View.INVISIBLE
                        }.lparams {
                            width = kpx.x(42)
                            height = width
                            centerVertically()
                            leftOf(kpx.id("kcomplete"))
                            rightMargin = kpx.x(12)
                        }
                    }.lparams {
                        width = matchParent
                        height = kpx.x(88)
                        alignParentBottom()
                    }
                }
            }
        }?.view
    }
}
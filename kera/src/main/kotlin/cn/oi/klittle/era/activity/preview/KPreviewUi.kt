package cn.oi.klittle.era.activity.preview

import android.content.Context
import android.graphics.Color
import android.support.v4.view.ViewPager
import android.view.Gravity
import android.view.View
import cn.oi.klittle.era.R
import cn.oi.klittle.era.activity.photo.entity.KLocalMedia
import cn.oi.klittle.era.activity.photo.manager.KPictureSelector
import cn.oi.klittle.era.activity.preview.adpater.KPreviewPagerAdapter
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.comm.KToast
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.toolbar.KToolbar
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.widget.compat.KTextView
import cn.oi.klittle.era.widget.viewpager.KViewPager
import org.jetbrains.anko.*

open class KPreviewUi : KBaseUi() {

    var toolbar: KToolbar? = null
    var complete: KTextView? = null//请选择，已完成
    var num: KTextView? = null//图片选择个数
    var viewPager: KViewPager? = null
    var previewAdapter: KPreviewPagerAdapter? = null

    var data: KLocalMedia? = null//当前选中的实体类

    //设置标题
    fun setTitle(title: String = (KPictureSelector.previewIndex + 1).toString() + "/" + KPictureSelector.previewMeidas?.size) {
        toolbar?.titleTextView?.setText(title)
    }

    fun setTitle(postion: String = (KPictureSelector.previewIndex + 1).toString(), size: String = KPictureSelector.previewMeidas?.size.toString()) {
        toolbar?.titleTextView?.setText(postion + "/" + size)
    }


    //选中个数回调
    fun checkNumCallback(checkNum: Int) {
        num?.setText(checkNum.toString())
        if (checkNum > 0) {
            num?.visibility = View.VISIBLE
            complete?.isSelected = true//已完成
        } else {
            num?.visibility = View.INVISIBLE
            complete?.isSelected = false//请选择
        }
    }

    fun initRightTextView(rightText:KTextView?=toolbar?.rightTextView) {
        if (KPictureSelector.isCheckable) {
            rightText?.apply {
                if (data != null) {
                    txt_selected {
                        text = data?.checkedNum.toString()
                    }
                    isSelected = data!!.isChecked!!
                }
            }
        }
    }

    override fun createView(ctx: Context?): View? {

        data = KPictureSelector.previewMeidas?.get(KPictureSelector.previewIndex)//当前选中的数据

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
                    }
                    titleTextView?.apply {
                        textColor = Color.WHITE
                        textSize = kpx.textSizeX(32)
                        gravity = Gravity.CENTER
                    }
                    rightTextView?.apply {
                        setText(null)
                    }
                    //判断图片预览是否具备选中能力
                    if (KPictureSelector.isCheckable) {
                        rightTextView?.apply {
                            layoutParams(width = kpx.x(64), height = kpx.x(64), rightMargin = kpx.x(12))
                            radius {
                                x = kpx.x(12f)
                                y = x
                                width = kpx.x(40)
                                height = width
                                all_radius(kpx.x(200))
                                strokeColor = Color.WHITE
                                strokeWidth = kpx.x(2f)
                            }
                            radius_selected {
                                bg_color = Color.parseColor("#00CAFC")
                            }
                            txt {
                                textColor = Color.TRANSPARENT
                                text = ""
                            }
                            txt_selected {
                                textColor = Color.WHITE
                                textSize = kpx.textSizeX(26f)
                            }
                            initRightTextView(this)//防止初始化的时候为空，所以最好还是传参进去。
                            onClick {
                                if (data == null) {
                                    return@onClick
                                }
                                data?.isChecked = !data!!.isChecked!!
                                isSelected = data!!.isChecked!!
                                if (isSelected) {
                                    //选中
                                    if (KPictureSelector.addNum(data)) {
                                        txt_selected {
                                            text = data?.checkedNum.toString()//显示当前选中数量
                                        }
                                    } else {
                                        isSelected = false
                                        //你最多可以选择%s张图片
                                        KToast.showInfo(KBaseUi.getString(R.string.kmaxSelectNum, KPictureSelector.maxSelectNum.toString()))
                                    }
                                } else {
                                    //取消选中
                                    KPictureSelector.reduceNum(data)
                                }
                                checkNumCallback(KPictureSelector.currentSelectNum)
                            }
                            leftPadding = 0
                            rightPadding = 0
                            gravity = Gravity.CENTER
                        }
                    }
                }
                relativeLayout {
                    viewPager = kviewPager {
                        id = kpx.id("kviewPager")
                        isScrollEnable = true
                        isFastScrollEnable = false
                        if (previewAdapter == null) {
                            KPictureSelector.previewMeidas?.let {
                                previewAdapter = KPreviewPagerAdapter(it)
                            }
                        }
                        adapter = previewAdapter
                        addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                            override fun onPageScrollStateChanged(state: Int) {

                            }

                            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                            }

                            override fun onPageSelected(position: Int) {
                                //fixme 滑动监听
                                KPictureSelector.previewMeidas?.let {
                                    if (it.size > position) {
                                        data = KPictureSelector.previewMeidas?.get(position)
                                        initRightTextView()
                                        setTitle(postion = (position + 1).toString())
                                    }
                                }
                                previewAdapter?.videoMap?.get(position - 1)?.let {
                                    it?.pause()
                                }
                                previewAdapter?.videoMap?.get(position + 1)?.let {
                                    it?.pause()
                                }
                            }
                        })
                    }.lparams {
                        width = matchParent
                        height = matchParent
                        above(kpx.id("photo_bottom"))
                    }
                    //判断预览是否具备图片选中能力
                    if (KPictureSelector.isCheckable) {
                        //底部
                        relativeLayout {
                            id = kpx.id("photo_bottom")
                            backgroundColor = Color.parseColor("#FAFAFA")
                            num = ktextView {
                                backgroundColor = Color.parseColor("#FA632D")
                                textColor = Color.WHITE
                                textSize = kpx.textSizeX(24)
                                gravity = Gravity.CENTER
                                radius {
                                    all_radius(kpx.x(200))
                                }
                            }.lparams {
                                width = kpx.x(42)
                                height = width
                                centerVertically()
                                leftOf(kpx.id("kcomplete"))
                                rightMargin = kpx.x(12)
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
                        }.lparams {
                            width = matchParent
                            height = kpx.x(88)
                            alignParentBottom()
                        }
                    }
                }
            }
        }?.view
    }
}
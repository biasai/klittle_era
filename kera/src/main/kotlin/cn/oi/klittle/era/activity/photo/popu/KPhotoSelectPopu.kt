package cn.oi.klittle.era.activity.photo.popu

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.R
import cn.oi.klittle.era.activity.photo.manager.KPictureSelector
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.popu.KSpinnerPop
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.utils.KSelectorUtils
import cn.oi.klittle.era.widget.compat.KTextView
import cn.oi.klittle.era.widget.compat.KView
import com.luck.picture.lib.config.PictureConfig
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

/**
 * 图片列表选择框
 */
open class KPhotoSelectPopu {
    var sp: KSpinnerPop? = null
    fun create(context: Context) {
        //数据
        KPictureSelector.folders?.let {
            var datas = it
            sp = KSpinnerPop(context, it)
            //创建recyclerView内部item视图,参数为下标position
            sp?.onCreateView(popWidth = kpx.screenWidth(),isNeverScroll = false) {
                context.UI {
                    relativeLayout {
                        id = kpx.id("popu_layout")
                        KSelectorUtils.selectorRippleDrawable(this, Color.WHITE, Color.parseColor("#EFF3F6"))
                        //KSelectorUtils.selectorRippleDrawable(this, Color.WHITE, Color.parseColor("#C0C0C0"))
                        //要设置具体的宽度kpx.screenWidth();不要使用wrapContent和matchParent
                        var layoutParams = ViewGroup.LayoutParams(kpx.screenWidth(), kpx.x(130))
                        setLayoutParams(layoutParams)
                        KBaseUi.apply {
                            kview {
                                id = kpx.id("popu_img")
                                radius {
                                    all_radius(kpx.x(12))
                                }
                                autoBg {
                                    isGlide = true
                                    width = kpx.x(110)
                                    height = width
                                }
                            }.lparams {
                                width = kpx.x(110)
                                height = width
                                centerVertically()
                                leftMargin = kpx.x(12)
                            }
                            ktextView {
                                id = kpx.id("popu_txt")
                                textColor = Color.BLACK
                                textSize = kpx.textSizeX(28)
                            }.lparams {
                                width = wrapContent
                                height = wrapContent
                                rightOf(kpx.id("popu_img"))
                                leftMargin = kpx.x(18)
                                topMargin=kpx.x(18)
                            }
                        }
                    }
                }.view
            }
            //视图刷新[业务逻辑都在这处理]，返回 视图itemView和下标postion
            sp?.onBindView { itemView, position ->
                var data = datas[position]
                itemView?.findViewById<View>(kpx.id("popu_layout")).apply {
                    if (position== KPictureSelector.checkedFolderIndex){
                        //backgroundColor=Color.parseColor("#C0C0C0")
                        backgroundColor=Color.parseColor("#EFF3F6")
                    }
                    onClick {
                        KPictureSelector.checkedFolderIndex=position
                        sp?.pop?.dismiss()//关闭
                    }
                }
                itemView?.findViewById<KView>(kpx.id("popu_img")).apply {
                    autoBg {
                        if (KPictureSelector.type == PictureConfig.TYPE_AUDIO) {
                            autoBg(R.mipmap.kera_icon_audio)//音频没有图片。
                        }else{
                            autoBgFromFile(data.firstImagePath)
                        }
                    }
                    onClick {
                        KPictureSelector.checkedFolderIndex=position
                        sp?.pop?.dismiss()//关闭
                    }
                }
                itemView?.findViewById<KTextView>(kpx.id("popu_txt")).apply {
                    setText(data.name + "(" + data.imageNum + ")")
                    onClick {
                        KPictureSelector.checkedFolderIndex=position
                        sp?.pop?.dismiss()//关闭
                    }
                }
            }
        }
    }

    //显示
    fun showAsDropDown(view: View?) {
        sp?.showAsDropDown(view, 0, 0)
    }

}
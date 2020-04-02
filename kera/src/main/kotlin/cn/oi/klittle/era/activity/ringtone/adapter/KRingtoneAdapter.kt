package cn.oi.klittle.era.activity.ringtone.adapter

import android.graphics.Color
import android.media.Ringtone
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.utils.KRingtoneManagerUtils
import cn.oi.klittle.era.utils.KSelectorUtils
import cn.oi.klittle.era.widget.compat.KTextView
import cn.oi.klittle.era.widget.compat.KView
import cn.oi.klittle.era.widget.recycler.adapter.KAdapter
import org.jetbrains.anko.*
//import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * fixme 这个Adaper适配器是为了方便测试使用的。
 * Created by 彭治铭 on 2019/3/18.
 */
open class KRingtoneAdapter(var datas: MutableList<Ringtone>? = null) : KAdapter<KRingtoneAdapter.Companion.MyViewHolder>() {

    companion object {

        open class MyViewHolder(itemView: View?, var viewType: Int) : RecyclerView.ViewHolder(itemView) {
            var left_txt: KTextView? = null//左边的文本
            var item_checked: KView? = null
            var item_layout: View? = null

            init {
                //正常
                left_txt = itemView?.findViewById(kpx.id("item_txt"))
                item_checked = itemView?.findViewById(kpx.id("item_checked"))
                item_layout = itemView?.findViewById(kpx.id("item_layout"))
            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        //正常
        var itemView = parent.context.UI {
            KBaseUi.apply {
                verticalLayout {
                    relativeLayout {
                        id = kpx.id("item_layout")
                        KSelectorUtils.selectorRippleDrawable(this, Color.WHITE, Color.parseColor("#EFF3F6"))
                        //左边的文本
                        KTextView(this).apply {
                            id = kpx.id("item_txt")
                            textSize = kpx.textSizeX(30)
                            textColor = Color.parseColor("#8C8D9F")
                            gravity = Gravity.CENTER_VERTICAL
                            isClickable = false
                            padding = 0
                        }.lparams {
                            width = kpx.screenWidth() / 3 * 2
                            height = kpx.x(45)
                            centerVertically()
                            leftMargin = kpx.x(24)
                            topMargin=kpx.x(18)
                            bottomMargin=topMargin
                        }
                        kview {
                            id = kpx.id("item_checked")
                            autoBg {
                                width = kpx.x(40)
                                height = width
                                //autoBg(R.mipmap.kera_ic_unchecked)
                                autoBg(R.mipmap.kera_ic_unselect)
                            }
                            autoBg_selected {
                                //autoBg(R.mipmap.kera_ic_checked)
                                autoBg(R.mipmap.kera_ic_select)
                            }
                        }.lparams {
                            width = kpx.x(40)
                            height = width
                            centerVertically()
                            alignParentRight()
                            rightMargin = kpx.x(24)
                            topMargin=kpx.x(10)
                            bottomMargin=topMargin
                        }
                    }.lparams {
                        width = matchParent
                        height = wrapContent
                    }
                    //分界线
                    view {
                        backgroundColor = Color.LTGRAY
                    }.lparams {
                        width = matchParent
                        height = kpx.x(1)
                    }
                }
            }
        }.view
        return MyViewHolder(itemView, viewType)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        try {
            //正常
            holder?.left_txt?.setText(null)
            var data = datas?.get(position)
            data?.let {
                //名称
                holder?.left_txt?.setText(KRingtoneManagerUtils.getTitle(position))
                holder?.item_checked?.isSelected = (position == KRingtoneManagerUtils.index)
                holder?.item_layout?.onClick {
                    checked(position)
                }
                holder?.item_checked?.onClick {
                    checked(position)
                }
            }
        }catch (e:Exception){e.printStackTrace()}

    }

    //选中
    fun checked(index: Int) {
        try {
            var index1 = KRingtoneManagerUtils.index
            KRingtoneManagerUtils.index = index
            KRingtoneManagerUtils.play(index)
            notifyItemChanged(index1)
            notifyItemChanged(index)
        }catch (e:java.lang.Exception){e.printStackTrace()}
    }


    override fun getItemViewType(position: Int): Int {
        //return super.getItemViewType(position)
        return position
    }

    override fun getItemCount(): Int {
        return datas?.size ?: 0
    }
}
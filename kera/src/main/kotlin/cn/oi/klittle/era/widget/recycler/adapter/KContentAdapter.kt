package cn.oi.klittle.era.widget.recycler.adapter

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KSelectorUtils
import cn.oi.klittle.era.widget.compat.KTextView
import cn.oi.klittle.era.widget.recycler.KFooterView
import org.jetbrains.anko.*

//                    fixme ConcatAdapter组合适配器的使用。
//                    //1.2.0-alpha04版本，将MergeAdapter重命名为ConcatAdapter。
//                    //组合适配器，适配器的显示顺序就是concatAdater.addAdapter（）的顺序。
//                    var concatAdater = ConcatAdapter()
//                    var contentAdapter=KContentAdapter(data)
//                    //fixme 刷新的使用，一定要调用具体的适配器进行刷新 contentAdapter?.notifyDataSetChanged()
//                    //fixme concatAdater?.notifyDataSetChanged() 是没有刷新效果的。组合适配器刷新要制定具体的适配器。
//                    concatAdater.addAdapter(contentAdapter)
//                    concatAdater.addAdapter(KFooterAdapter())
//                    adapter = concatAdater

/**
 * fixme 具体内容适配器。
 * Created by 彭治铭 on 2019/3/18.
 */
open class KContentAdapter(var datas: MutableList<String>? = null) : KAdapter<KContentAdapter.Companion.MyViewHolder>() {

    companion object {

        open class MyViewHolder(itemView: View, var viewType: Int) : RecyclerView.ViewHolder(itemView) {
            var left_txt: KTextView? = null//左边的文本

            init {
                //正常
                left_txt = itemView?.findViewById(kpx.id("item_txt"))
            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        //正常
        var itemView = parent.context.UI {
            verticalLayout {
                relativeLayout {
                    KSelectorUtils.selectorRippleDrawable(this, Color.WHITE, Color.parseColor("#EFF3F6"))
                    //左边的文本
                    KTextView(this).apply {
                        id = kpx.id("item_txt")
                        textSize = kpx.textSizeX(30)
                        textColor = Color.parseColor("#8C8D9F")
                        gravity = Gravity.CENTER
                        isClickable = false
                        padding = 0
                    }.lparams {
                        width = wrapContent
                        height = kpx.x(45)
                        centerVertically()
                        leftMargin = kpx.x(24)
                    }
                }.lparams {
                    width = matchParent
                    height = kpx.x(88)
                }
                //分界线
                view {
                    backgroundColor = Color.LTGRAY
                }.lparams {
                    width = matchParent
                    height = kpx.x(1)
                }
            }
        }.view

        return MyViewHolder(itemView, viewType)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        //正常
        holder?.left_txt?.setText(null)
        var data = datas?.get(position)
        data?.let {
            //名称
            holder?.left_txt?.setText(data)
        }
    }


    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return datas?.size ?: 0
    }
}
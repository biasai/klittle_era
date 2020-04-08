package cn.oi.klittle.era.widget.recycler.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KSelectorUtils
import cn.oi.klittle.era.widget.compat.KTextView
import cn.oi.klittle.era.widget.recycler.KFooterView
import org.jetbrains.anko.*

/**
 * fixme 这个Adaper适配器是为了方便测试使用的。
 * Created by 彭治铭 on 2019/3/18.
 */
open class KRecyclerAdapter(var datas: MutableList<String>? = null) : KAdapter<KRecyclerAdapter.Companion.MyViewHolder>() {

    companion object {
        var viewType_footView_loadMore = -200//末尾item(加载更多)
        var viewType_footView_loadMoreComplete = -201//末尾item(加载更多完成，没有更多数据了)

        open class MyViewHolder(itemView: View, var viewType: Int) : RecyclerView.ViewHolder(itemView) {
            var left_txt: KTextView? = null//左边的文本

            init {
                if (viewType == viewType_footView_loadMore) {
                    //加载更多
                } else if (viewType == viewType_footView_loadMoreComplete) {
                    //没有更多数据了
                } else {
                    //正常
                    left_txt = itemView?.findViewById(kpx.id("item_txt"))
                }
            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var itemView: View? = null
        if (viewType == viewType_footView_loadMore) {
            //加载更多
            itemView = parent.context.UI {
                verticalLayout {
                    gravity = Gravity.CENTER
                    KFooterView(this).apply {
                        circle_radus = kpx.x(30f)//半径
                        circle_strokeWidth = kpx.x(3f)
                        circle_x = kpx.screenWidth() / 2f - circle_radus*2//圆心x坐标
                        gravity = Gravity.CENTER_VERTICAL
                        setTextSize(kpx.textSizeX(26))//字体大小
                        setTextColor(Color.parseColor("#5D5D5D"))//字体颜色
                        setText(KBaseUi.getString(R.string.kloadMore))//正在加载...
                        leftPadding = (circle_x+circle_radus*2).toInt()
                    }.lparams {
                        width = kpx.screenWidth()//设置具体的宽度和高度，不然可能无法显示。
                        height = kpx.x(100)
                    }
                }
            }.view
        } else if (viewType == viewType_footView_loadMoreComplete) {
            //加载更多完成，没有更多数据了。
            itemView = parent.context.UI {
                verticalLayout {
                    gravity = Gravity.CENTER
                    KTextView(this).apply {
                        gravity = Gravity.CENTER_VERTICAL
                        setTextSize(kpx.textSizeX(26))//字体大小
                        setTextColor(Color.parseColor("#5D5D5D"))//字体颜色
                        setText(KBaseUi.getString(R.string.kloadMoreComplete))//没有更多数据了哦
                        var centerX=kpx.centerX(getTextWidth(),kpx.screenWidth())
                        leftPadding=centerX+kpx.x(50)
                        autoBg {
                            width=kpx.x(50)
                            height=width
                            autoBg(R.mipmap.kera_cry)
                            isAutoCenterVertical=true
                            autoLeftPadding= (leftPadding-width).toFloat()-kpx.x(24)
                        }
                    }.lparams {
                        width = kpx.screenWidth()
                        height = kpx.x(100)
                    }
                }
            }.view
        } else {
            //正常
            itemView = parent.context.UI {
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
        }
        return MyViewHolder(itemView, viewType)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (holder.viewType == viewType_footView_loadMore) {
            //加载更多
        } else if (holder.viewType == viewType_footView_loadMoreComplete) {
            //加载更多完成，没有更多数据了
        } else {
            //正常
            holder?.left_txt?.setText(null)
            var data = datas?.get(position)
            data?.let {
                //名称
                holder?.left_txt?.setText(data)
            }
        }
    }


    var isLoadMore = true//是否加载更多
    var isLoadMoreComplete = false//是否加载更多完成；false 没完成，可以继续加载更多；true 加载完成（没有更多数据了）
    override fun getItemViewType(position: Int): Int {
        //return super.getItemViewType(position)
        if (isLoadMore) {
            datas?.size?.let {
                if (position == it) {
                    if (isLoadMoreComplete) {
                        return viewType_footView_loadMoreComplete//加载更多完成
                    } else {
                        return viewType_footView_loadMore//加载更多
                    }
                }
            }
        }
        return position
    }

    override fun getItemCount(): Int {
        if (isLoadMore) {
            datas?.size?.let {
                if (it > 0) {
                    return it + 1//加载更多，末尾多加一个item
                }
            }
        }
        return datas?.size ?: 0
    }
}
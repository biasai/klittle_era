package cn.oi.klittle.era.widget.recycler.adapter

import android.support.v7.widget.RecyclerView
import android.view.animation.AnimationUtils
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseUi

abstract class KAdapter<VH : RecyclerView.ViewHolder>() : RecyclerView.Adapter<VH>() {

    override fun getItemViewType(position: Int): Int {
        //return super.getItemViewType(position)
        return position//返回下标
    }

    //fixme 是否开启动画，默认false
    open fun isOpenAinme(): Boolean {
        return false
    }

    //fixme 默认动画(子类可以重写)
    open fun onAnime(holder: VH, position: Int) {
        if (position % 2 === 0) {
            var animation_alpha = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.kera_zebra_left_in_without_alpha)
            holder.itemView.startAnimation(animation_alpha)//左边进入
        } else {
            var animation_alpha = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.kera_zebra_right_in_without_alpha)
            holder.itemView.startAnimation(animation_alpha)//右边进入
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (isOpenAinme()) {
            onAnime(holder, position)
        }
    }

    //fixme 是否释放AutoBg位图;默认false
    open fun isRecycleView(): Boolean {
        return false
    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        if (isRecycleView()) {
            holder.itemView?.let {
                KBaseUi.recycleAutoBgBitmap(it)//fixme (仅仅只释放AutoBg位图)
            }
        }
    }
}
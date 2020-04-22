package cn.oi.klittle.era.widget.recycler.adapter

import android.os.Build
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
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
        try {
            if (isOpenAinme()) {
                onAnime(holder, position)
            }
            if (isRecycleView2()) {
                if (vhMap == null) {
                    vhMap = linkedMapOf<String, VH?>()
                }
                vhMap?.put(position.toString(), holder)
            }
            //getItemY(holder) {}
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * fixme 获取Item与RecyvlerView顶部之间的距离。(一般在onBindViewHolder()方法里调用。)
     * @return 回调返回，距离值y
     */
    fun getItemY(holder: VH, callback: ((y: Float) -> Unit)? = null) {
        if (callback != null) {
            //fixme 局部变量，不会冲突的。获取item与RecyvlerView顶部之间的距离。
            var onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
            onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
                var y = holder?.itemView?.y//fixme 与父容器RecyvlerView顶部之间的距离。
                callback?.let {
                    if (y != null) {
                        it(y)
                    }
                }
                if (Build.VERSION.SDK_INT >= 16 && onGlobalLayoutListener != null) {
                    holder?.itemView?.viewTreeObserver?.removeOnGlobalLayoutListener(
                            onGlobalLayoutListener
                    )//移除监听
                }
                onGlobalLayoutListener = null
            }
            holder?.itemView?.viewTreeObserver?.addOnGlobalLayoutListener(
                    onGlobalLayoutListener
            )//监听布局加载
        }
    }

    //fixme 是否释放AutoBg位图;默认false
    open fun isRecycleView(): Boolean {
        return false
    }

    override fun onViewRecycled(holder: VH) {
        try {
            super.onViewRecycled(holder)
            if (isRecycleView()) {
                holder.itemView?.let {
                    KBaseUi.recycleAutoBgBitmap(it)//fixme (仅仅只释放AutoBg位图)
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    //fixme 是否开启onViewRecycled2();默认false
    open fun isRecycleView2(): Boolean {
        return false
    }

    private var vhMap: LinkedHashMap<String, VH?>? = null
    /**
     * fixme 手动销毁，调用了onDestroy()才会调用。交给子类去实现重写。
     */
    open fun onViewRecycled2(holder: VH?) {
        holder?.let {
            onViewRecycled(it)
        }
    }

    /**
     * fixme 销毁,最后记得主动置空
     */
    open fun onDestroy() {
        try {
            vhMap?.forEach {
                it.value?.let {
                    onViewRecycled2(it)
                }
            }
            vhMap?.clear()
            vhMap = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
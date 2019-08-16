package cn.oi.klittle.era.widget.recycler

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MotionEvent
import android.view.View.OVER_SCROLL_NEVER
import java.lang.reflect.Field

object KRecyclerUtils {

    //判断是否滑动到顶部
    fun isRecyclerViewTop(recyclerView: RecyclerView?): Boolean {
        if (recyclerView != null) {
            val layoutManager = recyclerView.layoutManager
            if (layoutManager is LinearLayoutManager) {
                val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                if (firstVisibleItemPosition == 0) {
                    return true
                }
            }
        }
        return false
    }

    //判断是否滑动到底部
    fun isRecyclerViewBottom(recyclerView: RecyclerView?): Boolean {
        if (recyclerView != null) {
            val layoutManager = recyclerView.layoutManager
            if (layoutManager is LinearLayoutManager) {
                val firstVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()
                //Log.e("test", "p:\t" + firstVisibleItemPosition + "\t" + (layoutManager.itemCount - 1))
                if (firstVisibleItemPosition == layoutManager.itemCount - 1) {
                    return true
                }
            }
        }
        return false
    }

    //设置RecyclerView最大滑动速度。
    fun setMaxFlingVelocity(recyclerView: RecyclerView?, velocity: Int) {
        recyclerView?.let {
            try {
                Log.e("test", "最大滑动速度:\t" + it.maxFlingVelocity)
                //it.maxFlingVelocity获取最大滑动速度（默认是24000）。是val类型，不能更改。只能通过反射更改。
                var field: Field = it.javaClass.getDeclaredField("mMaxFlingVelocity")
                field.isAccessible = true
                field.set(it, velocity)
            } catch (e: Exception) {
                Log.e("test", "RecyclerView最大滑动速度设置异常:\t" + e.message)
            }
        }
    }

    /**
     * 尽可能的解决RecyclerView和NestedScrollView同时滑动冲突。
     * 快速滑动的冲突，没有解决！
     */
    fun canScrollVertically(recyclerView: RecyclerView?) {
        recyclerView?.let {
            it.setOverScrollMode(OVER_SCROLL_NEVER);//设置滑动到边缘时无效果模式
            it.setVerticalScrollBarEnabled(false);//滚动条隐藏
            var py = 0f
            it?.setOnTouchListener { v, event ->
                //var cy = event.getY()//相对于父容器
                var cy = event.rawY//相对于整个父容器
                if (event.action == MotionEvent.ACTION_DOWN || py == 0f) {
                    py = cy
                    //Log.e("test", "按下:\t" + py + "\t" + cy)
                    //按下无法监听,因为按下已经被ScrollView先监听了。所以用0表示按下
                }
                //Log.e("test", "cy：当前\t" + cy + "\tpy:上一个\t" + py + "\t差:\t" + (cy - py) + "\tsy:\t" + it?.isNestedScrollingEnabled)
                //if (cy - py > 0 && py != 0f) {
                if (cy - py > 0) {
                    //向上滚动
                    if (isRecyclerViewTop(it)) {
                        //滑动了顶部
                        if (it?.isNestedScrollingEnabled) {
                            it?.isNestedScrollingEnabled = false
                            //Log.e("test", "禁止")
                        }
                    }
                } else if (cy - py != 0f) {
                    //向下滚动
                    if (isRecyclerViewBottom(it)) {
                        //滑动了底部
                        if (it?.isNestedScrollingEnabled) {
                            it?.isNestedScrollingEnabled = false
                            //Log.e("test", "禁止2")
                        }
                    } else if (!it?.isNestedScrollingEnabled) {
                        it?.isNestedScrollingEnabled = true
                    }
                }
                py = cy
                //离开可以监听到。
                if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                    if (!it?.isNestedScrollingEnabled) {
                        it?.isNestedScrollingEnabled = true
                    }
                    py = 0f
                    //Log.e("test", "离开")
                }
                false
            }

        }
    }
}
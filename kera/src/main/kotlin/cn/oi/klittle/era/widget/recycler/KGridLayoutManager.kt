package cn.oi.klittle.era.widget.recycler

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception

/**
 * fixme 解决recyclerView刷新时异常；这是Google的天坑。时不时会报错。以下已经解决。
 */
open class KGridLayoutManager(context: Context?, spanCount: Int) : GridLayoutManager(context, spanCount) {
    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            //fixme 捕捉数据刷新异常问题。
            super.onLayoutChildren(recycler, state)
            //fixme 另一种解决方案：
            //fixme 适配器数据个数发生改变时，最好及时调用一下notifyDataSetChanged()；不然就会报数据下标异常问题。
        } catch (e: Exception) {
            e.printStackTrace()
            //KLoggerUtils.e("LinearLayoutManagery异常：\t" + e.message)
        }
    }
}
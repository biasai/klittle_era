package cn.oi.klittle.era.widget.recycler

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception

/**
 * fixme 解决recyclerView刷新时异常；这是Google的天坑。是不是会报错。以下已经解决。
 */
open class KLinearLayoutManager(context: Context?) : LinearLayoutManager(context) {
    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: Exception) {
            e.printStackTrace()
            //KLoggerUtils.e("LinearLayoutManagery异常：\t" + e.message)
        }
    }
}
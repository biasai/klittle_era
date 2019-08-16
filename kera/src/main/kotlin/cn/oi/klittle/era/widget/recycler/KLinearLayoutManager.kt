package cn.oi.klittle.era.widget.recycler

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import cn.oi.klittle.era.utils.KLoggerUtils
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
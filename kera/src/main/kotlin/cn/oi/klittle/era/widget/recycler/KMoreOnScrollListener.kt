package cn.oi.klittle.era.widget.recycler

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

//MoreOnScrollListener 实现里面的loadMore()加载更多方法即可。

//addOnScrollListener(object : KMoreOnScrollListener(this@apply.layoutManager as LinearLayoutManager) {
//    override fun loadMore() {
//        async {
//            delay(1000)//延迟一秒
//            //模拟数据加载
//            for (i in 0..24){
//                list.add(i.toString())
//            }
//            runOnUiThread {
//                this@apply.adapter?.notifyDataSetChanged()//刷新
//            }
//        }
//    }
//})

/**
 * 滑动到底部加载更多
 */
abstract class KMoreOnScrollListener : RecyclerView.OnScrollListener {
    internal var linearLayoutManager: LinearLayoutManager
    var lastVisibleItemPostion: Int = 0
    var isDragging = false//判断是否为手动滑动。
    var isPull = false//判断是否为上拉

    //GridLayoutManager也继承于LinearLayoutManager
    constructor(linearLayoutManager: LinearLayoutManager) : super() {
        this.linearLayoutManager = linearLayoutManager
    }


    //item滑动时(item上下随手指滑动而移动)，才会调用
    //dx 水平滑动距离。大于0，屏幕向左滑动。
    //dy 垂直滑动距离。大于0，屏幕向上滑动(上拉)。
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        //Log.e("test", "onScrolled()\t dx:\t" + dx + "\tdy:\t" + dy);
        lastVisibleItemPostion = linearLayoutManager.findLastVisibleItemPosition()
        if (dy > 0) {
            isPull = true
        } else {
            isPull = false
        }
    }

    //滑动状态，会在开始滑动和停止滑动的时候调用。与item是否滑动无关。与手指是否滑动有关。
    //newState三个状态
    // RecyclerView.SCROLL_STATE_DRAGGING=1;正在被外部拖拽,一般为用户正在用手指滚动。开始滚动。
    // RecyclerView.SCROLL_STATE_SETTLING=2;自动滚动开始，一般手动滑动之后，都会带有一点自动滑动。
    // RecyclerView.SCROLL_STATE_IDLE=0闲置，没有滑动。结束滚动。
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        //Log.e("test", "onScrollStateChanged()\tnewState:\t$newState")
        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            isDragging = true
        }
        if (isPull && isDragging && newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItemPostion + 1 == linearLayoutManager.itemCount) {
            //Log.e("test", "滑动到底部")
            loadMore()
        }
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            isDragging = false
        }
    }

    //上拉加载更多
    abstract fun loadMore()

}
package cn.oi.klittle.era.widget.recycler

import android.content.Context
import android.graphics.*
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

//           fixme 带有悬浮置顶的Item;使用案例：
//            KBaseUi.apply {
//                krecyclerView {
//                    setLinearLayoutManager(true)
//                    //数据
//                    var data = arrayListOf<String>().apply {
//                        for (i in 1..100) {
//                            add("NO:\t" + i)
//                        }
//                    }
//                    adapter = KRecyclerAdapter(data)//适配器
//                    //指定悬浮下标标。从小到大进行排序
//                    var positiones = mutableListOf<Int>()
//                    addItemPositiones(positiones)?.apply {
//                        for (i in 0..1024) {
//                            if (i % 5 == 0) {
//                                add(i)
//                            }
//                        }
//                    }
//                    //控制各个Item间距
//                    addItemOffset { outRect, itemPosition ->
//                        outRect?.apply {
//                            top = kpx.x(0)//item底部的间距
//                            bottom = kpx.x(0)//item底部的间距
//                        }
//                    }
//                    //添加置顶悬浮View
//                    addItemView { canvas, paint, position, width, height, scrollY ->
//                        //postion当前悬浮item的下标。
//                        //scrollY当前悬浮item左上角y坐标。绘图位置以这个y值为准。
//                        //width,height是当前悬浮视图的宽度和高度
//                        paint.color = Color.WHITE
//                        var l = 0f
//                        var r = width - l
//                        var t = scrollY
//                        var b = height + scrollY
//                        canvas.drawRect(RectF(l, t, r, b), paint)//画矩形
//                        paint.textAlign = Paint.Align.LEFT
//                        paint.color = Color.parseColor("#8C8D9F")
//                        paint.textSize = kpx.x(30f)
//                        //var x = kpx.centerTextX(position.toString(), paint, w.toFloat())//居中
//                        var x = kpx.x(24f)
//                        var centerY = kpx.centerTextY(paint, height.toFloat()) + scrollY
//                        canvas.drawText(data[position], x, centerY, paint)//画垂直居中文字
//                        paint.color = Color.LTGRAY
//                        var startY = height - kpx.x(1f) + scrollY
//                        canvas.drawLine(0f, startY, width.toFloat(), startY, paint)//画底部线
//                    }
//
//                }.lparams {
//                    width = matchParent
//                    height = matchParent
//                }
//            }

//            fixme 悬浮置顶，下拉刷新，上拉加载更多。（没有更多数据了）；完整使用案例。
//            KBaseUi.apply {
//                swipeRefreshLayout {
//                    //颜色进度条，参数个数没有上限。自己随意添加颜色值，int类型，多个用逗号隔开。
//                    setColorSchemeColors(Color.parseColor("#0078D7"), Color.parseColor("#00FF40"), Color.parseColor("#00FFFF"), Color.parseColor("#0080C0"));
//                    //数据
//                    var mData = arrayListOf<String>().apply {
//                        for (i in 1..20) {
//                            add("" + i)
//                        }
//                    }
//                    var recyclerview = krecyclerView {
//                        setLinearLayoutManager(true)
//                        adapter = KRecyclerAdapter(mData)//适配器
//                        //指定悬浮下标标。从小到大进行排序
//                        var positiones = mutableListOf<Int>()
//                        addItemPositiones(positiones)?.apply {
//                            for (i in 0..1024) {
//                                if (i % 5 == 0) {
//                                    add(i)
//                                }
//                            }
//                        }
//                        //控制各个Item间距
//                        addItemOffset { outRect, itemPosition ->
//                            outRect?.apply {
//                                top = kpx.x(0)//item底部的间距
//                                bottom = kpx.x(0)//item底部的间距
//                            }
//                        }
//                        //添加置顶悬浮View
//                        addItemView { canvas, paint, position, width, height, scrollY ->
//                            //postion当前悬浮item的下标。
//                            //scrollY当前悬浮item左上角y坐标。绘图位置以这个y值为准。
//                            //width,height是当前悬浮视图的宽度和高度
//                            paint.color = Color.WHITE
//                            var l = 0f
//                            var r = width - l
//                            var t = scrollY
//                            var b = height + scrollY
//                            canvas.drawRect(RectF(l, t, r, b), paint)//画矩形
//                            paint.textAlign = Paint.Align.LEFT
//                            paint.color = Color.parseColor("#8C8D9F")
//                            paint.textSize = kpx.x(30f)
//                            //var x = kpx.centerTextX(position.toString(), paint, w.toFloat())//居中
//                            var x = kpx.x(24f)
//                            var centerY = kpx.centerTextY(paint, height.toFloat()) + scrollY
//                            canvas.drawText(mData[position], x, centerY, paint)//画垂直居中文字
//                            paint.color = Color.LTGRAY
//                            var startY = height - kpx.x(1f) + scrollY
//                            canvas.drawLine(0f, startY, width.toFloat(), startY, paint)//画底部线
//                        }
//
//                        //加载更多
//                        addLoadMore {
//                            //KLoggerUtils.e("加载更多：\t")
//                            isLoadMore = true//正在加载更多
//                            async {
//                                delay(500)//延迟
//                                //模拟数据加载
//                                for (i in mData.size + 1..mData.size + 20) {
//                                    mData.add(i.toString())
//                                }
//                                runOnUiThread {
//                                    adapter?.notifyDataSetChanged()//刷新
//                                    isLoadMore = false//加载更多完成
//                                    if (mData.size >= 150 && adapter is KRecyclerAdapter) {
//                                        isLoadMoreComplete = true//加载更多完成
//                                        (adapter as KRecyclerAdapter).isLoadMoreComplete = true//没有更多数据了。
//                                    }
//                                }
//                            }
//                        }
//
//                    }
//                    //下拉刷新监听
//                    setOnRefreshListener {
//                        setRefreshing(true);//正在刷新，true刷新进度圈会一直显示。false刷新进度圈会消失。刷新结束。
//                        recyclerview.adapter?.apply {
//                            mData.apply {
//                                clear()
//                                for (i in 1..20) {
//                                    add("" + i)
//                                }
//                            }
//                            notifyDataSetChanged()
//                            recyclerview?.apply {
//                                isLoadMoreComplete=false//还有数据。
//                            }
//                            if (this is KRecyclerAdapter){
//                                isLoadMoreComplete=false//还有数据。
//                            }
//                            setRefreshing(false)//刷新完成
//                        }
//                    }
//                }.lparams {
//                    width = matchParent
//                    height = matchParent
//                }
//
//            }

open class KRecyclerView : RecyclerView {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {}

    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    /**
     * 隐藏滑动条和边缘滑动效果
     */
    open fun hiddenScroll() {
        setOverScrollMode(View.OVER_SCROLL_NEVER);//设置滑动到边缘时无效果模式
        setVerticalScrollBarEnabled(false);//滚动条隐藏
    }

    //layoutManager 布局管理器
    //adapter 适配器

    /**
     * 设置网格布局管理器；fixme 需要手动调用
     * @param spanCount 每行网格的个数（列数）
     */
    open fun setGridLayoutManager(spanCount: Int) {
        context?.let {
            layoutManager = KGridLayoutManager(context, spanCount)
            setHasFixedSize(true)
        }
    }

    /**
     * 设置线行布局管理器；fixme 需要手动调用
     * @param isVertical 是否垂直；fixme 默认就是垂直。是true
     */
    open fun setLinearLayoutManager(isVertical: Boolean = true) {
        context?.let {
            var linearLayoutManager = KLinearLayoutManager(context)
            if (isVertical) {
                linearLayoutManager.orientation = LinearLayoutManager.VERTICAL//垂直线性布局
            } else {
                linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL//水平线性布局
            }
            layoutManager = linearLayoutManager
            setHasFixedSize(true)
        }
    }

    /**
     * 尽可能的解决RecyclerView和NestedScrollView同时滑动冲突。
     * 快速滑动的冲突，没有解决！
     */
    fun canScrollVertically() {
        KRecyclerUtils.canScrollVertically(this)
    }

    //添加悬浮置顶ItemView
    private var khoverItemDecoration: KHoverItemDecoration? = null

    private fun addItemDecoration() {
        if (khoverItemDecoration == null) {
            khoverItemDecoration = object : KHoverItemDecoration() {
                //控制各个Item间距
                override fun getItemOffsets(outRect: Rect?, itemPosition: Int, parent: RecyclerView?) {
                    super.getItemOffsets(outRect, itemPosition, parent)
//                    itemPosition//当前item的下标
//                    outRect?.apply {
//                        top = kpx.x(0)//item底部的间距
//                        bottom = kpx.x(0)//item底部的间距
//                    }
                    if (outRect != null) {
                        addItemOffset?.let {
                            it(outRect, itemPosition)
                        }
                    }
                }
            }.apply {
                this.positiones = positiones//悬浮的下标数组
                //w = px.realWidth().toInt()//悬浮宽度,默认就是第一个item的宽度
                //h = px.x(100)//悬浮高度，默认就是第一个item的高度
                //创建悬浮的itemView
                itemView { canvas, paint, position, y ->
                    addItemView?.let {
                        it(canvas, paint, position, w, h, y)
                    }
                    //postion当前悬浮item的下标。
                    //y当前悬浮item左上角y坐标。绘图位置以这个y值为准。
//                    paint.color = Color.WHITE
//                    var l = 0f
//                    var r = w - l
//                    var t = y
//                    var b = h + y
//                    canvas.drawRect(RectF(l, t, r, b), paint)//画矩形
//                    paint.textAlign = Paint.Align.LEFT
//                    paint.color = Color.parseColor("#8C8D9F")
//                    paint.textSize = kpx.x(30f)
//                    //var x = kpx.centerTextX(position.toString(), paint, w.toFloat())//居中
//                    var x = kpx.x(24f)
//                    var centerY = kpx.centerTextY(paint, h.toFloat()) + y
//                    canvas.drawText(data[position], x, centerY, paint)//画垂直居中文字
//                    paint.color = Color.LTGRAY
//                    var startY = h - kpx.x(1f) + y
//                    canvas.drawLine(0f, startY, w.toFloat(), startY, paint)//画底部线
                }
            }
            addItemDecoration(khoverItemDecoration)
        }
    }

    /**
     * 控制item之间的间距
     */
    private var addItemOffset: ((outRect: Rect, itemPosition: Int) -> Unit)? = null

    fun addItemOffset(addItemOffset: ((outRect: Rect, itemPosition: Int) -> Unit)? = null) {
        this.addItemOffset = addItemOffset
        addItemDecoration()
    }

    /**
     * 创建悬浮的itemView【使用画布画笔进行绘图，保证了效率。】
     */
    private var addItemView: ((canvas: Canvas, paint: Paint, position: Int, width: Int, height: Int, scrollY: Float) -> Unit)? = null

    open fun addItemView(addItemView: ((canvas: Canvas, paint: Paint, position: Int, width: Int, height: Int, scrollY: Float) -> Unit)? = null) {
        this.addItemView = addItemView
        addItemDecoration()
    }

    /**
     * 指定悬浮ItemView的下标组数。
     */
    fun addItemPositiones(positiones: MutableList<Int>? = null): MutableList<Int>? {
        addItemDecoration()
        khoverItemDecoration?.addPositiones(positiones)
        return khoverItemDecoration?.positiones
    }

    //添加置顶悬浮下标
    fun addItemPositiones(position: Int) {
        addItemDecoration()
        khoverItemDecoration?.addPositiones(position)
    }

    //移除悬浮的下标
    fun removeItemPositiones(position: Int) {
        addItemDecoration()
        khoverItemDecoration?.removePositiones(position)
    }

    //判断是否正在加载更多，防止重复加载。加载更多时，需要手动设置true，加载完了手动设置false
    var isLoadMore = false
    //是否加载更多完成；false 没完成，可以继续加载更多；true 加载完成（没有更多数据了,不会再回调加载更多。）
    var isLoadMoreComplete = false
    private var isHasLoadMore = false
    private fun addLoadMore() {
        if (!isHasLoadMore && layoutManager != null && layoutManager is LinearLayoutManager) {
            isHasLoadMore = true
            addOnScrollListener(object : KMoreOnScrollListener(layoutManager as LinearLayoutManager) {
                override fun loadMore() {
                    loadMore?.let {
                        if (!isLoadMore && !isLoadMoreComplete) {
                            try {
                                it()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            })
        }
    }

    //加载更多，（滑动到底部时会调用）
    private var loadMore: (() -> Unit)? = null

    fun addLoadMore(loadMore: (() -> Unit)?) {
        this.loadMore = loadMore
        addLoadMore()
    }
}
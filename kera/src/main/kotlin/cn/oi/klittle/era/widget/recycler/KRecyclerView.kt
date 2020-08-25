package cn.oi.klittle.era.widget.recycler

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.oi.klittle.era.exception.KCatchException
import cn.oi.klittle.era.utils.KLoggerUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.jetbrains.anko.runOnUiThread

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

//                fixme 有垂直进度条的KRecyclerView调用案例(可以多次调用)
//                kRecyclerViewBar(ctx,this)?.apply {
//                    setLinearLayoutManager()
//                    var datas: MutableList<String>? = mutableListOf()
//                    for (i in 0..300) {
//                        datas?.add("" + i)
//                    }
//                    adapter = KRecyclerAdapter(datas)
//                    lparams {
//                        width= matchParent
//                        height=kpx.screenHeight()/2
//                    }
//                }

//                        fixme 下拉刷新，加载更多在：KMoreOnScrollListener类里。
//                        swipeRefreshLayout {
//                            //颜色进度条，参数个数没有上限。自己随意添加颜色值，int类型，多个用逗号隔开。
//                            setColorSchemeColors(Color.parseColor("#FF8080"), Color.parseColor("#FFFF00"), Color.parseColor("#00FF40"));
//                            onRefresh {
//                                //正在刷新，true刷新进度圈会一直显示。false刷新进度圈会消失。刷新结束。数据加载完成。需要手动设置成false
//                                setRefreshing(true);
//                            }
//                            krecyclerView {  }
//                        }

//fixme addOnItemTouchListener()内部RecyclerView添加触摸事件
//fixme setSecondaryHeight()设置内部RcyclerView的具体高度。
open class KRecyclerView : RecyclerView {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {}

    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    /**
     * fixme 滑动置顶(亲测有效)
     */
    fun scrollToTop() {
        if (adapter == null || layoutManager == null) {
            return
        }
        try {
            scrollToPositionWithOffset(0)//fixme 滑动置顶，没有滑动效果。基本都是有效的。
            GlobalScope.async {
                try {
                    delay(200)//fixme 延迟200毫秒，防止无效。(防止初始化未完成)
                    try {
                        getContext()?.let {
                            if (it is Activity) {
                                if (!it.isFinishing) {
                                    it.runOnUiThread {
                                        try {
                                            adapter?.itemCount?.let {
                                                if (it > 0) {
                                                    layoutManager?.scrollToPosition(0)
                                                }
                                            }
                                        } catch (e: java.lang.Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * fixme 滑动到指定下标位置(亲测有效,但是不具体滑动效果)
     * @param position 下标，位置从0开始。(下标越界或者小于0，也不会报错。只是会无效而已。)
     * @param offset 偏移量
     */
    fun scrollToPositionWithOffset(position: Int, offset: Int = 0) {
        if (adapter == null || layoutManager == null) {
            return
        }
        adapter?.let {
            if (position >= 0 && position < it.itemCount) {
                try {
                    layoutManager?.let {
                        //GridLayoutManager也继承LinearLayoutManager
                        if (it is LinearLayoutManager) {
                            it.scrollToPositionWithOffset(position, offset)//fixme 这一步基本都是有效的。(不具备滑动效果)
                            GlobalScope.async {
                                try {
                                    delay(100)//延迟100毫秒，再来一次。低于200毫秒的。肉眼是感觉不出来的。
                                    getContext()?.let {
                                        if (it is Activity) {
                                            if (!it.isFinishing) {
                                                it.runOnUiThread {
                                                    try {
                                                        layoutManager?.let {
                                                            if (it is LinearLayoutManager) {
                                                                it.scrollToPositionWithOffset(position, offset)//fixme 这一步，只是为了以防万一无效，再来一遍。(一般都是有效的。没有这一步也无所谓的。)
                                                            }
                                                        }
                                                    } catch (e: java.lang.Exception) {
                                                        e.printStackTrace()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (e: java.lang.Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * fixme 显示垂直滑动条(没有效果，必须通过xml布局加载设置android:scrollbars="vertical"才会有滑动条，以下代码没有效果。)
     * fixme 请使用：kRecyclerViewBar(ctx,this)，这个有滑动条。
     */
    fun setVerticalScrollBarEnabled() {
        setVerticalScrollBarEnabled(true);
        setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
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
     * 设置网格布局管理器；fixme 需要手动调用，捕捉修复适配器下标异常错误问题！！KLinearLayoutManager捕捉了异常。
     * @param spanCount 每行网格的个数（列数）
     */
    open fun setGridLayoutManager(spanCount: Int) {
        layoutManager?.let {
            if (it is GridLayoutManager) {
                if (it.spanCount == spanCount) {
                    return//防止重复添加
                }
            }
        }
        context?.let {
            layoutManager = KGridLayoutManager(context, spanCount)
            setHasFixedSize(true)
        }
    }

    /**
     * 设置线行布局管理器；fixme 需要手动调用，捕捉修复适配器下标异常错误问题！！KLinearLayoutManager捕捉了异常。
     * @param isVertical 是否垂直；fixme 默认就是垂直。是true
     */
    open fun setLinearLayoutManager(isVertical: Boolean = true) {
        layoutManager?.let {
            if (it is LinearLayoutManager) {
                if (isVertical) {
                    if (it.orientation == LinearLayoutManager.VERTICAL) {
                        //垂直
                        return//防止重复添加
                    }
                } else {
                    if (it.orientation == LinearLayoutManager.HORIZONTAL) {
                        //水平
                        return//防止重复添加
                    }
                }
            }
        }
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

    override fun setAdapter(adapter: Adapter<*>?) {
        try {
            super.setAdapter(adapter)
            adapter?.let {
                if (layoutManager == null) {
                    setLinearLayoutManager()//fixme 防止遗忘，防止视图不显示。
                }
                if (it.itemCount > 0) {
                    it?.notifyDataSetChanged()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        View.VISIBLE//0
//        View.INVISIBLE//4
//        View.GONE//8
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
                override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {
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
            khoverItemDecoration?.let {
                addItemDecoration(it)
            }
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

    private var onItemTouchListener: OnItemTouchListener? = null

    /**
     * fixme 添加滑动触摸事件；针对内部RecyclerView；
     * fixme 防止二级RecyclerView滑动无效。
     */
    fun addOnItemTouchListener() {
        onItemTouchListener?.let {
            removeOnItemTouchListener(it)
        }
        if (onItemTouchListener == null) {
            onItemTouchListener = object : RecyclerView.OnItemTouchListener {
                override fun onTouchEvent(p0: RecyclerView, p1: MotionEvent) {
                }

                override fun onInterceptTouchEvent(recyclerView: RecyclerView, p1: MotionEvent): Boolean {
                    recyclerView.parent.requestDisallowInterceptTouchEvent(true)
                    return false
                }

                override fun onRequestDisallowInterceptTouchEvent(p0: Boolean) {
                }
            }
        }
        onItemTouchListener?.let {
            addOnItemTouchListener(it)
        }
    }

    /**
     * fixme 设置高度；主要争对内部RecyclerView;防止数据过大。视图无限实例化。导致内存溢出报错。
     * fixme 二级适配器高度最好固定。因为内部适配器的布局不会重复利用。会无限重新实例化。
     * @param isWrapConent 高度是否自适应
     * @param height 具体的高度。isWrapConent=false才有效。
     */
    fun setSecondaryHeight(isWrapConent: Boolean, height: Int) {
        context?.let {
            it.runOnUiThread {
                try {
                    layoutParams?.let {
                        if (isWrapConent) {
                            it.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        } else {
                            it.height = height
                        }
                        requestLayout()//重新布局，width或height属性改变时才有效。内部会自动判断的。
                    }
                } catch (e: java.lang.Exception) {
                    KLoggerUtils.e("RecyclerView高度异常：\t" + KCatchException.getExceptionMsg(e), true)
                }
            }
        }

    }

    /**
     * fixme 销毁,最后记得主动置空
     */
    open fun onDestroy() {
        try {
            onItemTouchListener?.let {
                removeOnItemTouchListener(it)
            }
            onItemTouchListener = null
            adapter = null
            removeAllViews()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun draw(c: Canvas?) {
        try {
            super.draw(c)
        } catch (e: java.lang.Exception) {
            KLoggerUtils.e("RecyclerView draw异常：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
        }
    }

    override fun dispatchDraw(canvas: Canvas?) {
        try {
            super.dispatchDraw(canvas)
        } catch (e: java.lang.Exception) {
            KLoggerUtils.e("RecyclerView dispatchDraw异常：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        try {
            super.onMeasure(widthSpec, heightSpec)
        } catch (e: java.lang.Exception) {
            KLoggerUtils.e("RecyclerView onMeasure异常：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
        }
    }

}
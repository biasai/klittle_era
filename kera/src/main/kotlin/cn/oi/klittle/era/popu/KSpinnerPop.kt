package cn.oi.klittle.era.popu

import android.content.Context
import android.view.View
import android.view.View.OVER_SCROLL_NEVER
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KPopuWindowUtils
import org.jetbrains.anko.*
//import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onClick

//                        fixme 调用案例：
//                        //数据
//                        var list = mutableListOf<String>()
//                        list.add("1")//fixme 记得添加数据，不然不会显示哦。
//                        list.add("2")
//                        list.add("3")
//                        var sp = KSpinnerPop(ctx, list)
//                        //创建recyclerView内部item视图,参数为下标position
//                        sp?.onCreateView(popWidth = kpx.screenWidth()) {
//                            context.UI {
//                                verticalLayout {
//                                    backgroundColor = Color.WHITE
//                                    //要设置具体的宽度kpx.screenWidth();不要使用wrapContent和matchParent
//                                    var layoutParams = ViewGroup.LayoutParams(kpx.screenWidth(), kpx.x(100))
//                                    setLayoutParams(layoutParams)
//                                    gravity = Gravity.CENTER
//                                    KBaseUi.apply {
//                                        ktextView {
//                                            id = kpx.id("pop_txt")
//                                            gravity = Gravity.CENTER
//                                            textSize = kpx.textSizeX(30f)
//                                            textColor = Color.BLACK
//                                            KSelectorUtils.selectorRippleDrawable(this, Color.WHITE, Color.parseColor("#EFF3F6"))
//                                        }.lparams {
//                                            width = matchParent
//                                            height = kpx.x(99)
//                                        }
//                                    }
//                                    view {
//                                        backgroundColor=Color.LTGRAY//分界线
//                                    }.lparams {
//                                        width= matchParent
//                                        height=kpx.x(1)
//                                    }
//                                }
//                            }.view //注意最好手动配置item的宽度和高度。
//                        }
//                        //视图刷新[业务逻辑都在这处理]，返回 视图itemView和下标postion
//                        sp.onBindView { itemView, position ->
//                            itemView?.findViewById<KTextView>(kpx.id("pop_txt"))?.apply {
//                                setText("" + position)
//                                onClick {
//                                    sp?.dismiss()//关闭
//                                }
//                            }
//                        }
//                        //显示
//                        sp.showAsDropDown(this@button, 50, -20)

//                            fixme 以下是仿气泡冒出的样式。即顶部有一个小三角。
//                            //数据
//                            var list = mutableListOf<String>()
//                            list.add("1")//fixme 记得添加数据，不然不会显示哦。
//                            list.add("2")
//                            list.add("3")
//                            var sp = KSpinnerPop(ctx, list)
//                            //创建recyclerView内部item视图,参数为下标position
//                            sp?.onCreateView(popWidth = kpx.screenWidth()) {
//                                var positon = it
//                                context.UI {
//                                    verticalLayout {
//                                        backgroundColor = Color.WHITE
//                                        //要设置具体的宽度kpx.screenWidth();不要使用wrapContent和matchParent
//                                        var layoutParams = ViewGroup.LayoutParams(kpx.screenWidth(), kpx.x(100))
//                                        setLayoutParams(layoutParams)
//                                        gravity = Gravity.CENTER
//                                        KBaseUi.apply {
//                                            ktextView {
//                                                if (positon == 0) {
//                                                    radius {
//                                                        left_top = 45f
//                                                        right_top = left_top
//                                                    }
//                                                } else if (positon == list.lastIndex) {
//                                                    radius {
//                                                        left_bottom = 45f
//                                                        right_bottom = left_bottom
//                                                    }
//                                                }
//                                                id = kpx.id("pop_txt")
//                                                gravity = Gravity.CENTER
//                                                textSize = kpx.textSizeX(30f)
//                                                textColor = Color.BLACK
//                                                KSelectorUtils.selectorRippleDrawable(this, Color.parseColor("#EEEEEE"), Color.parseColor("#EFF3F6"))
//                                            }.lparams {
//                                                width = matchParent
//                                                height = kpx.x(99)
//                                            }
//                                        }
//                                        view {
//                                            backgroundColor = Color.LTGRAY//分界线
//                                        }.lparams {
//                                            width = matchParent
//                                            height = kpx.x(1)
//                                        }
//                                    }
//                                }.view //注意最好手动配置item的宽度和高度。
//                            }
//                            //fixme sp?.topView在sp?.onCreateView（）创建之后才会有。这里在顶部创建了一个小三角气泡。
//                            sp?.topView?.apply {
//                                var air = cn.oi.klittle.era.widget.compat.KAirView(context)
//                                addView(air, 0)
//                                air.apply {
//                                    var mHeight=kpx.x(32)
//                                    var layoutParams = RelativeLayout.LayoutParams(kpx.screenWidth(),mHeight )
//                                    setLayoutParams(layoutParams)
//                                    air {
//                                        direction = KAirEntry.DIRECTION_TOP//气泡方向在左边；默认是居中。
//                                        bg_color = Color.parseColor("#EEEEEE")
//                                        //bgHorizontalColors(Color.parseColor("#EEEEEE"), Color.WHITE, Color.parseColor("#EEEEEE"))
//                                        //isBgGradient = true//背景颜色渐变
//                                        strokeColor = Color.CYAN
//                                        //strokeHorizontalColors(Color.BLUE,Color.RED)
//                                        //isStrokeGradient=false//边框颜色不渐变
//                                        //strokeWidth = kpx.x(2f)
//                                        //fixme 注意；一定要先设置宽度和高度；然后再调用。
//                                        airWidth = mHeight//气泡三角的宽度
//                                        airHeight = airWidth
//                                        //xOffset = 0f//气泡三角，x轴偏移量，正数向右偏移，负数向左偏移
//                                        //yOffset = -kpx.x(0f)//气泡三角，y轴偏移量；正数向下偏移，负数向上偏移。
//                                        all_radius = 45f//圆角（所有的圆角；包括气泡）
//                                        //isAirRadius = false//气泡三角是否具备圆角,true圆角，false尖角。(all_radius大于0才有效)
//                                        //isAirBorderRadius = false//气泡三角的两个边的连接处是否具有圆角效果。,true圆角，false尖角。
//                                        dashWidth = kpx.x(0f)
//                                        dashGap = kpx.x(0f)
//                                        setAutoPaddingForAir(kpx.x(16), this)//fixme 设置文本内补丁。
//                                    }
//                                }
//                            }
//                            //视图刷新[业务逻辑都在这处理]，返回 视图itemView和下标postion
//                            sp.onBindView { itemView, position ->
//                                itemView?.findViewById<KTextView>(kpx.id("pop_txt"))?.apply {
//                                    setText("" + position)
//                                    onClick {
//                                        sp?.dismiss()//关闭
//                                    }
//                                }
//                            }
//                            //显示
//                            sp.showAsDropDown(this@button, 0, -kpx.x(10))

//fixme 以下视图，根据需求可随意扩展。上下左右，四个容器只要不为空，就都会显示出来。centerView在四个容器的中间。
//fixme 记得手动配置各个容器的宽和高。默认都是wrapContent，如果没有内容。就等于是空，是不会显示出来的。

//                            sp.topView?.apply {//上方容器}
//                            sp.leftView?.apply {//左边容器}
//                            sp.rightView?.apply {//右边容器}
//                            sp.bottomView?.apply {//底部容器}
//                            sp.centerView?.apply {//中间容器，recyclerView的外框容器 }
//                            sp.containerView?.apply { //最外层容器，囊括以上所有布局。}

//传入的list和原有list是同一个对象，已经绑定。只要不重新赋值=，就会一直相互影响。
//styleAnime动画文件
open class KSpinnerPop(var context: Context, var list: MutableList<*>, var styleAnime: Int = R.style.kera_window_alpha_scale_drop) {
    var pop: PopupWindow? = null
    var recyclerView: RecyclerView? = null

    //fixme 最外层容器。
    var containerView: RelativeLayout? = null

    //fixme 左边容器，recyclerView正左边
    var leftView: RelativeLayout? = null

    //fixme 上方容器，recyclerView正上面
    var topView: RelativeLayout? = null

    //fixme 中间容器，recyclerView的外框容器,即可以当作外层控件使用。随意更改样式
    //fixme 上下左右，四个容器只要不为空，就都会显示出来。centerView在四个容器的中间。
    var centerView: RelativeLayout? = null

    //fixme 下方容器，recyclerView正下面
    var bottomView: RelativeLayout? = null

    //fixme 左边容器，recyclerView正右边
    var rightView: RelativeLayout? = null

    /**
     * fixme 创建itemView视图 [需要自动手动实现]
     * @param popWidth 视图宽度;必须在一开始初始化的时候就设置，后面设置都无效。
     * @param popHeight 视图高度
     * @param isNeverScroll 设置RecyclerView滑动到边缘时无效果模式
     * @param itemView 视图
     */
    open fun onCreateView(popWidth: Int = ViewGroup.LayoutParams.MATCH_PARENT, popHeight: Int = ViewGroup.LayoutParams.WRAP_CONTENT, isNeverScroll: Boolean = true, itemView: (positon: Int) -> View) {
        var view = context.UI {
            verticalLayout {
                var layoutParams = ViewGroup.LayoutParams(wrapContent, matchParent)
                setLayoutParams(layoutParams)

                //外层容器
//                containerView = RelativeLayout(context).lparams {
//                    width = wrapContent
//                    height = wrapContent
//                }
//                addView(containerView)
                containerView = relativeLayout { }.lparams {
                    width = wrapContent
                    height = wrapContent
                }
                containerView?.apply {
                    relativeLayout {
                        var layoutParams = RelativeLayout.LayoutParams(wrapContent, wrapContent)
                        setLayoutParams(layoutParams)
                        onClick {
                            pop?.dismiss()//fixme 防止触摸屏幕外不消失弹窗。
                        }
                        //左边容器
//                        leftView = RelativeLayout(context)?.apply {
//                            id = kpx.id("spinnerPop_leftView")
//                        }?.lparams {
//                            width = wrapContent
//                            height = wrapContent
//                            alignParentLeft()
//                            bottomOf(kpx.id("spinnerPop_topView"))
//                        }
//                        addView(leftView)
                        leftView = relativeLayout {
                            id = kpx.id("spinnerPop_leftView")
                        }.lparams {
                            width = wrapContent
                            height = wrapContent
                            alignParentLeft()
                            bottomOf(kpx.id("spinnerPop_topView"))
                        }

                        //上方容器
//                        topView = RelativeLayout(context)?.apply {
//                            id = kpx.id("spinnerPop_topView")
//                        }?.lparams {
//                            width = wrapContent
//                            height = wrapContent
//                            alignParentTop()
//                            leftView?.let {
//                                rightOf(it)
//                            }
//                        }
//                        addView(topView)
                        topView = relativeLayout {
                            id = kpx.id("spinnerPop_topView")
                        }.lparams {
                            width = wrapContent
                            height = wrapContent
                            alignParentTop()
                            leftView?.let {
                                rightOf(it)
                            }
                        }

                        //中间容器，recyclerView容器
//                        centerView = RelativeLayout(context)
//                        addView(centerView)
                        centerView = relativeLayout { }
                        centerView?.apply {
                            id = kpx.id("spinnerPop_centerView")
                            frameLayout {
                                var layoutParams = ViewGroup.LayoutParams(wrapContent, wrapContent)
                                setLayoutParams(layoutParams)
                                KBaseUi.apply {
                                    recyclerView = krecyclerView {
                                        setLinearLayoutManager(true)
                                        adapter = MyAdapter(this@KSpinnerPop, itemView)

                                        if (isNeverScroll) {
                                            setOverScrollMode(OVER_SCROLL_NEVER);//设置滑动到边缘时无效果模式
                                            setVerticalScrollBarEnabled(false);//滚动条隐藏
                                        }
                                    }.lparams {
                                        width = wrapContent
                                        height = wrapContent
                                    }
                                }
                            }.lparams {
                                width = wrapContent
                                height = wrapContent
                            }
                        }?.lparams {
                            width = wrapContent
                            height = wrapContent
                            topView?.let {
                                bottomOf(it)
                            }
                            leftView?.let {
                                rightOf(it)
                            }
                            above(kpx.id("spinnerPop_bottomView"))
                        }

                        //下方容器
//                        bottomView = RelativeLayout(context)?.apply {
//                            id = kpx.id("spinnerPop_bottomView")
//                        }?.lparams {
//                            width = wrapContent
//                            height = wrapContent
//                            alignParentBottom()
//                            leftView?.let {
//                                rightOf(it)
//                            }
//                        }
//                        addView(bottomView)
                        bottomView = relativeLayout {
                            id = kpx.id("spinnerPop_bottomView")
                        }.lparams {
                            width = wrapContent
                            height = wrapContent
                            alignParentBottom()
                            leftView?.let {
                                rightOf(it)
                            }
                        }

                        //右边容器
//                        rightView = RelativeLayout(context)?.apply {
//                            id = kpx.id("spinnerPop_rightView")
//                        }?.lparams {
//                            width = wrapContent
//                            height = wrapContent
//                            centerView?.let {
//                                rightOf(it)
//                            }
//                            topView?.let {
//                                bottomOf(it)
//                            }
//                        }
//                        addView(rightView)
                        rightView = relativeLayout {
                            id = kpx.id("spinnerPop_rightView")
                        }.lparams {
                            width = wrapContent
                            height = wrapContent
                            centerView?.let {
                                rightOf(it)
                            }
                            topView?.let {
                                bottomOf(it)
                            }
                        }

                    }
                }
                //fixme 填充popuwindow的底部
                view {
                    onClick {
                        //关闭
                        dismiss()
                    }
                }.lparams {
                    width = matchParent
                    height = matchParent
                }
            }
        }.view
        pop = KPopuWindowUtils.getInstance().showPopuWindow(view, styleAnime, popWidth, popHeight)
    }

    var onBindView: ((itemView: View, position: Int) -> Unit)? = null

    //fixme 刷新视图，数据展示+业务逻辑+点击事件 都在这里处理 [需要自动手动实现]
    open fun onBindView(onBindView: (itemView: View, position: Int) -> Unit) {
        this.onBindView = onBindView
    }

    //fixme 关闭
    open fun dismiss() {
        pop?.dismiss()
        //pop?.setOnDismissListener {} 这个是关闭监听
    }

    //fixme 显示[每次显示的时候，布局都会重新刷新]
    open fun showAsDropDown(view: View?, xoff: Int = 0, yoff: Int = 0) {
        view?.let {
            pop?.showAsDropDown(it, xoff, yoff)//fixme xoff 和 yoff是偏移量。
            //数据刷新
            if (list.size > 1) {
                recyclerView?.adapter?.notifyItemRangeChanged(0, list.size)
            } else {
                recyclerView?.adapter?.notifyItemChanged(0)
            }
        }
    }

    companion object {
        class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {}
        class MyAdapter(var sp: KSpinnerPop, var itemView: (positon: Int) -> View) : RecyclerView.Adapter<MyViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                return MyViewHolder(itemView(viewType))//自定义View每次都是重新实例话出来的
            }

            override fun getItemCount(): Int {
                return sp.list.size
            }

            override fun getItemViewType(position: Int): Int {
                return position
            }

            override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
                sp.onBindView?.let {
                    it(holder.itemView, position)
                }
            }
        }
    }
}
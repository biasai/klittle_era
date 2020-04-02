package cn.oi.klittle.era.widget.viewpager

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.support.v4.view.ViewCompat
//import android.support.design.widget.TabLayout
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.widget.compat.K1Widget
import cn.oi.klittle.era.widget.compat.KView
import org.jetbrains.anko.runOnUiThread
//import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onClick
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.Deferred

//                        fixme 使用案例
//                        var tab = kTabLayout { }.apply {
//                            backgroundColor = Color.BLUE
//                        }.lparams {
//                            width = matchParent
//                            height = kpx.x(96)
//                        }
//                        var vp = viewPager {
//                            var datas = mutableListOf<String>()
//                            datas.add("tab1")
//                            datas.add("tab2")
//                            datas.add("tab3")
//                            datas.add("tab4")
//                            datas.add("tab5")
//                            datas.add("tab6")
//                            datas.add("tab7")
//                            datas.add("tab8")
//                            datas.add("tab9")
//                            adapter = KPagerAdapter<String>(datas)
//                        }.lparams {
//                            width = matchParent
//                            height = matchParent
//                        }
//                        //tab滑动条选中监听
//                        tab.setOnTabSelectedListener {
//                            KLoggerUtils.e("选中：\t" + it.position)
//                        }
//                        tab.setupWithViewPager(vp);//TabLayout和ViewPager绑定
//                        //fixme tab原有文本标题在viewpager适配器的getPageTitle()方法里。
//                        //fixme 以下添加自定义文本视图(会覆盖tab原有的文本)
//                        tab.getTabAll()?.forEach {
//                            //fixme tab的宽度是自适应的(根据TabLayout的总宽度和tab的个数自动合理分配宽度的。)
//                            //fixme tab选中时，会触发isSelected选中属性
//                            it.setCustomView(KTextView(ctx).apply {
//                                text = "Tab" + (it.position + 1)
//                                padding = kpx.x(12)
//                                leftPadding = kpx.x(50)
//                                rightPadding = leftPadding
//                                radius_selected {
//                                    all_radius(kpx.x(60))
//                                    bg_color = Color.RED
//                                }
//                                txt {
//                                    textColor = Color.GRAY
//                                    textSize = kpx.textSizeX(28)
//                                }
//                                txt_selected {
//                                    textColor = Color.WHITE
//                                }
//                            })
//                        }
//                        //setTabMode(android.support.design.widget.TabLayout.MODE_SCROLLABLE);//tab个数过多显示不全时，可以滑动显示。
//                        //tab.setTabMode(TabLayout.MODE_FIXED);//tab个数会全部显示出来。
//                        //setSelectedTabIndicatorColor(Color.WHITE);//tab滑动条的颜色(透明色Color.TRANSPARENT也有效)
//                        //tab.line_radius=kpx.x(15f)//fixme 横线圆角度数
//                        tab.line_offset = kpx.x(50f)//fixme 横线的缩进宽度(通过这个可以控件横线的宽度)
//                        tab.setOnClickTab()//设置Tab点击能够切换页面

/**
 * fixme 继承系统自动的滑动条（android.support.design.widget.TabLayout）；tab过多显示不全时，可以滑动；效果不错。
 * fixme 现在继承的是自己重写的TabLayout(作了改进)
 */
open class KTabLayout : TabLayout {

    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
        initCon()
    }

    constructor(viewGroup: ViewGroup, HARDWARE: Boolean) : super(viewGroup.context) {
        if (HARDWARE) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        viewGroup.addView(this)//直接添加进去,省去addView(view)
        initCon()
    }

    constructor(context: Context) : super(context) {
        initCon()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initCon()
    }

    //初始化
    @SuppressLint("WrongConstant")
    private fun initCon() {
        try{
            setTabMode(android.support.design.widget.TabLayout.MODE_SCROLLABLE);//tab个数过多显示不全时，可以滑动显示。
            //tab.setTabMode(TabLayout.MODE_FIXED);//tab个数会全部显示出来。
            setTabTextColors(Color.GRAY, Color.WHITE);//普通颜色和选中颜色
            setSelectedTabIndicatorColor(Color.WHITE);//tab滑动条的颜色(透明色Color.TRANSPARENT也有效)
            //tab.setSelectedTabIndicatorHeight(kpx.x(4))//tab底部滑动条的高度(一般不用设置，默认高度即可)(无法设置宽度，宽度是根据tab的宽度自适应的。)
            ViewCompat.setElevation(this, kpx.x(10f));
        }catch (e:java.lang.Exception){e.printStackTrace()}
    }

    var tabSelectedListener: ((tab: cn.oi.klittle.era.widget.viewpager.TabLayout.Tab) -> Unit)? = null

    private var mOnTabSelectedListener: cn.oi.klittle.era.widget.viewpager.TabLayout.OnTabSelectedListener? = null
    //fixme tab选中回调监听
    open fun setOnTabSelectedListener(tabSelectedListener: ((tab: cn.oi.klittle.era.widget.viewpager.TabLayout.Tab) -> Unit)) {
        this.tabSelectedListener = tabSelectedListener
        mOnTabSelectedListener = object : cn.oi.klittle.era.widget.viewpager.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: cn.oi.klittle.era.widget.viewpager.TabLayout.Tab?) {
                //KLoggerUtils.e("选中：\t"+tab?.position)
                tabSelectedListener?.let {
                    if (tab != null) {
                        it(tab)
                    }
                }
            }

            override fun onTabUnselected(tab: cn.oi.klittle.era.widget.viewpager.TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: cn.oi.klittle.era.widget.viewpager.TabLayout.Tab?) {
            }

        }
        setOnTabSelectedListener(mOnTabSelectedListener)
    }

    /**
     * fixme 获取全部的Tab菜单；调用前一定要先设置ViewPager；
     * 即先调用tab.setupWithViewPager(vp);//TabLayout和ViewPager绑定
     */
    open fun getTabAll(): MutableList<Tab>? {
        mViewPager?.let {
            it.adapter?.let {
                if (it.count > 0) {
                    var mutableList = mutableListOf<Tab>()
                    for (i in 0..it.count - 1) {
                        getTabAt(i)?.let {
                            mutableList.add(it)
                        }
                    }
                    return mutableList
                }
            }
        }
        return null
    }

    var cSelectPostion: Int = 0//记录当前选中的下标
    open fun setOnClickTab() {
        //fixme 修复点击时，tab菜单无法对齐问题；亲测有效。
        getTabAll()?.forEach {
            var position = it.position
            it.customView?.let {
                if (it is KView) {
                    it?.onSetSelected {
                        if (it) {
                            //fixme 选中监听，防止点击事件未触发。这就是滑动条对不齐的根本原因。
                            mViewPager?.let {
                                if (it.currentItem != position && cSelectPostion != position) {
                                    cSelectPostion = position
                                    isOnClickTab = true//防止重复切换，页签闪烁。
                                    mViewPager?.setCurrentItem(position, true)
                                }
                            }
                        }
                    }
                }
            }
            it.customView?.onClick {
                try {
                    mViewPager?.let {
                        it.adapter?.let {
                            if (it.count > position) {
                                mViewPager?.let {
                                    if (it.currentItem != position) {
                                        cSelectPostion = position//fixme 设置一下，很重要。保存一致。
                                        isOnClickTab = true//防止重复切换，页签闪烁。
                                        mViewPager?.setCurrentItem(position, true)
                                    }
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

    open fun onDestroy() {
        try {
            tabSelectedListener = null
            if (mOnTabSelectedListener != null) {
                removeOnTabSelectedListener(mOnTabSelectedListener!!)
            }
            mOnTabSelectedListener = null
            getTabAll()?.forEach {
                it?.customView?.let {
                    if (it is K1Widget) {
                        it.onDestroy()
                    } else if (it is ViewGroup) {
                        it.removeAllViews()
                    }
                }
                it.customView = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
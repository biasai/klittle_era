package cn.oi.klittle.era.widget.viewpager

import android.content.Context
import android.support.design.widget.TabLayout
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

//                        fixme 使用案例
//                        var tab = kTabLayout {  }.apply {
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
//                        tab.setTabMode(TabLayout.MODE_SCROLLABLE);//tab个数过多显示不全时，可以滑动显示。
//                        //tab.setTabMode(TabLayout.MODE_FIXED);//tab个数会全部显示出来。
//                        tab.setTabTextColors(Color.GRAY, Color.WHITE);//普通颜色和选中颜色
//                        tab.setSelectedTabIndicatorColor(Color.WHITE);//tab滑动条的颜色(透明色Color.TRANSPARENT也有效)
//                        //tab.setSelectedTabIndicatorHeight(kpx.x(4))//tab底部滑动条的高度(一般不用设置，默认高度即可)(无法设置宽度，宽度是根据tab的宽度自适应的。)
//                        //tab滑动条选中监听
//                        tab.setOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
//                            override fun onTabReselected(tab: TabLayout.Tab?) {
//
//                            }
//
//                            override fun onTabUnselected(tab: TabLayout.Tab?) {
//                            }
//
//                            override fun onTabSelected(tab: TabLayout.Tab?) {
//                                KLoggerUtils.e("选中：\t"+tab?.position)
//                            }
//                        })
//                        ViewCompat.setElevation(tab, 10f);
//                        tab.setupWithViewPager(vp);//TabLayout和ViewPager绑定
//                        //fixme tab原有文本标题在viewpager适配器的getPageTitle()方法里。
//                        for (i in 0..8) {
//                            //fixme 添加自定义文本视图(会覆盖tab原有的文本)
//                            //fixme tab的宽度是自适应的
//                            //fixme tab选中时，会触发isSelected选中属性
//                            tab.getTabAt(i)?.let {
//                                it.setCustomView(KTextView(ctx).apply {
//                                    text = "Tab" + (i+1)
//                                    padding = kpx.x(12)
//                                    radius_selected {
//                                        all_radius(kpx.x(60))
//                                        bg_color = Color.RED
//                                    }
//                                    txt {
//                                        textColor = Color.GRAY
//                                        textSize = kpx.textSizeX(28)
//                                    }
//                                    txt_selected {
//                                        textColor = Color.WHITE
//                                    }
//                                })
//                            }
//                        }

/**
 * fixme 继承系统自动的滑动条（android.support.design.widget.TabLayout）；tab过多显示不全时，可以滑动；效果不错。
 */
open class KTabLayout : TabLayout {

    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(viewGroup: ViewGroup, HARDWARE: Boolean) : super(viewGroup.context) {
        if (HARDWARE) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

}
package cn.oi.klittle.era.widget.viewpager.adapter

import android.graphics.Color
import android.support.v4.view.PagerAdapter
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.*

/**
 * ViewPager适配器 简单使用案例。
 */
open class KPagerAdapter<T>(var datas: MutableList<T>? = null) : PagerAdapter() {

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view === obj//只有返回true时。才会显示视图
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        //super.destroyItem(container, position, obj)
        container?.removeView(obj as View)
        //可以根据下标position在这里释放位图哦。
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var itemView = container.context.UI {
            verticalLayout {
                gravity = Gravity.CENTER
                //backgroundColor=Color.LTGRAY
                textView {
                    text = "" + position
                    gravity = Gravity.CENTER
                }.lparams {
                    width = wrapContent
                    height = wrapContent
                }
            }
        }.view
        container.addView(itemView)//fixme 注意，必不可少。不然显示不出来。这里是itemView，不是view哦。之前就写错了，死活不出来
        return itemView
    }

    var mCount = datas?.size ?: 0
    override fun getCount(): Int {
        //不要return datas?.size?:0 这样很容易异常。如果getCount()每次返回的值不一样。会很容易异常崩溃的。
        //所以为了安全，最好将getCount()一开始就初始化固定。
        return mCount
    }
}

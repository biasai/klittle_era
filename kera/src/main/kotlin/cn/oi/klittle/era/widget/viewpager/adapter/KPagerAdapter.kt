package cn.oi.klittle.era.widget.viewpager.adapter

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import cn.oi.klittle.era.comm.KToast
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KLoggerUtils
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * ViewPager适配器 简单使用案例。
 */
open class KPagerAdapter<T>() : PagerAdapter() {

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
                backgroundColor = Color.LTGRAY
                textView {
                    text = "" + position
                    textSize= kpx.textSizeX(36)
                    gravity = Gravity.CENTER
                    onClick {
                        KToast.show("" + position)
                    }
                }.lparams {
                    width = wrapContent
                    height = wrapContent
                }
            }
        }.view
        container.addView(itemView)//fixme 注意，必不可少。不然显示不出来。这里是itemView，不是view哦。之前就写错了，死活不出来
        return itemView
    }

    var datas: MutableList<T>? = null
        set(value) {
            field = value
            mCount = field?.size ?: 0
            notifyDataSetChanged()//fixme 数据更新时，一定要刷选一下，不然可能会异常哦。
        }
    var mCount = datas?.size ?: 0


    override fun getCount(): Int {
        //KLoggerUtils.e("mCount:\t" + mCount)
        //不要return datas?.size?:0 这样很容易异常。如果getCount()每次返回的值不一样。会很容易异常崩溃的。
        //所以为了安全，最好将getCount()一开始就初始化固定。
        return mCount
    }

    //标题
    override fun getPageTitle(position: Int): CharSequence? {
        datas?.let {
            if (it.size > position) {
                it[position]?.let {
                    if (it is String) {
                        return it
                    }
                }
            }
        }
        return super.getPageTitle(position)
    }

}

package cn.oi.klittle.era.widget.viewpager.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentStatePagerAdapter

//调用案例：
//var fragments: ArrayList<Fragment> = arrayListOf()
//fragments.add(HotFocusFragment())
//fragments.add(RealNewsFragment())
//viewpager?.adapter = KFragmentStatePagerAdapter(fragmentManager, fragments)

//fixme 刷新案例
//        if (myAdapter == null) {
//            myAdapter = MyFragmentPagerAdapter(supportFragmentManager, fragments)
//        }
//        myAdapter?.fragments = fragments
//        ui?.viewPager?.adapter = myAdapter
//        ui?.viewPager?.adapter?.notifyDataSetChanged()

/**
 * FragmentStatePagerAdapter 适配器 fixme 这个adapter带有销毁功能,Fragment个数改变时，会自动刷新。亲测。效果杠杠的。
 * fixme fragment每次切换时候，都会重新加载。即会重新执行 onCreateView()方法。
 */
open class KFragmentStatePagerAdapter(fm: FragmentManager?, var fragments: MutableList<Fragment>) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return fragments.get(position)//根据选中下标postion，显示当前的Fragment
    }

    //这里不用返回固定值，直接返回实际Fragment个数就行了。不会出错。
    override fun getCount(): Int {
        fragments?.let {
            return it.size
        }
        return 0
    }

    override fun getItemPosition(`object`: Any): Int {
        //return super.getItemPosition(`object`)
        //fixme 解决刷新无效问题，在getItemPostion的方法，返回POSTION_NONE就可以了
        return POSITION_NONE
    }
}

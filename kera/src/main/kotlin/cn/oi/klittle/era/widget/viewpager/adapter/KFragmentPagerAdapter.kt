package cn.oi.klittle.era.widget.viewpager.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


//调用案例：
//var fragments: ArrayList<Fragment> = arrayListOf()
//fragments.add(HotFocusFragment())
//fragments.add(RealNewsFragment())
//viewpager?.adapter = KFragmentPagerAdapter(fragmentManager, fragments)

//fixme Activity获取FragmentManager
//supportFragmentManager 这个是 android.support.v4.app.FragmentManager
//fragmentManager 是android.app.FragmentManager!

/**
 * FragmentPagerAdapter 适配器
 */
open class KFragmentPagerAdapter(fm: FragmentManager?, var fragments: MutableList<Fragment>) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return fragments.get(position)//根据选中下标postion，显示当前的Fragment
    }

    var mCount = fragments?.size ?: 0
    override fun getCount(): Int {
        //不要return datas?.size?:0 这样很容易异常。如果getCount()每次返回的值不一样。会很容易异常崩溃的。
        //所以为了安全，最好将getCount()一开始就初始化固定。
        return mCount
    }

    //获取标题
    override fun getPageTitle(position: Int): CharSequence? {
        return super.getPageTitle(position)
    }

}

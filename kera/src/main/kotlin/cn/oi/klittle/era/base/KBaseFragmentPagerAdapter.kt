package cn.oi.klittle.era.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


open class KBaseFragmentPagerAdapter(fm: FragmentManager, open var fragments: List<Fragment>) : FragmentPagerAdapter(fm) {
    open override fun getItem(position: Int): Fragment {
        return fragments.get(position)//根据选中下标postion，显示当前的Fragment
    }

    open override fun getCount(): Int {
        return fragments.size//显示的页面个数
    }
}
package com.liangyue.strategy.moudle.base

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

open class KBaseFragmentPagerAdapter(fm: FragmentManager?, open var fragments: List<Fragment>) : FragmentPagerAdapter(fm) {
    open override fun getItem(position: Int): Fragment {
        return fragments.get(position)//根据选中下标postion，显示当前的Fragment
    }

    open override fun getCount(): Int {
        return fragments.size//显示的页面个数
    }
}
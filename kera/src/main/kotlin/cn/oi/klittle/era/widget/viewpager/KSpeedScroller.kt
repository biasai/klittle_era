package cn.oi.klittle.era.widget.viewpager

import android.content.Context
import android.support.v4.view.ViewPager
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.Scroller
import java.lang.reflect.Field

/**
 * 控制ViewPager滑动速度
 */
class KSpeedScroller : Scroller {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, interpolator: Interpolator) : super(context, interpolator) {}

    var mDuration = 400//滑动时间，单位毫秒
    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
        super.startScroll(startX, startY, dx, dy, mDuration)
    }


    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int) {
        super.startScroll(startX, startY, dx, dy, mDuration)
    }

    companion object {
        /**
         * 设置ViewPager的滑动时间，mDuration滑动时间，单位毫秒
         * @param viewPager 传ViewPager fixme  必须传具体的类型，反射只能获取当前类属性，所以必须指定具体的类型。
         */
        fun setViewPagerSpeed(viewPager: ViewPager, mDuration: Int = 400) {
            try {
                var field: Field? = null
                var clazz: Class<*> = viewPager.javaClass
                while (clazz != Any::class.java && field == null) {
                    field = getScrollerField(clazz)
                    if (field != null) {
                        break
                    } else {
                        clazz = clazz.superclass//子类属性没找到，就查找父类属性。
                    }
                }
                field?.let {
                    it.isAccessible = true
                    //LinearInterpolator 匀速；AccelerateInterpolator加速;DecelerateInterpolator减速
                    var speedScroller = KSpeedScroller(viewPager.context, LinearInterpolator())
                    speedScroller.mDuration = mDuration
                    it.set(viewPager, speedScroller)
                }
            } catch (e: Exception) {
                //Log.e("test", "viewpager滑动设置异常1:\t" + e.message)
            }
        }

        /**
         * 设置ViewPager的滑动时间，mDuration滑动时间，单位毫秒
         * @param viewPager 传VerticalViewPager fixme  子所以写两个方法，是因为，反射只能获取当前类属性，所以必须指定具体的类型。
         */
        fun setViewPagerSpeed(viewPager: VerticalViewPager, mDuration: Int = 400) {
            try {
                var field: Field? = null
                var clazz: Class<*> = viewPager.javaClass
                while (clazz != Any::class.java && field == null) {
                    field = getScrollerField(clazz)
                    if (field != null) {
                        break
                    } else {
                        clazz = clazz.superclass//子类属性没找到，就查找父类属性。
                    }
                }
                field?.let {
                    it.isAccessible = true
                    //LinearInterpolator 匀速；AccelerateInterpolator加速;DecelerateInterpolator减速
                    var speedScroller = KSpeedScroller(viewPager.context, LinearInterpolator())
                    speedScroller.mDuration = mDuration
                    it.set(viewPager, speedScroller)
                }
            } catch (e: Exception) {
                //Log.e("test", "viewpager滑动设置异常1:\t" + e.message)
            }
        }

        //放射只能获取当前Class类型的属性，无法获取父类。
        private fun getScrollerField(clazz: Class<*>): Field? {
            if (clazz != Any::class.java) {
                try {
                    var field = clazz.getDeclaredField("mScroller")
                    field.isAccessible = true
                    return field
                } catch (e: Exception) {
                    //Log.e("test", "viewpager滑动设置异常2:\t" + e.message)
                }
            }
            return null
        }
    }

}
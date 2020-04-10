package cn.oi.klittle.era.widget.viewpager

import android.content.Context
import androidx.core.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager

/**
 * 继承垂直VerticalViewPager；用法和 KViewPager一样。代码是完全复制过来的。
 *
 * 手指不可以滑动，但是可以代码调用setCurrentItem
 * setCurrentItem(0,true)//选中第一个。参数二表示是否具备滑动效果。默认就是true.
 * fixme isScroll 是否滑动，默认禁止滑动，isFastScroll 快速滑动，禁止快速滑动。
 *
 */
open class KVerticalViewPager : VerticalViewPager {

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

    var isScroll: Boolean = true//fixme true 能滑动，false不能滑动。默认能触摸滑动
    var isFastScroll: Boolean = false//fixme 是否可以快速滑动。
        //true快速滑动[也会禁止掉触摸滑动]，手指轻轻一划。就到下一页。false不能快速滑动
        set(value) {
            if (value) {
                duration = duration//如果支持快速滑动，就自动设置滑动时间。
            }
            field = value
        }

    var simpleOnGestureListener: GestureDetector.SimpleOnGestureListener? = null
    var gestureDetectorCompat: GestureDetectorCompat? = null

    var state = ViewPager.SCROLL_STATE_IDLE
    var pageListener = object : OnPageChangeListener {
        override fun onPageScrollStateChanged(mState: Int) {
            state = mState
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

        override fun onPageSelected(position: Int) {}

    }

    init {
        simpleOnGestureListener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                if (velocityX > 5) {
                    if (currentItem > 0) {
                        //上一页
                        if (state == ViewPager.SCROLL_STATE_IDLE) {
                            setCurrentItem(currentItem - 1, true)
                        }
                    }
                } else if (velocityX < -5) {
                    adapter?.let {
                        if (currentItem < it.count - 1) {
                            //下一页
                            if (state == ViewPager.SCROLL_STATE_IDLE) {
                                setCurrentItem(currentItem + 1, true)
                            }
                        }
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        }
        gestureDetectorCompat = GestureDetectorCompat(context, simpleOnGestureListener)
        setOnTouchListener { v, event ->
            var b = false
            if (isFastScroll) {
                gestureDetectorCompat?.onTouchEvent(event)//快速滑动[也会禁止滑动]。
                b = true
            } else if (!isScroll) {
                b = true//禁止滑动
            }
            b//false 正常，可以滑动。
        }
        addOnPageChangeListener(pageListener)//fixme addOnPageChangeListener 添加多个滑动监听，不会冲突。
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // TODO Auto-generated method stub
        if (isFastScroll) {
            if (ev.action == MotionEvent.ACTION_DOWN) {
                if (state != ViewPager.SCROLL_STATE_IDLE) {
                    return true//正在滑动的时候，事件禁止
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }


    var duration = 400
        //滑动时间，单位毫秒
        set(value) {
            field = value
            //设置滑动时间，必须要手动设置一遍才有效。
            KSpeedScroller.setViewPagerSpeed(this, value)
        }

}

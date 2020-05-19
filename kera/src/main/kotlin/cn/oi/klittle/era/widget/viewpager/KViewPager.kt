package cn.oi.klittle.era.widget.viewpager

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GestureDetectorCompat
import androidx.viewpager.widget.ViewPager

/**
 * 禁止滑动的ViewPager,也可以继承VerticalViewPager
 * 手指不可以滑动，但是可以代码调用setCurrentItem
 *
 * setCurrentItem(0,true)//选中第一个。参数二表示是否具备滑动效果。默认就是true
 * setPageTransformer(true, KStackTransformer())//fixme 多页滑动覆盖效果；在animationlibrary包下，还有很多效果。
 *
 * fixme isScroll 是否滑动，默认禁止滑动，isFastScroll 快速滑动，禁止快速滑动。
 */
open class KViewPager : ViewPager {

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

    var isScrollEnable: Boolean = true//fixme true 能滑动，false不能滑动。默认能滑动
    var isFastScrollEnable: Boolean = false//fixme 是否可以快速滑动，默认不能。(true 手指快速滑动时，会立即翻页。)
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
            if (isFastScrollEnable) {
                gestureDetectorCompat?.onTouchEvent(event)//快速滑动[也会禁止滑动]。
                b = true
            } else if (!isScrollEnable) {
                b = true//禁止滑动
            }
            b//false 正常，可以滑动。
        }
        addOnPageChangeListener(pageListener)//fixme addOnPageChangeListener 添加多个滑动监听，不会冲突。
    }

    companion object {
        var isViewPagerMotionEventing: Boolean = false//fixme 防止和左滑关闭Activity冲突。
        var currentIteming: Int = 0
        var counting = 0
        var isScrolling = true//true 能滑动，false不能滑动。默认能触摸滑动
        var isFastScrolling=false//
        /**
         * fixme 判断viewpager是否正在滑动。
         */
        fun isMotinEventing(): Boolean {
            if (isViewPagerMotionEventing&&(isScrolling||isFastScrolling)) {
                if (currentIteming > 0 && counting > 0) {
                    return true
                }
            }
            return false
        }
    }


    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        currentItem?.let {
            KViewPager.currentIteming = currentItem//当前选择item下标
        }
        childCount?.let {
            KViewPager.counting = childCount//item总个数。
        }
        isScrollEnable?.let {
            isScrolling = isScrollEnable//能否滑动
        }
        isFastScrollEnable?.let {
            isFastScrolling=isFastScrollEnable//能够快速滑动
        }
        //fixme 使用Raw
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                isViewPagerMotionEventing = true//fixme viewpager正在触摸
            }
            MotionEvent.ACTION_UP -> {
                isViewPagerMotionEventing = false
            }
        }

        // TODO Auto-generated method stub
        if (isFastScrollEnable) {
            if (ev.action == MotionEvent.ACTION_DOWN) {
                if (state != ViewPager.SCROLL_STATE_IDLE) {
                    return true//正在滑动的时候，事件禁止
                }
            }
        }
        try {
            if (ev != null) {
                return super.dispatchTouchEvent(ev)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }


    var duration = 400
        //fixme 滑动时间，单位毫秒;亲测有效，androidx也有效。
        set(value) {
            field = value
            //设置滑动时间，必须要手动设置一遍才有效。
            KSpeedScroller.setViewPagerSpeed(this, value)
        }


}

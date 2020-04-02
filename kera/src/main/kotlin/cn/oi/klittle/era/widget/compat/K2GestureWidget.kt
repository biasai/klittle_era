package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
//import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.lang.Exception

//                fixme kotlin 原始事件
//                //点击事件
//                onClick {
//                }
//                //长按事件
//                onLongClick {
//                }
//                //触摸事件
//                onTouch { v, event ->
//                }

//                fixme 手势使用案例，和上面的onClick，onLongClick事件不会冲突，都会触发。互不影响。相互独立。
//                //一：按下
//                onDown {
//                    KLoggerUtils.e("按下")
//                }
//                //二：按下之后，极短时间内手指没有移动
//                onShowPress {
//                    KLoggerUtils.e("按下之后，极短时间内手指没有移动")
//                }
//                //三：手指单击离开
//                onSingleTapUp {
//                    KLoggerUtils.e("手指单击离开")
//                }
//                //四：单击事件确认
//                onSingleTapConfirmed {
//                    KLoggerUtils.e("单击事件确认")
//                }
//
//                //五：长按事件
//                onLongPress {
//                    KLoggerUtils.e("长按事件")
//                }
//
//                //六：滑动事件（左上角滑动为正数）
//                //一般执行了onScroll(),最后手指离开时都会执行onFling()
//                onScroll { e1, e2, distanceX, distanceY ->
//                    KLoggerUtils.e("滑动事件\tdistanceX:\t"+distanceX+"\tdistanceY:\t"+distanceY)
//                }
//
//                //七：快速滑动，fixme 和onScroll()相反，向右下角滑动为正数。
//                onFling { e1, e2, velocityX, velocityY ->
//                    KLoggerUtils.e("快速滑动\tvelocityX:\t"+velocityX+"\tvelocityY:\t"+velocityY)
//                }
//
//                //八：双击事件
//                onDoubleTap {
//                    KLoggerUtils.e("双击事件")
//                }
//
//                //九：onDoubleTap()双击之后立即调用，最后双击手指离开的时候，也会触发。(即第二次按下和离开之间发生的动作。)
//                onDoubleTapEvent {
//                    KLoggerUtils.e("第二次按下和离开之间发生的动作")
//                }


/**
 * 二：手势
 */
open class K2GestureWidget : K2AnimeWidget {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    private var simpleOnGestureListener: GestureDetector.SimpleOnGestureListener? = null
    private var gestureDetectorCompat: GestureDetectorCompat? = null
    //启用手势
    private fun enableGesture() {
        if (simpleOnGestureListener == null || gestureDetectorCompat == null) {
            simpleOnGestureListener = object : GestureDetector.SimpleOnGestureListener() {
                //fixme 一：
                //手指按下，无论是那个手势，这个方法肯定会执行，而且是最先执行。
                override fun onDown(e: MotionEvent?): Boolean {
                    e?.let {
                        onDown?.apply {
                            this(it)
                        }
                    }
                    return super.onDown(e)
                }

                //fixme 二：
                //手指按下之后，极短的时间内，手指没有移动。就会触发。
                //手指不动，再接着按下去，就会触发onLongPress()长按事件。
                //手指移动了，就会触发onScroll()滑动事件
                override fun onShowPress(e: MotionEvent?) {
                    e?.let {
                        onShowPress?.apply {
                            this(it)
                        }
                    }
                    super.onShowPress(e)
                }

                //fixme 三：
                //手指离开的时候（单击）。后面不会再执行其他方法。(终结方法，只执行一次。)
                override fun onSingleTapUp(e: MotionEvent?): Boolean {
                    e?.let {
                        onSingleTapUp?.apply {
                            this(it)
                        }
                    }
                    return super.onSingleTapUp(e)
                }

                //fixme 四：
                //单击事件确认，onSingleTapUp()之后，系统等待一段时间后没有收到第二次点击事件则判定为单击
                override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                    e?.let {
                        onSingleTapConfirmed?.apply {
                            this(it)
                        }
                    }
                    return super.onSingleTapConfirmed(e)
                }

                //fixme 五：
                //长按事件，后面不会再执行其他的方法。(终结方法，只执行一次。)
                override fun onLongPress(e: MotionEvent?) {
                    super.onLongPress(e)
                    e?.let {
                        onLongPress?.apply {
                            this(it)
                        }
                    }
                }

                //fixme 六：
                //滑动事件，手指在屏幕上移动。一般执行了onScroll(),最后手指离开时都会执行onFling()，但是如果手指最后离开的时候。手指没有移动。则不会调用onFling()方法。概率比较小。
                //onScroll()一旦触发，并且手指在移动，那就会不停的触发，手指没有移动或停下了，也不会触发。
                override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                    //fixme 向左上角滑动为正数
                    //distanceX x轴滑动的距离。第一次是(e1-e2),其后就是 (前一个e2 - 后一个e2),由于是前一个减后一个，负数x轴向右，正数x轴向左滑动。
                    //distanceY y轴的滑动距离。第一次是(e1-e2),其后是  （前一个e2 - 后一个e2）,由于是前一个减后一个，负数y轴向下，正数y轴向上滑动。

                    if (e1 != null && e2 != null && distanceX != null && distanceY != null) {
                        onScroll?.let {
                            it(e1, e2, distanceX, distanceY)
                        }
                    }

                    return super.onScroll(e1, e2, distanceX, distanceY)
                }

                //fixme 七：
                //快速滑动事件
                //执行onFling()方法，前面一定先执行了onScroll()方法。后面不会再执行其他方法。(终结方法,只执行一次。)
                override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                    //fixme 和onScroll()相反；向右下角滑动为正数。
                    if (e1 != null && e2 != null && velocityX != null && velocityY != null) {
                        onFling?.let {
                            it(e1, e2, velocityX, velocityY)
                        }
                    }
                    return super.onFling(e1, e2, velocityX, velocityY)
                }

                //fixme 八：
                //双击事件,手指第二次快速按下的时候触发。不是离开的时候触发。
                override fun onDoubleTap(e: MotionEvent?): Boolean {
                    e?.let {
                        onDoubleTap?.apply {
                            this(it)
                        }
                    }
                    return super.onDoubleTap(e)
                }

                //fixme 九：
                //onDoubleTap()双击之后立即调用，最后双击手指离开的时候，也会触发。(即第二次按下和离开之间发生的动作。)
                override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
                    e?.let {
                        onDoubleTapEvent?.apply {
                            this(it)
                        }
                    }
                    return super.onDoubleTapEvent(e)
                }

            }
            gestureDetectorCompat = GestureDetectorCompat(context, simpleOnGestureListener)
//            onTouch { v, event ->
//                try {
//                    gestureDetectorCompat?.onTouchEvent(event)//fixme 添加手势
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
            setOnTouchListener { v, event ->
                try {
                    gestureDetectorCompat?.onTouchEvent(event)//fixme 添加手势
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                true
            }
        }

    }

    //一：手指按下，无论是那个手势，这个方法肯定会执行，而且是最先执行。
    private var onDown: ((e: MotionEvent) -> Unit)? = null

    fun onDown(onDown: ((e: MotionEvent) -> Unit)? = null) {
        this.onDown = onDown
        enableGesture()
    }

    //二：手指按下之后，极短的时间内，手指没有移动。就会触发。
    private var onShowPress: ((e: MotionEvent) -> Unit)? = null

    fun onShowPress(onShowPress: ((e: MotionEvent) -> Unit)? = null) {
        this.onShowPress = onShowPress
        enableGesture()
    }

    //三：手指离开的时候（单击）。后面不会再执行其他方法。(终结方法，只执行一次。)
    private var onSingleTapUp: ((e: MotionEvent) -> Unit)? = null

    fun onSingleTapUp(onSingleTapUp: ((e: MotionEvent) -> Unit)? = null) {
        this.onSingleTapUp = onSingleTapUp
        enableGesture()
    }

    //四：单击事件确认，onSingleTapUp()之后，系统等待一段时间后没有收到第二次点击事件则判定为单击
    private var onSingleTapConfirmed: ((e: MotionEvent) -> Unit)? = null

    fun onSingleTapConfirmed(onSingleTapConfirmed: ((e: MotionEvent) -> Unit)? = null) {
        this.onSingleTapConfirmed = onSingleTapConfirmed
        enableGesture()
    }

    //五：长按事件，后面不会再执行其他的方法。(终结方法，只执行一次。)
    private var onLongPress: ((e: MotionEvent) -> Unit)? = null

    fun onLongPress(onLongPress: ((e: MotionEvent) -> Unit)? = null) {
        this.onLongPress = onLongPress
        enableGesture()
    }

    //六：滑动事件; 向左上角滑动为正数(distanceX和distanceY)
    //一般执行了onScroll(),最后手指离开时都会执行onFling()
    private var onScroll: ((e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float) -> Unit)? = null

    fun onScroll(onScroll: ((e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float) -> Unit)? = null) {
        this.onScroll = onScroll
        enableGesture()
    }

    //七：快速滑动事件; fixme 和onScroll()相反；向右下角滑动为正数。(velocityX和velocityY)
    private var onFling: ((e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float) -> Unit)? = null

    fun onFling(onFling: ((e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float) -> Unit)? = null) {
        this.onFling = onFling
        enableGesture()
    }

    //八：双击事件,手指第二次快速按下的时候触发。不是离开的时候触发。
    private var onDoubleTap: ((e: MotionEvent) -> Unit)? = null

    fun onDoubleTap(onDoubleTap: ((e: MotionEvent) -> Unit)? = null) {
        this.onDoubleTap = onDoubleTap
        enableGesture()
    }

    //九：onDoubleTap()双击之后立即调用，最后双击手指离开的时候，也会触发。(即第二次按下和离开之间发生的动作。)
    private var onDoubleTapEvent: ((e: MotionEvent) -> Unit)? = null

    fun onDoubleTapEvent(onDoubleTapEvent: ((e: MotionEvent) -> Unit)? = null) {
        this.onDoubleTapEvent = onDoubleTapEvent
        enableGesture()
    }

    override fun onDestroy() {
        super.onDestroy()
        onDown = null
        onShowPress = null
        onSingleTapUp = null
        onSingleTapConfirmed = null
        onLongPress = null
        onScroll = null
        onFling = null
        onDoubleTap = null
        onDoubleTapEvent = null

        simpleOnGestureListener = null
        gestureDetectorCompat = null
    }

}
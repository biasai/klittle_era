package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.entity.widget.compat.KDragEntity
import cn.oi.klittle.era.utils.KCacheUtils
import cn.oi.klittle.era.utils.KLoggerUtils
import org.jetbrains.anko.runOnUiThread
import org.json.JSONObject
import java.lang.Exception
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.Deferred

//            fixme 调用案例
//            relativeLayout {
//                backgroundColor = Color.GREEN
//                KView(this).apply {
//                    autoBg {
//                        url = "http://test.app.bwg2017.com//photo/201905/10863/20190528111904_0.JPEG"
//                    }
//                    radius {
//                        all_radius(kpx.x(200f))
//                        strokeColor = Color.BLACK
//                        strokeWidth = kpx.x(3f)
//                    }
//                    dragUp { centerX, centerY, left, top, right, bottom ->
//                        //KLoggerUtils.e("手指离开：\tcenterX:\t"+centerX+"\tcenterY:\t"+centerY+"\tleft:\t"+left+"\ttop:\t"+top+"\tright:\t"+right+"\tbottom:\t"+bottom)
//                    }
//                }.lparams {
//                    width = kpx.x(200)
//                    height = width
//                    leftMargin = 100
//                    topMargin = leftMargin
//                    centerInParent()
//                }.apply {
//                    //fixme 防止初始化的时候layoutParams为空，而无法记录之前保存的位置。所以要在.lparams之后设置setDragId()。
//                    //fixme parentHeight传入父容器的高；父容器必须是相对布局
//                    drag(parentHeight = kpx.maxScreenHeight()) {
//                        isDragEnable = true//是否拖动
//                        top_gap = 0//上边的边界值；负数会超过边界。
//                        left_gap = -100
//                        right_gap = -100
//                        top_gap = -100
//                        bottom_gap = -100
//                        isAbs = true//手指离开后，是否吸附到边缘
//                        isAbsLeft = true//是否吸附到左边
//                        isAbsRight = true//是否吸附到右边
//                        isAbsTop = false
//                        isAbsBottom = false
//                        setDragId("test_id")//fixme 在.lparams之后设置setDragId();防止layoutParams为空。
//                    }
//                }

//                    fixme 获取ContentView的高度。即Activity视图的高度。
//                    contentViewHeight {
//                        drag(parentHeight = it) {
//                            isAbs = true
//                            setDragId("test")
//                        }
//                    }

//            }.lparams {
//                width = matchParent
//                height = 1000
//            }

//fixme  setDragId(dragId: String?, isSaveDrag: Boolean = true) 可以保存拖动后的位置状态。

/**
 * fixme 拖动控件
 */
open class K3DragMotionEventWidget : K3CTouchScaleMotionEventWidget {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//开启硬件加速,不然圆角没有效果
        clearBackground()
        //去除按钮原有阴影
        clearButonShadow()
    }

    companion object {
        var isDragMotionEventing: Boolean = false//fixme 防止和左滑关闭Activity冲突。
        /**
         * fixme 判断viewpager是否正在滑动。
         */
        fun isDrgMotinEventing(): Boolean {
            return isDragMotionEventing
        }
    }

    private var dragId: String? = null//fixme 拖动id,唯一标志；记录和保存该控件的实时位置。
    fun getDragId(): String? {
        dragId?.let {
            if (it.trim().length > 0) {
                return "KDragId:" + it.trim()
            }
        }
        return null
    }

    var isSaveDrag = true//是否保存拖动状态

    /**
     * fixme 设置拖动id;可以实时记录和保存控件的位置
     */
    fun setDragId(dragId: String?, isSaveDrag: Boolean = true) {
        this.dragId = dragId
        this.isSaveDrag = isSaveDrag
        initDrag()
    }

    //保存当前拖动状态（手机滑动离开的时候，会调用）
    fun saveDrag() {
        dragId?.let {
            if (it.trim().length > 0 && isSaveDrag) {
                GlobalScope.async {
                    try {
                        layoutParams?.let {
                            if (it is RelativeLayout.LayoutParams) {
                                var dragJson = JSONObject()
                                dragJson.put("leftMargin", it.leftMargin)
                                dragJson.put("topMargin", it.topMargin)
                                dragJson.put("mCanvasRotation", mCanvasRotation)//fixme 保存旋转角度
                                KCacheUtils.putSecret(getDragId(), dragJson.toString())
                            } else if (it is WindowManager.LayoutParams) {
                                var dragJson = JSONObject()
                                dragJson.put("leftMargin", it.x)
                                dragJson.put("topMargin", it.y)
                                dragJson.put("mCanvasRotation", mCanvasRotation)//fixme 保存旋转角度
                                KCacheUtils.putSecret(getDragId(), dragJson.toString())
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    //恢复上传拖动保存的状态
    fun initDrag() {
        dragId?.let {
            if (it.trim().length > 0 && isSaveDrag) {
                //fixme 一定要在主线程中进行。不然没有效果；操作layoutParams一定要在主线程中操作，不然很容易出很多意想不到的的问题。
                getContext()?.runOnUiThread {
                    try {
                        layoutParams?.let {
                            if (it is RelativeLayout.LayoutParams || it is WindowManager.LayoutParams) {
                                //fixme 防止初始化的时候layoutParams为空，而无法记录之前保存的位置。所以要在.lparams之后设置setDragId()。
                                var str = KCacheUtils.getSecret(getDragId())
                                if (str != null && str.toString().trim().length > 0) {
                                    var json = JSONObject(str.toString())
                                    var leftMargin = json.getString("leftMargin").toInt()
                                    var topMargin = json.getString("topMargin").toInt()
                                    //读取旋转角度
                                    if (json.has("mCanvasRotation")) {
                                        json.getString("mCanvasRotation")?.let {
                                            mCanvasRotation = it.toFloat()
                                            postInvalidate()
                                        }
                                    }
                                    //margin()在主线程和非主线程中，都有效。亲测！最好还是再主线程中调用，以防万一。
                                    //KLoggerUtils.e("leftMargin:\t"+leftMargin+"\ttopMargin:\t"+topMargin)
                                    margin(leftMargin = leftMargin, topMargin = topMargin)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private var drag: KDragEntity? = null
    private fun getDrag(): KDragEntity {
        if (drag == null) {
            drag = KDragEntity()
        }
        return drag!!
    }

    private var parentWidth: Int = kpx.screenWidth()
    private var parentHeight: Int = kpx.screenHeight()
    /**
     * fixme 由于view没有获取父容器的方法；所以，必须手动传入父容器的宽度和高度。
     * fixme 父容器必须是相对布局(RelativeLayout)才有效。
     * @param parentHeight 父容器高度 fixme 最好手动传一下。为了防止忘记就不设置默认值。
     * @param parentWidth 父容器宽度 一般都是屏幕的宽度。默认参数顺序一般放在后面。
     */
    fun drag(parentHeight: Int, parentWidth: Int = this.parentWidth, block: KDragEntity.() -> Unit): K3DragMotionEventWidget {
        this.parentWidth = parentWidth
        this.parentHeight = parentHeight
        block(getDrag())
        return this
    }

    var dragUp: ((centerX: Int, centerY: Int, left: Int, top: Int, right: Int, bottom: Int) -> Unit)? = null
    /**
     * 手指离开的时候回调；fixme 返回控件在父容器中的中心坐标，及左上右下的位置
     */
    fun dragUp(dragUp: ((centerX: Int, centerY: Int, left: Int, top: Int, right: Int, bottom: Int) -> Unit)? = null) {
        this.dragUp = dragUp
    }


//                    fixme 相对于父容器坐标
//                    event.x
//                    event.y
//                    fixme 相对于整个屏幕的坐标（是整个屏幕）
//                    event.rawX
//                    event.rawY

    private var isDrag: Boolean = false//是否拖动
    var kkmmmLeftMargin: Int = 0
        //属性动画不能私有
        set(value) {
            field = value
            if (!isDrag) {//fixme 非拖动状态才有效。
                margin(leftMargin = value)
            }
        }

    fun ofIntLeftMargin(startMargin: Int, endMargin: Int, duration: Long) {
        if (startMargin != endMargin) {
            ofInt("kkmmmLeftMargin", 0, duration, startMargin, endMargin) {
                if (it == endMargin) {
                    //fixme 动画结束，保存属性
                    saveDrag()
                }
            }
        }
    }

    var kkmmmTopMargin: Int = 0
        set(value) {
            field = value
            if (!isDrag) {
                margin(topMargin = value)
            }
        }

    fun ofIntTopMargin(startMargin: Int, endMargin: Int, duration: Long) {
        if (startMargin != endMargin) {
            ofInt("kkmmmTopMargin", 0, duration, startMargin, endMargin) {
                if (it == endMargin) {
                    //fixme 动画结束，保存属性
                    saveDrag()
                }
            }
        }
    }

    var kkmmmRightMargin: Int = 0
        set(value) {
            field = value
            if (!isDrag) {
                margin(rightMargin = value)
            }
        }

    fun ofIntRightMargin(startMargin: Int, endMargin: Int, duration: Long) {
        ofInt("kkmmmRightMargin", 0, duration, startMargin, endMargin)
    }

    var kkmmmBottomMargin: Int = 0
        set(value) {
            field = value
            if (!isDrag) {
                margin(bottomMargin = value)
            }
        }

    fun ofIntBootomMargin(startMargin: Int, endMargin: Int, duration: Long) {
        ofInt("kkmmmBottomMargin", 0, duration, startMargin, endMargin)
    }

    private var ww = right - left
    private var hh = bottom - top


    //偏移量
    private var xOffset = 0
    private var yOffset = 0
    private var b = false
    private var isdragAble = true
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        b = super.dispatchTouchEvent(event)
        event?.let {
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    //第一根手指按下
                    isdragAble = true//具备拖动能力
                    isDragMotionEventing = false
                    drag?.let {
                        if (it.isDragEnable) {
                            isDragMotionEventing = true//fixme 正在拖动
                        }
                    }
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    //多个手指按下
                    isdragAble = false//不具备拖动能力
                    drag?.let {
                        if (it.isDragEnable) {
                            isDragMotionEventing = true//fixme 正在拖动
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    //最后手指离开的时候，要保存位置信息，所以要为true
                    isdragAble = true
                    isDragMotionEventing = false
                }
            }
            //fixme 单指拖动。（双指要缩放和旋转；多指移动也不好控制很容易错乱。单指比较靠谱。）
            if (event.pointerCount == 1 && isdragAble) {
                drag?.let {
                    //fixme isTouchScale判断控件是否在缩放。不能同时进行
                    if (it.isDragEnable && !isTouchScale) {
                        ww = right - left
                        hh = bottom - top
                        //开启拖动功能
                        when (event.action and MotionEvent.ACTION_MASK) {
                            MotionEvent.ACTION_DOWN -> {
                                isDrag = true
                                //手指按下（第一个手指）
                                layoutParams?.let {
                                    if (it is RelativeLayout.LayoutParams) {
                                        //fixme 使用event.x会发生闪烁，效果不好。event.rawX效果比较好。
                                        xOffset = (event.rawX - left).toInt()//fixme: left和top可以获取在父容器中的位置;对应layout(int l, int t, int r, int b) 方法
                                        yOffset = (event.rawY - top).toInt()
                                        mLeftMargin = it.leftMargin
                                        mTopMargin = it.topMargin
                                    } else if (it is WindowManager.LayoutParams) {
                                        xOffset = (event.rawX - it.x).toInt()//解决偏差。
                                        yOffset = (event.rawY - it.y).toInt()
                                    }
                                }
                            }
                            MotionEvent.ACTION_MOVE -> {
                                //手指移动
                                measureMargin(event)
                                margin(leftMargin = mLeftMargin, topMargin = mTopMargin)
                            }
                            MotionEvent.ACTION_UP -> {
                                isDrag = false
                                //手指离开（最后一根手指）
                                dragUp?.let {
                                    //fixme fixme 返回控件在父容器中的中心坐标，及左上右下的位置
                                    it(mLeftMargin + ww / 2, mTopMargin + hh / 2, left, top, right, bottom)
                                }
                                if (isMoveMotion) {
                                    saveDrag()//保存当前拖动状态。
                                }
                                if (it.isAbs && isMoveMotion) {//isAbs 是否吸附,isMoveMotion 是否触摸过。
                                    layoutParams?.let {
                                        if (it is RelativeLayout.LayoutParams) {
                                            mLeftMargin = it.leftMargin
                                            mTopMargin = it.topMargin
                                        } else if (it is WindowManager.LayoutParams) {
                                            mLeftMargin = it.x
                                            mTopMargin = it.y
                                        }
                                    }
                                    //fixme 吸附
                                    if (it.isAbsLeft && it.isAbsTop && it.isAbsRight && it.isAbsBottom) {
                                        //根据距离，自动吸附到左上右下四个边界。（吸附到距离最短的一边）
                                        var mleft = left//左边的距离
                                        var mright = parentWidth - mleft - ww//右边的距离
                                        var mtop = top//顶部的距离
                                        var mbottom = parentHeight - mtop - hh//底部的距离
                                        layoutParams?.let {
                                            if (it is WindowManager.LayoutParams) {
                                                mleft = it.x
                                                mtop = it.y
                                                mright = parentWidth - mleft - ww//右边的距离
                                                mbottom = parentHeight - mtop - hh//底部的距离
                                            }
                                        }
                                        if (mleft <= mright && mleft <= mtop && mleft <= mbottom) {
                                            //吸附到左边界
                                            ofIntLeftMargin(mLeftMargin, it.left_gap, it.duraton)
                                        } else if (mtop <= mright && mtop <= mleft && mtop <= mbottom) {
                                            //吸附到上边界
                                            ofIntTopMargin(mTopMargin, it.top_gap, it.duraton)
                                        } else if (mright <= mtop && mright <= mleft && mright <= mbottom) {
                                            //吸附到右边界
                                            ofIntLeftMargin(mLeftMargin, parentWidth - it.right_gap - ww, it.duraton)
                                        } else if (mbottom <= mtop && mbottom <= mleft && mbottom <= mright) {
                                            //吸附到下边界
                                            ofIntTopMargin(mTopMargin, parentHeight - it.top_gap - hh, it.duraton)
                                        }
                                    } else if (it.isAbsLeft && it.isAbsRight) {
                                        //根据距离，自动吸附到左边和右边。（吸附到距离最短的一边）
                                        var mleft = left//左边的距离
                                        var mright = parentWidth - mleft - ww//右边的距离
                                        layoutParams?.let {
                                            if (it is WindowManager.LayoutParams) {
                                                mleft = it.x
                                                mright = parentWidth - mleft - ww//右边的距离
                                            }
                                        }
                                        if (mleft <= mright) {
                                            //吸附到左边界
                                            ofIntLeftMargin(mLeftMargin, it.left_gap, it.duraton)
                                        } else {
                                            //吸附到右边界
                                            ofIntLeftMargin(mLeftMargin, parentWidth - it.right_gap - ww, it.duraton)
                                        }
                                    } else if (it.isAbsTop && it.isAbsBottom) {
                                        //根据距离，自动吸附到上边和下边。（吸附到距离最短的一边）
                                        var mtop = top//顶部的距离
                                        var mbottom = parentHeight - mtop - hh//底部的距离
                                        layoutParams?.let {
                                            if (it is WindowManager.LayoutParams) {
                                                mtop = it.y
                                                mbottom = parentHeight - mtop - hh//底部的距离
                                            }
                                        }
                                        if (mtop <= mbottom) {
                                            //吸附到上边界
                                            ofIntTopMargin(mTopMargin, it.top_gap, it.duraton)
                                        } else {
                                            //吸附到下边界
                                            ofIntTopMargin(mTopMargin, parentHeight - it.top_gap - hh, it.duraton)
                                        }
                                    } else if (it.isAbsLeft) {
                                        //吸附到左边界
                                        ofIntLeftMargin(mLeftMargin, it.left_gap, it.duraton)
                                    } else if (it.isAbsRight) {
                                        //吸附到右边界
                                        ofIntLeftMargin(mLeftMargin, parentWidth - it.right_gap - ww, it.duraton)
                                    } else if (it.isAbsTop) {
                                        //吸附到上边界
                                        ofIntTopMargin(mTopMargin, it.top_gap, it.duraton)
                                    } else if (it.isAbsBottom) {
                                        //吸附到下边界
                                        ofIntTopMargin(mTopMargin, parentHeight - it.top_gap - hh, it.duraton)
                                    }
                                }
                            }
                            MotionEvent.ACTION_CANCEL -> {
                                //其他异常
                                isDrag = false
                            }
                        }
                    }
                }
            }
        }
        return b
    }

    //fixme (bottom-top) 计算控制最真实的高；(right-left)计算控件最真实的宽度
    //计算外补订
    private fun measureMargin(event: MotionEvent) {
        if (drag != null) {
            mLeftMargin = event.rawX.toInt() - xOffset
            mTopMargin = event.rawY.toInt() - yOffset
            //左边界判断
            if (mLeftMargin < drag!!.left_gap) {
                mLeftMargin = drag!!.left_gap
            }
            //上边界判断
            if (mTopMargin < drag!!.top_gap) {
                mTopMargin = drag!!.top_gap
            }
            //右边界判断
            if (parentWidth - mLeftMargin - ww < drag!!.right_gap) {
                mLeftMargin = parentWidth - ww - drag!!.right_gap
            }
            //下边界判断
            if (parentHeight - mTopMargin - hh < drag!!.bottom_gap) {
                mTopMargin = parentHeight - hh - drag!!.bottom_gap
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewGroup = null
        drag = null
        dragUp = null
    }

}
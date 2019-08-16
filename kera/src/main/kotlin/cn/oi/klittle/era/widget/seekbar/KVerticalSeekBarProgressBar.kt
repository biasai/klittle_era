package cn.oi.klittle.era.widget.seekbar

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.entity.widget.compat.KProgressbar_dst_src_Entity
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.widget.KBounceScrollView
import cn.oi.klittle.era.widget.compat.KView

//            使用案例
//            KVerticalSeekBarProgressBar(this).apply {
//                dst {
//                    all_radius(kpx.x(33f))
//                    bg_color = Color.GRAY
//                    height = kpx.x(700 - 70)
//                    width = kpx.x(30)
//                    //width = kpx.x(330)
//                    //bg_bp = getBitmapFromResource(R.mipmap.timg)//fixme 位图优先级要高
//                }
//                src {
//                    //进度条的方向是从下往上；但是垂直颜色渐变方向始终是：从上往下。
//                    bgVerticalColors(Color.RED, Color.CYAN, Color.GREEN)
//                    isBgGradient = false
//                    isSrcGradient = true
//                    //bg_bp = getBitmapFromResource(R.mipmap.timg2)//fixme 位图优先级要高
//                }
//                radius {
//                    all_radius(kpx.x(33f))
//                    strokeColor = Color.CYAN
//                    strokeWidth = kpx.x(3f)
//                    height = kpx.x(700 - 70)
//                    width = kpx.x(30)
//                }
//                //关闭硬件加速，阴影才会有效果
//                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
//                //画移动条块
//                drawThumb { canvas, paint, x, y ->
//                    KLoggerUtils.e("x:\t"+x+"\ty:\t"+y)
//                    paint.color = Color.WHITE
//                    paint.setShadowLayer(kpx.x(10f), 0f, 0f, Color.RED)
//                    canvas.drawCircle(x, y, kpx.x(25f), paint)
//                }
//                //fixme 防止宽高获取不到，防止滑动块显示异常；最好在布局加载完成之后，在设置进度。
//                gone {
//                    setProgress(61)
//                }
//                //进度变化监听
//                setOnSeekBarChangeListener { previousProgress, currentProgress, isActionUp, progressX, progressY ->
//                }
//            }.lparams {
//                width = kpx.x(113)
//                height = kpx.x(700)
//            }

/**
 * fixme 垂直进度条，方向：从下往上
 */
class KVerticalSeekBarProgressBar : KView, View.OnTouchListener {
    var isEnableSeekbarTouch = true//是否开启seekbar触摸事件。默认开启
    private var isACTION_UP = false//手指是否离开
    private var previousProgress: Float = 0f

    private var isTouch:Boolean=false
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event?.let {
            if (it.action==MotionEvent.ACTION_DOWN){
                isTouch=true
                KBounceScrollView.isChildScoll = true//fixme  解决scrollView的滑动冲突
            }else if (it.action==MotionEvent.ACTION_UP){
                KBounceScrollView.isChildScoll = false
                isTouch=false
            }
        }
        if (!isEnableSeekbarTouch||!isTouch) {
            isACTION_UP = true
            return false
        }
        event?.let {
            if (it.action == MotionEvent.ACTION_DOWN) {
                previousProgress = progress//记录触摸前的进度
                isACTION_UP = false
            }
            if (it.action == MotionEvent.ACTION_UP) {
                isACTION_UP = true
            }
            //滑动改变进度状态
            var p = (h.toFloat() - it.y) / h.toFloat()//event.x是相对于控件自身的。
            src?.let {
                if (it.height > 0) {
                    var bottom = centerY + it.height / 2
                    p = (bottom - event.y) / it.height
                }
            }
            if (p > 1) {
                p = 1f
            }
            if (p < 0) {
                p = 0f
            }
            if (p != progress || it.action == MotionEvent.ACTION_UP) {
                progress = p
            }
        }
        return true
    }

    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//开启硬件加速,不然圆角没有效果,fixme :再次亲测，必须开硬件加速。
        setOnTouchListener(this)
    }


    private var dst: KProgressbar_dst_src_Entity? = null//底层
    private var src: KProgressbar_dst_src_Entity? = null//进度条层
    var progressX: Float = 0f
    var progressY: Float = 0f
    var progress: Float = 0f
        //进度（0~1）
        set(value) {
            if (value > 1) {
                field = 1f
            } else if (value < 0) {
                field = 0f
            } else {
                field = value
            }
            progressX = centerX
            src?.let {
                if (it.height > 0) {
                    //数值计算是正确的。没问题
                    var left = centerY + it.height / 2
                    progressY = left - it.height * field
                } else {
                    progressY = w - w * field
                }
            }
            if (height > 0) {
                //回调，返回当前进度值。
                onSeekBarChangeListener?.let {
                    it(previousProgress, field, isACTION_UP, progressX, progressY)
                }
            }
            invalidate()//刷新
        }

    /**
     * 设置百分比
     * @param progress 进度值(0~100)）
     */
    fun setProgress(progress: Int) {
        if (progress <= 0) {
            this.progress = 0f
        } else if (progress >= 100) {
            this.progress = 1f
        } else {
            this.progress = progress.toFloat() / 100f
        }
        invalidate()
    }

    private var onSeekBarChangeListener: ((previousProgress: Float, currentProgress: Float, isActionUp: Boolean, progressX: Float, progressY: Float) -> Unit)? = null
    /**
     * 回调;
     * previousProgress 上次进度（未变化前的进度，即手指未触摸前的进度。），fixme 注意记录的是手指触摸前的进度
     * currentProgress当前进度;
     * isActionUp true手指离开,false 手指没离开，表示现在正处触摸状态。
     * x 当前进度对应的x坐标
     * y 当前进度对应的y坐标
     */
    fun setOnSeekBarChangeListener(onSeekBarChangeListener: (previousProgress: Float, currentProgress: Float, isActionUp: Boolean, progressX: Float, progressY: Float) -> Unit) {
        this.onSeekBarChangeListener = onSeekBarChangeListener
    }


    fun dst(block: KProgressbar_dst_src_Entity.() -> Unit): KVerticalSeekBarProgressBar {
        if (dst == null) {
            dst = KProgressbar_dst_src_Entity()
        }
        block(dst!!)
        invalidate()
        return this
    }

    fun src(block: KProgressbar_dst_src_Entity.() -> Unit): KVerticalSeekBarProgressBar {
        if (dst != null) {
            src = dst?.copy()
        }
        if (src == null) {
            src = KProgressbar_dst_src_Entity()
        }
        block(src!!)
        invalidate()
        return this
    }

    /**
     * 自己去画移动条
     */
    private var drawThumb: ((canvas: Canvas, paint: Paint, x: Float, y: Float) -> Unit)? = null


    fun drawThumb(drawThumb: ((canvas: Canvas, paint: Paint, x: Float, y: Float) -> Unit)?) {
        this.drawThumb = drawThumb
    }

    /**
     * 画dst(底层样式)和src(进度条样式)
     * @param canvas 画布
     * @param model 实体类样式
     * @param isSrc 是否为src层
     */
    fun drawDstOrSrc(canvas: Canvas, paint: Paint, model: KProgressbar_dst_src_Entity, isSrc: Boolean) {
        model?.let {
            var left = 0f + scrollX
            var right = w + left
            var top = 0f + scrollY
            var bottom = h + top
            if (it.width > 0) {
                left = centerX - it.width / 2
                right = centerX + it.width / 2
            }
            if (it.height > 0) {
                bottom = centerY + it.height / 2
                top = centerY - it.height / 2
            }
            //画颜色
            if (it.bg_bp == null || it.bg_bp!!.isRecycled) {
                var isDrawColor = false//是否画背景色
                if (it.bg_color != Color.TRANSPARENT) {
                    paint.color = it.bg_color
                    isDrawColor = true
                }
                if (it.bgVerticalColors != null) {
                    var shader: LinearGradient? = null
                    if (!it.isBgGradient) {
                        //垂直不渐变
                        shader = getNotLinearGradient(top, bottom, it.bgVerticalColors!!, true, scrollY)
                    }
                    //垂直渐变，优先级高于水平(渐变颜色值数组必须大于等于2，不然异常)
                    if (shader == null) {
                        //进度条的方向是从下往上；但是垂直颜色渐变方向始终是：从上往下。
                        shader = LinearGradient(0f, top, 0f, bottom, it.bgVerticalColors, null, Shader.TileMode.CLAMP)
                    }
                    paint.setShader(shader)
                    isDrawColor = true
                } else if (it.bgHorizontalColors != null) {
                    var shader: LinearGradient? = null
                    if (isSrc && it.isSrcGradient) {
                        //fixme 以进度条的长度为渐变标准
                        if (it.width > 0) {
                            right = left + (it.width * progress)
                        } else {
                            right = left + (w * progress)
                        }
                    }
                    if (!it.isBgGradient) {
                        //水平不渐变
                        shader = getNotLinearGradient(left, right, it.bgHorizontalColors!!, false, scrollY)
                    }
                    //水平渐变
                    if (shader == null) {
                        shader = LinearGradient(left, 0f, right, 0f, it.bgHorizontalColors, null, Shader.TileMode.CLAMP)
                    }
                    paint.setShader(shader)
                    isDrawColor = true
                }
                if (Build.VERSION.SDK_INT <= 17) {
                    var h2 = h.toFloat()
                    if (w < h) {
                        h2 = w.toFloat()//取小的那一边
                    }
                    h2 = h2 / 2
                    if (it.left_top > h2) {
                        it.left_top = h2
                    }
                    if (it.right_top > h2) {
                        it.right_top = h2
                    }
                    if (it.right_bottom > h2) {
                        it.right_bottom = h2
                    }
                    if (it.left_bottom > h2) {
                        it.left_bottom = h2
                    }
                }
                //进度条层
                if (isSrc) {
                    if (it.height > 0) {
                        top = bottom - (it.height * progress)
                    } else {
                        top = bottom - (h * progress)
                    }
                    progressX = centerX
                    progressY = top
                    //fixme 如果进度小于宽度，则宽度和高度设置对等；不然圆角样式很丑！！
                    //有圆角属性时，进度宽度最好不要小于宽度。不然不好看。
                    if ((bottom - top) < (right - left) && (it.left_top > 0 || it.left_bottom > 0 || it.right_top > 0 || it.right_bottom > 0)) {
                        left = w / 2 - (bottom - top) / 2
                        right = w / 2 + (bottom - top) / 2
                    }
                }
                if (isDrawColor) {
                    // fixme 矩形弧度,防止Toat背景色没有圆角效果。所以直接画圆角背景
                    val radian = floatArrayOf(it.left_top!!, it.left_top!!, it.right_top, it.right_top, it.right_bottom, it.right_bottom, it.left_bottom, it.left_bottom)
                    //fixme  画圆角矩形背景
                    var rectF = RectF(left, top, right, bottom)
                    var path = Path()
                    path.addRoundRect(rectF, radian, Path.Direction.CW)
                    canvas.drawPath(path, paint)
                }
            } else if (it != null && it.bg_bp != null && !it.bg_bp!!.isRecycled) {
                //画位图
                if (isSrc) {
                    //进度条层
                    if (it.height > 0) {
                        top = bottom - (it.height * progress)
                    } else {
                        top = bottom - (h * progress)
                    }
                    var r = it.bg_bp!!.width
                    var b = it.bg_bp!!.height
                    var t = it.bg_bp!!.height - (it.bg_bp!!.height * progress)
                    //fixme 对图片进行百分比截取。
                    canvas.drawBitmap(it.bg_bp!!, Rect(0, t.toInt(), r, b), Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt()), paint)
                } else {
                    //fixme 放心，图片会完整的显示出来（比例不一样，会拉伸出来。）
                    //底层
                    canvas.drawBitmap(it.bg_bp!!, null, Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt()), paint)
                }
            }
        }
    }

    override fun draw2(canvas: Canvas, paint: Paint) {
        super.draw2(canvas, paint)
        //画底层
        dst?.let {
            drawDstOrSrc(canvas, paint, it, false)
        }
        //画进度条层
        src?.let {
            drawDstOrSrc(canvas, paint, it, true)
        }
        //fixme 最外层如果想要圆角边框，radius {}圆角属性。
    }

    override fun draw2Last(canvas: Canvas, paint: Paint) {
        super.draw2Last(canvas, paint)
        drawThumb?.let {
            it(canvas, paint, progressX, progressY)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onSeekBarChangeListener = null
        drawThumb = null
    }

}
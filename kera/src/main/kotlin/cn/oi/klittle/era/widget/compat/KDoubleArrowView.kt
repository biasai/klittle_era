package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.ViewGroup
import cn.oi.klittle.era.entity.widget.compat.KDiamondEntity
import cn.oi.klittle.era.entity.widget.compat.KDoubleArrowEntity

//                    fixme 双箭头横线，调用案例：
//                    doubleArrow {
//                        strokeWidth = kpx.x(3f)//边框大小
//                        //strokeColor=Color.RED//边框颜色
//                        //strokeHorizontalColors(Color.RED, Color.BLUE, Color.CYAN, Color.RED)//线条水平渐变颜色
//                        //isHorizontal = false//true 水平；false 垂直
//                        if (!isHorizontal) {
//                            strokeVerticalColors = strokeHorizontalColors//fixme 垂直渐变，优先级比水平渐变高。
//                        }
//                        arrowLength = kpx.x(25f)//箭头长度
//                        isROUND = true//线条是否加上圆形线帽
//                        isGradient = false//颜色是否渐变
//                        dashGap = kpx.x(10f)//虚线
//                        dashWidth = kpx.x(15f)
//                        isArrowDash = false//箭头是否也为虚线。
//                        isDrawMain=true//是否绘制主干线（连接两个箭头之间的线条）
//                        isDrawLeftArrow=false//是否绘制左箭头（水平）
//                        isLeftTurnDownOrLeft = false//左边箭头向下转弯（水平）；上面的箭头向左边转弯（垂直）；优先级比isLeftTurnUpOrRight高。
//                        isLeftTurnUpOrRight = true//向上面（水平）转弯或右边（垂直）转弯；isLeftTurnDownOrLeft = false才有效。
//                        isRightTurnDownOrLeft = true//右边箭头向下面转弯（水平）；下面箭头向左边转弯（垂直）；优先级比isRightTurnUpOrRight高
//                        isRightTurnUpOrRight = false//右边箭头向上转弯（水平）；下面的箭头向右转弯（垂直）；isRightTurnDownOrLeft=false才有效。
//                    }
//                    doubleArrow_press {
//                        strokeColor=Color.RED
//                        strokeHorizontalColors(Color.CYAN, Color.RED, Color.BLUE, Color.CYAN)
//                        dashGap = 0f//虚线
//                        dashWidth = 0f
//                        isDrawLeftArrow=true
//                        isArrowSolid=true// fixme 箭头是否为实心。实心就画三角形箭头。亲测有效。
//                    }

//fixme 左右水平，双击箭头控件;最后绘制的，不会受radius切割的影响。
open class KDoubleArrowView : KTextView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    private var mPath: Path? = null//主干线。
    override fun draw2Last(canvas: Canvas, paint: Paint) {
        super.draw2Last(canvas, paint)
        currentDoubleArrow?.let {
            if (width < 0 || height <= 0) {
                return
            }
            resetPaint(paint)
            //fixme 画主干线（垂直+水平居中画）
            paint.style = Paint.Style.STROKE//fixme 虚线只对STROKE类型有效。
            paint.strokeWidth = it.strokeWidth
            paint.color = it.strokeColor//颜色值
            var linearGradient: LinearGradient? = null
            if (it.strokeVerticalColors != null) {
                if (!it.isGradient) {
                    //垂直不渐变
                    linearGradient = getNotLinearGradient(scrollY.toFloat(), height + scrollY.toFloat(), it.strokeVerticalColors!!, true)
                }
                //fixme 垂直渐变优先
                if (linearGradient == null) {
                    linearGradient = LinearGradient(0f, scrollY.toFloat(), 0f, height + scrollY.toFloat(), it.strokeVerticalColors, null, Shader.TileMode.CLAMP)
                }
            } else if (it.strokeHorizontalColors != null) {
                var start = scrollX.toFloat()
                var end = width.toFloat() + scrollX
                if (!it.isGradient) {
                    //水平不渐变
                    linearGradient = getNotLinearGradient(start, end, it.strokeHorizontalColors!!, false)
                }
                //fixme 水平渐变
                if (linearGradient == null) {
                    linearGradient = LinearGradient(start, 0f, end, 0f, it.strokeHorizontalColors, null, Shader.TileMode.CLAMP)
                }
            }
            if (linearGradient != null) {
                //颜色渐变
                paint.setShader(linearGradient)
            }
            if (mPath == null) {
                mPath = Path()
            }
            mPath?.reset()
            /**
             * fixme 测试发现，线条的长度不能太长，貌似超过一万二之后就无法显示了；即 width = kpx.x(12000)
             */
            if (it.isROUND) {
                paint.strokeCap = Paint.Cap.ROUND
            } else {
                paint.strokeCap = Paint.Cap.BUTT
            }
            //虚线
            if (it.dashWidth > 0 && it.dashGap > 0) {
                var dashPathEffect = DashPathEffect(floatArrayOf(it.dashWidth, it.dashGap), 0f)
                paint.setPathEffect(dashPathEffect)
            }
            if (!it.isLeftTurnDownOrLeft && !it.isLeftTurnUpOrRight && !it.isRightTurnDownOrLeft && !it.isRightTurnUpOrRight) {
                //fixme 正常
                var mStartX = scrollX + paint.strokeWidth
                var mStartY = scrollY + height / 2f
                var mEndX = scrollX + width - paint.strokeWidth
                var mEndY = scrollY + height / 2f
                if (!it.isHorizontal) {
                    //垂直
                    mStartX = scrollX + width / 2f
                    mStartY = scrollY + paint.strokeWidth
                    mEndX = scrollX + width / 2f
                    mEndY = scrollY + height - paint.strokeWidth
                }
                //虚线对矩形不管用,只对矩形的边框有效，所以只能画线
                mPath?.moveTo(mStartX, mStartY)
                mPath?.lineTo(mEndX, mEndY)
                if (it.isDrawMain) {
                    //fixme 主干线
                    canvas.drawPath(mPath, paint)
                }
                //fixme 画箭头
                if (it.arrowLength > 0) {
                    if (!it.isArrowDash && it.dashGap > 0 && it.dashWidth > 0) {
                        paint.setPathEffect(null)//fixme 箭头取消虚线。
                    }
                    if (it.isDrawLeftArrow) {
                        if (it.isHorizontal) {
                            //左边箭头
                            mPath?.reset()
                            var arrowX = mStartX + it.arrowLength
                            var arrowY = mStartY - it.arrowLength
                            var arrowX2 = mStartX + it.arrowLength
                            var arrowY2 = mStartY + it.arrowLength
                            mPath?.moveTo(arrowX, arrowY)
                            mPath?.lineTo(mStartX, mStartY)
                            mPath?.lineTo(arrowX2, arrowY2)
                            if (it.isArrowSolid){
                                //画实心三角形箭头
                                mPath?.close()
                                paint.style = Paint.Style.FILL_AND_STROKE
                            }
                            canvas.drawPath(mPath, paint)
                            paint.style = Paint.Style.STROKE
                        } else {
                            //上面的箭头
                            mPath?.reset()
                            var arrowX = mStartX - it.arrowLength
                            var arrowY = mStartY + it.arrowLength
                            var arrowX2 = mStartX + it.arrowLength
                            var arrowY2 = mStartY + it.arrowLength
                            mPath?.moveTo(arrowX, arrowY)
                            mPath?.lineTo(mStartX, mStartY)
                            mPath?.lineTo(arrowX2, arrowY2)
                            if (it.isArrowSolid){
                                //画实心三角形箭头
                                mPath?.close()
                                paint.style = Paint.Style.FILL_AND_STROKE
                            }
                            canvas.drawPath(mPath, paint)
                            paint.style = Paint.Style.STROKE
                        }
                    }
                    if (it.isDrawRightArrow) {
                        if (it.isHorizontal) {
                            //右箭头
                            mPath?.reset()
                            var arrowX = mEndX - it.arrowLength
                            var arrowY = mEndY - it.arrowLength
                            var arrowX2 = mEndX - it.arrowLength
                            var arrowY2 = mEndY + it.arrowLength
                            mPath?.moveTo(arrowX, arrowY)
                            mPath?.lineTo(mEndX, mEndY)
                            mPath?.lineTo(arrowX2, arrowY2)
                            if (it.isArrowSolid){
                                //画实心三角形箭头
                                mPath?.close()
                                paint.style = Paint.Style.FILL_AND_STROKE
                            }
                            canvas.drawPath(mPath, paint)
                            paint.style = Paint.Style.STROKE
                        } else {
                            //底部箭头
                            mPath?.reset()
                            var arrowX = mEndX - it.arrowLength
                            var arrowY = mEndY - it.arrowLength
                            var arrowX2 = mEndX + it.arrowLength
                            var arrowY2 = mEndY - it.arrowLength
                            mPath?.moveTo(arrowX, arrowY)
                            mPath?.lineTo(mEndX, mEndY)
                            mPath?.lineTo(arrowX2, arrowY2)
                            if (it.isArrowSolid){
                                //画实心三角形箭头
                                mPath?.close()
                                paint.style = Paint.Style.FILL_AND_STROKE
                            }
                            canvas.drawPath(mPath, paint)
                            paint.style = Paint.Style.STROKE
                        }
                    }
                }
            } else {
                //fixme 转弯的箭头;画主干线
                var mStartX = scrollX + paint.strokeWidth + it.arrowLength
                var mStartY = scrollY + height / 2f
                var mEndX = scrollX + width - paint.strokeWidth - it.arrowLength
                var mEndY = scrollY + height / 2f
                if (!it.isHorizontal) {
                    //垂直
                    mStartX = scrollX + width / 2f
                    mStartY = scrollY + paint.strokeWidth + it.arrowLength
                    mEndX = scrollX + width / 2f
                    mEndY = scrollY + height - paint.strokeWidth - it.arrowLength
                }
                //虚线对矩形不管用,只对矩形的边框有效，所以只能画线
                mPath?.moveTo(mStartX, mStartY)
                mPath?.lineTo(mEndX, mEndY)
                if (it.isLeftTurnDownOrLeft) {
                    //fixme 左边箭头，向下转弯(水平)
                    mPath?.moveTo(mStartX, mStartY)
                    if (it.isHorizontal) {
                        //水平，向下面转
                        mStartY = scrollY + height - paint.strokeWidth
                    } else {
                        //垂直，向左边转
                        mStartX = scrollX + paint.strokeWidth
                    }
                    mPath?.lineTo(mStartX, mStartY)
                } else if (it.isLeftTurnUpOrRight) {
                    //fixme 左边箭头，向上转弯
                    mPath?.moveTo(mStartX, mStartY)
                    if (it.isHorizontal) {
                        //水平，向上面转
                        mStartY = scrollY + paint.strokeWidth
                    } else {
                        //垂直，向右边转
                        mStartX = scrollX + width - paint.strokeWidth
                    }
                    mPath?.lineTo(mStartX, mStartY)
                } else {
                    //fixme 正常
                    mPath?.moveTo(mStartX, mStartY)
                    if (it.isHorizontal) {
                        //水平
                        mStartX = scrollX + paint.strokeWidth
                    } else {
                        mStartY = scrollY + paint.strokeWidth
                    }
                    mPath?.lineTo(mStartX, mStartY)
                }
                if (it.isRightTurnDownOrLeft) {
                    //fixme 右边箭头，向下转弯
                    mPath?.moveTo(mEndX, mEndY)
                    if (it.isHorizontal) {
                        //水平，向下面转
                        mEndY = scrollY + height - paint.strokeWidth
                    } else {
                        //垂直，向左边转
                        mEndX = scrollX + paint.strokeWidth
                    }
                    mPath?.lineTo(mEndX, mEndY)
                } else if (it.isRightTurnUpOrRight) {
                    //fixme 右边箭头，向上转弯
                    mPath?.moveTo(mEndX, mEndY)
                    if (it.isHorizontal) {
                        //水平，向下面转
                        mEndY = scrollY + paint.strokeWidth
                    } else {
                        //垂直，向左边转
                        mEndX = scrollX + width - paint.strokeWidth
                    }
                    mPath?.lineTo(mEndX, mEndY)
                } else {
                    //fixme 正常
                    mPath?.moveTo(mEndX, mEndY)
                    if (it.isHorizontal) {
                        //水平
                        mEndX = scrollX + width - paint.strokeWidth
                    } else {
                        mEndY = scrollY + height - paint.strokeWidth
                    }
                    mPath?.lineTo(mEndX, mEndY)
                }
                if (it.isDrawMain) {
                    //fixme 主干线
                    canvas.drawPath(mPath, paint)
                }
                //fixme 画箭头
                if (it.arrowLength > 0) {
                    if (!it.isArrowDash && it.dashGap > 0 && it.dashWidth > 0) {
                        paint.setPathEffect(null)//fixme 箭头取消虚线。
                    }
                    if (it.isDrawLeftArrow) {
                        //fixme 左箭头
                        if (it.isLeftTurnDownOrLeft) {
                            //向下弯
                            if (it.isHorizontal) {
                                //向下弯
                                mPath?.reset()
                                var arrowX = mStartX - it.arrowLength
                                var arrowY = mStartY - it.arrowLength
                                var arrowX2 = mStartX + it.arrowLength
                                var arrowY2 = mStartY - it.arrowLength
                                mPath?.moveTo(arrowX, arrowY)
                                mPath?.lineTo(mStartX, mStartY)
                                mPath?.lineTo(arrowX2, arrowY2)
                                if (it.isArrowSolid){
                                    //画实心三角形箭头
                                    mPath?.close()
                                    paint.style = Paint.Style.FILL_AND_STROKE
                                }
                                canvas.drawPath(mPath, paint)
                                paint.style = Paint.Style.STROKE
                            } else {
                                //向左弯
                                mPath?.reset()
                                var arrowX = mStartX + it.arrowLength
                                var arrowY = mStartY - it.arrowLength
                                var arrowX2 = mStartX + it.arrowLength
                                var arrowY2 = mStartY + it.arrowLength
                                mPath?.moveTo(arrowX, arrowY)
                                mPath?.lineTo(mStartX, mStartY)
                                mPath?.lineTo(arrowX2, arrowY2)
                                if (it.isArrowSolid){
                                    //画实心三角形箭头
                                    mPath?.close()
                                    paint.style = Paint.Style.FILL_AND_STROKE
                                }
                                canvas.drawPath(mPath, paint)
                                paint.style = Paint.Style.STROKE
                            }
                        } else if (it.isLeftTurnUpOrRight) {
                            //向上弯
                            if (it.isHorizontal) {
                                //向上弯
                                mPath?.reset()
                                var arrowX = mStartX - it.arrowLength
                                var arrowY = mStartY + it.arrowLength
                                var arrowX2 = mStartX + it.arrowLength
                                var arrowY2 = mStartY + it.arrowLength
                                mPath?.moveTo(arrowX, arrowY)
                                mPath?.lineTo(mStartX, mStartY)
                                mPath?.lineTo(arrowX2, arrowY2)
                                if (it.isArrowSolid){
                                    //画实心三角形箭头
                                    mPath?.close()
                                    paint.style = Paint.Style.FILL_AND_STROKE
                                }
                                canvas.drawPath(mPath, paint)
                                paint.style = Paint.Style.STROKE
                            } else {
                                //向右弯
                                mPath?.reset()
                                var arrowX = mStartX - it.arrowLength
                                var arrowY = mStartY - it.arrowLength
                                var arrowX2 = mStartX - it.arrowLength
                                var arrowY2 = mStartY + it.arrowLength
                                mPath?.moveTo(arrowX, arrowY)
                                mPath?.lineTo(mStartX, mStartY)
                                mPath?.lineTo(arrowX2, arrowY2)
                                if (it.isArrowSolid){
                                    //画实心三角形箭头
                                    mPath?.close()
                                    paint.style = Paint.Style.FILL_AND_STROKE
                                }
                                canvas.drawPath(mPath, paint)
                                paint.style = Paint.Style.STROKE
                            }
                        } else {
                            //正常
                            if (it.isHorizontal) {
                                //左边箭头
                                mPath?.reset()
                                var arrowX = mStartX + it.arrowLength
                                var arrowY = mStartY - it.arrowLength
                                var arrowX2 = mStartX + it.arrowLength
                                var arrowY2 = mStartY + it.arrowLength
                                mPath?.moveTo(arrowX, arrowY)
                                mPath?.lineTo(mStartX, mStartY)
                                mPath?.lineTo(arrowX2, arrowY2)
                                if (it.isArrowSolid){
                                    //画实心三角形箭头
                                    mPath?.close()
                                    paint.style = Paint.Style.FILL_AND_STROKE
                                }
                                canvas.drawPath(mPath, paint)
                                paint.style = Paint.Style.STROKE
                            } else {
                                //上面的箭头
                                mPath?.reset()
                                var arrowX = mStartX - it.arrowLength
                                var arrowY = mStartY + it.arrowLength
                                var arrowX2 = mStartX + it.arrowLength
                                var arrowY2 = mStartY + it.arrowLength
                                mPath?.moveTo(arrowX, arrowY)
                                mPath?.lineTo(mStartX, mStartY)
                                mPath?.lineTo(arrowX2, arrowY2)
                                if (it.isArrowSolid){
                                    //画实心三角形箭头
                                    mPath?.close()
                                    paint.style = Paint.Style.FILL_AND_STROKE
                                }
                                canvas.drawPath(mPath, paint)
                                paint.style = Paint.Style.STROKE
                            }
                        }
                    }
                    if (it.isDrawRightArrow) {
                        //fixme 右箭头（水平）；下箭头（垂直）
                        if (it.isRightTurnDownOrLeft) {
                            //向下弯
                            if (it.isHorizontal) {
                                //向下弯
                                mPath?.reset()
                                var arrowX = mEndX - it.arrowLength
                                var arrowY = mEndY - it.arrowLength
                                var arrowX2 = mEndX + it.arrowLength
                                var arrowY2 = mEndY - it.arrowLength
                                mPath?.moveTo(arrowX, arrowY)
                                mPath?.lineTo(mEndX, mEndY)
                                mPath?.lineTo(arrowX2, arrowY2)
                                if (it.isArrowSolid){
                                    //画实心三角形箭头
                                    mPath?.close()
                                    paint.style = Paint.Style.FILL_AND_STROKE
                                }
                                canvas.drawPath(mPath, paint)
                                paint.style = Paint.Style.STROKE
                            } else {
                                //向左弯
                                mPath?.reset()
                                var arrowX = mEndX + it.arrowLength
                                var arrowY = mEndY - it.arrowLength
                                var arrowX2 = mEndX + it.arrowLength
                                var arrowY2 = mEndY + it.arrowLength
                                mPath?.moveTo(arrowX, arrowY)
                                mPath?.lineTo(mEndX, mEndY)
                                mPath?.lineTo(arrowX2, arrowY2)
                                if (it.isArrowSolid){
                                    //画实心三角形箭头
                                    mPath?.close()
                                    paint.style = Paint.Style.FILL_AND_STROKE
                                }
                                canvas.drawPath(mPath, paint)
                                paint.style = Paint.Style.STROKE
                            }
                        } else if (it.isRightTurnUpOrRight) {
                            //向上弯
                            if (it.isHorizontal) {
                                //向上弯
                                mPath?.reset()
                                var arrowX = mEndX - it.arrowLength
                                var arrowY = mEndY + it.arrowLength
                                var arrowX2 = mEndX + it.arrowLength
                                var arrowY2 = mEndY + it.arrowLength
                                mPath?.moveTo(arrowX, arrowY)
                                mPath?.lineTo(mEndX, mEndY)
                                mPath?.lineTo(arrowX2, arrowY2)
                                if (it.isArrowSolid){
                                    //画实心三角形箭头
                                    mPath?.close()
                                    paint.style = Paint.Style.FILL_AND_STROKE
                                }
                                canvas.drawPath(mPath, paint)
                                paint.style = Paint.Style.STROKE
                            } else {
                                //向右弯
                                mPath?.reset()
                                var arrowX = mEndX - it.arrowLength
                                var arrowY = mEndY - it.arrowLength
                                var arrowX2 = mEndX - it.arrowLength
                                var arrowY2 = mEndY + it.arrowLength
                                mPath?.moveTo(arrowX, arrowY)
                                mPath?.lineTo(mEndX, mEndY)
                                mPath?.lineTo(arrowX2, arrowY2)
                                if (it.isArrowSolid){
                                    //画实心三角形箭头
                                    mPath?.close()
                                    paint.style = Paint.Style.FILL_AND_STROKE
                                }
                                canvas.drawPath(mPath, paint)
                                paint.style = Paint.Style.STROKE
                            }
                        } else {
                            //正常
                            if (it.isHorizontal) {
                                //右箭头
                                mPath?.reset()
                                var arrowX = mEndX - it.arrowLength
                                var arrowY = mEndY - it.arrowLength
                                var arrowX2 = mEndX - it.arrowLength
                                var arrowY2 = mEndY + it.arrowLength
                                mPath?.moveTo(arrowX, arrowY)
                                mPath?.lineTo(mEndX, mEndY)
                                mPath?.lineTo(arrowX2, arrowY2)
                                if (it.isArrowSolid){
                                    //画实心三角形箭头
                                    mPath?.close()
                                    paint.style = Paint.Style.FILL_AND_STROKE
                                }
                                canvas.drawPath(mPath, paint)
                                paint.style = Paint.Style.STROKE
                            } else {
                                //底部箭头
                                mPath?.reset()
                                var arrowX = mEndX - it.arrowLength
                                var arrowY = mEndY - it.arrowLength
                                var arrowX2 = mEndX + it.arrowLength
                                var arrowY2 = mEndY - it.arrowLength
                                mPath?.moveTo(arrowX, arrowY)
                                mPath?.lineTo(mEndX, mEndY)
                                mPath?.lineTo(arrowX2, arrowY2)
                                if (it.isArrowSolid){
                                    //画实心三角形箭头
                                    mPath?.close()
                                    paint.style = Paint.Style.FILL_AND_STROKE
                                }
                                canvas.drawPath(mPath, paint)
                                paint.style = Paint.Style.STROKE
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getDoubleArrowEntity(): KDoubleArrowEntity {
        if (doubleArrow == null) {
            doubleArrow = KDoubleArrowEntity()
        }
        return doubleArrow!!
    }

    private var currentDoubleArrow: KDoubleArrowEntity? = null//当前边框

    private var doubleArrow: KDoubleArrowEntity? = null//正常
    fun doubleArrow(block: KDoubleArrowEntity.() -> Unit): KDoubleArrowView {
        clearButonShadow()//自定义圆角，就去除按钮默认的圆角阴影。不然效果不好。
        block(getDoubleArrowEntity())
        invalidate()
        return this
    }

    var doubleArrow_enable: KDoubleArrowEntity? = null//fixme 不可用
    fun doubleArrow_enable(block: KDoubleArrowEntity.() -> Unit): KDoubleArrowView {
        if (doubleArrow_enable == null) {
            doubleArrow_enable = getDoubleArrowEntity().copy()
        }
        block(doubleArrow_enable!!)
        invalidate()
        return this
    }

    var doubleArrow_press: KDoubleArrowEntity? = null//按下
    fun doubleArrow_press(block: KDoubleArrowEntity.() -> Unit): KDoubleArrowView {
        if (doubleArrow_press == null) {
            doubleArrow_press = getDoubleArrowEntity().copy()
        }
        block(doubleArrow_press!!)
        invalidate()
        return this
    }

    var doubleArrow_focuse: KDoubleArrowEntity? = null//聚焦
    fun doubleArrow_focuse(block: KDoubleArrowEntity.() -> Unit): KDoubleArrowView {
        if (doubleArrow_focuse == null) {
            doubleArrow_focuse = getDoubleArrowEntity().copy()
        }
        block(doubleArrow_focuse!!)
        invalidate()
        return this
    }

    var doubleArrow_hove: KDoubleArrowEntity? = null//悬浮
    fun doubleArrow_hove(block: KDoubleArrowEntity.() -> Unit): KDoubleArrowView {
        if (doubleArrow_hove == null) {
            doubleArrow_hove = getDoubleArrowEntity().copy()
        }
        block(doubleArrow_hove!!)
        invalidate()
        return this
    }

    var doubleArrow_selected: KDoubleArrowEntity? = null//选中
    fun doubleArrow_selected(block: KDoubleArrowEntity.() -> Unit): KDoubleArrowView {
        if (doubleArrow_selected == null) {
            doubleArrow_selected = getDoubleArrowEntity().copy()
        }
        block(doubleArrow_selected!!)
        invalidate()
        return this
    }

    private fun normal() {
        currentDoubleArrow = doubleArrow
    }

    //状态：聚焦
    override fun changedFocused() {
        super.changedFocused()
        normal()
        doubleArrow_focuse?.let {
            currentDoubleArrow = it
        }
    }

    //状态：悬浮
    override fun changedHovered() {
        super.changedHovered()
        normal()
        doubleArrow_hove?.let {
            currentDoubleArrow = it
        }
    }

    //状态：选中
    override fun changedSelected() {
        super.changedSelected()
        normal()
        doubleArrow_selected?.let {
            currentDoubleArrow = it
        }
    }

    //状态：fixme 不可用
    override fun changedEnabled() {
        super.changedEnabled()
        normal()
        doubleArrow_enable?.let {
            currentDoubleArrow = it
        }
    }

    //状态：按下
    override fun changedPressed() {
        super.changedPressed()
        normal()
        doubleArrow_press?.let {
            currentDoubleArrow = it
        }
    }

    //状态：正常
    override fun changedNormal() {
        super.changedNormal()
        normal()
    }

    override fun onDestroy() {
        super.onDestroy()
        doubleArrow = null
        doubleArrow_press = null
        doubleArrow_hove = null
        doubleArrow_focuse = null
        doubleArrow_selected = null
        doubleArrow_enable = null
        mPath = null
        currentDoubleArrow=null
    }

}
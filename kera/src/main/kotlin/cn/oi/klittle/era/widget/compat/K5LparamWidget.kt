package cn.oi.klittle.era.widget.compat

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.graphics.Canvas
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import cn.oi.klittle.era.entity.widget.compat.KLparamEntity
import cn.oi.klittle.era.utils.KLoggerUtils

/**
 *五：宽高，外补丁控制
 */
open class K5LparamWidget : K4AutoBgView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    //按下
    var param_enable: KLparamEntity? = null

    fun param_enable(block: KLparamEntity.() -> Unit): K5LparamWidget {
        if (param_enable == null) {
            param_enable = getmParam().copy()//整个属性全部复制过来。
        }
        block(param_enable!!)
        paramView?.invalidate()
        paramView?.requestLayout()
        return this
    }

    //按下
    var param_press: KLparamEntity? = null

    fun param_press(block: KLparamEntity.() -> Unit): K5LparamWidget {
        if (param_press == null) {
            param_press = getmParam().copy()//整个属性全部复制过来。
        }
        block(param_press!!)
        paramView?.invalidate()
        paramView?.requestLayout()
        return this
    }

    //鼠标悬浮
    var param_hover: KLparamEntity? = null

    fun param_hover(block: KLparamEntity.() -> Unit): K5LparamWidget {
        if (param_hover == null) {
            param_hover = getmParam().copy()//整个属性全部复制过来。
        }
        block(param_hover!!)
        paramView?.invalidate()
        paramView?.requestLayout()
        return this
    }

    //聚焦
    var param_focuse: KLparamEntity? = null

    fun param_focuse(block: KLparamEntity.() -> Unit): K5LparamWidget {
        if (param_focuse == null) {
            param_focuse = getmParam().copy()//整个属性全部复制过来。
        }
        block(param_focuse!!)
        paramView?.invalidate()
        paramView?.requestLayout()
        return this
    }

    //选中
    var param_selected: KLparamEntity? = null

    fun param_selected(block: KLparamEntity.() -> Unit): K5LparamWidget {
        if (param_selected == null) {
            param_selected = getmParam().copy()//整个属性全部复制过来。
        }
        block(param_selected!!)
        paramView?.invalidate()
        paramView?.requestLayout()
        return this
    }

    //fixme 正常状态（先写正常样式，再写其他状态的样式，因为其他状态的样式初始值是复制正常状态的样式的。）
    var param: KLparamEntity? = null
    var paramView: View? = this
    private fun getmParam(): KLparamEntity {
        if (param == null) {
            param = getLayoutParamModel(paramView!!)//第一次，获取可能获取失败为空。如第一次加载时。布局还没加载成功。
            if (param == null) {
                param = KLparamEntity()
            }
        }
        return param!!
    }

    //fixme 注意方法名，没有s哦(s是系统的)。lparams那是anko布局的方法。为了和系统的区分开，专门把l和s去掉。
    fun param(block: KLparamEntity.() -> Unit): K5LparamWidget {
        block(getmParam())
        paramView?.invalidate()
        paramView?.requestLayout()
        return this
    }


    private var state_param: Int = -1//判断当前属性动画的状态

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        drawParam(canvas, this)
    }

    var lparamModel: KLparamEntity? = null
    fun drawParam(canvas: Canvas?, view: View) {
        view?.apply {
            if (param != null) {
                lparamModel = null
                if (isPressed && param_press != null) {
                    //按下
                    lparamModel = param_press
                } else if (isHovered && param_hover != null) {
                    //鼠标悬浮
                    lparamModel = param_hover
                } else if (isFocused && param_focuse != null) {
                    //聚焦
                    lparamModel = param_focuse
                } else if (isSelected && param_selected != null) {
                    //选中
                    lparamModel = param_selected
                }
                //不可用，优先级最高
                if (!isEnabled && param_enable != null) {
                    lparamModel = param_enable
                }
                //KLoggerUtils.e("isPressed:\t"+isPressed+"\tisEnabled:\t"+isEnabled+"\tparam_enable:\t"+param_enable+"\tlparamModel:\t"+lparamModel)
                //正常
                if (lparamModel == null) {
                    lparamModel = param
                }
                lparamModel?.let {
                    ofIntParams(it, view)
                    layoutParams(kmmWidth, kmmHeight, kmmLeftMargin, kmmTopMargin, kmmRightMargin, kmmBottomMargin, view)
                    var isinvalidate = false
                    if (scaleX != kmmScale || scaleY != kmmScale) {
                        scaleX = kmmScale
                        scaleY = kmmScale
                        isinvalidate = true
                    }
                    //fixme isView_jitter防止和抖动冲突。
                    if ((rotation != it.rotation) && !isView_jitter) {
                        //以组件的中心进行旋转
                        pivotX = (view.width + scrollX) / 2f
                        pivotY = (view.height + scrollY) / 2f
                        rotation = kmmRotation
                        isinvalidate = true
                    }
                    if (alpha != it.alpha) {
                        alpha = kmmAlpha
                        isinvalidate = true
                    }
                    if (isinvalidate) {
                        view.invalidate()
                    }
                }
            }
        }
    }

    //位置进行属性动画
    private fun ofIntParams(model: KLparamEntity, view: View) {
        if (state_param == state_current) {
            return//状态相同。不作处理
        }
        state_param = state_current
        if (model.duration <= 0) {
            //时间小于等于0，就没必要进行动画了。直接设置即可。
            kmmWidth = model.width
            kmmHeight = model.height
            kmmLeftMargin = model.leftMargin
            kmmTopMargin = model.topMargin
            kmmRightMargin = model.rightMargin
            kmmBottomMargin = model.bottomMargin
            kmmScale = model.scale
            kmmRotation = model.rotation
            kmmAlpha = model.alpha
            return
        }
        var params = getLayoutParamModel(view)
        params?.let {
            var propertyValuesHolder = PropertyValuesHolder.ofInt("kmmWidth", it.width, model.width)
            var propertyValuesHolder2 = PropertyValuesHolder.ofInt("kmmHeight", it.height, model.height)
            var propertyValuesHolder3 = PropertyValuesHolder.ofInt("kmmLeftMargin", it.leftMargin, model.leftMargin)
            var propertyValuesHolder4 = PropertyValuesHolder.ofInt("kmmTopMargin", it.topMargin, model.topMargin)
            var propertyValuesHolder5 = PropertyValuesHolder.ofInt("kmmRightMargin", it.rightMargin, model.rightMargin)
            var propertyValuesHolder6 = PropertyValuesHolder.ofInt("kmmBottomMargin", it.bottomMargin, model.bottomMargin)
            var propertyValuesHolder7 = PropertyValuesHolder.ofFloat("kmmScale", it.scale, model.scale)
            var propertyValuesHolder8 = PropertyValuesHolder.ofFloat("kmmRotation", it.rotation, model.rotation)
            var propertyValuesHolder9 = PropertyValuesHolder.ofFloat("kmmAlpha", it.alpha, model.alpha)
            val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(this, propertyValuesHolder, propertyValuesHolder2, propertyValuesHolder3, propertyValuesHolder4, propertyValuesHolder5, propertyValuesHolder6, propertyValuesHolder7, propertyValuesHolder8, propertyValuesHolder9)
            objectAnimator.repeatCount = 0
            objectAnimator.duration = model.duration
            objectAnimator.interpolator = LinearInterpolator()//线性变化，平均变化
            objectAnimator.addUpdateListener {
                view.invalidate()//刷新
            }
            objectAnimator.start()
        }
    }

    //fixme 获取当前的宽高和外补丁属性值
    fun getLayoutParamModel(view: View): KLparamEntity? {
        view?.apply {
            var layoutParams = view.layoutParams
            if (layoutParams != null) {
                var parama = KLparamEntity()
                layoutParams?.let {
                    parama.width = it.width
                    parama.height = it.height
                    parama.scale = kmmScale//记录缩放大小
                    parama.rotation = kmmRotation
                    parama.alpha = kmmAlpha
                    //绝对布局AbsoluteLayout没有外补丁
                    if (it is LinearLayout.LayoutParams) {
                        //线性布局
                        parama.leftMargin = it.leftMargin
                        parama.topMargin = it.topMargin
                        parama.rightMargin = it.rightMargin
                        parama.bottomMargin = it.bottomMargin
                    } else if (it is RelativeLayout.LayoutParams) {
                        //相对布局
                        parama.leftMargin = it.leftMargin
                        parama.topMargin = it.topMargin
                        parama.rightMargin = it.rightMargin
                        parama.bottomMargin = it.bottomMargin
                    } else if (it is ConstraintLayout.LayoutParams) {
                        //约束布局
                        parama.leftMargin = it.leftMargin
                        parama.topMargin = it.topMargin
                        parama.rightMargin = it.rightMargin
                        parama.bottomMargin = it.bottomMargin
                    } else if (it is FrameLayout.LayoutParams) {
                        //帧布局
                        parama.leftMargin = it.leftMargin
                        parama.topMargin = it.topMargin
                        parama.rightMargin = it.rightMargin
                        parama.bottomMargin = it.bottomMargin
                    }
                }
                return parama
            }
        }

        return null
    }

    //fixme 设置控件的高度和高度;必须要设置具体的数值才有效；matchparent等无效(高度可能无效，测试宽度都正常)
    // matchParent:	-1 wrapContent:	-2
    open fun layoutParams(width: Int = w, height: Int = h): ViewGroup.LayoutParams? {
        if (viewGroup == null) {
            viewGroup = this
        }
        viewGroup?.apply {
            if (width != w || height != h) {
                var w = width
                var h = height
                if (layoutParams == null) {
                    layoutParams = ViewGroup.LayoutParams(w, h)
                }
                viewGroup?.layoutParams?.apply {
                    //设置宽和高
                    this.width = w
                    this.height = h
                }
                layoutParams?.width = w
                layoutParams?.height = h
                viewGroup?.requestLayout()
                requestLayout()
            }
        }
        return viewGroup?.layoutParams
    }

    //fixme 重新设置宽高外补丁;和上面的layoutParams()方法不会冲突；能够正确识别。
    fun layoutParams(width: Int, height: Int, leftMargin: Int? = null, topMargin: Int? = null, rightMargin: Int? = null, bottomMargin: Int? = null, view: View? = this) {
        view?.apply {
            layoutParams?.let {
                var islayout = false//是否重新布局
                if (it.width != width) {
                    it.width = width
                    islayout = true
                }
                if (it.height != height) {
                    it.height = height
                    islayout = true
                }
                //绝对布局AbsoluteLayout没有外补丁
                if (it is LinearLayout.LayoutParams) {
                    //线性布局
                    if (leftMargin != null && it.leftMargin != leftMargin) {
                        it.leftMargin = leftMargin
                        islayout = true
                    }
                    if (topMargin != null && it.topMargin != topMargin) {
                        it.topMargin = topMargin
                        islayout = true
                    }
                    if (rightMargin != null && it.rightMargin != rightMargin) {
                        it.rightMargin = rightMargin
                        islayout = true
                    }
                    if (bottomMargin != null && it.bottomMargin != bottomMargin) {
                        it.bottomMargin = bottomMargin
                        islayout = true
                    }
                } else if (it is RelativeLayout.LayoutParams) {
                    //相对布局
                    if (leftMargin != null && it.leftMargin != leftMargin) {
                        it.leftMargin = leftMargin
                        islayout = true
                    }
                    if (topMargin != null && it.topMargin != topMargin) {
                        it.topMargin = topMargin
                        islayout = true
                    }
                    if (rightMargin != null && it.rightMargin != rightMargin) {
                        it.rightMargin = rightMargin
                        islayout = true
                    }
                    if (bottomMargin != null && it.bottomMargin != bottomMargin) {
                        it.bottomMargin = bottomMargin
                        islayout = true
                    }
                } else if (it is ConstraintLayout.LayoutParams) {
                    //约束布局
                    if (leftMargin != null && it.leftMargin != leftMargin) {
                        it.leftMargin = leftMargin
                        islayout = true
                    }
                    if (topMargin != null && it.topMargin != topMargin) {
                        it.topMargin = topMargin
                        islayout = true
                    }
                    if (rightMargin != null && it.rightMargin != rightMargin) {
                        it.rightMargin = rightMargin
                        islayout = true
                    }
                    if (bottomMargin != null && it.bottomMargin != bottomMargin) {
                        it.bottomMargin = bottomMargin
                        islayout = true
                    }
                } else if (it is FrameLayout.LayoutParams) {
                    //帧布局
                    if (leftMargin != null && it.leftMargin != leftMargin) {
                        it.leftMargin = leftMargin
                        islayout = true
                    }
                    if (topMargin != null && it.topMargin != topMargin) {
                        it.topMargin = topMargin
                        islayout = true
                    }
                    if (rightMargin != null && it.rightMargin != rightMargin) {
                        it.rightMargin = rightMargin
                        islayout = true
                    }
                    if (bottomMargin != null && it.bottomMargin != bottomMargin) {
                        it.bottomMargin = bottomMargin
                        islayout = true
                    }
                }
                if (islayout) {//fixme 为了以防万一，是否重新布局。自己还是做一下判断。
                    view.requestLayout()//fixme 重新刷新布局,内部自己做了判断，如果参数没有变化。是不会重新布局的。
                    //fixme 控件最大不会超过父容器的宽度和高度。最大只会等于父容器的宽度和高度。大于是无效的。
                }
            }
        }

    }

    var realHeight = -1
        //保存控件的实际高度
        get() {
            if (field < 0 && h > 0) {
                field = h
            }
            return field
        }
    var isShowHeight = true
        //true展开状态，false关闭状态
        get() {
            if (realHeight > 0) {
                if (h > realHeight / 2) {
                    return true//展开状态
                } else {
                    return false//关闭状态
                }
            } else {
                if (h > 0) {
                    return true//展开状态
                } else {
                    return false//关闭状态
                }
            }
        }

    //fixme 要显示的高度（控制高度的变化）
    fun height(mHeight: Int, duration: Long = 300) {
        viewGroup?.apply {
            if (realHeight < 0 && h > 0) {
                realHeight = h//保存实际原有高度
            }
            if (realHeight > 0) {
                //属性动画，随便搞个属性即可。不存在也没关系。仅仅需要这个属性值的变化过程
                ofInt("kmmmShowHeight", 0, duration, h, mHeight) {
                    kmmHeight = it//fixme 防止和属性动画冲突，同步
                    layoutParams?.apply {
                        //设置宽和高
                        height = it
                    }
                    requestLayout()
                }
            }
        }
    }

    //高度变化，0->h 或者 h->0 自主判断
    fun heightToggle(duration: Long = 300) {
        if (this != null && viewGroup != null) {
            if (realHeight < 0 && h > 0) {
                realHeight = h//保存实际原有高度
            }
            if (realHeight > 0) {
                if (isShowHeight) {
                    //显示状态 改为 关闭状态，高度设置为0
                    height(0, duration)
                } else {
                    //关闭状态 改为 显示状态，高度设置为原有高度
                    height(realHeight, duration)
                }
            }
        }
    }

    var realWidth = -1
        //保存控件的实际宽度
        get() {
            if (field < 0 && w > 0) {
                field = w
            }
            return field
        }
    var isShowWidth = true
        //true展开状态，false关闭状态
        get() {
            if (realWidth > 0) {
                if (w > realWidth / 2) {
                    return true//展开状态
                } else {
                    return false//关闭状态
                }
            } else {
                if (w > 0) {
                    return true//展开状态
                } else {
                    return false//关闭状态
                }
            }
        }

    //fixme 要显示的宽度（控制宽度的变化）
    fun width(mWidth: Int, duration: Long = 300) {
        if (realWidth < 0 && w > 0) {
            realWidth = w//保存实际原有宽度
        }
        viewGroup?.apply {
            if (realWidth > 0) {
                //属性动画，随便搞个属性即可。不存在也没关系。仅仅需要这个属性值的变化过程
                ofInt("mmmShowWidth", 0, duration, w, mWidth) {
                    kmmWidth = it//fixme 防止和属性动画冲突，同步
                    layoutParams?.apply {
                        //设置宽和高
                        width = it
                    }
                    requestLayout()
                }
            }
        }
    }

    //宽度变化，0->h 或者 h->0 自主判断
    fun widthToggle(duration: Long = 300) {
        if (this != null && viewGroup != null) {
            if (realWidth < 0 && w > 0) {
                realWidth = w//保存实际原有宽度
            }
            if (realWidth > 0) {
                if (isShowWidth) {
                    //显示状态 改为 关闭状态，宽度设置为0
                    width(0, duration)
                } else {
                    //关闭状态 改为 显示状态，宽度设置为原有宽度
                    width(realWidth, duration)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewGroup = null
        param = null
        param_focuse = null
        param_hover = null
        param_selected = null
        param_press = null
        param_enable = null
        paramView = null
        lparamModel = null
    }

}
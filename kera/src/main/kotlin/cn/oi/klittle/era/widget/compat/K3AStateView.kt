package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

//fixme 子类重写使用案例
////状态：聚焦
//override fun changedFocused() {
//    super.changedFocused()
//    normal()
//    border_focuse?.let {
//        currentBorder = it
//    }
//}
//
////状态：悬浮
//override fun changedHovered() {
//    super.changedHovered()
//    normal()
//    border_hove?.let {
//        currentBorder = it
//    }
//}
//
////状态：选中
//override fun changedSelected() {
//    super.changedSelected()
//    normal()
//    border_selected?.let {
//        currentBorder = it
//    }
//}
//
////状态：按下
//override fun changedPressed() {
//    super.changedPressed()
//    normal()
//    border_press?.let {
//        currentBorder = it
//    }
//}
//
////状态：正常
//override fun changedNormal() {
//    super.changedNormal()
//    normal()
//}

//状态：不可用 fixme 重点说明一下；isEnabled=false 不可用状态，只是屏蔽了点击事件，其他触摸状态，如isPressed是不受影响的。影响的仅仅是点击事件的回调。
//override fun changedNotEnabled() {
//    super.changedNotEnabled()
//    normal()
//    border_enable?.let {
//        currentBorder = it
//    }
//}

/**
 * 三：按下，悬浮，聚焦，选中，常态。
 * fixme 如果继承太多了，报错：java.lang.VerifyError: Verifier rejected class com.xx.xx
 * fixme 出现这个错误是因为改变了原有类继承的父类。导致Instant Run 增量编译时分包出错，验证继承关系时造成了混乱。可以clean工程后，重新编译即可。
 * fixme 即父类改变时，最好clean一下。
 *
 * fixme 为了保证，继承顺序和文件排列顺序一致，从上而下；所以命名加上了A;方便一眼查看继承关系。
 */
open class K3AStateView : K2GestureWidget {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    var onSetSelected: ((isSelected: Boolean) -> Unit)? = null
    fun onSetSelected(onSetSelected: ((isSelected: Boolean) -> Unit)? = null) {
        this.onSetSelected = onSetSelected
    }

    override fun setSelected(selected: Boolean) {
        try {
            var selected2 = isSelected
            super.setSelected(selected)
            if (selected2 != selected) {//fixme 防止重复调用,很重要。不要重复调用。
                onSetSelected?.let {
                    it(selected)//fixme 是否选中时监听
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //fixme 优先级：按下>悬浮>聚焦>选中>常态
    //这几个状态，是用来判断和控制属性动画的。
    protected var state_notEnable: Int = 0//不可用状态
    protected var state_press: Int = 1//按下状态
    protected var state_hover: Int = 2//鼠标悬浮状态
    protected var state_focuse: Int = 3//聚焦状态
    protected var state_selected: Int = 4//选中状态
    protected var state_normal: Int = 5//正常状态
    protected var state_current: Int = -3//当前状态
    protected var state_previous: Int = -4//上次状态
    fun drawState(view: View) {
        view.apply {
            if (!isEnabled) {
                //不可用状态
                if (state_current != state_notEnable) {
                    state_previous = state_current
                    state_current = state_notEnable
                    changedNotEnabled()
                }
            } else if (isPressed) {
                //按下
                if (state_current != state_press) {
                    state_previous = state_current
                    state_current = state_press
                    changedPressed()
                }
            } else if (isHovered) {
                //悬浮
                if (state_current != state_hover) {
                    state_previous = state_current
                    state_current = state_hover
                    changedHovered()
                }
            } else if (isFocused) {
                //聚焦
                if (state_current != state_focuse) {
                    state_previous = state_current
                    state_current = state_focuse
                    changedFocused()
                }
            } else if (isSelected) {
                //选中
                if (state_current != state_selected) {
                    state_previous = state_current
                    state_current = state_selected
                    changedSelected()
                }
            } else {
                //常态
                if (state_current != state_normal) {
                    state_previous = state_current
                    state_current = state_normal
                    changedNormal()
                }
            }
        }
    }

    override fun draw2Bg(canvas: Canvas, paint: Paint) {
        drawState(this)
        super.draw2Bg(canvas, paint)
    }

    var changedPressed: (() -> Unit)? = null
    fun changedPressed(changedPressed: (() -> Unit)) {
        this.changedPressed = changedPressed
    }

    var changedHovered: (() -> Unit)? = null
    fun changedHovered(changedHovered: (() -> Unit)) {
        this.changedHovered = changedHovered
    }


    var changedFocused: (() -> Unit)? = null
    fun changedFocused(changedFocused: (() -> Unit)) {
        this.changedFocused = changedFocused
    }

    var changedSelected: (() -> Unit)? = null
    fun changedSelected(changedSelected: (() -> Unit)) {
        this.changedSelected = changedSelected
    }

    var changedNormal: (() -> Unit)? = null
    fun changedNormal(changedNormal: (() -> Unit)) {
        this.changedNormal = changedNormal
    }

    var changedNotEnabled: (() -> Unit)? = null
    fun changedNotEnable(changedEnabled: (() -> Unit)) {
        this.changedNotEnabled = changedEnabled
    }

    //状态改变时，不可用
    open fun changedNotEnabled() {
        changedNotEnabled?.let {
            it()
        }
    }

    //状态改变时，按下
    open fun changedPressed() {
        changedPressed?.let {
            it()
        }
    }

    //状态改变时，悬浮
    open fun changedHovered() {
        changedHovered?.let {
            it()
        }
    }

    //状态改变时，聚焦
    open fun changedFocused() {
        changedFocused?.let {
            it()
        }
    }

    //状态改变时，选中
    open fun changedSelected() {
        changedSelected?.let {
            it()
        }
    }

    //状态改变时，常态
    open fun changedNormal() {
        changedNormal?.let {
            it()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        changedPressed = null
        changedFocused = null
        changedHovered = null
        changedSelected = null
        changedNormal = null
        changedNotEnabled = null
        onSetSelected = null
    }

}
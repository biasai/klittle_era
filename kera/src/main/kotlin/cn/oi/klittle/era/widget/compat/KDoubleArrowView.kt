package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.ViewGroup
import cn.oi.klittle.era.entity.widget.compat.KDiamondEntity
import cn.oi.klittle.era.entity.widget.compat.KDoubleArrowEntity

//fixme 左右水平，双击箭头控件
class KDoubleArrowView : KView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    override fun draw2Last(canvas: Canvas, paint: Paint) {
        super.draw2Last(canvas, paint)
        currentDoubleArrow?.let {

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
    }

}
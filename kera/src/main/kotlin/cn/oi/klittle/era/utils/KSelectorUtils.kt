package cn.oi.klittle.era.utils

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.*
import android.os.Build
import android.view.View
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.TextView
import cn.oi.klittle.era.comm.kpx
import java.lang.Exception

/**
 * fixme 用代码来实现selector 选择器 需要手动设置View的isSelected才会有选中效果。
 * fixme Chekbox和RadioButton 选中按钮自动设置了isSelected,所以不需要手动设置。
 *
 * fixme isSelected=true true 选中，false 未选中。可以通过代码设置
 * fixme isSelectable 所有的View都具备select选中能力,即文本框可以复制粘贴。
 *
 * fixme 自己定的优先级：鼠标按下(点击)>鼠标悬浮>聚焦>选中>一般状态  【这个是我add的顺序，状态的优先级，由drawable.addState()顺序决定。最先添加的优先级最高。】
 * fixme 鼠标点击会改变控件聚焦状态（一般都是失去焦点），但不会改变控件的选中状态。
 * fixme view?.isFocusableInTouchMode = true,鼠标点击时能够正确获取焦点。(第一次点击时，会先获取焦点。不会触发点击事件。只有获取焦点之后，才会触发点击事件。即没有焦点时，要点击两次才能触发点击事件。)
 * fixme view?.isFocusableInTouchMode=false//鼠标点击任何区域，都会失去焦点。点击自己也会失去焦点。
 * fixme requestFocusFromTouch()会获取焦点。与isFocusableInTouchMode属性设置没有关系。要想点击时候主动获取焦点。还是要手动调用requestFocusFromTouch()
 * fixme 鼠标悬浮，即鼠标停留(目前模拟器测不出效果，为了以防万一。还是加上。)
 */
object KSelectorUtils {

    fun selectorRippleDrawable(view: View, NormalColor: String, RipperColor: String, SelectColor: String? = null, FocuseColor: String? = null, HoverColor: String? = RipperColor, strokeWidth: Int = 0, strokeColor: Int = Color.TRANSPARENT, dashWidth: Float = 0F, dashGap: Float = 0F, all_radius: Float = 0f, left_top: Float = all_radius, right_top: Float = all_radius, right_bottom: Float = all_radius, left_bottom: Float = all_radius) {
        selectorRippleDrawable(view, parseColor(NormalColor)!!, parseColor(RipperColor)!!, parseColor(SelectColor), parseColor(FocuseColor), parseColor(HoverColor), strokeWidth, strokeColor, dashWidth, dashGap, all_radius, left_top, right_top, right_bottom, left_bottom)
    }

    //fixme 如：KSelectorUtils.selectorRippleDrawable(this, Color.WHITE, Color.parseColor("#EFF3F6")) 这个颜色值要好看点。
    //KSelectorUtils.selectorRippleDrawable(this, Color.WHITE, Color.parseColor("#C0C0C0"))
    /**
     * fixme 波纹点击本身就是一种按下的效果。所以在此波纹颜色取代点击颜色。
     * fixme 聚焦和悬浮时，并不会有波纹效果（波纹效果一定是点击时才会发生）仅仅只是被盖上了一层遮罩层而已。
     * @param view 组件
     * @param NormalColor 正常背景颜色
     * @param SelectColor 选中时颜色
     * @param FocuseColor 聚焦时颜色
     * @param HoverColor 鼠标悬浮时颜色
     * @param RipperColor 点击时波纹颜色,不可为空
     * @param strokeWidth 边框宽度
     * @param strokeColor 边框颜色
     * @param dashWidth 虚线宽度
     * @param dashGap 虚线间隔
     * @param all_radius 所有圆角度数
     * @param left_top 左上角圆角
     * @param right_top 右上角圆角
     * @param right_bottom 右下角圆角
     * @param left_bottom 左下角圆角
     */
    fun selectorRippleDrawable(view: View, NormalColor: Int, RipperColor: Int, SelectColor: Int? = null, FocuseColor: Int? = null, HoverColor: Int? = RipperColor, strokeWidth: Int = 0, strokeColor: Int = Color.TRANSPARENT, dashWidth: Float = 0F, dashGap: Float = 0F, all_radius: Float = 0f, left_top: Float = all_radius, right_top: Float = all_radius, right_bottom: Float = all_radius, left_bottom: Float = all_radius) {

        try {
            //fixme 鼠标悬浮
            var hoverGradientDrawable = GradientDrawable()
            hoverGradientDrawable?.apply {
                //fixme 圆角
                cornerRadii = floatArrayOf(left_top, left_top, right_top, right_top, right_bottom, right_bottom, left_bottom, left_bottom)
                //边框大小和边框颜色
                if (strokeWidth > 0 && strokeColor != Color.TRANSPARENT) {
                    setStroke(strokeWidth.toInt(), strokeColor, dashWidth, dashGap)
                }
                //背景颜色
                HoverColor?.let {
                    if (it != Color.TRANSPARENT) {
                        setColor(it)
                    }
                }
                if (HoverColor == null) {
                    setColor(NormalColor)
                }
            }
            //fixme 聚焦
            var focuseGradientDrawable = GradientDrawable()
            focuseGradientDrawable?.apply {
                //fixme 圆角
                cornerRadii = floatArrayOf(left_top, left_top, right_top, right_top, right_bottom, right_bottom, left_bottom, left_bottom)
                //边框大小和边框颜色
                if (strokeWidth > 0 && strokeColor != Color.TRANSPARENT) {
                    setStroke(strokeWidth.toInt(), strokeColor, dashWidth, dashGap)
                }
                //背景颜色
                FocuseColor?.let {
                    if (it != Color.TRANSPARENT) {
                        setColor(it)
                    }
                }
                if (FocuseColor == null) {
                    setColor(NormalColor)
                }
            }
            //fixme 选中
            var selectGradientDrawable = GradientDrawable()
            selectGradientDrawable?.apply {
                //fixme 圆角
                cornerRadii = floatArrayOf(left_top, left_top, right_top, right_top, right_bottom, right_bottom, left_bottom, left_bottom)
                //边框大小和边框颜色
                if (strokeWidth > 0 && strokeColor != Color.TRANSPARENT) {
                    setStroke(strokeWidth.toInt(), strokeColor, dashWidth, dashGap)
                }
                //背景颜色
                SelectColor?.let {
                    if (it != Color.TRANSPARENT) {
                        setColor(it)
                    }
                }
                if (SelectColor == null) {
                    setColor(NormalColor)
                }
            }
            //fixme 正常
            var normalGradientDrawable = GradientDrawable()
            normalGradientDrawable?.apply {
                //fixme 圆角
                cornerRadii = floatArrayOf(left_top, left_top, right_top, right_top, right_bottom, right_bottom, left_bottom, left_bottom)
                //边框大小和边框颜色
                if (strokeWidth > 0 && strokeColor != Color.TRANSPARENT) {
                    setStroke(strokeWidth.toInt(), strokeColor, dashWidth, dashGap)
                }
                //背景颜色
                NormalColor?.let {
                    if (it != Color.TRANSPARENT) {
                        setColor(it)
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= 21) {//5.0以上才支持波纹效果
//            //fixme 鼠标悬停
//            var hoverRippleDrawable = RippleDrawable(
//                    ColorStateList.valueOf(RipperColor),//波纹颜色
//                    hoverGradientDrawable,//控制波纹范围
//                    null
//            )
//            //fixme 聚焦（没有波纹效果，仅仅只是被盖上了一层遮罩层）
//            var focuseRippleDrawable = RippleDrawable(
//                    ColorStateList.valueOf(RipperColor),//波纹颜色
//                    focuseGradientDrawable,//控制波纹范围
//                    null
//            )
                //fixme 选中
                var selectRippleDrawable = RippleDrawable(
                        ColorStateList.valueOf(RipperColor),//波纹颜色
                        selectGradientDrawable,//控制波纹范围
                        null
                )
                //fixme 普通状态
                var normalRippleDrawable = RippleDrawable(
                        ColorStateList.valueOf(RipperColor),//波纹颜色
                        normalGradientDrawable,//控制波纹范围
                        null
                )
                view.isClickable = true//具体点击能力,必不可少
                //防止与触摸状态冲突。去掉触摸点击状态（波纹效果本身就是一种点击效果）。
                //selectorDrawable(view, normalRippleDrawable, null, selectRippleDrawable, focuseRippleDrawable, hoverRippleDrawable)
                selectorDrawable(view, normalRippleDrawable, null, selectRippleDrawable, focuseGradientDrawable, hoverGradientDrawable)
            } else {
                //fixme 按下（波纹点击颜色，就是按下的颜色）
                var pressGradientDrawable = GradientDrawable()
                pressGradientDrawable?.apply {
                    //fixme 圆角
                    cornerRadii = floatArrayOf(left_top, left_top, right_top, right_top, right_bottom, right_bottom, left_bottom, left_bottom)
                    //边框大小和边框颜色
                    if (strokeWidth > 0 && strokeColor != Color.TRANSPARENT) {
                        setStroke(strokeWidth.toInt(), strokeColor, dashWidth, dashGap)
                    }
                    //背景颜色
                    RipperColor?.let {
                        if (it != Color.TRANSPARENT) {
                            setColor(it)
                        }
                    }
                }
                //点击一般效果，5.0以下不支持波纹。
                selectorDrawable(view, normalGradientDrawable, pressGradientDrawable, selectGradientDrawable, focuseGradientDrawable, hoverGradientDrawable)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            KLoggerUtils.e("selectorRippleDrawable异常：\t" + e.message,isLogEnable = true)
        }

    }


    fun selectorDrawable(view: View, NormalColor: String, PressColor: String, SelectColor: String? = PressColor, FocuseColor: String? = null, HoverColor: String? = PressColor, strokeWidth: Int = 0, strokeColor: Int = Color.TRANSPARENT, dashWidth: Float = 0F, dashGap: Float = 0F, all_radius: Float = 0f, left_top: Float = all_radius, right_top: Float = all_radius, right_bottom: Float = all_radius, left_bottom: Float = all_radius) {
        selectorDrawable(view, parseColor(NormalColor)!!, parseColor(PressColor)!!, parseColor(SelectColor), parseColor(FocuseColor), parseColor(HoverColor), strokeWidth, strokeColor, dashWidth, dashGap, all_radius, left_top, right_top, right_bottom, left_bottom)
    }

    /**
     * fixme 圆角边框Drawable
     * fixme 聚焦和悬浮时，并不会有波纹效果（波纹效果一定是点击时才会发生）仅仅只是被盖上了一层遮罩层而已。
     * @param view 组件
     * @param NormalColor 正常背景颜色
     * @param SelectColor 选中时颜色
     * @param FocuseColor 聚焦时颜色
     * @param HoverColor 鼠标悬浮时颜色
     * @param RipperColor 点击时波纹颜色,不可为空
     * @param strokeWidth 边框宽度
     * @param strokeColor 边框颜色
     * @param dashWidth 虚线宽度
     * @param dashGap 虚线间隔
     * @param all_radius 所有圆角度数
     * @param left_top 左上角圆角
     * @param right_top 右上角圆角
     * @param right_bottom 右下角圆角
     * @param left_bottom 左下角圆角
     */
    fun selectorDrawable(view: View, NormalColor: Int, PressColor: Int, SelectColor: Int? = PressColor, FocuseColor: Int? = null, HoverColor: Int? = PressColor, strokeWidth: Int = 0, strokeColor: Int = Color.TRANSPARENT, dashWidth: Float = 0F, dashGap: Float = 0F, all_radius: Float = 0f, left_top: Float = all_radius, right_top: Float = all_radius, right_bottom: Float = all_radius, left_bottom: Float = all_radius) {
        //fixme 按下
        var pressGradientDrawable = GradientDrawable()
        pressGradientDrawable?.apply {
            //fixme 圆角
            cornerRadii = floatArrayOf(left_top, left_top, right_top, right_top, right_bottom, right_bottom, left_bottom, left_bottom)
            //边框大小和边框颜色
            if (strokeWidth > 0 && strokeColor != Color.TRANSPARENT) {
                setStroke(strokeWidth.toInt(), strokeColor, dashWidth, dashGap)
            }
            //背景颜色
            PressColor?.let {
                if (it != Color.TRANSPARENT) {
                    setColor(it)
                }
            }
            if (HoverColor == null) {
                setColor(NormalColor)
            }
        }
        //fixme 鼠标悬浮
        var hoverGradientDrawable = GradientDrawable()
        hoverGradientDrawable?.apply {
            //fixme 圆角
            cornerRadii = floatArrayOf(left_top, left_top, right_top, right_top, right_bottom, right_bottom, left_bottom, left_bottom)
            //边框大小和边框颜色
            if (strokeWidth > 0 && strokeColor != Color.TRANSPARENT) {
                setStroke(strokeWidth.toInt(), strokeColor, dashWidth, dashGap)
            }
            //背景颜色
            HoverColor?.let {
                if (it != Color.TRANSPARENT) {
                    setColor(it)
                }
            }
            if (HoverColor == null) {
                setColor(NormalColor)
            }
        }
        //fixme 聚焦
        var focuseGradientDrawable = GradientDrawable()
        focuseGradientDrawable?.apply {
            //fixme 圆角
            cornerRadii = floatArrayOf(left_top, left_top, right_top, right_top, right_bottom, right_bottom, left_bottom, left_bottom)
            //边框大小和边框颜色
            if (strokeWidth > 0 && strokeColor != Color.TRANSPARENT) {
                setStroke(strokeWidth.toInt(), strokeColor, dashWidth, dashGap)
            }
            //背景颜色
            FocuseColor?.let {
                if (it != Color.TRANSPARENT) {
                    setColor(it)
                }
            }
            if (FocuseColor == null) {
                setColor(NormalColor)
            }
        }
        //fixme 选中
        var selectGradientDrawable = GradientDrawable()
        selectGradientDrawable?.apply {
            //fixme 圆角
            cornerRadii = floatArrayOf(left_top, left_top, right_top, right_top, right_bottom, right_bottom, left_bottom, left_bottom)
            //边框大小和边框颜色
            if (strokeWidth > 0 && strokeColor != Color.TRANSPARENT) {
                setStroke(strokeWidth.toInt(), strokeColor, dashWidth, dashGap)
            }
            //背景颜色
            SelectColor?.let {
                if (it != Color.TRANSPARENT) {
                    setColor(it)
                }
            }
            if (SelectColor == null) {
                setColor(NormalColor)
            }
        }
        //fixme 正常
        var normalGradientDrawable = GradientDrawable()
        normalGradientDrawable?.apply {
            //fixme 圆角
            cornerRadii = floatArrayOf(left_top, left_top, right_top, right_top, right_bottom, right_bottom, left_bottom, left_bottom)
            //边框大小和边框颜色
            if (strokeWidth > 0 && strokeColor != Color.TRANSPARENT) {
                setStroke(strokeWidth.toInt(), strokeColor, dashWidth, dashGap)
            }
            //背景颜色
            NormalColor?.let {
                if (it != Color.TRANSPARENT) {
                    setColor(it)
                }
            }
        }
        selectorDrawable(view, normalGradientDrawable, pressGradientDrawable, selectGradientDrawable, focuseGradientDrawable, hoverGradientDrawable)
    }


    /**
     * NormalDrawable 默认状态
     * PressDrawable 按下状态
     * SelectDrawable 选中状态
     * FocuseDrawable 聚焦状态
     * HoverDrawable 鼠标悬浮状态
     */
    //fixme 添加了聚焦状态+悬浮状态
    fun selectorDrawable(view: View?, NormalDrawable: Drawable, PressDrawable: Drawable?, SelectDrawable: Drawable? = PressDrawable, FocuseDrawable: Drawable? = null, HoverDrawable: Drawable? = PressDrawable) {
        view?.let {
            val drawable = StateListDrawable()
            //fixme - 表示fasle
            //按下
            PressDrawable?.let {
                drawable.addState(intArrayOf(android.R.attr.state_pressed), it)
                view.isClickable = true//具体点击能力
            }
            //鼠标悬浮
            HoverDrawable?.let {
                drawable.addState(intArrayOf(android.R.attr.state_hovered), it)
                drawable.addState(intArrayOf(android.R.attr.state_drag_hovered), it)
            }
            //聚焦
            FocuseDrawable?.let {
                drawable.addState(intArrayOf(android.R.attr.state_focused), it)
                view?.isFocusable = true
                //true鼠标点击时能够正确获取焦点。(第一次点击时，会先获取焦点。不会触发点击事件。只有获取焦点之后，才会触发点击事件。)
                //view?.isFocusableInTouchMode = true
                view?.isFocusableInTouchMode = false//鼠标点击任何区域，都会失去焦点。点击自己也会失去焦点。
            }
            //选中
            SelectDrawable?.let {
                drawable.addState(intArrayOf(android.R.attr.state_checked), it)
                drawable.addState(intArrayOf(android.R.attr.state_selected), it)
            }
            //未选中 + 未按下 (也就是一般状态)
            NormalDrawable?.let {
                drawable.addState(intArrayOf(-android.R.attr.state_checked), it)
                drawable.addState(intArrayOf(-android.R.attr.state_selected), it)
            }
            if (view is CheckBox) {//多选框
                view.buttonDrawable = drawable
            } else if (view is RadioButton) {//单选框
                view.buttonDrawable = drawable
            } else {//一般View
                view.setBackgroundDrawable(drawable)
            }
        }
    }

    //fixme 添加了聚焦状态+悬浮状态
    fun selectorDrawable(view: View?, NormalID: Int, PressID: Int?, SelectID: Int? = PressID, FocuseID: Int? = null, HoverID: Int? = PressID) {
        selectorDrawable(view, parseDrawable(NormalID)!!, parseDrawable(PressID), parseDrawable(SelectID), parseDrawable(FocuseID), parseDrawable(HoverID))
    }

    /**
     * NormalBtmap 默认背景位图
     * PressBitmap 按下时背景位图
     * SelectBitmap 选中(默认和按下相同)时背景位图
     * FocuseBitmap 聚焦时图片
     * HoverBitmap 鼠标悬浮时图片
     */
    //fixme 添加了聚焦状态+悬浮状态
    fun selectorBitmap(view: View?, NormalBtmap: Bitmap?, PressBitmap: Bitmap?, SelectBitmap: Bitmap? = PressBitmap, FocuseBitmap: Bitmap? = null, HoverBitmap: Bitmap? = PressBitmap) {
        view?.let {
            val drawable = StateListDrawable()
            //fixme - 表示fasle
            view.isClickable = true//具体点击能力
            //按下
            var drawablePress: BitmapDrawable? = null
            PressBitmap?.let {
                drawablePress = BitmapDrawable(PressBitmap)
            }
            drawablePress?.let {
                drawable.addState(intArrayOf(android.R.attr.state_pressed), drawablePress)
            }
            //鼠标悬浮，即鼠标停留(目前模拟器测不出效果，为了以防万一。还是加上。)
            var drawableHover: BitmapDrawable? = null
            HoverBitmap?.let {
                drawableHover = BitmapDrawable(it)
            }
            drawableHover?.let {
                drawable.addState(intArrayOf(android.R.attr.state_hovered), it)
                drawable.addState(intArrayOf(android.R.attr.state_drag_hovered), it)
            }
            //聚焦
            var drawableFocuse: BitmapDrawable? = null
            FocuseBitmap?.let {
                drawableFocuse = BitmapDrawable(it)
            }
            drawableFocuse?.let {
                drawable.addState(intArrayOf(android.R.attr.state_focused), drawableFocuse)
                view?.isFocusable = true
                //true鼠标点击时能够正确获取焦点。(第一次点击时，会先获取焦点。不会触发点击事件。只有获取焦点之后，才会触发点击事件。)
                //view?.isFocusableInTouchMode = true
                view?.isFocusableInTouchMode = false//鼠标点击任何区域，都会失去焦点。点击自己也会失去焦点。
            }
            //选中
            var drawableSelect: BitmapDrawable? = null
            SelectBitmap?.let {
                drawableSelect = BitmapDrawable(SelectBitmap)
            }
            drawableSelect?.let {
                drawable.addState(intArrayOf(android.R.attr.state_checked), drawableSelect)
                drawable.addState(intArrayOf(android.R.attr.state_selected), drawableSelect)
            }
            //未选中 + 未按下 (也就是一般状态)
            var drawableNormal: BitmapDrawable? = null
            NormalBtmap?.let {
                drawableNormal = BitmapDrawable(NormalBtmap)
            }
            drawableNormal?.let {
                drawable.addState(intArrayOf(-android.R.attr.state_checked), drawableNormal)
                drawable.addState(intArrayOf(-android.R.attr.state_selected), drawableNormal)
            }
            //fixme 图片就使用以下方法。
            if (view is CheckBox) {//多选框
                view.buttonDrawable = drawable
            } else if (view is RadioButton) {//单选框
                view.buttonDrawable = drawable
            } else {//一般View
                view.setBackgroundDrawable(drawable)//如果是背景颜色就直接使用这个即可。
            }
        }
    }

    //fixme 添加了聚焦状态+悬浮状态
    fun selectorBitmap(view: View?, NormalID: Int, PressID: Int?, SelectID: Int? = PressID, FocuseID: Int? = null, HoverID: Int? = PressID) {
        selectorBitmap(view, parseBitmap(NormalID), parseBitmap(PressID), parseBitmap(SelectID), parseBitmap(FocuseID), parseBitmap(HoverID))
    }


    /**
     * NormalColor fixme 正常背景颜色值,不能为空。防止kotlin分辨不出方法类型。
     * PressColor  按下正常背景颜色值
     * SelectColor 选中(默认和按下相同)背景颜色值
     * FocuseColor 聚焦状态,为了防止冲突，默认设置为空。
     * HoverColor 鼠标悬浮状态，目前模拟器测试不出来。为了以防万一，还是加上
     */
    //fixme 添加了聚焦状态+悬浮状态
    fun selectorColor(view: View?, NormalColor: Int, PressColor: Int?, SelectColor: Int? = PressColor, FocuseColor: Int? = null, HoverColor: Int? = PressColor) {
        view?.let {
            val drawable = StateListDrawable()
            //fixme - 表示fasle
            //fixme 状态的优先级，由drawable.addState()顺序决定。最先添加的优先级最高。
            //按下
            var drawablePress: ColorDrawable? = null
            PressColor?.let {
                drawablePress = ColorDrawable(PressColor)
                view.isClickable = true//具体点击能力
            }
            drawablePress?.let {
                drawable.addState(intArrayOf(android.R.attr.state_pressed), drawablePress)
                view?.isClickable = true
            }
            //鼠标悬浮，即鼠标停留(目前模拟器测不出效果，为了以防万一。还是加上。)
            var drawableHover: ColorDrawable? = null
            HoverColor?.let {
                drawableHover = ColorDrawable(it)
            }
            drawableHover?.let {
                drawable.addState(intArrayOf(android.R.attr.state_hovered), it)
                drawable.addState(intArrayOf(android.R.attr.state_drag_hovered), it)
            }
            //聚焦
            var drawableFocuse: ColorDrawable? = null
            FocuseColor?.let {
                drawableFocuse = ColorDrawable(FocuseColor)
            }
            drawableFocuse?.let {
                drawable.addState(intArrayOf(android.R.attr.state_focused), drawableFocuse)
                view?.isFocusable = true
                //true鼠标点击时能够正确获取焦点。(第一次点击时，会先获取焦点。不会触发点击事件。只有获取焦点之后，才会触发点击事件。)
                //view?.isFocusableInTouchMode = true
                view?.isFocusableInTouchMode = false//鼠标点击任何区域，都会失去焦点。点击自己也会失去焦点。
            }
            //选中
            var drawableSelect: ColorDrawable? = null
            SelectColor?.let {
                drawableSelect = ColorDrawable(SelectColor)
            }
            drawableSelect?.let {
                drawable.addState(intArrayOf(android.R.attr.state_checked), drawableSelect)
                drawable.addState(intArrayOf(android.R.attr.state_selected), drawableSelect)
            }
            //未选中 + 未按下 (也就是一般状态)
            var drawableNormal: ColorDrawable? = null
            NormalColor?.let {
                drawableNormal = ColorDrawable(NormalColor)
            }
            drawableNormal?.let {
                drawable.addState(intArrayOf(-android.R.attr.state_checked), drawableNormal)
                drawable.addState(intArrayOf(-android.R.attr.state_selected), drawableNormal)
                drawable.addState(intArrayOf(-android.R.attr.state_pressed), drawableNormal)
                drawable.addState(intArrayOf(-android.R.attr.state_focused), drawableNormal)
                drawable.addState(intArrayOf(-android.R.attr.state_hovered), drawableNormal)
                drawable.addState(intArrayOf(-android.R.attr.state_drag_hovered), drawableNormal)
            }
            view.setBackgroundDrawable(drawable)
        }
    }

    //fixme 添加了聚焦状态+悬浮状态
    fun selectorColor(view: View?, NormalColor: String, PressColor: String?, SelectColor: String? = PressColor, FocuseColor: String? = null, HoverColor: String? = PressColor) {
        selectorColor(view, parseColor(NormalColor)!!, parseColor(PressColor), parseColor(SelectColor), parseColor(FocuseColor), parseColor(HoverColor))
    }

    /**
     * NormalColor 正常字体颜色值
     * PressColor  按下正常字体颜色值
     * SelectColor 选中(默认和按下相同)字体颜色值
     * FocuseColor 聚焦状态,为了防止冲突，默认设置为空。
     * HoverColor 鼠标悬浮状态，目前模拟器测试不出来。为了以防万一，还是加上
     */
    //fixme 添加了聚焦状态+悬浮状态
    fun selectorTextColor(view: View?, NormalColor: Int, PressColor: Int?, SelectColor: Int? = PressColor, FocuseColor: Int? = null, HoverColor: Int? = PressColor) {
        view?.let {
            var colors = mutableListOf<Int>()
            var states = mutableListOf<IntArray>()
            //fixme 状态的优先级，依然由states.add顺序决定。最先添加的优先级最高。
            //按下
            PressColor?.let {
                colors.add(it)
                states.add(intArrayOf(android.R.attr.state_pressed))
                view.isClickable = true//具体点击能力
            }
            //悬浮
            HoverColor?.let {
                colors.add(it)
                states.add(intArrayOf(android.R.attr.state_hovered))
                colors.add(it)
                states.add(intArrayOf(android.R.attr.state_drag_hovered))
            }
            //聚焦
            FocuseColor?.let {
                colors.add(it)
                states.add(intArrayOf(android.R.attr.state_focused))
                view?.isFocusable = true
                //true鼠标点击时能够正确获取焦点。(第一次点击时，会先获取焦点。不会触发点击事件。只有获取焦点之后，才会触发点击事件。)
                view?.isFocusableInTouchMode = true
                view?.isFocusableInTouchMode = false//鼠标点击任何区域，都会失去焦点。点击自己也会失去焦点。
            }
            //选中
            SelectColor?.let {
                colors.add(it)
                states.add(intArrayOf(android.R.attr.state_checked))
                colors.add(it)
                states.add(intArrayOf(android.R.attr.state_selected))
            }
            //正常
            NormalColor?.let {
                colors.add(it)
                states.add(intArrayOf(-android.R.attr.state_checked))
            }
            if (states.size > 0 && colors.size > 0 && states.size == colors.size) {
                var colorStateList = ColorStateList(states.toTypedArray(), colors.toIntArray())
                view.isClickable = true//具体点击能力
                if (view is TextView) {
                    view.setTextColor(colorStateList)
                }
            }
        }
    }


    //fixme 添加了聚焦状态+悬浮状态
    fun selectorTextColor(view: View?, NormalColor: String, PressColor: String?, SelectColor: String? = PressColor, FocuseColor: String? = null, HoverColor: String? = null) {
        selectorTextColor(view, parseColor(NormalColor)!!, parseColor(PressColor), parseColor(SelectColor), parseColor(FocuseColor), parseColor(HoverColor))
    }

    //获取颜色值
    fun parseColor(colorString: String?): Int? {
        colorString?.let {
            return Color.parseColor(it)
        }
        return null
    }

    //获取位图
    fun parseBitmap(resID: Int?): Bitmap? {
        resID?.let {
            return KAssetsUtils.getInstance().getBitmapFromAssets(null, it, true)
        }
        return null
    }

    //获取Drawable对象
    fun parseDrawable(resID: Int?): Drawable? {
        resID?.let {
            return kpx.context()?.resources?.getDrawable(it)
        }
        return null
    }

    //KSelectorUtils.getGradientDrawable("#00FFFF", "#0000FFFF")
    fun getGradientDrawable(vararg gradientColors: String, orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.TOP_BOTTOM, bgColor: Int = Color.TRANSPARENT, strokeWidth: Int = 0, strokeColor: Int = Color.TRANSPARENT, dashWidth: Float = 0F, dashGap: Float = 0F, all_radius: Float = 0f, left_top: Float = all_radius, right_top: Float = all_radius, right_bottom: Float = all_radius, left_bottom: Float = all_radius): GradientDrawable {
        var gradientColors2= mutableListOf<Int>()
        gradientColors.forEach {
            gradientColors2.add(Color.parseColor(it))
        }
        gradientColors2.toIntArray()//动态数组，转变为固定数组
        return getGradientDrawable(gradientColors = *gradientColors2.toIntArray(), orientation = orientation, bgColor = bgColor, strokeWidth = strokeWidth, strokeColor = strokeColor, dashWidth = dashWidth, dashGap = dashGap, all_radius = all_radius, left_top = left_top, right_top = right_top, right_bottom = right_bottom, left_bottom = left_bottom)
    }

    /**
     * 调用案例：
     * KSelectorUtils.getGradientDrawable(Color.parseColor("#00FFFF"), Color.TRANSPARENT) //fixme 这个在低版本(7.0及以下)会有黑色渐变；不好看。因为 Color.TRANSPARENT 默认是 Color.parseColor("#00000000")；会有黑色的颜色值。【高版本不会有这个问题。】
     * KSelectorUtils.getGradientDrawable(Color.parseColor("#00FFFF"), Color.parseColor("#0000FFFF"))//fixme 这样渐变才平滑
     * 获取渐变色GradientDrawable
     * @param gradientColors 颜色渐变数组
     * @param orientation 是个枚举；控制渐变方向;默认从上往下 TOP_BOTTOM
     * @param bgColor 背景色；渐变色为空的时候；有效。
     * @param strokeWidth 边框宽度
     * @param strokeColor 边框颜色
     * @param dashWidth 虚线宽度
     * @param dashGap 虚线间隔
     * @param all_radius 所有圆角度数
     * @param left_top 左上角圆角
     * @param right_top 右上角圆角
     * @param right_bottom 右下角圆角
     * @param left_bottom 左下角圆角
     */
    fun getGradientDrawable(vararg gradientColors: Int, orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.TOP_BOTTOM, bgColor: Int = Color.TRANSPARENT, strokeWidth: Int = 0, strokeColor: Int = Color.TRANSPARENT, dashWidth: Float = 0F, dashGap: Float = 0F, all_radius: Float = 0f, left_top: Float = all_radius, right_top: Float = all_radius, right_bottom: Float = all_radius, left_bottom: Float = all_radius): GradientDrawable {
        var normalGradientDrawable: GradientDrawable
        if (gradientColors != null && gradientColors.size > 0) {
            normalGradientDrawable = GradientDrawable(orientation, gradientColors)
        } else {
            normalGradientDrawable = GradientDrawable()
        }
        normalGradientDrawable?.apply {
            //fixme 圆角
            cornerRadii = floatArrayOf(left_top, left_top, right_top, right_top, right_bottom, right_bottom, left_bottom, left_bottom)
            //边框大小和边框颜色
            if (strokeWidth > 0 && strokeColor != Color.TRANSPARENT) {
                setStroke(strokeWidth.toInt(), strokeColor, dashWidth, dashGap)
            }
            //fixme 背景颜色[注意会覆盖渐变色的；如果有背景颜色；渐变颜色将无效。]
            bgColor?.let {
                if (it != Color.TRANSPARENT) {
                    setColor(it)
                }
            }

        }
        return normalGradientDrawable
    }

}
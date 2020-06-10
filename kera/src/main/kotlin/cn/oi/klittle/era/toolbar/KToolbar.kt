package cn.oi.klittle.era.toolbar

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseActivityManager
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.utils.KSelectorUtils
import cn.oi.klittle.era.widget.KGradientView
import cn.oi.klittle.era.widget.compat.KTextView
import cn.oi.klittle.era.widget.compat.KVerticalLayout
import org.jetbrains.anko.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.Deferred

//            fixme 使用案例
//        setStatusBarColor(Color.parseColor("#186FB5"))//状态栏颜色
//        var kToolbar: KToolbar? = null
//        verticalLayout {
//            backgroundColor = Color.WHITE
//            kToolbar = KToolbar(this, getActivity())?.apply {
//                //标题栏背景色
//                contentView?.apply {
//                    inner {
//                        radius {
//                            bgHorizontalColors(Color.parseColor("#0078D7"), Color.parseColor("#1A86DB"))
//                        }
//                    }
//                }
//                //左边返回文本（默认样式自带一个白色的返回图标）
//                leftTextView?.apply {
//                    text = "返回"
//                    leftPadding = kpx.x(60)
//                    //layoutParams()需要具体的数值才有效，matchparent等无效
//                    layoutParams(width = kpx.x(150), height = toolbarHeight)
//                }
//                //中间文本
//                titleTextView?.apply {
//                    text = "标题"
//                }

//                标题的右边文本
//                titleTextView2?.apply {
//                    text = "2"
//                    radius {
//                        all_radius(100)
//                        bg_color = Color.RED
//                        textSize = kpx.textSizeX(24f)
//                    }
//                    var w = getTextWidth() + kpx.x(24)
//                    layoutParams(w, w)
//                    layoutParams?.let {
//                        if (it is RelativeLayout.LayoutParams) {
//                            (it as RelativeLayout.LayoutParams).apply {
//                                topMargin = kpx.x(18)
//                            }
//                        }
//                    }
//                }

//                //右边文本
//                rightTextView?.apply {
//                    text = "确定"
//                    rightPadding = toolbarOffset
//                    var w = getTextWidth() + rightPadding * 2
//                    layoutParams(w, toolbarHeight)
//                }
//                //右边数,倒数第二个文本框
//                rightTextView2?.apply {
//                    text = "取消"
//                    rightPadding = toolbarOffset
//                    var w = getTextWidth() + rightPadding * 2
//                    layoutParams(w, toolbarHeight)
//                }
//                //底部线条颜色（阴影）
//                bottomGradientView?.apply {
//                }
//            }


//            view {
//                backgroundColor = Color.WHITE
//                if (Build.VERSION.SDK_INT >= 21) {
//                    //默认就是0，可以不用设置
//                    elevation=0f
//                    translationZ=0f
//                }
//            }.lparams {
//                width = matchParent
//                height = kpx.x(100)
//                //fixme 线性布局支持外部丁为负数,弥补阴影的高度;
//                //fixme 5.0以下，会遮挡住阴影（无法更改上下重叠问题，5.0以后，才有Z轴的概念。）
//                //topMargin = -kpx.x(24)
//                kToolbar?.let {
//                    fixme 现在已经不需要手动设置topMargin顶部负补定了，kToolbar的parentView已经设置了bottomMargin底部负补定了(效果是一样的，亲测有效)。
//                    topMargin = -it.bottomGradientView?.h!!//可以获取阴影的高度
//                    //topMargin = -Toolbar.getShadowHeight()
//                }
//            }
//        }
//fixme KToobar里的控件基本都设置了 isRecycleAutoBg=false；不释放位图。
/**
 * 顶部标题栏
 */
open class KToolbar {

    companion object {
        fun getActivity(): Activity? {
            return KBaseActivityManager.getInstance().stackTopActivity
        }

        //获取间距
        fun getOffset(): Int {
            return kpx.x(24)
        }

        //获取标题栏高度
        fun getHeight(): Int {
            return kpx.y(88)
        }

        //标题栏字体大小
        fun getTextSize(): Float {
            return kpx.textSizeY(32,false)
        }

        //获取标题栏默认字体颜色
        fun getTextColor(): Int {
            return Color.WHITE
        }

        //获取阴影线高度
        fun getShadowHeight(): Int {
            //KLoggerUtils.e("分辨率宽度：\t" + kpx.screenWidth())
            if (kpx.screenWidth() >= 720) {
                //现在主流分辨率基本都是1080x1920的。720都是很老的了。
                return kpx.x(24)//分辨率高的设备上，阴影大一点效果较好。太大了也不好。24刚刚好。
            } else {
                return kpx.x(12)//太大，太小效果都不好。12在低分辨率和高分辨率效果都还行，24在低版本低分辨率上效果不行。
            }
        }

        //获取阴影颜色
        fun getShadowColor(): Int {
            if (kpx.screenWidth() >= 720) {
                return Color.parseColor("#70000000")//高分辨率下，颜色稍微深一点。
            } else {
                return Color.parseColor("#50000000")//低分辨率颜色浅一点较好。
            }
        }

    }

    //与左边或右边的间距
    open var toolbarOffset = getOffset()
    //标题栏高度
    open var toolbarHeight = getHeight()
    //标题栏，字体大小
    open var toolbarTextSize = getTextSize()
    //默认字体颜色
    open var toolbarTextColor = getTextColor()
    //阴影线高度
    open var toolbarShadowHeight = getShadowHeight()
    //阴影线颜色
    open var toolbarShadowColor = getShadowColor()

    private var hasShadow: Boolean = true

    /**
     * @param hasShadow 是否有影响
     */
    constructor(viewGroup: ViewGroup?, activity: Activity? = getActivity(), hasShadow: Boolean = true) {
        this.hasShadow = hasShadow
        //自动添加到当前布局中。
        viewGroup?.addView(getToolbar(activity).parentView)
        //fixme 添加到viewGroup之后，才会有layoutParams
        if (hasShadow) {
            parentView?.apply {
                layoutParams?.let {
                    if (it is LinearLayout.LayoutParams) {
                        //fixme 低部外补丁为负数，亲测有效，下面的布局会顶上来。弥补阴影的高度间距。
                        //fixme 负补定，在线性布局里面是有效的。
                        it.bottomMargin = -toolbarShadowHeight
                    } else if (it is RelativeLayout.LayoutParams) {
                        //fixme 负补定，在现对布局里面也是有效的。
                        it.bottomMargin = -toolbarShadowHeight
                        GlobalScope.async {
                            activity?.runOnUiThread {
                                //fixme 相对布局里，可以显示在最上面。其他控件不会受影响。
                                //fixme 线性布局就不行。线性布局会改变他的显示添加顺序。
                                bringToFront()
                            }
                        }
                    }
                }
            }
        }
    }

    var parentView: LinearLayout? = null//最外层容器
    var contentView: KVerticalLayout? = null//内容容器
    var leftTextView: KTextView? = null//返回键
    var titleTextView: KTextView? = null//标题
    var titleTextView2: KTextView? = null//标题2(在标题的右上角位置)
    var rightTextView: KTextView? = null//右边文本框
    var rightTextView2: KTextView? = null//右边第二个文本框（从右边数,倒数第二个文本框）
    var bottomGradientView: KGradientView? = null//底部渐变横线


    //fixme 注意，必须是Context上下文。Activity不行。引用时，会报错。
    open fun getToolbar(activity: Activity? = getActivity()): KToolbar {
        var statusHeight = kpx.statusHeight
        if (activity != null) {
            if (!kpx.isStatusBarVisible(activity)) {
                //没有状态栏,即全屏
                statusHeight = 0
            }
            parentView = LinearLayout(activity.baseContext).apply {
                id = kpx.id("parent_view")
                orientation = LinearLayout.VERTICAL//默认垂直方向
                verticalLayout {
                    //fixme 在此最外层控件，必须是线性布局，其他布局会报错。
                    contentView = KVerticalLayout(this).apply {
                        padding = 0
                        backgroundColor=Color.parseColor("#0078D7")//浅蓝色。
//                        inner {
//                            radius {
//                                bgHorizontalColors(Color.parseColor("#0078D7"), Color.parseColor("#1A86DB"))//fixme 默认背景颜色,这个颜色会覆盖 backgroundColor 背景色。
//                            }
//                        }
                        relativeLayout {
                            //返回键
                            leftTextView = KTextView(this).apply {
                                id = kpx.id("txt_left")
                                textSize = toolbarTextSize
                                setTextColor(toolbarTextColor)
                                gravity = Gravity.CENTER or Gravity.LEFT
                                isClickable = true
                                isRecycleAutoBg = false//返回键不释放
                                autoBg {
                                    //                                    width = kpx.x(24)
//                                    height = kpx.x(41)
//                                    autoBg(R.mipmap.kera_top_back_white)//默认白色返回图标
                                    width = kpx.x(60)//和PDA的返回键大小保持一致。
                                    height = width
                                    autoBg(R.mipmap.kera_ic_back)
                                    //autoBgColor = Color.WHITE
                                    isAutoCenterVertical = true
                                    isAutoCenterHorizontal = false
                                    autoLeftPadding = toolbarOffset.toFloat()
                                }
                                onClick {
                                    if (visibility == View.VISIBLE) {
                                        autoBg {
                                            if (isDraw) {
                                                //返回键显示的时候，默认点击关闭
                                                activity?.finish()
                                            }
                                        }
                                    }
                                }
                            }.lparams {
                                //width = kpx.x(80)
                                width = toolbarHeight
                                height = matchParent
                            }
                            //标题
                            titleTextView = KTextView(this).apply {
                                id = kpx.id("txt_center")
                                textSize = toolbarTextSize
                                setTextColor(toolbarTextColor)
                                gravity = Gravity.CENTER
                                isRecycleAutoBg=false
                            }.lparams {
                                width = wrapContent
                                height = wrapContent
                                centerInParent()
                            }

                            //标题2
                            titleTextView2 = KTextView(this).apply {
                                id = kpx.id("txt_center2")
                                textSize = toolbarTextSize
                                setTextColor(toolbarTextColor)
                                gravity = Gravity.CENTER
                                isRecycleAutoBg=false
                            }.lparams {
                                width = wrapContent
                                height = wrapContent
                                rightOf(kpx.id("txt_center"))
                            }
                            //右边文字或图片
                            //[fixme rightTextView和rightTextView2位置有关联。
                            // fixme 所以不要GONE隐藏，但是可以visibility = View.INVISIBLE] 不用GONE;
                            // fixme 不然rightTextView2可能会覆盖在返回键上，挡住返回键点击事件。
                            rightTextView = KTextView(this).apply {
                                id = kpx.id("txt_right")
                                textSize = toolbarTextSize
                                setTextColor(toolbarTextColor)
                                gravity = Gravity.CENTER or Gravity.RIGHT
                                rightPadding = toolbarOffset
                                isRecycleAutoBg=false
                            }.lparams {
                                centerVertically()
                                alignParentRight()
                                width = (toolbarTextSize * 2 + toolbarOffset * 4).toInt()
                                height = wrapContent
                            }
                            //右边数,倒数第二个文字或图片
                            rightTextView2 = KTextView(this).apply {
                                id = kpx.id("txt_right2")
                                textSize = toolbarTextSize
                                setTextColor(toolbarTextColor)
                                gravity = Gravity.CENTER or Gravity.RIGHT
                                rightPadding = toolbarOffset
                                isRecycleAutoBg=false
                            }.lparams {
                                centerVertically()
                                leftOf(kpx.id("txt_right"))
                                width = (toolbarTextSize * 2 + toolbarOffset * 4).toInt()
                                height = wrapContent
                            }
                        }.lparams {
                            width = matchParent
                            height = toolbarHeight
                            topMargin = statusHeight
                        }
                    }.lparams {
                        width = matchParent
                        //height = toolbarHeight + statusHeight//不能为这个高度值，不然顶部会有间隙。
                        height = toolbarHeight + statusHeight + kpx.x(1)//fixme 加上获取减去一个数值就没有间隙了。很神奇的事情。系统Bug
                    }
                    if (hasShadow) {
                        //fixme 底部阴影分割线;在内容布局 contentView 的下面
                        bottomGradientView = KGradientView(this).apply {
                            id = kpx.id("shadow_view_bottom")
                            //verticalColors("#50000000", "#00000000")//这个颜色值效果不错。
                            //backgroundDrawable = resources?.getDrawable(R.drawable.kera_drawable_bottom_shadow)
                            //就使用#60000000这个颜色值，这个颜色值在低分辨率和高分辨率效果都不错。
                            //numStops渐变层为10，个人感觉效果不错。
                            gradientColor(toolbarShadowColor, toolbarShadowHeight)//使用KScrimUtil实现更柔和的渐变色。效果不错。
                        }.lparams {
                            width = matchParent
                            height = toolbarShadowHeight
                        }
                    }

                }

                if (hasShadow && Build.VERSION.SDK_INT >= 21) {
                    //bringToFront()不一定能实现显示在最上面的效果。
                    //线性布局上调用会有很多问题。建议不要使用，相对布局则没有问题。
                    //fixme 能够解决被后面的布局遮挡的问题，数值越大，显示越在前。
                    elevation = 1f
                    translationZ = 1f//默认是0，
                    z = 100f//5.0之后出现的Z轴，数值越大，越显示在前。
                }

//                if (Build.VERSION.SDK_INT >= 21) {
//                    //5.0之后出现的阴影投影；个人感觉效果不怎么样。
//                    //控件必须设置背景色，且不能为透明
//                    //控件与父控件的边界之间需有足够空间绘制出阴影才行
//                    backgroundColor = Color.WHITE//必须要设置背景色(不能是透明色)，不然无效。
//                    //一般控件的颜色和父容器颜色一致时效果才比较明显。
//                    elevation = kpx.x(20f)//主要用于给 View 增加一个高度，可以直接被加在 View 控件上，呈现在界面上，就是一个带阴影的效果。
//                    translationZ = kpx.x(30f)//阴影的实际效果 Z=elevation+translationZ ;Z值越大，阴影越大。显示越在前。
////                    ViewOutlineProvider可以决定阴影的轮廓
////                    object :ViewOutlineProvider(){
////                        override fun getOutline(view: View?, outline: Outline?) {
////                            outline?.setRoundRect()
////                        }
////                    }
//                }
            }
        }
        return this
    }
}
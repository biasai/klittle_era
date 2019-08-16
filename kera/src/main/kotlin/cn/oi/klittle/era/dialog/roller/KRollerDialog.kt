package cn.oi.klittle.era.dialog.roller

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseDialog
import cn.oi.klittle.era.base.KBaseEntity
import cn.oi.klittle.era.comm.KLunar
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.view.KRollerView
import cn.oi.klittle.era.widget.compat.KView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.lang.Exception

//            fixme 调用案例
//            if (kDialog==null){
//                var list=ArrayList<BaseBean>()
//                for (i in 0..10){
//                    var bean=BaseBean()
//                    bean.id=i.toString()
//                    bean.name="NO:"+i
//                    list.add(bean)
//                }
//                kDialog= KRollerDialog(act)
//                //添加数据
//                kDialog?.setItems(list)
//                //回调
//                kDialog?.setItemSelectListener{item: BaseBean, position: Int ->
//                    Log.e("test","选中 id:\t"+item.id+"\tname:\t"+item.name)
//                }
//            }else{
//                kDialog?.show()
//            }

/**
 * fixme 单滚轮弹框
 */
open class KRollerDialog<T : KBaseEntity>(ctx: Context, isStatus: Boolean = true, isTransparent: Boolean = true) : KBaseDialog(ctx, isStatus = isStatus, isTransparent = isTransparent) {
    var contenView: View? = null//包裹滚轮的容器，可以控制背景样式。
    var txtRelativeLayout: RelativeLayout? = null//文本容器
    var rollerView: KRollerView? = null//底部滚轮

    var cancel: Button? = null//取消按钮
    var ok: Button? = null//完成按钮
    var info: Button? = null//提示信息
    var line1: KView? = null//第一条线条,透明。
    var line2: KView? = null//第二条线条

    init {
        setWindowAnimations(R.style.kera_window_bottom)
        isDismiss(true)
    }

    override fun onCreateView(context: Context): View? {
        return context.UI {
            verticalLayout {
                gravity = Gravity.BOTTOM
                contenView = verticalLayout {
                    isClickable = true
                    backgroundColor = Color.parseColor("#FAFAFA")//背景色

                    //第一条线条，透明
                    line1 = KView(this).apply {
                        backgroundColor = Color.TRANSPARENT
                    }.lparams {
                        width = matchParent
                        height = kpx.x(2)
                    }

                    //文本
                    txtRelativeLayout = relativeLayout {
                        cancel = button {
                            text = getString(R.string.kcancel)// "取消"
                            textSize = kpx.textSizeX(38)
                            textColor = Color.parseColor("#4B97F0")
                            if (Build.VERSION.SDK_INT >= 16) {
                                background = null
                            }
                            onClick {
                                dismiss()
                            }
                        }.lparams {
                            centerVertically()
                        }

                        info = button {
                            text = getString(R.string.kchoose)//"请选择"
                            textSize = kpx.textSizeX(38)
                            textColor = Color.parseColor("#888888")
                            if (Build.VERSION.SDK_INT >= 16) {
                                background = null
                            }
                        }.lparams {
                            centerInParent()
                        }

                        ok = button {
                            text = getString(R.string.kgone)//"完成"
                            textSize = kpx.textSizeX(38)
                            textColor = Color.parseColor("#4B97F0")
                            if (Build.VERSION.SDK_INT >= 16) {
                                background = null
                            }
                            onClick {
                                try {
                                    callback?.apply {
                                        rollerView?.let {
                                            var t = itemMap?.get(it.currentItemValue)
                                            if (t != null) {
                                                this(t, it.currentPostion)
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                dismiss()
                            }
                        }.lparams {
                            centerVertically()
                            alignParentRight()
                        }

                    }.lparams {
                        width = matchParent
                        height = kpx.x(100)
                    }

                    //第二条线条
                    line2 = KView(this).apply {
                        backgroundColor = Color.parseColor("#88888888")
                    }.lparams {
                        width = matchParent
                        height = kpx.x(2)
                    }

                    //滚轮
                    rollerView = KRollerView(context).apply {

                        isCurved = true//fixme 设置卷尺效果
                        isCyclic = true//fixme 是否循环显示。
                        setCount(7)//当前显示的item可见个数。

                        setLineColor(Color.parseColor("#88888888"))//中间两条线条的颜色
                        setLineWidth(0)//线条的长度，0 就是全屏
                        setStrokeWidth(kpx.x(2))//线条边框的宽度

                        setTextSize(kpx.x(36f))//字体大小,这个不是文本框，单位就是像素。
                        setSelectTextColor(Color.parseColor("#444444"))//选中字体颜色
                        setDefaultTextColor(Color.parseColor("#888888"))//默认字体颜色

                    }.lparams {
                        width = matchParent
                        height = kpx.x(330)
                    }
                    addView(rollerView)
                }.lparams {
                    width = matchParent
                    height = wrapContent
                }
            }
        }.view
    }

    var itemMap: HashMap<String, T>? = HashMap<String, T>()
    //设置数据
    open fun setItems(items: ArrayList<T>): KRollerDialog<T> {
        if (itemMap == null) {
            itemMap = HashMap<String, T>()
        }
        itemMap?.clear()
        var list = ArrayList<String?>()
        for (i in 0..items.lastIndex) {
            if (items[i].showName != null) {//fixme 优先读取showName字段
                list.add(items[i].showName)
                items[i].showName?.let {
                    itemMap?.put(it, items[i])
                }
            } else {
                list.add(items[i].name)//fixme 名称
                items[i].name?.let {
                    itemMap?.put(it, items[i])
                }
            }
        }
        rollerView?.setItems(list)
        rollerView?.invalidate()
//        //监听
//        rollerView?.setItemSelectListener { item, position ->
//            //返回数据和下标
//        }
        return this
    }

    //选中指定下标
    open fun setCurrentPostion(position: Int): KRollerDialog<T> {
        rollerView?.setCurrentPostion(position)
        return this
    }

    //根据值选中指定下标。
    open fun setCurrentValue(value: String): KRollerDialog<T> {
        rollerView?.setCurrentValue(value)
        return this
    }

    var callback: ((item: T, position: Int) -> Unit)? = null
    //完成按钮，监听回调
    open fun setItemSelectListener(callback: (item: T, position: Int) -> Unit) {
        this.callback = callback
    }

    override fun onShow() {
        super.onShow()
    }

    override fun onDismiss() {
        super.onDismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        callback = null
        itemMap?.clear()
        itemMap = null
    }

}
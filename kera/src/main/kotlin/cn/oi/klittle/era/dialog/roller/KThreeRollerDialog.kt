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
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.view.KRollerView
import cn.oi.klittle.era.widget.compat.KView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.lang.Exception

//            fixme 三滾輪調用案例

//            var kDialog3:KThreeRollerDialog<BaseBean,BaseBean,BaseBean>?=null

//            if (kDialog3 == null) {
//                var list = ArrayList<BaseBean>()
//                for (i in 0..10) {
//                    var bean = BaseBean()
//                    bean.id = i.toString()
//                    bean.name = "NO:" + i
//                    list.add(bean)
//                }
//                kDialog3 = KThreeRollerDialog(act)
//                //第一個滾輪添加数据
//                kDialog3?.setItems(list)
//                //第一個滾輪對第二個滾輪的联动
//                kDialog3?.setItemSelectChanged{item, position ->
//                    var list = ArrayList<BaseBean>()
//                    for (i in 0..10) {
//                        var bean = BaseBean()
//                        bean.id = i.toString()
//                        bean.name = "NO:" + i+"標記:\t"+item.name
//                        list.add(bean)
//                    }
//                    kDialog3?.setItems2(list)
//                    kDialog3?.setCurrentPostion2(position)//fixme 指定选中。触发联动。最好手动调用。
//                }
//                //第二個滾輪對第三個滾輪的联动
//                kDialog3?.setItemSelectChanged2{item, position ->
//                    var list = ArrayList<BaseBean>()
//                    for (i in 0..10) {
//                        var bean = BaseBean()
//                        bean.id = i.toString()
//                        bean.name = "NO:" + i+"標記3:\t"+item.name
//                        list.add(bean)
//                    }
//                    kDialog3?.setItems3(list)
//                    kDialog3?.setCurrentPostion3(position)//指定选中。
//                }
//                //fixme 最後再选择一个。此时会触发联动。（一定要在添加联动之后，再选中。）
//                kDialog3?.setCurrentPostion(5)//指定选中。
//                //确认完成键回调
//                kDialog3?.setItemSelectListener{item, item2, item3 ->
//                    //item 第一个滚轮选中
//                    //item2第二个滚轮选中返回
//                    //item3第三个滚轮选中
//                    Log.e("test","选中 id:\t"+item?.id+"\tname:\t"+item?.name)
//                    Log.e("test","选中2 id:\t"+item2?.id+"\tname:\t"+item2?.name)
//                    Log.e("test","选中3 id:\t"+item3?.id+"\tname:\t"+item3?.name)
//                }
//            } else {
//                kDialog3?.show()
//            }

/**
 * 三滚轮弹框
 */
open class KThreeRollerDialog<T : KBaseEntity, T2 : KBaseEntity, T3 : KBaseEntity>(ctx: Context, isStatus: Boolean = true, isTransparent: Boolean = true) : KBaseDialog(ctx, isStatus = isStatus, isTransparent = isTransparent) {
    var contenView: View? = null//包裹滚轮的容器，可以控制背景样式。
    var txtRelativeLayout: RelativeLayout? = null//文本容器
    var rollerView: KRollerView? = null//底部滚轮
    var rollerView2: KRollerView? = null//底部滚轮2
    var rollerView3: KRollerView? = null//底部滚轮3

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
                                        var t: T? = null
                                        var t2: T2? = null
                                        var t3: T3? = null
                                        rollerView?.let {
                                            t = itemMap?.get(it.currentItemValue)
                                        }
                                        rollerView2?.let {
                                            t2 = itemMap2?.get(it.currentItemValue)
                                        }
                                        rollerView3?.let {
                                            t3 = itemMap3?.get(it.currentItemValue)
                                        }
                                        this(t, t2, t3)
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
                    linearLayout {
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
                            width = 0
                            weight = 1f
                            height = matchParent
                        }
                        addView(rollerView)
                        rollerView2 = KRollerView(context).apply {

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
                            width = 0
                            weight = 1f
                            height = matchParent
                        }
                        addView(rollerView2)
                        rollerView3 = KRollerView(context).apply {

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
                            width = 0
                            weight = 1f
                            height = matchParent
                        }
                        addView(rollerView3)
                    }.lparams {
                        width = matchParent
                        height = kpx.x(330)
                    }
                }.lparams {
                    width = matchParent
                    height = wrapContent
                }
            }
        }.view
    }

    var itemMap: HashMap<String, T>? = HashMap<String, T>()
    var itemMap2: HashMap<String, T2>? = HashMap<String, T2>()
    var itemMap3: HashMap<String, T3>? = HashMap<String, T3>()
    //设置数据
    open fun setItems(items: ArrayList<T>): KThreeRollerDialog<T, T2, T3> {
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

    open fun setItems2(items: ArrayList<T2>): KThreeRollerDialog<T, T2, T3> {
        if (itemMap2 == null) {
            itemMap2 = HashMap<String, T2>()
        }
        itemMap2?.clear()
        var list = ArrayList<String?>()
        for (i in 0..items.lastIndex) {
            if (items[i].showName != null) {//fixme 优先读取showName字段
                list.add(items[i].showName)
                items[i].showName?.let {
                    itemMap2?.put(it, items[i])
                }
            } else {
                list.add(items[i].name)//fixme 名称
                items[i].name?.let {
                    itemMap2?.put(it, items[i])
                }
            }
        }
        rollerView2?.setItems(list)
        rollerView2?.invalidate()
        return this
    }

    open fun setItems3(items: ArrayList<T3>): KThreeRollerDialog<T, T2, T3> {
        if (itemMap3 == null) {
            itemMap3 = HashMap<String, T3>()
        }
        itemMap3?.clear()
        var list = ArrayList<String?>()
        for (i in 0..items.lastIndex) {
            if (items[i].showName != null) {//fixme 优先读取showName字段
                list.add(items[i].showName)
                items[i].showName?.let {
                    itemMap3?.put(it, items[i])
                }
            } else {
                list.add(items[i].name)//fixme 名称
                items[i].name?.let {
                    itemMap3?.put(it, items[i])
                }
            }
        }
        rollerView3?.setItems(list)
        rollerView3?.invalidate()
        return this
    }

    //选中指定下标
    open fun setCurrentPostion(position: Int): KThreeRollerDialog<T, T2, T3> {
        rollerView?.setCurrentPostion(position)
        return this
    }

    //根据值选中指定下标。
    open fun setCurrentValue(value: String): KThreeRollerDialog<T, T2, T3> {
        rollerView?.setCurrentValue(value)
        return this
    }

    open fun setCurrentPostion2(position: Int): KThreeRollerDialog<T, T2, T3> {
        rollerView2?.setCurrentPostion(position)
        return this
    }

    open fun setCurrentValue2(value: String): KThreeRollerDialog<T, T2, T3> {
        rollerView2?.setCurrentValue(value)
        return this
    }

    open fun setCurrentPostion3(position: Int): KThreeRollerDialog<T, T2, T3> {
        rollerView3?.setCurrentPostion(position)
        return this
    }

    open fun setCurrentValue3(value: String): KThreeRollerDialog<T, T2, T3> {
        rollerView3?.setCurrentValue(value)
        return this
    }

    //fixme 完成按钮，监听回调
    var callback: ((item: T?, item2: T2?, item3: T3?) -> Unit)? = null

    open fun setItemSelectListener(callback: (item: T?, item2: T2?, item3: T3?) -> Unit) {
        this.callback = callback
    }

    //fixme 第一個滚轮选中改变时回调，用于实现联动。
    open fun setItemSelectChanged(callback: (item: T, position: Int) -> Unit) {
        rollerView?.setItemSelectListener(object : KRollerView.ItemSelectListener {
            override fun onItemSelect(item: String?, position: Int) {
                rollerView?.let {
                    var t = itemMap?.get(it.currentItemValue)
                    if (t != null) {
                        callback(t, it.currentItemPosition)
                    }
                }
            }
        })
    }

    //fixme 第二個滚轮选中改变时回调，用于实现联动。
    open fun setItemSelectChanged2(callback: (item: T2, position: Int) -> Unit) {
        rollerView2?.setItemSelectListener(object : KRollerView.ItemSelectListener {
            override fun onItemSelect(item: String?, position: Int) {
                rollerView2?.let {
                    var t = itemMap2?.get(it.currentItemValue)
                    if (t != null) {
                        callback(t, it.currentItemPosition)
                    }
                }
            }
        })
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
        itemMap2?.clear()
        itemMap2 = null
        itemMap3?.clear()
        itemMap3 = null
    }

}
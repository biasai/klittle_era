package cn.oi.klittle.era.comm

import android.view.View

/**
 * 系统的单选和多选按钮实在是太鸡肋了，连个圆圈大小都不能更改。实在看不下去了。
 * 单选或多选工具类
 *
 * fixme isMulti ture 多选，false单选。默认就是单选！
 * fixme minMulti多选中才有效，至少选择多少个。即必选几个。
 * fixme isSelected=true 选中，isSelected=false未选中
 */
//            使用案例
//            var choose = Choose(false, 2)
//            addView(BaseView(act).apply {
//                autoBg(R.mipmap.p_second_gou_gay, null, R.mipmap.p_second_gou_blue)
//                id=px.id("first")//id,可有可无
//                choose.add(this)//添加
//                choose.setChek(this)//默认选中一个
//                onClick {
//                    choose.setToogle(this@apply)
//                }
//            }.lparams {
//                leftMargin = px.x(24)
//                topMargin = px.x(24)
//            })
open class KChoose(var isMulti: Boolean = false, var minMulti: Int = 0) {
    private var list = arrayListOf<View>()//所有的控件
    //添加控件
    open fun add(view: View?) {
        view?.apply {
            if (!list.contains(view)) {
                list.add(view)
            }
        }
    }

    private var chekViews = arrayListOf<View>()//当前选中的多个控件。多选
    //获取多选
    open fun getChekViews(): ArrayList<View> {
        if (chekViews.size <= 0) {
            for (i in 0 until list.size) {
                var v = list[i]
                if (v.isSelected) {
                    if (!chekViews.contains(v)) {
                        chekViews.add(v)//记录多选按钮
                    }
                }
            }
        }
        return chekViews
    }

    //获取多选Id集合
    open fun getChekIds(): ArrayList<Int> {
        var views = getChekViews()
        var ids = arrayListOf<Int>()
        for (i in 0 until views.size) {
            var v = views[i]
            var id: Int? = v.id
            id?.let {
                ids.add(it)
            }
        }
        return ids
    }

    private var chekView: View? = null//单前选中控件。单选
    //获取单选
    open fun getChekView(): View? {
        if (chekView == null) {
            for (i in 0 until list.size) {
                var v = list[i]
                if (v.isSelected) {
                    chekView = v
                    break
                }
            }
        }
        return chekView
    }

    //获取单选id
    open fun getChekId(): Int? {
        return getChekView()?.id
    }

    //fixme 根据id获取View ,id为空的话，默认就是-1,-1就是没有id
    private fun findViewById(id: Int?): View? {
        var v: View? = null
        id?.let {
            for (i in 0 until list.size) {
                var v2 = list[i]
                var id2: Int? = v2.id
                if (id2 != null && id2 == it) {
                    v = v2
                    break
                }
            }
        }
        return v
    }

    //根据id选中View
    open fun setChek(id: Int?) {
        var v: View? = findViewById(id)
        if (v != null) {
            setChek(v)
        }
    }

    //选中
    open fun setChek(view: View?) {
        view?.isSelected = true
        view?.apply {
            chekView = view//记录单选按钮
            if (!chekViews.contains(view)) {
                chekViews.add(view)//记录多选按钮
            }

            if (!isMulti) {
                //单选
                for (i in 0 until list.size) {
                    var v = list[i]
                    if (v != view) {
                        if (v.isSelected) {
                            v.isSelected = false//单选只存在一个选中
                        }
                    }
                }
            }

        }
    }

    //根据id取消选中View
    open fun setUnChek(id: Int?) {
        var v: View? = findViewById(id)
        if (v != null) {
            setUnChek(v)
        }
    }

    //未选中（单选调用无效，单选取消是被动的，不是主动）
    open fun setUnChek(view: View?) {
        if (isMulti) {
            if (chekViews.size > minMulti) {//fixme minMulti必选项。不能少于该选项
                //多选才能取消选中
                view?.isSelected = false
                view?.apply {
                    if (chekViews.contains(view)) {
                        chekViews.remove(view)//去除未选中的
                    }
                }
            }
        }
    }

    //根据id进行交替
    open fun setToogle(id: Int?) {
        var v: View? = findViewById(id)
        if (v != null) {
            setToogle(v)
        }
    }

    //交替(选中变未选中，未选中变选中)
    open fun setToogle(view: View?) {
        view?.let {
            if (it.isSelected) {
                setUnChek(it)//选中变未选中
            } else {
                setChek(it)//未选中变选中
            }
        }
    }
}
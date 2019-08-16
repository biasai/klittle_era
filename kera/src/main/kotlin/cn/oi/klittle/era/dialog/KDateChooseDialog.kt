package cn.oi.klittle.era.dialog

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseDialog
import cn.oi.klittle.era.entity.feature.KDateChooseEntity
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KCalendarUtils
import cn.oi.klittle.era.utils.KProportionUtils
import cn.oi.klittle.era.view.KRollerView

/**
 * 日期选择器
 * Created by 彭治铭 on 2018/6/3.
 */
//使用说明
//var dateChoose = DateChoose()
//val dateChooseDialog:DateChooseDialog by lazy { DateChooseDialog(this, dateChoose).setCallBack { dateChoose = it }}
//dateChooseDialog.show()
/**
 * @param ctx: Context
 * @param dateChoose 时间选择器
 * @param isStatus 是否有状态栏
 * @param isTransparent 是否透明
 */
open class KDateChooseDialog(ctx: Context, var dateChoose: KDateChooseEntity = KDateChooseEntity(), isStatus: Boolean = true, isTransparent: Boolean = true) : KBaseDialog(ctx, R.layout.kera_dialog_date_choose,isStatus,isTransparent) {
    val yyyy: KRollerView by lazy { findViewById<KRollerView>(R.id.crown_roller_yyyy) }
    val MM: KRollerView by lazy { findViewById<KRollerView>(R.id.crown_roller_MM) }
    val dd: KRollerView by lazy { findViewById<KRollerView>(R.id.crown_roller_dd) }

    init {
        if (ctx is Activity){
            KProportionUtils.getInstance().adapterWindow(ctx,dialog?.window)//适配
        }
        dialog?.window?.setWindowAnimations(R.style.kera_window_bottom)//动画
        //取消
        findViewById<View>(R.id.crown_txt_cancel).setOnClickListener {
            dismiss()
        }
        //完成
        findViewById<View>(R.id.crown_txt_ok).setOnClickListener {
            dismiss()
        }
        //年
        var list_yyyy = ArrayList<String>()
        for (i in 2010..2030) {
            list_yyyy.add(i.toString())
        }
        yyyy.setLineColor(Color.TRANSPARENT).setItems(list_yyyy).setTextSize(kpx.x(40f)).setCount(5)
                .setDefaultTextColor(Color.parseColor("#444444")).setSelectTextColor(Color.parseColor("#444444"))
        //月
        var list_MM = ArrayList<String>()
        for (i in 1..12) {
            list_MM.add(i.toString())
        }
        MM.setLineColor(Color.TRANSPARENT).setItems(list_MM).setTextSize(kpx.x(40f)).setCount(5)
                .setDefaultTextColor(Color.parseColor("#444444")).setSelectTextColor(Color.parseColor("#444444"))
        MM.setItemSelectListener(object : KRollerView.ItemSelectListener {
            override fun onItemSelect(item: String?, position: Int) {
                //月份监听
                updateDays()
            }
        })
        //日
        dd.setLineColor(Color.TRANSPARENT).setTextSize(kpx.x(40f)).setCount(5)
                .setDefaultTextColor(Color.parseColor("#444444")).setSelectTextColor(Color.parseColor("#444444"))

        //fixme 设置数据滚轮循环效果
        yyyy.isCyclic = true
        MM.isCyclic = true
        dd.isCyclic = true

        isDismiss(true)
    }

    open fun updateDays() {
        //日，联动，更加月份而改变
        var list_dd = ArrayList<String>()
        val mDay = KCalendarUtils.getMonthOfDay(yyyy.currentItemValue + "-" + MM.currentItemValue,"yyyy-MM")//天数
        for (i in 1..mDay) {
            list_dd.add(i.toString())
        }
        dd.setItems(list_dd)
    }

    override fun onShow() {
        super.onShow()
        updateDays()
        //选中
        yyyy.setCurrentPostion(yyyy.getItemPostion(dateChoose.yyyy))
        MM.setCurrentPostion(MM.getItemPostion(dateChoose.MM))
        dd.setCurrentPostion(dd.getItemPostion(dateChoose.dd))
    }

    override fun onDismiss() {
        super.onDismiss()
    }

    //日期返回回调
    open fun setCallBack(callbak: (dateChoose: KDateChooseEntity) -> Unit): KDateChooseDialog {
        //完成
        findViewById<View>(R.id.crown_txt_ok).setOnClickListener {
            dateChoose.yyyy = yyyy.currentItemValue
            dateChoose.MM = MM.currentItemValue
            dateChoose.dd = dd.currentItemValue
            callbak(dateChoose)
            dismiss()
        }
        return this
    }

}
package cn.oi.klittle.era.activity.ringtone

import android.os.Bundle
import cn.oi.klittle.era.base.KBaseActivity
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KRingtoneManagerUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay


/**
 * fixme 系统铃声选择;铃声选中之后，改变的是KRingtoneManagerUtils.index下标
 */
open class KRingtoneActivity : KBaseActivity() {

    override fun isEnableSliding(): Boolean {
        return true
    }

    override fun shadowSlidingWidth(): Int {
        //return super.shadowSlidingWidth()
        return kpx.screenWidth() / 2
    }

    var ui: KRingtoneUi? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            ui = KRingtoneUi()
            setContentView(ui?.createView(this))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        if (ui?.ringtoneAdapter?.datas == null) {
            showProgressbar()//显示弹窗
            GlobalScope.async {
                try {
                    ui?.ringtoneAdapter?.datas = KRingtoneManagerUtils.getRingToneDatas()
                    ui?.ringtoneAdapter?.datas?.let {
                        if (it.size > 0) {
                            if (!isFinishing) {
                                runOnUiThread {
                                    ui?.ringtoneAdapter?.notifyDataSetChanged()
                                    shutProgressbar()//关闭弹窗
                                    if (KRingtoneManagerUtils.index > 12) {//PDA能显示13个左右。小米8能显示17个左右
                                        ui?.recyclerView?.scrollToPositionWithOffset(KRingtoneManagerUtils.index)//滑动到选中下标
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun finish() {
        try {
            ui?.ringtoneAdapter?.datas = null
            ui?.ringtoneAdapter = null
            //KRingtoneManagerUtils.clearListRingTone()//fixme 清除铃声集合;不需要清除，不怎么占内存。主要是获取的时候耗时。
            super.finish()
            ui?.destroy(this)//界面销毁
            KRingtoneManagerUtils.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}
package cn.oi.klittle.era.activity.ringtone

import android.os.Bundle
import android.view.View
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseActivity
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KRingtoneManagerUtils

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


    override fun finish() {
        super.finish()
        KRingtoneManagerUtils.stop()
    }

}
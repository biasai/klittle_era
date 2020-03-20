package cn.oi.klittle.era.activity.video

import android.os.Bundle
import cn.oi.klittle.era.base.KBaseActivity
import cn.oi.klittle.era.helper.KUiHelper
import cn.oi.klittle.era.utils.KLoggerUtils

//fixme 調用案例
// KUiHelper.goScreenVideoActivity(this,null)//參數二是視頻地址
/**
 * 视频全屏播放
 */
open class KScreenVideoActivity : KBaseActivity() {

    override fun isOrientation(): Boolean {
        return false
    }

    override fun isPortrait(): Boolean {
        return false//true竖屏，false横屏
    }

    var videoPath: String? = null//视频播放地址
    var ui: KScreenVideoUi? = null//ui布局界面
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.extras?.getString(KUiHelper.videoPath_key)?.let {
            videoPath = it
        }
        if (ui == null) {
            ui = KScreenVideoUi()
        }
        setContentView(ui?.createView(ctx = this))
    }
}
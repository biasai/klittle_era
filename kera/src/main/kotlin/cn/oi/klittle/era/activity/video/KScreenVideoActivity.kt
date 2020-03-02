package cn.oi.klittle.era.activity.video

import android.os.Bundle
import cn.oi.klittle.era.base.KBaseActivity
import cn.oi.klittle.era.helper.KUiHelper

/**
 * 视频全屏播放
 */
open class KScreenVideoActivity : KBaseActivity() {
    var videoPath: String? = null//视频播放地址
    var ui: KScreenVideoUi? = null//ui布局界面
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.extras?.getString(KUiHelper.videoPath_key)?.let {
            videoPath = it
        }
        if (ui == null) {
            ui = KScreenVideoUi()
            setContentView(ui?.createView(ctx = this))
        }
    }
}
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
        return true
    }

    override fun isPortrait(): Boolean {
        //return false//true竖屏，false横屏
        return KUiHelper.isPortrait_screenVideo
    }

    var videoPath: String? = null//视频播放地址
    var ui: KScreenVideoUi? = null//ui布局界面
    private var process_msec = 0//播放进度;毫秒。
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.extras?.getString(KUiHelper.videoPath_key)?.let {
            videoPath = it
        }
        if (ui == null) {
            ui = KScreenVideoUi()
        }
        setContentView(ui?.createView(ctx = this))
        process_msec=KUiHelper.process_msec_screenVideo//记录上一页面的播放进度
        ui?.video?.prepare(videoPath)
        ui?.mediaController?.leftTextView_txt?.setText(ui?.video?.getName())//视频名称（还是把后缀显示出来比较好。）
    }

    override fun onPause() {
        super.onPause()
        ui?.video?.currentPosition?.let {
            process_msec=it//fixme 记录当前播放毫秒数
            KUiHelper.process_msec_screenVideo=it
        }
        //KLoggerUtils.e("onPause()\tprocess_msec:\t"+process_msec)
    }

    override fun onResume() {
        super.onResume()
        if (process_msec>0){
            ui?.video?.seekTo(process_msec)//fixme 恢复播放进度
        }
        //KLoggerUtils.e("onResume()\tprocess_msec:\t"+process_msec)

    }

    override fun finish() {
        super.finish()
        ui?.mediaController?.onDestroy()
        ui?.destroy(this)
    }

}
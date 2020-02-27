package cn.oi.klittle.era.widget.video

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseUi.Companion.kview
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.widget.compat.KView
import cn.oi.klittle.era.widget.seekbar.KSeekBarProgressBar
import org.jetbrains.anko.*

open class KMediaController {
    var relativeLayout: RelativeLayout? = null//最外层布局
    var videoView: KVideoView? = null//视频播放器
    var play: KView? = null//播放按钮
    var seekbar: KSeekBarProgressBar? = null//进度条
    var screen: KView? = null//全屏按钮

    //播放活暂停
    fun play() {
        play?.apply {
            videoView?.toggle()
            videoView?.isPlaying?.let {
                play?.isSelected = it
            }
        }
    }

    //videoView视频播放会刷选该方法。
    fun updateView() {
        videoView?.context?.let {
            if (it is Activity) {
                if (!it.isFinishing) {
                    try {
                        //播放按钮刷选
                        play?.apply {
                            videoView?.isPlaying?.let {
                                play?.isSelected = it
                            }
                        }
                        //进度条刷选
                        videoView?.process?.let {
                            seekbar?.progress = it
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    constructor(viewGroup: ViewGroup?, videoView: KVideoView?) {
        this.videoView = videoView
        videoView?.setMediaController(this)
        viewGroup?.context?.apply {
            relativeLayout = relativeLayout {
                backgroundColor = Color.BLACK
                //播放
                play = kview {
                    id = kpx.id("kera_media_play")
                    autoBg {
                        width = kpx.x(64) / 2
                        height = kpx.x(64) / 2
                        autoBg(R.mipmap.kera_media_play)
                    }
                    autoBg_selected {
                        autoBg(R.mipmap.kera_media_pause)
                    }
                    videoView?.isPlaying?.let {
                        isSelected = it
                    }
                    onClick {
                        play()
                    }
                }.lparams {
                    width = kpx.x(64)
                    height = kpx.x(64)
                    centerVertically()
                    alignParentLeft()
                }
                //进度条
                seekbar = KSeekBarProgressBar(this).apply {
                    dst {
                        all_radius(kpx.x(33f))
                        bg_color = Color.GRAY
                        height = kpx.x(6)
                    }
                    src {
                        bg_color = Color.parseColor("#1F95FF")
                    }
                    //fixme 视频还是不需要拇指滚动条比较好。
                    //关闭硬件加速，阴影才会有效果
//                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
//                    drawThumb { canvas, paint, x, y ->
//                        paint.color = Color.WHITE
//                        paint.setShadowLayer(kpx.x(10f), 0f, 0f, Color.parseColor("#1F95FF"))
//                        canvas.drawCircle(x, y, kpx.x(12f), paint)
//                    }
                    //fixme 防止宽高获取不到，防止滑动块显示异常；最好在布局加载完成之后，在设置进度。
                    onGlobalLayoutListener {
                        dst {
                            width = w - kpx.x(24)
                        }
                        src {
                            width = w - kpx.x(24)
                        }
                    }
                    setOnSeekBarChangeListener { previousProgress, currentProgress, isActionUp, progressX, progressY ->
                        //KLoggerUtils.e("previousProgress:\t"+previousProgress+"\tcurrentProgress:\t"+currentProgress+"\tisActionUp:\t"+isActionUp)
                        if (!isActionUp){
                            //手指触摸时
                            videoView?.let {
                                videoView?.seekTo(currentProgress)
                            }

                        }
                    }
                }.lparams {
                    width = matchParent
                    height = kpx.x(64)
                    centerVertically()
                    leftOf(kpx.id("kera_media_screen"))
                    rightOf(kpx.id("kera_media_play"))
                }
                //全屏
                screen = kview {
                    id = kpx.id("kera_media_screen")
                    autoBg {
                        width = kpx.x(64) / 2
                        height = kpx.x(64) / 2
                        autoBg(R.mipmap.kera_media_screen_)
                    }
                }.lparams {
                    width = kpx.x(64)
                    height = kpx.x(64)
                    centerVertically()
                    alignParentRight()
                }
            }
            //一般默认就是 width = matchParent;height = wrapContent
            viewGroup?.addView(relativeLayout, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
    }
}
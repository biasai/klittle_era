package cn.oi.klittle.era.widget.video

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.RelativeLayout
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseUi.Companion.kview
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.helper.KUiHelper
import cn.oi.klittle.era.widget.compat.KTextView
import cn.oi.klittle.era.widget.compat.KView
import cn.oi.klittle.era.widget.seekbar.KSeekBarProgressBar
import org.jetbrains.anko.*

//      调用案例，参数一是父容器布局，参数二是KVideoView
//      KMediaController(this@verticalLayout, video)

/**
 * fixme KVideoView视频播放的控制器布局
 */
open class KMediaController {
    var relativeLayout: RelativeLayout? = null//最外层布局
    var videoView: KVideoView? = null//视频播放器
    var play: KView? = null//播放按钮
    var seekbar: KSeekBarProgressBar? = null//进度条
    var screen: KView? = null//全屏按钮
    var currentPosition: KTextView? = null//当前播放时间
    var duration: KTextView? = null//视频总时长

    //播放活暂停
    open fun play() {
        play?.apply {
            videoView?.toggle()
            videoView?.isPlaying?.let {
                play?.isSelected = it
            }
        }
    }

    //videoView视频播放会刷选该方法。
    open fun updateView() {
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
                        //当前播放时间
                        videoView?.getCurrentPositionTimeParse()?.let {
                            currentPosition?.setText(it)
                        }
                        //视频总时长
                        videoView?.getDurationTimeParse()?.let {
                            duration?.setText(it)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    //全屏
    open fun screen(){
        videoView?.let {
            it.context?.let {
                if (it is Activity){
                    if (!it.isFinishing){
                        KUiHelper.goScreenVideoActivity(it,videoView?.path)
                    }
                }
            }
        }
    }

    constructor(viewGroup: ViewGroup?, videoView: KVideoView?) {
        this.videoView = videoView
        videoView?.setMediaController(this)//fixme 绑定
        viewGroup?.context?.apply {
            relativeLayout = relativeLayout {
                backgroundColor = Color.BLACK
                linearLayout {
                    gravity = Gravity.CENTER
                    id = kpx.id("klayout_left")
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

                    }
                    //当前播放时间
                    currentPosition = KTextView(this).apply {
                        textSize = kpx.textSizeX(26, false)
                        textColor = Color.WHITE
                    }.lparams {
                        width = wrapContent
                        height = wrapContent
                    }
                }.lparams {
                    width = wrapContent
                    height = wrapContent
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
                    //进度条触摸监听
                    onTouchListener { progress, isActionDown, isActionUp ->
                        //progress 当前进度；isActionDown手指是否按下；isActionUp手指是否离开
                        videoView?.seekTo(progress)
                    }
                }.lparams {
                    width = matchParent
                    height = kpx.x(64)
                    centerVertically()
                    leftOf(kpx.id("klayout_right"))
                    rightOf(kpx.id("klayout_left"))
                }
                linearLayout {
                    gravity = Gravity.CENTER
                    id = kpx.id("klayout_right")
                    //视频总时长
                    duration = KTextView(this).apply {
                        textSize = kpx.textSizeX(26, false)
                        textColor = Color.WHITE
                    }.lparams {
                        width = wrapContent
                        height = wrapContent
                    }
                    //全屏
                    screen = kview {
                        id = kpx.id("kera_media_screen")
                        autoBg {
                            width = kpx.x(64) / 2
                            height = kpx.x(64) / 2
                            autoBg(R.mipmap.kera_media_screen_)
                        }
                        onClick {
                            screen()
                        }
                    }.lparams {
                        width = kpx.x(64)
                        height = kpx.x(64)
                    }
                }.lparams {
                    width = wrapContent
                    height = wrapContent
                    centerVertically()
                    alignParentRight()
                }
            }
            //一般默认就是 width = matchParent;height = wrapContent
            viewGroup?.addView(relativeLayout, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
    }
}
package cn.oi.klittle.era.widget.video

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.VideoView
import cn.oi.klittle.era.utils.KLoggerUtils
import org.jetbrains.anko.custom.async
import org.jetbrains.anko.runOnUiThread

//                                        setVideoPath(path)//加载本地视频，同样也支持网络视频。如果字符串是网络url，同样可以播放。
//                                        //在视频预处理完成后被调用。
//                                        setOnPreparedListener {
//                                            start()
//                                            pause()//立即播放和暂停（这样画面会停留在第一帧）；如果不播放整个控件的画面就是黑的。什么都没有
//                                        }

//                    var path = "/storage/emulated/0/tencent/MicroMsg/WeiXin/1576322118470.mp4"
//                    prepare(path) {
//                        //预加载完成,默认会自动播放
//                    }

//                                            //播放完成（画面会停留在最后一帧）
//                                            setOnCompletionListener {
//                                            }

//                                            setOnErrorListener { mp, what, extra ->
//                                                //播放错误监听
//                                                true
//                                            }

/**
 * 重写视频播放器；添加播放和暂停的监听；原生的没有。（原生的只有播放完成，播放错误监听。）
 */
class KVideoView : VideoView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    /**
     * 准备播放，并且画面停留在第一帧。fixme 播放完成之后，画面会停留在最后一帧。
     * @param path 播放资源路径
     * @param isStart 预加载完成之后释放立即播放。true 播放；false 不播放(会暂停在第一帧。)
     * @param callback 预处理完成之后，回调。
     */
    fun prepare(path: String?, isStart: Boolean = true, callback: (() -> Unit)? = null) {
        path?.trim()?.let {
            if (it.length > 0) {
                /**
                 * fixme 注意，加载完视频之后，videoView控件本身会根据视频宽高比例；自动调节本身控件的宽和高。即与视频的宽高比例保持一致。
                 */
                setVideoPath(path)//加载本地视频，同样也支持网络视频。如果字符串是网络url，同样可以播放。
                //在视频预处理完成后被调用。
                setOnPreparedListener {
                    start()
                    if (!isStart) {
                        pause()//fixme 立即播放和暂停（这样画面会停留在第一帧）；如果不播放整个控件的画面就是黑的。什么都没有
                    }
                    if (callback != null) {
                        async {
                            context?.runOnUiThread {
                                //fixme 之所以协程在主主线程跳；防止宽高更新不及时。
                                callback?.let {
                                    try {
                                        it()
                                    } catch (e: Exception) {
                                        KLoggerUtils.e("video预加载prepare()回调异常：\t"+e.message)
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //播放开始监听
    var onStart: (() -> Unit)? = null

    fun onStart(onStart: (() -> Unit)? = null) {
        this.onStart = onStart
    }

    //播放暂停监听
    var onPause: (() -> Unit)? = null

    fun onPause(onPause: (() -> Unit)? = null) {
        this.onPause = onPause
    }

    //播放
    override fun start() {
        super.start()
        onStart?.let {
            it()
        }
    }

    //暂停
    override fun pause() {
        super.pause()
        onPause?.let {
            it()
        }
    }

    //播放暂停；切换
    fun toggle() {
        if (!isPlaying) {
            start()//播放
        } else {
            pause()//暂停
        }
    }

    //重写，释放所有资源
    override fun suspend() {
        onStart = null
        onPause = null
        super.suspend()
    }

}
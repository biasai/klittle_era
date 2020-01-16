package cn.oi.klittle.era.widget.video

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.VideoView
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.utils.KRegexUtils
import cn.oi.klittle.era.utils.KStringUtils
import kotlinx.coroutines.experimental.Deferred
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
        initUi()
    }

    constructor(context: Context) : super(context) {
        initUi()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initUi()
    }


    fun initUi() {
        //fixme 播放完毕回调(isPlaying会自动变成false)，返回MediaPlayer对象
        //setOnCompletionListener {}
    }

    //mediaPlayer?.isLooping=false//fixme 是否循环播放
    var mediaPlayer: MediaPlayer? = null
    var path: String? = null//当前的播放视频路径
    /**
     * 准备播放，并且画面停留在第一帧。fixme 播放完成之后，画面会停留在最后一帧。
     * @param path 播放资源路径
     * @param isStart 预加载完成之后释放立即播放。true 播放；false 不播放(会暂停在第一帧。)
     * @param callback 预处理完成之后，回调。
     */
    fun prepare(path: String?, isStart: Boolean = true, callback: (() -> Unit)? = null) {
        path?.trim()?.let {
            if (it.length > 0) {
                if (it.equals(this.path?.trim())) {
                    return//fixme 防止重复
                }
                suspend()//fixme 释放掉之前的视频
                this.path = path
                /**
                 * fixme 注意，加载完视频之后，videoView控件本身会根据视频宽高比例；自动调节控件本身的宽和高。即与视频的宽高比例保持一致。
                 */
                if (KRegexUtils.isUrl(path)) {
                    setVideoURI(Uri.parse(path))// 播放网络视频
                } else {
                    setVideoPath(path)//加载本地视频，同样也支持网络视频。如果字符串是网络url，同样可以播放。
                }
                //在视频预处理完成后被调用。
                setOnPreparedListener {
                    mediaPlayer = it//fixme 每次返回都是一个新的MediaPlayer对象；所以需要重新赋值。
                    mediaPlayer?.setOnSeekCompleteListener {
                        //seekTo 方法完成时的回调
                        start()//跳转到指定播放时间之后，立即自动播放；fixme 同时解决，视频播放时间不准确的问题。
                        onSeekTo?.let {
                            it()
                        }
                    }
                    async {
                        context?.runOnUiThread {
                            start()
                            if (!isStart) {
                                pause()//fixme 立即播放和暂停（这样画面会停留在第一帧）；如果不播放整个控件的画面就是黑的。什么都没有
                            }
                            //fixme 之所以协程在主主线程跳；防止宽高更新不及时。
                            callback?.let {
                                try {
                                    it()
                                } catch (e: Exception) {
                                    KLoggerUtils.e("video预加载prepare()回调异常：\t" + e.message)
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //播放监听(每次播放都会调用)；即继续播放（和暂停对应）
    var onStart: (() -> Unit)? = null

    fun onStart(onStart: (() -> Unit)? = null) {
        this.onStart = onStart
    }

    //重新播放；即从第一帧开始重新播放。不是继续播放
    //重新播放/重头播放时调用(即从第一帧播放时调用)
    //fixme 一般onResume会在onStart的前面回调。
    var onResume: (() -> Unit)? = null

    fun onResume(onResume: (() -> Unit)? = null) {
        this.onResume = onResume
    }

    //暂停监听
    var onPause: (() -> Unit)? = null

    fun onPause(onPause: (() -> Unit)? = null) {
        this.onPause = onPause
    }

    //播放(继续播放，和暂停对应) fixme (视频如果播放完成之后，再调用start();视频会重新播放。即从第一帧开始播放。)
    override fun start() {
        var isPlaying = isPlaying
        if (!isPlaying) {
            //KLoggerUtils.e("currentPosition:\t"+currentPosition+"\tisResume:\t"+isResume)
            //防止播放关键帧不是0;currentPosition单位是毫秒
            if (currentPosition <= 101 && !isResume) {
                onResume?.let {
                    it()//重新播放回调。(从第一帧播放时调用。)
                }
            }
        }
        super.start()//isPlaying会变成ture
        if (!isPlaying) {
            //防止重复回调。
            onStart?.let {
                it()
            }
        }
        isResume = false
    }

    private var isResume = false
    //重新播放（从第一帧开始播放，不是继续播放）；
    override fun resume() {
        isResume = true
        super.resume()//内部调用了start()方法(异步调用，不是同步哦。)。
        onResume?.let {
            it()
        }
    }

    //暂停
    override fun pause() {
        super.pause()//isPlaying会变成false
        onPause?.let {
            it()
        }
    }

    //fixme isPlaying判断是否正在播放;true正在播放；false没有播放
    //播放暂停；切换
    fun toggle() {
        if (!isPlaying) {
            start()//播放
        } else {
            pause()//暂停
        }
    }

    //视频跳转到指定播放时间；
    //fixme msec单位是毫秒，1000等于1秒;currentPosition的当前播放时间；duration是总时长。单位都是毫秒。
    override fun seekTo(msec: Int) {
        //fixme msec小于0；内部会默认作为0处理
        //fixme msec大于等于duration;视频播放时，不会立即播放尾帧。而是倒退一两秒播放。(亲测)
        //fixme seek只能seek到关键帧，否则无法播放，如91800的位置不是关键帧，所以会往前找，直到找到关键帧，87000(假设)应该就是关键帧的位置了。
        super.seekTo(msec)
    }

    //seekTo回调
    var onSeekTo: (() -> Unit)? = null

    fun onSeekTo(onSeekTo: (() -> Unit)? = null) {
        this.onSeekTo = onSeekTo
    }

    //获取当前的播放时长
    fun getCurrentPositionForTime(): String {
        //currentPosition当前的播放时长，单位毫秒;fixme currentPosition初始值一般是0
        return KStringUtils.stringForTime(currentPosition)//转换为时间格式
    }

    //获取视频总时长
    fun getDurationForTime(): String {
        //duration视频总时长;如：00:13 ；fixme duration初始值是-1(没有视频时)
        return KStringUtils.stringForTime(duration)//转换为时间格式
    }

    var onSeekListener: ((progress: Float) -> Unit)? = null//进度回调
    private var isProgress: Boolean = false//是否正在刷新进度
    private var job: Deferred<Any?>? = null
    fun onSeekListener(onSeekListener: ((progress: Float) -> Unit)? = null) {
        this.onSeekListener = onSeekListener
//        if (!isProgress) {
//            isProgress = true
//            job?.cancel()//取消协程
//            job = kotlinx.coroutines.experimental.async {
//                when (isProgress) {
//
//                }
//                job = null//协程结束
//            }
//        }
    }

    //重写，释放所有资源
    override fun suspend() {
        try {
            this.path=null
            super.suspend()
        } catch (e: Exception) {
            e.printStackTrace()
            KLoggerUtils.e("video销毁异常;suspend():\t" + e.message)
        }
    }

    //销毁
    fun onDestory() {
        try {
            setOnClickListener(null)
            setOnTouchListener(null)
            setOnCompletionListener(null)
            onStart = null
            onResume = null
            onPause = null
            onSeekTo = null
            mediaPlayer?.setOnSeekCompleteListener(null)
            mediaPlayer = null
            path = null
            job?.cancel()
            job = null
            onSeekListener = null
            isProgress = false
            suspend()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
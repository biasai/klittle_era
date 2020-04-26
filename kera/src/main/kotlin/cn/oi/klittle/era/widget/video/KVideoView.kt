package cn.oi.klittle.era.widget.video

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.VideoView
import cn.oi.klittle.era.utils.KFileUtils
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.utils.KRegexUtils
import cn.oi.klittle.era.utils.KStringUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.Deferred
import org.jetbrains.anko.runOnUiThread

//                fixme 调用案例
//                var video: KVideoView? = null
//                relativeLayout {
//                    backgroundColor=Color.BLACK//fixme 不要直接给Video设置背景色，会覆盖视频内容的。video没有视频内容时，默认就是黑色的。
//                    video = kvideoView {
//                    }.lparams {
//                        width = wrapContent
//                        height = wrapContent
//                        centerInParent()
//                    }
//                }.lparams {
//                    width = matchParent
//                    height = (kpx.screenWidth() / 16f * 9).toInt()//现在视频比例一般都为16：9；fixme 宽度固定最好写在父容器里，video自适应。
//                }
//                KMediaController(this@verticalLayout, video)
//                var videoPath: String? = null
//                button {
//                    text = "视频选择"
//                    onClick {
//                        pictrueSelectorForPath(type = PictureConfig.TYPE_VIDEO) {
//                            it?.let {
//                                if (it.size > 0) {
//                                    videoPath = it[0]
//                                    video?.prepare(videoPath)
//                                }
//                            }
//                        }
//                    }
//                }

//                   setVideoPath(path)//加载本地视频，同样也支持网络视频。如果字符串是网络url，同样可以播放。
//                   //在视频预处理完成后被调用。
//                   setOnPreparedListener {
//                      start()
//                      pause()//立即播放和暂停（这样画面会停留在第一帧）；如果不播放整个控件的画面就是黑的。什么都没有
//                    }

//                    var path = "/storage/emulated/0/tencent/MicroMsg/WeiXin/1576322118470.mp4"
//                    prepare(path) {
//                       //预加载完成,默认会自动播放
//                    }

//                   //原生播放完成（画面会停留在最后一帧）;fixme 循环播放时，不会回调。不循环播放时，播放完成时才会回调。
//                   setOnCompletionListener {
//                   }

//                    //fixme 自己的播放完成回调。建议使用。原生的有Bug(修复播放完成之后，currentPosition时间不准确的问题)
//                    onCompletionListener {
//                        KLoggerUtils.e("setOnCompletionListener()播放完成：\t" + isPlaying + "\t" + currentPosition + "\t" + duration+"\t"+mediaPlayer?.currentPosition)
//                        KToast.showInfo("播放完成")
//                    }

//                   setOnErrorListener { mp, what, extra ->
//                     //播放错误监听
//                     true
//                   }

//                    //播放进度监听(回调在ui主线程中)
//                    onSeekListener {
//                        //process 播放进度（0~1）
//                        //getProcessPercent()播放进度百分比
//                        //currentPosition 当前播放时间，duration视频播放总时间；单位都是毫秒。duration为-1表示没有视频资源。
//                    }

//                    fixme 常用方法
//                    start()//播放(暂停之后，会继续播放)
//                    pause()//暂停
//                    toggle()//播放暂停；切换
//                    resume()//重新播放（从第一帧开始播放，不是继续播放）；
//                    seekTo()//跳转到指定播放时间(视频会自动播放)
//                    onSeekTo{}//seekTo()调用之后，会回调。
//                    isPlaying//判断是否正在播放
//                    path//当前视频播放路径
//                    suspend()资源释放，将VideoView所占用的视频资源释放掉
//                    getCurrentPositionTimeParse()//当前播放时间，毫秒转分钟格式：00:00
//                    getDurationTimeParse()//视频总时长，转分钟格式：00:00
//                    setVolume()//设置音量
//                    setLooping(true)//循环播放
//                    isLooping()//判断是否循环播放
//                    getName()//获取视频名称(包括文件后缀名)
//                    getName2()//获取视频名称(不包括文件后缀名)

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
    var mediaPlayer: MediaPlayer? = null//fixme videoView内部封装的就是一个mediaPlayer。
    var path: String? = null//当前的播放视频路径

    /**
     * 获取视频名称（带后缀名）
     */
    fun getName(): String? {
        path?.trim()?.let {
            if (it.length > 0) {
                return KFileUtils.getInstance().getFileName(it)
            }
        }
        return null
    }

    /**
     * 获取视频名称（不带后缀名）
     */
    fun getName2(): String? {
        path?.trim()?.let {
            if (it.length > 0) {
                return KFileUtils.getInstance().getFileName2(it)
            }
        }
        return null
    }

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
                this.path = path//在suspend（）后面赋值，防止被释放掉。
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
                    setVolume()//设置音量
                    setLooping()//是否循环播放
                    mediaPlayer?.setOnSeekCompleteListener {
                        //seekTo 方法完成时的回调
                        start()//跳转到指定播放时间之后，立即自动播放；fixme 同时解决，视频播放时间不准确的问题。
                        onSeekTo?.let {
                            it()
                        }
                    }
                    GlobalScope.async {
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
                            kMediaController?.updateView()
                            kMediaController2?.updateView()
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
            isOnComplet = false
            //防止重复回调。
            onStart?.let {
                it()
            }
            reStartOnSeekListener()//防止进度回调死掉
            kMediaController?.updateView()
            kMediaController2?.updateView()
        }
        isResume = false
    }

    private var isResume = false
    //重新播放（从第一帧开始播放，不是继续播放）；
    override fun resume() {
        isResume = true
        isOnComplet = false
        super.resume()//内部调用了start()方法(异步调用，不是同步哦。)。
        onResume?.let {
            it()
        }
        kMediaController?.updateView()
        kMediaController2?.updateView()
    }

    //暂停
    override fun pause() {
        super.pause()//isPlaying会变成false
        onPause?.let {
            it()
        }
        kMediaController?.updateView()
        kMediaController2?.updateView()
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
        if (msec < duration) {
            isOnComplet = false
        } else {
            isOnComplet = true
        }
        //fixme msec小于0；内部会默认作为0处理
        //fixme msec大于等于duration;视频播放时，不会立即播放尾帧。而是倒退一两秒播放。(亲测)
        //fixme seek只能seek到关键帧，否则无法播放，如91800的位置不是关键帧，所以会往前找，直到找到关键帧，87000(假设)应该就是关键帧的位置了。
        super.seekTo(msec)//fixme =================跳转该时间进行播放，如果视频是暂停的。会自动播放。======================
        setOnCompletionListener(getOnCompletionListener())//fixme 调用了seekTo()之后，完成回调的currentPosition就不准确了。修复这个异常

    }

    /**
     * 跳转道指定进度
     * @param process 进度(0~1f)
     */
    fun seekTo(process: Float?) {
        if (process!=null) {
            seekTo((duration * process).toInt())
        }
    }

    private var onCompletionListener: (() -> Unit)? = null
    //fixme 自己的播放完成回调。建议使用。(修复播放完成之后，currentPosition时间不准确的问题)
    fun onCompletionListener(onCompletionListener: (() -> Unit)? = null) {
        this.onCompletionListener = onCompletionListener
        if (onCompletionListener != null) {
            setOnCompletionListener(getOnCompletionListener())
        }
    }

    private var isOnComplet: Boolean = false//判断是否完成
    private var l: MediaPlayer.OnCompletionListener? = null
    private fun getOnCompletionListener(): MediaPlayer.OnCompletionListener? {
        if (l == null) {
            l = MediaPlayer.OnCompletionListener() {
                isOnComplet = true
                onCompletionListener?.let {
                    it()//fixme 播放完成回调。
                }
            }
        }
        return l
    }

    //fixme 这个是原生的播放完成回调；不要使用；建议使用onCompletionListener()自己的完成回调。
    override fun setOnCompletionListener(l: MediaPlayer.OnCompletionListener?) {
        super.setOnCompletionListener(l)
    }

    private var leftVolume: Float? = null
    private var rightVolume: Float? = null
    /**
     * 控制音量，左声道和有声道（范围0~1）
     */
    fun setVolume(leftVolume: Float? = this.leftVolume, rightVolume: Float? = this.rightVolume) {
        if (leftVolume != null && rightVolume != null) {
            this.leftVolume = leftVolume
            this.rightVolume = rightVolume
            this.leftVolume?.let {
                if (it > 1f) {
                    this.leftVolume = 1f
                }
                if (it < 0f) {
                    this.leftVolume = 0f
                }
            }
            this.rightVolume?.let {
                if (it > 1f) {
                    this.rightVolume = 1f
                }
                if (it < 0f) {
                    this.rightVolume = 0f
                }
            }
            mediaPlayer?.setVolume(this.leftVolume!!, this.rightVolume!!)
        }
    }

    private var isLooping: Boolean = false

    //判断是否循环播放
    fun isLooping(): Boolean {
        return isLooping
    }

    /**
     * 是否循环播放；(亲测有效)
     * @param isLooping true循环播放；false不循环播放。
     */
    fun setLooping(isLooping: Boolean = this.isLooping) {
        this.isLooping = isLooping
        mediaPlayer?.isLooping = this.isLooping
        if (isLooping && currentPosition == duration) {
            start()//播放完成。则自动播放一下。
        }
        if (isLooping) {
            isOnComplet = false
        }
    }

    //seekTo回调
    var onSeekTo: (() -> Unit)? = null

    fun onSeekTo(onSeekTo: (() -> Unit)? = null) {
        this.onSeekTo = onSeekTo
    }

    //获取当前的播放时长; 毫秒转分钟格式。如： 00:00
    fun getCurrentPositionTimeParse(): String {
        //currentPosition当前的播放时长，单位毫秒;fixme currentPosition初始值一般是0
        return KStringUtils.stringForTime(currentPosition)//转换为时间格式 fixme currentPosition 单位毫秒
    }

    /**
     * @param currentPosition 自己传入的时间；转毫秒转分钟格式。如： 00:00
     */
    fun getCurrentPositionTimeParse(currentPosition: Int): String {
        //currentPosition当前的播放时长，单位毫秒;fixme currentPosition初始值一般是0
        return KStringUtils.stringForTime(currentPosition)//转换为时间格式 fixme currentPosition 单位毫秒
    }

    //获取视频总时长
    fun getDurationTimeParse(): String {
        //duration视频总时长;如：00:13 ；fixme duration初始值是-1(没有视频时)
        return KStringUtils.stringForTime(duration)//转换为时间格式;fixme duration 单位毫秒
    }


    var onSeekListener: (() -> Unit)? = null//进度回调
    private var isProgress: Boolean = false//是否正在刷新进度
    private var job: Deferred<Any?>? = null
    var process: Float = 0F//播放进度(0~1)
    private var preProcess = -1f//记录上一次的播放进度

    override fun getCurrentPosition(): Int {
        super.getCurrentPosition()?.let {
            if ((duration > 0 && it == 100) || isOnComplet) {//fixme 修复播放完毕之后，currentPosition会变成100的Bug。
                return duration
            } else {
                return it
            }
        }
    }

    /**
     * 获取播放进度，百分比。
     * @param keep 百分比，小数点后的个数。
     */
    open fun getProcessPercent(keep: Int = 2): String {
        if (currentPosition <= 0 && duration <= 0) {
            return "0%"
        } else if ((currentPosition == 100 && duration > 0) || currentPosition == duration) {//播放完成之后，currentPosition自动变成了100；这是给Bug
            return "100%"
        } else {
            return KStringUtils.getPercent(currentPosition.toLong(), duration.toLong(), keep)
        }
    }

    //防止进度回调死掉；在start()里调用了。
    private fun reStartOnSeekListener() {
        onSeekListener?.let {
            job?.let {
                if (isProgress) {
                    //KLoggerUtils.e("isActive:\t" + job?.isActive + "\tisCompleted:\t" + job?.isCompleted + "\tisCancelled:\t" + job?.isCancelled)
                    if (!it.isActive && !it.isCancelled && !it.isCompleted) {
                        onSeekListener(this.onSeekListener)
                    }
                }
            }
        }
    }

    var kMediaController: KMediaController? = null

    fun setMediaController(kMediaController: KMediaController) {
        this.kMediaController = kMediaController
        onSeekListener2()
    }

    var kMediaController2: KMediaController2? = null

    fun setMediaController(kMediaController2: KMediaController2) {
        this.kMediaController2 = kMediaController2
        onSeekListener2()
    }

    fun onSeekListener(onSeekListener: (() -> Unit)? = null) {
        this.onSeekListener = onSeekListener
        preProcess = -1f;
        if (onSeekListener == null) {
            job?.cancel()
            job = null
            isProgress = false
        } else if (!isProgress || job == null || job?.isActive == false) {
            onSeekListener2()
        }
    }

    private fun onSeekListener2() {
        isProgress = true
        job?.cancel()//取消协程
        job = GlobalScope.async {
            while (isProgress) {
                if (!isFinish()) {
                    if (currentPosition >= 0 && duration > 0) {
                        process = currentPosition.toFloat() / duration.toFloat()
                    } else {
                        process = 0f
                    }
                    this@KVideoView?.context?.let {
                        if (it is Activity) {
                            if (!it.isFinishing) {
                                if (preProcess != process) {
                                    preProcess = process//防止重复回调。
                                    it.runOnUiThread {
                                        kMediaController?.updateView()
                                        kMediaController2?.updateView()
                                        this@KVideoView.onSeekListener?.let {
                                            it()//进度在主线程中回调。
                                        }
                                    }
                                }
                                delay(500)//1000毫秒是一秒。
                            }
                        }
                    }
                } else {
                    isProgress = false
                }
            }
            job = null//协程结束
        }
    }

    //判断Activity是否销毁;true已经销毁，false没有销毁
    private fun isFinish(): Boolean {
        if (!isDestory) {
            context?.let {
                if (it is Activity) {
                    if (!it.isFinishing) {
                        return false
                    }
                }
            }
        }
        return true
    }

    //重写，释放所有资源；系统会时不时的自动释放。不要在这里置空任何对象。
    override fun suspend() {
        try {
            super.suspend()
        } catch (e: Exception) {
            e.printStackTrace()
            KLoggerUtils.e("video销毁异常;suspend():\t" + e.message)
        }
    }

    var isDestory = false//判断是否销毁
    //销毁
    fun onDestory() {
        try {
            isDestory = true
            setOnClickListener(null)
            setOnTouchListener(null)
            l = null
            setOnCompletionListener(l)
            setOnCompletionListener(null)
            onCompletionListener = null
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
            kMediaController = null
            kMediaController2 = null
            onSeekListener(null)
            suspend()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
package cn.oi.klittle.era.widget.video

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseUi.Companion.kview
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.helper.KUiHelper
import cn.oi.klittle.era.toolbar.KToolbar
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.widget.compat.KTextView
import cn.oi.klittle.era.widget.compat.KView
import cn.oi.klittle.era.widget.seekbar.KSeekBarProgressBar
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.jetbrains.anko.*

//      fixme 这个是争对全屏播放做的布局。
//      调用案例，参数一是父容器布局，参数二是KVideoView
//      KMediaController2(this@verticalLayout, video)

/**
 * fixme KVideoView视频播放的控制器布局
 */
open class KMediaController2 {
    var relativeLayout: RelativeLayout? = null//最外层布局
    var layout_top: View? = null
    var layout_bottom: View? = null
    var videoView: KVideoView? = null//视频播放器
    var play: KView? = null//播放按钮
    var seekbar: KSeekBarProgressBar? = null//进度条
    var currentPosition: KTextView? = null//当前播放时间
    var duration: KTextView? = null//视频总时长

    var leftTextView: KTextView? = null//返回键
    var leftTextView_txt: KTextView? = null//返回键旁边的文本
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

    /**
     * 隐藏和显示
     * @param isHidden 是否强制隐藏
     */
    open fun hidden(isHidden: Boolean? = null) {
        isHidden?.let {
            if (it) {
                layout_top?.context?.let {
                    if (it is Activity) {
                        if (!it.isFinishing) {
                            it.runOnUiThread {
                                //fixme 隐藏
                                layout_top?.visibility = View.INVISIBLE
                                layout_bottom?.visibility = View.INVISIBLE
                            }
                        }
                    }
                }
                return
            }
        }
        layout_top?.let {
            if (it.visibility == View.INVISIBLE) {
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.INVISIBLE
            }
        }
        layout_bottom?.let {
            if (it.visibility == View.INVISIBLE) {
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.INVISIBLE
            }
        }
    }

    constructor(viewGroup: ViewGroup?, videoView: KVideoView?) {
        this.videoView = videoView
        videoView?.setMediaController(this)//fixme 绑定
        viewGroup?.context?.apply {
            relativeLayout = relativeLayout {
                //顶部，返回键
                layout_top = linearLayout {
                    id = kpx.id("mediaController_top")
                    gravity = Gravity.CENTER_VERTICAL
                    //返回键
                    leftTextView = KTextView(this).apply {
                        id = kpx.id("mediaController_back")
                        textSize = kpx.textSizeY(32, false)
                        setTextColor(Color.WHITE)
                        gravity = Gravity.CENTER or Gravity.LEFT
                        isRecycleAutoBg = false//返回键不释放
                        autoBg {
                            width = kpx.x(24)//和PDA的返回键大小保持一致。
                            height = kpx.x(41)
                            autoBg(R.mipmap.kera_top_back_white)
                            autoBgColor = Color.WHITE
                            isAutoCenterVertical = true
                            isAutoCenterHorizontal = false
                            autoLeftPadding = kpx.x(24f)
                        }
                        leftPadding = kpx.x(30)
                        isClickable = true
                        onClick {
                            if (visibility == View.VISIBLE) {
                                //返回键显示的时候，默认点击关闭
                                ctx?.let {
                                    if (it is Activity) {
                                        if (!it.isFinishing) {
                                            it.finish()
                                        }
                                    }
                                }
                            }
                        }
                    }.lparams {
                        width = kpx.y(50)
                        height = kpx.y(50)
                        topMargin = kpx.x(12)
                    }
                    leftTextView_txt = KTextView(this).apply {
                        id = kpx.id("mediaController_back_txt")
                        textSize = kpx.textSizeY(32, false)
                        setTextColor(Color.WHITE)
                        gravity = Gravity.CENTER or Gravity.LEFT
                        setMore(1)
                        isClickable = true
                        onClick {
                            if (visibility == View.VISIBLE) {
                                ctx?.let {
                                    if (it is Activity) {
                                        if (!it.isFinishing) {
                                            it.finish()
                                        }
                                    }
                                }
                            }
                        }
                    }.lparams {
                        width = wrapContent
                        height = wrapContent
                        topMargin = kpx.x(12)
                        leftMargin = kpx.x(12)
                    }
                }.lparams {
                    width = matchParent
                    height = wrapContent
                    centerHorizontally()
                    alignParentTop()
                }
                //中间（视频播放区域）
                relativeLayout {
                    kview {
                        //单击（进度条和返回键 隐藏和显示）
                        onSingleTapConfirmed {
                            hidden()
                            dispose()//fixme 自动隐藏
                        }
                        //双击（视频播放和暂停）
                        onDoubleTap {
                            videoView?.toggle()
                        }
                        var isOnscroll = false//是否进行了滑动
                        //手指按下
                        onDown {
                            isOnscroll = false
                        }
                        //滑动快进
                        onScroll { e1, e2, distanceX, distanceY ->
                            //KLoggerUtils.e("distanceX:\t" + distanceX)
                            if (distanceX > 0) {
                                //向左滑，快退
                                isOnscroll = true
                            } else if (distanceX < 0) {
                                //向右滑，快进
                                isOnscroll = true
                            }
                        }
                        onUp {
                            //KLoggerUtils.e("手指离开")
                            if (isOnscroll) {
                                //进行了滑动操作
                            }
                            isOnscroll = false
                        }
                    }.lparams {
                        width = matchParent
                        height = matchParent
                    }
                }.lparams {
                    width = matchParent
                    height = matchParent
                    centerInParent()
                    topMargin = kpx.x(80)
                    bottomMargin = kpx.x(80)
                    //below(kpx.id("mediaController_top"))
                    //above(kpx.id("mediaController_bottom"))
                }
                //播放按钮+进度条
                layout_bottom = relativeLayout {
                    id = kpx.id("mediaController_bottom")
                    //播放按钮
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
                                dispose()//fixme 自动隐藏
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
                            all_radius(kpx.x(33f))
                            bg_color = Color.parseColor("#1F95FF")
                        }
                        //fixme 视频还是不需要拇指滚动条比较好。
                        //fixme 防止宽高获取不到，防止滑动块显示异常；最好在布局加载完成之后，在设置进度。
                        //fixme isAlways = true，防止布局发生变化。防止不实时，不准确。
                        onGlobalLayoutListener(isAlways = true) {
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
                            if (isActionDown || isActionUp) {
                                //就在手指按下，和离开的时候调用即可。不要在触摸的时候调用。效果不好。
                                dispose()//fixme 自动隐藏
                            }
                        }
                    }.lparams {
                        width = matchParent
                        height = kpx.x(64)
                        centerVertically()
                        leftOf(kpx.id("klayout_right"))
                        rightOf(kpx.id("klayout_left"))
                    }
                    //视频总长度
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
                            rightMargin = kpx.x(12)
                        }
                        //全屏
//                    screen = kview {
//                        id = kpx.id("kera_media_screen")
//                        autoBg {
//                            width = kpx.x(64) / 2
//                            height = kpx.x(64) / 2
//                            autoBg(R.mipmap.kera_media_screen_)
//                        }
//                        onClick {
//                            screen()
//                        }
//                    }.lparams {
//                        width = kpx.x(64)
//                        height = kpx.x(64)
//                    }
                    }.lparams {
                        width = wrapContent
                        height = wrapContent
                        centerVertically()
                        alignParentRight()
                    }
                }.lparams {
                    width = matchParent
                    height = wrapContent
                    centerHorizontally()
                    alignParentBottom()
                }
            }
            //一般默认就是 width = matchParent;height = wrapContent
            viewGroup?.addView(relativeLayout, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            dispose()//fixme 默认自动隐藏
        }
    }

    //被观察者
    private var observable: Observable<Boolean>? = Observable.create<Boolean> {
        GlobalScope.async {
            delay(6000)//六秒
            it.onComplete()
        }
    }
            .subscribeOn(Schedulers.io())//执行线程在io线程(子线程)
            .observeOn(AndroidSchedulers.mainThread())//回调线程在主线程

    private var showTime: Long? = 0//记录显示的时间
    var disposable: Disposable? = null
    //观察者
    private var observe: Observer<Boolean>? = object : Observer<Boolean> {
        override fun onSubscribe(d: Disposable?) {
            disposable = d
        }

        override fun onComplete() {
            showTime?.let {
                hidden(true)//隐藏
            }
        }

        override fun onNext(value: Boolean?) {
        }

        override fun onError(e: Throwable?) {
        }
    }

    //取消和重新开始 fixme （自动隐藏）
    fun dispose() {
        try {
            //使用RxJava完成弹框超时设置。亲测可行。
            if (observe != null) {//timeOut小于等于0不做超时判断
                showTime = System.currentTimeMillis()//记录显示的时间
                disposable?.dispose()//旧的回调取消
                observable?.subscribe(observe)//开启新的回调计时
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //销毁
    fun onDestroy() {
        disposable?.dispose()//旧的回调取消
        disposable = null
        relativeLayout = null
        layout_top = null
        layout_bottom = null
        videoView?.onDestory()
        videoView = null
        play = null
        seekbar?.onDestroy()
        seekbar = null
        currentPosition = null
        duration = null
        leftTextView?.onDestroy()
        leftTextView = null
        leftTextView_txt?.onDestroy()
        leftTextView_txt = null
    }

}
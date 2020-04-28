package cn.oi.klittle.era.activity.preview.adpater

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.viewpager.widget.PagerAdapter
import cn.oi.klittle.era.R
import cn.oi.klittle.era.activity.photo.entity.KLocalMedia
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KAssetsUtils
import cn.oi.klittle.era.utils.KGlideUtils
import cn.oi.klittle.era.widget.compat.KView
import cn.oi.klittle.era.widget.photo.KPhotoView
import cn.oi.klittle.era.widget.video.KVideoView
import org.jetbrains.anko.*
//import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onClick

open class KPreviewPagerAdapter(var datas: MutableList<KLocalMedia>?) : PagerAdapter() {
    var keys = mutableMapOf<Int, String>()
    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view === obj//只有返回true时。才会显示视图
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        //super.destroyItem(container, position, obj)
        container?.removeView(obj as View)
        KAssetsUtils.getInstance().recycleBitmap(keys.get(position))//fixme 位图释放
        videoMap?.get(position)?.let {
            it.suspend()//释放
        }
        videoMap?.remove(position)
    }

    //在addOnPageChangeListener{}里面进行了暂停操作。
    var videoMap: MutableMap<Int, VideoView?>? = mutableMapOf()//缓存视频控件

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var itemView = container.context.UI {
            verticalLayout {
                gravity = Gravity.CENTER
                backgroundColor = Color.BLACK
                datas?.let {
                    if (it.size > position) {
                        datas?.get(position)?.let {
                            if (it.isVideo() || it.isAudio()) {
                                relativeLayout {
                                    //视频音频
                                    var video = KVideoView(this).apply {
                                        var path = it.path
                                        if (it.isCompressed && it.compressPath != null) {
                                            path = it.compressPath
                                        }
                                        prepare(path)
                                    }.lparams {
                                        width = matchParent
                                        height = matchParent
                                        centerInParent()//居中
                                    }
                                    videoMap?.put(position, video)
                                    var kView = KView(ctx).apply {
                                        autoBg {
                                            isCenterCrop = true
                                            width = kpx.x(105)
                                            height = kpx.x(100)
                                            autoBg(R.mipmap.kera_play2)
                                        }
                                        video?.apply {
                                            //播放完成（画面会停留在最后一帧）
                                            setOnCompletionListener {
                                                //播放完成
                                                autoBg {
                                                    isDraw = true
                                                }
                                            }
                                            setOnErrorListener { mp, what, extra ->
                                                //播放错误
                                                autoBg {
                                                    isDraw = true
                                                }
                                                true
                                            }
                                            onStart {
                                                autoBg {
                                                    isDraw = false
                                                }
                                            }
                                            onPause {
                                                autoBg {
                                                    isDraw = true
                                                }
                                            }
                                        }
                                        onClick {
                                            video?.toggle()
                                        }
                                        onDoubleTap {
                                            video?.pause()//暂停
                                            ctx?.let {
                                                if (it is Activity) {
                                                    it.finish()//双击关闭
                                                }
                                            }
                                        }
                                    }.lparams {
                                        width = matchParent
                                        height = matchParent
                                        centerInParent()
                                    }
                                    addView(kView)
                                }.lparams {
                                    width = matchParent
                                    height = matchParent
                                }
                            } else {
                                //图片
                                //PhotoView是第三方PictureSelector图片选择器里的控件
                                var photoView = KPhotoView(ctx).apply {
                                    datas?.let {
                                        if (it.size > position) {
                                            datas?.get(position)?.let {
                                                if (it.isGif()) {
                                                    //fixme Gif动态图片;(gif不能压缩，压缩之后就没有反应了。)
                                                    KGlideUtils.setGif(it.path, 480, 800, this)
                                                } else {
                                                    var path = it.path
                                                    if (it.isCompressed && it.compressPath != null) {
                                                        path = it.compressPath
                                                    }
                                                    //KLoggerUtils.e("path:\t"+it.path+"\tcompressPath:\t"+it.compressPath)
                                                    if (path != null) {
                                                        KGlideUtils.getBitmapFromPath(path, 480, 800, isCenterCrop = false) { key, bitmap ->
                                                            ctx?.runOnUiThread {
                                                                if (bitmap != null && !bitmap.isRecycled) {
                                                                    setImageBitmap(bitmap)
                                                                }
                                                            }
                                                            keys?.put(position, key)//fixme 保存位图对应的键值key
                                                        }
                                                    } else {
                                                    }
                                                }
                                            }
                                            onClick {
                                                ctx?.let {
                                                    if (it is Activity) {
                                                        it.finish()//单击关闭
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }.lparams {
                                    width = matchParent
                                    height = matchParent
                                    leftMargin = kpx.x(4)
                                    rightMargin = leftMargin
                                }
                                addView(photoView)
                            }
                        }
                    }
                }
            }
        }.view
        container.addView(itemView)//fixme 注意，必不可少。不然显示不出来。这里是itemView，不是view哦。之前就写错了，死活不出来
        return itemView
    }

    var mCount = datas?.size ?: 0
    override fun getCount(): Int {
        //不要return datas?.size?:0 这样很容易异常。如果getCount()每次返回的值不一样。会很容易异常崩溃的。
        //所以为了安全，最好将getCount()一开始就初始化固定。
        return mCount
    }

}

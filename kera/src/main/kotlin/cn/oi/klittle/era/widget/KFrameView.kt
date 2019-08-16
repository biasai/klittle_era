package cn.oi.klittle.era.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.base.KBaseView
import cn.oi.klittle.era.utils.KAssetsUtils

/**
 * 帧动画控件
 */
open class KFrameView : KBaseView {

    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//默认就开启硬件加速，不然圆角无效果
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(viewGroup: ViewGroup, HARDWARE: Boolean) : super(viewGroup.context) {
        if (HARDWARE) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context?) : super(context, true) {}

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    var bitmaps = arrayListOf<Bitmap?>()//位图集合
    var position: Int = 0//当前显示位图下标
    //资源文件
    //w,h指定位图宽和高
    //调用案例：addFrameFromAssets("eyes/" + name, w = px.x(126), h = px.x(102))
    open fun addFrame(vararg resid: Int, isRGB_565: Boolean = true, w: Int = 0, h: Int = 0) {
        for (i in 0 until resid.size) {
            var bitmap = KAssetsUtils.getInstance().getBitmapFromAssets(null, resid[i], isRGB_565)
            addFrameBitmap(bitmap, isRGB_565, w, h)
        }
    }

    /**
     * path assets 里的文件。如("文件夹/文件名.后缀")
     */
    open fun addFrameFromAssets(vararg path: String, isRGB_565: Boolean = true, w: Int = 0, h: Int = 0) {
        for (i in 0 until path.size) {
            var bitmap = KAssetsUtils.getInstance().getBitmapFromAssets(path[i], 0, isRGB_565)
            addFrameBitmap(bitmap, isRGB_565, w, h)
        }
    }

    /**
     * path SD卡 里的文件。完整路径
     */
    open fun addFrameFromFile(vararg path: String, isRGB_565: Boolean = true, w: Int = 0, h: Int = 0) {
        for (i in 0 until path.size) {
            var bitmap = KAssetsUtils.getInstance().getBitmapFromFile(path[i], isRGB_565)
            addFrameBitmap(bitmap, isRGB_565, w, h)
        }
    }

    open fun addFrameBitmap(bitmap: Bitmap, isRGB_565: Boolean = true, w: Int = 0, h: Int = 0) {
        bitmap?.let {
            if ((it.width != w || it.height != h) && (w > 0 && h > 0)) {
                var bitmap2 = Bitmap.createScaledBitmap(it, w, h, true)
                it.recycle()//释放原有的
                bitmaps.add(bitmap2)
            } else {
                bitmaps.add(bitmap)
            }
        }
    }

    /**
     * 释放位图
     */
    open fun recycleBitmaps() {
        for (i in 0 until bitmaps.size) {
            var bitmap = bitmaps[i]
            bitmap?.let {
                if (!it.isRecycled) {
                    it.recycle()
                }
            }
        }
        bitmaps.clear()
    }

    override fun draw2(canvas: Canvas, paint: Paint) {
        super.draw2(canvas, paint)
        if (bitmaps.size > position && position >= 0) {
            var bitmap = bitmaps[position]
            bitmap?.let {
                if (!it.isRecycled) {
                    canvas.drawBitmap(bitmap, 0f, 0f, paint)
                }
            }
        }
    }

    var isOrder = false//动画是否顺序，false顺序动画还没执行。true顺序动画已经执行。
    /**
     * 开始帧动画(顺序)
     * duration时间 单位毫秒
     * repeatCount 动画次数，默认一次。0就代表一次。1是两次
     * callback 回调，每一帧都回调，返回当前图片的下标
     */
    open fun startOrderAnime(duration: Long = 400, repeatCount: Int = 0, callback: ((postion: Int) -> Unit)? = null): ObjectAnimator? {
        var end = bitmaps.size
        if (end > 0) {
            end -= 1
            isOrder = true
            var pos = position
            if (repeatCount > 0) {
                pos = 0//动画两次以上，防止错乱
            }
            return ofInt("position", repeatCount, duration, pos, end) {
                if (callback != null) {
                    callback(it)
                }
            }
        }
        return null
    }

    /**
     * 开始帧动画(倒序)
     * duration时间 单位毫秒
     * repeatCount 动画次数，默认一次。0就代表一次。1是两次
     * callback 回调，每一帧都回调，返回当前图片的下标
     */
    open fun startReverseAnime(duration: Long = 400, repeatCount: Int = 0, callback: ((postion: Int) -> Unit)? = null): ObjectAnimator? {
        var end = bitmaps.size
        if (end > 0) {
            end -= 1
            isOrder = false
            var pos = position
            if (repeatCount > 0) {
                pos = end//动画两次以上，防止错乱
            }
            return ofInt("position", repeatCount, duration, pos, 0) {
                if (callback != null) {
                    callback(it)
                }
            }
        }
        return null
    }

    /**
     * 自动判断是进行顺序还是倒叙动画
     */
    open fun startToogleAnime(duration: Long = 400, repeatCount: Int = 0, callback: ((postion: Int) -> Unit)? = null): ObjectAnimator? {
        if (!isOrder) {
            return startOrderAnime(duration, repeatCount, callback)
        } else {
            return startReverseAnime(duration, repeatCount, callback)
        }
    }

    /**
     * 动画从开始到结束。再从结束到开始。即一个来回动画。默认次数最大值。即无限轮播。
     */
    open fun startCirCleAnime(duration: Long = 400, repeatCount: Int = Int.MAX_VALUE, callback: ((postion: Int) -> Unit)? = null): ObjectAnimator? {
        var end = bitmaps.size
        if (end > 0) {
            end -= 1
            isOrder = true
            return ofInt("position", repeatCount, duration, 0, end, 0) {
                if (callback != null) {
                    callback(it)
                }
            }
        }
        return null
    }

}
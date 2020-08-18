package cn.oi.klittle.era.view

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import cn.oi.klittle.era.R
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.exception.KCatchException
import cn.oi.klittle.era.utils.KAssetsUtils
import cn.oi.klittle.era.utils.KGlideUtils
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.utils.KProportionUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.jetbrains.anko.runOnUiThread

/**
 * 圆形进度条控件; fixme KProgressDialog网络进度条有使用到。
 * Created by 彭治铭 on 2017/9/24.
 */
open class KProgressCircleView : View {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        mInit()
        viewGroup.addView(this)
    }

    constructor(context: Context?) : super(context) {
        mInit()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        mInit()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(kpx.x(150), kpx.x(150))//fixme 这个改动时，要注意 KProgressDialog里面的布局。里面有用到哦。
    }

    companion object {
        public var dst: Bitmap? = null//fixme 位图图片，共用，设置成静态的。

        // fixme 在KBaseActivity里的onCreate()初始化里有调用哦。
        public fun initProgressDstBitmap(callback: (() -> Unit)? = null) {
            if (dst == null || dst!!.isRecycled) {
                //fixme 之所以dst位图设置成静态的，是为了防止网络请求多次同时请求时，GlobalScope.async会有延迟。数量越多，延迟越严重。所以设置成静态的。
                //fixme 网络请求同时超过或等于4次，就会有延迟。亲测数量为四。
                GlobalScope.async {
                    try {
                        dst = KGlideUtils.getBitmapFromAssets("kera/progress/circleloading.png", kpx.x(128), kpx.x(128))
                        callback?.let { it() }
                    } catch (e: Exception) {
                        KLoggerUtils.e("网络进度条图片获取异常：\t" + KCatchException.getExceptionMsg(e), true)
                    }
                }
            }
        }
    }


    public var load: String? = "Loading"//文本
    private fun mInit() {
        if (dst == null || dst!!.isRecycled) {
            initProgressDstBitmap() {
                postInvalidate()//fixme 画布刷新
            }
        }
    }

    var paint: Paint? = null
    var degress = 0f
    override fun draw(canvas: Canvas) {
        try {
            super.draw(canvas)
            if (canvas != null && dst != null && !dst!!.isRecycled) {
                if (paint == null) {
                    paint = Paint()
                    paint!!.isAntiAlias = true
                    paint!!.isDither = true
                    //paint!!.colorFilter = LightingColorFilter(Color.TRANSPARENT, Color.WHITE) //fixme 图片变为白色。（当前图片本身就是白色，不需要在渲染。）
                    setLayerType(LAYER_TYPE_HARDWARE, paint)
                    paint?.textAlign = Paint.Align.CENTER
                    paint?.color = Color.WHITE
                    paint?.textSize = kpx.x(22f)
                    paint?.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//字体加粗
                }
                if (dst != null && !dst!!.isRecycled) {
                    canvas.save()
                    if (degress >= Float.MAX_VALUE - 4) {
                        degress = 0f
                    }
                    degress += 4
                    canvas.rotate(degress, width / 2f, height / 2f)
                    canvas.drawBitmap(dst, (width - dst!!.getWidth()) / 2f, (height - dst!!.getHeight()) / 2f, paint)//fixme 旋转的图片
                    canvas.restore()
                }
                load?.trim()?.let {
                    if (it.length > 0) {
                        paint?.let {
                            if (load != null) {
                                canvas.drawText(load, width / 2f, height / 2f + it.textSize / 2, paint)//fixme 文本
                            }
                        }
                    }
                }
                invalidate()
            }
        } catch (e: Exception) {
            KLoggerUtils.e("网络进度条画布绘制异常：\t" + KCatchException.getExceptionMsg(e), true)
        }
    }
}
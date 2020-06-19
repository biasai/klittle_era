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

/**
 * 圆形进度条控件;fixme KSubmitProgressDialog提交弹框有使用到。
 * Created by 彭治铭 on 2017/9/24.
 */
open class KProgressCircleView2 : View {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        init()
        viewGroup.addView(this)
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(kpx.x(150), kpx.x(150))//fixme 这个改动时，要注意 KSubmitProgressDialog里面的布局。里面有用到哦。

    }

    var dst: Bitmap? = null//位图图片
    private fun init() {
        GlobalScope.async {
            dst = KGlideUtils.getBitmapFromResouce(R.mipmap.kera_loading, kpx.x(90), kpx.x(90))
            postInvalidate()
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
                    if (degress >= Float.MAX_VALUE - 3) {
                        degress = 0f
                    }
                    degress += 3
                    canvas.rotate(degress, width / 2f, height / 2f)
                    canvas.drawBitmap(dst, (width - dst!!.getWidth()) / 2f, (height - dst!!.getHeight()) / 2f, paint)//fixme 旋转的图片
                    canvas.restore()
                }
                invalidate()
            }
        } catch (e: Exception) {
            KLoggerUtils.e("网络提交进度条异常：\t" + KCatchException.getExceptionMsg(e))
        }
    }
}
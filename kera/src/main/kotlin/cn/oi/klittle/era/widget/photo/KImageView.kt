package cn.oi.klittle.era.widget.photo

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView

/**
 * 普通的ImageView，防止内部图片为空
 */
class KImageView : ImageView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    override fun draw(canvas: Canvas?) {
        try {
            drawable?.let {
                if (it is BitmapDrawable) {
                    it.bitmap?.let {
                        if (it.isRecycled) {
                            //KLoggerUtils.e("KPhotoView位图已经释放")
                            //fixme 亲测能够解决图片异常释放问题。
                            return//图片已经释放了，就不要继续执行了。会报错的。
                        }
                    }
                }
            }
            super.draw(canvas)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}
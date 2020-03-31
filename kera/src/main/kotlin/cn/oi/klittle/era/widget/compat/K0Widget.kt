package cn.oi.klittle.era.widget.compat

import android.content.Context
import android.graphics.*
import android.media.MediaPlayer
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import cn.oi.klittle.era.base.KBaseApplication
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.base.KBaseView
import cn.oi.klittle.era.utils.KAssetsUtils
import cn.oi.klittle.era.utils.KLoggerUtils
import java.lang.Exception

/**
 * 0：初始和静态方法。fixme 改成继承TextView；不要继承Button(问题老多了。)
 */
open class K0Widget : TextView {
    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    //ishardware是否进行硬件加速
    constructor(viewGroup: ViewGroup, ishardware: Boolean) : super(viewGroup.context) {
        if (ishardware) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    //fixme 初始化(加载完成可以调用addOnGlobalLayoutListener进行监听)
    //fixme 之所以加上m;是为了防止和init()方法冲突。无法识别。(与BaseUi自定义控件实例化的时候就冲突了)
    fun mInit(init: (() -> Unit)? = null) {
        init?.let {
            it()//fixme 自己调用自己;即马上执行
        }
    }

    private var isGone = false//判断是否执行过
    private var isAlways = false//fixme 是否每次加载完毕都回调；true每次都回调，false只回调一次。
    var onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    //fixme invalidate()重新和重新布局时，都会回调。
    fun onGlobalLayoutListener(isAlways: Boolean = false, gone: (() -> Unit)? = null) {
        mGone(isAlways, gone)
    }

    //fixme gone布局加载完成之后调用，只调用一次。（addOnGlobalLayoutListener可能执行多次。）
    fun mGone(isAlways: Boolean = false, gone: (() -> Unit)? = null) {
        this.isAlways = isAlways
        if (gone != null) {
            //fixme viewTreeObserver?.addOnGlobalLayoutListener可以多次添加，互不影响(亲测)
            if (onGlobalLayoutListener != null && Build.VERSION.SDK_INT >= 16) {
                viewTreeObserver?.removeOnGlobalLayoutListener(onGlobalLayoutListener)
            }
            onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
                //宽和高不能为空，要返回具体的值。
                if (width > 0 && height > 0) {
                    //防止多次重复调用，只执行一次
                    if (!isGone || isAlways) {
                        isGone = true
                        gone?.let {
                            it()
                        }
                    }
                }
            }
            viewTreeObserver?.addOnGlobalLayoutListener(onGlobalLayoutListener)
        } else {
            if (onGlobalLayoutListener != null && Build.VERSION.SDK_INT >= 16) {
                viewTreeObserver?.removeOnGlobalLayoutListener(onGlobalLayoutListener)
            }
            onGlobalLayoutListener = null
        }
    }

    var viewGroup: View? = this//兼容组件,只是尽可能的兼容，不能保证百分百。

    //重置画笔
    fun resetPaint(paint: Paint): Paint {
        return KBaseView.resetPaint(paint)
    }

    companion object {

        //获取位图
        open fun getBitmapFromAssets(filePath: String, isRGB_565: Boolean = false): Bitmap {
            return KAssetsUtils.getInstance().getBitmapFromAssets(filePath, isRGB_565)
        }

        open fun getBitmapFromResource(resID: Int, isRGB_565: Boolean = false): Bitmap {
            return KAssetsUtils.getInstance().getBitmapFromResource(resID, isRGB_565)
        }

        open fun getBitmapFromFile(filePath: String, isRGB_565: Boolean = false): Bitmap {
            return KAssetsUtils.getInstance().getBitmapFromFile(filePath, isRGB_565)
        }

        /**
         * 获取非线性渐变颜色数组
         * @param length 渐变的长度
         * @param srcColors 原始渐变颜色数组
         */
        fun getNotLinearGradientColors(length: Int, srcColors: IntArray): IntArray {
            var colors = IntArray(length)
            var size = srcColors.size
            var max = length / size
            if (max > 1) {
                var index = 0
                for (i in 0..srcColors!!.lastIndex) {
                    for (j in 0..max - 1) {
                        colors[index++] = srcColors!![i]
                    }
                }
            }
            return colors
        }

        /**
         * 获取LinearGradient（不具备渐变效果）
         * @param start 开始位置
         * @param end 结束位置
         * @param srcColors 渐变颜色数组
         * @param isVertical true 垂直，false 水平
         */
        fun getNotLinearGradient(start: Float, end: Float, srcColors: IntArray, isVertical: Boolean, scrollY: Int = 0): LinearGradient? {
            var length = (end - start).toInt()
            var colors = IntArray(length)
            var size = srcColors.size
            var max = length / size
            if (max > 1) {
                var index = 0
                for (i in 0..srcColors!!.lastIndex) {
                    for (j in 0..max - 1) {
                        colors[index++] = srcColors!![i]
                    }
                }
                if (isVertical) {
                    //垂直
                    return LinearGradient(0f, start, 0f, end, colors, null, Shader.TileMode.MIRROR)
                } else {
                    //水平
                    return LinearGradient(start, 0f, end, 0f, colors, null, Shader.TileMode.MIRROR)
                }
            }
            return null
        }

        //fixme 按钮声音播放;静态音频文件，不会自动销毁。需要自己手动去释放。
        //fixme 在K1Widget头部有写使用案例。
        var sMediaPlayer: MediaPlayer? = null

        //释放掉音频；不会自动销毁。需要自己手动去释放。
        fun sReleaseMediaPlayer() {
            sMediaPlayer?.stop()
            sMediaPlayer?.release()
            sMediaPlayer = null
        }

        fun setsSounds(sMediaPlayer: MediaPlayer?) {
            if (this.sMediaPlayer != sMediaPlayer) {
                sReleaseMediaPlayer()//判断一下，防止自己把自己销毁了。
            }
            this.sMediaPlayer = sMediaPlayer
        }

        /**
         * 设置Raw目录下的音频
         */
        fun setsSoundsRaw(rawId: Int) {
            try {
                sReleaseMediaPlayer()//先释放
                sMediaPlayer = MediaPlayer.create(KBaseApplication.getInstance(), rawId)
                sMediaPlayer?.prepare()//必不可少(目前好像已经不需要了)。必须用try异常进行捕捉。该方法抛出了异常。所以要捕捉。
                sMediaPlayer?.setLooping(false)//不循环播放
            } catch (e: Exception) {
            }
        }

        /**
         * 设置SD卡下的音频。
         * @param path 文件的完整路径（包括文件的后缀名,如:"sound/sb.WAV"）；
         */
        fun setsSoundsSD(path: String) {
            try {
                sReleaseMediaPlayer()//先释放
                sMediaPlayer = MediaPlayer()
                sMediaPlayer?.setDataSource(path)
                sMediaPlayer?.prepare()
                sMediaPlayer?.setLooping(false)//不循环播放
            } catch (e: Exception) {
            }
        }

        /**
         * 设置Assets目录下的音频。
         * @param path 文件的完整路径（包括文件的后缀名,如:"sound/sb.WAV"）；
         */
        fun setsSoundsAssets(path: String) {
            try {
                sReleaseMediaPlayer()//先释放
                sMediaPlayer = MediaPlayer()
                var fileDescriptor = KBaseApplication.getInstance().assets.openFd(path)
                sMediaPlayer?.setDataSource(fileDescriptor.fileDescriptor,
                        fileDescriptor.startOffset, fileDescriptor.length)
                sMediaPlayer?.prepare()
                sMediaPlayer?.setLooping(false)//不循环播放
            } catch (e: Exception) {
            }
        }

        /**
         * 设置SD卡下的音频。
         * @param url 网络上的音频地址
         */
        fun setsSoundsUrl(url: String) {
            try {
                sReleaseMediaPlayer()//先释放
                sMediaPlayer = MediaPlayer()
                sMediaPlayer?.setDataSource(url)//可以播放在线音频
                sMediaPlayer?.prepare()
                sMediaPlayer?.setLooping(false)//不循环播放
            } catch (e: Exception) {
            }
        }

        /**
         * 播放
         */
        fun sPlayMediaPlayer() {
            sMediaPlayer?.let {
                if (!it.isPlaying) {
                    it.start()
                }
            }
        }

        /**
         * getColor()这个方法系统已经有了，不能再重载
         * 获取颜色值（默认从Resources目录，从color文件中获取）
         */
        open fun getColor(id: Int): Int {
            return KBaseUi.getContext().getResources().getColor(id)
        }

        /**
         * 默认就从Res目录下读取
         * 获取String文件里的字符,<string name="names">你好%s</string>//%s 是占位符,位置随意
         * @param formatArgs 是占位符
         */
        open fun getString(id: Int, formatArgs: String? = null): String {
            if (formatArgs != null) {
                return KBaseUi.getContext().resources.getString(id, formatArgs) as String
            }
            return KBaseUi.getContext().getString(id) as String
        }

        /**
         * 获取String文件里的字符串數組
         */
        open fun getStringArray(id: Int): Array<String> {
            return KBaseUi.getContext().resources.getStringArray(id)
        }

    }

}
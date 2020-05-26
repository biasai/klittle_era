package cn.oi.klittle.era.activity.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.hardware.Camera.PictureCallback
import android.os.Bundle
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.SurfaceView
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseActivity
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KLoggerUtils
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * fixme 自定义相机开发；速度还行。
 */
open class KCameraActivity : KBaseActivity(), SurfaceHolder.Callback {
    private var cameraManager: KCameraManager? = null
    private var hasSurface = false
    public open var surfaceView: SurfaceView? = null//fixme 相机视图会投放到SurfaceView上面。

    override fun surfaceCreated(holder: SurfaceHolder?) {
        if (!hasSurface) {
            hasSurface = true
            initCamera(holder,isBackCamera)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        hasSurface = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUi()//初始化视图
    }

    //fixme 初始化视图，子类可以重写。
    public open fun initUi() {
        ui {
            relativeLayout {
                backgroundColor = Color.parseColor("#323232")//浅黑色
                //fixme 一定要初始化surfaceView；必不可少。
                surfaceView = surfaceView {

                }.lparams {
                    width = matchParent
                    height = matchParent
                }
                verticalLayout {
                    fitsSystemWindows = true
                    gravity = Gravity.CENTER
                    button {
                        text = getString(R.string.kaiding)//fixme "开灯"
                        onClick {
                            openLight()
                        }
                    }
                    button {
                        text = getString(R.string.kguangding)//fixme "关灯"
                        onClick {
                            offLight()
                        }
                    }

                    button {
                        text = getString(R.string.kcamera_front)//fixme "前置摄像头"
                        onClick {
                            isBackCamera = false//fixme 前置摄像头
                            resetCamera()//fixme 重新初始化相机
                        }
                    }

                    button {
                        text = getString(R.string.kcamera_back)//fixme "后置摄像头"
                        onClick {
                            isBackCamera = true//fixme 后置摄像头
                            resetCamera()//fixme 重新初始化相机
                        }
                    }

                    button {
                        text = getString(R.string.ktakePicture)//fixme "拍照"
                        textSize = kpx.textSizeX(36)
                        textColor = Color.CYAN
                        onClick {
                            //判断相机是否销毁
                            if (isRecycleCamera) {
                                resetCamera()//fixme 重新初始化相机
                                text = getString(R.string.ktakePicture)//fixme "拍照"
                            } else {
                                //拍照
                                takePicture() {
                                    KLoggerUtils.e("位图\t" + it.isRecycled + "\t宽：\t" + it.width + "\t高：\t" + it.height)
                                    text = getString(R.string.ktakePicture2)//fixme "继续拍照"
                                    backgroundDrawable = BitmapDrawable(it)
                                }
                            }
                        }
                    }.lparams {
                        width = kpx.x(1080) / 4
                        height = kpx.x(1920) / 4
                    }
                }.lparams {
                    width = matchParent
                    height = matchParent
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        resetCamera()//相机初始或重制
    }

    override fun onPause() {
        super.onPause()
        recycleCamera()//停止camera，释放资源操作
    }

    override fun finish() {
        recycleCamera()
        cameraManager = null
        surfaceView = null
        myjpegCallback = null
        callback = null
        pBitmap = null//fixme 拍摄位图。只置空，不要销毁。是否销毁交给调用者去执行。
        super.finish()
    }

    var isRecycleCamera = false //fixme 判断相机是否释放；true(已经释放，不能拍摄)；false(没有释放，可以拍摄)

    //fixme 重置和初始化相机
    public open fun resetCamera() {
        recycleCamera() //必须先停止
        initCamera() //再重置。才能继续重新拍照。
        isRecycleCamera = false//fixme 相机重置，可以进行拍摄操作。
    }

    //fixme 停止camera，释放资源操作
    public open fun recycleCamera() {
        isRecycleCamera = true//fixme 相机释放，不能进行拍摄操作。
        cameraManager?.let {
            it.offLight()//关灯
            it.startPreview()
            it.closeDriver()
            cameraManager?.autoFocusManager?.outstandingTask?.cancel(true)//fixme 取消线程。很重要。
            cameraManager?.autoFocusManager?.outstandingTask = null
        }
        if (!hasSurface) {
            surfaceView?.holder?.removeCallback(this)
        }
    }

    //fixme 判断灯是否打开
    public open fun isLight(): Boolean {
        cameraManager?.let {
            return it.judgeLight()
        }
        return false
    }

    //fixme 开灯
    public open fun openLight() {
        if (!isLight()) {
            cameraManager?.openLight()
        }
    }

    //fixme 关灯
    public open fun offLight() {
        if (isLight()) {
            cameraManager?.offLight()
        }
    }

    var callback: ((bitmap: Bitmap) -> Unit)? = null

    /**
     * fixme 拍照(拍照完成之后，surfaceView画面会暂停在当前拍摄的画面。)
     * @param callback fixme 位图回调；返回的竖屏位图（高大于宽）。
     */
    public open fun takePicture(callback: ((bitmap: Bitmap) -> Unit)) {
        if (!isRecycleCamera) {
            cameraManager?.camera?.let {
                this.callback = callback
                //只有重置过的才能继续拍照。在此加重置判断。防止奔溃
                cameraManager?.takePicture(null, null, myjpegCallback);
            }
        }
    }

    var isBackCamera: Boolean = true//fixme 是否为后置摄像头，默认是。

    //初始化相机camera
    private fun initCamera(isBackCamera: Boolean = this.isBackCamera) {
        cameraManager = KCameraManager()
        //预览相机里的画面，
        // fixme 注意，不要调用surfaceView.setVisibility(View.VISIBLE);和surfaceView.setVisibility(View.INVISIBLE);不然会闪屏或黑屏一下。
        //相机拍照是，SurfaceHolder会停留在拍摄的那一帧画面。
        val surfaceHolder = surfaceView!!.holder
        if (hasSurface) {
            // activity在paused时但不会stopped,因此surface仍旧存在；
            // surfaceCreated()不会调用，因此在这里初始化camera
            initCamera(surfaceHolder, isBackCamera)
        } else {
            // 重置callback，等待surfaceCreated()来初始化camera
            surfaceHolder!!.addCallback(this)
        }
    }

    /**
     * 初始camera
     * @param surfaceHolder SurfaceHolder
     */
    private fun initCamera(surfaceHolder: SurfaceHolder?, isBackCamera: Boolean) {
        if (cameraManager == null) {
            return
        }
        if (surfaceHolder == null) {
            KLoggerUtils.e("初始化异常No SurfaceHolder provided")
        }
        if (cameraManager!!.isOpen) {
            KLoggerUtils.e("相机已经打开")
            return
        }
        try {
            // 打开Camera硬件设备
            cameraManager!!.openDriver(surfaceHolder, isBackCamera)
            // 创建一个handler来打开预览，并抛出一个运行时异常
            cameraManager!!.startPreview()
        } catch (ioe: Exception) {
            KLoggerUtils.e("相机打开异常:\t" + ioe.message)
        }
    }

    private var pBitmap: Bitmap? = null//记录上一次拍摄的位图。

    /**
     * 拍照回调
     */
    var myjpegCallback: PictureCallback? = PictureCallback { data, camera -> //只返回数据，位图，和文件都需要自己手动创建。
        recycleCamera()//fixme 拍照完成之后，记得停止一下拍照。不然可能会异常。
        //根据拍照所得的数据自己创建创建位图，文件File也需要自己手动去创建。图片格式是.jpg格式。
        //原始横屏位图(宽和高以横屏显示为主)
        //KLoggerUtils.e("位图：\t" + data?.size)
        data?.let {
            if (it.size > 0 && callback != null) {

                pBitmap?.recycle()//fixme 销毁上一次的拍摄位图。只保留最后一次的图片拍摄
                pBitmap = null

                //fixme 横屏位图（默认拍出来的都是横屏图片）
                var landBitmap = BitmapFactory.decodeByteArray(data, 0,
                        data!!.size)
                if (landBitmap.width > landBitmap.height) {
                    //KLoggerUtils.e("位图宽：\t" + landBitmap.width + "\t位图高：\t" + landBitmap.height)
                    // 定义矩阵对象
                    var matrix = Matrix();
                    // 缩放原图
                    matrix.postScale(1f, 1f);
                    // 顺时针旋转90度
                    matrix.postRotate(90f);//竖屏拍出来的照片，仍然是横屏的，需要对图片进行旋转90度（横屏图片转竖屏图片）
                    if (!isBackCamera) {
                        matrix.postRotate(180f);//fixme 前置摄像头，还要到转一次，不然是倒象。
                    }
                    //fixme 竖屏位图
                    var portbitmap = Bitmap.createBitmap(landBitmap, 0, 0, landBitmap.getWidth(), landBitmap.getHeight(),
                            matrix, true);
                    landBitmap?.recycle()//fixme 释放横屏图片
                    //KLoggerUtils.e("位图宽2：\t" + portbitmap.width + "\t位图高2：\t" + portbitmap.height + "\t" + portbitmap?.isRecycled + "\t" + landBitmap?.isRecycled)
                    portbitmap?.let {
                        if (!it.isRecycled) {
                            var bm = it
                            pBitmap = bm//fixme 记录当前拍摄位图
                            callback?.let {
                                it(bm)//fixme 竖屏位图回调
                            }
                        }
                    }
                } else {
                    landBitmap?.let {
                        if (!it.isRecycled) {
                            var bm = it
                            pBitmap = bm//fixme 记录当前拍摄位图
                            callback?.let {
                                it(bm)//fixme 竖屏位图回调
                            }
                        }
                    }
                }
                callback = null
            }
        }
    }

}
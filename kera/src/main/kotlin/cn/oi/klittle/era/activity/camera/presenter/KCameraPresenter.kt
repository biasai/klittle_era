package cn.oi.klittle.era.activity.camera.presenter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView
import cn.oi.klittle.era.activity.camera.manager.KCameraManager
import cn.oi.klittle.era.exception.KCatchException
import cn.oi.klittle.era.utils.KLoggerUtils

//    fixme 自定义相机调用案例
//    var prensenter: KCameraPresenter? = null
//    var surfaceView: SurfaceView? = null//fixme 相机视图会投放到SurfaceView上面。
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        initUi()//初始化视图，里面初始了surfaceView
//        if (prensenter == null) {
//            prensenter = KCameraPresenter(surfaceView)
//        }
//        prensenter?.openLight()//fixme 开灯
//        prensenter?.offLight()//fixme 关灯
//        prensenter?.isLight()//fixme 是否开灯
//        prensenter?.resumeCamera(true)//fixme 后置摄像头（在Resume()中调用）
//        prensenter?.resumeCamera(false)//fixme 前置摄像头
//        prensenter?.isBackCamera//fixme 是否后置摄像机;true 后置摄像机，false前置摄像机
//        prensenter?.isHasFrontCamera()//fixme 是否有前置摄像头
//        prensenter?.isRecycleCamera//fixme 相机是否销毁
//        prensenter?.takePicture {  }//fixme 拍照，返回拍摄图片
//        prensenter?.recycleCamera()//fixme 相机销毁（在onPause（）中调用一下）
//        prensenter?.destroy()//fixme 销毁(在Activity里的finish()方法中调用。)
// }

//    override fun onResume() {
//        super.onResume()
//        prensenter?.resumeCamera()//相机初始或重制
//    }
//
//    override fun onPause() {
//        super.onPause()
//        prensenter?.recycleCamera()//停止camera，释放资源操作
//    }
//
//    override fun finish() {
//        super.finish()
//        prensenter?.destroy()//销毁
//    }

class KCameraPresenter(open var surfaceView: SurfaceView?) : SurfaceHolder.Callback {
    private var cameraManager: KCameraManager? = null

    var isRecycleCamera = true //fixme 判断相机是否释放；true(已经释放，不能拍摄)；false(没有释放，可以拍摄)
    var isBackCamera: Boolean = true//fixme 是否为后置摄像头，默认是。如果手机只有后置摄像头，没有前置摄像头。拍出来的仍然是后置摄像头。不会报错。亲测。

    private var hasSurface = false
    //public open var surfaceView: SurfaceView? = null//fixme 相机视图会投放到SurfaceView上面。

    override fun surfaceCreated(holder: SurfaceHolder?) {
        if (!hasSurface) {
            hasSurface = true
            initCamera(holder, isBackCamera)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        hasSurface = false
    }


    //fixme 判断是否有前置摄像头。必须调用了 initCamera（)后才有效。
    //fixme 不一定准确。少部分设备，依然无法正确判断是否有前置摄像头。没有前置摄像头的设备，也可能返回true
    //fixme 极少部分设备无法判断，但是大部分设备还是有效的。
    fun isHasFrontCamera(): Boolean {
        KCameraManager.sHasFrontCamera?.let {
            return it
        }
        return true
    }

    /**
     * fixme 重置和初始化相机
     * @param isBackCamera true 后置摄像头；false前置摄像头
     */
    public open fun resumeCamera(isBackCamera: Boolean = this.isBackCamera) {
        //KLoggerUtils.e("isBackCamera:\t"+isBackCamera+"\t this.isBackCamera:\t"+ this.isBackCamera+"\tisRecycleCamera:\t"+isRecycleCamera)
        if (isBackCamera == this.isBackCamera && !isRecycleCamera) {
            return//防止前置或后置摄像机重复初始化。
        }
        this.isBackCamera = isBackCamera
        try {
            recycleCamera() //必须先停止
            initCamera(isBackCamera) //再重置。才能继续重新拍照。
        } catch (e: java.lang.Exception) {
            KLoggerUtils.e("KCameraActivity->resumeCamera()异常：\t" + KCatchException.getExceptionMsg(e))
        }
        isRecycleCamera = false//fixme 相机重置，可以进行拍摄操作。
        if (!isBackCamera && !isHasFrontCamera()) {
            this.isBackCamera = true;//fixme 没有前置摄像头。仍然是后置摄像头。
        }
    }

    //fixme 停止camera，释放资源操作
    public open fun recycleCamera() {
        isRecycleCamera = true//fixme 相机释放，不能进行拍摄操作。
        cameraManager?.let {
            it.offLight()//关灯
            it.stopPreview()//停止预览，相机会停止最后一帧。
            it.closeDriver()
            //cameraManager?.autoFocusManager?.outstandingTask?.cancel(true)//fixme 取消线程。很重要；在stopPreview（）方法里，会执行。
            //cameraManager?.autoFocusManager?.outstandingTask = null
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


    //初始化相机camera
    private fun initCamera(isBackCamera: Boolean = this.isBackCamera) {
        this.isBackCamera = isBackCamera
        if (cameraManager == null) {
            cameraManager = KCameraManager()
        }
        //预览相机里的画面，
        // fixme 注意，不要调用surfaceView.setVisibility(View.VISIBLE);和surfaceView.setVisibility(View.INVISIBLE);不然会闪屏或黑屏一下。
        //相机拍照是，SurfaceHolder会停留在拍摄的那一帧画面。
        var surfaceHolder = surfaceView?.holder
        if (surfaceHolder == null) {
            return
        }
        if (hasSurface) {
            // activity在paused时但不会stopped,因此surface仍旧存在；
            // surfaceCreated()不会调用，因此在这里初始化camera
            initCamera(surfaceHolder, isBackCamera)
        } else {
            // 重置callback，等待surfaceCreated()来初始化camera
            surfaceHolder?.addCallback(this)
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
            cameraManager?.openDriver(surfaceHolder, isBackCamera)
            // 创建一个handler来打开预览，并抛出一个运行时异常
            cameraManager?.startPreview()
        } catch (ioe: Exception) {
            KLoggerUtils.e("相机打开异常:\t" + ioe.message)
        }
    }

    private var pBitmap: Bitmap? = null//fixme 记录上一次拍摄的位图。

    /**
     * 拍照回调
     */
    var myjpegCallback: Camera.PictureCallback? = Camera.PictureCallback { data, camera -> //只返回数据，位图，和文件都需要自己手动创建。
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

    //销毁
    fun destroy() {
        recycleCamera()
        cameraManager = null
        surfaceView = null
        myjpegCallback = null
        callback = null
        pBitmap = null//fixme 拍摄位图。只置空，不要销毁。是否销毁交给调用者去执行。
    }
}
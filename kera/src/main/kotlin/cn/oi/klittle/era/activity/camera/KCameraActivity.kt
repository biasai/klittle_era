package cn.oi.klittle.era.activity.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera.PictureCallback
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import cn.oi.klittle.era.base.KBaseActivity
import cn.oi.klittle.era.utils.KLoggerUtils

/**
 * fixme 自定义相机开发案例
 */
class KCameraActivity : KBaseActivity(), SurfaceHolder.Callback {
    private var cameraManager: KCameraManager? = null
    private var hasSurface = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //全屏
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //初始化控件
        initUI()
        //初始化相机
        initCamera()
    }

    override fun onPause() {
        /**
         * 停止camera，是否资源操作
         */
        if (cameraManager != null) {
            cameraManager!!.stopPreview()
            cameraManager!!.closeDriver()
        }
        if (!hasSurface) {
            surfaceView?.holder?.removeCallback(this)
        }
        super.onPause()
    }


    var surfaceView: SurfaceView? = null
    private fun initUI() {

    }

    //初始化相机
    private fun initCamera() {
        /**
         * 初始化camera
         */
        cameraManager = KCameraManager()
        //预览相机里的画面，
        // fixme 注意，不要调用surfaceView.setVisibility(View.VISIBLE);和surfaceView.setVisibility(View.INVISIBLE);不然会闪屏或黑屏一下。
        //相机拍照是，SurfaceHolder会停留在拍摄的那一帧画面。
        val surfaceHolder = surfaceView!!.holder
        if (hasSurface) {
            // activity在paused时但不会stopped,因此surface仍旧存在；
            // surfaceCreated()不会调用，因此在这里初始化camera
            initCamera(surfaceHolder)
        } else {
            // 重置callback，等待surfaceCreated()来初始化camera
            surfaceHolder!!.addCallback(this)
        }
    }

    var isRest = true //是否重置。

    //重置
    private fun reset() {
        onPause() //必须先停止
        initCamera() //再重置。才能继续重新拍照。
        isRest = true
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        if (!hasSurface) {
            hasSurface = true
            initCamera(holder)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        hasSurface = false
    }

    /**
     * 初始camera
     *
     * @param surfaceHolder SurfaceHolder
     */
    private fun initCamera(surfaceHolder: SurfaceHolder?) {
        if (surfaceHolder == null) {
            KLoggerUtils.e("初始化异常No SurfaceHolder provided")
        }
        if (cameraManager!!.isOpen) {
            KLoggerUtils.e("已经打开")
            return
        }
        try {
            // 打开Camera硬件设备
            cameraManager!!.openDriver(surfaceHolder)
            // 创建一个handler来打开预览，并抛出一个运行时异常
            cameraManager!!.startPreview()
        } catch (ioe: Exception) {
            KLoggerUtils.e("打开异常:\t" + ioe.message)
        }
    }

    var bitmap //当前拍照返回的位图
            : Bitmap? = null

    /**
     * 拍照回调
     */
    var myjpegCallback: PictureCallback? = PictureCallback { data, camera -> //只返回数据，位图，和文件都需要自己手动创建。
        //根据拍照所得的数据自己创建创建位图，文件File也需要自己手动去创建。图片格式是.jpg格式。
        //原始横屏位图(宽和高以横屏显示为主)
        val landBitmap = BitmapFactory.decodeByteArray(data, 0,
                data!!.size)

//            // 定义矩阵对象
//            Matrix matrix = new Matrix();
//            // 缩放原图
//            matrix.postScale(1f, 1f);
//            // 顺时针旋转90度
//            matrix.postRotate(90);//竖屏拍出来的照片，仍然是横屏的，需要对图片进行旋转90度（横屏图片转竖屏图片）
//            //竖屏位图
//            Bitmap portbitmap = Bitmap.createBitmap(landBitmap, 0, 0, landBitmap.getWidth(), landBitmap.getHeight(),
//                    matrix, true);

    }

}
package cn.oi.klittle.era.activity.camera.presenter

import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Environment
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
import android.view.SurfaceView
import cn.oi.klittle.era.exception.KCatchException
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.utils.KPathManagerUtils
import cn.oi.klittle.era.utils.KPictureUtils
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

//fixme 自定义相机视频录像;参照案例：https://blog.csdn.net/u013283676/article/details/52367149
open class KCameraRecorderPresenter(override var surfaceView: SurfaceView?) : KCameraPresenter(surfaceView) {

    var mMediaRecorder: MediaRecorder? = null
    var isRecorder: Boolean = false//fixme 判断是否正在录像

    //fixme 开始录像
    open fun startRecord(isBackCamera: Boolean = this.isBackCamera) {
        try {
            if (isRecorder) {
                return//fixme 防止重复调用录制。
            }
            isRecorder = true
            resumeCamera(isBackCamera)
            if (cameraManager == null || surfaceView == null) {
                return
            }
            if (mMediaRecorder == null) {
                mMediaRecorder = MediaRecorder()
            }
            mMediaRecorder?.reset()
            mMediaRecorder?.setCamera(cameraManager?.camera)
            //录制的视频的角度，要自行旋转，否则与预览角度不同
            mMediaRecorder?.setOrientationHint(90)
            var cameraId = 0
            cameraManager?.cameraId?.let {
                cameraId = it
            }
            if (cameraId === Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mMediaRecorder?.setOrientationHint(270)
            }
            //mMediaRecorder?.setAudioSource(MediaRecorder.AudioSource.DEFAULT)//fixme 异常报错
            mMediaRecorder?.setVideoSource(MediaRecorder.VideoSource.CAMERA)
            // 设置录像的质量（分辨率，帧数）
            /**
             * 下面两行代码的设置也可以具体写：
             * mMediaRecorder.setVideoSize(320, 240);//分辨率
             * mMediaRecorder.setVideoFrameRate(5);//帧数
             * 用别人的demo是没有问题的，但是我自己写的过程中总是出蜜汁BUG，
             * 而且各种方法都试了也无法解决，只能放弃了
             */
            //var mCamcorderProfile: CamcorderProfile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P)
            //mMediaRecorder?.setProfile(mCamcorderProfile)//fixme 异常报错
            // 设置视频文件输出的路径
            mMediaRecorder?.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString())//fixme 获取视频保存文件
            // 设置捕获视频图像的预览界面
            mMediaRecorder?.setPreviewDisplay(surfaceView?.holder?.surface)
            //mMediaRecorder?.prepare()//fixme 异常报错
            //mMediaRecorder?.start()//fixme 也异常报错。
        } catch (e: Exception) {
            KLoggerUtils.e("startRecord() 相机录像异常：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
        }
    }

    var mediaFile: File? = null//fixme 视频文件

    //fixme 获取文件保存的位置，目录不存在就创建
    open fun getOutputMediaFile(type: Int): File? {
        try {
            var filePath = KPathManagerUtils.getAppVideoPath()//fixme 获取视频保存路径
            var timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            if (type == MEDIA_TYPE_IMAGE) {
                mediaFile = File(filePath + File.separator.toString() +
                        "IMG_" + timeStamp + ".jpg")
            } else if (type == MEDIA_TYPE_VIDEO) {
                mediaFile = File(filePath + File.separator.toString() +
                        "VID_" + timeStamp + ".mp4")
            }
            if (mediaFile == null) {
                return null
            }
            mediaFile?.let {
                if (!it.exists()) {
                    if (!it.getParentFile().exists()) {
                        it.getParentFile().mkdirs()
                    }
                    try {
                        it.createNewFile()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            KLoggerUtils.e("getOutputMediaFile() 相机录像文件异常：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
        }
        //if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) { }
        return mediaFile
    }

    //fixme 停止录像
    open fun stopRecord() {
        try {
            if (!isRecorder) {
                return
            }
            isRecorder = false
            mediaFile?.let {
                if (it.exists() && it.length() > 0) {
                    KPictureUtils.updateFileFromDatabase_add(it)//fixme 通知系统。录屏位置
                }
            }
            recycleCamera()
            if (mMediaRecorder != null) {
                mMediaRecorder?.stop()
                mMediaRecorder?.reset()
                mMediaRecorder?.release()
                mMediaRecorder = null
            }
        } catch (e: java.lang.Exception) {
            KLoggerUtils.e("stopRecord() 相机录像停止异常：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
        }
    }

    //销毁
    override fun destroy() {
        super.destroy()
        stopRecord()
    }

}
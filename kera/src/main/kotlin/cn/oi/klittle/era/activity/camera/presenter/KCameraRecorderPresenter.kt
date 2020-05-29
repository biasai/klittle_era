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
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

//fixme 自定义相机视频录像;参照案例：https://blog.csdn.net/u013283676/article/details/52367149
class KCameraRecorderPresenter(override var surfaceView: SurfaceView?) : KCameraPresenter(surfaceView) {

    var mMediaRecorder: MediaRecorder? = null

    //fixme 开始录像
    fun startRecord() {
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
        mMediaRecorder?.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
        mMediaRecorder?.setVideoSource(MediaRecorder.VideoSource.CAMERA)
        // 设置录像的质量（分辨率，帧数）
        /**
         * 下面两行代码的设置也可以具体写：
         * mMediaRecorder.setVideoSize(320, 240);//分辨率
         * mMediaRecorder.setVideoFrameRate(5);//帧数
         * 用别人的demo是没有问题的，但是我自己写的过程中总是出蜜汁BUG，
         * 而且各种方法都试了也无法解决，只能放弃了
         */
        val mCamcorderProfile: CamcorderProfile = CamcorderProfile.get(cameraId,
                CamcorderProfile.QUALITY_720P)
        mMediaRecorder?.setProfile(mCamcorderProfile)
        // 设置视频文件输出的路径
        mMediaRecorder?.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString())//fixme 获取视频保存文件
        // 设置捕获视频图像的预览界面
        mMediaRecorder?.setPreviewDisplay(surfaceView?.holder?.surface)
        try {
            mMediaRecorder?.prepare()
        } catch (e: Exception) {
            KLoggerUtils.e("startRecord() 相机录像异常：\t" + KCatchException.getExceptionMsg(e))
        }
        mMediaRecorder?.start()
    }

    //fixme 获取文件保存的位置，目录不存在就创建
    private fun getOutputMediaFile(type: Int): File? {
        var mediaFile: File? = null
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            val filePath =KPathManagerUtils.getAppVideoPath()//fixme 获取视频保存路径
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
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
            if (!mediaFile.exists()) {
                if (!mediaFile.getParentFile().exists()) {
                    mediaFile.getParentFile().mkdirs()
                }
                try {
                    mediaFile.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return mediaFile
    }

}
package cn.oi.klittle.era.activity.camera.presenter

import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
import android.view.SurfaceView
import cn.oi.klittle.era.exception.KCatchException
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.utils.KPathManagerUtils
import cn.oi.klittle.era.utils.KPictureUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

//fixme 自定义相机视频录像;参照案例：https://blog.csdn.net/u013283676/article/details/52367149
open class KCameraRecorderPresenter(override var surfaceView: SurfaceView?) : KCameraPresenter(surfaceView) {

    var mMediaRecorder: MediaRecorder? = null
    var isRecorder: Boolean = false//fixme 判断是否正在录像


    /**
     * fixme 开始录像（需要权限：1.SD卡；2.相机；3.录音三种权限。不然会报错异常。）
     * fixme 重新录像，会删除之前旧的录像文件哦。
     * @param isBackCamera true 后置摄像头录像，false前置摄像头录像。
     * @param isShutSound fixme true 关闭系统自带的快门声，false不关闭。默认关闭。
     **/
    open fun startRecord(isBackCamera: Boolean = this.isBackCamera, isShutSound: Boolean = true) {
        try {
            if (isRecorder) {
                return//fixme 正在录制，防止重复调用录制。
            }
            isRecorder = true
            resumeCamera(isBackCamera)//初始化或重置相机
            if (cameraManager == null || surfaceView == null) {
                return
            }
            if (mMediaRecorder == null) {
                mMediaRecorder = MediaRecorder()//初始化录像
            }
            cameraManager?.camera?.unlock();//fixme 解锁摄像头 - 调用Camera.unlock()解锁摄像头以供MediaRecorder使用；必不可少
            mMediaRecorder?.reset()//重置录像
            mMediaRecorder?.setCamera(cameraManager?.camera)
            //录制的视频的角度，要自行旋转，否则与预览角度不同
            mMediaRecorder?.setOrientationHint(90)
            var cameraId = 0
            cameraManager?.cameraId?.let {
                cameraId = it
            }
            if (cameraId === Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mMediaRecorder?.setOrientationHint(270)//fixme 前置摄像头录像，左右是相反的。系统录像也是一样的效果。亲测。
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
            var mCamcorderProfile: CamcorderProfile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P)
            mMediaRecorder?.setProfile(mCamcorderProfile)
            // 设置视频文件输出的路径
            mMediaRecorder?.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString())//fixme 获取视频保存文件
            // 设置捕获视频图像的预览界面
            mMediaRecorder?.setPreviewDisplay(surfaceView?.holder?.surface)
            mMediaRecorder?.prepare()
            setStreamMute(isShutSound)//fixme 关闭或开启，系统默认的快门声音。必须放在start（）调用才有效。
            mMediaRecorder?.start()//fixme 开始录像。
        } catch (e: Exception) {
            KLoggerUtils.e("startRecord() 相机录像异常：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
        }
    }

    var mediaFile: File? = null//fixme 视频文件;停止录像，回调中会返回。

    //fixme 获取文件保存的位置，目录不存在就创建；已经存在的就删除掉。
    open fun getOutputMediaFile(type: Int): File? {
        try {
            mediaFile?.let {
                if (it.exists()) {
                    it.delete()//fixme 删除之前的旧文件。
                    mediaFile = null
                }
            }
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

    /**
     * fixme 停止录像
     * @param callback 回调录像文件。如果回调为空。录像文件会自动清除。
     */
    open fun stopRecord(callback: ((file: File) -> Unit)? = null) {
        recycleCamera()
        if (!isRecorder) {
            return//fixme 已经停止，防止重复停止调用。
        }
        isRecorder = false
        //停止录像比较耗时，所以开协程。
        GlobalScope.async {
            try {
                if (mMediaRecorder != null) {
                    mMediaRecorder?.stop()
                    mMediaRecorder?.reset()
                    mMediaRecorder?.release()
                    mMediaRecorder = null
                }
                mediaFile?.let {
                    if (it.exists()) {
                        if (it.length() > 0) {
                            var file = it
                            if (callback != null) {
                                KPictureUtils.updateFileFromDatabase_add(it)//fixme 通知系统。录屏位置
                                callback?.let { it(file) }//fixme 有回调就返回录像文件。
                            } else {
                                file?.delete()//fixme 回调为空；录像文件主动清除。没有回调，就说明不需要录屏文件。
                                mediaFile = null
                            }
                        } else {
                            it.delete()
                            mediaFile = null
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                KLoggerUtils.e("stopRecord() 相机录像停止异常：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
            }
        }
    }

    //销毁
    override fun destroy() {
        super.destroy()
        stopRecord()
    }

}
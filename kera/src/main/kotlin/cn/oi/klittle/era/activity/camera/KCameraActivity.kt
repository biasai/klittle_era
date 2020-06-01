package cn.oi.klittle.era.activity.camera

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.SurfaceView
import cn.oi.klittle.era.R
import cn.oi.klittle.era.activity.camera.presenter.KCameraPresenter
import cn.oi.klittle.era.base.KBaseActivity
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KLoggerUtils
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * fixme 自定义相机开发；速度还行。
 */
open class KCameraActivity : KBaseActivity() {

    var prensenter: KCameraPresenter? = null
    var surfaceView: SurfaceView? = null//fixme 相机视图会投放到SurfaceView上面。
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUi()//初始化视图，里面初始了surfaceView
        if (prensenter == null) {
            prensenter = KCameraPresenter(surfaceView)
            intent?.let {
                it.extras?.get("isBackCamera")?.let {
                    if (it is Boolean){
                        prensenter?.isBackCamera=it//fixme true后置摄像头，false前置摄像头
                    }
                }
            }

        }
 }

    //fixme 初始化视图，子类可以重写。一定要初始化surfaceView
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
                            prensenter?.openLight()
                        }
                    }
                    button {
                        text = getString(R.string.kguangding)//fixme "关灯"
                        onClick {
                            prensenter?.offLight()
                        }
                    }

                    button {
                        text = getString(R.string.kcamera_front)//fixme "前置摄像头"
                        onClick {
                            prensenter?.resumeCamera(false)//fixme 前置摄像头,重新初始化相机
                        }
                    }

                    button {
                        text = getString(R.string.kcamera_back)//fixme "后置摄像头"
                        onClick {
                            prensenter?.resumeCamera(true)//fixme 后置摄像头,重新初始化相机
                        }
                    }

                    button {
                        text = getString(R.string.ktakePicture)//fixme "拍照"
                        textSize = kpx.textSizeX(36)
                        textColor = Color.CYAN
                        onClick {
                            //判断相机是否销毁
                            prensenter?.let {
                                if (it.isRecycleCamera) {
                                    it?.resumeCamera()//fixme 重新初始化相机
                                    text = getString(R.string.ktakePicture)//fixme "拍照"
                                } else {
                                    //拍照
                                    it?.takePicture(true) {
                                        KLoggerUtils.e("拍摄位图\t" + it.isRecycled + "\t宽：\t" + it.width + "\t高：\t" + it.height)
                                        text = getString(R.string.ktakePicture2)//fixme "继续拍照"
                                        backgroundDrawable = BitmapDrawable(it)
                                    }
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
        prensenter?.resumeCamera()//相机初始或重制
    }

    override fun onPause() {
        super.onPause()
        prensenter?.recycleCamera()//停止camera，释放资源操作
    }

    override fun finish() {
        super.finish()
        prensenter?.destroy()//销毁
    }

}
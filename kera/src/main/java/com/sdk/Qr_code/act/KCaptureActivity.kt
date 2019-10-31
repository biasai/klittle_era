package com.sdk.Qr_code.act

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import cn.oi.klittle.era.R
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.toolbar.KToolbar
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.widget.compat.KTextView
import com.sdk.Qr_code.manager.CameraManager
import com.sdk.Qr_code.utils.KZxingUtils
import com.sdk.Qr_code.view.ViewfinderView
import org.jetbrains.anko.*

//    /**
//     * 跳转到 二维码扫描界面
//     */
//    fun goCaptureActivity(nowActivity: Activity? = getActivity()) {
//        //需要相机权限（必须）
//        KPermissionUtils.requestPermissionsCamera {
//            if (it) {
//                   goActivityForResult(KCaptureActivity::class.java,KZxingUtils.requestCode_Qr)
//            } else {
//                KPermissionUtils.showFailure()
//            }
//        }
//    }
//   KUiHelper.goCaptureActivity()//fixme 跳转到 二维码扫描界面
//       fixme 最下面有上一个Activity的回调结果案例。

/**
 * fixme 子类可以仿照以下进行重写；亲测可行。
 * Created by 彭治铭 on 2019/3/27.
 */
open class KCaptureActivity : CaptureActivity() {

    /**
     * fixme 重新布局
     */
    override fun initUI() {
        //super.initUI() //屏蔽并重写父类这个方法。
        relativeLayout {
            backgroundColor = Color.BLACK
            //fixme 初始化父类的surfaceView(必不可少)
            surfaceView = surfaceView {

            }.lparams {
                width = matchParent
                height = matchParent
            }
            //fixme 初始化父类的viewfinderView(必不可少)
            viewfinderView = ViewfinderView(this).apply {

            }.lparams {
                width = matchParent
                height = matchParent
            }
            KToolbar(this, ctx as Activity)?.apply {
                contentView?.apply {
                    //backgroundColor = Color.parseColor("#09099F")//标题栏背景颜色
                    backgroundColor = Color.parseColor("#0078D7")
                }
                //左边返回文本（默认样式自带一个白色的返回图标）
                leftTextView?.apply {
                }
                //中间文本
                titleTextView?.apply {
                    text = getString(R.string.kqr_code)//"二维码/条码"
                }
                //右上角图片选择器图标
                rightTextView?.apply {
                    autoBg {
                        width = kpx.x(45)
                        height = width
                        autoBgColor = Color.WHITE
                        autoBg(R.mipmap.kera_right_top_img_select)
                    }
                    layoutParams(kpx.x(100), kpx.x(45))
                    onClick {
                        pictrueSelector()//fixme 选择本地图片
                    }
                }
            }
            //fixme 开关灯
            KTextView(this).apply {
                radius {
                    all_radius(kpx.x(100f))
                    //bgHorizontalColors(Color.RED, Color.parseColor("#D96550"))
                    bg_color = Color.parseColor("#0078D7")//Color.parseColor("#09099F")
                    strokeColor = Color.WHITE
                    strokeWidth = kpx.x(3f)
                }
                gravity = Gravity.CENTER
                textColor = Color.WHITE
                textSize = kpx.textSizeX(38)
                if (CameraManager.get().judgeLight()) {
                    //开灯
                    text = getString(R.string.kguangding)//"关灯"
                } else {
                    //关灯
                    text = getString(R.string.kaiding)//"开灯"
                }
                onClick {
                    //判断灯光是否打开
                    if (CameraManager.get().judgeLight()) {
                        //关灯操作
                        CameraManager.get().offLight()
                        text = getString(R.string.kaiding)//"开灯"
                    } else {
                        //开灯操作
                        CameraManager.get().openLight()
                        text = getString(R.string.kguangding)//"关灯"
                    }
                }
            }.lparams {
                width = kpx.x(150)
                height = width
                centerHorizontally()
                alignParentBottom()
                bottomMargin = kpx.y(180)
            }
        }
    }


    /**
     * fixme 二位扫描结果（无论是扫描框的结果，还是扫描本地二维码图片，都会回调这个方法。）
     * fixme 普通二维码，一般字数不能超过150个字。
     */
    override fun onQrScanResult(str: String?, bitmap: Bitmap?) {
        super.onQrScanResult(str, bitmap)//父类的方法最好调用一下。
        //扫描框扫出来的二维码，bitmap一般都不为空；但是扫描本地图片则是空的。（因为扫描本地二维码时，没有传位图）
        //setResult(str)
        KLoggerUtils.e("二维码扫描结果：\t" + str)
    }

    //本地二维码图片识别失败时回调。
    override fun onQrScanResultFail() {
        super.onQrScanResultFail()
    }

    //关闭当前Activity，并将二维码传递给上一个Activity。
    fun setResult(str: String?) {
        if (str != null && str.length > 0) {
            //str是二维码扫描结果
            val bundle = Bundle()
            bundle.putString("result", str)
            intent.putExtras(bundle)
            setResult(KZxingUtils.resultCode_Qr, intent)
            finish()
        }
    }

//       fixme 这个是上一个Activity的回调结果案例。
//    var versionUpdateDialog: VersionUpdateDialog? = null//版本更新（或App下载弹框）
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        //fixme 二维码扫描结果。
//        if (requestCode == KZxingUtils.requestCode_Qr && resultCode == KZxingUtils.resultCode_Qr && data != null) {
//            var result = data.getStringExtra("result")
//            result?.trim()?.let {
//                if (KRegexUtils.isUrl(it)) {
//                    if (it.toLowerCase().contains(".apk")) {
//                        //apk下载链接
//                        runOnUiThread {
//                            var url = it
//                            if (versionUpdateDialog == null) {
//                                versionUpdateDialog = VersionUpdateDialog(this)
//                            }
//                            versionUpdateDialog?.apply {
//                                setUrl(url)
//                                setUpdateTitle(getString(R.string.kfaxianapp))//发现App
//                                setUpdateVersion2(KFileLoadUtils.getInstance(ctx).getUrlFileName(url))//文件名
//                                setUpdateContent(getString(R.string.kshifouxiazai))//"是否下载?"
//                                setOkButtonName(getString(R.string.kxiazai))//确认按钮名称（下载）
//                                isLocked(false)
//                                isDismiss(false)
//                                show()
//                            }
//                        }
//                    } else {
//                        //一般的网站,跳转系统浏览器
//                        KIntentUtils.goBrowser(act, it)
//                    }
//                } else if (KRegexUtils.isMobileNO(it)) {
//                    //电话号码，跳转到拨号界面
//                    KIntentUtils.goCallPhone(act, it)
//                } else {
//                    if (it.length > 15) {
//                        //KIntentUtils.goSMS(act, it)//发送短信
//                        KSharedUtils.sharedMsg(act, "", msgText = it)//分享
//                    } else {
//                        //其他显示一下扫描结果
//                        KToast.showInfo(it, act)
//                    }
//                }
//            }
//        }
//    }
}
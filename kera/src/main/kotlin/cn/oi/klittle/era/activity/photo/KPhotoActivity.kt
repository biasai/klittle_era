package cn.oi.klittle.era.activity.photo

import android.content.Intent
import android.os.Bundle
import android.view.View
import cn.oi.klittle.era.R
import cn.oi.klittle.era.activity.photo.popu.KPhotoSelectPopu
import cn.oi.klittle.era.activity.photo.manager.KPictureSelector
import cn.oi.klittle.era.activity.preview.KPreviewActivity
import cn.oi.klittle.era.base.KBaseActivity
import cn.oi.klittle.era.comm.KToast
import cn.oi.klittle.era.utils.KAssetsUtils
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.utils.KPermissionUtils
import java.util.concurrent.TimeUnit
//import kotlinx.coroutines.experimental.async
//import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

/**
 * 图片选择器
 */
open class KPhotoActivity : KBaseActivity() {

    var ui: KPhotoUi? = null
    var presenter: KPhotoPresenter? = null
    var sp: KPhotoSelectPopu? = null

    var checkedFolderIndex = KPictureSelector.checkedFolderIndex//记录当前选中目录

    //图片下拉选择列表
    fun showPopu() {
        try {
            checkedFolderIndex = KPictureSelector.checkedFolderIndex//记录当前选中目录
            if (sp == null) {
                sp = KPhotoSelectPopu()
                sp?.create(this)
                sp?.sp?.pop?.setOnDismissListener {
                    ui?.toolbar?.titleTextView?.let {
                        it.isSelected = false//不选中，图标向下。
                    }
                    if (checkedFolderIndex != KPictureSelector.checkedFolderIndex) {
                        checkedFolderIndex = KPictureSelector.checkedFolderIndex
                        loadAllMedia()//重新加载
                    }
                }
            }
            ui?.toolbar?.titleTextView?.let {
                it.isSelected = true//选中图表先上。
            }
            sp?.showAsDropDown(ui?.toolbar?.contentView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //加载数据
    fun loadAllMedia() {
        presenter?.loadAllMedia {
            runOnUiThread {
                try {
                    KPictureSelector.initCurrentSelectNum()//先初始化选中个数。
                    ui?.photoAdapter?.datas = it
                    ui?.photoAdapter?.notifyDataSetChanged()
                    if (it.size <= 0) {
                        KPictureSelector.currentSelectNum = 0
                    }
                    checkNumCallback(KPictureSelector.currentSelectNum)
                    //标题
                    ui?.toolbar?.titleTextView?.setText(KPictureSelector.getCheckedFolderName())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
//            enterAnim = R.anim.kera_from_small_to_large_a5
//            exitAnim = R.anim.kera_from_large_to_small_a3
            super.onCreate(savedInstanceState)
            ui = KPhotoUi()
            setContentView(ui?.createView(this))
            presenter = KPhotoPresenter(this, ui)
            KPermissionUtils.requestPermissionsStorage {
                if (it) {
                    loadAllMedia()
                    ui?.photoAdapter?.checkNumCallback {
                        checkNumCallback(it)
                    }
                    //完成
                    ui?.complete?.apply {
                        onClick {
                            if (isSelected) {
                                complete()
                            }
                        }
                    }
                    //选中数量
                    ui?.num?.apply {
                        onClick {
                            if (visibility == View.VISIBLE) {
                                complete()
                            }
                        }
                    }
                    //预览
                    ui?.preview?.apply {
                        onClick {
                            if (isSelected) {
                                KPictureSelector.openExternalPreview(index = 0, meidas = KPictureSelector.getPreSelectDatas(), isCheckable = true)
                            }
                        }
                    }
                    //下拉弹框
                    ui?.toolbar?.titleTextView?.apply {
                        onClick {
                            showPopu()
                        }
                    }
                } else {
                    KToast.showInfo(getString(R.string.kstoragy_not))
                    GlobalScope.async {
                        finish()//关闭
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    override fun onResume() {
        super.onResume()
        if (isCompleteFinish) {
            isCompleteFinish = false
            finish()//fixme 关闭，有动画效果。
            return
        } else {
            try {
                ui?.photoAdapter?.isRecyclerBitmap = false//不释放位图，防止整个界面抖动。
                ui?.photoAdapter?.notifyDataSetChanged()
                GlobalScope.async {
                    delay(500)
                    ui?.photoAdapter?.isRecyclerBitmap = true//恢复释放位图
                }
                var has = false
                ui?.photoAdapter?.datas?.let {
                    if (it.size > 0) {
                        has = true
                    }
                }
                if (has) {
                    checkNumCallback(KPictureSelector.currentSelectNum)//当前选中数
                } else {
                    checkNumCallback(0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var isCompleteFinish = false//是否完成关闭（如：预览点击已经完成。）
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            super.onActivityResult(requestCode, resultCode, data)
            //KLoggerUtils.e("requestCode:\t"+requestCode+"\tresultCode:\t"+resultCode)
            if (requestCode == KPictureSelector.requestCode_preview && resultCode == KPictureSelector.resultCode_preview && KPreviewActivity.isCompelete) {
                KPreviewActivity.isCompelete = false//恢复成false
                //finish()//fixme 完成，不要在这里关闭，这里关闭自己定义的动画效果可能无效。
                isCompleteFinish = true
            } else {
                isCompleteFinish = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //选中个数回调
    fun checkNumCallback(checkNum: Int) {
        try {
            ui?.num?.setText(checkNum.toString())
            if (checkNum > 0) {
                ui?.num?.visibility = View.VISIBLE//选中数量
                ui?.complete?.isSelected = true//已完成
                ui?.preview?.isSelected = true//预览
            } else {
                ui?.num?.visibility = View.INVISIBLE
                ui?.complete?.isSelected = false//请选择
                ui?.preview?.isSelected = false//预览
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //完成
    fun complete() {
        try {
            showProgressbar()//显示进度条
            KPictureSelector.selectCallback() {
                shutProgressbar()//关闭进度条
                finish()//关闭
            }//选中回调
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun finish() {
        super.finish()
        try {
            KPictureSelector.cameraFirstKLocalMedia?.let {
                KPictureSelector.getCheckedFolder()?.remove(it)
            }
            KPictureSelector.cameraFirstKLocalMedia = null
            KPictureSelector?.getCheckedFolder()?.let {
                it.forEach {
                    it.key?.let {
                        KAssetsUtils.getInstance().recycleBitmap(it)
                    }
                }
            }
            ui?.destroy(this)//界面销毁
            overridePendingTransition(0, R.anim.kera_from_large_to_small_a3)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

}

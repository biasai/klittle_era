package cn.oi.klittle.era.activity.preview

import android.os.Bundle
import android.view.View
import cn.oi.klittle.era.R
import cn.oi.klittle.era.activity.photo.manager.KPictureSelector
import cn.oi.klittle.era.base.KBaseActivity
import cn.oi.klittle.era.utils.KAssetsUtils

/**
 * fixme 图片预览;在KPictureSelector.openExternalPreview()方法里调用了
 */
open class KPreviewActivity : KBaseActivity() {


    var ui: KPreviewUi? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
//            enterAnim = R.anim.kera_from_small_to_large_a5
//            exitAnim = R.anim.kera_from_large_to_small_a3
            super.onCreate(savedInstanceState)
            ui = KPreviewUi()
            setContentView(ui?.createView(this))
            ui?.setTitle()
            ui?.viewPager?.setCurrentItem(KPictureSelector.previewIndex, false)
            if (KPictureSelector.isCheckable) {
                ui?.checkNumCallback(KPictureSelector.currentSelectNum)
                //完成
                ui?.complete?.apply {
                    onClick {
                        if (isSelected) {
                            complete()
                        }
                    }
                }
                ui?.num?.apply {
                    onClick {
                        if (visibility == View.VISIBLE) {
                            complete()
                        }
                    }
                }
            }
            isCompelete = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun finish() {
        super.finish()
        try {
            ui?.previewAdapter?.keys?.forEach {
                KAssetsUtils.getInstance().recycleBitmap(it.value)//释放位图
            }
            ui?.destroy(this)//界面销毁
            overridePendingTransition(0, R.anim.kera_from_large_to_small_a3)//对透明主题，动画可能无效。
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //完成
    fun complete() {
        try {
            showProgressbar()//显示进度条
            KPictureSelector.selectCallback() {
                isCompelete = true
                shutProgressbar()//关闭进度条
                setResult(KPictureSelector.resultCode_preview)
                finish()//关闭
            }//选中回调
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        var isCompelete = false//是否完成。
    }

}

package cn.oi.klittle.era.activity.photo

import android.support.v4.app.FragmentActivity
import cn.oi.klittle.era.activity.photo.entity.KLocalMedia
import cn.oi.klittle.era.activity.photo.entity.KLocalMediaFolder
import cn.oi.klittle.era.activity.photo.manager.KLocalMediaLoader
import cn.oi.klittle.era.activity.photo.manager.KPictureSelector
import cn.oi.klittle.era.utils.KFileUtils
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.entity.LocalMediaFolder
import java.io.File

open class KPhotoPresenter(activity: FragmentActivity, var ui: KPhotoUi?) {
    var mediaLoader: KLocalMediaLoader? = null//图片加载器

    init {
        mediaLoader = KLocalMediaLoader(activity, type = KPictureSelector.type, isGif = KPictureSelector.isGif)
    }

    //加载所有图片
    fun loadAllMedia(callback: ((datas: MutableList<KLocalMedia>) -> Unit)? = null) {
        try {
            //测试发现必须在主线程里调用才有效，协程async里都无效。
            //放心回调很快的，不会卡死。效率杠杠的。
            //fixme 注意，mediaLoader?.loadAllMedia{}在生命周期onResume会自动刷新重调。
            mediaLoader?.loadAllMedia {
                try {
                    it?.let {
                        if (it.size <= 0) {
                            return@let
                        }
                        if (it.size <= KPictureSelector.checkedFolderIndex) {
                            KPictureSelector.checkedFolderIndex = 0
                        }
                        KPictureSelector.folders?.let {
                            it?.forEach {
                                it.images?.clear()
                            }
                            it.clear()
                        }
                        if (KPictureSelector.folders == null) {
                            KPictureSelector.folders = mutableListOf()
                        }
                        var index = 0
                        it.forEach {
                            createFolder(it, index)
                            index++
                        }
                        //回调
                        KPictureSelector.getCheckedFolder()?.let {
                            var datas = it
                            callback?.let {
                                it(datas)
                            }
                        }
                    }
                }catch (e:java.lang.Exception){e.printStackTrace()}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var pathMap = mutableMapOf<String, String>()
    private fun createFolder(it: LocalMediaFolder?, index: Int) {
        try {
            pathMap?.clear()
            var folder = KLocalMediaFolder()
            it?.let {
                folder.checkedNum = it.checkedNum
                folder.isChecked = it.isChecked
                folder.firstImagePath = it.firstImagePath
                folder.name = it.name
                folder.path = it.path
                folder.imageNum = it.imageNum
            }
            if (folder.images == null) {
                folder.images = mutableListOf()
            }
            it?.images?.forEach {
                createLocalMedia(it)?.let {
                    if (KFileUtils.getInstance().getFileSize(it.path) > 0) {
                        if (it.path != null && !pathMap.containsKey(it!!.path)) {
                            folder.images?.add(it)//fixme 防止重复添加
                        }
                        it.path?.let {
                            pathMap?.put(it, it)
                        }
                    }
                }
            }

//        fixme 发送系统广播，这样图片选择器就能够读取到该图片文件了。
//        actvity?.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(KPictureUtils.cramefile)))
//        fixme 不需要在额外读取本应用的图片了。相机拍照完成之后，在KPictrueUtils里的onActivityResult发送了广播，图片选择器已经能够读取到该图片了。
//        fixme sendBroadcast不一定及时，所以还是手动读取一下本应用的图片。以防万一。
            if (index == 0 && (KPictureSelector.type == PictureConfig.TYPE_IMAGE || KPictureSelector.type == PictureConfig.TYPE_ALL)) {
                //读取本应用相机图片
                readCameraLocalMedia()?.forEach {
                    createLocalMedia(it.path)?.let {
                        if (KFileUtils.getInstance().getFileSize(it.path) > 0) {
                            if (it.path != null && !pathMap.containsKey(it!!.path)) {
                                //folder.images?.add(it)//fixme 防止重复添加
                                folder.images?.add(0, it)//fixme 添加到第一个。
                            }
                            it.path?.let {
                                pathMap?.put(it, it)
                            }
                        }
                    }
                }
            }

            it?.images?.let {
                if (it.size > 0) {
                    KPictureSelector.folders?.add(folder)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //读取本应用相机图片
    private fun readCameraLocalMedia(): Array<File> {
        var files = KFileUtils.getInstance().readfiles(KPictureSelector.getCameraPath())
        //（日期越大越在前，即最新的时间显示在最前面。）
        //return KFileUtils.getInstance().orderByDate(files.toTypedArray())
        return files.toTypedArray()//fixme 调用的时候，已经调整顺序，这里不需要再重新排序了。
    }

    private fun createLocalMedia(path: String?): KLocalMedia? {
        if (path == null) {
            return null
        } else {
            var media = LocalMedia()
            media.path = path
            return createLocalMedia(media)
        }
    }

    //fixme 根据路径创建。
    private fun createLocalMedia(media: LocalMedia?): KLocalMedia? {
        if (media == null) {
            return null
        }
        var kLocalMedia = KLocalMedia()
        if (media.path != null) {
            kLocalMedia.path = media.path
            media?.apply {
                pictureType?.let {
                    if (it.length > 0) {
                        kLocalMedia.pictureType = media.pictureType//图片类型。
                    }
                }
                duration?.let {
                    if (it > 0) {
                        kLocalMedia.duration = it//视频时长
                    }
                }
                width?.let {
                    if (it > 0) {
                        kLocalMedia.width = it
                    }
                }
                height?.let {
                    if (it > 0) {
                        kLocalMedia.height = it
                    }
                }
            }
            //fixme 判断是否选中
            KPictureSelector.contains(kLocalMedia)?.let {
                if (it.path != null) {
                    kLocalMedia = it//fixme 获取选中的。
                }
            }
        }
        return kLocalMedia
    }

}
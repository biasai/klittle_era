package cn.oi.klittle.era.activity.photo.manager

import android.app.Activity
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.transition.Fade
import android.view.View
import cn.oi.klittle.era.R
import cn.oi.klittle.era.activity.photo.KPhotoActivity
import cn.oi.klittle.era.activity.photo.config.PictureConfig
import cn.oi.klittle.era.activity.photo.entity.KLocalMedia
import cn.oi.klittle.era.activity.photo.entity.KLocalMediaFolder
import cn.oi.klittle.era.activity.preview.KPreviewActivity
import cn.oi.klittle.era.base.KBaseApplication
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.helper.KUiHelper
import cn.oi.klittle.era.utils.*
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File
import java.util.*

//fixme 调用案例
//KPictureSelector.type(PictureConfig.TYPE_ALL).isGif(true).isCamera(true).imageSpanCount().maxSelectNum(10).selectionMedia().minimumCompressSize(100).isCompress(true).forResult {}
//ictureSelector.openExternalPreview2(path = pPath)//fixme 单张图片预览。
/**
 * 图片选择器
 */
object KPictureSelector {

    //获取本应用拍照图片路径
    fun getCameraPath(): String {
        return KPictureUtils.getCameraPath()
    }

    //获取压缩路径
    fun getCompressPath(): String {
        File(compressPath)?.apply {
            if (!exists()) {
                //目录如果不存在，则自动创建。鲁班压缩不会自动创建目录，传入的目录必须存在。
                mkdir()
                mkdirs()
            }
        }
        return compressPath
    }

    var imageSpanCount = 3//图片适配器列表每行的列数。
    var currentSelectNum = 0//当前选中数
    var maxSelectNum = 50//最大选中数
    var isCompress: Boolean = false//是否压缩
    private var isCamera: Boolean = true//是否有摄像头（默认配置）
    private var isCamera2: Boolean = true//是否有摄像头（临时配置）
    var minimumCompressSize: Int = 100//单位KB,小于该值不压缩

    var type: Int = PictureConfig.TYPE_IMAGE//类型
    var isGif: Boolean = false//是否包含动态图片

    var checkedFolderIndex = 0//选中目录
    var folders: MutableList<KLocalMediaFolder>? = null//所有目录数据集合

    /**
     * 类型；
     * 图片:PictureConfig.TYPE_IMAGE;
     * 视频:PictureConfig.TYPE_VIDEO;
     * 音频:PictureConfig.TYPE_AUDIO;
     * 全部:PictureConfig.TYPE_ALL;fixme （视频，图片，gif动图。不包含音频。音频是独立的。）
     */
    fun type(type: Int = PictureConfig.TYPE_IMAGE): KPictureSelector {
        KPictureSelector.type = type
        return this
    }

    fun isGif(isGif: Boolean = false): KPictureSelector {
        KPictureSelector.isGif = isGif
        return this
    }

    //获取当前选中目录名称
    fun getCheckedFolderName(): String? {
        folders?.let {
            if (it.size > checkedFolderIndex) {
                return it[checkedFolderIndex].name
            }
        }
        return null
    }

    //获取中目录的数据
    fun getCheckedFolder(): MutableList<KLocalMedia>? {
        folders?.let {
            if (it.lastIndex < checkedFolderIndex) {
                return null
            }
        }
        return folders?.get(checkedFolderIndex)?.images
    }

    //初始化当前选中个数
    fun initCurrentSelectNum() {
        currentSelectNum = 0
        try {
            KPictureSelector.cameraFirstKLocalMedia?.let {
                try {
                    if (it.isChecked) {
                        File(it.path)?.let {
                            if (it.exists() && it.length() > 0) {
                                currentSelectNum = 1
                            }
                        }
                    }
                } catch (e: Exception) {
                    KLoggerUtils.e("KPictureSelector->initCurrentSelectNum()异常：\t"+e.message)
                }
            }
            getCheckedFolder()?.forEach {
                if (it.isChecked) {
                    File(it.path)?.let {
                        if (it.exists() && it.length() > 0) {
                            currentSelectNum++
                        }
                    }
                }
            }
        }catch (e:Exception){
            KLoggerUtils.e("KPictureSelector->initCurrentSelectNum()异常2：\t"+e.message)
        }
    }


    //判断是否有相机拍照功能
    fun isCamera(): Boolean {
        //fixme 目前音频和视频，没有开启手动拍摄功能。
        if (isCamera2 && checkedFolderIndex == 0 && type != PictureConfig.TYPE_AUDIO && type != PictureConfig.TYPE_VIDEO) {
            return true
        }
        return false
    }

    fun isCamera(isCamera: Boolean): KPictureSelector {
        this.isCamera = isCamera
        return this
    }

    private var compressPath: String = KCacheUtils.getCachePath() + "/compress"//fixme 压缩路径（本应用缓存路径）,相机拍照路径是SD存储卡。

    fun setCompressPath(compressPath: String) {
        KPictureSelector.compressPath = compressPath
    }


    //存储压缩后路径（防止已经存在压缩文件之后，重复压缩）
    fun putCompreessPath(path: String?, compreePath: String?) {
        if (path != null) {
            compreePath?.let {
                //就使用KCacheUtils.putString();即getCache()
                KCacheUtils.putString(("kcompress_" + path).trim(), compreePath)
            }
        }
    }

    /**
     * 根据原图路径，获取对应的压缩路径
     */
    fun getCompressPath(path: String?): String? {
        if (path == null) {
            return null
        }
        KCacheUtils.getString(("kcompress_" + path).trim())?.toString()?.let {
            if (KFileUtils.getInstance().getFileSize(it) > 0) {//判断压缩文件是否存在
                return it
            }
        }
        return null
    }

    /**
     * fixme 获取压缩后的图片
     * @param path 原图片路径
     * @param minimumCompressSize 单位KB,小于该值不压缩
     * @return 返回压缩后的图片路径
     */
    fun getCompressImage(path: String?, minimumCompressSize: Int = KPictureSelector.minimumCompressSize, callback: (compressPath: String) -> Unit) {
        var isCompress = false
        var localPath = path
        //判断是否压缩过
        KPictureSelector.getCompressPath(localPath)?.let {
            if (it != null) {
                isCompress = true
                callback(it)
            }
        }
        if (!isCompress) {
            KLubanUtils.compress(localPath, KPictureSelector.minimumCompressSize) {
                if (it != null) {
                    KPictureSelector.putCompreessPath(localPath, it)//保存和记录，该文件已经压缩过了。
                    callback(it)
                }
            }
        }
    }

    //fixme 删除缓存及拍照图片(包括裁剪和压缩后的缓存，要在上传成功后调用)
    fun deleteCacheDirFile() {
        try {
            KFileUtils.getInstance().delAllFiles(getCompressPath())//删除压缩文件
            KFileUtils.getInstance().delAllFiles(getCameraPath())//删除本应用相机文件
            KFileUtils.getInstance().delAllFiles(Environment.getExternalStorageDirectory().absolutePath + "/PictureSelector/CameraImage/")//第三方图片选择器里的拍照图片位置。
            clear()//清除
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * fixme 清除数据
     */
    fun clear() {
        selectionMedia?.clear()
        selectionMedia2?.clear()//选中文件也清空，防止因为文件删除，而导致顺序错乱。
        selectionMedia = null
        selectionMedia2 = null
        folders?.clear()
        folders = null
    }

    //图片列表没列显示的个数
    fun imageSpanCount(imageSpanCount: Int = KPictureSelector.imageSpanCount): KPictureSelector {
        KPictureSelector.imageSpanCount = imageSpanCount
        return this
    }

    //最大选中数
    fun maxSelectNum(maxSelectNum: Int = KPictureSelector.maxSelectNum): KPictureSelector {
        KPictureSelector.maxSelectNum = maxSelectNum
        return this
    }

    //压缩的最小值，小于该值不压缩
    fun minimumCompressSize(minimumCompressSize: Int = KPictureSelector.minimumCompressSize): KPictureSelector {
        KPictureSelector.minimumCompressSize = minimumCompressSize
        return this
    }

    //是否压缩
    fun isCompress(isCompress: Boolean = KPictureSelector.isCompress): KPictureSelector {
        KPictureSelector.isCompress = isCompress
        return this
    }

    var selectionMedia: MutableList<KLocalMedia>? = null//自己记录选中的数据
    private var selectionMedia2: MutableList<KLocalMedia>? = null//用户自己传入的选中数据

    //默认选中的数据
    fun selectionMedia(selectionMedia: MutableList<KLocalMedia>? = KPictureSelector.selectionMedia): KPictureSelector {
        selectionMedia2 = selectionMedia
        return this
    }

    /**
     * fixme 跳转到图片选择器
     * @param selectCallback 选中回调
     */
    fun forResult(activity: Activity? = KBaseUi.getActivity(), selectCallback: ((selectDatas: MutableList<KLocalMedia>) -> Unit)? = null) {
        checkedFolderIndex = 0
        currentSelectNum = 0
        selectMap?.clear()
        this.isCamera2 = this.isCamera//是否有摄像头
        this.isCamera = true//恢复摄像头原有配置
        //selectionMedia2是默认选中的数据。
        selectionMedia2?.let {
            var checkedNum = 1
            it.forEach {
                it.checkedNum = checkedNum//fixme 防止图片删除之后顺序错乱，给选中重新编号。
                checkedNum++
                it.isChecked = true//fixme 重新赋值选中，防止异常没有选中。
                if (it.path != null) {
                    selectMap.put(it.path!!, it)//fixme 保存选中的数据
                }
            }
            currentSelectNum = it.size
            //selectionMedia2?.clear()//不要clear(),会影响selectionMedia
            selectionMedia2 = null//置空，不会影响selectionMedia
        }
        KPictureSelector.selectCallback = selectCallback
//        activity?.apply {
//            if (Build.VERSION.SDK_INT>=21){
//                window?.enterTransition = Fade().setDuration(350)//fixme 使用5.0渐变转场动画。
//                window?.exitTransition = Fade().setDuration(350)
//            }
//        }
        KUiHelper.goActivity(KPhotoActivity::class.java, activity)
        activity?.apply {
            overridePendingTransition(R.anim.kera_from_small_to_large_a5, 0)
        }

    }

    var previewMeidas: MutableList<KLocalMedia>? = null//图片预览集合
    var previewIndex: Int = 0
    var isCheckable: Boolean = false//图片预览是否具备图片选中能力
    var requestCode_preview = 203
    var resultCode_preview = 204

    fun openExternalPreview(activity: Activity? = KBaseUi.getActivity(), index: Int = 0, meidas: MutableList<KLocalMedia>? = getCheckedFolder(), isCheckable: Boolean = false) {
        openExternalPreview(activity = activity, sharedElement = null, index = index, meidas = meidas, isCheckable = isCheckable)
    }

    /**
     * fixme 图片预览
     * @param sharedElement 共享元素;fixme 不为空会进行共享元素动画跳转；预览里面：共享元素名称； transitionName = "share_kitem_img"；在viewPager上。
     * @param index 当前所在图片下标
     * @param meidas 所有图片集合
     * @param isCheckable 图片是否具备选择能力，true,图片右上角会有选中框。false则没有。
     */
    fun openExternalPreview(activity: Activity? = KBaseUi.getActivity(), sharedElement: View?, index: Int = 0, meidas: MutableList<KLocalMedia>? = getCheckedFolder(), isCheckable: Boolean = false) {
        meidas?.let {
            if (it.size > 0) {
                previewIndex = index
                previewMeidas = meidas
                KPictureSelector.isCheckable = isCheckable
                if (Build.VERSION.SDK_INT >= 21 && sharedElement != null && sharedElement.transitionName != null) {
                    //fixme 共享元素动画跳转
                    KUiHelper.goActivityForResult(KPreviewActivity::class.java, sharedElement, activity, requestCode = requestCode_preview)
                } else {
                    //普通正常跳转
                    KUiHelper.goActivityForResult(KPreviewActivity::class.java, activity, requestCode = requestCode_preview)
                    activity?.apply {
                        overridePendingTransition(R.anim.kera_from_small_to_large_a5, 0)
                    }
                }
            }
        }
    }

    fun openExternalPreview2(activity: Activity? = KBaseUi.getActivity(), index: Int = 0, meidas: MutableList<File>?, isCheckable: Boolean = false) {
        openExternalPreview2(activity = activity, sharedElement = null, index = index, meidas = meidas, isCheckable = isCheckable)
    }

    fun openExternalPreview2(activity: Activity? = KBaseUi.getActivity(), sharedElement: View?, index: Int = 0, meidas: MutableList<File>?, isCheckable: Boolean = false) {
        meidas?.let {
            if (it.size > 0) {
                var meidas2 = mutableListOf<KLocalMedia>()
                var index2 = 0
                meidas?.forEach {
                    var localMedia = KLocalMedia()
                    localMedia.path = it.absolutePath
                    if (index == index2 && isCheckable) {
                        localMedia.isChecked = true
                    }
                    meidas2.add(localMedia)
                }
                previewIndex = index
                previewMeidas = meidas2
                KPictureSelector.isCheckable = isCheckable
                if (Build.VERSION.SDK_INT >= 21 && sharedElement != null && sharedElement.transitionName != null) {
                    //fixme 共享元素动画跳转
                    KUiHelper.goActivityForResult(KPreviewActivity::class.java, sharedElement, activity, requestCode = requestCode_preview)
                } else {
                    //普通正常跳转
                    KUiHelper.goActivityForResult(KPreviewActivity::class.java, activity, requestCode = requestCode_preview)
                    activity?.apply {
                        overridePendingTransition(R.anim.kera_from_small_to_large_a5, 0)
                    }
                }
            }
        }
    }

    fun openExternalPreview2(activity: Activity? = KBaseUi.getActivity(), sharedElement: View?, file: File?) {
        if (file != null) {
            var meidas = mutableListOf<File>()
            meidas.add(file)
            openExternalPreview2(activity = activity, sharedElement = sharedElement, index = 0, meidas = meidas, isCheckable = false)
        }
    }

    fun openExternalPreview2(activity: Activity? = KBaseUi.getActivity(), file: File?) {
        openExternalPreview2(activity = activity, sharedElement = null, file = file)
    }

    /**
     * fixme 单张图片预览。
     */
    fun openExternalPreview2(activity: Activity? = KBaseUi.getActivity(), path: String?) {
        if (path != null) {
            openExternalPreview2(activity, File(path))
        }
    }

    private var selectCallback: ((selectDatas: MutableList<KLocalMedia>) -> Unit)? = null

    //fixme 选中回调
    fun selectCallback(callback: () -> Unit) {
        if (selectCallback != null) {
            getSelectDatas()?.let {
                if (it.size > 0) {
                    var data = it
                    if (isCompress) {
                        //压缩
                        compress(0, it) {
                            callback?.let {
                                it()//完成回调
                            }
                            selectCallback?.let {
                                it(data)//选中数据大于0，且不为空才会回调。
                            }
                            selectCallback = null
                        }
                    } else {
                        callback?.let {
                            it()//完成回调
                        }
                        selectCallback?.let {
                            it(data)//选中数据大于0，且不为空才会回调。
                        }
                        selectCallback = null
                    }
                } else {
                    callback?.let {
                        it()//完成回调
                    }
                }
            }
        } else {
            callback?.let {
                it()//完成回调
            }
        }
    }

    /**
     * fixme 压缩
     * @param index 下标
     * @param medias 集合
     * @param callback 回调（所有集合都压缩完成之后才调用）
     */
    fun compress(index: Int, medias: MutableList<KLocalMedia>, callback: () -> Unit) {
        //KLoggerUtils.e("当前进度：\t" + index + "\t总进度：\t" + medias.lastIndex)
        var media = medias[index]
        if (!media.isCompressed || media.compressPath == null) {
            media?.path?.let {
                //判断本地是否存在压缩文件（防止重复压缩）;亲测有效！
                getCompressPath(it)?.let {
                    media.isCompressed = true
                    media.compressPath = it
                }
            }
        }
        //KFileUtils.getInstance().getFileSize(media.compressPath) 防止压缩文件为空（防止已经删除）
        if (media.isCompressed && media.compressPath != null && KFileUtils.getInstance().getFileSize(media.compressPath) > 0) {
            //KLoggerUtils.e("已经压缩了")
            //已经压缩过
            var index2 = index + 1
            if (index2 > medias.lastIndex) {
                callback()//回调压缩完成
            } else {
                compress(index2, medias, callback)//继续压缩
            }
        } else {
            //KLoggerUtils.e("没有压缩")
            media.isCompressed = false
            media.compressPath = null
            if (media.isPicture()) {
                //fixme 对图片进行压缩
                //没有压缩则对图片进行压缩
                KLubanUtils.compress(media.path, minimumCompressSize = this.minimumCompressSize) {
                    if (it != null) {
                        medias[index].isCompressed = true
                        medias[index].compressPath = it
                        putCompreessPath(medias[index].path, medias[index].compressPath)
                    }
                    var index2 = index + 1
                    if (index2 >= 0 && index2 <= medias.lastIndex) {
                        compress(index2, medias, callback)//继续压缩
                    } else {
                        callback()//回调压缩完成
                    }
                }
            } else {
                //fixme 图片以外，如视频，不压缩。
                var index2 = index + 1
                if (index2 >= 0 && index2 <= medias.lastIndex) {
                    compress(index2, medias, callback)//继续压缩
                } else {
                    callback()//回调压缩完成
                }
            }
        }

    }

    /**
     * 获取选中的集合
     */
    private fun getSelectDatas(): MutableList<KLocalMedia>? {
        if (selectionMedia == null) {
            selectionMedia = mutableListOf()
        }
        selectionMedia?.clear()
        getCheckedFolder()?.forEach {
            if (it.isChecked) {
                selectionMedia?.add(it)
            }
        }
        selectionMedia?.let {
            if (it.size<=0){
                cameraFirstKLocalMedia?.let {
                    if (it.isChecked){
                        selectionMedia?.add(it)
                    }
                }
            }
        }
        return selectionMedia
    }

    //fixme 获取预览选中数据。
    fun getPreSelectDatas(): MutableList<KLocalMedia>? {
        var selectionMedia: MutableList<KLocalMedia> = mutableListOf()
        getCheckedFolder()?.forEach {
            if (it.isChecked) {
                selectionMedia?.add(it)
            }
        }
        if (selectionMedia.size > 0) {
            var comparator = object : Comparator<KLocalMedia> {
                override fun compare(p0: KLocalMedia?, p1: KLocalMedia?): Int {
                    if (p0 != null && p1 != null) {
                        //KLoggerUtils.e("p0：\t"+p0.checkedNum+"\tp1:\t"+p1.checkedNum)
                        //p0-p1 大于0；p0排序在后；
                        //即返回值：大于0 p0排序在后，小于0排序在前。
                        return p0.checkedNum - p1.checkedNum
                    }
                    return 0
                }
            }
            Collections.sort(selectionMedia, comparator)
        }
        return selectionMedia
    }

    //保持选中的集合(键：path和值：checkedNum)
    var selectMap = mutableMapOf<String, KLocalMedia>()

    //判断是否选中（如果存在则返回）
    fun contains(localMedia: KLocalMedia): KLocalMedia? {
        var key = localMedia.path
        if (selectMap.containsKey(key)) {
            return selectMap.get(key)
        }
        return null
    }

    //选中加1
    fun addNum(index: Int): Boolean {
        return addNum(getCheckedFolder()?.get(index))
    }

    fun addNum(path: String?): Boolean {
        return addNum(File(path))
    }

    var cameraFirstKLocalMedia: KLocalMedia? = null//fixme 相册为空时；记录相机拍照的第一个图片（防止相册图片为空时，图片没有显示问题）。

    fun addNum(file: File?): Boolean {
        file?.let {
            if (it.path != null && it.length() > 0) {
                var kLocalMedia = KLocalMedia()
                kLocalMedia.path = it.path
                return addNum(kLocalMedia)
            }
        }
        return false
    }

    fun addNum(data: KLocalMedia?): Boolean {
        if (data != null && data.path != null) {
            if (currentSelectNum < maxSelectNum) {
                currentSelectNum++
                data?.isChecked = true
                data?.checkedNum = currentSelectNum
                selectMap?.put(data.path!!, data)//fixme 保存选中的键值
                cameraFirstKLocalMedia = data
                return true//选中成功
            } else {
                data?.isChecked = false
                return false//选中失败
            }
        }
        return false
    }

    //不选中则减1
    fun reduceNum(index: Int) {
        reduceNum(getCheckedFolder()?.get(index))
    }

    fun reduceNum(data: KLocalMedia?) {
        if (data != null && data.path != null) {
            currentSelectNum--
            if (currentSelectNum <= 0) {
                currentSelectNum = 0
            }
            data.isChecked = false
            selectMap?.remove(data.path!!)
            var checkNum = data.checkedNum
            getCheckedFolder()?.forEach {
                if (it.isChecked != null && it.isChecked!! && it.checkedNum >= checkNum) {
                    it.checkedNum--
                    if (it.checkedNum <= 0) {
                        it.checkedNum = 0
                    }
                    if (it.path != null) {
                        selectMap?.put(it.path!!, it)//fixme 保存变化的键值
                    }
                }
            }
            //调用者记得刷新适配器。
        }
    }


}
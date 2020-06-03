package cn.oi.klittle.era.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import cn.oi.klittle.era.R
import cn.oi.klittle.era.activity.photo.manager.KPictureSelector
import cn.oi.klittle.era.activity.photo.utils.KDCIMUtils
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.exception.KCatchException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.*


//fixme 注意调用前，需要在Activity中添加以下方法。[不过我已经在BaseActivity中添加以下方法了，如果继承了BaseActvity就不用再写了]
//override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    PermissionUtils.onRequestPermissionsResult(getActivity(), requestCode, permissions, grantResults)
//}
//
//override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//    super.onActivityResult(requestCode, resultCode, data)
//    PictureUtils.onActivityResult(this, requestCode, resultCode, data)
//}

//fixme 相机拍照，返回压缩后的图片：KPictureUtils.cameraCompress{}
//fixme photo{}系统相册
//fixme crop{}系统裁剪

/**
 *相册，相机，视频，剪切
 */
object KPictureUtils {

    //文件路径[需要file_paths.xml才能访问]
    //fixme 本应用，相机拍摄的图片会保存在该位置。
    fun getCameraPath(): String {
        return KPathManagerUtils.getCameraPath()
    }

    //视频录制路径
    fun getAppVideoPath(): String {
        return KPathManagerUtils.getAppVideoPath()
    }

    //文件裁剪路径
    fun getAppCropPath(): String {
        return KPathManagerUtils.getAppCropPath()
    }

    //获取相册图片路径
    fun getPhotoPath(data: Intent?, activtiy: Activity? = KPathManagerUtils.getActivity()): String? {
        return KPathManagerUtils.getPhotoPath(data, activtiy)
    }

    val DEFAULT_KEYS_PICTURE_PHOTO = 3828//相册图库选择
    val DEFAULT_KEYS_CROP_PHOTO = 3829//图片剪切
    val DEFAULT_KEYS_CAMARA_PHOTO = 3830//相机
    val DEFAULT_KEYS_VIDEO_PHOTO = 3831//视频相册
    val DEFAULT_KEYS_VIDEO_CAPTURE = 3832//视频录制
    var cllback: ((file: File) -> Unit)? = null//fixme 回调，返回是原始数据文件哦。是本地原文件

    //fixme 打开系统相册【不需要任何权限，亲测百分百可用】,系统会跳出一个相册选择框。无法跳过这一步【无解】。
    //只能选择一个。系统没有多选。都是单选。
    var galleryPackName: String? = null//相册包名
    var packNameError = KBaseUi.getString(R.string.kpackNameError)//"指定包名异常"
    fun photo(activity: Activity, callback2: (file: File) -> Unit) {
        try {
            val intent = Intent()
            //i.setType("image/jpeg");//一般拍照的格式就是jpeg【jpeg就是.jpg】
            intent.type = "image/*"
            intent.putExtra("return-data", false)//true的话直接返回bitmap，可能会很占内存 不建议。我们这里直接返回File文件
            intent.action = Intent.ACTION_PICK//不能再调用intent.addCategory(),会出错。
            intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI//必不可少，不然图片返回错误
            if (galleryPackName == null) {
                galleryPackName = KAppUtils.getGalleryPackName(activity)//获取系统相册包名
            }
            galleryPackName?.let {
                if (!it.equals(packNameError)) {
                    intent.setPackage(galleryPackName)//指定系统相册（不会再跳选择框了。欧耶！）
                }
            }
            activity.startActivityForResult(intent, DEFAULT_KEYS_PICTURE_PHOTO)//自定义相册标志
            this.cllback = callback2
        } catch (e: Exception) {
            KLoggerUtils.e("相册崩坏" + e.message, isLogEnable = true)
            galleryPackName?.let {
                if (e.message?.contains(it) ?: false) {
                    if (!it.equals(packNameError)) {
                        galleryPackName = packNameError
                        photo(activity, callback2)
                    }
                }
            }

        }
    }

    /**
     * fixme 调用相机拍照成功后，需要通知系统。这样系统相册里才能看到该图片。只对系统相机拍摄的照片才有效(亲测！)。应用本身内的图片无效。
     * fixme 只对存储在SD卡上的文件才有效，应用自带的缓存目录。系统依然读不出来。
     * @param file 照片文件。
     */
    fun updateFileFromDatabase_add(file: File?, activity: Activity? = KBaseUi.getActivity()) {
        if (file == null) {
            return
        }
        activity?.let {
            if (!it.isFinishing) {
                try {
                    if (file != null && file.exists() && file.length() > 0) {
                        //fixme 关闭发送很快的，不需要协程。不耗时。
                        //fixme 发送系统广播，这样图片选择器就能够读取到该图片文件了。基本对所有机型都有效。
                        it?.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))//fixme 这里不要使用FileProvider；不然无效（图片选择器会无法读取）。
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * @param filePath 照片文件路径
     */
    fun updateFileFromDatabase_add(filePath: String, activity: Activity? = KBaseUi.getActivity()) {
        if (filePath == null) {
            return
        }
        if (filePath.length <= 0) {
            return
        }
        updateFileFromDatabase_add(File(filePath), activity)
    }

    /**
     * fixme 删除文件后更新数据库  通知媒体库更新文件;
     * @param filepath fixme 文件的完整路径。如：/data/user/0/com.example.myapplication/cache/compress/1585131992658549.jpeg
     */
    public fun updateFileFromDatabase_del(filepath: String, context: Context? = KBaseUi.getActivity()) {
        if (context == null) {
            return
        }
        GlobalScope.async {
            MediaScannerConnection.scanFile(context, arrayOf(filepath), null, null)//fixme 通知系统更新。该文件已经删除。亲测有效。基本对所有设备都有效。pda也有效。
        }
        //var dirPath = KFileUtils.getInstance().getFileDir(filepath)//根据文件的完整路径，获取文件所在文件夹路径。
        //updateDirFromDatabase_del(dirPath, context)
    }

    /**
     * fixme 删除文件后更新数据库  通知媒体库更新文件夹,！！！！！dirPath（文件夹路径）要求尽量精确。系统会更新该文件夹。
     * @param dirPath fixme 文件夹路径，如：/data/user/0/com.example.myapplication/cache/compress
     */
    public fun updateDirFromDatabase_del(dirPath: String, context: Context? = KBaseUi.getActivity()) {
        if (dirPath == null) {
            return
        }
        if (dirPath.trim().length <= 0) {
            return
        }
        //fixme 更新系统相册是耗时操作，所以放在协程里。文件夹里的照片数量越多越耗时。
        GlobalScope.async {
            try {
                //var ltime=System.currentTimeMillis()
                //fixme 以下方法，亲测有效。在协程里面也有效，非主线程也有效。这个更新文件夹。大多数设备都有效。pda测试好像无效。
                var where = MediaStore.Audio.Media.DATA + " like \"" + dirPath + "%" + "\""
                context?.getContentResolver()?.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null)//fixme 文件夹内的照片数量越多，越耗时。
//          var i = context?.getContentResolver()?.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null)
//        if (i > 0) {
//            Log.e(TAG, "媒体库更新成功！");
//        }
                //KLoggerUtils.e("相册更新耗时：\t"+(System.currentTimeMillis()-ltime))//小米8耗时2832；
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //KPictureUtils.cameraCompress { srcfile, compressFile -> }
    /**
     *fixme 相机拍照，会对图片进行压缩处理;调用案例：KPictureUtils.cameraCompress{}
     *fixme 相机拍照的时候，在onActivityResult里发送了广播（往下翻），所以相机能够看到拍照的图片，但是不能看到压缩后的图片。因为压缩后的图片，并没有发送系统广播。
     * @param minimumCompressSize 单位KB,小于该值不压缩
     * @return 返回原文件，和压缩后的文件
     */
    fun cameraCompress(activity: Activity? = KBaseUi.getActivity(), minimumCompressSize: Int = KPictureSelector.minimumCompressSize, callback2: (srcfile: File, compressFile: File) -> Unit) {
        camera(activity) {
            var src = it//fixme 原文件
            KPictureSelector.getCompressImage(it.absolutePath, minimumCompressSize) {
                var compress = it//fixme 压缩后的文件
                callback2(src, File(compress))
            }
        }
    }

    //KPictureUtils.cameraCompress { compressFile -> }
    /**
     * fixme 相机拍照，只返回压缩后的图片。原文件会自动删除。只保留压缩后的。
     */
    fun cameraCompress(activity: Activity? = KBaseUi.getActivity(), minimumCompressSize: Int = KPictureSelector.minimumCompressSize, callback2: (compressFile: File) -> Unit) {
        camera(activity) {
            var src = it//fixme 原文件
            KPictureSelector.getCompressImage(it.absolutePath, minimumCompressSize) {
                var compress = it//fixme 压缩后的文件
                try {
                    if (!src.absolutePath.equals(it)) {
                        src.delete()//fixme 原文件和压缩文件不一致，直接删除原文件。只保留压缩后的。
                        updateFileFromDatabase_del(filepath = src.absolutePath)//fixme 告诉系统，更新该文件夹。
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                callback2(File(compress))
            }
        }
    }

    //var fileUri: Uri? = null// 相机拍照时创建的Uri链接,fileUri.getPath()获取拍照图片路径
    //var cramefile: File? = null//相机照片
    var cramePath: String? = null//相机照片路径
    var CameraPackName: String? = null//相机包名

    //相机拍照【需要相机权限,如果清单里不写明相机权限,部分设备默认是开启。但是有的设备就不行，可能异常奔溃。所以保险还是在清单里加上权限声明】
    //fixme 相机拍照，不会对图片进行压缩处理。
    //fixme 亲测相机拍摄，还需要SD卡存储的权限
    fun camera(activity: Activity? = KBaseUi.getActivity(), callback2: (file: File) -> Unit) {
        if (activity == null || activity.isFinishing) {
            return
        }
        //fixme 获取SD卡的权限
        KPermissionUtils.requestPermissionsStorage {
            if (it) {
                //fixme 获取相机的权限
                KPermissionUtils.requestPermissionsCamera(activity) {
                    if (it) {
                        try {
                            var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            //resolveActivity()查询是否有第三方能够启动该intent;fixme resolveActivity()靠不住。建议不要使用。
                            //if (intent.resolveActivity(activity.getPackageManager()) != null) {
                            if (activity != null && !activity.isFinishing()) {
                                //PNG格式的不能显示在相册中
                                var cramefile = KFileUtils.getInstance().createFile(getCameraPath(), "IMG_" + KCalendarUtils.getCurrentTime("yyyyMMdd_HHmmssSSS") + ".jpg")//相机拍摄的照片位置。不使用SD卡。这样就不需要SDK权限。
                                cramePath = cramefile?.absolutePath
                                var fileUri: Uri
                                if (Build.VERSION.SDK_INT >= 21) {//7.0及以上版本(版本号24),为了兼容6.0(版本号23)，防止6.0也可能会有这个问题。22是5.1的系统。
                                    //getPackageName()和${applicationId}显示的都是当前应用的包名。无论是在library还是moudle中，都是一样的。都显示的是当前应用moudle的。与类库无关。请放心使用。
                                    //fixme 无论是Activity还是Context。getPackageName()返回的都是当前应用的包名。
                                    fileUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".kera.provider", //与android:authorities="${applicationId}.kera.provider"对应上
                                            cramefile!!)
                                    //测试发现，5.0（版本号21）也可以使用FileProvider
                                    //FileProvider 图片路径不管是SD卡上，还是本应用缓存的路径，都有效。相机图片都能返回。
                                } else {
                                    fileUri = Uri.fromFile(cramefile!!)//路径必须是本地SD卡存储上的路径，不然图片无法返回。
                                }
                                //KLoggerUtils.e("相机拍照：\t" + cramefile + "\t" + Build.VERSION.SDK_INT)
                                //以下两个addFlags必不可少。【以防万一出错】
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                                intent.putExtra("return-data", false)
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)//必不可少
                                if (CameraPackName == null) {
                                    CameraPackName = KAppUtils.getCameraPackName(activity)//获取系统相机包名
                                }
                                CameraPackName?.let {
                                    if (!it.equals(packNameError)) {
                                        intent.setPackage(CameraPackName)//指定系统相机（不会再跳选择框了。欧耶！）
                                    }
                                }
                                //fixme 默认一般打开的都是后置摄像机。放心。基本都是打开的后置。
                                //intent.putExtra("android.intent.extras.CAMERA_FACING", 1);//fixme 前置相机，部分机型有效（小米有效）。不是全部机型都有效(PDA就无效)。
                                activity.startActivityForResult(intent, DEFAULT_KEYS_CAMARA_PHOTO)//自定义相机标志
                                this.cllback = callback2
                            }
                        } catch (e: Exception) {
                            // TODO: handle exception
                            KLoggerUtils.e("相机拍照崩坏:\t" + e.message, isLogEnable = true)
                            CameraPackName?.let {
                                if (e.message?.contains(it) ?: false) {
                                    if (!it.equals(packNameError)) {
                                        CameraPackName = packNameError
                                        camera(activity, callback2)
                                    }
                                }
                            }
                        }
                    } else {
                        KPermissionUtils.showFailure(activity, KPermissionUtils.perMissionTypeCamera)//fixme 提示打开相机权限
                    }
                }
            } else {
                KPermissionUtils.showFailure(activity, KPermissionUtils.perMissionTypeStorage)//fixme 提示SD卡读取权限
            }
        }

    }

    var cropfile: File? = null//裁剪文件

    //                               fixme 裁剪调用案例
//                            KPictureUtils.crop(srcfile,1,1){
//                                KLoggerUtils.e("裁剪：\t"+it.absolutePath)
//                            }
    fun crop(file: File, w: Int, h: Int, isDel: Boolean = true, callback2: (file: File) -> Unit) {
        crop(KBaseUi.getActivity(), file, w, h, -1, -1, isDel, callback2)
    }

    /**
     * fixme 图片剪辑【可以在相册，拍照回调成功后手动调用哦。】,兼容7.0。模拟器上没有上面效果。6.0的真机都没问题。7.0的真机没有测试。
     * w:h 宽和高的比率
     * width:height实际裁剪的宽和高的具体值。
     * @param isDel fixme 裁剪之后，是否删除原文件。true删除（原文件与裁剪文件不同时才会删除原文件）；false不删除。
     */
    fun crop(activity: Activity? = KBaseUi.getActivity(), file: File, w: Int, h: Int, isDel: Boolean, callback2: (file: File) -> Unit) {
        crop(activity, file, w, h, -1, -1, isDel, callback2)
    }

    public var isDelSrc: Boolean = true;//fixme 裁剪之后，是否删除原文件。
    public var srcFile: File? = null//fixme 裁剪之前的原文件；在下面的 onActivityResult（）方法里。
    fun crop(activity: Activity? = KBaseUi.getActivity(), file: File, w: Int, h: Int, width: Int, height: Int, isDel: Boolean, callback2: (file: File) -> Unit) {
        try {
            if (activity == null || activity.isFinishing) {
                return
            }
            //fixme 申请SD卡权限。
            KPermissionUtils.requestPermissionsStorage {
                if (it) {
                    try {
                        if (file == null || file.length() <= 0) {
                            return@requestPermissionsStorage
                        }
                        this.isDelSrc = isDel
                        this.srcFile = file
                        //fixme AssetsUtils.getInstance().getBitmapFromFile(it.path, true,false)
                        //fixme [注意了哦。如果图片剪切了，就不要读取缓存哦。]
                        cropfile = KFileUtils.getInstance().copyFile(file, getAppCropPath(), file.name)
                        var intent = Intent("com.android.camera.action.CROP")
                        //resolveActivity()查询是否有第三方能够启动该intent;fixme 不要使用。系统自带的裁剪可能查不出来，不可能，不要使用。
                        //if (intent.resolveActivity(activity.getPackageManager()) != null) {
                        if (activity != null && !activity.isFinishing()) {
                            var fileUri: Uri
                            if (Build.VERSION.SDK_INT >= 21) {//7.0及以上版本(版本号24),为了兼容6.0(版本号23)，防止6.0也可能会有这个问题。
                                //相机里面也使用了这个，多次使用不会出错。可以重复使用，不冲突。
                                fileUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".kera.provider", //与android:authorities="${applicationId}.kera.provider"对应上
                                        cropfile!!)
                            } else {
                                fileUri = Uri.fromFile(cropfile)//这个是原图
                            }
                            //剪辑图片之后，保存的位置。
                            //这个是裁剪之后的图,截图保存的uri必须使用Uri.fromFile(),之前测试是这样，现在好像又不是这样了。也需要使用FileProvider了
                            //保存位置只能是SD卡或者file源文件位置。无法指定我们app的私有目录。
                            //var cropUri = Uri.fromFile(cropfile)
                            var cropUri = fileUri//两个uri指向同一个才有效。这样裁剪才能访问我们app的私有目录。亲测。
                            //以下两个addFlags必不可少。
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                            intent.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
                            //告诉系统需要裁剪,以下這兩个参数可有可无，为了兼容性，两个都设置成true,以防万一。
                            intent.putExtra("crop", "true")
                            intent.putExtra("scale", true)//缩放功能禁止不了，系统裁剪，肯定自带缩放的功能。无法固定大小，只能固定宽高比例裁剪。总之缩放功能禁止不了，无论你设置true还是false都一样。自带缩放。
                            //width:height 裁剪框的宽高比
                            //intent.putExtra("aspectX", 1);
                            //intent.putExtra("aspectY", 1);
                            intent.putExtra("aspectX", w)
                            intent.putExtra("aspectY", h)
                            //裁剪的宽和高。具体的数值。
                            // [亲测，真实有效,不管数值是多少（小于0无效，其他都有效，多大都有效）。
                            // 宽高比例不对，会对图片进行拉伸。即会变形。]
                            //图片太小会模糊，太大不会模糊。
                            if (width > 0 && height > 0) {
                                //如果不传，就会根据裁剪，自定义决定大小。
                                //必须是Int类型才有效。
                                intent.putExtra("outputX", width);
                                intent.putExtra("outputY", height);
                            }
                            intent.putExtra("return-data", false)//true的话直接返回bitmap，可能会很占内存 不建议
                            intent.putExtra("noFaceDetection", true)//去除默认的人脸识别，否则和剪裁匡重叠
                            intent.setDataAndType(fileUri, "image/*")//读取的uri,即要裁剪的uri
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, cropUri)//截图保存的uri必须使用Uri.fromFile()
                            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())//输出格式
                            activity.startActivityForResult(intent, DEFAULT_KEYS_CROP_PHOTO)//自定义剪辑标志
                            this.cllback = callback2
                        }
                    } catch (e: Exception) {
                        KLoggerUtils.e("调用系统裁剪异常:\t" + KCatchException.getExceptionMsg(e))
                    }
                } else {
                    KPermissionUtils.showFailure(activity)
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            KLoggerUtils.e("调用系统裁剪异常2:\t" + KCatchException.getExceptionMsg(e))
        }
    }

    //打开系统本地视频,只能选择一个。系统没有多选。都是单选。
    //【不需要任何权限，亲测百分百可用】
    //本地视频，一般只能识别mp4和3gp格式。其他特殊格式暂时识别不出。
    var videoPackName: String? = null//视频包名

    /**
     * 选取本地视频（在小米上，有时明明选择的是视频，返回的确实图片。这个Bug是硬伤啊。其他的还好。）
     * fixme 注意了，小米上，可能返回的是图片哦。一般都是视频。
     * fixme 之所以返回的是图片，是因为该是视频存储在云端。视频还没下载下来，所以返回的图片。
     * fixme 如果该视频已经下载下来了。肯定返回的就是视频。视频还没下载下来。只能返回图片了。
     */
    fun video(activity: Activity? = KBaseUi.getActivity(), callback2: (file: File) -> Unit) {
        if (activity == null) {
            return
        }
        activity?.let {
            if (it.isFinishing) {
                return
            }
        }
        try {
            var intent = Intent(Intent.ACTION_PICK)
            //fixme resolveActivity()靠不住，建议不要使用。
            //if (activity != null && !activity.isFinishing() && intent.resolveActivity(activity.getPackageManager()) != null) {
            if (activity != null && !activity.isFinishing()) {
                intent.type = "video/*"
                intent.putExtra("return-data", false)//true的话直接返回bitmap，可能会很占内存 不建议
                //intent.action = Intent.ACTION_PICK//不能再调用intent.addCategory(),会出错。
                //亲测，不加这个也能获取流。也能获取文件名。只能获取文件名。无法获取标题路径和ID等。最好不要加这个，加了这个就没有本地视频选项，只有相册【相册里面也是视频】。
                //intent.setData(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);//不加这个，也能获取流，获取文件名。还能有本地视频选项。加了这句就没有本地视频选项。
                //以下两个addFlags必不可少。【以防万一出错】
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)

                if (videoPackName == null) {
                    videoPackName = KAppUtils.getGalleryPackName(activity)//fixme 获取系统相册包名（本地视频的无法获取。）
                }
                videoPackName?.let {
                    if (!it.equals(packNameError)) {
                        intent.setPackage(videoPackName)//指定系统相册（不会再跳选择框了。欧耶！）
                    }
                }
                activity.startActivityForResult(intent, DEFAULT_KEYS_VIDEO_PHOTO)//自定义视频相册标志
                this.cllback = callback2
            }
        } catch (e: Exception) {
            videoPackName?.let {
                if (e.message?.contains(it) ?: false) {
                    if (!it.equals(packNameError)) {
                        videoPackName = packNameError
                        video(activity, callback2)
                    }
                }
            }
            KLoggerUtils.e("视频相册崩坏2" + e.message, isLogEnable = true)
        }

    }

    var cameraVideoFile: File? = null//视频录制文件

    /**
     * fixme 系统视频拍摄录制。
     * 跳转系统相机视频拍摄【需要相机权限】,进行视频录制
     * 手机拍摄的格式一般都是mp4的格式。
     */
    fun cameraVideo(activity: Activity? = KBaseUi.getActivity(), callback2: (file: File) -> Unit) {
        if (activity == null) {
            return
        }
        activity?.let {
            if (it.isFinishing) {
                return
            }
        }
        //fixme 获取SD卡的权限
        KPermissionUtils.requestPermissionsStorage {
            if (it) {
                KPermissionUtils.requestPermissionsCamera(activity) {
                    if (it) {
                        try {
                            var fileUri: Uri? = null
                            val path = getAppVideoPath()//相机视频拍摄存储位置。不使用SD卡。使用自己应用私有SD卡目录，这样就不需要外部的SD卡权限。
                            cameraVideoFile = KFileUtils.getInstance().createFile(path, System.currentTimeMillis().toString() + ".mp4")//视频拍摄基本都是MP4格式。每次都以当前毫秒数重新创建拍摄文件。
                            cameraVideoFile?.let {
                                if (Build.VERSION.SDK_INT >= 23) {//7.0及以上版本(版本号24),为了兼容6.0(版本号23)，防止6.0也可能会有这个问题。
                                    //getPackageName()和${applicationId}显示的都是当前应用的包名。无论是在library还是moudle中，都是一样的。都显示的是当前应用moudle的。与类库无关。请放心使用。
                                    fileUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".kera.provider", //与android:authorities="${applicationId}.kera.provider"对应上
                                            it)
                                } else {
                                    fileUri = Uri.fromFile(cameraVideoFile)
                                }
                                val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                                //fixme resolveActivity()建议不要使用，靠不住。
                                //if (activity != null && !activity.isFinishing() && intent.resolveActivity(activity.getPackageManager()) != null) {
                                if (activity != null && !activity.isFinishing()) {
                                    intent.putExtra("return-data", false)//true的话直接返回bitmap，可能会很占内存 不建议
                                    //以下两个addFlags必不可少。【以防万一出错】
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                                    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)//必不可少。拍摄视频使用自定义文件路径。如果没有。默认使用系统的路径。那样就需要申请SD卡权限。不使用系统默认的。

                                    if (CameraPackName == null) {
                                        CameraPackName = KAppUtils.getCameraPackName(activity)//获取系统相机包名
                                    }
                                    CameraPackName?.let {
                                        if (!it.equals(packNameError)) {
                                            intent.setPackage(CameraPackName)//指定系统相机（不会再跳选择框了。欧耶！）
                                        }
                                    }

                                    activity.startActivityForResult(intent, DEFAULT_KEYS_VIDEO_CAPTURE)
                                    this.cllback = callback2
                                }
                            }
                        } catch (e: Exception) {
                            // TODO: handle exception
                            KLoggerUtils.e("系统相机录像崩坏" + e.message, isLogEnable = true)
                            CameraPackName?.let {
                                if (e.message?.contains(it) ?: false) {
                                    if (!it.equals(packNameError)) {
                                        CameraPackName = packNameError
                                        camera(activity, callback2)
                                    }
                                }
                            }
                        }
                    } else {
                        KPermissionUtils.showFailure(activity, KPermissionUtils.perMissionTypeCamera)
                    }
                }
            }
        }

    }

    //fixme 权限申请回调结果；
    fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            //KLoggerUtils.e("requestCode：\t" + requestCode + "\tresultCode:\t" + resultCode + "\tdata:\t" + data + "\tdata.data:\t" + data?.data)
            //resultCode 0 系统设置默认就是取消。
            if (requestCode == DEFAULT_KEYS_PICTURE_PHOTO && data != null && data.data != null) {
                //fixme 相册
                try {
                    var file: File? = null
                    val uri = data.data
                    var photoPath: String? = null
                    var photoName: String? = null
                    try {
                        photoPath = getPhotoPath(data, activity)// 获取相册图片原始路径
                        photoPath?.let {
                            photoName = it.substring(it.lastIndexOf("/") + 1)
                        }
                    } catch (e: Exception) {
                        // TODO: handle exception
                        KLoggerUtils.e("相册图片路径获取失败" + e.message, isLogEnable = true)
                    }
                    if (KPermissionUtils.requestPermissionsStorage(activity)) {
                        //有SD卡权限（可以直接操作原始图片）
                        //Log.e("test","系统图片:\t"+photoPath)
                        file = File(photoPath)
                    } else {
                        //没有SD卡权限（不能操作原始图片）
                        var f = KCacheUtils.getString(photoPath)
                        f?.let {
                            file = File(it)//获取缓存文件，避免重复创建。
                            file?.let {
                                if (it.exists()) {
                                    if (it.length() <= 0) {
                                        file = null
                                    }
                                } else {
                                    file = null
                                }
                            }
                        }
                        if (file == null) {
                            //创建新图片文件
                            file = KFileUtils.getInstance().createFile(getCameraPath(), photoName)
                            // 将Uri图片的内容复制到file上
                            writeFile(activity.getContentResolver(),
                                    file, uri)
                            photoPath?.let {
                                file?.getPath()?.apply {
                                    KCacheUtils.put(it, this)//存储文件路径
                                }
                            }
                        }
                    }
                    cllback?.let {
                        if (file != null) {
                            it(file!!)
                        }
                    }
                } catch (e: Exception) {
                    KLoggerUtils.e("图库相册异常:\t" + e.message, isLogEnable = true)
                }
            } else if (requestCode == DEFAULT_KEYS_CROP_PHOTO) {
                //fixme 裁剪
                //此时的data是空的。直接返回临时保存的裁剪文件
                cropfile?.let {
                    if (it.exists() && it.length() > 0) {
                        //fixme 再发送一次广播（防止第一次没有收到）。多次发送不会有影响。
                        activity?.let {
                            if (!it.isFinishing) {
                                //fixme 发送系统广播，这样图片选择器就能够读取到该图片文件了。
                                it?.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(cropfile)))
                            }
                        }
                    }
                }
                //回调
                cllback?.let {
                    if (cropfile != null && cropfile?.exists() ?: false && cropfile!!.length() > 0) {
                        it(cropfile!!)
                    }
                }
                cropfile?.let {
                    if (it.exists() && it.length() > 0) {
                        //fixme 再发送一次广播（防止第一次没有收到）。多次发送不会有影响。
                        activity?.let {
                            if (!it.isFinishing) {
                                //fixme 发送系统广播，这样图片选择器就能够读取到该图片文件了。
                                it?.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(cropfile)))
                            }
                        }
                    }
                }
                if (isDelSrc) {
                    //fixme 删除裁剪之前的原文件
                    srcFile?.let {
                        var src = it
                        //fixme 原文件与裁剪文件不相同。才删除原文件。
                        KFileUtils.getInstance().isSameFile(it, cropfile)?.let {
                            if (!it) {
                                KFileUtils.getInstance().delFile(src)//fixme 删除，内部会通知系统。该文件已经删除。
                            }
                        }
                    }
                }
                srcFile = null//fixme 这里置空，对之前it不受影响。
            } else if (requestCode == DEFAULT_KEYS_CAMARA_PHOTO) {
                //fixme 相机
                cllback?.let {
                    cramePath?.let {
                        var cramefile = File(it)
                        if (cramefile != null && cramefile?.exists() ?: false && cramefile!!.length() > 0) {
                            try {
                                it(cramefile!!)//fixme 回调
                                //fixme 再发送一次，cramefile可能在回调里，已经做了压缩等处理，文件已经改变。
                                activity?.let {
                                    if (!it.isFinishing) {
                                        //fixme 发送系统广播，这样图片选择器就能够读取到该图片文件了。
                                        it?.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(cramefile)))//fixme 这里不要使用FileProvider；不然无效（图片选择器会无法读取）。
                                    }
                                }
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }
                            try {
                                //fixme 再发送一次广播（防止第一次没有收到）。多次发送不会有影响。
                                activity?.let {
                                    if (!it.isFinishing) {
                                        //fixme 发送系统广播，这样图片选择器就能够读取到该图片文件了。
                                        it?.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(cramefile)))//fixme 这里不要使用FileProvider；不然无效（图片选择器会无法读取）。
                                    }
                                }
                                //fixme  删除部分手机 拍照在DCIM也生成一张的问题（防止异常生成多张，所以多调用remove()几次。）
                                KDCIMUtils.remove(activity)
                                KDCIMUtils.remove(activity)
                                KDCIMUtils.remove(activity)
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }
                            cramePath = null//置空
                        }
                    }

                }
            } else if (requestCode == DEFAULT_KEYS_VIDEO_PHOTO && data != null && data.data != null) {
                //fixme 本地视频
                val uri = data?.data
                //uri.getPath() 这个路径不行。靠不住。不要用。
                val cursor = activity.getContentResolver().query(uri!!, null, null, null, null)
                if (cursor != null) {
                    cursor!!.moveToFirst()
                    // 视频ID:MediaStore.Audio.Media._ID
                    //int videoId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    // 视频标题【没有后缀】：MediaStore.Audio.Media.TITLE
                    // 视频名称【文件名带后缀】：MediaStore.Audio.Media.DISPLAY_NAME，这个无论是相册里的视频，还是本地视频里的视频都能过获取。
                    //val fileName = cursor!!.getString(cursor!!.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))//亲测能够获取。
                    //Log.e("test","名称:\t"+fileName);
                    var videoPath: String? = null
                    try {
                        // 视频路径：MediaStore.Audio.Media.DATA。相册里面的视频可以获取。但是本地视频里的视频，路径无法获取。
                        videoPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                        if (videoPath == null) {
                            videoPath = getPhotoPath(data, activity)//这个能获取图片路径，自然也能获取视频路径。
                        }
                    } catch (e: Exception) {
                        videoPath = null
                        KLoggerUtils.e("本地视频路径获取失败:\t" + e.message, isLogEnable = true);
                    }
                    //Log.e("test", "视频路径:\t" + videoPath + "\t路径:\t" + getPhotoPath(activity, data) + "\t名称:\t" + fileName + "\turi路径:\t" + data.data.path)
                    videoPath?.let {
                        var file = File(videoPath)
                        cllback?.let {
                            if (file != null && file.exists()) {
                                it(file!!)
                            }
                        }
                    }
                }
            } else if (requestCode == DEFAULT_KEYS_VIDEO_CAPTURE && data != null && data.data != null) {
                //fixme 相机，视频录制
                cameraVideoFile?.let {
                    if (it.exists() && it.length() > 0) {
                        activity?.let {
                            if (!it.isFinishing) {
                                //fixme 发送系统广播，这样图片选择器就能够读取到该图片文件了。
                                it?.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(cameraVideoFile)))
                            }
                        }
                        cllback?.let {
                            it(cameraVideoFile!!)
                        }
                        //fixme 再发送一次广播（防止第一次没有收到）。多次发送不会有影响。
                        activity?.let {
                            if (!it.isFinishing) {
                                //fixme 发送系统广播，这样图片选择器就能够读取到该图片文件了。
                                it?.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(cameraVideoFile)))
                            }
                        }
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //处理相册中的图片旋转问题
    //每张图片都有自己的显示方向，ExifInterface
    //桌面是看不出来方向的，都是处理过的。你要直接放进studio里面。就可以看出方向了。

    /**
     * bitmap 位图
     * path 位图文件对于的路径
     */
    fun rotateBitmap(bitmap: Bitmap?, path: String): Bitmap? {
        var degree = readPictureDegree(path)
        return rotateBitmap(bitmap, degree)
    }

    fun rotateBitmap(bitmap: Bitmap?, input: InputStream): Bitmap? {
        var degree = readPictureDegree(input = input)
        return rotateBitmap(bitmap, degree)
    }

    //获取图片的旋转角度。（文件路径或流，随便传一个都行，SD卡的路径需要SD卡权限，无论是流还是文件。）
    fun readPictureDegree(path: String? = null, input: InputStream? = null): Int {
        var degree = 0
        try {
            var exifInterface: ExifInterface? = null
            path?.let {
                exifInterface = ExifInterface(path)
            }
            if (exifInterface != null && android.os.Build.VERSION.SDK_INT >= 24) {
                input?.let {
                    exifInterface = ExifInterface(input)//通过流取加载，最后还是会转换为File,所以还是需要SD卡权限。
                }
            }
            exifInterface?.let {
                val orientation = it.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL)
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
                }
            }
        } catch (e: IOException) {
            //Log.e("test", "获取图片角度异常:\t" + e.message)
        }
        return degree
    }

    //对位图进行指定角度旋转。
    private fun rotateBitmap(bitmap: Bitmap?, rotate: Int): Bitmap? {
        if (bitmap == null)
            return null
        if (rotate == 0) {
            return bitmap
        }
        val w = bitmap.width
        val h = bitmap.height

        // Setting post rotate to 90
        val mtx = Matrix()
        mtx.postRotate(rotate.toFloat())
        var bm = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true)
        bitmap.recycle()//释放掉
        return bm
    }

    //将Uri图片的内容复制到file上,成功返回Bitmap,错误返回null
    fun writeFile(cr: ContentResolver, file: File?, uri: Uri) {
        if (file == null) {
            return
        }
        var bitmap: Bitmap? = null//位图
        try {
            val fout = FileOutputStream(file)
            bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri), null, KAssetsUtils.getInstance().optionsARGB_8888)
            //保存位图原文件
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fout)// 80是压缩率，表示压缩20%;取值范围在0~100，代表质量
            try {
                fout.flush()
                fout.close()
            } catch (e: IOException) {
                e.printStackTrace()
                KLoggerUtils.e("相册图片异常0" + e.message, isLogEnable = true)
            }
            bitmap?.recycle()//释放
            bitmap = null
        } catch (e: FileNotFoundException) {
            KLoggerUtils.e("相册图片异常1" + e.message, isLogEnable = true)
        } catch (e: Exception) {
            KLoggerUtils.e("相册图片异常2" + e.message, isLogEnable = true)
        }
    }

}
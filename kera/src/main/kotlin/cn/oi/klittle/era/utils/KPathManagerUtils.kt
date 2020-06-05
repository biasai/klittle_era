package cn.oi.klittle.era.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import cn.oi.klittle.era.base.KBaseActivityManager
import cn.oi.klittle.era.base.KBaseApplication
import cn.oi.klittle.era.exception.KCatchException
import java.io.File

//                                fixme 路径返回案例。
//                                getCacheDir():	/data/user/0/com.example.myapplication/cache
//                                getCacheDirSecret():	/data/user/0/com.example.myapplication/files
//                                getFileLoadDownPath():	/storage/emulated/0/Android/data/com.example.myapplication/cache
//                                getCameraPath():	/storage/emulated/0/com.example.myapplication/CameraImage

/**
 * fixme 缓存，图片，相机照片。所有路径通用管理。注意：目录名称一旦定了，就不要轻易再改了。最好不要再改了。
 */
public object KPathManagerUtils {

    fun getContext(): Context {
        return KBaseApplication.getInstance()
    }

    fun getActivity(): Activity? {
        return KBaseActivityManager.getInstance().stackTopActivity
    }

    /**
     * fixme KCacheUtils 缓存类路径；不需要SD卡权限。
     */

    //fixme 1.缓存目录:/data/user/0/com.example.myapplication/cache
    //网络json数据缓存，KHttp.GetNetBitmap 和 KGldeUtils网络位图也是缓存到这个位置的。；即都是使用的：KCacheUtils.getCache()
    open fun getCacheDir(): File {
        //KCacheUtils.get(KApplication.getInstance().getFilesDir().getAbsoluteFile());//这个是之前用的。
        //return KBaseApplication.getInstance().cacheDir//fixme 获取本应用的缓存路径。
        var file = File(KBaseApplication.getInstance().cacheDir.absolutePath + "/data")
        try {
            if (file != null && !file.exists()) {
                file?.mkdirs() //fixme 目录不存在，则创建目录。
            }
            return file
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return KBaseApplication.getInstance().cacheDir//fixme 获取本应用的缓存路径。
            KLoggerUtils.e("KPathManagerUtils->getCacheDir()路径获取异常：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
        }
    }

    //fixme 2.缓存目录的路径,不需要SD卡权限
    open fun getCachePath(): String {
        return getCacheDir().absolutePath//fixme 在应用cache目录下，如：/data/user/0/com.应用包名/cache
    }

    //fixme 3.私有缓存目录:/data/user/0/com.example.myapplication/files
    open fun getCacheSecretDir(): File {
        return KBaseApplication.getInstance().getFilesDir()//fixme 获取本应用的私有缓存路径。
    }

    //fixme 4.私有缓存目录的路径,不需要SD卡权限
    open fun getCacheSecretPath(): String {
        return getCacheSecretDir().absolutePath//fixme 在应用files目录下。如：/data/user/0/com.应用包名/files
    }

    //fixme 4.1 图片缓存目录
    open fun getCacheImgDir(): File {
        var file = File(KBaseApplication.getInstance().cacheDir.absolutePath + "/img")//之前是IMG；fixme 目录还是统一小写比较好。不要再改了。
        try {
            if (file != null && !file.exists()) {
                file?.mkdirs() //fixme 目录不存在，则创建目录。
            }
            return file
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            KLoggerUtils.e("KPathManagerUtils->getCacheImgDir()路径获取异常：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
            return KBaseApplication.getInstance().cacheDir
        }
    }

    //fixme 4.2图片缓存目录
    open fun getCacheImgPath(): String {
        return getCacheImgDir().absolutePath//fixme 在应用files目录下。如：/data/user/0/com.应用包名/files
    }

    /**
     * fixme KPictureUtils相机拍摄保存路径;Environment需要SK卡权限。
     */

    //文件路径[需要file_paths.xml才能访问]
    //fixme 5.本应用，相机拍摄的图片会保存在该位置:/storage/emulated/0/com.example.myapplication/CameraImage
    open fun getCameraPath(): String {
        //defaultConfig {
        //targetSdkVersion 23//getExternalFilesDir才能正常访问，无需权限。但是如果是22及以下。就需要开启SD卡读取权限。
        //}
        //return getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        //return context.getFilesDir().getAbsoluteFile().getAbsolutePath() + "/cache"
        //return KCacheUtils.getCachePath() + "/img"
        var path: String? = null
        try {
            var context = KBaseApplication.getInstance()
            //fixme 相机拍照最好使用本地存储卡。其他应用也是应用，基本相机拍照都是使用的SD卡上的路径。
            //fixme 这样可以防止5.0没有使用FileProvider.getUriForFile();也能获取相机图片。不然Uri.fromFile()只能获取本地SD卡存储上的相机图片。
            //fixme 一般来说系统相机是读不出来我们保存在本地的图片的，系统相机一般只读出系统路径(DCIM)下的图片，其他位置的图片是不会读出来的。
            //fixme 相机图片系统路径：/storage/emulated/0/DCIM/Camera/
            path = Environment.getExternalStorageDirectory().absolutePath + "/" + context.packageName + "/CameraImage"//context.packageName获取的是应用app的包名。
            //return Environment.getExternalStorageDirectory().absolutePath + "/PictureSelector/CameraImage/" //第三方图片选择器的相机拍照图片路径。
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            KLoggerUtils.e("KPathManagerUtils->getCameraPath()路径获取异常：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
        }
        if (path == null) {
            path = getCachePath() + "/CameraImage"//fixme 如果异常，就使用本应用路径。最好不要使用本应用缓存。一个应用的缓存空间好像是有限的。
        }
        try {
            var file: File? = File(path)
            if (file != null && !file.exists()) {
                file?.mkdirs() //fixme 目录不存在，则创建目录。
            }
            file = null;
        } catch (e: java.lang.Exception) {
            KLoggerUtils.e("KPathManagerUtils->getCameraPath()异常2：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
        }
        return path
    }

    //fixme 6.视频录制路径
    open fun getAppVideoPath(): String {
        //return context.getFilesDir().getAbsoluteFile().getAbsolutePath() + "/video"
        //return KCacheUtils.getCachePath() + "/video"
        var path: String? = null
        try {
            var context = KBaseApplication.getInstance()
            //fixme 相机拍照最好使用本地存储卡。其他应用也是应用，基本相机拍照都是使用的SD卡上的路径。
            //fixme 这样可以防止5.0没有使用FileProvider.getUriForFile();也能获取相机图片。不然Uri.fromFile()只能获取本地SD卡存储上的相机图片。
            //fixme 一般来说系统相机是读不出来我们保存在本地的图片的，系统相机一般只读出系统路径(DCIM)下的图片，其他位置的图片是不会读出来的。
            //fixme 相机图片系统路径：/storage/emulated/0/DCIM/Camera/
            path = Environment.getExternalStorageDirectory().absolutePath + "/" + context.packageName + "/video"//context.packageName获取的是应用app的包名。
        } catch (e: Exception) {
            e.printStackTrace()
            KLoggerUtils.e("KPathManagerUtils->getAppVideoPath()路径获取异常：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
        }
        if (path == null) {
            path = getCachePath() + "/video"//fixme 如果异常，就使用本应用路径。最好不要使用本应用缓存。一个应用的缓存空间好像是有限的。
        }
        try {
            var file: File? = File(path)
            if (file != null && !file.exists()) {
                file?.mkdirs() //fixme 目录不存在，则创建目录。
            }
            file = null;
        } catch (e: java.lang.Exception) {
            KLoggerUtils.e("KPathManagerUtils->getAppVideoPath()异常2：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
        }
        return path
    }

    //fixme 7.文件裁剪路径
    open fun getAppCropPath(): String {
        //return context.getFilesDir().getAbsoluteFile().getAbsolutePath() + "/crop"
        //return KCacheUtils.getCachePath() + "/crop"
        var path: String? = null
        try {
            var context = KBaseApplication.getInstance()
            //fixme 相机拍照最好使用本地存储卡。其他应用也是应用，基本相机拍照都是使用的SD卡上的路径。
            //fixme 这样可以防止5.0没有使用FileProvider.getUriForFile();也能获取相机图片。不然Uri.fromFile()只能获取本地SD卡存储上的相机图片。
            //fixme 一般来说系统相机是读不出来我们保存在本地的图片的，系统相机一般只读出系统路径(DCIM)下的图片，其他位置的图片是不会读出来的。
            //fixme 相机图片系统路径：/storage/emulated/0/DCIM/Camera/
            path = Environment.getExternalStorageDirectory().absolutePath + "/" + context.packageName + "/crop"//context.packageName获取的是应用app的包名。
        } catch (e: Exception) {
            e.printStackTrace()
            KLoggerUtils.e("KPathManagerUtils->getAppCropPath()路径获取异常：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
        }
        if (path == null) {
            path = getCachePath() + "/crop"//fixme 如果异常，就使用本应用路径。最好不要使用本应用缓存。一个应用的缓存空间好像是有限的。
        }
        try {
            var file: File? = File(path)
            if (file != null && !file.exists()) {
                file?.mkdirs() //fixme 目录不存在，则创建目录。
            }
            file = null;
        } catch (e: java.lang.Exception) {
            KLoggerUtils.e("KPathManagerUtils->getAppCropPath()异常2：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
        }
        return path
    }

    //fixme 8.获取相册图片路径
    open fun getPhotoPath(data: Intent?, activtiy: Activity? = getActivity()): String? {
        if (activtiy == null) {
            return null
        }
        if (data == null) {
            return null
        }
        var photoPath: String? = null
        try {
            val uri = data.data
            // 获取相册图片路径
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            // 好像是android多媒体数据库的封装接口，具体的看Android文档
            var cursor: Cursor? = null
            if (Build.VERSION.SDK_INT >= 19) {//4.4版本
                //managedQuery()现在已经被getContentResolver().query()替代了，不过它们的参数都是一样的。效果也是一样的。
                cursor = activtiy.getContentResolver().query(uri!!, proj, null, null, null)
            } else {
                //低版本
                cursor = activtiy.managedQuery(uri, proj, null, null, null)
            }
            // 按我个人理解 这个是获得用户选择的图片的索引值
            val column_index = cursor!!
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            // 将光标移至开头 ，这个很重要，不小心很容易引起越界
            cursor.moveToFirst()
            // 最后根据索引值获取图片路径
            photoPath = cursor.getString(column_index)
            // bm = BitmapFactory.decodeFile(path);
        } catch (e: Exception) {
            // TODO: handle exception
            KLoggerUtils.e("KPathManagerUtils 相册图片路径获取失败" + KCatchException.getExceptionMsg(e), isLogEnable = true)
        }
        return photoPath
    }

    /**
     * fixme KPictureSelector图片选择器路径
     */

    //fixme 9.压缩路径，还是使用本应用的缓存目录。不要使用SD卡。防止被系统相册读取。（压缩文件建议还是不要被系统相册读取比较好）；
    //fixme 应用内的缓存目录，系统相册读取不到。系统相册只能读SD卡上的。应用删除之后，应用内的缓存目录数据都会自动删除掉。
    open fun getCompressPath(): String {
        var path = KCacheUtils.getCachePath() + "/compress"//fixme 压缩路径（本应用缓存路径）,相机拍照路径是SD存储卡。
        try {
            var file: File? = File(path)
            if (file != null && !file.exists()) {
                file?.mkdirs() //fixme 目录不存在，则创建目录。
            }
            file = null;
            return path
        } catch (e: Exception) {
            e.printStackTrace()
            KLoggerUtils.e("KPathManagerUtils->getCompressPath()下载文件目录创建失败：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
        }
        return path
    }

    /**
     * fixme KFileLoadUtils文件下载路径；不需要权限。
     */

    //fixme 10.获取本应用统一文件下载路径:/storage/emulated/0/Android/data/com.example.myapplication/cache
    open fun getFileLoadDownPath(): String {
        //var context: Context = KBaseApplication.getInstance()
        //this.cacheDir = context.getApplicationContext().getFilesDir().getAbsolutePath();//这个地址，文件无法分享。(内部位置无法分享出去),不需要权限
        //this.cacheDir = context.getApplicationContext().getFilesDir().getAbsolutePath();//这个地址，文件无法分享。(内部位置无法分享出去),不需要权限
        //this.cacheDir = context.applicationContext.externalCacheDir.absolutePath //这个位置，可以分享。（SD卡的东西可以分享出去）,不需要权限。推荐使用这个
        return KBaseApplication.getInstance().externalCacheDir.absolutePath //fixme 这个位置，可以分享。（SD卡的东西可以分享出去）,不需要权限。推荐使用这个
        //this.cacheDir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();//需要SD卡权限
    }

    //fixme 11.获取本应用，apk安装包下载路径
    open fun getApkLoadDownPath(): String {
        try {
            //之前apk下载目录就已经使用了 down；就不改了。就接着使用。兼容之前 目录。不要变了。
            var path: String = getFileLoadDownPath() + "/down" //fixme 统一下载路径; 与 KCacheUtils缓存目录不是同一个目录；互不影响。
            var file: File? = File(path)
            //KLoggerUtils.e("下载文件路径是否存在：\t"+file?.exists())
            if (file != null && !file.exists()) {
                file?.mkdirs() //fixme 目录不存在，则创建目录。
            }
            file = null;
            return path
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            KLoggerUtils.e("KPathManagerUtils->getApkLoadDownPath()下载文件目录创建失败：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
        }
        return getFileLoadDownPath()
    }

}
package cn.oi.klittle.era.utils

import cn.oi.klittle.era.activity.photo.manager.KPictureSelector
import cn.oi.klittle.era.base.KBaseApplication
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File

//                            fixme 调用案例，传入原图片地址即可。只能压缩图片；视频压缩不支持。
//                            KLubanUtils.compress(it.absolutePath, KPictureSelector.minimumCompressSize) {
//                                if (it != null) {
//                                    KLoggerUtils.e("压缩地址：\t"+it+"\t大小：\t"+File(it).length())
//                                }
//                            }

/**
 * 鲁班压缩；效果杠杠的。最解决微信的压缩方法。
 * fixme 目前只能压缩图片；视频压缩不支持。
 * fixme gif动态图压缩之后，就没有效果了。所以动态图也不支持压缩。
 */
object KLubanUtils {


    /**
     * 鲁班压缩
     * @param path 压缩文件路径
     * @param minimumCompressSize 单位KB,小于该值不压缩
     * @param callback 回调
     */
    fun compress(path: String?, minimumCompressSize: Int = 100, callback: (compreePath: String?) -> Unit) {
        if (path == null) {
            callback(null)
            return
        }
//        fixme 鲁班压缩，改变的是实际大小。不是宽和高。效果杠杠，和微信差不多。
//                方法	描述
//                load	传入原图
//                filter	设置开启压缩条件
//                ignoreBy	不压缩的阈值，单位为K
//                setFocusAlpha	设置是否保留透明通道
//                setTargetDir	缓存压缩图片路径
//                setCompressListener	压缩回调接口
//                setRenameListener	压缩前重命名接口
        Luban.with(KBaseApplication.getInstance())
                .load(path)
                .ignoreBy(minimumCompressSize)
                .filter {
                    //it参数是原始路径
                    //压缩过滤条件；true压缩，false不压缩。（不压缩会直接返回原始路径。）
                    //gif动态图压缩之后就不是gif了。动态图就无效了。所以不能压缩。
                    if (isPicture(it)) {
                        true //压缩，返回压缩后的完整路径
                    } else {
                        false//不压缩，返回原始路径
                    }
                    //!(TextUtils.isEmpty(it) || it.toLowerCase().endsWith(".gif"))//为空或者为gif不压缩
                }.setTargetDir(getTargetDir()).setFocusAlpha(true).setCompressListener(object : OnCompressListener {
                    override fun onSuccess(file: File?) {
                        //压缩成功
                        //KLoggerUtils.e("压缩成功：\t" + file)
                        callback(file?.absolutePath)
                    }

                    override fun onError(e: Throwable?) {
                        //压缩失败
                        //KLoggerUtils.e("压缩失败：\t" + e?.message)
                        callback(null)
                    }

                    override fun onStart() {
                        //开始压缩
                    }
                })
                .launch()//启动
    }

    /**
     * 压缩后的文件保存目录。是目录，不是文件。
     */
    fun getTargetDir(): String {
        return KPictureSelector.getCompressPath()
    }

    /**
     * 判断是否为图片
     * @param path 图片完整路径(包括后缀名)
     */
    fun isPicture(path: String?): Boolean {
        path?.trim()?.toLowerCase()?.let {
            if (it.length > 0) {
                //判断结尾endsWith()
                //.png 支持透明度
                //.jpg 全名应该是JPEG
                //.bmp位图
                if (it.endsWith(".png") || it.endsWith(".jpg") || it.endsWith(".jpeg") || it.endsWith(".bmp")) {
                    return true
                }
            }
        }
        return false
    }

}
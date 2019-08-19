package cn.oi.klittle.era.activity.photo.entity

import cn.oi.klittle.era.utils.KAssetsUtils
import java.io.Serializable

/**
 * 图片实体类信息
 */
open class KLocalMedia : Serializable {

    var key: String? = null//fixme 位图缓存的键值;用于图片释放

    //fixme 图片释放
    fun recyclerBitmap() {
        key?.let {
            KAssetsUtils.getInstance().recycleBitmap(it)
        }
    }

    //fixme 原图（视频或音频）路径(除了音频Audio没有图片，其他视频gif都有图片。)
    //fixme 路径就算是视频，Glide也能获取到第一帧的位图。Glide能够自动获取到位图。
    //fixme 但是如果是音频Glide就无法获取到位图了（因为音频里没有位图）
    var path: String? = null
    /**
     * 执行顺序是，先裁剪，后压缩。压缩始终是最后一步。
     */
    var cutPath: String? = null//裁剪路径
    var compressPath: String? = null//压缩路径;fixme 压缩功能已经实现（鲁班压缩）

    var isCut: Boolean = false//是否裁剪
    var isCompressed: Boolean = false//是否压缩;fixme 裁剪有点复杂，暂时先放一放。

    var isChecked: Boolean = false//是否选中
    var checkedNum = 0//当前选中数目

    var duration: Long = 0L//视频或音频时长，单位秒；(gif和图片没有时长，默认都是0)；
    var width: Int = 0//图片宽度
    var height: Int = 0//图片高度

    var pictureType: String? = "image/jpeg"//图片类型，默认是jpeg

    //判断是否为动态图片
    fun isGif(): Boolean {
        when (pictureType) {
            "image/GIF",
            "image/gif" -> {
                return true//gif动态图
            }
            else -> {
                return false
            }
        }
    }

    //判断是否为图片
    fun isPicture(): Boolean {
        when (pictureType) {
            "image/png",
            "image/PNG",
            "image/jpeg",
            "image/JPEG",
            "image/webp",
            "image/WEBP",
            "image/gif",
            "image/bmp",
            "image/GIF",
            "imagex-ms-bmp" -> {
                return true//图片
            }
        }
        return false
    }

    //判断是否为视频
    fun isVideo(): Boolean {
        when (pictureType) {
            "video/3gp",
            "video/3gpp",
            "video/3gpp2",
            "video/avi",
            "video/mp4",
            "video/quicktime",
            "video/x-msvideo",
            "video/x-matroska",
            "video/mpeg",
            "video/webm",
            "video/mp2ts" -> {
                return true//视频
            }
        }
        return false
    }

    /**
     * 是否为音频
     */
    fun isAudio(): Boolean {
        when (pictureType) {
            "audio/mpeg",
            "audio/x-ms-wma",
            "audio/x-wav",
            "audio/amr",
            "audio/wav",
            "audio/aac",
            "audio/mp4",
            "audio/quicktime",
            "audio/lamr",
            "audio/3gpp" -> {
                return true//音频
            }
        }
        return false
    }
}
package cn.oi.klittle.era.encry

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import cn.oi.klittle.era.utils.KAssetsUtils
import cn.oi.klittle.era.utils.KFileUtils
import cn.oi.klittle.era.utils.KStringUtils
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

/**
 * 这里Base64是安卓原生。但是不管是安卓原生还是第三方的。Base64都是一样的。（即是统一的。）
 * 实现了；字符串；字节数组；流；文件；位图与Base64的互转。fixme 亲测能够正确转换。
 * Created by 彭治铭 on 2019/4/20.
 */
object KBase64Utils {

    /**
     * 字符串转Base64
     */
    fun stringToBase64(string: String):String {
        return byteArrayToBase64(string.toByteArray())
    }


    /**
     * Base64转String字符串
     */
    fun base64ToString(base64: String): String {
        var bytes = Base64.decode(base64, Base64.DEFAULT)
        return KStringUtils.bytesToString(bytes)
    }

    /**
     * 字节数组转Base64【fixme base64能够正确互转； String与byte直接转换。太危险。】
     */
    fun byteArrayToBase64(bytes: ByteArray):String {
        var length = bytes.size
        return Base64.encodeToString(bytes, 0, length, Base64.DEFAULT)
    }

    /**
     * Base64转字节数组
     */
    fun base64ToByteArray(base64: String): ByteArray {
        var bytes = Base64.decode(base64, Base64.DEFAULT)// 将字符串转换为byte数组
        return bytes
    }

    /**
     * 流转Base64【fixme base64能够正确的转换流和文本。】
     */
    fun inputSteamToBase64(inputStream: InputStream): String? {
        return KFileUtils.getInstance().inputStreamToBase64(inputStream)
    }

    /**
     * Base64转流
     */
    fun base64ToInputStream(base64: String): InputStream {
        var bytes = base64ToByteArray(base64)
        return ByteArrayInputStream(bytes)
    }

    /**
     * 文件转Base64
     */
    fun fileToBase64(file: File): String? {
        return KFileUtils.getInstance().fileToBase64(file)
    }

    /**
     * Base64转文件
     * @param base64 加密的字符串
     * @param file 要转的文件
     * @return 返回的文件和传入的文件；是同一个。
     */
    fun base64ToFile(base64: String, file: File): File {
        return KFileUtils.getInstance().base64ToFile(base64, file)
    }

    /**
     * 位图转Base64【fixme 亲测；能够正确互转。】
     */
    fun bitmapToBase64(bitmap: Bitmap): String {
        return KFileUtils.getInstance().bitmapToBase64(bitmap)
    }

    /**
     * 位图转Base64
     * @param resID Resouce目录的位图
     */
    fun bitmapToBase64(resID: Int): String {
        return KFileUtils.getInstance().inputStreamToBase64(KAssetsUtils.getInstance().getInputStreamFromResouce(resID))
    }


    /**
     * Base64转位图
     */
    fun base64ToBitmap(base64: String): Bitmap {
        var bytes = base64ToByteArray(base64)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

}
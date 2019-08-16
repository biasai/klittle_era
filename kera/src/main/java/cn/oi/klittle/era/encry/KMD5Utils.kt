package cn.oi.klittle.era.encry

import java.security.MessageDigest

/**
 * MD5加密工具类
 * Created by 彭治铭 on 2019/4/20.
 */
object KMD5Utils {
    /**
     * 字符串转MD5
     * 返回的是32位长度的字符串。
     */
    fun stringToMD5(str: String): String {
        return KMd5.encrypt(str)
    }

    /**
     * 字节数组转MD5;
     * 返回的字符串长度由字节数组的长度决定。
     */
    fun byteArrayToMD5(byteArray: ByteArray): String {
        return KMd5.encrypt(byteArray)
    }

}
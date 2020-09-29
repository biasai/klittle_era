package cn.oi.klittle.era.utils

import java.lang.Exception
import java.util.regex.Pattern

/**
 * 正则表达式
 *
 * @author 彭治铭
 */
object KRegexUtils {
    /**
     * 是否包含中文
     *
     * @param str
     * @return
     */
    fun isContainChinese(str: String?): Boolean {
        if (str != null) {
            val p = Pattern.compile("[\u4e00-\u9fa5]")
            val m = p.matcher(str)
            if (m.find()) {
                return true
            }
        }
        return false
    }

    /**
     * 判断是否为手机号码
     *
     * @param mobiles
     * @return
     */
    fun isMobileNO(mobiles: String?): Boolean {
        if (mobiles==null){
            return false
        }
        if (mobiles.trim().length<=0){
            return false
        }
        //        Pattern p = Pattern.compile("^(13|15|18|16|17)\\d{9}$");
        //        Matcher m = p.matcher(mobiles);
        //        return m.matches();
        val pattern = Pattern.compile("1[0-9]{10}")
        val matcher = pattern.matcher(mobiles)
        return matcher.matches()
    }

    /**
     * 判断是否为固定电话号码
     *
     * @param phone
     * @return
     */
    fun isPhoneNo(phone: String?): Boolean {
        if (phone==null){
            return false
        }
        if (phone.trim().length<=0){
            return false
        }
        val p = Pattern.compile("[0]{1}[0-9]{2,3}-[0-9]{7,8}")
        val m = p.matcher(phone)
        return m.matches()
    }

    /**
     * 判断是否为邮箱地址
     *
     * @param email
     * @return
     */
    fun isEmail(email: String?): Boolean {
        if (email==null){
            return false
        }
        if (email.trim().length<=0){
            return false
        }
        val p = Pattern
                .compile("^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$")
        val m = p.matcher(email)
        return m.matches()
    }

    /**
     * 判断是否为合法IP
     *
     * @return the ip
     */
    fun isBoolIp(ipAddress: String?): Boolean {
        if (ipAddress==null){
            return false
        }
        if (ipAddress.trim().length<=0){
            return false
        }
        if (ipAddress.length < 7 || ipAddress.length > 15
                || "" == ipAddress) {
            return false
        }
        val ip = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}"
        val pattern = Pattern.compile(ip)
        val matcher = pattern.matcher(ipAddress)
        return matcher.matches()
    }

    /**
     * 是否为ip
     */
    fun isIP(str: String?): Boolean {
        if (str==null){
            return false
        }
        if (str.trim().length<=0){
            return false
        }
        val pattern = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])" +
                "\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\." +
                "((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\." +
                "((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b")
        return pattern.matcher(str).matches()
    }

    /**
     * 是否mac地址
     */
    fun isMAC(str: String?): Boolean {
        if (str==null){
            return false
        }
        if (str.trim().length<=0){
            return false
        }
        var str = str
        str = str.trim { it <= ' ' }
        if (str.length != 12) {
            return false
        }
        val chars = CharArray(12)
        str.toCharArray(chars, 0, 0, 12)
        for (i in chars.indices) {
            if (!(chars[i] >= '0' && chars[i] <= '9' || chars[i] >= 'A' && chars[i] <= 'F' || chars[i] >= 'a' && chars[i] <= 'f')) {
                return false
            }
        }
        return true
    }

    /**
     * 判断是否为身份证号
     *
     * @param idNum
     * @return
     */
    fun isIdCard(idNum: String?): Boolean {
        if (idNum==null){
            return false
        }
        if (idNum.trim().length<=0){
            return false
        }
        val p = Pattern
                .compile("(\\d{14}[0-9a-zA-Z])|(\\d{17}[0-9a-zA-Z])")
        val m = p.matcher(idNum)
        return m.matches()
    }

    /**
     * 根据身份证号，获取出生年月日
     *
     * @param idNum
     * @return
     */
    fun getBirth(idNum: String?): String {
        if (idNum==null){
            return ""
        }
        if (idNum.trim().length<=0){
            return ""
        }
        val birthDatePattern = Pattern
                .compile("\\d{6}(\\d{4})(\\d{2})(\\d{2}).*")// 身份证上的前6位以及出生年月日
        // 通过Pattern获得Matcher
        val birthDateMather = birthDatePattern.matcher(idNum)
        // 通过Matcher获得用户的出生年月日
        if (birthDateMather.find()) {
            val year = birthDateMather.group(1)
            val month = birthDateMather.group(2)
            val date = birthDateMather.group(3)
            // 输出用户的出生年月日
            return year + "年" + month + "月" + date + "日"//fixme 身份证就中国有，所以直接使用中文。
        }
        return ""
    }

    /**
     * 判断URL是否合理
     *
     * @param url
     * @return
     */
    fun isUrl(url: String?): Boolean {
        if (url==null){
            return false
        }
        if (url.trim().length<=0){
            return false
        }
        try {
            //fixme 之前的正则表达式，不可靠。改成以下方法去判断。虽然简单，但是绝对能够识别。亲测可行！
            var url = url.trim()
            if (url.length > 6) {
                var head = url.substring(0, 6)
                if ((head.contains("https:") || head.contains("http:") || head.contains("ftp:")) && (url.contains(":/"))) {
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 判断字符串是否为指定长度的数字
     *
     * @param str 字符串
     * @param length   长度
     * @return
     */
    fun isNumber(str: String?, length: Int): Boolean {
        if (str==null){
            return false
        }
        if (str.trim().length<=0){
            return false
        }
        val pattern = Pattern.compile("[0-9]*")
        if (pattern.matcher(str).matches()) {
            if (str.length == length) {
                return true
            }
        }
        return false
    }

    /**
     * 判断字符串是否为纯数字
     *
     * @param str 字符串
     * @return
     */
    fun isNumber(str: String?): Boolean {
        if (str==null){
            return false
        }
        if (str.trim().length<=0){
            return false
        }
        val pattern = Pattern.compile("[0-9]*")
        if (pattern.matcher(str).matches()) {
            return true
        }
        return false
    }

    //fixme isFloatOrNumber()测试结果  123:	true	1.23	true	0123:	true	0.123:	true	.123:	false	.:	false	.0:	false	.01:	false
    /**
     * fixme 判断字符串是否为小数或整数。即主要判断是否为小数Float类型。亲测可行。
     *
     * @param str 字符串
     * @return true 是小数或者整数；false不是。
     */
    fun isFloatOrNumber(str: String?): Boolean {
        if (str==null){
            return false
        }
        if (str.trim().length<=0){
            return false
        }
        //val pattern = Pattern.compile("-?[0-9]+.?[0-9]+")
        val pattern = Pattern.compile("[+-]?[0-9]+(\\.[0-9]+)?")
        if (pattern.matcher(str).matches()) {
            return true
        }
        return false
    }

    // 判断是否为纯字母
    fun isLetter(str: String?): Boolean {
        if (str==null){
            return false
        }
        if (str.trim().length<=0){
            return false
        }
        val pattern = Pattern.compile("^[A-Za-z]+")
        val isNum = pattern.matcher(str)
        return if (!isNum.matches()) {
            false
        } else true
    }

    // 判断是否为数字或（和）字母
    fun isNumberOrLetter(str: String?): Boolean {
        if (str==null){
            return false
        }
        if (str.trim().length<=0){
            return false
        }
        val pattern = Pattern.compile("^[A-Za-z0-9]+")
        val isNum = pattern.matcher(str)
        return if (!isNum.matches()) {
            false
        } else true
    }

    /**
     * 校验银行卡卡号
     *
     * @param cardId
     * @return
     */
    fun isBankCard(cardId: String?): Boolean {
        if (cardId==null){
            return false
        }
        if (cardId.trim().length<=0){
            return false
        }
        val bit = getBankCardCheckCode(cardId
                .substring(0, cardId.length - 1))
        return if (bit == 'N') {
            false
        } else cardId[cardId.length - 1] == bit
    }

    /**
     * 从不含校验位的银行卡卡号采用 Luhm 校验算法获得校验位
     *
     * @param nonCheckCodeCardId
     * @return
     */
    fun getBankCardCheckCode(nonCheckCodeCardId: String?): Char {
        if (nonCheckCodeCardId == null
                || nonCheckCodeCardId.trim { it <= ' ' }.length == 0
                || !nonCheckCodeCardId.matches("\\d+".toRegex())) {
            // 如果传的不是数据返回N
            return 'N'
        }
        val chs = nonCheckCodeCardId.trim { it <= ' ' }.toCharArray()
        var luhmSum = 0
        var i = chs.size - 1
        var j = 0
        while (i >= 0) {
            var k = chs[i] - '0'
            if (j % 2 == 0) {
                k *= 2
                k = k / 10 + k % 10
            }
            luhmSum += k
            i--
            j++
        }
        return if (luhmSum % 10 == 0) '0' else (10 - luhmSum % 10 + '0'.toInt()).toChar()
    }

}

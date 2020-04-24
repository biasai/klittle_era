package cn.oi.klittle.era.utils

import android.app.Activity
import android.content.Context
import android.text.ClipboardManager
import android.util.Log
import cn.oi.klittle.era.base.KBaseActivityManager
import cn.oi.klittle.era.base.KBaseApplication
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.lang.Exception
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

/**
 * 字符串处理工具类
 * 之所以写这个工具，是因为NumberFormat的四舍五入有问题。不可靠。所以自己写。
 */
object KStringUtils {

    private fun getContext(): Context {
        return KBaseApplication.getInstance()
    }

    private fun getActivity(): Activity? {
        return KBaseActivityManager.getInstance().stackTopActivity
    }

    /**
     * 获取颜色值（默认从Resources目录，从color文件中获取）
     */
    open fun getColor(id: Int): Int {
        return getContext().getResources().getColor(id)
    }

    //默认就从Res目录下读取
    open fun getString(id: Int, formatArgs: String? = null): String {
        if (formatArgs != null) {
            return getContext().resources.getString(id, formatArgs) as String
        }
        return getContext().resources.getString(id) as String
    }

    /**
     * 获取String文件里的字符串數組
     */
    open fun getStringArray(id: Int): Array<String> {
        return getContext().resources.getStringArray(id)
    }

    /**
     * fixme 字节数组转String字符串（亲测，能够正确转换）
     */
    fun bytesToString(data: ByteArray, length: Int? = null): String {
        var size = length
        if (size == null || size <= 0) {
            size = data.size
        }
        var str = String(data, 0, size).trim { it <= ' ' }
        //fixme str.toByteArray() ;字符串可以直接转换成字节数组。
        return str.trim()
    }

    fun bytesToString(data: MutableList<ByteArray>): String {
        var stringBuffer = StringBuffer()
        data?.forEach {
            stringBuffer.append(KStringUtils.bytesToString(it))
        }
        return stringBuffer.toString()
    }

    /**
     * 字节数组转流
     */
    fun bytesToInputStream(bytes: ByteArray): InputStream {
        return ByteArrayInputStream(bytes)
    }

    /**
     * 或者该字符第二次出现的位置。
     */
    fun indexOf2(str: String, char: Char): Int {
        var index = str.indexOf(char)//第一个
        if (index != -1 && str.lastIndex > index) {
            var str2 = str.substring(index + 1)
            var index2 = str2.indexOf(char)//第二个
            if (index2 != -1) {
                return index2 + index + 1
            }
        }
        return -1
    }

    //判断是否为空
    fun isEmpty(text: String?): Boolean {
        text?.let {
            if (it.trim().length > 0) {
                return false//不为空
            }
        }
        return true//空
    }

    //去除换行符(清楚有效)
    fun removeEnter(text: String?): String? {
        return text?.replace("\r", "")?.replace("\n", "")
    }

    //去除字符串双引号
    fun removeMarks(result: String): String {
        var result = result
        result = result.replace("\"", "")
        return result
    }

    /**
     * 给两头加上双引号
     * @param string
     * @return
     */
    fun addMarks(string: String): String {
        var result = removeMarks(string)//先去除最外层双引号，防止重复添加多余的双引号。
        return "\"" + result + "\""
    }

    //如果数字小于10，则前面自动补上一个0
    fun addZero(s: String): String {
        val n = Integer.valueOf(s)
        return if (n < 10) {
            "0$s"
        } else s
    }

    /**
     * @param num 随机数的个数
     * @return 返回随机数
     */
    fun getRandom(num: Int): String {
        return getRandom(num, (Math.random() * 100).toInt())
    }


    /**
     * @param num   随机数的个数[1返回一个随机数；2返回两个随机数；0返回空]
     * @param seeds 随机种子
     * @return
     */
    fun getRandom(num: Int, seeds: Int): String {
        var code = ""
        // 以时间为种子
        val rand = Random(System.currentTimeMillis() + seeds.toLong() + (Math.random() * 1000).toInt().toLong())
        // 生成真正随机数
        for (i in 0 until num) {// until 0和num相等或大于的时候不会执行；一定要比num小才行。until是不到的意思；即小于的时候才会执行。
            code = code + Math.abs(rand.nextInt() % 10)
        }
        return code
    }


    /**
     * 将字符串转换为整形。精确到小数后两位（其后全部割舍，不采用四舍五入），
     * 转换后的整数是原有的100倍（因为 微信的1对应0.01）
     *
     * @param s
     * @return
     */
    fun toWeixinInt(s: String): String {
        var b: Double? = java.lang.Double.parseDouble(s) * 100
        val format = DecimalFormat("0.00 ")
        b = java.lang.Double.parseDouble(format.format(b))
        val i = b.toInt()
        return i.toString() + ""
    }

    /**
     * 提供精确的加法运算
     *
     * @param v1
     *            被加数
     * @param v2
     *            加数
     * @return 两个参数的和
     */
    fun addDouble(v1: Double, v2: Double): Double {
        val b1 = BigDecimal(java.lang.Double.toString(v1))
        val b2 = BigDecimal(java.lang.Double.toString(v2))
        return b1.add(b2).toDouble()
    }

    /**
     * 提供精确的减法运算
     * @param v1
     * 被減数 如：3-2=1 其中3是被减数(前面那个)，2是减数。
     * @param v2
     * 減数
     * @return两个参数的差
     */
    fun subDouble(v1: Double, v2: Double): Double {
        val b1 = BigDecimal(java.lang.Double.toString(v1))
        val b2 = BigDecimal(java.lang.Double.toString(v2))
        return b1.subtract(b2).toDouble()
    }

    fun doubleString(d: Double, num: Int = 2): String? {
        return doubleString(d, num, true, false)
    }

    /**
     * @param d fixme double数据类型，数值太大，小数太长都会丢失精度。（精度丢失时会四舍五入。）
     * @param num 保留小数点后的位数
     * @param isKeepEnd0 是否保留小数点后末尾的0
     * @param isMicro 是否保留千位分隔符。（1如：789,012.12399即逗号）只有整数部分有千位分隔符，小数部分没有。
     */
    fun doubleString(d: Double, num: Int = 2, isKeepEnd0: Boolean = true, isMicroSymb: Boolean = false): String? {
        try {
            //DecimalFormatm默认四舍五如，银行家算法：四舍六入五考虑，五后非零就进一，五后为零看奇偶，五前为偶应舍去，五前为奇要进一
            //DecimalFormatm的四舍五入，这个五考虑规律有点复杂。规律还没完全摸清。
            val df = DecimalFormat()
            //这里是保留的小数位，如果末尾是0。会自动去除末尾的0。
            //fixme （如果数据太大[超过21.4亿左右]，依然会进行四舍五入，因为精度会丢失。）21.4亿以下，小数能完整显示。不会丢失。
            //fixme 之所以会进行四舍五入，就是因为精度丢失。无法完整显示所有小数。精度丢失，就会自动进行四舍五入。小数位数太长了，也会丢失精度。（即数值太大，小数太长都会丢失精度。）
            //fixme 多加几位。后面再截取，尽可能不进行四舍五入。
            df.maximumFractionDigits = num + 8
            if (isMicroSymb) {
                df.setGroupingUsed(true)//保留千位分隔符
            } else {
                df.setGroupingUsed(false)//去掉数值中的千位分隔符
            }
            df.setRoundingMode(RoundingMode.DOWN)
            //df.setGroupingUsed(true)//保留千位符号
            //fixme 原则是使用BigDecimal并且一定要用String来够造。这样精度更高。如果本身已经是double类型，就直接传double。不要在转string,因为double转String的时候也会丢失精度。
            //var bigDecimal = BigDecimal(java.lang.Double.toString(d))//BigDecimal才能完整的显示出来，Double不行。
            var bigDecimal = BigDecimal(d)//还是直接传double。double转String的时候可能会丢失精度。
            var str = df.format(bigDecimal).trim()
            //Log.e("test", "str:\t" + str + "\tnum:\t" + num + "\t" + bigDecimal.toString())
            //fixme 是否保留小数点末尾的0
            if (str.contains(".")) {
                //有小数点
                str.let {
                    var index = it.indexOf(".")//小数点下标
                    var front = it.substring(0, index)//小数点前面的数
                    var behind = it.substring(index + 1)//小数点后面的数（不包含小数点）
                    //fixme 去除多加的位数。
                    if (behind.length > num && behind.length > 1) {
                        behind = behind.substring(0, num)
                    }
                    if (isKeepEnd0) {
                        if (behind.length < num) {
                            //保留小点末尾的0
                            var length = num - behind.length
                            var zero = ""
                            for (i in 1..length) {
                                zero += "0"
                            }
                            behind += zero
                        }
                    }
                    if (num > 0) {
                        str = front + "." + behind
                    } else {
                        str = front
                    }
                }
            } else if (isKeepEnd0 && num > 0) {
                //没有小数点，末尾补上0
                var zero = ""
                for (i in 1..num) {
                    zero += "0"
                }
                str = str + "." + zero
            }
            return str
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun decimalString(str: Float, num: Int = 2): String? {
        return decimalString(str.toString(), num)
    }

    fun decimalString(str: String?, num: Int = 2): String? {
        return decimalString(str, num, true, false)
    }

    //人民币符号 ￥

    /**
     * @param str fixme 字符串,不会发生精度丢失问题。所以不会发生四舍五入。
     * @param num 小数点保留个数
     * @param isKeepEnd0 小数点后末尾如果是0,是否保留0。true保留0，false不保留。默认保留。
     * @param isKeepEndPoint 是否保留末尾的小数点，如：12. ->true 12. ->false 12
     * @param isMicro 是否保留千位分隔符。（1如：789,012.12399即逗号）只有整数部分有千位分隔符，小数部分没有。
     * @param microSymb 千位分隔符
     */
    fun decimalString(str: String?, num: Int = 2, isKeepEnd0: Boolean = true, isKeepEndPoint: Boolean = false, isMicroSymb: Boolean = false, microSymb: String = ","): String? {
        try {
            str?.trim()?.let {
                var front = ""//小数点前面的数
                var behind = ""//小数点后面数（不包含小数点）
                var ishasPoint: Boolean = false
                if (it.contains(".")) {
                    ishasPoint = true
                    //有小数点
                    var index = it.indexOf(".")//小数点下标
                    front = it.substring(0, index)//小数点前面的数
                    behind = it.substring(index + 1)//小数点后面数（不包含小数点）
                } else {
                    //没有小数点
                    front = it
                }
                //fixme 千位分隔符
                if (isMicroSymb) {
                    front = addMicroSymb(front, microSymb)!!
                }
                //fixme 去除多余的位数。
                if (behind.length > num && behind.length > 1) {
                    behind = behind.substring(0, num)
                }
                //fixme 末尾是否保留0
                if (isKeepEnd0 && num > 0) {
                    if (behind.length < num) {
                        //保留小点末尾的0
                        var length = num - behind.length
                        var zero = ""
                        for (i in 1..length) {
                            zero += "0"
                        }
                        behind += zero//fixme 末尾保留0
                    }
                } else {
                    behind = removeZero(behind)//fixme 不保留0
                }
                if (behind.length > 0 || (ishasPoint && isKeepEndPoint)) {
                    return front + "." + behind
                } else {
                    return front
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return str//fixme 为空的情况，返回空
    }

    /**
     * 去除小数点末尾的0 （包括末尾相连的0）。如：1.10 ->1.1 ; 1.00->1
     * s1 小数点前面的数，s2小数点后面的数
     */
    fun removeZero(s1: String, s2: String): String {
//        for (i in s2.length downTo 1) {
//            if (s2.substring(i - 1, i) != "0") {
//                return s1 + "." + s2.substring(0, i)
//            }
//        }
//        return s1
        var s22= removeZero(s2)
        s22?.trim()?.let {
            if (it.length>0){
                return s1 + "." +it
            }
        }
        return s1
    }

    /**
     * fixme 去除末尾的0 （包括末尾相连的0）。如：10 ->1 ; 100->1；000->""空;
     * s2 一般为小数点后面的数；不包含小数点哦。是小数点后面的字符。
     */
    fun removeZero(s2: String?): String {
        if (s2 == null) {
            return ""
        }
        try {
            for (i in s2.length downTo 1) {
                if (s2.substring(i - 1, i) != "0") {
                    return s2.substring(0, i)
                }
            }
            s2?.let {
                if (it.length > 0) {
                    var length = it.length - 1
                    //KLoggerUtils.e("第一个:\t"+s2.substring(0, 1)+"最后一个\t"+s2.substring(length, length+1)+"\tlength：\t"+length+"\ts2:\t"+s2)
                    //substring(0，1)包含参数一所在位置；不包含参数二。即包含下标0的字符，不包含下标1的字符。fixme 即不包含参数二所在的位置。
                    //fixme 防止 1.00 ；2.000；后面全是0的无法去除。
                    if (s2.substring(0, 1) == "0" && s2.substring(length, length+1) == "0") {
                        return ""
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return s2
    }

    //去除前面的0,如002->2
    fun removeFrontZero(str: String): String {
        if (str.contains("0") && str.length > 1) {
            var s2 = str
            for (i in 0..s2.lastIndex) {
                if (s2.length > 1 && s2.substring(0, 1).equals("0")) {
                    s2 = s2.substring(1, s2.length)
                } else {
                    break
                }
            }
            return s2
        }
        return str
    }

    //添加千位分隔符
    fun addMicroSymb(text: String?, microSymb: String = ","): String? {
        text?.let {
            var str = it
            var str2: String? = ""
            if (str.length > 3) {
                var str3 = str.reversed()//数据反转
                for (i in 0 until str3.length) {
                    if (str3[i] != null) {
                        str2 = str3[i].toString() + str2
                        if ((i + 1) % 3 == 0 && i != str.length - 1) {
                            str2 = microSymb + str2//fixme 加上符号2
                        }
                    }
                }
            } else {
                str2 = str
            }
            return str2
        }
        return text
    }

    /**
     * 计算大小,单位根据文件大小情况返回。返回结果带有单位。
     *
     * @param data 数据大小
     * @return
     */
    fun getDataSize(data: Double): String? {
        var data = data
        if (data < 1024 && data >= 0) {
            return doubleString(data, num = 2, isKeepEnd0 = false) + "B"
        } else if (data >= 1024 && data < 1024 * 1024) {
            data = data / 1024
            return doubleString(data, num = 2, isKeepEnd0 = false) + "KB"
        } else if (data >= 1024 * 1024) {
            data = data / 1024.0 / 1024.0
            return doubleString(data, num = 2, isKeepEnd0 = false) + "MB"
        }
        return null
    }

    /**
     * @param data 单位MB。返回的结果不会带有MB两个字。返回格式 "0.00"
     * @return
     */
    fun getDataSizeMB(data: Double): Double? {
        var format = DecimalFormat("0.00")// 格式
        var data = data
        data = data / 1024.0 / 1024.0
        data = java.lang.Double.parseDouble(format.format(data))
        return data
    }

    /**
     * 计算百分比
     *
     * @param curent     当前数值
     * @param total 总数值
     * @param keep  保留小数个数。0不保留小数，1小数一位，2小数2位。最大支持小数后四位
     * @return 返回百分比字符串。自带%百分比符合。
     */
    fun getPercent(curent: Long, total: Long, keep: Int = 2): String {
        var result = ""// 接受百分比的值
        val x_double = curent * 1.0
        val tempresult = curent / total.toDouble()
        // NumberFormat nf = NumberFormat.getPercentInstance(); 注释掉的也是一种方法
        // nf.setMinimumFractionDigits( 2 ); 保留到小数点后几位
        var df1: DecimalFormat? = null
        if (keep <= 0) {
            df1 = DecimalFormat("0%")
        } else if (keep == 1) {
            df1 = DecimalFormat("0.0%")
        } else if (keep == 2) {
            df1 = DecimalFormat("0.00%") // ##.00%
        } else if (keep == 3) {
            df1 = DecimalFormat("0.000%") // ##.00%
        } else if (keep >= 4) {
            df1 = DecimalFormat("0.0000%") // ##.00%
        }
        // 百分比格式，后面不足2位的用0补齐
        // result=nf.format(tempresult);
        result = df1!!.format(tempresult)
        return result
    }

    //文本复制
    fun copyText(copyText: String?, context: Context? = getContext()) {
        if (context != null) {
            context.apply {
                try {
                    if (copyText != null && copyText.length > 0) {
                        var cm: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setText(copyText)//复制指定文本
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    //将长度转换为时间
    var mFormatBuilder = StringBuilder();
    var mFormatter = Formatter(mFormatBuilder, Locale.getDefault());

    //将长度转换为时间(Video里面调用。)
    fun stringForTime(timeMs: Int): String {
        if (timeMs <= 0) {
            return "00:00"
        }
        var totalSeconds = timeMs / 1000;
        var seconds = totalSeconds % 60;
        var minutes = (totalSeconds / 60) % 60;
        var hours = totalSeconds / 3600;
        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }
}
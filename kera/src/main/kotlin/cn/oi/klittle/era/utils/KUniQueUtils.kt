package cn.oi.klittle.era.utils

import android.content.Context
import android.os.Build
import android.telephony.TelephonyManager
import cn.oi.klittle.era.base.KBaseApplication
import org.jetbrains.anko.db.NULL
import java.util.*

//                    fixme uuid基本使用
//                    var mostSigBits:Long=0
//                    var leastSigBits:Long=0
//                    //生成一个UUID;生成后的格式为：8-4-4-4-12 共32位;如：d9bc5194-431d-49ed-b3b6-4a1b72005934
//                    var uuid=UUID(mostSigBits,leastSigBits)//uuid 就等价于 uuid.toString()
//
//                    //生成一个指定的uuid,格式必须是 8-4-4-4-12;生成的uuid就是参数里的字符串(fixme 即字符串转UUID)
//                    var uuid2=UUID.fromString("d9bc5194-431d-49ed-b3b6-4a1b72005934")

/**
 * uuid的格式是：8-4-4-4-12 共32个字符，加上中间的横杆就是36位。如：d9bc5194-431d-49ed-b3b6-4a1b72005934
 * var uuid=UUID.randomUUID()//生成一个随机的uuid
 * var uuid2=UUID.fromString("d9bc5194-431d-49ed-b3b6-4a1b72005934")//生成一个指定的uuid,格式必须是 8-4-4-4-12
 * 其中 uuid 就等价于 uuid.toString()
 */
object KUniQueUtils {

    private fun getContext(): Context {
        return KBaseApplication.getInstance().applicationContext
    }

    /**
     * fixme UUID+设备号序列号 唯一识别码（不可变）;注意，这个需要读取手机状态的权限哦(不然异常)。
     * 返回结果如：00000000-11d5-776d-fcc1-ea550033c587 32个字符，加上中间的-就是36个。
     */
    fun getDeviceUuid(): String? {
        //需要读取手机状态的权限。这里仅仅做判断，不主动做动态申请。
        if (KPermissionUtils.requestPermissionsReadPhoneState(null)) {
            //TelephonyManager 这一步就需要权限。deviceId或androidId都需要权限。
            var tm = getContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager;
            var deviceId: String = tm?.getDeviceId();
            var tmSerial = "" + tm.getSimSerialNumber();
            var androidId = android.provider.Settings.Secure.getString(
                    getContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            val deviceUuid = UUID(androidId.hashCode().toLong(), deviceId.hashCode().toLong() shl 32 or tmSerial.hashCode().toLong())
            return deviceUuid.toString()
        }
        return null//没有权限，返回为空。自己手动去申请。
    }

    //不需要任何权限。最保险。
    //获得独一无二的Psuedo ID【如：ffffffff-80ac-a8f1-ffff-ffff8e4712be 一般为32位，加上中间的-就是36位。】,等价于设备号ID【设备唯一标识】
    fun getUniquePsuedoID(): String {
        var serial: String? = null
        val m_szDevIDShort = "Psuedo ID:" +
                Build.BOARD.length % 10 + Build.BRAND.length % 10 +
                Build.CPU_ABI.length % 10 + Build.DEVICE.length % 10 +
                Build.DISPLAY.length % 10 + Build.HOST.length % 10 +
                Build.ID.length % 10 + Build.MANUFACTURER.length % 10 +
                Build.MODEL.length % 10 + Build.PRODUCT.length % 10 +
                Build.TAGS.length % 10 + Build.TYPE.length % 10 +
                Build.USER.length % 10 //13 位
        try {
            serial = Build::class.java.getField("SERIAL").get(null).toString()
            //API>=9 使用serial号
            return UUID(m_szDevIDShort.hashCode().toLong(), serial.hashCode().toLong()).toString()
        } catch (exception: Exception) {
            //serial需要一个初始化
            serial = "serial" // 随便一个初始化
        }
        //使用硬件信息拼凑出来的15位号码
        return UUID(m_szDevIDShort.hashCode().toLong(), serial!!.hashCode().toLong()).toString()
    }

    var letteres = mutableListOf<String>("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z")

    //生成随机字符【格式(包含数字和小写字符)：7gc0y3】
    //确保每次生成都尽可能不一样（随机个数count越大越好。）
    fun getSecurityRandom(count: Int): String {
        if (count > 0) {
            var code = ""
            // 生成正随机数
            for (i in 0 until count) {
                // 以时间为种子(每次循环都重新实例化一个Random，增大随机概率。)
                var rand = Random((Math.random() * 100000000).toLong() + System.currentTimeMillis())
                var c = Math.abs(rand.nextInt() % 36).toString()
                c = letteres.get(c.toInt())
                code = code + c
            }
            return code
        } else {
            KLoggerUtils.e("TEST", "随机个数少于0")
            return "0"
        }
    }

    /**
     * 将32位的字符串转UUID格式：8-4-4-4-12 共32个字符；加上中间的斜杠共36个字符。
     * @param str 必须是32位长度的字符串
     *
     */
    fun toUUID(str: String): UUID? {
        try {
            str?.let {
                if (it.length == 32) {
                    //fixme 必须是32位长度
                    //substring(开始下标，结束下标)；截取的结果是包含开始下标；但不包含结束下标。
                    var one_8 = it.substring(0, 8)
                    var two_4 = it.substring(8, 12)
                    var three_4 = it.substring(12, 16)
                    var four_4 = it.substring(16, 20)
                    var five_12 = it.substring(20, 32)
                    return UUID.fromString(one_8 + "-" + two_4 + "-" + three_4 + "-" + four_4 + "-" + five_12)
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            KLoggerUtils.e("32位字符串转UUID格式异常:\t" + e.message)
        }
        return null
    }
}
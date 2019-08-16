package cn.oi.klittle.era.utils

import cn.oi.klittle.era.R
import cn.oi.klittle.era.comm.KLunar
import java.text.SimpleDateFormat
import java.util.*

/**
 * 时间工具管理类
 * 完整的时间格式 ：yyyy-MM-dd HH:mm:ss：SSS; ,h是12小时制，H是24小时制,大S是毫秒
 * System.currentTimeMillis() 是当前系统时间毫秒数。
 */
object KCalendarUtils {

    fun getDate(long: Long): Date {
        return Date(long)
    }

    fun getDate(long: String): Date {
        try {
            return getDate(long.toLong())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return Date()
    }

    /**
     * Date 转 Calendar
     */
    fun getCalendar(date: Date): Calendar {
        var calendar = Calendar.getInstance()
        calendar.time = date
        return calendar
    }

    /**
     * 获取自定义时间的Calendar对象
     * @param time 时间
     * @param format 时间格式，必须对于上。
     */
    fun getCalendar(time: String? = null, format: String = "yyyy-MM-dd"): Calendar {
        if (!KStringUtils.isEmpty(time) && (time!!.trim().length == format.trim().length)) {
            var sdf = SimpleDateFormat(format)
            //字符串转时间
            var date = sdf.parse(time)
            var calendar = GregorianCalendar()
            calendar.time = date
            return calendar//返回自定义时间
        }
        return Calendar.getInstance()//fixme 返回当前时间,每次都会返回一个新的对象。
    }

    /**
     * 时间加减（正数是加，负数是减）
     * @param calendar 时间
     * @param year 年
     * @param month 月
     * @param day 日
     * @param hour 小时（24小时制）
     * @param minute 分钟
     * @param second 秒
     * @param millsecond 毫秒
     */
    fun getAddCalendar(calendar: Calendar, year: Int? = null, month: Int? = null, day: Int? = null, hour: Int? = null, minute: Int? = null, second: Int? = null, millsecond: Int? = null): Calendar {
        year?.let {
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + year)// 年份加减
        }
        month?.let {
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + month)// 月份加减
        }
        day?.let {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + day)// 日期加减
        }
        hour?.let {
            //小时相加，不分12小时制和24小时制。统一按24小时制相加的。
            //以下两种方式都可以。都是按24小时相加减的。
            //calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + hour)// 小时相加，这个虽然是12小时制的。但是相加都是按24小时制。
            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + hour)// 24小时制相加
        }
        minute?.let {
            calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + minute)// 分钟相加
        }
        second?.let {
            calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + second)// 秒相加
        }
        millsecond?.let {
            calendar.set(Calendar.MILLISECOND, calendar.get(Calendar.MILLISECOND) + millsecond)// 毫秒相加
        }
        return calendar
    }

    /**
     * 获取时间
     * @param long 时间毫秒
     */
    fun getTime(long: Long, format: String = "yyyy-MM-dd"): String {
        return getTime(getCalendar(getDate(long)), format)
    }

    /**
     * 获取时间
     */
    fun getTime(date: Date, format: String = "yyyy-MM-dd"): String {
        return getTime(getCalendar(date), format)
    }

    /**
     * 获取时间
     */
    fun getTime(calendar: Calendar, format: String = "yyyy-MM-dd"): String {
        var sdf = SimpleDateFormat(format)
        return sdf.format(calendar.time)
    }

    /**
     * 获取当前时间
     * @param format 时间格式
     */
    fun getCurrentTime(format: String = "yyyy-MM-dd"): String {
        var sdf = SimpleDateFormat(format)
        return sdf.format(Date())
    }

    /**
     * 将时间按指定格式输出
     * @param time 时间
     * @param oldFormat 旧时间格式
     * @param newFormat 新时间格式
     */
    fun getNewFormatTime(time: String, oldFormat: String, newFormat: String): String {
        var calendar = getCalendar(time, oldFormat)
        var sdf = SimpleDateFormat(newFormat)
        return sdf.format(calendar.time)//不能是calendar对象，必须是Date对象。calendar.time就是date对象。
    }

    /**
     * 获取年
     */
    fun getYear(calendar: Calendar = Calendar.getInstance()): Int {
        return calendar.get(Calendar.YEAR)
    }

    /**
     * 获得这个年的第几天(即今天是这一年的第几天)
     */
    fun getYearOfDay(calendar: Calendar = Calendar.getInstance()): Int {
        return calendar.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * 获取月
     */
    fun getMonth(calendar: Calendar = Calendar.getInstance()): Int {
        return calendar.get(Calendar.MONTH) + 1//fixme 注意 是从0开始的。0表示一月。11表示十二月。所以为了正常显示；要加上一
    }

    /**
     * 获取月份的天数,即该月份共有多少天
     */
    fun getMonthOfDay(calendar: Calendar = Calendar.getInstance()): Int {
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    /**
     * 获取月份的天数,即该月份共有多少天
     * @param time 时间
     * @param format 时间格式
     */
    fun getMonthOfDay(time: String, format: String = "yyyy-MM"): Int {
        return getMonthOfDay(getCalendar(time, format))
    }

    /**
     * 获取时间--日期，即几号
     */
    fun getDate(calendar: Calendar = Calendar.getInstance()): Int {
        return calendar.get(Calendar.DATE)
    }

    /**
     * 获取时间--小时（24小时制）
     */
    fun getHourOfDay(calendar: Calendar = Calendar.getInstance()): Int {
        return calendar.get(Calendar.HOUR_OF_DAY)//24小时制
        //return calendar.get(Calendar.HOUR)//12小时制
    }

    /**
     * 获取时间--小时（12小时制）
     */
    fun getHour(calendar: Calendar = Calendar.getInstance()): Int {
        return calendar.get(Calendar.HOUR)//12小时制
    }

    /**
     * 获取时间--分钟
     */
    fun getMinute(calendar: Calendar = Calendar.getInstance()): Int {
        return calendar.get(Calendar.MINUTE)
    }

    /**
     * 获取时间--秒
     */
    fun getSecond(calendar: Calendar = Calendar.getInstance()): Int {
        return calendar.get(Calendar.SECOND)
    }

    /**
     * 获取时间--毫秒
     */
    fun getMilliSecond(calendar: Calendar = Calendar.getInstance()): Int {
        return calendar.get(Calendar.MILLISECOND)
    }

    /**
     * 获取时间--毫秒（是时间的毫秒总计）
     */
    fun getTimeMillis(calendar: Calendar = Calendar.getInstance()): Long {
        //System.currentTimeMillis()//当前时间的毫秒总数
        return calendar.timeInMillis//这个是Calendar毫秒总数
    }

    private val week = KStringUtils.getStringArray(R.array.KWeek)
    /**
     * 获取时间--星期
     *
     * @param calendar 如果为null,则已当前时间为标准
     * @return
     */
    fun getWeek(calendar: Calendar = Calendar.getInstance()): String {
        return week[calendar.get(Calendar.DAY_OF_WEEK) - 1]// 国外是从星期天为第一天。中国是星期一为第一天，所以减去1
    }

    /**
     * 获取时间--星期
     * @param time 时间
     * @param format 时间格式
     */
    fun getWeek(time: String, format: String = "yyyy-MM-dd"): String {
        return getWeek(getCalendar(time, format))
    }

    /**
     * 判断是否为润年；true是润年，fasle不是润年
     * @param year 年
     */
    fun isLeapYear(year: Int): Boolean {
        //val days: Int//某年(year)的天数
        if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {//闰年的判断规则
            //days = 366
            return true
        } else {
            //days = 365
            return false
        }
    }

    fun isLeapYear(calendar: Calendar): Boolean {
        return isLeapYear(getYear(calendar))
    }

    fun isLeapYear(time: String, format: String = "yyyy-MM-dd"): Boolean {
        return isLeapYear(getCalendar(time, format))
    }

    /**
     * 时间比较
     * @param calendar 时间一
     * @param calendar2 时间二（默认当前时间）
     * true（endCalendar 大于 startCalendar）；fixme true 第二个时间大（为了distance（）方法统一，规定第二个时间大为true）
     * fasle（endCalendar 小于 startCalendar ）fixme false 第二个时间小。(等于都返回false,只有大才返回true)
     */
    fun compare(startCalendar: Calendar, endCalendar: Calendar = Calendar.getInstance()): Boolean {
        //return endCalendar.before(startCalendar)// 当前时间是否在传入时间之前，true，则大于当前时间，false,小于当前时间。
        return startCalendar.before(endCalendar)
    }

    /**
     * 两个日期之间的毫秒数之差
     * @param startCalendar 开始日期
     * @param endCalendar 结束日期（结束日期-开始日期，所以结束日期应该要大，这样算出的结果才为正。）
     */
    fun distanceOfTimeMillis(startCalendar: Calendar, endCalendar: Calendar): Long {
        var from: Long = 0
        var to: Long = 0
        try {
            from = startCalendar.getTime().getTime()
            to = endCalendar.getTime().getTime()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (to - from) //结束时间 减去 开始时间
    }

    /**
     * 两个日期之间的秒数之差
     */
    fun distanceOfSeconds(startCalendar: Calendar, endCalendar: Calendar): Long {
        return distanceOfTimeMillis(startCalendar, endCalendar) / 1000
    }

    /**
     * 两个日期之间的分钟差
     */
    fun distanceOfMinutes(startCalendar: Calendar, endCalendar: Calendar): Int {
        return (distanceOfSeconds(startCalendar, endCalendar) / 60).toInt()
    }

    /**
     * 两个日期之间的小时之差
     */
    fun distanceOfHours(startCalendar: Calendar, endCalendar: Calendar): Int {
        return (distanceOfMinutes(startCalendar, endCalendar) / 60)
    }

    /**
     * 两个日期之间的天数之差。
     * @param startCalendar 开始日期
     * @param endCalendar 结束日期（结束日期-开始日期，所以结束日期应该要大，这样算出的结果才为正。）
     */
    fun distanceOfDay(startCalendar: Calendar, endCalendar: Calendar): Int {
        return (distanceOfHours(startCalendar, endCalendar) / 24)
    }


    /**
     * 获取年龄
     */
    fun getAge(birth: Calendar): Int {
        val cal = Calendar.getInstance()
        // 当前时间
        val yearNow = cal.get(Calendar.YEAR)
        val monthNow = cal.get(Calendar.MONTH) + 1// 注意此处，如果不加1的话计算结果是错误的
        val dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH)

        // 出生时间
        val yearBirth = birth.get(Calendar.YEAR)
        val monthBirth = birth.get(Calendar.MONTH) + 1
        val dayOfMonthBirth = birth.get(Calendar.DAY_OF_MONTH)

        var age = yearNow - yearBirth

        if (monthNow == monthBirth) {
            if (dayOfMonthNow < dayOfMonthBirth) {
                age--
            }
        } else if (monthNow < monthBirth) {
            age--
        }
        return age
    }

    /**
     * 获取年龄
     * @param birthTime 出生时间
     * @param format 时间格式
     */
    fun getAge(birthTime: String, format: String = "yyyy-MM-dd"): Int {
        return getAge(getCalendar(birthTime, format))
    }

    //fixme 星做是按阳历算的。
    var constellationArray = KStringUtils.getStringArray(R.array.KConstellation)
    private val constellationEdgeDay = intArrayOf(20, 19, 21, 21, 21, 22, 23, 23, 23, 23, 22, 22)

    /**
     * 获取星座
     */
    fun getConstellation(calendar: Calendar): String {
        var month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        if (day < constellationEdgeDay[month]) {
            month = month - 1
        }
        return if (month >= 0) {
            constellationArray[month]
        } else constellationArray[11]
        // default to return 魔羯
    }

    /**
     *获取星座
     * @param time 时间
     * @param format 时间格式
     */
    fun getConstellation(time: String, format: String = "yyyy-MM-dd"): String {
        return getConstellation(getCalendar(time, format))
    }

    //fixme 生肖按英历算
    val zodiacArray = KStringUtils.getStringArray(R.array.KZodiacArray)

    /**
     * 获取生肖（传阳历即可，会自动转换成阴历）
     *
     * @return
     */
    fun getZodica(calendar: Calendar): String {
        val lunar = KLunar(calendar)//将阳历转换为阴历
        return zodiacArray[lunar.animalsYearPosition()]
    }

    /**
     * 获取生肖（传阳历即可，会自动转换成阴历）
     * @param time 时间
     * @param format 时间格式
     */
    fun getZodica(time: String, format: String = "yyyy-MM-dd"): String {
        return getZodica(getCalendar(time, format))
    }

    /**
     * 与当前时间作比较，仿QQ聊天记录时间显示。
     *
     * @param startDate 日期。不能大于当前日期。
     * @return
     */
    fun getRelativeCurrentTime(startCalendar: Calendar): String {
        try {
            val endDate = Calendar.getInstance()// 当前日期
            val distence = distanceOfDay(startCalendar, endDate)
            if (distence < 7) {
                if (distence <= 1) {
                    if (distence == 0) {
                        return getTime(startCalendar, "HH:mm")//显示今天
                    } else {
                        var time = getTime(startCalendar, " HH:mm")
                        time = KStringUtils.getString(R.string.KYesterday) + time
                        return time//显示昨天
                    }
                } else {
                    return getWeek(startCalendar) + getTime(startCalendar, " HH:mm")//显示星期
                }
            } else {
                return getTime(startCalendar, "MM-dd HH:mm")//日差大于七，显示月份日期
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

}
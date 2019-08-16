package cn.oi.klittle.era.comm

import cn.oi.klittle.era.utils.KCalendarUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * format 时间格式，如：var kdate=KDate("yyyy-MM-dd HH:mm:ss SSS")
 * 三个大写的S是毫秒。小写的s是秒
 * 大写的HH是24小时制，小写的h是12小时制
 *
 * date 获取自定义时间，如："2018-01-03-23-59-00",必须和参数format的时间格式对应上。如果为空或与格式对不上。默认获取当前时间
 * fixme 时间统一按阳历计算的。
 *
 */
open class KDate(var format: String = "yyyy-MM-dd HH:mm", var date: String? = null) {
    var calendar: Calendar
    var lunar: KLunar//fixme 农历时间,直接调用 kDate.lunar 就返回农历时间，如1992年十二月廿十。默认就调用了toString()方法。

    init {
        if (date == null) {
            calendar = Calendar.getInstance()//获取当前时间，一旦获取。Calendar就锁定了。即时间就停止在那一刻了，不会再变了。即每个Calendar的时间都是固定不变的。是锁定的。
        } else {
            calendar = KCalendarUtils.getCalendar(date, format)//自定义时间
        }
        date = SimpleDateFormat(format).format(calendar.time)//获取当前格式的时间
        lunar = KLunar(calendar)//fixme 阳历转阴历
    }

    //获取指定格式的时间
    open fun date(format: String = this.format): String {
        return SimpleDateFormat(format).format(calendar.time)
    }

    //年
    var YEAR: Int = calendar?.get(Calendar.YEAR)
    //月
    var MONTH: Int = calendar.get(Calendar.MONTH) + 1//Calendar月份从0开始。所以加1
    //日
    var DAY: Int = calendar.get(Calendar.DATE)
    //小时，12小时制度
    var HOUR: Int = calendar.get(Calendar.HOUR)
    //小时，24小时制度
    var HOUR_OF_DAY: Int = calendar.get(Calendar.HOUR_OF_DAY)
    //分钟
    var MINUTE: Int = calendar.get(Calendar.MINUTE)
    //秒
    var SECOND: Int = calendar.get(Calendar.SECOND)
    //毫秒
    var MILLISECOND: Int = calendar.get(Calendar.MILLISECOND)
    //当前calendar时间的总毫秒数。System.currentTimeMillis()返回的是当前时间的总毫秒数。
    var TimeInMillis: Long = calendar.timeInMillis

    //fixme 星期
    var DAY_OF_WEEK: String = ""
        get() {
            return getWeek()
        }
    //星期数组，可以自定义。
    //如：kDate.week= arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
    var week = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")

    private fun getWeek(): String {
        return week[calendar.get(Calendar.DAY_OF_WEEK) - 1]// 国外是从星期天为第一天。中国是星期一为第一天，所以减去1
    }

    //fixme 年龄（当前的calendar与当前的时间做比较）
    var AGE: Int = KCalendarUtils.getAge(calendar)

    //星座名称数组，也可以自定义。
    var constellationArray = arrayOf("水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座")
    private val constellationEdgeDay = intArrayOf(20, 19, 21, 21, 21, 22, 23, 23, 23, 23, 22, 22)

    //fixme 星座,按阳历算。
    var CONSTELLATION: String = ""
        get() {
            return getConstellation()
        }

    /**
     * 根据日期Calendar获取星座
     *
     * @param time
     * @return
     */
    private fun getConstellation(): String {
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

    //生肖数组，名称也可以自定义
    var zodiacArray = arrayOf("子鼠", "丑牛", "寅虎", "卯兔", "辰龙", "巳蛇", "午马", "未羊", "申猴", "酉鸡", "戌狗", "亥猪")
    //fixme 生肖,属相是按阴历算
    var ZODIAC: String = ""
        get() {
            return get2Zodica()
        }

    /**
     * 根据日期Calendar获取生肖
     *
     * @return
     */
    private fun get2Zodica(): String {
        return zodiacArray[lunar.animalsYearPosition()]
    }

    //fixme 是否为润年(润年2月为29天),按阳历计算。
    var isLeap = false
        get() {
            if (YEAR % 4 == 0 && YEAR % 100 != 0) {
                return true
            } else if (YEAR % 400 == 0) {
                return true
            }
            return false
        }

    /**
     * 求两个日期的分钟差
     * compare 比较日期，默认与当前时间进行比较。
     * 返回：大于0，表示当前calendar日期大于比较日期
     */
    open fun DISTANCE_MINUTE(compare: Calendar = Calendar.getInstance()): Long {
        var from: Long = calendar.time.time
        var to: Long = compare.time.time
        return ((from - to) / (1000 * 60))//当前calendar时间 减去 比较时间。
    }

    open fun DISTANCE_MINUTE(compare: KDate): Long {
        return DISTANCE_MINUTE(compare.calendar)
    }

    /**
     * 求两个日期的小时之差
     * compare 比较日期，默认与当前时间进行比较。
     * 返回：大于0，表示当前calendar日期大于比较日期
     */
    open fun DISTANCE_HOUR(compare: Calendar = Calendar.getInstance()): Long {
        var from: Long = calendar.time.time
        var to: Long = compare.time.time
        return ((from - to) / (1000 * 60 * 60))//当前calendar时间 减去 比较时间。
    }

    open fun DISTANCE_HOUR(compare: KDate): Long {
        return DISTANCE_HOUR(compare.calendar)
    }

    /**
     * 求两个日期的天数之差
     * compare 比较日期，默认与当前时间进行比较。
     * 返回：大于0，表示当前calendar日期大于比较日期
     */
    open fun DISTANCE_DAY(compare: Calendar = Calendar.getInstance()): Long {
        var from: Long = calendar.time.time
        var to: Long = compare.time.time
        return ((from - to) / (1000 * 60 * 60 * 24))//当前calendar时间 减去 比较时间。
    }

    open fun DISTANCE_DAY(compare: KDate): Long {
        return DISTANCE_DAY(compare.calendar)
    }

}
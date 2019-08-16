package cn.oi.klittle.era.utils

import java.util.Locale

import android.content.Context
import android.os.Build
import cn.oi.klittle.era.base.KBaseApplication

//            Locale使用案例：
//            var locale = Locale.ENGLISH//英语
//            locale = Locale.CHINA//简体中文
//            locale = Locale.TRADITIONAL_CHINESE//繁体中文（中国台湾）
//            /**
//             * fixme Locale.ENGLISH;Locale.CHINA;Locale.TRADITIONAL_CHINESE这些常量有限，不包含所有语言。
//             * fixme 可以使用构造函数可以创建所有语言
//             * fixme Locale(String language) language 小写的两位英文字母;如：en,zh
//             * fixme Locale(String language,String country)country 大写的两个字母，如 CN,HK,MY
//             * fixme values-zh-rCN 如：language对应 zh ; country对应 CN;( 前面的r省略)
//             */
//
//            //英语 values-en
//            locale = Locale("en")
//            //简体中文 values-zh-rCN fixme country 其中的r省略，不需要。但是文件夹values-zh-rCN里的r不能省。亲测省了报错。
//            locale = Locale("zh", "CN")
//            //繁体中文（中国台湾）values-zh-rTW
//            locale = Locale("zh", "CN")
//            //繁体中文（中国香港）values-zh-rHK
//            locale = Locale("zh", "HK")
//            //马来西亚 values-en-rMY
//            locale = Locale("en", "MY")

/**
 * 语言切换工具类。
 * fixme 注意：只对指定的上下文有效。即Application的上下文和Activity的上下文(context),是不同的。不同的Activity之间的上下文也不同。
 * fixme 即语言配置时，要注意对应的上下文。所以最好在Application和BaseActivity中配置。这样全部都有效。
 */
object KLanguageUtil {

    fun getContext(): Context {
        return KBaseApplication.getInstance().applicationContext
    }

    /**
     * 获取获取系统语言。（即是系统的语言，不是应用的。）
     */
    fun getDefaultLocale(): Locale {
        return Locale.getDefault()//亲测有效。
    }

    /**
     * 获取当前资源配置的语言(即当前应用的语言)
     */
    fun getCurrentLocale(context: Context? = getContext()): Locale? {
        var locale: Locale? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context?.getResources()?.getConfiguration()?.getLocales()?.get(0)//7.0版本以上
        } else {
            locale = context?.getResources()?.getConfiguration()?.locale//7.0之前被弃用了。
        }
        //locale.language//语言
        //locale.country//国家
        return locale
    }

    /**
     * 判断两个语言Locale是否相同
     */
    fun equals(locale: Locale?, locale2: Locale?): Boolean {
        if (locale != null && locale2 != null) {
            if (locale.language.equals(locale2.language) && locale.country.equals(locale2.country)) {
                return true
            }
        } else if (locale == null && locale2 == null) {
            return true
        }
        return false
    }

    /**
     * 设置语言；只对当前自己的app有影响，不影响其他app
     * @param locale  Locale.ENGLISH英语；Locale.SIMPLIFIED_CHINESE简体中文；根据需求设置各种语言
     */
    fun setLanguage(locale: Locale?, context: Context? = getContext()) {
        if (locale == null) {
            return
        }
        /**
         * 以下两张设置方法都行
         */
        var locale = locale
        context?.let {
            // 第一种设置方法
            val res = context.resources
            val config = res.configuration
            config.locale = locale
            val dm = res.displayMetrics
            res.updateConfiguration(config, dm)

            // 第二种设置方法(一样，设置一种就行了。)
//            var config2 = Configuration();
//            config2.locale = locale;
//            it.getResources().updateConfiguration(config2,
//                    it.getResources().getDisplayMetrics());
        }
    }

    /**
     * @param language 语言；小写的两位英文字母；如：en,zh
     */
    fun setLanguage(language: String, context: Context? = getContext()) {
        setLanguage(Locale(language), context)
    }

    /**
     * @param language 语言；小写的两位英文字母；如：en,zh
     * @param country 国家；大写的两位英文字母；如;CN,HK,MY;其中的r省略，不需要。
     *
     * fixme 如：设置马来西亚语言 values-en-rMY（可以参考对照码查看） language对应en;country对应MY
     *
     */
    fun setLanguage(language: String, country: String, context: Context? = getContext()) {
        setLanguage(Locale(language, country), context)
    }


    /**
     * 设置简体中文 values-zh-rCN
     */
    fun setLanguage_zh(context: Context? = getContext()) {
        KLanguageUtil.setLanguage(Locale.SIMPLIFIED_CHINESE, context)
    }

    /**
     * 设置繁体中文（中国台湾）values-zh-rTW
     */
    fun setLanguage_zh_rTW(context: Context? = getContext()) {
        KLanguageUtil.setLanguage(Locale.TRADITIONAL_CHINESE, context)
    }

    /**
     * 设置英文 values-en
     */
    fun setLanguage_en(context: Context? = getContext()) {
        KLanguageUtil.setLanguage(Locale.ENGLISH, context)
    }

    /**
     * 设置法语 values-fr
     */
    fun setLanguage_fr(context: Context? = getContext()) {
        KLanguageUtil.setLanguage(Locale.FRANCE, context)
    }

    /**
     * 设置德语 values-de
     */
    fun setLanguage_de(context: Context? = getContext()) {
        KLanguageUtil.setLanguage(Locale.GERMANY, context)
    }

    /**
     * 设置日语 values-ja
     */
    fun setLanguage_ja(context: Context? = getContext()) {
        KLanguageUtil.setLanguage(Locale.JAPANESE, context)
    }

    /**
     * 设置马来西亚语言 values-en-rMY fixme 亲测有效。
     */
    fun setLanguage_en_rMY(context: Context? = getContext()) {
        //fixme country省略前面的r,不需要。
        KLanguageUtil.setLanguage("en", "MY", context)
    }

    /**
     * 设置俄语 values-ru-rRU
     */
    fun setLanguage_ru_rRU(context: Context? = getContext()) {
        KLanguageUtil.setLanguage("ru", "RU", context)
    }

}

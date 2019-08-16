package cn.oi.klittle.era.sdk.youdao

import cn.oi.klittle.era.https.ko.KOhttp
import cn.oi.klittle.era.sdk.youdao.entity.KYouDaoResultEntity
import com.google.gson.Gson

class KYouDaoHttp {
    var isJava: Boolean = false//fixme 是否运行在java端。true是；false在android端运行。
    /**
     * 默认的转换方式  中文简体  to  英文
     *
     * @param src
     * @return
     */
    fun translate(src: String, callback: CallBack?): String {
        return translateInner(src, "zh-CHS", "en", callback)
    }

    /**
     * 可以指定由什么语言 转换到什么语言
     *
     * @param src  要翻译的字符串
     * @param from 从什么语言
     * @param to   翻译到什么语言；fixme 这个语言编码和android国际语言编码基本一致。
     * @return
     */
    fun translate(src: String, from: String, to: String, callback: CallBack?): String {
        return translateInner(src, from, to, callback)
    }


    private fun translateInner(src: String, from: String, to: String, callback: CallBack?): String {
        var params = KYouDaoTranslate.getParams(src, from, to)
        /** 处理结果  */
        var result = ""
        try {
            KOhttp.url(KYouDaoTranslate.YOUDAO_URL).apply {
                contentType = null//fixme 和.net一样。必须设置为空。才行。
                addParam(params)
                isFirstCacle(true)
                isCacle(true)
                isUrlUniqueParams(false)//不要所有参数作为标志
                urlUniqueParams(src?.trim() + from?.toLowerCase().trim() + to?.toLowerCase().trim())//挑几个固定参数作为唯一标志
                isShowLoad(false)
                isShowParams(false)
                isJava(this@KYouDaoHttp.isJava)
                onIsCacle {
                    if (it.contains("translation")) {
                        true
                    } else {
                        false
                    }
                }
            }.post<String>() {
                result = it
                callback?.let {
                    try {
                        Gson()?.apply {
                            fromJson(result, KYouDaoResultEntity::class.java).translation?.let {
                                if (it.size > 0) {
                                    result = it[0]
                                }
                            }
                        }
//                        fixme JSONObject在java mian()方法中报错。
//                        JSONObject(result)?.let {
//                            //查询正确时，translation一定存在
//                            if (it.has("translation")){
//                                result=it.getJSONArray("translation")[0].toString()
//                            }
//                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    it.onResponse(result)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            System.out.println("异常：\t" + e.message)
        }

        return result
    }

    interface CallBack {
        fun onResponse(result: String)
    }

}
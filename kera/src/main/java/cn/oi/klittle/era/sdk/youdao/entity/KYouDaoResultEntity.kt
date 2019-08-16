package cn.oi.klittle.era.sdk.youdao.entity

import cn.oi.klittle.era.base.KBaseEntity

/**
 * 有道http请求；返回实体类数据
 */
open class KYouDaoResultEntity : KBaseEntity() {
    open var errorCode: String? = null//错误编码
    open var translation: ArrayList<String>? = null//翻译文本
}
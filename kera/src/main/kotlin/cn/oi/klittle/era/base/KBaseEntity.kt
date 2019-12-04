package cn.oi.klittle.era.base

import cn.oi.klittle.era.gson.KGsonJavaUtils
import cn.oi.klittle.era.gson.KGsonUtils
import java.io.Serializable

/**
 * fixme JAVA程序设计中数据访问层的数据模型一般以Bean结尾，表示它是一个JavaBean，
 * fixme 而.NET中更多的是使用Model作为后缀，
 * fixme 也有人以Entity作为后缀(个人必须喜欢使用Entity)，这也就解释了为什么任何一个项目中都免不了看到这三个单词。
 * 基本实体类，在滚轮弹框里面有使用到。
 *
 * fixme Serializable 可序列化；继承之后才能保存到本地。
 */
open class KBaseEntity : Serializable {
    open var id: String? = null
    open var name: String? = null

    open var showName: String? = null//fixme 滚轮弹窗里显示的信息(如果为空，会去读name这个属性，优先读取showName字段)

    constructor(id: String? = null, name: String? = null, showName: String? = null) {
        this.id = id
        this.name = name
        this.showName = showName
    }

    constructor() {}

    //fixme 转换成json数据（第三方）;(父类和子类所有属性都有效。都能够转行。)
    //fixme 反射只能过反射当前的属性；无法反射父类的属性。但是这个toJson()方法；当前类和父类的属性都能够读到，都能转化。
    open fun toJson(): String {
        //fixme "/0name" 还是 "/0name"；保持原样不会变。不会加上多余的反斜杠！！
        return KGsonJavaUtils.toJson(this)//在java main()方法里也能直接运行。
    }

    //fixme 斜杠/和反斜杠\;java只识别斜杠/和双反斜杠\\；不识别单个反斜杠\ (单个反斜杠当作转义字符处理)

    //fixme 转成JSON数据（原生）；（现在也以及支持父类属性。能够获取父类和子类的所有属性，亲测有效。）
    open fun toJSONObject(): String {
        //fixme "/0name" 会变成 "\/0name" ;即斜杠/前面会加上反斜杠\;  / -> \/
        return KGsonUtils.parseAnyToJSON(this).toString()//只能在安卓设备上运行；java mian()运行不了。
    }

}
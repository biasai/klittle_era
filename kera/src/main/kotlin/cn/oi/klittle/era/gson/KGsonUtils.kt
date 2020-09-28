package cn.oi.klittle.era.gson

import android.util.Log
import cn.oi.klittle.era.exception.KCatchException
import cn.oi.klittle.era.gson.type.KTypeReference
import cn.oi.klittle.era.utils.KLoggerUtils
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Method
import java.util.*
import kotlin.collections.HashMap


/**
 * Kotlin json解析类。data class类型，参数必须有默认值才行。即data类型有默认参数，就等价空构造函数。
 * fixme 必须有空构造函数（用于实例化，内部类无法直接实例化） 和 属性有set方法即可。
 * fixme 遍历类 成员属性【只对当前类有效，父类无效，即只获取本类的属性】
 * fixme 现在以及支持父类属性了。能够获取当前类及其所有父类的属性。 KFieldsUtils.getAllFields()亲测有效。能够获取。
 * 单例模式。直接通过类名即可调用里面的方法。都是静态的。
 * Created by 彭治铭 on 2018/4/24.
 *
 *
 *
 * fixme 注意一哈
 * 1485.0 JSONObject 会自动去除0；变成 1485 ；如果是 1485.1 或 s1234.0 任然保存原数据；只会去除纯数字的末尾.0
 * 3171.000 也会变成 3171 ；
 * 3171.0010 会变成 3171.001 [fixme 即会去除纯数字类型的末尾0]
 *
 * fixme 在KBaseEntity类里面有JSON的转换案例。
 * fixme Character 类型，目前好像会乱码。Character目前转换还不太支持，建议不要使用Character类型。
 *
 * fixme 泛型新增，和支持String,Boolean，Int,Float等基本类型（不支持Character字符类型）
 * fixme 支持 Map 类型。亲测有效（使用谷歌Gson工具类转换的）。
 * fixme 支持 ArrayList<> 类型（ArrayList()可以实例化）；不支持MutableList<>类型(不能直接实例化)；
 *
 * fixme 即：目前不支持 Character 和 MutableList；其他基本都支持。
 *
 * fixme 兼容json字段首字母大小写问题。
 */
object KGsonUtils {

    //fixme 斜杠/和反斜杠\;java只识别斜杠/和双反斜杠\\；不识别单个反斜杠\ (单个反斜杠当作转义字符处理)

    /**
     * fixme 将字符串转成JSONObject字符串。(斜杠前面会加上反斜杠); /Date(1557278096000)/ 会变成 \/Date(1557278096000)\/
     * fixme 注意： KGsonJavaUtils.toJson(this); 这个第三方库，(斜杠前面不会加上反斜杠) 。 /Date(1557278096000)/不会变，不会加上反斜杠。
     *
     * fixme 注意作用就是给斜杠/前面加上反斜杠\;（即 / ->变 \/ ）；已经集成到KBaseEntity实体类里面去了。
     */
    fun toJSONObject(str: String?): String? {
        str?.let {
            try {
                var JSONObject = JSONObject(it)
                return JSONObject.toString()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return str
    }

//    var datas: AppHttpResult<ArrayList<WareHousingInfo>?>//fixme 调用案例。
//    datas=KGsonUtils.parseJSONToAny(json)
    /**
     * fixme JSON数据转Any对象;注意这个方法调用的时候可能不会提示哦。需要手动去敲出来。
     * @param json json数据(字符串类型)
     * @param field 字段；根据字段解析数据(如果该字段不存在，就返回原有数据)
     */
    //直接传入泛型即可。支持所有类型。泛型可以无限嵌套。[一个类里面只支持一个泛型，不支持同时有两个泛型。如：Model<T,T2>]
    //[之前就是json格式不正确，才转换失败。][只要json格式正确，都能转换。亲测！]
    //兼容ArrayList和MutableList（两个都可以。亲测可行;他们类型都是 class java.util.ArrayList）
    //注意：嵌套具体实体类[普通实体类不支持数组]
    //即：嵌套实体类时，实体类不能为数组（ArrayList）。如果包含数组。必须通过泛型进行指明。即：不能包含数组，数组必须通过泛型指明！
    //fixme 现在已经能够兼容识别数组里的实体类了。数组里的实体类不用通过泛型了。也可以识别了。
    inline fun <reified T> parseJSONToAny(json: Any?, vararg field: String): T {
        //Log.e("test", "json:\t" + json)
        var kjson = parseStringToJSON(json?.toString(), *field)//解析指定字段里的json数据。
        //Log.e("test", "json2:\t" + kjson)
        var className = T::class.java.toString().trim()
        //KLoggerUtils.e("className:\t"+className)
        if (className.contains("Map") && (className.equals("class java.util.HashMap")
                        || className.equals("class java.util.LinkedHashMap") || className.equals("interface java.util.Map"))) {
            try {
                var type = object : TypeToken<T?>() {}.type
                var map: T = KGsonJavaUtils.fromJson(kjson, type)//fixme JSON转Map也借助Gson谷歌第三方库。
                return map
            } catch (e: java.lang.Exception) {
                KLoggerUtils.e("parseJSONToAny(),JSON转map异常：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
            }
        }
        var typeReference = object : KTypeReference<T>() {}
        return getObject(kjson, typeReference.classes, 0) as T
    }


    /**
     * fixme 实体类转JSON
     * @param t 可以是任意类型；支持数组(ArrayList或MutableList)；能够正确识别
     * @return 如果参数是实体类；返回的是JSONObject类型；如果是数组；则返回的是JSONArray
     */
    //实体类转JSON数据;不需要使用泛型。(能够识别具体的类型;因为传入的是对象。对象能够完整识别。泛型不能完整识别。)
    //也能够识别ArrayList里面的类型。也能够识别泛型哦（多个泛型也能够识别），传对象比传泛型好用。
    //MutableList和ArrayList两个都可以（他们两个class类型都是一样的）。ArrayList可以转化成MutableList [arrayList.toMutableList()]
    //因为是反射；所以还是无法识别父类的属性。父类属性读不到。
    //返回类型是Any（可以正确的转换成JSONObject或JSONArray类型）;不要返回String类型；可能会出现很多斜杠\\。
    //如果参数传递的是实体类；返回的就是JSONObject类型；如果是ArrayList数组；返回的就JSONArray
    fun parseAnyToJSON(t: Any?): Any {
        if (t == null) {
            return ""
        }
        val jsonObject = JSONObject()
        try {
            //KLoggerUtils.e("CLASS:\t" + t::class.java)
            var className = t::class.java.toString().trim()
            if (className.contains("class java.lang.")) {//fixme=============================
                if (className.equals("class java.lang.String")
                        || className.equals("class java.lang.Object")) {
                    try {
                        return JSONObject(t.toString())//如果String是json格式就能顺利转换；如果不是就转换不了。
                    } catch (e: Exception) {
                        //KLoggerUtils.e("String转JSONObject异常：\t" + e.toString())
                        jsonObject.put(autoField, t.toString())//fixme 兼容String类型能够转成JSONObject
                        return jsonObject//fixme 千万不要 jsonObject.toString()；这样会多出很多斜杆\\的。不要返回string类型；就返回jsonObject类型
                    }
                } else if (className.equals("class java.lang.Integer")
                        || className.equals("class java.lang.Long")
                        || className.equals("class java.lang.Float")
                        || className.equals("class java.lang.Double")
                        || className.equals("class java.lang.Byte")
                        || className.equals("class java.lang.Boolean")
                        || className.equals("class java.lang.Short")
                        || className.equals("class java.lang.Character")) {
                    jsonObject.put(autoField, t.toString())//fixme 兼容基本类型能够转成JSONObject
                    return jsonObject//fixme 千万不要 jsonObject.toString()；这样会多出很多斜杆\\的。不要返回string类型；就返回jsonObject类型
                }
            } else if (className.contains("Map")) {
                if (className.equals("class java.util.HashMap")
                        || className.equals("class java.util.LinkedHashMap") || className.equals("interface java.util.Map")) {
                    var json = KGsonJavaUtils.toJson(t)//fixme map转json，借助谷歌GSON工具类。
                    return JSONObject(json)
                }
            }
            if (className.equals("class java.util.ArrayList") && (t is ArrayList<*>)) {
                //fixme 数组
                var jsonAny = JSONArray()
                t.forEach {
                    jsonAny.put(parseAnyToJSON(it))
                }
                return jsonAny
            }
            //class java.util.HashMap

            //遍历类 成员属性【只对当前类有效，父类无效，即只获取本类的属性】
            //val fields = t::class.java.getDeclaredFields()
            val fields = KFieldsUtils.getAllFields(t::class.java)//fixme 获取当前类，及其父类所有的属性
            for (i in fields.indices) {
                // 获取属性的名字
                var name = fields[i].getName()
                if (name.trim({ it <= ' ' }) == "\$change" || name.trim({ it <= ' ' }) == "serialVersionUID") {
                    continue
                }
                val jName = name
                // 将属性的首字符大写，方便构造get，set方法
                name = name.substring(0, 1).toUpperCase() + name.substring(1)
                // 获取属性的类型
                val type = fields[i].getGenericType().toString()
                //Log.e("test","type:\t"+type+"\tjName:\t"+jName);
                var m: Method? = null
                try {
                    if (type == "boolean" || type.equals("class java.lang.Boolean")) {
                        var name2 = name
                        try {
                            if (name2.contains("Is")) {
                                var index = name2.indexOf("Is")
                                if (index == 0 && name2.length > 2) {
                                    name2 = name2.substring(2)
                                    name2 = name2.substring(0, 1).toUpperCase() + name2.substring(1)
                                    m = t::class.java.getMethod("is$name2")
                                    val obj = m!!.invoke(t) ?: continue
                                }
                            }
                        } catch (e: java.lang.Exception) {
                            KLoggerUtils.e("布尔类型转json异常：\t" + KCatchException.getExceptionMsg(e))
                        }
                        //Log.e("test","name：\t"+name+"\tname2:\t"+name2)
                        if (m == null) {
                            // 调用getter方法获取属性值
                            m = t::class.java.getMethod("get$name2")
                            val obj = m!!.invoke(t) ?: continue
                        }
                    } else {
                        // 调用getter方法获取属性值
                        m = t::class.java.getMethod("get$name")
                        if (m != null) {
                            val obj = m!!.invoke(t) ?: continue
                        }
                    }
                    //Log.e("test","obj:\t"+obj);
                } catch (e: Exception) {
                    KLoggerUtils.e(msg = "get()异常:\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
                }
                // 如果type是类类型，则前面包含"class "，后面跟类名
                if (type.equals("class java.lang.String")) {
                    val value = m!!.invoke(t) as String
                    jsonObject.put(jName, value)
                } else if (type.contains("class java.lang.")) {//fixme 基本类型对象
                    if (type.equals("class java.lang.Double")) {
                        m?.invoke(t)?.let {
                            if (it is Double) {
                                jsonObject.put(jName, it as Double)
                            }
                        }
                    } else if (type.equals("class java.lang.Object")) {
                        val value = m!!.invoke(t)
                        jsonObject.put(jName, value)
                    } else if (type.equals("class java.lang.Float")) {
                        m?.invoke(t)?.let {
                            if (it is Float) {
                                jsonObject.put(jName, it as Float)
                            }
                        }
                    } else if (type.equals("class java.lang.Integer")) {
                        m?.invoke(t)?.let {
                            if (it is Int) {
                                jsonObject.put(jName, it as Int)
                            }
                        }
                    } else if (type.equals("class java.lang.Boolean")) {
                        m?.invoke(t)?.let {
                            if (it is Boolean) {
                                jsonObject.put(jName, it as Boolean)
                            }
                        }
                    } else if (type.equals("class java.lang.Long")) {
                        m?.invoke(t)?.let {
                            if (it is Long) {
                                jsonObject.put(jName, it as Long)
                            }
                        }
                    } else if (type.equals("class java.lang.Short")) {
                        m?.invoke(t)?.let {
                            if (it is Short) {
                                jsonObject.put(jName, it as Short)
                            }
                        }
                    } else if (type.equals("class java.lang.Byte")) {
                        m?.invoke(t)?.let {
                            if (it is Byte) {
                                jsonObject.put(jName, it as Byte)
                            }
                        }
                    } else if (type.equals("class java.lang.Character")) {
                        val value = m?.invoke(t) as Character
                        jsonObject.put(jName, value)
                    }
                }//fixme 基本类型
                else if (type == "double") {
                    m?.invoke(t)?.let {
                        if (it is Double) {
                            jsonObject.put(jName, it as Double)
                        }
                    }
                } else if (type == "float") {
                    m?.invoke(t)?.let {
                        if (it is Float) {
                            jsonObject.put(jName, it as Float)
                        }
                    }
                } else if (type == "int") {
                    m?.invoke(t)?.let {
                        if (it is Int) {
                            jsonObject.put(jName, it as Int)
                        }
                    }
                } else if (type == "boolean") {
                    m?.invoke(t)?.let {
                        if (it is Boolean) {
                            jsonObject.put(jName, it as Boolean)
                        }
                    }
                } else if (type == "long") {
                    m?.invoke(t)?.let {
                        if (it is Long) {
                            jsonObject.put(jName, it as Long)
                        }
                    }
                } else if (type == "short") {
                    m?.invoke(t)?.let {
                        if (it is Short) {
                            jsonObject.put(jName, it as Short)
                        }
                    }
                } else if (type == "byte") {
                    m?.invoke(t)?.let {
                        if (it is Byte) {
                            jsonObject.put(jName, it as Byte)
                        }
                    }
                } else if (type == "char") {
                    val value = m?.invoke(t) as Character
                    jsonObject.put(jName, value)
                } else {
                    var value = m?.invoke(t)
                    if (value != null && value != "null" && value != "") {
                        //KLoggerUtils.e("type:\t" + type + "\t" + (value is ArrayList<*>))
                        if (type.contains("java.util.ArrayList") && (value is ArrayList<*>)) {
                            //fixme 数组
                            var jsonAny = JSONArray()
                            value.forEach {
                                jsonAny.put(parseAnyToJSON(it))
                            }
                            jsonObject.put(jName, jsonAny)//
                        } else {
                            jsonObject.put(jName, parseAnyToJSON(value))//fixme 普通实体类,兼容Map
                        }
                    }
                }
            }
        } catch (e: Exception) {
            KLoggerUtils.e(msg = "KGsonUtils实体类转JSON数据异常:\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
        }
        return jsonObject
    }

    /**
     *fixme String类型转JSON格式
     * @param result stirng数据
     * @param field 字段；根据字段解析数据(如果该字段不存在，就返回原有数据)
     */
    //数据解析(解析之后，可以显示中文。)
    //解析出来的数据最后是json格式的数据（如果不是json格式，就返回为空）
    fun parseStringToJSON(result: String?, vararg field: String): String? {
        var response = getString(result, *field)
        //判断是否为合法JSON格式
        try {
            response?.let {
                if (it.contains("{") || it.contains("}") || it.contains("[") || it.contains("]")) {
                    JSONObject(response)//fixme 判断是否为JSONObject对象
                } else {
                    response = null//fixme 不包含{和[肯定不是json数据。
                }
            }
        } catch (e: Exception) {
            try {
                JSONArray(response)//fixme 判断是否为 JSONArray对象
            } catch (e: Exception) {
                try {
                    JSONObject()//fixme 直接实例化一个对象；在Android端不会报错；但是在Java main()方法中直接运行就会报错。在main()方法中就不要置空了。
                    response = null //fixme 即不能转JSONObject也不能转JSONArray。不合法则制空,防止异常报错。
                } catch (e: java.lang.Exception) {
                    //e.printStackTrace()
                }

            }
            //e.printStackTrace()//fixme 会在控制台里面打印出来。
        }
        return response
    }

    //根据字段解析数据(如果该字段不存在，就返回原有数据);fixme 解析出来的数据，最后是String类型的数据。
    private fun getString(result: String?, vararg field: String): String? {
        if (result == null) {
            return null
        }
        var response = result
        if (field.size > 0) {
            //解析字段里的json数据
            for (i in field) {
                i?.let {
                    try {
                        var json = JSONObject(response)
                        if (json.has(it)) {
                            response = json.getString(it)
                        }
                    } catch (e: Exception) {
                        KLoggerUtils.e(msg = "KGsonUtils.parseJson解析异常：\t" + e.toString())
                    }
                }
            }
        }
        return response
    }

    private val autoField: String = "zynxbd_autoStringField_kotlin"//自定义String字段；因为纯String文本无法转JSONObject[有键值对];fixme 注意，这个字段规定了，以后就不能再变了。不然不兼容。千万不要变。

    //FIXME JSON数据转实体类
    fun getObject(json: String?, classes: List<Class<*>>, index: Int): Any {
        var clazz = classes[index]//当前类型
        var className = clazz.name
        //Log.e("test", "当前类型:\t" + clazz + "\t" + clazz.name + "\t长度:\t" + classes.size + "\t" + classes + "\t數據：\t" + json)
        if (className.contains("java.lang.")) {//fixme=======================================
            try {
                if (className.equals("java.lang.String")) {
                    json?.let {
                        if (it.contains(autoField)) {
                            try {
                                var jsonObject = JSONObject(it)
                                if (jsonObject.has(autoField)) {//双重判断保险
                                    return jsonObject.getString(autoField)//fixme 兼容自定义属性字段
                                }
                            } catch (e: Exception) {
                            }
                        }
                        return it//fixme string类型就返回整个JSON数据本身。
                    }
                    return ""
                } else if (className.equals("java.lang.Boolean")) {
                    json?.let {
                        if (it.contains(autoField)) {
                            try {
                                var jsonObject = JSONObject(it)
                                if (jsonObject.has(autoField)) {//双重判断保险
                                    return jsonObject.getString(autoField).toBoolean()//fixme 兼容自定义属性字段
                                }
                            } catch (e: Exception) {
                            }
                        }
                        if (it.toString().trim().toLowerCase().equals("true")) {
                            return it.toBoolean()
                        }
                    }
                    return false
                } else if (className.equals("java.lang.Integer")) {
                    json?.let {
                        if (it.contains(autoField)) {
                            try {
                                var jsonObject = JSONObject(it)
                                if (jsonObject.has(autoField)) {//双重判断保险
                                    return jsonObject.getString(autoField).toDouble().toInt()//fixme 兼容自定义属性字段
                                }
                            } catch (e: Exception) {
                            }
                        }
                        return it.toDouble().toInt()
                    }
                    return 0.toInt()
                } else if (className.equals("java.lang.Long")) {
                    json?.let {
                        if (it.contains(autoField)) {
                            try {
                                var jsonObject = JSONObject(it)
                                if (jsonObject.has(autoField)) {//双重判断保险
                                    return jsonObject.getString(autoField).toDouble().toLong()//fixme 兼容自定义属性字段
                                }
                            } catch (e: Exception) {
                            }
                        }
                        return it.toDouble().toLong()
                    }
                    return 0.toLong()
                } else if (className.equals("java.lang.Float")) {
                    json?.let {
                        if (it.contains(autoField)) {
                            try {
                                var jsonObject = JSONObject(it)
                                if (jsonObject.has(autoField)) {//双重判断保险
                                    return jsonObject.getString(autoField).toFloat()//fixme 兼容自定义属性字段
                                }
                            } catch (e: Exception) {
                            }
                        }
                        return it.toFloat()
                    }
                    return 0.toFloat()
                } else if (className.equals("java.lang.Double")) {
                    json?.let {
                        if (it.contains(autoField)) {
                            try {
                                var jsonObject = JSONObject(it)
                                if (jsonObject.has(autoField)) {//双重判断保险
                                    return jsonObject.getString(autoField).toDouble()//fixme 兼容自定义属性字段
                                }
                            } catch (e: Exception) {
                            }
                        }
                        return it.toDouble()
                    }
                    return 0.toDouble()
                } else if (className.equals("java.lang.Short")) {
                    json?.let {
                        if (it.contains(autoField)) {
                            try {
                                var jsonObject = JSONObject(it)
                                if (jsonObject.has(autoField)) {//双重判断保险
                                    return jsonObject.getString(autoField).toDouble().toShort()//fixme 兼容自定义属性字段
                                }
                            } catch (e: Exception) {
                            }
                        }
                        return it.toShort()
                    }
                    return 0.toShort()
                } else if (className.equals("java.lang.Character")) {
                    //fixme Character类型好像会乱码。
//                json?.let {
//                    if (it.contains(autoField)) {
//                        try {
//                            var jsonObject = JSONObject(it)
//                            if (jsonObject.has(autoField)) {//双重判断保险
//                                return jsonObject.getString(autoField)[0].toChar()//fixme 兼容自定义属性字段
//                            }
//                        } catch (e: Exception) {
//                        }
//                    }
//                    return it[0].toChar()
//                }
                    return 0.toChar()//也会乱码，如：��
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                KLoggerUtils.e("getObject()異常：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
            }
        }
        var clazzT: Class<*>? = null//当前类型里面的泛型
        if (classes.size > (index + 1)) {
            clazzT = classes[index + 1]
        }
        //Log.e("test", "当前类型:\t" + clazz + "\t泛型:\t" + clazzT + "\t下标：\t" + index)
        //必须有空构造函数，或者所有参数都有默认参数。说的是所有参数。不然无法实例化。
        var t: Any? = null
        var isMutableList = false
        if (clazz.name.equals("java.util.List") || clazz.name.equals("interface java.util.List")) {
            t = mutableListOf<Any>()
            isMutableList = true
        } else {
            t = clazz.newInstance()
        }
        //var t = clazz.newInstance()
        //判断json数据是否为空
        if (json == null || json.toString().trim().equals("") || json.toString().trim().equals("{}") || json.toString().trim().equals("[]")) {
            return t!!
        }
        //Log.e("test","clazz名稱:\t"+clazz.name)
        if (clazz.name.equals("java.util.ArrayList") || clazz.name.equals("class java.util.ArrayList") || isMutableList) {
            //fixme 数组
            var list = ArrayList<Any>()
            try {
                var jsonArray = JSONArray(json)
                var last = jsonArray.length()
                last -= 1//最后一个下标
                if (last < 0) {
                    last = 0
                }
                clazzT?.let {
                    var position = index + 1
                    for (i in 0..last) {
                        var m = getObject(jsonArray.getString(i), classes, position)
                        m?.let {
                            list.add(it as Any)
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                KLoggerUtils.e(msg = "JSONArray转换异常:\t" + e.message)
            }
            return list//直接返回数组
        } else {
            //fixme 非数组
            try {
                var jsonObject = JSONObject(json)
                val fields = KFieldsUtils.getAllFields(clazz)//fixme 获取当前类，及其父类所有的属性
//                clazz?.declaredFields?.forEach {
                fields?.forEach {
                    var value: String? = null
                    //fixme it.name就是实体类属性名称。和属性名称一样的。是大写就是大写，是小写就是小写。
                    if (jsonObject.has(it.name)) {//判斷json數據是否存在該字段；默认首字母是小写的。
                        value = jsonObject.getString(it.name)//获取json数据
                    } else {
                        //fixme 兼容json字段首字母大写问题。
                        var name2 = it.name.substring(0, 1).toUpperCase() + it.name.substring(1)//属性名称【首字目进行大写】。
                        if (jsonObject.has(name2)) {//判斷json數據是否存在該字段
                            value = jsonObject.getString(name2)//获取json数据
                        } else {
                            //fixme 兼容json字段首字母小写问题。
                            var name3 = it.name.substring(0, 1).toLowerCase() + it.name.substring(1)//属性名称【首字目进行小写】。
                            if (jsonObject.has(name3)) {//判斷json數據是否存在該字段
                                value = jsonObject.getString(name3)//获取json数据
                            } else {
                                var name4 = it.name.toLowerCase()//fixme 全部小写
                                if (jsonObject.has(name4)) {//判斷json數據是否存在該字段
                                    value = jsonObject.getString(name4)//获取json数据
                                } else {
                                    var name5 = it.name.toUpperCase()//fixme 全部大写
                                    if (jsonObject.has(name5)) {//判斷json數據是否存在該字段
                                        value = jsonObject.getString(name5)//获取json数据
                                    }
                                }
                            }
                        }
                    }
                    //Log.e("test","name:\t"+it.name+"\tvalue:\t"+value)
                    if (value != null && !value.trim().equals("") && !value.toLowerCase().trim().equals("null")) {//fixme 去除null
                        var type = it.genericType.toString().trim()//属性类型
                        //KLoggerUtils.e("type:\t"+type)
                        var name = it.name.substring(0, 1).toUpperCase() + it.name.substring(1)//属性名称【首字目进行大写】。
                        var m: Method? = null
                        if (type == "boolean" || type.equals("class java.lang.Boolean")) {//fixme boolean是基本類型，class java.lang.Boolean是對象類型。都存在。
                            //KLoggerUtils.e("name2:\t"+name2)
                            //FIXME TRUE 和 true不区分大小写，都能顺利转换成布尔类型。false和FALSE也能转布尔类型。
                            try {
                                m = clazz.getMethod("set" + name, it.type)
                            } catch (e: java.lang.Exception) {
                                //KLoggerUtils.e("GSON布尔类型异常：\t" + KCatchException.getExceptionMsg(e))
                                try {
                                    var name2 = name
                                    //fixme 只有 is是特殊的。has不是。has正常处理就行。
                                    if (name2.contains("Is")) {
                                        var index = name2.indexOf("Is")
                                        if (index == 0 && name2.length > 2) {
                                            name2 = name2.substring(2)
                                            name2 = name2.substring(0, 1).toUpperCase() + name2.substring(1)
                                        }
                                    }
                                    m = clazz.getMethod("set" + name2, it.type)
                                } catch (e: java.lang.Exception) {
                                    KLoggerUtils.e("GSON布尔类型异常2：\t" + KCatchException.getExceptionMsg(e))
                                }
                            }

                        }
                        if (m == null) {
                            //isFinishedLabel 小写的is;会自动去除掉，如：setFinishedLabel（）；大写的Is就不会。如：setIsFinishedLabel（）
                            if (name.contains("Is")) {
                                try {
                                    var name2 = name
                                    if (name2.contains("Is")) {
                                        var index = name2.indexOf("Is")
                                        if (index == 0 && name2.length > 2) {
                                            name2 = name2.substring(2)
                                            name2 = name2.substring(0, 1).toUpperCase() + name2.substring(1)//fixme 去除了Is
                                        }
                                    }
                                    m = clazz.getMethod("set" + name2, it.type)
                                } catch (e: java.lang.Exception) {
                                    //KLoggerUtils.e("JSON异常：\t" + KCatchException.getExceptionMsg(e))
                                }
                                if (m == null) {
                                    m = clazz.getMethod("set" + name, it.type)
                                }
                            } else {
                                m = clazz.getMethod("set" + name, it.type)
                            }
                        }
                        //Log.e("test", "属性:\t" + it.name + "\ttype:\t" + type)
                        //Log.e("test", "属性:\t" + it.name + "\t值：\t" + value + "\t类型:\t" + it.genericType.toString() + "\ttype:\t" + type)
                        if (type == "class java.lang.String" || type == "class java.lang.Object") {//Object 就是Any,class类型是相同的。
                            m?.invoke(t, value)//String类型 Object类型
                        } else if (type.contains("class java.lang.")) {//fixme 基本類型的對象
                            if (type.equals("class java.lang.Integer")) {
                                m?.invoke(t, value.toInt())//Int类型
                            } else if (type.equals("class java.lang.Float")) {
                                m?.invoke(t, value.toFloat())//Float类型
                            } else if (type.equals("class java.lang.Double")) {
                                m?.invoke(t, value.toDouble())//Double类型
                            } else if (type.equals("class java.lang.Long")) {
                                m?.invoke(t, value.toLong())//Long类型
                            } else if (type.equals("class java.lang.Boolean")) {
                                //FIXME TRUE 和 true不区分大小写，都能顺利转换成布尔类型。
                                m?.invoke(t, value.toBoolean())//布尔类型。 "true".toBoolean() 只有true能够转换为true，其他所有值都只能转换为false
                            } else if (type.equals("class java.lang.Short")) {
                                m?.invoke(t, value.toShort())//Short类型
                            } else if (type.equals("class java.lang.Byte")) {
                                var byte = value.toInt()//不能有小数点，不然转换异常。小数点无法正常转换成Int类型。可以有负号。负数能够正常转换。
                                if (byte > 127) {
                                    byte = 127
                                } else if (byte < -128) {
                                    byte = -128
                                }
                                m?.invoke(t, byte.toByte())//Byte类型 ,范围是：-128~127
                            } else if (type == "char" || type.equals("class java.lang.Character")) {
                                m?.invoke(t, value.toCharArray()[0])//Char类型。字符只有一个字符。即单个字符。
                            }
                        } else if (type == "int") {//fixme 基本類型
                            m?.invoke(t, value.toInt())//Int类型
                        } else if (type == "float") {
                            m?.invoke(t, value.toFloat())//Float类型
                        } else if (type == "double") {
                            m?.invoke(t, value.toDouble())//Double类型
                        } else if (type == "long") {
                            m?.invoke(t, value.toLong())//Long类型
                        } else if (type == "boolean") {
                            m?.invoke(t, value.toBoolean())//布尔类型。 "true".toBoolean() 只有true能够转换为true，其他所有值都只能转换为false
                        } else if (type == "short") {
                            m?.invoke(t, value.toShort())//Short类型
                        } else if (type == "byte") {
                            var byte = value.toInt()//不能有小数点，不然转换异常。小数点无法正常转换成Int类型。可以有负号。负数能够正常转换。
                            if (byte > 127) {
                                byte = 127
                            } else if (byte < -128) {
                                byte = -128
                            }
                            m?.invoke(t, byte.toByte())//Byte类型 ,范围是：-128~127
                        } else if (type == "char") {
                            m?.invoke(t, value.toCharArray()[0])//Char类型。字符只有一个字符。即单个字符。
                        } else {
                            try {
                                //fixme map
                                if (type.contains("Map") && (type.contains("java.util.HashMap")
                                                || type.contains("java.util.LinkedHashMap") || type.contains("java.util.Map"))) {
                                    //KLoggerUtils.e("进来了:\ttype:\t"+type+"\tvalue:"+value)
                                    try {
                                        m?.invoke(t, KGsonJavaUtils.fromJson(value, it.genericType))//fixme JSON转Map也借助Gson谷歌第三方库。
                                    } catch (e: java.lang.Exception) {
                                        KLoggerUtils.e("getObject(),JSON转map异常：\t" + KCatchException.getExceptionMsg(e), isLogEnable = true)
                                    }
                                } else {
                                    //fixme 泛型标志固定一下。就用T，T1或者T2。不要用其他的。不然不好辨别。
                                    if ((type.toString().trim().equals("T") || type.toString().trim().equals("T1") || type.toString().trim().equals("T2")) && clazzT != null) {
                                        //fixme 嵌套泛型。
                                        if (clazzT.name.equals("java.util.ArrayList")) {
                                            //fixme 嵌套泛型数组
                                            var jsonArray = JSONArray(value)
                                            var last = jsonArray.length()
                                            last -= 1//最后一个下标
                                            var list = ArrayList<Any>()
                                            if (last >= 0) {//fixme 数据长度必须大于0，不然异常。
                                                clazzT?.let {
                                                    var position = index + 2//fixme 注意就这里数组要加2（亲测）
                                                    for (i in 0..last) {
                                                        //Log.e("test", "嵌套数组循环:\t" + jsonArray.getString(i) + "\t下标:\t" + position)
                                                        var m = getObject(jsonArray.getString(i), classes, position)
                                                        m?.let {
                                                            list.add(it as Any)
                                                        }
                                                    }
                                                }
                                            }
                                            m?.invoke(t, list)
                                        } else {
                                            //fixme 嵌套泛型实体类
                                            var position = index + 1
                                            //KLoggerUtils.e("嵌套实体类1\tvalue:\t" + value + "\tclasses:\t" + classes)
                                            m?.invoke(t, getObject(value, classes, position))
                                            //KLoggerUtils.e("嵌套实体类2")
                                        }
                                    } else {
                                        //Log.e("test", "属性2:\t" + it.name + "\t值：\t" + value + "\t类型:\t" + it.genericType.toString() + "\ttype:\t" + type)
                                        //fixme 嵌套具体实体类[普通实体类不支持数组]
                                        if (!type.contains("java.util.ArrayList")) {
//                                        Log.e("test", "属性2:\t" + it.name + "\t值：\t" + value + "\t类型:\t" + it.genericType.toString() + "\ttype:\t" + type+"\tclasses:\t"+classes)
//                                        var position = index + 1
//                                        m?.invoke(t, getObject(value, classes, position))//fixme 非数组(不行，有问题。)

                                            //fixme 以下方法亲测，百分百可行！
                                            var classes = arrayListOf<Class<*>>()
                                            classes.add(Class.forName(type.substring("class ".length).trim()))
                                            m?.invoke(t, getObject(value, classes, 0))//fixme 非数组

                                        } else {
                                            //fixme 数组，如： java.util.ArrayList<com.example.myapplication.model.B>
                                            //[class java.util.ArrayList, class com.example.myapplication.model.C]
                                            //KLoggerUtils.e("数组类型：\t"+type)
                                            //KLoggerUtils.e("value:\t"+value)
                                            //KLoggerUtils.e("classes:\t"+classes)
                                            var start = type.indexOf("<")
                                            var end = type.indexOf(">")
                                            var newType = type.substring(start + 1, end)
                                            var newClasses: MutableList<Class<*>> = java.util.ArrayList()
                                            newClasses.add(Class.forName("java.util.ArrayList"))
                                            newClasses.add(Class.forName(newType))
                                            m?.invoke(t, getObject(value, newClasses, 0))//fixme 兼容数组
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                KLoggerUtils.e("kGsonUtils嵌套json解析异常:\t" + KCatchException.getExceptionMsg(e), true)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                KLoggerUtils.e("KGsonUtils转实体类异常:\t" + KCatchException.getExceptionMsg(e), true)
            }
        }
        return t!!
    }

}
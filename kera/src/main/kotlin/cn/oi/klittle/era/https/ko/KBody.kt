package cn.oi.klittle.era.https.ko

import cn.oi.klittle.era.gson.KGsonUtils
import org.json.JSONObject
import java.io.Serializable

/**
 * 自定义Body实体类,目的是解决参数body里面参数嵌套。即主要简化实体类包含实体类。
 */

//fixme 如：
//var body=KBody()
//body.addParam("id","001")
//body.addParam("name","孙悟空")
//body.addBody("newBody2").addParam("id","002").addParam("name","孙小圣")//fixme 返回新的body
//body.addBody("newBody3").addParam("id","003").addParam("name","孙小二").addBody("newBody4").addParam("id","004").addParam("name","孙小三")
//Log.e("test","json格式：\t"+body.toString())

//fixme 以上格式如下

//{
//    "id": "001",
//    "name": "孙悟空",
//    "newBody2": {
//    "id": "002",
//    "name": "孙小圣"
//},
//    "newBody3": {
//    "id": "003",
//    "name": "孙小二",
//    "newBody4": {
//    "id": "004",
//    "name": "孙小三"
//}
//}
//}

open class KBody :Serializable{
    var json: JSONObject? = null
    protected var map: MutableMap<String, KBody?>? = null

    open fun map(): MutableMap<String, KBody?> {
        if (map == null) {
            map = mutableMapOf()
        }
        return map!!
    }

    constructor(){
        json = JSONObject()
    }

    //销毁
    open fun onDestroy() {
        map?.clear()
        map = null
        json = null
    }

    /**
     * fixme 获取所有json数据格式
     */
    override fun toString(): String {
        map?.let {
            for ((key, value) in it.entries) {
                json?.put(key, JSONObject(value.toString()))//fixme 这里会递归调用。
            }
        }
        if (json != null) {
            return json.toString()
        }
        return ""
    }

    //添加单个字段参数
    open fun addParam(key: String, value: String?): KBody {
        value?.let {
//            if (!it.trim().equals("") && !it.trim().equals("null") && !it.trim().equals("NULL")) {
//                json?.put(key, value)
//            }
            json?.put(key, value)//允许为空字符"" ,JSONObject可以为空字符。
        }
        return this
    }

    //添加实体类
    open fun addParam(key: String, value: Any?): KBody {
        value?.let {
            json?.put(key, KGsonUtils.parseAnyToJSON(value))//fixme 实体类转JSON对象
        }
        return this
    }

    //添加多个字段参数。
    open fun addParam(param: MutableMap<String, String>? = null): KBody {
        param?.let {
            for ((key, value) in param.entries) {
                json?.put(key, value)
            }
        }
        return this
    }

    /**
     * fixme 返回新的KBody(如果已经存在，则返回旧的)，（即实体类里面再包含实体类）
     */
    open fun addBody(key: String): KBody? {
        if (map().contains(key)) {
            if (map().get(key) != null) {
                return map().get(key)//fixme 防止重复。返回原有已经存在的。
            }
        }
        //fixme 新建kbody
        map().put(key, KBody())
        return map().get(key)
    }

}
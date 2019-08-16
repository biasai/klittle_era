package cn.oi.klittle.era.activity.photo.entity

import java.io.Serializable

/**
 * 图片集合
 */
open class KLocalMediaFolder : Serializable {
    var name: String? = null//文件夹名称；如 相机胶卷，Camera,WeiXin等。方向会根据中英文自动切换。
    var path: String? = null//文件路径
    var firstImagePath: String? = null//第一张图片路径
    var imageNum: Int = 0//图片个数
    var checkedNum: Int = 0//选中个数
    var isChecked: Boolean = false//是否选中
    var images: MutableList<KLocalMedia>? = null//图片集合
}
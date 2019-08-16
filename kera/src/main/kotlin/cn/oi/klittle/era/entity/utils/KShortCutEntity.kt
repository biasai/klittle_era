package cn.oi.klittle.era.entity.utils

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import cn.oi.klittle.era.utils.KAssetsUtils
import cn.oi.klittle.era.utils.KPermissionUtils
import cn.oi.klittle.era.utils.KShortcutUtils

//            fixme 调用案例
//            KShortCut().apply {
//                name = "快捷方式二"
//                id="123456"
//                bitmap = getBitmapFromAssets("kera/progress/circleprgoress.png")
//                clazz = Main2Activity::class.java
//                putString("参数", "快捷方式二 123456")//目标Activity可以通过intent里的bundle获取得到。
//                addShortCut(this@MainActivity) {
//                    KLoggerUtils.e("test", "添加结果2：\t" + it)
//                }
//            }

/**
 * 桌面快捷方式实体类
 */
open class KShortCutEntity {

    var id: String? = null//8.0才需要（8.0以下不需要），8.0用id来区分不同的快捷方式。8.0以下是通过判断indent是否相同（包括里面的参数）来进行判断的。
    var name: String? = null//名称
    var icon: Bitmap? = null//快捷方式图标(优先使用)
    var iconRes: Int? = null//快捷方式图标(res目录下的)
    var clazz: Class<*>? = null//目标Activity

    //calss类型这样设置即可
    fun clazz(any: Any) {
        clazz = any::class.java
    }

    var bundle: Bundle? = null//存放一些数据

    //设置特定参数
    open fun putString(key: String, value: String): KShortCutEntity {
        if (bundle == null) {
            bundle = Bundle()
        }
        bundle?.putString(key, value)
        return this
    }

    companion object {
        //获取位图
        open fun getBitmapFromAssets(filePath: String, isRGB_565: Boolean = false): Bitmap {
            return KAssetsUtils.getInstance().getBitmapFromAssets(filePath, isRGB_565)
        }

        open fun getBitmapFromResource(resID: Int, isRGB_565: Boolean = false): Bitmap {
            return KAssetsUtils.getInstance().getBitmapFromResource(resID, isRGB_565)
        }

        open fun getBitmapFromFile(filePath: String, isRGB_565: Boolean = false): Bitmap {
            return KAssetsUtils.getInstance().getBitmapFromFile(filePath, isRGB_565)
        }
    }


    /**
     * 开始添加快捷方式
     * @param activity
     * @param callback 回调是否成功
     */

    open fun addShortCut(activity: Activity, callback: ((isSuccess: Boolean) -> Unit)? = null) {
        if (id == null) {
            name?.let {
                if (it.length > 0) {
                    id = it//在此统一，默认id和name相同。
                }
            }
        }
        if (activity != null && !activity.isFinishing) {
            KPermissionUtils.requestPermissionsLaunch(activity) {
                if (it) {
                    KShortcutUtils.addShortCut(id = id, name = name, iconBitmap = icon, iconRes = iconRes, clazz = clazz, bundle = bundle)
                    callback?.let {
                        it(true)//fixme 具备快捷方式添加权限，添加成功。(不一定，有的设备（如小米）无法判断该权限是否开启，始终返回true，所以还是需要用户去手动设置开启权限。)
                    }
                } else {
                    //KPermissionUtils.showFailure(activity) 显示权限问题。需要自己手动调用。
                    callback?.let {
                        it(false)//不具备快捷方式的权限，快捷方式添加失败
                    }
                }
            }
        }
    }

}
package cn.oi.klittle.era.service

import android.os.Binder
import cn.oi.klittle.era.utils.KLoggerUtils

/**
 * Activity和Service进行关联。即Activity调用Binder里面的方法。
 */
open class KBindder : Binder() {}
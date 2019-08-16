package cn.oi.klittle.era.comm

import cn.oi.klittle.era.base.KBasePx

//可以根据不同需求，创建对应的适配标准。
object kpx : KBasePx() {
    init {
        //创建视图适配标准。
        init(baseWidth = 750f, baseHeight = 1334f)
    }
}
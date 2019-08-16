package cn.oi.klittle.era.entity.feature

import android.graphics.Color

/**
 * 高斯模糊（毛玻璃效果）；主要用于弹窗背景。
 * @param isBlur 是否开启高斯模糊效果
 * @param level 模糊等级【0 ~ 25之间】
 * @param coverColor 要蒙上的颜色
 */
data class KBlur(var isBlur: Boolean = true, var level: Float = 25f, var coverColor: Int? = Color.TRANSPARENT) {
}
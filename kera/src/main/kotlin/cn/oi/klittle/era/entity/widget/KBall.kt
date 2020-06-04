package cn.oi.klittle.era.entity.widget

import android.graphics.Color

/**
 * fixme 两个翻转的小球动画，防抖音加载进度。
 * @param radius 半径
 * @param centerX 圆心坐标X
 * @param centerY 圆心坐标Y
 * @param color 小圆圈的颜色 Color.RED 和 Color.CYAN 是抖音的两个小球的颜色。
 */
data class KBall(var radius: Float = 0f, var centerX: Float = 0f, var centerY: Float = 0f, var color: Int = Color.RED) {
    var isAdd=true//fixme centerX 坐标是否加加；true ++;false --。这个不需要去关。在K4AutoBgView的drawAuto（）方法里。会自动去算。
}
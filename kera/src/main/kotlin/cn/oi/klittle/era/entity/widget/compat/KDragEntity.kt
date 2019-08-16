package cn.oi.klittle.era.entity.widget.compat

/**
 * @param left_gap 左边边界值；即与父容器左边的距离。支持正负数。负数会越过父容器最左边。
 * @param top_gap 上边边界值。
 * @param right_gap 右边边界值，支持正负数。负数会越过父容器最右边。fixme 总之 正数表示与父容器之间的间距。负数会越过父容器。0就是父容器的边界。
 * @param bottom_gap 下边边界值。
 * @param isDragEnable 是否开启拖动功能
 * @param isAbs 是否开启吸附功能（自动吸附到边缘）
 * @param duraton 吸附到边缘所画的时间。单位毫秒;200毫秒个人感觉效果最好。
 * @param isAbsLeft 是否吸附到左边缘
 * @param isAbsTop 是否吸附到上边缘
 * @param isAbsRight 是否吸附到右边缘
 * @param isAbsBottom 是否吸附到下边缘
 */
data class KDragEntity(var left_gap: Int = 0, var top_gap: Int = 0, var right_gap: Int = 0, var bottom_gap: Int = 0,
                       var isDragEnable: Boolean = true, var isAbs: Boolean = false, var duraton: Long = 200,
                       var isAbsLeft: Boolean = true,
                       var isAbsTop: Boolean = true,
                       var isAbsRight: Boolean = true,
                       var isAbsBottom: Boolean = true) {

    //设置所有边界值。
    fun all_gap(gap: Int = 0) {
        left_gap = gap
        top_gap = gap
        right_gap = gap
        bottom_gap = gap
    }

}
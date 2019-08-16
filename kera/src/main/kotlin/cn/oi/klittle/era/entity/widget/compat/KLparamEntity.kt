package cn.oi.klittle.era.entity.widget.compat

/**
 * 控制宽高和外补丁变化
 * @param width 宽度
 * @param height 高度
 * @param leftMargin 左外补丁
 * @param topMargin 上外补丁
 * @param rightMargin 右外补丁
 * @param bottomMargin 下外补丁
 * @param scale 对x,y同时缩放(如果是viewgroup,子view也会一起缩放)。缩放倍率1是正常大小。（这个也可以实现大小变化效果）
 * @param alpha 透明度(组件的透明度，这里范围0~1，canvas画布才是0~255)
 * @param rotation 旋转角度(如果有子控件，子控件会跟着一起旋转)
 * @param duration 变化时间，单位毫秒。1000等于一秒。
 */
data class KLparamEntity(var width: Int = 0, var height: Int = 0, var leftMargin: Int = 0, var topMargin: Int = 0, var rightMargin: Int = 0, var bottomMargin: Int = 0,
                         var scale: Float = 1f, var alpha: Float = 1f, var rotation: Float = 0f,
                         var duration: Long = 200) {
}
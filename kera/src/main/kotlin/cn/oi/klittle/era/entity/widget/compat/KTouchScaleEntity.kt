package cn.oi.klittle.era.entity.widget.compat

/**
 * @param isTouchScaleEnable 是否开启触摸缩放功能
 * @param scaleMaxWidth 缩放最大宽度;直接使用具体的值;就不使用倍率了.
 * @param scaleMinWidth 缩放最小宽度
 * @param isRotationEnable 是否开启旋转功能；默认false
 * @param rotation 旋转角度(是度数。);会实时记录当前画布的旋转角度
 */
data class KTouchScaleEntity(var isTouchScaleEnable: Boolean = true, var scaleMaxWidth: Int? = null, var scaleMinWidth: Int? = null,
                             var isRotationEnable: Boolean = false, var rotation: Float = 0F) {


}
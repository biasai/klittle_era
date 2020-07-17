package cn.oi.klittle.era.widget.gamepad.entiy

/**
 * 滚轮方向回调
 */
class KOrientation {

    //记录当前手指的触摸点。
    var x:Float=0F
    var y:Float=0F

    //正中间（原点）;fixme 手指离开的时候，一定会回调该方法。
    var center: (() -> Unit)? = null
    fun center(center: (() -> Unit)? = null) {
        this.center = center
    }

    //正左边，即后面
    var left: (() -> Unit)? = null
    fun left(left: (() -> Unit)? = null) {
        this.left = left
    }

    //正右边，即前面
    var right: (() -> Unit)? = null
    fun right(right: (() -> Unit)? = null) {
        this.right = right
    }

    //正上方
    var top: (() -> Unit)? = null
    fun top(top: (() -> Unit)? = null) {
        this.top = top
    }

    //正下方
    var bottom: (() -> Unit)? = null
    fun bottom(bottom: (() -> Unit)? = null) {
        this.bottom = bottom
    }

    //第一象限（右上角）
    var one_right_top: (() -> Unit)? = null
    fun one_right_top(one_right_top: (() -> Unit)? = null) {
        this.one_right_top = one_right_top
    }

    //第二象限（左上角）
    var two_left_top: (() -> Unit)? = null
    fun two_left_top(two_left_top: (() -> Unit)? = null) {
        this.two_left_top = two_left_top
    }

    //第三象限（左下角）
    var three_left_bottom: (() -> Unit)? = null
    fun three_left_bottom(three_left_bottom: (() -> Unit)? = null) {
        this.three_left_bottom = three_left_bottom
    }

    //第四象限（右下角）
    var four_right_bottom: (() -> Unit)? = null
    fun four_right_bottom(four_right_bottom: (() -> Unit)? = null) {
        this.four_right_bottom = four_right_bottom
    }

    //fixme 销毁
    fun destroy() {
        center = null
        left = null
        right = null
        top = null
        bottom = null
        one_right_top = null
        two_left_top = null
        three_left_bottom = null
        four_right_bottom = null
    }

}
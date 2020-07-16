package cn.oi.klittle.era.widget.gamepad.entiy

/**
 * 滚轮方向回调
 */
class KOrientation {
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
    fun bottom(top: (() -> Unit)? = null) {
        this.bottom = bottom
    }

    //第一象限（右上角）
    var one: (() -> Unit)? = null
    fun one(one: (() -> Unit)? = null) {
        this.one = one
    }

    //第二象限（左上角）
    var two: (() -> Unit)? = null
    fun two(two: (() -> Unit)? = null) {
        this.two = two
    }

    //第三象限（左下角）
    var three: (() -> Unit)? = null
    fun three(three: (() -> Unit)? = null) {
        this.three = three
    }

    //第四象限（右下角）
    var four: (() -> Unit)? = null
    fun four(four: (() -> Unit)? = null) {
        this.four = four
    }

}
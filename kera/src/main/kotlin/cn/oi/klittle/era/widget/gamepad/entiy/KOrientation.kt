package cn.oi.klittle.era.widget.gamepad.entiy

import android.view.MotionEvent

/**
 * fixme 滚轮方向回调;只有方向发生改变时（或者手指按下和离开时），才会回调；即：相同的方向不会重复回调。
 */
class KOrientation {

    //记录当前手指的触摸点。
    var x: Float = 0F
    var y: Float = 0F
    var action: Int = MotionEvent.ACTION_UP//手指的动作

    //判断手指是否离开
    fun isActionUp(): Boolean {
        if (action == MotionEvent.ACTION_UP) {
            return true
        }
        return false
    }

    //判断手指是否按下
    fun isActionDown(): Boolean {
        if (action == MotionEvent.ACTION_DOWN) {
            return true
        }
        return false
    }

    //判断手指是否触摸移动
    fun isActionMove(): Boolean {
        if (action == MotionEvent.ACTION_MOVE) {
            return true
        }
        return false
    }

    val orientation_left = 0//左（后面）
    val orientation_right = 1//右（前面）
    val orientation_top = 2//上
    val orientation_bottom = 3//下
    val orientation_one_right_top = 6//右上
    val orientation_two_left_top = 4//左上
    val orientation_three_left_bottom = 5//左下
    val orientation_four_right_bottom = 7//右下
    val orientation_center = 8//中间

    var orientation_current = orientation_center//fixme 记录当前方向

    /**
     * 判断方向是否一致
     * @param orientation 方向
     * @param isRecord 是否记录当前方向
     */
    fun isSameOrientation(orientation: Int, isRecord: Boolean): Boolean {
        if (orientation == orientation_current) {
            return true
        }
        if (isRecord) {
            orientation_current = orientation//记录当前的方向
        }
        return false
    }

    //判断当前方向是否为左边（后面）
    fun isLeft(): Boolean {
        if (orientation_left == orientation_current) {
            return true
        }
        return false
    }

    fun isRight(): Boolean {
        if (orientation_right == orientation_current) {
            return true
        }
        return false
    }

    fun isTop(): Boolean {
        if (orientation_top == orientation_current) {
            return true
        }
        return false
    }

    fun isBottom(): Boolean {
        if (orientation_bottom == orientation_current) {
            return true
        }
        return false
    }

    fun isOne_right_top(): Boolean {
        if (orientation_one_right_top == orientation_current) {
            return true
        }
        return false
    }

    fun isTow_left_top(): Boolean {
        if (orientation_two_left_top == orientation_current) {
            return true
        }
        return false
    }

    fun isThree_left_bottom(): Boolean {
        if (orientation_three_left_bottom == orientation_current) {
            return true
        }
        return false
    }

    fun isFour(): Boolean {
        if (orientation_four_right_bottom == orientation_current) {
            return true
        }
        return false
    }

    fun isCenter(): Boolean {
        if (orientation_center == orientation_current) {
            return true
        }
        return false
    }

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
package cn.oi.klittle.era.entity.camera

import android.graphics.Camera
import android.graphics.Canvas
import android.graphics.Matrix
import cn.oi.klittle.era.widget.compat.K1Widget
import java.lang.Exception

//               fixme 调用案例
//                kView {
//                    var rotateX = 0F//旋转角度
//                    var mCamera: KCamera? =  KCamera(this)
//                    draw { canvas, paint ->
//                        mCamera?.rotateX(rotateX++) {
//                            paint.color = Color.RED
//                            paint.style = Paint.Style.FILL
//                            paint.strokeWidth = kpx.x(5f)
//                            canvas.drawCircle(centerX, centerY, kpx.x(50f), paint)
//                        }
//                        mCamera?.rotateY(rotateX) {
//                            paint.color = Color.BLUE
//                            paint.style = Paint.Style.STROKE
//                            canvas.drawCircle(centerX, centerY, kpx.x(100f), paint)
//                        }
//                        invalidate()//不断刷新
//                    }
//                }.lparams {
//                    width = kpx.x(500)
//                    height = width
//                    centerInParent()
//                }


//               fixme 调用案例二（更简单，已经集成到K1Widget类里面了）
//                kView {
//                    var rotateX = 0F//旋转角度
//                    draw { canvas, paint ->
//                        rotateX += 1
//                        rotateX(rotateX) {
//                            paint.color = Color.RED
//                            paint.style = Paint.Style.FILL
//                            paint.strokeWidth = kpx.x(5f)
//                            canvas.drawCircle(centerX, centerY, kpx.x(100f), paint)
//                            paint.color = Color.CYAN
//                            paint.textAlign=Paint.Align.CENTER
//                            paint.textSize=kpx.x(88f)
//                            canvas.drawText("帝",centerX,kpx.centerTextY(paint,h.toFloat()),paint)
//                        }
//                        rotateY(rotateX) {
//                            paint.color = Color.BLUE
//                            paint.style = Paint.Style.STROKE
//                            canvas.drawCircle(centerX, centerY, kpx.x(200f), paint)
//                        }
//                        invalidate()//不断刷新
//                    }
//
//                }.lparams {
//                    width = kpx.x(500)
//                    height = width
//                    centerInParent()
//                }

/**
 * fixme 画图Camera；用于实现3d翻转效果。翻转之后能够显示它翻转之后的效果。90度什么都不显示。180度是旋转之后的效果。360度不变。
 * @param canvas 画布
 * @param centerX 控件中心坐标X
 * @param centerY 控件中心坐标Y
 */
class KCamera(var widget: K1Widget) {
    var mCamera = Camera()
    var mMatrix = Matrix()

    init {
        //保存最初始的状态
        save()
    }

    /**
     * 保存状态
     */
    fun save() {
        try {
            widget?.mCanvas?.save()
            mCamera.save()
            mMatrix.reset()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 恢复状态
     */
    fun restore() {
        try {
            widget.mCanvas?.restore()
            mCamera.restore()
            mMatrix.reset()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 相机和画布绑定；将相机的效果更新到画布上面去。
     */
    fun concat(canvas: Canvas, centerX: Float, centerY: Float) {
        mCamera.getMatrix(mMatrix)//在相机属性设置完成之后，再调用getMatrix()。
        //以下属性在getMatrix()和concat()之间调用才有效。因为相机属性会覆盖以下属性，所以在相机属性设置之后再设置。
        //mMatrix.preScale(2f, 2f)//fixme 缩放两倍，不要调用setScale();set会覆盖所有属性，相机旋转效果也会无效。
        mMatrix.preTranslate(-centerX, -centerY)//mMatrix旋转中心默认是(0,0)；所以将旋转中心移动画布中心。
        mMatrix.postTranslate(centerX, centerY)//恢复画布位置。
        canvas.concat(mMatrix)//fixme 建议使用，不要使用 mCamera.applyToCanvas(canvas) 效果不好。
    }

    /**
     * 沿X轴旋转
     * @param degree 旋转角度，是角度不是弧度。
     * @param centerX 旋转中心X
     * @param centerY 旋转中心Y
     * @param isRestore 是否保存和恢复画布相机原始状态。
     * @param callBack 相机旋转后的回调
     */
    fun rotateX(degree: Float, centerX: Float = widget.centerX, centerY: Float = widget.centerY, isRestore: Boolean = true, callBack: (() -> Unit)? = null) {
        try {
            if (widget.mCanvas == null) {
                return
            }
            if (degree == 0f) {
                //fixme 回调。
                callBack?.let {
                    it()
                }
                return
            }
            //保存正常状态
            if (isRestore) {
                save()
            }
            mCamera.rotateX(degree)
            concat(widget.mCanvas!!, centerX, centerY)
            //fixme 回调。
            callBack?.let {
                it()
            }
            //恢复正常状态
            if (isRestore) {
                restore()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 沿Y轴旋转
     * @param degree 旋转角度，是角度不是弧度。
     * @param centerX 旋转中心坐标x
     * @param centerY 旋转中心坐标y
     * @param isRestore 是否保存和恢复画布相机原始状态。
     * @param callBack 相机旋转后的回调
     */
    fun rotateY(degree: Float, centerX: Float = widget.centerX, centerY: Float = widget.centerY, isRestore: Boolean = true, callBack: (() -> Unit)? = null) {
        try {
            if (widget.mCanvas == null) {
                return
            }
            if (degree == 0f) {
                //fixme 回调。
                callBack?.let {
                    it()
                }
                return
            }
            //保存正常状态
            if (isRestore) {
                save()
            }
            mCamera.rotateY(degree)
            concat(widget.mCanvas!!, centerX, centerY)
            //fixme 回调。
            callBack?.let {
                it()
            }
            //恢复正常状态
            if (isRestore) {
                restore()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 沿Z轴旋转。
     * @param degree 旋转角度
     * @param centerX 旋转中心坐标x
     * @param centerY 旋转中心坐标y
     * @param isRestore 是否保存和恢复画布相机原始状态。
     * @param callBack 相机旋转后的回调
     */
    fun rotateZ(degree: Float, centerX: Float = widget.centerX, centerY: Float = widget.centerY, isRestore: Boolean = true, callBack: (() -> Unit)? = null) {
        try {
            if (widget.mCanvas == null) {
                return
            }
            if (degree == 0f) {
                //fixme 回调。
                callBack?.let {
                    it()
                }
                return
            }
            //保存正常状态
            if (isRestore) {
                save()
            }
            mCamera.rotateZ(degree)
            concat(widget.mCanvas!!, centerX, centerY)
            //fixme 回调。
            callBack?.let {
                it()
            }
            //恢复正常状态
            if (isRestore) {
                restore()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 沿X,Y,Z三轴同时旋转。
     * @param degree 旋转角度
     * @param centerX 旋转中心坐标x
     * @param centerY 旋转中心坐标y
     * @param isRestore 是否保存和恢复画布相机原始状态。
     * @param callBack 相机旋转后的回调
     */
    fun rotate(degree: Float, centerX: Float = widget.centerX, centerY: Float = widget.centerY, isRestore: Boolean = true, callBack: (() -> Unit)? = null) {
        try {
            if (widget.mCanvas == null) {
                return
            }
            if (degree == 0f) {
                //fixme 回调。
                callBack?.let {
                    it()
                }
                return
            }
            //保存正常状态
            if (isRestore) {
                save()
            }
            mCamera.rotate(degree, centerX, centerY)
            concat(widget.mCanvas!!, centerX, centerY)
            //fixme 回调。
            callBack?.let {
                it()
            }
            //恢复正常状态
            if (isRestore) {
                restore()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
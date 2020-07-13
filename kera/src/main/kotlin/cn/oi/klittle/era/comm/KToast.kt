package cn.oi.klittle.era.comm

import android.app.Activity
import android.graphics.Color
import android.graphics.Paint
import android.view.Gravity
import android.view.View
import android.widget.Toast
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseActivityManager
import cn.oi.klittle.era.base.KBaseApplication
import cn.oi.klittle.era.exception.KCatchException
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.widget.compat.KButton
import cn.oi.klittle.era.widget.compat.KTextView
import org.jetbrains.anko.*
import java.lang.Exception

/**
 * Created by 彭治铭 on 2018/6/24.
 */
object KToast {

    fun getActivity(): Activity? {
        return KBaseActivityManager.getInstance().stackTopActivity
    }

    var toast: Toast? = null
    var textView: KTextView? = null
    fun view(): View? = with(kpx.context()?.baseContext) {
        this?.let {
            with(it) {
                UI {
                    verticalLayout {
                        gravity = Gravity.CENTER
                        textView = KTextView(this).apply {
                            gravity = Gravity.CENTER
                            clearButonShadow()
                        }.lparams { }
                    }
                }?.view
            }
        }
    }

    var yOffset = kpx.y(160)//提示框的与屏幕底部的距离。

    //以下默认属性，可以全局修改。根据需求来改。
    //var defaultColor = Color.parseColor("#ab313131")//默认背景颜色（浅黑色）
    //var defaultColor = Color.parseColor("#61A465")//浅绿色，效果不错。
    var defaultTextSize = kpx.textSizeX(32)//默认字体大小
    var defaultTextColor = Color.WHITE//默认字体颜色
    private fun default() {
        textView?.let {
            it.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL//这个是最合理的。
            //默认样式
            it.padding = 0
            it.leftPadding = kpx.x(30)
            it.rightPadding = kpx.x(30)
            it.topPadding = kpx.x(16)
            it.bottomPadding = it.topPadding
            it.radius {
                all_radius(kpx.x(600f))
                strokeWidth = kpx.x(2f)
                dashWidth = kpx.x(10f)
                dashGap = kpx.x(10f)
                bgHorizontalColors(Color.parseColor("#ee010101"), Color.parseColor("#ee2E2E2C"), Color.parseColor("#ee414141"))
                strokeHorizontalColors(Color.TRANSPARENT, Color.parseColor("#ee2E2E2C"), Color.WHITE)
            }
            it.setTextSize(defaultTextSize)
            it.setTextColor(defaultTextColor)
            it.draw { canvas, paint -> }//自定义画空

            it.autoBg {
                isDraw = false//不绘制图片
            }
        }
    }

//    调用案例
//    Toast.show("提示信息")
//    Toast.show(exitInfo){
//        it.apply {
//            backgroundColor= Color.parseColor("#61A465")//根据需求自定义文本框样式,这里是浅绿色，效果不错。
//        }
//    }

    //显示成功文本,浅绿色背景
    open fun showSuccess(text: String?, activity: Activity? = getActivity(), init: ((textView: KTextView) -> Unit)? = null) {
//        if (activity==null){
//            return
//        }
        var isShow = show(text, activity) {
            it.apply {
                radius {
                    bgHorizontalColors(Color.parseColor("#61A465"), Color.parseColor("#90C551"))
                    strokeHorizontalColors(Color.TRANSPARENT, Color.parseColor("#ee2E2E2C"), Color.parseColor("#61A465"))
                }
                leftPadding = kpx.x(80)
                rightPadding = kpx.x(30)
                //画正确的勾勾
//                draw { canvas, paint ->
//                    paint.color = Color.WHITE
//                    paint.strokeCap = Paint.Cap.ROUND
//                    paint.style = Paint.Style.STROKE
//                    paint.strokeWidth = kpx.x(4.5f)
//                    var w = kpx.x(30f)
//                    var startX = leftPadding.toFloat() / 9 * 4
//                    var endX = startX + w / 3
//                    var startY = centerY + w / 7
//                    var endY = startY + w / 4
//                    canvas.drawLine(startX, startY, endX, endY, paint)
//                    var endX2 = endX + w / 3 * 2
//                    var endY2 = endY - w / 3 * 2
//                    canvas.drawLine(endX, endY, endX2, endY2, paint)
//                }

                //使用正确的勾勾图片
                autoBg {
                    isDraw = true
                    width = kpx.x(40)
                    height = kpx.x(40)
                    isAutoCenterVertical = true
                    autoLeftPadding = leftPadding.toFloat() / 3f
                    autoBg(R.mipmap.kera_correct1)
                    autoBgColor = Color.WHITE
                }

            }
        }
        if (isShow) {
            init?.let {
                textView?.let {
                    init(it)//可根据需求自定义样式
                }
            }
        }
    }

    //显示失败文本,浅红色背景
    open fun showError(text: String?, activity: Activity? = getActivity(), init: ((textView: KTextView) -> Unit)? = null) {
//        if (activity==null){
//            return
//        }
        var isShow = show(text, activity) {
            it.apply {
                radius {
                    bgHorizontalColors(Color.parseColor("#DA2222"), Color.parseColor("#EB7AA0"))
                    strokeHorizontalColors(Color.TRANSPARENT, Color.parseColor("#ee2E2E2C"), Color.parseColor("#DA2222"))
                }
                leftPadding = kpx.x(80)
                rightPadding = kpx.x(30)
                //画错误的叉叉
//                draw { canvas, paint ->
//                    paint.color = Color.WHITE
//                    paint.strokeCap = Paint.Cap.ROUND
//                    paint.style = Paint.Style.STROKE
//                    paint.strokeWidth = kpx.x(4.5f)
//                    var w = kpx.x(25f)
//                    var startX = leftPadding.toFloat() / 2
//                    var endX = startX + w
//                    var startY = centerY - w / 2
//                    var endY = centerY + w / 2
//                    canvas.drawLine(startX, startY, endX, endY, paint)
//                    canvas.drawLine(startX, endY, endX, startY, paint)
//                }

                //画错误提示图片
                autoBg {
                    isDraw = true
                    width = kpx.x(45)
                    height = width
                    isAutoCenterVertical = true
                    autoLeftPadding = leftPadding.toFloat() / 3f
                    autoBg(R.mipmap.kera_error2)
                    autoBgColor = Color.WHITE
                }
            }
        }
        if (isShow) {
            init?.let {
                textView?.let {
                    init(it)//可根据需求自定义样式
                }
            }
        }
    }

    //显示提示信息，浅蓝色背景
    open fun showInfo(text: String?, activity: Activity? = getActivity(), init: ((textView: KTextView) -> Unit)? = null) {
//        if (activity==null){
//            return
//        }
        var isShow = show(text, activity) {
            it.apply {
                radius {
                    bgHorizontalColors(Color.parseColor("#525FB7"), Color.parseColor("#3388FF"))
                    strokeHorizontalColors(Color.TRANSPARENT, Color.parseColor("#ee2E2E2C"), Color.parseColor("#525FB7"))
                }
                leftPadding = kpx.x(80)
                rightPadding = kpx.x(30)
                //画提示图标
//                draw { canvas, paint ->
//                    paint.color = Color.WHITE
//                    paint.strokeCap = Paint.Cap.ROUND
//                    paint.style = Paint.Style.STROKE
//                    paint.strokeWidth = kpx.x(3.5f)
//                    var w = kpx.x(35f)
//                    var startX = leftPadding.toFloat() / 9 * 3
//                    var startY = centerY - w / 2 + kpx.x(3f)
//                    var endY = centerY + w / 2
//                    canvas.drawCircle(startX + w / 2, centerY, w / 2, paint)//圆
//                    paint.style = Paint.Style.FILL
//                    paint.strokeWidth = 0F
//                    var r = kpx.x(5f) / 2
//                    canvas.drawCircle(startX + w / 2, startY + r + kpx.x(5f), r, paint)//点
//                    paint.style = Paint.Style.STROKE
//                    paint.strokeWidth = kpx.x(3.5f)
//                    canvas.drawLine(startX + w / 2, startY + r * 6, startX + w / 2, endY - r * 3.5f, paint)//线
//                }

                //使用提示图标图片
                autoBg {
                    isDraw = true
                    width = kpx.x(45)
                    height = width
                    isAutoCenterVertical = true
                    autoLeftPadding = leftPadding.toFloat() / 3.6f
                    autoBg(R.mipmap.kera_info3)
                    autoBgColor = Color.WHITE
                }

            }
        }
        if (isShow) {
            init?.let {
                textView?.let {
                    init(it)//可根据需求自定义样式
                }
            }
        }
    }

    //显示一般文本,浅黑色背景
    open fun show(text: String?, activity: Activity? = getActivity(), init: ((textView: KTextView) -> Unit)? = null): Boolean {
        var time = System.currentTimeMillis() - currentTime
        if (time <= time_interval) {
            return false//调用间隔小于这个1500毫秒；不显示。
        }
        var act = activity
        if (act == null) {
            act = getActivity()
        }
        try {
            if (act == null) {
                showText(text, init)
            } else {
                act?.runOnUiThread {
                    showText(text, init)
                }
            }
        } catch (e: Exception) {
            KLoggerUtils.e("Toast异常：\t" + KCatchException.getExceptionMsg(e), true)
            return false
        }
        return true
    }

    var currentTime = 0L//fixme 解決短时间内重复调用，无法显示的问题。
    var time_interval = 500L//最少间隔时间
    private fun showText(text: String?, init: ((textView: KTextView) -> Unit)? = null) {
        var time = System.currentTimeMillis() - currentTime
        if (time <= time_interval) {
            return
        }
        text?.let {
            if (!it.trim().equals("")) {
                if (toast == null || (time <= 3500)) {
                    //短时间内重复调用，必须重新实例化一个才有效(不然不会显示)。
                    toast?.cancel()
                    toast?.view = null
                    toast = null
                    toast = Toast(KBaseApplication.getInstance())
                    //toast?.setDuration(Toast.LENGTH_SHORT)// 显示时长，1000为1秒
                    toast?.setDuration(Toast.LENGTH_LONG)
                    val view = view()
                    toast?.setView(view)// 自定义view
                }
                default()//默认样式
                init?.let {
                    textView?.let {
                        init(it)//可根据需求自定义样式
                    }
                }
                toast?.setGravity(Gravity.CENTER or Gravity.BOTTOM, 0, yOffset)// 显示位置
                textView?.setText(text)
                toast?.show()//ui主线程
                currentTime = System.currentTimeMillis()
            }
        }
    }
}
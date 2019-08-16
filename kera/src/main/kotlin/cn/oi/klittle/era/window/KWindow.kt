package cn.oi.klittle.era.window

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import cn.oi.klittle.era.base.KBaseApplication
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.widget.compat.KShadowView

//                    fixme 调用案例
//                    button {
//                        text = "创建悬浮窗"
//                        var shadowView: KShadowView? = null
//                        onClick {
//                            var width = kpx.x(200)
//                            var height = width
//                            KPermissionUtils.requestPermissionsCanDrawOverlays {
//                                fixme 没有悬浮窗权限，也不会报错，也能正常允许，不影响实例化。只是悬浮窗无法显示而已。
//                                fixme 如果获取了悬浮窗权限，悬浮窗马上又会显示出来。关闭了权限，又会显示不见。
//                                fixme 所有实例化悬浮窗和获取权限，没有先后顺序。可以先实例化悬浮窗，再去请求权限。谁先谁后，都可以。
//                                KLoggerUtils.e("系统悬浮窗：\t" + it)
//                            }
//                            shadowView = KWindow.createView("id_window",width, height, y = kpx.statusHeight)?.apply {
//                                shadow {
//                                    all_radius(width)
//                                    bg_color=Color.parseColor("#DD5145")
//                                    shadow_color=bg_color
//                                }
//                                //拖动能力
//                                drag(parentHeight = kpx.maxScreenHeight()) {
//                                }
//                                onDoubleTap {
//                                    //双击，图片选择
//                                    pictrueSelector(1) {
//                                        shadowView?.apply {
//                                            autoBg {
//                                                autoBgFromFile(it[0])
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }

object KWindow {

    fun getContext(): Context {
        return KBaseApplication.getInstance()
    }

    /**
     * 获取窗体参数
     * @param width 宽度
     * @param height 高度
     * @param x 起始位置
     * @param y
     */
    fun getWindowParam(width: Int, height: Int, x: Int = 0, y: Int = 0): WindowManager.LayoutParams {
        var mWindowParams = WindowManager.LayoutParams()
        //不需要权限，只能悬浮在当前Activity上面，且WindowManager必须通过Activity获取，不能使用上下文。
        //mWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION
        if (Build.VERSION.SDK_INT >= 26) {//8.0新特性
            //8.0以下会崩溃，能够悬浮所有界面之上,需要悬浮窗权限。WindowManager可以通过上下文Context获取
            mWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            //8.0,已经弃用会直接崩溃。需要悬浮窗权限 <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
            mWindowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        //mWindowParams.flags = mWindowParams.flags or WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW//设置全屏，否则，始终在状态栏下面。（有Bug设置之后，会挡住下面的焦点点击事件,建议不要使用）
        //mWindowParams.flags = mWindowParams.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE// 不接受获取焦点事件,很重要。不然会阻塞下面的事件（控件外部区域也会挡住下面的事件）
        //下面配置的flags效果很好（不会在状态栏的下面，移动范围是整个屏幕），没有问题，就这样配置。
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        mWindowParams.flags = mWindowParams.flags or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS//允许窗口延伸到屏幕外。
        //只对 mWindowManager.addView(roundedImageView, mWindowParams);有效，即图片一开始出现时所展示的动画效果。
        //mWindowParams.windowAnimations = android.R.style.Animation_Toast;//若隐出现
        mWindowParams.windowAnimations = android.R.style.Animation_Translucent//位移一下
        // 以屏幕左上角为原点
        mWindowParams.gravity = Gravity.LEFT or Gravity.TOP
        //设置开始位置
        //mWindowParams.x = 0
        //mWindowParams.y = 0
        mWindowParams.x = x
        mWindowParams.y = y
        // 设置悬浮窗口长宽数据
        //mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        //mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        mWindowParams.width = width
        mWindowParams.height = height
        mWindowParams.format = PixelFormat.RGBA_8888
        return mWindowParams
    }

    fun getWindowManager(): WindowManager? {
        return getContext().getSystemService(WINDOW_SERVICE) as WindowManager
    }

    var windowViews = mutableListOf<KShadowView?>()//记录所有的悬浮控件
    private var windowMap = mutableMapOf<String, KShadowView?>()//防止相同键值key的悬浮窗重复实例化。

    fun createView(width: Int, height: Int, x: Int = 0, y: Int = 0): KShadowView? {
        return KWindow.createView(kpx.id().toString(), width, height, x, y)
    }

    /**
     * @param key 悬浮窗唯一标志，防止相同的悬浮窗重复实例化。
     * @param width 悬浮窗的宽度
     * @param height 高度
     * @param x 起始位置
     * @param y
     * 创建悬浮窗；每次都会创建一个新的控件；最好放在Application里面或主MainActivity里。
     * 需要悬浮窗权限哦。KPermissionUtils.requestPermissionsCanDrawOverlays可以判断权限。
     * fixme 没有悬浮窗权限，也不会报错，也能正常允许，不影响实例化。只是悬浮窗无法显示而已。
     * fixme 如果获取了悬浮窗权限，悬浮窗马上又会显示出来。关闭了权限，又会显示不见。
     * fixme 所有实例化悬浮窗和获取权限，没有先后顺序。可以先实例化悬浮窗，再去请求权限。谁先谁后，都可以。
     */
    fun createView(key: String?, width: Int, height: Int, x: Int = 0, y: Int = 0): KShadowView? {
        var key = key
        if (key == null) {
            key = kpx.id().toString()//生成随机键值
        }
        if (key != null && key.trim().length > 0 && windowMap.containsKey(key) && windowMap.get(key) != null) {
            return windowMap.get(key)//返回已经存在的悬浮窗，防止重复实例化。
        } else {
            var shadowView = KShadowView(getContext())
            var windowParam = getWindowParam(width, height, x, y)
            getWindowManager()?.addView(shadowView, windowParam)
            windowViews?.add(shadowView)
            if (key != null) {
                shadowView.windowKey = key
                windowMap?.put(key, shadowView)
            }
            return shadowView
        }
    }

    /**
     * 更新悬浮窗位置;(有点点延迟，没有普通view移动的顺畅和实时)
     */
    fun updateViewLayout(view: View?) {
        view?.let {
            getWindowManager()?.updateViewLayout(it, it.layoutParams)
        }
    }

    /**
     * 根据键key清除
     */
    fun removeView(key: String?) {
        if (key != null) {
            windowMap?.get(key)?.let {
                KWindow.removeView(key, it)
            }
        }
    }

    /**
     * 根据View,清除
     */
    fun removeView(view: KShadowView) {
        KWindow.removeView(view.windowKey, view)
    }

    /**
     * 清除悬浮窗
     */
    private fun removeView(key: String?, view: KShadowView) {
        windowViews?.remove(view)
        key?.let {
            windowMap?.remove(key)
        }
        getWindowManager()?.removeView(view)
    }

    //移除所有的悬浮窗
    fun clearAllView() {
        try {
            windowViews?.forEach {
                getWindowManager()?.removeView(it)
            }
            windowViews?.clear()
            windowMap?.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
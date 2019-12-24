package cn.oi.klittle.era.service

import android.app.Activity
import android.app.ActivityManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import cn.oi.klittle.era.base.KBaseActivityManager
import cn.oi.klittle.era.service.aidl.KAidlInterface
import cn.oi.klittle.era.utils.KLoggerUtils
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import java.util.concurrent.TimeUnit


/**
 * fixme 和应用同进程服务。
 * fixme startService()重复执行， 不会再执行onCreate()；只会执行onStartCommand()再执行onStart()方法；
 * fixme 重复绑定服务onBind()只会重复执行onServiceConnected()方法；因为每次bindService()都是新建一个连接绑定。
 * fixme startService()和bindService()可以同时使用。
 * fixme 即Service是单例的，不会存在两个同样的。所以不用怕重复调用。
 */
open class KService : Service() {

    companion object {

        private fun getActivity(): Activity? {
            return KBaseActivityManager.getInstance().stackTopActivity
        }

        private val action: String = "android.intent.action.kera.service.KService"
        private val serviceClassName = KService::class.java.name
        private val serviceClazz = KService::class.java

        /**
         * 开启服务
         * @param isRepeat 是否允许服务重复开启。true允许；false不允许重复开启（默认）。
         */
        fun startService(activity: Activity? = getActivity(), isRepeat: Boolean = false) {
            activity?.apply {
                if (!this.isFinishing) {
                    if (!isRepeat && isServiceExisted()) {
                        return
                    }
                    var mCompontName = ComponentName(this, serviceClazz)//上下文 ，Service类
                    var mIntent = Intent(action)
                    mIntent.setComponent(mCompontName)
                    startService(mIntent)
//                    if (Build.VERSION.SDK_INT >= 26) {
//                        //8.0 开启前台进程。优先级高。
//                        startForegroundService(mIntent)//fixme 建议不要使用，很容易崩溃。开启服务之后，不一会就崩溃了。
//                    } else {
//                        startService(mIntent)
//                    }
                }
            }
        }

        //关闭服务
        fun stopService(activity: Activity? = getActivity()): Boolean {
            activity?.apply {
                if (!this.isFinishing) {
                    var mCompontName = ComponentName(this, serviceClazz)//上下文 ，Service类
                    var mIntent = Intent(action)
                    mIntent.setComponent(mCompontName)
                    //stopService()成功执行返回true
                    return stopService(mIntent)
                }
            }
            return false
        }

//                    fixme bindService()调用案例(每一次bindService都是一次新的绑定。)
//                    KService.bindService(object : ServiceConnection {
//                        override fun onServiceDisconnected(name: ComponentName?) {
//                            //服务连接断开
//                        }
//
//                        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//                            //服务连接打开
//                            service?.let {
//                                //service转换为指定的Binder(即onBind()返回的IBinder对象)
//                                if (it is KBindder) {
//                                    //Activity和Service进行关联。即Activity调用Binder里面的方法。
//                                    var binder = it as KBindder
//                                }
//                            }
//                        }
//                    })

        private var serviceConnectiones = arrayListOf<ServiceConnection?>()
        //绑定服务；activity默认参数最好放在最后，方便调用。
        //每一次的bindService()都是一次新的连接;一个新的对象,一个新的绑定。
        fun bindService(serviceConnection: ServiceConnection, activity: Activity? = getActivity()) {
            activity?.apply {
                if (!this.isFinishing) {
                    serviceConnectiones.add(serviceConnection)
                    var mCompontName = ComponentName(this, serviceClazz)//上下文 ，Service类
                    var mIntent = Intent(action)
                    mIntent.setComponent(mCompontName)
                    //serviceConnection不可为空。
                    bindService(mIntent, serviceConnection, Context.BIND_AUTO_CREATE)//Context.BIND_AUTO_CREATE 是一个标志。
                }
            }
        }

        //解绑服务
        //bindService()和unbindServie()参数ServiceConnection必须要一致。不然奔溃
        //只有所有的binder都解除之后；服务才会结束。
        // stopService()和unbindService()都无效。必须关闭所有绑定的Activity
        fun unbindService(serviceConnection: ServiceConnection?, activity: Activity? = getActivity()) {
            activity?.apply {
                if (!this.isFinishing) {
                    if (serviceConnection != null) {
                        unbindService(serviceConnection)
                        serviceConnectiones.remove(serviceConnection)
                    }
                }
            }
        }

        //解除所有的绑定
        fun unbindAllService() {
            if (serviceConnectiones.size > 0) {
                unbindService(serviceConnectiones[0])
            }
            if (serviceConnectiones.size > 0) {
                unbindAllService()
            }
        }

        //判断当前Servicer是否已经开启；true开启；false为开启；fixme 亲测有效。
        fun isServiceExisted(activity: Activity? = getActivity(), className: String = serviceClassName): Boolean {
            activity?.apply {
                var activityManager = this
                        .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                //fixme 能够获取自己应用开启的Service;只能获取自身App的Service。其他应用的不行。
                //fixme 只要是应用本身的服务，哪怕是不同进程也能获取的到。能够获取本地应用的服务（不同进程也行）
                var serviceList = activityManager
                        .getRunningServices(Integer.MAX_VALUE)
                if (serviceList.size <= 0) {
                    return false
                }
                for (i in serviceList.indices) {
                    var serviceInfo = serviceList.get(i)
                    var serviceName = serviceInfo.service
                    if (serviceName.getClassName() == className) {
                        return true
                    }
                }
            }
            return false
        }


        /**
         * fixme 以下是生命周期回调方法。(如果在不同的进程，则无法回调；必须在同一进程回调函数才有效。)
         */
        private var onCreate: (() -> Unit)? = null

        /**
         * @param isRepeat 回调函数是否允许覆盖。true运行，false不允许（默认）
         */
        fun onCreate(isRepeat: Boolean = false, onCreate: (() -> Unit)?) {
            if (this.onCreate != null && !isRepeat) {
                return
            }
            this.onCreate = onCreate
        }

        private var onStartCommand: (() -> Unit)? = null

        fun onStartCommand(isRepeat: Boolean = false, onStartCommand: (() -> Unit)?) {
            if (this.onStartCommand != null && !isRepeat) {
                return
            }
            this.onStartCommand = onStartCommand
        }

        private var onStart: (() -> Unit)? = null

        fun onStart(isRepeat: Boolean = false, onStart: (() -> Unit)?) {
            if (this.onStart != null && !isRepeat) {
                return
            }
            this.onStart = onStart
        }

        private var onBind: (() -> Unit)? = null

        fun onBind(isRepeat: Boolean = false, onBind: (() -> Unit)?) {
            if (this.onBind != null && !isRepeat) {
                return
            }
            this.onBind = onBind
        }

        private var onRebind: (() -> Unit)? = null

        fun onRebind(isRepeat: Boolean = false, onRebind: (() -> Unit)?) {
            if (this.onRebind != null && !isRepeat) {
                return
            }
            this.onRebind = onRebind
        }

        private var onUnbind: (() -> Unit)? = null

        fun onUnbind(isRepeat: Boolean = false, onUnbind: (() -> Unit)?) {
            if (this.onUnbind != null && !isRepeat) {
                return
            }
            this.onUnbind = onUnbind
        }

        private var onDestroy: (() -> Unit)? = null

        fun onDestroy(isRepeat: Boolean = false, onDestroy: (() -> Unit)?) {
            if (this.onDestroy != null && !isRepeat) {
                return
            }
            this.onDestroy = onDestroy
        }

    }


    //首次启动的时候会执行，只执行一次
    override fun onCreate() {
        //KLoggerUtils.e("service=======================================================onCreate()")
        super.onCreate()
        onCreate?.let {
            it()
        }
    }

    //startService()会先执行onStartCommand()再执行onStart()方法。
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //KLoggerUtils.e("service=======================================================onStartCommand()")
        var b = super.onStartCommand(intent, flags, startId)
        onStartCommand?.let {
            it()
        }
        return b
    }

    override fun onStart(intent: Intent?, startId: Int) {
        //KLoggerUtils.e("service=======================================================onStart()")
        super.onStart(intent, startId)
        onStart?.let {
            it()
        }
    }

    var binder: KBindder? = null

    // bindService(Intent service, ServiceConnection conn, int flags)会调用onBind()方法。
    //第一次绑定bindService()的时候，会执行；其后再绑定只会执行onServiceConnected()方法
    //执行执行一次，重新绑定，不会再执行。
    override fun onBind(intent: Intent?): IBinder? {
        //KLoggerUtils.e("service=======================================================onBind()")
        if (binder == null) {
            binder = KBindder()
        }
        onBind?.let {
            it()
        }
        return binder//返回Binder子类，会调用onServiceConnected()方法。如果返回为null，则没有连接。则不会调用任何方法。
    }

    //fixme onRebind()执行条件，满足以下两个即可。
    //onUnBind方法返回值为true;默认的super.onUnbind(intent)是不行的。
    //服务对象被解绑后没有被销毁，之后再次被绑定
    override fun onRebind(intent: Intent?) {
        //KLoggerUtils.e("service=======================================================onRebind()")
        super.onRebind(intent)
        onRebind?.let {
            it()
        }
    }

    //bindService()和unbindServie()参数ServiceConnection必须要一致。不然奔溃
    //所有binder都断开的时候，才会执行onUnbind()
    override fun onUnbind(intent: Intent?): Boolean {
        //KLoggerUtils.e("service=======================================================onUnbind()")
        onUnbind?.let {
            it()
        }
        serviceConnectiones.clear()
        //return super.onUnbind(intent)
        return true//手动返回true;这样再次绑定的时候，才会执行onRebind()
    }

    //最后销毁的时候会执行（直接杀进程，则不会调用）。
    override fun onDestroy() {
        //KLoggerUtils.e("service=======================================================onDestroy()")
        onDestroy?.let {
            it()
        }
        serviceConnectiones.clear()
        binder = null
        onCreate = null
        onStartCommand = null
        onStart = null
        onBind = null
        onRebind = null
        onUnbind = null
        onDestroy = null
        super.onDestroy()
    }

}
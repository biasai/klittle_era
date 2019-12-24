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
import cn.oi.klittle.era.service.aidl.KAidlCallback
import cn.oi.klittle.era.service.aidl.KAidlInterface
import cn.oi.klittle.era.utils.KLoggerUtils
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import java.util.concurrent.TimeUnit


/**
 * fixme 跨进程服务
 * fixme startService()重复执行， 不会再执行onCreate()；只会执行onStartCommand()再执行onStart()方法；
 * fixme 重复绑定服务onBind()只会重复执行onServiceConnected()方法；因为每次bindService()都是新建一个连接绑定。
 * fixme startService()和bindService()可以同时使用。
 * fixme 即Service是单例的，不会存在两个同样的。所以不用怕重复调用。
 */
open class KAidlService : Service() {

    companion object {

//        fixme 因为是跨进程，所以这里是无法获取到Activity的。
//        private fun getActivity(): Activity? {
//            return KBaseActivityManager.getInstance().stackTopActivity
//        }

        private val action: String = "android.intent.action.kera.service.KAidlService"
        private val serviceClassName = KAidlService::class.java.name
        private val serviceClazz = KAidlService::class.java

        /**
         * 开启服务
         * @param isRepeat 是否允许服务重复开启。true允许；false不允许重复开启（默认）。
         */
        fun startService(activity: Activity?, isRepeat: Boolean = false) {
            activity?.apply {
                if (!this.isFinishing) {
                    if (!isRepeat && isServiceExisted(activity)) {
                        return
                    }
                    var mCompontName = ComponentName(this, serviceClazz)//上下文 ，Service类
                    var mIntent = Intent(action)
                    mIntent.setComponent(mCompontName)
                    startService(mIntent)
                }
            }
        }

        //关闭服务
        fun stopService(activity: Activity?): Boolean {
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

//                    fixme bindService()跨进程Service调用案例(每一次bindService都是一次新的绑定。)
//                    KAidlService.bindService(object : ServiceConnection {
//                        override fun onServiceDisconnected(name: ComponentName?) {
//                            //服务连接断开
//                            KLoggerUtils.e("服务断开")
//                        }
//
//                        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//                            //服务连接打开
//                            KLoggerUtils.e("服务连接")
//                            service?.let {
//                                //binder转换为aidl接口
//                                var aidl = KAidlInterface.Stub.asInterface(service)
//                                aidl.onMessage(object : KAidlCallback.Stub() {
//                                    override fun onMessage(msg: String?) {
//                                        //fixme 这里就是跨进程接口数据回调了。
//                                        KLoggerUtils.e("跨进程数据接收：\t"+msg)
//                                    }
//                                })
//                            }
//                        }
//                    })

        private var serviceConnectiones = arrayListOf<ServiceConnection?>()
        //绑定服务；activity默认参数最好放在最后，方便调用。
        //每一次的bindService()都是一次新的连接;一个新的对象,一个新的绑定。
        fun bindService(activity: Activity?, serviceConnection: ServiceConnection) {
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
        fun unbindService(activity: Activity?, serviceConnection: ServiceConnection?) {
            activity?.apply {
                if (!this.isFinishing) {
                    if (serviceConnection != null) {
                        unbindService(serviceConnection)
                        serviceConnectiones?.remove(serviceConnection)
                    }
                }
            }
        }

        //解除所有的绑定
        fun unbindAllService(activity: Activity?) {
            activity?.apply {
                if (!this.isFinishing) {
                    if (serviceConnectiones != null && serviceConnectiones.size > 0) {
                        serviceConnectiones?.let {
                            if (it.size > 0) {
                                unbindService(activity, it[0])
                            }
                        }
                        serviceConnectiones?.let {
                            if (it.size > 0) {
                                unbindAllService(activity)//fixme 闭合继续移除。
                            }
                        }
                    }
                }
            }

        }

        //判断当前Servicer是否已经开启；true开启；false为开启；fixme 亲测有效。
        fun isServiceExisted(activity: Activity?, className: String = serviceClassName): Boolean {
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
    }

    /**
     * fixme 获取当前进程Id(是当前调用者所在的进程)
     * fixme 作为普通方法，不要作为静态方法
     */
    fun getPid(): Int {
        return android.os.Process.myPid()
    }

    override fun onCreate() {
        super.onCreate()
//        KLoggerUtils.e("onCreate()\t服务进程：\t" + android.os.Process.myPid())
//        async {
//            for (i in 0..1000) {
//                KLoggerUtils.e("i:\t" + i)
//                delay(1000, TimeUnit.MILLISECONDS)
//            }
//        }
    }

    var aidlInterface: KAidlInterface.Stub? = null

    // bindService(Intent service, ServiceConnection conn, int flags)会调用onBind()方法。
    //第一次绑定bindService()的时候，会执行；其后再绑定只会执行onServiceConnected()方法
    override fun onBind(intent: Intent?): IBinder? {
        //KLoggerUtils.e("onBind()")
        if (aidlInterface == null) {
            aidlInterface = object : KAidlInterface.Stub() {
                var aidlCallback: KAidlCallback? = null
                //fixme 跨进程回调接口。
                override fun onMessage(callback: KAidlCallback?) {
                    this.aidlCallback = callback
                }

                //回调
                override fun setMessage(msg: String?) {
                    aidlCallback?.onMessage(msg)
                }
            }
        }

        //fixme 这里模拟数据回调
        //aidlInterface?.setMessage("")

//        async {
//            var count = 0
//            while (true) {
//                delay(1000)
//                aidlInterface?.setMessage((count++).toString())
//            }
//        }

        return aidlInterface//aidl可直接作为IBinder对象返回。
    }

    override fun onUnbind(intent: Intent?): Boolean {
        serviceConnectiones.clear()
        return super.onUnbind(intent)
    }

    /**
     * fixme 所有的ServiceConnection断开之后，会自动销毁。
     * fixme 当Activity销毁时，所创建的ServiceConnection也会自动断开。
     */
    override fun onDestroy() {
//        KLoggerUtils.e("onDestroy()")
        serviceConnectiones.clear()
        aidlInterface = null
        super.onDestroy()
        //因为是新开进程，所以在此杀进程（能够完全清除内存）
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
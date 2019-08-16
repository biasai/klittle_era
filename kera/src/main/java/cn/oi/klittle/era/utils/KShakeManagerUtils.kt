package cn.oi.klittle.era.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import cn.oi.klittle.era.base.KBaseApplication

//            调用案例（最后不用了，记得stop()关闭一下，很耗性能）
//                            KShakeManagerUtils.setShakeListener {
//                                KLoggerUtils.e("摇一摇")//每次摇动都会调用。
//                                if(true){
//                                    KShakeManagerUtils.stop()//如果调用完了，不用了，记得关闭；很耗性能
//                                }
//                            }

/**
 * fixme 摇一摇工具类（亲测有效，不需要任何权限。）
 */
object KShakeManagerUtils {
    private fun getContext(): Context {
        return KBaseApplication.getInstance()
    }

    private var shake = KShakeManager(getContext())

    private var shakeListener: (() -> Unit)? = null
    /**
     * @param speed fixme 这个值越大需要越大的力气来摇晃手机;即摇动的速度
     * @param shakeListener fixme 摇一摇监听（每次摇动都会调用，即摇多少次，就会调用多少次）
     */
    fun setShakeListener(speed: Int = 4500, shakeListener: (() -> Unit)? = null) {
        KShakeManager.SPEED_SHRESHOLD = speed
        this.shakeListener = shakeListener
        start()//开始
    }


    private var isStart = false//判断摇一摇是否开启
    /**
     * fixme 开始(设置监听的时候，会主动调用)
     */
    private fun start() {
        if (!isStart) {
            isStart = true
            shake.setShakeListener(object : KShakeManager.ShakeListener {
                override fun onShake() {
                    shakeListener?.let {
                        it()
                    }
                }
            })
            shake.start()
        }
    }

    /**
     * fixme 停止(最后不用的时候记得关闭);停止之後，需要重新设置监听，因为监听也会清空
     */
    fun stop() {
        if (isStart) {
            isStart = false
            shake.stop()
            shake.setShakeListener(null)
            this.shakeListener = null
        }
    }

    //内部类；摇一摇监听器
    private class KShakeManager(private val mContext: Context) : SensorEventListener {

        companion object {
            var SPEED_SHRESHOLD = 4500//这个值越大需要越大的力气来摇晃手机
        }

        private val UPTATE_INTERVAL_TIME = 50
        private var sensorManager: SensorManager? = null
        private var sensor: Sensor? = null
        private var shakeListener: ShakeListener? = null
        private var lastX: Float = 0.toFloat()
        private var lastY: Float = 0.toFloat()
        private var lastZ: Float = 0.toFloat()
        private var lastUpdateTime: Long = 0

        /**
         * 启动
         */
        fun start() {
            sensorManager = mContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            if (sensorManager != null) {
                sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            }
            if (sensor != null) {
                sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
            }
        }

        /**
         * 停止
         */
        fun stop() {
            sensorManager?.unregisterListener(this)
            sensorManager = null
        }

        /**
         * fixme 摇一摇监听（每次摇动都会调用，即摇多少次，就会调用多少次）
         *
         * @param shakeListener
         */
        fun setShakeListener(shakeListener: ShakeListener?) {
            this.shakeListener = shakeListener
        }

        interface ShakeListener {
            fun onShake()
        }

        override fun onSensorChanged(event: SensorEvent) {
            shakeListener?.let {
                val currentUpdateTime = System.currentTimeMillis()
                val timeInterval = currentUpdateTime - lastUpdateTime
                if (timeInterval < UPTATE_INTERVAL_TIME)
                    return
                lastUpdateTime = currentUpdateTime

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val deltaX = x - lastX
                val deltaY = y - lastY
                val deltaZ = z - lastZ

                lastX = x
                lastY = y
                lastZ = z

                val speed = Math.sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()) / timeInterval * 10000
                if (speed >= SPEED_SHRESHOLD) {
                    shakeListener?.onShake()
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    }
}
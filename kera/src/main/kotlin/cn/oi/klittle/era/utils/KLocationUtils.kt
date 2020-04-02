package cn.oi.klittle.era.utils

import android.app.Activity
import android.content.Context
import android.location.*
import android.os.Bundle
import cn.oi.klittle.era.base.KBaseActivityManager
import cn.oi.klittle.era.base.KBaseApplication
import cn.oi.klittle.era.comm.KToast
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.lang.Exception
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.Deferred


/**
 * 定位管理工具类；可以获取经度，纬度，或地理位置信息（精度不高，只能简单获取当前的位置）
 */
object KLocationUtils {
    private fun getContext(): Context {
        return KBaseApplication.getInstance()
    }

    private fun getActivity(): Activity? {
        return KBaseActivityManager.getInstance().stackTopActivity
    }

    //fixme 也不要置空，和LocationListener一样；防止置空之后，位置监听无法移除的问题。
    private var locationManager: LocationManager? = null

    private fun getLocationManager(): LocationManager? {
        if (locationManager == null) {
            locationManager = KBaseApplication.getInstance().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        return locationManager
    }

    //fixme 位置监听就使用这个，不要置空。防止移除位置监听的的时候无效。
    val mlocationListener: LocationListener? = object : LocationListener {
        override fun onLocationChanged(p0: Location?) {//定位改变监听
            //KLoggerUtils.e("定位改变监听:\t" + p0)
            locationListenerCallback?.let {
                it(p0)//位置实时监听
            }
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {//定位状态监听
            //KLoggerUtils.e("定位状态监听:\t" + p0)
        }

        override fun onProviderEnabled(p0: String?) {//定位状态可用监听(gps开启时调用)
            //KLoggerUtils.e("定位状态可用监听:\t" + p0)
        }

        override fun onProviderDisabled(p0: String?) {//定位状态不可用监听（gps关闭时）
            //KLoggerUtils.e("定位状态不可用监听:\t" + p0)
        }
    }

    fun getLocationListener(): LocationListener? {
        return mlocationListener
    }

    /**
     * fixme getLastKnownLocation获取的是最后一次(上一次)缓存的位置(不准确)。第一次获取可能为空。
     * 获取当前位置的经纬度；如： 经度：	121.69989298	纬度：	29.8759242
     * location.longitude//经度
     * location.latitude//纬度
     */
    fun getLocation(activity: Activity? = getActivity(), callbak: ((location: Location?) -> Unit)? = null) {
        activity?.apply {
            if (KIntentUtils.isGpsEnable()) {//fixme 需要打开gps定位服务
                KPermissionUtils.requestPermissionsLocation(activity) {
                    try {
                        //fixme 需要定位权限
                        if (it) {
                            var locationManager: LocationManager? = getLocationManager()
                            if (locationManager == null) {
                                return@requestPermissionsLocation
                            }
                            locationManager?.removeUpdates(getLocationListener())//调用之前移除一下。
                            //fixme 地理位置刷新一下（提高一下准确度）
                            //provider 定位方式;
                            //minTime 最短时间间隔（单位毫秒(亲测)，1000是一秒。）;
                            //minDistance最短更新距离
                            //fixme requestLocationUpdates 会不停的请求，minTime是请求的间隔时间。
                            //locationManager?.requestLocationUpdates("gps", 0, 0f, getLocationListener())//请求获取最新位置信息（getLastKnownLocation获取的是上一次定位）
                            //locationManager?.requestLocationUpdates("network", 0, 0f, getLocationListener())
                            //fixme requestSingleUpdate只请求一次。
                            locationManager?.requestSingleUpdate("gps", getLocationListener(), null)//请求获取最新位置信息（getLastKnownLocation获取的是上一次定位）
                            locationManager?.requestSingleUpdate("network", getLocationListener(), null)
                            var criteria = Criteria()
                            // 获得最好的定位效果
                            criteria.setAccuracy(Criteria.ACCURACY_FINE)
                            criteria.setAltitudeRequired(false)
                            criteria.setBearingRequired(false)
                            criteria.setCostAllowed(false)
                            // 使用省电模式
                            criteria.setPowerRequirement(Criteria.POWER_LOW)
                            // 获得当前的位置提供者
                            var provider = locationManager?.getBestProvider(criteria, true)//有 network 和gps
                            // 获得当前的位置;fixme getLastKnownLocation获取的是最后一次(上一次)缓存的位置(不准确)。第一次获取可能为空。
                            //该方法不会发起监听，返回的是上一次的位置信息，但此前如果没有位置更新的话，返回的位置信息可能是错误的；
                            var location: Location? = locationManager?.getLastKnownLocation(provider)
                            if (location == null) {
                                provider = locationManager?.getProvider(LocationManager.GPS_PROVIDER)?.getName()//gps
                                if (provider != null) {
                                    location = locationManager?.getLastKnownLocation(provider)
                                }
//                            if (location == null) {
//                                provider = locationManager?.getProvider(LocationManager.NETWORK_PROVIDER).getName()//network fixme 这个可能会异常报错(不要用)
//                                location = locationManager?.getLastKnownLocation(provider)
//                            }
                                if (location == null) {
                                    var providers = locationManager?.getProviders(true)
                                    if (providers != null) {
                                        for (provider in providers) {
                                            var l = locationManager?.getLastKnownLocation(provider);
                                            if (l == null) {
                                                continue
                                            }
                                            if (location == null || l.getAccuracy() < location.getAccuracy()) {
                                                // Found best last known location: %s", l);
                                                location = l;
                                            }
                                        }
                                    }
                                }
                            }
                            locationManager = null
                            //location.longitude//经度
                            //location.latitude//纬度
                            //KLoggerUtils.e("location:\t" + location + "\tprovider\t" + provider + "\tcallbak:\t" + callbak)
                            callbak?.let {
                                it(location)//fixme 返回如果为空，GPS可能没有打开，也可能在室内(GPS信号弱，获取不到位置)，没有在室外。
                            }
                        } else {
                            //fixme 没有定位权限
                            callbak?.let {
                                it(null)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                //fixme 没有开启定位服务
                callbak?.let {
                    it(null)//返回为空，肯定没有打开gps定位服务，调用KIntentUtils.goGPS()跳转gps服务设置界面，手动打开。
                }
            }
        }

    }

    private var locationListenerCallback: ((location: Location?) -> Unit)? = null
    /**
     * fixme 定位实时监听位置；需要手动调用KLocationUtils.removeUpdates()移除监听。不然会一直监听。
     */
    fun requestLocationUpdates(activity: Activity? = getActivity(), locationListener: ((location: Location?) -> Unit)? = null) {
        activity?.apply {
            if (KIntentUtils.isGpsEnable()) {//fixme 需要打开gps定位服务
                KPermissionUtils.requestPermissionsLocation(activity) {
                    try {
                        //fixme 需要定位权限
                        if (it) {
                            locationListenerCallback = locationListener//监听器
                            var locationManager: LocationManager? = getLocationManager()
                            if (locationManager == null) {
                                return@requestPermissionsLocation
                            }
                            locationManager?.removeUpdates(getLocationListener())//fixme 调用之前移除一下之前的监听。
                            //fixme 地理位置刷新一下（提高一下准确度）
                            //provider 定位方式;
                            //minTime 最短时间间隔（单位毫秒(亲测)，1000是一秒。）;
                            //minDistance最短更新距离(单位米)
                            //fixme requestLocationUpdates 会不停的请求，minTime是请求的间隔时间。
                            locationManager?.requestLocationUpdates("gps", 0, 0f, getLocationListener())//请求获取最新位置信息（getLastKnownLocation获取的是上一次定位）
                            locationManager?.requestLocationUpdates("network", 0, 0f, getLocationListener())
                            //fixme GPS定位：在室内开发时，你的手机根本就没法获取位置信息。只有在室外才有效(亲测有效，快的话一秒，慢的话要几十秒。)。
                            locationManager?.requestSingleUpdate(LocationManager.GPS_PROVIDER, getLocationListener(), null)
                            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, getLocationListener())
                            //fixme 网络定位，需要服务商的支持。一般的手机一般都自带百度地图的服务(谷歌的用不了在国内)。（如果没有服务，那网络定位是无效的。百度地址的SDK就解决了这个问题）
                            //fixme 新版的PDA上面有百度的服务,网络位置("com.baidu.map.location");旧版本的没有，所以旧版本无法进行网络定位。
                            locationManager?.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, getLocationListener(), null)
                            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, getLocationListener())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    //被观察者
    private var observable: Observable<Boolean>? = Observable.create<Boolean> {
        if (requestLocationUpdate_timeOut > 0) {
            GlobalScope.async {
                delay(requestLocationUpdate_timeOut)//等待中
                it.onComplete()//超时回调
            }
        }
    }
            .subscribeOn(Schedulers.io())//执行线程在io线程(子线程)
            .observeOn(AndroidSchedulers.mainThread())//回调线程在主线程


    var disposable: Disposable? = null
    //观察者
    private var observe: Observer<Boolean>? = object : Observer<Boolean> {
        override fun onSubscribe(d: Disposable?) {
            disposable = d
        }

        override fun onComplete() {
            //超时回调
            locationListener?.let {
                it(null)//超时位置返回为空
            }
            locationListener = null//回调监听置空
            if (isRequestLocationUpdate) {
                isRequestLocationUpdate = false//只请求一次，要移除监听
                removeUpdates()//fixme 超时也要立即移除监听。
            }
        }

        override fun onNext(value: Boolean?) {
        }

        override fun onError(e: Throwable?) {
        }
    }

    //旧的回调取消
    private fun dispose() {
        try {
            //使用RxJava完成弹框超时设置。亲测可行。
            if (observe != null) {//timeOut小于等于0不做超时判断
                disposable?.dispose()//旧的回调取消
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //开启新的回调计时
    private fun subscribe() {
        try {
            //使用RxJava完成弹框超时设置。亲测可行。
            if (observe != null) {//timeOut小于等于0不做超时判断
                observable?.subscribe(observe)//开启新的回调计时
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var requestLocationUpdate_timeOut: Long = 10000//超时回调时间10秒。（单位是毫秒）
    private var requestLocationUpdate_timeOut_default: Long = 10000//超时回调时间10秒（默认参数设置）。（单位是毫秒）
    private var locationListener: ((location: Location?) -> Unit)? = null//回调监听
    private var isRequestLocationUpdate = false//是否只请求一次
    /**
     * fixme 实时监听位置一次(只监听一次。)；正常的一般两三秒就回调了。最迟不会超过10秒。如果超过10秒，基本都是获取不到GPS(比如在室内且网络定位功能没有开启)。
     * @param timeOut fixme 回调超时时间(亲测有效,亲测不会错乱。超时时会回调为空null)
     * @param locationListener 回调监听;fixme 监听完一次之后，会自动移除位置监听。
     */
    fun requestLocationUpdate(activity: Activity? = getActivity(), timeOut: Long = requestLocationUpdate_timeOut_default, locationListener: ((location: Location?) -> Unit)? = null) {
        this.requestLocationUpdate_timeOut = timeOut
        this.locationListener = locationListener
        dispose()//旧的回调取消
        subscribe()//新的回调重新开始计时
        isRequestLocationUpdate = true
        requestLocationUpdates {
            var location = it
            locationListener?.let {
                if (this.locationListener != null) {
                    this.locationListener = null//回调监听置空。
                    dispose()//旧的回调取消
                    it(location)
                    removeUpdates()//fixme 监听完成之后，立即移除监听。
                    isRequestLocationUpdate = false
                }
            }
        }
    }

    //fixme 移除位置监听（亲测有效）
    fun removeUpdates(activity: Activity? = getActivity()) {
        try {
            locationListenerCallback = null//监听置空
            locationListener = null
            dispose()//旧的回调取消
            if (mlocationListener != null) {
                activity?.apply {
                    if (mlocationListener != null) {
                        var locationManager: LocationManager? = getLocationManager()
                        locationManager?.removeUpdates(mlocationListener)//异常监听
                        locationManager = null
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

//    fixme 调用案例           只能简单的获取当前的位置，精度并不高。
//                                KLocationUtils.getAdress {
//                                it?.getAddressLine(0)//浙江省宁波市北仑区805县道
//                                it?.locality//宁波市
//                                it?.featureName//805县道
//                                it?.countryName//是null空的
//                            }

    /**
     * 获取地理位置
     * @param location 经纬度坐标信息，如果为空。默认获取当前位置的经纬度
     */
    fun getAdress(activity: Activity? = getActivity(), location: Location? = null, callbak: ((adress: Address?) -> Unit)? = null) {
        if (activity != null) {
            if (location != null) {
                var adress: Address? = getAdress(activity, location)
                callbak?.let {
                    it(adress)
                }
            } else {
                getLocation {
                    var adress: Address? = getAdress(activity, it)
                    callbak?.let {
                        it(adress)
                    }
                }
            }
        }
    }

    private fun getAdress(activity: Activity? = getActivity(), location: Location?): Address? {
        if (activity == null || location == null) {
            return null
        }
        val gc = Geocoder(activity)
        var addresses: List<Address>? = null
        try {
            addresses = gc.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1)
            if (addresses!!.size > 0) {
                //  fixme 这个只能简单的获取当前的位置，精度并不高。
                //	AddressLine：浙江省宁波市北仑区805县道   addresses[0].getAddressLine(0)
                //  CountryName：null                        addresses[0].getCountryName()
                //  Locality：宁波市                         addresses[0].getLocality()
                //  FeatureName：805县道                     addresses[0].getFeatureName()
                return addresses[0]
            }
        } catch (e: Exception) {
        }
        return null
    }

}
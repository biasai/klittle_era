package cn.oi.klittle.era.utils

import android.app.Activity
import android.content.Context
import android.location.*
import android.os.Bundle
import cn.oi.klittle.era.base.KBaseActivityManager
import cn.oi.klittle.era.base.KBaseApplication
import kotlinx.coroutines.experimental.async
import java.lang.Exception


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

    var mlocationListener: LocationListener? = null

    fun getLocationListener(): LocationListener? {
        if (mlocationListener == null) {
            mlocationListener = object : LocationListener {
                override fun onLocationChanged(p0: Location?) {//定位改变监听
                    KLoggerUtils.e("定位改变监听:\t" + p0)
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
        }
        return mlocationListener
    }

    /**
     * fixme 注意，需要定位服务，一般手机都自带有服务。如果是谷歌服务可能就用不了，会返回空。
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
                            var locationManager: LocationManager? = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                            if (locationManager == null) {
                                return@requestPermissionsLocation
                            }
                            //fixme 地理位置刷新一下（提高一下准确度）
                            //provider 定位方式;
                            //minTime 最短时间间隔（单位毫秒(亲测)，1000是一秒。）;
                            //minDistance最短更新距离
                            locationManager?.requestLocationUpdates("gps", 0, 0f, getLocationListener())//请求获取最新位置信息（getLastKnownLocation获取的是上一次定位）
                            locationManager?.requestLocationUpdates("network", 0, 0f, getLocationListener())

                            val criteria = Criteria()
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
                            locationManager?.removeUpdates(mlocationListener)//fixme 移除位置监听
                            mlocationListener = null
                            locationManager = null
                            //location.longitude//经度
                            //location.latitude//纬度
                            //KLoggerUtils.e("location:\t" + location + "\tprovider\t" + provider + "\tcallbak:\t" + callbak)
                            callbak?.let {
                                it(location)
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
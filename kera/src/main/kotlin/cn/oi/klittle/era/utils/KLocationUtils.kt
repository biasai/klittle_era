package cn.oi.klittle.era.utils

import android.app.Activity
import android.content.Context
import android.location.*
import cn.oi.klittle.era.base.KBaseActivityManager
import cn.oi.klittle.era.base.KBaseApplication
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


    /**
     * 获取当前位置的经纬度；如： 经度：	121.69989298	纬度：	29.8759242
     * location.longitude//经度
     * location.latitude//纬度
     */
    fun getLocation(activity: Activity? = getActivity(), callbak: ((location: Location?) -> Unit)? = null) {
        activity?.apply {
            if (KIntentUtils.isGpsEnable()) {//fixme 需要打开gps定位服务
                KPermissionUtils.requestPermissionsLocation(activity) {
                    //fixme 需要定位权限
                    if (it) {
                        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        val criteria = Criteria()
                        // 获得最好的定位效果
                        criteria.setAccuracy(Criteria.ACCURACY_FINE)
                        criteria.setAltitudeRequired(false)
                        criteria.setBearingRequired(false)
                        criteria.setCostAllowed(false)
                        // 使用省电模式
                        criteria.setPowerRequirement(Criteria.POWER_LOW)
                        // 获得当前的位置提供者
                        val provider = locationManager?.getBestProvider(criteria, true)
                        // 获得当前的位置
                        var location: Location? = locationManager?.getLastKnownLocation(provider)
                        //location.longitude//经度
                        //location.latitude//纬度
                        callbak?.let {
                            it(location)
                        }
                    } else {
                        //fixme 没有定位权限
                        callbak?.let {
                            it(null)
                        }
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
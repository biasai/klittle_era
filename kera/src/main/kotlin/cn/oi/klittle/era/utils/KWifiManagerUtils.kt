package cn.oi.klittle.era.utils

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import cn.oi.klittle.era.base.KBaseActivityManager
import cn.oi.klittle.era.base.KBaseApplication
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import java.lang.Exception
import java.util.concurrent.TimeUnit

/**
 * fixme wifi管理工具类、wifi权限无法完全获取。
 * fixme 断开，禁用。开关wifi；每次操作，系统都会跳出wifi权限询问框。
 *
 * fixme SSID 是wifi名称；BSSID 姑且理解成热点的mac地址，但实际有所不同;level 描述wifi信号强弱的值(格式如:RSSI:-67),貌似数字越小越强
 * fixme 每一个WifiConfiguration都有一个netWorkId；scanResult扫描类是没有netWorkId的。需要注意的是netWorkId可能会变，所有要实时获取。
 *
 * fixme 获取wifi密码。就不要瞎搞了，需要root系统权限哦。
 * fixme 系统对应用的权限太低了。对wifi的操作很有限。有时都不一定能自动连接上。最好交给用户手动去连接。
 *
 * fixme 多个设备产生相同的网络，如SmartAP,手机上只会显示一个，手机默认他们是同一个网络(netWorkId也是同样的)。并且都能连接上。
 * fixme 注意如果连接的网络无法访问外网，部分手机可能会自动断开。（最好交给用户手动去连，用户手动去连则不会断开。）,这都是系统的坑。
 *
 */
object KWifiManagerUtils {
    private fun getContext(): Context {
        return KBaseApplication.getInstance().applicationContext
    }

    private fun getActivity(): Activity? {
        return KBaseActivityManager.getInstance().stackTopActivity
    }

    var kWifiManager: WifiManager = getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
    var kwifiBroadCastReceiver: KWifiBroadCastReceiver? = null

    init {
        try {
            //打开wifi，最好交给用户自己去开。不用擅自去开，用户体验不好
            //kWifiManager.setWifiEnabled(true)//开启wifi,不代表wifi状态的变化（切记！）虽然代码简单，但是wifi的开启关闭过程是有延时性的
            kwifiBroadCastReceiver = KWifiBroadCastReceiver()
            val filter = IntentFilter()
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)//wifi开关变化通知
            //filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)//wifi扫描结果通知
            filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)//wifi连接结果通知 fixme 基本上只需要监听这个。就够了。其他的太频繁了。
            //filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)//网络状态变化通知
            getContext().registerReceiver(kwifiBroadCastReceiver, filter)//注册广播
        } catch (e: Exception) {
            KLoggerUtils.e("TEST", "广播监听注册异常：\t" + e.message)
        }
    }

    /**
     * fixme true打开wifi;false关闭wifi;这个系统自己会弹出wifi权限申请框。每次打开或关闭。都会去询问。无法获得永久的权限(除非root过，root过的手机不会一直弹。)。
     * fixme 亲测有效。可以打开或关闭wifi
     */
    fun setWifiEnabled(isEnabled: Boolean, callback: (() -> Unit)? = null) {
        if (isEnabled) {
            if (isWifiEnabled()) {
                callback?.let { it() }//wifi已经打开
            } else {
                kwifiBroadCastReceiver?.openCallback(callback)
                kWifiManager.setWifiEnabled(true)
            }
        } else {
            if (!isWifiEnabled()) {
                callback?.let { it() }//wifi已经关闭
            } else {
                kwifiBroadCastReceiver?.shutCallback(callback)
                kWifiManager.setWifiEnabled(false)
            }
        }
    }

    /**
     * fixme 判断wifi是否开启
     */
    fun isWifiEnabled(): Boolean {
        if (kWifiManager?.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            return true//开启
        }
        return false//为开启
    }

    /**
     * fixme 扫描附近的wifi（需要定位权限，wifi必须开启），如果返回为空null,手动判断wifi是否开启，如果开启，那么肯定是没有定位权限。
     * ScanResult 是扫描结果类，其中 SSID 是wifi名称；BSSID 姑且理解成热点的mac地址，但实际有所不同;level 描述wifi信号强弱的值(格式如:RSSI:-67),貌似数字越小越强
     * 扫描类，是无法获取netWorkId的
     */
    fun startScan(activity: Activity? = getActivity(), callback: ((it: MutableList<ScanResult>?) -> Unit)? = null) {
        if (isWifiEnabled()) {//需要开启wifi
            //需要定位权限
            KPermissionUtils.requestPermissionsLocation(activity) {
                if (it) {
                    kWifiManager?.startScan()
                    var results = kWifiManager?.scanResults
                    callback?.let {
                        it(results)
                    }
                } else {
                    callback?.let {
                        it(null)
                    }
                }
            }
        } else {
            callback?.let {
                it(null)
            }
        }
    }

    //获取曾经连接过的wifi信息。能够获取netWorkId。fixme 注意wifi名SSID自带双引号""
    fun getConfiguredNetworks(): MutableList<WifiConfiguration> {
        return kWifiManager?.configuredNetworks
    }

    /**
     * 根据wifi名SSID查询，曾经是否配置过该网络。返回为空表示没有配置过。
     * @param ssid wifi名。带不带双引号无所谓！会自动去除
     */
    fun getConfiguredNetwork(ssid: String): WifiConfiguration? {
        var SSID = KStringUtils.removeMarks(ssid)//去除双引号
        var list = mutableListOf<WifiConfiguration>()//fixme 一般都不会重复，但以防万一。防止搜索出多个。
        getConfiguredNetworks().forEach {
            var itSSID = KStringUtils.removeMarks(it.SSID)//去除双引号。
            if (itSSID.equals(SSID) && it.networkId > 0) {
                list.add(it)
            }
        }
        if (list.size > 1) {
            //搜索出多个
            var index = 0;//记录选中的下标
            var networkId = list[0].networkId
            for (i in 0..list.lastIndex) {
                if (list[i].networkId > networkId) {
                    networkId = list[i].networkId
                    index = i
                }
            }
            return list[index]//选出id最大的，最新的。（id大的说明是新加的。）
        } else if (list.size == 1) {
            //就搜索到一个
            return list[0]
        }
        return null//一个都没有
    }

    /**
     * 获取当前链接的wifi信息,如果没有连接会返回null。fixme 获取当前wifi信息，不需要权限。扫描需要权限。
     * ssid wifi名，注意这个自带双引号""
     * bssid 地址
     * networkId 数字型的id （已经连上的wifi是可以或的netWorkId的。）
     * rssi wifi强度，即level
     */
    fun getConnectionInfo(): WifiInfo? {
        if (isWifiEnabled()) {
            return kWifiManager?.getConnectionInfo()
        }
        return null
    }

    //获取当前连接wifi的名称
    fun getConnectionInfoSSID(): String? {
        getConnectionInfo()?.let {
            //去除字符串双引号,去除空格
            return KStringUtils.removeMarks(it.ssid).trim()
        }
        return null
    }

    //获取Ip4地址，格式如：10.10.100.100 和KIpPort.getHostIp4()一样。获取的是手机自己的Ip4地址。
    fun getIpAddress(): String? {
        var ipAddress = getConnectionInfo()?.ipAddress
        ipAddress?.let {
            var inetAddress = KInetAddressUtils.intToInetAddress(it)
            return inetAddress.hostAddress
        }
        return null
    }

    /**
     * 根据AccessPoint获取网络id
     */
    fun getNetWorkId(ap: AccessPoint): Int {
        var networkId = getNetWorkId(ap.ssid)//fixme 判断是否已经存在。防止重复添加！
        if (networkId < 0) {
            var config = createConfiguration(ap)
            //fixme 如果你设置的wifi，设备已经存储过了，那么这个networkId会返回小于0的值。一般都是-1
            networkId = kWifiManager?.addNetwork(config)//添加一个wifi配置，并返回networkId
        }
        return networkId
    }

    /**
     * 根据wifi名称获取网络id。如果不存在则返回-1
     */
    fun getNetWorkId(ssid: String): Int {
        var wifiConfiguration = getConfiguredNetwork(ssid)
        wifiConfiguration?.let {
            return it.networkId
        }
        return -1
    }

    /**
     * 根据wifi名称和密码；获取网络id。如果不存在则返回-1（小于0的数）（不存在时会自动创建和添加该网络，如果返回为负数。一般都是用户拒绝了扫描定位权限）
     * @param ssid WiFi名称
     * @param password 密码，没有密码，可以传空null
     * @param callback 回调返回netWorkId;因为扫描是异步，为了同步，所以使用回调
     */
    fun getNetWorkId(ssid: String, password: String?, callback: ((netWorkId: Int) -> Unit)) {
        var networkId = -1
        var wifiConfiguration = getConfiguredNetwork(ssid)
        wifiConfiguration?.let {
            networkId = it.networkId
            if (password == null && removeNetWorkId(networkId)) {
                networkId = -1///对已经存在的网络配置进行删除，删除成功了再重新配置（增大连接成功的概率）。一般只能删除自己创建的网络配置。
            }
        }
        if (networkId < 0) {//fixme 判断是否存在该网络，防止重复添加。
            startScan {
                it?.forEach {
                    if (it.SSID.equals(ssid)) {
                        //fixme 未配置过该网络，不存在。则重新创建该网络配置
                        var configuration = createConfiguration(AccessPoint(it, password))
                        if (configuration != null) {
                            networkId = kWifiManager.addNetwork(configuration)//fixme 如果已经存在，则会返回小于0的数字。所以自己创建的wifi最好自己手动删除！
                            callback(networkId)
                        }
                        return@startScan//直接跳出循环，不会再执行startScan()方法
                    }
                }
                callback(networkId)
            }
        } else {
            callback(networkId)
        }
    }

    /**
     * 根据网络id;删除指定的网络。
     * fixme 注意：只能删除自己添加的网络。即：networkId = kWifiManager.addNetwork(configuration) 只能删除自己的。
     * fixme 别的应用或系统创建的网络，6.0以上是无法删除的。只能删除自己创建的。
     */
    fun removeNetWorkId(networkId: Int): Boolean {
        var b = kWifiManager.removeNetwork(networkId)
        kWifiManager.saveConfiguration()
        return b//true删除成功；false删除失败
    }

    /**
     * 更加ssid 删除指定网络（只能删除自己的网络配置）
     */
    fun removeNetWorkId(ssid: String): Boolean {
        var networkId = getNetWorkId(ssid)
        if (networkId >= 0) {
            var b = kWifiManager.removeNetwork(networkId)
            kWifiManager.saveConfiguration()
            return b//true删除成功；false删除失败
        } else {
            return true//没有该网络，也表示删除成功
        }
    }

    //连接网络
    fun enableNetwork(accessPoint: AccessPoint, callback: ((isSuccess: Boolean) -> Unit)? = null) {
        enableNetwork(accessPoint.networkId, callback)
    }

    //根据netWorkId进行wifi连接。回调返回是否连接成功。
    fun enableNetwork(networkId: Int, callback: ((isSuccess: Boolean) -> Unit)? = null) {
        if (networkId >= 0) {
            //KLoggerUtils.e("当前wifi id:\t" + getConnectionInfo()?.networkId + "\t要连接的网络：\t" + networkId)
            if (getConnectionInfo()?.networkId != networkId) {
                //先断开现有连接。然后再连接（这样才能增大连接的概率）。
                disconnect {
                    kwifiBroadCastReceiver?.connetCallBack {
                        var wifiInfo = getConnectionInfo()
                        if (wifiInfo != null) {
                            //KLoggerUtils.e("TEST", "当前连接的netWorkId:\t" + it.networkId)
                            if (wifiInfo.networkId == networkId && wifiInfo.networkId >= 0) {
                                callback?.let {
                                    it(true)//连接成功
                                }
                            } else {
                                callback?.let {
                                    it(false)//连接失败
                                }
                            }
                        } else {
                            callback?.let {
                                it(false)//连接失败
                            }
                        }
                    }
                    kWifiManager?.enableNetwork(networkId, true)
                    kWifiManager?.saveConfiguration()
                    kWifiManager?.reconnect()
                }
            } else {
                callback?.let {
                    it(true)//已经连接上
                }
            }
        } else {
            callback?.let {
                it(false)//无效的网络id
            }
        }
    }

    /**
     * 简化；根据ssid和密码去连接指定wifi;fixme 注意如果连接的网络无法访问外网，部分手机可能会自动断开。（最好交给用户手动去连，用户手动去连则不会断开。）
     * @param ssid wifi名称
     * @param password 密码（没有密码可以为空null）
     * @param callback 回调
     */
    fun enableNetwork(ssid: String, password: String? = null, callback: ((isSuccess: Boolean) -> Unit)? = null) {
        getNetWorkId(ssid, password) {
            if (it > 0) {
                enableNetwork(it, callback)
            } else {
                callback?.let {
                    it(false)//连接失败
                }
            }
        }
    }

    //fixme 连接断开,断开之后，会回调。亲测有效。能够断开网络（不过断开之后，手机一般都会重新连接。）
    fun disconnect(callback: (() -> Unit)? = null) {
        kwifiBroadCastReceiver?.connetCallBack(callback)
        kWifiManager?.disconnect()
    }

    /**
     * fixme 根据wifi名，禁止掉特定的网络。防止重连。注意会跳出wifi权限询问框。
     */
    fun disableNetwork(ssid: String) {
        var SSID = KStringUtils.removeMarks(ssid)
        getConfiguredNetworks().forEach {
            var itSSID = KStringUtils.removeMarks(it.SSID)
            if (itSSID.equals(SSID)) {
                kWifiManager.disableNetwork(it.networkId)//fixme 禁掉指定的网络。会跳wifi权限询问框。(ROOT过的手机则不会)
                return
            }
        }
    }

    /**
     * fixme 禁止所有已经连接过的网络。注意会跳出wifi权限询问框。而且是循环的弹出。建议不要用（除非手机root过）。
     */
    fun disableAllNetwork() {
        //fixme 以下循环，每循环一次，都会跳出询问框。非常不友好。
        getConfiguredNetworks().forEach {
            kWifiManager.disableNetwork(it.networkId)//禁掉所有的网络，防止主动连接。会跳出wifi权限询问框
        }
    }

    //重新实例化一个AccessPoint
    fun getAccessPoint(scanResult: ScanResult?, password: String = ""): AccessPoint {
        return AccessPoint(scanResult, password)
    }

    /**
     * @param scanResult wifi扫描类
     * @param password WiFi密码
     */
    open class AccessPoint(var scanResult: ScanResult?, var password: String? = null) {
        var networkId: Int = -1//id
        var ssid: String = ""//wifi名称
        var capabilities: String = ""//加密方式;一种是WEP，一种是WPA，还有没有密码的情况
        var security: String = ""//capabilities的小写格式。

        init {
            scanResult?.let {
                ssid = it.SSID
                capabilities = it.capabilities
                security = capabilities.trim().toLowerCase()
                networkId = getNetWorkId(this)
            }
        }
    }

    //创建一个wifi配置。
    fun createConfiguration(ap: AccessPoint): WifiConfiguration {
        var SSID = ap.ssid
        var config = WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.priority = Int.MAX_VALUE
        config.hiddenSSID = true;
        config.SSID = "\"" + SSID + "\"";//fixme 这里的SSID的带双引号的。

        var security = ap.security
        var password = ap.password
        if ((security?.contains("wep") ?: false) && password != null) {
            //wep必须根据密码长度进行特殊处理
            var i = password.length
            if (((i == 10 || (i == 26) || (i == 58))) && (password.matches("[0-9A-Fa-f]*".toRegex()))) {
                config.wepKeys[0] = password;
            } else {
                config.wepKeys[0] = "\"" + password + "\"";//fixme 密码也带双引号
            }
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (security.contains("wpa")) {//fixme 基本上，大多数都是这个。
            config.preSharedKey = "\"" + password + "\"";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            // 此处需要修改否则不能自动重联
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;

        } else {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        return config;
    }

    var mConnetCallBack: ((ssid: String?) -> Unit)? = null
    //fixme 连接状态变化监听;不会主动制空；一直都会有回调;返回当前连接的wifi名称，如果为空则表示没有连接
    fun setConnetCallBack(mConnetCallBack: ((ssid: String?) -> Unit)? = null) {
        this.mConnetCallBack = mConnetCallBack
    }

    /**
     * fixme 广播监听(主要用户回调)。
     */
    class KWifiBroadCastReceiver : BroadcastReceiver() {
        var openCallback: (() -> Unit)? = null//wifi打开回调
        var shutCallback: (() -> Unit)? = null//wifi关闭回调
        var connetCallBack: (() -> Unit)? = null//wifi连接回调true连接成功，false连接失败
        fun openCallback(openCallback: (() -> Unit)?) {
            this.openCallback = openCallback
        }

        fun shutCallback(shutCallback: (() -> Unit)?) {
            this.shutCallback = shutCallback
        }

        fun connetCallBack(connetCallBack: (() -> Unit)?) {
            this.connetCallBack = connetCallBack
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (it.action == WifiManager.WIFI_STATE_CHANGED_ACTION) {
                    //fixme wifi开关变化通知
                    var wifiState = it.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED)
                    if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                        //wifi关闭
                        if (shutCallback != null) {
                            shutCallback?.let {
                                async {
                                    shutCallback = null//回调一次即可。防止重复回调。不会影响下面的it()
                                    delay(500, TimeUnit.MILLISECONDS)//fixme wifi开关需要时间，所以延迟一哈。时间间隔500毫秒即可，太大了也不行。
                                    it()
                                }
                            }
                        }
                    } else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                        //wifi开启
                        if (openCallback != null) {
                            openCallback?.let {
                                async {
                                    openCallback = null//回调一次即可。防止重复回调。不会影响下面的it()
                                    delay(500, TimeUnit.MILLISECONDS)
                                    it()
                                }
                            }
                        }
                    }
                } else if (it.action == WifiManager.SUPPLICANT_STATE_CHANGED_ACTION) {
                    //wifi连接结果通知
                    //fixme 基本上只要这个就够。其他的广播发送太频繁了。
                    connetCallBack?.let {
                        async {
                            connetCallBack = null//回调一次即可。防止重复回调。不会影响下面的it()
                            delay(500, TimeUnit.MILLISECONDS)
                            it()

                        }
                    }
                    //fixme 连接状态变化监听;不会主动制空；一直都会有回调
                    mConnetCallBack?.let {
                        it(KWifiManagerUtils.getConnectionInfoSSID())
                    }
                }
//                when (it.action) {
//                    WifiManager.WIFI_STATE_CHANGED_ACTION -> {
////                        //wifi开关变化通知
////                        var wifiState = it.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED)
////                        if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
////                            //wifi关闭
////                            //KLoggerUtils.e("TEST", "wifi关闭")
////                        } else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
////                            //wifi开启
////                            //KLoggerUtils.e("TEST", "wifi开启")
////                        }
////                    }
//                    WifiManager.SCAN_RESULTS_AVAILABLE_ACTION -> {
//                        //wifi扫描结果通知
//                        //KLoggerUtils.e("TEST", "wifi扫描结果通知")
//                    }
//                    WifiManager.SUPPLICANT_STATE_CHANGED_ACTION -> {
//                        //wifi连接结果通知
//                        //KLoggerUtils.e("TEST", "wifi连接结果通知")
//                        callBack?.let {
//                            it()//fixme 基本上只要这个就够。
//                        }
//                    }
//                    WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
//                        //网络状态变化通知
//                        //这个是最多的
//                        //KLoggerUtils.e("TEST", "wifi网络状态变化通知")
//                    }
//                }
            }
        }
    }

}
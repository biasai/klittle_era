package cn.oi.klittle.era.bluetooth

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import cn.oi.klittle.era.base.KBaseActivityManager
import cn.oi.klittle.era.base.KBaseApplication
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import cn.oi.klittle.era.utils.KAppUtils
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.utils.KPermissionUtils
import cn.oi.klittle.era.utils.KStringUtils
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import org.jetbrains.anko.runOnUiThread
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


/**
 * fixme 蓝牙的基本操作，比如打开关闭,蓝牙连接等
 * fixme 有些设备搜索不到，需要停留在蓝色设置界面才能搜索到蓝牙，比如PDA
 */
object KBluetoothAdapter {
    private fun getContext(): Context {
        return KBaseApplication.getInstance()//fixme 使用这个是全局的。
        //return KBaseApplication.getInstance().applicationContext//fixme 不要使用这个，和Activity差不多。应用退出再进入之后，就无效了。
    }

    fun isVersion18(): Boolean {
        return Build.VERSION.SDK_INT >= 18//4.3
    }

    fun isVersion21(): Boolean {
        return Build.VERSION.SDK_INT >= 21//5.0
    }

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var manager: BluetoothManager? = null

    /**
     * fixme 蓝牙状态回调
     */

    //蓝牙已关闭
    private var onOff: (() -> Unit)? = null

    fun onOff(onOff: (() -> Unit)? = null): KBluetoothAdapter {
        this.onOff = onOff
        return this
    }

    //蓝牙正在关闭
    private var onOffing: (() -> Unit)? = null

    fun onOffing(onOffing: (() -> Unit)? = null): KBluetoothAdapter {
        this.onOffing = onOffing
        return this
    }

    //蓝牙已打开
    private var onOn: (() -> Unit)? = null

    fun onOn(onOn: (() -> Unit)? = null): KBluetoothAdapter {
        this.onOn = onOn
        return this
    }

    //蓝牙正在打开
    private var onOning: (() -> Unit)? = null

    fun onOning(onOning: (() -> Unit)? = null): KBluetoothAdapter {
        this.onOning = onOning
        return this
    }

    init {
        /**
         * fixme 蓝牙4.0需要api 18及以上（安卓系统4.3）;现在绝大多数的系统都已经超过这个版本。4.3以的下几乎不用考虑了。
         */
        if (isVersion18()) {
            manager = getContext().getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothAdapter = manager?.adapter

            //fixme 广播接收发现蓝牙设备（这个是最传统的接收方式。最稳定）
            var mReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent != null) {
                        val action = intent.getAction()

                        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                                    BluetoothAdapter.ERROR)
                            when (state) {
                                BluetoothAdapter.STATE_OFF -> {
                                    disConnectAllBluetooth()//fixme 蓝牙关闭之后，释放连接的设备资源。
                                    //手机蓝牙关闭
                                    //KLoggerUtils.e("手机蓝牙关闭:\t"+ isEnabled())
                                    onOff?.let {
                                        it()
                                    }
                                }
                                BluetoothAdapter.STATE_TURNING_OFF -> {
                                    disConnectAllBluetooth()
                                    //fixme 手机蓝牙正在关闭(关闭过程中，不会重复调用，只会调用一次)
                                    //KLoggerUtils.e("手机蓝牙正在关闭")
                                    onOffing?.let {
                                        it()
                                    }
                                }
                                BluetoothAdapter.STATE_ON -> {
                                    //STATE_ON 手机蓝牙开启
                                    //KLoggerUtils.e("手机蓝牙开启：\t"+ isEnabled())
                                    onOn?.let {
                                        it()
                                    }
                                    onOnEnable?.let {
                                        it()
                                    }
                                }
                                BluetoothAdapter.STATE_TURNING_ON -> {
                                    disConnectAllBluetooth()
                                    //fixme 手机蓝牙正在开启(开启过程中，不会重复调用，也只会调用一次)
                                    //KLoggerUtils.e("手机蓝牙正在开启")
                                    onOning?.let {
                                        it()
                                    }
                                }
                            }
                        } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED == action) {
                            //开始扫描
                            //KLoggerUtils.e("开始扫描")
                        } else if (BluetoothDevice.ACTION_FOUND == action) {
                            //扫描到设备
                            var device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                            if (device != null) {
                                //KLoggerUtils.e("广播接收：\t" + device.name + "\tmac:\t" + device.address)
                                addDevice(device)
                            }
                        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                            //扫描结束
                            //KLoggerUtils.e("扫描结束")
                        }

                    }
                }
            }
            var filterFound = IntentFilter(BluetoothDevice.ACTION_FOUND)
            getContext()?.registerReceiver(mReceiver, filterFound)
            var filterStart = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            getContext()?.registerReceiver(mReceiver, filterStart)
            var filterFinish = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            getContext()?.registerReceiver(mReceiver, filterFinish)
            //蓝牙打开关闭状态监听
            var filterState = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            getContext()?.registerReceiver(mReceiver, filterState)
        }
    }

    //BLE 即 Bluetooth Low Energy，蓝牙低功耗技术，是蓝牙4.0引入的新技术,在安卓4.3(API 18)以上为BLE的核心功能提供平台支持和API。
    // 与传统的蓝牙相比，BLE更显著的特点是低功耗，所以现在越来越多的智能设备使用了BLE，比如满大街的智能手环，还有体重秤、血压计、心电计等很多BLE设备都使用了BLE与终端设备进行通信。
    //判断手机是否支持BLE,是否支持蓝牙低功耗技术
    fun hasSystemFeature(): Boolean {
        if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            //KLoggerUtils.e("支持BLE")
            return true
        } else {
            //KLoggerUtils.e("不支持BLE")
            return false
        }
    }

    /**
     * 判断蓝牙是否打开；true打开，false没有打开
     * fixme 不需要权限，能够判断蓝牙是否打开
     */
    fun isEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled ?: true
    }

    //蓝牙已打开回调
    private var onOnEnable: (() -> Unit)? = null

    private fun onOnEnable(onOnEnable: (() -> Unit)? = null): KBluetoothAdapter {
        this.onOnEnable = onOnEnable
        return this
    }

    /**
     * 打开蓝牙,成功返回true;fixme 系统自己会弹出询问框（原生系统不会询问，直接就打开了）。不需要动态去申请（也无法动态申请）
     * @param callback 回调，true打开；false没有打开
     */
    fun enable(callback: ((b: Boolean) -> Unit)? = null) {
        if (!isEnabled()) {
            bluetoothAdapter?.enable()//打开。方法本身不阻塞，阻塞线程的是那个系统弹框。
            var isCallback = false//判断是否回调
            onOnEnable {
                //加快回调的速度（可能会比下面定时的方法快。）
                onOnEnable = null//制空，防止重复调用
                if (!isCallback) {
                    isCallback = true
                    callback?.let {
                        it(true)
                    }
                }
            }
            //fixme 蓝牙打开需要时间，测试大约500毫秒;小米8上面测试时间为2000毫秒。打开时间比较耗时。
            callback?.let {
                async {
                    delay(3100, TimeUnit.MILLISECONDS)
                    if (!isCallback) {
                        isCallback = true
                        it(isEnabled())
                    }

                }
            }
        } else {
            callback?.let {
                it(true)
            }
        }
    }

    /**
     * 关闭蓝牙,成功关闭返回true;fixme 关闭蓝牙(8.0及以下)不需要任何权限。能够成功；但是9.0及以上就会弹出系统询问框。
     * @param callback 回调，true关闭；false没有关闭
     */
    fun disable(callback: ((b: Boolean) -> Unit)? = null) {
        if (isEnabled()) {
            bluetoothAdapter?.disable()//fixme 蓝牙关闭时间很短。100毫秒即可;比打开时间要短很多。
            callback?.let {
                async {
                    delay(300, TimeUnit.MILLISECONDS)
                    it(!isEnabled())
                }
            }
        } else {
            callback?.let {
                it(true)
            }
        }
    }

    //设备扫描列表
    private var bluetoothDevices = arrayListOf<BluetoothDevice>()

    //根据mac地址获取蓝牙设备；device?.address
    //BluetoothDevice里的address属性就是mac地址，如：50:8F:4C:15:11:D6
    fun getRemoteDevice(address: String): BluetoothDevice? {
        return bluetoothAdapter?.getRemoteDevice(address)
    }

    fun getRemoteDevice(address: ByteArray): BluetoothDevice? {
        if (Build.VERSION.SDK_INT >= 16) {
            return bluetoothAdapter?.getRemoteDevice(address)
        }
        return bluetoothAdapter?.getRemoteDevice(KStringUtils.bytesToString(address))
    }

    /**
     * fixme 获取已经配对的蓝牙设备(不需要定位权限);扫描设备需要定位权限。
     */
    fun getBondedDevices(): MutableList<BluetoothDevice>? {
        var bondedDevices = bluetoothAdapter?.bondedDevices
        bondedDevices?.let {
            if (it.size > 0) {
                var mutableList = mutableListOf<BluetoothDevice>()
                it.forEach {
                    mutableList.add(it)
                }
                return mutableList
            }
        }
        return null
    }

    private var isStopSan = true//扫描是否停止

    /**
     * 判断该设备是否有效
     */
    private fun isEffective(device: BluetoothDevice?): Boolean {
        if (device != null && !KStringUtils.isEmpty(device.name)) {
            return true
        }
        return false
    }

    //添加搜索到的设备
    private fun addDevice(device: BluetoothDevice?) {
        if (device != null && !isStopSan && isEffective(device) && !bluetoothDevices.contains(device)) {//去重
            bluetoothDevices.add(device!!)
        }
    }

    /**
     * fixme uuid需要从硬件工程师中获取，这样你才能匹配到你要的。不为空，搜索指定的蓝牙。为空，搜索附近全部的蓝牙。
     * @param serviceUuid 特定的UUID,如：UUID.fromString("d9bc5194-431d-49ed-b3b6-4a1b72005934")
     * @param delay 预先定义停止蓝牙扫描的时间,单位毫秒
     * @param callback 回调
     */
    fun startLeScan(serviceUuid: UUID, delay: Long = 5000, callback: (bluetoothDevices: MutableList<BluetoothDevice>) -> Unit) {
        var serviceUuids = mutableListOf<UUID>()
        serviceUuids.add(serviceUuid)
        startLeScan(serviceUuids, delay, callback)
    }

    var scanCallback: ScanCallback? = null
    //蓝牙扫描回调监听
    fun getScanCallBack(): ScanCallback? {
        bluetoothDevices.clear()//设备清空一下
        if (scanCallback == null && Build.VERSION.SDK_INT >= 21) {//5.0
            scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)
                    addDevice(result?.device)
                }
            }
        }
        return scanCallback
    }

    /**
     * 扫描设备(搜索的蓝牙设备如果是休眠或者手机黑屏状态，一般都搜索不到。)
     * @param serviceUuids UUID数组
     * @param delay 预先定义蓝牙停止扫描的时间,单位毫秒
     * @param callback 回调，返回搜索到设备。（不管是已匹配的还是没有匹配的，都会搜索到。即返回所有搜索的设备。）
     */
    fun startLeScan(serviceUuids: MutableList<UUID>? = null, delay: Long = 5000, callback: (bluetoothDevices: MutableList<BluetoothDevice>) -> Unit) {
        if (isVersion18() && hasSystemFeature()) {

            //closeBluetoothServerSocket()//fixme 关闭客户端就行了，不要关闭服务断（不然就无法继续接收客户端连接了）
            closeBluetoothSocket()//fixme 关闭客户端，RFCOMM 一次只允许每个通道有一个已经连接的客户端。所以搜索之前，要关闭已经连接的客户端。不然搜索不出来。

            //搜索蓝牙，必须打开蓝牙。
            enable {
                KPermissionUtils.requestPermissionsBlueTooth {
                    if (it) {
                        //6.0以后需要定位权限，才能搜索蓝牙。亲测，定位权限必不可少！
                        async {
                            bluetoothAdapter?.startDiscovery()//fixme 开始扫描，使用广播接收(防止以下方法搜索不到设备，所以加上广播一起搜索。)
                            isStopSan = false//fixme 开始扫描标志
                            if (isVersion21()) {
                                //KLoggerUtils.e("开始搜索：\t" + bluetoothAdapter?.bluetoothLeScanner + "\t" + scanCallback)
                                bluetoothAdapter?.bluetoothLeScanner?.startScan(getScanCallBack())
                            } else {
                                bluetoothAdapter?.startLeScan(serviceUuids?.toTypedArray()) { device, rssi, scanRecord ->
                                    addDevice(device)
                                }
                            }
                        }
                        async {
                            if (delay <= 0) {
                                kotlinx.coroutines.experimental.delay(3500, TimeUnit.MILLISECONDS)
                            } else {
                                kotlinx.coroutines.experimental.delay(delay, TimeUnit.MILLISECONDS)
                            }
                            kotlinx.coroutines.experimental.delay(100)
                            // fixme 预先定义停止蓝牙扫描的时间（因为蓝牙扫描需要消耗较多的电量）
                            stopLeScan()
                            kotlinx.coroutines.experimental.delay(100, TimeUnit.MILLISECONDS)
                            isStopSan = true//fixme 结束扫描标志
                            if (isEnabled()) {
                                getContext().runOnUiThread {
                                    callback(bluetoothDevices)
                                }
                            } else {
                                bluetoothDevices.clear()
                            }
                        }
                    }
                }
            }

        }
    }

    //停止扫描（在开始扫描里面，会主动停止扫描，所以不需要手动调用。）
    fun stopLeScan() {
        if (isVersion18() && hasSystemFeature()) {
            kotlinx.coroutines.experimental.async {
                bluetoothAdapter?.cancelDiscovery()//fixme 停止扫描(停止广播接收)
                isStopSan = true
                if (isVersion21()) {
                    bluetoothAdapter?.bluetoothLeScanner?.stopScan(object : ScanCallback() {
                        override fun onScanResult(callbackType: Int, result: ScanResult?) {
                            super.onScanResult(callbackType, result)
                            var device = result?.device
                            addDevice(device)
                        }
                    })
                } else {
                    bluetoothAdapter?.stopLeScan { device, rssi, scanRecord ->
                        addDevice(device)
                    }
                }
            }
        }
    }

    //保存已经连接的BluetoothGatt ;(device.address作为key键)
    private var gattMap = mutableMapOf<String, BluetoothGatt?>()
    private var deviceMap = mutableMapOf<String, KBluetoothDevice?>()
    //fixme 如果3次都连接不上，可能是长时间开启蓝牙的原因，需要关闭蓝牙重启。设备搜索不到。也需要重启蓝牙
    /**
     * 连接(fixme 可以重复连接调用;)
     * @param device 连接的蓝牙设备
     * @param autoConnect 自动连接
     * @param timeout 连接超时时间，单位毫秒（最好大于2000毫秒，一般都需要2000毫秒左右）
     * @param connetCallback 回调，成功返回KBluetoothDevice，失败返回空null（可以提示用户手动重启蓝牙）
     */
    fun connectBluetooth(device: BluetoothDevice?, autoConnect: Boolean = false, timeout: Long = 4000, connetCallback: ((gatt: KBluetoothDevice?) -> Unit)? = null) {
        if (device == null) {
            connetCallback?.let {
                it(null)
            }
            return
        }
        if (isVersion18()) {
            //KLoggerUtils.e("开始连接：\t" + device?.name)
            //BLE是低功耗，一般最多只允许重复连接六次，其后就连接不上了。为了防止重复连接。在连接之前，先关闭。防止多次连接之后连接不上。
            disConnectBluetooth(device)
            var isCallBack = false//fixme 是否回调
            if (device == null) {
                //设备为空
                if (!isCallBack) {
                    isCallBack = true
                    connetCallback?.let {
                        it(null)
                    }
                }
            } else {
                var callback = connetCallback
                //设备不为空
                device?.connectGatt(getContext(), autoConnect, object : BluetoothGattCallback() {
                    //不要执行耗时操作
                    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                        super.onConnectionStateChange(gatt, status, newState)
                        //KLoggerUtils.e("连接状态：\tstatus:\t" + status + "\tnewState:\t" + newState + "\tgatt:\t" + gatt?.device?.name)
                        if (status == BluetoothProfile.GATT_SERVER && newState == BluetoothProfile.STATE_DISCONNECTED && callback == null) {
                            //fixme 连接断开(设备断开之后，会回调此方法，自己手动断开的不会回调。)
                            gatt?.let {
                                if (it.device != null) {
                                    gattMap.remove(it.device.address)
                                    deviceMap?.remove(it.device.address)
                                }
                                it.close()
                            }
                        } else if (gatt != null) {
                            //fixme 注意：这里不管回调的是已经连接还是连接失败，只要gatt对象不为空，都能够进行Socket通信连接。
                            //fixme 连接成功
                            if (callback != null) {
                                onServicesDiscovered(gatt, BluetoothGatt.GATT_SUCCESS)
                            } else {
                                gatt?.discoverServices()//fixme 会在onServicesDiscovered这个方法中回调
                            }
                        }
//                        if (newState == BluetoothProfile.STATE_CONNECTED || (status == 257 && gatt != null)) {
//                            //连接成功
//                            //KLoggerUtils.e("连接成功")
//                            if (status == 257) {//257特殊处理，发生设备如：PDA
//                                onServicesDiscovered(gatt, BluetoothGatt.GATT_SUCCESS)
//                            } else {
//                                gatt?.discoverServices()//fixme 会在onServicesDiscovered这个方法中回调
//                            }
//                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                            //fixme 连接断开(设备断开之后，会回调此方法，自己手动断开的不会回调。)
//                            //关闭当前新的连接
//                            //KLoggerUtils.e("连接断开：\t" + gatt?.device?.name)
//                            gatt?.let {
//                                if (it.device != null) {
//                                    gattMap.remove(it.device.address)
//                                    deviceMap?.remove(it.device.address)
//                                }
//                                it.close()
//                            }
//                        }
                    }

                    //成功连接
                    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                        super.onServicesDiscovered(gatt, status)
                        //KLoggerUtils.e("gatt:\t"+gatt+"\tstatus:\t"+status)
                        //fixme 回调之后，设备之间才真正通信连接起来
                        gatt?.let {
                            if (gatt.device != null) {
                                gattMap.put(gatt.device.address, it)
                            }
                        }
                        if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
                            //蓝牙连接正常
                            //KLoggerUtils.e("连接正常")
                            if (!isCallBack) {
                                isCallBack = true
                                callback?.let {
                                    var kBluetoothDevice = KBluetoothDevice(gatt)
                                    if (gatt.device != null) {
                                        //保持连接的设备
                                        deviceMap?.put(gatt.device.address, kBluetoothDevice)
                                    }
                                    it(kBluetoothDevice)
                                }
                                callback = null//fixme 防止重复回调
                            }
                        } else {
                            gatt?.let {
                                if (it.device != null) {
                                    gattMap.remove(it.device.address)
                                    deviceMap?.remove(it.device.address)
                                }
                                it.close()
                            }
                            //KLoggerUtils.e("连接失败")
                        }
                    }

                    //这个方法一般用不到
                    override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                        super.onCharacteristicRead(gatt, characteristic, status)
                    }

                    //这个方法是写入数据时的回调，可以和你写入的数据做对比
                    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                        super.onCharacteristicWrite(gatt, characteristic, status)
                    }

                    //fixme 设备发出通知时会调用到该接口，蓝牙设备给手机发送数据，在这个方法接收
                    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                        super.onCharacteristicChanged(gatt, characteristic)
                        characteristic?.value?.let {
                            //fixme 接收蓝牙设备发来的通知
                            var msg = KStringUtils.bytesToString(it)
                            if (msg != null) {
                                gatt?.device?.address?.let {
                                    deviceMap.get(it)?.let {
                                        it.onMessage?.let {
                                            it(msg)
                                        }
                                    }
                                }
                            }
                        }
                    }

                })
                async {
                    //fixme 回调超时
                    delay(timeout, TimeUnit.MILLISECONDS)
                    if (!isCallBack) {
                        isCallBack = true
                        callback?.let {
                            disConnectBluetooth(device)
                            it(null)
                        }
                        callback = null//fixme 防止重复回调
                    }
                }
            }
        }
    }

    /**
     * @param device fixme 设备名称或者设备mac地址，通过设备名称或者mac地址进行蓝牙连接。
     */
    fun connectBluetooth(device: String?, autoConnect: Boolean = false, timeout: Long = 4000, callback: ((gatt: KBluetoothDevice?) -> Unit)? = null) {
        KBluetoothAdapter.startLeScan {
            var has = false
            it?.forEach {
                if (it.name != null && it.address != null && (it.name.equals(device) || it.address.equals(device))) {
                    has = true
                    KBluetoothAdapter.connectBluetooth(it, autoConnect, timeout, callback)
                }
            }
            //没有发现设备
            if (!has) {
                callback?.let {
                    it(null)
                }
            }
        }
    }

    /**
     * @param deviceName 设备名称，通过设备名称进行蓝牙连接。
     */
    fun connectBluetoothForName(deviceName: String?, autoConnect: Boolean = false, timeout: Long = 4000, callback: ((gatt: KBluetoothDevice?) -> Unit)? = null) {
        KBluetoothAdapter.startLeScan {
            var has = false
            it?.forEach {
                if (it.name != null && it.name.equals(deviceName)) {
                    has = true
                    KBluetoothAdapter.connectBluetooth(it, autoConnect, timeout, callback)
                }
            }
            //没有发现设备
            if (!has) {
                callback?.let {
                    it(null)
                }
            }
        }
    }

    /**
     * @param adress 设备mac地址，通过设备mac地址进行蓝牙连接。
     */
    fun connectBluetoothForAdress(adress: String?, autoConnect: Boolean = false, timeout: Long = 4000, callback: ((gatt: KBluetoothDevice?) -> Unit)? = null) {
        KBluetoothAdapter.startLeScan {
            var has = false
            it?.forEach {
                if (it.address != null && it.address.equals(adress)) {
                    has = true
                    KBluetoothAdapter.connectBluetooth(it, autoConnect, timeout, callback)
                }
            }
            //没有发现设备
            if (!has) {
                callback?.let {
                    it(null)
                }
            }
        }
    }

    //断开连接
    fun disConnectBluetooth(device: BluetoothDevice?) {
        if (isVersion18()) {
            device?.let {
                var mGatt = gattMap.get(it.address)
                if (mGatt != null) {
                    mGatt?.disconnect()
                    mGatt?.close()
                    mGatt = null
                    gattMap.remove(it.address)
                }
                deviceMap?.remove(it.address)
            }

        }
    }

    fun disConnectBluetooth(gatt: BluetoothGatt?) {
        if (isVersion18()) {
            disConnectBluetooth(gatt?.device)
        }
    }

    //断开连接(根据mac地址)
    fun disConnectBluetoothForAddress(address: String?) {
        if (isVersion18()) {
            address?.let {
                var mGatt = gattMap.get(address)
                if (mGatt != null) {
                    mGatt?.disconnect()
                    mGatt?.close()
                    mGatt = null
                    gattMap.remove(address)
                }
                deviceMap?.remove(it)
            }

        }
    }

    //断开连接(根据设备名称)
    fun disConnectBluetoothForDeviceName(deviceName: String?) {
        if (isVersion18()) {
            var address: String? = null
            gattMap.forEach {
                it.value?.device?.let {
                    if (it.name.equals(deviceName)) {
                        address = it.address
                    }
                }
            }
            address?.let {
                disConnectBluetoothForAddress(it)
            }
        }
    }

    //断开所有连接
    fun disConnectAllBluetooth() {
        if (isVersion18()) {
            gattMap?.forEach {
                var mGatt = it.value
                mGatt?.disconnect()
                mGatt?.close()
                mGatt = null
            }
            gattMap?.clear()
            deviceMap?.clear()
        }
    }

    /**
     * 获取所有连接的BluetoothGatt
     */
    fun getConnectGattForAll(): MutableList<BluetoothGatt>? {
        var list = mutableListOf<BluetoothGatt>()
        gattMap.forEach {
            it.value?.let {
                list.add(it)
            }
        }
        if (list.size > 0) {
            return list
        }
        return null
    }

    /**
     * 根据蓝牙mac地址，获取连接的BluetoothGatt
     */
    fun getConnectGattForAddress(address: String?): BluetoothGatt? {
        var bluetoothGatt: BluetoothGatt? = null
        if (isVersion18()) {
            gattMap.forEach {
                var gatt = it.value
                gatt?.let {
                    it.device?.let {
                        if (it.address != null && it.address.equals(address)) {
                            bluetoothGatt = gatt
                            return@forEach
                        }
                    }
                }
            }
        }
        return bluetoothGatt
    }

    /**
     * 根据蓝牙设备名称，获取连接的BluetoothGatt；（如果有多个相同的名称，优化获取第一个）
     */
    fun getConnectGattForName(name: String?): BluetoothGatt? {
        var bluetoothGatt: BluetoothGatt? = null
        if (isVersion18()) {
            gattMap.forEach {
                var gatt = it.value
                gatt?.let {
                    it.device?.let {
                        if (it.name != null && it.name.equals(name)) {
                            bluetoothGatt = gatt
                            return@forEach
                        }
                    }
                }
            }
        }
        return bluetoothGatt
    }

    /**
     * 判断蓝色设备是否连接
     */
    fun isConnect(gatt: BluetoothGatt?): Boolean {
        return gattMap.containsValue(gatt)
    }

    fun isConnect(device: BluetoothDevice?): Boolean {
        device?.let {
            return gattMap.containsKey(it.address)
        }
        return false
    }

    fun isConnect(address: String?): Boolean {
        return gattMap.containsKey(address)
    }

    /**
     * 设置通知
     * @param bluetoothGattCharacteristic
     * @param bluetoothGatt
     * @param enable 设置为true以启用通知/指示;不要动，最好默认设置成true
     */
    fun setCharacteristicNotification(bluetoothGattCharacteristic: BluetoothGattCharacteristic?, bluetoothGatt: BluetoothGatt?, enable: Boolean = true) {
        if (isVersion18()) {
            if (bluetoothGattCharacteristic != null && bluetoothGatt != null) {
                bluetoothGatt?.setCharacteristicNotification(bluetoothGattCharacteristic, enable)
            }
        }
    }

    /**
     * 发送数据；写入命令
     * @param bluetoothGattCharacteristic
     * @param bluetoothGatt
     * @param msg 数据
     * @param callback 回调；true发送成功，false 发送失败
     */
    fun send(bluetoothGattCharacteristic: BluetoothGattCharacteristic?, bluetoothGatt: BluetoothGatt?, msg: ByteArray?, callback: ((b: Boolean) -> Unit)? = null) {
        if (isVersion18() && isConnect(bluetoothGatt) && msg != null) {
            if (bluetoothGattCharacteristic != null && bluetoothGatt != null) {
                // 发出数据
                bluetoothGattCharacteristic?.setValue(msg)
                if (bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic)) {
                    //写入成功
                    callback?.let { it(true) }
                    //KLoggerUtils.e("写入成功")
                } else {
                    //写入失败
                    callback?.let { it(false) }
                    //KLoggerUtils.e("写入失败:\t"+bluetoothGattCharacteristic+"\t"+bluetoothGattCharacteristic.uuid)
                }
            } else {
                callback?.let { it(false) }
            }
        } else {
            callback?.let { it(false) }
        }
    }

    fun send(bluetoothGattCharacteristic: BluetoothGattCharacteristic?, bluetoothGatt: BluetoothGatt?, msg: String?, callback: ((b: Boolean) -> Unit)? = null) {
        send(bluetoothGattCharacteristic, bluetoothGatt, msg?.toByteArray(), callback)
    }

    /**
     * 在进行连接之前应该始终调用这个方法，而且调用的时候无需检测是否正在扫描。
     */
    fun cancleDiscovery() {
        bluetoothAdapter?.cancelDiscovery()// 关闭发现设备
    }

    private var mBluetoothServerSocket: BluetoothServerSocket? = null
    private var mmBluetoothServerSockets = ArrayList<BluetoothServerSocket?>()//fixme 保存服务集合
    /**
     * fixme 添加BluetoothServerSocket服务端
     */
    fun addBluetoothServerSocket(bluetoothServerSocket: BluetoothServerSocket?) {
        mmBluetoothServerSockets?.add(bluetoothServerSocket)
    }

    var name = KAppUtils.getAppName()
    var uuid = UUID.fromString("d9bc5194-431d-49ed-b3b6-4a1b72005934")//uuid的格式在 KUniQueUtils里有说明
    /**
     * fixme 获取蓝牙服务器
     * @param name 字符串是我们自己定义的服务的可识别名称，可以直接使用包名。系统会自定将其写入到设备上的新服务发现协议（SDP）数据库条目中。
     * @param uuid UUID 也在 SDP 中，作为与客户端设备连接协议的匹配规则。只有客户端和这里的UUID 一样了才可以会被连接
     */
    fun getBluetoothServerSocket(name: String = this.name, uuid: UUID = this.uuid): BluetoothServerSocket? {
        if (mBluetoothServerSocket == null) {
            mBluetoothServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(name, uuid)
            addBluetoothServerSocket(mBluetoothServerSocket)
        }
        return mBluetoothServerSocket
    }

    private var isOpenBluetoothServerSocket = false
    /**
     * fixme 开启 BluetoothSocket服务，并返回BluetoothSocket客户端
     */
    fun openBluetoothServerSocket(callback: ((kBluetoothSocket: KBluetoothSocket) -> Unit)?) {
        if (isOpenBluetoothServerSocket) {
            return//防止重复开启服务。
        }
        async {
            isCloseBluetoothSocket = false
            while (!isCloseBluetoothSocket) {
                isOpenBluetoothServerSocket = true
                //阻塞调用，将在连接被接受或者发生异常的时候返回，操作成功后，会返回 BluetoothSocket。
                var bluetoothSocket = getBluetoothServerSocket()?.accept()//fixme 这一步会阻塞
                if (bluetoothSocket != null) {
                    var kBluetoothSocket = KBluetoothSocket(bluetoothSocket)
                    addBluetoothSocket(kBluetoothSocket)
                    callback?.let {
                        it(kBluetoothSocket)
                    }
                    //close()会释放服务器套接字及其所有资源，但不会关闭已经连接的 BluetoothSocket。
                    //与 TCP/IP 不同的是，RFCOMM 一次只允许每个通道有一个已经连接的客户端。
                    //mBluetoothServerSocket?.close()//fixme 最好不要关闭
                    //mBluetoothServerSocket = null//fixme 如果关闭了，就要置空。close()之后，mBluetoothServerSocket就已经没有用了。
                }
            }
            isOpenBluetoothServerSocket = false
        }
    }

    private var isCloseBluetoothSocket = false
    /**
     * fixme 关闭BluetoothSocket服务
     */
    fun closeBluetoothServerSocket() {
        async {
            try {
                isCloseBluetoothSocket = true
                mmBluetoothServerSockets?.forEach {
                    it?.close()
                }
                mmBluetoothServerSockets?.clear()
                mBluetoothServerSocket?.close()
                mBluetoothServerSocket = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //fixme 保存记录所有的连接BluetoothSocket客户端
    var mBluetoothSockets = ArrayList<KBluetoothSocket?>()

    /**
     * fixme 添加BluetoothSocket客户端
     */
    fun addBluetoothSocket(bluetoothSocket: KBluetoothSocket?) {
        mBluetoothSockets?.add(bluetoothSocket)
    }

    /**
     * fixme 关闭BluetoothSocket客户端
     */
    fun closeBluetoothSocket() {
        async {
            try {
                mBluetoothSockets?.forEach {
                    it?.close()
                    it?.name = null
                    it?.address = null
                    it?.bluetoothSocket = null
                }
                mBluetoothSockets?.clear()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
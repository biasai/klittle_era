package cn.oi.klittle.era.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Camera
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.view.View
import android.widget.TextView
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseActivityManager
import cn.oi.klittle.era.base.KBaseApplication
import cn.oi.klittle.era.dialog.KTimiAlertDialog
import java.io.File

//   fixme 在Activity里面调用一下方法。回调才有效。[不过我已经在BaseActivity中添加以下方法了，如果继承了BaseActvity就不用再写了]
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        PermissionUtils.onRequestPermissionsResult(getActivity(),requestCode, permissions, grantResults)
//    }

//  调用案例（虽然有两个参数，但只要打出第一个参数的第一个字母，后面会自动提示出来的。现在已经改成只有一个参数了。）
//        PermissionUtils.requestPermissionsStorage(this){
//            Log.e("test","权限是否允许:\t"+it)
//            if(it){
//                //权限申请成功
//            }else{
//                PermissionUtils.showFailure(getActivity())//显示默认失败界面
//            }
//        }

/**
 * fixme 权限相关,一般都是一些隐私权限才需要动态申请。
 */
object KPermissionUtils {

    private fun getContext(): Context {
        return KBaseApplication.getInstance()
    }

    private fun getActivity(): Activity? {
        return KBaseActivityManager.getInstance().stackTopActivity
    }

    /**
     * 获取String文件里的字符,<string name="names">你好%s</string>//%s 是占位符,位置随意
     * @param formatArgs 是占位符
     */
    open fun getString(id: Int, formatArgs: String? = null): String {
        if (formatArgs != null) {
            return getContext().resources.getString(id, formatArgs) as String
        }
        return getContext().resources.getString(id) as String
    }

    /**
     * 判断Activity是否有效
     */
    fun isEffectiveActivity(activity: Activity?): Boolean {
        if (activity == null || activity.isFinishing) {
            return false
        } else {
            return true
        }
    }

    //fixme 注意：申请权限之前，一定要在清单里注册一下。没有注册权限，是无法申请权限的。没有注册只会返回失败，不会弹出权限询问框。
    //权限数组
    val DANGEROUS_PERMISSION = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    val DANGEROUS_PERMISSION_CAMERA = arrayOf(Manifest.permission.CAMERA)//相机
    val DANGEROUS_PERMISSION_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)//SD卡读写
    val DANGEROUS_PERMISSION_RECORD = arrayOf(Manifest.permission.RECORD_AUDIO)//录音
    val DANGEROUS_PERMISSION_READ_PHONE_STATE = arrayOf(Manifest.permission.READ_PHONE_STATE)//用于调用 JNI ,及读取设备的权限，如手机设备号

    val DANGEROUS_PERMISSION_LAUNCHER_SHORTCUT = arrayOf(Manifest.permission.INSTALL_SHORTCUT, Manifest.permission.UNINSTALL_SHORTCUT)//桌面快捷图标，添加，删除

    val DANGEROUS_PERMISSION_INSTALL_PACKAGES = arrayOf(Manifest.permission.REQUEST_INSTALL_PACKAGES)//安卓8.0系统安装app 未知来源权限

    val DANGEROUS_PERMISSION_ACCESS_FINE_LOCATION = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)//定位权限，获取wifi(扫描)需要定位权限

    val DANGEROUS_PERMISSION_SYSTEM_ALERT_WINDOW = arrayOf(Manifest.permission.SYSTEM_ALERT_WINDOW)//系统悬浮窗权限
    val DANGEROUS_PERMISSION_ACCESS_WIFI_STATE = arrayOf(Manifest.permission.CHANGE_WIFI_MULTICAST_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.CHANGE_NETWORK_STATE, Manifest.permission.ACCESS_NETWORK_STATE)//打开wifi权限
    val DANGEROUS_PERMISSION_GET_ACCOUNTS = arrayOf(Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_CONTACTS)//通讯录权限（读取手机号码）
    val DANGEROUS_PERMISSION_RECEIVE_SMS = arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)//读取手机短信的权限
    val DANGEROUS_PERMISSION_BLUE_TOOTH = arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)//打开关闭，蓝牙的权限
    val DANGEROUS_PERMISSION_NFC = arrayOf(Manifest.permission.NFC,Manifest.permission.BIND_NFC_SERVICE)//NFC的权限
    // requestCode 权限请求码(统一使用这个)[fixme 数字标志为 0~6万，负数会奔溃，高于7万也会奔溃。]
    val READ_PHONE_STATE_REQUEST_CODE = 3820//权限请求标志

    var perMissionType = 3821//当前权限申请的标识。默认为相机拍照。
    var perMissionTypeALL = 3822//以下所有权限的集合。
    var perMissionTypeCamera = 3823//相机拍照权限申请标识。图片,相机拍照和拍摄都需要相机权限。
    var perMissionTypeVideo = 3824//相机拍摄权限申请标识。视频
    var perMissionTypeStorage = 3825//SD卡权限申请标识。
    var perMissionTypeRecording = 3826//录音权限申请标识。
    var perMissionTypeReadPhoneState = 3827//手机状态权限申请，如手机设备号等。
    var perMissionTypeLauncher = 3828//桌面快捷方式
    var perMissionTypeInstallApp = 3829//安装app 未知来源权限
    var perMissionTypeLocation = 3830//定位权限
    var perMissionTypeWindow = 3831//系统悬浮窗权限
    var perMissionTypeWifi = 3832//打开连接wifi权限
    var perMissionTypeAccount = 3833//打开通讯录权限
    var perMissionTypeSMS = 3834//读取手机短信权限
    var perMissionTypeBLUETOOTH = 3835//打开关闭，蓝牙的权限
    var perMissionTypeNFC = 3836//读取手机短信权限
    var requestCode_CanDrawOverlays = 3837//悬浮窗权限申请标志。

    //fixme 动态权限申请，SDK必须大于等于23，且targetSdkVersion也必须大于等于23才有效。
    //低于23的版本，权限默认就是开启的。
    fun isVersion23(): Boolean {
        if (Build.VERSION.SDK_INT >= 23 && KBaseApplication.getInstance().targetSdkVersion >= 23) {
            return true
        }
        return false
    }
    //fixme 注意，如果该权限已经申请成功。再次调用权限申请，不会弹出权限申请窗口。会直接返回成功回调。

    //一般的权限申请，手机信息，存储卡权限。相机，录音。
    fun requestPermissionsALL(activity: Activity? = getActivity(), onRequestPermissionsResult2: ((isAllow: Boolean) -> Unit)? = null): Boolean {
        perMissionType = perMissionTypeALL
        if (isVersion23()) {//Android6.0权限申请
            // 权限已经授予,直接初始化
            if (ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionsResult2?.let {
                    if (isEffectiveActivity(activity)) {
                        this.onRequestPermissionsResult = it
                        // 申请权限（回调不为空，才会申请）
                        ActivityCompat.requestPermissions(activity!!, DANGEROUS_PERMISSION, READ_PHONE_STATE_REQUEST_CODE)
                    } else {
                        it(false)
                    }
                }
                return false
            } else {
                onRequestPermissionsResult2?.let {
                    it(true)
                }
                return true
            }
        } else {
            onRequestPermissionsResult2?.let {
                it(true)
            }
            return true
        }
    }

    //相机权限请求，true通过，false 会弹出权限请求窗口。【6.0(api 23)以上才需要】
    fun requestPermissionsCamera(activity: Activity? = getActivity(), onRequestPermissionsResult2: ((isAllow: Boolean) -> Unit)? = null): Boolean {
        perMissionType = perMissionTypeCamera
        if (isVersion23()) {
            if (ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {//判断权限是否已授权。没有授权再发生请求回调。已经授权，就不再申请。
                onRequestPermissionsResult2?.let {
                    if (isEffectiveActivity(activity)) {
                        this.onRequestPermissionsResult = it
                        // 申请权限【sdk6.0即以上才有效。targetSdkVersion23及以上才有效。】
                        ActivityCompat.requestPermissions(activity!!, DANGEROUS_PERMISSION_CAMERA, READ_PHONE_STATE_REQUEST_CODE)
                    } else {
                        it(false)
                    }
                }
                return false
            } else {
                onRequestPermissionsResult2?.let {
                    it(true)
                }
                return true
            }
        } else {
            //6.0以下。第一次会弹出系统询问权限框。后面就不会弹了。
            var mCamera: Camera? = null
            try {
                mCamera = Camera.open()//第一次会弹出系统询问权限框【是线程阻塞的。 询问弹框消失了才会继续向下执行。】
                val mParameters = mCamera!!.parameters
                mCamera.parameters = mParameters
            } catch (e: Exception) {
                onRequestPermissionsResult2?.let {
                    it(false)
                }
                return false
            }
            if (mCamera != null) {
                try {
                    mCamera.release()
                } catch (e: Exception) {
                    onRequestPermissionsResult2?.let {
                        it(false)
                    }
                    return false
                }
            }
            onRequestPermissionsResult2?.let {
                it(true)
            }
            return true
        }
    }

    //SD卡权限申请
    fun requestPermissionsStorage(activity: Activity? = getActivity(), onRequestPermissionsResult2: ((isAllow: Boolean) -> Unit)? = null): Boolean {
        perMissionType = perMissionTypeStorage//当前SD卡申请标志
        if (isVersion23()) {
            if (ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionsResult2?.let {
                    if (isEffectiveActivity(activity)) {
                        this.onRequestPermissionsResult = it
                        // 申请权限(回调不为空，才申请)
                        ActivityCompat.requestPermissions(activity!!, DANGEROUS_PERMISSION_STORAGE, READ_PHONE_STATE_REQUEST_CODE)
                    } else {
                        it(false)
                    }
                }
                return false
            } else {
                onRequestPermissionsResult2?.let {
                    it(true)
                }
                return true
            }
        } else {
            //可能返回内置存储卡，也可能返回外置存储卡。如：/storage/emulated/0 注意路径末尾是不带"/"的。
            try {
                var path = android.os.Environment.getExternalStorageDirectory().path
                path = path + "/permisonn000test554861655468.txt"
                var file = File(path)
                if (!file.exists()) {
                    file.createNewFile()
                }
                if (file.exists()) {
                    file.delete()
                }
                onRequestPermissionsResult2?.let {
                    it(true)
                }
                return true
            } catch (e: Exception) {
                onRequestPermissionsResult2?.let {
                    it(false)
                }
                return false
            }

        }
    }

    //录音权限申请
    fun requestPermissionsRecording(activity: Activity? = getActivity(), onRequestPermissionsResult2: ((isAllow: Boolean) -> Unit)? = null): Boolean {
        perMissionType = perMissionTypeRecording
        if (isVersion23()) {
            if (ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionsResult2?.let {
                    if (isEffectiveActivity(activity)) {
                        this.onRequestPermissionsResult = it
                        // 申请权限
                        ActivityCompat.requestPermissions(activity!!, DANGEROUS_PERMISSION_RECORD, READ_PHONE_STATE_REQUEST_CODE)
                    } else {
                        it(false)
                    }
                }
                return false
            } else {
                onRequestPermissionsResult2?.let {
                    it(true)
                }
                return true
            }
        } else {
            onRequestPermissionsResult2?.let {
                it(true)
            }
            return true
        }
    }

    //手机状态权限申请，如手机设备号等。
    fun requestPermissionsReadPhoneState(activity: Activity? = getActivity(), onRequestPermissionsResult2: ((isAllow: Boolean) -> Unit)? = null): Boolean {
        perMissionType = perMissionTypeReadPhoneState
        if (isVersion23()) {
            if (ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionsResult2?.let {
                    // 申请权限
                    if (isEffectiveActivity(activity)) {
                        //当Activity有效时,且回调不为空时才会动态申请权限，不然直接返回false
                        this.onRequestPermissionsResult = it
                        ActivityCompat.requestPermissions(activity!!, DANGEROUS_PERMISSION_READ_PHONE_STATE, READ_PHONE_STATE_REQUEST_CODE)
                    } else {
                        it(false)
                    }
                }
                return false
            } else {
                onRequestPermissionsResult2?.let {
                    it(true)
                }
                return true
            }
        } else {
            onRequestPermissionsResult2?.let {
                it(true)
            }
            return true
        }
    }

    //定位权限申请(亲测，可以动态申请。获取wifi信息（扫描附件热点wifi），需要定位权限。)
    fun requestPermissionsLocation(activity: Activity? = getActivity(), onRequestPermissionsResult2: ((isAllow: Boolean) -> Unit)? = null): Boolean {
        perMissionType = perMissionTypeLocation
        if (isVersion23()) {
            if (ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionsResult2?.let {
                    if (isEffectiveActivity(activity)) {
                        this.onRequestPermissionsResult = it
                        // 申请权限
                        ActivityCompat.requestPermissions(activity!!, DANGEROUS_PERMISSION_ACCESS_FINE_LOCATION, READ_PHONE_STATE_REQUEST_CODE)
                    } else {
                        it(false)
                    }
                }
                return false
            } else {
                onRequestPermissionsResult2?.let {
                    it(true)
                }
                return true
            }
        } else {
            onRequestPermissionsResult2?.let {
                it(true)
            }
            return true
        }
    }

    //获取手机通讯录权限(可以动态申请)
    fun requestPermissionsAccount(activity: Activity? = getActivity(), onRequestPermissionsResult2: ((isAllow: Boolean) -> Unit)? = null): Boolean {
        perMissionType = perMissionTypeAccount
        if (isVersion23()) {
            if (ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionsResult2?.let {
                    if (isEffectiveActivity(activity)) {
                        this.onRequestPermissionsResult = it
                        // 申请权限
                        ActivityCompat.requestPermissions(activity!!, DANGEROUS_PERMISSION_GET_ACCOUNTS, READ_PHONE_STATE_REQUEST_CODE)
                    } else {
                        it(false)
                    }
                }
                return false
            } else {
                onRequestPermissionsResult2?.let {
                    it(true)
                }
                return true
            }
        } else {
            onRequestPermissionsResult2?.let {
                it(true)
            }
            return true
        }
    }

    //读取手机短信权限(可以动态申请)
    fun requestPermissionsSMS(activity: Activity? = getActivity(), onRequestPermissionsResult2: ((isAllow: Boolean) -> Unit)? = null): Boolean {
        perMissionType = perMissionTypeSMS
        if (isVersion23()) {
            if (ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionsResult2?.let {
                    if (isEffectiveActivity(activity)) {
                        this.onRequestPermissionsResult = it
                        // 申请权限
                        ActivityCompat.requestPermissions(activity!!, DANGEROUS_PERMISSION_RECEIVE_SMS, READ_PHONE_STATE_REQUEST_CODE)
                    } else {
                        it(false)
                    }
                }
                return false
            } else {
                onRequestPermissionsResult2?.let {
                    it(true)
                }
                return true
            }
        } else {
            onRequestPermissionsResult2?.let {
                it(true)
            }
            return true
        }
    }

    private var maxNfcRequest = 0;//以防万一；防止权限无限申请。
    //fixme NFC权限申请[无法动态判断;始终返回true;NFC权限真心无法判断，系统不会告诉你NFC的权限。];
    //fixme NFC权限会在首次启动应用的时候，自动弹出申请权限；其后都不会再申请；如果拒绝了，你无法判断权限是否开启，只能手动进行权限设置。
    fun requestPermissionsNFC(activity: Activity? = getActivity(), onRequestPermissionsResult2: ((isAllow: Boolean) -> Unit)? = null): Boolean {
        perMissionType = perMissionTypeNFC
        if (isVersion23()) {
            if (ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.NFC) != PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionsResult2?.let {
                    if (isEffectiveActivity(activity)) {
                        if (maxNfcRequest < 10) {
                            maxNfcRequest++
                            this.onRequestPermissionsResult = it
                            // 申请权限;fixme 权限回调，Activity会重新执行onResume()
                            ActivityCompat.requestPermissions(activity!!, DANGEROUS_PERMISSION_NFC, READ_PHONE_STATE_REQUEST_CODE)
                        }
                    } else {
                        it(false)
                    }
                }
                return false
            } else {
                onRequestPermissionsResult2?.let {
                    it(true)
                }
                maxNfcRequest = 0
                return true
            }
        } else {
            onRequestPermissionsResult2?.let {
                it(true)
            }
            return true
        }
    }


    //fixme 打开，关闭蓝牙的权限(打开蓝牙时，系统自己会主动弹出询问框，关闭蓝牙时不会。)
    //fixme 搜索設備時，必須需要动态申请，主要申请的是定位权限。需要定位权限才能搜索。（6.0以后需要定位权限）
    //fixme 部分机型不仅需要位置权限，还需要打开位置服务。打开GPS开关才能搜索到蓝牙设备，如oppo手机。最好打开。位置服务跳转到KIntentUtils里面。
    //fixme 蓝牙长时间打开之后，可能也无法搜索到设备，需要关闭蓝色再重启。如果连接不上，可以也需要重启蓝牙。
    fun requestPermissionsBlueTooth(activity: Activity? = getActivity(), onRequestPermissionsResult2: ((isAllow: Boolean) -> Unit)? = null): Boolean {
        perMissionType = perMissionTypeBLUETOOTH
        if (isVersion23()) {
            if (ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionsResult2?.let {
                    if (isEffectiveActivity(activity)) {
                        this.onRequestPermissionsResult = it
                        // 申请权限
                        ActivityCompat.requestPermissions(activity!!, DANGEROUS_PERMISSION_BLUE_TOOTH, READ_PHONE_STATE_REQUEST_CODE)
                    } else {
                        it(false)
                    }
                }
                return false
            } else {
                onRequestPermissionsResult2?.let {
                    it(true)
                }
                return true
            }
        } else {
            onRequestPermissionsResult2?.let {
                it(true)
            }
            return true
        }
    }


    //fixme 获取打开或关闭WIFI的权限;没有用的。始终返回true。无法判断。
    //fixme 没关系。每次打开或关闭wifi时。系统会自动跳出询问框。每次打开或关闭都会询问。不会给你真正的权限。
    fun requestPermissionsWifi(activity: Activity? = getActivity(), onRequestPermissionsResult2: ((isAllow: Boolean) -> Unit)? = null): Boolean {
        perMissionType = perMissionTypeWifi
        if (isVersion23()) {
            if (ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.CHANGE_WIFI_MULTICAST_STATE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.CHANGE_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionsResult2?.let {
                    if (isEffectiveActivity(activity)) {
                        this.onRequestPermissionsResult = it
                        // 申请权限
                        ActivityCompat.requestPermissions(activity!!, DANGEROUS_PERMISSION_ACCESS_WIFI_STATE, READ_PHONE_STATE_REQUEST_CODE)
                    } else {
                        it(false)
                    }
                }
                return false
            } else {
                onRequestPermissionsResult2?.let {
                    it(true)
                }
                return true
            }
        } else {
            onRequestPermissionsResult2?.let {
                it(true)
            }
            return true
        }
    }


    //添加桌面快捷键权限申请
    //fixme 小米，无法判断。小米桌面快捷方式的权限判断，始终返回的都是true。还是需要用户手动去设置界面去设置。这个权限判断是个Bug，估计是系统故意的。
    fun requestPermissionsLaunch(activity: Activity? = getActivity(), onRequestPermissionsResult2: ((isAllow: Boolean) -> Unit)? = null): Boolean {
        perMissionType = perMissionTypeLauncher
        if (isVersion23()) {
            if (ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.INSTALL_SHORTCUT) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.UNINSTALL_SHORTCUT) != PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionsResult2?.let {
                    if (isEffectiveActivity(activity)) {
                        this.onRequestPermissionsResult = it
                        // 回调不为空才会申请权限
                        ActivityCompat.requestPermissions(activity!!, DANGEROUS_PERMISSION_LAUNCHER_SHORTCUT, READ_PHONE_STATE_REQUEST_CODE)
                    } else {
                        it(false)
                    }
                }
                return false
            } else {
                onRequestPermissionsResult2?.let {
                    it(true)
                }
                return true
            }
        } else {
            onRequestPermissionsResult2?.let {
                it(true)
            }
            return true
        }
    }

    //安卓8.0系统安装app 未知来源权限。其实这个权限不需要动态去申请。安装应用的时候，会自己去动态申请。
    //为了以防万一，这个权限的申请还是加上。虽然没什么用。
    //fixme 这个动态申请没什么用，不管是允许还拒绝，都是返回false。和桌面快捷方式一样。无法判断。不过这个不用去动态申请。跳转到安装界面时会自己提示的。
    fun requestPermissionsInstallApp(activity: Activity? = getActivity(), onRequestPermissionsResult2: ((isAllow: Boolean) -> Unit)? = null): Boolean {
        perMissionType = perMissionTypeInstallApp
        if (isVersion23()) {
            if (ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.REQUEST_INSTALL_PACKAGES) != PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionsResult2?.let {
                    if (isEffectiveActivity(activity)) {
                        this.onRequestPermissionsResult = it
                        // 申请权限
                        ActivityCompat.requestPermissions(activity!!, DANGEROUS_PERMISSION_INSTALL_PACKAGES, READ_PHONE_STATE_REQUEST_CODE)
                    } else {
                        it(false)
                    }
                }
                return false
            } else {
                onRequestPermissionsResult2?.let {
                    it(true)
                }
                return true
            }
        } else {
            onRequestPermissionsResult2?.let {
                it(true)
            }
            return true
        }
    }

    //fixme 系统悬浮窗权限申请(是系统隐私级别的，也无法判断。始终返回fasle)；未知来源权限，桌面快捷方式，系统悬浮窗权限；都无法判断是否获取。只能用户手动去开。
    //虽然无法判断，但是好像创建悬浮窗的时候，会自己主动去询问。就和安装未知来源一样。会主动弹框询问！
    fun requestPermissionsSystemWindow(activity: Activity? = getActivity(), onRequestPermissionsResult2: ((isAllow: Boolean) -> Unit)? = null): Boolean {
        perMissionType = perMissionTypeWindow
        if (isVersion23()) {
            if (ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionsResult2?.let {
                    if (isEffectiveActivity(activity)) {
                        this.onRequestPermissionsResult = it
                        // 申请权限
                        ActivityCompat.requestPermissions(activity!!, DANGEROUS_PERMISSION_SYSTEM_ALERT_WINDOW, READ_PHONE_STATE_REQUEST_CODE)
                    } else {
                        it(false)
                    }
                }
                return false
            } else {
                onRequestPermissionsResult2?.let {
                    it(true)
                }
                return true
            }
        } else {
            onRequestPermissionsResult2?.let {
                it(true)
            }
            return true
        }
    }

    //fixme 最新悬浮窗权限判断；亲测有效！(建议使用)
    fun requestPermissionsCanDrawOverlays(activity: Activity? = getActivity(), isStatus: Boolean = true, isTransparent: Boolean = false, onRequestPermissionsResult2: ((isAllow: Boolean) -> Unit)? = null) {
        if (activity != null && !activity.isFinishing) {
            //权限判断
            if (Build.VERSION.SDK_INT >= 23) {
                if (!Settings.canDrawOverlays(activity.getApplicationContext())) {
                    onRequestPermissionsResult2?.let {
                        if (isEffectiveActivity(activity)) {
                            KTimiAlertDialog(activity, isStatus, isTransparent).apply {
                                mession(KPermissionUtils.getString(R.string.kpermissionCanDrawOverlays))//是否开启悬浮窗权限
                                negative {
                                    it(false)//取消，权限申请失败
                                    onDestroy()//自动销毁
                                }
                                positive(KPermissionUtils.getString(R.string.kkaiqi)) {
                                    this@KPermissionUtils.onRequestPermissionsResult = onRequestPermissionsResult2//在KBaseAppCompatActivity回调。
                                    // 申请权限
                                    try {
                                        //启动Activity让用户授权(一般都是跳转到权限设置界面，自己去开启)
                                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
                                        activity.startActivityForResult(intent, requestCode_CanDrawOverlays)//魅族可能会异常。
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        KIntentUtils.goPermissionsSetting(activity, requestCode_CanDrawOverlays)
                                    }
                                }
                                show()//显示
                            }

                        } else {
                            it(false)
                        }
                    }
                    return;
                } else {
                    //执行6.0以上绘制代码
                    onRequestPermissionsResult2?.let {
                        it(true)
                    }
                }
            } else {
                //执行6.0以下绘制代码、
                onRequestPermissionsResult2?.let {
                    it(true)
                }
            }
        }
    }

    //判断权限数组是否全部都授权，有一个没有授权都返回false。全部授权才返回true
    fun judgePermission(grantResults: IntArray): Boolean {
        var b = true
        for (i in grantResults.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                b = false//没有授权
            }
        }
        return b
    }

    fun getContent(activity: Activity? = getActivity()): View? {
        if (isEffectiveActivity(activity)) {
            return activity?.getWindow()?.getDecorView()?.findViewById(android.R.id.content)
        }
        return null
    }

    //消息提示
    fun Snackbarmake(activity: Activity? = getActivity(), info: String) {
        getContent(activity)?.let {
            val snackbar = Snackbar.make(it, info, Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.ksetting)) {
                        //立即设置
//                        val packageURI = Uri.parse("package:" + activity!!.getPackageName())
//                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI)
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                        activity.startActivity(intent)//跳转权限设置界面。基本上通用。小米是肯定行的。android6.0基本都可以。
                        KIntentUtils.goPermissionsSetting(activity)//fixme 跳转权限设置界面
                    }
            val snackView = snackbar.view
            //int textSize= (int) (UtilProportion.getInstance(this).adapterInt(24) * UtilProportion.getInstance(this).getTextProportion()/ UtilProportion.getInstance(this).getDensity());
            snackView.setBackgroundColor(Color.parseColor("#61A465"))//浅绿色背景
            //snackView.setBackgroundResource(R.drawable.shape_drawable_snackbar);
            val snackbar_text = snackView.findViewById<View>(R.id.snackbar_text) as TextView
            snackbar_text.setTextColor(Color.parseColor("#ffffff"))//设置通知文本的颜色，白色
            //snackbar_text.setTextSize(textSize);
            val snackbar_action = snackView.findViewById<View>(R.id.snackbar_action) as TextView
            snackbar_action.setTextColor(Color.parseColor("#FF3B80"))//点击文本的颜色,绯红
            //snackbar_action.setTextSize(textSize);
            //snackbar_action.setBackground(null);
            snackbar_action.setBackgroundDrawable(null)
            snackbar.show()
        }
    }

    var onRequestPermissionsResult: ((isAllow: Boolean) -> Unit)? = null
    //权限请求成功回调，返回参数为属于什么类型的权限申请。
    fun onRequestPermissionsResult(isAllow: Boolean) {
        //isAllow true权限允许，false权限禁止
        onRequestPermissionsResult?.let {
            it(isAllow)
            onRequestPermissionsResult = null
        }
        this.onRequestPermissionsResult
    }

    //权限申请失败时，显示的界面，需要手动调用。如果不喜欢这个界面。需要自己去额外实现。
    open fun showFailure(activity: Activity? = getActivity(), perMissionType: Int = KPermissionUtils.perMissionType) {
        var info: String? = null
        if (perMissionType == perMissionTypeCamera || perMissionType == perMissionTypeVideo) {
            info = getString(R.string.perMissionTypeCamera)//相机
        } else if (perMissionType == perMissionTypeStorage) {
            info = getString(R.string.perMissionTypeStorage)//存储卡
        } else if (perMissionType == perMissionTypeRecording) {
            info = getString(R.string.perMissionTypeRecording)//录音
        } else if (perMissionType == perMissionTypeReadPhoneState) {
            info = getString(R.string.perMissionTypeReadPhoneState)//手机状态
        } else if (perMissionType == perMissionTypeLauncher) {
            info = getString(R.string.perMissionTypeLauncher)//桌面快捷方式
        } else if (perMissionType == perMissionTypeInstallApp) {
            info = getString(R.string.perMissionTypeInstallApp)//安装未知来源app
        } else if (perMissionType == perMissionTypeLocation) {
            info = getString(R.string.perMissionTypeLocation)//定位
        } else if (perMissionType == perMissionTypeWindow) {
            info = getString(R.string.perMissionTypeWindow)//悬浮窗
        } else if (perMissionType == perMissionTypeWifi) {
            info = getString(R.string.perMissionTypeWifi)//wifi
        } else if (perMissionType == perMissionTypeAccount) {
            info = getString(R.string.perMissionTypeAccount)//手机通讯录
        } else if (perMissionType == perMissionTypeSMS) {
            info = getString(R.string.perMissionTypeSMS)//读取手机短信
        } else if (perMissionType == perMissionTypeBLUETOOTH) {
            info = getString(R.string.perMissionTypeBLUETOOTH)//打开关闭蓝牙的权限
        } else if (perMissionType == perMissionTypeNFC) {
            info = getString(R.string.perMissionTypeNFC)//NFC权限
        } else if (perMissionType == perMissionTypeALL) {
            info = getString(R.string.perMissionTypeALL)//几个常用的基本权限。
        }
        if (info != null) {
            Snackbarmake(activity, info)
        }
    }

    //权限申请结果
    fun onRequestPermissionsResult(activity: Activity, requestCode: Int, permissions: Array<String>, grantResults: IntArray) {//grantResults就是之前申请的权限数组。一模一样。权限的数量和顺序都一模一样。
        if (requestCode == READ_PHONE_STATE_REQUEST_CODE && grantResults.size > 0) {//一定要判断一下grantResults是否大于0，防止他脑抽。
            //if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {//PackageManager.PERMISSION_GRANTED 授权成功
            if (judgePermission(grantResults)) {
                // 权限授予成功
                onRequestPermissionsResult(true)
            } else {
                onRequestPermissionsResult(false)
            }
        }
    }

}
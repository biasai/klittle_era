package cn.oi.klittle.era.utils

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import cn.oi.klittle.era.base.KBaseActivityManager
import cn.oi.klittle.era.base.KBaseApplication
import androidx.core.content.FileProvider
import cn.oi.klittle.era.R
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.comm.KToast
import cn.oi.klittle.era.exception.KCatchException
import java.io.File


object KIntentUtils {

    private fun getContext(): Context {
        return KBaseApplication.getInstance()
    }

    private fun getActivity(): Activity? {
        return KBaseActivityManager.getInstance().stackTopActivity
    }

    //跳转到系统设置界面
    fun goSetting(activity: Activity? = getActivity()) {
        try {
            if (activity != null && !activity.isFinishing) {
                val intent = Intent(Settings.ACTION_SETTINGS)
                activity.startActivity(intent)//跳转权限设置界面。基本上通用。小米是肯定行的。android6.0基本都可以。
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //判断是否具备修改系统设置的权限。
    fun isWriteSetting(activity: Activity? = getActivity()): Boolean {
        try {
            if (Build.VERSION.SDK_INT >= 23) {//23是6.0
                if (activity != null && !activity.isFinishing) {
                    return Settings.System.canWrite(activity)
                } else {
                    return false
                }
            } else {
                return true
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return false
    }

    //跳转到可修改系统设置界面；清单文件中需要android.permission.WRITE_SETTINGS，否则打开的设置页面开关是灰色的
    fun goWriteSetting(activity: Activity? = getActivity()) {
        try {
            if (activity != null && !activity.isFinishing) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 跳转到设置里面的应用列表界面
     */
    fun goSettingApps(activity: Activity? = getActivity()) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
            activity?.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * fixme 根据包名，跳转到其他应用的详情页面（如：PDA上面的网络位置包名是："com.baidu.map.location"）
     * @param activity
     * @param packageName 要跳转应用详情页的包名。即第三方应用包名。
     * @return true 跳转成功，false跳转失败（没有该应用）
     */
    fun goAppDetailedSetting(activity: Activity? = getActivity(), packageName: String): Boolean {
        try {
            if (activity != null && !activity.isFinishing && packageName != null) {
                //获取所有应用
                var hasPackage = KAppUtils.isAppInstalled(activity, packageName)//fixme 判断是否包含该app(即判断是否安装)
                if (hasPackage) {
                    var intent = KIntentPersionSettingUtils.getAppDetailSettingIntent(packageName)
                    activity.startActivity(intent)
                    return true
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return false
    }

    //fixme 跳转到本应用详情界面（里面有清除应用全部缓存数据的方法，系统自带的。）
    fun goAppDetailedSetting(activity: Activity? = getActivity()) {
        try {
            if (activity != null && !activity.isFinishing) {
                //val packageURI = Uri.parse("package:" + activity.getPackageName())
                //val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI)
                var intent = KIntentPersionSettingUtils.getAppDetailSettingIntent(activity)
                activity.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    //fixme 跳转到权限设置界面(小米，魅族，华为可以直接跳转到应用权限设置界面，其他的只能跳转的应用详情界面(详情里面可以进入权限设置))
    fun goPermissionsSetting(activity: Activity? = getActivity(), requestCode: Int? = null) {
        try {
            var requestCode = requestCode?.toString()
            KIntentPersionSettingUtils.goPermisssion(activity, requestCode)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //跳转到安装未知应用权限设置界面; KPermissionUtils.requestCanRequestPackageInstalls {  } 这个是判断未知安装权限。
    fun goUnKnownAppSources(activity: Activity? = getActivity()) {
        try {
            if (activity != null && !activity.isFinishing) {
                var packageURI = Uri.parse("package:" + activity.getPackageName())
                //注意这个是8.0新API
                var intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                activity.startActivity(intent);
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    //fixme 跳转到声音设置界面(控制声音大小，亲测有效。)
    fun goSoundSetting(activity: Activity? = getActivity()) {
        try {
            if (activity != null && !activity.isFinishing) {
                val intent = Intent(Settings.ACTION_SOUND_SETTINGS)
                activity.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //跳转到个人字典设置界面
    fun goDictionarySetting(activity: Activity? = getActivity()) {
        try {
            if (activity != null && !activity.isFinishing) {
                val intent = Intent(Settings.ACTION_USER_DICTIONARY_SETTINGS)
                activity.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * "021-80370889" 座机号码，格式就这样，系统可以识别出来，直接拔掉。(手机，座机都能识别)
     * 拨打电话（跳转到拨号界面，用户手动点击拨打,不需要要权限。）
     *
     * @param phoneNum 电话号码
     */
    fun goCallPhone(activity: Activity? = getActivity(), phoneNum: String) {
        try {
            if (activity != null && !activity.isFinishing) {
                val intent = Intent(Intent.ACTION_DIAL)
                val data = Uri.parse("tel:$phoneNum")
                intent.data = data
                activity.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 跳转到短信(亲测有效)
     * @param smsBody 短信内容
     * @param phoneNum 手机号码（可以为空）
     */
    fun goSMS(activity: Activity? = getActivity(), smsBody: String, phoneNum: String? = null) {
        try {
            if (activity != null && !activity.isFinishing) {
                //val intent = Intent(Intent.ACTION_MAIN)
                //intent.setType("vnd.android-dir/mms-sms")
                var uri: Uri? = null
                if (phoneNum != null && phoneNum.trim().length > 0) {
                    uri = Uri.parse("smsto:" + phoneNum)
                } else {
                    uri = Uri.parse("smsto:")//就算手机号为空，uri也不能为空，不然异常。
                }
                val intent = Intent(Intent.ACTION_SENDTO, uri)
                intent.putExtra("sms_body", smsBody)
                activity.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            KLoggerUtils.e("发送短信异常：\t" + e.message, isLogEnable = true)
        }
    }

    val requestCode_people = 3811;
    var peopleCallback: ((name: String?, tel: String?) -> Unit)? = null
//                        fixme 获取联系人，调用案例。
//                        KIntentUtils.goPeople() { name, tel ->
//                            KLoggerUtils.e("姓名：\t" + name + "\t手机号：\t" + tel+"\t"+KStringUtils.getTelStr(tel)+"\t"+KStringUtils.getTelStr2(tel))
//                        }
    /**
     * fixme 跳转手机系统通讯录，并获取联系人姓名和号码。
     * @param peopleCallback 回调。返回联系人姓名和手机号。
     */
    fun goPeople(activity: Activity? = getActivity(), peopleCallback: ((name: String?, tel: String?) -> Unit)? = null) {
        try {
            if (activity != null && !activity.isFinishing) {
                KPermissionUtils.requestPermissionsAccount(activity) {
                    if (it) {
                        //需要手机通讯录权限
//                        <!--获取通讯录权限-->
//                        <uses-permission android:name="android.permission.READ_CONTACTS" />
//                        <uses-permission android:name="android.permission.READ_PHONE_STATE" />
                        this.peopleCallback = peopleCallback
                        //var uri: Uri? = Uri.parse("content://contacts/people");//fixme 这个会弹窗 联系人，文件夹选择框。
                        //var intent = Intent(Intent.ACTION_PICK, uri)
                        var intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)//fixme 这个直接跳转到手机联系人。
                        activity.startActivityForResult(intent, requestCode_people)
                    } else {
                        KPermissionUtils.showFailure(activity)//显示申请权限失败提示。
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            this.peopleCallback = null
            KLoggerUtils.e("跳转通讯录联系人异常：\t" + e.message, isLogEnable = true)
        }
    }

    /**
     * 跳转到wifi设置界面！
     */
    fun goWifiSetting(activity: Activity? = getActivity()) {
        try {
            if (activity != null && !activity.isFinishing) {
                val intent = Intent("android.net.wifi.PICK_WIFI_NETWORK")
                activity.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 跳转到移动网络设置界面！
     */
    fun goNetSetting(activity: Activity? = getActivity()) {
        try {
            if (activity != null && !activity.isFinishing) {
                val intent = Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)
                activity.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 跳转到日期设置界面！
     */
    fun goDateSetting(activity: Activity? = getActivity()) {
        try {
            if (activity != null && !activity.isFinishing) {
                val intent = Intent(Settings.ACTION_DATE_SETTINGS)
                activity.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     *  跳转系统浏览器
     *  其中url不能有空格。 .trim() 就可以了
     *  并且网址格式必须正确，必须有 http://  不然报错。
     */
    fun goBrowser(activity: Activity? = getActivity(), url: String) {
        try {
            if (activity != null && !activity.isFinishing) {
                val intent = Intent()
                intent.action = "android.intent.action.VIEW"
                val content_url = Uri.parse(url.trim())
                intent.data = content_url
                activity.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //跳转到语言设置界面
    fun goLanguageSetting(activity: Activity? = getActivity()) {
        try {
            if (activity != null && !activity.isFinishing) {
                var intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //跳转输入法切换界面(键盘管理);fixme 控制输入法是否开启；
    fun goInputSetting(activity: Activity? = getActivity()) {
        try {
            if (activity != null && !activity.isFinishing) {
                try {
                    var intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                    //intent.setAction("android.settings.INPUT_METHOD_SETTINGS");
                    activity.startActivity(intent)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //fixme 跳转输入法切换弹窗（输入法默认切换弹窗）；只显示开启的输入法。
    fun goInputSettingDialog(activity: Activity? = getActivity()) {
        try {
            if (activity != null && !activity.isFinishing) {
                try {
                    (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //跳转到更多连接方式
    fun goMoreConnectedSetting(activity: Activity? = getActivity()) {
        try {
            if (activity != null && !activity.isFinishing) {
                val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //跳转到NFC设置界面(目前和更多链接方式跳转的是同一个界面)
    fun goNFCSetting(activity: Activity? = getActivity()) {
        try {
            if (activity != null && !activity.isFinishing) {
                val intent = Intent(Settings.ACTION_NFC_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            KLoggerUtils.e("NFC界面跳转异常：\t" + KCatchException.getExceptionMsg(e), true)
        }
    }

    //跳转到无障碍设置界面
    fun goAccessibilitySetting(activity: Activity? = getActivity()) {
        try {
            if (activity != null && !activity.isFinishing) {
                //ACTION_ACCESSIBILITY_SETTINGS
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    //判断adb调试是否打开(即开发者模式)，打开之后，才能进入开发者选项。
    fun isAdb(): Boolean {
        try {
            val enableAdb = Settings.Secure.getInt(getContext().getContentResolver(), Settings.Secure.ADB_ENABLED, 0) > 0//判断adb调试模式是否打开
            if (enableAdb) {
                return true//adb调试模式已经打开
            } else {
                return false
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return false
    }

    //跳转到开发者选项界面(需要打开adb调试，即开发者模式)
    fun goDevelopment(activity: Activity? = getActivity()) {
        try {
            if (activity != null && !activity.isFinishing && isAdb()) {//判断adb调试是否打开，必须打开
                activity?.apply {
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                        startActivity(intent)
                    } catch (e: Exception) {
                        try {
                            val componentName = ComponentName("com.android.settings", "com.android.settings.DevelopmentSettings")
                            val intent = Intent()
                            intent.component = componentName
                            intent.action = "android.intent.action.View"
                            startActivity(intent)
                        } catch (e1: Exception) {
                            try {
                                val intent = Intent("com.android.settings.APPLICATION_DEVELOPMENT_SETTINGS")//部分小米手机采用这种方式跳转
                                startActivity(intent)
                            } catch (e2: Exception) {

                            }
                        }
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //GPS位置服务，安卓6.0开始，连接BLE(蓝牙4.0)，不仅需要位置权限，还需要打开位置服务。
    //fixme 部分机型打开GPS开关才能搜索到蓝牙设备，如oppo手机。最好打开。
    fun goGPS(activity: Activity? = getActivity()) {
        try {
            if (activity != null && !activity.isFinishing) {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    //fixme 判断GPS定位服务是否打开
    fun isGpsEnable(): Boolean {
        try {
            var locationManager = getContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            var network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (gps || network) {
                return true
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 跳转到蓝牙系统设置界面
     */
    fun goBluetoothSetting(activity: Activity? = getActivity()) {
        try {
            if (activity != null && !activity.isFinishing) {
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                activity.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    val BLUTOOTH_REQUESTCODE_DISCOVER = 357

    //fixme 回调。(在BaseActivity的onActivityResult方法中已经配置好)
    var BluetoothDiscoverCallback: ((isOpen: Boolean) -> Unit)? = null

    /**
     * 让其他的设备在120秒内，能够检测到你的手机（蓝牙）。6.0之后，会自己弹出询问框。亲测可行。
     * @param seconds 可见时间。单位秒
     * @param callback 回调。
     */
    fun bluetoothDiscoverable(seconds: Int = 120, callback: ((b: Boolean) -> Unit)? = null) {
        try {
            var enabler = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            // 第二个参数可设置的范围是0~3600秒，在此时间区间（窗口期）内可被发现
            // 任何不在此区间的值都将被自动设置成120秒。
            enabler.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, seconds);
            getActivity()?.startActivityForResult(enabler, BLUTOOTH_REQUESTCODE_DISCOVER)
            this.BluetoothDiscoverCallback = callback
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //fixme 回调。(在BaseActivity的onActivityResult方法中已经配置好)
    val BLUTOOTH_REQUEST_ENABLE_BT = 257

    //蓝牙打开回调。true打开，false没有打开
    var BluetoothOpenCallback: ((isOpen: Boolean) -> Unit)? = null

    /**
     * 打开蓝牙。会弹出蓝牙是否打开提示框(亲测可行，如果蓝牙已经打开了，则不会弹提示框),不管蓝牙是否打开，都会回调。
     * fixme 打开蓝牙时，这个自带系统打开动画。自带蓝牙打开的进度过程。
     * @param callback 回调。
     */
    fun bluetoothOpen(callback: ((b: Boolean) -> Unit)? = null) {
        try {
            //fixme BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE 这个是设备可见的操作，不是关闭。
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)//fixme 只有打开的操作，没有关闭的操作
            getActivity()?.startActivityForResult(enableBtIntent, BLUTOOTH_REQUEST_ENABLE_BT)
            this.BluetoothOpenCallback = callback
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //跳转到打印设置界面
    fun goPrintSetting(activity: Activity? = getActivity()) {
        try {
            if (activity != null && !activity.isFinishing) {
                val intent = Intent(Settings.ACTION_PRINT_SETTINGS)
                activity.startActivity(intent)//跳转权限设置界面。基本上通用。小米是肯定行的。android6.0基本都可以。
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    //跳转到桌面
    fun goHome(activity: Activity? = getActivity()) {
        try {
            if (activity != null && !activity.isFinishing) {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)//防止报错
                activity.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * fixme 通过第三方软件，打开文件。如excel表格文件等。
     * @param filePath 文件的完整地址，包括后缀名
     */
    fun goOpenFile(activity: Activity? = getActivity(), filePath: String?) {
        if (filePath == null) {
            KToast.showError(KBaseUi.getString(R.string.fileNotEmpty))//文件不能为空
            return
        } else {
            goOpenFile(activity, File(filePath))
        }
    }

    /**
     * fixme 通过第三方软件，打开文件。如excel表格文件等。
     * @param file 要打开的文件；亲测 KPathManagerUtils路径里的文件都能打开。
     * fixme 文件不管是在SD卡上，还是在私有目录上。都能打开; 如果打不开，说明文件本身已损坏。
     */
    fun goOpenFile(activity: Activity? = getActivity(), file: File?) {
        if (file == null) {
            KToast.showError(KBaseUi.getString(R.string.fileNotEmpty))//文件不能为空
            return
        }
        try {
            if (activity != null && !activity.isFinishing) {
                if (file.length() <= 0) {
                    KToast.showError(KBaseUi.getString(R.string.fileLength0))//文件不存在
                    return
                }
                var fileSuffix = KFileUtils.getInstance().getFileSuffix(file, true).toLowerCase().trim();//文获取件名后缀，全部转小写,
                if (fileSuffix.equals(".apk")) {
                    KAppUtils.installation(activity, file)//fixme apk安装。
                    return
                }
                val intent = Intent("android.intent.action.VIEW")
                intent.addCategory("android.intent.category.DEFAULT")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)//防止报错
                var fileUri: Uri? = null
                if (Build.VERSION.SDK_INT >= 23) {//7.0及以上版本(版本号24),为了兼容6.0(版本号23)，防止6.0也可能会有这个问题。
                    //getPackageName()和${applicationId}显示的都是当前应用的包名。无论是在library还是moudle中，都是一样的。都显示的是当前应用moudle的。与类库无关。请放心使用。
                    fileUri = FileProvider.getUriForFile(activity, activity.packageName + ".kera.provider", //与android:authorities="${applicationId}.provider"对应上
                            file)
                } else {
                    fileUri = Uri.fromFile(file)
                }
                //application/msword 文档
                //application/vnd.ms-excel 表格
                //参考地址：https://blog.csdn.net/u010229714/article/details/74530611/
                if (fileSuffix.contains(".docx") || fileSuffix.contains(".doc")) {
                    intent.setDataAndType(fileUri, "application/msword");
                } else if (fileSuffix.contains(".xlsx") || fileSuffix.contains(".xls")) {
                    //KLoggerUtils.e(".xlsx表格")
                    intent.setDataAndType(fileUri, "application/vnd.ms-excel");
                } else if (fileSuffix.contains(".ppt")) {
                    intent.setDataAndType(fileUri, "application/vnd.ms-powerpoint");
                } else if (fileSuffix.contains(".pdf")) {
                    intent.setDataAndType(fileUri, "application/pdf");
                } else if (fileSuffix.contains(".png") || fileSuffix.contains(".jpg") || fileSuffix.contains(".3gp") || fileSuffix.contains(".psd")) {
                    intent.setDataAndType(fileUri, "image/*");
                } else if (fileSuffix.contains(".html") || fileSuffix.contains(".htm") || fileSuffix.contains(".jsp") || fileSuffix.contains(".aspx")) {
                    intent.setDataAndType(fileUri, "text/html");
                } else {
                    intent.setDataAndType(fileUri, "text/plain");
                }
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                activity.startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            //没有安装第三方的软件会提示
            KToast.showError(KBaseUi.getString(R.string.fileNotExe))//没有找到打开该文件的应用程序
            e.printStackTrace()
            KLoggerUtils.e("调用第三方打开文件异常：\t" + KCatchException.getExceptionMsg(e), true)
        }
    }

    var isGoRest: Boolean = false//fixme 判断是否为手动重启

    //fixme App重启
    fun goRest() {
        goRest(getActivity())
    }

    private var restTime = "KGOREST_TIME"//记录重启时间
    fun goRest(activity: Activity?) {
        activity?.let {
            if (!it.isFinishing) {
                //fixme 重启最好在主线程中进行。效果最好。不会出现其他问题。
                //fixme 主线程中重启，NFC刷卡才会正常。子线程中不行，刷卡可能依然无效。所以最好在主线程中进行重启。
                it.runOnUiThread {
                    try {
                        if (activity != null && !activity.isFinishing) {
                            var isGoRest = true
                            KCacheUtils.getCache().getAsObject(restTime)?.let {
                                if (it is Long) {
                                    //KLoggerUtils.e("重启时间2：\t"+(System.currentTimeMillis() - it))
                                    if ((System.currentTimeMillis() - it) < 1500) {//两次重启间隔时间不能少于1.5秒。手动操作最快一般都在1051。比一秒大。
                                        isGoRest = false//fixme 防止应用异常无限重启卡死。
                                    }
                                }
                            }
                            //KLoggerUtils.e("是否重启：\t"+isGoRest)
                            if (isGoRest) {
                                KCacheUtils.getCache().put(restTime, System.currentTimeMillis())//fixme 保存当前重启的时间。
                                var intent = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName())
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                isGoRest = true//fixme 重启标志；在KBaseActivity里的onCreate()方法里，判断充值。
                                activity.startActivity(intent)
                                android.os.Process.killProcess(android.os.Process.myPid());//fixme 殺進程，不然重啟無效果。（杀进程之后，重启亲测有效。）
                            }
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        KLoggerUtils.e("App重启异常：\t" + KCatchException.getExceptionMsg(e), true)
                    }
                }
            }
        }
    }

}
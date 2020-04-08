package cn.oi.klittle.era.https.ko

import android.app.Activity
import android.content.Context
import cn.oi.klittle.era.base.KBaseActivityManager
import cn.oi.klittle.era.base.KBaseApplication
import cn.oi.klittle.era.base.KBaseDialog
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.dialog.KProgressDialog
import cn.oi.klittle.era.https.KHttp
import cn.oi.klittle.era.gson.KGsonUtils
import cn.oi.klittle.era.utils.KLoggerUtils
import java.io.File
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.Deferred

open class KHttps() {

    companion object {

        var isNetting: Boolean = false//fixme 全局，判断网络是否正在进行中。

        fun getContext(): Context {
            return KBaseApplication.getInstance()
        }

        open fun getActivity(): Activity? {
            return KBaseActivityManager.getInstance().stackTopActivity//获取当前栈顶Activity
        }

        /**
         * getColor()这个方法系统已经有了，不能再重载
         * 获取颜色值（默认从Resources目录，从color文件中获取）
         */
        open fun getColor(id: Int): Int {
            return KBaseUi.getContext().getResources().getColor(id)
        }

        /**
         * 默认就从Res目录下读取
         * 获取String文件里的字符,<string name="names">你好%s</string>//%s 是占位符,位置随意
         * @param formatArgs 是占位符
         */
        open fun getString(id: Int, formatArgs: String? = null): String {
            if (formatArgs != null) {
                return KBaseUi.getContext().resources.getString(id, formatArgs) as String
            }
            return KBaseUi.getContext().getString(id) as String
        }

        /**
         * 获取String文件里的字符串數組
         */
        open fun getStringArray(id: Int): Array<String> {
            return KBaseUi.getContext().resources.getStringArray(id)
        }

        open var isFirstError: Boolean = true//是否为第一次错误(第一次不需要判断时间)。
        open var errorTime = System.currentTimeMillis()//错误时间
        open var errorTimeInterval = 5000//错误间隔时间，单位毫秒；即5秒内不会重复调用。
        //失败回调(一般都服务器连接失败)
        open var error: ((url: String?, errStr: String?, isCacle: Boolean, hasCahe: Boolean, cacleInfo: String?) -> Unit)? = null

        //调用案例：KHttps.onError {}

        //fixme 与 onFailure()回调是一样的。唯一的不同就是，onFailure每个实例都有一个，onError只有一个；[即新调用的会覆盖旧调用的。]
        //fixme 即所有网络请求错误，都会执行这个方法；是全局的。[不会置空；必须手动值空。]
        //error 错误信息（onFailure返回的信息），isCacle 是否缓存，hasCahe 是否有缓存数据(false缓存数据为空)，cacleInfo缓存标志（可以根据需求自定义）。
        fun onError(error: ((url: String?, errStr: String?, isCacle: Boolean, hasCahe: Boolean, cacleInfo: String?) -> Unit)? = null) {
            this.error = error
        }

        public open var progressbar2: KBaseDialog? = null//共用弹窗;在KBaseActivity里的finish()会自动销毁。
        public open var progressbar2Count = 0//保存记录共享弹窗的数量。在KBaseActivity里的finish()和onCreate()会自动清0。
    }

    /**
     * 回调参数；后来加的。与http本身的回调方法不冲突。二者并列。
     */
    var requestCallback: KRequestCallback? = null

    fun requestCallback(requestCallback: KRequestCallback? = null) {
        this.requestCallback = requestCallback
    }

    /**
     * fixme 这个是用来实现网络轮询的；会在onfinish()中回调。普通请求，没必要使用。
     */
    open var next: (() -> Unit)? = null

    fun onNext(next: (() -> Unit)? = null): KHttps {
        this.next = next
        return this
    }

    var activity: Activity? = null

    fun activity(activity: Activity? = getActivity()): KHttps {
        this.activity = activity
        return this
    }

    open var isUiThread: Boolean = false//是否在主线程回调
    //提供一个Activity，防止Activity为空
    open fun isUiThread(isUiThread: Boolean = true, activity: Activity? = getActivity()): KHttps {
        this.isUiThread = isUiThread
        if (isUiThread) {
            if (activity != null && !activity.isFinishing) {
                this.activity = activity
            }
        } else {
            this.activity = activity
        }
        return this
    }

    //fixme 斜杠 \ ;反斜杠/;双双斜杠 //(等价于斜杠\)
    //fixme 格式(注意最末尾是没有反斜杠的/ ),如：http://test.app.bwg2017.com/glassreceivegood.ashx
    open var url: String? = null

    //fixme 子类可以重写，返回自己的类型。
    open fun url(url: String?): KHttps {
        this.url = url
        return this
    }

    protected open var timeOut = 5000//超时链接时间，单位毫秒,一般500毫秒足已。亲测100%有效。极少数设备可能脑抽无效。不用管它。
    open fun timeOut(timeOut: Int = this.timeOut): KHttps {
        this.timeOut = timeOut
        return this
    }

    open var isShowLoad: Boolean = false//fixme 是否显示进度条，默认不显示
    open fun isShowLoad(isShowLoad: Boolean = true): KHttps {
        this.isShowLoad = isShowLoad
        return this
    }

    open var isLocked: Boolean = true//fixme 网络进度条，是否屏蔽返回键。默认屏蔽。
    open fun isLocked(isLocked: Boolean = true) {
        this.isLocked = isLocked
    }

    //进度条变量名，子类虽然可以重写，但是类型改不了。所以。进度条就不允许继承了。子类自己去定义自己的进度条。
    public open var progressbar: KProgressDialog? = null//进度条(Activity不能为空，Dialog需要Activity的支持)

    //fixme 显示进度条[子类要更改进度条，可以重写这个]
    //重写的时候，注意屏蔽父类的方法，屏蔽 super.showProgress()
    open fun showProgressbar() {
        if (isShowLoad) {
            //进度条必须在主线程中实例化
            if (activity == null) {
                activity = getActivity()
            }
            activity?.let {
                if (!it.isFinishing) {
                    it.runOnUiThread {
                        try {
                            if (!isDismissProgressbar) {
                                if ((progressbar == null || progressbar?.dialog == null) && activity != null) {
                                    if (isSharingDialog) {
                                        progressbar2?.let {
                                            it.ctx?.let {
                                                if (it is Activity) {
                                                    if (it == activity && progressbar2 is KProgressDialog) {
                                                        progressbar2?.isShow()?.let {
                                                            if (it) {
                                                                progressbar2?.isDestory?.let {
                                                                    if (!it) {
                                                                        progressbar = progressbar2 as KProgressDialog//fixme 同一个Activity共享网络进度条
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (progressbar == null) {
                                        progressbar = KProgressDialog(activity!!, https = this@KHttps)
                                        if (isSharingDialog) {
                                            progressbar2 = progressbar
                                        }
                                    }
                                }
                                progressbar?.isLocked(isLocked)//是否屏蔽返回键
                                progressbar?.show()
                                if (isDismissProgressbar) {
                                    dismissProgressbar2()//fixme 防止網絡請求已經結束了，網絡進度條還未關閉。
                                }
                            }
                        } catch (e: Exception) {
                            //这里异常了不会影响回调进度。服务器（域名错误）异常时，会回调onFailure()
                            //KLoggerUtils.e("网络弹框实例化异常：\t" + e.message+"\t"+progressbar+"\t"+activity)
                        }
                    }
                }
            }
        }
    }

    private var isDismissProgressbar: Boolean = false//fixme 是否已經關閉了進度條。因為進度條是在主線程中進行。所以有可能還沒等網絡條初始化完成，網絡請求就結束了。
    private fun dismissProgressbar2() {
        isDismissProgressbar = true
        progressbar?.let {
            it.dismiss()//关闭弹窗
            it.onDestroy()//销毁
        }
        progressbar = null
    }

    //fixme ++在 KHttp里的Get2（）和Post2（）里记录网络弹窗的个数。在那里计算才准确。
    fun addProgressbarCount() {
        if (isShowLoad && isSharingDialog) {
            if (KHttps.progressbar2Count < 0) {
                KHttps.progressbar2Count = 0
            }
            KHttps.progressbar2Count++//fixme 网络进度条计数++
        }
    }

    //fixme 关闭进度条[子类可以重写,重写的时候，记得对自己的进度条进行内存释放。]
    //重写的时候，注意屏蔽父类的方法，屏蔽 super.showProgress()
    open fun dismissProgressbar() {
        if (isShowLoad) {
            if (isSharingDialog) {
                progressbar2Count--//fixme 网络进度条计算--
                if (progressbar == progressbar2) {
                    //共享弹窗。
                    if (progressbar2Count <= 0) {
                        dismissProgressbar2()//fixme 所以的共享弹窗都结束了，才能关闭。即最后一个弹窗关闭。
                        progressbar2 = null
                    }
                } else {
                    dismissProgressbar2()
                }
            } else {
                //正常关闭弹窗
                dismissProgressbar2()
            }
        }
    }

    open var isSharingDialog: Boolean = false//fixme 是否共用Dialog网络进度弹窗
    open fun isSharingDialog(isSharingDialog: Boolean) {
        this.isSharingDialog = isSharingDialog
    }

    var isJava: Boolean = false//是否在java端运行，true是。false不是（在安卓设备上运行）
    fun isJava(isJava: Boolean = true) {
        this.isJava = isJava
        if (isJava) {
            isShowLoad(false)//fixme 在java端运行；不能显示弹框哦。
            //isURLEncoder=true//java mian() 不支持 Uri.encode(e.value.toString());已经不用担心了，转码的时候做了判断处理。
            isUiThread = false//java mian()是没有安卓的UI主线程的。
        }
    }

    open var javaCacheDir: String = "D:\\java\\cache"//java缓存目录。
    open fun getJaveCacheFile(): File {
        var file = File(javaCacheDir)
        if (!file.exists()) {
            file.mkdirs()//不存在则创建
        }
        return file
    }

    open var cacleInfo: String? = null//fixme 是否显示缓存弹窗信息,会返回给onError(); 在onError()里可以根据需求来自定义信息
    fun cacleInfo(cacleInfo: String? = null): KHttps {
        this.cacleInfo = cacleInfo
        return this
    }

    open var isCacle: Boolean = false//是否缓存(访问失败的时候,会读取缓存)，默认不缓存
    fun isCacle(isCache: Boolean = true): KHttps {
        this.isCacle = isCache
        return this
    }

//                fixme isFirstCacle 正确使用案例;
//                isFirstCacle(true)
//                isCacle(true)
//                isUrlUniqueParams(false)//不要所有参数作为标志
//                urlUniqueParams(src?.trim() + from?.trim() + to?.trim())//挑几个固定参数作为唯一标志

    open var isFirstCacle: Boolean = false//fixme 是否有效读取缓存(如果有缓存,就不访问网络了);fixme 亲测有效
    fun isFirstCacle(isFirstCacle: Boolean = true): KHttps {
        this.isFirstCacle = isFirstCacle
        return this
    }

    //fixme 缓存的条件；即自己判断缓存的条件（一般都是缓存成功的，错误的一般都不缓存）
    //respon 服务器返回的原始数据；true 缓存；false不缓存。
    open var onIsCacle: ((respon: String) -> Boolean)? = null

    fun onIsCacle(onIsCacle: ((respon: String) -> Boolean)? = null): KHttps {
        this.onIsCacle = onIsCacle
        return this
    }

    //fixme 网络取消(不存在真正的网络请求取消,仅仅只是取消了网络回调而已)
    fun cancel() {
        try {
            //fixme 去除网络请求标志(网络请求结束)
            //fixme [放在最前；放在https?.finish之前执行。防止finish()中再次执行网络请求无效。]
            var https: KHttps? = this
            https?.let {
                var key = KHttp.getUrlUnique(it)
                if (KHttp.map.containsKey(key)) {
                    KHttp.map.remove(key)
                }

                var key2 = KHttp.getUrlUnique2(it)
                if (KHttp.map.containsKey(key2)) {
                    KHttp.map.remove(key2)//fixme 去除网络请求标志2
                }

                it.urlUniqueParams = null
            }
            //fixme 关闭进度条
            https?.isShowLoad?.let {
                if (it) {
                    https?.dismissProgressbar()
                }
            }
            https?.finish0?.let {
                it()
            }
            //结束回调（在进度条关闭之后，再回调。防止进度条和activity同时关闭。）
            https?.finish?.let {
                it()
            }
            https?.requestCallback?.finish?.let {
                it()
            }
            //fixme 网络轮询回调
            https?.next?.let {
                GlobalScope.async {
                    it()
                }
            }
            https?.onDestrory()
            https = null
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    open var saveTime: Int? = null//缓存数据时间；单位秒。空null没有时间限制,及永久存储.
    fun saveTime(saveTime: Int? = null): KHttps {
        this.saveTime = saveTime
        return this
    }

    open var isRepeatRequest: Boolean = false//是否允许网络重复请求。默认不允许重复请求。
    fun isRepeat(isRepeatRequest: Boolean = true): KHttps {
        this.isRepeatRequest = isRepeatRequest
        return this
    }

    //fixme 网络唯一标志额外数据,会添加到url的后面,作为该网络请求的唯一标志;
    //之所以要添加这个参数,是因为,如果所有参数里面带有时间标志或签名;那么每次参数肯定都不一样;所有参数作为标志也就没有意义
    //所以利用这个参数,我们可以添加几个固定的参数作为标志.
    open var urlUniqueParams: String? = null

    fun urlUniqueParams(urlUniqueParams: String?): KHttps {
        this.urlUniqueParams = urlUniqueParams
        return this
    }

    open var isUrlUniqueParams: Boolean = false//网络唯一标志是否包含参数；默认不包含；就以url为标识
    fun isUrlUniqueParams(isUrlUniqueParams: Boolean = true): KHttps {
        this.isUrlUniqueParams = isUrlUniqueParams
        return this
    }

    open var isShowParams: Boolean = false//是否显示打印参数，默认不打印
    fun isShowParams(isShowParam: Boolean = true): KHttps {
        this.isShowParams = isShowParam
        return this
    }

    //fixme 开始回调
    open var start: (() -> Unit)? = null

    fun onStart(start: (() -> Unit)? = null): KHttps {
        this.start = start
        return this
    }

    open var start0: (() -> Unit)? = null

    fun onStart0(start0: (() -> Unit)? = null): KHttps {
        this.start0 = start0
        return this
    }

    //fixme 成功回调(返回服务器原始数据)
    open var success: ((result: String) -> Unit)? = null

    fun onSuccess(success: ((result: String) -> Unit)? = null): KHttps {
        this.success = success
        return this
    }

    //新增一个回调（多一个，可以防止冲突）（在onSuccess()的前面。）
    open var success0: ((result: String) -> Unit)? = null

    fun onSuccess0(success0: ((result: String) -> Unit)? = null): KHttps {
        this.success0 = success0
        return this
    }

    //fixme 失败回调
    open var failure: ((errStr: String?) -> Unit)? = null

    fun onFailure(failure: ((errStr: String?) -> Unit)? = null): KHttps {
        this.failure = failure
        return this
    }

    //新增一个回调
    open var failure0: ((errStr: String?) -> Unit)? = null

    fun onFailure0(failure0: ((errStr: String?) -> Unit)? = null): KHttps {
        this.failure0 = failure0
        return this
    }

    //fixme 结束回调，无论是成功还是失败都会调用(最后执行)
    open var finish: (() -> Unit)? = null

    fun onFinish(finish: (() -> Unit)? = null): KHttps {
        this.finish = finish
        return this
    }

    open var finish0: (() -> Unit)? = null

    fun onFinish0(finish0: (() -> Unit)? = null): KHttps {
        this.finish0 = finish0
        return this
    }

    var contentType: String? = "application/json;charset=UTF-8"//fixme java一般都是这个；

    /**
     * fixme 一般服务器无法正常交互，一般都这个问题;
     * fixme .net asp 无法交互时；建议设置为空（为空时，http就不会设置contentType,就会使用默认的。）；
     */
    fun contentType(contentType: String? = null): KHttps {
        this.contentType = contentType
        return this
    }

    //fixme 注意不要转两次(两次无法识别)；只需要转一次。亲测能够保证服务器端正确识别。
    //fixme android.net.Uri.encode是安卓自带的；比URLEncoder.encode更安全(不会把空格转成+加号)；保证服务器端能够正确识别。
    //fixme 用法，Uri.encode(e.value.toString())
    //fixme 对整个body最好不要进行转码；因为对整个body进行转码；会破坏内部数据结构。导致服务器无法识别（无法区分键和值）。
    //fixme 所以这个参数设置对body参数类型无效。需要自己手动对body内的参数手动转码。
    var isURLEncoder: Boolean = true//fixme 是否进行url转码；防止服务器端无法正确识别。如加号等。默认true(进行转码);get,post都有效。

    fun isURLEncoder(isURLEncoder: Boolean = true): KHttps {
        this.isURLEncoder = isURLEncoder
        return this
    }


    //fixme 参数 body (body,param,file独立存在，可以同时使用，亲测可行)

    //参数
    //header头部参数。Get，Post都行
    open val headers: MutableMap<String, String> by lazy { mutableMapOf<String, String>() }
    //params属于 body子集。Get，Post都行
    open val params: MutableMap<String, String?> by lazy { mutableMapOf<String, String?>() }
    //files也属于params，文件上传。Pst请求
    open val files: MutableMap<String, File> by lazy { mutableMapOf<String, File>() }
    //params,files,body都可以同时使用。Post请求
    open var body: String? = null

    //fixme 方法必须放在变量声明之后
    //设置默认参数，以及对参数做一些特殊处理（如加密）。子类可以重写
    //在请求之前会调用。
    protected open fun onPreParameter() {
        //header默认参数
        //headers.put("1", "1")
        //params默认参数
        //params.put("2", "2")
    }

    //fixme 对服务器返回数据最先处理，做一些特殊处理。
    //fixme 如数据解密等（先解密，然后才进行json解析）。子类可以重写
    open fun onPostResponse(response: String): String {
        return response
    }

    open fun body(body: String? = null): KHttps {
        body?.let {
            this.body = body
        }
        return this
    }

    //fixme Body参数
    private var kBody: KBody? = null

    //fixme 之所以设置为私有；就是为了防止传值的时候；传错。
    fun getKBody(): KBody? {
        return kBody
    }

    open fun body(body: KBody?): KHttps {
        body?.let {
            this.kBody = it
        }
        return this
    }

    //添加头部参数
    open fun addHeader(key: String, value: String): KHttps {
        headers.put(key, value)
        return this
    }

    //添加头部参数(融合两个Map)
    open fun addHeader(header: MutableMap<String, String>? = null): KHttps {
        header?.let {
            for ((key, value) in header.entries) {
                headers.put(key, value)
            }
        }
        return this
    }

    open fun addHeader(header: HashMap<String, String>? = null): KHttps {
        header?.let {
            for ((key, value) in header.entries) {
                headers.put(key, value)
            }
        }
        return this
    }

    open fun addParam(key: String, value: String?): KHttps {
        value?.let {
            //           if (!it.trim().equals("") && !it.trim().equals("null") && !it.trim().equals("NULL")) {
//                params.put(key, value)
//            }
            params.put(key, value)//允許為空字符"",JSONObject可以为空字符。
        }
        return this
    }

    open fun addParam(param: MutableMap<String, String>? = null): KHttps {
        param?.let {
            for ((key, value) in param.entries) {
                params.put(key, value)
            }
        }
        return this
    }

    open fun addParam(param: HashMap<String, String>? = null): KHttps {
        param?.let {
            for ((key, value) in param.entries) {
                params.put(key, value)
            }
        }
        return this
    }

    open fun addFile(key: String, value: String): KHttps {
        files.put(key, File(value))
        return this
    }

    open fun addFile(key: String, value: File): KHttps {
        files.put(key, value)
        return this
    }


    open fun addFile(file: MutableMap<String, File>? = null): KHttps {
        file?.let {
            for ((key, value) in file.entries) {
                files.put(key, value)
            }
        }
        return this
    }


    //fixme 上传多张图片(多张图片上传)
    /**
     * @param key fixme 参数名；可以随便取；服务器支持多张图片上传时，参数名已经无所谓了。只要是文件类型；都会接收。
     * @param values 多张图片文件
     */
    open fun addFile(key: String = "files", values: MutableList<File>): KHttps {
        if (values.size > 1) {
            for (i in 0..values.lastIndex) {
                files.put(key + i, values[i])//fixme 键key 服务器会自动识别的。参数名已经无所谓了。只要是文件类型都会接收。
            }
        } else if (values.size == 1) {
            files.put(key, values[0])
        }
        return this
    }

    open fun addFile(key: String = "files", values: ArrayList<File>): KHttps {
        addFile(key, values.toMutableList())//fixme ArrayList 可以转 MutableList；他们两个class类型都是一样的。
        return this
    }

    /**
     * fixme 销毁
     * fixme 这个在KGenericsCallback里的onFinish()中调用；其他地方不要调用
     */
    fun onDestrory() {
        try {
            headers?.clear()
            params?.clear()
            files?.clear()
            body?.let {
                body = null
            }
            kBody?.apply {
                onDestroy()
            }
            kBody = null
            url = null
            activity = null
            next = null
            start0 = null
            start = null
            failure0 = null
            success0 = null
            failure = null
            success = null
            finish0 = null
            finish = null
            requestCallback = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //fixme Get请求,所有参数设置完成之后再调用
    inline fun <reified T> get(vararg field: String, noinline callback: ((t: T) -> Unit)? = null) {
        onPreParameter()
        //fixme kbody参数
        getKBody()?.let {
            var str = it.toString()
            if (str != null && str.length > 0) {
                this.body = it.toString()
            }
        }
        KHttp.LogParams(this)
        KHttp.Get2(url, this, requestCallBack = object : KGenericsCallback(this) {
            override fun onResponse(response: String?) {
                try {
                    if (isCallback()) {
                        callback?.let {
                            response?.let {
                                try {
                                    //fixme 默认返回原始数据String(包括缓存数据)，缓存数据不为空并且开启缓存isCacle==true的时候，会返回缓存数据。
                                    callback(KGsonUtils.parseJSONToAny<T>(it, *field))
                                } catch (e: Exception) {
                                    //防止异常之后，finish()不执行;捕捉之后就没事了
                                    KLoggerUtils.e("get回调处理异常：\t" + e.message)
                                }
                            }
                        }
                    }
                    super.onResponse(response)
                } catch (e: Exception) {
                    e.printStackTrace()
                    KLoggerUtils.e("get回调处理异常2：\t" + e.message)
                }
            }
        }, timeOut = timeOut)
    }


    //fixme 判断是否需要回调。
    public fun isCallback(): Boolean {
        var isCallback = true
        try {
            if (isUiThread) {//主线程
                activity?.let {
                    if (it.isFinishing) {
                        isCallback = false//fixme activity关闭了，则不用再回调了。
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isCallback
    }

    //fixme Post请求,所有参数设置完成之后再调用
    inline fun <reified T> post(vararg field: String, noinline callback: ((t: T) -> Unit)? = null) {
        onPreParameter()
        //fixme kbody参数
        getKBody()?.let {
            var str = it.toString()
            if (str != null && str.length > 0) {
                this.body = it.toString()
            }
        }
        KHttp.LogParams(this)
        KHttp.Post2(url, this, requestCallBack = object : KGenericsCallback(this) {
            //fixme 只要有数据,成功或是失败都会回调onResponse()返回;失败了会读取缓存,只要缓存有数据,就会回调
            override fun onResponse(response: String?) {
                try {
                    if (isCallback()) {
                        callback?.let {
                            response?.let {
                                //fixme 默认返回原始数据String(包括缓存数据)，缓存数据不为空并且开启缓存isCacle==true的时候，会返回缓存数据。
                                try {
                                    callback(KGsonUtils.parseJSONToAny<T>(it, *field))
                                } catch (e: Exception) {
                                    //防止异常之后，finish()不执行;捕捉之后就没事了
                                    KLoggerUtils.e("post回调处理异常：\t" + e.message)
                                }
                            }
                        }
                    }
                    super.onResponse(response)
                } catch (e: Exception) {
                    e.printStackTrace()
                    KLoggerUtils.e("post回调处理异常2：\t" + e.message)
                }
            }

        }, timeOut = timeOut)
    }
}
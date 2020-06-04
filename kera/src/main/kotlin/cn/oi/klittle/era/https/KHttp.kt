package cn.oi.klittle.era.https

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.https.bit.KBitmapCallback
import cn.oi.klittle.era.https.bit.KBitmaps
import cn.oi.klittle.era.https.ko.KGenericsCallback
import cn.oi.klittle.era.https.ko.KHttps
import cn.oi.klittle.era.utils.KAssetsUtils
import cn.oi.klittle.era.utils.KCacheUtils
import cn.oi.klittle.era.utils.KLoggerUtils
import cn.oi.klittle.era.utils.KStringUtils
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.Deferred

//获取得到的数据，Log打印不出中文，但是JSON解析是可以解析中文的。
object KHttp {
    //存储网络请求（防止网络重复请求）fixme [timeOut超时设置是有效果，除非url格式不正确时，可能无效]
    var map: MutableMap<String, String> = mutableMapOf()

    fun Get2(url: String?, requestParams: KHttps?, requestCallBack: KGenericsCallback? = null, timeOut: Int = 3000) {
        var url = url?.replace("\\", "/");//不识别反斜杠；只识别斜杠。
        url?.let {
            requestParams?.let {
                if (it.isUiThread) {
                    it.activity?.let {
                        if (it.isFinishing) {
                            return//Activity都关闭了，不需要请求了。
                        }
                    }
                }
                if (!it.isRepeatRequest) {
                    //不允许网络重复请求
                    if (map.containsKey(getUrlUnique(it))) {
                        //Log.e("test","重复了get")
                        return
                    }
                }
                map.put(getUrlUnique(it), "网络请求标志开始")//去除标志，在onFinish()方法里
            }
            requestParams?.addProgressbarCount()//fixme 网络弹窗计数++
            //开启协程协议
            GlobalScope.async {
                requestParams?.let {
                    if (it.isShowLoad && it.isSharingDialog) {
                        var isRpeat2 = map.containsKey(getUrlUnique2(it))//判断网络是否重复
                        if (isRpeat2) {
                            //重复了
                            delay(100)//fixme 第二次相同url请求延迟;网络进度条创建大于耗时60毫秒左右
                        } else {
                            map.put(getUrlUnique2(it), "网络请求标志开始2")//去除标志，在onFinish()方法里
                        }
                    }
                }

                var isFinish = false
                //fixme 开始链接
                requestCallBack?.let {
                    requestParams?.apply {

                        if (isFirstCacle) {
                            //fixme 优先读取缓存
                            var response: String? = null
                            if (isCacle ?: false) {
                                KHttp.getUrlUnique(this)?.let {
                                    //fixme 读取缓存数据
                                    if (isJava) {
                                        response = KCacheUtils.getCacheAuto(getJaveCacheFile()).getAsString(it)
                                    } else {
                                        response = KCacheUtils.getCache().getAsString(it)
                                    }
                                }
                                response?.let {
                                    if (it.length > 0) {
                                        requestCallBack?.let {
                                            requestParams?.apply {
                                                if (isUiThread) {
                                                    activity?.apply {
                                                        runOnUiThread {
                                                            it.onSuccess(response!!)
                                                        }
                                                    }
                                                } else {
                                                    it.onSuccess(response!!)
                                                }
                                            }
                                        }
                                        isFinish = true//结束
                                        return@async
                                    }
                                }
                            }
                        }

                        if (isUiThread) {
                            activity?.apply {
                                runOnUiThread {
                                    it.onStart()
                                }
                            }
                        } else {
                            it.onStart()
                        }
                    }
                }
                if (isFinish) {
                    return@async
                }
                var result: String? = ""//返回数据
                var errStr: String? = null//异常信息
                var input: BufferedReader? = null
                var urlNameString = url
                //fixme 参数 params
                if (requestParams?.params?.size ?: 0 > 0) {
                    val sb = StringBuffer()
                    for ((key, value) in requestParams?.params?.entries!!) {
                        sb.append(key)
                        sb.append("=")
                        if (value != null && requestParams != null && requestParams!!.isURLEncoder) {
                            try {
                                if (requestParams.isJava) {
                                    sb.append(Uri.encode(URLEncoder.encode(value.toString(), "utf-8")))//fixme 转码
                                } else {
                                    sb.append(Uri.encode(value))//fixme 转码
                                }
                            } catch (exc: java.lang.Exception) {
                                exc.printStackTrace()
                            }
                        } else {
                            sb.append(value)
                        }
                        //sb.append(value)

                        sb.append("&")
                    }
                    urlNameString = urlNameString + "?" + sb.substring(0, sb.length - 1)//Get传值，其实也是params，都在body里面
                }
                val realUrl = URL(urlNameString)
                // 打开和URL之间的连接
//                val connection = realUrl.openConnection()
                val connection = realUrl.openConnection() as HttpURLConnection//fixme 打开链接
                //fixme 参数 header
                if (requestParams?.headers?.size ?: 0 > 0) {
                    for ((key, value) in requestParams?.headers?.entries!!) {

                        if (value != null && requestParams != null && requestParams!!.isURLEncoder) {
                            try {
                                if (requestParams.isJava) {
                                    connection.setRequestProperty(key, URLEncoder.encode(value.toString(), "utf-8"))//fixme 转码
                                } else {
                                    connection.setRequestProperty(key, Uri.encode(value))//fixme 转码
                                }
                            } catch (exc: java.lang.Exception) {
                                exc.printStackTrace()
                            }
                        } else {
                            connection.setRequestProperty(key, value)
                        }

                        //connection.setRequestProperty(key, value)
                        //Log.e("test", "键：\t" + e.getKey() + "\t值：\t" + e.getValue());
                    }
                }
                //fixme get请求没有body类型参数。
                var res = 0//返回码
                try {
                    //超时设置（放在异常捕捉里，防止不生效。）
                    connection.connectTimeout = timeOut//设置连接主机超时（单位：毫秒）。时间设置绝对有效。前提：手机开机之后至少必须连接一次网络，其后再断网。都有效。如果手机开机就没有网络,则设置无效。
                    connection.readTimeout = 0//设置从主机读取数据超时（单位：毫秒）。默认就是0。实际的连接超时时间的 ConnectTimeout+ReadTimeout
                    // 设置通用的请求属性
                    connection.setRequestProperty("accept", "*/*")
//                    connection.setRequestProperty("connection", "Keep-Alive")
//                    connection.setRequestProperty("user-agent",
//                            "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")

                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
                    connection.setRequestProperty("Charsert", "UTF-8")

                    //fixme 如果Content-Type不对，就会返回415错误码。
                    //conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
                    //conn.setRequestProperty("Content-Type", "text/plain; charset=$CHARSET$LINEND")
                    //conn.setRequestProperty("Content-Type", "text/plain")

//fixme 为空时，就不会设置。即使用默认的。（一般.net都不需要设置，一般都是java需要设置）。
// fixme Get请求，一般都不需要设置 Content-Type ，不要做多余的操作。
//                    requestParams?.contentType?.let {
//                        if (it.trim().length > 0) {
//                            connection.setRequestProperty("Content-Type", it)
//                        }
//                    }

                    connection.setRequestMethod("GET");
                    // Get设置如下。
                    // POST两个都必须是true。Get不能。Get必须设置如下。默认就是如下。
                    connection.doOutput = false//不允许写
                    connection.doInput = true//只允许读
                    // 建立实际的连接
                    connection.connect()
                    try {
                        res = connection.responseCode
                        if (res == 200) {
                            // 定义 BufferedReader输入流来读取URL的响应
                            input = BufferedReader(InputStreamReader(
                                    connection.getInputStream()))
                            var line = input.readLine()
                            while (line != null) {
                                result += line
                                line = input.readLine()
                            }
                        } else {
                            result = null
                            errStr = "错误返回码：\t" + res//如果为405，一般都表示不允许进行Get访问。
                        }
                    } catch (e: Exception) {
                        //fixme 异常一 流异常
                        result = null
                        errStr = "异常1：\t" + e.message
                    } finally {
                        // 使用finally块来关闭输入流
                        input?.close()
                    }
                } catch (e: Exception) {
                    //fixme 异常二 网络链接异常
                    result = null
                    errStr = "异常2：\t" + e.message
                }
                if (result != null && errStr == null) {
                    //fixme 成功
                    requestCallBack?.let {
                        requestParams?.apply {
                            if (isUiThread) {
                                activity?.apply {
                                    runOnUiThread {
                                        it.onSuccess(result)
                                    }
                                }
                            } else {
                                it.onSuccess(result)
                            }
                        }
                    }

                } else {
                    //fixme 失败
                    requestCallBack?.let {
                        requestParams?.apply {
                            if (isUiThread) {
                                activity?.apply {
                                    runOnUiThread {
                                        it.onFailure(errStr)
                                    }
                                }
                            } else {
                                it.onFailure(errStr)
                            }
                        }
                    }

                }
            }
        }
    }

    fun Post2(url: String?, requestParams: KHttps?, requestCallBack: KGenericsCallback? = null, timeOut: Int = 3000) {
        var url = url?.replace("\\", "/");//不识别反斜杠；只识别斜杠。
        url?.let {
            requestParams?.let {
                if (it.isUiThread) {
                    it.activity?.let {
                        if (it.isFinishing) {
                            return//Activity都关闭了，不需要请求了。
                        }
                    }
                }
                if (!it.isRepeatRequest) {
                    //不允许网络重复请求
                    if (map.containsKey(getUrlUnique(it))) {
                        //Log.e("test","重复了post")
                        return
                    }
                }
                map.put(getUrlUnique(it), "网络请求标志开始")//去除标志，在onFinish()方法里
            }
            requestParams?.addProgressbarCount()//fixme 网络弹窗计数++
            //开启协程协议
            GlobalScope.async {
                requestParams?.let {
                    if (it.isShowLoad && it.isSharingDialog) {
                        var isRpeat2 = map.containsKey(getUrlUnique2(it))//判断网络是否重复
                        if (isRpeat2) {
                            //重复了
                            delay(100)//fixme 第二次相同url请求延迟；网络进度条创建大于耗时60毫秒左右
                        } else {
                            map.put(getUrlUnique2(it), "网络请求标志开始2")//去除标志，在onFinish()方法里
                        }
                    }
                }

                var isFinish = false
                //fixme 开始链接
                requestCallBack?.let {
                    requestParams?.apply {
                        if (isFirstCacle) {
                            //fixme 优先读取缓存
                            var response: String? = null
                            if (isCacle ?: false) {
                                KHttp.getUrlUnique(this)?.let {
                                    //fixme 读取缓存数据
                                    if (isJava) {
                                        response = KCacheUtils.getCacheAuto(getJaveCacheFile()).getAsString(it)
                                    } else {
                                        response = KCacheUtils.getCache().getAsString(it)
                                    }
                                }
                                response?.let {
                                    if (it.length > 0) {
                                        requestCallBack?.let {
                                            requestParams?.apply {
                                                if (isUiThread) {
                                                    activity?.apply {
                                                        runOnUiThread {
                                                            it.onSuccess(response!!)
                                                        }
                                                    }
                                                } else {
                                                    it.onSuccess(response!!)
                                                }
                                            }
                                        }
                                        //KLoggerUtils.e("优先读取缓存") //亲测有效
                                        isFinish = true//结束
                                        return@async
                                    }
                                }
                            }
                        }
                        if (isUiThread) {
                            activity?.apply {
                                runOnUiThread {
                                    it.onStart()
                                }
                            }
                        } else {
                            it.onStart()
                        }
                    }
                }
                if (isFinish) {
                    return@async//fixme 结束
                }
                var errStr: String? = null//异常信息
                var result: String? = ""//返回信息
                var res: Int = 0//返回码
                val CHARSET = "UTF-8"
                try {
                    val BOUNDARY = UUID.randomUUID().toString()
                    val PREFIX = "--"
                    val LINEND = "\r\n"
                    val MULTIPART_FROM_DATA = "multipart/form-data" //图片上传格式
                    val uri = URL(url)
                    //android 6.0(23)淘汰的是 HttpClient。HttpURLConnection是纯java的。是可以使用的。不需要任何第三方包。
                    val conn = uri.openConnection() as HttpURLConnection//fixme 打开链接
                    //conn.setReadTimeout(10 * 1000); // 缓存的最长时间
                    conn.connectTimeout = timeOut//超时连接，超过这个时间还没连接上，就会连接失败
                    conn.readTimeout = 0
                    conn.useCaches = false // 不允许使用缓存
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
                    conn.setRequestProperty("accept", "*/*");
                    //conn.setRequestProperty("connection", "Keep-Alive");
                    //conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
                    conn.setRequestProperty("Charsert", CHARSET)
                    conn.setInstanceFollowRedirects(true)
                    var hasFile = false//是否包含文件上传
                    if (requestParams?.files?.size ?: 0 > 0) {
                        //fixme 文件上传，必须使用以下contentType
                        conn.setRequestProperty("Content-Type", "$MULTIPART_FROM_DATA;boundary=$BOUNDARY")
                        hasFile = true
                    } else {
                        //fixme 如果Content-Type不对，就会返回415错误码。
                        //conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
                        //conn.setRequestProperty("Content-Type", "text/plain; charset=$CHARSET$LINEND")
                        //conn.setRequestProperty("Content-Type", "text/plain")
                        //fixme 为空时，就不会设置。即使用默认的。（一般.net都不需要设置，一般都是java需要设置）。
                        requestParams?.contentType?.let {
                            if (it.trim().length > 0) {
                                conn.setRequestProperty("Content-Type", it)
                            }
                        }
                        hasFile = false
                    }
                    //fixme 参数 header
                    if (requestParams?.headers?.size ?: 0 > 0) {
                        for ((key, value) in requestParams?.headers?.entries!!) {
                            if (value != null && requestParams != null && requestParams!!.isURLEncoder) {
                                try {
                                    if (requestParams.isJava) {
                                        conn.setRequestProperty(key, URLEncoder.encode(value.toString(), "utf-8"))//fixme 转码
                                    } else {
                                        conn.setRequestProperty(key, Uri.encode(value))//fixme 转码
                                    }
                                } catch (exc: java.lang.Exception) {
                                    exc.printStackTrace()
                                }

                            } else {
                                conn.setRequestProperty(key, value)
                            }
                        }
                    }
                    conn.doInput = true// 允许输入
                    conn.doOutput = true// 允许输出
                    conn.requestMethod = "POST"//POST 只能为大写，严格限制，post会不识别
                    conn.connect()//fixme 在header参数后调用。(不然报错)
                    var out: PrintWriter? = null//非文件上传使用
                    var outStream: DataOutputStream? = null//文件上传使用
                    if (!hasFile) {
                        //fixme 文件不存在
                        // 获取URLConnection对象对应的输出流
                        out = PrintWriter(conn.outputStream)
                        var params = requestParams?.params
                        // 发送请求参数
                        if (params != null && params.size > 0) {
                            var sb = StringBuffer()
                            for (e in params.entries) {
                                sb.append(e.key)//键
                                sb.append("=")
                                if (e != null && e.value != null && requestParams != null && requestParams!!.isURLEncoder) {
                                    //var encode=URLEncoder.encode(e.value.toString(), "utf-8")
                                    //fixme Uri.encode是安卓自带的；比URLEncoder.encode更安全(不会把空格转成+加号)；保证服务器端能够正确识别。
                                    //fixme 记住只能转一次哦；不能连续转两次哦。转两次就乱了。【传参时就不要再手动转码了。】
                                    try {
                                        if (requestParams.isJava) {
                                            sb.append(URLEncoder.encode(e.value.toString(), "utf-8"))//值;对参数进行转码；防止服务器无法识别。如加号"+"等。
                                        } else {
                                            sb.append(Uri.encode(e.value.toString()))//值;对参数进行转码；防止服务器无法识别。如加号"+"等。
                                        }
                                    } catch (exc: java.lang.Exception) {
                                        exc.printStackTrace()
                                    }
                                    //fixme java mian() 不支持 Uri.encode(e.value.toString())

                                } else {
                                    sb.append(e.value)//值
                                }
                                sb.append("&")
                                //Log.e("test", "键：\t" + e.key + "\t值：\t" + e.value);
                            }
                            //KLoggerUtils.e("总值：\t" + sb.toString())
                            out.print(sb.substring(0, sb.length - 1))//params在body里面。
                            //out.write(URLEncoder.encode(sb.toString(), "utf-8"))
                            //out.print(sb.toString())
                            //out.write(sb.substring(0, sb.length - 1))
                            //out.write(sb.toString())
                            //conn.outputStream.write(sb.toString().toByteArray())
                        } else {
                            var body = requestParams?.body
                            if (body != null && body.length > 0) {
//                                if (body != null && requestParams != null && requestParams!!.isURLEncoder) {
//                                    //fixme 对整个body最好不要进行转码；会破坏内部数据结构。服务器无法识别。
//                                    out.print(Uri.encode(body))//fixme 转码
//                                } else {
//                                    out.print(body)//out整体就是一个body
//                                }
                                out.print(body)//out整体就是一个body
                            }
                        }
                        // flush输出流的缓冲
                        out.flush()
                    } else {
                        //fixme 存在文件上传
                        outStream = DataOutputStream(conn.outputStream)
                        // 首先组拼文本类型的参数
                        var sb = StringBuilder()
                        //fixme 参数 params
                        if (requestParams?.params?.size ?: 0 > 0) {
                            for (entry in requestParams?.params?.entries!!) {
                                sb.append(PREFIX)
                                sb.append(BOUNDARY)
                                sb.append(LINEND)
                                sb.append("Content-Disposition: form-data; name=\"" + entry.key + "\"" + LINEND)//键
                                sb.append("Content-Type: text/plain; charset=$CHARSET$LINEND")
                                sb.append("Content-Transfer-Encoding: 8bit$LINEND")
                                sb.append(LINEND)

                                if (entry != null && entry.value != null && requestParams != null && requestParams!!.isURLEncoder) {
                                    try {
                                        if (requestParams.isJava) {
                                            sb.append(URLEncoder.encode(entry.value.toString(), "utf-8"))//值;对参数进行转码；防止服务器无法识别。如加号"+"等。
                                        } else {
                                            sb.append(Uri.encode(entry.value.toString()))//值;对参数进行转码；防止服务器无法识别。如加号"+"等。
                                        }
                                    } catch (exc: java.lang.Exception) {
                                        exc.printStackTrace()
                                    }
                                } else {
                                    sb.append(entry.value)//值
                                }

                                //sb.append(entry.value)//值；
                                sb.append(LINEND)
                            }
                            //KLoggerUtils.e("设置params参数：\t" + sb.toString())
                            //outStream.write(sb.toString().toByteArray())
                            outStream.writeBytes(sb.toString())
                        }
                        // fixme 参数files 发送文件数据
                        //fixme file.key 参数名
                        //fixme 服务器如果支持多张图片上传时，参数名已经无所谓了(可以随便取)。只要是文件类型；都会接收。
                        if (requestParams?.files?.size ?: 0 > 0) {
                            for (file in requestParams?.files?.entries!!) {
                                if (file.value == null || file.value.toString().trim().equals("") || file.value.toString().trim().equals("null")) {
                                    continue
                                }
                                if (file.value.isFile && file.value.exists()) {
                                    val sb1 = StringBuilder()
                                    sb1.append(PREFIX)
                                    sb1.append(BOUNDARY)
                                    sb1.append(LINEND)
                                    sb1.append("Content-Disposition: form-data; name=" + file.key.trim({ it <= ' ' }) + "; filename=\""
                                            + file.value.getName() + "\"" + LINEND)
                                    sb1.append("Content-Type: application/octet-stream; charset=$CHARSET$LINEND")
                                    sb1.append(LINEND)
                                    outStream.write(sb1.toString().toByteArray())
                                    val input = FileInputStream(file.value)//fixme 文件参数是文件的路径；这里根据路径自动获取流。所以流不需要转码。
                                    val buffer = ByteArray(1024)
                                    var len = input.read(buffer)
                                    while (len != -1) {
                                        outStream.write(buffer, 0, len)
                                        len = input.read(buffer)
                                    }
                                    input.close()
                                    outStream.write(LINEND.toByteArray())
                                }
                            }
                        }
                        //fixme 参数 body (body,param,file独立存在，可以同时使用，亲测可行)
                        requestParams?.body?.let {
                            if (requestParams?.body?.length ?: 0 > 0) {
//                                //fixme 对body不要进行转码。（会破坏数据结构，导致服务器端无法识别）
//                                if (it != null && requestParams != null && requestParams!!.isURLEncoder) {
//                                    outStream.writeBytes(Uri.encode(it.toString()))//fixme 转码
//                                } else {
//                                    //outStream.writeUTF(requestParams?.body)//out整体就是一个body
//                                    outStream.writeBytes(it)//fixme 一般用这个,写入字节。，上面那个很容易出错。
//                                }
                                outStream.writeBytes(it)//fixme 一般用这个,写入字节。，上面那个很容易出错。
                            }
                        }
                        // 请求结束标志
                        val end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).toByteArray()
                        outStream.write(end_data)
                        outStream.flush()
                    }

                    // 得到响应码
                    res = conn.responseCode
                    if (res == 200) {//200是成功。
                        // 定义BufferedReader输入流来读取URL的响应
                        var input = BufferedReader(
                                InputStreamReader(conn.inputStream, CHARSET))//解决中文乱码。
                        var line = input.readLine()
                        while (line != null) {
                            result += line
                            line = input.readLine()
                        }
                    } else {
                        result = null;
                        errStr = "失败返回码：\t" + res;//fixme 异常一
                    }
                    out?.close()
                    outStream?.close()
                    conn.disconnect()//fixme 断开链接
                    //String result = sb2.toString();
                    //result = new String(result.getBytes("iso-8859-1"), CHARSET);//这个可以解决中文乱码。以上方法已经解决了乱码问题。这个不用了。
                } catch (e: Exception) {
                    result = null;
                    errStr = e.message + "\t返回码：\t" + res;//fixme 异常一
                }
                if (result != null && errStr == null) {
                    //fixme 成功
                    //fixme json自带unicode编码解析成中文的能力。
                    requestCallBack?.let {
                        requestParams?.apply {
                            if (isUiThread) {
                                activity?.apply {
                                    runOnUiThread {
                                        it.onSuccess(result)
                                    }
                                }
                            } else {
                                it.onSuccess(result)
                            }
                        }
                    }

                } else {
                    //fixme 失败
                    requestCallBack?.let {
                        requestParams?.apply {
                            if (isUiThread) {
                                activity?.apply {
                                    runOnUiThread {
                                        it.onFailure(errStr)
                                    }
                                }
                            } else {
                                it.onFailure(errStr)
                            }
                        }
                    }
                }
            }
        }
    }

    //宽度和高度的标志；fixme 还是要加上宽和高创建不同尺寸的位图。防止位图并发。（目前最有效的方法，就是缓存不同尺寸的位图。亲测）
    private fun getWH(w: Int = 0, h: Int = 0): String {
        //(0就是原图尺寸的标志)
        if (w <= 0 || h <= 0 || w >= 10000 || h >= 10000) {
            return "w0" + "h0"
        } else {
            return "w" + w.toString() + "h" + h.toString()
        }
    }

    //Get请求获取网络位图，位图一般都是使用Get
    //width,height获取网络位图的宽度和高度。
    //fixme 亲测有效；能够正确获取网络图片。
    /**
     * @param url 网络图片地址，如：http://test.app.bwg2017.com/photo/201905/10863/20190517134844_0.jpg
     * fixme 图片地址多几个斜杠/是没有关系的，是可以正常访问的。亲测可行。如：http://test.app.bwg2017.com///photo/201905/10863/20190517134844_0.jpg
     */
    fun GetNetBitmap(url: String?, activity: Activity? = null, requestParams: KBitmaps?, requestCallBack: KBitmapCallback? = null, timeOut: Int = 3000, width: Int = 0, height: Int = 0) {
        var url = url?.replace("\\", "/");//不识别反斜杠；只识别斜杠。
        var w = width
        var h = height
        GlobalScope.async {
            var isStop = false//是否停止
            url?.let {
                requestParams?.let {
                    //KLoggerUtils.e("是否重复：\t"+map.containsKey(it.getUrlUnique()))
                    var isRpeat = map.containsKey(it.getUrlUnique())//判断网络是否重复
                    if (!it.isRepeatRequest) {
                        //不允许网络重复请求
                        if (isRpeat) {
                            //Log.e("test","重复了get")
                            isStop = true
                            return@async
                        }
                    }

                    map.put(it?.getUrlUnique(), "网络请求标志开始")//去除标志，在onFinish()方法里
                    //fixme 优先读取缓存数据
                    if (it.cacle) {
                        //fixme 读取缓存[网络位图，优先从本地读取]
                        var bitmap: Bitmap? = KCacheUtils.getCacheImg().getAsBitmap(it.getUrlUnique() + getWH(w, h), it.optionsRGB_565);//此次对UtilCache进行优化，内部使用了UtilAssets。优化了位图。
                        if (bitmap != null && !bitmap.isRecycled) {
                            if (w <= 0) {
                                w = bitmap.width
                            }
                            if (h <= 0) {
                                h = bitmap.height
                            }
                            //KLoggerUtils.e("缓存位图：\t" + bitmap)
                            //fixme 使用keyBitmap对位图进行压缩；防止位图并发操作。
                            kpx.keyBitmap(url, bitmap, w, h, isCompress = requestParams?.isCompress, isRecycle = it.isRecycle) {
                                bitmap = it
                                //fixme 成功
                                requestCallBack?.apply {
                                    if (isUiThread) {
                                        activity?.apply {
                                            runOnUiThread {
                                                onSuccess(bitmap!!)
                                            }
                                        }
                                    } else {
                                        onSuccess(bitmap!!)
                                    }
                                }
                            }
                            isStop = true
                            return@async
                        }
                    }
                    //fixme 网络请求重复时。
                    if (it.isRepeatRequest) {
                        //fixme 允许重复请求
                        if (isRpeat) {
                            //fixme 请求重复了
                            //fixme 尽可能的解决并发；防止同一时间对同一个位图进行操作。防止异常。所以要随机延迟处理一下。
                            try {
                                var delay = 1500L//延迟请求（一次网络请求差不多在一秒左右。）
                                delay(delay)
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                            }
                            //fixme 延迟之后，再读一次缓存。
                            if (it.cacle) {
                                //fixme 读取缓存[网络位图，优先从本地读取]
                                var bitmap: Bitmap? = KCacheUtils.getCacheImg().getAsBitmap(it.getUrlUnique() + getWH(w, h), it.optionsRGB_565);//此次对UtilCache进行优化，内部使用了UtilAssets。优化了位图。
                                if (bitmap != null && !bitmap!!.isRecycled && bitmap!!.width > 0) {
                                    if (w <= 0) {
                                        w = bitmap!!.width
                                    }
                                    if (h <= 0) {
                                        h = bitmap!!.height
                                    }
                                    //KLoggerUtils.e("缓存位图2：\t" + bitmap)
                                    //fixme 使用keyBitmap对位图进行压缩；防止位图并发操作。
                                    kpx.keyBitmap(url, bitmap!!, w, h, isCompress = requestParams?.isCompress, isRecycle = it.isRecycle) {
                                        bitmap = it
                                        //fixme 成功
                                        requestCallBack?.apply {
                                            if (isUiThread) {
                                                activity?.apply {
                                                    runOnUiThread {
                                                        onSuccess(bitmap!!)
                                                    }
                                                }
                                            } else {
                                                onSuccess(bitmap!!)
                                            }
                                        }
                                    }
                                    isStop = true
                                    return@async
                                }
                            }

                        }
                    }
                }
                if (!isStop) {
                    //开启协程协议
                    async {
                        //fixme 开始链接
                        requestCallBack?.apply {
                            if (isUiThread) {
                                activity?.apply {
                                    runOnUiThread {
                                        onStart()
                                    }
                                }
                            } else {
                                onStart()
                            }
                        }
                        var result: Bitmap? = null//返回位图
                        var errStr: String? = null//异常信息
                        var urlNameString = url
                        //fixme 参数 params
                        if (requestParams?.params?.size ?: 0 > 0) {
                            val sb = StringBuffer()
                            for ((key, value) in requestParams?.params?.entries!!) {
                                sb.append(key)
                                sb.append("=")
                                sb.append(value)
                                sb.append("&")
                            }
                            urlNameString = urlNameString + "?" + sb.substring(0, sb.length - 1)//Get传值，其实也是params，都在body里面
                        }
                        val realUrl = URL(urlNameString)
                        // 打开和URL之间的连接
                        val connection = realUrl.openConnection()
                        //fixme 参数 header
                        if (requestParams?.headers?.size ?: 0 > 0) {
                            for ((key, value) in requestParams?.headers?.entries!!) {
                                connection.setRequestProperty(key, value)
                                //Log.e("test", "键：\t" + e.getKey() + "\t值：\t" + e.getValue());
                            }
                        }
                        try {
                            //超时设置（放在异常捕捉里，防止不生效。）
                            connection.connectTimeout = timeOut//设置连接主机超时（单位：毫秒）。时间设置绝对有效。前提：手机开机之后至少必须连接一次网络，其后再断网。都有效。如果手机开机就没有网络,则设置无效。
                            connection.readTimeout = 0//设置从主机读取数据超时（单位：毫秒）。默认就是0。实际的连接超时时间的 ConnectTimeout+ReadTimeout
                            // 设置通用的请求属性
                            connection.setRequestProperty("accept", "*/*")
                            connection.setRequestProperty("connection", "Keep-Alive")
                            connection.setRequestProperty("user-agent",
                                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
                            // Get设置如下。
                            // POST两个都必须是true。Get不能。Get必须设置如下。默认就是如下。
                            connection.doOutput = false//不允许写
                            connection.doInput = true//只允许读
                            // 建立实际的连接
                            connection.connect()
                            try {
                                // 定义 BufferedReader输入流来读取URL的响应
                                var inputStream: InputStream? = connection.getInputStream()
                                var b: ByteArray? = readToByteArray(inputStream)
                                inputStream?.close()
                                inputStream = null
                                if (requestParams?.optionsRGB_565 ?: false) {
                                    result = BitmapFactory.decodeByteArray(b, 0, b!!.size, KAssetsUtils.getInstance().optionsRGB_565)//fixme ===========================================最省内存法
                                } else {
                                    result = BitmapFactory.decodeByteArray(b, 0, b!!.size)
                                }
                            } catch (e: Exception) {
                                //fixme 异常一 流异常
                                result = null
                                errStr = e.message
                            } finally {
                                // 使用finally块来关闭输入流
                            }
                        } catch (e: Exception) {
                            //fixme 异常二 网络链接异常
                            result = null
                            errStr = e.message
                        }
                        if (result != null && !result.isRecycled && errStr == null) {
                            //KLoggerUtils.e("网络位图：\t" + result+"\t"+ requestParams?.getUrlUnique())
                            //fixme 使用keyBitmap对位图进行压缩；防止位图并发操作。
                            kpx.keyBitmap(url, result, w, h, isCompress = requestParams?.isCompress, isRecycle = requestParams?.isRecycle) {
                                result = it

                                requestParams?.let {
                                    if (it.cacle) {
                                        //fixme 存储缓存,顺序调一下。先存储。再回调。防止出错（不会浪费多少时间）。（比如说，回调里面释放了图片。再存储就会报错。）
                                        result?.let {
                                            if (!it.isRecycled) {
                                                //fixme 缓存对应尺寸的位图。
                                                if (requestParams?.saveTime == null) {
                                                    KCacheUtils.getCacheImg().put(requestParams.getUrlUnique() + getWH(w, h), result)
                                                } else {
                                                    requestParams?.saveTime?.let {
                                                        KCacheUtils.getCacheImg().put(requestParams.getUrlUnique() + getWH(w, h), result, it)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                //fixme 成功
                                requestCallBack?.apply {
                                    if (isUiThread) {
                                        activity?.apply {
                                            runOnUiThread {
                                                onSuccess(result!!)
                                            }
                                        }
                                    } else {
                                        onSuccess(result!!)
                                    }
                                }
                            }
                        } else {
                            //fixme 失败
                            requestCallBack?.apply {
                                if (isUiThread) {
                                    activity?.apply {
                                        runOnUiThread {
                                            onFailure(errStr)
                                        }
                                    }
                                } else {
                                    onFailure(errStr)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //获取网络流（该流可以自己转换成位图图片。）,fixme 最直接的网络请求，没有做任何处理（如重复请求等。）
    //里面的网络请求，需要调用者在子线程调用。
    //fixme Cleartext HTTP traffic to hbimg.b0.upaiyun.com not permitted 错误是该服务器不支持http,可以试试https（一般都可以。）。
    fun getNetByteArray(url: String?, headers: Map<String, String>? = null, params: Map<String, String>? = null): ByteArray? {
        var url = url?.replace("\\", "/");//不识别斜杠；只识别反斜杠。
        url?.let {
            var urlNameString = url
            //fixme 参数 params
            if (params?.size ?: 0 > 0) {
                val sb = StringBuffer()
                for ((key, value) in params?.entries!!) {
                    sb.append(key)
                    sb.append("=")
                    sb.append(value)
                    sb.append("&")
                }
                urlNameString = urlNameString + "?" + sb.substring(0, sb.length - 1)//Get传值，其实也是params，都在body里面
            }
            val realUrl = URL(urlNameString)
            // 打开和URL之间的连接
            val connection = realUrl.openConnection()
            //fixme 参数 header
            if (headers?.size ?: 0 > 0) {
                for ((key, value) in headers?.entries!!) {
                    connection.setRequestProperty(key, value)
                    //Log.e("test", "键：\t" + e.getKey() + "\t值：\t" + e.getValue());
                }
            }
            try {
                //超时设置（放在异常捕捉里，防止不生效。）
                connection.connectTimeout = 5000//设置连接主机超时（单位：毫秒）。时间设置绝对有效。前提：手机开机之后至少必须连接一次网络，其后再断网。都有效。如果手机开机就没有网络,则设置无效。
                connection.readTimeout = 0//设置从主机读取数据超时（单位：毫秒）。默认就是0。实际的连接超时时间的 ConnectTimeout+ReadTimeout
                // 设置通用的请求属性
                connection.setRequestProperty("accept", "*/*")
                connection.setRequestProperty("connection", "Keep-Alive")
                connection.setRequestProperty("user-agent",
                        "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
                // Get设置如下。
                // POST两个都必须是true。Get不能。Get必须设置如下。默认就是如下。
                connection.doOutput = false//不允许写
                connection.doInput = true//只允许读
                // 建立实际的连接
                connection.connect()
                try {
                    // 定义 BufferedReader输入流来读取URL的响应
                    var inputStream: InputStream? = connection.getInputStream()
                    var b: ByteArray? = readToByteArray(inputStream)
                    inputStream?.close()
                    inputStream = null
                    return b
                } catch (e: Exception) {
                    //fixme 异常一 流异常
                    KLoggerUtils.e("KHttp 异常一 流异常:\t" + e.printStackTrace(), isLogEnable = true)
                } finally {
                    // 使用finally块来关闭输入流
                }
            } catch (e: Exception) {
                //fixme 异常二 网络链接异常
                KLoggerUtils.e("kHttp 异常二 网络链接异常:\t" + e.printStackTrace(), isLogEnable = true)
            }
        }
        return null
    }

    /**
     * fixme InputStream流，读取成字符串
     */
    fun readToString(inputStream: InputStream?): String? {
        inputStream?.let {
            //KLoggerUtils.e("读取中...")
            var available = it.available()//读取流的有效长度
            if (available < 1024) {
                available = 1024
            }
            var buff = ByteArray(available)
            var len = it.read(buff)
            if (len != -1) {
                KStringUtils.bytesToString(buff)?.let {
                    return it
                }
            }
        }
        return null
    }


    internal val BUFFER_SIZE = 4096

    /**
     * fixme InputStream转byte字节，使用字节比使用流更省内存。
     * fixme 当然测试发现只对网络输入流有效，一般的本地流就不用转了。转一下还浪费效率。
     */
    fun readToByteArray(inputStream: InputStream?): ByteArray {
        val outStream = ByteArrayOutputStream()
        var data: ByteArray? = ByteArray(BUFFER_SIZE)
        var count = -1
        try {
            count = inputStream?.read(data!!, 0, BUFFER_SIZE) ?: -1
            while (count != -1) {
                outStream.write(data, 0, count)
                count = inputStream?.read(data!!, 0, BUFFER_SIZE) ?: -1
            }
        } catch (e: Exception) {
            Log.e("test", "流转换字节出错:\t" + e.message)
        }

        data = null
        return outStream.toByteArray()
    }

    //打印参数(不需要手动到调用。会在请求调用前，自动调用)
    fun LogParams(https2: KHttps) {
        https2.apply {
            if (isShowParams) {
                if (isJava) {
                    System.out.println("url:==================================================\t" + url)
                } else {
                    KLoggerUtils.e_long("url:==================================================\t" + url)
                }
                if (headers?.size > 0) {
                    if (isJava) {
                        System.out.println("头部Header=========================================")
                    } else {
                        KLoggerUtils.e_long("头部Header=========================================")
                    }
                    for ((key, value) in headers.entries) {
                        if (isJava) {
                            System.out.println("key:\t" + key + "\tvalue:\t" + value)
                        } else {
                            KLoggerUtils.e_long("key:\t" + key + "\tvalue:\t" + value)
                        }
                    }
                }
                if (params?.size > 0) {
                    if (isJava) {
                        System.out.println("Params=============================================")
                    } else {
                        KLoggerUtils.e_long("Params=============================================")
                    }
                    for ((key, value) in params.entries) {
                        if (isJava) {
                            System.out.println("key:\t" + key + "\tvalue:\t" + value)
                        } else {
                            KLoggerUtils.e_long("key:\t" + key + "\tvalue:\t" + value)
                        }
                    }
                }
                if (files?.size > 0) {
                    if (isJava) {
                        System.out.println("文件===============================================")
                    } else {
                        KLoggerUtils.e_long("文件===============================================")
                    }
                    for ((key, value) in files.entries) {
                        if (isJava) {
                            System.out.println("key:\t" + key + "\tvalue:\t" + value?.absoluteFile)
                        } else {
                            KLoggerUtils.e_long("key:\t" + key + "\tvalue:\t" + value?.absoluteFile)
                        }
                    }
                }
                body?.let {
                    if (isJava) {
                        System.out.println("Body===============================================")
                        System.out.println("body:\t" + body)
                    } else {
                        KLoggerUtils.e_long("Body===============================================")
                        KLoggerUtils.e_long("body:\t" + body)
                    }
                }
            }
        }

    }

    //获取网络请求唯一标志(纯url标志);
    fun getUrlUnique2(https2: KHttps): String {
        var stringBuffer = StringBuffer("")
        https2.apply {
            //fixme 防止参数里面有时间戳(当前时间 System.currentTimeMillis())；
            //fixme 所以用参数来判断是否唯一；已经不保险了。还是直接使用url最保险。
            stringBuffer.append(url)
            if (urlUniqueParams != null) {
                stringBuffer.append(urlUniqueParams)//添加该参数作为唯一标志
            }
            if (isUiThread) {
                activity?.toString()?.let {
                    stringBuffer.append(it)//fixme 绑定Activity。
                }
            }
        }
        return stringBuffer.toString().trim()
    }

    //获取网络请求唯一标志(url+所有参数集合);fixme 防止网络重复请求。
    fun getUrlUnique(https2: KHttps): String {
        var stringBuffer = StringBuffer("")
        https2.apply {
            //fixme 防止参数里面有时间戳(当前时间 System.currentTimeMillis())；
            //fixme 所以用参数来判断是否唯一；已经不保险了。还是直接使用url最保险。
            stringBuffer.append(url)
            if (urlUniqueParams != null) {
                stringBuffer.append(urlUniqueParams)//添加该参数作为唯一标志
            }
            //网络唯一标志；是否包含参数；默认是不包含的。
            if (isUrlUniqueParams) {
                if (headers?.size > 0) {
                    for ((key, value) in headers.entries) {
                        stringBuffer.append(key)
                        stringBuffer.append(value)
                    }
                }
                if (params?.size > 0) {
                    for ((key, value) in params.entries) {
                        stringBuffer.append(key)
                        stringBuffer.append(value)
                    }
                }
                if (files?.size > 0) {
                    for ((key, value) in files.entries) {
                        stringBuffer.append(key)
                        stringBuffer.append(value)
                    }
                }
                body?.let {
                    stringBuffer.append(it)
                }
            }
            if (isUiThread) {
                activity?.toString()?.let {
                    stringBuffer.append(it)//fixme 绑定Activity。
                }
            }
            //Log.e("test", "" + stringBuffer)
        }
        return stringBuffer.toString().trim()
    }

    //获取缓存唯一键值(url+所有参数集合)
    fun getCacheUnique(https2: KHttps): String {
        var stringBuffer = StringBuffer("")
        https2.apply {
            stringBuffer.append(url)
            //缓存唯一标志；包含所有参数。
            if (headers?.size > 0) {
                for ((key, value) in headers.entries) {
                    stringBuffer.append(key)
                    stringBuffer.append(value)
                }
            }
            if (params?.size > 0) {
                for ((key, value) in params.entries) {
                    stringBuffer.append(key)
                    stringBuffer.append(value)
                }
            }
            if (files?.size > 0) {
                for ((key, value) in files.entries) {
                    stringBuffer.append(key)
                    stringBuffer.append(value)
                }
            }
            body?.let {
                stringBuffer.append(it)
            }
            //Log.e("test", "" + stringBuffer)
        }
        return stringBuffer.toString().trim()
    }

}
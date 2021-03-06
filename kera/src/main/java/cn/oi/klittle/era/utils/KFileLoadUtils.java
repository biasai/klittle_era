package cn.oi.klittle.era.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.oi.klittle.era.R;
import cn.oi.klittle.era.base.KBaseApplication;
import cn.oi.klittle.era.base.KBaseUi;

//            fixme kotlin使用案例; 下载url必须为具体的文件地址。不然无法下载；像 http://test.bwg2017.com/GlassSystem/ErpConfigManage.aspx 这样的就无法下载。
//            var url="http://dl.ludashi.com/ludashi/ludashi_home.apk"//鲁大师app；指向具体的apk网络文件路径即可。亲测可行。
//            KFileLoadUtils.getInstance(ctx).downLoad(url, null,"123.apk", object : KFileLoadUtils.RequestCallBack {
//                override fun onStart() {
//                    //开始下载
//                    KLoggerUtils.e("开始下载")
//                }
//
//                override fun onFailure(isLoad: Boolean?, result: String?,code:Int, file: File?) {
//                    //下载失败（关闭弹窗）
//                    if (isLoad!!) {
//                        //已经下载(进行安装)
//                        file?.let {
//                            KAppUtils.installation(ctx, file)
//                        }
//                    } else {
//                        KToast.showError("下载失败")
//                        KLoggerUtils.e("下载失败")
//                    }
//                }
//
//                override fun onSuccess(file: File?) {
//                    KLoggerUtils.e("下载成功：\t"+file?.path)
//                    //下载完成安装（关闭弹窗）
//                    KAppUtils.installation(ctx, file)
//                }
//
//                override fun onLoad(current: Long, max: Long, bias: Int) {
//                    //下载进度
//                    KLoggerUtils.e("下载进度：\t"+bias)
//                }
//            })

/**
 * fixme 文件下载工具类，支持断点下载(不会重复下载)。
 * 上传就不需要单独写工具类了【所谓的上传就是将文件(File file)作为参数通过Http协议Post提交即可】,如：params.addBodyParameter(key, file);
 * Created by 彭治铭 on 2017/5/24.
 */

public class KFileLoadUtils {
    private static KFileLoadUtils fileDown;
    private ThreadPoolExecutor threadPoolExecutor;
    //fixme 文件下载目录
    public String cacheDir;
    public boolean isApk = true;//fixme 下载的是否为APK安装包。默认是的。

    //fixme 设置下载路径
    public void setCacheDir(Boolean isApk) {
        if (isApk != null) {
            if (isApk) {
                //apk下载目录
                if (cacheDir == null || this.isApk != isApk) {
                    this.cacheDir = KPathManagerUtils.INSTANCE.getApkLoadDownPath();
                }
            } else {
                if (cacheDir == null || this.isApk != isApk) {
                    this.cacheDir = KPathManagerUtils.INSTANCE.getFileLoadDownPath();
                }
            }
            this.isApk = isApk;
        }
    }

    //判断该uri是否正在下载
    private Map<String, Boolean> mapLoad;
    private Map<String, RequestCallBack> mapCallback;

    //构造函数
    private KFileLoadUtils() {
        try {
            //Context context = KBaseApplication.getInstance();
            //this.cacheDir = context.getApplicationContext().getFilesDir().getAbsolutePath();//这个地址，文件无法分享。(内部位置无法分享出去),不需要权限
            //this.cacheDir = context.getApplicationContext().getExternalCacheDir().getAbsolutePath();//这个位置，可以分享。（SD卡的东西可以分享出去）,不需要权限。推荐使用这个
            //this.cacheDir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();//需要SD卡权限
//            this.cacheDir = KPathManagerUtils.INSTANCE.getFileLoadDownPath();//fixme 通用获取下载路径。
//            try {
//                String cacheDir2 = cacheDir + "/down";//fixme 统一下载路径; 与 KCacheUtils缓存目录不是同一个目录；互不影响。
//                new File(cacheDir2).mkdirs();//fixme 创建目录
//                cacheDir = cacheDir2;
//            } catch (Exception e) {
//                e.printStackTrace();
//                KLoggerUtils.INSTANCE.e("KFileLoadUtils 下载文件目录创建失败：\t" + e.getMessage(), true);
//            }
            setCacheDir(isApk);//fixme 设置下载路径
            mapLoad = new HashMap<>();
            mapCallback = new HashMap<>();
            int corePoolSize = Runtime.getRuntime().availableProcessors() + 2;
            int maxinumPoolSize = corePoolSize * 2 + 1;
            long keepAliveTime = 10;
            TimeUnit unit = TimeUnit.SECONDS;
            BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<>();
            threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxinumPoolSize, keepAliveTime, unit, workQueue);
        } catch (Exception e) {
            e.printStackTrace();
            KLoggerUtils.INSTANCE.e("KFileLoadUtils初始异常：\t" + e.getMessage(), true);
        }
    }

    //初始化
    public static KFileLoadUtils getInstance() {
        if (fileDown == null) {
            fileDown = new KFileLoadUtils();
        }
        return fileDown;
    }

    /**
     * 初始化
     *
     * @param isApk 是否下载apk
     * @return
     */
    public static KFileLoadUtils getInstance(Boolean isApk) {
        if (fileDown == null) {
            fileDown = new KFileLoadUtils();
        }
        fileDown.setCacheDir(isApk);
        return fileDown;
    }

    /**
     * fixme 删除下载目录的所有文件。
     *
     * @return
     */
    public boolean delLoadDownAll() {
        return KFileUtils.getInstance().delAllFiles(cacheDir, null);
    }


//    async {
//        KFileLoadUtils.getInstance().delLoadDownApk() fixme  (建议：可以在应用第一次启动的时候调用删除；这样就能删除上一个版本的安装包。)
//    }

    /**
     * fixme 删除下载目录的所有APK安装包。(建议：可以在应用第一次启动的时候调用删除；这样就能删除上一个版本的安装包。)
     *
     * @return
     */
    public boolean delLoadDownApk() {
        return KFileUtils.getInstance().delAllFiles(cacheDir, ".apk");
    }

    //回调接口
    public interface RequestCallBack {
        public boolean isCall = false;//是否已經回調

        //开始下载，即网络链接成功。
        public void onStart();

        /**
         * 成功
         *
         * @param file 下载文件
         */
        public void onSuccess(File file);

        /**
         * 进度
         *
         * @param current 当前下载大小
         * @param max     总大小
         * @param bias    下载百分比（0-100）
         */
        public void onLoad(long current, long max, int bias);

        /**
         * 失败
         *
         * @param isLoad true 文件已经下载，false文件没有下载
         * @param result 失败原因
         * @param code   错误代码;没有错误代码时，返回的是0(自己加的)
         * @param file   下载的文件
         */
        public void onFailure(Boolean isLoad, String result, int code, File file);
    }


    /**
     * fixme 下载(下载速度杠杠的。)
     *
     * @param context         上下文
     * @param uri             下载链接
     * @param downDir         文件下载路径，可以为空，如果为空。会使用默认下载目录 cacheDir
     * @param srcFileName     文件名，包括后缀。可以为空，如果为null或""空，会自动获取网络上的名称。
     * @param requestCallBack 回调函数
     */
    public void downLoad(Context context, final String uri, final String downDir, final String srcFileName, RequestCallBack requestCallBack) {
        if (context == null || uri == null) {
            return;
        }
        if (requestCallBack != null) {
            mapCallback.put(uri, requestCallBack);//fixme 回调函数要实时更新
        }
        if (mapLoad.get(uri) != null && mapLoad.get(uri)) {
            //判断是否正在下载，防止重复下载。
            return;
        }
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                downLoad2(context, uri, downDir, srcFileName, requestCallBack, 0);
            }
        });
    }

    /**
     * @param context
     * @param uri
     * @param downDir         文件下载路径，可以为空，如果为空。会使用默认下载目录 cacheDir
     * @param srcFileName     文件名，包括后缀。可以为空，如果为null或""空，会自动获取网络上的名称。
     * @param requestCallBack
     * @param downCount
     */
    private void downLoad2(Context context, final String uri, final String downDir, final String srcFileName, RequestCallBack requestCallBack, int downCount) {
        try {
            //fixme 修复低版本，如5.0；不识别反斜杠\;需要转换成斜杠才有效，亲测有效。
            String uri2 = uri.replace("\\", "/");
            mapLoad.put(uri2, true);//标志正在下载
            URL url = new URL(uri2);
            //KLoggerUtils.INSTANCE.e("URI:\t"+uri);
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);//连接超时设置，绝对有效。一般50毫秒即可连接成功。fixme （亲测五秒效果最好(之前是3.5秒)，时间太短了，获取网络文件大小可能会不对。）
            conn.setRequestMethod("GET");
            String fileName = srcFileName;//fixme 传入的文件名
            if (fileName == null || (fileName != null && fileName.trim().equals(""))) {
                fileName = getConnFileName(conn).trim().toLowerCase();//转小写
            }
            if (fileName == null || fileName.trim().length() <= 0) {
                fileName = System.currentTimeMillis() + ".apk";//fixme 防止文件名为空，默认作为app安装包处理。
            }
            if (!fileName.contains(".")) {
                fileName = fileName + ".apk";//fixme 没有后缀，也默认作为app安装包处理。防止文件名为空。
            }
            if (downDir != null && downDir.trim().length() > 0) {
                fileName = downDir + "/" + fileName;//文件完整名称，包括路径和文件名后缀
            } else {
                fileName = cacheDir + "/" + fileName;//文件完整名称，包括路径和文件名后缀
            }
            final File file = new File(fileName);
            //file.getName()//fixme 文件名(包含.后缀)
            //KLoggerUtils.INSTANCE.e("fileName:\t"+fileName+"\t"+file.getName());
            if (file.exists() && file.length() > 0 && context != null) {//文件已经存在;file.length() >= 100 下载的文件应该不存在小于100B的文件吧。(B单位是字节)
                if (fileName.contains(".apk") || fileName.contains(".APK")) {//判断下载文件是否是apk包
                    if (file.length() > 100 && getUninatllApkInfo(context, fileName)) {//判断本地apk包是否完整
                        RequestCallBack requestCallBack1 = mapCallback.get(uri);//fixme 子所以新建变量，防止线程跳转之后，mapCallback被清空。
                        if (requestCallBack1 != null && context != null && context instanceof Activity) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //主线程回调
                                    if (requestCallBack1 != null) {
                                        requestCallBack1.onFailure(true, KBaseUi.Companion.getString(R.string.kappdown), 0, file);//"apk已經下載"
                                    }
                                }
                            });
                        } else {
                            if (requestCallBack1 != null) {
                                requestCallBack1.onFailure(true, KBaseUi.Companion.getString(R.string.kappdown), 0, file);//"apk已經下載"
                            }
                        }
                        conn.disconnect();//断开链接
                        return;
                    }
                }
                // 设置 User-Agent,这个设不设置没有影响
                conn.setRequestProperty("User-Agent", "NetFox");
                //fixme 设置断点续传的开始位置,这个是关键，末尾必须加"-"。会返回206
                String RANGE = "bytes=" + (file.length()) + "-";
                conn.setRequestProperty("RANGE", RANGE);
            } else {
                new File(cacheDir).mkdirs();//fixme 新建文件夹(可以多级创建,mkdir不能多级创建),没有才创建，不会覆盖原有文件夹
                file.createNewFile();//新建文件,没有则创建，有，则不会创建，即不会覆盖原有文件
            }
            //KLoggerUtils.INSTANCE.e("文件大小：\t" + file.length() + "\t路径：\t" + file.getAbsolutePath());
            if (mapCallback.get(uri) != null) {
                RequestCallBack requestCallBack1 = mapCallback.get(uri);
                if (context != null && context instanceof Activity) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //主线程回调
                            if (requestCallBack1 != null) {
                                requestCallBack1.onStart();//fixme 开始下载回调。
                            }
                        }
                    });
                } else {
                    requestCallBack1.onStart();
                }
            }
            int ResponseCode = conn.getResponseCode();//一旦调用了get函数，就不能再设置参数了。
            //file.length()和conn.getContentLength()是对等。都是一个文件的实际大小。
            long max = conn.getContentLength();//文件总大小,一旦调用了这个方法，就不能再设置参数了【如：setRequestProperty】//fixme 安装包异常，就是这个获取网络文件实际大小错误导致的。239，150可能就是获取错误(偶尔会发生！)。
            //KLoggerUtils.INSTANCE.e("下载文件总大小：\t" + max + "\t本地已存文件总大小:\t" + file.length() + "\tRANGE:\t" + conn.getRequestProperty("RANGE") + "\tResponseCode:\t" + ResponseCode + "\tdownCount:\t" + downCount);
            if (max > 0 && max <= 600 && downCount <= 3) {//小于600B;下载文件不肯能这么小，估计获取异常了。
                //if (downCount < 3) {//测试(亲测没问题)
                //KLoggerUtils.INSTANCE.e("进来了");
                conn.disconnect();//断开链接
                downCount++;
                downLoad2(context, uri, downDir, srcFileName, requestCallBack, downCount);//fixme 文件大小获取错误，重新获取，亲测有效。
                return;
            }
            //判断文件是否存在，以及大小是否相当。避免重复下载。
            if ((ResponseCode == 200 || ResponseCode == 206) && file.length() != max) {
                if (conn.getResponseCode() == 206) {//200连接成功，206断点续传。
                    max = file.length() + max;
                }
                InputStream inputStream = conn.getInputStream();
                //下载主要耗时就是花费在对流的读写操作上。
                //持久化操作，将流转化为本地文件
                OutputStream output = new FileOutputStream(file, true);//参数二 true,在文件末尾继续写，否则会覆盖原有文件
                byte[] buffer = new byte[4 * 1024];
                int iLen = 0;
                long currentTimeMillis = 0;
                while ((iLen = inputStream.read(buffer)) != -1) {
                    //耗时操作都在这里。
                    output.write(buffer, 0, iLen);
                    if (mapCallback.get(uri) != null && (System.currentTimeMillis() - currentTimeMillis) > 1000) {
                        float current = file.length();
                        int bias = (int) (current / max * 100);
                        RequestCallBack requestCallBack2 = mapCallback.get(uri);
                        if (requestCallBack2 != null) {
                            if (context != null && context instanceof Activity) {
                                long finalMax = max;
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //主线程回调
                                        if (requestCallBack2 != null) {
                                            requestCallBack2.onLoad((int) current, finalMax, bias);
                                        }
                                    }
                                });
                            } else {
                                requestCallBack2.onLoad((int) current, max, bias);
                            }
                        }
                        currentTimeMillis = System.currentTimeMillis();
                    }
                }
                output.flush();
                output.close();
                RequestCallBack requestCallBack2 = mapCallback.get(uri);
                if (requestCallBack2 != null) {
                    if (context != null && context instanceof Activity) {
                        long finalMax1 = max;
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //主线程回调
                                if (requestCallBack2 != null && file != null) {
                                    requestCallBack2.onLoad(finalMax1, finalMax1, 100);
                                    requestCallBack2.onSuccess(file);
                                }
                            }
                        });
                    } else {
                        mapCallback.get(uri).onLoad(max, max, 100);
                        mapCallback.get(uri).onSuccess(file);
                    }
                }
                inputStream.close();
                inputStream = null;
                System.gc();
            } else {
                //Log.e("test", "其他状态ResponseCode:\t" + ResponseCode);
                //416文件已经下载
                RequestCallBack requestCallBack2 = mapCallback.get(uri);
                if (ResponseCode == 416 && requestCallBack2 != null) {
                    if (context != null && context instanceof Activity) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //主线程回调
                                if (requestCallBack2 != null) {
                                    requestCallBack2.onFailure(true, KBaseUi.Companion.getString(R.string.kfiledown), ResponseCode, file);//"文件已經下載"
                                }
                            }
                        });
                    } else {
                        requestCallBack2.onFailure(true, KBaseUi.Companion.getString(R.string.kfiledown), ResponseCode, file);//"文件已經下載"
                    }
                } else {
                    //其他连接状态
                    if (requestCallBack2 != null) {
                        if (context != null && context instanceof Activity) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //主线程回调
                                    if (requestCallBack2 != null) {
                                        requestCallBack2.onFailure(false, KBaseUi.Companion.getString(R.string.kappdownfail), ResponseCode, file);//下载失败
                                    }
                                }
                            });
                        } else {
                            requestCallBack2.onFailure(false, KBaseUi.Companion.getString(R.string.kappdownfail), ResponseCode, file);//下载失败
                        }
                    }
                }
            }
        } catch (Exception e) {
            try {
                RequestCallBack requestCallBack1 = mapCallback.get(uri);
                if (requestCallBack1 != null) {
                    String errorMsg = e.getMessage();
                    if (errorMsg != null && (errorMsg.contains("recvfrom failed") || errorMsg.contains("Connection timed out") || errorMsg.toLowerCase().contains("failed to connect"))) {
                        errorMsg = KBaseUi.Companion.getString(R.string.kconnetfailure_filedown);//下载失败，网络连接超时
                    }
                    if (context != null && context instanceof Activity) {
                        String finalErrorMsg = errorMsg;
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //主线程回调
                                if (requestCallBack1 != null) {
                                    requestCallBack1.onFailure(false, finalErrorMsg, 0, null);
                                }
                            }
                        });
                    } else {
                        requestCallBack1.onFailure(false, errorMsg, 0, null);
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            KLoggerUtils.INSTANCE.e("下载失败异常:\t" + e.getMessage());
        } finally {
            //标志下载结束
            mapLoad.put(uri, false);
            mapLoad.remove(uri);
            mapCallback.remove(uri);
            //Log.e("test", "结束");
        }
    }

    /**
     * 根据url,获取文件路径，获取文件名称（包括后缀）。
     *
     * @param url
     * @return
     */
    public String getUrlFileName(String url) {
        try {
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.lastIndexOf("?"));//去除?号后面的参数
            }
            //或 url.substring(url.lastIndexOf("\\")+1); //fixme "/" 和 "\\" 是一样的。亲测可行。
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    //获取网络文件名称(包括文件名后缀)
    public String getConnFileName(HttpURLConnection conn) {
        try {
            String fileName = conn.getURL().getFile();//conn.getURL()真实的URL
            //KLoggerUtils.INSTANCE.e("fileName:\t"+fileName);
            //fixme 一般都能获取成功；(如果uri包含了斜杠，则无法识别。也无法获取网络名称了。)；就是因为包含了斜杠，无法识别uri；所以文件名才获取为空的。
            //fixme 修复低版本，如5.0；不识别斜杠\;需要转换成放斜杠才有效，亲测有效。
            //fixme String uri2=uri.replace("\\", "/");
            if (fileName == null || fileName.trim().length() <= 0) {
                fileName = conn.getURL().toString();//fixme 防止低版本为空。（5.0系统，getURL().getFile()可能返回为空。）
            }
            if (fileName.contains("/")) {
                fileName = fileName
                        .substring(fileName.lastIndexOf("/") + 1);// fixme 通过最真实的url获取文件的真实名称(能够获取文件名后缀)
            }
            if (fileName.contains("\\")) {
                fileName = fileName
                        .substring(fileName.lastIndexOf("\\") + 1);
            }
            //去除不合法的字符，以免本地文件生成失败
            fileName = fileName.replace('/', '0');
            fileName = fileName.replace('\\', '0');
            fileName = fileName.replace('|', '0');
            fileName = fileName.replace('?', '0');
            fileName = fileName.replace('<', '0');
            fileName = fileName.replace('>', '0');
            fileName = fileName.replace('*', '0');
            fileName = fileName.replace(':', '0');
            fileName = fileName.replace('"', '0');

            fileName = fileName.trim().toLowerCase();
            if (fileName.contains(".apk")) {
                fileName = fileName.substring(0, fileName.lastIndexOf(".apk") + 4);//以方万一，防止后缀后面还有其他的字符。
            }
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    //将输入流转换为文本【如，读取网络输入流文本】
    public String getConnAsString(InputStream inputStream) throws Exception {
        StringBuilder sb = new StringBuilder();
        String temp = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        while ((temp = br.readLine()) != null) {
            //Log.e("test", "读取：\t" + temp);
            sb.append(temp);
        }
        return sb.toString();//转化为文本
    }

    /**
     * 判断apk安装包是否完整
     *
     * @param filePath
     * @return true完整， false apk有损坏，不完整。
     */
    public boolean getUninatllApkInfo(Context context, String filePath) {
        boolean result = false;
        try {
            if (context != null && filePath != null) {
                PackageManager pm = context.getPackageManager();
                PackageInfo info = pm.getPackageArchiveInfo(filePath,
                        PackageManager.GET_ACTIVITIES);
                if (info != null) {
                    result = true;
                }
            }
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    /**
     * 判断下载apk，本地是否已经存在。以免重复下载。
     *
     * @param uri         下载链接
     * @param srcFileName 文件名，包括后缀。如果为null或""空，会自动获取网络上的名称。
     * @return 返回apk完整路径，本地已存在。 null 不存在
     */
    public String judgeApk(Context context, final String uri, final String srcFileName) {
        try {
            String fileName = srcFileName;
            if (fileName == null || (fileName != null && fileName.trim().equals(""))) {
                URL url = new URL(uri);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                fileName = getConnFileName(conn);
                conn.disconnect();//断开链接
            }
            fileName = cacheDir + "/" + fileName;//文件完整名称，包括路径和文件名后缀
            final File file = new File(fileName);
            if (file.exists()) {//文件已经存在
                if (fileName.contains(".apk")) {//判断下载文件是否是apk包
                    if (getUninatllApkInfo(context, fileName)) {//判断本地apk包是否完整
                        return fileName;//本地已经存在
                    }
                }
            }
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("test", "本地apk判断异常:\t" + e.getMessage());
        }
        return null;
    }

}

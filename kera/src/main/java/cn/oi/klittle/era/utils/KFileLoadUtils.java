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

//            fixme kotlin使用案例
//            var url="http://dl.ludashi.com/ludashi/ludashi_home.apk"//鲁大师app
//            KFileLoadUtils.getInstance(ctx).downLoad(url, "123.apk", object : KFileLoadUtils.RequestCallBack {
//                override fun onStart() {
//                    //开始下载
//                    KLoggerUtils.e("开始下载")
//                }
//
//                override fun onFailure(isLoad: Boolean?, result: String?, file: File?) {
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
 * 文件下载工具类，支持断点下载。
 * 上传就不需要单独写工具类了【所谓的上传就是将文件(File file)作为参数通过Http协议Post提交即可】,如：params.addBodyParameter(key, file);
 * Created by 彭治铭 on 2017/5/24.
 */

public class KFileLoadUtils {
    private static KFileLoadUtils fileDown;
    private ThreadPoolExecutor threadPoolExecutor;
    //文件下载目录
    public String cacheDir;
    //判断该uri是否正在下载
    private Map<String, Boolean> mapLoad;
    private Map<String, RequestCallBack> mapCallback;

    //构造函数
    private KFileLoadUtils() {
        Context context = KBaseApplication.getInstance();
        //this.cacheDir = context.getApplicationContext().getFilesDir().getAbsolutePath();//这个地址，文件无法分享。(内部位置无法分享出去),不需要权限
        this.cacheDir = context.getApplicationContext().getExternalCacheDir().getAbsolutePath();//这个位置，可以分享。（SD卡的东西可以分享出去）,不需要权限。推荐使用这个
        //this.cacheDir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();//需要SD卡权限
        mapLoad = new HashMap<>();
        mapCallback = new HashMap<>();
        int corePoolSize = Runtime.getRuntime().availableProcessors() + 2;
        int maxinumPoolSize = corePoolSize * 2 + 1;
        long keepAliveTime = 10;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<>();
        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxinumPoolSize, keepAliveTime, unit, workQueue);
    }

    //初始化
    public static KFileLoadUtils getInstance() {
        if (fileDown == null) {
            fileDown = new KFileLoadUtils();
        }
        return fileDown;
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
         * @param file   下载的文件
         */
        public void onFailure(Boolean isLoad, String result, File file);
    }


    /**
     * 下载(下载速度杠杠的。)
     *
     * @param context         上下文
     * @param uri             下载链接
     * @param srcFileName     文件名，包括后缀。如果为null或""空，会自动获取网络上的名称。
     * @param requestCallBack 回调函数
     */
    public void downLoad(Context context, final String uri, final String srcFileName, RequestCallBack requestCallBack) {
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
                try {
                    mapLoad.put(uri, true);//标志正在下载
                    URL url = new URL(uri);
                    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(3500);//连接超时设置，绝对有效。一般50毫秒即可连接成功。
                    conn.setRequestMethod("GET");
                    String fileName = srcFileName;
                    if (fileName == null || (fileName != null && fileName.trim().equals(""))) {
                        fileName = getConnFileName(conn).trim().toLowerCase();
                    }
                    fileName = cacheDir + "/" + fileName;//文件完整名称，包括路径和文件名后缀
                    final File file = new File(fileName);
                    if (file.exists() && context != null) {//文件已经存在
                        if (fileName.contains(".apk")) {//判断下载文件是否是apk包
                            if (getUninatllApkInfo(context, fileName)) {//判断本地apk包是否完整
                                RequestCallBack requestCallBack1 = mapCallback.get(uri);//fixme 子所以新建变量，防止线程跳转之后，mapCallback被清空。
                                if (requestCallBack1 != null && context != null && context instanceof Activity) {
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //主线程回调
                                            if (requestCallBack1 != null) {
                                                requestCallBack1.onFailure(true, KBaseUi.Companion.getString(R.string.kappdown), file);//"apk已經下載"
                                            }
                                        }
                                    });
                                } else {
                                    if (requestCallBack1 != null) {
                                        requestCallBack1.onFailure(true, KBaseUi.Companion.getString(R.string.kappdown), file);//"apk已經下載"
                                    }
                                }
                                conn.disconnect();//断开链接
                                return;
                            }
                        }
                        // 设置 User-Agent,这个设不设置没有影响
                        conn.setRequestProperty("User-Agent", "NetFox");
                        // 设置断点续传的开始位置,这个是关键，末尾必须加"-"。会返回206
                        String RANGE = "bytes=" + (file.length()) + "-";
                        conn.setRequestProperty("RANGE", RANGE);
                    } else {
                        new File(cacheDir).mkdirs();//新建文件夹(可以多级创建,mkdir不能多级创建),没有才创建，不会覆盖原有文件夹
                        file.createNewFile();//新建文件,没有则创建，有，则不会创建，即不会覆盖原有文件
                    }
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
                    long max = conn.getContentLength();//文件总大小,一旦调用了这个方法，就不能再设置参数了【如：setRequestProperty】
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
                                            requestCallBack2.onFailure(true, KBaseUi.Companion.getString(R.string.kfiledown), file);//"文件已經下載"
                                        }
                                    }
                                });
                            } else {
                                requestCallBack2.onFailure(true, KBaseUi.Companion.getString(R.string.kfiledown), file);//"文件已經下載"
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
                                                requestCallBack2.onFailure(false, KBaseUi.Companion.getString(R.string.kappdownfail)+":\t"+ResponseCode, file);//下载失败
                                            }
                                        }
                                    });
                                } else {
                                    requestCallBack2.onFailure(false, KBaseUi.Companion.getString(R.string.kappdownfail)+":\t"+ResponseCode, file);//下载失败
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    try {
                        RequestCallBack requestCallBack1 = mapCallback.get(uri);
                        if (requestCallBack1 != null) {
                            if (context != null && context instanceof Activity) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //主线程回调
                                        if (requestCallBack1 != null) {
                                            requestCallBack1.onFailure(false, e.getMessage(), null);
                                        }
                                    }
                                });
                            } else {
                                requestCallBack1.onFailure(false, e.getMessage(), null);
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
        });
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
            fileName = fileName
                    .substring(fileName.lastIndexOf("/") + 1);// fixme 通过最真实的url获取文件的真实名称(能够获取文件名后缀)
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

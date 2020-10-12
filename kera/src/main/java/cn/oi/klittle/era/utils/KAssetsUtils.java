package cn.oi.klittle.era.utils;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cn.oi.klittle.era.R;
import cn.oi.klittle.era.base.KBaseApplication;
import cn.oi.klittle.era.base.KBaseCallBack;
import cn.oi.klittle.era.base.KBaseUi;
import cn.oi.klittle.era.comm.kpx;
import cn.oi.klittle.era.exception.KCatchException;


/**
 * Created by 彭治铭 on 2017/3/6.
 */

public class KAssetsUtils {
    private AssetManager am;
    private static KAssetsUtils assets;

    private KAssetsUtils() {
        am = KBaseApplication.getInstance().getResources().getAssets();
        map = new HashMap<>();
        mapViews = new HashMap<>();
    }

    //初始化(兼容了Context和Activity,以Activity为主)
    public static KAssetsUtils getInstance() {
        if (assets == null) {
            assets = new KAssetsUtils();
        }
        return assets;
    }

    //位图数组(java对象赋值是传引用，都指向同一个对象);fixme 静态对象，全局就一个。
    private static HashMap<String, Bitmap> map;

    //fixme 获取存储键；

    /**
     * @param path         路径(优先级比id高)
     * @param resID        id
     * @param isRGB_565
     * @param inSampleSize 采样率。防止内存溢出。1 是正常。2长和宽缩小到2分之一。
     * @return
     */
    public String getKey(String path, int resID, boolean isRGB_565, int inSampleSize) {
        String key = null;
        if (path != null && !path.equals("")) {
            key = "" + path + inSampleSize + isRGB_565;
        } else {
            key = "" + resID + "" + inSampleSize + isRGB_565;
        }
        return key;
    }

    //pathName
    public String getKeyForPath(String path, boolean isRGB_565, int inSampleSize) {
        return getKey(path, 0, isRGB_565, inSampleSize);
    }

    public String getKeyForPath(String path, int inSampleSize) {
        return getKey(path, 0, false, inSampleSize);
    }

    public String getKeyForPath(String path, boolean isRGB_565) {
        return getKey(path, 0, isRGB_565, 1);
    }

    //path是本地路径
    public String getKeyForPath(String path, boolean isRGB_565, int width, int height) {
        return getKey(path, 0, isRGB_565, getInSampleSizeFromFile(width, height, path));
    }

    public String getKeyForPath(String path, int width, int height) {
        return getKey(path, 0, false, getInSampleSizeFromFile(width, height, path));
    }

    public String getKeyForAssets(String path, boolean isRGB_565, int width, int height) {
        return getKey(path, 0, isRGB_565, getInSampleSizeFromAssets(width, height, path));
    }

    public String getKeyForAssets(String path, boolean isRGB_565) {
        return getKey(path, 0, isRGB_565, 1);
    }

    public String getKeyForUrl(String url, int width, int height) {
        if (width <= 0 && height <= 0) {
            return url;
        }
        //fixme 好了，以下的规则就不要随便变了。
        if (width > kpx.INSTANCE.x(100)) {
            int inSampleSize_width = width / kpx.INSTANCE.x(150);//大小差不多就可以使用一个标志，差距太大了就不行。
            int inSampleSize_height = height / kpx.INSTANCE.x(150);
            return url + "inSampleSize_width_" + inSampleSize_width + "_inSampleSize_height_" + inSampleSize_height;
        } else {
            int inSampleSize_width = width / kpx.INSTANCE.x(20);//大小差不多就可以使用一个标志，差距太大了就不行。
            int inSampleSize_height = height / kpx.INSTANCE.x(20);
            return url + "inSampleSize_width2_" + inSampleSize_width + "_inSampleSize_height2_" + inSampleSize_height;
        }
        //return url+"_width_"+width+"_height_"+height;
    }

    public String getKeyForPath(String path) {
        return getKey(path, 0, false, 1);
    }

    //resID
    public String getKeyForRes(int resID, boolean isRGB_565, int inSampleSize) {
        return getKey(null, resID, isRGB_565, inSampleSize);
    }

    public String getKeyForRes(int resID, int inSampleSize) {
        return getKey(null, resID, false, inSampleSize);
    }

    public String getKeyForRes(int resID, int width, int height) {
        return getKey(null, resID, false, getInSampleSizeFromRes(width, height, resID));
    }

    public String getKeyForRes(int resID, boolean isRGB_565) {
        return getKey(null, resID, isRGB_565, 1);
    }

    public String getKeyForRes(int resID, boolean isRGB_565, int width, int height) {
        return getKey(null, resID, isRGB_565, getInSampleSizeFromRes(width, height, resID));
    }

    public String getKeyForRes(int resID) {
        return getKey(null, resID, false, 1);
    }

    //获取缓存位图
    public Bitmap getCacleBitmap(String key) {
        try {
            if (map.containsKey(key) && map.get(key) != null && !map.get(key).isRecycled()) {//确保Bitmap不为null
                return map.get(key);//防止重复加载，浪费内存
            }
            if (map.containsKey(key)) {
                try {
                    map.remove(key);//移除多余无用的键值
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //设置缓存位图
    public void setCacleBiatmap(String key, Bitmap bitmap) {
        if (key == null || bitmap == null) {
            return;
        }
//        fixme 为了程序安全性，不要在这里释放位图，不然问题多多。亲测不好。
//        fixme 多个控件同时加载同一张图片时，问题就出来了（崩溃），所以这里不要私自释放掉位图。
//        if (map.containsKey(key)) {
//            Bitmap keyBitmap = map.get(key);
//            if (bitmap != null && !bitmap.isRecycled() && keyBitmap != null && !keyBitmap.isRecycled() && keyBitmap != bitmap) {
//                //释放掉之前的位图。（保存的位图对象不同时）
//                if (!bitmap.equals(keyBitmap) && (bitmap.getWidth() != keyBitmap.getWidth() || bitmap.getHeight() != keyBitmap.getHeight())) {
//                    //Log.e("test","位图释放：\t"+bitmap.getWidth()+"\t:\t"+keyBitmap.getWidth());
//                    recycleBitmap(key);//fixme 释放原有位图
//                    keyBitmap = null;
//                }
//            } else {
//                keyBitmap = null;
//                return;
//            }
//        }
        map.put(key, bitmap);
    }

    //释放单个位图
    public void recycleBitmap(String key) {
        try {
            if (map.containsKey(key) && map.get(key) != null && !map.get(key).isRecycled()) {//确保Bitmap不为null
                map.get(key).recycle();
                map.remove(key);
                //KLoggerUtils.INSTANCE.e("释放：\t"+key);
            } else if (map.containsKey(key)) {
                map.remove(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //保存每一个View，防止View重复适配
    private HashMap<String, View> mapViews;

    /**
     * 【asset/res和被绑定在apk里，并不会解压到/data/data/YourApp目录下去，所以我们无法直接获取到assets的绝对路径，因为它们根本就没有。只能把里面的文件复制出来再操作】
     * 获取assets下文件的绝对路径【只是针对html的展示来使用的，比如webview。其他情况不行】
     *
     * @param fileName 文件名,如("文件夹/文件名.后缀"),直接写assets下的文件目录即可。
     * @return
     */
    public String getAssetsPath(String fileName) {
        return "file:///android_asset/" + fileName;//fixme 只是针对html的展示来使用的，比如webview。其他情况不行；其他情况就按照正常路径即可。
    }

    /**
     * 获取Assets目录下的文件流。
     *
     * @param assetsPath assets 里的文件。如("文件夹/文件名.后缀");就是正常的路径。
     * @return
     */
    public InputStream getInputStream(String assetsPath) {
        try {
            return am.open(assetsPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取Resouce资源目录下的流。
     *
     * @param resId
     * @return
     */
    public InputStream getInputStreamFromResouce(int resId) {
        try {
            return KBaseApplication.getInstance().getResources().openRawResource(resId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取Assets目录下的文本。
     *
     * @param assetsPath assets 里的文件。如("文件夹/文件名.后缀");就是正常的路径。
     * @return
     */
    public String getString(String assetsPath) {
        try {
            InputStream input = getInputStream(assetsPath);
            StringBuffer out = new StringBuffer();
            byte[] b = new byte[4096];
            for (int n; (n = input.read(b)) != -1; ) {
                out.append(new String(b, 0, n));//fixme 将流转成文本。
            }
            input.close();
            input = null;
            return out.toString();
        } catch (Exception e) {
        }
        return "";
    }

    /**
     * 保存Bitmap位图到本地。
     * 兼容以前。首页保留这个方法。
     *
     * @param bitmap
     * @param path    路径 如：context.getApplicationContext().getFilesDir().getAbsolutePath();
     * @param picName 图片名称，记得要有.png的后缀。【一定要加.png的后缀】
     * @return 返回保存文件
     */
    public File saveBitmap(Bitmap bitmap, String path, String picName) {
        return KFileUtils.getInstance().saveBitmap(bitmap, path, picName);
    }

    /**
     * 复制assets文件到指定目录
     *
     * @param assetsPath assets里的文件目录
     * @param filePath   要复制到文件目录。是目录（只要路径。不需要文件名）
     * @param callBack   回调
     */
    public void copyFileFromAssets(final String assetsPath, final String filePath, final KBaseCallBack<File> callBack) {
        copyFileFromAssets(assetsPath, filePath, null, callBack);
    }

    /**
     * 复制assets文件到指定目录
     *
     * @param assetsPath assets 里的文件。如("文件夹/文件名.后缀")
     * @param path       指定路径 如：context.getApplicationContext().getFilesDir().getAbsolutePath();
     * @param fileName   文件名(包括后缀名)；可以为空。如果会空，会自动获取文件名。
     * @param callBack   回调，返回文件
     * @return
     */
    public void copyFileFromAssets(final String assetsPath, final String path, final String fileName, final KBaseCallBack<File> callBack) {
        if (assetsPath == null) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                super.run();
                File fs = null;
                try {
                    String name = fileName;
                    if (fileName == null) {
                        //文件名为空，主动获取文件名(包括后缀)
                        name = KFileUtils.getInstance().getFileName(assetsPath);
                    }
                    boolean pathHasSuffix = false;//fixme 判断路径是否包含文件完整名。
                    if (path != null) {
                        String suffix = KFileUtils.getInstance().getFileSuffix(assetsPath, true);
                        if (path.contains(suffix)) {//判断路径是否包文件名。
                            fs = new File(path);
                            pathHasSuffix = true;
                        }
                    }
                    if (fs == null) {
                        fs = new File(path, name);
                    }
//                    File target = new File(getAssetsPath(assetsPath));//fixme assets下来文件，无法直接读取，即无法判断大小。
//                    KLoggerUtils.INSTANCE.e("文件大小：\t"+target.length());
//                    if (fs.exists() && target.length() != fs.length()) {//fixme 无法直接读取assets目录下的文件大小。
//                        fs.delete();//不一样，就删除
//                    }
//                    target = null;
                    if (fs.exists() && fs.length() <= 1024) {//文件小于1KB,就当错误文件处理删除掉。
                        //一个普通excel表格的大小是 34630
                        fs.delete();//fixme 如果文件太小就删除掉(防止文件错误)
                    }
                    if (!fs.exists() ) {//判断文件是否存在，不存在则创建
                        if (!path.equals(fs.getAbsolutePath()) && !pathHasSuffix) {//这里的判断是防止path就是文件的完整路径。防止完整路径被创建成目录。
                            File dirs = new File(path);
                            //dirs.isDirectory() 判断是否为目录，前提是该目录必须存在。不存在也会返回false
                            if (!dirs.exists()) {
                                dirs.mkdirs();//防止目录不存在所以创建
                            }
                            dirs = null;
                        }
                        if (!fs.exists()) {
                            fs.createNewFile();//创建文件
                        }
                        InputStream myInput;
                        OutputStream myOutput = new FileOutputStream(fs);
                        myInput = am.open(assetsPath);
                        byte[] buffer = new byte[1024];
                        int length = myInput.read(buffer);
                        while (length > 0) {
                            myOutput.write(buffer, 0, length);
                            length = myInput.read(buffer);
                        }
                        myOutput.flush();
                        myInput.close();
                        myOutput.close();
                        myOutput = null;
                        myInput = null;
                        KPictureUtils.INSTANCE.updateFileFromDatabase_add(fs, KBaseUi.Companion.getActivity());//fixme 通知系统。更新该文件目录。（只对SD卡上的目录有效。）
                    }
                    if (callBack != null) {
                        callBack.onResult(fs);
                    }
                } catch (Exception e) {
                    try {
                        if (fs != null && fs.exists()) {
                            fs.delete();
                            fs = null;
                        }
                    } catch (Exception e1) {
                    }
                    KLoggerUtils.INSTANCE.e("assets文件复制错误:\t" + KCatchException.getExceptionMsg(e), true);
                }
            }
        }.start();
    }

    /**
     * 从本地加载Bitmap
     *
     * @param path      图片完整路径，保存路径和文件后缀名。
     * @param isRGB_565 true 节省内存(推荐)，false不节省内存(效果较好)
     * @param isCache   fixme 是否读取缓存[注意了哦。如果图片剪切了，就不要读取缓存哦。]
     * @return
     */
    public Bitmap getBitmapFromFile(String path, boolean isRGB_565, boolean isCache) {
        return getBitmapFromFile(path, isRGB_565, 1, isCache);
    }

    public Bitmap getBitmapFromFile(String path, boolean isRGB_565) {
        return getBitmapFromFile(path, isRGB_565, 1, true);//默认读取缓存
    }

    public Bitmap getBitmapFromFile(String path, boolean isRGB_565, int width, int height) {
        return getBitmapFromFile(path, isRGB_565, getInSampleSizeFromFile(width, height, path), true);//默认读取缓存
    }

    public Bitmap getBitmapFromFile(String path) {
        return getBitmapFromFile(path, false, 1, true);//默认读取缓存
    }

    public Bitmap getBitmapFromFile(String path, boolean isRGB_565, int inSampleSize) {
        return getBitmapFromFile(path, isRGB_565, inSampleSize, true);//默认读取缓存
    }


    /**
     * 根据路径获取SD卡上面的，文件。
     *
     * @param path
     * @param isRGB_565
     * @param inSampleSize SD卡上的文件，添加了 采样率。防止内存溢出。1 是正常。2长和宽缩小到2分之一。4就缩小到4分之一。
     * @param isCache      是否读取缓存
     * @return
     */
    public Bitmap getBitmapFromFile(String path, boolean isRGB_565, int inSampleSize, Boolean isCache) {
        //String key = pathName + inSampleSize + isRGB_565;
        String key = getKeyForPath(path = path, isRGB_565 = isRGB_565, inSampleSize = inSampleSize);
        Bitmap bitmap = getCacleBitmap(key);//获取缓存位图
        if (bitmap != null && !bitmap.isRecycled()) {
            if (isCache) {
                return bitmap;//防止重复加载，浪费内存
            } else {
                bitmap.recycle();//释放
            }
        }
        try {
            if (isRGB_565) {
                bitmap = BitmapFactory.decodeFile(path, getOptionsRGB_565(inSampleSize));
            } else {
                bitmap = BitmapFactory.decodeFile(path, getOptionsARGB_8888(inSampleSize));
//                FileInputStream fis = new FileInputStream(path);
//                bitmap = BitmapFactory.decodeStream(fis, null, getOptionsARGB_8888(inSampleSize));
//                fis.close();
//                fis = null;
            }
            //解决图片方向显示不正确的问题。(图片模糊和这行没有关系，亲测。)
            bitmap = KPictureUtils.INSTANCE.rotateBitmap(bitmap, path);
            //保存当前Bitmap
            setCacleBiatmap(key, bitmap);
        } catch (Exception e) {
            Log.e("test", "File流异常" + e.getMessage());
        }
        return bitmap;
    }

    //fixme 通过第三方库获取位图（位图不会糊，很清晰，效果很好。所以采用，谷歌也推荐使用）
    //fixme 注意，调用的时候，必须在子线程中调用。
    public Bitmap getBitmapFromFileFromGlide(String path, Boolean isCache, int overrideWidth, int overrideHeight) {
        String key = getKeyForPath(path = path, overrideWidth, overrideHeight);
        Bitmap bitmap = getCacleBitmap(key);//获取缓存位图
        if (bitmap != null && !bitmap.isRecycled()) {
            if (isCache) {
                return bitmap;//防止重复加载，浪费内存
            } else {
                bitmap.recycle();//释放
            }
        }
        try {
            RequestOptions options = new RequestOptions();
            if (overrideWidth <= 0 && overrideHeight <= 0) {
                options.sizeMultiplier(0.5f);//0~1之间
            } else {
                options.override(overrideWidth, overrideHeight);//限制图片的大小
            }
            options.skipMemoryCache(true);// 不使用内存缓存
            options.diskCacheStrategy(DiskCacheStrategy.NONE); // 不使用磁盘缓存
            //options.diskCacheStrategy(DiskCacheStrategy.ALL);
            options.centerCrop();//居中剪切
            //options.placeholder(R.drawable.image_placeholder)
            //必须放在子线程中执行
            bitmap = Glide.with(KBaseApplication.getInstance())
                    .asBitmap()
                    .load(path)
                    .apply(options)
                    .submit().get();
            //fixme Glide内部已经解决了图片方向不正确的问题了。
            //解决图片方向显示不正确的问题。(图片模糊和这行没有关系，亲测。)
            //bitmap = KPictureUtils.INSTANCE.rotateBitmap(bitmap, path);
            //保存当前Bitmap
            setCacleBiatmap(key, bitmap);
        } catch (Exception e) {
            Log.e("test", "File流异常" + e.getMessage());
        }
        return bitmap;
    }


    /**
     * @param path 文件路径，SD上的路径。不是assets
     * @return 返回位图的宽或高，谁大返回谁。返回较大的一方。（主要用于计算inSampleSize）
     */
    public int getBitmapSizeFromFile(String path) {
        /**
         * 重要说明，一个BitmapFactory.Options对应一个Bitmap位图，不能共用【共用了之后，反而占内存】。
         * 必须重新实例化一个Options，才有效果。
         */
        //bitmap所占内存大小计算方式：图片长度 x 图片宽度 x 一个像素点占用的字节数（bitmap占用内存大小和图片本身大小无关）
        BitmapFactory.Options options = new BitmapFactory.Options();
        //为true时不会真正加载图片到内存，仅仅是得到图片尺寸信息，存在Options.outHeight和outWidth和outMimeType中
        options.inJustDecodeBounds = true;
        if (path != null && !path.trim().equals("")) {
            BitmapFactory.decodeFile(path, options);
        }
//        else {
//            BitmapFactory.decodeResource(BaseApplication.getInstance().getResources(), resId, options);
//        }
        return options.outHeight > options.outWidth ? options.outHeight : options.outWidth;//返回位图宽或高。较大的一个。谁大返回谁。
    }

    /**
     * 自动计算inSampleSize
     *
     * @param width  要求图片的宽度
     * @param height 要求图片的高度
     * @param path   图片本地路径
     * @return
     */
    public int getInSampleSizeFromFile(int width, int height, String path) {
        if (width <= 0 && height <= 0 || path == null) {
            return 1;
        }
        int size = width;
        if (size < height) {
            size = height;//取较长边
        }
        int size2 = getBitmapSizeFromFile(path);
        if (size2 > size) {
            size = size2 / size;
            if (size > 1) {
                return size;
            }
        }
        return 1;
    }

    public int getBitmapSizeFromRes(int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(KBaseApplication.getInstance().getResources(), resId, options);
        return options.outHeight > options.outWidth ? options.outHeight : options.outWidth;//返回位图宽或高。较大的一个。谁大返回谁。
    }

    public int getInSampleSizeFromRes(int width, int height, int resId) {
        if (width <= 0 && height <= 0) {
            return 1;
        }
        int size = width;
        if (size < height) {
            size = height;//取较长边
        }
        int size2 = getBitmapSizeFromRes(resId);
        if (size2 > size) {
            size = size2 / size;
            if (size > 1) {
                return size;
            }
        }
        return 1;
    }

    public int getBitmapSizeFromAssets(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        if (path != null && !path.trim().equals("")) {
            try {
                InputStream is = am.open(path);
                BitmapFactory.decodeStream(is, null, options);
                is.close();
                is = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return options.outHeight > options.outWidth ? options.outHeight : options.outWidth;//返回位图宽或高。较大的一个。谁大返回谁。
    }

    public int getInSampleSizeFromAssets(int width, int height, String path) {
        if (width <= 0 && height <= 0 || path == null) {
            return 1;
        }
        int size = width;
        if (size < height) {
            size = height;//取较长边
        }
        int size2 = getBitmapSizeFromAssets(path);
        if (size2 > size) {
            size = size2 / size;
            if (size > 1) {
                return size;
            }
        }
        return 1;
    }

    /**
     * @param path 文件路径，SD上的路径。不是assets
     * @return 返回位图的宽
     */
    public int getBitmapWidth(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //为true时不会真正加载图片到内存，仅仅是得到图片尺寸信息，存在Options.outHeight和outWidth和outMimeType中
        options.inJustDecodeBounds = true;
        if (path != null && !path.trim().equals("")) {
            BitmapFactory.decodeFile(path, options);
        }
        return options.outWidth;//返回位图宽
    }

    /**
     * @param path 文件路径，SD上的路径。不是assets
     * @return 返回位图的高。
     */
    public int getBitmapHeight(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //为true时不会真正加载图片到内存，仅仅是得到图片尺寸信息，存在Options.outHeight和outWidth和outMimeType中
        options.inJustDecodeBounds = true;
        if (path != null && !path.trim().equals("")) {
            BitmapFactory.decodeFile(path, options);
        }
        return options.outHeight;//返回位图高
    }

    public BitmapFactory.Options getOptionsRGB_565() {
        return getOptionsRGB_565(1);
    }

    /**
     * @param inSampleSize 采样率。1 是正常。2长和宽缩小到2分之一。4就缩小到4分之一。
     * @return
     */
    public BitmapFactory.Options getOptionsRGB_565(int inSampleSize) {
        /**
         * 重要说明，一个BitmapFactory.Options对应一个Bitmap位图，不能共用【共用了之后，反而占内存】。
         * 必须重新实例化一个Options，才有效果。
         */
        //bitmap所占内存大小计算方式：图片长度 x 图片宽度 x 一个像素点占用的字节数（bitmap占用内存大小和图片本身大小无关）
        BitmapFactory.Options optionsRGB_565 = new BitmapFactory.Options();
        optionsRGB_565.inPurgeable = true;//这个是关键。使用之后，基本不吃内存(内存不足是允许系统自动回收)
        optionsRGB_565.inInputShareable = true;//和inPurgeable一起使用才有效。
        //其实如果不需要 alpha 通道，特别是资源本身为 jpg 格式的情况下，用这个格式RGB_565比较理想。
        optionsRGB_565.inPreferredConfig = Bitmap.Config.RGB_565;//ARGB8888格式的图片(默认)，每像素占用 4 Byte，而 RGB565则是 2 Byte。内存可以直接缩小一半
        optionsRGB_565.inSampleSize = inSampleSize;//如果采样率为 2，那么读出来的图片只有原始图片的 1/4 大小。即长宽缩小一半,10就是缩小到原来的10分之1。一般不使用。图片质量会缩水。
        // options.inBitmap=inBitmap;//重用该bitmap的内存。节省内存。两个bitmap的长度和宽度必须一致才有效。才能重用。尽量不要使用。(会报错Problem decoding into existing bitmap)
        return optionsRGB_565;
    }

    public BitmapFactory.Options getOptionsARGB_8888() {
        return getOptionsARGB_8888(1);
    }

    public BitmapFactory.Options getOptionsARGB_8888(int inSampleSize) {
        /**
         * 一个BitmapFactory.Options对应一个Bitmap位图，不然没有效果。
         */
        BitmapFactory.Options optionsARGB_8888 = new BitmapFactory.Options();
        optionsARGB_8888.inJustDecodeBounds = false;//true不会加载位图，只会获取位图的宽和高
        optionsARGB_8888.inPurgeable = true;
        optionsARGB_8888.inInputShareable = true;
        optionsARGB_8888.inPreferredConfig = Bitmap.Config.ARGB_8888;
        optionsARGB_8888.inSampleSize = inSampleSize;
        return optionsARGB_8888;
    }

    /**
     * 获取Res目录下的位图资源
     *
     * @param resID
     * @param isRGB_565
     * @return
     */
    public Bitmap getBitmapFromResource(int resID, boolean isRGB_565) {
        return getBitmapFromAssets(null, resID, isRGB_565);
    }

    public Bitmap getBitmapFromResource(int resID) {
        return getBitmapFromAssets(null, resID, false);
    }

    public Bitmap getBitmapFromResource(int resID, boolean isRGB_565, int width, int height) {
        return getBitmapFromRes(resID, isRGB_565, getInSampleSizeFromRes(width, height, resID));
    }

    public Bitmap getBitmapFromRes(int resID, boolean isRGB_565, int inSampleSize) {
        String key = getKeyForRes(resID = resID, isRGB_565 = isRGB_565, inSampleSize);
        Bitmap bitmap = getCacleBitmap(key);//获取缓存位图
        if (bitmap != null) {
            return bitmap;//防止重复加载，浪费内存
        }
        try {
            if (isRGB_565) {
                bitmap = BitmapFactory.decodeResource(KBaseApplication.getInstance().getResources(), resID, getOptionsRGB_565(inSampleSize));
            } else {
                bitmap = BitmapFactory.decodeResource(KBaseApplication.getInstance().getResources(), resID, getOptionsARGB_8888(inSampleSize));
            }            //保存当前Bitmap
            setCacleBiatmap(key, bitmap);
        } catch (Exception e) {
            Log.e("ui", "assets流异常" + e.getMessage());
        }
        return bitmap;
    }

    private Map<String, Boolean> galidMap = new HashMap<>();

    //fixme 通过第三方库获取位图（位图不会糊，很清晰，效果很好。所以采用，谷歌也推荐使用）
    //fixme 注意，调用的时候，必须在子线程中调用。
    public void getBitmapFromResourceFromGlide(int resID, Boolean isCache, int overrideWidth, int overrideHeight, KBaseCallBack<Bitmap> callBack) {
        String key = getKeyForRes(resID = resID, overrideWidth, overrideHeight);
        Boolean isRepeat = galidMap.get(key);
        galidMap.put(key, true);
        Bitmap bitmap = getCacleBitmap(key);//获取缓存位图
        try {
            if (bitmap != null && !bitmap.isRecycled()) {
                if (isCache) {
                    //return bitmap;//防止重复加载，浪费内存
                    galidMap.put(key, false);
                    galidMap.remove(key);
                    if (callBack != null) {
                        callBack.onResult(bitmap);
                    }
                    return;
                } else {
                    bitmap.recycle();//释放
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isRepeat != null && isRepeat && isCache) {
            //重复了。
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        sleep(300);
                        Bitmap bitmap = getCacleBitmap(key);//获取缓存位图
                        if (bitmap != null && !bitmap.isRecycled()) {
                            try {
                                if (isCache) {
                                    //return bitmap;//防止重复加载，浪费内存
                                    galidMap.put(key, false);
                                    galidMap.remove(key);
                                    if (callBack != null) {
                                        callBack.onResult(bitmap);
                                    }
                                    return;
                                } else {
                                    bitmap.recycle();//释放
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } else {
            final int resID2 = resID;
            KThreadPoolUtils.INSTANCE.getPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        //fixme 注意，调用的时候，也必须在子线程中调用。
                        RequestOptions options = new RequestOptions();
                        if (overrideWidth <= 0 && overrideHeight <= 0) {
                            options.sizeMultiplier(0.5f);//0~1之间
                        } else {
                            options.override(overrideWidth, overrideHeight);//限制图片的大小
                        }
                        options.skipMemoryCache(true);// 不使用内存缓存
                        options.diskCacheStrategy(DiskCacheStrategy.NONE); // 不使用磁盘缓存
                        //options.diskCacheStrategy(DiskCacheStrategy.ALL);
                        options.centerCrop();//居中剪切
                        //options.placeholder(R.drawable.image_placeholder)
                        //必须放在子线程中执行
                        Bitmap bitmap = Glide.with(KBaseApplication.getInstance())
                                .asBitmap()
                                .load(resID2)
                                .apply(options)
                                .submit().get();
                        //fixme Glide内部已经解决了图片方向不正确的问题了。
                        //解决图片方向显示不正确的问题。(图片模糊和这行没有关系，亲测。)
                        //bitmap = KPictureUtils.INSTANCE.rotateBitmap(bitmap, path);
                        //保存当前Bitmap
                        setCacleBiatmap(key, bitmap);
                        galidMap.put(key, false);
                        galidMap.remove(key);
                        if (callBack != null) {
                            callBack.onResult(bitmap);
                        }
                    } catch (Exception e) {
                        Log.e("test", "File流异常" + e.getMessage());
                    }
                }
            });
        }

    }

    /**
     * 充Assets目录下获取位图
     *
     * @param path      位图路径，如("文件夹/文件名.后缀")
     * @param isRGB_565
     * @return
     */
    public Bitmap getBitmapFromAssets(String path, boolean isRGB_565) {
        return getBitmapFromAssets(path, 0, isRGB_565);
    }

    public Bitmap getBitmapFromAssets(String path) {
        return getBitmapFromAssets(path, 0, false);
    }

    public Bitmap getBitmapFromAssets(String path, int resID, boolean isRGB_565) {
        return getBitmapFromAssets(path, resID, isRGB_565, 1);
    }

    public Bitmap getBitmapFromAssets(String path, boolean isRGB_565, int width, int height) {
        return getBitmapFromAssets(path, 0, isRGB_565, getInSampleSizeFromAssets(width, height, path));
    }

    //设置位图，如("文件夹/文件名.后缀"),如果在assets文件夹共目录下直接写文件名即可。assets支持中文文件夹，如:"中文/nicks2.png"
    //从asstes里面加载图片和从mipmap-nodpi里面加载图片占用内存是一样的。主要还是要看Bitmap的优化。布局文件尽量不要直接引用mipmap-nodpi里的图片。没有对内存进行优化，很占内存。
    //以下方式是最省内存的加载Bitmap方法。
    //isRGB_565 true 节省内存(推荐)，false不节省内存(效果较好)
    public Bitmap getBitmapFromAssets(String path, int resID, boolean isRGB_565, int inSampleSize) {
        String key;
        if (path != null && !path.equals("")) {
            key = getKeyForPath(path = path, isRGB_565 = isRGB_565, inSampleSize = inSampleSize);
        } else {
            key = getKeyForRes(resID = resID, isRGB_565 = isRGB_565, inSampleSize = inSampleSize);
        }
        //Log.e("test","key：\t"+key);
        Bitmap bitmap = getCacleBitmap(key);//获取缓存位图
        if (bitmap != null) {
            return bitmap;//防止重复加载，浪费内存
        }
        try {
//            Log.e("ui", "开始新建");
            if (path != null && !path.trim().equals("")) {
                InputStream is = am.open(path);
                if (isRGB_565) {
                    bitmap = BitmapFactory.decodeStream(is, null, getOptionsRGB_565(inSampleSize));
                } else {
                    bitmap = BitmapFactory.decodeStream(is, null, getOptionsARGB_8888(inSampleSize));
                }
                //byte[] b = UtilConnBitimap.InputStreamTOByte(is);
                //bitmap = BitmapFactory.decodeByteArray(b, 0, b.length, getOptions());//使用字节比使用流更省内存。
                //b = null;
                is.close();
                is = null;
            } else {
                if (isRGB_565) {
                    bitmap = BitmapFactory.decodeResource(KBaseApplication.getInstance().getResources(), resID, getOptionsRGB_565(inSampleSize));
                } else {
                    bitmap = BitmapFactory.decodeResource(KBaseApplication.getInstance().getResources(), resID, getOptionsARGB_8888(inSampleSize));
                }

            }
//            view.setBackgroundDrawable(bitmapDrawable);//设置背景图片，背景图片会拉升和控件同等大小。即这个方法，背景图片始终和控件同等大小。所以只要对控件进行适配即可。图片保持原图。
//            bitmap=UtilProportion.getInstance(activity).adapterBitmap(bitmap);//对图片进行统一适配。因为View图片时是放在背景里，背景里的图片不需要做适配。
//            Log.e("ui", "大小:\t" + bitmap.getByteCount() / 1024 + "KB" + "\t宽度:\t" + bitmap.getWidth() + "\t高度:\t" + bitmap.getHeight() + "\tconfig:\t" + bitmap.getConfig());
            //保存当前Bitmap
            setCacleBiatmap(key, bitmap);
        } catch (Exception e) {
            Log.e("ui", "assets流异常" + e.getMessage());
        }
        return bitmap;
    }

    /**
     * 这样获取的九文件（9文件；文件名.9.png），基本不占内存
     * <p>
     * 设置九文件图片：view.setBackground(ninePatchDrawable);经查阅后才知道.9只针对background来进行拉伸。不管是九文件还是其他图片。background都是对图片拉伸到和控件同等大小。
     * fixme 9文件必须设置黑点（黑边）；不然编译会报错的。
     *
     * @param resID drawable-nodpi文件夹下的九文件ID
     * @return NinePatchDrawable九文件，无则返回null
     */
    public NinePatchDrawable getNinePatchDrawable(int resID, boolean isRGB_565) {
        //fixme 九文件必须放在drawable-nodpi,mipmap-nodpi等系统文件夹下才有效。放在assets里是没有伸拉效果的，切记！
        Bitmap bitmap = getBitmapFromAssets(null, resID, isRGB_565);
        //确认Bitmap是合法的NinePatch文件
        if (NinePatch.isNinePatchChunk(bitmap.getNinePatchChunk())) {
            //Log.e("test","我是合法九文件");
            NinePatchDrawable ninePatchDrawable = new NinePatchDrawable(KBaseApplication.getInstance().getResources(), bitmap, bitmap.getNinePatchChunk(), new Rect(), null);
            return ninePatchDrawable;
        } else {
            bitmap.recycle();
            bitmap = null;
            System.gc();
            return null;
        }
    }

    /**
     * 设置背景图片(控件选中样式，自己手动调用该方法。选中监听事件没有)
     *
     * @param view     控件
     * @param fileName assets文件夹下背景图片名称
     * @param resID    如果fileName为null，才有效。
     */
    public void setBackGraound(final View view, final String fileName, final int resID, boolean isRGB_565) {
        setBackGraound(view, fileName, resID, view.getLayoutParams().width, view.getLayoutParams().height, false, isRGB_565);
    }

    /**
     * @param isRepeatAdapter 适配强制重新适配，true每次都重新适配。false只适配一次【默认就是false】
     */
    public void setBackGraound(final View view, final String fileName, final int resID, boolean isRepeatAdapter, boolean isRGB_565) {
        setBackGraound(view, fileName, resID, view.getLayoutParams().width, view.getLayoutParams().height, isRepeatAdapter, isRGB_565);
    }

    public void setBackGraound(final View view, final String fileName, final int resID, int width, int heigh, boolean isRepeatAdapter, boolean isRGB_565) {
        setBackGraound(view, fileName, resID, width, heigh, isRepeatAdapter, isRGB_565, true);//最后一个参数。默认都做适配
    }

    //最后一个参数 isadapter 是否做适配，true做适配。false不做适配
    public void setBackGraound(final View view, final String fileName, final int resID, int width, int heigh, boolean isRepeatAdapter, boolean isRGB_565, boolean isadapter) {
        if (view == null) {
            return;
        }
        //不要再线程中加载，会延迟。
        Bitmap bitmap = getBitmapFromAssets(fileName, resID, isRGB_565);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
        view.setBackgroundDrawable(bitmapDrawable);//设置背景图片，背景图片会拉伸到控件同等大小。即这个方法，背景图片始终和控件同等大小。所以只要对控件进行适配即可。图片保持原图。

        if (!isadapter) {
            return;//不做适配。
        } else {
            //做适配
            //isRepeatAdapter true没次都重新适配，以图片尺寸为标准。不能以自身。否则尺寸会出错。
            if (width <= 0 || isRepeatAdapter) {
                width = bitmap.getWidth();
            }
            if (heigh <= 0 || isRepeatAdapter) {
                heigh = bitmap.getHeight();
            }
            adapterView(bitmap, view, width, heigh, isRepeatAdapter);
        }
        bitmapDrawable = null;
    }

    //针对ImageView的Src
    public void setImageBitmap(final Class clazz, final ImageView view, final String fileName, final int resID, int width, int heigh, boolean isRGB_565) {
        if (view == null) {
            return;
        }
        //不要再线程中加载，会延迟。
        Bitmap bitmap = getBitmapFromAssets(fileName, resID, isRGB_565);
        view.setImageBitmap(bitmap);

        if (width <= 0) {
            width = bitmap.getWidth();
        }
        if (heigh <= 0) {
            heigh = bitmap.getHeight();
        }

        adapterView(bitmap, view, width, heigh, false);
    }

    //UI适配
    private void adapterView(Bitmap bitmap, final View view, int width, int heigh, boolean isRepeatAdapter) {

        if (!isRepeatAdapter && mapViews.containsKey(view.hashCode() + "") && mapViews.get(view.hashCode() + "").equals(view)) {
//            Log.e("test", "重复适配:\t" + view.hashCode());
            return;//重复适配。防止控件重复适配，浪费时间
        }

        mapViews.put(view.hashCode() + "", view);
//        Log.e("test", "适配:\t" + view.hashCode());
        //控件大小与图片大小一致
        ViewGroup.LayoutParams laParams = view
                .getLayoutParams();
        if (width <= 0) {
            laParams.width = bitmap.getWidth();
        } else {
            laParams.width = width;//自定义宽度
        }
        if (heigh <= 0) {
            laParams.height = bitmap.getHeight();//自定义高度
        } else {
            laParams.height = heigh;
        }
        view.setLayoutParams(laParams);
        //屏幕适配(调用UtilAssets就不需要再调用UtilProportion),只要对文字没有适配，基本都可以使用adapterView()
        if (view instanceof TextView) {
            KProportionUtils.getInstance().adapterTextView((TextView) view);//button也能转化成textView。textView是button和eidtText的父类。子类是可以转化成父类的。
        } else if (view instanceof GridView) {
            KProportionUtils.getInstance().adapterGridview((GridView) view);
        } else {
            KProportionUtils.getInstance().adapterView(view);
        }
        bitmap = null;
        System.gc();
    }

    private View.OnFocusChangeListener oldOnFocusChangeListener;//记录旧焦点的聚焦事件,防止旧焦点失灵。

    /**
     * 设置聚焦图片
     *
     * @param view      控件(所有的控件包括布局都继承View)
     * @param falseName 非聚焦图片名称(正常图片)
     * @param trueName  聚焦图片名称
     * @param falseID   非聚焦图片ID,falseName为null才有效
     * @param trueID    聚焦图片ID,trueName为null才有效
     * @param Bfaouse   是否聚焦
     */
    public void setOnFocusChanged(final View view, final String falseName, final String trueName, final int falseID, final int trueID, Boolean Bfaouse, final boolean isRGB_565) {
        view.setFocusable(true);//是否具备聚焦能力
        if (Bfaouse) {
            view.requestFocus();
            setBackGraound(view, trueName, trueID, isRGB_565);
        } else {
            setBackGraound(view, falseName, falseID, isRGB_565);//正常样式
        }
        //保存原有的聚焦事件,失去焦点时要恢复原有聚焦事件
        oldOnFocusChangeListener = view.getOnFocusChangeListener();
        //Log.e("ui", "聚焦变化初始化:\t" + view.getId());
        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //Log.e("ui", "聚焦:" + hasFocus);
                if (hasFocus) {
                    setBackGraound(view, trueName, trueID, isRGB_565);
                } else {
                    //Log.e("test","失去焦点：:\t"+falseID);
                    setBackGraound(view, falseName, falseID, isRGB_565);
                }
                //集成原有聚焦事件，防止原有聚焦事件失灵。
                if (oldOnFocusChangeListener != null) {
                    oldOnFocusChangeListener.onFocusChange(v, hasFocus);
                }
            }
        });
    }


    /**
     * 设置按下图片样式(手指按下不会聚焦，聚焦和手指是否按下没有直接影响)
     *
     * @param view            控件(所有的控件包括布局都继承View)
     * @param gennerName      手指离开图片样式(一般样式)
     * @param pressName       手机按下图片样式
     * @param gennerID        手指离开图片资源ID,gennerName为null有效
     * @param pressID         手指按下图片ID,pressName为null有效
     * @param isRGB_565
     * @param isRepeatAdapter true 每次都适配。false只适配一次。
     * @param isAdapter       是否做适配
     */
    public void setOnTouch(final View view, final String gennerName, final String pressName, final int gennerID, final int pressID, boolean isRGB_565, final boolean isRepeatAdapter, final boolean isAdapter) {
        setOnTouch(view, gennerName, pressName, gennerID, pressID, null, isRGB_565, isRepeatAdapter, isAdapter);
    }

    //兼容原有触摸事件【放心，和点击事件不会冲突】
    public void setOnTouch(final View view, final String gennerName, final String pressName, final int gennerID, final int pressID, final View.OnTouchListener onTouchListener, final boolean isRGB_565, final boolean isRepeatAdapter, final boolean isAdapter) {
        //setBackGraound(clazz, view, gennerName, gennerID, isRGB_565);//正常样式
        setBackGraound(view, gennerName, gennerID, view.getLayoutParams().width, view.getLayoutParams().height, isRepeatAdapter, isRGB_565, isAdapter);
        view.setClickable(true);//是否具备点击能力,必须设置否则无效
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Log.e("ui","事件:"+event.getAction());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN://按下
//                        Log.e("ui", "按下");
                        //setBackGraound(clazz, view, pressName, pressID, isRGB_565);
                        setBackGraound(view, pressName, pressID, view.getLayoutParams().width, view.getLayoutParams().height, isRepeatAdapter, isRGB_565, isAdapter);
                        break;
                    case MotionEvent.ACTION_UP://离开
//                        Log.e("ui", "离开");
                        //setBackGraound(clazz, view, gennerName, gennerID, isRGB_565);
                        setBackGraound(view, gennerName, gennerID, view.getLayoutParams().width, view.getLayoutParams().height, isRepeatAdapter, isRGB_565, isAdapter);
                        break;
                    default:
                        break;
                }
                if (onTouchListener != null) {//防止原有触摸事件无效。兼容原有触摸事件。
                    return onTouchListener.onTouch(v, event);
                }
                return false;
            }
        });
    }

    /**
     * 获取帧动画
     *
     * @param clazz    getClass()
     * @param view     控件
     * @param fileName 文件名。不包括数字标志和后缀。如"star_0.png",传 "star_"
     * @param size     帧动画个数。下标从0开始。size就是最后一个数。如"0,1,2,3",就传3
     * @param duration 帧动画时间，单位毫秒。1000等于一秒。
     * @param BAssets  true图片在asstes文件夹。false图片在mipmap文件夹。
     * @return
     */
    public AnimationDrawable getBackGraoundAAnimationDrawable(final Class clazz, final View view, String fileName, int size, int duration, Boolean BAssets, boolean isRGB_565) {
        AnimationDrawable anim = new AnimationDrawable();
        for (int i = 0; i <= size; i++) {
//            Log.e("ui", " " + (fileName + i));
            Bitmap bitmap = null;
            if (BAssets) {
                fileName = fileName + i + ".png";
                bitmap = getBitmapFromAssets(fileName, 0, isRGB_565);//资源在asstes文件下
            } else {
                int id = KBaseApplication.getInstance().getResources().getIdentifier(fileName + i, "mipmap", KBaseApplication.getInstance().getPackageName());//图片资源在mipmap下面
                bitmap = getBitmapFromAssets(null, id, isRGB_565);
            }

            Drawable drawable = new BitmapDrawable(bitmap);
            anim.addFrame(drawable, duration);
        }
        //anim.setOneShot(false);//是否只循环一次
        //view.setBackground(anim);
        view.setBackgroundDrawable(anim);
        return (AnimationDrawable) view.getBackground();
    }

    //获取assets下文本，参数("文件夹/文件名.后缀")
    public String getStringFromAssets(String fileName) {
        //将json数据变成字符串
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //通过管理器打开文件并读取
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    am.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
            bf.close();
            bf = null;
        } catch (IOException e) {
            Log.e("test", "asset json");
        }
        return stringBuilder.toString();
    }

    /**
     * 销毁所有位图,释放内存
     */
    public void recycleAll() {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            if (entry.getValue() == null) {
                continue;
            }
            Bitmap bitmap = (Bitmap) entry.getValue();
            if (bitmap != null && !bitmap.isRecycled()) {
                // 回收并且置为null
                bitmap.recycle();
                bitmap = null;
            }
        }
        map.clear();
        System.gc();//回收无用的对象,释放内存
    }
}

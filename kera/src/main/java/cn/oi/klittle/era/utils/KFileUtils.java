package cn.oi.klittle.era.utils;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by 彭治铭 on 2018/1/21.
 */

public class KFileUtils {
    private static KFileUtils fileUtils;

    public static KFileUtils getInstance() {
        if (fileUtils == null) {
            fileUtils = new KFileUtils();
        }
        return fileUtils;
    }

    private KFileUtils() {
    }

    /**
     * 根据路径获取文件
     *
     * @param filePath
     * @return
     */
    public File getFile(String filePath) {
        if (filePath == null) {
            return null;
        }
        return new File(filePath);
    }

    /**
     * 获取文件大小;单位是字节B
     *
     * @param filePath
     * @return
     */
    public Long getFileSize(String filePath) {
        if (filePath == null) {
            return 0L;
        }
        return new File(filePath).length();
    }

    /**
     * 获取文件流
     *
     * @param filePath
     * @return
     */
    public FileInputStream getFileInputStream(String filePath) {
        if (filePath == null) {
            return null;
        }
        File file = new File(filePath);
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据文件路径，获取文件名（包括文件后缀）
     *
     * @param path
     * @return
     */
    public String getFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    /**
     * 获取文件名（包括文件名后缀）
     *
     * @param file
     * @return
     */
    public String getFileName(File file) {
        return file.getName();//包括后缀
    }

    /**
     * 创建文件
     *
     * @param path     路径【目录，不包含文件名】。
     * @param fileName 文件名。包含文件名后缀。
     * @return
     */
    public File createFile(String path, String fileName) {
        File fileParent = new File(path);
        if (fileParent.exists() == false) {
            fileParent.mkdirs();//判断该目录是否存在，不存在，就创建目录。
        }
        File file = new File(path + "/" + fileName);
        if (file.exists() == false) {
            try {
                file.createNewFile();//创建文件
            } catch (IOException e) {
                Log.e("test", "文件创建失败:\t" + e.getMessage());
            }
        }
        return file;
    }

    /**
     * 读取某个文件夹下的所有文件【包括子文件夹】
     *
     * @param filepath 文件夹路径
     */
    public List<File> readfiles(String filepath) {
        return readfiles(filepath, null);
    }

    /**
     * 读取某个文件夹下的所有文件【包括子文件夹】
     *
     * @param filepath 文件夹路径
     * @param files    文件集合，可以为null.为null时，会自行创建集合。
     */
    public List<File> readfiles(String filepath, List<File> files) {
        if (files == null) {
            files = new ArrayList<File>();
        }
        try {
            File file = new File(filepath);
            if (!file.isDirectory()) {
                files.add(file);
            } else if (file.isDirectory()) {
                String[] filelist = file.list();
                for (int i = 0; i < filelist.length; i++) {
                    File readfile = new File(filepath + "/" + filelist[i]);//不要使用双反斜杠"\\"【可能不识别】,最好使用斜杠"/"
                    if (!readfile.isDirectory()) {
                        files.add(readfile);
                        //Log.e("test","文件名:\t"+readfile.getName()+"\t路径：\t"+readfile.getPath()+"\t是否为文件"+readfile.isFile()+"\t大小:\t"+readfile.length()+"\tfilepath:\t"+filepath);
                    } else if (readfile.isDirectory()) {
                        readfiles(filepath + "\\" + filelist[i], files);//递归，遍历文件夹下的子文件夹。
                    }
                }
            }
        } catch (Exception e) {
            Log.e("获取所有文件失败", "原因" + e.getMessage());
        }
        return files;
    }

    public File[] orderByDate(String filePath) {
        File file = new File(filePath);
        File[] files = file.listFiles();
        return orderByDate(files);
    }

    /**
     * 按 文件日期 排序（日期越大越在前，即最新的时间显示在最前面。）
     *
     * @param files
     * @return
     */
    public File[] orderByDate(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                long diff = f1.lastModified() - f2.lastModified();
                if (diff < 0)//时间越大越在前；if (diff > 0) 时间越大越在后。（系统读取文件夹默认就是时间越大越在后。）
                    return 1;
                else if (diff == 0)
                    return 0;
                else
                    return -1;//如果 if 中修改为 返回-1 同时此处修改为返回 1  排序就会是递减
            }

            public boolean equals(Object obj) {
                return true;
            }

        });
        return files;
//        for (int i = 0; i < files.length; i++) {
//            System.out.println(files[i].getName());
//            System.out.println(new Date(files[i].lastModified()));
//        }
    }

    /**
     * 删除文件
     *
     * @param path 文件完整路径，包括文件后缀名 （以path为主，当path为null时，dir和name才有效）
     * @param dir  文件目录
     * @param name 文件名（包括后缀）
     */
    public void delFile(String path, String dir, String name) {
        try {
            if (path == null) {
                File file = new File(dir, name);
                if (file.exists()) {
                    file.delete();
                }
            } else {
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            Log.e("文件删除异常", "异常信息" + e.getMessage());
        }

    }

    /**
     * 删除某个文件夹下的所有文件夹和文件
     *
     * @param delpath 文件夹路径
     */
    public boolean delAllFiles(String delpath) {
        try {
            File file = new File(delpath);
            if (!file.isDirectory()) {
                file.delete();
            } else if (file.isDirectory()) {
                String[] filelist = file.list();
                for (int i = 0; i < filelist.length; i++) {
                    File delfile = new File(delpath + "/" + filelist[i]);//不要使用双反斜杠"\\"【可能不识别】,最好使用斜杠"/"
                    //Log.e("test","文件路径:\t"+delfile.getPath()+"\t名称:\t"+delfile.getName()+"\t"+filelist[i]);
                    if (!delfile.isDirectory()) {
                        //Log.e("test","删除:\t"+delfile.getName());
                        delfile.delete();
                    } else if (delfile.isDirectory()) {
                        delAllFiles(delpath + "/" + filelist[i]);
                    }
                }
                file.delete();
            }
        } catch (Exception e) {
            Log.e("test", "删除所有文件异常:\t" + e.getMessage());
        }
        return true;
    }

    /**
     * 復制文件
     *
     * @param target  复制对象
     * @param dirPath 要复制到的文件目录。（只要目录。不需要文件名。会自动获取原文件名）
     * @return
     */
    public File copyFile(File target, String dirPath) {
        //file.getName()获取文件名，是包含后缀的。
        return copyFile(target, dirPath, target.getName());
    }

    /**
     * 復制文件
     *
     * @param target 复制对象
     * @param path   路径
     * @param name   文件名(包括后缀) target.getName();
     * @return
     */
    public File copyFile(File target, String path, String name) {
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdirs();//创建目录。
        }
        dirFile = null;
        File destFile = new File(path + "/" + name);////不要使用双反斜杠"\\"【可能不识别】,最好使用斜杠"/"
        try {
            if (destFile.exists()) {
                if (target.length() == destFile.length() && target.getName().equals(destFile.getName())) {
                    return destFile;//完全一样，就返回
                } else {
                    destFile.delete();//不一样，就删除
                }
            }
            destFile.createNewFile();//创建文件
            //先进行输入才能进行输出，代码书序不能变
            InputStream in = new FileInputStream(target);
            OutputStream out = new FileOutputStream(destFile);
            byte[] bytes = new byte[1024];
            int len = -1;
            while ((len = in.read(bytes)) != -1) {
                out.write(bytes, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("test", "文件复制异常:\t" + e.getMessage());
        }
        return destFile;
    }

    /**
     * 流转换成文件
     *
     * @param inputStream 流
     * @param path        路径
     * @param fileName    文件名
     * @return
     */
    public File inputSteamToFile(InputStream inputStream, String path, String fileName) {
        File destFile = new File(path + "/" + fileName);////不要使用双反斜杠"\\"【可能不识别】,最好使用斜杠"/"
        try {
            //先进行输入才能进行输出，代码书序不能变
            OutputStream out = new FileOutputStream(destFile);
            byte[] bytes = new byte[1024];
            int len = -1;
            while ((len = inputStream.read(bytes)) != -1) {
                out.write(bytes, 0, len);
            }
            out.close();
            inputStream.close();
        } catch (Exception e) {
            Log.e("test", "流转换文件异常:\t" + e.getMessage());
        }
        return destFile;
    }

    /**
     * 流转换成字符串
     *
     * @param input 流
     * @return
     */
    public String inputSteamToString(InputStream input) {
        try {
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
     *
     * @param bitmap
     * @param path    路径 如：context.getApplicationContext().getFilesDir().getAbsolutePath();
     * @param picName 图片名称，记得要有.png的后缀。【一定要加.png的后缀】
     * @return 返回保存文件
     */
    public File saveBitmap(Bitmap bitmap, String path, String picName) {
        File file = new File(path, picName);
        FileOutputStream out = null;
        if (file.exists()) {
            file.delete();//如果已经存在，则删除旧的。
        }
        try {
            file.createNewFile();
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
        } catch (Exception e) {
            Log.e("test", "Bitmap位图保存异常:\t" + e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.e("test", "Bitmap位图保存异常2:\t" + e.getMessage());
            }
        }
        return file;
    }

    /**
     * @param bitmap
     * @param path   完整路径，包括后缀名
     * @return
     */
    public File saveBitmap(Bitmap bitmap, String path) {
        File file = new File(path);
        FileOutputStream out = null;
        if (file.exists()) {
            file.delete();//如果已经存在，则删除旧的。
        }
        try {
            file.createNewFile();
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
        } catch (Exception e) {
            Log.e("test", "Bitmap位图保存异常:\t" + e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.e("test", "Bitmap位图保存异常2:\t" + e.getMessage());
            }
        }
        return file;
    }

    /**
     * 位图转换成流
     *
     * @param bitmap
     * @return
     */
    public InputStream bitmapToInputStream(Bitmap bitmap) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
            return inputStream;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 位图转Base64
     *
     * @param bitmap
     * @return
     */
    public String bitmapToBase64(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            InputStream inputStream = bitmapToInputStream(bitmap);
            return inputStreamToBase64(inputStream);
        }
        return "";
    }

    /**
     * 获取系统相册路径
     *
     * @return
     */
    public String getDCIMPath() {
        String path = null;
        if (Build.BRAND.equals("Xiaomi") || Build.BRAND.trim().toLowerCase().equals("xiaomi") || Build.BRAND.trim().toUpperCase().equals("HUAWEI")) { // 小米手机,华为手机
            path = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/";
        } else {  // Meizu 、Oppo
            path = Environment.getExternalStorageDirectory().getPath() + "/DCIM/";
        }
        return path;
    }

    /**
     * 保存文件到系统相册[需要SD权限哦。]
     *
     * @param target
     * @return
     */
    public File saveFileToDCIM(File target) {
        //Log.e("test","路径:\t"+path+"\t"+Build.BRAND);
        String path = getDCIMPath();
        if (path != null) {
            return copyFile(target, path, "IMG_" + target.getName());
        }
        return null;
    }

    /**
     * 保存位图到系统相册[需要SD权限哦。],保存到手机之后，有时不能马上显示出来。手机必须重启后(获取畅享6s)。才显示。小米则不用。能够马上显示出来。
     *
     * @param bitmap
     * @param picName 图片名称，记得要有.jpg的后缀。[相册里的一般都是这个格式。PNG格式的不能显示在相册中]
     * @return
     */
    public File saveBitmapToDCIM(Bitmap bitmap, String picName) {
        //Log.e("test","路径:\t"+path+"\t"+Build.BRAND);
        String path = getDCIMPath();
        if (path != null) {
            return saveBitmap(bitmap, path, "IMG_" + picName);//手机一般都是有个IMG_这个格式。
        }
        return null;
    }

    /**
     * 文件转换成字符串
     *
     * @param file
     * @return
     */
    public String fileToString(File file) {
        try {
            InputStream inputStream = new FileInputStream(file);
            inputSteamToString(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 文件转换成流
     *
     * @param file
     * @return
     */
    public InputStream fileToInputStream(File file) {
        try {
            InputStream inputStream = new FileInputStream(file);
            return inputStream;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 这里Base64是安卓原生。但是不管是安卓原生还是第三方的。Base64都是一样的。
     * <p>
     * 之所以要使用64加密字符串，是因为64解码出来的字节与原有文件字节大小一模一样，不会发生任何改变。
     * <p>
     * String与byte直接转换。太危险。由于特殊符号。比如空格等。数据肯定会丢失(这样file肯定就无法正确转换成bitmap了)。
     * 所以一般的做法就是是弄成比如Base64这样的
     * <p>
     * 流转base64字符串
     *
     * @param inputStream 流
     * @return 返回64加密的字符串。Base64.decode(base64, Base64.DEFAULT);// 将字符串转换为byte数组
     */
    public String inputStreamToBase64(InputStream inputStream) {
        String base64 = null;
        //InputStream in = null;
        try {
            //in = new FileInputStream(file);
            byte[] bytes = new byte[inputStream.available()];
            int length = inputStream.read(bytes);
            base64 = Base64.encodeToString(bytes, 0, length, Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return base64;
    }

    /**
     * 文件转Base64
     *
     * @param file
     * @return
     */
    public String fileToBase64(File file) {
        return inputStreamToBase64(fileToInputStream(file));
    }

    /**
     * base64字符串转文件
     *
     * @param base64 加密字符
     * @param file   文件(file.toString()与file.getAbsolutePath()一样都是返回绝对路径(包括后缀名)，file.getName()文件名(包括后缀名))
     * @return
     */
    public File base64ToFile(String base64, File file) {
        FileOutputStream out = null;
        try {
            // 解码，然后将字节转换为文件
            if (!file.exists())
                file.createNewFile();
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);// 将字符串转换为byte数组
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            byte[] buffer = new byte[1024];
            out = new FileOutputStream(file);
            int bytesum = 0;
            int byteread = 0;
            while ((byteread = in.read(buffer)) != -1) {
                bytesum += byteread;
                out.write(buffer, 0, byteread); // 文件写操作
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return file;
    }

    //获得某目录下文件总大小
    public static double getDirSize(File file) {
        //判断文件是否存在
        if (file.exists()) {
            //如果是目录则递归计算其内容的总大小
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                double size = 0;
                for (File f : children)
                    size += getDirSize(f);
                return size;
            } else {//如果是文件则直接返回其大小,以“B”为单位
                double size = (double) file.length();
                return size;
            }
        } else {
            //System.out.println("文件或者文件夹不存在，请检查路径是否正确！");
            return 0.0;
        }
    }

}

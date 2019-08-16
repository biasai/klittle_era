package cn.oi.klittle.era.mian;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.oi.klittle.era.utils.KStringUtils;

/**
 * fixme 生成屏幕适配；亲测可以执行main()方法。
 * fixme KStringUtils.INSTANCE.doubleString 直接调用没有问题。
 * 直接右键该文件，执行main方法即可
 * Created by 彭治铭 on 2018/3/23.
 */

public class KDimens {
    /**
     * 生成文件路径
     */
    static String FilePath = "D:/Test/res/";

    //文件名
    static String fileNameX = "pixelX.xml";
    static String fileNameY = "pixelY.xml";
    //dimen標簽名
//    static String dimenNameX = "x";
//    static String dimenNameY = "y";
    static String dimenNameX = "x";
    static String dimenNameY = "y";
    static double width = 750;//标准宽度
    static double height = 1334;//标准高度

    static class Dimen {
        public double height;
        public double widht;

        public Dimen(double height, double widht) {
            this.height = height;
            this.widht = widht;
        }
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        List<Dimen> dimens = new ArrayList<>();
//        dimens.add(new Dimen(320, 240));
//        dimens.add(new Dimen(400, 240));
//        dimens.add(new Dimen(400, 320));
//        dimens.add(new Dimen(480, 320));
//        dimens.add(new Dimen(640, 640));
//        dimens.add(new Dimen(640, 460));//早起的安卓机子在用，现在已经基本没有了
//        dimens.add(new Dimen(640, 480));
//        dimens.add(new Dimen(728, 480));
//        dimens.add(new Dimen(800, 480));
//        dimens.add(new Dimen(854, 480));
//        dimens.add(new Dimen(864, 480));
//        dimens.add(new Dimen(960, 540));//HTC的高端机很喜欢用
//        dimens.add(new Dimen(960, 640));
//        dimens.add(new Dimen(960, 720));
//        去掉以上基本淘汰的设备。减少体积

        dimens.add(new Dimen(1024, 600));
        dimens.add(new Dimen(1024, 768));
        dimens.add(new Dimen(1080, 920));
        dimens.add(new Dimen(1136, 640));
        dimens.add(new Dimen(1184, 720));
        dimens.add(new Dimen(1184, 750));
        dimens.add(new Dimen(1184, 768));
        dimens.add(new Dimen(1196, 720));
        dimens.add(new Dimen(1208, 720));//华为畅享6
        dimens.add(new Dimen(1280, 672));
        dimens.add(new Dimen(1280, 720));
        dimens.add(new Dimen(1280, 768));
        dimens.add(new Dimen(1280, 800));
        dimens.add(new Dimen(1280, 1024));
        dimens.add(new Dimen(1334, 750));//这个是苹果的尺寸
        dimens.add(new Dimen(1336, 768));
        dimens.add(new Dimen(1440, 720));//红米S2
        dimens.add(new Dimen(1440, 900));
        dimens.add(new Dimen(1440, 1080));
        dimens.add(new Dimen(1600, 900));
        dimens.add(new Dimen(1680, 1050));
        dimens.add(new Dimen(1776, 1080));
        dimens.add(new Dimen(1795, 1080));
        dimens.add(new Dimen(1800, 1080));//魅族MX3
        dimens.add(new Dimen(1808, 1080));//华为 mate9
        dimens.add(new Dimen(1812, 1080));//荣耀 8
        dimens.add(new Dimen(1920, 1080));
        dimens.add(new Dimen(1920, 1200));
        dimens.add(new Dimen(1952, 1536));
        dimens.add(new Dimen(1980, 1080));//红米5plus版本
        dimens.add(new Dimen(2001, 1125));//iPhone6\7\8 plus放大版
        dimens.add(new Dimen(2048, 1080));
        dimens.add(new Dimen(2030, 1080));//红米5Plus又一个分辨率
        dimens.add(new Dimen(2040, 1080));
        dimens.add(new Dimen(2048, 1536));
        dimens.add(new Dimen(2160, 1080));//18:9 手机主流分辨率。一般比例都是 16:9
        dimens.add(new Dimen(2208, 1242));// iPhone6\7\8 plus
        dimens.add(new Dimen(2224, 1668));//iPad Pro 10.5
        dimens.add(new Dimen(2240, 1080));
        dimens.add(new Dimen(2244, 1080));//华为P20
        dimens.add(new Dimen(2268, 1080));//高/宽 =2.1 的比例
        dimens.add(new Dimen(2280, 1080));//一加6 vivo X21 即 19:9 ，比例2.111111
        dimens.add(new Dimen(2392, 1440));
        dimens.add(new Dimen(2436, 1125));//iPhone X(异形屏幕)
        dimens.add(new Dimen(2560, 1440));
        dimens.add(new Dimen(2560, 1536));//魅族MX4 Pro
//        dimens.add(new Dimen(2560, 1600));
//        dimens.add(new Dimen(2560, 1800));
//        dimens.add(new Dimen(2732, 2048));//iPad Pro 12.9
//        dimens.add(new Dimen(2880, 1440));
//        dimens.add(new Dimen(2960, 1440));
//        dimens.add(new Dimen(3040, 1440));//夏普Aquos R2
//        dimens.add(new Dimen(3840, 2160));//sony Xperia xz premium
        for (int i = 0; i < dimens.size(); i++) {
            Dimen dimen = dimens.get(i);
            //生成X文件
            String allPx = getAllPx(dimen.widht, false);
            writeFile(FilePath, (int) dimen.height, (int) dimen.widht, false, allPx);

            //生成Y文件
            allPx = getAllPx(dimen.height, true);
            writeFile(FilePath, (int) dimen.height, (int) dimen.widht, true, allPx);
        }
        System.out.println("设备个数:\t" + dimens.size());//目前共有67个设备，打包了46个。基本包括2018所有主流屏幕
    }

    /**
     * 創建dimens文件裏的内容
     *
     * @param max 寬或高的最大值
     * @param isY true 以高為標準，false以寬度為標準
     * @return
     */
    public static String getAllPx(double max, boolean isY) {
        StringBuilder sb = new StringBuilder();
        double max2 = max;
        String name = dimenNameX;
        double p = max2 / width;//比率
        max = width;
        if (isY) {
            name = dimenNameY;
            p = max2 / height;
            max = height;
        }
        /**
         * 注意，虽然适配的比较多，生成的文件大概3M左右，但是生成APK包。签名打包时会再压缩的。对apk的体积是没有多大影响的。实际也就几百KB而已。
         */
        try {
            //sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"+ "\r\n");這個最好不要，現在一般不需要了，加上了反而可能不識別了。
            sb.append("<resources>" + "\r\n");
            //像素【像素适配从1开始。0就不需要了】
            for (int i = 1; i <= max; i++) {
                System.out.println("i=" + i);
                sb.append("<dimen name=\"" + name + i + "\">" + KStringUtils.INSTANCE.doubleString(i * p, 3) + "px</dimen>"
                        + "\r\n");
            }
            //负数(X里面取3分之一即可，针对线性布局支持负数的外补丁。用的很少。以下这个是以备不时之需)
            //Y也取3分之一负数。保证比例（3分之一才够）
//            if (!isY) {
            for (int i = 1; i <= max / 3; i++) {
                System.out.println("i=" + i);
                sb.append("<dimen name=\"_" + name + i + "\">" + KStringUtils.INSTANCE.doubleString(i * p * -1, 3) + "px</dimen>"
                        + "\r\n");
            }
//            }
//            //百分比（去掉百分比，减少体积）
//            for (int i = 1; i <= 100; i++) {
//                sb.append("<dimen name=\"" + name + "100_" + i + "\">" + StringUtils.getInstance().double2String(max2 / 100f * i,3) + "px</dimen>" + "\r\n");
//            }
            sb.append("<dimen name=\"" + name + "100_" + 25 + "\">" + KStringUtils.INSTANCE.doubleString(max2 / 100f * 25, 3) + "px</dimen>" + "\r\n");//四分之一
            sb.append("<dimen name=\"" + name + "100_" + 50 + "\">" + KStringUtils.INSTANCE.doubleString(max2 / 100f * 50, 3) + "px</dimen>" + "\r\n");//二分之一
            sb.append("<dimen name=\"" + name + "100_" + 100 + "\">" + KStringUtils.INSTANCE.doubleString(max2 / 100f * 100, 3) + "px</dimen>" + "\r\n");//百分百
            sb.append("</resources>" + "\r\n");
            System.out.println(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 存为新文件
     *
     * @param filepath 路徑
     * @param height   寬度
     * @param width    寬度
     * @param isY      true 以高度為標準，false以寬度為標準
     * @param st       文本信息
     */
    public static void writeFile(String filepath, int height, int width, boolean isY, String st) {
        try {
            String fileName = fileNameX;
            if (isY) {
                fileName = fileNameY;
            }
            String dir = filepath + "values-" + height + "x" + width + "/";
            deleteFile(dir + fileName);
            File file = new File(dir);
            file.mkdirs();
            file = new File(dir + fileName);
            file.createNewFile();
            FileWriter fw = new FileWriter(dir + fileName);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(st);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除单个文件
     *
     * @param sPath 被删除文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }

}

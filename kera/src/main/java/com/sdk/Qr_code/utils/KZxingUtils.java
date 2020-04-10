package com.sdk.Qr_code.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import cn.oi.klittle.era.utils.KLoggerUtils;

/**
 * 二维码；条码生成工具
 * Created by 彭治铭 on 2019/4/13.
 */
public class KZxingUtils {

    /**
     * 创建二维码
     *
     * @param content   content 二维码内容
     * @param widthPix  widthPix 宽度
     * @param heightPix heightPix 高度
     * @param logoBm    logoBm 在二维码中间添加Logo图案（如果为空，就不绘制）
     * @return 二维码
     */
    public static Bitmap createQRCode(String content, int widthPix, int heightPix, Bitmap logoBm) {
        try {
            if (content == null || "".equals(content)) {
                return null;
            }
            // 配置参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            // 容错级别 这里选择最高H级别
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            // 图像数据转换，使用了矩阵转换 参数顺序分别为：编码内容，编码类型，生成图片宽度，生成图片高度，设置参数
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, widthPix,
                    heightPix, hints);
            int[] pixels = new int[widthPix * heightPix];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < heightPix; y++) {
                for (int x = 0; x < widthPix; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * widthPix + x] = 0xff000000; // 黑色
                    } else {
                        pixels[y * widthPix + x] = 0xffffffff;// 白色
                    }
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix);
            if (logoBm != null) {//绘制logo
                bitmap = addLogo(bitmap, logoBm);
            }
            //必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 在二维码中间添加Logo图案
     */
    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }
        if (logo == null) {
            return src;
        }
        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();
        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }
        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }
        //logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);
            //canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }
        return bitmap;
    }


    public static Bitmap createBarcode(String content, int widthPix, int heightPix) {
        return createBarcode(content, widthPix, heightPix, BarcodeFormat.CODE_128, false);
    }

    public static Bitmap createBarcode(String content, int widthPix, int heightPix, BarcodeFormat format) {
        return createBarcode(content, widthPix, heightPix, format, false);
    }


    /**
     * 绘制条形码
     *
     * @param content       要生成条形码包含的内容
     * @param widthPix      条形码的宽度
     * @param heightPix     条形码的高度
     * @param isShowContent 是否显示条形码包含的内容 fixme 显示效果已经做了微调修改，个人感觉还不错。
     * @param format        条码生成编码格式，如：BarcodeFormat.CODE_128 ；fixme CODE_128效果是最好的。目前好像只支持 CODE_128 和 CODE_39 其他的好像会报错（BarcodeFormat.CODE_93不支持）。 BarcodeFormat.QR_CODE 是二维码
     * @return 返回生成条形的位图
     */
    public static Bitmap createBarcode(String content, int widthPix, int heightPix, BarcodeFormat format, boolean isShowContent) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        //配置参数
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        // 容错级别 这里选择最高H级别
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        MultiFormatWriter writer = new MultiFormatWriter();

        try {
            // 图像数据转换，使用了矩阵转换 参数顺序分别为：编码内容，编码类型，生成图片宽度，生成图片高度，设置参数
            //BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.CODE_128, widthPix, heightPix, hints);
            BitMatrix bitMatrix = writer.encode(content, format, widthPix, heightPix, hints);
            int[] pixels = new int[widthPix * heightPix];
            //下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < heightPix; y++) {
                for (int x = 0; x < widthPix; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * widthPix + x] = 0xff000000; // 黑色
                    } else {
                        pixels[y * widthPix + x] = 0xffffffff;// 白色
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix);
            if (isShowContent) {
                bitmap = showContent(bitmap, content);
            }
            return bitmap;
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("生成条码异常：\t" + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 显示条形的内容
     *
     * @param bCBitmap 已生成的条形码的位图
     * @param content  条形码包含的内容
     * @return 返回生成的新位图, 返回的位图与新绘制文本content的组合
     */
    private static Bitmap showContent(Bitmap bCBitmap, String content) {
        if (TextUtils.isEmpty(content) || null == bCBitmap) {
            return null;
        }
        if (bCBitmap.isRecycled()||bCBitmap.getWidth()<=0){
            return null;
        }
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);//设置填充样式
        //paint.setTextSize(kpx.INSTANCE.x(38));
        int textSize=bCBitmap.getHeight()/5;//fixme 设置文本的大小
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        //paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗(不要加粗，感觉不好看)
        //测量字符串的宽度
        //int textWidth = (int) paint.measureText(content);
        Paint.FontMetrics fm = paint.getFontMetrics();
        //绘制字符串矩形区域的高度
        int textHeight = (int) (fm.bottom - fm.top);
        // x 轴的缩放比率（文本会拉升效果很不好）
        //float scaleRateX = bCBitmap.getWidth() / textWidth;
        //paint.setTextScaleX(scaleRateX);
        //绘制文本的基线
        int baseLine = bCBitmap.getHeight() + textHeight;
        int baseOffset= (int) (textHeight*0.3f);//微调一下高度
        //创建一个图层，然后在这个图层上绘制bCBitmap、content
        Bitmap bitmap = Bitmap.createBitmap(bCBitmap.getWidth(), bCBitmap.getHeight() + 2 * textHeight-baseOffset, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas();
        canvas.drawColor(Color.WHITE);
        canvas.setBitmap(bitmap);
        canvas.drawBitmap(bCBitmap, 0, 0, null);
        canvas.drawText(content, bCBitmap.getWidth() / 2, baseLine-baseOffset, paint);
        //canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.save();
        canvas.restore();
        return bitmap;
    }


    //fixme 扫描二维码图片

    public static int requestCode_Qr = 333;//二维码回调标志
    public static int resultCode_Qr = 334;

    public static final Map<DecodeHintType, Object> HINTS = new EnumMap<>(DecodeHintType.class);

    /**
     * 同步解析bitmap二维码。该方法是耗时操作，请在子线程中调用。
     *
     * @param bitmap    要解析的二维码图片
     * @param isRecycle 是否释放原位图，true释放，false不释放
     * @return 返回二维码图片里的内容 或 null
     */
    public static String syncDecodeQRCode(Bitmap bitmap, Boolean isRecycle) {
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }
        Result result = null;
        RGBLuminanceSource source = null;
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            source = new RGBLuminanceSource(width, height, pixels);
            result = new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(source)), HINTS);
            if (isRecycle && bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();//释放原有位图
                bitmap = null;
            }
            return result.getText();
        } catch (Exception e) {
            e.printStackTrace();
            if (source != null) {
                try {
                    result = new MultiFormatReader().decode(new BinaryBitmap(new GlobalHistogramBinarizer(source)), HINTS);
                    if (isRecycle && bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();//释放原有位图
                        bitmap = null;
                    }
                    return result.getText();
                } catch (Throwable e2) {
                    e2.printStackTrace();
                }
            }
            if (isRecycle && bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();//释放原有位图
                bitmap = null;
            }
            return null;
        }
    }

    public static String syncDecodeQRCode(Bitmap bitmap) {
        return syncDecodeQRCode(bitmap, true);
    }

    //fixme 解析图片二维码，建议使用该方法。亲测有效。效果杠杠的。
    public static String syncDecodeQRCode(String picturePath) {
        return syncDecodeQRCode(getDecodeAbleBitmap(picturePath), true);
    }

    /**
     * 将本地图片文件转换成可解码二维码的 Bitmap。为了避免图片太大，这里对图片进行了压缩
     *
     * @param picturePath 本地图片文件路径
     * @return
     */
    static Bitmap getDecodeAbleBitmap(String picturePath) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(picturePath, options);
            int sampleSize = options.outHeight / 800;//之前是400，400还是太小了，识别率很低。800测试识别可以。
            if (sampleSize <= 0) {
                sampleSize = 1;
            }
            options.inSampleSize = sampleSize;
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(picturePath, options);
        } catch (Exception e) {
            return null;
        }
    }

}
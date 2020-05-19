package cn.oi.klittle.era.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.widget.ImageView;

//                    fixme 高斯模糊图片调用案例（在KBaseDialog弹框里面有使用。）
//                    var currentActivityBitmap = KBaseApplication.getInstance().getCurrentActivityBitmap(ctx)//获取当前Activity的位图
//                    currentActivityBitmap = KBitmapUtils.blurBitmap(ctx, currentActivityBitmap,25f)//获取高斯模糊后的位图
//                    //currentActivityBitmap = KBitmapUtils.coverColorForBitmap(ctx, currentActivityBitmap, Color.parseColor("#888080FF"))//要蒙上的颜色；

/**
 * 位图工具类；主要实现高斯模糊(毛玻璃效果)；效果很不错；杠杆的。算法速度很快。
 * 高斯模糊在有图片的背景下效果才好看。在纯文本纯颜色的背景下效果不怎么好看。
 */
public class KBitmapUtils {
    /**
     * 图片缩放比例
     */
    private static final float BITMAP_SCALE = 0.2f;

    /**
     * 将Drawable对象转化为Bitmap对象
     *
     * @param drawable Drawable对象
     * @return 对应的Bitmap对象
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;
        try {
            //如果本身就是BitmapDrawable类型 直接转换即可
            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                if (bitmapDrawable.getBitmap() != null) {
                    return bitmapDrawable.getBitmap();
                }
            }
            //取得Drawable固有宽高
            if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
                //创建一个1x1像素的单位色图
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            } else {
                //直接设置一下宽高和ARGB
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            }
            //重新绘制Bitmap
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("KBitmapUtils->drawableToBitmap()异常：\t" + e.getMessage(),true);
        }
        return bitmap;
    }

    /**
     * 将位图Bitmap转成BitmapDrawable【Drawable的子类】
     *
     * @param bitmap 位图
     * @return
     */
    public static BitmapDrawable bitmapToDrawable(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            return new BitmapDrawable(bitmap);
        }
        return null;
    }


    /**
     * 模糊ImageView
     *
     * @param context
     * @param img     ImageView
     * @param level   模糊等级【0 ~ 25之间】
     */
    public static void blurImageView(Context context, ImageView img, float level) {
        // 将图片处理成模糊
        Bitmap bitmap = blurBitmap(context, drawableToBitmap(img.getDrawable()), level);
        if (bitmap != null) {
            img.setImageBitmap(bitmap);
        }
    }

    /**
     * 模糊ImageView
     *
     * @param context
     * @param img     ImageView
     * @param level   模糊等级【0 ~ 25之间】
     * @param color   为ImageView蒙上一层颜色
     */
    public static void blurImageView(Context context, ImageView img, float level, int color) {
        // 将图片处理成模糊
        Bitmap bitmap = blurBitmap(context, drawableToBitmap(img.getDrawable()), level);
        if (bitmap != null) {
            Drawable drawable = coverColor(context, bitmap, color);
            img.setScaleType(ImageView.ScaleType.FIT_XY);
            img.setImageDrawable(drawable);
        } else {
            img.setImageBitmap(null);
            img.setBackgroundColor(color);
        }
    }

    /**
     * 将bitmap转成蒙上颜色的Drawable
     *
     * @param context
     * @param bitmap
     * @param color   要蒙上的颜色
     * @return Drawable
     */
    public static Drawable coverColor(Context context, Bitmap bitmap, int color) {
        if (color != Color.TRANSPARENT) {
            Paint paint = new Paint();
            paint.setColor(color);
            RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
            new Canvas(bitmap).drawRoundRect(rect, 0, 0, paint);
        }
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    //给位图蒙上颜色。
    public static Bitmap coverColorForBitmap(Bitmap bitmap, int color) {
        if (color != Color.TRANSPARENT) {
            Paint paint = new Paint();
            paint.setColor(color);
            RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
            new Canvas(bitmap).drawRoundRect(rect, 0, 0, paint);
        }
        return bitmap;//fixme 返回的是同一个位图对象。
    }

    public static Bitmap blurBitmap(Context context, Bitmap bitmap, float blurRadius) {
        return blurBitmap(context, bitmap, blurRadius, true);
    }


//                            fixme 毛玻璃效果，调用案例。
//                          kview {
//                          autoBg {
//                              width = kpx.x(640)
//                              height = kpx.x(795)
//                              autoBg(R.mipmap.timg){
//                                  autoBg= KBitmapUtils.blurBitmap(ctx, it, 25f, true)//fixme 毛玻璃效果
//                              }
//                          }
//                        }.lparams {
//                            width = kpx.x(640)
//                            height = kpx.x(795)
//                            topMargin = kpx.x(50)
//                        }

    /**
     * 模糊图片的具体方法；fixme 用的是安卓原生 ScriptIntrinsicBlur里的算法；速度很快；效果杠杠的。
     *
     * @param context    上下文对象
     * @param bitmap     需要模糊的图片
     * @param blurRadius 模糊等级【0 ~ 25之间】;一般都是设置25
     * @param isRecycled 是否释放掉原图；一般都释放,设置为true
     * @return 模糊处理后的图片
     */
    public static Bitmap blurBitmap(Context context, Bitmap bitmap, float blurRadius, boolean isRecycled) {
        if (blurRadius < 0) {
            blurRadius = 0;
        }
        if (blurRadius > 25) {
            blurRadius = 25;
        }
        Bitmap outputBitmap = null;
        try {
            if (Build.VERSION.SDK_INT >= 17) {//17是安卓4.2版本
                Class.forName("android.renderscript.ScriptIntrinsicBlur");
                // 计算图片缩小后的长宽
                int width = Math.round(bitmap.getWidth() * BITMAP_SCALE);
                int height = Math.round(bitmap.getHeight() * BITMAP_SCALE);
                if (width < 2 || height < 2) {
                    return null;
                }

                // 将缩小后的图片做为预渲染的图片。
                Bitmap inputBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
                // 创建一张渲染后的输出图片。
                outputBitmap = Bitmap.createBitmap(inputBitmap);

                // 创建RenderScript内核对象
                RenderScript rs = RenderScript.create(context);
                // 创建一个模糊效果的RenderScript的工具对象
                ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

                // 由于RenderScript并没有使用VM来分配内存,所以需要使用Allocation类来创建和分配内存空间。
                // 创建Allocation对象的时候其实内存是空的,需要使用copyTo()将数据填充进去。
                Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
                Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);

                // 设置渲染的模糊程度, 25f是最大模糊度
                blurScript.setRadius(blurRadius);
                // 设置blurScript对象的输入内存
                blurScript.setInput(tmpIn);
                // 将输出数据保存到输出内存中
                blurScript.forEach(tmpOut);

                // 将数据填充到Allocation中
                tmpOut.copyTo(outputBitmap);

                if (inputBitmap != outputBitmap && inputBitmap != null && !inputBitmap.isRecycled()) {
                    inputBitmap.isRecycled();//fixme 释放无用的资源；亲测可以释放。
                    inputBitmap = null;
                }
                if (isRecycled) {
                    if (bitmap != outputBitmap && bitmap != null && !bitmap.isRecycled()) {
                        bitmap.isRecycled();//fixme 释放掉原图
                        bitmap = null;
                    }
                }
                return outputBitmap;//fixme 返回的是一个新的位图，和原图已经不是同一个对象了。
            }


        } catch (Exception e) {
            Log.e("test", "Android版本过低");
        }
        return null;
    }
}

package cn.oi.klittle.era.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.lang.reflect.Field;

import cn.oi.klittle.era.comm.kpx;

/**
 * Created by 彭治铭 on 2018/7/21.
 */
public class KBaseProportion {

    private float textProportion;//文字比例大小,以水平为标准
    private float horizontalProportion;//水平比例大小
    private float verticalProportion;//垂直比例大小,以水平为标准
    private float realVerticalProportion;//真实的垂直比例大小。
    private float density;//当前设备dpi密度值比例，即 dpi/160 的比值
    private Boolean ignore = false;//是否忽悠比例缩放
    private int statusHeight = -1;//状态栏高度


    //真实分辨率
    public static float realWidthPixels;
    public static float realHeightPixels;

    //获取最真实的屏幕宽【宽和高会随屏幕的横屏竖屏的切换而改变。宽始终在是屏幕的水平方向】
    public static float getRealWidthPixels(Activity activity) {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    //获取最真实屏幕高【宽和高会随屏幕的横屏竖屏的切换而改变。高始终是屏幕的垂直方向】
    public static float getRealHeightPixels(Activity activity) {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    //Dp转像素
    public double dpToPixel(double dp) {
        return dp * density;//其中 density就是 dpi/160的比值。
    }

    //像素转Dp
    public double pixelToDp(double px) {
        return px / density;
    }

    /**
     * 以当前控件的x,y值坐标。即以当前的位置为基础，宽度扩张到屏幕右边，高度扩张到屏幕底部。
     *
     * @param activity
     * @param view      当前控件
     * @param maxWidth  传人的宽，会和屏幕右边的宽度对比，maxWidth 大于比较值时才有效，小不起作用。
     * @param maxHeight 传人的高，会和屏幕底部的高对比。maxHeight 大于比较值时才有效，小不起作用。
     */
    public void adapterScreenRightAndBottom(final Activity activity, final View view, final int maxWidth, final int maxHeight) {
        int[] location = new int[2];//获取现对于整个屏幕的位置。
        view.getLocationOnScreen(location);
        if (location[0] <= 0 || location[1] <= 0) {
            //坐标小于或等于0，表示控件位置还没加载出来。要延迟一会儿。
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    int[] location = new int[2];
                    view.getLocationOnScreen(location);
                    int width = (int) (getRealWidthPixels(activity) - location[0]);//获得也屏幕右边的宽度
                    if (maxWidth > width) {
                        width = maxWidth;
                    }
                    int height = (int) (getRealHeightPixels(activity) - location[1]);//获得与屏幕底部的高度。
                    if (maxHeight > height) {
                        height = maxHeight;
                    }
                    view.getLayoutParams().width = width;
                    view.getLayoutParams().height = height;
                    view.requestLayout();
                }
            }, 100);
        } else {
            int width = (int) (getRealWidthPixels(activity) - location[0]);//获得也屏幕右边的宽度
            if (maxWidth > width) {
                width = maxWidth;
            }
            int height = (int) (getRealHeightPixels(activity) - location[1]);//获得与屏幕底部的高度。
            if (maxHeight > height) {
                height = maxHeight;
            }
            view.getLayoutParams().width = width;
            view.getLayoutParams().height = height;
            view.requestLayout();
        }
    }

    protected KBaseProportion() {
        //标准分辨率
        //垂直分辨率 1920x1080作为主流标准。比例为16:9 。或 1280x720【小,省内存,一般这个就足够了】。使用本类，所有单位都使用px像素为单位。
        //1334x750是苹果的主流屏
        init(750f, 1334f);
    }

    //如果宽和高变了。可以调用这个方法重新初始化基准宽和高
    //ProportionUtils.getInstance().init(720,1280);
    public void init(float widthPixels, float heightPixels) {//必须是float类型
        //真实值
        DisplayMetrics displayMetrics = KBaseApplication.getInstance().getResources().getDisplayMetrics();
        realWidthPixels = displayMetrics.widthPixels;
        realHeightPixels = displayMetrics.heightPixels;
        density = displayMetrics.density;
        //小的和小的比较，大的和大的比较防止出错【彻底忽略横屏和竖屏的约束】,已经确认，下面的逻辑完全正确，没有错误。
        //横屏和竖屏切换之后，宽和高的数值也会更换。
        if (widthPixels < heightPixels) {
            if (displayMetrics.widthPixels < displayMetrics.heightPixels) {
                horizontalProportion = displayMetrics.widthPixels / widthPixels;
                verticalProportion = displayMetrics.heightPixels / heightPixels;
            } else {
                horizontalProportion = displayMetrics.heightPixels / widthPixels;
                verticalProportion = displayMetrics.widthPixels / heightPixels;
            }
        } else {
            if (displayMetrics.widthPixels > displayMetrics.heightPixels) {
                horizontalProportion = displayMetrics.heightPixels / heightPixels;
                verticalProportion = displayMetrics.widthPixels / widthPixels;
            } else {
                horizontalProportion = displayMetrics.widthPixels / heightPixels;
                verticalProportion = displayMetrics.heightPixels / widthPixels;
            }
        }
        //垂直比例,文本比例,都以水平比例为标准.屏幕适配的比例一定要保持一致。fixme 以水平适配为标准
        realVerticalProportion = verticalProportion;//保存真实的垂直比值。
        verticalProportion = horizontalProportion;
        textProportion = horizontalProportion;
        ignore();
//        Log.e("ui", "密度：" + this.density);
//        Log.e("ui", "displayMetrics.widthPixels:" + displayMetrics.widthPixels + "\ndisplayMetrics.heightPixels:" + displayMetrics.heightPixels);
//        Log.e("ui", "horizontalProportion:\t" + horizontalProportion + "\tverticalProportion\t" + verticalProportion);
        displayMetrics = null;
    }


    /**
     * 获得状态栏的高度，单位像素
     *
     * @return
     */
    public int getStatusHeight(Context context) {
        if (statusHeight <= 0) {
            try {
                Class<?> clazz = Class.forName("com.android.internal.R$dimen");
                Object object = clazz.newInstance();
                int height = Integer.parseInt(clazz.getField("status_bar_height")
                        .get(object).toString());
                statusHeight = context.getResources().getDimensionPixelSize(height);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusHeight;
    }

    private void ignore() {
        //防止比例为1的时候做多余的适配
        if (horizontalProportion > 0.99 && horizontalProportion < 1.01) { //750/720=1.04166 苹果/安卓
            ignore = true;
        } else {
            ignore = false;
        }
//        Log.e("ui","ignore:\t"+ignore);
    }

    //适配最外层布局控件，全屏设置【真实全屏高度和宽度】。即使属性是match_parent【靠不住，不真实】，不建议调用。【对Dialog弹出框不行。】
    public void adapterOutView(View view, boolean rotation90) {
        ViewGroup.LayoutParams laParams = view
                .getLayoutParams();
        DisplayMetrics displayMetrics = view.getResources().getDisplayMetrics();//建议每次都实例化。因为宽度和高度会随切屏而改变。
        if (rotation90) {//旋转90度。宽度和高度对调。
            laParams.width = displayMetrics.heightPixels;
            laParams.height = displayMetrics.widthPixels;
            view.requestLayout();
            //旋转90度。自定义竖屏变横屏
            if (view.getRotation() != 90) {
                view.setPivotX(view.getLayoutParams().height / 2);
                view.setPivotY(view.getLayoutParams().height / 2);
                view.setRotation(90);
            }

        } else {
            laParams.width = displayMetrics.widthPixels;
            laParams.height = displayMetrics.heightPixels;
            view.requestLayout();
            //恢复原始正常
            if (view.getRotation() != 0) {
                view.setRotation(0);
            }
        }
        adapterPadding(view);//适配内补丁,最外层控制不需要设置外补丁，无意义。设置内补丁即可
    }

    /**
     * 全屏显示。对所有控件都有效【以当前Activity的宽和高 为基准】【对Dialog弹出框同样有效。百分百全屏】
     *
     * @param activity
     * @param view
     */
    public void adapterScreen(Activity activity, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        ViewGroup.LayoutParams laParams = view
                .getLayoutParams();
        laParams.width = displayMetrics.widthPixels;
        //laParams.height = displayMetrics.heightPixels;
        //laParams.height= displayMetrics.heightPixels+kpx.INSTANCE.getNavigationBarHeight(activity.getApplication())+kpx.INSTANCE.getStatusHeight();//fixme 获取屏幕真正的高。
        //laParams.height= displayMetrics.heightPixels+kpx.INSTANCE.getNavigationBarHeight(activity.getApplication());
        //Log.e("test","最大高:\t"+kpx.INSTANCE.maxScreenHeight()+"\theight:\t"+laParams.height);
        laParams.height = kpx.INSTANCE.maxScreenHeight();
        view.requestLayout();
        adapterPadding(view);//适配内补丁,最外层控制不需要设置外补丁，无意义。设置内补丁即可
    }

    //适配旋转90度【竖屏变横屏】之后的getRawX()事件。
    public float getAdapter90RawX(MotionEvent event) {
        return event.getRawY();
    }

    //适配旋转90度【竖屏变横屏】之后的getRawY()事件。
    public float getAdapter90RawY(MotionEvent event) {
        if (realWidthPixels >= realHeightPixels) {
            return realWidthPixels - event.getRawX();
        } else {
            return realHeightPixels - event.getRawX();
        }
    }

    public void adapterView(View view) {
        adapterView(view, true);
    }

    /**
     * @param view         适配View
     * @param isHorizontal 是否是水平为标准。默认是true
     */
    public void adapterView(View view, boolean isHorizontal) {
        if (ignore || view == null) {//防止View为空
            return;
        }
        if (view.getTag() != null && view.getTag().equals("noadapter")) {
            return;//android:tag="noadapter" 不适配
        }
        float proporttion = horizontalProportion;
        if (!isHorizontal) {
            proporttion = realVerticalProportion;//以垂直为标准适配
        }
        try {
            //Log.e("test", "宽:\t" + view.getLayoutParams().width + "\t高:\t" + view.getLayoutParams().height + "\t背景:\t" + view.getBackground()+"\t"+(view.getBackground() instanceof BitmapDrawable));
            //match_parent【-1】和wrap_content【-2】数值都是小于0的，不会对这两个属性进行适配。
            //boolean b = true;
            if (view.getLayoutParams().width > 0) {
                //单位都是像素
                view.getLayoutParams().width = (int) Math.ceil(view.getLayoutParams().width * proporttion);//取大。防止为0
            } else if (view.getBackground() != null && (view.getBackground() instanceof BitmapDrawable)) {//确保背景能够转化成位图
                //Log.e("test", "背景长度:\t" + view.getBackground().getIntrinsicWidth() + "\t位图:\t" + (view.getBackground() instanceof BitmapDrawable) + "\t颜色:\t" + (view.getBackground() instanceof ColorDrawable));
                //根据背景图片大小，来适配控件大小。
                view.getLayoutParams().width = (int) Math.ceil(view.getBackground().getIntrinsicWidth() * proporttion);
            }

            if (view.getLayoutParams().height > 0) {
                view.getLayoutParams().height = (int) Math.ceil(view.getLayoutParams().height * proporttion);//取大
            } else if (view.getBackground() != null && (view.getBackground() instanceof BitmapDrawable)) {
                view.getLayoutParams().height = (int) Math.ceil(view.getBackground().getIntrinsicHeight() * proporttion);
            }

            view.requestLayout();
            adapterPadding(view);//内补丁
            adapterMargin(view);//外补丁
        } catch (Exception e) {
            Log.e("ui", "View:\t" + e.getMessage());
        }
    }

    /**
     * @param textView 文本框
     * @param parent   true 强制宽和高布满父容器【本身宽度和高度小于或等于0时有效果】，false 普通适配。
     */
    public void adapterTextView(TextView textView, boolean parent, boolean isHorizontal) {
        if (textView != null) {
            adapterTextView(textView, isHorizontal);
            if (parent) {
                //防止textView.setGravity(Gravity.CENTER);文本居中无效。居中无效的原因就是没有具体的宽和高。高和宽只有具体了，居中设置才有效。
                //以下方法，使文本框的宽和高，布满父容器。
                //Log.e("test","父容器宽度:\t"+((View) (textView.getParent())).getLayoutParams().width);
                if (textView.getLayoutParams().width <= 0 && textView.getParent() != null && ((View) (textView.getParent())).getLayoutParams().width > 0) {
                    textView.getLayoutParams().width = ((View) (textView.getParent())).getLayoutParams().width;
                }
                if (textView.getLayoutParams().height <= 0 && textView.getParent() != null && ((View) (textView.getParent())).getLayoutParams().height > 0) {
                    textView.getLayoutParams().height = ((View) (textView.getParent())).getLayoutParams().height;
                }
                textView.requestLayout();
            }
        }
    }

    public void adapterTextView(TextView textView) {
        adapterTextView(textView, true);
    }

    //适配文本框【EditTextView也继承TextView】
    public void adapterTextView(TextView textView, boolean isHorizontal) {
        if (ignore) {
            //Log.e("test","忽悠");
            return;
        }
        try {

            float proporttion = horizontalProportion;
            if (!isHorizontal) {
                proporttion = realVerticalProportion;//以垂直为标准适配
            }

            //Log.e("test","宽:\t"+textView.getLayoutParams().width);
            adapterView(textView, isHorizontal);//适配宽和高
            //Log.e("test","宽2:\t"+textView.getLayoutParams().width);
            //px=dp*dpi/160=dp*density,所以dp=px/density
            textView.setTextSize(textView.getTextSize() * proporttion / density);//除以density是将px转化为dp。textView.setTextSize单位是dp 。设置文字大小
            //设置行间距，单位是Px像素。默认行间距是0
            if (Build.VERSION.SDK_INT >= 16) {
                textView.setLineSpacing(textView.getLineSpacingExtra() * proporttion, textView.getLineSpacingMultiplier());
            }
        } catch (Exception e) {
            Log.e("ui", "TextView:\t" + e.getMessage());
        }
    }

    public void adapterButton(Button button) {
        adapterButton(button, true);
    }

    //适配按钮【Button也继承TextView】
    public void adapterButton(Button button, boolean isHorizontal) {
        if (ignore) {
            return;
        }
        adapterTextView(button, isHorizontal);
    }

    public Bitmap adapterBitmap(Bitmap bitmap) {
        return adapterBitmap(bitmap, true);
    }

    //适配Bitmap位图【适配当前的屏幕标准】
    public Bitmap adapterBitmap(Bitmap bitmap, boolean isHorizontal) {
        if (ignore) {
            return bitmap;
        }
        float proporttion = horizontalProportion;
        if (!isHorizontal) {
            proporttion = realVerticalProportion;//以垂直为标准适配
        }
        //Log.e("test","比例:\t"+horizontalProportion);
        bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * proporttion), (int) (bitmap.getHeight() * proporttion), true);
        return bitmap;
    }

    public Bitmap adapterBitmap(Bitmap bitmap, int width, int height) {
        return adapterBitmap(bitmap, width, height, true);
    }

    /**
     * 适配位图
     *
     * @param bitmap
     * @param width  位图原有的宽
     * @param height 位图原有的高
     * @return
     */
    public Bitmap adapterBitmap(Bitmap bitmap, int width, int height, boolean isHorizontal) {
        if (ignore) {
            return bitmap;
        }
        float proporttion = horizontalProportion;
        if (!isHorizontal) {
            proporttion = realVerticalProportion;//以垂直为标准适配
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, (int) (width * proporttion), (int) (height * proporttion), true);
        return bitmap;
    }

    /**
     * 压缩位图。
     *
     * @param bitmap
     * @param width  压缩后的宽
     * @param height 压缩后的高度
     * @return
     */
    public Bitmap scaleBitmap(Bitmap bitmap, int width, int height) {
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        return bitmap;
    }

    //等比压缩，压缩之后，宽和高相等。
    //参数values是压缩后的宽度及高度。
    //计算比率时，千万要注意，一定要使用float类型。千万不要使用int类型。不然计算不出。
    //这个方法，图片不会变形[取中间的那一部分]
    public Bitmap GeometricCompressionBitmap(Bitmap src, float value) {
        Bitmap dst;
        if (src.getWidth() == src.getHeight()) {
            dst = Bitmap.createScaledBitmap(src, (int) value, (int) value, true);
        } else {
            //以较小边长为计算标准
            //宽小于高
            if (src.getWidth() < src.getHeight()) {
                float p = (float) src.getWidth() / (float) value;
                float heith = (float) src.getHeight() / p;
                dst = Bitmap.createScaledBitmap(src, (int) value, (int) heith, true);
                int y = (dst.getHeight() - dst.getWidth()) / 2;
                dst = Bitmap.createBitmap(dst, 0, y, dst.getWidth(), dst.getWidth());
            } else {
                //宽大于高，或等于高。
                //高小于宽
                float p = (float) src.getHeight() / (float) value;
                float width = (float) src.getWidth() / p;
                dst = Bitmap.createScaledBitmap(src, (int) width, (int) value, true);
                int x = (dst.getWidth() - dst.getHeight()) / 2;
                dst = Bitmap.createBitmap(dst, x, 0, dst.getHeight(), dst.getHeight());
            }
        }
        return dst;
    }

    /**
     * 不要在适配器里。对图片进行压缩。适配器反反复复的执行。多次执行。会内存溢出的。切记。
     * <p>
     * 根据宽和比率压缩Bitmap
     * 这个方法，图片不会变形[取中间的那一部分]
     *
     * @param src   原图
     * @param width 压缩后的宽
     * @param sp    高的比率。即 height=width*sp; float sp=(float) height/(float) width;
     * @return
     */
    public Bitmap GeometricCompressionBitmap(Bitmap src, float width, float sp) {
        Bitmap dst = null;
        float dp = (float) src.getHeight() / (float) src.getWidth();
        float pp = Math.abs(sp - dp);
        if (pp < 0.01) {
            //fixme Bitmap.createScaledBitmap 如果缩放位图和原有位图大小差异在1%之内，使用的还是同一个位图对象。
            //fixme 大小差异超过1%左右，使用的就是新的位图，和原位图就没有关系了。
            dst = Bitmap.createScaledBitmap(src, (int) width, (int) (width * sp), true);
        } else {
            float p = (float) src.getWidth() / (float) width;
            float heith = (float) src.getHeight() / p;
            dst = Bitmap.createScaledBitmap(src, (int) width, (int) heith, true);
            int height = (int) (width * sp);
            //要求高，小于压缩后的高。对压缩后的高进行截取
            if (height < dst.getHeight()) {
                int y = (dst.getHeight() - height) / 2;
                dst = Bitmap.createBitmap(dst, 0, y, dst.getWidth(), height);
            }
        }
        return dst;
    }

    /**
     * 适配数值
     *
     * @param offset 数值
     * @return
     */
    public int adapterInt(int offset) {
        return adapterInt(offset, true);
    }

    /**
     * 适配数值
     *
     * @param offset       数值
     * @param isHorizontal true以水平比例为标准（默认），false以垂直比例为标准
     * @return
     */
    public int adapterInt(int offset, boolean isHorizontal) {
        if (ignore) {
            return offset;
        }
        float proporttion = horizontalProportion;
        if (!isHorizontal) {
            proporttion = realVerticalProportion;//以垂直为标准适配
        }
        offset = (int) (offset * proporttion);
        return offset;
    }

    /**
     * 适配数值Double
     *
     * @param offset 数值
     * @return
     */
    public Double adapterDouble(Double offset) {
        return adapterDouble(offset, true);
    }

    /**
     * 适配数值Double
     *
     * @param offset       数值
     * @param isHorizontal true以水平比例为标准（默认），false以垂直比例为标准
     * @return
     */
    public Double adapterDouble(Double offset, boolean isHorizontal) {
        if (ignore) {
            return offset;
        }
        float proporttion = horizontalProportion;
        if (!isHorizontal) {
            proporttion = realVerticalProportion;//以垂直为标准适配
        }
        offset = (Double) (offset * proporttion);
        return offset;
    }

    /**
     * 根据id，获取dimens文件里的数值
     *
     * @param id 如 R.dimen.pixelX_100
     * @return
     */
    public float getDimension(int id) {
        return KBaseApplication.getInstance().getResources().getDimension(id);
    }

    public void adapterGridview(GridView gridView) {
        adapterGridview(gridView, true);
    }

    @SuppressLint("NewApi")
    public void adapterGridview(GridView gridView, boolean isHorizontal) {
        if (ignore) {
            return;
        }
        try {
            adapterView(gridView, isHorizontal);
            float proporttion = horizontalProportion;
            if (!isHorizontal) {
                proporttion = realVerticalProportion;//以垂直为标准适配
            }
            //Log.e("test","水平间隙"+gridView.getHorizontalSpacing()+"\t垂直间隙"+gridView.getVerticalSpacing());
            //setVerticalSpacing参数是像素，只需要设置垂直间隙。水平间隙会根据item的宽度合理安排。不需要设置(设置了也无效)
            gridView.setVerticalSpacing((int) (gridView.getVerticalSpacing() * proporttion));
        } catch (Exception e) {
            Log.e("ui", "GridView:\t" + e.getMessage());
        }
    }

    private void adapterPadding(View view) {
        adapterPadding(view, true);
    }

    //适配内补丁
    //内补丁默认都是0
    private void adapterPadding(View view, boolean isHorizontal) {
        float proporttion = horizontalProportion;
        if (!isHorizontal) {
            proporttion = realVerticalProportion;//以垂直为标准适配
        }
        //单位为像素px(get和set都是PX)
        //Log.e("ui","padding\tleft:\t"+view.getPaddingLeft()+"\ttop:\t"+view.getPaddingTop()+"\tright:\t"+view.getPaddingRight()+"\tbottom:\t"+view.getPaddingBottom());
        view.setPadding((int) (view.getPaddingLeft() * proporttion), (int) (view.getPaddingTop() * proporttion), (int) (view.getPaddingRight() * proporttion), (int) (view.getPaddingBottom() * proporttion));
        //Log.e("ui","padding\tleft:\t"+view.getPaddingLeft()+"\ttop:\t"+view.getPaddingTop()+"\tright:\t"+view.getPaddingRight()+"\tbottom:\t"+view.getPaddingBottom());
    }

    private void adapterMargin(View view) {
        adapterMargin(view, true);
    }

    //适配外补丁【只对父容器是约束布局(ConstraintLayout)起作用。其他的布局亲测没有任何效果】
    //ConstraintLayout.LayoutParams 继承于 ViewGroup.MarginLayoutParams 继承于 ViewGroup.LayoutParams
    //所有的布局LayoutParams基本都继承于 ViewGroup.MarginLayoutParams,而 ViewGroup.MarginLayoutParams又继承于ViewGroup.LayoutParams
    //外补丁默认也是0
    private void adapterMargin(View view, boolean isHorizontal) {

        float proporttion = horizontalProportion;
        if (!isHorizontal) {
            proporttion = realVerticalProportion;//以垂直为标准适配
        }

//        //只对ConstraintLayout这个布局设置有效，其他布局。无效。
//        if (view.getLayoutParams() != null && (view.getLayoutParams() instanceof ConstraintLayout.LayoutParams)) {
//            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) view.getLayoutParams();
//            //Log.e("test","左外补丁:\t"+layoutParams.leftMargin);
//            layoutParams.setMargins((int) (layoutParams.leftMargin * proporttion), (int) (layoutParams.topMargin * proporttion), (int) (layoutParams.rightMargin * proporttion), (int) (layoutParams.bottomMargin * proporttion));
//            //Log.e("test","左外补丁2:\t"+layoutParams.leftMargin);
//            view.requestLayout();
//        } else if (view.getLayoutParams() != null && (view.getLayoutParams() instanceof LinearLayout.LayoutParams)) {
//
//            //view.getLayoutParams()由父容器来决定。一直以为只对约束布局有效。其实只要获取正确的LayoutParams就能够设置外布丁。亲测有效。
//
//            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
//            layoutParams.setMargins((int) (layoutParams.leftMargin * proporttion), (int) (layoutParams.topMargin * proporttion), (int) (layoutParams.rightMargin * proporttion), (int) (layoutParams.bottomMargin * proporttion));
//            //Log.e("test","左外补丁2:\t"+layoutParams.leftMargin);
//            view.requestLayout();
//        }else if(view.getLayoutParams() != null && (view.getLayoutParams() instanceof RelativeLayout.LayoutParams)){
//            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
//            layoutParams.setMargins((int) (layoutParams.leftMargin * proporttion), (int) (layoutParams.topMargin * proporttion), (int) (layoutParams.rightMargin * proporttion), (int) (layoutParams.bottomMargin * proporttion));
//            //Log.e("test","左外补丁2:\t"+layoutParams.leftMargin);
//            view.requestLayout();
//        }else if(view.getLayoutParams() != null && (view.getLayoutParams() instanceof FrameLayout.LayoutParams)){
//            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
//            layoutParams.setMargins((int) (layoutParams.leftMargin * proporttion), (int) (layoutParams.topMargin * proporttion), (int) (layoutParams.rightMargin * proporttion), (int) (layoutParams.bottomMargin * proporttion));
//            //Log.e("test","左外补丁2:\t"+layoutParams.leftMargin);
//            view.requestLayout();
//        }


        //一劳永逸，对所有控件都有效。
        //view.getLayoutParams()由父容器来决定。一直以为只对约束布局有效。其实只要获取正确的LayoutParams就能够设置外布丁。亲测有效。
        //注意约束布局外补丁没有效果。是因为没有指定参照对象。即app:layout_constraintLeft_toLeftOf等属性。
        if (view.getLayoutParams() != null && (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            layoutParams.setMargins((int) (layoutParams.leftMargin * proporttion), (int) (layoutParams.topMargin * proporttion), (int) (layoutParams.rightMargin * proporttion), (int) (layoutParams.bottomMargin * proporttion));
            view.requestLayout();
        }

    }

    //获取xml文件最外层控件。不是是dialog还是activity。都一样。都是使用Window获取
    public ViewGroup getParentView(Window window) {
        View decorView = window.getDecorView();//布局里面的最顶层控件，本质上是FrameLayout(帧布局)，FrameLayout.LayoutParams
        ViewGroup contentView = (ViewGroup) decorView.findViewById(android.R.id.content);//我们的布局文件。就放在contentView里面。contentView本质上也是FrameLayout(帧布局)，FrameLayout.LayoutParams
        View parent = contentView.getChildAt(0);//这就是我们xml布局文件最外层的那个父容器控件。
        return (ViewGroup) parent;
    }

    public void adapterWindow(Activity activity, Window window) {
        adapterWindow(activity, window, true);
    }

    /**
     * 适配Activity或Dialog。都可以。只需传人当前窗口的Window对象即可。
     *
     * @param activity 上下文
     * @param window   如：getWindow()
     */
    public void adapterWindow(Activity activity, Window window, boolean isHorizontal) {
        adapterAllView(activity, getParentView(window), true, true, isHorizontal);
    }

    public void adapterAllView(Activity activity, View parent, boolean isadapter, boolean isfull) {
        adapterAllView(activity, parent, isadapter, isfull, true);
    }

    /**
     * 适配一个View本身，及其所有的子View。【含嵌套布局(包含子View的子View)】
     *
     * @param activity
     * @param parent    需要适配的View,一般是最外层的ViewGroup控件。
     * @param isadapter 是否对传入的view做适配
     * @param isfull    是否对传入的view,全屏适配宽和高。
     */
    public void adapterAllView(Activity activity, View parent, boolean isadapter, boolean isfull, boolean isHorizontal) {
        if (isadapter) {
            if (isfull) {
                adapterScreen(activity, parent);
            } else {
                adapterView(parent, isHorizontal);
            }
        }
        if (parent instanceof ViewGroup) {
            adapterChildView((ViewGroup) parent, isHorizontal);
        }
    }

    public void adapterChildView(ViewGroup vp) {
        adapterChildView(vp, true);
    }

    //对ViewGroup循环遍历，适配所有的子View。对传入的ViewGroup不做适配。直接适配子View
    public void adapterChildView(ViewGroup vp, boolean isHorizontal) {
        //子类可以转化为父类。并且转化后的父类能够保持子类的所有信息。父类再转化成子类时，信息不会丢失。
        //View是所有控件的父类。ViewGroup也继承于View
        for (int i = 0; i < vp.getChildCount(); i++) {
            View viewchild = vp.getChildAt(i);
            boolean b = true;//true需要适配，false不需要适配
            //适配文本
            //Button,EditText都继承于TextView
            if (b && (viewchild instanceof TextView)) {
                adapterTextView((TextView) viewchild, isHorizontal);
                b = false;
//                    Log.e("test", "文本或按钮");
            } else if (b && (viewchild instanceof GridView)) {
                adapterGridview((GridView) viewchild, isHorizontal);
                b = false;
//                    Log.e("test", "GridView");
            } else {
                //适配View
                if (b) {
                    adapterView(viewchild, isHorizontal);
//                        Log.e("test", "View");
                }
            }
            //ConstraintLayout 继承ViewGroup
            //RecyclerView ViewGroup 继承ViewGroup
            //ScrollView 继承 FrameLayout【帧布局】 ,FrameLayout继承ViewGroup
            //HorizontalScrollView 水平 也继承FrameLayout
            //FrameLayout 继承ViewGroup
            //RelativeLayout 继承ViewGroup
            //LinearLayout 继承ViewGroup
            if (viewchild instanceof ViewGroup) {
                //对AbsoluteLayout,FrameLayout,ListView,GridView,RecyclerView过滤，子View不做适配，因为。这些子View都是适配器动态加载的。不好做处理。交给适配器自己去做适配。
                //就只对ConstraintLayout，ScrollView，RelativeLayout，LinearLayout做适配。其他的ViewGroup暂不做处理。
                //FrameLayout的子View不做适配。
                if ((viewchild instanceof ConstraintLayout) || (viewchild instanceof ScrollView) || (viewchild instanceof HorizontalScrollView) || (viewchild instanceof RelativeLayout) || (viewchild instanceof LinearLayout)) {
                    adapterChildView((ViewGroup) viewchild, isHorizontal);//递归调用(自己调自己)，循环一个View里面的所有子View。包括子View的子View。
                }
            }
        }
    }

    /**
     * 通过反射来适配UI,测试时使用没问题。
     * 但是App签名打包后，运行会错误。可能是系统自动自带混淆，导致反射找不到变量名了。
     *
     * @param clazz  参数一 this.getClass()
     * @param entity 参数二 this
     */
    public void reflection(Class clazz, Object entity) {
        //Class<?> clazz = getClass();
        Field[] fields = clazz.getFields();//获取当前类所有的属性
        for (Field field : fields) {
            try {
                Object obj = field.get(entity);
                if (obj instanceof View) {//对属性进行过滤
                    //View view = (View) obj;//直接对view操作即可。无论是控件还是布局都继承View
                    //Log.e("ui", "属性" + field.getName());
                    if (obj instanceof Button) {//Button继承TextView，所有判断放在TextView前面
                        adapterButton((Button) obj);
                    } else if (obj instanceof TextView) {
                        adapterTextView((TextView) obj);
                    } else if (obj instanceof GridView) {
                        adapterGridview((GridView) obj);
                    } else {
                        adapterView((View) obj);
                    }
                    field = null;
                    obj = null;
                }
            } catch (Exception e) {
                Log.e("ui", "field:\t" + e.getMessage());
            }
        }
        fields = null;
        System.gc();
    }

    public float getTextProportion() {
        return textProportion;
    }

    public float getHorizontalProportion() {
        return horizontalProportion;
    }

    public float getVerticalProportion() {
        return verticalProportion;
    }

    public float getDensity() {
        return density;
    }

    public float getRealVerticalProportion() {
        return realVerticalProportion;
    }

    public void setRealVerticalProportion(float realVerticalProportion) {
        this.realVerticalProportion = realVerticalProportion;
    }
}

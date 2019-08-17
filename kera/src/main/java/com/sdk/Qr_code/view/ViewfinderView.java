package com.sdk.Qr_code.view;

import java.util.Collection;
import java.util.HashSet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.ResultPoint;
import com.sdk.Qr_code.manager.CameraManager;

import cn.oi.klittle.era.R;
import cn.oi.klittle.era.base.KBaseUi;

/**
 * 自定义扫描控件
 *
 * @author 彭治铭
 */
public final class ViewfinderView extends View {
    /**
     * 四个蓝色边角对应的长度，边框的长度。
     */
    private int ScreenRate;

    /**
     * 四个蓝色边角对应的宽度，边框的宽度。
     */
    private static final int CORNER_WIDTH = 6;

    /**
     * 扫描框中的中间线（移动的线）的与扫描框左右的间隙
     */
    private static final int MIDDLE_LINE_PADDING = 5;

    /**
     * 中间那条线（移动的线）每次刷新移动的距离
     */
    private static final int SPEEN_DISTANCE = 5;

    /**
     * 手机的屏幕密度
     */
    private static float density;
    /**
     * 字体大小
     */
    private static final int TEXT_SIZE = 16;
    /**
     * 字体距离扫描框下面的距离
     */
    private static final int TEXT_PADDING_TOP = 30;

    private Paint paint;
    /**
     * 返回的照片
     */
    private Bitmap resultBitmap;
    /**
     * 框架颜色
     */
    private int frameColor;
    /**
     * 结果点的颜色， 扫描框中，闪动的点
     */
    private int resultPointColor;
    /**
     * 可能的结果点数
     */
    private Collection<ResultPoint> possibleResultPoints;
    /**
     * 最后的结果点数
     */
    private Collection<ResultPoint> lastPossibleResultPoints;

    /**
     * 中间滑动线的最顶端位置
     */
    private int slideTop;

    /**
     * 中间滑动线的最底端位置
     */
    private int slideBottom;

    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ViewfinderView(Context context) {
        super(context);
        init(context);
    }

    public ViewfinderView(ViewGroup group) {
        this(group.getContext());
        group.addView(this);
    }

    //初始化
    private void init(Context context) {
        density = context.getResources().getDisplayMetrics().density;
        // 将像素转化成dp
        ScreenRate = (int) (25 * density);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);

        resultPointColor = Color.parseColor("#c0ffff00");// 扫描框中间那些一闪一闪的点点。
        possibleResultPoints = new HashSet<ResultPoint>(5);

        setLayerType(View.LAYER_TYPE_HARDWARE, paint);
    }

    Rect frame;//扫码边框

    @Override
    public void onDraw(Canvas canvas) {

        if (paint == null) {
            return;
        }

        if (frame == null) {
            // 控制扫描框的大小及位置，在getFramingRect()方法中。
            frame = CameraManager.get().getFramingRect();
            if (frame == null) {
                return;
            }
            slideTop = frame.top + CORNER_WIDTH;//扫描线最顶部的位置
            slideBottom = frame.bottom - CORNER_WIDTH;//扫描线最底部的位置
        }

        canvas.drawColor(Color.parseColor("#cc000000"));//背景半透明
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRect(frame, paint);//扫码框透明
        paint.setXfermode(null);

        if (resultBitmap != null && !resultBitmap.isRecycled()) {
            paint.setAlpha(255);
            //绘制二维码结果图片
            canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
        } else {
            frameColor = Color.parseColor("#6D6D6D");// 灰色
            paint.setColor(frameColor);
            // 画框架(四个角之间的连线)
            canvas.drawRect(frame.left, frame.top, frame.right + 1,
                    frame.top + 2, paint);//上
            canvas.drawRect(frame.left, frame.top + 2, frame.left + 2,
                    frame.bottom - 1, paint);//左
            canvas.drawRect(frame.right - 1, frame.top, frame.right + 1,
                    frame.bottom - 1, paint);//右
            canvas.drawRect(frame.left, frame.bottom - 1, frame.right + 1,
                    frame.bottom + 1, paint);//下
            frameColor = Color.parseColor("#08C8FF");// 蓝色
            paint.setColor(frameColor);
            // 画框架上的四个角（扫描框边上的角，共8个部分）
            //左上角
            canvas.drawRect(frame.left - CORNER_WIDTH / 2, frame.top
                    - CORNER_WIDTH / 2, frame.left + ScreenRate, frame.top
                    + CORNER_WIDTH / 2, paint);
            canvas.drawRect(frame.left - CORNER_WIDTH / 2, frame.top
                            - CORNER_WIDTH / 2, frame.left + CORNER_WIDTH / 2,
                    frame.top + ScreenRate, paint);
            //左下角
            canvas.drawRect(frame.left - CORNER_WIDTH / 2, frame.bottom
                    - ScreenRate, frame.left + CORNER_WIDTH / 2, frame.bottom
                    + CORNER_WIDTH / 2, paint);
            canvas.drawRect(frame.left - CORNER_WIDTH / 2, frame.bottom
                    - CORNER_WIDTH / 2, frame.left + ScreenRate, frame.bottom
                    + CORNER_WIDTH / 2, paint);
            //右上角
            canvas.drawRect(frame.right - ScreenRate, frame.top - CORNER_WIDTH
                    / 2, frame.right + CORNER_WIDTH / 2, frame.top
                    + CORNER_WIDTH / 2, paint);
            canvas.drawRect(frame.right - CORNER_WIDTH / 2, frame.top
                            - CORNER_WIDTH / 2, frame.right + CORNER_WIDTH / 2,
                    frame.top + ScreenRate, paint);
            //右下角
            canvas.drawRect(frame.right - CORNER_WIDTH / 2, frame.bottom
                    - ScreenRate, frame.right + CORNER_WIDTH / 2, frame.bottom
                    + CORNER_WIDTH / 2, paint);
            canvas.drawRect(frame.right - ScreenRate, frame.bottom
                            - CORNER_WIDTH / 2, frame.right + CORNER_WIDTH / 2,
                    frame.bottom + CORNER_WIDTH / 2, paint);

            // 用图片,画中间移动的线
            Rect lineRect = new Rect();
            lineRect.left = frame.left;
            lineRect.right = frame.right;
            lineRect.top = slideTop;
            lineRect.bottom = slideTop + MIDDLE_LINE_PADDING;
            canvas.drawBitmap(((BitmapDrawable) (getResources()
                            .getDrawable(R.drawable.kera_scan_line))).getBitmap(), null,
                    lineRect, paint);

            // 让线动起来，设置中间移动的线的坐标
            slideTop += SPEEN_DISTANCE;
            if (slideTop >= slideBottom) {
                slideTop = frame.top + CORNER_WIDTH;
            }

            // 画扫描框下面的字
            paint.setColor(Color.WHITE);
            paint.setTextSize(TEXT_SIZE * density);
            paint.setAlpha(200);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextAlign(Paint.Align.CENTER);
            //将二维码/条码放入框内，即可自动扫描
            canvas.drawText(KBaseUi.Companion.getString(R.string.kqr_timi), frame.left + (frame.right - frame.left) / 2, frame.bottom
                    + TEXT_PADDING_TOP * density, paint);


            /**
             * 扫描框中，闪动的点【像素关键点】;感觉有点不好看。建议屏蔽点。
             */
//            Collection<ResultPoint> currentPossible = possibleResultPoints;
//            Collection<ResultPoint> currentLast = lastPossibleResultPoints;
//            if (currentPossible.isEmpty()) {
//                lastPossibleResultPoints = null;
//            } else {
//                possibleResultPoints = new HashSet<ResultPoint>(5);
//                lastPossibleResultPoints = currentPossible;
//                paint.setColor(resultPointColor);
//                for (ResultPoint point : currentPossible) {
//                    canvas.drawCircle(frame.left + point.getX(), frame.top
//                            + point.getY(), 6.0f, paint);// 画扫描到的可能的点
//                }
//            }
//            if (currentLast != null) {
//                paint.setColor(resultPointColor);
//                for (ResultPoint point : currentLast) {
//                    canvas.drawCircle(frame.left + point.getX(), frame.top
//                            + point.getY(), 3.0f, paint);
//                }
//            }
//            扫描点结束

            //不停的自我刷新视图
            invalidate();
        }
    }

    //位图设置为空。
    public void drawViewfinder() {
        resultBitmap = null;
        invalidate();
    }

    /**
     * 设置扫码结果图片
     *
     * @param barcode An image of the decoded barcode.
     */
    public void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        possibleResultPoints.add(point);
    }

}

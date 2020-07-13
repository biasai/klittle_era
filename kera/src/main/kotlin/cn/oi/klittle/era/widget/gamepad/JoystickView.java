package cn.oi.klittle.era.widget.gamepad;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import cn.oi.klittle.era.exception.KCatchException;
import cn.oi.klittle.era.utils.KAssetsUtils;
import cn.oi.klittle.era.utils.KLoggerUtils;
import cn.oi.klittle.era.utils.KVibratorUtils;
import cn.oi.klittle.era.widget.gamepad.listener.JoystickListener;


/**
 * 滚珠方向键
 * 注意：底盘的宽度和高度占自身控件宽度和高度的2/3(居中),滚珠占1/3,且滚珠的滚动无法超过控件本身的宽度和高度(初始居中)
 * 控件的宽度和高度相等，且以较小的那个为标准。
 *
 * @author 彭治铭
 */
public class JoystickView extends View {

    //底盘
    private Bitmap mJoystickBG;
    //滚珠
    private Bitmap mJoystickRock;
    private Paint mPaint;

    //控件的宽带
    private int mWidth;
    //控件的高度
    private int mHeight;
    //位图偏移左边的位置
    private int paddingLeft = 0;
    //位图偏移顶部的位置
    private int paddingTop = 0;

    //中心坐标X
    private double mCenterX;
    //中心坐标Y
    private double mCenterY;
    //滚珠圆心X
    private double mRockCenterX;
    //滚珠圆心Y
    private double mRockCenterY;

    //低盘半径
    private double mOuterRadius;
    //滚珠半径
    private double mInnerRadius;

    private boolean mIsMotion = false;

    private JoystickListener mJoystickListener;

    public JoystickView(Context context) {
        this(context, null);
    }

    public JoystickView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JoystickView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPaint = new Paint();
    }

    //设置滚珠监听事件
    public void setJoystickListener(JoystickListener joystickListener) {
        mJoystickListener = joystickListener;
    }

    @Override
    public void layout(int left, int top, int right, int bottom) {
        try {
            if (null == mJoystickBG) {
                mWidth = right - left;
                mHeight = bottom - top;
                mWidth = mWidth - 2 * paddingLeft;
                mHeight = mHeight - 2 * paddingLeft;
                mOuterRadius = mWidth / 3;
                mInnerRadius = mWidth / 6;
                mCenterX = mWidth / 2;
                mCenterY = mHeight / 2;

                mRockCenterX = mCenterX;
                mRockCenterY = mCenterY;

                Resources res = getResources();
                //fixme 滚珠底盘图片
                mJoystickBG = KAssetsUtils.getInstance().getBitmapFromAssets("手柄/滚珠/底盘.png");
                //fixme 滚珠图片
                mJoystickRock = KAssetsUtils.getInstance().getBitmapFromAssets("手柄/滚珠/球.png");
                if (isInEditMode()) {
                    return;
                }
                // 低盘的大小
                mJoystickBG = Bitmap.createScaledBitmap(mJoystickBG,
                        Math.round((float) (mOuterRadius * 2)),
                        Math.round((float) (mOuterRadius * 2)), true);
                // 滚珠大小
                mJoystickRock = Bitmap.createScaledBitmap(mJoystickRock,
                        Math.round((float) (mInnerRadius * 2)),
                        Math.round((float) (mInnerRadius * 2)), true);
            }
            super.layout(left, top, right, bottom);
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("滚珠布局layout异常：\t" + KCatchException.getExceptionMsg(e), true);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        try {
            super.draw(canvas);
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("滚珠draw()异常:\t" + KCatchException.getExceptionMsg(e), true);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        try {
            if (isInEditMode()) {
                return;
            }
            // 底盘(固定不变，画在画布中心)
            canvas.drawBitmap(mJoystickBG,
                    Math.round(mCenterX - mJoystickBG.getWidth() / 2 + paddingLeft),
                    Math.round(mCenterY - mJoystickBG.getHeight() / 2 + paddingLeft), mPaint);
            // 滚珠
            canvas.drawBitmap(mJoystickRock,
                    Math.round(mRockCenterX - mInnerRadius + paddingLeft),
                    Math.round(mRockCenterY - mInnerRadius + paddingLeft), mPaint);
            super.onDraw(canvas);
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("滚珠自我绘制异常：\t" + KCatchException.getExceptionMsg(e), true);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        try {
            float x = event.getX() - paddingLeft;
            float y = event.getY() - paddingLeft;

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    return false;
                case MotionEvent.ACTION_DOWN:
                    if (Math.sqrt(Math.pow((mCenterX - x), 2)
                            + Math.pow((mCenterY - y), 2)) > mOuterRadius) {
                        return false;
                    }
                    mIsMotion = true;
                    //事件传递
                    new MotionTask().start();
                    break;
                case MotionEvent.ACTION_MOVE:

                    if (Math.sqrt(Math.pow((mCenterX - x), 2)
                            + Math.pow((mCenterY - y), 2)) < mWidth / 4) {
                        mRockCenterX = x;
                        mRockCenterY = y;
                    } else {
                        double rad = getRad(mCenterX, mCenterY, x, y);
                        getXY(mCenterX, mCenterY, mWidth / 4, rad);
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    return false;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mRockCenterX = mCenterX;
                    mRockCenterY = mCenterY;
                    mIsMotion = false;
                    break;
            }

            invalidate();
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("滚珠事件分发异常：\t" + KCatchException.getExceptionMsg(e), true);
        }
        return true;
    }

    private double getRad(double px1, double py1, float px2, float py2) {
        double x = px2 - px1;
        double y = py2 - py1;
        double xx = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        double cosA = x / xx;
        double rad = Math.acos(cosA);
        return py2 < py1 ? -rad : rad;
    }

    private void getXY(double centerX, double centerY, double radius, double rad) {
        mRockCenterX = radius * Math.cos(rad) + mCenterX;
        mRockCenterY = radius * Math.sin(rad) + mCenterY;
    }

    private class MotionTask extends Thread {
        @Override
        public void run() {
            try {
                //震动音效
                KVibratorUtils.Vibrate((Activity) getContext(), 200);
                while (mIsMotion) {
                    if (null != mJoystickListener) {
                        float x = (float) (mRockCenterX - mCenterX) / (float) (mWidth / 4);
                        float y = (float) (mRockCenterY - mCenterY) / (float) (mWidth / 4);

                        x = x > 0.999 ? 1 : x;
                        x = x < -0.999 ? -1 : x;
                        y = y > 0.999 ? 1 : y;
                        y = y < -0.999 ? -1 : y;
                        x = x < 0.033 && x > -0.033 ? 0 : x;
                        y = y < 0.033 && y > -0.033 ? 0 : y;
                        //Log.e("test", "mRockCenterX:\t" + mRockCenterX + "\tmCenterX:\t" + mCenterX + "\tmWidth:\t" + mWidth + "\tx:\t" + x + "\ty:\t" + y);
                        MotionEvent event = MotionEvent.obtain(
                                SystemClock.uptimeMillis(),
                                SystemClock.uptimeMillis(),
                                MotionEvent.ACTION_MOVE, x, y, 0, 0, 0,
                                0, 0, 1, 0);//倒数第二个参数是 设备ID.设备id最好大于等于1,不用小于等于0.不灵
                        mJoystickListener.onJoystikMotionEvent(event);
                        event.recycle();
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //循环之外，恢复原状
                MotionEvent event2 = MotionEvent.obtain(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_MOVE, 0, 0, 0, 0, 0,
                        0, 0, 1, 0);
                mJoystickListener.onJoystikMotionEvent(event2);
                event2.recycle();
            } catch (Exception e) {
                KLoggerUtils.INSTANCE.e("滚珠异常：\t" + KCatchException.getExceptionMsg(e), true);
            }
        }
    }
}

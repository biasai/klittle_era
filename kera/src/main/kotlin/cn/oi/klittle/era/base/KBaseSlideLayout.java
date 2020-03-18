package cn.oi.klittle.era.base;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Scroller;

import cn.oi.klittle.era.R;
import cn.oi.klittle.era.comm.kpx;
import cn.oi.klittle.era.utils.KLoggerUtils;
import cn.oi.klittle.era.widget.compat.K3DragMotionEventWidget;
import cn.oi.klittle.era.widget.viewpager.KViewPager;

/**
 * fixme 左滑关闭Activity;修复了和KViewPager滑动冲突问题;修复了和K3DragMotionEventWidget拖到控件的冲突
 */
public class KBaseSlideLayout extends FrameLayout {
    // 页面边缘阴影的宽度默认值
    public static final int SHADOW_WIDTH = 16;
    private Activity mActivity;
    private Scroller mScroller;
    // 页面边缘的阴影图
    public Drawable mLeftShadow;
    public int mLeftShadowRes = R.drawable.kera_drawable_left_shadow;
    // 页面边缘阴影的宽度
    private int mShadowWidth;
    private int mInterceptDownX;
    private int mLastInterceptX;
    private int mLastInterceptY;
    private int mTouchDownX;
    private int mLastTouchX;
    private int mLastTouchY;
    private boolean isConsumed = false;

    /**
     * @param context
     * @param mLeftShadowRes 自定义左边阴影效果
     */
    public KBaseSlideLayout(Context context, int mLeftShadowRes) {
        this(context, null);
        if (mLeftShadowRes > 0 && mLeftShadowRes != this.mLeftShadowRes) {
            this.mLeftShadowRes = mLeftShadowRes;
            initView(context);//刷新阴影效果
        }
    }

    public KBaseSlideLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KBaseSlideLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mScroller = new Scroller(context);
        mLeftShadow = getResources().getDrawable(mLeftShadowRes);//滑动阴影效果
        int density = (int) getResources().getDisplayMetrics().density;
        mShadowWidth = SHADOW_WIDTH * density;
    }

    /**
     * 绑定Activity
     */
    public void bindActivity(Activity activity) {
        mActivity = activity;
        ViewGroup decorView = (ViewGroup) mActivity.getWindow().getDecorView();
        View child = decorView.getChildAt(0);
        decorView.removeView(child);
        addView(child);
        decorView.addView(this);
    }

    public int shadowSlidingWidth = -1;//fixme 控制有效滑动间距

    public void setShadowSlidingWidth(int shadowSlidingWidth) {
        this.shadowSlidingWidth = shadowSlidingWidth;
    }

    public int shadowSlidingHeight = kpx.INSTANCE.y(200);//fixme 控制有效滑动垂直间距

    public void setShadowSlidingHeight(int shadowSlidingHeight) {
        this.shadowSlidingHeight = shadowSlidingHeight;
    }

    public int shadowSlidingReboundWidth = -1;//fixme 控制有效反弹间距；根据手指释放时的位置决定回弹还是关闭

    public void setShadowSlidingReboundWidth(int shadowSlidingReboundWidth) {
        this.shadowSlidingReboundWidth = shadowSlidingReboundWidth;
    }

    public Boolean isEnableSliding = true;//fixme 是否开启滑动，新增变量方便进行手动控制。(亲测有效)

    private boolean isActionDown2 = false;//判断是否触发了按下事件

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnableSliding) {
            return false;//不拦截
        }
        boolean intercept = false;
//        int x = (int) ev.getX();
//        int y = (int) ev.getY();
        //fixme 使用Raw
        int x = (int) ev.getRawX();
        int y = (int) ev.getRawY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                intercept = false;
                mInterceptDownX = x;
                mLastInterceptX = x;
                mLastInterceptY = y;
                isActionDown2 = true;
                KViewPager.Companion.setViewPagerMotionEventing(false);//fixme kviewpager没有滑动
                K3DragMotionEventWidget.Companion.setDragMotionEventing(false);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isActionDown2) {//fixme 防止按下事件，没有触发。
                    mInterceptDownX = x;
                    mLastInterceptX = x;
                    mLastInterceptY = y;
                    isActionDown2 = true;
                }
                int deltaX = x - mLastInterceptX;
                int deltaY = y - mLastInterceptY;
                if (shadowSlidingWidth < 0) {
                    shadowSlidingWidth = getWidth() / 10;//fixme 控制有效滑动间距
                }
                // 手指处于屏幕边缘，且横向滑动距离大于纵向滑动距离时，拦截事件
                if (mInterceptDownX < (shadowSlidingWidth) && Math.abs(deltaX) > Math.abs(deltaY) && deltaX > 5) {
                    if (KViewPager.Companion.isMotinEventing() || K3DragMotionEventWidget.Companion.isDrgMotinEventing()) {//fixme 防止和KViewPager滑动冲突。
                        intercept = false;//不拦截
                    } else {
                        intercept = true;//事件拦截
                    }
                } else {
                    intercept = false;
                }
                mLastInterceptX = x;
                mLastInterceptY = y;
                break;
            case MotionEvent.ACTION_UP:
                intercept = false;
                isActionDown2 = false;
                mInterceptDownX = mLastInterceptX = mLastInterceptY = 0;
                KViewPager.Companion.setViewPagerMotionEventing(false);
                K3DragMotionEventWidget.Companion.setDragMotionEventing(false);
                break;
        }
        return intercept;
    }

    private boolean isActionDown = false;//判断是否触发了按下事件

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            if (!isEnableSliding) {
                return super.onTouchEvent(ev);//不拦截
            }
            //        int x = (int) ev.getX();
//        int y = (int) ev.getY();
            //fixme 使用Raw
            int x = (int) ev.getRawX();
            int y = (int) ev.getRawY();
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN://fixme 如果点击区域有点击事件，这里就不会触发按下。
                    mTouchDownX = x;
                    mLastTouchX = x;
                    mLastTouchY = y;
                    isConsumed = false;
                    isActionDown = true;
                    //Log.e("test","x:\t"+x+"\ty:\t"+y);
                    //Log.e("test","按下");
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!isActionDown) {//fixme 防止按下事件，没有触发。
                        mTouchDownX = x;
                        mLastTouchX = x;
                        mLastTouchY = y;
                        isActionDown = true;
                    }
                    int deltaX = x - mLastTouchX;
                    int deltaY = y - mLastTouchY;
                    if (mLastTouchX <= 0) {
                        mLastTouchX = (int) ev.getRawX();//fixme 防止数据异常
                    }
                    if (mLastTouchY <= 0) {
                        mLastTouchY = (int) ev.getRawY();
                    }
                    if (shadowSlidingWidth < 0) {
                        shadowSlidingWidth = getWidth() / 10;//fixme 控制有效滑动间距
                    }
                    //Log.e("test", "mLastTouchX:\t" + mLastTouchX + "\tgetRawX():\t" + ev.getRawX() + "\tdeltaX:\t" + deltaX + "\t" + kpx.INSTANCE.screenWidth(false));
                    //Log.e("test","mInterceptDownX:\t"+mInterceptDownX+"\tshadowSlidingWidth:\t"+shadowSlidingWidth);
                    if (!isConsumed && mTouchDownX < (shadowSlidingWidth) && Math.abs(deltaX) > Math.abs(deltaY) && deltaX > 5) {//fixme 水平滑动限制，deltaX>5 足够了。一般点击事件的偏移都不超过3
                        if (mLastTouchY > shadowSlidingHeight) {//fixme 垂直滑动限制，触摸Y坐标大于这个高度才有效。
                            isConsumed = true;
                        }
                    }
                    if (isConsumed) {
//                    int rightMovedX = mLastTouchX - (int) ev.getX();
                        int rightMovedX = mLastTouchX - (int) ev.getRawX();
                        // 左侧即将滑出屏幕
                        if (getScrollX() + rightMovedX >= 0) {
                            scrollTo(0, 0);
                        } else {
                            scrollBy(rightMovedX, 0);
                        }
                    }
                    mLastTouchX = x;
                    mLastTouchY = y;
                    break;
                case MotionEvent.ACTION_UP://fixme 这里一定会调用
                    isConsumed = false;
                    isActionDown = false;
                    mTouchDownX = mLastTouchX = mLastTouchY = 0;
                    if (shadowSlidingReboundWidth < 0) {
                        shadowSlidingReboundWidth = getWidth() / 4;//fixme 控制有效反弹间距
                    }
                    // 根据手指释放时的位置决定回弹还是关闭
                    if (-getScrollX() < shadowSlidingReboundWidth) {
                        scrollBack();
                    } else {
                        scrollClose();
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 滑动返回
     */
    private void scrollBack() {
        int startX = getScrollX();
        int dx = -getScrollX();
        mScroller.startScroll(startX, 0, dx, 0, 300);
        invalidate();
    }

    /**
     * 滑动关闭(关闭当前Activity)
     */
    private void scrollClose() {
        int startX = getScrollX();
        //int dx = -getScrollX() - getWidth() - mShadowWidth;
        int dx = -getScrollX() - getWidth();
        mScroller.startScroll(startX, 0, dx, 0, 300);
        invalidate();
    }

    @Override
    public void computeScroll() {
        try {
            if (mScroller.computeScrollOffset()) {
                scrollTo(mScroller.getCurrX(), 0);
                postInvalidate();
            } else if (-getScrollX() >= getWidth()) {
                mActivity.finish();
                //activity调用了finishi()里的super.finish()；isFinishing才会为true
                if (mActivity.isFinishing()) {
                    mActivity.overridePendingTransition(0, 0);//滑动关闭原始动画效果(finish后面调用的会之前的动画设置，亲测有效!)。
                } else {
                    //未关闭，界面恢复。亲测正常有效。
                    scrollBack();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        try {
            drawPreViousBitmap(canvas);
            super.dispatchDraw(canvas);
            drawShadow(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 绘制上一个Activity的位图，实现视觉差效果。如果不绘制，就不会有微信那样位图移动的效果。
     *
     * @param canvas
     */
    private void drawPreViousBitmap(Canvas canvas) {
        Bitmap bitmap = KBaseApplication.getInstance().previousBitmap;
        if (bitmap != null && !bitmap.isRecycled()) {
            int moveWidth = (int) (getWidth() / 1.6);//fixme 视差滑动宽度（1.6可以了，微信差不多就是这个值）
            int startX = -getWidth();//起点滑动
            int x = startX + moveWidth;
            int y = 0;
            float p = (float) Math.abs(getScrollX()) / (float) getWidth();
            x = (int) (x - moveWidth * p);
            if (x <= -getWidth()) {
                x = -getWidth();
            }
            canvas.drawBitmap(bitmap, x, y, null);
            //Log.e("test","x:\t"+x+"\t"+getScrollX());
        }
    }

    int mScrollX = 0;
    int mWidth2 = 0;

    /**
     * 绘制边缘的阴影
     */
    private void drawShadow(Canvas canvas) {
        mLeftShadow.setBounds(0, 0, mShadowWidth, getHeight());
        canvas.save();
        canvas.translate(-mShadowWidth, 0);
        mLeftShadow.draw(canvas);
        mScrollX = Math.abs(getScrollX());
        mWidth2 = getWidth() / 2;
        if (mScrollX > mWidth2) {
            float alpha = (float) (mScrollX - mWidth2) / ((float) mWidth2);
            alpha = 255 - alpha * 255;
            //Log.e("test","透明度:\t"+alpha);
            mLeftShadow.setAlpha((int) alpha);//给边缘阴影添加渐变效果。
        } else {
            mLeftShadow.setAlpha(255);
        }
        canvas.restore();
    }
}


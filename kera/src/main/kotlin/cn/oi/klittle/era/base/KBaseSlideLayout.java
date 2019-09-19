package cn.oi.klittle.era.base;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Scroller;

import cn.oi.klittle.era.R;


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

    public boolean isEnableSlingBp = true;//是否开启位图视觉差效果

    /**
     * @param context
     * @param isEnableSlingBp 是否开启位图视觉差效果
     * @param mLeftShadowRes  自定义左边阴影效果
     */
    public KBaseSlideLayout(Context context, boolean isEnableSlingBp, int mLeftShadowRes) {
        this(context, null);
        this.isEnableSlingBp = isEnableSlingBp;
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

    public int shadowSlidingReboundWidth = -1;//fixme 控制有效反弹间距；根据手指释放时的位置决定回弹还是关闭

    public void setShadowSlidingReboundWidth(int shadowSlidingReboundWidth) {
        this.shadowSlidingReboundWidth = shadowSlidingReboundWidth;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
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
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastInterceptX;
                int deltaY = y - mLastInterceptY;
                if (shadowSlidingWidth < 0) {
                    shadowSlidingWidth = getWidth() / 10;//fixme 控制有效滑动间距
                }
                // 手指处于屏幕边缘，且横向滑动距离大于纵向滑动距离时，拦截事件
                if (mInterceptDownX < (shadowSlidingWidth) && Math.abs(deltaX) > Math.abs(deltaY)) {
                    intercept = true;
                } else {
                    intercept = false;
                }
                mLastInterceptX = x;
                mLastInterceptY = y;
                break;
            case MotionEvent.ACTION_UP:
                intercept = false;
                mInterceptDownX = mLastInterceptX = mLastInterceptY = 0;
                break;
        }
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            //        int x = (int) ev.getX();
//        int y = (int) ev.getY();
            //fixme 使用Raw
            int x = (int) ev.getRawX();
            int y = (int) ev.getRawY();
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTouchDownX = x;
                    mLastTouchX = x;
                    mLastTouchY = y;
                    isConsumed = false;
                    break;
                case MotionEvent.ACTION_MOVE:
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
                    //Log.e("test", "mLastTouchX:\t" + mLastTouchX + "\tgetRawX():\t" + ev.getRawX() + "\tdeltaX:\t" + deltaX);
                    //Log.e("test","mInterceptDownX:\t"+mInterceptDownX+"\tshadowSlidingWidth:\t"+shadowSlidingWidth);
                    if (!isConsumed && mTouchDownX < (shadowSlidingWidth) && Math.abs(deltaX) > Math.abs(deltaY)) {
                        isConsumed = true;
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
                case MotionEvent.ACTION_UP:
                    isConsumed = false;
                    mTouchDownX = mLastTouchX = mLastTouchY = 0;
                    if (shadowSlidingReboundWidth < 0) {
                        shadowSlidingReboundWidth = getWidth() / 2;//fixme 控制有效反弹间距
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
     * 滑动关闭
     */
    private void scrollClose() {
        int startX = getScrollX();
        int dx = -getScrollX() - getWidth() - mShadowWidth;
        mScroller.startScroll(startX, 0, dx, 0, 300);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            postInvalidate();
        } else if (-getScrollX() >= getWidth()) {
            mActivity.overridePendingTransition(0, 0);//滑动关闭原始动画效果（不一定有效果）。
            if (mActivity != null && !mActivity.isFinishing()) {
                if (mActivity instanceof KBaseActivity) {
                }
            }
            mActivity.finish();
            //mActivity.overridePendingTransition(R.anim.kera_show, R.anim.kera_hidden);
            mActivity.overridePendingTransition(0, 0);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (isEnableSlingBp) {
            drawPreViousBitmap(canvas);
        }
        super.dispatchDraw(canvas);
        drawShadow(canvas);
    }

    /**
     * 绘制上一个Activity的位图，实现视觉差效果。如果不绘制，就不会有微信那样位图移动的效果。
     *
     * @param canvas
     */
    private void drawPreViousBitmap(Canvas canvas) {
        Bitmap bitmap = KBaseApplication.getInstance().previousBitmap;
        //KLoggerUtils.e("test","上一個Activity圖片:\t"+bitmap);
        if (bitmap != null && !bitmap.isRecycled() && getScrollX() > -getWidth()) {
            int moveWidth = (int) (getWidth() / 1.6);//fixme 视差滑动宽度（1.6可以了，微信差不多就是这个值）
            int startX = -getWidth();//起点滑动
            int x = startX + moveWidth;
            int y = 0;
            float p = (float) Math.abs(getScrollX()) / (float) getWidth();
            x = (int) (x - moveWidth * p);
            canvas.drawBitmap(bitmap, x, y, null);
        }
    }

    /**
     * 绘制边缘的阴影
     */
    private void drawShadow(Canvas canvas) {
        mLeftShadow.setBounds(0, 0, mShadowWidth, getHeight());
        canvas.save();
        canvas.translate(-mShadowWidth, 0);
        mLeftShadow.draw(canvas);
        canvas.restore();
    }
}


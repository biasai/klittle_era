package cn.oi.klittle.era.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.HashMap;
import java.util.Map;

import cn.oi.klittle.era.base.KBaseView;
import cn.oi.klittle.era.utils.KLoggerUtils;
import cn.oi.klittle.era.widget.compat.KMyEditText;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * fixme 弹性ScrollView【滑动原理，对scrollview里面的第一个View进行位置上下偏移滑动。】，没有滑动条哦。
 * fixme 解决滑动冲突问题：recyclerView?.hasFixedSize();recyclerView?.isNestedScrollingEnabled=false;（只有NestedScrollView可以滑动。）
 * fixme isChildScoll = false;//fixme  解决子View的滑动冲突(子View如果在滑动，该属性设置为true，ScrollView就不会再滑动。)
 * Created by 彭治铭 on 2018/4/25.
 */
public class KBounceScrollView extends NestedScrollView {

    public int mContentHeight = getHeight();//fixme 获取scrollView内容的真实高度。在dispatchTouchEvent()方法里，按下时赋值。

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

    }

    public static Boolean isChildScoll = false;//fixme  解决子View的滑动冲突(子View如果在滑动，该属性设置为true，ScrollView就不会再滑动。)

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        if (isChildScoll)
            return false;
        else
            return super.onTouchEvent(arg0);
    }

    public boolean isBanChildScroll = true;//fixme 是否禁止子控件的垂直滑动，如RecycleView;
    private int mTouchSlop;
    private int downY2;

    //请求允许打断滑动或不允许打断该组件的滑动事件
    //true会拦截子View的事件，false不会。
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        try {
            //Log.e("test", "顶部Y:\t" + inner.getTop());
            if (inner != null && inner.getTop() != inerTop) {
                return true;//子View在移动的时候，拦截对子View的事件处理。
            }
            if (isChildScoll) {
                getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            } else {
                if (!isHorizon && isBanChildScroll) {
                    //竖屏滑动
                    //==开始；这一段解决了ScrollView和RecycleView的滑动冲突。
                    // fixme 本质是RecycleView的内部滑动被禁止，只能滑动ScrollView;所以RecycleView的高度要是设置成wrapContent自适应。防止item显示不全。
                    // openDownAnime = false openUpAnime = false 有RecyclerView时，不要开启上拉和下拉，效果不好。
                    // RecycleView列表item太长了，如条目大于100了。就不要使用了。建议 isBanChildScroll=false
                    mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            downY2 = (int) e.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            int moveY = (int) e.getRawY();
                            if (Math.abs(moveY - downY2) > mTouchSlop) {
                                return true;
                            }
                    }
                    //===结束
                }
                return super.onInterceptTouchEvent(e);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return false;
    }

    private View inner;// 孩子View
    private float downY;// 点击时y坐标
    private Rect normal = new Rect();// 矩形(这里只是个形式，只是用于判断是否需要动画.)

    private boolean isCount = false;// 是否开始计算
    private float lastX = 0;
    private float lastY = 0;
    private float currentX = 0;
    private float currentY = 0;
    private float distanceX = 0;
    private float distanceY = 0;
    private boolean upDownSlide = false; //判断上下滑动的flag

    public KBounceScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFillViewport(true);
        setOverScrollMode(OVER_SCROLL_NEVER);//设置滑动到边缘时无效果模式
        setVerticalScrollBarEnabled(false);//滚动条隐藏
    }

    public KBounceScrollView(@NonNull Context context) {
        super(context);
        //fixme 默认，内部容器的宽和高填充ScrollView的宽和高。即一样大小。
        setFillViewport(true);
        setOverScrollMode(OVER_SCROLL_NEVER);//fixme 设置滑动到边缘时无效果模式，不然RecyclerView的滑动边缘效果可能会无法隐藏。
        setVerticalScrollBarEnabled(false);//滚动条隐藏
    }

    /***
     * 根据 XML 生成视图工作完成.该函数在生成视图的最后调用，在所有子视图添加完之后. 即使子类覆盖了 onFinishInflate
     * 方法，也应该调用父类的方法，使该方法得以执行.
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 0) {
            inner = getChildAt(0);
        }
    }


    public boolean openUpAnime = true;//fixme 上滑弹性动画开启。
    public boolean openDownAnime = true;//fixme 下滑弹性动画开启
    public int maxMoveHeightDrop_Down = 0;//fixme 最大下拉高度(0,默认就是整个控件的高度。)
    public int maxMoveHeightDrop_Up = 0;//fixme 最大上拉高度

    //解决嵌套滑动冲突。
    @Override
    public boolean startNestedScroll(int axes) {
        //子View滑动时，禁止弹性动画。
        isDownAnime = false;
        isUpAnime = false;
        return super.startNestedScroll(axes);
    }


    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        //Log.e("test", "dyConsumed:\t" + dyConsumed + "\tdyUnconsumed:\t" + dyUnconsumed);
        if (dyConsumed == 0) {
            isDownAnime = true;
            isUpAnime = true;
        } else {
            isDownAnime = false;
            isUpAnime = false;
        }
        if (dyConsumed == 0 && dyUnconsumed == 0) {
            isDownAnime = false;
            isUpAnime = false;
        }
        return super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public void stopNestedScroll() {
        super.stopNestedScroll();
        //结束时，开启弹性滑动。
        isDownAnime = true;
        isUpAnime = true;
    }

    public boolean isSlidingDown = false;//是否下滑。true 下滑（手指向下移动），false上滑

    private boolean isDownAnime = true;
    private boolean isUpAnime = true;

    boolean isHorizon = false;//是否属于横屏滑动(水平滑动，不具备弹性效果)
    boolean isfirst = true;//是否为第一次滑动。
    byte isfirstDirection = 0;//第一次滑动方向,0初始化，没有方向，1 横屏方法，2 竖屏方向。

    int inerTop = 0;//记录原始的顶部高度。

    Map map = new HashMap<Integer, MPoint>();

    class MPoint {
        public float y;// 点击时y坐标
        public float preY = y;// 按下时的y坐标
        public float nowY = y;// 时时y坐标
        public int deltaY = 0;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                isChildScoll = false;//fixme 按下时，滑动会自动恢复。
                mContentHeight = getHeight();
                if (getChildCount() > 0) {
                    int hh = getChildAt(0).getHeight();
                    if (hh > mContentHeight) {
                        mContentHeight = hh;
                    }
                }
                break;
        }
        if (!isChildScoll) {
            if (inner == null) {
                if (getChildCount() > 0) {
                    inner = getChildAt(0);
                } else {
                    return super.dispatchTouchEvent(ev);
                }
            }
            currentX = ev.getX();
            currentY = ev.getY();
            switch (ev.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    map.put(ev.getPointerId(ev.getActionIndex()), new MPoint());
                    //Log.e("test", "按下:\t" + ev.getPointerCount());
                    inerTop = inner.getTop();//原始顶部，不一定都是0，所以要记录一下。
                    isfirst = true;
                    isfirstDirection = 0;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN://第二个手指按下
                    map.put(ev.getPointerId(ev.getActionIndex()), new MPoint());
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    map.remove(ev.getPointerId(ev.getActionIndex()));
                    break;
                case MotionEvent.ACTION_MOVE:
                    distanceX = currentX - lastX;
                    distanceY = currentY - lastY;
//                Log.e("test", "x滑动:\t" + distanceX + "\ty滑动:\t" + distanceY);
//                Log.e("test", "currentX:\t" + currentX + "\tcurrentY:\t" + currentY);
                    if (distanceY > 0) {
                        isSlidingDown = true;//下滑大于0
                    } else {
                        isSlidingDown = false;//上滑小于0
                    }
                    if ((Math.abs(distanceX) < Math.abs(distanceY)) && Math.abs(distanceY) > 12) {
                        if (isfirstDirection == 0) {
                            isfirstDirection = 2;//上下方向
                        }
                        upDownSlide = true;//表示上下滑动
                    } else {
                        if (isfirstDirection == 0 && (Math.abs(distanceX) > Math.abs(distanceY))) {
                            isfirstDirection = 1;//水平滑动方向
                        }
                    }
                    if (isfirst) {
                        isHorizon = !upDownSlide;//横屏滑动(水平滑动，不具备弹性效果)
                        isfirst = false;
                    }
                    if (isfirstDirection == 2) {
                        isHorizon = false;//上下方向
                    } else if (isfirstDirection == 1) {
                        isHorizon = true;//水平方向
                    }
                    //Log.e("test", "x:\t" + Math.abs(distanceX) + "\ty:\t" + Math.abs(distanceY) + "\tisHorizon:\t" + isHorizon);
                    //Log.e("test","isSlidingDown:\t"+isSlidingDown+"\tisDownAnime:\t"+isDownAnime+"\tisHorizon:\t"+isHorizon+"\tupDownSlide:\t"+upDownSlide+"\tinner:\t"+inner+"\topenDownAnime:\t"+openDownAnime);
                    if (isSlidingDown && isDownAnime && !isHorizon) {
                        if (upDownSlide && inner != null && openDownAnime) {
                            commOnTouchEvent(ev);//fixme 开启下拉弹性
                        }
                    } else if (!isSlidingDown && isUpAnime && !isHorizon) {
                        if (upDownSlide && inner != null && openUpAnime) {
                            commOnTouchEvent(ev);//fixme 开启上拉弹性
                        }
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    //Log.e("test", "离开");
                    //以防万一，恢复原始状态
                    isDownAnime = true;
                    isUpAnime = true;
                    if (upDownSlide && inner != null) {
                        commOnTouchEvent(ev);
                    }
                    map.remove(ev.getPointerId(ev.getActionIndex()));
                    map.clear();
                    break;
                default:
                    break;
            }
            lastX = currentX;
            lastY = currentY;
        }
        return super.dispatchTouchEvent(ev);
    }

    boolean bAnime = false;//是否开始动画

    /***
     * 触摸事件
     *
     * @param ev
     */
    public void commOnTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                // 手指松开.
                if (isNeedAnimation()) {
                    animation();
                    isCount = false;
                }
                clear0();
                break;
            case MotionEvent.ACTION_MOVE:
                final float preY = downY;// 按下时的y坐标
                float nowY = ev.getY();// 时时y坐标
                int deltaY = (int) (preY - nowY);// 滑动距离

                if (ev.getPointerCount() > 1) {
                    //多指滑动
                    for (int i = 0; i < ev.getPointerCount(); i++) {
                        MPoint m = (MPoint) map.get(ev.getPointerId(i));
                        m.preY = m.y;
                        m.nowY = ev.getY(i);
                        m.deltaY = (int) (m.preY - m.nowY);
                        if (i == 0) {
                            deltaY = (int) m.deltaY;
                        } else {
                            //移动距离取最大的
                            if (Math.abs(deltaY) < Math.abs(m.deltaY)) {
                                deltaY = (int) m.deltaY;
                            }
                        }
                        m.y = m.nowY;
                    }
                }

                if (!isCount) {
                    deltaY = 0; // 在这里要归0.
                }
                //Log.e("test","按下y：\t"+preY+"\tnowY：\t"+nowY+"\t距离:\t"+deltaY+"\t是否移动:\t"+isNeedMove());
                downY = nowY;
                //Log.e("test", "deltaY滑动:\t" + deltaY);
                // 当滚动到最上或者最下时就不会再滚动，这时移动布局/速度大于了200都是异常。不做移动处理
                //KLoggerUtils.INSTANCE.e("isNeedMove():\t" + isNeedMove()+"\ttop:\t"+inner.getTop());
                if ((isNeedMove()) && Math.abs(deltaY) < 200) {
                    // 初始化头部矩形
                    if (normal.isEmpty() || normal.right <= 0 || normal.bottom <= 0 || (normal.bottom - normal.top) != mContentHeight) {
                        //fixme 保存正常的布局位置
                        //KLoggerUtils.INSTANCE.e("left2:\t"+inner.getLeft()+"\ttop2:\t"+inner.getTop()+"\tright2:\t"+inner.getRight()+"\tbottom2:\t"+inner.getBottom());
                        normal.set(inner.getLeft(), inner.getTop(),
                                inner.getRight(), inner.getBottom());
                    }
                    // 移动布局
                    int top = inner.getTop() - deltaY / 2;
                    int bottom = inner.getBottom() - deltaY / 2;
                    //移动最大不能超过总高度的一半
                    if (maxMoveHeightDrop_Down <= 0) {
                        //maxMoveHeightDrop_Down = getHeight() / 2;//最大下拉高度
                        maxMoveHeightDrop_Down = getHeight();
                    }
                    if (maxMoveHeightDrop_Up <= 0) {
                        //maxMoveHeightDrop_Up = getHeight() / 2;//最大上拉拉高度
                        maxMoveHeightDrop_Up = getHeight();
                    }
                    if (top > maxMoveHeightDrop_Down) {
                        top = maxMoveHeightDrop_Down;
                    }
                    //fixme getHeight() 改为mContentHeight
                    if (bottom < (mContentHeight - maxMoveHeightDrop_Up)) {
                        bottom = (mContentHeight - maxMoveHeightDrop_Up);
                    }
                    currentTop = top;
                    //KLoggerUtils.INSTANCE.e("top:\t" + top + "\tbottom:\t" + bottom+"\toffset:\t"+(bottom-top)+"\tmContentHeight:\t"+mContentHeight+"\tscrolly:\t"+getScrollY());
                    if (top <= maxMoveHeightDrop_Down && bottom >= (mContentHeight - maxMoveHeightDrop_Up) && !bAnime) {
                        layout2(inner.getLeft(), top,
                                inner.getRight(), bottom);
                    } else {
                        animation();//恢复原状
                    }
                }
                isCount = true;
                break;

            default:
                break;
        }
    }

    int currentTop = 0;//当前顶部值。

    /***
     * 回缩动画
     */
    public void animation() {
        if (!bAnime) {
            bAnime = true;//开始动画
//            int top = inner.getTop();
            // 开启移动动画
//            TranslateAnimation ta = new TranslateAnimation(0, 0, inner.getTop(),
//                    normal.top);
//            ta.setDuration(200);
//            ta.setAnimationListener(new Animation.AnimationListener() {
//                @Override
//                public void onAnimationStart(Animation animation) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animation animation) {
//                    bAnime = false;//动画结束
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation animation) {
//                    bAnime = false;
//                }
//            });
//            inner.startAnimation(ta);
            // 设置回到正常的布局位置
            //layout2(normal.left, normal.top, normal.right, normal.bottom);
            KBaseView.Companion.ofInt(this, "currentTop", 0, 100, new int[]{currentTop, 0}, true, new Function1<Integer, Unit>() {
                @Override
                public Unit invoke(Integer values) {
                    layout2(normal.left, values, normal.right, values + (normal.bottom - normal.top));
                    if (values == 0) {
                        bAnime = false;//动画结束
                    }
                    return null;
                }
            });
            //normal.setEmpty();//fixme 不要清空，防止布局异常。
        }
    }

    int topM = -30000;
    MarginLayoutParams layoutParams = null;
    int tt = 0;

    public Boolean isHasEditTextView = false;//fixme 是否有输入文本框;默认没有；如果有的话；需要手动设置true。

    public void layout2(int l, int t, int r, int b) {
        //inner.layout(l, t, r, b);//原始方法
        //解决ViewPager滑动时无效问题。所以使用外补丁。
        if (inner != null) {
            if (isHasEditTextView) {
                try {
                    //fixme 解决与输入文本框的冲突。29是android 10;10.0不需要聚焦。聚焦了反而可能会弹出软键盘。
                    if (Build.VERSION.SDK_INT < 29) {
                        requestFocus();//fixme 聚焦
                        requestFocusFromTouch();
                    }
                    KMyEditText.Companion.hideSoftKeyboard(getContext(), this);//隐藏软键盘（防止软键盘是不是的冒出来）
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (layoutParams == null) {
                layoutParams = (MarginLayoutParams) inner.getLayoutParams();
            }
            if (topM <= -30000) {
                topM = layoutParams.topMargin;//保存原始顶部外补丁。
            }

            tt = t + topM;//补丁为负数同样有效。4.2的系统外补丁不支持负数。
            //KLoggerUtils.INSTANCE.e("left:\t" + l + "\ttop:\t" + t + "\tright:\t" + r + "\tbottom:\t" + b);
            if (true || Build.VERSION.SDK_INT <= 17 && tt < 0) {
                //fixme 建议使用这个。效果好，有保证。
                inner.layout(l, t, r, b);//fixme viewpager滑动时，会无效;在androidx上面还未验证。
            } else {
                //fixme 这个在androidx上面有Bug;不建议使用。效果不好。上拉会无效。
                layoutParams.setMargins((int) (layoutParams.leftMargin), tt, (int) (layoutParams.rightMargin), (int) (layoutParams.bottomMargin));
                inner.requestLayout();
            }
            //layoutParams.setMargins((int) (layoutParams.leftMargin), tt, (int) (layoutParams.rightMargin), (int) (layoutParams.bottomMargin));
            //Log.e("test", "t:\t"+t+"\ttt:\t" + tt);
            //下拉
            if (t >= 0) {
                onDropDownAutoMatrixBg(t);
                if (dropDown != null) {
                    dropDown.onDown(t);
                }
            }
            //上拉
            if (t <= 0) {
                if (dropUp != null) {
                    dropUp.onUp(Math.abs(t));
                }
            }
        }
    }

    // 是否需要开启动画
    public boolean isNeedAnimation() {
        return !normal.isEmpty();
    }

    /***
     * 是否需要移动布局 inner.getMeasuredHeight():获取的是控件的总高度
     *
     * getHeight()：获取的是屏幕的高度
     *
     * @return
     */
    public boolean isNeedMove() {
        int offset = inner.getMeasuredHeight() - getHeight();//fixme 这个判断没有问题。
        int scrollY = getScrollY();
        //KLoggerUtils.INSTANCE.e("scrollY:\t"+scrollY+"\toffset:\t"+offset+"\tinner.getMeasuredHeight():\t"+inner.getMeasuredHeight()+"\tgetHeight():\t"+getHeight()+"\tmContentHeight:\t"+mContentHeight);
        // 0是顶部，后面那个是底部
        if (scrollY == 0 || scrollY == offset) {
            return true;
        }
        return false;
    }

    private void clear0() {
        lastX = 0;
        lastY = 0;
        distanceX = 0;
        distanceY = 0;
        upDownSlide = false;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (onScrollChanged != null) {
            onScrollChanged.onScrollChanged(l, t, oldl, oldt);
        }
    }

    private OnScrollChanged onScrollChanged;

    //fixme 接口，监听Scroll的滑动状态改变。滑动坐标。
    public void onScrollChanged(OnScrollChanged onScrollChanged) {
        this.onScrollChanged = onScrollChanged;
    }

    public interface OnScrollChanged {
        //x,y是当前scroll滑动的坐标,oldx,oldy是记录上一次的滑动坐标。
        void onScrollChanged(int x, int y, int oldx, int oldy);
    }
//    调用案例。（Kotlin自带高阶函数）
//    onScrollChanged { x, y, oldx, oldy ->
//        //y就是当前滑动的y坐标值，oldy就是上一次滑动的坐标值
//        if (y > oldy) {
//            //向下滚动（下面的内容显示出来。）
//        } else if (y < oldy) {
//            //向上滚动（上面的内容显示出来）
//        }
//    }

    //图片下拉监听。
    protected void onDropDownAutoMatrixBg(int distance) {
    }

    private DropDown dropDown;

    //fixme 接口，下拉监听
    public void dropDown(DropDown dropDown) {
        this.dropDown = dropDown;
    }

    public interface DropDown {
        //distance是当前下拉的值，即下拉的距离。是正数。
        void onDown(int distance);
    }

    private DropUp dropUp;

    //fixme 接口，上拉监听
    public void dropUp(DropUp dropUp) {
        this.dropUp = dropUp;
    }

    public interface DropUp {
        //distance是当前上拉的值，即上拉的距离。是正数。距离都是正数。
        void onUp(int distance);
    }

}

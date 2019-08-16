package cn.oi.klittle.era.widget.viewpager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * 垂直ViewPager，和 ViewPager一样的用法。参考地址：https://www.jianshu.com/p/f1163d1161a2
 * fixme 不要继承KViewPager;就直接继承ViewPager；保证原生态。
 */
public class VerticalViewPager extends ViewPager {

    public VerticalViewPager(ViewGroup viewGroup) {
        super(viewGroup.getContext());
        myInit();
        viewGroup.addView(this);//直接添加进去,省去addView(view)
    }

    public VerticalViewPager(Context context) {
        super(context);
        myInit();
    }

    public VerticalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        myInit();
    }

    //fixme 其实就是使用setPageTransformer来改变每一个item的显示效果，加上setOverScrollMode(OVER_SCROLL_NEVER)
    private void myInit() {
        // The majority of the magic happens here
        //fixme 设置setPageTransformer来实现竖直滑动
        setPageTransformer(true, new VerticalPageTransformer());
        // The easiest way to get rid of the overscroll drawing that happens on the left and right
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    private class VerticalPageTransformer implements ViewPager.PageTransformer {

        @Override
        public void transformPage(View view, float position) {

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                view.setAlpha(1);

                // Counteract the default slide transition
                view.setTranslationX(view.getWidth() * -position);

                //set Y position to swipe in from top
                float yPosition = position * view.getHeight();
                view.setTranslationY(yPosition);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

    /**
     * Swaps the X and Y coordinates of your touch event.
     */
    private MotionEvent swapXY(MotionEvent ev) {
        float width = getWidth();
        float height = getHeight();

        float newX = (ev.getY() / height) * width;
        float newY = (ev.getX() / width) * height;

        ev.setLocation(newX, newY);

        return ev;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = super.onInterceptTouchEvent(swapXY(ev));
        swapXY(ev); // return touch coordinates to original reference frame for any child views
        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(swapXY(ev));
    }
}
package cn.oi.klittle.era.utils;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import cn.oi.klittle.era.comm.kpx;

/**
 * Created by 彭治铭 on 2017/10/9.
 */

public class KPopuWindowUtils {
    private KPopuWindowUtils() {
    }

    private static KPopuWindowUtils popuWindowUtils;

    public static KPopuWindowUtils getInstance() {
        if (popuWindowUtils == null) {
            popuWindowUtils = new KPopuWindowUtils();
        }
        return popuWindowUtils;
    }

//    View contentView = LayoutInflater.from(MainActivity.this).inflate(R.layout.popuplayout, null); 一般来说布局可以通过xml文件获取。
//    如果是手动实例化出一个View,
//    view=new View(activity){
//            @Override
//            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//                //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//                设置Params不起作用时，则必须通过setMeasuredDimension来控制大小。
//                setMeasuredDimension(UtilProportion.getInstance(activity).adapterInt(260),UtilProportion.getInstance(activity).adapterInt(177));
//            }
//        };
//        ViewGroup.LayoutParams params=new ViewGroup.LayoutParams(UtilProportion.getInstance(activity).adapterInt(260),UtilProportion.getInstance(activity).adapterInt(177));
//        view.setLayoutParams(params);//虽然此时params无法控制大小。但是还是必不可少。必须添加一个params。因为默认是为空的。

    /**
     * @param contentView popuwindow布局
     * @param styleAnim   动画
     * @return 返回 PopupWindow，位置显示自己去调用showAtLocation(),showAsDropDown(),自己决定。
     */
    public PopupWindow showPopuWindow(final View contentView, int styleAnim,int width,int height) {
        //AlertDialog是非阻塞线程的，而PopupWindow是阻塞线程的
        PopupWindow popupWindow = new PopupWindow() {
            @Override
            public void showAsDropDown(View anchor) {
                //解决7.0显示不正确的问题，7.0以下和7.1都没有问题。
                //fixme 防止异常，popuwindow设置成固定高度。
                //if (Build.VERSION.SDK_INT == 24) {
                Rect rect = new Rect();
                anchor.getGlobalVisibleRect(rect);
                int h = anchor.getResources().getDisplayMetrics().heightPixels - rect.bottom;
                int off = kpx.INSTANCE.y(10);//与屏幕底部边缘，留10像素的空隙
                if (h > off) {
                    h -= off;
                }
                setHeight(h);//设置popuwind的高度（当前view到屏幕底部的距离），这个是popuwindow的高度，不是容器contentView的高度。确是contentView的最大高度。
                //}
                //fixme popuwindow的高度不等于contentView的高度。确是contentView的最大高度。
                //popwindow的高度必须在显示之前设置才有效。
                super.showAsDropDown(anchor);
            }

            @Override
            public void showAsDropDown(View anchor, int xoff, int yoff) {
                Rect rect = new Rect();
                anchor.getGlobalVisibleRect(rect);
                int h = anchor.getResources().getDisplayMetrics().heightPixels - rect.bottom - yoff;
                int off = kpx.INSTANCE.y(10);
                if (h > off) {
                    h -= off;
                }
                setHeight(h);
                super.showAsDropDown(anchor, xoff, yoff);
            }
        };
        //View myView=LayoutInflater.from(this).inflate(R.layout.recyclerview_item_empty,null);
        popupWindow.setContentView(contentView);
        //popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        //popupWindow.setWidth(kpx.INSTANCE.screenWidth(true));
        popupWindow.setWidth(width);
        //popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(height);
        popupWindow.setFocusable(true);//false点击外部区域【外部区域点击能力正常，点击没有被拦截,返回键也不会被拦截。】，popuwindow不会消失。true 外部区域的点击能力popuwindow被拦截。即按键和触摸事件像dialog一样被拦截。
        popupWindow.setOutsideTouchable(true);//测试发现，这个属性基本没什么用(对效果么有一点影响)，以防万一还是设置成true,防止触摸popuwindow外区，popuwindow不消失。
        if (styleAnim >= 0) {
            popupWindow.setAnimationStyle(styleAnim);//若隐出现，popuwindow的动画在style里面。是属于window级别的。
        }
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//设置背景,这样触摸返回键 和 popuwindow触摸外区，会让popuwindow会被自动关闭【没有背景，触摸外区就不会自动消失】,前提setFocusable(true)
        popupWindow.setClippingEnabled(false);//解决被状态栏遮挡。true会被状态栏遮挡。false不会被状态栏遮挡。
        // 刷新
        popupWindow.update();
        //获取View坐标
        //int[] location = new int[2];
        //parent.getLocationOnScreen(location);
        //Gravity.LEFT 在对于控件的最左边(正左方)
        //Gravity.TOP 在对于控件的最上方(正上方)
        //Gravity.RIGHT 在对应控件的最右边(正右边)
        //Gravity.BOTTOM 在对应控件的最下方(正下方)
        //Gravity.NO_GRAVITY 无 位置完全由参数3和4的坐标决定。
        //popupWindow.showAtLocation(parent, Gravity.NO_GRAVITY, location[0], location[1] - 100);
        //在指定控件的下方，参数二 x的偏移量【负数先左边偏移，正数先右边偏移】，参数三 y偏移量【负数先上面偏移，正数先下面偏移】,坐标原点是屏幕左上角，向右为正，向下为正。
        //popupWindow.showAsDropDown(view,0,-(view.getHeight()+myView.getHeight()));
        return popupWindow;
    }

}

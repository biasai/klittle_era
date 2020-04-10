package cn.oi.klittle.era.widget.drawerLayout.utils;

import android.app.Activity;
import android.graphics.Point;

import androidx.customview.widget.ViewDragHelper;
import androidx.drawerlayout.widget.DrawerLayout;

import java.lang.reflect.Field;

import cn.oi.klittle.era.exception.KCatchException;
import cn.oi.klittle.era.utils.KLoggerUtils;
import cn.oi.klittle.era.widget.drawerLayout.KDrawerLayout;

/**
 * fixme 控制左右滑动菜单的边缘滑动距离。
 */
public class KDrawerLayoutUtils {

    /**
     * 设置左右两边的滑动距离。
     *
     * @param activity
     * @param drawerLayout
     * @param displayWidthPercentage
     */
    public static void setDrawerEdgeSize(Activity activity, DrawerLayout drawerLayout, float displayWidthPercentage) {
        setDrawerLeftEdgeSize(activity, drawerLayout, displayWidthPercentage);
        setDrawerRightEdgeSize(activity, drawerLayout, displayWidthPercentage);
    }

    public static void setDrawerEdgeSize(Activity activity, KDrawerLayout drawerLayout, float displayWidthPercentage) {
        setDrawerLeftEdgeSize(activity, drawerLayout, displayWidthPercentage);
        setDrawerRightEdgeSize(activity, drawerLayout, displayWidthPercentage);
    }

    /**
     * 设置左边滑动的间距
     *
     * @param activity
     * @param drawerLayout
     * @param displayWidthPercentage 0~1 ;1是整个DrawerLayout控件的宽度，是比例；最好不要超过菜单本身的宽度。不然效果不好。会闪。
     */
    public static void setDrawerLeftEdgeSize(Activity activity, DrawerLayout drawerLayout, float displayWidthPercentage) {
        if (activity == null || drawerLayout == null) return;
        try {
            // 找到 ViewDragHelper 并设置 Accessible 为true
            Field leftDraggerField =
                    DrawerLayout.class.getDeclaredField("mLeftDragger");
            leftDraggerField.setAccessible(true);
            ViewDragHelper leftDragger = (ViewDragHelper) leftDraggerField.get(drawerLayout);

            // 找到 edgeSizeField 并设置 Accessible 为true
            Field edgeSizeField = leftDragger.getClass().getDeclaredField("mEdgeSize");
            edgeSizeField.setAccessible(true);
            int edgeSize = edgeSizeField.getInt(leftDragger);
            // 设置新的边缘大小
            Point displaySize = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
            edgeSizeField.setInt(leftDragger, Math.max(edgeSize, (int) (displaySize.x *
                    displayWidthPercentage)));
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("DrawerLayoutUtils异常Left:\t" + KCatchException.getExceptionMsg(e));
        }
    }

    public static void setDrawerRightEdgeSize(Activity activity, DrawerLayout drawerLayout, float displayWidthPercentage) {
        if (activity == null || drawerLayout == null) return;
        try {
            // 找到 ViewDragHelper 并设置 Accessible 为true
            Field leftDraggerField =
                    DrawerLayout.class.getDeclaredField("mRightDragger");//Right
            leftDraggerField.setAccessible(true);
            ViewDragHelper leftDragger = (ViewDragHelper) leftDraggerField.get(drawerLayout);

            // 找到 edgeSizeField 并设置 Accessible 为true
            Field edgeSizeField = leftDragger.getClass().getDeclaredField("mEdgeSize");
            edgeSizeField.setAccessible(true);
            int edgeSize = edgeSizeField.getInt(leftDragger);
            // 设置新的边缘大小
            Point displaySize = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
            edgeSizeField.setInt(leftDragger, Math.max(edgeSize, (int) (displaySize.x *
                    displayWidthPercentage)));
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("DrawerLayoutUtils异常Right:\t" + KCatchException.getExceptionMsg(e));
        }
    }


    /**
     * @param activity
     * @param drawerLayout
     * @param displayWidthPercentage 0~1 ;1是整个DrawerLayout控件的宽度，是比例；最好不要超过菜单本身的宽度。不然效果不好。会闪。
     */
    public static void setDrawerLeftEdgeSize(Activity activity, KDrawerLayout drawerLayout, float displayWidthPercentage) {
        if (activity == null || drawerLayout == null) return;
        try {
            // 设置新的边缘大小
            Point displaySize = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
            int edgeSize =  drawerLayout.mLeftDragger.mEdgeSize;
            drawerLayout.mLeftDragger.mEdgeSize =Math.max(edgeSize, (int) (displaySize.x *
                    displayWidthPercentage));
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("DrawerLayoutUtils异常Left:\t" + KCatchException.getExceptionMsg(e));
        }
    }

    public static void setDrawerRightEdgeSize(Activity activity, KDrawerLayout drawerLayout, float displayWidthPercentage) {
        if (activity == null || drawerLayout == null) return;
        try {
            Point displaySize = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
            int edgeSize =  drawerLayout.mRightDragger.mEdgeSize;
            drawerLayout.mRightDragger.mEdgeSize =Math.max(edgeSize, (int) (displaySize.x *
                    displayWidthPercentage));
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("DrawerLayoutUtils异常Right:\t" + KCatchException.getExceptionMsg(e));
        }
    }


}

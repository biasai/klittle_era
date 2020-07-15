package cn.oi.klittle.era.widget.gamepad.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

/**
 * Created by 彭治铭 on 2016/8/1.
 */
public final class EventUtils {

    //获取屏幕宽度和高度。下标0为宽度。1为高度
    public static int[] obtainXY(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return new int[]{dm.widthPixels, dm.heightPixels};
    }

    /**
     * 模拟MotionEvent事件
     *
     * @param action
     * @param x
     * @param y
     * @return
     */
    public static MotionEvent obtainMotionEvent(int action, float x, float y) {
        return obtainMotionEvent(action, new float[]{x}, new float[]{y}, 4098);
    }

    //模拟MotionEvent事件
    @SuppressLint("NewApi")
    public static MotionEvent obtainMotionEvent(int action, float[] xseries, float[] yseries, int source) {

        int pointerCount = xseries.length;
        MotionEvent.PointerProperties[] pointerPropertiesArray = new MotionEvent.PointerProperties[pointerCount];
        MotionEvent.PointerCoords[] pointerCoordsArray = new MotionEvent.PointerCoords[pointerCount];

        for (int i = 0; i < pointerCount; i++) {
            MotionEvent.PointerProperties pointerProperties = new MotionEvent.PointerProperties();
            pointerProperties.id = i;
            pointerProperties.toolType = MotionEvent.TOOL_TYPE_FINGER;

            MotionEvent.PointerCoords pointerCoords = new MotionEvent.PointerCoords();
            pointerCoords.x = xseries[i];
            pointerCoords.y = yseries[i];

            pointerPropertiesArray[i] = pointerProperties;
            pointerCoordsArray[i] = pointerCoords;
        }

        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();

        return MotionEvent.obtain(downTime, eventTime, action
                , pointerCount, pointerPropertiesArray, pointerCoordsArray
                , 0, 0, 1, 1, 9/*deviceId*/, 0 /*edageflag*/, source /*source*/, 0 /*flag*/);
    }

}

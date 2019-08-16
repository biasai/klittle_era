package cn.oi.klittle.era.utils;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

/**
 * 获得屏幕宽高
 */

public class KScreenUtil {
    /**
     * 获得屏幕宽高
     * 调用getWidth()，getHeight()
     */
    public static Display getWindowDisplay(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay();
    }
}

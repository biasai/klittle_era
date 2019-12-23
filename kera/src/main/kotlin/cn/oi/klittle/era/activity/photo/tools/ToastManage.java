package cn.oi.klittle.era.activity.photo.tools;

import android.content.Context;
import android.widget.Toast;

public final class ToastManage {

    public static void s(Context mContext, String s) {
        Toast.makeText(mContext.getApplicationContext(), s, Toast.LENGTH_LONG)
                .show();
    }
}

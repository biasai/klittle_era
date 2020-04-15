package cn.oi.klittle.era.widget.web;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;

import com.github.lzyzsd.jsbridge.BridgeWebView;

//import cn.oi.klittle.era.utils.KLoggerUtils;

//fixme 解决androidx在Android 5.1部分机型报Resources$NotFoundException错误。亲测有效！参考地址：https://www.jianshu.com/p/a24a47bbbfc5
//fixme K0JsBridgeWebView继承于本类。
public class KBridgeWebView extends BridgeWebView {
    public KBridgeWebView(Context context) {
        this(context, null);
    }

    public KBridgeWebView(Context context, AttributeSet attrs) {
        this(context, attrs, getDefStyleAtrr());
        //KLoggerUtils.INSTANCE.e("defStyleAttr:\t" + getDefStyleAtrr());
    }

    public KBridgeWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(getFixedContext(context), attrs, defStyleAttr);
    }

    /**
     * fixme 构造方法里调用的 this(context, attrs, 0); 第三个参数 defStyleAttr 不能传 0
     * fixme 因为这样会出现 webView中的输入框不能调起软键盘的问题 需要修改为 如下：
     *
     * @return
     */
    private static int getDefStyleAtrr() {
        try {
            return Resources.getSystem().getIdentifier("webViewStyle", "attr", "android");//结果为：16842885
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * fixme 解决androidx在Android 5.1部分机型报Resources$NotFoundException错误。
     *
     * @param context
     * @return
     */
    private static Context getFixedContext(Context context) {
        // Android Lollipop 5.0 & 5.1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                return context.createConfigurationContext(new Configuration());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return context;
    }
}

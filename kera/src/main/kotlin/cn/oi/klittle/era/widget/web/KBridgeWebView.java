package cn.oi.klittle.era.widget.web;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;

import com.github.lzyzsd.jsbridge.BridgeWebView;

//import cn.oi.klittle.era.utils.KLoggerUtils;

//fixme 解决androidx在Android 5.1部分机型报Resources$NotFoundException错误。亲测有效！参考地址：https://www.jianshu.com/p/a24a47bbbfc5
//fixme 亲测BridgeWebView与js交互，在androidx里可以正常使用。不受影响。可以正常进行PDA云打印。
//fixme K0JsBridgeWebView继承于本类。
public class KBridgeWebView extends BridgeWebView {

    //context是系统的。mContext是自己的。
    public Context mContext=null;//fixme 保存原始的上下文(可以正常转换成之前的Activity)；防止5.1版本的上下文不是原始的上下文(5.1的是新建的，不能转成之前的Activity)。

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public KBridgeWebView(Context context) {
        this(context, null);
        mContext = context;
    }

    public KBridgeWebView(Context context, AttributeSet attrs) {
        this(context, attrs, getDefStyleAtrr());
        //KLoggerUtils.INSTANCE.e("defStyleAttr:\t" + getDefStyleAtrr());
        mContext = context;
    }

    public KBridgeWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(getFixedContext(context), attrs, defStyleAttr);
        mContext = context;
    }

    //销毁
    public void onDestroy() {
        mContext = null;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {//LOLLIPOP 21(5.0);M 23(6.0)
            try {
                return context.createConfigurationContext(new Configuration());//fixme 新建了一个Context；已经不是之前的Context了。是两个不同的对象。
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return context;
    }
}

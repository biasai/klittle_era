package cn.oi.klittle.era.exception;

import android.content.Context;
import android.os.Looper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import cn.oi.klittle.era.utils.KIntentUtils;
import cn.oi.klittle.era.utils.KLoggerUtils;

//                fixme 异常回调监听接口
//                KCatchException.getInstance().exceptionCallback(object :KExceptionCallback{
//                    override fun catchException(msg: String?) {
//                        KLoggerUtils.e("异常回调监听：\t"+msg)
//                    }
//                })

/**
 * fixme 初始化；应用全局异常捕捉。用户没有进行try{}捕捉时，会被全局捕捉。本地捕捉了，就不会调用全局捕捉。
 * 已经在KBaseApplication里初始化了。
 * KCatchException.getInstance().init(sInstance);
 * fixme 注意，在onCreate()发生异常(如：0作为除数会异常陷入死循环重启，布局异常好像不会陷入死循环)，会陷入死循环重启。系统会自动重启的。不是我手动重启的。所以这个要注意一哈。(在小米手机上测试的)；布局异常好像没事。
 */
public class KCatchException implements Thread.UncaughtExceptionHandler {

    //本类实例
    private static KCatchException mInstance;
    //系统默认的uncatchException
    private Thread.UncaughtExceptionHandler mDefaultException;

    private Context mContext;

    //保证只有一个实例
    public KCatchException() {
    }

    //单例模式
    public static KCatchException getInstance() {
        if (mInstance == null) {
            mInstance = new KCatchException();
        }
        return mInstance;
    }

    //获取系统默认的异常处理器,并且设置本类为系统默认处理器
    public void init(Context ctx) {
        if (ctx != null) {
            this.mContext = ctx;
            mDefaultException = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
    }

    /**
     * fixme 获取详细错误原因，包括具体错误代码的位置。
     *
     * @param ex 异常类，如：Exception
     * @return
     */
    static public String getExceptionMsg(Throwable ex) {
        if (ex != null) {
            //获取错误原因
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            printWriter.close();
            String result = writer.toString();
            return result;
        }
        return "";
    }

    public KExceptionCallback exceptionCallback = null;

    public void exceptionCallback(KExceptionCallback exceptionCallback) {
        this.exceptionCallback = exceptionCallback;
    }

    //自定义错误处理器;在uncaughtException()方法里手动调用了。
    private boolean handlerException(Throwable ex) {
        if (ex == null) {  //如果已经处理过这个Exception,则让系统处理器进行后续关闭处理
            return false;
        }

        final String msg = getExceptionMsg(ex);
        if (exceptionCallback != null) {
            exceptionCallback.catchException(msg);//异常监听回调。
        }
        KLoggerUtils.INSTANCE.e("App全局异常捕捉:\t" + msg);
        KIntentUtils.INSTANCE.goRest();//fixme app应用崩溃后，自动重启（如果不重启，整个应用也是卡着的。没有任何响应。）

//        new Thread() {
//            public void run() {
//                Toast 显示需要出现在一个线程的消息队列中
//                Looper.prepare();
//                Toast.makeText(mContext, "程序出错:" + msg.toString(), Toast.LENGTH_LONG).show();
//                //将异常记录到本地的数据库或者文件中.或者直接提交到后台服务器
//                KLoggerUtils.INSTANCE.e("全局异常", msg);
//                Looper.loop();
//            }
//        }.start();
        return true;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            if (!handlerException(ex) && mDefaultException != null) {
                // 如果用户没有处理则让系统默认的异常处理器来处理
                mDefaultException.uncaughtException(thread, ex);
            } else { //否则自己进行处理
//            try {  //Sleep 来让线程停止一会是为了显示Toast信息给用户，然后Kill程序
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                KLoggerUtils.INSTANCE.e("全局异常", e.getMessage());
//                //Log.d("2635", "uncaughtException: "+e.getMessage());
//            } catch (Exception e) {
//                KLoggerUtils.INSTANCE.e("全局异常", e.getMessage());
//                //Log.d("2635", "Exception: "+e.getMessage());
//            }
                //fixme 如果不关闭程序,会导致程序无法启动,需要完全结束进程才能重新启动(亲测有效，在杀进程前重启)
                //fixme 全局异常捕捉之后，应用就必须杀进程重启。如果不重启；整个app应用都是卡着的。不会有响应的。
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(10);//退出JVM(java虚拟机),释放所占内存资源,0表示正常退出(非0的都为异常退出)
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
package cn.oi.klittle.era.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.*
import androidx.print.PrintHelper
import cn.oi.klittle.era.base.KBaseActivityManager
import cn.oi.klittle.era.base.KBaseApplication
import java.io.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.Deferred

/**
 * 安卓原生系统打印功能；对4.4 api 19 以上的才有效；8.0和9.0不需要安装插件也可以；8.0以下需要安装Mopria Print Service 插件；下载地址：https://app.mopria.org/MopriaPrintService
 * Created by 彭治铭 on 2019/4/12.
 */
object KPrintUtils {

    private fun getContext(): Context {
        return KBaseApplication.getInstance()
    }

    private fun getActivity(): Activity? {
        return KBaseActivityManager.getInstance().stackTopActivity
    }

    /**
     * 打印位图
     * @param bitmap
     * @param SCALE_MODE
     * @param jobName 打印任务名称；随便取(设置作业名称，它将显示在打印队列中)
     */
    fun printBitmap(activity: Activity? = getActivity(), bitmap: Bitmap, SCALE_MODE: Int = PrintHelper.SCALE_MODE_FIT, jobName: String = "print bitmap") {
        activity?.apply {
            var printHelper: PrintHelper? = PrintHelper(activity)
            /**
             * PrintHelper通过setScaleMode()方法设置模式，现在有两种模式
             * SCALE_MODE_FIT：这个打印完整的图片，这样打印纸的边缘可能有空白
             * SCALE_MODE_FILL：这个填满所有的打印纸，因此图片的边缘可能打印不出来
             */
            printHelper?.setScaleMode(SCALE_MODE);
            //val bitmap = BitmapFactory.decodeResource(resources,
            //        R.drawable.timg)//R.drawable.ic_launcher_background fixme 这里要放一张图片；而不是一个xml文件（不然没效果）。
            printHelper?.printBitmap(jobName, bitmap)
            printHelper = null
        }
    }

    fun printBitmap(activity: Activity? = getActivity(), resID: Int, SCALE_MODE: Int = PrintHelper.SCALE_MODE_FIT, jobName: String = "print bitmap") {
        printBitmap(activity, KAssetsUtils.getInstance().getBitmapFromResource(resID, false), SCALE_MODE, jobName)
    }

    /**
     * 打印文档
     * @param printDocumentAdapter 打印适配器与Android系统的打印框架进行交互，处理打印的生命周期方法
     * @param printAttributes 可以用来设置一些打印时的属性。
     */
    fun printDocument(activity: Activity? = getActivity(), jobName: String = "print text", printDocumentAdapter: PrintDocumentAdapter? = null, printAttributes: PrintAttributes? = null) {
        if (Build.VERSION.SDK_INT >= 19) {//要求安卓版本4.4及以上
            activity?.apply {
                // Get a PrintManager instance
                var printManager = activity?.getSystemService(Context.PRINT_SERVICE) as PrintManager
                // Start a print job, passing in a PrintDocumentAdapter implementation
                // to handle the generation of a print document
                printManager?.print(jobName, printDocumentAdapter,
                        printAttributes)
            }
        }
    }

    /**
     * 打印文檔
     * @param inputStream 打印的文档流；（注意只支持pdf格式；其他格式好像不支持。）
     */
    fun printDocument(inputStream: InputStream?, activity: Activity? = getActivity(), jobName: String = "print document", printAttributes: PrintAttributes? = null) {
        if (Build.VERSION.SDK_INT >= 19) {
            KPrintUtils.printDocument(activity = activity, jobName = jobName, printAttributes = printAttributes, printDocumentAdapter = object : PrintDocumentAdapter() {
                override fun onStart() {
                    super.onStart()
                    //打印开始的时候调用
                    //KLoggerUtils.e("开始")
                }

                //默认在主线程中调用（如果打印过程比较耗时，应该在后台线程中进行。）
                //当用户更改打印设置导致打印结果改变时调用，如更改纸张尺寸，纸张方向等；
                //主要任务就是计算在新的设置下，需要打印的页数
                override fun onLayout(oldAttributes: PrintAttributes?, newAttributes: PrintAttributes?, cancellationSignal: CancellationSignal?, callback: LayoutResultCallback?, extras: Bundle?) {
                    //KLoggerUtils.e("onLayout()")
                    if (Build.VERSION.SDK_INT >= 19) {
                        //onLayout（）方法的执行有完成，取消，和失败三种结果，你必须通过调用 PrintDocumentAdapter.LayoutResultCallback类
                        cancellationSignal?.let {
                            if (it.isCanceled) {
                                //响应取消请求
                                callback?.onLayoutCancelled();
                                return
                            }
                        }
                        if (newAttributes == null) {
                            return
                        }
                        // 将打印信息返回到打印框架
                        var info = PrintDocumentInfo
                                .Builder(jobName)
                                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build()
                        //完成
                        callback?.onLayoutFinished(info, true);
                    }
                }

                //默认在主线程中调用
                //当将要打印的结果写入到文件中时调用，该方法在每次onLayout（）调用后会调用一次或多次；
                //该方法的参数指明要打印的页以及结果写入的文件
                override fun onWrite(pages: Array<out PageRange>?, destination: ParcelFileDescriptor?, cancellationSignal: CancellationSignal?, callback: WriteResultCallback?) {
                    //KLoggerUtils.e("onWrite()")
                    if (destination == null) {
                        return
                    }
                    //耗时操作；在协程中进行。
                    GlobalScope.async {
                        var input: InputStream? = null
                        var output: OutputStream? = null
                        try {
                            input = inputStream
                            if (input == null) {
                                return@async
                            }
                            output = FileOutputStream(destination.getFileDescriptor())
                            var buf = ByteArray(1024)
                            var bytesRead = input.read(buf)
                            while (bytesRead > 0) {
                                output.write(buf, 0, bytesRead)
                                bytesRead = input.read(buf)
                            }
                            if (Build.VERSION.SDK_INT >= 19) {
                                callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                            }
                        } catch (e: Exception) {
                            e.printStackTrace();
                            KLoggerUtils.e("打印机onWrite()异常：\t" + e.message)
                        } finally {
                            try {
                                output?.close();
                            } catch (e: Exception) {
                                e.printStackTrace();
                            }
                            try {
                                input?.close();
                            } catch (e: Exception) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                override fun onFinish() {
                    super.onFinish()
                    //打印结束的时候调用
                    //KLoggerUtils.e("打印结束")
                }
            })
        }
    }

    /**
     * 打印文档[只支持PDF格式]
     * @param assetsPath assets 里的文件。如("文件夹/文件名.后缀");就是正常的路径。
     */
    fun printDocumentFromAssets(assetsPath: String?, activity: Activity? = getActivity(), jobName: String = "print document", printAttributes: PrintAttributes? = null) {
        assetsPath?.let {
            printDocument(activity = activity, inputStream = KAssetsUtils.getInstance().getInputStream(it), jobName = jobName, printAttributes = printAttributes)
        }
    }

    /**
     * 打印文档[只支持PDF格式]
     * @param filePath 文件路径
     */
    fun printDocumentFromFile(filePath: String?, activity: Activity? = getActivity(), jobName: String = "print document", printAttributes: PrintAttributes? = null) {
        filePath?.let {
            printDocument(activity = activity, inputStream = KFileUtils.getInstance().getFileInputStream(filePath), jobName = jobName, printAttributes = printAttributes)
        }
    }

}
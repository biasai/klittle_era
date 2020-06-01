package cn.oi.klittle.era.activity.camera.manager;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.RejectedExecutionException;

import cn.oi.klittle.era.exception.KCatchException;
import cn.oi.klittle.era.utils.KLoggerUtils;

/**
 * 自动对焦
 * User:lizhangqu(513163535@qq.com)
 * Date:2015-09-05
 * Time: 11:11
 */
public class KAutoFocusManager implements Camera.AutoFocusCallback{
    private static final String TAG = KAutoFocusManager.class.getSimpleName();
    private static final long AUTO_FOCUS_INTERVAL_MS = 2000L;
    private static final Collection<String> FOCUS_MODES_CALLING_AF;
    static {
        FOCUS_MODES_CALLING_AF = new ArrayList<String>(2);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_AUTO);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_MACRO);
    }

    private boolean stopped;
    private boolean focusing;
    private final boolean useAutoFocus;
    private final Camera camera;
    public AsyncTask<?,?,?> outstandingTask;

    public KAutoFocusManager(Camera camera) {
        this.camera = camera;
        String currentFocusMode = camera.getParameters().getFocusMode();
        useAutoFocus = FOCUS_MODES_CALLING_AF.contains(currentFocusMode);
        Log.e(TAG, "Current focus mode '" + currentFocusMode + "'; use auto focus? " + useAutoFocus);
        start();
    }

    @Override
    public synchronized void onAutoFocus(boolean success, Camera theCamera) {
        focusing = false;
        autoFocusAgainLater();
    }


    private synchronized void autoFocusAgainLater() {
        if (!stopped && outstandingTask == null) {
            AutoFocusTask newTask = new AutoFocusTask();
            try {
                newTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                outstandingTask = newTask;
            } catch (RejectedExecutionException ree) {
                Log.e(TAG, "Could not request auto focus", ree);
            }
        }
    }

    /**
     * 开始自动对焦
     */
    public synchronized void start() {
        if (useAutoFocus) {
            outstandingTask = null;
            if (!stopped && !focusing) {
                try {
                    if (camera!=null) {
                        camera.autoFocus(this);
                        focusing = true;
                    }
                } catch (RuntimeException re) {
                    // Have heard RuntimeException reported in Android 4.0.x+; continue?
                    //Log.e(TAG, "Unexpected exception while focusing", re);
                    //fixme 很少发生异常，就算发生异常了，对拍照和录像都没有什么影响。
                    KLoggerUtils.INSTANCE.e("KAutoFocusManager->start()异常：\t"+ KCatchException.getExceptionMsg(re),true);
                    // Try again later to keep cycle going
                    autoFocusAgainLater();
                }
            }
        }
    }


    private synchronized void cancelOutstandingTask() {
        if (outstandingTask != null) {
            if (outstandingTask.getStatus() != AsyncTask.Status.FINISHED) {
                outstandingTask.cancel(true);
            }
            outstandingTask = null;
        }
    }

    /**
     * 停止自动对焦
     */
    public synchronized void stop() {
        stopped = true;
        if (useAutoFocus) {
            cancelOutstandingTask();
            // Doesn't hurt to call this even if not focusing
            try {
                camera.cancelAutoFocus();
            } catch (RuntimeException re) {
                // Have heard RuntimeException reported in Android 4.0.x+; continue?
                Log.e(TAG, "Unexpected exception while cancelling focusing", re);
            }
        }
    }


    private final class AutoFocusTask extends AsyncTask<Object,Object,Object> {
        @Override
        protected Object doInBackground(Object... voids) {
            try {
                Thread.sleep(AUTO_FOCUS_INTERVAL_MS);
                start();
            } catch (InterruptedException e) {
                // continue
            }
            return null;
        }
    }
}

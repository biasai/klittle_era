/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sdk.Qr_code.manager;

import java.io.IOException;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import com.sdk.Qr_code.code.AutoFocusCallback;
import com.sdk.Qr_code.code.CameraConfigurationManager;
import com.sdk.Qr_code.code.FlashlightManager;
import com.sdk.Qr_code.code.PlanarYUVLuminanceSource;
import com.sdk.Qr_code.code.PreviewCallback;

import cn.oi.klittle.era.comm.kpx;
import cn.oi.klittle.era.exception.KCatchException;
import cn.oi.klittle.era.utils.KLoggerUtils;

/**
 * This object wraps the Camera service object and expects to be the only one
 * talking to it. The implementation encapsulates the steps needed to take
 * preview-sized images, which are used for both preview and decoding.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * fixme 核心管理类；getFramingRect()重要方法
 */
public final class CameraManager {
    private static CameraManager cameraManager;

    public static final int SDK_INT; // Later we can use Build.VERSION.SDK_INT

    static {
        int sdkInt;
        try {
            sdkInt = Build.VERSION.SDK_INT;
        } catch (NumberFormatException nfe) {
            // Just to be safe
            sdkInt = 10000;
        }
        SDK_INT = sdkInt;
    }

    @SuppressWarnings("unused")
    private final Context context;
    private final CameraConfigurationManager configManager;
    private Camera camera;
    private Rect framingRect;
    private Rect framingRectInPreview;// 扫描框里图像资源
    private boolean initialized;
    private boolean previewing;
    private final boolean useOneShotPreviewCallback;
    /**
     * Preview frames are delivered here, which we pass on to the registered
     * handler. Make sure to clear the handler so it will only receive one
     * message.
     */
    private final PreviewCallback previewCallback;
    /**
     * Autofocus callbacks arrive here, and are dispatched to the Handler which
     * requested them.
     */
    private final AutoFocusCallback autoFocusCallback;

    /**
     * Initializes this static object with the Context of the calling Activity.
     *
     * @param context The Activity which wants to use the camera.
     */
    public static void init(Context context) {
        if (cameraManager == null) {
            cameraManager = new CameraManager(context);
        }
    }

    /**
     * Gets the CameraManager singleton instance.
     *
     * @return A reference to the CameraManager singleton.
     */
    public static CameraManager get() {
        return cameraManager;
    }

    private CameraManager(Context context) {

        this.context = context;
        this.configManager = new CameraConfigurationManager(context);

        // Camera.setOneShotPreviewCallback() has a race condition in Cupcake,
        // so we use the older
        // Camera.setPreviewCallback() on 1.5 and earlier. For Donut and later,
        // we need to use
        // the more efficient one shot callback, as the older one can swamp the
        // system and cause it
        // to run out of memory. We can't use SDK_INT because it was introduced
        // in the Donut SDK.
        // useOneShotPreviewCallback = Integer.parseInt(Build.VERSION.SDK) >
        // Build.VERSION_CODES.CUPCAKE;
        useOneShotPreviewCallback = Build.VERSION.SDK_INT > 3; // 3 = Cupcake

        previewCallback = new PreviewCallback(configManager,
                useOneShotPreviewCallback);
        autoFocusCallback = new AutoFocusCallback();
    }

    /**
     * Opens the camera driver and initializes the hardware parameters.
     *
     * @param holder The surface object which the camera will draw preview frames
     *               into.
     * @throws IOException Indicates the camera driver failed to open.
     */
    @SuppressWarnings("deprecation")
    public void openDriver(Context context, SurfaceHolder holder)
            throws IOException {
        if (camera == null) {
            try {
                //camera = Camera.open();//打开摄像头(一般默认都是打开后置摄像头)
                camera = openCamera(BACK);//后置摄像头（建议使用这个）
            } catch (Exception e) {
                e.printStackTrace();
                //异常：Fail to connect to camera service
                if (camera != null) {
                    try {
                        camera.release();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                camera = null;
                KLoggerUtils.INSTANCE.e("相机后置摄像头调用异常：\t" + e.getMessage());
            }
            if (camera == null) {
                try {
                    //fixme 如果后置摄像头打开异常，则先调用一次前置摄像头，然后再调用后置摄像头。就可以了。
                    camera = openCamera(FRONT);//前置摄像头；
                    if (camera != null) {
                        camera.release();//释放
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                camera = openCamera(BACK);//后置摄像头
            }
            if (camera == null) {
                throw new IOException();
            }
            if (!initialized) {
                initialized = true;
                configManager.initFromCameraParameters(camera);
            }
            configManager.setDesiredCameraParameters(camera);
            camera.setPreviewDisplay(holder);
            FlashlightManager.enableFlashlight();
        }
    }

    private static final int FRONT = 1;//前置摄像头标记
    private static final int BACK = 2;//后置摄像头标记
    private int currentCameraType = -1;//当前打开的摄像头标记

    private Camera openCamera(int type) {
        try {
            int frontIndex = -1;
            int backIndex = -1;
            int cameraCount = Camera.getNumberOfCameras();
            Camera.CameraInfo info = new Camera.CameraInfo();
            for (int cameraIndex = 0; cameraIndex < cameraCount; cameraIndex++) {
                Camera.getCameraInfo(cameraIndex, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    frontIndex = cameraIndex;//前置摄像头
                } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    backIndex = cameraIndex;//后置摄像头
                }
            }

            currentCameraType = type;
            if (type == FRONT && frontIndex != -1) {
                return Camera.open(frontIndex);
            } else if (type == BACK && backIndex != -1) {
                return Camera.open(backIndex);
            }
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("openCamera()异常：\t" + KCatchException.getExceptionMsg(e), true);
        }
        return null;
    }

    /**
     * Closes the camera driver if still in use.
     */
    public void closeDriver() {
        if (camera != null) {
            try {
                FlashlightManager.disableFlashlight();
                camera.release();
                camera = null;
            } catch (Exception e) {
                KLoggerUtils.INSTANCE.e("closeDriver()异常：\t" + KCatchException.getExceptionMsg(e), true);
            }
        }
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    public void startPreview() {
        if (camera != null && !previewing) {
            try {
                camera.startPreview();
                previewing = true;
            } catch (Exception e) {
                KLoggerUtils.INSTANCE.e("startPreview()异常：\t" + KCatchException.getExceptionMsg(e), true);
            }
        }
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    public void stopPreview() {
        if (camera != null && previewing) {
            try {
                if (!useOneShotPreviewCallback) {
                    camera.setPreviewCallback(null);
                }
                camera.stopPreview();
                previewCallback.setHandler(null, 0);
                autoFocusCallback.setHandler(null, 0);
                previewing = false;
            } catch (Exception e) {
                KLoggerUtils.INSTANCE.e("stopPreview()异常：\t" + KCatchException.getExceptionMsg(e), true);
            }
        }
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data
     * will arrive as byte[] in the message.obj field, with width and height
     * encoded as message.arg1 and message.arg2, respectively.
     *
     * @param handler The handler to send the message to.
     * @param message The what field of the message to be sent.
     */
    public void requestPreviewFrame(Handler handler, int message) {
        if (camera != null && previewing) {
            previewCallback.setHandler(handler, message);
            if (useOneShotPreviewCallback) {
                camera.setOneShotPreviewCallback(previewCallback);
            } else {
                camera.setPreviewCallback(previewCallback);
            }
        }
    }

    /**
     * Asks the camera hardware to perform an autofocus.
     *
     * @param handler The Handler to notify when the autofocus completes.
     * @param message The message to deliver.
     */
    public void requestAutoFocus(Handler handler, int message) {
        if (camera != null && previewing) {
            autoFocusCallback.setHandler(handler, message);
            // Log.d(TAG, "Requesting auto-focus callback");
            camera.autoFocus(autoFocusCallback);
        }
    }

    /**
     * getFramingRect()参照物必须是全屏的。因为相机是全屏的。ViewfinderView也必须是全屏的。这样截取的图片才能对应上
     * ViewfinderView画框，以及Camera扫码都在这个区域。
     * <p>
     * 二维码像素采集也是对该区域进行扫描。
     * <p>
     * fixme 控制扫描框的大小以及位置 . 这个方法在实时刷新执行(在ViewfinderView的onDraw方法中被调用)
     *
     * @return
     */
    public Rect getFramingRect() {
        //int widthPercent = 55;
        int widthPercent = 65;// 扫描框的百分比宽度，以整个屏幕宽带为基础,取值范围(0~100)
        int left = 50;// 扫描框与屏幕左边的距离。以整个屏幕的百分比为基础,取值范围(0~100)
        //int top = 30;
        int top = 40;// 扫描框与屏幕顶部的距离。同上
        Point screenResolution = configManager.getScreenResolution();
        if (framingRect == null || (framingRect.right - framingRect.left) <= 0 || (framingRect.bottom - framingRect.top) <= 0) {
            if (camera == null) {
                return null;
            }
            //screenResolution.x 屏幕的宽度
            //screenResolution.y屏幕的高度
            int width = (int) (screenResolution.x * widthPercent / 100);
            int height = width;
            int leftOffset = (int) ((screenResolution.x - width) * left / 100);
            int topOffset = (int) ((screenResolution.y - height) * top / 100);
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width,
                    topOffset + height);

            //Log.e("test", "扫描框\tleft:\t" + framingRect.left + "\ttop:\t" + framingRect.top + "\tright:\t" + framingRect.right + "\tbottom:\t" + framingRect.bottom + "\twidth:\t" + (framingRect.right - framingRect.left + "\theight:\t" + (framingRect.bottom - framingRect.top)));

        }
        return framingRect;
    }

    /**
     * 获取扫描框里的图像【fixme 扫描完成之后，截图就是这个区域的图；只对这个区域进行扫描。】
     * fixme 这个区域即是扫描区域；生成的位图也是这个区域的。(不能说生成图和该区域百分百相等，但基本差不多了。)
     */
    public Rect getFramingRectInPreview() {
        if (framingRectInPreview == null || (framingRectInPreview.right - framingRectInPreview.left) <= 0 || (framingRectInPreview.bottom - framingRectInPreview.top) <= 0) {
            Rect rect = new Rect(getFramingRect());//获取扫描框的矩形
            //fixme cameraResolution.x相机的高度；cameraResolution.y相机的宽度（因为是竖屏的，所以相机的宽和高切换了。）
            Point cameraResolution = configManager.getCameraResolution();
            //screenResolution.x屏幕的宽度；screenResolution.y屏幕的高度
            Point screenResolution = configManager.getScreenResolution();
//            相机的宽:	1080	相机的高:	1920
//            Log.e("test","相机的宽:\t"+cameraResolution.y+"\t相机的高:\t"+cameraResolution.x);
//            屏幕的宽:	1080	屏幕的高:	2028 (小米8的测试数据)
//            Log.e("test","屏幕的宽:\t"+screenResolution.x+"\t屏幕的高:\t"+screenResolution.y);

            //竖屏
//            rect.left = rect.left * cameraResolution.y / screenResolution.x;
//            rect.right = rect.right * cameraResolution.y / screenResolution.x;
//            rect.top = rect.top * cameraResolution.x / screenResolution.y;
//            rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y;

            int x = rect.left * cameraResolution.y / screenResolution.x;
            int y = rect.top * cameraResolution.x / screenResolution.y;
            int width = rect.right - rect.left;
            int height = rect.bottom - rect.top;

            rect.left = x;
            rect.right = x + width;
            rect.top = y;
            rect.bottom = y + height;

            framingRectInPreview = rect;
            //Log.e("test", "图像框\tleft:\t" + rect.left + "\ttop:\t" + rect.top + "\tright:\t" + rect.right + "\tbottom:\t" + rect.bottom + "\twidth:\t" + (rect.right - rect.left + "\theight:\t" + (rect.bottom - rect.top)));
        }
        return framingRectInPreview;
    }

    /**
     * 打开闪光灯
     */
    public synchronized void openLight() {
        if (camera != null) {
            try {
                Parameters parameters = camera.getParameters();
                if (parameters != null) {
                    parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(parameters);
                    judgeLight();
                }
            } catch (Exception e) {
                KLoggerUtils.INSTANCE.e("openLight()异常：\t" + KCatchException.getExceptionMsg(e));
            }
        }
    }

    /**
     * 关闭闪光灯
     */
    public synchronized void offLight() {
        if (camera != null) {
            try {
                Parameters parameters = camera.getParameters();
                if (parameters != null) {
                    parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
                    camera.setParameters(parameters);
                    judgeLight();
                }
            } catch (Exception e) {
                KLoggerUtils.INSTANCE.e("offLight()异常：\t" + KCatchException.getExceptionMsg(e), true);
            }
        }
    }

    /**
     * 判断闪光灯状态，true开，false关。
     *
     * @return
     */
    public synchronized boolean judgeLight() {
        if (camera != null) {
            try {
                Parameters parameters = camera.getParameters();
                if (parameters != null) {
                    String flashMode = parameters.getFlashMode();
                    if (flashMode != null) {
                        //Log.e("test", "flashMode:\t" + flashMode + "\t开:\t" + Parameters.FLASH_MODE_TORCH + "\t关:\t" + Parameters.FLASH_MODE_OFF);
                        if (flashMode.equals(Parameters.FLASH_MODE_TORCH)) {
                            //Log.e("test", "开");
                            return true;
                        }
                        if (flashMode.equals(Parameters.FLASH_MODE_OFF)) {
                            //Log.e("test", "关");
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                KLoggerUtils.INSTANCE.e("judgeLight()异常：\t" + KCatchException.getExceptionMsg(e),true);
            }
        }
        return false;
    }


    /**
     * Converts the result points from still resolution coordinates to screen
     * coordinates.
     *
     * @param points
     *            The points returned by the Reader subclass through
     *            Result.getResultPoints().
     * @return An array of Points scaled to the size of the framing rect and
     *         offset appropriately so they can be drawn in screen coordinates.
     */
    /*
     * public Point[] convertResultPoints(ResultPoint[] points) { Rect frame =
     * getFramingRectInPreview(); int count = points.length; Point[] output =
     * new Point[count]; for (int x = 0; x < count; x++) { output[x] = new
     * Point(); output[x].x = frame.left + (int) (points[x].getX() + 0.5f);
     * output[x].y = frame.top + (int) (points[x].getY() + 0.5f); } return
     * output; }
     */

    /**
     * A factory method to build the appropriate LuminanceSource object based on
     * the format of the preview buffers, as described by Camera.Parameters.
     *
     * @param data   A preview frame.
     * @param width  The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data,
                                                         int width, int height) {
        Rect rect = getFramingRectInPreview();
        int previewFormat = configManager.getPreviewFormat();
        String previewFormatString = configManager.getPreviewFormatString();
        switch (previewFormat) {
            // This is the standard Android format which all devices are REQUIRED to
            // support.
            // In theory, it's the only one we should ever care about.
            case PixelFormat.YCbCr_420_SP:
                // This format has never been seen in the wild, but is compatible as
                // we only care
                // about the Y channel, so allow it.
            case PixelFormat.YCbCr_422_SP:
                return new PlanarYUVLuminanceSource(data, width, height, rect.left,
                        rect.top, rect.width(), rect.height());
            default:
                // The Samsung Moment incorrectly uses this variant instead of the
                // 'sp' version.
                // Fortunately, it too has all the Y data up front, so we can read
                // it.
                if ("yuv420p".equals(previewFormatString)) {
                    return new PlanarYUVLuminanceSource(data, width, height,
                            rect.left, rect.top, rect.width(), rect.height());
                }
        }
        throw new IllegalArgumentException("Unsupported picture format: "
                + previewFormat + '/' + previewFormatString);
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

}

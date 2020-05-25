package cn.oi.klittle.era.activity.camera;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;


import java.io.IOException;
import java.util.regex.Pattern;

import cn.oi.klittle.era.base.KBaseUi;
import cn.oi.klittle.era.comm.kpx;

/**
 * User:lizhangqu(513163535@qq.com)
 * Date:2015-09-05
 * Time: 10:56
 */
public class KCameraManager {
    private static final String TAG = KCameraManager.class.getName();
    private Camera camera;
    private Camera.Parameters parameters;
    private KAutoFocusManager autoFocusManager;
    private int requestedCameraId = -1;

    private boolean initialized;
    private boolean previewing;
    private final Pattern COMMA_PATTERN = Pattern.compile(",");

    /**
     * 打开摄像头
     *
     * @param cameraId 摄像头id
     * @return Camera
     */
    public Camera open(int cameraId) {
        int numCameras = Camera.getNumberOfCameras();
        if (numCameras == 0) {
            Log.e(TAG, "No cameras!");
            return null;
        }
        boolean explicitRequest = cameraId >= 0;
        if (!explicitRequest) {
            // Select a camera if no explicit camera requested
            int index = 0;
            while (index < numCameras) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(index, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    break;
                }
                index++;
            }
            cameraId = index;
        }
        Camera camera;
        if (cameraId < numCameras) {
            Log.e(TAG, "Opening camera #" + cameraId);
            camera = Camera.open(cameraId);
        } else {
            if (explicitRequest) {
                Log.e(TAG, "Requested camera does not exist: " + cameraId);
                camera = null;
            } else {
                Log.e(TAG, "No camera facing back; returning camera #0");
                camera = Camera.open(0);
            }
        }
        return camera;
    }

    /**
     * 打开camera
     *
     * @param holder SurfaceHolder 把相机视图投射到SurfaceView上。
     * @throws IOException
     */
    public synchronized void openDriver(SurfaceHolder holder)
            throws IOException {
        Log.e(TAG, "openDriver");
        Camera theCamera = camera;
        if (theCamera == null) {
            theCamera = open(requestedCameraId);//打开摄像头
            if (theCamera == null) {
                throw new IOException();
            }
            camera = theCamera;
        }
        theCamera.setPreviewDisplay(holder);//相机投射到SurfaceView上面。
        //相机在横屏上是正常的，所见即所得。
        // 在竖屏上是倾斜的。
        //fixme 这个设置之后，就是竖屏。横屏不用设置。默认就是横屏
        theCamera.setDisplayOrientation(90);//解决竖屏倾斜的的问题。但是拍出来的照片仍然是横屏的（反转90的，逆时针）。
        //fixme 竖屏获得图片之后，还需要手动对位图进行旋转90度(顺时针。)
        //也就是说竖屏自定义相机拍照，需要对相机就旋转90，之后还要对图片进行旋转90度【顺时针】。
        //即无论是横屏拍，还是竖屏拍，拍出来的图片都横屏图片。宽>高。竖屏要对图片进行旋转90度处理。

        //fixme 注意：以下不设置也可以，如果不设置的话，拍出来的照片就比较糊，不清晰。
        //下面的作用就是调节拍照像素。
        if (!initialized) {
            initialized = true;
            parameters = camera.getParameters();
            int w = parameters.getPreviewSize().width;
            int h = parameters.getPreviewSize().height;
            //标准尺寸，相机拍照是以横屏为主的。所以宽大于高。
            //int width = 1920;
            //int height = 1080;
            int width = kpx.INSTANCE.screenWidth(false);
            int height = kpx.INSTANCE.screenHeight(false, KBaseUi.Companion.getActivity());
            if (width > 1920) {
                width = 1920;
            }
            if (height > 1080) {
                height = 1080;
            }
            int min = 0;
            //相机预览尺寸【获取与标准尺寸，最接近的尺寸。】
            //parameters.getSupportedPreviewSizes()获取支持的所有预览尺寸。排序顺序是从大到小。
            for (int i = 0; i < parameters.getSupportedPreviewSizes().size(); i++) {
                Camera.Size size = parameters.getSupportedPreviewSizes().get(i);
                int dx = Math.abs(size.width - width);
                int dy = Math.abs(size.height - height);
                int distance = dx + dy;
                if (i == 0) {
                    min = distance;
                    w = size.width;
                    h = size.height;
                } else {
                    if (distance < min) {//屏幕适配也是按这个算计去读取对应的文件夹的。
                        min = distance;
                        w = size.width;
                        h = size.height;
                    }
                }
//                Log.e("test", "预览 width:" + size.width + "\theight:\t" + size.height);
            }
//            Log.e("test", "预览尺寸：\t宽:\t" + w + "\t高:\t" + h);
            parameters.setPreviewSize(w, h);//相机预览尺寸，如果和SurfaceView比例不同。照片预览会拉伸。不需要设置，使用默认的即可。
            parameters.setPictureFormat(ImageFormat.JPEG);//图片格式
            parameters.setJpegQuality(100);//图片的质量

            width = w;
            height = h;
            //图片尺寸【获取与相机预览尺寸，最接近的尺寸。】。
            //parameters.getSupportedPictureSizes()获取支持的所有图片尺寸。排序顺序也是从大到小。
            for (int i = 0; i < parameters.getSupportedPictureSizes().size(); i++) {
                Camera.Size size = parameters.getSupportedPictureSizes().get(i);
                int dx = Math.abs(size.width - width);
                int dy = Math.abs(size.height - height);
                int distance = dx + dy;
                if (i == 0) {
                    min = distance;
                    w = size.width;
                    h = size.height;
                } else {
                    if (distance < min) {
                        min = distance;
                        w = size.width;
                        h = size.height;
                    }
                }
//                Log.e("test", "图片Sizes width:" + size.width + "\theight:\t" + size.height + "\tdistance:\t" + distance + "\tmin:\t" + min);
            }
//            Log.e("test", "图片尺寸：\t宽:\t" + w + "\t高:\t" + h);
            parameters.setPictureSize(w, h);//设置拍照图片的尺寸。
            theCamera.setParameters(parameters);
        }
    }

    /**
     * camera是否打开
     *
     * @return camera是否打开
     */
    public synchronized boolean isOpen() {
        return camera != null;
    }

    /**
     * 关闭camera
     */
    public synchronized void closeDriver() {
        Log.e(TAG, "closeDriver");
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    /**
     * 开始预览
     */
    public synchronized void startPreview() {
        Log.e(TAG, "startPreview");
        Camera theCamera = camera;
        if (theCamera != null && !previewing) {
            theCamera.startPreview();
            previewing = true;
            autoFocusManager = new KAutoFocusManager(camera);
        }
    }

    /**
     * 关闭预览
     */
    public synchronized void stopPreview() {
        Log.e(TAG, "stopPreview");
        if (autoFocusManager != null) {
            autoFocusManager.stop();
            autoFocusManager = null;
        }
        if (camera != null && previewing) {
            camera.stopPreview();
            previewing = false;
        }
    }

    /**
     * 打开闪光灯
     */
    public synchronized void openLight() {
        Log.e(TAG, "openLight");
        if (camera != null) {
            parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
        }
    }

    /**
     * 判断闪光灯状态，true开，false关。
     *
     * @return
     */
    public synchronized boolean judgeLight() {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            String flashMode = parameters.getFlashMode();
            //Log.e("test", "flashMode:\t" + flashMode + "\t开:\t" + Parameters.FLASH_MODE_TORCH + "\t关:\t" + Parameters.FLASH_MODE_OFF);
            if (flashMode.equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                //Log.e("test", "开");
                return true;
            }
            if (flashMode.equals(Camera.Parameters.FLASH_MODE_OFF)) {
                //Log.e("test", "关");
                return false;
            }
        }
        return false;
    }


    /**
     * 关闭闪光灯
     */
    public synchronized void offLight() {
        Log.e(TAG, "offLight");
        if (camera != null) {
            parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(parameters);
        }
    }

    /**
     * 拍照【此时预览的SurfaceHolder会停留在拍照的那一帧，surfaceView不要隐藏，不然页面会闪屏。】
     *
     * @param shutter ShutterCallback
     * @param raw     PictureCallback
     * @param jpeg    PictureCallback
     */
    public synchronized void takePicture(final Camera.ShutterCallback shutter, final Camera.PictureCallback raw,
                                         final Camera.PictureCallback jpeg) {
        camera.takePicture(shutter, raw, jpeg);
    }
}

package com.sdk.Qr_code.act;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.luck.picture.lib.tools.PictureFileUtils;
import com.sdk.Qr_code.manager.CameraManager;
import com.sdk.Qr_code.code.CaptureActivityHandler;
import com.sdk.Qr_code.code.InactivityTimer;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.sdk.Qr_code.utils.KZxingUtils;
import com.sdk.Qr_code.view.ViewfinderView;

import cn.oi.klittle.era.R;
import cn.oi.klittle.era.activity.photo.entity.KLocalMedia;
import cn.oi.klittle.era.activity.photo.manager.KPictureSelector;
import cn.oi.klittle.era.base.KBaseActivity;
import cn.oi.klittle.era.comm.KToast;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

//    /**
//     * 跳转到 二维码扫描界面
//     */
//    fun goCaptureActivity(nowActivity: Activity? = getActivity()) {
//        //需要相机权限
//        KPermissionUtils.requestPermissionsCamera {
//            if (it) {
//                        goActivityForResult(KCaptureActivity::class.java,KZxingUtils.requestCode_Qr)
//            } else {
//                KPermissionUtils.showFailure()
//            }
//        }
//    }
//   KUiHelper.goCaptureActivity()//fixme 跳转到 二维码扫描界面

//github地址：https://github.com/zxing/zxing
//jitpack引用地址：https://jitpack.io/private#zxing/zxing/zxing-3.4.0
//以下是引用
//  api 'com.google.zxing:android-core:3.3.0'
//  api 'com.google.zxing:android-core:3.3.0'
//  api 'com.google.zxing:core:3.3.2'
//  api 'com.google.zxing:core:3.3.3'//二维码库；之前是3.3.0;版本;3.3.3版本更好(读取速度更快)，能够兼容3.3.0的版本；不会报错；
//  api 'com.github.zxing.zxing:core:zxing-3.4.0'

//fixme 注意，一般都能够识别，如果识别不出来，请换个网站重新生成一下二维码。
//https://cli.im/text? 草料二维码 ，部分生成的二维码识别不出来，如：564654565645645646 ；111111111191111111234 ；J20190821012001C (新版的3.4.0好像能识别出来，亲测。)
//https://www.liantu.com/ 联图网二维码 这个生成的二维码质量比较好，一般都能识别的出来。

/**
 * 竖屏二维码扫描(二维码，条码都能扫描)
 * 二维码识别率比较高；但是条码识别率就不一定准确(因为条码很容易失真)
 */
public class CaptureActivity extends KBaseActivity implements Callback, OnClickListener {

    private CaptureActivityHandler handler;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    public InactivityTimer inactivityTimer;
    //扫描框
    public ViewfinderView viewfinderView;
    //相机预览在SurfaceView上。
    public SurfaceView surfaceView;
    // 判断surface有没有被绘制
    private boolean hasSurface;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CameraManager.init(getApplication());
        initUI();
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);// activity静止一段时间会自动关闭
    }

    //fixme 如果想要重新布局的话；子类可以重写这个方法。
    //fixme 重写布局时，一定要初始化viewfinderView和surfaceView；这两个必不可少。
    public void initUI() {
        setContentView(R.layout.kera_activity_capture);
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            if (hasSurface) {
                initCamera(surfaceHolder);
            } else {
                surfaceHolder.addCallback(this);
                surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            }
            decodeFormats = null;
            characterSet = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (handler != null) {
                handler.quitSynchronously();
                handler = null;
            }
            CameraManager.get().closeDriver();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        try {
            inactivityTimer.shutdown();
            super.onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 初始化照相机
    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            try {
                CameraManager.get().openDriver(this, surfaceHolder);
            } catch (IOException ioe) {
                return;
            } catch (RuntimeException e) {
                return;
            }
            if (handler == null) {
                handler = new CaptureActivityHandler(this, decodeFormats,
                        characterSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (!hasSurface) {
                hasSurface = true;
                initCamera(holder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }


    //重置，重新扫码
    private void reset() {
        try {
            viewfinderView.drawViewfinder();
            onPause();
            onResume();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //fixme 重要方法
    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == R.id.btn_ok) {
                //开始扫码/重新扫码
                reset();
            } else if (v.getId() == R.id.btn_openLight) {
                //开灯
                CameraManager.get().openLight();
            } else if (v.getId() == R.id.btn_offLight) {
                //关灯
                CameraManager.get().offLight();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Bitmap scanBitmap = null;

    /**
     * fixme 扫描完成;在CaptureActivityHandler里有调用。
     *
     * @param obj    ,obj.getText()扫描文本
     * @param bitmap ,扫描位图（二维码截图）
     */
    public void handleDecode(Result obj, Bitmap bitmap) {
        try {
            //Log.e("test", "扫描结果：\t" + obj + "\twidth:\t" + bitmap.getWidth() + "\theight:\t" + bitmap.getHeight());
            vibrator();//震动
            if (bitmap != null && !bitmap.isRecycled()) {
                //显示扫描结果位图
                viewfinderView.drawResultBitmap(bitmap);
            }
            if (obj != null) {
                //obj.toString() 二维码扫描结果。
                onQrScanResult(obj.toString().trim(), bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //fixme 二维码扫描结果，子类就重写这个方法即可。
    protected void onQrScanResult(String str, Bitmap bitmap) {
        try {
            //Log.e("test","二维码：\t"+str);
            scanBitmap = bitmap;
            if (inactivityTimer != null) {
                inactivityTimer.onActivity();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //震动(不需要任何权限)
    public void vibrator() {
        try {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(200L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    List<KLocalMedia> selectList = null;

    //图片上传
    public void pictrueSelector() {
        try {
//            PictureSelector.create(getActivity())
//                    .openGallery(PictureMimeType.ofImage()).imageSpanCount(4).maxSelectNum(1)
//                    //fixme 测试发现 宽和高直接压一半；这里就不需要压缩了。
//                    //fixme 识别二维码那里我已经做了是否压缩处理。（不要两边都压缩,效率慢）
//                    //.compress(true)//是否压缩；true压缩
//                    //.selectionMedia(selectList)//选中默认的
//                    //.minimumCompressSize(100)// 小于100kb的图片不压缩
//                    .forResult(PictureConfig.CHOOSE_REQUEST);
            KPictureSelector.INSTANCE.imageSpanCount(3).maxSelectNum(1).isCompress(true).isCamera(false).minimumCompressSize(100).forResult(getActivity(), new Function1<List<KLocalMedia>, Unit>() {
                @Override
                public Unit invoke(List<KLocalMedia> kLocalMedia) {

                    selectList = kLocalMedia;
                    if (selectList != null && selectList.size() > 0) {
                        KLocalMedia localMedia = selectList.get(0);
                        String path = localMedia.getPath();//原图路径
                        if (localMedia.isCut()) {
                            path = localMedia.getCutPath();//裁剪路径
                        }
                        if (localMedia.isCompressed()) {
                            path = localMedia.getCompressPath();//压缩后路径；最后进行。(图片选择器顺序是先裁剪最后再压缩的)
                        }
                        if (path != null && path.trim().length() > 0) {
                            //新开线程
                            final String finalPath = path;
                            new Thread() {
                                @Override
                                public void run() {
                                    super.run();
                                    try {
                                        //fixme 识别本地二维码图片；识别速度很快的，不需要加载进度条。
                                        String str = KZxingUtils.syncDecodeQRCode(finalPath);
                                        if (str != null && str.trim().length() > 0) {
                                            onQrScanResult(str.trim(), null);//回调，位图传空
                                        } else {
                                            onQrScanResultFail();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        }
                    }

                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    //扫描自己选择的本地二维图图片。
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        try {
//            super.onActivityResult(requestCode, resultCode, data);
//            if (resultCode == Activity.RESULT_OK) {
//                if (requestCode == PictureConfig.CHOOSE_REQUEST) {
//                    selectList = PictureSelector.obtainMultipleResult(data);
//                    if (selectList != null && selectList.size() > 0) {
//                        LocalMedia localMedia = selectList.get(0);
//                        String path = localMedia.getPath();//原图路径
//                        if (localMedia.isCut()) {
//                            path = localMedia.getCutPath();//裁剪路径
//                        }
//                        if (localMedia.isCompressed()) {
//                            path = localMedia.getCompressPath();//压缩后路径；最后进行。(图片选择器顺序是先裁剪最后再压缩的)
//                        }
//                        if (path != null && path.trim().length() > 0) {
//                            //新开线程
//                            final String finalPath = path;
//                            new Thread() {
//                                @Override
//                                public void run() {
//                                    super.run();
//                                    //fixme 识别本地二维码图片；识别速度很快的，不需要加载进度条。
//                                    String str = KZxingUtils.syncDecodeQRCode(finalPath);
//                                    if (str != null && str.trim().length() > 0) {
//                                        onQrScanResult(str.trim(), null);//回调，位图传空
//                                    } else {
//                                        onQrScanResultFail();
//                                    }
//                                }
//                            }.start();
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    //本地图片二维码识别失败的时候，回调。
    protected void onQrScanResultFail() {
        KToast.INSTANCE.showInfo(getString(R.string.kqr_fair), getActivity(), null);
    }

    //资源释放
    @Override
    public void finish() {
        try {
//            if (surfaceView != null) {
//                surfaceView.setVisibility(View.GONE);//这里不要隐藏，关闭的时候效果不好。
//            }
            surfaceView = null;
            //位图释放
            if (scanBitmap != null && !scanBitmap.isRecycled()) {
                scanBitmap.recycle();
                scanBitmap = null;
            }
            if (selectList != null) {
                selectList.clear();
                selectList = null;
                PictureFileUtils.deleteCacheDirFile(getActivity());//图片选择器缓存清除
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.finish();
    }

}
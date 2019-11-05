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

package com.sdk.Qr_code.code;

import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.sdk.Qr_code.act.CaptureActivity;
import com.sdk.Qr_code.manager.CameraManager;

/**
 * This class handles all the messaging which comprises the state machine for
 * capture.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivityHandler extends Handler {
    public static int auto_focus = 10001;
    public static int restart_preview = 10002;
    public static int decode_succeeded = 10003;
    public static int decode_failed = 10004;
    public static int decode = 10005;
    public static int return_scan_result = 10006;
    public static int launch_product_query = 10007;
    public static int quit2 = 10008;
    private static final String TAG = CaptureActivityHandler.class
            .getSimpleName();

    private CaptureActivity activity = null;
    private Activity activity2 = null;
    private final DecodeThread decodeThread;// 解码线程
    private State state;

    // 枚举当前的状态类型
    private enum State {
        PREVIEW, // 预览
        SUCCESS, // 成功
        DONE// 完成
    }

    public CaptureActivityHandler(CaptureActivity activity,
                                  Vector<BarcodeFormat> decodeFormats, String characterSet) {
        this.activity = activity;
        decodeThread = new DecodeThread(activity, decodeFormats, characterSet,
                new ViewfinderResultPointCallback(activity.getViewfinderView()));
        decodeThread.start();
        state = State.SUCCESS;

        // Start ourselves capturing previews and decoding.
        CameraManager.get().startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        try {
            int id = message.what;
            if (id == auto_focus) {
                if (state == State.PREVIEW) {
                    CameraManager.get().requestAutoFocus(this, auto_focus);
                }
            }
            if (id == restart_preview) {
                restartPreviewAndDecode();
            }
            if (id == decode_succeeded) {
                state = State.SUCCESS;
                Bundle bundle = message.getData();
                Bitmap barcode = bundle == null ? null : (Bitmap) bundle
                        .getParcelable(DecodeThread.BARCODE_BITMAP);
                activity.handleDecode((Result) message.obj, barcode);
            }
            if (id == decode_failed) {
                state = State.PREVIEW;
                CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
                        decode);
            }
            if (id == return_scan_result) {
                if (activity != null) {
                    activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
                    activity.finish();
                }
            }
            if (id == launch_product_query) {
                if (activity != null) {
                    String url = (String) message.obj;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    activity.startActivity(intent);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 退出同步
    public void quitSynchronously() {
        try{
            state = State.DONE;
            CameraManager.get().stopPreview();
            Message quit = Message.obtain(decodeThread.getHandler(), quit2);
            quit.sendToTarget();
            try {
                decodeThread.join();
            } catch (InterruptedException e) {
                // continue
            }

            // Be absolutely sure we don't send any queued up messages
            removeMessages(decode_succeeded);
            removeMessages(decode_failed);
        }catch (Exception e){e.printStackTrace();}
    }

    private void restartPreviewAndDecode() {
        try{
            if (state == State.SUCCESS) {
                state = State.PREVIEW;
                CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
                        decode);
                CameraManager.get().requestAutoFocus(this, auto_focus);
                activity.drawViewfinder();
            }
        }catch (Exception e){e.printStackTrace();}
    }

}

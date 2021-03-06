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

import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPointCallback;
import com.sdk.Qr_code.act.Qr_codeActivity;

/**
 * This thread does all the heavy lifting of decoding the images.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
final class DecodeThread extends Thread {

    public static final String BARCODE_BITMAP = "barcode_bitmap";

    private Qr_codeActivity activity = null;
    private Activity activity2 = null;
    private Hashtable<DecodeHintType, Object> hints;
    private Handler handler;
    private CountDownLatch handlerInitLatch;

    DecodeThread(Qr_codeActivity activity, Vector<BarcodeFormat> decodeFormats,
                 String characterSet, ResultPointCallback resultPointCallback) {

        try {
            this.activity = activity;
            handlerInitLatch = new CountDownLatch(1);

            hints = new Hashtable<DecodeHintType, Object>(3);

            // // The prefs can't change while the thread is running, so pick them
            // up once here.
            // if (decodeFormats == null || decodeFormats.isEmpty()) {
            // SharedPreferences prefs =
            // PreferenceManager.getDefaultSharedPreferences(activity);
            // decodeFormats = new Vector<BarcodeFormat>();
            // if (prefs.getBoolean(PreferencesActivity.KEY_DECODE_1D, true)) {
            // decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
            // }
            // if (prefs.getBoolean(PreferencesActivity.KEY_DECODE_QR, true)) {
            // decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
            // }
            // if (prefs.getBoolean(PreferencesActivity.KEY_DECODE_DATA_MATRIX,
            // true)) {
            // decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
            // }
            // }
            if (decodeFormats == null || decodeFormats.isEmpty()) {
                decodeFormats = new Vector<BarcodeFormat>();
                decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
                decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
                decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);

            }

            hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

            if (characterSet != null) {
                hints.put(DecodeHintType.CHARACTER_SET, characterSet);
            }

            hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK,
                    resultPointCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (Exception e) {
            // continue?
        }
        return handler;
    }

    @Override
    public void run() {
        try {
            Looper.prepare();
            handler = new DecodeHandler(activity, hints);
            handlerInitLatch.countDown();
            Looper.loop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

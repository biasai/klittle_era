package com.sdk.scan.act;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sdk.scan.RFID.KLF125KTagReadThread;
import com.sdk.scan.RFID.KUtil;

import org.jetbrains.annotations.Nullable;

import cn.oi.klittle.era.base.KBaseActivityManager;
import cn.oi.klittle.era.utils.KAppUtils;

/**
 * fixme RFID ID卡读取；针对：新版PDA扫描；品牌：alps；型号：Alps PDA
 * fixme 一般RFID id卡读卡器和NFC ic卡读卡器不可共用。
 */
public class KRfidActivity extends KNfcActivity {

    /**
     * fixme 判断是否位新版Alps PDA
     */
    public Boolean isNewPdA_Alpas() {
        String name = KAppUtils.getDeviceName();
        if (name != null && name.equals("Alps PDA")) {
            return true;
        }
        return false;
    }

    /**
     * fixme 判断是否开启ID卡扫描；如果开启了RFID;NFC就不会开启；RFID优先级高。
     *
     * @return
     */
    @Override
    public Boolean isEnableRFID() {
        if (isNewPdA_Alpas()) {
            return true;//开启ID卡扫描
        }
        return super.isEnableRFID();
    }

    private Handler handler = null;
    private KLF125KTagReadThread lf;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isEnableNFC() && isNewPdA_Alpas() && isEnableRFID()) {
            try {
                if (handler == null) {
                    //fixme 接收消息
                    handler = new Handler() {

                        @Override
                        public void handleMessage(Message msg) {
                            try {
                                //fixme 只对当前Activity进行回调。（防止多个Activity冲突）
                                //getActivity()会获取到子类实际对象
                                if (KBaseActivityManager.getInstance().getStackTopActivity() == getActivity()) {
                                    if (msg.what == KLF125KTagReadThread.MSG_RESULT) {
                                        if (isFastNfc()) {
                                            return;//fixme 防止快速刷卡
                                        }
                                        long id = msg.getData().getLong("id");
                                        //int country = msg.getData().getInt("country");
                                        //Log.e("MainActivity", "id = " + Long.valueOf(id) + "; country = " + country);
                                        KUtil.play(1);
                                        if (isEnableNF2C()) {
                                            //fixme 回调和NFC回调统一。
                                            onNfcResult(String.valueOf(id).trim());
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                }
                KUtil.getInstance(this);
                if (lf == null) {
                    lf = new KLF125KTagReadThread();
                    lf.setHandler(handler);
                    lf.init();
                    //lf.startRead();//启动读取卡;在onResume()里启动
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (lf != null) {
            lf.stopRead();//停止读卡（最好停用一下，防止多个Activity同时开启冲突）
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lf != null) {
            lf.startRead();//启动读取卡
        }
    }

    @Override
    public void finish() {
        super.finish();
        try {
            if (lf != null) {
                lf.stopRead();
                lf.close();//关闭读卡器
                lf.setHandler(null);
                lf = null;
            }
            if (handler != null) {
                // 移除所有消息
                handler.removeCallbacksAndMessages(null);
                // 或者移除单条消息
                handler.removeMessages(KLF125KTagReadThread.MSG_RESULT);
                handler = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

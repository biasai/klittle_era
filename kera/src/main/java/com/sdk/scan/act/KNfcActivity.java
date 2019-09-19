package com.sdk.scan.act;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;

import org.jetbrains.annotations.Nullable;

import cn.oi.klittle.era.base.KBaseActivity;
import cn.oi.klittle.era.utils.KIntentUtils;
import cn.oi.klittle.era.utils.KLoggerUtils;

//   fixme 使用案例，子类主要重写以下方法即可

//    override fun isEnableNFC(): Boolean {
//        return true//开始NFC功能
//    }
//
//    override fun onNfcResult(cardNo: String) {
//        super.onNfcResult(cardNo)
//        //NFC读卡信息回调
//    }
//
//    override fun onNfcNotEnabled() {
//        super.onNfcNotEnabled()
//        //需要开启NFC读卡功能，是否开启
//        showMsg(getString(R.string.knf_timi))?.apply {
//            positive(getString(R.string.kkaiqi)) {
//                //开启
//                goNFCSetting()
//            }
//        }
//    }
//
//    override fun onNfcNotSupport() {
//        super.onNfcNotSupport()
//        //该设备不支持NFC读卡功能
//        KToast.showError(getString(R.string.knfc_no), this)
//    }

/**
 * 子类直接重写 onNfcResult()方法，直接获取IC卡的读取结果即可。
 * fixme 启动模式最好设置成 android:launchMode="singleTask" 防止刷卡时多次重复跳转
 * fixme 注意看看清单AndroidManifest.xml配置。
 * fixme [nfc的权限问题；始终无法动态判断；必须用户手动开启。]
 */
public class KNfcActivity extends KBaseActivity {

    //fixme 是否开启NFC;子类可以重写。
    public Boolean isEnableNFC() {
        return true;//默认开启
    }

    /**
     * fixme 处理nfc读取结果回调（isEnableNFC()开启了才会回调）;子类可以重写
     *
     * @param cardNo 卡号
     */
    protected void onNfcResult(String cardNo) {
    }

    /**
     * 设备不支持NFC刷卡功能时调用,在onStart()里调用，只调用一次。
     */
    protected void onNfcNotSupport() {
    }

    /**
     * 设备支持NFC功能，但是NFC功能没有开启时调用。在onResume()里调用，每次重新进入界面都会刷新。
     */
    protected void onNfcNotEnabled() {
    }

    /**
     * 判断是否支持NFC功能
     */
    public Boolean isNfcSupport() {
        return isNfcSupport;
    }

    /**
     * 判断NFC功能是否开启
     */
    public Boolean isNfcEnabled() {
        if (isNfcSupport && mNfcAdapter != null) {
            return mNfcAdapter.isEnabled();
        }
        return false;
    }

    /**
     * 关闭nfc读卡功能【nfc实际功能没有关闭，只是当前页不再监听nfc读卡】
     */
    public void disableNfc() {
        try {
            if (mNfcAdapter != null) {
                mNfcAdapter.disableForegroundDispatch(this);// 取消调度
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开启nfc读卡功能【当前页面继续监听nfc读卡功能】
     */
    public void enableNfc() {
        try {
            if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
                if (pendingIntent == null) {
                    pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
                }
                if (pendingIntent != null) {
                    //fixme 这行代码是添加调度，效果是读标签的时候不会弹出候选程序，直接用本程序处理(当前Activity处理，就不会再弹系统询问框了。)
                    mNfcAdapter.enableForegroundDispatch(this, pendingIntent, FILTERS, TECHLISTS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转NFC设置界面
     */
    public void goNFCSetting() {
        KIntentUtils.INSTANCE.goNFCSetting(this);
    }

    // nfc
    protected NfcAdapter mNfcAdapter;
    protected PendingIntent pendingIntent;
    public static String[][] TECHLISTS; //NFC技术列表
    public static IntentFilter[] FILTERS; //过滤器
    public Boolean isNfcSupport = true;//fixme 判断是否支持NFC功能

    static {
        try {
            TECHLISTS = new String[][]{{NfcA.class.getName()}};
            FILTERS = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED, "*/*")};
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isEnableNFC()) {
            initNfc();
        }
    }

    //处理NFC触发(fixme nfc读卡会调用这个方法)
    // fixme [如果Activity还没实例化的话；onNewIntent()可能会比onCreate()先执行。];如在系统界面直接刷ic卡，然后直接跳转到该界面。
    @Override
    protected void onNewIntent(Intent intent) {
        if (isEnableNFC()) {
            readFromNfc(intent);
        }
    }

    //初始化nfc配置
    private void initNfc() {
        try {
            if (mNfcAdapter == null) {
                mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
            }
            if (mNfcAdapter == null) {
                //设备不支持NFC读取
                isNfcSupport = false;
            } else if (!mNfcAdapter.isEnabled()) {
                //请在系统设置中先启用NFC功能！
            } else if (pendingIntent == null) {
                pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
                onNewIntent(getIntent());
            }
        } catch (Exception e) {
            isNfcSupport = false;
            KLoggerUtils.INSTANCE.e("NFC初始化异常：\t" + e.getMessage());
        }
    }

    @Override
    protected void onStart() {
        try {
            super.onStart();
            if (!isNfcSupport) {
                if (isEnableNFC()) {
                    onNfcNotSupport();//fixme 设备不支持NFC读卡功能
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
            if (isEnableNFC() && isNfcSupport) {
                enableNfc();
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            //NFC开启和关闭需要时间不是实时的，所以延迟一下,单位毫秒。
                            //测试发现，大概需要2秒的时候（太慢了，等待2秒不太好）。关很快的，就是开需要点时间。
                            // 这里就等待200毫秒，太长了不好。（只有用户点击开关按钮之后不是马上返回，基本都没问题）
                            sleep(200);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (!isNfcEnabled()) {
                                            onNfcNotEnabled();//fixme NFC功能没有开启
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isEnableNFC()) {
            disableNfc();
        }
    }

    private void readFromNfc(Intent intent) {
        try {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (null != tag) {
                byte[] tagId = tag.getId();
                // 15AF503D
                String str = bytesToHexString(tagId);
                // 363810877
                String nfcCardNo = hexToDecString(str);
                //回调
                if (nfcCardNo != null && nfcCardNo.trim().length() > 0) {
                    onNfcResult(nfcCardNo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String bytesToHexString(byte[] bArray) {
        StringBuilder sb = new StringBuilder(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    private String hexToDecString(String hex) {
        int before = (int) Long.parseLong(hex, 16); //大于Integer.MAX_VALUE时出现截断，估计就不对了
        int r24 = before >> 24 & 0x000000FF;
        int r8 = before >> 8 & 0x0000FF00;
        int l8 = before << 8 & 0x00FF0000;
        int l24 = before << 24 & 0xFF000000;

        return String.valueOf(Long.parseLong(Integer.toHexString((r24 | r8 | l8 | l24)), 16));
    }

    @Override
    public void finish() {
        super.finish();
        mNfcAdapter = null;
        pendingIntent = null;
    }
}

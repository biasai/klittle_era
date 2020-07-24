package com.sdk.scan.act;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.util.Log;

import org.jetbrains.annotations.Nullable;

import cn.oi.klittle.era.base.KBaseActivity;
import cn.oi.klittle.era.exception.KCatchException;
import cn.oi.klittle.era.utils.KIntentUtils;
import cn.oi.klittle.era.utils.KLoggerUtils;
import cn.oi.klittle.era.utils.KStringUtils;

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

    //fixme 是否开启了RFID功能；默认不开启，如果开启了，NFC就不会开启了。优先级比NFC高；RFID和NFC不可兼容。即硬件目前只支持其中一种；不能同时支持。
    public Boolean isEnableRFID() {
        return false;//默认不开启
    }

    //新增方法，控制NFC回调开关。
    public Boolean isEnableNF2C() {
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
     * fixme 设备支持NFC,但是开机启动时，NFC服务还没有开启导致的。从而异常。导致判断失误。这时需要重启NFC刷卡功能即可。
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
        if (isEnableRFID()) {
            //fixme 如果开了RFID扫描；NFC就不开。
            return;
        }
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
        if (isEnableRFID()) {
            //fixme 如果开了RFID扫描；NFC就不开。
            return;
        }
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
        try {
            if (isEnableNFC()) {
                initNfc();
                if (isNfcSupport) {
                    enableNfc();
                }
                lastClickTime_nfc = 0;//fixme 防止第一次进入，刷卡无效。
            }
        } catch (Exception e) {
            e.printStackTrace();
            //KLoggerUtils.INSTANCE.e("NFC初始化异常：\t" + e.getMessage());
        }
    }

    //处理NFC触发(fixme nfc读卡会调用这个方法)
    // fixme [如果Activity还没实例化的话；onNewIntent()可能会比onCreate()先执行。];如在系统界面直接刷ic卡，然后直接跳转到该界面。
    //fixme Activity初始化的时候，在onCreate（）前面会执行一次。
    @Override
    protected void onNewIntent(Intent intent) {
        if (isEnableNFC()) {
            readFromNfc(intent);
        }
    }

    //初始化nfc配置
    private void initNfc() {
        if (isEnableRFID()) {
            //fixme 如果开了RFID扫描；NFC就不开。
            return;
        }
        if (mNfcAdapter == null) {
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
                //fixme 这种异常，一般都是开机启动时，NFC服务还没有开启导致的。
                KLoggerUtils.INSTANCE.e("mNfcAdapter:\t" + mNfcAdapter);
                KLoggerUtils.INSTANCE.e("KNfcActivity NFC初始化异常：\t" + e.getMessage(), true);
                mNfcAdapter = null;
            }
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
            if (isEnableNFC()) {
                initNfc();//fixme 防止未初始化异常，未完成。再初始化一次。
            }
            if (isEnableNFC() && isNfcSupport) {
                enableNfc();
                if (!isNfcEnabled()) {
                    //NFC功能没有开启回调
                    //fixme 关很快的，就是开需要点时间。
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
            } else {
                try {
                    if (!isNfcSupport) {
                        if (isEnableNFC()) {
                            onNfcNotSupport();//fixme 设备不支持NFC读卡功能(或者NFC刷卡失败。)
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

    //fixme NFC刷卡读取
    private void readFromNfc(Intent intent) {
        if (intent == null) {
            return;
        }
        try {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (null != tag) {
                byte[] tagId = tag.getId();
                // 15AF503D
                String str = bytesToHexString(tagId);
                //KLoggerUtils.INSTANCE.e("STR:\t"+str);
                // 363810877
                String nfcCardNo = hexToDecString(str);
                //KLoggerUtils.INSTANCE.e("nfcCardNo:\t"+nfcCardNo,true);
                //回调
                if (nfcCardNo != null && nfcCardNo.trim().length() > 0) {
                    if (isEnableNF2C()) {
                        if (isFastNfc()) {
                            return;//fixme 防止快速刷卡
                        }
                        onNfcResult(nfcCardNo);
                    }
                } else {
                    KLoggerUtils.INSTANCE.e("NFC刷卡为空,nfcCardNo:\t" + nfcCardNo, true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            KLoggerUtils.INSTANCE.e("NFC刷卡异常：\t" + KCatchException.getExceptionMsg(e), true);
        }
    }

    //原厂的
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

    //原厂的
    private String hexToDecString(String hex) {
        try {
            int before = (int) Long.parseLong(hex, 16); //大于Integer.MAX_VALUE时出现截断，估计就不对了
            int r24 = before >> 24 & 0x000000FF;
            int r8 = before >> 8 & 0x0000FF00;
            int l8 = before << 8 & 0x00FF0000;
            int l24 = before << 24 & 0xFF000000;
            //fixme Long类型；会去除前面的0；如：0758694741 会变成 758694741
            //fixme 所以，如果首字符是0；本来是十位，读取的时候就是九位了；位数就发生变化了。（目前没办法知道前面是否带0，且带了几个0）
            return String.valueOf(Long.parseLong(Integer.toHexString((r24 | r8 | l8 | l24)), 16));
        } catch (Exception e) {
            e.printStackTrace();
            KLoggerUtils.INSTANCE.e("KNfcActivity NFC读卡异常：\t" + e.getMessage(), true);
        }
        return "";
    }

    //和bytesToHexString效果是一样的。
    private String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
                "B", "C", "D", "E", "F"};
        String out = "";
        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    @Override
    public void finish() {
        super.finish();
        mNfcAdapter = null;
        pendingIntent = null;
    }

    // 两次刷卡之间的点击间隔不能少于350毫秒（即0.35秒）
    static long MIN_CLICK_DELAY_TIME_nfc = 350;
    static long lastClickTime_nfc = 0;//记录最后一次刷卡时间

    //判断是否快速刷卡
    public static boolean isFastNfc() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime_nfc) <= MIN_CLICK_DELAY_TIME_nfc) {
            flag = true;//快速点击
        }
        lastClickTime_nfc = curClickTime;
        return flag;
    }

}

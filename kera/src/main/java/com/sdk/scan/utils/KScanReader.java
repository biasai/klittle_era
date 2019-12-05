package com.sdk.scan.utils;

import android.content.Context;
import android.content.Intent;

/**
 * fixme 新版PDA扫描；品牌：alps；型号：Alps PDA
 */
public class KScanReader {

    public static final String ACTION_SCAN_RESULT = "com.action.SCAN_RESULT" ;

    public static final String SCAN_RESULT = "scanContext" ;
    public static final String ACTION_START_SCAN = "com.action.START_SCAN" ;
    public static final String ACTION_STOP_SCAN = "com.action.STOP_SCAN" ;
    public static final String ACTION_INIT = "com.action.INIT_SCAN" ;
    public static final String ACTION_KILL = "com.action.KILL_SCAN" ;

    private Context context ;
    public KScanReader(Context context){
        this.context = context ;
    }


    public void init() {
        Intent intent = new Intent();
        intent.setAction(ACTION_INIT) ;

        context.sendBroadcast(intent);
    }


    public void startScan() {
        Intent intent = new Intent();
        intent.putExtra("third", true) ;
        intent.setAction(ACTION_START_SCAN);
        context.sendBroadcast(intent);
    }

    public void stopScan() {
        Intent intent = new Intent();
        intent.setAction(ACTION_STOP_SCAN);
        context.sendBroadcast(intent);
    }

    public void closeScan() {
        Intent intent = new Intent();
        intent.setAction(ACTION_KILL);
        intent.putExtra("third", false) ;
        context.sendBroadcast(intent);
    }
}

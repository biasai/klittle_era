package com.sdk.scan.RFID;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.pl.serialport.SerialPort;
import com.znht.iodev2.PowerCtl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class KLF125KTagReadThread {

    private String path = "/dev/ttysWK2" ;
    private int rate = 9600 ;
    private PowerCtl powerCtl ;
    private SerialPort mSerial ;
    private InputStream in ;
    private OutputStream out ;
    private Handler handler ;
    private ReadThread readThread ;
    private boolean isRead = false ;

    public static final int MSG_RESULT = 1101 ;

    public void setPath(String path) {
        this.path = path ;
    }

    public void setHandler(Handler handler) {
        this.handler = handler ;
    }

    //init thread
    public void init() {
        Log.e("LF134KTag", "init");
        powerCtl = new PowerCtl() ;
        powerCtl.identity_uhf_power(1);
        powerCtl.identity_ctl(1);
        powerCtl.uhf_ctl(1);
        try {
            mSerial = new SerialPort(path,rate,0) ;
            in = mSerial.getInputStream() ;
            out = mSerial.getOutputStream() ;
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startRead() {
        isRead = true ;
        if (readThread == null) {
            readThread = new ReadThread() ;
            readThread.start();
        }
    }

    public void stopRead() {
//        if (readThread != null) {
//            readThread.interrupt();
//        }
        isRead = false ;
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[256];
                    if (in == null) return;
                    size = in.read(buffer);
                    if (size > 0) {
                        //解析数据
                        //scanFlag = false ;
                        //onDataReceived(buffer, size);
                        Log.e("LF125KTag", KTools.Bytes2HexString(buffer, size));
                        getID(buffer, size) ;
                        //powerCtl.scan_trig(1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    //LF134.2 = 02413245423737423930303438333031313030303030303030303000FF03
    //LF125K = 02373930303838384431466303
    private KLFTag getID(byte[] buff, int size) {
        KLFTag tag = new KLFTag() ;
        long id = 0L ;
        byte[] idBuff = new byte[8];
        byte[] user = new byte[2];
        if (buff[0] != 0x02 || buff[size - 1] != 03 || size < 10) {
            return null ;
        }
        //ID
        System.arraycopy(buff, 3, idBuff, 0, 8);
        //
        System.arraycopy(buff, 1, user, 0, 2);
        String idStr = new String(idBuff) ;
        //将十六进制数据解析为数字类型的
        id = Long.parseLong(idStr, 16) ;
        tag.setId(id);
        String countryStr = new String(user);
        countryStr = "0" + countryStr  ;
        if (handler != null && isRead) {
            Message msg = new Message() ;
            msg.what = MSG_RESULT ;
            Bundle b = new Bundle();
            b.putLong("id", id);
            b.putInt("user", Integer.parseInt(countryStr, 16));
            msg.setData(b);
            handler.sendMessage(msg);
        }
//        Log.e("LF134KTag", "String idStr = " + idStr);
//        Log.e("LF134KTag", "long idStr = " + Long.parseLong(idStr, 16));
//        Log.e("LF134KTag", "long countryStr = " + Long.parseLong(countryStr, 16));
        return tag ;
    }



    //
    public static void reverseOrderArray(byte[] arr) {
        // 把原数组元素倒序遍历
        for(int i = 0; i < arr.length/2; i++) {
            // 把数组中的元素收尾交换
            byte temp = arr[i];
            arr[i] = arr[arr.length - i - 1];
            arr[arr.length - i - 1] = temp;
        }
    }


    public void close() {
        if (mSerial != null) {
            mSerial.close();
        }
        if (powerCtl != null) {
            powerCtl.identity_uhf_power(0);
            powerCtl.identity_ctl(0);
            powerCtl.uhf_ctl(0);
        }

    }

}

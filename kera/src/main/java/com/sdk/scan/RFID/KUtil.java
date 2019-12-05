package com.sdk.scan.RFID;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.Map;

import cn.oi.klittle.era.R;

public class KUtil {


	public static SoundPool sp ;
	public static Map<Integer, Integer> suondMap;
	public static Context context;

	//初始化声音池
	public static SoundPool getInstance(Context context){
		KUtil.context = context;
		//sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
		if(sp==null){
			sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
		}
		sp.load(context, R.raw.kpda_rfid_msg, 1);
		return  sp;
	}

	//播放声音池声音
	public static  void play(int soundID){
		sp.play(soundID, 1, 1, 0, 0, 1);
	}

}

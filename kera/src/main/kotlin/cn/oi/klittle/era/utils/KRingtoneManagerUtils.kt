package cn.oi.klittle.era.utils

import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import cn.oi.klittle.era.base.KBaseApplication
import kotlinx.coroutines.experimental.async

import java.util.ArrayList

/**
 * fixme 系统铃声;KIntentUtils.goSoundSetting()跳转到声音大小调节设置界面。
 */
object KRingtoneManagerUtils {
    var listRingTone: ArrayList<Ringtone>? = ArrayList()//铃声集合
    var index = 0//当前选择铃声下标

    init {
        init()
    }

    private var isInit: Boolean = false//判断是否初始化成功
    //初始化(重新初始化)
    fun init() {
        async {
            try {
                isInit = false
                val ringtoneManager = RingtoneManager(KBaseApplication.getInstance()) // 铃声管理器
                val cursor = ringtoneManager.cursor //获取铃声表,根据表名取值
                val count = cursor.count //获取铃声列表数量
                listRingTone?.clear()
                for (i in 0 until count) {
                    listRingTone?.add(ringtoneManager.getRingtone(i))
                }
                isInit = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //获取对应铃声的名字
    fun getTitle(index: Int): String? {
        try {
            if (isInit) {
                return if (listRingTone != null && listRingTone!!.size > index) {
                    listRingTone!![index].getTitle(KBaseApplication.getInstance())
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    //播放铃声(目前只播放异常，不会重复播放)
    fun play(index: Int = this.index) {
        if (isInit) {
            if (listRingTone != null && listRingTone!!.size > index) {
                try {
                    if (Build.VERSION.SDK_INT >= 28) {//28是android 9.0
                        //                    listRingTone.get(index).setLooping(true);//是否循环播放，默认值为true，API28或以上才能操作;fixme 声音默认是不循环的。
                        //                    listRingTone.get(index).setVolume(1.0f);//设置音量大小，值范围0~1，API28或以上才能操作
                    }
                    listRingTone?.let {
                        if (!listRingTone!![index].isPlaying) {//不在播放状态
                            listRingTone!![index].play()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    //停止播放
    fun stop(index: Int = this.index) {
        if (isInit) {
            if (listRingTone != null && listRingTone!!.size > index) {
                try {
                    listRingTone?.let {
                        if (listRingTone!![index].isPlaying) {
                            listRingTone!![index].stop()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

}

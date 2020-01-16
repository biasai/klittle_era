package cn.oi.klittle.era.utils

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import cn.oi.klittle.era.base.KBaseApplication
import kotlinx.coroutines.experimental.async

import java.util.ArrayList


/**
 * fixme 系统铃声;KIntentUtils.goSoundSetting()跳转到声音大小调节设置界面。
 */
object KRingtoneManagerUtils {
    var listRingTone = mutableListOf<Ringtone>()//fixme 铃声集合
    private var index_key = "kera_ringtone_index_key"
    var index = 0
        //当前选择铃声下标
        set(value) {
            stop()//fixme 下标改变时，先停止播放。
            if (field != value) {
                field = value
                async {
                    try {
                        KCacheUtils.putSecret(index_key, value.toString())//fixme 保存下标
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

    init {
        reset(true)
        try {
            KCacheUtils.getSecret(index_key)?.let {
                it.toString().toInt()?.let {
                    index = it//fixme 获取缓存下标
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private var isInit: Boolean = false//判断是否初始化成功
    /**
     * fixme 重置，初始化(重新初始化)
     * @param isForce 是否强制重置。
     */
    fun reset(isForce: Boolean = false) {
        if (!isInit || isForce) {
            isInit = true
            try {
                stop()//fixme 初始化之前，先停止之前播放的。
                val ringtoneManager = RingtoneManager(KBaseApplication.getInstance()) // 铃声管理器
                val cursor = ringtoneManager.cursor //获取铃声表,根据表名取值
                val count = cursor.count //获取铃声列表数量
                listRingTone?.clear()
                for (i in 0 until count) {
                    listRingTone?.add(ringtoneManager.getRingtone(i))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //获取对应铃声的名字
    fun getTitle(index: Int = this.index): String? {
        try {
            if (isInit) {
                listRingTone?.let {
                    if (it.size > 0 && it.size > index) {
                        return if (listRingTone != null && listRingTone!!.size > index) {
                            listRingTone!![index].getTitle(KBaseApplication.getInstance())
                        } else null
                    }
                }
            } else {
                reset()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 播放铃声
     * @param index 铃声下标
     * @param isLoop 是否循环播放。
     * @param delay 设定时长(单位毫秒，1000是一秒)，自动停止播放(循环播放下有效)。
     */
    fun play(index: Int = this.index, isLoop: Boolean = false, delay: Long = 0) {
        if (isInit) {
            try {
                listRingTone?.let {
                    if (it.size > 0 && it.size > index) {
                        stop()//fixme 停止之前播放的。
                        if (isLoop) {
                            listRingTone!![index].setStreamType(AudioManager.STREAM_RING);//因为rt.stop()使得MediaPlayer置null,所以要重新创建（具体看源码）;fixme 这一步必不可少。不然无法重复播放。
                            setRingtoneRepeat(listRingTone!![index])
                            if (delay > 10) {
                                var stopIndex = index
                                async {
                                    kotlinx.coroutines.experimental.delay(delay)
                                    stop(stopIndex)//fixme 指定时间自动停止播放。
                                }
                            }
                        }
                        if (!listRingTone!![index].isPlaying) {//不在播放状态
                            listRingTone!![index].play()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            reset()
        }
    }

    //fixme 通过反射设置循环播放;Ringtone内部封装的就是MediaPlayer;亲测有效。
    private fun setRingtoneRepeat(ringtone: Ringtone) {
        //mAudio,mLocalPlayer
        try {
            var clazz = Ringtone::class.java
            var audio = clazz.getDeclaredField("mLocalPlayer")
            audio?.isAccessible = true
            audio.get(ringtone)?.let {
                if (it is MediaPlayer) {
                    var target = audio.get(ringtone) as MediaPlayer
                    target.setLooping(true)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            KLoggerUtils.e("KRingtoneManagerUtils反射异常：\t" + e.message)
            try {
                var clazz = Ringtone::class.java
                var audio = clazz.getDeclaredField("mAudio")
                audio?.isAccessible = true
                audio.get(ringtone)?.let {
                    if (it is MediaPlayer) {
                        var target = audio.get(ringtone) as MediaPlayer
                        target.setLooping(true)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                KLoggerUtils.e("KRingtoneManagerUtils反射异常2：\t" + e.message)
            }
        }
    }

    //停止播放
    fun stop(index: Int = this.index) {
        if (isInit) {
            try {
                listRingTone?.let {
                    if (it.size > index && it.size > 0)
                        it[index]?.let {
                            if (it.isPlaying) {
                                it.stop()
                            }
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            //reset()//停止不需要初始化。
        }
    }

}

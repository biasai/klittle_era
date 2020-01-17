package cn.oi.klittle.era.utils

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import cn.oi.klittle.era.base.KBaseApplication
import kotlinx.coroutines.experimental.async

/**
 * fixme 系统铃声;KIntentUtils.goSoundSetting()跳转到声音大小调节设置界面。
 */
object KRingtoneManagerUtils {
    private var listRingTone: MutableList<Ringtone>? = mutableListOf()//fixme 铃声集合

    /**
     * fixme 获取所有铃声
     */
    fun getRingToneDatas(): MutableList<Ringtone>? {
        if (listRingTone == null) {
            listRingTone = mutableListOf()
        }
        listRingTone?.let {
            if (it.size > 0) {
                return it
            }
        }
        var t = System.currentTimeMillis()
        //KLoggerUtils.e("获取所有铃声=开始")
        ringtoneManager?.let {
            var cursor = it.cursor //获取铃声表,根据表名取值
            var count = cursor.count //获取铃声列表数量
            listRingTone?.clear()
            //加载所有铃声
            for (i in 0 until count) {
                listRingTone?.add(it.getRingtone(i))//fixme ringtoneManager.getRingtone(i) 这一步是耗时的操作。(大约获取一个需要116毫秒)
            }
        }
        //KLoggerUtils.e("获取所有铃声=结束:\t" + (System.currentTimeMillis() - t))
        return listRingTone
    }

    private var index_key = "kera_ringtone_index_key"
    var index = 0
        //当前选择铃声下标
        set(value) {
            try {
                stop()//fixme 下标改变时，先停止播放。
                if (field != value) {
                    field = value
                    listRingTone?.let {
                        if (it.size > value) {
                            ringtone = it[index]//fixme 记录当前铃声
                        }
                    }
                    async {
                        try {
                            KCacheUtils.putSecret(index_key, value.toString())//fixme 保存下标
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    var ringtone: Ringtone? = null//当前铃声

    init {
        reset()
    }

    var ringtoneManager: RingtoneManager? = null
    private var isResting: Boolean = false//判断reset()方法是否正在执行
    /**
     * fixme 重置
     * @param isAll true加载所有铃声，false只加载当前铃声
     */
    fun reset() {
        if (isResting) {
            return
        }
        isResting = true//正在执行
        try {
            var t = System.currentTimeMillis()
            //KLoggerUtils.e("初始开始")
            stop()//fixme 初始化之前，先停止之前播放的。
            if (ringtoneManager == null) {
                ringtoneManager = RingtoneManager(KBaseApplication.getInstance()) // 铃声管理器
            }
            ringtoneManager?.let {
                var cursor = it.cursor //获取铃声表,根据表名取值
                var count = cursor.count //获取铃声列表数量
                KCacheUtils.getSecret(index_key)?.let {
                    it.toString().toInt()?.let {
                        index = it//fixme 获取缓存下标
                    }
                }
                //只加载当前选中铃声
                if (index < count) {
                    ringtone = it.getRingtone(index)//fixme 获取这一步是耗时操作(大约获取一个需要116毫秒)
                }
                //KLoggerUtils.e("初始化结束:\t" + (System.currentTimeMillis() - t))
            }
        } catch (e: Exception) {
            KLoggerUtils.e("KRingtoneManagerUtils重置异常:\t" + e.message)
            e.printStackTrace()
        }
        isResting = false//执行完毕
    }

    /**
     * 清除所有
     */
    fun clearAll() {
        listRingTone?.let {
            if (it.size > 0) {
                try {
                    stop()
                    listRingTone?.clear()
                    listRingTone = null
                    ringtone = null
                    ringtoneManager = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 清除铃声集合
     * fixme 不需要清除，不怎么占内存。主要是获取的时候耗时。本身不占多少内存。
     */
    fun clearListRingTone() {
        listRingTone?.let {
            if (it.size > 0) {
                stop()
                listRingTone?.clear()
                listRingTone = null
            }
        }
    }

    //根据下标获取对应铃声的名字
    fun getTitle(index: Int): String? {
        try {
            listRingTone?.let {
                if (it.size > 0 && it.size > index) {
                    return it[index].getTitle(KBaseApplication.getInstance())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    //获取当前铃声的名字
    fun getTitle(): String? {
        try {
            return ringtone?.getTitle(KBaseApplication.getInstance())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 根据下标播放指定铃声
     * @param index 铃声下标
     * @param isLoop 是否循环播放。
     * @param delay 设定时长(单位毫秒，1000是一秒)，自动停止播放(循环播放下有效)。
     */
    fun play(index: Int, isLoop: Boolean = false, delay: Long = 0) {
        try {
            listRingTone?.let {
                if (it.size > 0 && it.size > index) {
                    stop()//fixme 停止之前播放的。
                    it[index]?.let {
                        if (isLoop) {
                            it.setStreamType(AudioManager.STREAM_RING);//因为rt.stop()使得MediaPlayer置null,所以要重新创建（具体看源码）;fixme 这一步必不可少。不然无法重复播放。
                            setRingtoneRepeat(it)
                            if (delay > 10) {
                                var stopIndex = index
                                async {
                                    kotlinx.coroutines.experimental.delay(delay)
                                    stop(stopIndex)//fixme 指定时间自动停止播放。
                                }
                            }
                        }
                        if (!it.isPlaying) {//不在播放状态
                            it.play()//播放
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 播放当前铃声
     * @param isLoop 是否循环播放。
     * @param delay 设定时长(单位毫秒，1000是一秒)，自动停止播放(循环播放下有效)。
     */
    fun play(isLoop: Boolean = false, delay: Long = 0) {
        try {
            stop()//fixme 停止之前播放的。
            ringtone?.let {
                if (isLoop) {
                    it.setStreamType(AudioManager.STREAM_RING);//因为rt.stop()使得MediaPlayer置null,所以要重新创建（具体看源码）;fixme 这一步必不可少。不然无法重复播放。
                    setRingtoneRepeat(it)
                    if (delay > 10) {
                        var stopIndex = index
                        async {
                            kotlinx.coroutines.experimental.delay(delay)
                            stop(stopIndex)//fixme 指定时间自动停止播放。
                        }
                    }
                }
                if (!it.isPlaying) {//不在播放状态
                    it.play()//播放
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //停止播放
    fun stop(index: Int = this.index) {
        try {
            ringtone?.let {
                if (it.isPlaying) {
                    it.stop()
                }
            }
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

}

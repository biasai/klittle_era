package cn.oi.klittle.era.activity.photo.manager

import android.support.v4.app.FragmentActivity
import cn.oi.klittle.era.activity.photo.config.PictureConfig
import cn.oi.klittle.era.activity.photo.model.LocalMediaLoader

/**
 * 图片加载管理类，继承LocalMediaLoader(PictureSelector类库里的)
 * @param activity FragmentActivity
 * @param type 类型； 图片:PictureConfig.TYPE_IMAGE;视频:PictureConfig.TYPE_VIDEO;音频:PictureConfig.TYPE_AUDIO;全部:PictureConfig.TYPE_ALL;
 * @param isGif 是否显示gif图片
 * @param videoMaxS 视频最长时间（单位毫秒，1000等于1秒）；0表示不计时常，会显示所有的。
 * @param videoMinS 视频最短时间
 */
open class KLocalMediaLoader(activity: FragmentActivity, type: Int = PictureConfig.TYPE_IMAGE, isGif: Boolean = false, videoMaxS: Long = 0, videoMinS: Long = 0) : LocalMediaLoader(activity, type, isGif, videoMaxS, videoMinS) {
//fixme 能够读取当前应用缓存目录(cache)下的图片。
//fixme 视频和gig动图，获取的都是第一帧的图片。
}
package cn.oi.klittle.era.activity.video

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import cn.oi.klittle.era.base.KBaseActivity
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.widget.video.KMediaController2
import cn.oi.klittle.era.widget.video.KVideoView
import org.jetbrains.anko.*

class KScreenVideoUi : KBaseUi() {

    var view: View? = null
    var video: KVideoView? = null
    var mediaController:KMediaController2?=null
    override fun createView(ctx: Context?): View? {
        view?.let {
            return it
        }
        view = ctx?.UI {
            verticalLayout {
                relativeLayout {
                    backgroundColor = Color.BLACK
                    video = kvideoView {
                    }.lparams {
                        width = wrapContent
                        height = wrapContent
                        ctx?.let {
                            if (it is KBaseActivity){
                                if (it.isPortrait()){
                                    //竖屏（以屏幕宽为标准，即以短边为标准，另一边自适应）
                                    width = matchParent
                                    height = wrapContent
                                }else{
                                    //横屏（以屏幕高为标准，fixme 即以短边为标准，另一边自适应）
                                    width = wrapContent
                                    height = matchParent
                                }
                            }
                        }
                        centerInParent()//居中
                    }
                    mediaController=KMediaController2(this, video)
                    mediaController?.let {
                        //最外层父容器
                        it.relativeLayout?.lparams {
                            centerHorizontally()
                            alignParentBottom()
                        }
                    }
                }.lparams {
                    width = matchParent
                    height = matchParent
                }
            }
        }?.view
        return view
    }

    override fun destroy(activity: Activity?) {
        super.destroy(activity)
        view=null
        video?.onDestory()
        video=null
        mediaController?.onDestroy()
        mediaController=null
    }
}
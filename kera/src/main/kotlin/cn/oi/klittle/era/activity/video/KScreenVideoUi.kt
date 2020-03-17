package cn.oi.klittle.era.activity.video

import android.content.Context
import android.graphics.Color
import android.view.View
import cn.oi.klittle.era.base.KBaseUi
import org.jetbrains.anko.*

class KScreenVideoUi : KBaseUi() {

    var view:View?=null
    override fun createView(ctx: Context?): View? {
        view?.let {
            return it
        }
        view= ctx?.UI {
            verticalLayout {
                backgroundColor = Color.WHITE
                button {
                    text = "Hello"
                }
            }
        }?.view
        return view
    }
}
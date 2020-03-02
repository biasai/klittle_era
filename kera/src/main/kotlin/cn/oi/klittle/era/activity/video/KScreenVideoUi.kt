package cn.oi.klittle.era.activity.video

import android.content.Context
import android.graphics.Color
import android.view.View
import cn.oi.klittle.era.base.KBaseUi
import org.jetbrains.anko.*

class KScreenVideoUi : KBaseUi() {

    override fun createView(ctx: Context?): View? {
        return ctx?.UI {
            verticalLayout {
                backgroundColor = Color.WHITE
                button {
                    text = "Hello"
                }
            }
        }?.view
    }
}
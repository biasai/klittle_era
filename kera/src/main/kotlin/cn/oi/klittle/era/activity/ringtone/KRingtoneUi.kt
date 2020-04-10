package cn.oi.klittle.era.activity.ringtone

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import cn.oi.klittle.era.R
import cn.oi.klittle.era.activity.ringtone.adapter.KRingtoneAdapter
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.toolbar.KToolbar
import cn.oi.klittle.era.widget.recycler.KRecyclerView
import org.jetbrains.anko.*

open class KRingtoneUi : KBaseUi() {

    var toolbar: KToolbar? = null
    var recyclerView: KRecyclerView? = null
    var ringtoneAdapter: KRingtoneAdapter? = null

    override fun destroy(activity: Activity?) {
        super.destroy(activity)
        toolbar = null
        recyclerView = null
        ringtoneAdapter = null
    }

    override fun createView(ctx: Context?): View? {

        return ctx?.UI {
            verticalLayout {
                backgroundColor = Color.WHITE
                toolbar = KToolbar(this, getActivity())?.apply {
                    //标题栏背景色
                    contentView?.apply {
                        backgroundColor = Color.parseColor("#0078D7")
                    }
                    //左边返回文本（默认样式自带一个白色的返回图标）
                    leftTextView?.apply {
                    }
                    titleTextView?.apply {
                        text = Companion.getString(R.string.kringtone)//铃声
                        textColor = Color.WHITE
                        textSize = kpx.textSizeX(32)
                        gravity = Gravity.CENTER
                    }
                    rightTextView?.apply {
                        setText(null)
                    }
                }
                recyclerView = krecyclerView {
                    try {
                        setLinearLayoutManager()
                        ringtoneAdapter = KRingtoneAdapter()
                        adapter = ringtoneAdapter
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.lparams {
                    width = matchParent
                    height = matchParent
                }
            }
        }?.view
    }
}
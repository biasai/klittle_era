package cn.oi.klittle.era.activity.photo.adapter

import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.oi.klittle.era.R
import cn.oi.klittle.era.activity.photo.entity.KLocalMedia
import cn.oi.klittle.era.activity.photo.manager.KPictureSelector
import cn.oi.klittle.era.activity.photo.utils.KDateUtils
import cn.oi.klittle.era.base.KBaseUi
import cn.oi.klittle.era.comm.KToast
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KPictureUtils
import cn.oi.klittle.era.widget.compat.KTextView
import cn.oi.klittle.era.widget.recycler.adapter.KAdapter
import org.jetbrains.anko.*
import java.util.concurrent.TimeUnit
//import kotlinx.coroutines.experimental.async
//import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.Deferred

open class KPhotoAdapter(var datas: MutableList<KLocalMedia>? = null) : KAdapter<KPhotoAdapter.Companion.MyViewHolder>() {

    companion object {
        open class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var item_camera: KTextView? = null
            var ui_camera: View? = null

            var job: Deferred<Any?>? = null
            var item_img: KTextView? = null//图片
            var item_right_top: KTextView? = null//右上角图标
            var item_right_bottom: KTextView? = null//右下角GIF

            init {
                //正常
                item_img = itemView?.findViewById(kpx.id("item_img"))
                item_right_top = itemView?.findViewById(kpx.id("item_right_top"))
                item_right_bottom = itemView?.findViewById(kpx.id("item_right_bottom"))
                //相机
                item_camera = itemView?.findViewById(kpx.id("item_camera"))
                ui_camera = itemView?.findViewById(kpx.id("ui_camera"))
            }
        }
    }

    var spanCount = KPictureSelector.imageSpanCount//列的个数
    var itemWidth = kpx.screenWidth() / spanCount//每列的宽度
    var lineWidth = kpx.x(12) / spanCount//每列之间的间隙。
    var spanWidth = itemWidth - lineWidth//图片的宽度
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var itemView = parent.context.UI {
            relativeLayout {
                //正常界面
                verticalLayout {
                    relativeLayout {
                        //图片
                        KTextView(this).apply {
                            id = kpx.id("item_img")
                            if (Build.VERSION.SDK_INT >= 21) {
                                transitionName = "share_kitem_img"
                            }
                            autoBg {
                                width = spanWidth
                                height = width
                                isAutoCenter = true
                                isRGB_565 = false
                                isCompress = false
                                isGlide = true
                                fg_color = Color.parseColor("#20000000")
                            }
                            autoBg_selected {
                                fg_color = Color.parseColor("#80000000")
                            }
                            autoBg_load {
                                width = spanWidth / 2
                                height = width
                                isAutoCenter = true
                                isRGB_565 = true
                                isCompress = false
                                bg_color = Color.parseColor("#999999")
                                isGlide = true
                                autoBg(R.mipmap.kera_placeholder)
                            }
                        }.lparams {
                            width = spanWidth
                            height = width
                            centerInParent()
                        }
                        //右上角选择框
                        KTextView(this).apply {
                            id = kpx.id("item_right_top")
                            radius {
                                x = kpx.x(12f)
                                y = x
                                width = kpx.x(40)
                                height = width
                                all_radius(kpx.x(200))
                                strokeColor = Color.WHITE
                                strokeWidth = kpx.x(2f)
                            }
                            radius_selected {
                                bg_color = Color.parseColor("#00CAFC")
                            }
                            txt {
                                textColor = Color.TRANSPARENT
                                text = ""
                            }
                            txt_selected {
                                textColor = Color.WHITE
                                textSize = kpx.textSizeX(26f, false)
                            }
                            gravity = Gravity.CENTER
                        }.lparams {
                            width = kpx.x(64)
                            height = width
                            alignParentTop()
                            alignParentRight()
                        }
                        //右下角GIF
                        KTextView(this).apply {
                            id = kpx.id("item_right_bottom")
                            text = "GIF"
                            textColor = Color.WHITE
                            textSize = kpx.textSizeX(32, false)
                            //backgroundColor = Color.parseColor("#60000000")//不要背景色，感觉添加了背景不好看。
                            gravity = Gravity.CENTER
                            leftPadding = kpx.x(8)
                            rightPadding = leftPadding
                        }.lparams {
                            width = wrapContent
                            height = wrapContent
                            alignParentBottom()
                            alignParentRight()
                            rightMargin = lineWidth
                            bottomMargin = rightMargin
                        }
                    }.lparams {
                        width = itemWidth
                        height = width
                    }
                }
                //相机界面
                verticalLayout {
                    id = kpx.id("ui_camera")
                    relativeLayout {
                        KTextView(this).apply {
                            id = kpx.id("item_camera")
                            autoBg {
                                width = (spanWidth * 0.4).toInt()
                                height = width
                                autoBg(R.mipmap.kera_camera)
                                isAutoCenterHorizontal = true
                                autoTopPadding = (spanWidth - width) / 3f
                                autoBg(R.mipmap.kera_camera)
                                autoBgColor = Color.parseColor("#B9B9B9")
                                bg_color = Color.parseColor("#999999")
                                bottomPadding = autoTopPadding.toInt()
                            }
                            textColor = Color.WHITE
                            textSize = kpx.textSizeX(28, false)
                            text = KBaseUi.getString(R.string.kcamera)//拍摄
                            gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                        }.lparams {
                            width = spanWidth
                            height = width
                            centerInParent()
                        }
                    }.lparams {
                        width = itemWidth
                        height = width
                    }
                }
            }
        }.view
        return MyViewHolder(itemView)
    }

    var isRecyclerBitmap = true//是否释放位图
    override fun onViewRecycled(holder: MyViewHolder) {
        super.onViewRecycled(holder)
        holder?.job?.let {
            it.cancel()//协程没有完成的就取消掉，fixme 很关键，能够减少占用资源。加快加载速度。
        }
        holder?.job = null
        holder?.item_img?.let {
            if (isRecyclerBitmap) {
                KBaseUi.recycleAutoBgBitmap(it)//fixme (仅仅只释放AutoBg位图)；防止内存泄漏，还是释放掉。
            }
        }
    }

    var checkNumCallback: ((checkNum: Int) -> Unit)? = null
    //选中变化回调，返回当前选中的个数
    fun checkNumCallback(checkNumCallback: ((checkNum: Int) -> Unit)? = null) {
        this.checkNumCallback = checkNumCallback
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (position == 0 && KPictureSelector.isCamera()) {
            //第一个(相机拍摄)
            holder?.item_camera?.onClick {
                KPictureUtils.camera() {
                    KPictureSelector.addNum(it)
                }
            }
            holder?.ui_camera?.visibility = View.VISIBLE
        } else {
            holder?.ui_camera?.visibility = View.GONE
            //正常
            var index = position
            if (KPictureSelector.isCamera()) {
                //有相机
                index = index - 1
            }
            var data = datas?.get(index)
            if (data == null) {
                return
            }
            data?.isGif()?.let {
                if (it) {
                    holder.item_right_bottom?.visibility = View.VISIBLE//gif
                    holder.item_right_bottom?.setText("GIF")
                } else {
                    data?.let {
                        if (it.duration > 0) {
                            holder.item_right_bottom?.visibility = View.VISIBLE
                            holder.item_right_bottom?.setText(KDateUtils.timeParse(it.duration))//视频时长
                        } else {
                            holder.item_right_bottom?.visibility = View.GONE
                        }
                    }
                }
            }
            //KLoggerUtils.e("路径：\t"+data.path)
            //图片
            holder.item_img?.apply {
                //setText(index.toString()+"")
                autoBg {
                    isCenterCrop = true
                    width = spanWidth
                    height = width
                    try {
                        data?.let {
                            if (it.isAudio()) {
                                width = spanWidth / 2
                                height = width
                                //音频是没有图片的。
                                holder.job = autoBg(R.mipmap.kera_icon_audio)
                                data?.key = key
                            } else {
                                var path = it.path
                                if (it.compressPath != null) {
                                    path = it.compressPath//优先显示压缩图片
                                }
                                //KLoggerUtils.e("path:\t"+it.path+"\tcompressPath:\t"+it.compressPath)
                                path?.let {
                                    holder.job = autoBgFromFile(it)
                                    data?.key = key
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                onClick {
                    isRecyclerBitmap = false//不释放位图
                    if (data.isVideo() || data.isAudio()) {
                        //视频预览；适配器里视频不使用共享元素动画，原因是因为效果非常不好。
                        KPictureSelector.openExternalPreview(index = index, meidas = datas, isCheckable = true)
                    } else {
                        //图片预览
                        KPictureSelector.openExternalPreview(sharedElement = holder.item_img, index = index, meidas = datas, isCheckable = true)
                    }
                    GlobalScope.async {
                        delay(500)
                        isRecyclerBitmap = true//恢复释放位图
                    }
                }
            }
            //右上角选择框
            holder.item_right_top?.apply {
                txt_selected {
                    text = data?.checkedNum.toString()
                }
                isSelected = data.isChecked!!
                onClick {
                    data?.isChecked = !data.isChecked!!
                    isSelected = data.isChecked!!
                    if (isSelected) {
                        //选中
                        if (KPictureSelector.addNum(data)) {
                            txt_selected {
                                text = data?.checkedNum.toString()//显示当前选中数量
                            }
                        } else {
                            isSelected = false
                            //你最多可以选择%s张图片
                            KToast.showInfo(KBaseUi.getString(R.string.kmaxSelectNum, KPictureSelector.maxSelectNum.toString()))
                        }
                    } else {
                        //取消选中
                        KPictureSelector.reduceNum(data)
                        isRecyclerBitmap = false//刷新时不释放位图
                        notifyDataSetChanged()
                        GlobalScope.async {
                            delay(500)
                            isRecyclerBitmap = true//恢复释放位图
                        }
                    }
                    //回调当前选中的个数
                    checkNumCallback?.let {
                        it(KPictureSelector.currentSelectNum)
                    }
                }
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        //return super.getItemViewType(position)
        return position
    }

    override fun getItemCount(): Int {
        datas?.size?.let {
            if (KPictureSelector.isCamera()) {
                return it + 1
            } else {
                return it
            }
        }
        if (KPictureSelector.isCamera()) {
            return 1
        } else {
            return 0
        }
    }
}
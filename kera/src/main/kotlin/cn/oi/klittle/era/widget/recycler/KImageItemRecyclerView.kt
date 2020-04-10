package cn.oi.klittle.era.widget.recycler

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.oi.klittle.era.R
import cn.oi.klittle.era.activity.photo.entity.KLocalMedia
import cn.oi.klittle.era.activity.photo.manager.KPictureSelector
import cn.oi.klittle.era.base.KBaseUi.Companion.ktextView
import cn.oi.klittle.era.comm.kpx
import cn.oi.klittle.era.utils.KFileUtils
import cn.oi.klittle.era.widget.compat.KTextView
import org.jetbrains.anko.*
import java.lang.Exception

//                fixme 使用案例，直接放到布局里，调用update()方法即可。
//                ui {
//                    kImageItemRecyclerView {
//                        update(null)//初始化或刷新
//                    }.lparams {
//                        width = matchParent
//                        height = wrapContent
//                    }
//                }
//                fixme 重要方法：getPathes()获取图片路径集合。recyclerBitmap()释放所有位图

/**
 * 图片选择器，图片选中之后的列表显示
 */
open class KImageItemRecyclerView : KRecyclerView {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {}

    constructor(viewGroup: ViewGroup) : super(viewGroup.context) {
        viewGroup.addView(this)//直接添加进去,省去addView(view)
    }

    private var spanCount: Int = 4
    private var parentWidth: Int = kpx.screenWidth()
    private var datasSize: Int = 0//记录上一次数据的个数
    private var data: String? = null
    /**
     * 刷新或初始化
     * @param datas 图片数据
     * @param spanCount 网格列数
     * @param itemViewWidth KRecyclerView的宽度（最好指明一下）
     * @param isRecyclerBitmap 是否自动释放释放位图
     */
    open fun update(datas: MutableList<KLocalMedia>?, spanCount: Int = 4, parentWidth: Int = kpx.screenWidth()) {
        try {
            //(!datas?.toString()?.trim().equals(data)) 判断两个对象是否相等，不相等就重新初始化
            if (layoutManager == null || adapter == null || this.spanCount != spanCount || this.parentWidth != parentWidth || datas == null || (!datas?.toString()?.trim().equals(data)) || datas!!.size <= 0 || datasSize <= 0) {
                hiddenScroll()
                setGridLayoutManager(spanCount)
                adapter = KImageItemAdapter(datas, spanCount, parentWidth)
            }
            this.spanCount = spanCount
            this.parentWidth = parentWidth
            data = datas?.toString()?.trim()
            datas?.let {
                datasSize = it.size//记录上一次数据个数
            }
            context?.runOnUiThread {
                try {
                    adapter?.notifyDataSetChanged()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            this.datas = datas//图片
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    public var datas: MutableList<KLocalMedia>? = null//fixme 图片数据
    /**
     * fixme 获取图片路径集合
     */
    public fun getPathes(): MutableList<String>? {
        try {
            var pathes = arrayListOf<String>()
            datas?.forEach {
                var path = it.path//原始路径
                if (it.isCompressed && it.compressPath != null) {
                    path = it.compressPath//压缩路径
                }
                if (path != null && KFileUtils.getInstance().getFileSize(path) > 0) {
                    pathes.add(path)
                }
            }
            if (pathes.size > 0) {
                return pathes
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * fixme 释放所有位图
     */
    public fun recyclerBitmap() {
        datas?.forEach {
            it?.recyclerBitmap()
        }
    }

    companion object {

        var viewType_footView = -200//末尾
        var viewType_empty = -1//空布局

        open class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var img: KTextView? = null

            init {
                img = itemView?.findViewById(kpx.id("img"))
            }
        }

        open class EmpatyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var img: KTextView? = null

            init {
                img = itemView?.findViewById(kpx.id("img"))
            }
        }

        open class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var img: KTextView? = null

            init {
                img = itemView?.findViewById(kpx.id("img"))
            }
        }

        open class KImageItemAdapter(var datas: MutableList<KLocalMedia>?, spanCount: Int = 4, parentWidth: Int = kpx.screenWidth()) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            var itemWidth = parentWidth / spanCount//单个item的宽度
            var imgWidth = itemWidth / 10 * 8//图片的宽度

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                if (viewType == viewType_empty || viewType == viewType_footView) {
                    //空布局,末尾布局
                    var itemView = parent.context.UI {
                        verticalLayout {
                            gravity = Gravity.CENTER
                            ktextView {
                                id = kpx.id("img")
                                autoBg {
                                    isAutoCenter = true
                                    isGlide = true
                                    width = itemWidth / 2
                                    height = width
                                    autoBgColor = Color.WHITE
                                    autoBg(R.mipmap.kera_add)
                                    bg_color = Color.LTGRAY
                                }
                                onClick {
                                    KPictureSelector.selectionMedia().isCompress(true).forResult {
                                        if (parent is KImageItemRecyclerView) {
                                            parent?.update(it)//fixme 图片选择器
                                        }
                                    }
                                }
                            }.lparams {
                                width = imgWidth
                                height = width
                            }
                        }
                    }.view
                    if (viewType == viewType_empty) {
                        return EmpatyViewHolder(itemView)//空布局
                    } else {
                        return FooterViewHolder(itemView)//末尾布局
                    }
                } else {
                    //正常布局
                    var itemView = parent.context.UI {
                        verticalLayout {
                            gravity = Gravity.CENTER
                            ktextView {
                                id = kpx.id("img")
                            }.lparams {
                                width = itemWidth
                                height = width
                            }
                        }
                    }.view
                    return MyViewHolder(itemView)
                }
            }

            override fun getItemCount(): Int {
                datas?.let {
                    if (it.size > 0) {
                        return it.size + 1
                    }
                }
                return 1
            }

            override fun getItemViewType(position: Int): Int {
                datas?.let {
                    if (it.size > 0) {
                        if (it.size == position) {
                            return viewType_footView
                        } else {
                            return position
                        }
                    }
                }
                return viewType_empty
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                if (holder is EmpatyViewHolder) {
                    //空布局
                } else if (holder is FooterViewHolder) {
                    //末尾布局
                } else if (holder is MyViewHolder) {
                    //正常
                    try {
                        if (datas == null) {
                            return
                        } else {
                            datas?.let {
                                if (it.size <= position) {
                                    return
                                }
                            }
                        }
                        var data = datas!![position]
                        holder?.img?.apply {
                            var path = data.path
                            if (data.isCompressed && data.compressPath != null) {
                                path = data.compressPath
                            }
                            autoBg {
                                isAutoCenter = true
                                isGlide = true
                                width = imgWidth
                                height = width
                                autoBgFromFile(path, true)
                                data.key = key//fixme 图片缓存键值
                            }
                            autoBg2 {
                                //width = imgWidth/3
                                width = kpx.x(50)
                                height = width
                                autoBg(R.mipmap.kera_error)
                                isAutoCenter = false
                                isAutoRight = true
                                autoTopPadding = (itemWidth - imgWidth).toFloat() / 5
                                autoRightPadding = autoTopPadding
                                onClickCallback {
                                    try {
                                        datas?.removeAt(position)//fixme 移除当前图片
                                        notifyDataSetChanged()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                            onClick {
                                //fixme 图片预览
                                holder?.itemView?.context?.let {
                                    if (it is Activity) {
                                        KPictureSelector.openExternalPreview(it, position, datas, false)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

}
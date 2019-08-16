package cn.oi.klittle.era.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.oi.klittle.era.comm.kpx;
import cn.oi.klittle.era.view.datepicker.WheelPicker;
//import cn.android.support.v7.lib.sin.crown.view.picker.WheelPicker;
//        使用说明
//        RollerView view = findViewById(R.id.myFramLayout);
//        final List<String> items = new ArrayList<>();
//        for (int i = 1; i <= 12; i++) {
//            items.add(i + "月");
//        }
//        view.setItems(items);//添加数据
//        view.setItemSelectListener(new RollerView.ItemSelectListener() {//添加回调
//            @Override
//            public void onItemSelect(String item, int postion) {
//                //Log.e("test", "数据:\t" + item + "\t下标:\t" + postion);
//            }
//        });
//        view.setCurrentPostion(6,true);//选中指定下标

//注意：android:layout_width="@dimen/x100_100" 設置寬度和高度時候，一定要設置明確的值。不能設置為0dp ,不然子View無法顯示。

/**
 * 滚轮选择器【新版】,解决华为mate9+荣耀8不显示问题
 * Created by 彭治铭 on 2018/3/29.
 */

public class KRollerView extends WheelPicker {

    /**
     * 设置中间两条线条的宽度
     *
     * @param lineWidth
     */
    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * 设置中间两条线条的颜色
     *
     * @param lineColor
     */
    public KRollerView setLineColor(int lineColor) {
        this.lineColor = lineColor;
        invalidate();
        return this;
    }

    /**
     * 设置线条的高度【边框的宽度】
     *
     * @param strokeWidth
     */
    public KRollerView setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
        invalidate();
        return this;
    }

    /**
     * 设置选中字体的颜色
     *
     * @param selectTextColor
     */
    public KRollerView setSelectTextColor(int selectTextColor) {
        setSelectedItemTextColor(selectTextColor);
        return this;
    }

    /**
     * 设置默认字体的颜色
     *
     * @param defaultTextColor
     */
    public KRollerView setDefaultTextColor(int defaultTextColor) {
        setItemTextColor(defaultTextColor);
        return this;
    }

    int textSize = 0;

    //设置字体大小
    public KRollerView setTextSize(float textSize) {
        setItemTextSize((int) textSize);
        this.textSize = (int) textSize;
        //线条的位置
        startLine = mDrawnCenterY - getItemTextSize() - getItemTextSize() / 4;
        endLine = mDrawnCenterY + getItemTextSize() / 2;
        postInvalidate();
        return this;
    }

    /**
     * 设置回调接口，返回选中的数据和下标
     *
     * @param itemSelectListener
     */
    public KRollerView setItemSelectListener(final ItemSelectListener itemSelectListener) {
        setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelPicker picker, Object data, int position) {
                itemSelectListener.onItemSelect(data.toString(), position);
            }
        });
        return this;
    }

    /**
     * 設置當前顯示item的個數【一定要在設置數據之前，設置。必須】
     *
     * @param count
     */
    public KRollerView setCount(int count) {
        setVisibleItemCount(count);
        return this;
    }

    //保存数据的下标
    public Map<String, Integer> map = new HashMap();

    public List<String> items;//数据集合

    /**
     * fixme ======================================================================================= 设置数据集合
     *
     * @param items
     */
    public KRollerView setItems(List<String> items) {
        if (this.items == null) {
            this.items = new ArrayList<>();//与参数传入的数据不会发生关联。
        }
        this.items.clear();
        this.items.addAll(items);
        setData(this.items);
        for (int i = 0; i < items.size(); i++) {
            map.put(items.get(i), i);//保存数据的下标
        }
        return this;
    }

    //getCurrentItemValue() 获取当前选中的值
    //getCurrentPostion() 获取当前选中的下标

    /**
     * 根据数据，获取对应下标
     *
     * @param data
     * @return
     */
    public int getItemPostion(String data) {
        if (map.containsKey(data)) {
            return map.get(data);
        }
        return 0;
    }

    /**
     * 选中指定下标【数据集合的实际下标】。
     *
     * @param position
     * @return
     */
    public KRollerView setCurrentPostion(int position) {
        setSelectedItemPosition(position);
        return this;
    }

    /**
     * 选中指定的数据。
     *
     * @param value
     * @return
     */
    public KRollerView setCurrentValue(String value) {
        if (value != null && items != null&&value.trim().length()>0) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).trim().equals(value.trim())) {//判断数据是否相等
                    if (getCurrentPostion() != i) {
                        setCurrentPostion(i);
                    }
                    break;
                }
            }
        }
        return this;
    }

    //获取当前选中下标【不算空格。是原数据的下标，即实际下标。】
    public int getCurrentPostion() {
        return getCurrentItemPosition();
    }

    //获取当前选中的值
    public String getCurrentItemValue() {
        return items.get(getCurrentItemPosition());
    }

    public KRollerView(Context context) {
        super(context);
        setCurved(true);//设置卷尺效果
        setCyclic(true);//fixme 默认设置数据滚轮循环显示。
    }

    public KRollerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setCurved(true);//fixme 设置卷尺效果
        setCyclic(true);//fixme 默认设置数据滚轮循环显示。
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (paint == null) {
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
        }
//        setLayerType(View.LAYER_TYPE_SOFTWARE, paint);
//        setOverScrollMode(OVER_SCROLL_NEVER);//设置滑动到边缘时无效果模式
//        setVerticalScrollBarEnabled(false);//滚动条隐藏
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mVisibleItemCount <= 0) {
            mVisibleItemCount = 5;//item显示个数
        }
        int chidHeith = h / mVisibleItemCount;

        //设置默认字体大小
        if (textSize <= 0) {
            int textSize = (int) (chidHeith * 0.72);//字体大小。默认就是这个了。
            setItemTextSize(textSize);
        }
        startLine = mDrawnCenterY - getItemTextSize() - getItemTextSize() / 4;
        endLine = mDrawnCenterY + getItemTextSize() / 2;
    }


    Paint paint;
    int startLine;
    int endLine;
    int lineColor = Color.parseColor("#000000");//线条颜色
    int strokeWidth = (int) kpx.INSTANCE.x(2);//线条的宽度
    private int lineWidth = 0;//线条的宽度
    public int lineOffset = 0;//fixme 线条的间距（可以为正数，也可以为负数），可以控制中间两条线条的距离。

    @Override
    public void draw(Canvas canvas) {
        if (mData != null && mData.size() > 0) {
            super.draw(canvas);
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(lineColor);//线条颜色
        paint.setStrokeWidth(strokeWidth);//线条宽度
        //中间两条线
        if (lineWidth <= 0) {
            canvas.drawLine(0, startLine - lineOffset, getWidth(), startLine - lineOffset, paint);
            canvas.drawLine(0, endLine + lineOffset, getWidth(), endLine + lineOffset, paint);
        } else {
            int starx = (int) ((getWidth() - lineWidth) / 2);
            canvas.drawLine(starx, startLine - lineOffset, starx + lineWidth, startLine - lineOffset, paint);
            canvas.drawLine(starx, endLine + lineOffset, starx + lineWidth, endLine + lineOffset, paint);
        }
        paint.setColor(Color.BLACK);
    }


    //回调
    public interface ItemSelectListener {
        //原始数据，和下标
        void onItemSelect(String item, int position);
    }

}

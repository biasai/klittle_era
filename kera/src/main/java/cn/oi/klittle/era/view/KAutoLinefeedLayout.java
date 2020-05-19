package cn.oi.klittle.era.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import cn.oi.klittle.era.utils.KLoggerUtils;

//                        fixme 调用案例；向左靠齐。没有居中。
//                        KAutoLinefeedLayout {
//                            removeAllViews()//清除所有View
//                            for (i in 0..50) {
//                                verticalLayout {
//                                    ktextView {
//                                        text = "Hello 世界"+i
//                                        radius {
//                                            all_radius(kpx.x(30))
//                                            strokeWidth = kpx.x(3f)
//                                            strokeHorizontalColors(Color.LTGRAY, Color.GRAY)
//                                            bg_color=Color.parseColor("#FF8080")
//                                        }
//                                        padding = kpx.x(12)
//                                        onClick {
//                                            KToast.showInfo(text.toString())
//                                        }
//                                    }.lparams {
//                                        leftMargin = kpx.x(18)
//                                        topMargin = leftMargin
//                                    }
//                                }
//                            }
//                        }.lparams {
//                            width = matchParent
//                            height = wrapContent
//                        }

/**
 * 自动水平换行布局组件（向左靠齐。没有居中。）
 * 该组件无需特别设置，只要将子View塞给它，就会自动换行显示，无任何限制。
 * fixme 注意：该组件对放入的View（最外层的View）进行宽高测量时候，是不计算外补丁margin的。但会计算内补丁padding
 * fixme 但是只对最外层的view不计算外补丁。内部的子View还是计算的。
 * fixme 总之：最外层的View不计算外补丁
 */
public class KAutoLinefeedLayout extends ViewGroup {

    public KAutoLinefeedLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public KAutoLinefeedLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KAutoLinefeedLayout(Context context) {
        this(context, null);
    }

    @Override
    public void draw(Canvas canvas) {
        try {
            super.draw(canvas);
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("KAutoLinefeedLayout->draw()异常：\t" + e.getMessage(),true);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutHorizontal();
    }

    private void layoutHorizontal() {
        try {
            final int count = getChildCount();
            final int lineWidth = getMeasuredWidth() - getPaddingLeft()
                    - getPaddingRight();
            int paddingTop = getPaddingTop();
            int childTop = 0;
            int childLeft = getPaddingLeft();

            int availableLineWidth = lineWidth;
            int maxLineHight = 0;

            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                if (child == null) {
                    continue;
                } else if (child.getVisibility() != GONE) {
                    final int childWidth = child.getMeasuredWidth();
                    final int childHeight = child.getMeasuredHeight();

                    if (availableLineWidth < childWidth) {
                        availableLineWidth = lineWidth;
                        paddingTop = paddingTop + maxLineHight;
                        childLeft = getPaddingLeft();
                        maxLineHight = 0;
                    }
                    childTop = paddingTop;
                    setChildFrame(child, childLeft, childTop, childWidth,
                            childHeight);
                    childLeft += childWidth;
                    availableLineWidth = availableLineWidth - childWidth;
                    maxLineHight = Math.max(maxLineHight, childHeight);
                }
            }
        } catch (Exception e) {
            KLoggerUtils.INSTANCE.e("KAutoLinefeedLayout->layoutHorizontal()异常：\t" + e.getMessage(),true);
        }
    }

    private void setChildFrame(View child, int left, int top, int width,
                               int height) {
        child.layout(left, top, left + width, top + height);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
        }
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            final int width = MeasureSpec.getSize(widthMeasureSpec);
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(
                    getDesiredHeight(width), MeasureSpec.EXACTLY));
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private int getDesiredHeight(int width) {
        final int lineWidth = width - getPaddingLeft() - getPaddingRight();
        int availableLineWidth = lineWidth;
        int totalHeight = getPaddingTop() + getPaddingBottom();
        int lineHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            final int childWidth = child.getMeasuredWidth();
            final int childHeight = child.getMeasuredHeight();
            if (availableLineWidth < childWidth) {
                availableLineWidth = lineWidth;
                totalHeight = totalHeight + lineHeight;
                lineHeight = 0;
            }
            availableLineWidth = availableLineWidth - childWidth;
            lineHeight = Math.max(childHeight, lineHeight);
        }
        totalHeight = totalHeight + lineHeight;
        return totalHeight;
    }

}


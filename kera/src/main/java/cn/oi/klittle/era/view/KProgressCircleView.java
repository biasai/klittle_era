package cn.oi.klittle.era.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import cn.oi.klittle.era.utils.KAssetsUtils;
import cn.oi.klittle.era.utils.KProportionUtils;

/**
 * 圆形进度条控件
 * Created by 彭治铭 on 2017/9/24.
 */

public class KProgressCircleView extends View {

    public KProgressCircleView(ViewGroup viewGroup) {
        super(viewGroup.getContext());
        init();
        viewGroup.addView(this);
    }

    public KProgressCircleView(Context context) {
        super(context);
        init();
    }

    public KProgressCircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    public Bitmap dst, load;
    public int width, height;

    private void init() {
        dst = KAssetsUtils.getInstance().getBitmapFromAssets("kera/progress/circleprgoress.png", 0, true);
        int dstWidth = KProportionUtils.getInstance().adapterInt(150);
        float bias = (float) dstWidth / dst.getWidth();
        dst = Bitmap.createScaledBitmap(dst, dstWidth, dstWidth, true);
        load = KAssetsUtils.getInstance().getBitmapFromAssets("kera/progress/loading.png", 0, true);
        load = Bitmap.createScaledBitmap(load, (int) (load.getWidth() * bias), (int) (load.getHeight() * bias), true);
        width = dstWidth * 2;
        height = width;
        centerX = width / 2;
        centerY = height / 2;
        dstX = (width - dst.getWidth()) / 2;
        dstY = (height - dst.getHeight()) / 2;
        loadX = (width - load.getWidth()) / 2;
        loadY = (width - load.getHeight()) / 2;
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColorFilter(new LightingColorFilter(Color.TRANSPARENT, 0xFFFFFFFF));//原有图片总感觉会失真，效果不好，所以直接变成白色。效果会好点。
        setLayerType(View.LAYER_TYPE_HARDWARE, paint);
    }

    Paint paint;
    public int degress = 0;
    int centerX;
    int centerY;
    int dstX, dstY;
    int loadX, loadY;

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            if (dst != null && !dst.isRecycled()) {
                canvas.save();
                canvas.rotate(degress += 3, centerX, centerY);
                canvas.drawBitmap(dst, dstX, dstY, paint);
                canvas.restore();
            }
            if (load != null && !load.isRecycled()) {
                canvas.drawBitmap(load, loadX, loadY, paint);
            }
            invalidate();
        }
    }

}

package cn.oi.klittle.era.widget.viewpager.animationlibrary;

import android.view.View;

/**
 * 一大一小缩放切换；setPageTransformer(true, KScaleInOutTransformer())
 */
public class KScaleInOutTransformer extends KABaseTransformer {

	@Override
	protected void onTransform(View view, float position) {
		view.setPivotX(position < 0 ? 0 : view.getWidth());
		view.setPivotY(view.getHeight() / 2f);
		float scale = position < 0 ? 1f + position : 1f - position;
		view.setScaleX(scale);
		view.setScaleY(scale);
	}

}

package cn.oi.klittle.era.widget.compat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * fixme 禁止文本框的复制黏贴，亲测有效。参考地址：https://blog.csdn.net/lihenair/article/details/78901648
 * <p>
 * fixme 正常的一般文本输入框，一点击就出现复制粘性选项的条件是：(KMenuHidingEditText已经禁止了复制黏贴的功能)
 * fixme 1:字体大小 textSize = kpx.textSizeX(30);和控件高度，height = kpx.x(40)（大于或等于41，且输入框里已经有文本了，那么一点击输入框就会出现复制黏贴的选项）
 * fixme 2:输入文本框里已经有文本了。如果没文本，则不会一点击有出现复制黏贴选项。
 */
public class KMenuHidingEditText extends KEditText {
    private final Context mContext;

    public KMenuHidingEditText(ViewGroup viewGroup) {
        super(viewGroup.getContext());
        this.mContext = viewGroup.getContext();
        blockContextMenu();
        viewGroup.addView(this);
    }

    public KMenuHidingEditText(Context context) {
        super(context);
        this.mContext = context;
        blockContextMenu();
    }

    public KMenuHidingEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        blockContextMenu();
    }

//    public KMenuHidingEditText(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//        this.mContext = context;
//
//        blockContextMenu();
//    }

    private void blockContextMenu() {
        try {
            this.setCustomSelectionActionModeCallback(new BlockedActionModeCallback());
            this.setLongClickable(false);
            this.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    KMenuHidingEditText.this.clearFocus();
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // setInsertionDisabled when user touches the view
            this.setInsertionDisabled();
        }
        return super.onTouchEvent(event);
    }

    private void setInsertionDisabled() {
        try {
            Field editorField = TextView.class.getDeclaredField("mEditor");
            editorField.setAccessible(true);
            Object editorObject = editorField.get(this);

            Class editorClass = Class.forName("android.widget.Editor");
            Field mInsertionControllerEnabledField = editorClass.getDeclaredField("mInsertionControllerEnabled");
            mInsertionControllerEnabledField.setAccessible(true);
            mInsertionControllerEnabledField.set(editorObject, false);
        } catch (Exception ignored) {
            // ignore exception here
        }
    }

    @Override
    public boolean isSuggestionsEnabled() {
        return false;
    }

    private class BlockedActionModeCallback implements ActionMode.Callback {

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        public void onDestroyActionMode(ActionMode mode) {
        }
    }
}
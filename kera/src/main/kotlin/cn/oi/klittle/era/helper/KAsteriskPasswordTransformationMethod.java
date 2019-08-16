package cn.oi.klittle.era.helper;

import android.text.method.PasswordTransformationMethod;
import android.view.View;

/**
 * 自定义密码
 * transformationMethod = AsteriskPasswordTransformationMethod(c)//必须每次都重新实例化，不然可能会奔溃。
 * Created by 彭治铭 on 2018/5/17.
 */
public class KAsteriskPasswordTransformationMethod extends PasswordTransformationMethod {

    public char c = '*';//自定义密码字符

    public KAsteriskPasswordTransformationMethod(char c) {
        this.c = c;
    }


    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        return new PasswordCharSequence(source);
    }

    private class PasswordCharSequence implements CharSequence {
        private CharSequence mSource;

        public PasswordCharSequence(CharSequence source) {
            mSource = source; // Store char sequence
        }

        public char charAt(int index) {
            return c; //  密码字符
        }

        public int length() {
            return mSource.length(); // Return default
        }

        public CharSequence subSequence(int start, int end) {
            return mSource.subSequence(start, end); // Return default
        }

    }
}

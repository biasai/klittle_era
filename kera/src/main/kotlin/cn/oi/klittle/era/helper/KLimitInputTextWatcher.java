package cn.oi.klittle.era.helper;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class KLimitInputTextWatcher implements TextWatcher {
    /**
     * et
     */
    private EditText et = null;
    /**
     * 筛选条件
     */
    public String regex;
    /**
     * 默认的筛选条件(正则:不能输入中文和空格)
     * \u4E00-\u9FA5匹配中文\u0020匹配空格
     */
    public String DEFAULT_REGEX = "[\u4E00-\u9FA5\u0020]";

    /**
     * 只允许输入中文
     */
    public String CHINA_REGEX = "[^\\u4E00-\\u9FA5]";

    /**
     * fixme 只允许输入大小写字母和数字
     * fixme "[^A-Za-z0-9*.,，。+-]" 要什么字符就往后面加就行了
     */
    public String ENGLISH_REGEX = "[^A-Za-z0-9]";

    /**
     * 构造方法
     *
     * @param et
     */
    public KLimitInputTextWatcher(EditText et) {
        this.et = et;
        this.regex = DEFAULT_REGEX;
    }

    /**
     * 构造方法
     *
     * @param et    et
     * @param regex 筛选条件
     */
    public KLimitInputTextWatcher(EditText et, String regex) {
        this.et = et;
        this.regex = regex;
    }

    /**
     * @param et
     * @param type 0不能输入中文和空格,1 只允许输入中文，2 只允许输入大小写字母和数字
     */
    public KLimitInputTextWatcher(EditText et, int type) {
        this.et = et;
        this.regex = DEFAULT_REGEX;
        if (type == 1) {
            this.regex = CHINA_REGEX;
        } else if (type == 2) {
            this.regex = ENGLISH_REGEX;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        try {
            String str = editable.toString();
            String inputStr = clearLimitStr(regex, str);
            et.removeTextChangedListener(this);
            // et.setText方法可能会引起键盘变化,所以用editable.replace来显示内容
            editable.replace(0, editable.length(), inputStr.trim());
            et.addTextChangedListener(this);
        } catch (Exception e) { }
    }

    /**
     * 清除不符合条件的内容
     *
     * @param regex
     * @return
     */
    private String clearLimitStr(String regex, String str) {
        return str.replaceAll(regex, "");
    }
}

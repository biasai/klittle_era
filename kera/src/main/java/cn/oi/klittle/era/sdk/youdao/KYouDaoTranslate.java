package cn.oi.klittle.era.sdk.youdao;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

//fixme 注意；调用自己的http请求时，和.net一样。contentType=null设置为空才行。
//fixme 以及参数一定要进行 URLEncoder.encode(e.value.toString(), "utf-8") 转码。不然签名也没错误。
//fixme 还有就是不能同时请求，同一时间只能请求一次。不然报207 重复请求错误。
//fixme 在平台应用管理里面添加应用，选择接入方式为 api
//fixme 在平台自然语言翻译添加服务。然后再绑定应用即可。
//fixme 操作地址：http://ai.youdao.com/index.s

/**
 * 有道翻译帮助类
 * https://ai.youdao.com/docs/doc-trans-api.s#p10
 */
public class KYouDaoTranslate {
    static String YOUDAO_URL = "http://openapi.youdao.com/api";

    //static String YOUDAO_URL = "https://openapi.youdao.com/api";

    //fixme 在有道平台上创建应用；选择API;就会生成APP_KEY（应用id）和APP_SECRET（密钥）；好像是计费的。48元/百万字符；默认送了100元免费的。
    //地址：https://ai.youdao.com/app_detail.s?id=00429f552660242b
    static String APP_KEY = "00429f552660242b";//应用id

    static String APP_SECRET = "tJkOGBdWaqclyzREmvvk4TJ37lpaq4Ii";

    /**
     * 获取参数
     * @param src  要翻译的字符串
     * @param from 从什么语言
     * @param to   翻译到什么语言
     * @return
     */
    public static  Map<String,String> getParams(String src, String from, String to){
        Map<String,String> params = new HashMap();
        String q = src;
        String salt = String.valueOf(System.currentTimeMillis());
        params.put("from", from);
        params.put("to", to);
        params.put("signType", "v3");
        String curtime = String.valueOf(System.currentTimeMillis() / 1000);
        params.put("curtime", curtime);
        String signStr = APP_KEY + truncate(q) + salt + curtime + APP_SECRET;
        String sign = getDigest(signStr);
        params.put("appKey", APP_KEY);
        params.put("q", q);
        params.put("salt", salt);
        params.put("sign", sign);
        return params;
    }

    /**
     * 生成加密字段
     */
    public static String getDigest(String string) {
        if (string == null) {
            return null;
        }
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        byte[] btInput = string.getBytes();
        try {
            MessageDigest mdInst = MessageDigest.getInstance("SHA-256");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String truncate(String q) {
        if (q == null) {
            return null;
        }
        int len = q.length();
        String result;
        return len <= 20 ? q : (q.substring(0, 10) + len + q.substring(len - 10, len));
    }

}
			
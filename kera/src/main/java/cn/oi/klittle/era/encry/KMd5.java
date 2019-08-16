package cn.oi.klittle.era.encry;

import java.security.MessageDigest;

/**
 * MD5转码是不可逆的；即转成MD5之后；数据就不能再恢复。
 * 每家每户的MD5格式可能都不一样。不统一；（我这里用的是映美云打印机的。）
 */
public class KMd5 {
    /**
     * 字符串转MD5
     *
     * @param dataStr
     * @return fixme 返回始终是大写的32位字符；如：F8B9B53F6BC292CBEEDA84C794C41596
     */
    public static String encrypt(String dataStr) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(dataStr.getBytes("UTF8"));
            byte s[] = m.digest();//返回的 s.length长度始终是16
            String result = "";
            for (int i = 0; i < s.length; i++) {
                //(Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6))返回是两位数；如：6e或53等等
                result += Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6);
            }
            return result.toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 字节数组转MD5;
     *
     * @param bytes
     * @return 返回的String个数不一定；由bytes字节数组的长度决定。
     */
    public static String encrypt(byte[] bytes) {
        try {
            String result = "";
            for (int i = 0; i < bytes.length; i++) {
                result += Integer.toHexString((0x000000FF & bytes[i]) | 0xFFFFFF00).substring(6);
            }
            return result.toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
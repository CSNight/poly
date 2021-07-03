package csnight.spider.poly.utils;


import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class IdentifyUtils {
    /**
     * 生成UUID
     *
     * @return string
     */
    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成UUID，但会过滤-
     *
     * @return string
     */
    public static String getUUID2() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "").toUpperCase();
    }

    /***
     * MD5加码 生成32位md5码
     */
    public static String string2MD5(String inStr, String prefix) {
        MessageDigest md = null;// 生成一个MD5加密计算摘要
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert md != null;
        md.update(inStr.getBytes(StandardCharsets.UTF_8));// 计算md5函数
        String hashedPwd = new BigInteger(1, md.digest()).toString(16);// 16是表示转换为16进制数
        return prefix + hashedPwd;
    }
}

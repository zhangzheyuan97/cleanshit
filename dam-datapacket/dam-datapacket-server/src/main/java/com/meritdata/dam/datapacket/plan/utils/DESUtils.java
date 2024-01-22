package com.meritdata.dam.datapacket.plan.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.*;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @Author fanpeng
 * @Date 2023/5/6
 * @Describe DES加密解密工具类
 */
public class DESUtils {

    private static Key key;
    private static String KEY_STR = "packet";
    private static String CHARSET_NAME = "UTF-8";
    private static String ALGORITHM = "DES";
    private static String SAFE_METHOD = "SHA1PRNG";

    static {
        try {
            //生成DES算法对象
            KeyGenerator generator = KeyGenerator.getInstance(ALGORITHM);
            //运用SHA1安全策略
            SecureRandom secureRandom = SecureRandom.getInstance(SAFE_METHOD);
            //设置上密钥种子
            secureRandom.setSeed(KEY_STR.getBytes());
            //初始化基于SHA1的算法对象
            generator.init(secureRandom);
            //生成密钥对象
            key = generator.generateKey();
            generator = null;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 加密
     *
     * @param str
     * @return
     */
    public static String getEncryptString(String str) {
        //基于base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        try {
            byte[] bytes = str.getBytes(CHARSET_NAME);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //加密
            byte[] doFinal = cipher.doFinal(bytes);
            return encoder.encode(doFinal);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解密
     *
     * @param str
     * @return
     */
    public static String getDecryptString(String str) {
        //基于base64编码
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] bytes = decoder.decodeBuffer(str);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            //解密
            byte[] doFinal = cipher.doFinal(bytes);
            return new String(doFinal, CHARSET_NAME);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

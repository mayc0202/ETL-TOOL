package com.ds.etl.common.utils;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Arrays;


/**
 * AES
 *
 * @author mayc
 */
@Slf4j
public final class AESUtil {
    private static final String ALGORITHMSTR = "AES/ECB/PKCS5Padding";
    private static final String AES = "AES";
    private static final String AES_KEY = "1q2w3e4r5t6ymayc"; // 密钥
    private static final String UTF8 = "UTF-8";

    public static String encrypt(String content, String key) {
        try {
            byte[] keyBytes = processKey(key);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES);

            Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encrypted = cipher.doFinal(content.getBytes(UTF8));
            return Base64.encodeBase64String(encrypted);
        } catch (Exception e) {
            log.error("AES加密失败", e);
            throw new RuntimeException("AES加密失败", e);
        }
    }

    public static String decrypt(String content, String key) {
        try {
            byte[] keyBytes = processKey(key);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES);

            Cipher cipher = Cipher.getInstance(ALGORITHMSTR);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] encryptedBytes = Base64.decodeBase64(content.trim());
            byte[] original = cipher.doFinal(encryptedBytes);
            return new String(original, UTF8);
        } catch (Exception e) {
            log.error("AES解密失败", e);
            throw new RuntimeException("AES解密失败", e);
        }
    }

    private static byte[] processKey(String key) throws UnsupportedEncodingException {
        byte[] keyBytes = key.getBytes(UTF8);
        return Arrays.copyOf(keyBytes, 16); // 强制16字节长度
    }

    public static String getAesKey() {
        return AES_KEY;
    }
}

package com.ds.etl.common.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * RSA 非对称性加密工具
 * @author mayc
 */
public final class RSAConfig {
    // 算法常量 -- 算法
    private static final String KEY_ALGORITHM = "RSA";
    // 算法签名常量 -- 签名
    private static final String SIGNATURE_ALGORITHM = "MD5withRSA";
    // 存储的公钥名称
    private static final String PUBLIC_KEY = "RSAPublicKey";
    // 存储的私钥名称
    private static final String PRIVATE_KEY = "RSAPrivateKey";

    /**
     * 初始化密钥
     * 利用非对称加密KeyPairGenerator类，根据指定的加密算法，生成密钥构造器对象，设置密钥构造器大小，生成密钥对
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static Map<String, Key> initKey() throws NoSuchAlgorithmException {
        // 根据指定算法，生成公钥/私钥对，返回KeyPairGemerator包含密钥信息的对象
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        // 初始化密钥的大小 1024bit
        keyPairGenerator.initialize(1024);
        // 生成密钥对
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        // 创建一个存储Key密钥的map对象存储密钥
        Map<String,Key> keyMap = new HashMap<>();
        // 存储公钥
        keyMap.put(PUBLIC_KEY,keyPair.getPublic());
        // 存储私钥
        keyMap.put(PRIVATE_KEY,keyPair.getPrivate());
        return keyMap;
    }

    /**
     * 将String字符串编码为Base64数组
     * @param key String
     * @return
     */
    private static byte[] decryptBASE64(String key) {
        return Base64.decodeBase64(key);
    }

    /**
     * 通过私钥解密
     *
     * @param data 需要私密解密的数据
     * @param key  私密
     * @return 使用私密解密后的数据 byte 类型
     * @throws NoSuchPaddingException    安全异常
     * @throws NoSuchAlgorithmException  当请求特定加密算法但在环境中不可用时，抛出此异常。
     * @throws IllegalBlockSizeException 安全异常
     * @throws BadPaddingException       安全异常
     * @throws InvalidKeyException       无效密钥（无效编码，错误长度，未初始化等）
     * @throws InvalidKeySpecException   无效密钥
     * @throws GeneralSecurityException  总异常
     */
    public static byte[] decryptByPrivateKey(String data, String key) throws GeneralSecurityException {
        return decryptByPrivateKey(decryptBASE64(data), key);
    }


    /**
     * 通过公钥加密
     *
     * @param data 需要公钥加密的数据
     * @param key  公钥
     * @return 使用公钥加密后的数据
     * @throws NoSuchPaddingException    安全异常
     * @throws NoSuchAlgorithmException  当请求特定加密算法但在环境中不可用时，抛出此异常。
     * @throws InvalidKeySpecException   无效密钥
     * @throws InvalidKeyException       无效密钥（无效编码，错误长度，未初始化等）
     * @throws BadPaddingException       安全异常
     * @throws IllegalBlockSizeException 安全异常
     */
    public static byte[] encryptByPublicKey(String data, String key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        Map<String, Object> map = publicKey(key);
        Cipher cipher = (Cipher) map.get("cipher");
        Key publicKey = (Key) map.get("publicKey");

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data.getBytes());
    }


    /**
     * 公钥加密解密相关
     *
     * @param key 公钥
     * @return Key + Cipher
     * @throws NoSuchAlgorithmException 当请求特定加密算法但在环境中不可用时，抛出此异常。
     * @throws InvalidKeySpecException  无效密钥
     * @throws NoSuchPaddingException   安全异常
     */
    public static Map<String, Object> publicKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException {

        Map<String, Object> map = new HashMap<>(2);

        // 对公钥解密
        byte[] keyBytes = decryptBASE64(key);

        // 取得公钥
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicKey = keyFactory.generatePublic(x509KeySpec);

        // 对数据加密
        String algorithm = keyFactory.getAlgorithm();
        Cipher cipher = Cipher.getInstance(algorithm);

        map.put("publicKey", publicKey);
        map.put("cipher", cipher);

        return map;
    }

    /**
     * 通过私钥解密
     *
     * @param data 需要私密解密的数据
     * @param key  私密
     * @return 私密解密后的数据 byte 类型
     * @throws NoSuchPaddingException    安全异常
     * @throws NoSuchAlgorithmException  当请求特定加密算法但在环境中不可用时，抛出此异常。
     *                                   如果没有Provider支持指定算法的KeyFactorySpi实现。
     * @throws InvalidKeySpecException   无效密钥
     * @throws InvalidKeyException       无效密钥（无效编码，错误长度，未初始化等）
     * @throws BadPaddingException       安全异常
     * @throws IllegalBlockSizeException 安全异常
     */
    public static byte[] decryptByPrivateKey(byte[] data, String key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        Map<String, Object> map = privateKey(key);
        Cipher cipher = (Cipher) map.get("cipher");
        Key privateKey = (Key) map.get("privateKey");

        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }


    /**
     * 私钥加密/解密相关
     *
     * @throws NoSuchAlgorithmException 当请求特定加密算法但在环境中不可用时，抛出此异常。
     *                                  如果没有Provider支持指定算法的KeyFactorySpi实现。
     * @throws InvalidKeySpecException  无效的密钥
     *                                  如果给定的密钥规范不适合此密钥工厂生成私钥。
     * @throws NoSuchPaddingException   安全异常
     */
    public static Map<String, Object> privateKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException {

        Map<String, Object> map = new HashMap<>(2);

        // 对密钥解密
        byte[] keyBytes = decryptBASE64(key);
        // 取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        // 对数据加密
        String algorithm = keyFactory.getAlgorithm();
        Cipher cipher = Cipher.getInstance(algorithm);

        map.put("privateKey", privateKey);
        map.put("cipher", cipher);

        return map;
    }

    // 避免每次解密重复生成Key对象
    public static PrivateKey getPrivateKeyObj(String privateKeyStr) throws Exception {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }
}

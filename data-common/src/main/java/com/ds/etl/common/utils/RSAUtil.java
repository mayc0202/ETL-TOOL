package com.ds.etl.common.utils;


import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Map;

@Slf4j
public final class RSAUtil {
    /**
     * 生成公钥密钥
     */
    private static Map<String,Key> key; // 存放密钥对
    private static String publicKey; // 公钥
    private static String privateKey; // 私钥

    /**
     * 固定的公钥密钥
     * 后期写在配置文件里面
     */
    private static String fixedPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCBYupdUTMX6LKiMZvRa3rxUDiE5oiKVWkEFE6oVvggrumLJr2iZwr5UsFjKjrr+9vIZrHQW6IjL0cJ4o3bs7UE51VHeTHxEUs6Sl0Q9xconreQC94X70tYj0feYk+NqkGOO+THLd9+BXtoh9gRB0Iv2E2VVYuWWC6rutQKJUTAkwIDAQAB";
    private static String fixedPrivateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAIFi6l1RMxfosqIxm9FrevFQOITmiIpVaQQUTqhW+CCu6YsmvaJnCvlSwWMqOuv728hmsdBboiMvRwnijduztQTnVUd5MfERSzpKXRD3Fyiet5AL3hfvS1iPR95iT42qQY475Mct334Fe2iH2BEHQi/YTZVVi5ZYLqu61AolRMCTAgMBAAECgYAlRZhb32tHhOoInR/U2RBmeYR+jGZi6y3AVhU+mycJSznNsxBHu0VHA1bgOScWGcx7U1H/wpXXauQc7d2Nn5wSbUVlUQKWMhiilL4ZEqivn9cw5Fk3iMRcwzoi4PFLhva1VQc2s2TgH9tgjjXeip5MKbRFS3lDaW8t3HamM0+8uQJBAOGUSSCbwr3n3RQLU3giB3f1NVRzk4TigH7jbWHLG1ZErVPdcPArS2qXTK/1DsAfvX9uVe26X7obk6qCdDEwCg8CQQCS1b75dtq+9lULPk+ps7m/u4YS/E2GW7J3tSmzx1vo6h75Z70nS35a5PiUSMDUm8/e6DztcaFTVlIXjYaTrvU9AkEAtMjtrneGpEHtuUG2fIHxMF9RhUQ3Rvlr98V3BTgMyZ+ytZK0D5bzExL8v8wLLUiCy1z2+tYyH+o39yAj1MPmWwJAfA7hso2r1Yn0YnJ8BgpWVtsONT48FdelmqRSnpVCXzBniRsP4oJTOGKab1ZkrX0TjOa0i3zk669T3phxapd4lQJBALYz/3ZRGC57S7eCysCwRC/BClcRHmL/4khMtjjPg5I1NI5p/iuMF7tCXI8oOagj6tESUuBX86l57WPQszkmKx8=";

    static {
        try {
            publicKey = fixedPublicKey;
            privateKey = fixedPrivateKey;

            key = RSAConfig.initKey();
        } catch (Exception e) {
            log.error("RSA initialization failed ", e);
        }
    }

    /**
     * 获取公钥
     * @return
     */
    public static String getRsaPublicKey() {
        return publicKey;
    }

    /**
     * 获取密钥
     * @return
     */
    public static String getRsaPrivateKey() {
        return privateKey;
    }

    /**
     * 非固定私钥解密
     * 私钥解密，返回字符串形式加密嫩肉
     * @param data
     * @return
     * @throws GeneralSecurityException
     */
    public static String decryptByPrivateKeytoString(String data) throws Exception {
        byte[] encryptedData = Base64.getDecoder().decode(data);
        int keySize = 1024; // 与密钥长度一致
        int maxBlockSize = keySize / 8;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, RSAConfig.getPrivateKeyObj(privateKey));

        int offset = 0;
        while (offset < encryptedData.length) {
            int inputLen = Math.min(encryptedData.length - offset, maxBlockSize);
            byte[] decryptedBlock = cipher.doFinal(encryptedData, offset, inputLen);
            out.write(decryptedBlock, 0, decryptedBlock.length);
            offset += inputLen;
        }
        return new String(out.toByteArray());
    }

    /**
     * 固定公钥加密
     * @param data
     * @return
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     */
    public static  String encryptByFixedPublicKeytoString(String data) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        return Base64.getEncoder().encodeToString(RSAConfig.encryptByPublicKey(data, fixedPublicKey));
    }

    /**
     * 固定私钥解密
     * @param data
     * @return
     */
    public static String decryptByFixedPrivateKeytoString(String data) {
        try {
            return new String(RSAConfig.decryptByPrivateKey(data, fixedPrivateKey));
        } catch (BadPaddingException e) {
            log.error("Decryption failed: Please check if the key matches | if the data is complete ");
            throw new RuntimeException("Decryption failed, please confirm using the correct private key ");
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Security handling exceptions ", e);
        }
    }
}

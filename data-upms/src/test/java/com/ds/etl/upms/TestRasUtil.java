package com.ds.etl.upms;


import com.ds.etl.common.utils.AESUtil;
import com.ds.etl.common.utils.RSAUtil;
import org.junit.Test;


/**
 * @Author: yc
 * @CreateTime: 2025-05-23
 * @Description:
 * @Version: 1.0
 */
//@SpringBootTest
public class TestRasUtil {

    @Test
    public void tests() throws Exception {
        String pwd = "1q2w3e4R";
        // RSA模拟前端加密
        String decryptPwd = RSAUtil.encryptByFixedPublicKeytoString(pwd);
        System.out.println(decryptPwd);

        // RSA解密
        String encryptPwd = RSAUtil.decryptByFixedPrivateKeytoString(decryptPwd);
        System.out.println(encryptPwd);

        // AES加密入库
        String decrypt = AESUtil.encrypt(encryptPwd, AESUtil.getAesKey());
        System.out.println(decrypt);
    }
}
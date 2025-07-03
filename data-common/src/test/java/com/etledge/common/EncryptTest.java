package com.etledge.common;

import com.etledge.common.utils.AESUtil;
import org.junit.Test;


public class EncryptTest {

    @Test
    public void encrypt() {
        String pwd = "1q2w3e4R";
        System.out.println(AESUtil.encrypt(pwd,AESUtil.getAesKey()));
    }
}

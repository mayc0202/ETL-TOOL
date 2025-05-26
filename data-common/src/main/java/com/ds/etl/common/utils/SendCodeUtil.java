package com.ds.etl.common.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * 发送验证码工具类
 */
public class SendCodeUtil {
    private static final Random random = new Random(); // 生成随对象

    private static final DecimalFormat FOUR_CODE = new DecimalFormat("0000"); // 四位验证码

    private static final DecimalFormat SIX_CODE = new DecimalFormat("000000"); // 六位验证码

    // get四位随机数
    public static String getFourBitRandom() {
        //               格式匹配          随机数范围
        return FOUR_CODE.format(random.nextInt(10000));
    }

    // get六位随机数
    public static String getSixBitRandom() {
        //               格式匹配          随机数范围
        return SIX_CODE.format(random.nextInt(1000000));
    }

    // 给定数组，抽取n个数据
    public static ArrayList getRandom(List list, int n) {
        Random random = new Random();
        HashMap<Object, Object> hashMap = new HashMap<>();
        // 生成随机数并存入hashMap中
        for (int i = 0; i < list.size(); i++) {
            int number = random.nextInt(100) + 1;
            hashMap.put(number, i);
        }
        // 从hashMap导入数据
        Object[] robjs = hashMap.values().toArray();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < n; i++) {
            arrayList.add(list.get((Integer) robjs[i]));
        }
        return arrayList;
    }
}

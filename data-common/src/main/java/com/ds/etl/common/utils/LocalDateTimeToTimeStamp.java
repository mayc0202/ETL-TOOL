package com.ds.etl.common.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 时间戳与localDatetime日期格式转换
 */
public class LocalDateTimeToTimeStamp {

    /*时间戳转成LocalDateTime*/
    public static LocalDateTime timeStampToLocalDateTime(long timeStamp) {
        Instant instant = Instant.ofEpochSecond(timeStamp);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    /*LocalDateTime转成时间戳*/
    public static long localDateTimeToTimeStamp(LocalDateTime time) {
        long timeStamp = time.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        return timeStamp;
    }

    /*LocalDateTime转字符串*/
    public static String getLocalDateTimeStr(LocalDateTime time) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String localTime = df.format(time);
        return localTime;
    }

    /*字符串转LocalDataTime*/
    public static LocalDateTime getLocalDateTime(String str) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime time = LocalDateTime.parse(str, df);
        return time;
    }
}

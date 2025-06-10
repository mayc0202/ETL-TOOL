package com.etledge.common.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @Author: yc
 * @CreateTime: 2025-06-11
 * @Description:
 * @Version: 1.0
 */
public class DataUtil {

    /**
     * Date to String
     * @param date
     * @return
     */
    public static String dateConvertString(Date date) {
        LocalDateTime ldt = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return ldt.format(formatter);
    }

}
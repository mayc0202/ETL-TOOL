package com.etledge.database.db.connector.ftp;

import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class DGPCopyStreamListener implements CopyStreamListener {
    long count = 0;
    long lastSize = 0;
    long lastTime = 0;
    long frequency = 5;//5秒显示一下进度
    private static final Logger logger = LoggerFactory.getLogger(DGPCopyStreamListener.class);

    @Override
    public void bytesTransferred(CopyStreamEvent event) {

    }

    @Override
    public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {

        long currentTime = System.currentTimeMillis();
        if (lastTime == 0) {
            lastTime = currentTime;
            lastSize = totalBytesTransferred;
        }

        if (currentTime - lastTime < frequency * 1000) {
            return;
        }

        long fileSize = streamSize;
        long finishSize = totalBytesTransferred;
        long rate = (negativeToZero(finishSize - lastSize) * 1000) / (currentTime - lastTime);


        lastTime = currentTime;
        lastSize = finishSize;
        logger.info(String.format("文件大小：%s, 已完成传输：%s, 当前速度：%s", pretty(fileSize), pretty(finishSize), pretty(rate) + "/s"));

        //System.out.println(String.format("文件大小：%s, 已完成传输：%s, 本次传输：%s",streamSize, totalBytesTransferred,bytesTransferred));
    }

    public static String pretty(long b) {

        BigDecimal b1 = new BigDecimal(b);

        if (b < 1024) {
            return b + " byte";
        } else if (b >= 1024 && b < 1024 * 1024) {
            return b1.divide(new BigDecimal(1024)).setScale(2, BigDecimal.ROUND_HALF_UP) + " KB";
        } else if (b >= 1024 * 1024 && b < 1024 * 1024 * 1024) {
            return b1.divide(new BigDecimal(1024 * 1024)).setScale(2, BigDecimal.ROUND_HALF_UP) + " MB";
        } else {
            return b1.divide(new BigDecimal(1024 * 1024 * 1024)).setScale(2, BigDecimal.ROUND_HALF_UP) + " GB";
        }
    }

    public static Long negativeToZero(Long l) {
        if (l <= 0) {
            return 0l;
        }

        return l;
    }

}

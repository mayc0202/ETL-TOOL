package com.etledge.upms;

import org.apache.dubbo.config.spring.context.annotation.DubboComponentScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author: yc
 * @CreateTime: 2025-05-17
 * @Description: UPMS
 * @Version: 1.0
 */
@DubboComponentScan(basePackages = "com.etledge.upms.user.service")
@SpringBootApplication
public class ETLUpmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ETLUpmsApplication.class,args);
    }
}
package com.etledge.database.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * Cross domain interceptor
 */
@Configuration
public class WebInterceptorConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Set cross domain paths allowed
        registry.addMapping("/**")
                // Set up cross domain request domain names
                .allowedOrigins("*")
//                .allowedOriginPatterns("*")
                // Set whether to allow certificates (Yes)
                .allowCredentials(true)
                // Set the allowed header properties (any)
                .allowedHeaders("*")
                // Set allowed methods
                .allowedMethods("PUT","GET","POST","DELETE");
    }
}

package com.etledge.upms.config.web;

import com.etledge.common.Constants;
import com.etledge.upms.config.intercepter.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;


/**
 * Cross domain interceptor
 */
@Configuration
public class WebInterceptorConfig implements WebMvcConfigurer {

    private final TokenInterceptor tokenInterceptor;

    // 从配置文件中读取排除路径
    @Value("#{'${security.interceptor.exclude-paths:}'.split(',')}")
    private List<String> excludePaths;

    @Value("${security.interceptor.enabled:true}")
    private boolean interceptorEnabled;

    @Autowired
    public WebInterceptorConfig(TokenInterceptor tokenInterceptor) {
        this.tokenInterceptor = tokenInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (interceptorEnabled) {
            registry.addInterceptor(tokenInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns(excludePaths)
                    .excludePathPatterns("/error")
                    .order(1);
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("PUT","GET","POST","DELETE","OPTIONS","PATCH")
                .allowedHeaders("*")
                .exposedHeaders(
                        Constants.ETL_EDGE_TOKEN_CONFIG.AUTHORIZATION,
                        Constants.ETL_EDGE_TOKEN_CONFIG.ETL_EDGE_TOKEN)
                .allowCredentials(true)
                .maxAge(3600);
    }
}

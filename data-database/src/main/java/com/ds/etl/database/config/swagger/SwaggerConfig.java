package com.ds.etl.database.config.swagger;

import com.google.common.collect.Sets;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.boot.SpringBootVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableOpenApi
public class SwaggerConfig {
    /** API 页面上半部分展示信息 */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("数据源模块接口文档")
                .description("Swagger-Ui")
                .contact(new Contact("Mayc", "#", "203373485@qq.com"))
                .version(
                        "Application Version: "
                                + "1.0.0"
                                + ", Spring Boot Version: "
                                + SpringBootVersion.getVersion())
                .build();
    }
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.OAS_30)
                .pathMapping("/")
                // 定义是否开启swagger，false为关闭，可以通过变量控制
                .enable(true)
                // 将api的元信息设置为包含在json ResourceListing响应中。
                .apiInfo(apiInfo())
                // 接口调试地址
                .host("http://localhost:8083")
                // 选择哪些接口作为swagger的doc发布
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.ds.etl.database"))
                .paths(PathSelectors.any())
                .build()
                // 支持的通讯协议集合
                .protocols(Sets.newHashSet("http", "https"))
                // 授权信息设置，必要的header token等认证信息
                .securitySchemes(securitySchemes())
                // 授权信息全局应用
                .securityContexts(securityContexts());
    }
    /** 设置授权信息 */
    private List securitySchemes() {
        ApiKey apiKey = new ApiKey("BASE_TOKEN", "token_mayc", In.HEADER.toValue());
        return Collections.singletonList(apiKey);
    }

    /** 授权信息全局应用 */
    private List securityContexts() {
        return Collections.singletonList(
                SecurityContext.builder()
                        .securityReferences(
                                Collections.singletonList(
                                        new SecurityReference(
                                                "BASE_TOKEN",
                                                new AuthorizationScope[] {new AuthorizationScope("global", "")})))
                        .build());
    }

    public void addInterceptors(InterceptorRegistry registry) {
        try {
            Field registrationsField =
                    FieldUtils.getField(InterceptorRegistry.class, "registrations", true);
            List<InterceptorRegistration> registrations =
                    (List<InterceptorRegistration>) ReflectionUtils.getField(registrationsField, registry);
            if (registrations != null) {
                for (InterceptorRegistration interceptorRegistration : registrations) {
                    interceptorRegistration
                            .excludePathPatterns("/swagger**/**")
                            .excludePathPatterns("/webjars/**")
                            .excludePathPatterns("/v3/**")
                            .excludePathPatterns("/doc.html");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


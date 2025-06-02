package com.etledge.collect.config.intercepter;

import com.alibaba.fastjson2.JSONObject;
import com.etledge.collect.config.jwt.JwtConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * token拦截器
 */
@Slf4j
@Component("tokenInterceptor")
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtConfig jwtConfig;

    //  ---- [拦截验证] ---- controller
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //跨域请求会首先发一个option请求，直接返回正常状态并通过拦截器
        if(request.getMethod().equals("OPTIONS")){
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        //获取到token
        String token = request.getHeader("token");
        System.out.println("拦截器获得token的值"+token);
        if (token!=null){
            boolean flag = jwtConfig.checkToken(token);
            if (flag){
                System.out.println("通过拦截器");
                return true;
            }
        }
        try {
            JSONObject json=new JSONObject();
            json.put("msg","token verify fail");
            json.put("code","500");
            response.getWriter().append(json.toString());
            System.out.println("认证失败，未通过拦截器>>>>>>>>>>>>>");
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}

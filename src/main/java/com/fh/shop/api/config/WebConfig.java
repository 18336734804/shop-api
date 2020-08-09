package com.fh.shop.api.config;

import com.fh.shop.api.annotation.Idempotent;
import com.fh.shop.api.interceptor.IdempotentInterceptor;
import com.fh.shop.api.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//等同于xml中的beans标签
@Configuration
public class WebConfig implements WebMvcConfigurer {
    //WebMvcConfigurer重写中的addInterceptors方法
    public void addInterceptors(InterceptorRegistry registry) {
        //拦截所有
        registry.addInterceptor(loginInterceptor()).addPathPatterns("/api/**");
        registry.addInterceptor(idempotentInterceptor()).addPathPatterns("/api/**");
    }

    //相当于xml 中bean标签 id默认为方法名字
    @Bean
    public LoginInterceptor loginInterceptor(){
        return new  LoginInterceptor();
    }
    @Bean
    public IdempotentInterceptor idempotentInterceptor(){
        return new IdempotentInterceptor();
    }
}

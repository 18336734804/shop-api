package com.fh.shop.api.interceptor;

import com.fh.shop.api.annotation.Idempotent;
import com.fh.shop.api.common.ResponseEnum;
import com.fh.shop.api.exception.IdempotentException;
import com.fh.shop.api.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public class IdempotentInterceptor extends HandlerInterceptorAdapter {


    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断是否需要幂等性
        HandlerMethod handlerMethod= (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        if(!method.isAnnotationPresent(Idempotent.class)){
            return true;
        }
        //判断token
        String token = request.getHeader("x-token");
        //token是否存在
        if (StringUtils.isEmpty(token)){
            throw new IdempotentException(ResponseEnum.TOKEN_IS_ONT);
        }
        //如果存在判断是否和redis中的一样
        boolean exists = RedisUtil.exists(token);
        if (!exists){
            throw new IdempotentException(ResponseEnum.TOKEN_FEIFA);
        }

        //删除token
        Long flag = RedisUtil.delete(token);
        //判断是否是第一次请求
        if (flag==0){
            throw new IdempotentException(ResponseEnum.TOKEN_CHONGFU);
        }
        return true;
    }
}

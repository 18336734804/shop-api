package com.fh.shop.api.interceptor;


import com.alibaba.fastjson.JSONObject;
import com.fh.shop.api.annotation.Check;
import com.fh.shop.api.common.ResponseEnum;
import com.fh.shop.api.common.SystemConstant;
import com.fh.shop.api.exception.GlobalException;
import com.fh.shop.api.member.vo.MemberVo;
import com.fh.shop.api.util.KeyUtil;
import com.fh.shop.api.util.MD5Util;
import com.fh.shop.api.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Base64;

public class LoginInterceptor extends HandlerInterceptorAdapter {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //允许跨域请求
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,"*");
        //允许自定义的头信息
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,"x-auth,content-type,x-token");
        //允许跨域请求的方法类型
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,"GET POST DELETE PUT");
        //获取请求方式
        String method1 = request.getMethod();
        if ("options".equalsIgnoreCase(method1)){
            //全部返回
            return false;
        }



        HandlerMethod handlerMethod= (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        if(!method.isAnnotationPresent(Check.class)){
            return true;
        }
        //=======拦截验证========
        //获取头信息
        //判断是否在头信息x-auth
        //验签
        //判断是否超时
        //续命
        //将用户信息放入request,方便以后查看
        //=====================
        //获取头信息
        String header = request.getHeader("x-auth");
        if (StringUtils.isEmpty(header)){
            throw  new GlobalException(ResponseEnum.LOGIN_HEARD_IS_MISS);
        }
        //分割 判断头信息是否完整
        String[] split = header.split("\\.");
        if (split.length!=2){
            throw  new GlobalException(ResponseEnum.LOGIN_HEARD_IS_BUWANZHENG);
        }
        //判断头信息是否一致
        String  memberJsonBaes64=split[0];
        String  signBaes64=split[1];
        String sign = MD5Util.sign(memberJsonBaes64, MD5Util.SECRET);
        String newSign = Base64.getEncoder().encodeToString(sign.getBytes("utf-8"));
        if (!signBaes64.equals(newSign)){
            throw  new GlobalException(ResponseEnum.LOGIN_HEARD_IS_BEICUAN);
        }
        //查看登陆是否过期
        //通过 new string(字节数组，编码格式) ；将字节转换为字符串
        String memberJson = new String(Base64.getDecoder().decode(memberJsonBaes64), "utf-8");
        MemberVo memberVo = JSONObject.parseObject(memberJson, MemberVo.class);
        Long id = memberVo.getId();
        String uuid = memberVo.getUuid();
        boolean exists = RedisUtil.exists(KeyUtil.buildMemberKey(uuid, id));
        if (!exists){
            throw  new GlobalException(ResponseEnum.LOGIN_TIME_CHAOSHI);
        }
        //续命
        RedisUtil.expire(KeyUtil.buildMemberKey(uuid,id),KeyUtil.MEMBER_KEY_GUOQI_TIME);
        //将用户信息放入request,方便后续使用
        request.setAttribute(SystemConstant.CURR_MEMBER,memberVo);
        return true;
    }

}

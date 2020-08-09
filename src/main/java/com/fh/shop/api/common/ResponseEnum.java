package com.fh.shop.api.common;

public enum ResponseEnum {

    TOKEN_IS_ONT(6000,"token不存在"),
    TOKEN_FEIFA(6001,"token错误"),
    TOKEN_CHONGFU(6002,"请求重复"),

    PAY_IS_FALL(5000,"未支付成功"),

    ORDER_STOCK_LESS(4000,"库存不足"),
    ORDER_CHAOSHI(4001,"订单在排队"),
    ORDER_IS_ERROR(4002,"订单创建失败"),


    GART_SHOP_IS_NOT(3000,"添加商品不存在"),
    GART_SHOP_STATUS_NOT(3001,"添加的商品没上架"),
    GART_SHOP_NUM_NOT(3002,"添加的商品数量不合法"),


    LOGIN_XINXI_IS_NULL(2000,"登录信息为空"),
    LOGIN_NAME_IS_NOT(2001,"会员名不存在"),
    LOGIN_PASSWORD_NOT(2002,"密码错误"),
    LOGIN_HEARD_IS_MISS(2003,"头信息丢失"),
    LOGIN_HEARD_IS_BUWANZHENG(2004,"头信息不完整"),
    LOGIN_HEARD_IS_BEICUAN(2005,"头信息别篡改"),
    LOGIN_TIME_CHAOSHI(2006,"登陆超时"),



    REG_Member_Is_NULL(1004,"信息为空"),
    GET_PHONE_IS_ESIST(1003,"手机号已存在"),
    GET_MAIL_IS_ESIST(1002,"邮箱已存在"),
    GET_MEMBERNAME_IS_ESIST(1001,"会员名已存在"),
   GET_MEMBER_IS_NULL(1000,"注册会员信息为空");

    private int code;
    private String msg;

    private ResponseEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}

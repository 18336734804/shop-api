package com.fh.shop.api.common;

public class SystemConstant {
    public static final String CURR_MEMBER="user";

    public static final int IS_STATUS=0;

    public interface OrderStock{
        int  WAIT_PAY=10;
        int  PAY_SUCCESS=20;
        int  SEND_GOODS=30;

    }
    public interface PayStatus{
        int  WAIT_PAY=10;
        int  PAY_SUCCESS=20;
        int  SEND_GOODS=30;

    }


}

package com.fh.shop.api.exception;

import com.fh.shop.api.common.ResponseEnum;

public class IdempotentException extends RuntimeException {

    private ResponseEnum responseEnum;

    public IdempotentException(ResponseEnum responseEnum){
        this.responseEnum=responseEnum;

    }

    public ResponseEnum getResponseEnum(){
        return responseEnum;
    }
}


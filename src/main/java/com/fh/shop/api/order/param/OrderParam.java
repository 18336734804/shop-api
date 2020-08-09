package com.fh.shop.api.order.param;

import lombok.Data;

@Data
public class OrderParam {
    private Long recipientId;

    private Long memberId;

    private int payType;
}

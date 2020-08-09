package com.fh.shop.api.paylog.op;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("t_paylog")
public class PayLog {
    @TableId(value = "out_trade_no",type = IdType.INPUT)
    private String outTradeNo;

    private String orderId;

    private Long userId;

    private String transactionId;

    private Date createTime;

    private Date payTime;

    private BigDecimal payMoney;

    private int payStatus;

    private int payType;


}

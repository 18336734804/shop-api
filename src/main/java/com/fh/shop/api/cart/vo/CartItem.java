package com.fh.shop.api.cart.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fh.shop.api.util.BigDecimalJackson;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItem {

    private Long goodsId;

    private String goodsName;
    @JsonSerialize(using = BigDecimalJackson.class)
    private BigDecimal price;

    private int num;

    private String imageUrl;
    @JsonSerialize(using = BigDecimalJackson.class)
    private BigDecimal subPrice;

}

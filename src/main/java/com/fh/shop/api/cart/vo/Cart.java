package com.fh.shop.api.cart.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fh.shop.api.util.BigDecimalJackson;
import com.fh.shop.api.util.BigDecimalUtil;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class Cart {
    private int totalNum;
    @JsonSerialize(using = BigDecimalJackson.class)
    private BigDecimal toralPrice;

    private List<CartItem> cartItemList=new ArrayList<>();
}

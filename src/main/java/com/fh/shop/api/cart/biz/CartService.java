package com.fh.shop.api.cart.biz;

import com.fh.shop.api.common.ServerResponse;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface CartService {
    ServerResponse addCart(Long memberId, Long goodsId, int num);

    ServerResponse findCart(Long memberId);

    ServerResponse deleteCartItem(Long memberId, Long goodsId);

    ServerResponse deleteAllCartItem(Long memberId, String ids);
}

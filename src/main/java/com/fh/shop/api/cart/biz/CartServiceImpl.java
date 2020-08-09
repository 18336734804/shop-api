package com.fh.shop.api.cart.biz;

import com.alibaba.fastjson.JSONObject;
import com.fh.shop.api.cart.vo.Cart;
import com.fh.shop.api.cart.vo.CartItem;
import com.fh.shop.api.common.ResponseEnum;
import com.fh.shop.api.common.ServerResponse;
import com.fh.shop.api.common.SystemConstant;
import com.fh.shop.api.product.mapper.ProductMapper;
import com.fh.shop.api.product.po.Product;
import com.fh.shop.api.util.BigDecimalUtil;
import com.fh.shop.api.util.KeyUtil;
import com.fh.shop.api.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private ProductMapper productMapper;

    @Override
    public ServerResponse addCart(Long memberId, Long goodsId, int num) {
        //判断商品是否存在
        Product product = productMapper.selectById(goodsId);
        if (product ==null){
            return ServerResponse.error(ResponseEnum.GART_SHOP_IS_NOT);
        }
        //判断商品的状态是否正常
        if (product.getStatus()== SystemConstant.IS_STATUS){
            return ServerResponse.error(ResponseEnum.GART_SHOP_STATUS_NOT);
        }

        //判断会员是否有购物车
        String cartKey = KeyUtil.buildCartKey(memberId);
        String cartJson = RedisUtil.get(cartKey);
        if (StringUtils.isNotEmpty(cartJson)){
            //如果有直接往购物车里添加商品
            Cart cart = JSONObject.parseObject(cartJson, Cart.class);
            List<CartItem> cartItemList = cart.getCartItemList();
            CartItem cartItem=null;
            for (CartItem item:cartItemList){
                if (item.getGoodsId().longValue()==goodsId.longValue()){
                    cartItem =item;
                    break;
                }
            }
            //判断商品是否已经在购物车里
            if (cartItem!=null){
                //商品已经存在 就更新商品的数量和小计  在更新购物车的总价和总个数
                //数量
                cartItem.setNum(cartItem.getNum()+num);
                int num1=  cartItem.getNum();
                //判断商品数量
                if (num1 <=0){
                    //当商品数量小于等于0 的时候从cartItemList删除这个商品
                    cartItemList.remove(cartItem);
                }else {
                    //小计
                    BigDecimal subPrice = BigDecimalUtil.num(num1 + "", product.getPrice().toString());
                    cartItem.setSubPrice(subPrice);
                }

                //更新购物车
                UpdateCart(memberId,cart);
                return ServerResponse.success(cart);
            }else {
                if (num<=0){
                    return ServerResponse.error(ResponseEnum.GART_SHOP_NUM_NOT);
                }
                //商品不存在  就添加商品  在更新购物车的总价和总个数
                //构建商品
                CartItem cartItemInfo = buildCartItem(num, product);
                //加入购物车
                cart.getCartItemList().add(cartItemInfo);
                //更新购物车
                UpdateCart(memberId,cart);
                return ServerResponse.success(cart);
            }

        }else {
            if (num<=0){
                return ServerResponse.error(ResponseEnum.GART_SHOP_NUM_NOT);
            }
            //如果会员没有对应的购物车p
            //创建购物车
            Cart cart=new Cart();
            //构建商品
            CartItem cartItemInfo = buildCartItem(num, product);
            //加入购物车
            cart.getCartItemList().add(cartItemInfo);
            //更新购物车
            UpdateCart(memberId,cart);
            return ServerResponse.success(cart);
        }


    }

    @Override
    public ServerResponse findCart(Long memberId) {
        String cartKey = KeyUtil.buildCartKey(memberId);
        String cartJson = RedisUtil.get(cartKey);
        Cart cart = JSONObject.parseObject(cartJson, Cart.class);
        return ServerResponse.success(cart);
    }

    @Override
    public ServerResponse deleteCartItem(Long memberId, Long goodsId) {
        String cartKey = KeyUtil.buildCartKey(memberId);
        String cartJson = RedisUtil.get(cartKey);
        Cart cart = JSONObject.parseObject(cartJson, Cart.class);
        List<CartItem> cartItemList = cart.getCartItemList();
        //CartItem cartItem=null;
        for (CartItem item:cartItemList){
            if (item.getGoodsId().longValue()==goodsId.longValue()){
                cartItemList.remove(item);
                break;
            }
        }
        UpdateCart(memberId,cart);
        return ServerResponse.success();
    }

    @Override
    public ServerResponse deleteAllCartItem(Long memberId, String ids) {
        String cartKey = KeyUtil.buildCartKey(memberId);
        String cartJson = RedisUtil.get(cartKey);
        Cart cart = JSONObject.parseObject(cartJson, Cart.class);
        List<CartItem> cartItemList = cart.getCartItemList();
        String[] idsArr = ids.split(",");

        for (String goodsId:idsArr){

            for (CartItem item:cartItemList){
                if (item.getGoodsId().toString().equals(goodsId)){
                    cartItemList.remove(item);
                    break;
                }
            }

        }
        UpdateCart(memberId,cart);
        return ServerResponse.success();
    }

    private CartItem buildCartItem(int num, Product product) {
        CartItem cartItemInfo = new CartItem();
        cartItemInfo.setGoodsId(product.getId());
        cartItemInfo.setGoodsName(product.getProductName());
        cartItemInfo.setImageUrl(product.getImage());
        cartItemInfo.setPrice(product.getPrice());
        cartItemInfo.setNum(num);
        BigDecimal subPrice = BigDecimalUtil.num(num + "", product.getPrice().toString());
        cartItemInfo.setSubPrice(subPrice);
        return cartItemInfo;
    }

    private void UpdateCart(Long memberId, Cart cart) {
        List<CartItem> cartItemList = cart.getCartItemList();
        //判断cartItemList是否有商品
        if (cartItemList.size()==0){
            //没有商品删掉购物车
            RedisUtil.delete(KeyUtil.buildCartKey(memberId));
            return;
        }
        int totalCount = 0;
        BigDecimal totalSubPrice = new BigDecimal(0);
        //更新购物车
        for (CartItem item : cartItemList) {
            totalCount += item.getNum();
            totalSubPrice = BigDecimalUtil.add(totalSubPrice.toString(), item.getSubPrice().toString());
        }
        cart.setToralPrice(totalSubPrice);
        cart.setTotalNum(totalCount);
        //更新redis
        String cartNewJson = JSONObject.toJSONString(cart);
        String cartKey = KeyUtil.buildCartKey(memberId);
        RedisUtil.set(cartKey, cartNewJson);
    }
}

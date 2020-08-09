package com.fh.shop.api.cart.controller;

import com.fh.shop.api.annotation.Check;
import com.fh.shop.api.cart.biz.CartService;
import com.fh.shop.api.common.ServerResponse;
import com.fh.shop.api.common.SystemConstant;
import com.fh.shop.api.member.vo.MemberVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/carts")
@Api(tags = "购物车接口")
public class CartController {

    @Autowired
    private CartService cartService;


    @PostMapping("/addCart")
    @Check
    @ApiOperation("给购物车中添加商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-auth",value = "头信息",type = "string",required = true,paramType = "header"),
            @ApiImplicitParam(name = "goodsId",value = "商品id",type = "long",required = true,paramType = "query"),
            @ApiImplicitParam(name = "num",value = "商品数量",type = "int",required = true,paramType = "query")

    })
    public ServerResponse addCart(HttpServletRequest request,Long goodsId,int num){
        MemberVo member = (MemberVo) request.getAttribute(SystemConstant.CURR_MEMBER);
        Long memberId = member.getId();
        return cartService.addCart(memberId,goodsId,num);
    }
    @Check
    @GetMapping("/findCart")
    @ApiOperation("查询指定用户的购物车")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-auth",value = "头信息",type = "string",required = true,paramType = "header"),

    })
    public ServerResponse findCart(HttpServletRequest request){
        MemberVo member = (MemberVo) request.getAttribute(SystemConstant.CURR_MEMBER);
        Long memberId = member.getId();
        return cartService.findCart(memberId);

    }
    @Check
    @PostMapping("/delete")
    @ApiOperation("删除购物车商品接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-auth",value = "头信息",type = "string",required = true,paramType = "header"),
            @ApiImplicitParam(name = "goodsId",value = "商品id",type = "long",required = true,paramType = "query")

    })
    public ServerResponse deleteCartItem(HttpServletRequest request,Long goodsId){
        MemberVo member = (MemberVo) request.getAttribute(SystemConstant.CURR_MEMBER);
        Long memberId = member.getId();
        return cartService.deleteCartItem(memberId,goodsId);
    }


    @Check
    @PostMapping("/deleteAll")
    @ApiOperation("批量删除购物车商品接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-auth",value = "头信息",type = "string",required = true,paramType = "header"),
            @ApiImplicitParam(name = "ids",value = "商品id用逗号分隔",type = "String",required = true,paramType = "query")

    })
    public ServerResponse deleteAllCartItem(HttpServletRequest request,String ids){
        MemberVo member = (MemberVo) request.getAttribute(SystemConstant.CURR_MEMBER);
        Long memberId = member.getId();
        return cartService.deleteAllCartItem(memberId,ids);
    }


}

package com.fh.shop.api.type.controller;

import com.fh.shop.api.annotation.Check;
import com.fh.shop.api.common.ServerResponse;
import com.fh.shop.api.type.biz.TypeService;
import com.fh.shop.api.type.po.Type;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/types")
@Api(tags = "分类接口")
public class TypeController {
    @Autowired
    private TypeService typeService;

    @GetMapping
    @ApiOperation("查询分类接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-auth",value = "头信息",type = "string",required = true,paramType = "header")
    })
    public ServerResponse queryTypeList(){
        List<Type> typeList = typeService.queryTypeList();
        return ServerResponse.success(typeList);
    }
}

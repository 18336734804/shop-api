package com.fh.shop.api.area.controller;

import com.fh.shop.api.annotation.Check;
import com.fh.shop.api.area.biz.AreaService;
import com.fh.shop.api.common.ServerResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/areas")
@Api(tags = "地区接口")
public class AreaController {

    @Autowired
    private AreaService areaService;

    @GetMapping
    @ApiOperation("根据父类id查询子类接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "父类id",type = "long",required = true,paramType = "query")
    })
    public ServerResponse findChrds(Long id){
        return areaService.findChrds(id);
    }
}

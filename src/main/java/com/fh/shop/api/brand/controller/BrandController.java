package com.fh.shop.api.brand.controller;

import com.fh.shop.api.annotation.Check;
import com.fh.shop.api.brand.biz.BrandService;
import com.fh.shop.api.brand.po.Brand;
import com.fh.shop.api.common.ServerResponse;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@Api(tags = "品牌接口")
public class BrandController {
    @Autowired
    private BrandService brandService;
    private List<Brand> brandList;

    @PostMapping
    @ApiOperation("添加品牌接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "brandName",value = "品牌名称",type = "string",required = true,paramType = "query")
    })
    public ServerResponse add(Brand brand){
        return brandService.addBrand(brand);
    }

    @GetMapping
    @ApiOperation("查询品牌信息接口")
    public ServerResponse query(){
        try {
            List<Brand> brandList= brandService.queryBrand();
          return   ServerResponse.success(brandList);
        } catch (Exception e) {
            e.printStackTrace();
            return ServerResponse.error();
        }
    }

    @DeleteMapping("/{brandId}")
    @ApiOperation("删除品牌接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "brandId",value = "品牌id",type = "long",required = true,paramType = "path")

    })
    public ServerResponse delete(@PathVariable("brandId") Long id) {
        return brandService.delete(id);
    }

    @PutMapping
    @ApiOperation("修改品牌接口")
    public ServerResponse update(@ApiParam(value = "修改品牌所需的json",required = true) @RequestBody Brand brand) {
        return brandService.update(brand);
    }

    @DeleteMapping
    @ApiOperation("批量删除品牌接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name="ids",value = "需要删除的id",type = "string",required = true,paramType = "query")
    })
    public ServerResponse deleteBatch(String ids) {
        return brandService.deleteBatch(ids);
    }
}

package com.fh.shop.api.product.biz;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fh.shop.api.util.RedisUtil;
import com.fh.shop.api.common.ServerResponse;

import com.fh.shop.api.product.mapper.ProductMapper;
import com.fh.shop.api.product.po.Product;
import com.fh.shop.api.product.vo.ProductVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService{
    @Autowired
    private ProductMapper productMapper;

    @Override
    public ServerResponse queryIsHot() {
        String hotProductList = RedisUtil.get("hotProductList");
        if (StringUtils.isNotEmpty(hotProductList)){
            List<ProductVo> productList = JSONObject.parseArray(hotProductList, ProductVo.class);
        return ServerResponse.success(productList);
        }
        QueryWrapper<Product> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("id","productName","price","image");
        queryWrapper.eq("isHot",1);
        queryWrapper.eq("status",1);
        List<Product> productList=productMapper.selectList(queryWrapper);
       List<ProductVo> productVoList=new ArrayList<>();
        for (Product product:productList){
            ProductVo productVo=new ProductVo();
            productVo.setId(product.getId());
            productVo.setImage(product.getImage());
            productVo.setProductName(product.getProductName());
            productVo.setPrice(product.getPrice().toString());
            productVoList.add(productVo);
        }
        String hotProductListJson = JSONObject.toJSONString(productVoList);
        RedisUtil.set("hotProductList",hotProductListJson);
        return ServerResponse.success(productVoList);
    }

    @Override
    public List<Product> queryStockLessProductList() {

        QueryWrapper<Product> queryWrapper=new QueryWrapper<>();
        queryWrapper.lt("stock",10);
        List<Product> productList = productMapper.selectList(queryWrapper);

        return productList;
    }
}

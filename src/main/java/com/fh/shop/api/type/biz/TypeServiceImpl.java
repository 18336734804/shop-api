package com.fh.shop.api.type.biz;

import com.alibaba.fastjson.JSONObject;
import com.fh.shop.api.annotation.Check;
import com.fh.shop.api.util.RedisUtil;
import com.fh.shop.api.type.mapper.TypeMapper;
import com.fh.shop.api.type.po.Type;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TypeServiceImpl implements TypeService{
    @Autowired
    private TypeMapper typeMapper;
    @Check
    @Override
    public List<Type> queryTypeList() {
        String hotTypeList = RedisUtil.get("hotTypeList");
        if (StringUtils.isNotEmpty(hotTypeList)){
            List<Type> types = JSONObject.parseArray(hotTypeList, Type.class);
            return types;
        }
        List<Type> typeList = typeMapper.selectList(null);
        String typeArr = JSONObject.toJSONString(typeList);
        RedisUtil.setEx("hotTypeList",typeArr,20*60);
        return typeList;
    }
}

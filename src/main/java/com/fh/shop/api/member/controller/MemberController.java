package com.fh.shop.api.member.controller;

import com.fh.shop.api.annotation.Check;
import com.fh.shop.api.common.ServerResponse;
import com.fh.shop.api.common.SystemConstant;
import com.fh.shop.api.member.biz.MemberService;
import com.fh.shop.api.member.po.Member;
import com.fh.shop.api.member.vo.MemberVo;
import com.fh.shop.api.util.KeyUtil;
import com.fh.shop.api.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/members")
@Api(tags="会员接口")
public class MemberController {
    @Autowired
    private MemberService memberService;


    @PostMapping
    @ApiOperation("会员注册")
    @ApiImplicitParams({
            @ApiImplicitParam(name ="memberName",value = "会员名",type = "string",required = true,paramType = "query"),
            @ApiImplicitParam(name ="password",value = "密码",type = "string",required = true,paramType = "query"),
            @ApiImplicitParam(name ="realName",value = "真实姓名",type = "string",required = true,paramType = "query"),
            @ApiImplicitParam(name ="birthday",value = "生日",type = "string",required = true,paramType = "query"),
            @ApiImplicitParam(name ="mail",value = "邮箱",type = "string",required = true,paramType = "query"),
            @ApiImplicitParam(name ="phone",value = "手机号",type = "string",required = true,paramType = "query"),
            @ApiImplicitParam(name ="shengId",value = "省id",type = "long",required = false,paramType = "query"),
            @ApiImplicitParam(name ="shiId",value = "市id",type = "long",required = false,paramType = "query"),
            @ApiImplicitParam(name ="xianId",value = "县id",type = "long",required = false,paramType = "query"),
            @ApiImplicitParam(name ="areaName",value = "地区名称",type = "string",required = false,paramType = "query"),
    })
    public ServerResponse addMember(Member member) throws Exception {

     return   memberService.addMember(member);

    }
    @PostMapping("login")
    @ApiOperation("会员登陆接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName",value = "会员名",type = "string",required = true,paramType = "query"),
            @ApiImplicitParam(name = "password",value = "密码",type = "string",required = true,paramType = "query")
    })
    public ServerResponse login(String userName,String password){
        return memberService.login(userName,password);

    }
    @GetMapping("/findMember")
    @Check
    @ApiOperation("获取会员信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-auth",value = "头信息",type = "string",required = true,paramType = "header")
    })
    public ServerResponse findMember(HttpServletRequest request){
        MemberVo memberVo = (MemberVo) request.getAttribute(SystemConstant.CURR_MEMBER);
        return ServerResponse.success(memberVo);
    }
    @GetMapping("/logout")
    @Check
    @ApiOperation("会员退出接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-auth",value = "头信息",type = "string",required = true,paramType = "header")
    })
    public ServerResponse logout(HttpServletRequest request){
        MemberVo memberVo = (MemberVo) request.getAttribute(SystemConstant.CURR_MEMBER);
        Long id = memberVo.getId();
        String uuid = memberVo.getUuid();
        RedisUtil.delete(KeyUtil.buildMemberKey(uuid,id));
        return ServerResponse.success();
    }

    @GetMapping("validaterMemName")
    @ApiOperation("检测会员名是否存在的接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "memberName",value = "会员名称",type = "string",required = true,paramType = "query")
    })
    public ServerResponse validaterMemName(String memberName) {
        return memberService.validaterMemName(memberName);

    }

    @GetMapping("validaterPhone")
    @ApiOperation("检测手机号是否存在的接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone",value = "手机号",type = "string",required = true,paramType = "query")
    })
    public ServerResponse validaterPhone(String phone) {
        return memberService.validaterPhone(phone);

    }

    @GetMapping("validaterEmail")
    @ApiOperation("检测邮箱是否存在的接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mail",value = "邮箱",type = "string",required = true,paramType = "query")
    })
    public ServerResponse validaterEmail(String mail) {
        return memberService.validaterEmail(mail);

    }


}

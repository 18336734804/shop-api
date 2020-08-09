package com.fh.shop.api.member.biz;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fh.shop.api.mq.MQMessage;
import com.fh.shop.api.mq.MQSender;
import com.fh.shop.api.util.*;
import com.fh.shop.api.common.ResponseEnum;
import com.fh.shop.api.common.ServerResponse;
import com.fh.shop.api.member.mapper.MemberMapper;
import com.fh.shop.api.member.po.Member;
import com.fh.shop.api.member.vo.MemberVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class MemberServiceImpl implements MemberService{
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private MailUtil mailUtil;
    @Autowired
    private MQSender mqSender;

    @Override
    public ServerResponse addMember(Member member) throws Exception {
        String memberName= member.getMemberName();
        String password= member.getPassword();
        String mail= member.getMail();
        String phone= member.getPhone();
        if (StringUtils.isEmpty(memberName) ||
                StringUtils.isEmpty(password)||
                StringUtils.isEmpty(mail)||
                StringUtils.isEmpty(phone)){
            return ServerResponse.error(ResponseEnum.GET_MEMBER_IS_NULL);
        }
        QueryWrapper<Member> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("memberName",memberName);
        Member member1=memberMapper.selectOne(queryWrapper);
        if (member1 != null){
            return ServerResponse.error(ResponseEnum.GET_MEMBERNAME_IS_ESIST);
        }
        QueryWrapper<Member> queryWrapper1=new QueryWrapper<>();
        queryWrapper1.eq("mail",mail);
        Member member2=memberMapper.selectOne(queryWrapper1);
        if (member2 != null){
            return ServerResponse.error(ResponseEnum.GET_MEMBERNAME_IS_ESIST);
        }
        QueryWrapper<Member> queryWrapper2=new QueryWrapper<>();
        queryWrapper2.eq("phone",phone);
        Member member3=memberMapper.selectOne(queryWrapper2);
        if (member3 != null){
            return ServerResponse.error(ResponseEnum.GET_MEMBERNAME_IS_ESIST);
        }

        memberMapper.addMember(member);

        mailUtil.DaoMail(mail,"注册成功","恭喜"+member.getRealName()+"成为我们的会员");
        return ServerResponse.success();
    }

    @Override
    public ServerResponse validaterMemName(String memberName) {
        if(StringUtils.isEmpty(memberName)){
            return ServerResponse.error(ResponseEnum.REG_Member_Is_NULL);
        }
        QueryWrapper<Member> objectQueryWrapper = new QueryWrapper();
        objectQueryWrapper.eq("memberName",memberName);
        Member member1 = memberMapper.selectOne(objectQueryWrapper);
        if(member1!=null){
            return ServerResponse.error(ResponseEnum.GET_MEMBERNAME_IS_ESIST);
        }
        return ServerResponse.success();
    }

    @Override
    public ServerResponse validaterEmail(String mail) {
        if(StringUtils.isEmpty(mail)){
            return ServerResponse.error(ResponseEnum.REG_Member_Is_NULL);
        }
        QueryWrapper<Member> objectQueryWrapper1 = new QueryWrapper();
        objectQueryWrapper1.eq("mail",mail);
        Member member2 = memberMapper.selectOne(objectQueryWrapper1);
        if(member2!=null){
            return ServerResponse.error(ResponseEnum.GET_MAIL_IS_ESIST);
        }
        return ServerResponse.success();
    }

    @Override
    public ServerResponse validaterPhone(String phone) {
        if(StringUtils.isEmpty(phone)){
            return ServerResponse.error(ResponseEnum.REG_Member_Is_NULL);
        }
        QueryWrapper<Member> objectQueryWrapper2 = new QueryWrapper();
        objectQueryWrapper2.eq("phone",phone);
        Member member3 = memberMapper.selectOne(objectQueryWrapper2);
        if(member3!=null){
            return ServerResponse.error(ResponseEnum.GET_PHONE_IS_ESIST);
        }
        return ServerResponse.success();
    }

    @Override
    public ServerResponse login(String userName, String password) {
        //判断用户信息是否为空
        if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(password)){
            return ServerResponse.error(ResponseEnum.LOGIN_XINXI_IS_NULL);
        }
        //判断用户是否存在
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("memberName",userName);
        Member member = memberMapper.selectOne(queryWrapper);
        if (member==null){
            return ServerResponse.error(ResponseEnum.LOGIN_NAME_IS_NOT);
        }
        //判断密码是否正确
        if (!password.equals(member.getPassword())){
            return ServerResponse.error(ResponseEnum.LOGIN_PASSWORD_NOT);
        }
        //=======生成token==========
        //模拟jwt(json web token)
        //生成token样子类似于xxx.uuu(用户信息。对应用户信息的签名)
        //签名的目的：保证用户信息不被篡改
        //生成签名：MD5(用户信息 结合 密钥)
        //sign代表签名 secret/secretKey 代表密钥
        //==============================
        //生成用户信息对应的json
        MemberVo memberVo = new MemberVo();
        Long memberId= member.getId();
        memberVo.setId(memberId);
        memberVo.setMemberName(member.getMemberName());
        memberVo.setRealName(member.getRealName());
        String uuid = UUID.randomUUID().toString();
        memberVo.setUuid(uuid);
        String memberJson = JSONObject.toJSONString(memberVo);

        try {
            //给用户信息base64编码
            String memberJsonBase64 = Base64.getEncoder().encodeToString(memberJson.getBytes("utf-8"));
            //生成用户信息对应的签名
            String sign = MD5Util.sign(memberJsonBase64, MD5Util.SECRET);
            //给用户信息对应的签名base64编码
            String signBase64 = Base64.getEncoder().encodeToString(sign.getBytes("utf-8"));
            //处理超时
            RedisUtil.setEx(KeyUtil.buildMemberKey(uuid,memberId),"",KeyUtil.MEMBER_KEY_GUOQI_TIME);
            //发送邮件
            MQMessage mqMassage=new MQMessage();
            mqMassage.setMail(member.getMail());
            mqMassage.setTitle("登陆成功");
            mqMassage.setContent(member.getRealName()+"登陆成功，登陆时间:"+ DateUtil.date2str(new Date(),DateUtil.FULL_TIME));
            String mailJson = JSONObject.toJSONString(mqMassage);
            mqSender.sendMail(mailJson);
            //给前台返回的数据
            return ServerResponse.success(memberJsonBase64+"."+signBase64);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            //throw new RuntimeException(e);
            return ServerResponse.error();
        }



    }

}

package com.fh.shop.api.pay.biz;

import com.alibaba.fastjson.JSONObject;
import com.fh.shop.api.common.ResponseEnum;
import com.fh.shop.api.common.ServerResponse;
import com.fh.shop.api.common.SystemConstant;
import com.fh.shop.api.config.WXConfig;
import com.fh.shop.api.order.mapper.OrderMapper;
import com.fh.shop.api.order.po.Order;
import com.fh.shop.api.paylog.mapper.PayLogMapper;
import com.fh.shop.api.paylog.op.PayLog;
import com.fh.shop.api.util.BigDecimalUtil;
import com.fh.shop.api.util.DateUtil;
import com.fh.shop.api.util.KeyUtil;
import com.fh.shop.api.util.RedisUtil;
import com.github.wxpay.sdk.WXPay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PayLogServiceImpl implements PayLogService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private PayLogMapper payLogMapper;

    @Override
    public ServerResponse createNative(Long memberId) {
        //中redis中获取支付日志的信息
        String payLogJson = RedisUtil.get(KeyUtil.buildPayLogKey(memberId));
        PayLog payLog = JSONObject.parseObject(payLogJson, PayLog.class);
        String outTradeNo = payLog.getOutTradeNo();
        BigDecimal payMoney = payLog.getPayMoney();
        String orderId = payLog.getOrderId();
        //调用微信接口进行统一下单
        WXConfig config = new WXConfig();
        try {
            WXPay wxpay = new WXPay(config);

            int money = BigDecimalUtil.num(payMoney.toString(), "100").intValue();
            Map<String, String> data = new HashMap<String, String>();
            data.put("body", "飞狐乐购-订单支付");
            data.put("out_trade_no", outTradeNo);
            data.put("total_fee", money+"");
            data.put("notify_url", "http://www.example.com/wxpay/notify");
            data.put("trade_type", "NATIVE");  // 此处指定为扫码支付
            String time = DateUtil.addMinutes(new Date(), 2, DateUtil.FULLTIMEINFO);
            data.put("time_expire",time);
            Map<String, String> resp = wxpay.unifiedOrder(data);
            String return_code = resp.get("return_code");
            String return_msg = resp.get("return_msg");
            if (!return_code.equals("SUCCESS")){
                return ServerResponse.error(99999,return_msg);
            }
            String result_code = resp.get("result_code");
            String err_code_des = resp.get("err_code_des");
            if (!result_code.equals("SUCCESS")){
                return ServerResponse.error(99999,err_code_des);
            }
            String code_url = resp.get("code_url");
            Map<String,String> map=new HashMap<>();
            map.put("codeUrl",code_url);
            map.put("orderId",orderId);
            map.put("totalPrice",payMoney.toString());
            return ServerResponse.success(map);

        } catch (Exception e) {
            e.printStackTrace();
            return ServerResponse.error();
        }



    }

    @Override
    public ServerResponse queryStatus(Long memberId) {
        String payLogJson = RedisUtil.get(KeyUtil.buildPayLogKey(memberId));
        PayLog payLog = JSONObject.parseObject(payLogJson, PayLog.class);
        String outTradeNo = payLog.getOutTradeNo();
        String orderId = payLog.getOrderId();
        WXConfig config = new WXConfig();
        try {
            WXPay wxpay = new WXPay(config);

            Map<String, String> data = new HashMap<String, String>();
            data.put("out_trade_no", outTradeNo);
            int count=0;
            while (true){
                Map<String, String> resp = wxpay.orderQuery(data);
                String return_code = resp.get("return_code");
                String return_msg = resp.get("return_msg");
                if (!return_code.equals("SUCCESS")){
                    return ServerResponse.error(99999,return_msg);
                }
                String result_code = resp.get("result_code");
                String err_code_des = resp.get("err_code_des");
                if (!result_code.equals("SUCCESS")){
                    return ServerResponse.error(99999,err_code_des);
                }
                String trade_state = resp.get("trade_state");
                if (trade_state.equals("SUCCESS")){
                    //证明支付成功了
                    String transaction_id_ = resp.get("transaction_id ");
                    //更新订单表
                    Order order = new Order();
                    order.setId(orderId);
                    order.setPayTime(new Date());
                    order.setStatus(SystemConstant.OrderStock.PAY_SUCCESS);
                    orderMapper.updateById(order);
                    //更新支付日志表
                    PayLog payLog1 = new PayLog();
                    payLog1.setOutTradeNo(outTradeNo);
                    payLog1.setPayStatus(SystemConstant.PayStatus.PAY_SUCCESS);
                    payLog1.setPayTime(new Date());
                    payLog1.setTransactionId(transaction_id_);
                    payLogMapper.updateById(payLog1);
                    //删除redis的支付日志
                    RedisUtil.delete(KeyUtil.buildPayLogKey(memberId));
                    return ServerResponse.success();
                }else {
                    //睡眠
                    Thread.sleep(2000);
                    count++;
                    if (count>60){
                        return ServerResponse.error(ResponseEnum.PAY_IS_FALL);
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            return ServerResponse.error();
        }


    }
}

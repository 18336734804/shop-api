package com.fh.shop.api.order.biz;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fh.shop.api.cart.vo.Cart;
import com.fh.shop.api.cart.vo.CartItem;
import com.fh.shop.api.common.ResponseEnum;
import com.fh.shop.api.common.ServerResponse;
import com.fh.shop.api.common.SystemConstant;
import com.fh.shop.api.config.MQConfig;
import com.fh.shop.api.order.mapper.OrderItemMapper;
import com.fh.shop.api.order.mapper.OrderMapper;
import com.fh.shop.api.order.param.OrderParam;
import com.fh.shop.api.order.po.Order;
import com.fh.shop.api.order.po.OrderItem;
import com.fh.shop.api.order.vo.OrderConfigVo;
import com.fh.shop.api.paylog.mapper.PayLogMapper;
import com.fh.shop.api.paylog.op.PayLog;
import com.fh.shop.api.product.mapper.ProductMapper;
import com.fh.shop.api.recipient.biz.IRecipientService;
import com.fh.shop.api.recipient.mapper.IRecipientMapper;
import com.fh.shop.api.recipient.po.Recipient;
import com.fh.shop.api.util.KeyUtil;
import com.fh.shop.api.util.RedisUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
      @Autowired
      private IRecipientService recipientService;
      @Autowired
      private RabbitTemplate rabbitTemplate;
      @Autowired
      private ProductMapper productMapper;
      @Autowired
      private IRecipientMapper recipientMapper;
      @Autowired
      private OrderMapper orderMapper;
      @Autowired
      private OrderItemMapper orderItemMapper;
      @Autowired
      private PayLogMapper payLogMapper;

    @Override
    public ServerResponse generateOrderConfirm(Long memberId) {
        List<Recipient> recipientList = recipientService.findList(memberId);
        String cartJson = RedisUtil.get(KeyUtil.buildCartKey(memberId));
        Cart cart = JSONObject.parseObject(cartJson, Cart.class);
        OrderConfigVo orderConfigVo = new OrderConfigVo();
        orderConfigVo.setCart(cart);
        orderConfigVo.setRecipientList(recipientList);
        return ServerResponse.success(orderConfigVo);
    }

    @Override
    public ServerResponse generateOrder(OrderParam orderParam) {
        Long memberId = orderParam.getMemberId();
        RedisUtil.delete(KeyUtil.buildOrderKey(memberId));
        RedisUtil.delete(KeyUtil.buildStockLessKey(memberId));
        RedisUtil.delete(KeyUtil.buildErrorKey(memberId));
        String orderParamJson = JSONObject.toJSONString(orderParam);
        rabbitTemplate.convertAndSend(MQConfig.ORDEREXCHANGE,MQConfig.LUYOUKEYORDER,orderParamJson);
        return ServerResponse.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(OrderParam orderParam) {
        Long memberId = orderParam.getMemberId();
        String cartJson = RedisUtil.get(KeyUtil.buildCartKey(memberId));
        Cart cart = JSONObject.parseObject(cartJson, Cart.class);
        List<CartItem> cartItemList = cart.getCartItemList();
        //减库存
        //update t_product set stock=stock-num where id=productId and stock>=num
        //考虑并发
        for (CartItem cartItem : cartItemList) {
            Long goodsId = cartItem.getGoodsId();
            int num = cartItem.getNum();
            int rowCount = productMapper.updateStock(goodsId, num);
            if (rowCount==0){
                //证明没有更新成功 ，库存不足
                //既要回滚事务也要提示
                throw  new SecurityException("stock less");

            }
        }
        //获取收件人信息
        Long recipientId = orderParam.getRecipientId();
        Recipient recipient = recipientMapper.selectById(recipientId);
        //插入订单表
        Order order = new Order();
        String orderId = IdWorker.getIdStr();
        order.setId(orderId);
        order.setCreateTime(new Date());
        order.setUserId(memberId);
        int payType=orderParam.getPayType();
        order.setPayType(payType);
        order.setTotalNum(cart.getTotalNum());
        BigDecimal toralPrice=cart.getToralPrice();
        order.setTotalPrice(toralPrice);
        order.setRecipientId(recipientId);
        order.setStatus(SystemConstant.OrderStock.WAIT_PAY);
        order.setRecipientor(recipient.getRecipientor());
        order.setPhone(recipient.getPhone());
        order.setMail(recipient.getMail());
        order.setAddress(recipient.getAddress());
        orderMapper.insert(order);
        //插入订单明细表
        //需要批量插入
        List<OrderItem> orderItemList=new ArrayList<>();
        for (CartItem cartItem : cartItemList) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setUserId(memberId);
            orderItem.setSubPrice(cartItem.getSubPrice());
            orderItem.setProductName(cartItem.getGoodsName());
            orderItem.setProductId(cartItem.getGoodsId());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setNum(cartItem.getNum());
            orderItem.setImagePath(cartItem.getImageUrl());
            orderItemList.add(orderItem);
        }
        //批量加入
        orderItemMapper.batchInsert(orderItemList);
        //给支付日志表加入信息
        PayLog payLog = new PayLog();
       String outTradeNo= IdWorker.getIdStr();
        payLog.setOutTradeNo(outTradeNo);
        payLog.setUserId(memberId);
        payLog.setPayType(payType);
        payLog.setPayMoney(toralPrice);
        payLog.setOrderId(orderId);
        payLog.setCreateTime(new Date());
        payLog.setPayStatus(SystemConstant.PayStatus.WAIT_PAY);
        payLogMapper.insert(payLog);
        //将支付日志存入redis中
        String payLogJson = JSONObject.toJSONString(payLog);
        RedisUtil.set(KeyUtil.buildPayLogKey(memberId),payLogJson);
        //删除购物车中的商品
        RedisUtil.delete(KeyUtil.buildCartKey(memberId));
        //订单提交成功
        RedisUtil.set(KeyUtil.buildOrderKey(memberId),"ok");


    }

    @Override
    public ServerResponse getResult(Long memberId) {
        if (RedisUtil.exists(KeyUtil.buildStockLessKey(memberId))){
            RedisUtil.delete(KeyUtil.buildStockLessKey(memberId));
            return ServerResponse.error(ResponseEnum.ORDER_STOCK_LESS);
        }
        if (RedisUtil.exists(KeyUtil.buildOrderKey(memberId))){
            RedisUtil.delete(KeyUtil.buildOrderKey(memberId));
            return ServerResponse.success();
        }
        if (RedisUtil.exists(KeyUtil.buildErrorKey(memberId))){
            RedisUtil.delete(KeyUtil.buildErrorKey(memberId));
            return ServerResponse.error(ResponseEnum.ORDER_IS_ERROR);
        }

        return ServerResponse.error(ResponseEnum.ORDER_CHAOSHI);
    }
}

package com.fh.shop.api.mq;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fh.shop.api.cart.vo.Cart;
import com.fh.shop.api.cart.vo.CartItem;
import com.fh.shop.api.config.MQConfig;
import com.fh.shop.api.exception.StockLessException;
import com.fh.shop.api.order.biz.OrderService;
import com.fh.shop.api.order.param.OrderParam;
import com.fh.shop.api.product.mapper.ProductMapper;
import com.fh.shop.api.product.po.Product;
import com.fh.shop.api.product.vo.ProductVo;
import com.fh.shop.api.util.KeyUtil;
import com.fh.shop.api.util.MailUtil;
import com.fh.shop.api.util.RedisUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.impl.AMQImpl;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MQReceiver {


    @Autowired
    private MailUtil mailUtil;

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = MQConfig.MAILQUEUE)
    public void handleMailMessage(String mail, Message message, Channel channel) throws IOException {
        MQMessage mqMessage = JSONObject.parseObject(mail, MQMessage.class);
        mailUtil.DaoMail(mqMessage.getMail(),mqMessage.getTitle(),mqMessage.getContent());
        MessageProperties messageProperties = message.getMessageProperties();
        long deliveryTag = messageProperties.getDeliveryTag();
        channel.basicAck(deliveryTag,false);
    }
    @RabbitListener(queues = MQConfig.ORDERQUEUE)
    public void handleOrderMessage(String msg, Message message, Channel channel) throws IOException {
        MessageProperties messageProperties = message.getMessageProperties();
        long deliveryTag = messageProperties.getDeliveryTag();
        //获取消息队列的消息
        OrderParam orderParam = JSONObject.parseObject(msg, OrderParam.class);
        Long memberId = orderParam.getMemberId();
        //获取redis中购物车的信息
        String cartJson = RedisUtil.get(KeyUtil.buildCartKey(memberId));
        Cart cart = JSONObject.parseObject(cartJson, Cart.class);
        //购买商品的数量和商品的库存作比较 提醒余额不足
        if (cart==null){
            //删除消息
            channel.basicAck(deliveryTag,false);
            return;
        }
        List<CartItem> cartItemList = cart.getCartItemList();
        //循环获取商品id
        List<Long> goodIdList = cartItemList.stream().map(x -> x.getGoodsId()).collect(Collectors.toList());
        //根据id集合查出数据库中对应的商品集合
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.in("id",goodIdList);
        List<Product> productList = productMapper.selectList(queryWrapper);
        //循环对比看看库存是否充足
        for (CartItem cartItem : cartItemList){
        for (Product product : productList) {
                if (cartItem.getGoodsId().longValue()==product.getId().longValue()){
                    if (cartItem.getNum() > product.getStock()){
                        //库存不足提醒
                        RedisUtil.set(KeyUtil.buildStockLessKey(memberId),"stock less");
                        //删除消息
                        channel.basicAck(deliveryTag,false);
                        return;
                    }
                }

            }
        }

        //创建订单
        try {
            orderService.createOrder(orderParam);
            channel.basicAck(deliveryTag,false);
        } catch (StockLessException e) {
            e.printStackTrace();
            //库存不足
            //提醒库存不足
            RedisUtil.set(KeyUtil.buildStockLessKey(memberId),"stock less");
            channel.basicAck(deliveryTag,false);
        } catch (Exception e){
            e.printStackTrace();
            RedisUtil.set(KeyUtil.buildErrorKey(memberId),"error");
            channel.basicAck(deliveryTag,false);

        }

    }
}

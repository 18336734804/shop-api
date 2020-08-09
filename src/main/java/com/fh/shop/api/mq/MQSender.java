package com.fh.shop.api.mq;

import com.fh.shop.api.config.MQConfig;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MQSender {

    @Autowired
    private AmqpTemplate amqpTemplate;


    public void sendMail(String mail){

        amqpTemplate.convertAndSend(MQConfig.MAILEXCHANGE,MQConfig.MAIL,mail);
    }

}

package com.fh.shop.api.mq;

import lombok.Data;

@Data
public class MQMessage {
    private String mail;

    private String title;

    private String content;
}

package com.bamboo.core.mq.bean;

import lombok.Data;

@Data
public class BookRequest {
    private String openid;
    private String userId;
    private String contractId;
}

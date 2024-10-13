package com.example.odyssey.common;

import lombok.Getter;

@Getter
public enum FunctionTopicEnum {

    TRANSFER("Transfer", "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef","转入");

    private String code;

    private String topic;

    private String name;

    FunctionTopicEnum(String code, String topic, String name) {
        this.code = code;
        this.topic = topic;
        this.name = name;
    }

    public static FunctionTopicEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (FunctionTopicEnum status : FunctionTopicEnum.values()) {
            if (code.equals(status.code)) {
                return status;
            }
        }
        return null;
    }
}

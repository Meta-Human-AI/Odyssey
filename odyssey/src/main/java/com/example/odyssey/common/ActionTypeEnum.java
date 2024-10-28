package com.example.odyssey.common;

import lombok.Getter;

@Getter
public enum ActionTypeEnum {
    BUY("buy", "购买"),

    AIRDROP("airdrop", "空投"),
    TRANSFER_OUT("transferOut", "转出"),

    TRANSFER_IN("transferIn", "转入");

    private String code;

    private String name;

    ActionTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ActionTypeEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (ActionTypeEnum status : ActionTypeEnum.values()) {
            if (code.equals(status.code)) {
                return status;
            }
        }
        return null;
    }
}

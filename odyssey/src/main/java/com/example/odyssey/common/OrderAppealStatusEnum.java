package com.example.odyssey.common;

import lombok.Getter;

@Getter
public enum OrderAppealStatusEnum {


    PENDING("pending", "待处理"),

    CANCEL("cancel", "已取消"),

    FINISH("finish", "已完成");

    private String code;

    private String name;

    OrderAppealStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static OrderAppealStatusEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (OrderAppealStatusEnum status : OrderAppealStatusEnum.values()) {
            if (code.equals(status.code)) {
                return status;
            }
        }
        return null;
    }
}

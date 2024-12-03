package com.example.odyssey.common;

import lombok.Getter;

@Getter
public enum OrderStatusEnum {

    AUTHENTICATION("authentication", "认证中"),

    EXAMINE("examine", "待审核"),

    PASS("pass", "审核通过"),

    REJECT("reject", "审核拒绝"),

    CANCEL("cancel", "已取消"),

    FAIL("fail", "预定失败"),

    FINISH("finish", "已完成");

    private String code;

    private String name;

    OrderStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static OrderStatusEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (OrderStatusEnum status : OrderStatusEnum.values()) {
            if (code.equals(status.code)) {
                return status;
            }
        }
        return null;
    }
}

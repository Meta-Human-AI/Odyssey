package com.example.odyssey.common;

import lombok.Getter;

@Getter
public enum OperatorStatusEnum {

    NORMAL("normal", "正常"),
    DISABLE("disable", "禁用");

    private final String code;
    private final String desc;

    OperatorStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OperatorStatusEnum getEnumByCode(String code) {
        for (OperatorStatusEnum value : OperatorStatusEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}

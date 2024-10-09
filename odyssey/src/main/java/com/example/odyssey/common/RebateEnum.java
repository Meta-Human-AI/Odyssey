package com.example.odyssey.common;

import lombok.Getter;
/**
 * 返佣类型
 */
@Getter
public enum RebateEnum {

    ODS("ods-payback", "挖矿"),

    USDT("usdt-payback", "交易");

    private String code;

    private String name;

    RebateEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static RebateEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (RebateEnum status : RebateEnum.values()) {
            if (code.equals(status.code)) {
                return status;
            }
        }
        return null;
    }
}

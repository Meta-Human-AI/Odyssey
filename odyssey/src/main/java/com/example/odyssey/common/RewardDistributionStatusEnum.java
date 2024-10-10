package com.example.odyssey.common;

import lombok.Getter;

@Getter
public enum RewardDistributionStatusEnum {

    UNISSUED("unissued", "未发放"),

    ISSUED("issued", "已发放");

    private String code;

    private String name;

    RewardDistributionStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static RewardDistributionStatusEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (RewardDistributionStatusEnum status : RewardDistributionStatusEnum.values()) {
            if (code.equals(status.code)) {
                return status;
            }
        }
        return null;
    }
}

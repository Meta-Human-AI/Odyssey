package com.example.odyssey.common;

import lombok.Getter;

@Getter
public enum RewardDistributionStatus {

    UNISSUED("unissued", "未发放"),

    ISSUED("issued", "已发放");

    private String code;

    private String name;

    RewardDistributionStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static RewardDistributionStatus of(String code) {
        if (code == null) {
            return null;
        }
        for (RewardDistributionStatus status : RewardDistributionStatus.values()) {
            if (code.equals(status.code)) {
                return status;
            }
        }
        return null;
    }
}

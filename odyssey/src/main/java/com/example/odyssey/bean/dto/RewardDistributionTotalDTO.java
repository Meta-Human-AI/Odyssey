package com.example.odyssey.bean.dto;

import lombok.Data;

@Data
public class RewardDistributionTotalDTO {
    /**
     * 钱包地址
     */
    private String address;
    /**
     * 奖励总数量
     */
    private String rewardTotalNumber;
    /**
     * 已发放奖励数量
     */
    private String rewardTotalIssuedNumber;
    /**
     * 未发放奖励数量
     */
    private String rewardTotalUnIssuedNumber;
    /**
     * 奖励类型
     */
    private String rewardType;
}
